package org.amshove.natlint.api;

import org.amshove.natparse.IDiagnostic;

@FunctionalInterface
public interface IDiagnosticReporter
{
	void report(IDiagnostic diagnostic);
}
