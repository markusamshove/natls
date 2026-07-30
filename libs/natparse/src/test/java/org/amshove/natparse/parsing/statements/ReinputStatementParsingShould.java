package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IReinputStatementNode;
import org.amshove.natparse.natural.IValueAttributeNode;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReinputStatementParsingShould extends StatementParseTest
{

	@Test
	void parseMinimum()
	{
		var reinput = assertParsesSingleStatement("REINPUT USING HELP", IReinputStatementNode.class);
		assertThat(reinput.isUsingHelp()).isTrue();
	}

	@Test
	void parseFull()
	{
		var reinput = assertParsesSingleStatement("REINPUT FULL USING HELP", IReinputStatementNode.class);
		assertThat(reinput.isUsingHelp()).isTrue();
	}

	@Test
	void parseStatementAttributes()
	{
		var reinput = assertParsesSingleStatement("REINPUT (AD=I) USING HELP", IReinputStatementNode.class);
		var attribute = assertNodeType(reinput.statementAttributes().first(), IValueAttributeNode.class);

		assertThat(attribute.kind()).isEqualTo(SyntaxKind.AD);
		assertThat(attribute.value()).isEqualTo("I");
	}

	@Test
	void parseWithText() {
		assertParsesSingleStatement("REINPUT WITH TEXT 'try again'", IReinputStatementNode.class);
	}

	@Test
	void parseWithTextWithoutWithText() {
		assertParsesSingleStatement("REINPUT 'try again'", IReinputStatementNode.class);
	}

	@Test
	void parseOutputAttributes() {
		assertParsesSingleStatement("REINPUT 'try again' (CD=GR)", IReinputStatementNode.class);
	}

	@Test
	void parseFormatOperands() {
		assertParsesSingleStatement("REINPUT 'try again for the :1:th time', 5", IReinputStatementNode.class);
		assertParsesSingleStatement("REINPUT 'try :1:x harder for the :2: reasons', 5, 'best'", IReinputStatementNode.class);
	}

	@Test
	void parseMark() {
		assertParsesSingleStatement("REINPUT USING HELP MARK POSITION 4 IN FIELD *BADGER", IReinputStatementNode.class);
	}

	@Test
	void parseMarkExtraFieldsWithAttributes() {
		assertParsesSingleStatement("""
			REINPUT USING HELP MARK
				*BADGER (CV=GR)
				*SQUIRREL (CV=BL)
			""", IReinputStatementNode.class);
	}

	@Test
	void parseAlarm() {
		assertParsesSingleStatement("REINPUT USING HELP AND SOUND ALARM", IReinputStatementNode.class);
		assertParsesSingleStatement("REINPUT USING HELP AND ALARM", IReinputStatementNode.class);
		assertParsesSingleStatement("REINPUT USING HELP SOUND ALARM", IReinputStatementNode.class);
		assertParsesSingleStatement("REINPUT USING HELP ALARM", IReinputStatementNode.class);
	}
}
