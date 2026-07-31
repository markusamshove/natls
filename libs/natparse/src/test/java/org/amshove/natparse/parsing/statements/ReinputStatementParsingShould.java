package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ILiteralNode;
import org.amshove.natparse.natural.IReinputStatementNode;
import org.amshove.natparse.natural.IValueAttributeNode;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
	void parseWithText()
	{
		var reinput = assertParsesSingleStatement("REINPUT WITH TEXT 'try again'", IReinputStatementNode.class);
		assertThat(((ILiteralNode) reinput.messageOperand()).token()).returns("try again", from(SyntaxToken::stringValue));
	}

	@Test
	void parseWithTextWithoutWithText()
	{
		var reinput = assertParsesSingleStatement("REINPUT 'try again'", IReinputStatementNode.class);
		assertThat(((ILiteralNode) reinput.messageOperand()).token()).returns("try again", from(SyntaxToken::stringValue));
	}

	@Test
	void parseOutputAttributes()
	{
		assertParsesSingleStatement("REINPUT 'try again' (CD=GR)", IReinputStatementNode.class);
	}

	@Test
	void parseFormatOperands()
	{
		assertParsesSingleStatement("REINPUT 'try again for the :1:th time', 5", IReinputStatementNode.class);
		var reinput = assertParsesSingleStatement("REINPUT 'try :1:x harder for the :2: reasons', 5, 'best'", IReinputStatementNode.class);
		assertThat(reinput.messageFormatOperands()).hasSize(2);
		assertThat(((ILiteralNode) reinput.messageFormatOperands().get(0)).token()).returns(5, from(SyntaxToken::intValue));
		assertThat(((ILiteralNode) reinput.messageFormatOperands().get(1)).token()).returns("best", from(SyntaxToken::stringValue));
	}

	@Test
	void parseMark()
	{
		assertParsesSingleStatement("REINPUT USING HELP MARK POSITION 4 IN FIELD *BADGER", IReinputStatementNode.class);
	}

	@Test
	void parseMarkExtraFieldsWithAttributes()
	{
		assertParsesSingleStatement("""
			REINPUT USING HELP MARK
				*BADGER (CV=GR)
				*SQUIRREL (CV=BL)
			""", IReinputStatementNode.class);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"REINPUT USING HELP AND SOUND ALARM",
		"REINPUT USING HELP AND ALARM",
		"REINPUT USING HELP SOUND ALARM",
		"REINPUT USING HELP ALARM"
	})
	void parseAlarm(String statement)
	{
		var reinput = assertParsesSingleStatement(statement, IReinputStatementNode.class);
		assertThat(reinput.hasAlarm()).isTrue();
	}
}
