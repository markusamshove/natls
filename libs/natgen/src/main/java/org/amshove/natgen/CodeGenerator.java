package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

/// Holds the building blocks for code that is going to be generated.
/// TODO: call different methods to generate variables, subroutines (and statements?)
public class CodeGenerator
{
	private final List<Variable> variables = new ArrayList<>();

	public Variable addVariable(VariableScope scope, String name, String type)
	{
		var variable = new Variable(1, scope, name, type);
		variables.add(variable);
		return variable;
	}

	public List<Variable> variables()
	{
		return variables;
	}
}
