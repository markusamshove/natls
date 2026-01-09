package org.amshove.natgen.generatable;

public class Subroutine implements IGeneratable
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

	private String body()
	{
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
