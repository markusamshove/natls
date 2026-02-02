package org.amshove.natgen;

import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.Subroutine;
import org.junit.jupiter.api.Test;

class SubroutineGenerationShould extends CodeGenerationTest
{
	@Test
	void generateASimpleSubroutine()
	{
		var subroutine = new Subroutine("simple");
		assertGenerated(subroutine, """
			/***********************************************************************
			DEFINE SUBROUTINE simple
			/***********************************************************************

			IGNORE

			END-SUBROUTINE
			""");
	}

	@Test
	void notGenerateTheIgnoreWhenAStatementHasBeenAdded()
	{
		var subroutine = new Subroutine("simple");
		subroutine.addToBody(NaturalCode.assignment(NaturalCode.plain("#VAR"), NaturalCode.plain("#VAR")));

		assertGenerated(subroutine, """
			/***********************************************************************
			DEFINE SUBROUTINE simple
			/***********************************************************************

			#VAR := #VAR

			END-SUBROUTINE
			""");
	}

	@Test
	void generateASubroutineWithMultipleStatements()
	{
		var subroutine = new Subroutine("MULT")
			.addToBody(NaturalCode.ignore())
			.addToBody(NaturalCode.assignment(NaturalCode.plain("#VAR"), NaturalCode.plain("#VAR")));

		assertGenerated(subroutine, """
			/***********************************************************************
			DEFINE SUBROUTINE MULT
			/***********************************************************************

			IGNORE
			#VAR := #VAR

			END-SUBROUTINE
			""");
	}
}
