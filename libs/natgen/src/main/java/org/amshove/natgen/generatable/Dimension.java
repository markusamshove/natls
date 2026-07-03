package org.amshove.natgen.generatable;

import static org.amshove.natgen.generatable.NaturalCode.*;

public final class Dimension implements IGeneratable
{
	private final IGeneratable lowerBound;
	private final IGeneratable upperBound;
	private final boolean lowerUnbound;
	private final boolean upperUnbound;

	private Dimension(IGeneratable lowerBound, IGeneratable upperBound, boolean lowerUnbound, boolean upperUnbound)
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.lowerUnbound = lowerUnbound;
		this.upperUnbound = upperUnbound;
	}

	public static Dimension range(IGeneratable lowerBound, IGeneratable upperBound)
	{
		return new Dimension(lowerBound, upperBound, false, false);
	}

	public static Dimension range(int lowerBound, IGeneratable upperBound)
	{
		return new Dimension(numberLiteral(lowerBound), upperBound, false, false);
	}

	public static Dimension staticRange(int lowerBound, int upperBound)
	{
		if (lowerBound < 1)
		{
			throw new IllegalArgumentException("Lower array bound can't be less than 1");
		}
		return new Dimension(numberLiteral(lowerBound), numberLiteral(upperBound), false, false);
	}

	public static Dimension upperBound(IGeneratable upperBound)
	{
		return new Dimension(numberLiteral(1), upperBound, false, false);
	}

	/// X-Array in form of `1:*`
	public static Dimension upperUnbound()
	{
		return new Dimension(numberLiteral(1), plain("*"), false, true);
	}

	///  Array in form of `1:upperBound`
	public static Dimension upperBound(int upperBound)
	{
		return Dimension.staticRange(1, upperBound);
	}

	public boolean isUpperUnbound()
	{
		return upperUnbound;
	}

	public boolean isLowerUnbound()
	{
		return lowerUnbound;
	}

	public IGeneratable lowerBound()
	{
		return lowerBound;
	}

	public IGeneratable upperBound()
	{
		return upperBound;
	}

	@Override
	public String toString()
	{
		return "Dimension[" +
			"lowerBound=" + lowerBound + ", " +
			"upperBound=" + upperBound + ']';
	}

	@Override
	public String generate()
	{
		return "%s:%s".formatted(lowerBound.generate(), upperBound.generate());
	}
}
