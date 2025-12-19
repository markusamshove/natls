package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator
{
	private final List<Variable> variables = new ArrayList<>();

	public void addVariable(VariableScope scope, String name, String type)
	{
		variables.add(new Variable(1, scope, name, type));
	}

	public List<Variable> variables()
	{
		return variables;
	}
}
