package org.amshove.natlint.cli;

public record AnalyzerOutputFlags(boolean diagnosticStats)
{
	public boolean showDiagnosticStats()
	{
		return diagnosticStats;
	}
}
