package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.amshove.natgen.generatable.NaturalCode.*;

@NullMarked
public class RequestDocument implements IGeneratableStatement
{
	private final IGeneratable url;
	private final IGeneratable responseCode;
	private @Nullable IGeneratable userId;
	private @Nullable IGeneratable password;
	private @Nullable IGeneratable requestBody;
	private @Nullable IGeneratable requestCodepage;
	private @Nullable IGeneratable responseBody;
	private @Nullable IGeneratable responseCodepage;
	private @Nullable IGeneratable responseMimeType;
	private @Nullable IGeneratable responseAllHeader;
	private @Nullable IGeneratable givingErrorNumber;
	private final List<NamedValue> requestHeaderPairs = new ArrayList<>();
	private final List<NamedValue> responseHeaderPairs = new ArrayList<>();
	private final List<NamedValue> formDataPairs = new ArrayList<>();

	public RequestDocument(IGeneratable url, IGeneratable responseCode)
	{
		this.url = url;
		this.responseCode = responseCode;
	}

	public RequestDocument withUserId(IGeneratable userId)
	{
		this.userId = userId;
		return this;
	}

	public RequestDocument withUserPassword(IGeneratable password)
	{
		this.password = password;
		return this;
	}

	/// Adds a header with the given `name` and `value` to the request.
	/// Can be called multiple times to add more headers.
	public RequestDocument withRequestHeader(IGeneratable name, IGeneratable headerValue)
	{
		requestHeaderPairs.add(new NamedValue(name, headerValue));
		return this;
	}

	/// Sets the Content-Type header.
	public RequestDocument withContentType(IGeneratable contentType)
	{
		requestHeaderPairs.add(new NamedValue(stringLiteral("Content-Type"), contentType));
		return this;
	}

	/// Adds a header with the given `name` and `value` to the request.
	/// Can be called multiple times to add more form parameter.
	public RequestDocument withFormData(IGeneratable name, IGeneratable value)
	{
		formDataPairs.add(new NamedValue(name, value));
		validateFormAndRequestBody();
		return this;
	}

	/// Explicitly sets the specified HTTP method for the request.
	public RequestDocument withMethod(IGeneratable method)
	{
		requestHeaderPairs.add(new NamedValue(stringLiteral("REQUEST-METHOD"), method));
		return this;
	}

	/// Sets the request body which is used for `WITH DATA ALL`.
	public RequestDocument withRequestBody(IGeneratable body)
	{
		requestBody = body;
		validateFormAndRequestBody();
		return this;
	}

	///  Sets the encoding of the request.
	public RequestDocument withRequestCodepage(IGeneratable requestCodepage)
	{
		this.requestCodepage = requestCodepage;
		return this;
	}

	/// Save the body of the response to the given variable.
	public RequestDocument withResponseBody(IGeneratable responseBody)
	{
		this.responseBody = responseBody;
		return this;
	}

	/// Set the code page for the response.
	public RequestDocument withResponseCodepage(IGeneratable responseCodepage)
	{
		this.responseCodepage = responseCodepage;
		return this;
	}

	/// Set the return clause mime-types
	public RequestDocument withResponseMimeType(IGeneratable responseMimeType)
	{
		this.responseMimeType = responseMimeType;
		return this;
	}

	/// Save all response header information into the given variable.
	public RequestDocument withResponseAllHeader(IGeneratable responseAllHeader)
	{
		this.responseAllHeader = responseAllHeader;
		return this;
	}

	/// Save header with the given `name` into the given `storage` (e.g. variable).
	public RequestDocument withResponseHeader(IGeneratable name, IGeneratable storage)
	{
		responseHeaderPairs.add(new NamedValue(name, storage));
		return this;
	}

	/// Saves the Natural error number into the given variable.
	public RequestDocument withGivingErrorNumber(IGeneratable errorNumber)
	{
		this.givingErrorNumber = errorNumber;
		return this;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder.append("REQUEST DOCUMENT FROM ").appendLine(url);
		builder.indent();

		var hasWith = false;
		if (userId != null)
		{
			hasWith = appendWith(hasWith, builder);
			builder.append("USER ").appendLine(userId);
		}

		if (password != null)
		{
			hasWith = appendWith(hasWith, builder);
			builder.append("PASSWORD ").appendLine(password);
		}

		if (!requestHeaderPairs.isEmpty())
		{
			hasWith = appendWith(hasWith, builder);
			for (var pair : requestHeaderPairs)
			{
				builder.append("HEADER NAME ").append(pair.name()).append(" VALUE ").appendLine(pair.value());
			}
		}

		if (requestBody != null)
		{
			hasWith = appendWith(hasWith, builder);
			builder.append("DATA ALL ").append(requestBody);

			if (requestCodepage != null)
			{
				builder.spaceOrBreak().append("ENCODED IN CODEPAGE ").append(requestCodepage);
			}

			builder.lineBreak();
		}

		if (!formDataPairs.isEmpty())
		{
			hasWith = appendWith(hasWith, builder);
			builder.appendLine("DATA").indent();
			for (var pair : formDataPairs)
			{
				builder.append("NAME ").append(pair.name()).append(" VALUE ").appendLine(pair.value());
			}
			builder.unindent();
		}

		if (hasWith)
		{
			builder.unindent();
		}

		builder.appendLine("RETURN").indent();

		if (!responseHeaderPairs.isEmpty() || responseAllHeader != null)
		{
			builder.append("HEADER");

			if (responseAllHeader != null)
			{
				builder.spaceOrBreak().append("ALL ").append(responseAllHeader);
			}
			builder.lineBreak();
			builder.indent();

			for (var pair : responseHeaderPairs)
			{
				builder.append("NAME ").append(pair.name()).append(" VALUE ").appendLine(pair.value());
			}

			builder.unindent();
		}

		if (responseBody != null)
		{
			builder.append("PAGE ").append(responseBody);

			if (responseCodepage != null)
			{
				builder.spaceOrBreak().append("ENCODED");
				if (responseMimeType != null)
				{
					builder.spaceOrBreak().append("FOR TYPES").spaceOrBreak().append(responseMimeType);
				}

				builder.spaceOrBreak().append("IN CODEPAGE ").append(responseCodepage);
			}

			builder.lineBreak();
		}

		builder
			.append("RESPONSE ").append(responseCode)
			.unindent();

		if (givingErrorNumber != null)
		{
			builder.lineBreak().append("GIVING ").append(givingErrorNumber);
		}

		builder.unindent();
	}

	private boolean appendWith(boolean alreadyHasWith, CodeBuilder builder)
	{
		if (!alreadyHasWith)
		{
			builder.appendLine("WITH").indent();
		}

		return true;
	}

	private void validateFormAndRequestBody()
	{
		if (!formDataPairs.isEmpty() && requestBody != null)
		{
			throw new IllegalStateException("Can not add form data and request body to REQUEST DOCUMENT at the same time");
		}
	}

	private record NamedValue(IGeneratable name, IGeneratable value)
	{}
}
