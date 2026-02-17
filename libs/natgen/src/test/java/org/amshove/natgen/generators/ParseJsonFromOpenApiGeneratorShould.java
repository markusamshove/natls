package org.amshove.natgen.generators;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParseJsonFromOpenApiGeneratorShould extends CodeGenerationTest
{
	private CodeGenerationContext generate(String schemaName, String openApiSpec)
	{
		var openAPIParser = new OpenAPIParser();
		var options = new ParseOptions();
		options.setFlatten(true);
		var result = openAPIParser.readContents(openApiSpec, null, options);
		var spec = result.getOpenAPI();
		var schema = spec.getComponents().getSchemas().get(schemaName);
		var generatorSettings = new ParseJsonGenerator.Settings();
		var generator = ParseJsonGenerator.forOpenAPISchema(spec, schemaName, schema, generatorSettings);
		return generator.generate();
	}

	@Test
	void generateForAnObjectWithASingleSimpleProperty()
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        name:
          type: string
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#NAME", VariableScope.LOCAL, VariableType.alphanumericDynamic())
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
	void generateAVariableForUUIDFormat()
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        id:
          type: string
          format: uuid
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#ID", VariableScope.LOCAL, VariableType.alphanumeric(36));
	}

	@Test
	void generateForAnObjectWithAConstrainedStringProperty()
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        name:
          type: string
          maxLength: 25
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#NAME", VariableScope.LOCAL, VariableType.alphanumeric(25))
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
	void generateSimpleNumberProperties()
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        age:
          type: number
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#AGE", VariableScope.LOCAL, VariableType.integer(4))
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"float", "double"
	})
	void generateAVariableForFloatingPointValues(String format)
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        money:
          type: number
          format: %s
			""".formatted(format));

		assertOn(context).hasVariable(3, "##PARSED-JSON.#MONEY", VariableScope.LOCAL, VariableType.floating(8));
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"int32", "null"
	})
	void generateAVariableForIntegerNumbers(String format)
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        age:
          type: integer
          format: %s
			""".formatted(format));

		assertOn(context).hasVariable(3, "##PARSED-JSON.#AGE", VariableScope.LOCAL, VariableType.integer(4));
	}

	@Test
	void generateAVariableForLongNumbers()
	{
		var context = generate("Person", """
openapi: 3.1.0
components:
  schemas:
    Person:
      type: object
      properties:
        bip:
          type: integer
          format: int64
			""");

		assertOn(context).hasVariable(3, "##PARSED-JSON.#BIP", VariableScope.LOCAL, VariableType.numeric(8));
	}

	@Test
	void generateAssignmentsForBooleans()
	{
		var context = generate("Different", """
openapi: 3.1.0
components:
  schemas:
    Different:
      type: object
      properties:
        employed:
          type: boolean
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#EMPLOYED", VariableScope.LOCAL, VariableType.logical())
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</employed/$'
				      ##PARSED-JSON.#EMPLOYED := ATOB(<##JSON-PARSING.#VALUE>)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void generateAssignmentsForDateTypes()
	{
		var context = generate("Baby", """
openapi: 3.1.0
components:
  schemas:
    Baby:
      type: object
      properties:
        birthdate:
          type: string
          format: date
			""");

		assertOn(context)
			.hasVariable(3, "##PARSED-JSON.#BIRTHDATE", VariableScope.LOCAL, VariableType.date())
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</birthdate/$'
				      MOVE EDITED ##JSON-PARSING.#VALUE TO ##PARSED-JSON.#BIRTHDATE (EM=YYYY-MM-DD)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}

	@Test
	void generateAssignmentsForDateTimeTypes()
	{
		var context = generate("Baby", """
openapi: 3.1.0
components:
  schemas:
    Baby:
      type: object
      properties:
        birthdatetime:
          type: string
          format: date-time
			""");

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
				  2 #BABY
				    3 #BIRTHDATETIME (A20)
				    3 REDEFINE #BIRTHDATETIME
				      4 #BIRTHDATETIME-DATEPART (A10)
				      4 FILLER 1X
				      4 #BIRTHDATETIME-TIMEPART (A8)
				    3 #BIRTHDATETIME-DATE (D)
				    3 #BIRTHDATETIME-TIME (T)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</birthdatetime/$'
				      ##PARSED-JSON.#BIRTHDATETIME := ##JSON-PARSING.#VALUE
				      MOVE EDITED ##PARSED-JSON.#BIRTHDATETIME-DATEPART TO ##PARSED-JSON.#BIRTHDATETIME-DATE (EM=YYYY-MM-DD)
				      MOVE EDITED ##PARSED-JSON.#BIRTHDATETIME-TIMEPART TO ##PARSED-JSON.#BIRTHDATETIME-TIME (EM=HH:II:SS)
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}
}
