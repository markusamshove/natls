package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

public interface IGeneratableStatement
{
	/// Generate the statement into the given [CodeBuilder]
	void generateInto(CodeBuilder builder);
}
