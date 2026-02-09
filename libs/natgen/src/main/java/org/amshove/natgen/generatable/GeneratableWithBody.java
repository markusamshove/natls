package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

class GeneratableWithBody<T extends IGeneratableStatement>
{
	protected final List<IGeneratableStatement> bodyParts = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public T addToBody(IGeneratableStatement generatable)
	{
		bodyParts.add(generatable);
		return (T) this;
	}

	/// Generates the body by concatenating all body parts with line separators and indentation.
	/// Will return a single `IGNORE` statement if no parts have been added.
	protected void generateBody(CodeBuilder builder)
	{
		generateBody(builder, true);
	}

	/// Generates the body by concatenating all body parts with line separators.
	/// Will return a single `IGNORE` statement if no parts have been added.
	/// **Will not indent the statements**
	protected void generateBodyWithoutIndentation(CodeBuilder builder)
	{
		generateBody(builder, false);
	}

	private void generateBody(CodeBuilder builder, boolean withIndentation)
	{
		builder.lineBreak();
		if (withIndentation)
		{
			builder.indent();
		}
		if (bodyParts.isEmpty())
		{
			NaturalCode.ignore().generateInto(builder);
			builder.lineBreak();
		}
		else
		{
			for (var bodyPart : bodyParts)
			{
				if (bodyPart instanceof Subroutine)
				{
					// Separate a subroutine declaration from
					// other statements to make it more visible
					builder.lineBreak();
				}
				bodyPart.generateInto(builder);
				builder.lineBreak();
			}
		}

		if (withIndentation)
		{
			builder.unindent();
		}
	}
}
