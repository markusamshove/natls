package org.amshove.natgen;

import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ParseJsonGenerationShould
{
	@Test
	void generateParseJsonStatement()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));

		assertThat(parseJson.generate())
			.isEqualTo("""
				PARSE JSON #JSON
				IGNORE
				END-PARSE""");
	}
}
