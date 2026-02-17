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

		var naturalType = switch(theType) {
			case STRING_TYPE -> schema.getMaxLength() != null
				? VariableType.alphanumeric(schema.getMaxLength())
				: VariableType.alphanumericDynamic();
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
				.addToBody(assignPrimitiveValue(theVariable, theType, currentPath));

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

	private IGeneratableStatement assignPrimitiveValue(Variable variable, String type, String currentPath)
	{
		if (STRING_TYPE.equals(type))
		{
			return assignment(variable, jsonValue);
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
