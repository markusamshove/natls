package org.amshove.natgen.generators;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.CodeGenerationTest;
import org.junit.jupiter.api.Test;

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
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</$'
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
				    3 #NAME (A25)
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE""")
			.generatesStatements("""
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '</$'
				      ##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE""");
	}
}
