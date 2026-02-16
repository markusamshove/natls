package org.amshove.natgen.generators;

import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import static org.amshove.natgen.generatable.NaturalCode.decideOnFirst;
import static org.amshove.natgen.generatable.NaturalCode.parseJson;

public abstract class ParseJsonGenerator
{
	public static class Settings
	{
		private String parsedJsonGroupName = "##PARSED-JSON";
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

	protected final ParseJsonGenerator.Settings settings;
	protected Variable jsonParsingGroup;
	protected Variable jsonValue;
	protected Variable parsedJsonRoot;

	protected ParseJsonGenerator(Settings settings)
	{
		this.settings = settings != null ? settings : new Settings();
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
		createDecideOnBranches(context, decideOnJsonPath);

		var parseJsonStatement = parseJson(jsonSourceVariable)
			.intoPath(jsonPath)
			.intoValue(jsonValue)
			.givingErrorCode(jsonErrCode)
			.givingErrorSubcode(jsonErrSubcode)
			.addToBody(decideOnJsonPath);

		context.addStatement(parseJsonStatement);

		return context;
	}

	protected abstract void createDecideOnBranches(CodeGenerationContext context, DecideOn decide);

	/// Create a `PARSE JSON` generator which uses a raw example JSON as basis.
	public static ParseJsonGenerator forRawJson(String rawJson, Settings settings)
	{
		return new ParseJsonFromJsonGenerator(rawJson, settings);
	}
}
