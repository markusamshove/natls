package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.junit.jupiter.api.Test;

import static org.amshove.natgen.generatable.NaturalCode.plain;
import static org.amshove.natgen.generatable.NaturalCode.stringLiteral;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompressGenerationShould extends CodeGenerationTest
{
	@Test
	void generateASimpleCompress()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.withOperand(stringLiteral("First"))
			.withOperand(stringLiteral("Second"));

		assertGenerated(compress, """
			COMPRESS 'First' 'Second' INTO #TARGET""");
	}

	@Test
	void generateASimpleCompressNumeric()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.numeric()
			.withOperand(plain("10.0"))
			.withOperand(stringLiteral("Second"));

		assertGenerated(compress, """
			COMPRESS NUMERIC 10.0 'Second' INTO #TARGET""");
	}

	@Test
	void generateASimpleCompressFull()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.full()
			.withOperand(plain("10.0"))
			.withOperand(stringLiteral("Second"));

		assertGenerated(compress, """
			COMPRESS FULL 10.0 'Second' INTO #TARGET""");
	}

	@Test
	void generateASimpleCompressWithLeavingNo()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.leavingNoSpace()
			.withOperand(stringLiteral("First"))
			.withOperand(stringLiteral("Second"));

		assertGenerated(compress, """
			COMPRESS 'First' 'Second' INTO #TARGET LEAVING NO SPACE""");
	}

	@Test
	void beAbleToOverrideLeavingNoSpace()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.leavingNoSpace()
			.leavingSpace()
			.withOperand(stringLiteral("First"))
			.withOperand(stringLiteral("Second"));

		assertGenerated(compress, """
			COMPRESS 'First' 'Second' INTO #TARGET""");
	}

	@Test
	void breakTheCompressWithLotsOfOperands()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.leavingNoSpace()
			.withOperand(stringLiteral("Lets"))
			.withOperand(stringLiteral("compress"))
			.withOperand(stringLiteral("some"))
			.withOperand(stringLiteral("strings"))
			.withOperand(stringLiteral("together"))
			.withOperand(stringLiteral("shall we?"))
			.withOperand(stringLiteral("Then lets do this now"))
			.withOperand(stringLiteral("so that the operands may break"))
			.withOperand(stringLiteral("the line because it gets"))
			.withOperand(stringLiteral("very long"))
			.withOperand(stringLiteral("very fast"));

		assertGenerated(compress, """
			COMPRESS 'Lets' 'compress' 'some' 'strings' 'together' 'shall we?' 'Then lets do this now'
			  'so that the operands may break' 'the line because it gets' 'very long' 'very fast'
			  INTO #TARGET
			  LEAVING NO SPACE""");
	}

	@Test
	void generateWithDelimiters()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.withOperand(stringLiteral("First"))
			.withOperand(stringLiteral("Second"))
			.withDelimiters(stringLiteral(";"));

		assertGenerated(compress, """
			COMPRESS 'First' 'Second' INTO #TARGET WITH DELIMITERS ';'""");
	}

	@Test
	void generateWithAllDelimiters()
	{
		var into = NaturalCode.newLocalVariable("#TARGET", VariableType.alphanumeric(10));
		var compress = NaturalCode.compress()
			.into(into)
			.withOperand(stringLiteral("First"))
			.withOperand(stringLiteral("Second"))
			.withAllDelimiters(stringLiteral(";"));

		assertGenerated(compress, """
			COMPRESS 'First' 'Second' INTO #TARGET WITH ALL DELIMITERS ';'""");
	}

	@Test
	void throwAnExceptionIfNoIntoTargetIsSpecified()
	{
		var compress = NaturalCode.compress();
		assertThatThrownBy(() -> compress.generateInto(new CodeBuilder()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Can not generate COMPRESS statement without into operand. Make sure to call `into`");
	}

	@Test
	void throwAnExceptionIfNoOperandIsSpecified()
	{
		var compress = NaturalCode.compress().into(NaturalCode.newLocalVariable("#INTO", VariableType.alphanumericDynamic()));
		assertThatThrownBy(() -> compress.generateInto(new CodeBuilder()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Can not generate COMPRESS statement without operands. Make sure to call `withOperand` at least once");
	}
}
