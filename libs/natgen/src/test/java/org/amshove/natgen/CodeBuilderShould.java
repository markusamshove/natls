package org.amshove.natgen;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CodeBuilderShould
{
	private final CodeBuilder sut = new CodeBuilder();

	@Test
	void buildSimpleCode()
	{
		sut.append("WRITE 'Hello'");

		assertGenerated("WRITE 'Hello'");
	}

	@Test
	void beAbleToAppendCodeAndThenBreakTheLine()
	{
		sut.appendLine("TEST").append("TEST2");
		assertGenerated("TEST%nTEST2".formatted());
	}

	@Test
	void notBreakTheLineOnSpaceOrBreakWhenLineLengthIsNotExceeded()
	{
		var lotOfChars = "A".repeat(40);
		sut.append(lotOfChars).spaceOrBreak().append("B");
		assertGenerated(lotOfChars + " B");
	}

	@Test
	void breakTheLineOnSpaceOrBreakWhenLineLengthIsExceeded()
	{
		var lotOfChars = "A".repeat(80);
		sut.append(lotOfChars).spaceOrBreak().append("B");
		assertGenerated(lotOfChars + "\nB");
	}

	@Test
	void breakTheLineOnSpaceOrBreakWhenLineLengthWillBeExceededBySpace()
	{
		var lotOfChars = "A".repeat(79);
		sut.append(lotOfChars).spaceOrBreak().append("B");
		assertGenerated(lotOfChars + "\nB");
	}

	@Test
	void increaseTheIndentationLevelForNewLines()
	{
		sut
			.append("First Line")
			.lineBreak()
			.indent()
			.append("Second Line Indented");

		assertGenerated("First Line\n  Second Line Indented");
	}

	@Test
	void beAbleToIncreaseIndentationMultipleTimes()
	{
		sut
			.append("First Line")
			.lineBreak()
			.indent()
			.indent()
			.indent()
			.append("Second Line Indented");

		assertGenerated("First Line\n      Second Line Indented");
	}

	@Test
	void beAbleToDecrement()
	{
		sut
			.append("IF 'A' = 'B'")
			.lineBreak()
			.indent()
			.append("WRITE 'What?'")
			.unindent()
			.lineBreak()
			.append("END-IF");

		assertGenerated("""
			IF 'A' = 'B'
			  WRITE 'What?'
			END-IF""");
	}

	private void assertGenerated(String expected)
	{
		assertThat(sut.toString()).isEqualToIgnoringNewLines(expected);
	}
}
