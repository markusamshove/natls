package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;

/// Formatting aware builder for Natural code
public final class CodeBuilder
{
	private static final int MAX_LINE_LENGTH = 80;
	private static final String INDENTATION = "  ";

	private final StringBuilder code = new StringBuilder();
	private int currentLineLength = 0;
	private int indentationLevel = 0;

	/// Append the `value` as is
	public CodeBuilder append(Object value)
	{
		var asString = value.toString();
		if (currentLineLength == 0)
		{
			code.append(INDENTATION.repeat(indentationLevel));
		}

		code.append(asString);
		currentLineLength += asString.length();
		return this;
	}

	/// Append the [IGeneratable] by calling its [IGeneratable#generate()] method
	public CodeBuilder append(IGeneratable generatable)
	{
		return append(generatable.generate());
	}

	///  Append the [IGeneratableStatement] into this [CodeBuilder]
	public CodeBuilder append(IGeneratableStatement statement)
	{
		statement.generateInto(this);
		return this;
	}

	///  Adds a linebreak
	public CodeBuilder lineBreak()
	{
		code.append(System.lineSeparator());
		currentLineLength = 0;
		return this;
	}

	/// Append the `value` as is and add a linebreak after
	public CodeBuilder appendLine(Object value)
	{
		append(value);
		return lineBreak();
	}

	/// Generate the [IGeneratableStatement] into this [CodeBuilder] and add a linebreak after
	public CodeBuilder appendLine(IGeneratableStatement statement)
	{
		statement.generateInto(this);
		return lineBreak();
	}

	/// Append the [org.amshove.natgen.generatable.IGeneratable] as is and add a linebreak after
	public CodeBuilder appendLine(IGeneratable generatable)
	{
		return appendLine(generatable.generate());
	}

	///  Append a whitespace or break the line if max line length has been exceeded
	public CodeBuilder spaceOrBreak()
	{
		if (currentLineLength + 1 >= MAX_LINE_LENGTH)
		{
			return lineBreak();
		}

		code.append(" ");
		return this;
	}

	/// Increases the indentation level by one
	public CodeBuilder indent()
	{
		indentationLevel++;
		return this;
	}

	/// Decreases the indentation level by one
	public CodeBuilder unindent()
	{
		indentationLevel = Math.max(0, --indentationLevel);
		return this;
	}

	@Override
	public String toString()
	{
		return code.toString();
	}
}
