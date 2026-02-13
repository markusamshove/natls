package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.amshove.natgen.generatable.NaturalCode.stringLiteral;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestDocumentGenerationShould extends CodeGenerationTest
{
	private final Variable responseCode = new Variable(1, VariableScope.LOCAL, "#RC", VariableType.integer(4));

	@Test
	void generateASimpleRequestDocument()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void generateARequestDocumentWithUserId()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		requestDocument.withUserId(stringLiteral("Max"));
		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    USER 'Max'
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void generateARequestDocumentWithUserIdAndPassword()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);

		requestDocument
			.withUserPassword(new Variable(1, VariableScope.LOCAL, "#PASSWORD", VariableType.alphanumeric(10)))
			.withUserId(stringLiteral("Max"));
		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    USER 'Max'
			    PASSWORD #PASSWORD
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void generateARequestDocumentWithContentTypeHeader()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);

		requestDocument
			.withContentType(stringLiteral("application/json"));
		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    HEADER NAME 'Content-Type' VALUE 'application/json'
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void generateARequestDocumentWithMultipleHeaders()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var tokenVar = new Variable(1, VariableScope.LOCAL, "#AUTH-TOKEN", VariableType.alphanumeric(128));

		requestDocument
			.withContentType(stringLiteral("application/json"))
			.withMethod(stringLiteral("DELETE"))
			.withRequestHeader(stringLiteral("Authentication"), tokenVar);

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    HEADER NAME 'Content-Type' VALUE 'application/json'
			    HEADER NAME 'REQUEST-METHOD' VALUE 'DELETE'
			    HEADER NAME 'Authentication' VALUE #AUTH-TOKEN
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void addFormDataToTheRequest()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var formDataVariable = new Variable(1, VariableScope.LOCAL, "#NAME", VariableType.alphanumeric(10));

		requestDocument
			.withFormData(NaturalCode.stringLiteral("Age"), NaturalCode.plain("30"))
			.withFormData(NaturalCode.stringLiteral("Name"), formDataVariable);

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    DATA
			      NAME 'Age' VALUE 30
			      NAME 'Name' VALUE #NAME
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void addTheRequestBody()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		requestDocument.withRequestBody(
			new Variable(1, VariableScope.LOCAL, "#REQUEST-BODY", VariableType.alphanumericDynamic())
		);

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    DATA ALL #REQUEST-BODY
			  RETURN
			    RESPONSE #RC""");
	}

	@Test
	void addTheRequestBodyWithEncoding()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		requestDocument
			.withRequestBody(new Variable(1, VariableScope.LOCAL, "#REQUEST-BODY", VariableType.alphanumericDynamic()))
			.withRequestCodepage(NaturalCode.stringLiteral("UTF-8"));

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  WITH
			    DATA ALL #REQUEST-BODY ENCODED IN CODEPAGE 'UTF-8'
			  RETURN
			    RESPONSE #RC""");
	}

	@ParameterizedTest
	@ValueSource(booleans =
	{
		true, false
	})
	void throwAnExceptionWhenTryingToPassRequestBodyAndFormData(boolean formDataFirst)
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		if (formDataFirst)
		{
			requestDocument.withFormData(stringLiteral("Name"), stringLiteral("TheName"));
		}
		else
		{
			requestDocument.withRequestBody(stringLiteral("Hello"));
		}

		assertThatThrownBy(() ->
		{
			if (formDataFirst)
			{
				requestDocument.withRequestBody(stringLiteral("Hello"));
			}
			else
			{
				requestDocument.withFormData(stringLiteral("Name"), stringLiteral("TheName"));
			}
		})
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Can not add form data and request body to REQUEST DOCUMENT at the same time");
	}

	@Test
	void generateTheResponseBody()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var body = new Variable(1, VariableScope.LOCAL, "#BODY", VariableType.alphanumericDynamic());

		assertGenerated(requestDocument.withResponseBody(body), """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    PAGE #BODY
			    RESPONSE #RC""");
	}

	@Test
	void generateTheResponseBodyWithEncoding()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var body = new Variable(1, VariableScope.LOCAL, "#BODY", VariableType.alphanumericDynamic());
		requestDocument
			.withResponseBody(body)
			.withResponseCodepage(stringLiteral("ISO-8859-1"));

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    PAGE #BODY ENCODED IN CODEPAGE 'ISO-8859-1'
			    RESPONSE #RC""");
	}

	@Test
	void generateReturnMimeType()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var body = NaturalCode.newLocalVariable("#RESP-BODY", VariableType.alphanumeric(50));
		var mimeType = NaturalCode.newLocalVariable("#MIME-TYPE", VariableType.alphanumeric(50));
		requestDocument
			.withResponseMimeType(mimeType)
			.withResponseBody(body)
			.withResponseCodepage(stringLiteral("ISO-8859-1"));

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    PAGE #RESP-BODY ENCODED FOR TYPES #MIME-TYPE IN CODEPAGE 'ISO-8859-1'
			    RESPONSE #RC""");
	}

	@Test
	void generateReturnAllHeader()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var allheader = NaturalCode.newLocalVariable("#ALL-HEADER", VariableType.alphanumeric(50));

		assertGenerated(requestDocument.withResponseAllHeader(allheader), """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    HEADER ALL #ALL-HEADER
			    RESPONSE #RC""");
	}

	@Test
	void generateReturnHeader()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var returnCookie = NaturalCode.newLocalVariable("#COOKIE", VariableType.alphanumeric(50));
		var returnContentType = NaturalCode.newLocalVariable("#RETURN-CONTENT-TYPE", VariableType.alphanumeric(50));

		requestDocument
			.withResponseHeader(stringLiteral("Cookie"), returnCookie)
			.withResponseHeader(stringLiteral("Content-Type"), returnContentType);

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    HEADER
			      NAME 'Cookie' VALUE #COOKIE
			      NAME 'Content-Type' VALUE #RETURN-CONTENT-TYPE
			    RESPONSE #RC""");
	}

	@Test
	void generateBothAllHeaderAndSpecificHeader()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var returnCookie = NaturalCode.newLocalVariable("#COOKIE", VariableType.alphanumeric(50));
		var returnContentType = NaturalCode.newLocalVariable("#RETURN-CONTENT-TYPE", VariableType.alphanumeric(50));
		var allHeader = NaturalCode.newLocalVariable("#ALL-HEADER", VariableType.alphanumeric(50));

		requestDocument
			.withResponseAllHeader(allHeader)
			.withResponseHeader(stringLiteral("Cookie"), returnCookie)
			.withResponseHeader(stringLiteral("Content-Type"), returnContentType);

		assertGenerated(requestDocument, """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    HEADER ALL #ALL-HEADER
			      NAME 'Cookie' VALUE #COOKIE
			      NAME 'Content-Type' VALUE #RETURN-CONTENT-TYPE
			    RESPONSE #RC""");
	}

	@Test
	void generateGivingErrorNumber()
	{
		var requestDocument = NaturalCode.requestDocument(stringLiteral("https://softwareag.com"), responseCode);
		var errorNumber = NaturalCode.newLocalVariable("#ERROR-NUMBER", VariableType.numeric(4));

		assertGenerated(requestDocument.withGivingErrorNumber(errorNumber), """
			REQUEST DOCUMENT FROM 'https://softwareag.com'
			  RETURN
			    RESPONSE #RC
			  GIVING #ERROR-NUMBER""");
	}
}
