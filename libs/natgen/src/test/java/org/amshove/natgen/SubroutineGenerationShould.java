package org.amshove.natgen;

import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.Subroutine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SubroutineGenerationShould
{
	@Test
	void generateASimpleSubroutine()
	{
		var subroutine = new Subroutine("simple");
		assertThat(subroutine.generate())
			.isEqualTo("""
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
		assertThat(subroutine.generate())
			.isEqualTo("""
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

		assertThat(subroutine.generate())
			.isEqualTo("""
			/***********************************************************************
			DEFINE SUBROUTINE MULT
			/***********************************************************************

			IGNORE
			#VAR := #VAR

			END-SUBROUTINE
			""");
	}
}
