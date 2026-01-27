package org.amshove.natgen.generatable;

public class Subroutine extends GeneratableWithBody<Subroutine> implements IGeneratable
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
	public String generate()
	{
		return """
			/***********************************************************************
			DEFINE SUBROUTINE %s
			/***********************************************************************

			%s

			END-SUBROUTINE
			""".formatted(name, body());
	}

}
