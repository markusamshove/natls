package org.amshove.natlint.analyzers;

import org.amshove.natlint.linter.AbstractAnalyzerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueTruncationAnalyzerShould extends AbstractAnalyzerTest
{

	@Test
	void reportThatAnythingFitsInADynamic()
	{
		testDiagnostics(
			"""
			DEFINE DATA LOCAL
			01 #A-HOLE (A) DYNAMIC
			01 #U-HOLE (U) DYNAMIC
			01 #B-HOLE (B) DYNAMIC
			END-DEFINE

			#A-HOLE := 'A'
			#U-HOLE := 'U'
			#B-HOLE := H'42'

			END
			""", expectNoDiagnostic(6, ValueTruncationAnalyzer.VALUE_TRUNCATED), expectNoDiagnostic(7, ValueTruncationAnalyzer.VALUE_TRUNCATED), expectNoDiagnostic(8, ValueTruncationAnalyzer.VALUE_TRUNCATED)

		);
	}

	@Test
	void raiseADiagnosticWhenAConstInitializerIsTruncatedForCompatibleFormats()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #C-CONST (A1) CONST<20>
			END-DEFINE
			""", expectDiagnostic(1, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void raiseADiagnosticWhenAnInitInitializerIsTruncatedForCompatibleFormats()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #C-CONST (A1) INIT<20>
			END-DEFINE
			""", expectDiagnostic(1, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void reportADiagnosticWhenNumericAssignmentValuesGetTruncated()
	{
		testDiagnostics(
			"""
				DEFINE DATA LOCAL
				1 #CONST-N1-I4 (I4) CONST<1>
				1 #N1 (N1)
				1 #I1 (I1)
				END-DEFINE
				#N1 := 23
				#I1 := 128
				END
				""",
			expectDiagnostic(5, ValueTruncationAnalyzer.VALUE_TRUNCATED),
			expectDiagnostic(6, ValueTruncationAnalyzer.VALUE_TRUNCATED)
		);
	}

	@Test
	void reportADiagnosticWhenNumericAssignmentValuesGetTruncatedWithCompatibleTargetFormat()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #CONST-N1-I4 (I4) CONST<1>
			1 #A1 (A1)
			END-DEFINE
			#A1 := 10
			END
			""", expectDiagnostic(4, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@Test
	void reportADiagnosticWhenAlphanumericAssignmentValuesGetTruncated()
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #A1 (A1)
			END-DEFINE
			#A1 := 'AB'
			END
			""", expectDiagnostic(3, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'AB'",
		"H'4142'",
		"U'AB'",
		"UH'00410042'"
	})
	void reportADiagnosticWhenAlphaTruncatesAlphaFamily(String value)
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #A1 (A1)
			END-DEFINE
			#A1 := %s
			END
			""".formatted(value), expectDiagnostic(3, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'AB'",
		"H'4142'",
		"U'AB'",
		"UH'00410042'"
	})
	void reportADiagnosticWhenUnicodeTruncatesAlphaFamily(String value)
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #U1 (U1)
			END-DEFINE
			#U1 := %s
			END
			""".formatted(value), expectDiagnostic(3, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"'AB'",
		"H'4142'",
		"U'A'",
		"U'AB'",
		"UH'0041'",
		"UH'00410042'"
	})
	void reportDiagnosticWhenStringsTruncatedByBinary(String value)
	{
		testDiagnostics("""
			DEFINE DATA LOCAL
			1 #B1 (B1)
			END-DEFINE
			#B1 := %s
			END
			""".formatted(value), expectDiagnostic(3, ValueTruncationAnalyzer.VALUE_TRUNCATED));
	}

	protected ValueTruncationAnalyzerShould()
	{
		super(new ValueTruncationAnalyzer());
	}
}
