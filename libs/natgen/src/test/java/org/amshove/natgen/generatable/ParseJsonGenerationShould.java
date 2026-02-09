package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

class ParseJsonGenerationShould extends CodeGenerationTest
{
	@Test
	void generateParseJsonStatement()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));
		assertGenerated(parseJson, """
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

		assertGenerated(parseJson, """
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

		assertGenerated(parseJson, """
				PARSE JSON #JSON INTO VALUE #VALUE
				  IGNORE
				END-PARSE""");
	}

	@Test
	void acceptBodyStatements()
	{
		var parseJson = NaturalCode.parseJson(new Variable(1, VariableScope.LOCAL, "#JSON", VariableType.alphanumericDynamic()));
		parseJson
			.addToBody(NaturalCode.plainStatement("DISPLAY 'Parsing JSON'"));

		assertGenerated(parseJson, """
				PARSE JSON #JSON
				  DISPLAY 'Parsing JSON'
				END-PARSE""");
	}
}
