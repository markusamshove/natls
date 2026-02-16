package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.amshove.natgen.generatable.NaturalCode.*;

public abstract class ParseJsonGenerator
{
	public static class Settings
	{
		public static final String DEFAULT_PARSED_JSON_GROUP_NAME = "##PARSED-JSON";
		private String parsedJsonGroupName = DEFAULT_PARSED_JSON_GROUP_NAME;
		private VariableScope jsonSourceScope = VariableScope.LOCAL;

		public void setParsedJsonGroupName(String name)
		{
			parsedJsonGroupName = name;
		}

		public String parsedJsonGroupName()
		{
			return parsedJsonGroupName;
		}

		public void setJsonSourceScope(VariableScope jsonSourceScope)
		{
			this.jsonSourceScope = jsonSourceScope;
		}

		public VariableScope jsonSourceScope()
		{
			return jsonSourceScope;
		}
	}

	protected static final String JSON_SEPARATOR = "/";
	protected static final String START_OBJECT = "<";
	protected static final String END_OBJECT = ">";
	protected static final String START_ARRAY = "(";
	protected static final String PARSED_DATA = "$";

	protected final ParseJsonGenerator.Settings settings;
	protected Variable jsonParsingGroup;
	protected Variable jsonValue;
	protected Variable parsedJsonRoot;

	protected final Map<String, Variable> variablesByJsonPath = new HashMap<>();
	protected final Map<Variable, Variable> arraySizeVariablesByArray = new HashMap<>();

	protected ParseJsonGenerator(Settings settings)
	{
		this.settings = settings != null ? settings : new Settings();
	}

	/// Create a `PARSE JSON` generator which uses a raw example JSON as basis.
	public static ParseJsonGenerator forRawJson(String rawJson, Settings settings)
	{
		return new ParseJsonFromJsonGenerator(rawJson, settings);
	}

	/// Create a `PARSE JSON` generator which uses an OpenAPI spec as basis.
	public static ParseJsonGenerator forOpenAPISchema(OpenAPI spec, String schemaName, Schema<?> schema, Settings settings)
	{
		return new ParseJsonFromOpenApiSchemaGenerator(spec, schemaName, schema, settings);
	}

	/// Creates a [CodeGenerationContext] which contains all variables and a single `PARSE JSON`
	public CodeGenerationContext generate()
	{
		var context = new CodeGenerationContext();
		jsonParsingGroup = context.addVariable(new Variable(1, VariableScope.LOCAL, "##JSON-PARSING", VariableType.group()));
		var jsonPath = jsonParsingGroup.addVariable("#PATH", VariableType.alphanumericDynamic());
		jsonValue = jsonParsingGroup.addVariable("#VALUE", VariableType.alphanumericDynamic());
		var jsonErrCode = jsonParsingGroup.addVariable("#ERR-CODE", VariableType.integer(4));
		var jsonErrSubcode = jsonParsingGroup.addVariable("#ERR-SUBCODE", VariableType.integer(4));
		parsedJsonRoot = context.addVariable(VariableScope.LOCAL, settings.parsedJsonGroupName(), VariableType.group());

		var jsonSourceVariable = context.addVariable(settings.jsonSourceScope(), "#JSON-SOURCE", VariableType.alphanumericDynamic());

		var decideOnJsonPath = decideOnFirst(jsonPath);
		generateInternal(context, decideOnJsonPath);

		var parseJsonStatement = parseJson(jsonSourceVariable)
			.intoPath(jsonPath)
			.intoValue(jsonValue)
			.givingErrorCode(jsonErrCode)
			.givingErrorSubcode(jsonErrSubcode)
			.addToBody(decideOnJsonPath);

		context.addStatement(parseJsonStatement);

		return context;
	}

	protected abstract void generateInternal(CodeGenerationContext context, DecideOn decide);

	protected static String appendPath(String currentPath, String newPathElement)
	{
		if (currentPath.isEmpty())
		{
			return newPathElement;
		}

		return "%s%s%s".formatted(currentPath, JSON_SEPARATOR, newPathElement);
	}

	protected Variable[] findAllArrayAccessVariablesForCurrentPathInOrder(String path)
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

	protected Variable findSizeVariableByPath(String arrayPath)
	{
		var variablePath = arrayPath.substring(0, arrayPath.lastIndexOf(START_ARRAY) - 1);
		return findSizeVariableForArray(variablesByJsonPath.get(variablePath));
	}

	protected void createResetArrayVariablesForNestedArrays(DecideOn decideOnJsonPath)
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

	protected int getNumberOfDimensions(String path)
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

	protected Variable findSizeVariableForArray(Variable array)
	{
		return arraySizeVariablesByArray.computeIfAbsent(
			array,
			_ -> jsonParsingGroup.addVariable("#S-" + array.name(), VariableType.integer(4))
		);
	}
}
