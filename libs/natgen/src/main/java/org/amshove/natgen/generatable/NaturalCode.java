package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.DefineDataGenerator;
import org.amshove.natgen.VariableType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NaturalCode implements IGeneratable
{
	private final String code;

	private NaturalCode(String code)
	{
		this.code = code;
	}

	@Override
	public String generate()
	{
		return code;
	}

	public static NaturalCode plain(String plaintext)
	{
		return new NaturalCode(plaintext);
	}

	public static NaturalCode assignment(IGeneratable lhs, IGeneratable rhs)
	{
		return new NaturalCode("%s := %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static NaturalCode definePrototype(IGeneratable name, @Nullable VariableType returnType, CodeGenerationContext context)
	{
		var returnCode = returnType != null ? " RETURNS %s".formatted(returnType) : "";
		var defineData = context.parameter().isEmpty()
			? ""
			: new DefineDataGenerator().generate(context) + System.lineSeparator();

		return new NaturalCode("""
			DEFINE PROTOTYPE %s%s
			%sEND-PROTOTYPE""".formatted(name.generate(), returnCode, defineData));
	}

	public static NaturalCode val(IGeneratable val)
	{
		return new NaturalCode("VAL(%s)".formatted(val.generate()));
	}

	public static Subroutine subroutine(String name)
	{
		return new Subroutine(name);
	}

	public static NaturalCode ignore()
	{
		return new NaturalCode("IGNORE");
	}
}
