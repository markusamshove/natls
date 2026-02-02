package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

public class Subroutine extends GeneratableWithBody<Subroutine> implements IGeneratableStatement
{
	private final String name;

	public Subroutine(String name)
	{
		this.name = name;
	}

	public String name()
	{
		return name;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder
			.appendLine(NaturalCode.separatorComment())
			.appendLine("DEFINE SUBROUTINE %s".formatted(name))
			.appendLine(NaturalCode.separatorComment());

		generateBodyWithoutIndentation(builder);

		builder
			.lineBreak()
			.appendLine("END-SUBROUTINE");
	}

}
