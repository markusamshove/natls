package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.*;
import org.amshove.natgen.generatable.Compress;
import org.amshove.natgen.generatable.NatGenFunctions;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.conditions.Conditions;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.NaturalOpenApi.isNullable;
import static org.amshove.natgen.NaturalOpenApi.resolveSchema;
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
		generateSchema(schemaName, schema, whereToAddJsonSourceVariablesTo);

		if (needsToSetDecimalSessionParameter)
		{
			setDecimalSessionParameter();
		}

		return context;
	}

	private void generateSchema(String name, Schema<?> schema, IVariableAddable parentGroup)
	{
		var isRootSchema = name.equals(rootSchemaName);

		if (!isRootSchema)
		{
			newCompress().withOperand(propertyNameColonOperand(name));
		}

		var theType = NaturalOpenApi.extractOpenApiType(schema, spec);
		if (theType.equals(OBJECT_TYPE))
		{
			var object = parentGroup.addVariable("#" + name, VariableType.group());

			newCompress().withOperand(OBJECT_START);

			var propertyIterator = schema.getProperties().entrySet().iterator();
			while (propertyIterator.hasNext())
			{
				var property = propertyIterator.next();
				generateSchema(property.getKey(), resolveSchema(property.getValue(), spec), object);
				if (propertyIterator.hasNext())
				{
					newCompress().withOperand(COMMA);
				}
			}
			newCompress().withOperand(OBJECT_END);

			return;
		}

		var variableType = NaturalOpenApi.inferNaturalType(schema, spec);
		var propertyVariable = parentGroup.addVariable("#" + name, variableType);

		switch (theType)
		{
			case STRING_TYPE ->
			{
				if (isNullable(schema))
				{
					var ifStatement = _if(Conditions.equal(propertyVariable, stringLiteral(" ")));
					ifStatement.addToBody(newCompressOutsideContext().withOperand(stringLiteral("null")))
						._else()
						.addToBody(newCompressOutsideContext().withOperand(QUOTE).withOperand(propertyVariable).withOperand(QUOTE));
					context.addStatement(ifStatement);
				}
				else
				{
					newCompress().withOperand(QUOTE).withOperand(propertyVariable).withOperand(QUOTE);
				}
			}
			case NUMBER_TYPE, INTEGER_TYPE ->
			{
				if (FLOAT_FORMAT.equals(schema.getFormat()) || DOUBLE_FORMAT.equals(schema.getFormat()))
				{
					needsToSetDecimalSessionParameter = true;
				}
				newCompress().numeric().withOperand(propertyVariable);
			}
			case BOOLEAN_TYPE -> newCompress().withOperand(NatGenFunctions.logicalToJsonBoolean(propertyVariable));
		}
	}

	private void setDecimalSessionParameter()
	{
		var jsonDecimalSeparator = context.addVariable(VariableScope.LOCAL, "##C-DECIMAL-CHARACTER", VariableType.alphanumeric(1)).withConstantValue(stringLiteral("."));
		var previousDc = context.addVariable(VariableScope.LOCAL, "##PREVIOUS-DC", VariableType.alphanumeric(1));

		context.addStatementToFront(setGlobals("DC", jsonDecimalSeparator));
		context.addStatementToFront(assignment(previousDc, NatGenFunctions.retrieveCurrentDecimalPointCharacterFromSession()));

		context.addStatement(setGlobals("DC", previousDc));
	}

	private Compress newCompressOutsideContext()
	{
		return compress()
			.withOperand(jsonResult)
			.into(jsonResult)
			.leavingNoSpace();
	}

	private Compress newCompress()
	{
		var compress = newCompressOutsideContext();
		context.addStatement(compress);
		return compress;
	}

	private static NaturalCode propertyNameColonOperand(String name)
	{
		return plain("H'22' '%s' H'22' ':'".formatted(name));
	}
}
