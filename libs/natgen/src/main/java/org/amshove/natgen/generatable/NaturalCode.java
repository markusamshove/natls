package org.amshove.natgen.generatable;

public class NaturalCode implements IGeneratable
{
	private final String code;

	private NaturalCode(String code)
	{
		this.code = code;
	}

	public static NaturalCode plain(String plaintext)
	{
		return new NaturalCode(plaintext);
	}

	public static NaturalCode assignment(IGeneratable lhs, IGeneratable rhs)
	{
		return new NaturalCode("%s := %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static NaturalCode val(IGeneratable val)
	{
		return new NaturalCode("VAL(%s)".formatted(val.generate()));
	}

	@Override
	public String generate()
	{
		return code;
	}
}
