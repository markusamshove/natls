package org.amshove.natgen.generators;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.amshove.natgen.generatable.NaturalCode.*;

public class ParseJsonFromJsonGenerator
{
	private static final String JSON_SEPARATOR = "/";
	private static final String START_OBJECT = "<";
	//	private static final String END_OBJECT = ">";
	private static final String START_ARRAY = "(";
	//	private static final String END_ARRAY = ")";
	private static final String PARSED_DATA = "$";
	private Variable jsonPath;
	private Variable jsonValue;
	private Variable jsonErrCode;
	private Variable jsonErrSubcode;
	private Variable parsedJsonRoot;

	private final Map<String, Variable> variablesByJsonPath = new HashMap<>();
	private final Map<Variable, Variable> arraySizeVariablesByJsonPath = new HashMap<>();

	public CodeGenerationContext generate(String json)
	{
		var gson = new Gson();
		var rootElement = gson.fromJson(json, JsonElement.class);

		var context = new CodeGenerationContext();
		var jsonVariableGroup = context.addVariable(new Variable(1, VariableScope.LOCAL, "##JSON-PARSING", VariableType.group()));
		jsonPath = jsonVariableGroup.addVariable("#PATH", VariableType.alphanumericDynamic());
		jsonValue = jsonVariableGroup.addVariable("#VALUE", VariableType.alphanumericDynamic());
		jsonErrCode = jsonVariableGroup.addVariable("#ERR-CODE", VariableType.integer(4));
		jsonErrSubcode = jsonVariableGroup.addVariable("#ERR-SUBCODE", VariableType.integer(4));
		parsedJsonRoot = context.addVariable(VariableScope.LOCAL, "##PARSED-JSON", VariableType.group());

		var jsonSourceVariable = context.addVariable(VariableScope.LOCAL, "#JSON-SOURCE", VariableType.alphanumericDynamic());
		var decideOnJsonPath = decideOnFirst(jsonPath);
		var parseJsonStatement = parseJson(jsonSourceVariable)
			.intoPath(jsonPath)
			.intoValue(jsonValue)
			.givingErrorCode(jsonErrCode)
			.givingErrorSubcode(jsonErrSubcode)
			.addToBody(decideOnJsonPath);

		createDecideOnJsonElementBranches(decideOnJsonPath, rootElement, "");

		context.addStatement(parseJsonStatement);
		return context;
	}

	private void createDecideOnJsonElementBranches(
		DecideOn decideStatement, JsonElement currentElement,
		String currentPath
	)
	{
		if (currentElement.isJsonArray())
		{
			// TODO: What if root JSON is an array?
		}

		if (currentElement.isJsonObject())
		{
			var currentObjectPath = appendPath(currentPath, START_OBJECT);
			var jsonObject = currentElement.getAsJsonObject();
			for (var property : jsonObject.entrySet())
			{
				// TODO: Nested objects/arrays?
				if (property.getValue().isJsonPrimitive())
				{
					createPrimitiveValueBranch(
						decideStatement, property.getKey(),
						property.getValue().getAsJsonPrimitive(), currentObjectPath
					);
				}
				else
					if (property.getValue().isJsonArray())
					{
						// This is the primitive path
						var propertyNamePath = appendPath(currentObjectPath, property.getKey());
						var arrayVariable = getVariableForProperty(propertyNamePath, property.getKey(), property.getValue());
						var sizeVariable = getSizeVariableForArray(arrayVariable);

						var arrayStartPath = appendPath(propertyNamePath, START_ARRAY);
						var newArrayValuePath = appendPath(arrayStartPath, PARSED_DATA);
						decideStatement
							.addBranch(stringLiteral(newArrayValuePath))
							.addToBody(incrementVariable(sizeVariable))
							.addToBody(expandArray(arrayVariable, sizeVariable))
							.addToBody(
								assignment(
									arrayVariable.arrayAccess(sizeVariable),
									valueAssignment(property.getValue().getAsJsonArray().get(0).getAsJsonPrimitive())
								)
							);
					}
			}
		}

		//		throw new UnsupportedOperationException(
		//			"Unsupported element type: %s".formatted(currentElement.getClass().getSimpleName())
		//		);
	}

	private void createPrimitiveValueBranch(
		DecideOn statement, String propertyName, JsonPrimitive primitive,
		String currentPath
	)
	{
		var propertyNamePath = appendPath(currentPath, propertyName);
		var valueJsonPath = appendPath(propertyNamePath, PARSED_DATA);

		var parsedVariable = getVariableForProperty(propertyNamePath, propertyName, primitive);

		statement
			.addBranch(stringLiteral(valueJsonPath))
			.addToBody(assignment(parsedVariable, valueAssignment(primitive)));
	}

	private IGeneratable valueAssignment(JsonPrimitive primitive)
	{
		if (primitive.isNumber())
		{
			return val(jsonValue);
		}

		if (primitive.isString())
		{
			return jsonValue;
		}

		if (primitive.isBoolean())
		{
			return functionCall("ATOB", jsonValue);
		}

		throw new UnsupportedOperationException("Unknown json primitive: %s".formatted(primitive));
	}

	private static String appendPath(String currentPath, String newPathElement)
	{
		if (currentPath.isEmpty())
		{
			return newPathElement;
		}

		return "%s%s%s".formatted(currentPath, JSON_SEPARATOR, newPathElement);
	}

	private Variable getVariableForProperty(String propertyNamePath, String propertyName, JsonElement property)
	{
		return variablesByJsonPath.computeIfAbsent(propertyNamePath, _ ->
		{
			var type = inferJsonType(property);
			return parsedJsonRoot.addVariable("#" + propertyName.toUpperCase(Locale.ROOT), type);
		});
	}

	private Variable getSizeVariableForArray(Variable array)
	{
		return arraySizeVariablesByJsonPath.computeIfAbsent(array, _ -> parsedJsonRoot.addVariable("#S-" + array.name(), VariableType.integer(4)));
	}

	private static VariableType inferJsonType(JsonElement element)
	{
		if (element.isJsonPrimitive())
		{
			var primitive = element.getAsJsonPrimitive();
			if (primitive.isBoolean())
			{
				return VariableType.logical();
			}

			if (primitive.isNumber())
			{
				return VariableType.numeric(12.7);
			}

			return VariableType.alphanumericDynamic();
		}

		if (element.isJsonObject())
		{
			return VariableType.group();
		}

		if (element.isJsonArray())
		{
			var array = element.getAsJsonArray();
			if (array.isEmpty())
			{
				return VariableType.alphanumericDynamic().asArray();
			}

			return inferJsonType(array.get(0)).asArray();
		}

		// TODO: What do we do as fallback? Throw?
		return VariableType.control();
	}
}
