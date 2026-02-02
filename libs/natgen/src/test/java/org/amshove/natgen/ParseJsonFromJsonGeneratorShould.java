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
			.hasVariable(1, "##JSON-PARSING", VariableScope.LOCAL)
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

	@Test
	void parseSimpleScalarStrings()
	{
		var context = sut.generate("{ \"name\": \"natls\" }");
		assertOn(context)
			.hasVariable(2, "##PARSED-JSON.#NAME", VariableScope.LOCAL, VariableType.alphanumericDynamic())
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '/</name/$'
				      ##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void parseSimpleScalarNumbers()
	{
		var context = sut.generate("{ \"age\": 10 }");
		assertOn(context)
			.hasVariable(2, "##PARSED-JSON.#AGE", VariableScope.LOCAL, VariableType.numeric(12.7))
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '/</age/$'
				      ##PARSED-JSON.#AGE := VAL(##JSON-PARSING.#VALUE)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void parseSimpleScalarBooleans()
	{
		var context = sut.generate("{ \"nice\": true }");
		assertOn(context)
			.hasVariable(2, "##PARSED-JSON.#NICE", VariableScope.LOCAL, VariableType.logical())
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '/</nice/$'
				      ##PARSED-JSON.#NICE := ATOB(<##JSON-PARSING.#VALUE>)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}
}
