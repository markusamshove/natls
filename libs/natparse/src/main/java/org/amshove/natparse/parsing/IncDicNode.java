package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IIncDicNode;
import org.jspecify.annotations.Nullable;

/**
 * INCDIC represents a validation rule reference
 *
 * This statement is also used just before an inline rule without a rule name
 */
class IncDicNode extends StatementWithBodyNode implements IIncDicNode
{
	private SyntaxToken ruleName;
	private IDefineData define;

	public void setRuleName(SyntaxToken ruleName)
	{
		this.ruleName = ruleName;
	}

	@Nullable
	@Override
	public SyntaxToken ruleName()
	{
	    return this.ruleName;
	}

	public void setDefine(IDefineData define)
	{
		this.define = define;
	}


}

