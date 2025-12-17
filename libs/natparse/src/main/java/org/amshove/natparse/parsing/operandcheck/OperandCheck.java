package org.amshove.natparse.parsing.operandcheck;

import org.amshove.natparse.natural.IOperandNode;

import java.util.Set;

public sealed class OperandCheck
{
	private final IOperandNode lhs;

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

	/**
	 * Checks if two operands have compatible types. RHS must be implicitly convertible to LHS.
	 */
	public static final class CompatibleBinaryCheck extends OperandCheck
	{
		final IOperandNode rhs;

		public CompatibleBinaryCheck(IOperandNode lhs, IOperandNode rhs)
		{
			super(lhs);
			this.rhs = rhs;
		}

		public IOperandNode rhs()
		{
			return rhs;
		}
	}

	/**
	 * Checks if two operands have strictly the same family.
	 */
	public static final class FamilyBinaryCheck extends OperandCheck
	{

		final IOperandNode rhs;

		public FamilyBinaryCheck(IOperandNode lhs, IOperandNode rhs)
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
