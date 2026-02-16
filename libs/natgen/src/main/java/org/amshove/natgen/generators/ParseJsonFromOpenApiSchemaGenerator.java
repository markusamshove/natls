package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.Locale;

import static io.swagger.v3.parser.util.SchemaTypeUtil.OBJECT_TYPE;
import static io.swagger.v3.parser.util.SchemaTypeUtil.STRING_TYPE;
import static org.amshove.natgen.generatable.NaturalCode.assignment;
import static org.amshove.natgen.generatable.NaturalCode.stringLiteral;

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

		if (theType.equals(STRING_TYPE))
		{
			var naturalType = schema.getMaxLength() != null ? VariableType.alphanumeric(schema.getMaxLength()) : VariableType.alphanumericDynamic();
			var theVariable = parentVariable.addVariable(naturalSchemaName, naturalType);
			var valueJsonPath = appendPath(currentPath, PARSED_DATA);

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

		return null;
	}

}
