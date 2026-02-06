package org.amshove.natgen.generators;

import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ModuleGeneratorShould
{
	private final CodeGenerationContext context = new CodeGenerationContext();
	private final ModuleGenerator sut = new ModuleGenerator();

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LOCAL", "PARAMETER", "GLOBAL"
	})
	void generateDataAreas(String expectedScope)
	{
		var fileType = switch (expectedScope)
		{
			case "LOCAL" -> NaturalFileType.LDA;
			case "PARAMETER" -> NaturalFileType.PDA;
			case "GLOBAL" -> NaturalFileType.GDA;
			default -> throw new IllegalArgumentException("Unexpected scope: " + expectedScope);
		};

		context
			.addVariable(VariableScope.LOCAL, "MY-VAR", VariableType.group())
			.addVariable("SUB", VariableType.alphanumericDynamic());

		assertThat(sut.generate(context, fileType))
			.isEqualToIgnoringNewLines("""
				DEFINE DATA %s
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				1 MY-VAR
				  2 SUB (A) DYNAMIC
				END-DEFINE
				""".formatted(expectedScope));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"PROGRAM", "SUBPROGRAM"
	})
	void generateProgramTypes(String fileType)
	{
		var naturalFileType = switch (fileType)
		{
			case "PROGRAM" -> NaturalFileType.PROGRAM;
			case "SUBPROGRAM" -> NaturalFileType.SUBPROGRAM;
			default -> throw new IllegalArgumentException("Unexpected file type: " + fileType);
		};

		context.addUsing(VariableScope.PARAMETER, "MYPDA");
		context.addUsing(VariableScope.LOCAL, "MYLDA");
		context.addVariable(VariableScope.LOCAL, "#VAR", VariableType.alphanumeric(10));

		context.addStatement(NaturalCode.ignore());

		assertThat(sut.generate(context, naturalFileType))
			.isEqualToIgnoringNewLines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE DATA
				PARAMETER USING MYPDA
				LOCAL USING MYLDA
				LOCAL
				1 #VAR (A10)
				END-DEFINE

				IGNORE

				END
				""");
	}

	@Test
	void generateAProgramWithSomeStatements()
	{
		var variable = context.addVariable(VariableScope.LOCAL, "MY-VAR", VariableType.integer(4));

		var subroutine = NaturalCode.subroutine("MY-SUBROUTINE");
		subroutine
			.addToBody(NaturalCode.assignment(variable, NaturalCode.plain("42")))
			.addToBody(NaturalCode.incrementVariable(variable));

		context.addStatement(NaturalCode.perform(subroutine));
		context.addStatement(NaturalCode.plainStatement("WRITE MY-VAR"));
		context.addStatement(subroutine);

		assertThat(sut.generate(context, NaturalFileType.PROGRAM))
			.isEqualToIgnoringNewLines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE DATA
				LOCAL
				1 MY-VAR (I4)
				END-DEFINE

				PERFORM MY-SUBROUTINE
				WRITE MY-VAR

				/***********************************************************************
				DEFINE SUBROUTINE MY-SUBROUTINE
				/***********************************************************************

				MY-VAR := 42
				ADD 1 TO MY-VAR

				END-SUBROUTINE

				END
				""");
	}
}
