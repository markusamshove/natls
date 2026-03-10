package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;
import org.jspecify.annotations.Nullable;

public interface IIncDicNode extends IStatementNode
{
	@Nullable
	SyntaxToken ruleName();
}

