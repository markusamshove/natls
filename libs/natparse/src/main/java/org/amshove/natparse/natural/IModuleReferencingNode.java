package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.jspecify.annotations.Nullable;

public interface IModuleReferencingNode extends ISyntaxNode
{
	/**
	 * The referenced module.
	 */
	@Nullable
	INaturalModule reference();

	/**
	 * Contains the name {@link SyntaxToken} which holds the name of the called module.
	 */
	SyntaxToken referencingToken();

	ReadOnlyList<IOperandNode> providedParameter();
}
