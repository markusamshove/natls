package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.Dimension;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.NatGenFunctions;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.Locale;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.NaturalOpenApi.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

class ParseJsonFromOpenApiSchemaGenerator extends ParseJsonGenerator
{
	private final OpenAPI spec;
	private final Schema<?> rootSchema;
	private final String rootSchemaName;

	protected ParseJsonFromOpenApiSchemaGenerator(OpenAPI spec, String schemaName, Settings settings)
	{
		this(spec, schemaName, spec.getComponents().getSchemas().get(schemaName), settings);
	}

	protected ParseJsonFromOpenApiSchemaGenerator(OpenAPI spec, String schemaName, Schema<?> schema, Settings settings)
	{
		super(settings);
		this.spec = spec;
		this.rootSchemaName = schemaName;
		this.rootSchema = schema;
	}

	@Override
	protected void generateInternal(CodeGenerationContext context, DecideOn decide)
	{
		createDecideOnBranch(decide, parsedJsonRoot, rootSchemaName, rootSchema, "");
	}

	private void createDecideOnBranch(
		DecideOn decide, Variable parentVariable, String schemaName, Schema<?> schema,
		String currentPath
	)
	{
		if (schema.get$ref() != null)
		{
			var referencedComponent = findSchemaByReference(schema.get$ref(), spec);
			createDecideOnBranch(decide, parentVariable, schemaName, referencedComponent, currentPath);
			return;
		}

		var naturalSchemaName = "#" + schemaName.toUpperCase(Locale.ROOT);
		var theType = extractOpenApiType(schema, spec);

		// The root schema gets embedded, meaning that the schema name will not be
		// included in the JSON path of naturals `PARSE JSON`
		var currentSchemaPath = schemaName.equals(rootSchemaName)
			? currentPath
			: appendPath(currentPath, schemaName);

		if (theType.equals(OBJECT_TYPE))
		{
			// If the current parent variable is an array we want to pass it down.
			// This happens when an object (this) is within an array. The object
			// definition (natural group) should be the array, not each single property
			var nextParentVariable = parentVariable.type().isArray()
				? parentVariable
				: parentVariable.addVariable(naturalSchemaName, VariableType.group());

			var currentObjectPath = appendPath(currentSchemaPath, START_OBJECT);
			for (var property : schema.getProperties().entrySet())
			{
				createDecideOnBranch(
					decide, nextParentVariable, property.getKey(), property.getValue(),
					currentObjectPath
				);
			}

			return;
		}

		if (theType.equals("array"))
		{
			var arrayItemSchema = resolveSchema(schema.getItems(), spec);
			var openApiTypeOfItems = extractOpenApiType(arrayItemSchema, spec);
			var arrayVariable = getVariableForProperty(
				currentSchemaPath, parentVariable, schemaName,
				inferNaturalType(arrayItemSchema, spec)
			);
			arrayVariable.type().withDimension(Dimension.upperUnbound());
			var sizeVariable = findSizeVariableForArray(arrayVariable);

			var arrayStartPath = appendPath(currentSchemaPath, START_ARRAY);
			var newArrayValuePath = openApiTypeOfItems.equals(OBJECT_TYPE)
				? appendPath(arrayStartPath, START_OBJECT) // Array expansion needs to happen when a new object starts
				: appendPath(arrayStartPath, PARSED_DATA); // Array expansion needs to happen on every new primitive value

			var numberOfDimensions = getNumberOfDimensions(arrayStartPath);
			var branch = decide
				.addBranch(stringLiteral(newArrayValuePath))
				.addStatement(incrementVariable(sizeVariable))
				.addStatement(expandNthArrayDimension(arrayVariable, numberOfDimensions, sizeVariable));

			if (openApiTypeOfItems.equals(OBJECT_TYPE))
			{
				createDecideOnBranch(decide, arrayVariable, schemaName, arrayItemSchema, arrayStartPath);
			}
			else
			{
				branch.addStatement(
					assignValueToVariable(
						arrayVariable, extractOpenApiType(arrayItemSchema, spec),
						arrayItemSchema.getFormat(), newArrayValuePath
					)
				);
			}

			return;
		}

		// Primitive values

		var valueJsonPath = appendPath(currentSchemaPath, PARSED_DATA);

		if (STRING_TYPE.equals(theType) && DATE_TIME_FORMAT.equals(schema.getFormat()))
		{
			decideOnForDateTime(decide, parentVariable, schemaName, valueJsonPath);
			return;
		}

		var naturalType = inferNaturalType(schema, spec);

		if (naturalType != null)
		{
			var theVariable = parentVariable.addVariable(naturalSchemaName, naturalType);

			decide
				.addBranch(stringLiteral(valueJsonPath))
				.addStatement(assignValueToVariable(theVariable, theType, schema.getFormat(), currentPath));

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
			.addStatement(assignment(parsingBaseVariable, jsonValue))
			.addStatement(moveEdited(dateParsingPart, targetDate, "YYYY-MM-DD"))
			.addStatement(moveEdited(timeParsingPart, targetTime, "HH:II:SS"));
	}

	private IGeneratableStatement assignValueToVariable(Variable variable, String type, String format,
		String currentPath)
	{

		var variableAccess = variable.arrayAccess(findAllArrayAccessVariablesForCurrentPathInOrder(currentPath));

		if (STRING_TYPE.equals(type))
		{
			return switch (format)
			{
				case DATE_FORMAT -> moveEdited(jsonValue, variableAccess, "YYYY-MM-DD");
				case null, default -> assignment(variableAccess, primitiveValueAssignment(type));
			};
		}

		return assignment(variableAccess, primitiveValueAssignment(type));
	}

	private IGeneratable primitiveValueAssignment(String type)
	{
		if (STRING_TYPE.equals(type))
		{
			return jsonValue;
		}

		if (NUMBER_TYPE.equals(type) || INTEGER_TYPE.equals(type))
		{
			return val(jsonValue);
		}

		if (BOOLEAN_TYPE.equals(type))
		{
			return NatGenFunctions.jsonBooleanToLogical(jsonValue);
		}

		throw new UnsupportedOperationException("No value assignment implemented for type <%s>".formatted(type));
	}

}
