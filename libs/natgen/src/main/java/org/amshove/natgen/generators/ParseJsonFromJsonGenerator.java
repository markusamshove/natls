package org.amshove.natgen.generators;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.Dimension;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.amshove.natgen.generatable.NaturalCode.*;

public class ParseJsonFromJsonGenerator extends ParseJsonGenerator
{
	private static final String JSON_SEPARATOR = "/";
	private static final String START_OBJECT = "<";
	private static final String END_OBJECT = ">";
	private static final String START_ARRAY = "(";
	private static final String PARSED_DATA = "$";

	private final String rawJson;

	private final Map<String, Variable> variablesByJsonPath = new HashMap<>();
	private final Map<Variable, Variable> arraySizeVariablesByArray = new HashMap<>();

	ParseJsonFromJsonGenerator(String rawJson, Settings settings)
	{
		super(settings);
		this.rawJson = rawJson;
	}

	@Override
	protected void createDecideOnBranches(CodeGenerationContext context, DecideOn decide)
	{
		var gson = new Gson();
		var rootElement = gson.fromJson(rawJson, JsonElement.class);

		createDecideOnJsonElementBranches(decide, parsedJsonRoot, "", rootElement, "");

		createResetArrayVariablesForNestedArrays(decide);
	}

	private void createDecideOnJsonElementBranches(
		DecideOn decideStatement, Variable parentVariable, String elementName, JsonElement currentElement,
		String currentPath
	)
	{

		if (currentElement.isJsonPrimitive() || currentElement.isJsonNull())
		{
			var valueJsonPath = appendPath(currentPath, PARSED_DATA);

			var variableForPrimitive = getVariableForProperty(currentPath, parentVariable, elementName, currentElement);

			decideStatement
				.addBranch(stringLiteral(valueJsonPath))
				.addToBody(assignValueToVariable(variableForPrimitive, currentElement, valueJsonPath));
			return;
		}

		if (currentElement.isJsonArray())
		{
			// This is the primitive path
			var arrayVariable = getVariableForProperty(currentPath, parentVariable, elementName, currentElement);
			var sizeVariable = findSizeVariableForArray(arrayVariable);
			var firstElementInArray = currentElement.getAsJsonArray().get(0);

			var arrayStartPath = appendPath(currentPath, START_ARRAY);
			var newArrayValuePath = firstElementInArray.isJsonObject()
				? appendPath(arrayStartPath, START_OBJECT) // Array expansion needs to happen when a new object starts
				: appendPath(arrayStartPath, PARSED_DATA); // Array expansion needs to happen on every new primitive value

			var numberOfDimensions = getNumberOfDimensions(arrayStartPath);
			var branch = decideStatement
				.addBranch(stringLiteral(newArrayValuePath))
				.addToBody(incrementVariable(sizeVariable))
				.addToBody(expandNthArrayDimension(arrayVariable, numberOfDimensions, sizeVariable));

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

			return;
		}

		throw new IllegalStateException("Can not handle JSON element type <%s>".formatted(currentElement.getClass().getSimpleName()));
	}

	private void createResetArrayVariablesForNestedArrays(DecideOn decideOnJsonPath)
	{
		// For each array inside objects we need to reset the counter variable when the object ends.
		for (var variableByJsonPath : variablesByJsonPath.entrySet())
		{
			var path = variableByJsonPath.getKey();
			var array = variableByJsonPath.getValue();

			if (!array.type().isArray())
			{
				continue;
			}

			var sizeVariable = findSizeVariableForArray(array);
			// go from `</obj/(/</arrayInObj`
			// to `</obj/(/>` to get the path of closing the enclosing object
			var closeObjectPath = path.substring(0, path.lastIndexOf(START_OBJECT)) + END_OBJECT;
			decideOnJsonPath
				.addBranch(NaturalCode.stringLiteral(closeObjectPath))
				.addToBody(reset(sizeVariable));
		}
	}

	private int getNumberOfDimensions(String path)
	{
		var startArrayChar = START_ARRAY.charAt(0);
		int dimensions = 0;
		for (var c : path.toCharArray())
		{
			if (c == startArrayChar)
			{
				dimensions++;
			}
		}

		return dimensions;
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

	private IGeneratableStatement assignValueToVariable(Variable variableForPrimitive, JsonElement element, String currentPath)
	{
		if (currentPath.contains(START_ARRAY))
		{
			var arrayAccessVariables = findAllArrayAccessVariablesForCurrentPathInOrder(currentPath);
			return assignment(variableForPrimitive.arrayAccess(arrayAccessVariables), valueAssignment(element));
		}
		return assignment(variableForPrimitive, valueAssignment(element));
	}

	private IGeneratable valueAssignment(JsonElement element)
	{
		if (element.isJsonNull())
		{
			return jsonValue;
		}

		var primitive = element.getAsJsonPrimitive();

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
			var variableName = new StringBuilder("#" + propertyName.toUpperCase(Locale.ROOT));
			while (variableNameIsTaken(variableName.toString()))
			{
				variableName.insert(0, "#");
			}
			return parentVariable.addVariable(variableName.toString(), type);
		});
	}

	private Variable findSizeVariableByPath(String arrayPath)
	{
		var variablePath = arrayPath.substring(0, arrayPath.lastIndexOf(START_ARRAY) - 1);
		return findSizeVariableForArray(getVariableForProperty(variablePath, null, null, null));
	}

	private Variable findSizeVariableForArray(Variable array)
	{
		return arraySizeVariablesByArray.computeIfAbsent(
			array,
			_ -> jsonParsingGroup.addVariable("#S-" + array.name(), VariableType.integer(4))
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
				return VariableType.alphanumericDynamic().withDimension(Dimension.upperUnbound());
			}

			return inferJsonType(array.get(0)).withDimension(Dimension.upperUnbound());
		}

		return VariableType.alphanumericDynamic();
	}

	private boolean variableNameIsTaken(String plannedName)
	{
		return Stream.concat(variablesByJsonPath.values().stream(), arraySizeVariablesByArray.values().stream())
			.anyMatch(v -> v.name().equals(plannedName));
	}
}
