package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnnecessaryIgnoreAnalyzerShould extends AbstractAnalyzerTest
{
	protected UnnecessaryIgnoreAnalyzerShould()
	{
		super(new UnnecessaryIgnoreAnalyzer());
	}

	@Test
	void reportADiagnosticIfIgnoreIsUnnecessaryInIfs()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			IF TRUE
			  WRITE 'Hi'
			  IGNORE
			END-IF
			END
			""",
			expectDiagnostic(4, UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportNoDiagnosticIfIgnoreIsNeccessaryInIfs()
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				END-DEFINE
				IF TRUE
				  IGNORE
				END-IF
				END
				""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportADiagnosticIfIgnoreIsTheOnlyStatementInAModule()
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				END-DEFINE
				IGNORE
				END
				""",
			expectDiagnostic(2, UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportNoDiagnosticIfIgnoreIsNeccessaryInDecideBlock()
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				DECIDE ON FIRST VALUE OF #VAR
				  VALUE 1
				    IGNORE
				  VALUE 2
				    IGNORE
				  NONE VALUE
				    IGNORE
				  ANY VALUE
				    IGNORE
				END-DECIDE
				END
				""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"CALLNAT 'SUB2'",
		"CALLNAT 'SUB2' #VAR",
		"PERFORM INTERNAL-SUB",
		"PERFORM EXTERNAL-SUB #VAR",
		"RESET #VAR",
		"RESET INITIAL #VAR"
	})
	void reportNoDiagnsticIfIgnoreIsNeccessaryToSeparatePossibleOperandListsFromFunctionCalls(String previousStatement)
	{
		// The developer does not intend to have the function call as argument to the operand list.
		// To separate those two, an IGNORE statement is neccessary.

		allowParserError(ParserError.UNRESOLVED_MODULE);
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				%s
				IGNORE
				FUNC(<>)
				END
			""".formatted(previousStatement),
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}
}
