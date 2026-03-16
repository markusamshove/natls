package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.*;
import org.amshove.natgen.generatable.*;
import org.amshove.natgen.generatable.conditions.Conditions;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.NaturalOpenApi.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

public class CompressJsonFromOpenApiGenerator
{
	public static class Settings
	{
		@Nullable
		private Variable jsonSourceGroup;

		@Nullable
		private Variable jsonResultVariable;

		public void setJsonSourceGroup(@Nullable Variable group)
		{
			jsonSourceGroup = group;
		}

		public void setJsonResultVariable(@Nullable Variable jsonResultVariable)
		{
			this.jsonResultVariable = jsonResultVariable;
		}
	}

	private static final NaturalCode QUOTE = plain("H'22'");
	private static final NaturalCode COMMA = stringLiteral(",");
	private static final NaturalCode OBJECT_START = stringLiteral("{");
	private static final NaturalCode OBJECT_END = stringLiteral("}");
	private static final NaturalCode ARRAY_START = stringLiteral("[");
	private static final NaturalCode ARRAY_END = stringLiteral("]");

	private final OpenAPI spec;
	private final Settings settings;
	private Variable jsonResult;
	private CodeGenerationContext context;
	private String rootSchemaName;
	private boolean needsToSetDecimalSessionParameter;
	private final Map<Schema<?>, ArrayVariables> arrayVariablesBySchema = new HashMap<>();

	public CompressJsonFromOpenApiGenerator(OpenAPI spec, Settings settings)
	{
		this.spec = spec;
		this.settings = settings;
	}

	public CodeGenerationContext generate(String schemaName, Schema<?> schema)
	{
		rootSchemaName = schemaName;
		context = new CodeGenerationContext();
		jsonResult = Objects.requireNonNullElseGet(
			settings.jsonResultVariable,
			() -> context.addVariable(VariableScope.LOCAL, "##JSON-RESULT", VariableType.alphanumericDynamic())
		);

		var whereToAddJsonSourceVariablesTo = Objects.requireNonNullElse(settings.jsonSourceGroup, context);
		generateSchema(schemaName, schema, whereToAddJsonSourceVariablesTo, context);

		if (needsToSetDecimalSessionParameter)
		{
			setDecimalSessionParameter();
		}

		return context;
	}

	private void generateSchema(
		String name, Schema<?> schema, IVariableAddable parentGroup,
		IStatementAddable<?> currentStatementScope, IGeneratable... currentDimensions
	)
	{
		var isRootSchema = name.equals(rootSchemaName);

		if (!isRootSchema)
		{
			newCompress(currentStatementScope).withOperand(propertyNameColonOperand(name));
		}

		var theType = NaturalOpenApi.extractOpenApiType(schema, spec);
		if (theType.equals(OBJECT_TYPE))
		{
			generateObject(name, schema, parentGroup, currentStatementScope, true, currentDimensions);
			return;
		}

		var variableType = NaturalOpenApi.inferNaturalType(schema, spec);
		var propertyVariable = parentGroup.addVariable("#" + name, variableType);

		if (theType.equals("array"))
		{
			var iterationVariables = getIterationVariables(schema, name);
			newCompress(currentStatementScope).withOperand(ARRAY_START);

			var assignment = assignment(iterationVariables.size(), occ(propertyVariable));
			currentStatementScope.addStatement(assignment);
			var forLoop = _for(iterationVariables.iterator(), numberLiteral(1), iterationVariables.size());
			forLoop.addStatement(
				_if(Conditions.greaterThan(iterationVariables.iterator(), numberLiteral(1)))
					.addStatement(newCompressOutsideContext().withOperand(COMMA))
			);
			currentStatementScope.addStatement(forLoop);

			var arrayItemSchema = extractOpenApiType(schema.getItems(), spec);
			if (arrayItemSchema.equals(OBJECT_TYPE))
			{
				var resolvedObjectSchema = resolveSchema(schema.getItems(), spec);
				generateObject(name, resolvedObjectSchema, propertyVariable, forLoop, false, iterationVariables.iterator());
			}
			else
			{
				forLoop.addStatement(
					createCompressValueStatement(
						extractOpenApiType(schema.getItems(), spec), propertyVariable, schema,
						iterationVariables.iterator()
					)
				);
			}

			newCompress(currentStatementScope).withOperand(ARRAY_END);
			return;
		}

		currentStatementScope.addStatement(createCompressValueStatement(theType, propertyVariable, schema, currentDimensions));
	}

	/// If `assignObjectToProperty` is `true`, a new group variable is created for the object
	/// and the object itself is assigned to a property name, e.g. `{ "propertyname": {}}`
	/// If it is false, then the object itself won't have a property assignment, because it might be
	/// in an array `[ {}, {} ]`
	private void generateObject(
		String name, Schema<?> schema, IVariableAddable parentGroup,
		IStatementAddable<?> currentStatementScope, boolean assignObjectToProperty, IGeneratable... currentDimensions
	)
	{
		// If we do not need to assign to a property it means we're object literals {} within an array:
		// [ {}, {} ]
		// The parentGroup already is a group array for the objects
		var object = assignObjectToProperty
			? parentGroup.addVariable("#" + name, VariableType.group())
			: parentGroup;

		newCompress(currentStatementScope).withOperand(OBJECT_START);

		var propertyIterator = schema.getProperties().entrySet().iterator();
		while (propertyIterator.hasNext())
		{
			var property = propertyIterator.next();
			generateSchema(property.getKey(), resolveSchema(property.getValue(), spec), object, currentStatementScope, currentDimensions);
			if (propertyIterator.hasNext())
			{
				newCompress(currentStatementScope).withOperand(COMMA);
			}
		}
		newCompress(currentStatementScope).withOperand(OBJECT_END);
	}

	private IGeneratableStatement createCompressValueStatement(
		String type, Variable sourceVariable, Schema<?> schema,
		IGeneratable... dimensions
	)
	{
		var sourceAccess = dimensions.length > 0 ? sourceVariable.arrayAccess(dimensions) : sourceVariable;

		return switch (type)
		{
			case STRING_TYPE ->
			{
				if (isNullable(schema))
				{
					var ifStatement = _if(Conditions.equal(sourceAccess, stringLiteral(" ")));
					ifStatement.addStatement(newCompressOutsideContext().withOperand(stringLiteral("null")))
						._else()
						.addStatement(
							newCompressOutsideContext().withOperand(QUOTE).withOperand(sourceAccess)
								.withOperand(QUOTE)
						);
					yield ifStatement;
				}
				else
				{
					yield newCompressOutsideContext().withOperand(QUOTE).withOperand(sourceAccess).withOperand(QUOTE);
				}
			}
			case NUMBER_TYPE, INTEGER_TYPE ->
			{
				if (FLOAT_FORMAT.equals(schema.getFormat()) || DOUBLE_FORMAT.equals(schema.getFormat()))
				{
					needsToSetDecimalSessionParameter = true;
				}
				yield newCompressOutsideContext().numeric().withOperand(sourceAccess);
			}
			case BOOLEAN_TYPE -> newCompressOutsideContext().withOperand(NatGenFunctions.logicalToJsonBoolean(sourceAccess));
			default -> throw new IllegalStateException(
				"Unknown OpenAPI type <%s> to create a COMPRESS statement for values".formatted(type)
			);
		};
	}

	private void setDecimalSessionParameter()
	{
		var jsonDecimalSeparator = context.addVariable(VariableScope.LOCAL, "##C-DECIMAL-CHARACTER", VariableType.alphanumeric(1))
			.withConstantValue(stringLiteral("."));
		var previousDc = context.addVariable(VariableScope.LOCAL, "##PREVIOUS-DC", VariableType.alphanumeric(1));

		context.addStatementToFront(setGlobals("DC", jsonDecimalSeparator));
		context.addStatementToFront(
			assignment(previousDc, NatGenFunctions.retrieveCurrentDecimalPointCharacterFromSession())
		);

		context.addStatement(setGlobals("DC", previousDc));
	}

	private Compress newCompressOutsideContext()
	{
		return compress()
			.withOperand(jsonResult)
			.into(jsonResult)
			.leavingNoSpace();
	}

	private Compress newCompress(IStatementAddable statementScope)
	{
		var compress = newCompressOutsideContext();
		statementScope.addStatement(compress);
		return compress;
	}

	private static NaturalCode propertyNameColonOperand(String name)
	{
		return plain("H'22' '%s' H'22' ':'".formatted(name));
	}

	private ArrayVariables getIterationVariables(Schema<?> schema, String name)
	{
		return arrayVariablesBySchema.computeIfAbsent(schema, _ ->
		{
			var size = context.addVariable("#S-#" + name, VariableType.integer(4));
			var iterator = context.addVariable("#I-#" + name, VariableType.integer(4));

			return new ArrayVariables(size, iterator);
		});
	}

	private record ArrayVariables(Variable size, Variable iterator)
	{}
}
