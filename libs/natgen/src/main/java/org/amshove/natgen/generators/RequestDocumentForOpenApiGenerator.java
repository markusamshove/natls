package org.amshove.natgen.generators;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.*;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static org.amshove.natgen.NaturalOpenApi.*;
import static org.amshove.natgen.generatable.NaturalCode.*;
import static org.amshove.natgen.generatable.conditions.Conditions.specified;

public class RequestDocumentForOpenApiGenerator
{

	public static class Settings
	{
		private @Nullable Variable responseBodyRootGroup;
		private @Nullable Variable requestBodyRootGroup;

		/// Sets the variable which is used to add new variables for request bodies.
		public void setRequestBodyRootGroup(@Nullable Variable root)
		{
			this.requestBodyRootGroup = root;
		}

		/// Sets the variable which is used to add new variables for bodies per HTTP return codes.
		public void setResponseBodyRootGroup(@Nullable Variable root)
		{
			this.responseBodyRootGroup = root;
		}
	}

	private final OpenAPI openApi;
	private final Settings settings;
	private Variable requestGroup;
	private Variable queryDelimiterVariable;

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
		Variable baseUrlParameter = context.addParameter("#P-BASE-URL", VariableType.alphanumericDynamic()).asByValue();

		requestGroup = context.addVariable(VariableScope.LOCAL, "##REQUEST", VariableType.group());
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

		addRequestParameter(context, requestDocument, builtRequestUrl, operation);
		addRequestBody(context, requestDocument, operation.getRequestBody());

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
				.addStatement(perform(subroutine));
		}

		return context;
	}

	private void addRequestParameter(
		CodeGenerationContext context, RequestDocument requestDocument,
		Variable requestUrl, Operation operation
	)
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

			if (parameter.getIn().equals("header"))
			{
				var inferredType = inferNaturalType(parameter.getSchema(), openApi);
				var moduleParameter = context.addParameter("#P-" + parameter.getName(), inferredType).asByValue();

				requestDocument.withRequestHeader(stringLiteral(parameter.getName()), moduleParameter);
			}

			if (parameter.getIn().equals("query"))
			{
				var inferredType = inferNaturalType(parameter.getSchema(), openApi);
				var moduleParameter = context.addParameter("#P-" + parameter.getName(), inferredType).asByValue();
				var queryDelimiterVariable = getQueryDelimiterVariable();

				var compress = compress()
					.withOperand(requestUrl)
					.withOperand(queryDelimiterVariable)
					.withOperand(stringLiteral(parameter.getName() + "="))
					.withOperand(variableRhsValue(moduleParameter))
					.into(requestUrl)
					.leavingNoSpace();
				var delimiterAssignment = assignment(queryDelimiterVariable, stringLiteral("&"));

				if (parameter.getRequired())
				{
					context
						.addStatement(compress)
						.addStatement(delimiterAssignment);
				}
				else
				{
					moduleParameter.asOptional();
					var ifSpecified = _if(specified(moduleParameter))
						.addStatement(compress)
						.addStatement(delimiterAssignment);
					context.addStatement(ifSpecified);
				}
			}
		}
	}

	private void addRequestBody(CodeGenerationContext context, RequestDocument requestDocument, RequestBody requestBody)
	{
		if (requestBody == null)
		{
			return;
		}

		var contentEntry = requestBody.getContent().firstEntry();
		var contentType = contentEntry.getKey();
		if (!contentType.equals("application/json"))
		{
			// TODO: Diagnostic on unsupported content types?
			return;
		}

		var content = contentEntry.getValue();
		var requestSchema = resolveSchema(content.getSchema(), openApi);

		var requestBodyParameter = Objects.requireNonNullElse(
			settings.requestBodyRootGroup,
			context.addParameter("#P-BODY", VariableType.group())
		);
		var jsonBodyString = context.addVariable(VariableScope.LOCAL, "##JSON-BODY", VariableType.alphanumericDynamic());

		var jsonSettings = new CompressJsonFromOpenApiGenerator.Settings();
		jsonSettings.setJsonSourceGroup(requestBodyParameter);
		jsonSettings.setJsonResultVariable(jsonBodyString);
		var compressContext = new CompressJsonFromOpenApiGenerator(openApi, jsonSettings)
			.generate(resolveSchemaName(content.getSchema(), "body"), requestSchema);

		context.consume(compressContext);

		requestDocument
			.withRequestHeader(stringLiteral("Content-Type"), stringLiteral(contentType))
			.withRequestBody(jsonBodyString);
	}

	private IGeneratable variableRhsValue(Variable variable)
	{
		if (variable.type().format() == DataFormat.LOGIC)
		{
			return NatGenFunctions.logicalToJsonBoolean(variable);
		}

		return variable;
	}

	private Variable getQueryDelimiterVariable()
	{
		if (queryDelimiterVariable == null)
		{
			queryDelimiterVariable = requestGroup.addVariable("#QUERY-DELIMITER", VariableType.alphanumeric(1))
				.withInitialValue(stringLiteral("?"));
		}

		return queryDelimiterVariable;
	}

	private Subroutine createResponseSubroutine(
		String responseCode, ApiResponse response,
		CodeGenerationContext context
	)
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
			settings.setParsingGroupName("##PARSE-" + responseCode);
			if (this.settings.responseBodyRootGroup != null)
			{
				settings.setParsedJsonRoot(
					this.settings.responseBodyRootGroup.addVariable("#RESPONSE-" + responseCode, VariableType.group())
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

			subroutine.addStatement(assignment(NaturalCode.plain("#JSON-SOURCE"), NaturalCode.plain("#BODY")));
			context.consumeExceptStatements(parseJsonContext);
			subroutine.addStatements(parseJsonContext.statements());
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
