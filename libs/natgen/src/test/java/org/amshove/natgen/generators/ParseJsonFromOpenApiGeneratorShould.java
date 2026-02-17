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

}
