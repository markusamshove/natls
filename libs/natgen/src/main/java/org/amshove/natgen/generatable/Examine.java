package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

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
	private boolean isDelete;
	private boolean isDeleteFirst;
	private IGeneratable givingNumber;
	private IGeneratable givingPosition;
	private IGeneratable givingLength;
	private List<IGeneratable> givingIndex = new ArrayList<IGeneratable>();

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

	/// Make this examine as `DELETE`
	public Examine delete()
	{
		isDeleteFirst = false;
		isDelete = true;
		return this;
	}

	/// Make this examine as `DELETE FIRST`
	public Examine deleteFirst()
	{
		isDelete = true;
		isDeleteFirst = true;
		return this;
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

	/// Add `GIVING NUMBER` to count the number of occurrences
	public Examine givingNumber(IGeneratable number)
	{
		this.givingNumber = number;
		return this;
	}

	/// Add `GIVING POSITION` to retrieve the first position
	/// of the pattern occurrence
	public Examine givingPosition(IGeneratable position)
	{
		this.givingPosition = position;
		return this;
	}

	/// Add `GIVING LENGTH` to retrieve length of the examined value
	/// after all delete/replace operations have been performed
	public Examine givingLength(IGeneratable length)
	{
		this.givingLength = length;
		return this;
	}

	/// Add `GIVING INDEX` to retrieve the index within the array
	/// where the first search value was found
	public Examine givingIndex(IGeneratable index)
	{
		this.givingIndex.add(index);
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

		if (isWithDefaultDelimiter)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder.append("WITH DELIMITER");
		}
		else
			if (delimiter != null)
			{
				didIndent = builder.spaceOrBreakAndIndent();
				builder
					.append("WITH DELIMITER ")
					.append(delimiter);
			}

		if (replacer != null)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("REPLACE ")
				.appendIf(isWithFirstOption, "FIRST ")
				.append("WITH ")
				.append(replacer);
		}

		if (isDelete)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("DELETE")
				.appendIf(isDeleteFirst, " FIRST");
		}

		if (givingNumber != null)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("GIVING NUMBER ")
				.append(givingNumber);
		}

		if (givingPosition != null)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("GIVING POSITION ")
				.append(givingPosition);
		}

		if (givingLength != null)
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("GIVING LENGTH ")
				.append(givingLength);
		}

		if (!givingIndex.isEmpty())
		{
			didIndent = builder.spaceOrBreakAndIndent();
			builder
				.append("GIVING INDEX");

			givingIndex.forEach(i -> builder.append(" ").append(i));
		}

		if (didIndent)
		{
			builder.unindent();
		}
	}
}
