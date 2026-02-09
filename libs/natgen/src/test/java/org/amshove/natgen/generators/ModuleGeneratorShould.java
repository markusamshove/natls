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
			.isEqualToNormalizingNewlines("""
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
			.isEqualToNormalizingNewlines("""
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
			.isEqualToNormalizingNewlines("""
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

	@Test
	void generateAFunctionWithReturnType()
	{
		var parameter = context.addParameter("#P-NAME", VariableType.alphanumericDynamic());
		var returnType = VariableType.alphanumericDynamic();

		context.addStatement(NaturalCode.assignment(NaturalCode.plain("EMPTYA"), NaturalCode.stringLiteral(" ")));
		context.addStatement(NaturalCode.assignment(parameter, NaturalCode.stringLiteral(" ")));

		assertThat(sut.generateFunction(context, "EMPTYA", returnType))
			.isEqualToNormalizingNewlines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE FUNCTION EMPTYA
				  RETURNS (A) DYNAMIC

				DEFINE DATA
				PARAMETER
				1 #P-NAME (A) DYNAMIC
				END-DEFINE

				EMPTYA := ' '
				#P-NAME := ' '

				END-FUNCTION
				END
				""");
	}

	@Test
	void generateAFunctionWithoutAReturnType()
	{
		var parameter = context.addParameter("#P-NAME", VariableType.alphanumericDynamic());

		context.addStatement(NaturalCode.assignment(parameter, NaturalCode.stringLiteral(" ")));

		assertThat(sut.generateFunction(context, "EMPTYA", null))
			.isEqualToNormalizingNewlines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE FUNCTION EMPTYA

				DEFINE DATA
				PARAMETER
				1 #P-NAME (A) DYNAMIC
				END-DEFINE

				#P-NAME := ' '

				END-FUNCTION
				END
				""");
	}

	@Test
	void generateAnExternalSubroutine()
	{
		var parameter = context.addParameter("#P-NAME", VariableType.alphanumericDynamic());

		context.addStatement(NaturalCode.assignment(parameter, NaturalCode.stringLiteral(" ")));
		context.addUsing(VariableScope.LOCAL, "LALDA");

		assertThat(sut.generateSubroutine(context, "LA-SUBROUTINA"))
			.isEqualToNormalizingNewlines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE DATA
				PARAMETER
				1 #P-NAME (A) DYNAMIC
				LOCAL USING LALDA
				END-DEFINE
				
				/***********************************************************************
				DEFINE SUBROUTINE LA-SUBROUTINA
				/***********************************************************************

				#P-NAME := ' '

				END-SUBROUTINE
				END
				""");
	}

	@Test
	void generateAnExternalSubroutineWithNestedSubroutines()
	{
		var parameter = context.addParameter("#P-NAME", VariableType.alphanumericDynamic());

		var nested = NaturalCode.subroutine("NESTED-SUBROUTINE");
		nested.addToBody(NaturalCode.assignment(parameter, NaturalCode.stringLiteral(" ")));

		context.addStatement(NaturalCode.perform(nested));
		context.addStatement(nested);

		assertThat(sut.generateSubroutine(context, "LA-SUBROUTINA"))
			.isEqualToNormalizingNewlines("""
				/* >Natural Source Header 000000
				/* :Mode S
				/* :CP
				/* <Natural Source Header
				DEFINE DATA
				PARAMETER
				1 #P-NAME (A) DYNAMIC
				END-DEFINE
				
				/***********************************************************************
				DEFINE SUBROUTINE LA-SUBROUTINA
				/***********************************************************************
				
				PERFORM NESTED-SUBROUTINE

				/***********************************************************************
				DEFINE SUBROUTINE NESTED-SUBROUTINE
				/***********************************************************************

				#P-NAME := ' '

				END-SUBROUTINE


				END-SUBROUTINE
				END
				""");
	}

}
