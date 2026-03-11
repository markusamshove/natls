package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.*;
import org.amshove.natgen.generatable.Compress;
import org.amshove.natgen.generatable.NatGenFunctions;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

public class CompressJsonFromOpenApiGenerator
{
	public static class Settings
	{
		@Nullable
		private Variable jsonSourceGroup;

		void setJsonSourceGroup(@Nullable Variable group)
		{
			jsonSourceGroup = group;
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
	private boolean needsToSetDecimalSessionParameter;

	public CompressJsonFromOpenApiGenerator(OpenAPI spec, Settings settings)
	{
		this.spec = spec;
		this.settings = settings;
	}

	public CodeGenerationContext generate(String schemaName, Schema<?> schema)
	{
		context = new CodeGenerationContext();
		jsonResult = context.addVariable(VariableScope.LOCAL, "##JSON-RESULT", VariableType.alphanumericDynamic());

		var whereToAddJsonSourceVariablesTo = Objects.requireNonNullElse(settings.jsonSourceGroup, context);
		generateSchema(schemaName, schema, whereToAddJsonSourceVariablesTo);

		if (needsToSetDecimalSessionParameter)
		{
			setDecimalSessionParameter();
		}

		return context;
	}

	private void setDecimalSessionParameter()
	{
		var sessionParameter = context.addVariable(VariableScope.LOCAL, "##SESSION-PARAMETER", VariableType.alphanumeric(253).withDimension(Dimension.upperBound(4)));
		var previousDc = sessionParameter.newRedefine().withFiller(70).addVariable("##PREVIOUS-DC", VariableType.alphanumeric(1));

		var jsonDecimalSeparator = context.addVariable(VariableScope.LOCAL, "##C-DECIMAL-CHARACTER", VariableType.alphanumeric(1)).withConstantValue(stringLiteral("."));

		context.addStatement(plainStatement("CALLNAT 'USR1005N' ##SESSION-PARAMETER(*)"));
		context.addStatement(plainStatement("SET GLOBALS DC=##C-DECIMAL-CHARACTER"));
		context.addStatement(plainStatement("SET GLOBALS DC=##PREVIOUS-DC"));
	}

	private void generateSchema(String name, Schema<?> schema, IVariableAddable parentGroup)
	{
		var theType = NaturalOpenApi.extractOpenApiType(schema, spec);
		if (theType.equals(OBJECT_TYPE))
		{
			var object = parentGroup.addVariable("#" + name, VariableType.group());
			newCompress().withOperand(OBJECT_START);

			var propertyIterator = schema.getProperties().entrySet().iterator();
			while (propertyIterator.hasNext())
			{
				var property = propertyIterator.next();
				generateSchema(property.getKey(), property.getValue(), object);
				if (propertyIterator.hasNext())
				{
					newCompress().withOperand(COMMA);
				}
			}
			newCompress().withOperand(OBJECT_END);

			return;
		}

		newCompress().withOperand(propertyNameColonOperand(name));

		var variableType = NaturalOpenApi.inferNaturalType(schema, spec);
		var propertyVariable = parentGroup.addVariable("#" + name, variableType);

		switch (theType)
		{
			case STRING_TYPE -> newCompress().withOperand(QUOTE).withOperand(propertyVariable).withOperand(QUOTE);
			case NUMBER_TYPE, INTEGER_TYPE ->
			{
				if (FLOAT_FORMAT.equals(schema.getFormat()))
				{
					needsToSetDecimalSessionParameter = true;
				}
				newCompress().numeric().withOperand(propertyVariable);
			}
			case BOOLEAN_TYPE -> newCompress().withOperand(NatGenFunctions.logicalToJsonBoolean(propertyVariable));
		}
	}

	private Compress newCompress()
	{
		var compress = compress()
			.withOperand(jsonResult)
			.into(jsonResult)
			.leavingNoSpace();
		context.addStatement(compress);
		return compress;
	}

	private NaturalCode propertyNameColonOperand(String name)
	{
		return plain("H'22' '%s' H'22' ':'".formatted(name));
	}
}
