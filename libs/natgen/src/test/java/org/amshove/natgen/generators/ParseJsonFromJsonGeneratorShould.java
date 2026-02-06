package org.amshove.natgen.generators;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.Dimension;
import org.amshove.natgen.VariableType;
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
				    VALUE '</name/$'
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
				    VALUE '</age/$'
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
				    VALUE '</nice/$'
				      ##PARSED-JSON.#NICE := ATOB(<##JSON-PARSING.#VALUE>)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void parseArrayOfPrimitiveStringsWhichItselfIsAProperty()
	{
		var context = sut.generate("{ \"names\": [ \"natls\", \"natparse\" ] }");
		assertOn(context)
			.hasVariable(2, "##JSON-PARSING.#S-#NAMES", VariableScope.LOCAL, VariableType.integer(4))
			.hasVariable(
				2, "##PARSED-JSON.#NAMES", VariableScope.LOCAL, VariableType.alphanumericDynamic().withDimension(
					Dimension.upperUnbound()
				)
			)
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</names/(/$'
				      ADD 1 TO ##JSON-PARSING.#S-#NAMES
				      EXPAND ARRAY ##PARSED-JSON.#NAMES TO (1:##JSON-PARSING.#S-#NAMES)
				      ##PARSED-JSON.#NAMES(#S-#NAMES) := ##JSON-PARSING.#VALUE
				    VALUE '>'
				      RESET ##JSON-PARSING.#S-#NAMES
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE
				""");
	}

	@Test
	void parseNestedObjectsAsGroupsWithVariables()
	{
		var context = sut.generate("{ \"person\": { \"name\": \"Peter\", \"age\": 30 } }");

		assertOn(context)
			.hasVariable(2, "##PARSED-JSON.#PERSON", VariableScope.LOCAL, VariableType.group())
			.hasVariable(3, "##PARSED-JSON.#NAME", VariableScope.LOCAL, VariableType.alphanumericDynamic())
			.hasVariable(3, "##PARSED-JSON.#AGE", VariableScope.LOCAL, VariableType.numeric(12.7))
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				1 ##PARSED-JSON
				  2 #PERSON
				    3 #NAME (A) DYNAMIC
				    3 #AGE (N12,7)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE
				""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</person/</name/$'
				      ##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE
				    VALUE '</person/</age/$'
				      ##PARSED-JSON.#AGE := VAL(##JSON-PARSING.#VALUE)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE
				""");
	}

	@Test
	void parseNestedObjectsWithinArrays()
	{
		var context = sut.generate("{ \"persons\": [ { \"name\": \"Peter\", \"age\": 30 }, { \"name\": \"Hilde\", \"age\": 28 } ] }");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				  2 #S-#PERSONS (I4)
				1 ##PARSED-JSON
				  2 #PERSONS (1:*)
				    3 #NAME (A) DYNAMIC
				    3 #AGE (N12,7)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE
				""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</persons/(/<'
				      ADD 1 TO ##JSON-PARSING.#S-#PERSONS
				      EXPAND ARRAY ##PARSED-JSON.#PERSONS TO (1:##JSON-PARSING.#S-#PERSONS)
				    VALUE '</persons/(/</name/$'
				      ##PARSED-JSON.#NAME(#S-#PERSONS) := ##JSON-PARSING.#VALUE
				    VALUE '</persons/(/</age/$'
				      ##PARSED-JSON.#AGE(#S-#PERSONS) := VAL(##JSON-PARSING.#VALUE)
				    VALUE '>'
				      RESET ##JSON-PARSING.#S-#PERSONS
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void parseObjectsWithArraysWithinArrays()
	{
		var context = sut.generate("{ \"persons\": [ { \"name\": \"Peter\", \"numbers\": [ 30, 35 ] }, { \"name\": \"Hilde\", \"numbers\": [ 40, 45 ] } ] }");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				  2 #S-#PERSONS (I4)
				  2 #S-#NUMBERS (I4)
				1 ##PARSED-JSON
				  2 #PERSONS (1:*)
				    3 #NAME (A) DYNAMIC
				    3 #NUMBERS (N12,7/1:*)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE
				""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</persons/(/<'
				      ADD 1 TO ##JSON-PARSING.#S-#PERSONS
				      EXPAND ARRAY ##PARSED-JSON.#PERSONS TO (1:##JSON-PARSING.#S-#PERSONS)
				    VALUE '</persons/(/</name/$'
				      ##PARSED-JSON.#NAME(#S-#PERSONS) := ##JSON-PARSING.#VALUE
				    VALUE '</persons/(/</numbers/(/$'
				      ADD 1 TO ##JSON-PARSING.#S-#NUMBERS
				      EXPAND ARRAY ##PARSED-JSON.#NUMBERS TO (*, 1:##JSON-PARSING.#S-#NUMBERS)
				      ##PARSED-JSON.#NUMBERS(#S-#PERSONS, #S-#NUMBERS) := VAL(##JSON-PARSING.#VALUE)
				    VALUE '</persons/(/>'
				      RESET ##JSON-PARSING.#S-#NUMBERS
				    VALUE '>'
				      RESET ##JSON-PARSING.#S-#PERSONS
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void disambiguatePropertyNames()
	{
		var context = sut.generate("{ \"employee\": { \"name\": \"Heinz\" }, \"boss\": { \"name\": \"Bossman\" } }");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				1 ##PARSED-JSON
				  2 #EMPLOYEE
				    3 #NAME (A) DYNAMIC
				  2 #BOSS
				    3 ##NAME (A) DYNAMIC
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE
				""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</employee/</name/$'
				      ##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE
				    VALUE '</boss/</name/$'
				      ##PARSED-JSON.##NAME := ##JSON-PARSING.#VALUE
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void disambiguateSizeVariableNames()
	{
		var context = sut.generate("{ \"obj1\": { \"numbers\": [1, 2, 3] }, \"obj2\": { \"numbers\": [4, 5,6] } }");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				  2 #S-#NUMBERS (I4)
				  2 #S-##NUMBERS (I4)
				1 ##PARSED-JSON
				  2 #OBJ1
				    3 #NUMBERS (N12,7/1:*)
				  2 #OBJ2
				    3 ##NUMBERS (N12,7/1:*)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE
				""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</obj1/</numbers/(/$'
				      ADD 1 TO ##JSON-PARSING.#S-#NUMBERS
				      EXPAND ARRAY ##PARSED-JSON.#NUMBERS TO (1:##JSON-PARSING.#S-#NUMBERS)
				      ##PARSED-JSON.#NUMBERS(#S-#NUMBERS) := VAL(##JSON-PARSING.#VALUE)
				    VALUE '</obj2/</numbers/(/$'
				      ADD 1 TO ##JSON-PARSING.#S-##NUMBERS
				      EXPAND ARRAY ##PARSED-JSON.##NUMBERS TO (1:##JSON-PARSING.#S-##NUMBERS)
				      ##PARSED-JSON.##NUMBERS(#S-##NUMBERS) := VAL(##JSON-PARSING.#VALUE)
				    VALUE '</obj2/>'
				      RESET ##JSON-PARSING.#S-##NUMBERS
				    VALUE '</obj1/>'
				      RESET ##JSON-PARSING.#S-#NUMBERS
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void inferAlphanumericForNulls()
	{
		var context = sut.generate("{ \"name\": null }");
		assertOn(context)
			.generatesDefineData("""
					DEFINE DATA
					LOCAL
					1 ##JSON-PARSING
					  2 #PATH (A) DYNAMIC
					  2 #VALUE (A) DYNAMIC
					  2 #ERR-CODE (I4)
					  2 #ERR-SUBCODE (I4)
					1 ##PARSED-JSON
					  2 #NAME (A) DYNAMIC
					1 #JSON-SOURCE (A) DYNAMIC
					END-DEFINE
					""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</name/$'
				      ##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}
}
