package org.amshove.natparse.parsing.operandcheck;

import java.util.Set;

import org.amshove.natparse.natural.IOperandNode;

public sealed class OperandCheck
{
	private IOperandNode lhs;

	OperandCheck(IOperandNode lhs)
	{
		this.lhs = lhs;
	}

	public IOperandNode lhs()
	{
		return lhs;
	}

	public static final class DefinitionCheck extends OperandCheck
	{
		final Set<OperandDefinition> definitionTable;

		public DefinitionCheck(IOperandNode lhs, Set<OperandDefinition> definitionTable)
		{
			super(lhs);
			this.definitionTable = definitionTable;
		}

		public Set<OperandDefinition> definitionTable()
		{
			return definitionTable;
		}
	}

	public static final class BinaryCheck extends OperandCheck
	{
		final IOperandNode rhs;

		public BinaryCheck(IOperandNode lhs, IOperandNode rhs)
		{
			super(lhs);
			this.rhs = rhs;
		}

		public IOperandNode rhs()
		{
			return rhs;
		}
	}

}
