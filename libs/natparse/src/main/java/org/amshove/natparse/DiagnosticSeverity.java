package org.amshove.natparse;

import java.nio.file.Path;

public enum DiagnosticSeverity
{
	INFO(0),
	WARNING(1),
	ERROR(2);

	private final int weight;

	DiagnosticSeverity(int weight)
	{
		this.weight = weight;
	}

	public boolean isWorseOrEqualTo(DiagnosticSeverity other)
	{
		return this.weight >= other.weight;
	}

	public static DiagnosticSeverity fromString(String sev)
	{
		return switch (sev.toUpperCase())
		{
			case "WARN", "WARNING" -> WARNING;
			case "INFO" -> INFO;
			case "ERROR" -> ERROR;
			default -> throw new IllegalArgumentException("Invalid severity: " + sev);
		};
	}

	public IDiagnostic wrapped(IDiagnostic diagnostic)
	{
		return new Wrapper(diagnostic);
	}

	private class Wrapper implements IDiagnostic
	{

		IDiagnostic inner;

		public Wrapper(IDiagnostic inner)
		{
			this.inner = inner;
		}

		@Override
		public String id()
		{
			return inner.id();
		}

		@Override
		public String message()
		{
			return inner.message();
		}

		@Override
		public DiagnosticSeverity severity()
		{
			return DiagnosticSeverity.this;
		}

		@Override
		public ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo()
		{
			return inner.additionalInfo();
		}

		@Override
		public int offset()
		{
			return inner.offset();
		}

		@Override
		public int offsetInLine()
		{
			return inner.offsetInLine();
		}

		@Override
		public int line()
		{
			return inner.line();
		}

		@Override
		public int length()
		{
			return inner.length();
		}

		@Override
		public Path filePath()
		{
			return inner.filePath();
		}

	}
}
