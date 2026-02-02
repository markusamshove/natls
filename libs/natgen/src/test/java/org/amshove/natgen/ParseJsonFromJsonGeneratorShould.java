package org.amshove.natgen;

import org.amshove.natgen.generators.ParseJsonFromJsonGenerator;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

class ParseJsonFromJsonGeneratorShould extends CodeGenerationTest
{
	private final ParseJsonFromJsonGenerator sut = new ParseJsonFromJsonGenerator();

	@Test
	void createVariablesForJsonParsing()
	{
		var context = sut.generate("{ }");
		assertOn(context)
			.hasVariable(1, "#JSON-SOURCE", VariableScope.LOCAL)
			.hasVariable(1, "##PARSED-JSON", VariableScope.LOCAL)
			.hasVariable(2, "##JSON-PARSING.#PATH", VariableScope.LOCAL)
			.hasVariable(2, "##JSON-PARSING.#VALUE", VariableScope.LOCAL)
			.hasVariable(2, "##JSON-PARSING.#ERR-CODE", VariableScope.LOCAL)
			.hasVariable(2, "##JSON-PARSING.#ERR-SUBCODE", VariableScope.LOCAL);
	}

	@Test
	void generateASimpleParseJsonFromEmptyJson()
	{
		var context = sut.generate("{ }");
		assertOn(context)
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}
}
