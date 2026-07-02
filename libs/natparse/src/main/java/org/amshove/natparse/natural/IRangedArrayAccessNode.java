package org.amshove.natparse.natural;

public interface IRangedArrayAccessNode extends IOperandNode
{
	IOperandNode lowerBound();

	IOperandNode upperBound();

	boolean isAnyUnbound();

	/// Determines if lower unbound `(*:)`
	boolean isLowerUnbound();

	/// Determines if upper unbound `(:*)`
	boolean isUpperUnbound();

	/// Determines if lower and upper are unbound `(*:*)` or `(*)`
	boolean isUnbound();
}
