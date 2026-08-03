package org.amshove.natgen.generatable;

public record PlainGeneratable(String plaintext) implements IGeneratable
{
	@Override
	public String generate()
	{
		return plaintext;
	}
}
