package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.RequestDocument;
import org.amshove.natgen.generatable.Subroutine;
import org.amshove.natparse.natural.VariableScope;

import static org.amshove.natgen.OpenApiExtensions.resolveSchema;
import static org.amshove.natgen.generatable.NaturalCode.*;

public class RequestDocumentForOpenApiGenerator
{
	private final OpenAPI openApi;

	public RequestDocumentForOpenApiGenerator(OpenAPI openApi)
	{
		this.openApi = openApi;
	}

	/// Create a [CodeGenerationContext] for a given HTTP method, path and OpenAPI specification
	public CodeGenerationContext generate(String method, String path, Operation operation)
	{
		var context = new CodeGenerationContext();
		var baseUrlParameter = context.addParameter("#P-BASE-URL", VariableType.alphanumericDynamic());
		var requestGroup = context.addVariable(VariableScope.LOCAL, "##REQUEST", VariableType.group());
		var calledUrl = requestGroup.addVariable("#URL", VariableType.alphanumericDynamic());
		var responseGroup = context.addVariable(VariableScope.LOCAL, "##RESPONSE", VariableType.group());
		var responseCode = responseGroup.addVariable("#CODE", VariableType.integer(4));
		var responseBody = responseGroup.addVariable("#BODY", VariableType.alphanumericDynamic());

		context
			.addStatement(
				compress()
					.withOperand(baseUrlParameter)
					.withOperand(stringLiteral(path))
					.into(calledUrl)
					.leavingNoSpace()
			)
			.addStatement(emptyLine());

		var requestDocument = NaturalCode
			.requestDocument(calledUrl, responseCode)
			.withMethod(stringLiteral(method));

		//		addRequestContentType(requestDocument, operation);

		requestDocument.withResponseBody(responseBody);

		context.addStatement(requestDocument);
		context.addStatement(emptyLine());

		var decideOnResponse = decideOnFirst(responseCode);
		//		decideOnResponse
		//			.onNoneValue()
		//			.addToBody();
		context.addStatement(decideOnResponse).addStatement(emptyLine());
		for (var response : operation.getResponses().entrySet())
		{
			var subroutine = createResponseSubroutine(response.getKey(), response.getValue(), context);
			context.addStatement(subroutine);
			decideOnResponse
				.addBranch(numberLiteral(response.getKey()))
				.addToBody(perform(subroutine));
		}

		return context;
	}

	private Subroutine createResponseSubroutine(String responseCode, ApiResponse response, CodeGenerationContext context)
	{
		var subroutine = subroutine("HANDLE-" + responseCode);

		if (response.getContent() == null || response.getContent().isEmpty())
		{
			return subroutine;
		}

		var responseContent = response.getContent().firstEntry();
		var mediaType = responseContent.getKey();
		if (mediaType.equals("application/json"))
		{
			var schema = resolveSchema(responseContent.getValue().getSchema(), openApi);
			var schemaName = "Response";

			var settings = new ParseJsonGenerator.Settings();
			settings.setParsedJsonGroupName("#RESPONSE-" + responseCode);
			var generator = ParseJsonGenerator.forOpenAPISchema(
				openApi,
				schemaName,
				schema,
				settings
			);
			var parseJsonContext = generator.generate();

			subroutine.addToBody(assignment(NaturalCode.plain("#JSON-SOURCE"), NaturalCode.plain("#BODY")));
			context.consumeExceptStatements(parseJsonContext);
			subroutine.addToBody(parseJsonContext.statements());
		}

		return subroutine;
	}

	private void addRequestContentType(RequestDocument requestDocument, Operation operation)
	{
		if (operation.getResponses().isEmpty())
		{
			return;
		}

	}
}
