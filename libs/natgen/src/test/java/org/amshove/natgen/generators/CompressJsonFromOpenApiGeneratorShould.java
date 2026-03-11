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
				COMPRESS ##JSON-RESULT L2JBOOL(<#PERSON.#VERIFIED>) INTO ##JSON-RESULT LEAVING NO SPACE
				COMPRESS ##JSON-RESULT '}' INTO ##JSON-RESULT LEAVING NO SPACE
				""");
	}
}
