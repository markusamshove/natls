package org.amshove.natgen.generatable;

public class ParseJson extends GeneratableWithBody<ParseJson> implements IGeneratable
{
	private final IGeneratable referenceToJsonSource;
	private IGeneratable pathVariable;
	private IGeneratable nameVariable;
	private IGeneratable valueVariable;
	private IGeneratable errorCodeVariable;
	private IGeneratable errorSubcodeVariable;

	ParseJson(IGeneratable referenceToJsonSource)
	{
		this.referenceToJsonSource = referenceToJsonSource;
	}

	public ParseJson intoPath(IGeneratable pathVariable)
	{
		this.pathVariable = pathVariable;
		return this;
	}

	public ParseJson intoName(IGeneratable nameVariable)
	{
		this.nameVariable = nameVariable;
		return this;
	}

	public ParseJson intoValue(IGeneratable valueVariable)
	{
		this.valueVariable = valueVariable;
		return this;
	}

	public ParseJson givingErrorCode(IGeneratable errorCodeVariable)
	{
		this.errorCodeVariable = errorCodeVariable;
		return this;
	}

	public ParseJson givingErrorSubcode(IGeneratable errorSubcodeVariable)
	{
		this.errorSubcodeVariable = errorSubcodeVariable;
		return this;
	}

	@Override
	public String generate()
	{
		var code = new StringBuilder();
		code.append("PARSE JSON ")
			.append(referenceToJsonSource.generate());

		var hasInto = false;
		if (pathVariable != null)
		{
			hasInto = appendInto(code, hasInto);
			code.append(" PATH ").append(pathVariable.generate());
		}

		if (nameVariable != null)
		{
			hasInto = appendInto(code, hasInto);
			code.append(" NAME ").append(nameVariable.generate());
		}

		if (valueVariable != null)
		{
			appendInto(code, hasInto);
			code.append(" VALUE ").append(valueVariable.generate());
		}

		if (errorCodeVariable != null)
		{
			code.append(" GIVING ").append(errorCodeVariable.generate());
			if (errorSubcodeVariable != null)
			{
				code.append(" SUBCODE ").append(errorSubcodeVariable.generate());
			}
		}

		code.append(System.lineSeparator());
		code.append(body()).append(System.lineSeparator());
		code.append("END-PARSE");

		return code.toString();
	}

	private boolean appendInto(StringBuilder code, boolean hasInto)
	{
		if (!hasInto)
		{
			code.append(" INTO");
		}

		return true;
	}
}
