package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.IStatementAddable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class GeneratableWithBody<T extends IGeneratableStatement> implements IStatementAddable<T>
{
	protected final List<IGeneratableStatement> bodyParts = new ArrayList<>();

	@Override
	@SuppressWarnings("unchecked")
	public T addStatementToFront(IGeneratableStatement statement)
	{
		bodyParts.addFirst(statement);
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addStatement(IGeneratableStatement generatable)
	{
		bodyParts.add(generatable);
		return (T) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T addStatements(Collection<IGeneratableStatement> statements)
	{
		for (var statement : statements)
		{
			addStatement(statement);
		}
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
