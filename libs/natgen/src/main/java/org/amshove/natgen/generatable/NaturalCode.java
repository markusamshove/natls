package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.DefineDataGenerator;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn.DecideOnValueCheck;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

@NullMarked
public class NaturalCode implements IGeneratable
{
	private final String code;

	private NaturalCode(String code)
	{
		this.code = code;
	}

	public static IGeneratableStatement separatorComment()
	{
		return new GeneratableStatement("/***********************************************************************");
	}

	public static IGeneratableStatement lineComment(String comment)
	{
		return new GeneratableStatement("/* %s".formatted(comment));
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

	public static IGeneratableStatement plainStatement(String plaintext)
	{
		return new GeneratableStatement(plaintext);
	}

	public static NaturalCode stringLiteral(String plaintext)
	{
		return new NaturalCode("'%s'".formatted(plaintext));
	}

	public static IGeneratableStatement incrementVariable(IGeneratable variable)
	{
		return new GeneratableStatement("ADD 1 TO " + variable.generate());
	}

	/// Expand a one dimensional array to `1:upperBound`, where `upperBound` can also be a variable
	public static IGeneratableStatement expandArray(IGeneratable array, IGeneratable toUpperBound)
	{
		return new GeneratableStatement("EXPAND ARRAY %s TO (1:%s)".formatted(array.generate(), toUpperBound.generate()));
	}

	/// Expand a one dimensional array to `lowerBound:upperBound`
	public static IGeneratableStatement expandArray(IGeneratable array, int lowerBound, int upperBound)
	{
		return new GeneratableStatement("EXPAND ARRAY %s TO (%d:%d)".formatted(array.generate(), lowerBound, upperBound));
	}

	/// Expand the nth dimension of a multidimensional array to `1:upperBound`, where `upperBound` can also be a variable
	public static IGeneratableStatement expandNthArrayDimension(IGeneratable array, int nthDimension, IGeneratable toUpperBound)
	{
		var dimensionList = "*,".repeat(Math.max(0, nthDimension - 1));
		dimensionList += "1:" + toUpperBound.generate();
		return new GeneratableStatement("EXPAND ARRAY %s TO (%s)".formatted(array.generate(), dimensionList));
	}

	/// Expand the nth dimension of a multidimensional array to `lowerBound:upperBound`
	public static IGeneratableStatement expandNthArrayDimension(IGeneratable array, int nthDimension, int lowerBound, int upperBound)
	{
		var dimensionList = "*,".repeat(Math.max(0, nthDimension - 1));
		dimensionList += "%d:%d".formatted(lowerBound, upperBound);
		return new GeneratableStatement("EXPAND ARRAY %s TO (%s)".formatted(array.generate(), dimensionList));
	}

	public static IGeneratableStatement assignment(IGeneratable lhs, IGeneratable rhs)
	{
		return new GeneratableStatement("%s := %s".formatted(lhs.generate(), rhs.generate()));
	}

	public static NaturalCode definePrototype(
		IGeneratable name, @Nullable VariableType returnType,
		CodeGenerationContext context
	)
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

	public static IGeneratableStatement ignore()
	{
		return new GeneratableStatement("IGNORE");
	}

	/// Create a new PARSE JSON statement. `referenceToJsonSource` is the Natural variable containing the JSON to
	/// parse.
	public static ParseJson parseJson(IGeneratable referenceToJsonSource)
	{
		return new ParseJson(referenceToJsonSource);
	}

	/// Create a new DECIDE ON statement for checking the *FIRST* value of the given field.
	public static DecideOn decideOnFirst(IGeneratable reference)
	{
		return new DecideOn(reference, DecideOnValueCheck.FIRST);
	}

	/// Create a new DECIDE ON statement for checking the *EVERY* value of the given field.
	public static DecideOn decideOnEvery(IGeneratable reference)
	{
		return new DecideOn(reference, DecideOnValueCheck.EVERY);
	}

	public static NaturalCode functionCall(String functionName, IGeneratable... parameter)
	{
		return new NaturalCode(
			"%s(<%s>)".formatted(
				functionName,
				Arrays.stream(parameter).map(IGeneratable::generate).collect(Collectors.joining(", "))
			)
		);
	}

	private record GeneratableStatement(String plainCode) implements IGeneratableStatement
	{
		@Override
		public void generateInto(CodeBuilder builder)
		{
			builder.append(plainCode);
		}
	}
}
