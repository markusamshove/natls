package org.amshove.natlint.coverage;

import java.nio.file.Path;

import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;

/**
 * The sole purpose of this diagnostic is to note that this position in the source code could return a coverage event,
 * if execution passes through it.
 */
public class CoverageDiagnostic implements IDiagnostic
{

	public CoverageDiagnostic(IPosition position)
	{
		this.position = position;
	}

	@Override
	public String id()
	{
		return "NL000";
	}

	@Override
	public String message()
	{
		return "This line could be covered";
	}

	@Override
	public DiagnosticSeverity severity()
	{
		return DiagnosticSeverity.INFO;
	}

	private static final ReadOnlyList<AdditionalDiagnosticInfo> NONE = ReadOnlyList.empty();

	@Override
	public ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo()
	{
		return NONE;
	}

	IPosition position;

	@Override
	public int offset()
	{
		return position.offset();
	}

	@Override
	public int offsetInLine()
	{
		return position.offsetInLine();
	}

	@Override
	public int line()
	{
		return position.line();
	}

	@Override
	public int length()
	{
		return position.length();
	}

	@Override
	public Path filePath()
	{
		return position.filePath();
	}

	@Override
	public String toString()
	{
		return "CoverageDiagnostic{position=" + position + '}';
	}
}
