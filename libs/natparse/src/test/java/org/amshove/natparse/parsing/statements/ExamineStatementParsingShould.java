package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.IExamineNode;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.ISubstringOperandNode;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ExamineStatementParsingShould extends StatementParseTest
{

	@ParameterizedTest
	@ValueSource(strings =
	{
		"DELIMITER", "DELIMITERS", "DELIMITER ' '", "DELIMITERS ' '", "DELIMITER #DEL", "DELIMITERS #DEL",
	})
	void parseAnExamineWithDelimiters(String delimiter)
	{
		assertParsesSingleStatement("EXAMINE #VAR FOR #VAR2 WITH %s GIVING INDEX #INDEX".formatted(delimiter), IExamineNode.class);
	}

	@Test
	void parseAComplexExamineReplace()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION FORWARD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND REPLACE FIRST WITH FULL VALUE OF #TAB(*) ", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(32);
	}

	@Test
	void parseAComplexExamineDelete()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION FORWARD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND DELETE FIRST", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(27);
	}

	@Test
	void parseAComplexExamineDeleteGiving()
	{
		var examine = assertParsesSingleStatement("EXAMINE DIRECTION #FWD FULL VALUE OF #DOC STARTING FROM POSITION 7 ENDING AT POSITION 10 FOR FULL VALUE OF PATTERN #HTML(*) WITH DELIMITERS ',' AND DELETE FIRST GIVING INDEX IN #ASD #EFG #HIJ", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(33);
	}

	@Test
	void parseASimpleExamineReplace()
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR 'a' REPLACE 'b'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		assertIsVariableReference(examine.examined(), "#VAR");
	}

	@Test
	void parseAExamineWithMultipleGivings()
	{
		var examine = assertParsesSingleStatement("EXAMINE #DOC FOR FULL VALUE OF 'a' GIVING NUMBER #NUM GIVING POSITION #POS GIVING LENGTH #LEN GIVING INDEX #INDEX", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(19);
	}

	@Test
	void parseAnExamineTranslateStatement()
	{
		var examine = assertParsesSingleStatement("EXAMINE #ASD AND TRANSLATE INTO UPPER CASE", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(7);
	}

	@Test
	void parseAnExamineTranslateUsingStatement()
	{
		var examine = assertParsesSingleStatement("EXAMINE #ASD AND TRANSLATE USING INVERTED #EFG", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(7);
	}

	@Test
	void parseAnExamineWithKeywordThatCanBeIdentifier()
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR FOR 'a' WITH DELIMITER SPACE AND DELETE FIRST INDEX #IX", IExamineNode.class);
		assertThat(examine.descendants().size()).isEqualTo(12);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"FOR 'a' GIVING NUMBER #N POSITION #P LENGTH #L INDEX #I",
		"FOR 'a' GIVING #N POSITION #P LENGTH #L INDEX #I",
		"FOR 'a' AND DELETE FIRST GIVING INDEX #I",
		"FOR 'a' AND DELETE FIRST INDEX #I",
		"FOR #EXAM WITH DELIMITERS GIVING #N INDEX #I",
		"FOR #EXAM WITH DELIMITERS GIVING #N",
		"FOR #EXAM WITH DELIMITERS GIVING NUMBER #N INDEX #I",
		"FOR #EXAM WITH DELIMITERS INDEX #I",
		"FOR #EXAM WITH DELIMITERS SPACE GIVING NUMBER #N POSITION #P LENGTH #L INDEX #I",
		"FOR #EXAM WITH DELIMITERS SPACE GIVING #N POSITION #P LENGTH #L INDEX #I",
		"FOR #EXAM WITH DELIMITERS SPACE GIVING POSITION #P GIVING LENGTH #L INDEX #I",
		"#EXAM WITH DELIMITER REPLACE ' '",
		"#EXAM WITH DELIMITER SPACE REPLACE ' '",
	})
	void parseAnExamineWithDelimtersAndGiving(String statement)
	{
		assertParsesSingleStatement("EXAMINE #VAR %s".formatted(statement), IExamineNode.class);
	}

	@ParameterizedTest
	@CsvSource(
		{
			"GIVING NUMBER #NUM",
			"NUMBER #NUM",
			"NUMBER IN #NUM",
			"GIVING IN #NUM",
			"GIVING NUMBER IN #NUM",
			"GIVING #NUM"
		}
	)
	void parseAnExamineWithGivingNumber(String extra)
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR FOR ',' %s".formatted(extra), IExamineNode.class);
		assertIsVariableReference(examine.givingNumber(), "#NUM");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"GIVING POSITION IN #NUM",
			"GIVING POSITION #NUM",
			"POSITION IN #NUM",
			"POSITION #NUM"
		}
	)
	void parseAnExamineWithGivingPosition(String extra)
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR FOR ',' %s".formatted(extra), IExamineNode.class);
		assertIsVariableReference(examine.givingPosition(), "#NUM");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"GIVING LENGTH IN #NUM",
			"GIVING LENGTH #NUM",
			"LENGTH IN #NUM",
			"LENGTH #NUM"
		}
	)
	void parseAnExamineWithGivingLength(String extra)
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR FOR ',' %s".formatted(extra), IExamineNode.class);
		assertIsVariableReference(examine.givingLength(), "#NUM");
	}

	@ParameterizedTest
	@CsvSource(
		{
			"GIVING INDEX IN #NUM",
			"GIVING INDEX #NUM",
			"INDEX IN #NUM",
			"INDEX #NUM"
		}
	)
	void parseAnExamineWithIndex(String extra)
	{
		var examine = assertParsesSingleStatement("EXAMINE #VAR(*) FOR ',' %s".formatted(extra), IExamineNode.class);
		assertThat(examine.givingIndex()).hasSize(1);
		assertIsVariableReference(examine.givingIndex().first(), "#NUM");
	}

	@Test
	void parseAnExamineWithMultipleIndices()
	{
		var examine = assertParsesSingleStatement("EXAMINE #ARR FOR ',' GIVING INDEX #I1 #I2 #I3", IExamineNode.class);
		assertThat(examine.givingIndex()).hasSize(3);
		assertIsVariableReference(examine.givingIndex().get(0), "#I1");
		assertIsVariableReference(examine.givingIndex().get(1), "#I2");
		assertIsVariableReference(examine.givingIndex().get(2), "#I3");
	}

	@Test
	void parseAnExamineWithSubstring()
	{
		var examine = assertParsesSingleStatement("EXAMINE SUBSTR(#VAR, 1, 5) FOR 'a'", IExamineNode.class);
		assertThat(examine.examined()).isNotNull();
		var substringOperand = assertNodeType(examine.examined(), ISubstringOperandNode.class);
		assertIsVariableReference(substringOperand.operand(), "#VAR");
		assertThat(assertNodeType(substringOperand.startPosition().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(1);
		assertThat(assertNodeType(substringOperand.length().orElseThrow(), ILiteralNode.class).token().intValue()).isEqualTo(5);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"FOR CHARPOSITION #POS", "FOR CHARPOSITION #POS CHARLENGTH #LEN", "CHARLENGTH #LEN", "FOR CHARLENGTH #LEN"
	})
	void parseUnicodeVariantOfExamine(String permutation)
	{
		assertParsesSingleStatement("EXAMINE #VAR %s GIVING LENGTH #RESLEN".formatted(permutation), IExamineNode.class);
	}
}
