package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IRuleVarNode;

class RuleVarNode extends StatementWithBodyNode implements IRuleVarNode
{

	private SyntaxToken nameToken;

	private IncDirNode incDirNode;
	// OR
	private IncDicNode incDicNode;

	void setName(SyntaxToken nameToken)
	{
		this.nameToken = nameToken;
	}

	void setIncDir(IncDirNode incDirNode)
	{
		this.incDirNode = incDirNode;
	}

	void setIncDic(IncDicNode incDicNode)
	{
		this.incDicNode = incDicNode;
	}

}
