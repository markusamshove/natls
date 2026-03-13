package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratableStatement;

import java.util.Collection;

public interface IStatementAddable<SELF>
{
	/// Prepend a statement to the statement list
	SELF addStatementToFront(IGeneratableStatement statement);

	SELF addStatement(IGeneratableStatement statement);

	SELF addStatements(Collection<IGeneratableStatement> statement);
}
