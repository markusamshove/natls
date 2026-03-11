package org.amshove.natgen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import static io.swagger.v3.parser.util.SchemaTypeUtil.*;
import static io.swagger.v3.parser.util.SchemaTypeUtil.BINARY_FORMAT;
import static io.swagger.v3.parser.util.SchemaTypeUtil.BOOLEAN_TYPE;
import static io.swagger.v3.parser.util.SchemaTypeUtil.BYTE_FORMAT;
import static io.swagger.v3.parser.util.SchemaTypeUtil.DOUBLE_FORMAT;
import static io.swagger.v3.parser.util.SchemaTypeUtil.FLOAT_FORMAT;
import static io.swagger.v3.parser.util.SchemaTypeUtil.INTEGER64_FORMAT;
import static io.swagger.v3.parser.util.SchemaTypeUtil.INTEGER_TYPE;
import static io.swagger.v3.parser.util.SchemaTypeUtil.NUMBER_TYPE;
import static io.swagger.v3.parser.util.SchemaTypeUtil.OBJECT_TYPE;

/// Common extension methods for OpenAPI schemas in the context of Natural
public class NaturalOpenApi
{
	public static Schema<?> findSchemaByReference(String reference, OpenAPI spec)
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

	/// Return the `schema` if it is already resolved (e.g. no `ref` set), otherwise
	/// resolve the reference.
	public static Schema<?> resolveSchema(Schema<?> schema, OpenAPI spec)
	{
		if (schema.get$ref() != null)
		{
			return findSchemaByReference(schema.get$ref(), spec);
		}

		return schema;
	}

	/// Resolve a name for the given schema. If schema has a `$ref`, then the name will be extracted.
	/// If not, the `defaultName` will be returned.
	public static String resolveSchemaName(Schema<?> schema, String defaultName)
	{
		if (schema.get$ref() != null)
		{
			var splitReference = schema.get$ref().split("/");
			return splitReference[splitReference.length - 1];
		}

		return defaultName;
	}

	/// Infer the Natural [VariableType] for the given [Schema].
	/// If the [Schema] is a reference, the reference will be resolved.
	public static VariableType inferNaturalType(Schema<?> schema, OpenAPI spec)
	{
		if (schema.get$ref() != null)
		{
			return inferNaturalType(findSchemaByReference(schema.get$ref(), spec), spec);
		}

		var firstType = extractOpenApiType(schema, spec);
		return switch (firstType)
		{
			case STRING_TYPE -> switch (schema.getFormat())
			{
				case DATE_FORMAT -> VariableType.date();
				case UUID_FORMAT -> VariableType.alphanumeric(36);
				case BINARY_FORMAT -> VariableType.binaryDynamic();
				case BYTE_FORMAT -> VariableType.alphanumericDynamic();
				case null, default -> schema.getMaxLength() != null
					? VariableType.alphanumeric(schema.getMaxLength())
					: VariableType.alphanumericDynamic();
			};
			case NUMBER_TYPE -> switch (schema.getFormat())
			{
				case DOUBLE_FORMAT, FLOAT_FORMAT -> VariableType.numeric(12.7);
				case null, default -> VariableType.integer(4);
			};
			case INTEGER_TYPE -> switch (schema.getFormat())
			{
				case INTEGER64_FORMAT -> VariableType.numeric(8);
				case null, default -> VariableType.integer(4);
			};
			case BOOLEAN_TYPE -> VariableType.logical();
			case OBJECT_TYPE -> VariableType.group();
			default -> null;
		};
	}

	/// Extract the OpenApi type (e.g. `number`) for the give [Schema] which is an array.
	public static String extractOpenApiType(Schema<?> schema, OpenAPI spec)
	{
		var resolvedSchema = resolveSchema(schema, spec);
		return resolvedSchema.getTypes().stream().filter(t -> !"null".equals(t)).findFirst().orElseThrow(
			() -> new IllegalStateException("Can not extract type from Schema %s".formatted(resolvedSchema))
		);
	}

	/// Check if the given [Schema] can contain null values.
	public static boolean isNullable(Schema<?> schema)
	{
		if (schema.getNullable() != null)
		{
			return schema.getNullable();
		}

		if (schema.get$ref() != null && schema.getAnyOf() == null)
		{
			return false;
		}

		return schema.getTypes().contains("null");
	}
}
