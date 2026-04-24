package org.amshove.natgen.generatable.conditions;

import org.amshove.natgen.generatable.IGeneratable;

public class Conditions
{
	public static IConditional _true()
	{
		return new PlainConditional("TRUE");
	}

	public static IConditional _false()
	{
		return new PlainConditional("FALSE");
	}

	public static IConditional specified(IGeneratable parameter)
	{
		return new PlainConditional("%s SPECIFIED".formatted(parameter.generate()));
	}

	public static IConditional notSpecified(IGeneratable parameter)
	{
		return new PlainConditional("%s NOT SPECIFIED".formatted(parameter.generate()));
	}

	public static IConditional not(IConditional conditional)
	{
		return new PlainConditional("NOT %s".formatted(conditional.generate()));
	}

	public static IConditional equal(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s = %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static IConditional notEqual(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s <> %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static IConditional greaterThan(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s > %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static IConditional greaterEqual(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s >= %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static IConditional lessThan(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s < %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static IConditional lessEqual(IGeneratable lhs, IGeneratable rhs)
	{
		return new PlainConditional("%s <= %s".formatted(lhs.generate(), rhs.generate()));
	}

	private record PlainConditional(String source) implements IConditional
	{
		@Override
		public String generate()
		{
			return source;
		}
	}
}
