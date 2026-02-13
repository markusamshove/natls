package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Compress implements IGeneratableStatement
{
	private IGeneratable into;
	private final List<IGeneratable> operands = new ArrayList<>();
	private boolean leavingNo;
	private boolean isNumeric;
	private boolean isFull;

	/// Specify the target of the `COMPRESS`.
	public Compress into(IGeneratable target)
	{
		into = target;
		return this;
	}

	/// Add an operand that is compressed into the target.
	public Compress withOperand(IGeneratable operand)
	{
		operands.add(operand);
		return this;
	}

	/// Specify the `LEAVING NO SPACE` option.
	public Compress leavingNoSpace()
	{
		leavingNo = true;
		return this;
	}

	/// Specify the `LEAVING SPACE` option.
	public Compress leavingSpace()
	{
		leavingNo = false;
		return this;
	}

	/// Specify the `NUMERIC` option.
	public Compress numeric()
	{
		isNumeric = true;
		return this;
	}

	/// Specify the `FULL` option.
	public Compress full()
	{
		isFull = true;
		return this;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		if (into == null)
		{
			throw new IllegalStateException("Can not generate COMPRESS statement without into operand. Make sure to call `into`");
		}

		if (operands.isEmpty())
		{
			throw new IllegalStateException("Can not generate COMPRESS statement without operands. Make sure to call `withOperand` at least once");
		}

		var indentationBeforeCompress = builder.currentIndentation();

		builder.append("COMPRESS");

		if (isNumeric)
		{
			builder.append(" NUMERIC");
		}

		if (isFull)
		{
			builder.append(" FULL");
		}

		for (var operand : operands)
		{
			builder.spaceOrBreakIndentTo(indentationBeforeCompress + 1);
			builder.append(operand);
		}

		builder.spaceOrBreak().append("INTO ").append(into);

		if (leavingNo)
		{
			if (builder.currentIndentation() > indentationBeforeCompress)
			{
				builder.lineBreak();
			}
			else
			{
				builder.spaceOrBreak();
			}
			builder.append("LEAVING NO SPACE");
		}

		if (builder.currentIndentation() > indentationBeforeCompress)
		{
			builder.unindent();
		}
	}
}
