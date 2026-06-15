package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.Variable;

public interface IVariableAddable
{
	Variable addVariable(Variable variable);

	Variable addVariable(String name, VariableType type);
}
