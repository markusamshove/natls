package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IReinputStatementNode;

class ReinputStatementNode extends StatementNode implements IReinputStatementNode
{
	private boolean usingHelp;
	private IAttributeListNode statementAttributes;
	private IAttributeListNode outputAttributes;

	@Override
	public boolean isUsingHelp() {
		return usingHelp;
	}

	public void setUsingHelp(boolean usingHelp) {
		this.usingHelp = usingHelp;
	}

	@Override
	public ReadOnlyList<IAttributeNode> statementAttributes() {
		return statementAttributes == null ?
			ReadOnlyList.empty() : statementAttributes.attributes();
	}

	public void setStatementAttributes(IAttributeListNode statementAttributes) {
		this.statementAttributes = statementAttributes;
	}

	public void setOutputAttributes(IAttributeListNode outputAttributes) {
		this.outputAttributes = outputAttributes;
	}

}
