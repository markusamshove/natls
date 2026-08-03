package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.ResizeAndResetAnalyzer;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MergeResizeAndResetQuickFixShould extends CodeActionTest
{
	@Test
	void mergeBothStatements()
	{
		assertCodeActionWithTitle("Merge RESIZE and RESET", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #ARR (A8/1:*)
			END-DEFINE

			RE${}$SIZE ARRAY #ARR TO (1:5)
			RESET #ARR(*)
			END
			""")
			.fixes(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED.getId())
			.resultsApplied("""
			DEFINE DATA LOCAL
			1 #ARR (A8/1:*)
			END-DEFINE

			RESIZE AND RESET ARRAY #ARR TO (1:5)

			END
			""");
	}

	@Test
	void keepTheResetWhenItResetsOtherStuffToo()
	{
		assertCodeActionWithTitle("Merge RESIZE and RESET", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #ARR (A8/1:*)
			1 #VAR (A10)
			END-DEFINE

			RE${}$SIZE ARRAY #ARR TO (1:5)
			RESET #VAR #ARR(*) #VAR
			END
			""")
			.fixes(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED.getId())
			.resultsApplied("""
			DEFINE DATA LOCAL
			1 #ARR (A8/1:*)
			1 #VAR (A10)
			END-DEFINE

			RESIZE AND RESET ARRAY #ARR TO (1:5)
			RESET #VAR  #VAR
			END
			""");
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new MergeResizeAndResetQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}
}
