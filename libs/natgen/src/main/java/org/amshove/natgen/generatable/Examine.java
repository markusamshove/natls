package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

public final class Examine implements IGeneratableStatement
{
	private final IGeneratable examined;
	private boolean isExamineFull;
	private IGeneratable _for;
	private IGeneratable forPattern;
	private IGeneratable replacer;
	private IGeneratable direction;
	private IGeneratable startingFrom;
	private IGeneratable endingAt;
	private boolean isWithDefaultDelimiter;
	private IGeneratable delimiter;
	private boolean isWithFirstOption;

	public Examine(IGeneratable examined)
	{
		this.examined = examined;
	}

	/// Mark as `EXAMINE FULL`
	public Examine asExamineFull()
	{
		isExamineFull = true;
		return this;
	}

	/// Set the value that is searched within the examined value
	public Examine _for(IGeneratable _for)
	{
		this._for = _for;
		return this;
	}

	/// Set the value that is searched within the examined value with `FOR PATTERN`
	/// Once this is set, the generated `EXAMINE` will have `FOR PATTERN`, no matter
	/// if [#_for] is called afterward.
	public Examine forPattern(IGeneratable forPattern)
	{
		this.forPattern = forPattern;
		return this;
	}

	/// Replace examined value(s) with given value
	public Examine replaceWith(IGeneratable replacer)
	{
		this.replacer = replacer;
		return this;
	}

	/// Replace first examined value with given value
	public Examine replaceFirstWith(IGeneratable replacer)
	{
		isWithFirstOption = true;
		return replaceWith(replacer);
	}

	/// Set the examine `DIRECTION FORWARD`
	public Examine forward()
	{
		return direction(NaturalCode.plain("FORWARD"));
	}

	/// Set the examine `DIRECTION BACKWARD`
	public Examine backward()
	{
		return direction(NaturalCode.plain("BACKWARD"));
	}

	/// Set the examine `DIRECTION`
	public Examine direction(IGeneratable direction)
	{
		this.direction = direction;
		return this;
	}

	/// Set the `STARTING FROM` position
	public Examine startingFrom(IGeneratable startingFrom)
	{
		this.startingFrom = startingFrom;
		return this;
	}

	/// Set the `STARTING FROM` and `ENDING AT` positions
	public Examine startingFromEndingAt(IGeneratable startingFrom, IGeneratable endingAt)
	{
		this.startingFrom = startingFrom;
		this.endingAt = endingAt;
		return this;
	}

	/// Add the `WITH DELIMITERS` option (without an operand)
	public Examine withDelimiter()
	{
		this.isWithDefaultDelimiter = true;
		return this;
	}

	/// Add the `WITH DELIMITERS` option with the given delimiter
	public Examine withDelimiter(IGeneratable delimiters)
	{
		this.delimiter = delimiters;
		return this;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder.append("EXAMINE ");

		if (direction != null)
		{
			builder
				.append("DIRECTION ")
				.append(direction)
				.spaceOrBreakAndIndent();
		}

		if (isExamineFull)
		{
			builder.append("FULL ");
		}

		builder.append(examined);

		var didIndent = builder.spaceOrBreakAndIndent();

		if (startingFrom != null)
		{
			builder.append("STARTING FROM ").append(startingFrom);
			didIndent = builder.spaceOrBreakAndIndent();

			if (endingAt != null)
			{
				builder.append("ENDING AT ").append(endingAt);
				didIndent = builder.spaceOrBreakAndIndent();
			}
		}

		builder.append("FOR ");

		if (forPattern != null)
		{
			builder.append("PATTERN ").append(forPattern);
		}
		else
		{
			builder.append(_for);
		}

		didIndent = builder.spaceOrBreakAndIndent();

		if (isWithDefaultDelimiter)
		{
			builder.append("WITH DELIMITER").spaceOrBreak();
		}
		else
			if (delimiter != null)
			{
				builder
					.append("WITH DELIMITER ")
					.append(delimiter)
					.spaceOrBreak();
			}

		if (replacer != null)
		{
			builder
				.append("REPLACE ")
				.appendIf(isWithFirstOption, "FIRST ")
				.append("WITH ")
				.append(replacer);
		}

		if (didIndent)
		{
			builder.unindent();
		}
	}
}
