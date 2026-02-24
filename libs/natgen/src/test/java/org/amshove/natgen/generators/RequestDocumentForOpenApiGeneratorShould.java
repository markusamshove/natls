package org.amshove.natgen.generators;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CodeGenerationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RequestDocumentForOpenApiGeneratorShould extends CodeGenerationTest
{
	private static OpenAPI openApi;
	private final RequestDocumentForOpenApiGenerator sut = new RequestDocumentForOpenApiGenerator(openApi);

	@Test
	void generateCodeForAGetRequestWithArrayResponse()
	{
		var path = openApi.getPaths().get("/weather");
		var operation = path.getGet();
		var context = sut.generate("GET", "/weather", operation);

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				PARAMETER
				1 #P-BASE-URL (A) DYNAMIC
				LOCAL
				1 ##REQUEST
				  2 #URL (A) DYNAMIC
				1 ##RESPONSE
				  2 #CODE (I4)
				  2 #BODY (A) DYNAMIC
				1 ##JSON-PARSING
				  2 #PATH (A) DYNAMIC
				  2 #VALUE (A) DYNAMIC
				  2 #ERR-CODE (I4)
				  2 #ERR-SUBCODE (I4)
				  2 #S-#RESPONSE (I4)
				1 #RESPONSE-200
				  2 #RESPONSE (1:*)
				    3 #ID (A36)
				    3 #TEMPERATURE (F8)
				    3 #DESCRIPTION (A) DYNAMIC
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE""")
			.generatesStatements("""
				COMPRESS #P-BASE-URL '/weather' INTO ##REQUEST.#URL LEAVING NO SPACE
				
				REQUEST DOCUMENT FROM ##REQUEST.#URL
				  WITH
				    HEADER
				      NAME 'REQUEST-METHOD' VALUE 'GET'
				      NAME 'Content-Type' VALUE 'application/json'
				  RETURN
				    PAGE ##RESPONSE.#BODY
				    RESPONSE ##RESPONSE.#CODE
				""");
	}

	@BeforeAll
	static void parseOpenApi()
	{
		var openApiSpec = """
openapi: 3.1.0
components:
  schemas:
    UUID:
      type: string
      format: uuid
      pattern: "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
    Forecast:
      type: object
      properties:
        id:
          $ref: "#/components/schemas/UUID"
        temperature:
          type: number
          format: double
        description:
          type: string
paths:
  /weather:
    get:
      responses:
        "200":
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: "#/components/schemas/Forecast"
      summary: Index
      tags:
      - Weatherforecast
  /weather/random:
    post:
      responses:
        "200":
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Forecast"
      summary: Save random forecast and return it
      tags:
      - Weatherforecast
  /weather/{id}:
    delete:
      parameters:
      - name: id
        in: path
        required: true
        schema:
          $ref: "#/components/schemas/UUID"
      responses:
        "204":
          description: No Content
      summary: Delete
      tags:
      - Weatherforecast
info:
  title: api API
  version: 1.0.0
			""";

		var options = new ParseOptions();
		options.setFlatten(true);
		var parser = new OpenAPIParser();
		var parseResult = parser.readContents(openApiSpec, null, options);
		openApi = parseResult.getOpenAPI();
	}
}
