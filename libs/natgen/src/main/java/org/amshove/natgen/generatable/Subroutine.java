package org.amshove.natgen.generatable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Subroutine implements IGeneratable
{
	private final String name;
	private final List<IGeneratable> bodyParts = new ArrayList<>();

	public Subroutine(String name)
	{
		this.name = name;
	}

	public String name()
	{
		return name;
	}

	public Subroutine addToBody(IGeneratable generatable)
	{
		bodyParts.add(generatable);
		return this;
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
		return bodyParts.stream().map(IGeneratable::generate).collect(Collectors.joining(System.lineSeparator()));
	}
}
