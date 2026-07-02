package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

/**
 * This node is an "INCluded from DIRectory" node
 *
 * These nodes are validation rules attached to the Adabas file / fields
 * by the DBA. Not clear whether these are inlined into the program at
 * compile time, or whether they just include a hook to call them
 *
 * The map editor just adds these automatically when you reference a view
 *
 */
public interface IIncDirNode extends IStatementNode
{
	SyntaxToken ddmName();

	SyntaxToken fieldName();

}
