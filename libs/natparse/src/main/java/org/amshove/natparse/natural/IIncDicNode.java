package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;
import org.jspecify.annotations.Nullable;

/**
 * This node defines an "INCluded from DICtionary" rule
 *
 * This is either an inline rule in the body of the map program, or a rule accessed from the FDIC file attached to the
 * compiler
 *
 */
public interface IIncDicNode extends IStatementNode
{
	@Nullable
	SyntaxToken ruleName();
}
