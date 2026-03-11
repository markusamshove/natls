package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class FunctionFilenameAnalyzerShould extends AbstractAnalyzerTest
{
	public FunctionFilenameAnalyzerShould()
	{
		super(new FunctionFilenameAnalyzer());
	}

	@Test
	void raiseADiagnosticIfFilenameAndFunctionNameDiffer()
	{
		configureEditorConfig("""
			[*]
			natls.style.function_name_should_match_file_name=true
			""");
		testDiagnostics(
			"FUNCC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			FUNC := TRUE
			END-FUNCTION
			END
			""",
			expectDiagnostic(0, FunctionFilenameAnalyzer.FUNCTION_FILE_NAME_MISMATCH)
		);
	}

	@Test
	void notRaiseADiagnosticIfFilenameAndFunctionNameDifferButSettingIsTurnedOff()
	{
		configureEditorConfig("""
			[*]
			natls.style.function_name_should_match_file_name=false
			""");
		testDiagnostics(
			"FUNCC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			FUNC := TRUE
			END-FUNCTION
			END
			""",
			expectNoDiagnosticOfType(FunctionFilenameAnalyzer.FUNCTION_FILE_NAME_MISMATCH)
		);
	}

	@Test
	void raiseNoDiagnosticIfFilenameAndFunctionNameMatch()
	{
		testDiagnostics(
			"FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			FUNC := TRUE
			END-FUNCTION
			END
			""",
			expectNoDiagnosticOfType(FunctionFilenameAnalyzer.FUNCTION_FILE_NAME_MISMATCH)
		);
	}

	@Test
	void raiseNoDiagnosticIfFileIsNotFunction()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""",
			expectNoDiagnosticOfType(FunctionFilenameAnalyzer.FUNCTION_FILE_NAME_MISMATCH)
		);
	}
}
