package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.Locale;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

class ParseJsonFromOpenApiSchemaGenerator extends ParseJsonGenerator
{
	private final OpenAPI spec;
	private final Schema<?> schema;
	private final String rootSchemaName;

	protected ParseJsonFromOpenApiSchemaGenerator(OpenAPI spec, String schemaName, Schema<?> schema, Settings settings)
	{
		super(settings);
		this.spec = spec;
		this.rootSchemaName = schemaName;
		this.schema = schema;
	}

	@Override
	protected void generateInternal(CodeGenerationContext context, DecideOn decide)
	{
		createDecideOnBranch(decide, parsedJsonRoot, rootSchemaName, schema, "");
	}

	private void createDecideOnBranch(DecideOn decide, Variable parentVariable, String schemaName, Schema<?> schema, String currentPath)
	{
		var naturalSchemaName = "#" + schemaName.toUpperCase(Locale.ROOT);
		var theType = schema.getTypes().stream().findFirst().orElseThrow(() -> new IllegalStateException("Can not determine type from Schema %s".formatted(schema)));

		var currentSchemaPath = appendPath(currentPath, schemaName);
		var valueJsonPath = appendPath(currentSchemaPath, PARSED_DATA);

		if (STRING_TYPE.equals(theType) && DATE_TIME_FORMAT.equals(schema.getFormat()))
		{
			decideOnForDateTime(decide, parentVariable, schemaName, valueJsonPath);
			return;
		}

		var naturalType = switch(theType) {
			case STRING_TYPE -> switch (schema.getFormat()) {
				case DATE_FORMAT -> VariableType.date();
				case null, default -> schema.getMaxLength() != null
					? VariableType.alphanumeric(schema.getMaxLength())
					: VariableType.alphanumericDynamic();
			};
			case NUMBER_TYPE -> switch (schema.getFormat()) {
				case DOUBLE_FORMAT, FLOAT_FORMAT -> VariableType.floating(8);
				case null, default -> VariableType.integer(4);
			};
			case INTEGER_TYPE -> switch (schema.getFormat()) {
				case INTEGER64_FORMAT -> VariableType.numeric(8);
				case null, default -> VariableType.integer(4);
			};
			case BOOLEAN_TYPE -> VariableType.logical();
			default -> null;
		};

		if (naturalType != null)
		{
			var theVariable = parentVariable.addVariable(naturalSchemaName, naturalType);

			decide
				.addBranch(stringLiteral(valueJsonPath))
				.addToBody(assignPrimitiveValue(theVariable, theType, schema.getFormat(), currentPath));

			return;
		}

		if (theType.equals(OBJECT_TYPE))
		{
			var currentObjectPath = appendPath(currentPath, START_OBJECT);
			var group = parentVariable.addVariable(naturalSchemaName, VariableType.group());
			for (var property : schema.getProperties().entrySet())
			{
				createDecideOnBranch(decide, group, property.getKey(), property.getValue(), currentObjectPath);
			}

			return;
		}
	}

	private void decideOnForDateTime(DecideOn decide, Variable parentVariable, String schemaName, String valueJsonPath)
	{
		var naturalBaseName = "#" + schemaName.toUpperCase(Locale.ROOT);
		var parsingBaseVariable = parentVariable
			.addVariable(naturalBaseName, VariableType.alphanumeric(20)); // RFC 3339 5.6, e.g. 2017-07-21T17:32:28Z

		var redefinition = parsingBaseVariable.newRedefine();

		var dateParsingPart = redefinition.addVariable(naturalBaseName + "-DATEPART", VariableType.alphanumeric(10));
		redefinition.withFiller(1); // T
		var timeParsingPart = redefinition.addVariable(naturalBaseName + "-TIMEPART", VariableType.alphanumeric(8));

		var targetDate = parentVariable
			.addVariable(naturalBaseName + "-DATE", VariableType.date());
		var targetTime = parentVariable
			.addVariable(naturalBaseName + "-TIME", VariableType.time());

		decide
			.addBranch(stringLiteral(valueJsonPath))
			.addToBody(assignment(parsingBaseVariable, jsonValue))
			.addToBody(moveEdited(dateParsingPart, targetDate, "YYYY-MM-DD"))
			.addToBody(moveEdited(timeParsingPart, targetTime, "HH:II:SS"));
	}

	private IGeneratableStatement assignPrimitiveValue(Variable variable, String type, String format, String currentPath)
	{
		if (STRING_TYPE.equals(type))
		{
			return switch(format) {
				case DATE_FORMAT -> moveEdited(jsonValue, variable, "YYYY-MM-DD");
				case null, default -> assignment(variable, jsonValue);
			};
		}

		if (NUMBER_TYPE.equals(type) || INTEGER_TYPE.equals(type))
		{
			return assignment(variable, val(jsonValue));
		}

		if (BOOLEAN_TYPE.equals(type))
		{
			return assignment(variable, functionCall("ATOB", jsonValue));
		}

		throw new UnsupportedOperationException("No value assignment implemented for type <%s>".formatted(type));
	}

}
