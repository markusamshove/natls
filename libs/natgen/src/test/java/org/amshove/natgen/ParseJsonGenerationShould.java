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
			.isEqualToIgnoringNewLines("""
				PARSE JSON #JSON
				IGNORE
				END-PARSE""");
	}

	@Test
	void generateParseJsonWithAllOptions()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));

		parseJson
			.intoPath(NaturalCode.plain("#PATH"))
			.intoName(NaturalCode.plain("#NAME"))
			.intoValue(NaturalCode.plain("#VALUE"))
			.givingErrorCode(NaturalCode.plain("#ERR-CODE"))
			.givingErrorSubcode(NaturalCode.plain("#ERR-SUBCODE"));

		assertThat(parseJson.generate())
			.isEqualToIgnoringNewLines("""
				PARSE JSON #JSON INTO PATH #PATH NAME #NAME VALUE #VALUE GIVING #ERR-CODE SUBCODE #ERR-SUBCODE
				IGNORE
				END-PARSE""");
	}

	@Test
	void addTheIntoKeywordWhenNeeded()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));

		parseJson
			.intoValue(NaturalCode.plain("#VALUE"));

		assertThat(parseJson.generate())
			.isEqualToIgnoringNewLines("""
				PARSE JSON #JSON INTO VALUE #VALUE
				IGNORE
				END-PARSE""");
	}

	@Test
	void acceptBodyStatements()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));

		parseJson
			.addToBody(NaturalCode.plain("DISPLAY 'Parsing JSON'"));

		assertThat(parseJson.generate())
			.isEqualToIgnoringNewLines("""
				PARSE JSON #JSON
				DISPLAY 'Parsing JSON'
				END-PARSE""");
	}
}
