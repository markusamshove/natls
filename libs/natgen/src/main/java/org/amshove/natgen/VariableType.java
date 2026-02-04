package org.amshove.natgen;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.IDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class VariableType
{
	private final DataFormat format;
	private final String length;
	private final boolean isDynamic;
	private final List<Dimension> dimensions = new ArrayList<>();

	public static VariableType alphanumeric(int length)
	{
		return new VariableType(DataFormat.ALPHANUMERIC, Integer.toString(length));
	}

	public static VariableType alphanumericDynamic()
	{
		return new VariableType(DataFormat.ALPHANUMERIC, true);
	}

	public static VariableType integer(int length)
	{
		return new VariableType(DataFormat.INTEGER, Integer.toString(length));
	}

	public static VariableType group()
	{
		return new VariableType(DataFormat.NONE);
	}

	private VariableType(DataFormat format, double length)
	{
		this(
			format,
			(int) length == length ? Integer.toString((int) length) : Double.toString(length).replace('.', ',')
		);
	}

	private VariableType(DataFormat format, String length)
	{
		this.format = format;
		this.length = length;
		isDynamic = false;
	}

	private VariableType(DataFormat format, int length)
	{
		this.format = format;
		this.length = Integer.toString(length);
		isDynamic = false;
	}

	private VariableType(DataFormat format)
	{
		this.format = format;
		this.length = "";
		isDynamic = false;
	}

	private VariableType(DataFormat format, boolean isDynamic)
	{
		this.format = format;
		this.length = "";
		this.isDynamic = isDynamic;
	}

	public static VariableType numeric(double length)
	{
		return new VariableType(DataFormat.NUMERIC, length);
	}

	public static VariableType logical()
	{
		return new VariableType(DataFormat.LOGIC);
	}

	public static VariableType binary(int length)
	{
		return new VariableType(DataFormat.BINARY, length);
	}

	public static VariableType binaryDynamic()
	{
		return new VariableType(DataFormat.BINARY, true);
	}

	public static VariableType control()
	{
		return new VariableType(DataFormat.CONTROL);
	}

	public static VariableType date()
	{
		return new VariableType(DataFormat.DATE);
	}

	public static VariableType floating(int length)
	{
		return new VariableType(DataFormat.FLOAT, length);
	}

	public static VariableType packed(double length)
	{
		return new VariableType(DataFormat.PACKED, length);
	}

	public static VariableType time()
	{
		return new VariableType(DataFormat.TIME);
	}

	public static VariableType unicode(int length)
	{
		return new VariableType(DataFormat.UNICODE, length);
	}

	public static VariableType unicodeDynamic()
	{
		return new VariableType(DataFormat.UNICODE, true);
	}

	/// Add an array dimension to this type
	public VariableType withDimension(Dimension dimension)
	{
		dimensions.add(dimension);
		return this;
	}

	public boolean isArray()
	{
		return !dimensions.isEmpty();
	}

	public static VariableType fromDataType(IDataType type)
	{
		return switch (type.format())
		{
			case ALPHANUMERIC -> type.hasDynamicLength() ? VariableType.alphanumericDynamic() : VariableType.alphanumeric((int) type.length());
			case BINARY -> type.hasDynamicLength() ? VariableType.binaryDynamic() : VariableType.binary((int) type.length());
			case CONTROL -> VariableType.control();
			case DATE -> VariableType.date();
			case FLOAT -> VariableType.floating((int) type.length());
			case INTEGER -> VariableType.integer((int) type.length());
			case LOGIC -> VariableType.logical();
			case NUMERIC -> VariableType.numeric(type.length());
			case PACKED -> VariableType.packed(type.length());
			case TIME -> VariableType.time();
			case UNICODE -> type.hasDynamicLength() ? VariableType.unicodeDynamic() : VariableType.unicode((int) type.length());
			default -> throw new IllegalArgumentException(
				"Format <%s> can't be translated to VariableType".formatted(type.format())
			);
		};
	}

	public DataFormat format()
	{
		return format;
	}

	@Override
	public String toString()
	{
		var formattedDimensionList = dimensions.stream().map(Dimension::toDeclaration).collect(Collectors.joining(", "));
		// Group
		if (format == DataFormat.NONE)
		{
			return dimensions.isEmpty() ? "" : "(%s)".formatted(formattedDimensionList);
		}

		var type = "(%s".formatted(format.identifier());

		if (!isDynamic)
		{
			type += length;
		}

		if (isArray())
		{
			type += "/%s".formatted(formattedDimensionList);
		}

		type += ")";

		if (isDynamic)
		{
			type += " DYNAMIC";
		}

		return type;
	}
}
