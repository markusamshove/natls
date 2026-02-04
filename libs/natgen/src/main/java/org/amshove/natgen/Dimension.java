package org.amshove.natgen;

public record Dimension(int lowerBound, int upperBound)
{
	public Dimension
	{
		if (lowerBound < 1)
		{
			throw new IllegalArgumentException("Lower array bound can't be less than 1");
		}
	}

	/// X-Array in form of `1:*`
	public static Dimension upperUnbound()
	{
		return new Dimension(1, Integer.MAX_VALUE);
	}

	///  Array in form of `1:upperBound`
	public static Dimension upperBound(int upperBound)
	{
		return new Dimension(1, upperBound);
	}

	public boolean isUpperUnbound()
	{
		return upperBound == Integer.MAX_VALUE;
	}

	public String toDeclaration()
	{
		var upperBoundFormatted = isUpperUnbound() ? "*" : upperBound;
		return "%d:%s".formatted(lowerBound, upperBoundFormatted);
	}
}
