package org.amshove.natgen.generators;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.*;

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

		createDecideOnJsonElementBranches(decideOnJsonPath, parsedJsonRoot, "", rootElement, "");

		context.addStatement(parseJsonStatement);
		return context;
	}

	private void createDecideOnJsonElementBranches(
		DecideOn decideStatement, Variable parentVariable, String elementName, JsonElement currentElement,
		String currentPath
	)
	{

		if (currentElement.isJsonPrimitive())
		{
			var primitive = currentElement.getAsJsonPrimitive();
			var valueJsonPath = appendPath(currentPath, PARSED_DATA);

			var variableForPrimitive = getVariableForProperty(currentPath, parentVariable, elementName, primitive);

			decideStatement
				.addBranch(stringLiteral(valueJsonPath))
				.addToBody(assignValueToVariable(variableForPrimitive, primitive, valueJsonPath));
			return;
		}

		if (currentElement.isJsonArray())
		{
			// This is the primitive path
			var arrayVariable = getVariableForProperty(currentPath, parentVariable, elementName, currentElement);
			var sizeVariable = getSizeVariableForArray(arrayVariable);
			var firstElementInArray = currentElement.getAsJsonArray().get(0);

			var arrayStartPath = appendPath(currentPath, START_ARRAY);
			var newArrayValuePath = firstElementInArray.isJsonObject()
				? appendPath(arrayStartPath, START_OBJECT) // Array expansion needs to happen when a new object starts
				: appendPath(arrayStartPath, PARSED_DATA); // Array expansion needs to happen on every new primitive value

			var branch = decideStatement
				.addBranch(stringLiteral(newArrayValuePath))
				.addToBody(incrementVariable(sizeVariable))
				.addToBody(expandArray(arrayVariable, sizeVariable));

			if (firstElementInArray.isJsonPrimitive())
			{
				branch.addToBody(
					assignValueToVariable(arrayVariable, firstElementInArray.getAsJsonPrimitive(), newArrayValuePath)
				);
			}
			else
				if (firstElementInArray.isJsonObject())
				{
					createDecideOnJsonElementBranches(decideStatement, arrayVariable, elementName, firstElementInArray, arrayStartPath);
				}

			return;
		}

		if (currentElement.isJsonObject())
		{
			var newParentVariable = parentVariable;
			if (!elementName.isEmpty() && !parentVariable.type().isArray())
			{
				newParentVariable = parentVariable.addVariable("#" + elementName.toUpperCase(Locale.ROOT), VariableType.group());
			}

			var currentObjectPath = appendPath(currentPath, START_OBJECT);
			var jsonObject = currentElement.getAsJsonObject();
			for (var property : jsonObject.entrySet())
			{
				createDecideOnJsonElementBranches(
					decideStatement, newParentVariable, property.getKey(), property.getValue(), appendPath(
						currentObjectPath,
						property.getKey()
					)
				);
			}
		}
	}

	private Variable[] findAllArrayAccessVariablesForCurrentPathInOrder(String path)
	{
		// Move through the path and find the array access variable for each array
		// from left to right.
		// e.g. for the path `</persons/(` find the array variable `</persons` and find its
		// array access variable.
		// If the path contains more than one array variable, e.g. `</persons/(/</dates/(` it will
		// first get the access variable for `</persons` and then `</persons/(/</dates`.
		var arrayAccessVariables = new ArrayList<Variable>();
		var arrayStartIndex = path.indexOf(START_ARRAY);
		var lastArrayStartIndex = 0;
		while (arrayStartIndex > -1)
		{
			var arrayPath = path.substring(0, arrayStartIndex + 1);
			var sizeVariable = findSizeVariableByPath(arrayPath);
			arrayAccessVariables.add(sizeVariable);
			lastArrayStartIndex = arrayStartIndex + 1;
			arrayStartIndex = path.indexOf(START_ARRAY, lastArrayStartIndex);
		}
		return arrayAccessVariables.toArray(new Variable[0]);
	}

	private IGeneratableStatement assignValueToVariable(Variable variableForPrimitive, JsonPrimitive primitive, String currentPath)
	{
		if (currentPath.contains(START_ARRAY))
		{
			var arrayAccessVariables = findAllArrayAccessVariablesForCurrentPathInOrder(currentPath);
			return assignment(variableForPrimitive.arrayAccess(arrayAccessVariables), valueAssignment(primitive));
		}
		return assignment(variableForPrimitive, valueAssignment(primitive));
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

	private Variable getVariableForProperty(
		String propertyNamePath, Variable parentVariable, String propertyName,
		JsonElement property
	)
	{
		return variablesByJsonPath.computeIfAbsent(propertyNamePath, _ ->
		{
			if (parentVariable == null || propertyName == null || property == null)
			{
				// this shouldn't happen. the only way this can happen is
				// when looking up an S- variable for an array but the array doesn't exist yet
				throw new IllegalStateException("Can't create variable if it does not target a json element");
			}
			var type = inferJsonType(property);
			return parentVariable.addVariable("#" + propertyName.toUpperCase(Locale.ROOT), type);
		});
	}

	private Variable findSizeVariableByPath(String arrayPath)
	{
		var variablePath = arrayPath.substring(0, arrayPath.lastIndexOf(START_ARRAY) - 1);
		return getSizeVariableForArray(getVariableForProperty(variablePath, null, null, null));
	}

	private Variable getSizeVariableForArray(Variable array)
	{
		return arraySizeVariablesByJsonPath.computeIfAbsent(
			array,
			_ -> parsedJsonRoot.addVariable("#S-" + array.name(), VariableType.integer(4))
		);
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
