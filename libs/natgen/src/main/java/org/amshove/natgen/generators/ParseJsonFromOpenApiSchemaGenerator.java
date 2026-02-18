package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.Dimension;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.Locale;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

class ParseJsonFromOpenApiSchemaGenerator extends ParseJsonGenerator
{
	private final OpenAPI spec;
	private final Schema<?> rootSchema;
	private final String rootSchemaName;

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

	private void createDecideOnBranch(DecideOn decide, Variable parentVariable, String schemaName, Schema<?> schema, String currentPath)
	{
		if (schema.get$ref() != null)
		{
			var referencedComponent = findSchemaByReference(schema.get$ref());
			createDecideOnBranch(decide, parentVariable, schemaName, referencedComponent, currentPath);
			return;
		}

		var naturalSchemaName = "#" + schemaName.toUpperCase(Locale.ROOT);
		var theType = extractOpenApiType(schema);

		var currentSchemaPath = appendPath(currentPath, schemaName);
		if (theType.equals(OBJECT_TYPE))
		{
			// The root schema gets embedded, meaning that the object name will not be
			// included in the JSON path of naturals `PARSE JSON`
			var objectBasePath = schemaName.equals(rootSchemaName) ? currentPath : currentSchemaPath;

			// If the current parent variable is an array we want to pass it down.
			// This happens when an object (this) is within an array. The object
			// definition (natural group) should be the array, not each single property
			var nextParentVariable = parentVariable.type().isArray()
				? parentVariable
				: parentVariable.addVariable(naturalSchemaName, VariableType.group());

			var currentObjectPath = appendPath(objectBasePath, START_OBJECT);
			for (var property : schema.getProperties().entrySet())
			{
				createDecideOnBranch(decide, nextParentVariable, property.getKey(), property.getValue(), currentObjectPath);
			}

			return;
		}

		if (theType.equals("array"))
		{
			var arrayItemSchema = schema.getItems();
			var openApiTypeOfItems = extractOpenApiType(arrayItemSchema);
			var arrayVariable = getVariableForProperty(currentSchemaPath, parentVariable, schemaName, inferNaturalType(arrayItemSchema));
			arrayVariable.type().withDimension(Dimension.upperUnbound());
			var sizeVariable = findSizeVariableForArray(arrayVariable);

			var arrayStartPath = appendPath(currentSchemaPath, START_ARRAY);
			var newArrayValuePath = openApiTypeOfItems.equals(OBJECT_TYPE)
				? appendPath(arrayStartPath, START_OBJECT) // Array expansion needs to happen when a new object starts
				: appendPath(arrayStartPath, PARSED_DATA); // Array expansion needs to happen on every new primitive value

			var numberOfDimensions = getNumberOfDimensions(arrayStartPath);
			var branch = decide
				.addBranch(stringLiteral(newArrayValuePath))
				.addToBody(incrementVariable(sizeVariable))
				.addToBody(expandNthArrayDimension(arrayVariable, numberOfDimensions, sizeVariable));

			if (openApiTypeOfItems.equals(OBJECT_TYPE))
			{
				createDecideOnBranch(decide, arrayVariable, schemaName, arrayItemSchema, arrayStartPath);
			}
			else
			{
				branch.addToBody(
					assignValueToVariable(
						arrayVariable, extractOpenApiType(arrayItemSchema),
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

		var naturalType = inferNaturalType(schema);

		if (naturalType != null)
		{
			var theVariable = parentVariable.addVariable(naturalSchemaName, naturalType);

			decide
				.addBranch(stringLiteral(valueJsonPath))
				.addToBody(assignValueToVariable(theVariable, theType, schema.getFormat(), currentPath));

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

	private IGeneratableStatement assignValueToVariable(Variable variable, String type, String format, String currentPath)
	{
		if (currentPath.contains(START_ARRAY))
		{
			var arrayAccessVariables = findAllArrayAccessVariablesForCurrentPathInOrder(currentPath);
			return assignment(variable.arrayAccess(arrayAccessVariables), primitiveValueAssignment(type));
		}

		if (STRING_TYPE.equals(type))
		{
			return switch(format) {
				case DATE_FORMAT -> moveEdited(jsonValue, variable, "YYYY-MM-DD");
				case null, default -> assignment(variable, primitiveValueAssignment(type));
			};
		}

		return assignment(variable, primitiveValueAssignment(type));
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
			return functionCall("ATOB", jsonValue);
		}

		throw new UnsupportedOperationException("No value assignment implemented for type <%s>".formatted(type));
	}

	private VariableType inferNaturalType(Schema<?> schema)
	{
		if (schema.get$ref() != null)
		{
			return inferNaturalType(findSchemaByReference(schema.get$ref()));
		}

		var firstType = extractOpenApiType(schema);
		return switch(firstType) {
			case STRING_TYPE -> switch (schema.getFormat()) {
				case DATE_FORMAT -> VariableType.date();
				case UUID_FORMAT -> VariableType.alphanumeric(36);
				case BINARY_FORMAT -> VariableType.binaryDynamic();
				case BYTE_FORMAT -> VariableType.alphanumericDynamic();
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
			case OBJECT_TYPE -> VariableType.group();
			default -> null;
		};
	}

	private String extractOpenApiType(Schema<?> schema)
	{
		if (schema.get$ref() != null)
		{
			return extractOpenApiType(findSchemaByReference(schema.get$ref()));
		}

		return schema.getTypes().stream().findFirst().orElseThrow(() -> new IllegalStateException("Can not extract type from Schema %s".formatted(schema)));
	}

	private Schema<?> findSchemaByReference(String reference)
	{
		// Since we always flatten the spec, we assume we're looking
		// for a component.
		var splitReference = reference.split("/");
		var componentKey = splitReference[splitReference.length - 1];
		var referencedComponentSchema = spec.getComponents().getSchemas().get(componentKey);
		if (referencedComponentSchema == null)
		{
			throw new IllegalStateException("Can not find referenced component with reference <%s>".formatted(reference));
		}

		return referencedComponentSchema;
	}
}
