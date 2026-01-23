package org.amshove.natgen.generatable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class GeneratableWithBody<T extends IGeneratable>
{
	protected final List<IGeneratable> bodyParts = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public T addToBody(IGeneratable generatable)
	{
		bodyParts.add(generatable);
		return (T) this;
	}

	/// Generates the body by concatenating all body parts with line separators.
	/// Will return a single `IGNORE` statement if no parts have been added.
	protected String body()
	{
		if (bodyParts.isEmpty())
		{
			return NaturalCode.ignore().generate();
		}

		return bodyParts.stream().map(IGeneratable::generate).collect(Collectors.joining(System.lineSeparator()));
	}

	/// Generate the body with the given indentation.
	protected String bodyWithIndentation(String indentation)
	{
		if (bodyParts.isEmpty())
		{
			return indentation + NaturalCode.ignore().generate();
		}

		return bodyParts.stream()
			.map(IGeneratable::generate)
			.map(line -> indentation + line)
			.collect(Collectors.joining(System.lineSeparator()));
	}
}

