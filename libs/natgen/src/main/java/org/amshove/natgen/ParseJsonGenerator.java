package org.amshove.natgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

public class ParseJsonGenerator
{
	private static final String JSON_SEPARATOR = "/";
	private static final String START_OBJECT = "<";
	private static final String END_OBJECT = ">";
	private static final String START_ARRAY = "(";
	private static final String END_ARRAY = ")";
	private static final String PARSED_DATA = "$";

	public String generate(String json)
	{
		var gson = new Gson();
		var rootElement = gson.fromJson(json, JsonElement.class);
		var parseBranches = parseDecideForJsonElement(rootElement, "");

		return """
			PARSE JSON ##RESPONSE-BODY INTO PATH ##JSON.##PATH NAME ##JSON.##NAME VALUE ##JSON.##VALUE GIVING ##JSON.##ERR SUBCODE ##JSON.##ERR-SUBCODE
			  DECIDE ON FIRST VALUE OF ##JSON.##PATH
			    %s
			    NONE
			      IGNORE
			  END-DECIDE
			END-PARSE
			""".formatted(parseBranches);
	}

	private static String parseDecideForJsonElement(JsonElement element, String currentJsonPath)
	{
		if (element.isJsonArray())
		{
			var elementPath = appendPath(currentJsonPath, START_ARRAY);
			var resizeBranch = """
				VALUE '%s'
				ADD 1 TO ##S-##ARR
				RESIZE ARRAY ##ANTWORT.##ARR TO (1:##S-##ARR)
				""".formatted(elementPath);
			var valueBranch = parseDecideForJsonElement(element.getAsJsonArray().get(0), elementPath);
			return """
				%s
				%s
				""".formatted(resizeBranch, valueBranch);
		}

		if (element.isJsonObject())
		{
			var elementPath = appendPath(currentJsonPath, START_OBJECT);
			var jsonObject = element.getAsJsonObject();
			var nestedBranches = new ArrayList<String>();
			for (var property : jsonObject.entrySet())
			{
				if (property.getValue().isJsonPrimitive())
				{
					nestedBranches.add(
						parseDecideForPrimitive(
							property.getKey(), property.getValue().getAsJsonPrimitive(),
							elementPath
						)
					);
				}
			}

			return String.join(System.lineSeparator(), nestedBranches);
		}

		throw new UnsupportedOperationException(
			"Unsupported element type: %s".formatted(element.getClass().getSimpleName())
		);
	}

	private static String parseDecideForPrimitive(
		String propertyName, JsonPrimitive propertyValue,
		String currentJsonPath
	)
	{
		var propertyNamePath = appendPath(currentJsonPath, propertyName);
		var valueJsonPath = appendPath(propertyNamePath, PARSED_DATA);
		return """
			VALUE '%s'
			  %s := %s
			""".formatted(valueJsonPath, propertyName, parseValueAssignment(propertyValue));
	}

	private static String parseValueAssignment(JsonPrimitive primitive)
	{
		if (primitive.isNumber())
		{
			return "VAL(##JSON.##VALUE)";
		}

		if (primitive.isString())
		{
			return "##JSON.##VALUE";
		}

		if (primitive.isBoolean())
		{
			return "ATOB(<##JSON.##VALUE>)";
		}

		throw new UnsupportedOperationException("Unknown json primitive: %s".formatted(primitive));
	}

	private static String appendPath(String currentPath, String newPathElement)
	{
		return "%s%s%s".formatted(currentPath, JSON_SEPARATOR, newPathElement);
	}
}
