package org.amshove.natgen.generators;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.CodeGenerationTest;
import org.junit.jupiter.api.Test;

class CompressJsonFromOpenApiGeneratorShould extends CodeGenerationTest
{
	private CodeGenerationContext generate(String schemaName, String openApiSpec)
	{
		var openAPIParser = new OpenAPIParser();
		var options = new ParseOptions();
		options.setFlatten(true);
		var result = openAPIParser.readContents(openApiSpec, null, options);
		var spec = result.getOpenAPI();
		var generator = new CompressJsonFromOpenApiGenerator(spec, new CompressJsonFromOpenApiGenerator.Settings());
		return generator.generate(schemaName, spec.getComponents().getSchemas().get(schemaName));
	}

	@Test
	void generateForAnObjectWithASingleSimpleProperty()
	{
		var context = generate("Person", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Person:
      type: object
      properties:
        name:
          type: string
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #PERSON")
			.generatedDefineDataSourceContains("2 #NAME (A) DYNAMIC")
			.generatesStatements("""
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'name' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' #PERSON.#NAME H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}

	@Test
	void handleNullableStrings()
	{
		var context = generate("Person", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Person:
      type: object
      properties:
        name:
          type:
           - "string"
           - "null"
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #PERSON")
			.generatedDefineDataSourceContains("2 #NAME (A) DYNAMIC")
			.generatesStatements("""
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'name' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				IF #PERSON.#NAME = ' '
				  COMPRESS ##JSON-RESULT 'null' INTO ##JSON-RESULT LEAVING NO SPACE
				ELSE
				  COMPRESS ##JSON-RESULT H'22' #PERSON.#NAME H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				END-IF
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}

	@Test
	void generateForAnObjectWithAMultipleSimpleProperties()
	{
		var context = generate("Person", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Person:
      type: object
      properties:
        name:
          type: string
        age:
          type: number
          format: integer
        verified:
          type: boolean
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #PERSON")
			.generatedDefineDataSourceContains("2 #NAME (A) DYNAMIC")
			.generatedDefineDataSourceContains("2 #AGE (I4)")
			.generatedDefineDataSourceContains("2 #VERIFIED (L)")
			.generatesStatements("""
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'name' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' #PERSON.#NAME H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT ',' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'age' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS NUMERIC ##JSON-RESULT #PERSON.#AGE INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT ',' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'verified' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT LOGICAL-TO-JSON-BOOL(<#PERSON.#VERIFIED>) INTO ##JSON-RESULT
				  LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}

	@Test
	void setTheDecimalNotationCharacterWhenFloatingPointNumbersArePresent()
	{
		var context = generate("Person", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Person:
      type: object
      properties:
        height:
          type: number
          format: double
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #PERSON")
			.generatedDefineDataSourceContains("2 #HEIGHT (N12,7)")
			.generatedDefineDataSourceContains("1 ##C-DECIMAL-CHARACTER (A1) CONST<'.'>")
			.generatedDefineDataSourceContains("1 ##PREVIOUS-DC (A1)")
			.generatesStatements("""
				##PREVIOUS-DC := GET-CURRENT-DECIMAL-CHARACTER(<>)
				SET GLOBALS DC=##C-DECIMAL-CHARACTER
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'height' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS NUMERIC ##JSON-RESULT #PERSON.#HEIGHT INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				SET GLOBALS DC=##PREVIOUS-DC
				""");
	}

	@Test
	void generateNestedObjects()
	{
		var context = generate("Person", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Address:
      type: object
      properties:
        street:
          type: string
        city:
          type: string
    Person:
      type: object
      properties:
        name:
          type: string
        address:
          $ref: '#/components/schemas/Address'
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #PERSON")
			.generatedDefineDataSourceContains("2 #NAME (A) DYNAMIC")
			.generatedDefineDataSourceContains("2 #ADDRESS")
			.generatedDefineDataSourceContains("3 #STREET (A) DYNAMIC")
			.generatedDefineDataSourceContains("3 #CITY (A) DYNAMIC")
			.generatesStatements("""
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'name' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' #PERSON.#NAME H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT ',' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'address' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'street' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' #PERSON.#STREET H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT ',' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'city' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' #PERSON.#CITY H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}

	@Test
	void generatePrimitiveArrays()
	{
		var context = generate("Book", """
openapi: 3.1.0
info:
  title: api API
  version: 1.0.0
components:
  schemas:
    Book:
      type: object
      properties:
        editions:
          type: array
          items:
            type: string
			""");

		assertOn(context)
			.generatedDefineDataSourceContains("1 #BOOK")
			.generatedDefineDataSourceContains("2 #EDITIONS (A/1:*) DYNAMIC")
			.generatedDefineDataSourceContains("1 #S-#EDITIONS (I4)")
			.generatedDefineDataSourceContains("1 #I-#EDITIONS (I4)")
			.generatesStatements("""
				COMPRESS ##JSON-RESULT '{' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT H'22' 'editions' H'22' ':' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '[' INTO ##JSON-RESULT LEAVING NO SPACE
				#S-#EDITIONS := *OCC(#BOOK.#EDITIONS)
				FOR #I-#EDITIONS := 1 TO #S-#EDITIONS
				  IF #I-#EDITIONS > 1
				    COMPRESS ##JSON-RESULT ',' INTO ##JSON-RESULT LEAVING NO SPACE
				  END-IF
				  COMPRESS ##JSON-RESULT H'22' #BOOK.#EDITIONS(#I-#EDITIONS) H'22' INTO ##JSON-RESULT LEAVING NO SPACE
				END-FOR
				COMPRESS ##JSON-RESULT ']' INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}

	// TODO: Array of Objects
	// TODO: Root document is array
}
