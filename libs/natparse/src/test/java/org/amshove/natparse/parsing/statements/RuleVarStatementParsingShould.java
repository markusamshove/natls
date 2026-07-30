package org.amshove.natparse.parsing.statements;

import org.amshove.natparse.natural.IRuleVarNode;
import org.amshove.natparse.parsing.StatementParseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RuleVarStatementParsingShould extends StatementParseTest {

	@Test
	void consumeRuleVar()
	{
		assertParsesSingleStatement("""
               RULEVAR F01L-VARIABLE
            """, IRuleVarNode.class);
	}

	@Test
	void ruleVarHasType()
	{
		var ruleVar = assertParsesSingleStatement("""
               RULEVAR F01L-VARIABLE
            """, IRuleVarNode.class);

		assertThat(ruleVar.type()).isEqualTo(IRuleVarNode.Type.FREE_RULE);
	}

	@Test
	void ruleVarForPfKey()
	{
		var ruleVar = assertParsesSingleStatement("""
               RULEVAR F01*PF-KEY
            """, IRuleVarNode.class);

		assertThat(ruleVar.type()).isEqualTo(IRuleVarNode.Type.FREE_RULE);
	}

}
