package org.amshove.natgen;

import org.amshove.natgen.generatable.NaturalCode;
import org.junit.jupiter.api.Test;

class DecideOnGenerationShould extends CodeGenerationTest
{
	@Test
	void generateAnEmptyDecideOnFirst()
	{
		var decideOn = NaturalCode.decideOnFirst(NaturalCode.plain("#VAR"));

		assertGenerated(decideOn, """
                DECIDE ON FIRST VALUE OF #VAR
                  NONE VALUE
                    IGNORE
                END-DECIDE""");
	}

	@Test
	void generateAnEmptyDecideOnEvery()
	{
		var decideOn = NaturalCode.decideOnEvery(NaturalCode.plain("#VAR"));

		assertGenerated(decideOn, """
                DECIDE ON EVERY VALUE OF #VAR
                  NONE VALUE
                    IGNORE
                END-DECIDE""");
	}

	@Test
	void generateABranchWithMultipleValues()
	{
		var decideOn = NaturalCode.decideOnEvery(NaturalCode.plain("#VAR"));

		var branch = decideOn
			.addBranch(NaturalCode.plain("'A'"), NaturalCode.plain("'B'"));
		branch.addToBody(NaturalCode.plainStatement("WRITE 'Matched A or B'"));

		assertGenerated(decideOn, """
                DECIDE ON EVERY VALUE OF #VAR
                  VALUE 'A', 'B'
                    WRITE 'Matched A or B'
                  NONE VALUE
                    IGNORE
                END-DECIDE""");
	}

	@Test
	void generateMultipleBranches()
	{
		var decideOn = NaturalCode.decideOnFirst(NaturalCode.plain("#VAR"));

		var branchA = decideOn
			.addBranch(NaturalCode.plain("'A'"));
		branchA.addToBody(NaturalCode.plainStatement("WRITE 'Matched A'"));

		var branchB = decideOn
			.addBranch(NaturalCode.plain("'B'"));
		branchB.addToBody(NaturalCode.plainStatement("WRITE 'Matched B'"));

		assertGenerated(decideOn, """
                DECIDE ON FIRST VALUE OF #VAR
                  VALUE 'A'
                    WRITE 'Matched A'
                  VALUE 'B'
                    WRITE 'Matched B'
                  NONE VALUE
                    IGNORE
                END-DECIDE""");
	}

	@Test
	void allowACustomNoneBranch()
	{
		var decide = NaturalCode.decideOnFirst(NaturalCode.plain("#VAR"));

		decide.onNoneValue()
			.addToBody(NaturalCode.plainStatement("WRITE 'No match'"));

		assertGenerated(decide, """
                DECIDE ON FIRST VALUE OF #VAR
                  NONE VALUE
                    WRITE 'No match'
                END-DECIDE""");
	}

	@Test
	void generateAnyAndAllBranches()
	{
		var decide = NaturalCode.decideOnFirst(NaturalCode.plain("#VAR"));

		decide.onAnyValue()
			.addToBody(NaturalCode.plainStatement("WRITE 'Got something in ANY'"));
		decide.onAllValues()
			.addToBody(NaturalCode.plainStatement("WRITE 'Got something in ALL'"));

		assertGenerated(decide, """
                DECIDE ON FIRST VALUE OF #VAR
                  ANY VALUE
                    WRITE 'Got something in ANY'
                  ALL VALUE
                    WRITE 'Got something in ALL'
                  NONE VALUE
                    IGNORE
                END-DECIDE""");
	}
}
