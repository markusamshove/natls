package org.amshove.natparse.parsing;

import java.util.ArrayList;
import java.util.List;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeListNode;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IReinputStatementNode;

class ReinputStatementNode extends StatementNode implements IReinputStatementNode
{
	private boolean isFull;
	private IAttributeListNode statementAttributes;

	private boolean usingHelp;

	private IOperandNode messageOperand;
	private IAttributeListNode messageAttributes;
	private List<IOperandNode> messageFormatOperands = new ArrayList<>();

	private boolean hasAlarm;

	@Override
	public boolean isFull()
	{
		return isFull;
	}

	public void setIsFull(boolean isFull)
	{
		this.isFull = isFull;
	}

	@Override
	public boolean isUsingHelp()
	{
		return usingHelp;
	}

	public void setUsingHelp(boolean usingHelp)
	{
		this.usingHelp = usingHelp;
	}

	@Override
	public ReadOnlyList<IAttributeNode> statementAttributes()
	{
		return statementAttributes == null ? ReadOnlyList.empty() : statementAttributes.attributes();
	}

	public void setStatementAttributes(IAttributeListNode statementAttributes)
	{
		this.statementAttributes = statementAttributes;
	}

	@Override
	public IOperandNode messageOperand()
	{
		return this.messageOperand;
	}

	public void setMessageOperand(IOperandNode messageOperand)
	{
		this.messageOperand = messageOperand;
	}

	@Override
	public ReadOnlyList<IAttributeNode> messageAttributes()
	{
		return messageAttributes == null ? ReadOnlyList.empty() : messageAttributes.attributes();
	}

	public void setMessageAttributes(IAttributeListNode outputAttributes)
	{
		this.messageAttributes = outputAttributes;
	}

	@Override
	public ReadOnlyList<IOperandNode> messageFormatOperands()
	{
		return messageFormatOperands == null ? ReadOnlyList.empty() : ReadOnlyList.from(messageFormatOperands);
	}

	public void addMessageFormatOperand(IOperandNode messageFormatOperand)
	{
		messageFormatOperands.add(messageFormatOperand);
	}

	@Override
	public boolean hasAlarm()
	{
		return hasAlarm;
	}

	public void setHasAlarm(boolean hasAlarm)
	{
		this.hasAlarm = hasAlarm;
	}
}
