package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;

class ResizeAndResetAnalyzerShould extends AbstractAnalyzerTest
{

	protected ResizeAndResetAnalyzerShould()
	{
		super(new ResizeAndResetAnalyzer());
	}

	@Test
	void reportADiagnosticWhenResizingAndResettingAnArray()
	{
		testDiagnostics("""
		DEFINE DATA LOCAL
		1 #ARR (A10/1:*)
		END-DEFINE
		RESIZE ARRAY #ARR TO (1:10)
		RESET #ARR(*)
		END
		""", expectDiagnostic(3, ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED));
	}

	@Test
	void notReportADiagnosticWhenTheResetIsResettingASpecificRange()
	{
		testDiagnostics("""
		DEFINE DATA LOCAL
		1 #ARR (A10/1:*)
		END-DEFINE
		RESIZE ARRAY #ARR TO (1:10)
		RESET #ARR(1:5)
		END
		""", expectNoDiagnosticOfType(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED));
	}

	@Test
	void notReportADiagnosticWhenTheResetIsResettingASingleElement()
	{
		testDiagnostics("""
		DEFINE DATA LOCAL
		1 #ARR (A10/1:*)
		END-DEFINE
		RESIZE ARRAY #ARR TO (1:10)
		RESET #ARR(1)
		END
		""", expectNoDiagnosticOfType(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED));
	}

	@Test
	void notReportADiagnosticWhenTheResetIsNotResettingTheArray()
	{
		testDiagnostics("""
		DEFINE DATA LOCAL
		1 #ALPH (A5)
		1 #ARR (A10/1:*)
		END-DEFINE
		RESIZE ARRAY #ARR TO (1:10)
		RESET #ALPH
		END
		""", expectNoDiagnosticOfType(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED));
	}
}
