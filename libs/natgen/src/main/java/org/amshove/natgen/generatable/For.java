package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

public class For extends GeneratableWithBody<For> implements IGeneratableStatement
{
	private final IGeneratable iterationVariable;
	private final IGeneratable startValue;
	private final IGeneratable upper;

	public For(IGeneratable iterationVariable, IGeneratable startValue, IGeneratable upper)
	{
		this.iterationVariable = iterationVariable;
		this.startValue = startValue;
		this.upper = upper;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder.append("FOR ").append(iterationVariable).append(" := ").append(startValue).append(" TO ").append(upper);

		generateBody(builder);

		builder.append("END-FOR");
	}
}
