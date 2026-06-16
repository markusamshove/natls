package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

public interface IIncDirNode extends IStatementNode
{
	SyntaxToken ddmName();

	SyntaxToken fieldName();

}
