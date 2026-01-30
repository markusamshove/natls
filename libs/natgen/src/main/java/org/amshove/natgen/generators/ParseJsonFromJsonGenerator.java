package org.amshove.natgen.generators;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.HashMap;
import java.util.Map;

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
		var decideOnJsonPath = NaturalCode.decideOnFirst(jsonPath);
		var parseJsonStatement = NaturalCode
			.parseJson(jsonSourceVariable)
			.intoPath(jsonPath)
			.intoValue(jsonValue)
			.givingErrorCode(jsonErrCode)
			.givingErrorSubcode(jsonErrSubcode)
			.addToBody(decideOnJsonPath);

		createDecideOnJsonElementBranches(decideOnJsonPath, rootElement, "");

		context.addStatement(parseJsonStatement);
		return context;
	}

	private void createDecideOnJsonElementBranches(DecideOn decideStatement, JsonElement currentElement, String currentPath)
	{
		if (currentElement.isJsonArray())
		{
			var elementPath = appendPath(currentPath, START_ARRAY);
			// TODO: RESIZE
			createDecideOnJsonElementBranches(decideStatement, currentElement.getAsJsonArray().get(0), elementPath);
		}

		if (currentElement.isJsonObject())
		{
			var elementPath = appendPath(currentPath, START_OBJECT);
			var jsonObject = currentElement.getAsJsonObject();
			for (var property : jsonObject.entrySet())
			{
				// TODO: Nested objects/arrays?
				if (property.getValue().isJsonPrimitive())
				{
					createPrimitiveValueBranch(decideStatement, property.getKey(), property.getValue().getAsJsonPrimitive(), elementPath);
				}
			}
		}

		//		throw new UnsupportedOperationException(
		//			"Unsupported element type: %s".formatted(currentElement.getClass().getSimpleName())
		//		);
	}

	private void createPrimitiveValueBranch(DecideOn statement, String propertyName, JsonPrimitive primitive, String currentPath)
	{
		var propertyNamePath = appendPath(currentPath, propertyName);
		var valueJsonPath = appendPath(propertyNamePath, PARSED_DATA);
		statement
			.addBranch(NaturalCode.stringLiteral(valueJsonPath))
			.addToBody(NaturalCode.lineComment(propertyName))
			.addToBody(NaturalCode.assignment(NaturalCode.plain(propertyName), valueAssignment(primitive)));
	}

	private IGeneratable valueAssignment(JsonPrimitive primitive)
	{
		if (primitive.isNumber())
		{
			return NaturalCode.val(jsonValue);
		}

		if (primitive.isString())
		{
			return jsonValue;
		}

		if (primitive.isBoolean())
		{
			return NaturalCode.functionCall("ATOB", jsonValue);
		}

		throw new UnsupportedOperationException("Unknown json primitive: %s".formatted(primitive));
	}

	private static String appendPath(String currentPath, String newPathElement)
	{
		return "%s%s%s".formatted(currentPath, JSON_SEPARATOR, newPathElement);
	}
}
