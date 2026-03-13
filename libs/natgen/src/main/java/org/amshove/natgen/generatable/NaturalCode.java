package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.generatable.conditions.IConditional;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natgen.generators.DefineDataGenerator;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.DecideOn.DecideOnValueCheck;
import org.amshove.natparse.natural.VariableScope;
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

	/// Create new variable with scope LOCAL and level 1.
	public static Variable newLocalVariable(String name, VariableType type)
	{
		return new Variable(1, VariableScope.LOCAL, name, type);
	}

	public static If _if(IConditional conditional)
	{
		return new If(conditional);
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

	public static NaturalCode numberLiteral(String plaintext)
	{
		return plain(plaintext);
	}

	public static NaturalCode numberLiteral(int number)
	{
		return plain(Integer.toString(number));
	}

	public static IGeneratableStatement incrementVariable(IGeneratable variable)
	{
		return new GeneratableStatement("ADD 1 TO " + variable.generate());
	}

	/// Expand a one dimensional array to `1:upperBound`, where `upperBound` can also be a variable
	public static IGeneratableStatement expandArray(IGeneratable array, IGeneratable toUpperBound)
	{
		return new GeneratableStatement(
			"EXPAND ARRAY %s TO (1:%s)".formatted(array.generate(), toUpperBound.generate())
		);
	}

	/// Expand a one dimensional array to `lowerBound:upperBound`
	public static IGeneratableStatement expandArray(IGeneratable array, int lowerBound, int upperBound)
	{
		return new GeneratableStatement(
			"EXPAND ARRAY %s TO (%d:%d)".formatted(array.generate(), lowerBound, upperBound)
		);
	}

	/// Expand the nth dimension of a multidimensional array to `1:upperBound`, where `upperBound` can also be a variable
	public static IGeneratableStatement expandNthArrayDimension(
		IGeneratable array, int nthDimension,
		IGeneratable toUpperBound
	)
	{
		var dimensionList = "*, ".repeat(Math.max(0, nthDimension - 1));
		dimensionList += "1:" + toUpperBound.generate();
		return new GeneratableStatement("EXPAND ARRAY %s TO (%s)".formatted(array.generate(), dimensionList));
	}

	/// Expand the nth dimension of a multidimensional array to `lowerBound:upperBound`
	public static IGeneratableStatement expandNthArrayDimension(
		IGeneratable array, int nthDimension, int lowerBound,
		int upperBound
	)
	{
		var dimensionList = "*, ".repeat(Math.max(0, nthDimension - 1));
		dimensionList += "%d:%d".formatted(lowerBound, upperBound);
		return new GeneratableStatement("EXPAND ARRAY %s TO (%s)".formatted(array.generate(), dimensionList));
	}

	public static IGeneratableStatement reset(IGeneratable... resettable)
	{
		var toReset = Arrays.stream(resettable).map(IGeneratable::generate).collect(Collectors.joining(" "));
		return new GeneratableStatement("RESET %s".formatted(toReset));
	}

	public static Assignment assignment(IGeneratable lhs, IGeneratable rhs)
	{
		return new Assignment(lhs, rhs);
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

	/// Create a new EXAMINE statement
	public static Examine examine(IGeneratable examined)
	{
		return new Examine(examined);
	}

	/// Create a new EXAMINE FULL statement
	public static Examine examineFull(IGeneratable examined)
	{
		return new Examine(examined).asExamineFull();
	}

	public static IGeneratableStatement emptyLine()
	{
		return new GeneratableStatement("");
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

	public static IGeneratableStatement callnat(IGeneratable module, IGeneratable... parameter)
	{
		return new GeneratableStatement(
			"CALLNAT %s %s".formatted(
				module.generate(),
				Arrays.stream(parameter).map(IGeneratable::generate).collect(Collectors.joining(" "))
			)
		);
	}

	public static IGeneratableStatement setGlobals(String key, IGeneratable value)
	{
		return new GeneratableStatement("SET GLOBALS %s=%s".formatted(key, value.generate()));
	}

	/// Create a `MOVE EDITED` statement where `attribute` is set on `target` resulting in
	/// `MOVE EDITED source TO target (EM=editMask)`
	public static IGeneratableStatement moveEdited(IGeneratable source, IGeneratable target, String editMask)
	{
		return new GeneratableStatement(
			"MOVE EDITED %s TO %s (EM=%s)".formatted(source.generate(), target.generate(), editMask)
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

	public static IGeneratableStatement perform(Subroutine subroutine)
	{
		return new GeneratableStatement("PERFORM %s".formatted(subroutine.name()));
	}

	/// Creates a [RequestDocument] statement for the given `uri` saving the HTTP response code into `responseCode`.
	public static RequestDocument requestDocument(IGeneratable uri, IGeneratable responseCode)
	{
		return new RequestDocument(uri, responseCode);
	}

	/// Creates an `*OCC` for the given [IGeneratable]
	public static IGeneratable occ(IGeneratable array)
	{
		return new NaturalCode("*OCC(%s)".formatted(array.generate()));
	}

	/// Creates an `*OCC` for the given [Variable]. If the variable is a group array, the first child will
	/// be used.
	public static IGeneratable occ(Variable array)
	{
		if (array.type().isGroup() && !array.children().isEmpty())
		{
			array = array.children().getFirst();
		}
		return new NaturalCode("*OCC(%s)".formatted(array.generate()));
	}

	/// Create a [For] in the form of `FOR iterationVariable := startValue TO upper`
	public static For _for(IGeneratable iterationVariable, IGeneratable startValue, IGeneratable upper)
	{
		return new For(iterationVariable, startValue, upper);
	}

	/// Creates a [Compress] statement
	public static Compress compress()
	{
		return new Compress();
	}
}
