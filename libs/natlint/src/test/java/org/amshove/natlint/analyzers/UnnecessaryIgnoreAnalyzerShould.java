package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.Test;

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

	@Test
	void reportNoDiagnsticIfIgnoreIsNeccessaryToSeparateFromCallnat()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE);
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				CALLNAT 'SUB2'
				IGNORE
				FUNC(<>)
				END
			""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportNoDiagnsticIfIgnoreIsNeccessaryToSeparateFromPerformInternal()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE);
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				PERFORM INTERNAL-SUB
				IGNORE
				FUNC(<>)
				END
			""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportNoDiagnsticIfIgnoreIsNeccessaryToSeparateFromPerformExternal()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE);
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				PERFORM INTERNAL-SUB #VAR
				IGNORE
				FUNC(<>)
				END
			""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}

	@Test
	void reportNoDiagnsticIfIgnoreIsNeccessaryToSeparateFromReset()
	{
		allowParserError(ParserError.UNRESOLVED_MODULE);
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #VAR (N1)
				END-DEFINE
				RESET #VAR
				IGNORE
				FUNC(<>)
				END
			""",
			expectNoDiagnosticOfType(UnnecessaryIgnoreAnalyzer.UNNECESSARY_IGNORE)
		);
	}
}
