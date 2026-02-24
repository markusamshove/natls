package org.amshove.natgen;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class OpenApiExtensions
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
}
