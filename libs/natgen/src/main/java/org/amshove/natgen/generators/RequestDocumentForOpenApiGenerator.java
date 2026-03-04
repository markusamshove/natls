package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.RequestDocument;
import org.amshove.natgen.generatable.Subroutine;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import static org.amshove.natgen.NaturalOpenApi.*;
import static org.amshove.natgen.generatable.NaturalCode.*;

public class RequestDocumentForOpenApiGenerator
{

	public static class Settings
	{
		private @Nullable Variable returnBodyRootGroup;

		/// Sets the variable which is used to add new variables for bodies per HTTP return codes.
		public void setReturnBodyRootGroup(@Nullable Variable root)
		{
			this.returnBodyRootGroup = root;
		}
	}

	private final OpenAPI openApi;
	private final Settings settings;
	private Variable baseUrlParameter;

	public RequestDocumentForOpenApiGenerator(OpenAPI openApi)
	{
		this(openApi, new Settings());
	}

	public RequestDocumentForOpenApiGenerator(OpenAPI openApi, Settings settings)
	{
		this.openApi = openApi;
		this.settings = settings;
	}

	/// Create a [CodeGenerationContext] for a given HTTP method, path and OpenAPI specification
	public CodeGenerationContext generate(String method, String path, Operation operation)
	{
		var context = new CodeGenerationContext();
		baseUrlParameter = context.addParameter("#P-BASE-URL", VariableType.alphanumericDynamic()).asByValue();

		var requestGroup = context.addVariable(VariableScope.LOCAL, "##REQUEST", VariableType.group());
		var builtRequestUrl = requestGroup.addVariable("#URL", VariableType.alphanumericDynamic());
		var responseGroup = context.addVariable(VariableScope.LOCAL, "##RESPONSE", VariableType.group());
		var responseCode = responseGroup.addVariable("#CODE", VariableType.integer(4));
		var responseBody = responseGroup.addVariable("#BODY", VariableType.alphanumericDynamic());

		var compressUrl = compress()
			.withOperand(baseUrlParameter)
			.withOperand(stringLiteral(path))
			.into(builtRequestUrl)
			.leavingNoSpace();

		context
			.addStatement(compressUrl);

		var requestDocument = NaturalCode
			.requestDocument(builtRequestUrl, responseCode)
			.withMethod(stringLiteral(method));

		addRequestParameter(context, builtRequestUrl, operation);

		//		addRequestContentType(requestDocument, operation);

		requestDocument.withResponseBody(responseBody);

		context
			.addStatement(emptyLine())
			.addStatement(requestDocument);
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

	private void addRequestParameter(CodeGenerationContext context, Variable requestUrl, Operation operation)
	{
		if (operation.getParameters() == null || operation.getParameters().isEmpty())
		{
			return;
		}

		for (var parameter : operation.getParameters())
		{
			if (parameter.getIn().equals("path"))
			{
				var inferredType = inferNaturalType(parameter.getSchema(), openApi);
				var moduleParameter = context.addParameter("#P-" + parameter.getName(), inferredType).asByValue();
				context.addStatement(
					examineFull(requestUrl)
						._for(stringLiteral("{%s}".formatted(parameter.getName())))
						.replaceWith(moduleParameter)
				);
			}
		}
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
			var schemaName = resolveSchemaName(responseContent.getValue().getSchema(), "Inlineresponse");
			var settings = new ParseJsonGenerator.Settings();
			if (this.settings.returnBodyRootGroup != null)
			{
				settings.setParsedJsonRoot(
					this.settings.returnBodyRootGroup.addVariable("#RESPONSE-" + responseCode, VariableType.group())
				);
			}
			else
			{
				settings.setParsedJsonGroupName("#RESPONSE-" + responseCode);
			}
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
