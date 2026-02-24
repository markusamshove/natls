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
}
