package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

public class Assignment implements IGeneratableStatement
{
	private IGeneratable lhs;
	private IGeneratable rhs;

	public Assignment(IGeneratable lhs, IGeneratable rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder.append(lhs).append(" := ").append(rhs);
	}

	public void setLhs(IGeneratable lhs)
	{
		this.lhs = lhs;
	}

	public void setRhs(IGeneratable rhs)
	{
		this.rhs = rhs;
	}
}
