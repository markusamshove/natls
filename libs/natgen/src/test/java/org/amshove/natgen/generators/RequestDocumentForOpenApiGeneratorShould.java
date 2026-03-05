package org.amshove.natgen.generators;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
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

		// NOTE: If a JSON document has an array as its root element then Naturals PARSE JSON
		// starts with a JSON_SEPARATOR. Meaning it will start with '/(' instead of '('.
		// For an object as root element it would start with '<'

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				PARAMETER
				1 #P-BASE-URL (A) DYNAMIC BY VALUE
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
				  2 #S-#INLINERESPONSE (I4)
				1 #RESPONSE-200
				  2 #INLINERESPONSE (1:*)
				    3 #ID (A36)
				    3 #TEMPERATURE (N12,7)
				    3 #DESCRIPTION (A) DYNAMIC
				1 #JSON-SOURCE (A) DYNAMIC
				END-DEFINE""")
			.generatesStatements("""
				COMPRESS #P-BASE-URL '/weather' INTO ##REQUEST.#URL LEAVING NO SPACE

				REQUEST DOCUMENT FROM ##REQUEST.#URL
				  WITH
				    HEADER NAME 'REQUEST-METHOD' VALUE 'GET'
				  RETURN
				    PAGE ##RESPONSE.#BODY
				    RESPONSE ##RESPONSE.#CODE

				DECIDE ON FIRST VALUE OF ##RESPONSE.#CODE
				  VALUE 200
				    PERFORM HANDLE-200
				  NONE VALUE
				    IGNORE
				END-DECIDE

				/***********************************************************************
				DEFINE SUBROUTINE HANDLE-200
				/***********************************************************************

				#JSON-SOURCE := #BODY
				PARSE JSON #JSON-SOURCE INTO PATH ##JSON-PARSING.#PATH VALUE ##JSON-PARSING.#VALUE GIVING ##JSON-PARSING.#ERR-CODE SUBCODE ##JSON-PARSING.#ERR-SUBCODE
				  DECIDE ON FIRST VALUE OF ##JSON-PARSING.#PATH
				    VALUE '/(/<'
				      ADD 1 TO ##JSON-PARSING.#S-#INLINERESPONSE
				      EXPAND ARRAY #RESPONSE-200.#INLINERESPONSE TO (1:##JSON-PARSING.#S-#INLINERESPONSE)
				    VALUE '/(/</id/$'
				      #RESPONSE-200.#ID(#S-#INLINERESPONSE) := ##JSON-PARSING.#VALUE
				    VALUE '/(/</temperature/$'
				      #RESPONSE-200.#TEMPERATURE(#S-#INLINERESPONSE) := VAL(##JSON-PARSING.#VALUE)
				    VALUE '/(/</description/$'
				      #RESPONSE-200.#DESCRIPTION(#S-#INLINERESPONSE) := ##JSON-PARSING.#VALUE
				    NONE VALUE
				      IGNORE
				  END-DECIDE
				END-PARSE

				END-SUBROUTINE

				""");
	}

	@Test
	void addPathParameterAndUseThemInTheUrl()
	{
		var path = openApi.getPaths().get("/weather/{id}");
		var operation = path.getDelete();
		var context = sut.generate("DELETE", "/weather/{id}", operation);

		assertOn(context)
			.hasParameterByValue(1, "#P-ID", VariableType.alphanumeric(36))
			.generatedStatementSourceContains("""
				COMPRESS #P-BASE-URL '/weather/{id}' INTO ##REQUEST.#URL LEAVING NO SPACE
				EXAMINE FULL ##REQUEST.#URL FOR '{id}' REPLACE WITH #P-ID
				""");
	}

	@Test
	void addQueryParameterToTheUrl()
	{
		var path = openApi.getPaths().get("/weather/filter");
		var operation = path.getGet();
		var context = sut.generate("GET", "/weather/filter", operation);

		assertOn(context)
			.hasParameterByValue(1, "#P-REVERSE", VariableType.logical())
			.hasParameterByValue(1, "#P-SKIP", VariableType.integer(4))
			.generatedStatementSourceContains("""
				COMPRESS #P-BASE-URL '/weather/filter' INTO ##REQUEST.#URL LEAVING NO SPACE
				COMPRESS ##REQUEST.#URL ##REQUEST.#QUERY-DELIMITER 'reverse=' L2JBOOL(<#P-REVERSE>)
				INTO ##REQUEST.#URL LEAVING NO SPACE
				##REQUEST.#QUERY-DELIMITER := '&'
				COMPRESS ##REQUEST.#URL ##REQUEST.#QUERY-DELIMITER 'skip=' #P-SKIP INTO ##REQUEST.#URL
				LEAVING NO SPACE
				##REQUEST.#QUERY-DELIMITER := '&'
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
  /weather/filter:
    get:
      parameters:
      - name: reverse
        in: query
        schema:
          type: boolean
      - name: skip
        in: query
        schema:
          type: integer
          format: int32
      responses:
        "200":
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: "#/components/schemas/Forecast"
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
