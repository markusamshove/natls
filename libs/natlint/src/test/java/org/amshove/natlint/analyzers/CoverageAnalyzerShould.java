package org.amshove.natlint.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class CoverageAnalyzerShould extends AbstractAnalyzerTest
{

	private DiagnosticAssertion[] covered(int... lines)
	{
		return Arrays.stream(lines)
			.mapToObj(line -> expectDiagnostic(line, CoverageAnalyzer.COVERED))
			.toArray(DiagnosticAssertion[]::new);

	}

	private DiagnosticAssertion[] all(DiagnosticAssertion[]... assertions)
	{
		List<DiagnosticAssertion> allAssertions = new ArrayList<>();
		Arrays.stream(assertions)
			.forEach(assertionArray ->
			{
				allAssertions.addAll(Arrays.asList(assertionArray));
			});
		return allAssertions.stream().toArray(DiagnosticAssertion[]::new);
	}

	private DiagnosticAssertion[] uncovered(int... lines)
	{
		return Arrays.stream(lines)
			.mapToObj(line -> expectNoDiagnostic(line, CoverageAnalyzer.COVERED))
			.toArray(DiagnosticAssertion[]::new);

	}

	@Test
	void coverEnd()
	{
		testDiagnostics(
			"""
				END
				""",
			all(
				covered(0)
			)
		);
	}

	@Test
	void notCoverEndIf()
	{
		// Reports the start of block statements but not the end of blocks
		testDiagnostics(
			"""
				IF
				  #FISHSTICKS
				  IGNORE
				END-IF

				END
				""",
			all(
				covered(0, 2, 5),
				uncovered(1, 3, 4)
			)
		);
	}

	@Test
	void coverStartOfForLoop()
	{
		testDiagnostics("""
			FOR #I = 1 TO 3
				IGNORE
			END-FOR
			END
			""",
			all(
				covered(0, 1, 3),
				uncovered(2)
			)
		);
	}

	@Test
	void notCoverDecisionStatements()
	{
		testDiagnostics(
			"""
				DECIDE ON FIRST VALUE OF #THING                /* X

                VALUE 3
                  WRITE "This gets covered"                    /* X

                VALUE 12
                  IGNORE                                       /* X

                NONE VALUE
                  WRITE "You have to have a none"              /* X

                END-DECIDE

				END
				""",
			all(
				covered(0, 3, 6, 9, 13),
				uncovered(2, 5, 8, 11, 12)
			)
		);
	}

	@Test
	void coverRepeat()
	{
		testDiagnostics(
			"""
                REPEAT UNTIL #RCOUNT = 0
                  SUBTRACT 1 FROM #RCOUNT
                END-REPEAT
				END
                """,
			all(
				covered(0, 1, 3),
				uncovered(2)
			)
		);
	}

	@Test
	void coverRepeatLabel()
	{
		// The coverage reporter will report the label line
		// but not the REPEAT line (this applies to all labelled loops AFAICT)
		testDiagnostics(
			"""
				LB1.
                REPEAT UNTIl #RCOUNT = 0
                  SUBTRACT 1 FROM #RCOUNT
                END-REPEAT
				END
                """,
			all(
				covered(0, 2, 4),
				uncovered(1, 3)
			)
		);
	}

	@Test
	void coverLastLineOfDataAreaModule()
	{
		testDiagnostics(
			"TESTLDA.NSG", """
                DEFINE DATA GLOBAL
                01 CURSE-EDGE-CASES (A1)
                END-DEFINE
                """,
			all(
				covered(3),
				uncovered(0, 1, 2)
			)
		);
	}

	protected CoverageAnalyzerShould()
	{
		super(new CoverageAnalyzer());
	}
}
