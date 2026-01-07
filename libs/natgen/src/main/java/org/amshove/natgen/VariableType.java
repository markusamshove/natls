package org.amshove.natgen;

import org.amshove.natparse.natural.DataFormat;

public final class VariableType
{
	private final DataFormat format;
	private final String length;
	private final boolean isDynamic;

	public static VariableType alphanumeric(int length)
	{
		return new VariableType(DataFormat.ALPHANUMERIC, Integer.toString(length));
	}

	public static VariableType alphanumericDynamic()
	{
		return new VariableType(DataFormat.ALPHANUMERIC);
	}

	public static VariableType integer(int length)
	{
		return new VariableType(DataFormat.INTEGER, Integer.toString(length));
	}

	public static VariableType group()
	{
		return new VariableType(DataFormat.NONE);
	}

	private VariableType(DataFormat format, String length)
	{
		this.format = format;
		this.length = length;
		isDynamic = false;
	}

	private VariableType(DataFormat format)
	{
		this.format = format;
		this.length = "";
		isDynamic = true;
	}

	public DataFormat format()
	{
		return format;
	}

	@Override
	public String toString()
	{
		// Group
		if (format == DataFormat.NONE)
		{
			return "";
		}

		if (isDynamic)
		{
			return "(%s) DYNAMIC".formatted(format.identifier());
		}

		return "(%s%s)".formatted(format.identifier(), length);
	}
}
