package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;

import java.util.*;

/// Holds the building blocks for code that is going to be generated.
/// TODO: call different methods to generate variables, subroutines (and statements?)
public class CodeGenerationContext
{
	private final List<Variable> variables = new ArrayList<>();
	private final Map<VariableScope, Set<String>> usings = new HashMap<>();

	public Variable addVariable(VariableScope scope, String name, VariableType type)
	{
		var variable = new Variable(1, scope, name, type);
		variables.add(variable);
		return variable;
	}

	public List<Variable> variables()
	{
		return variables;
	}

	/// Adds a USING to the generator. Insertion order of USINGs are preserved within their scope
	public void addUsing(VariableScope scope, String name)
	{
		usings
			.computeIfAbsent(scope, _ -> new LinkedHashSet<>())
			.add(name);
	}

	public Map<VariableScope, Set<String>> usings()
	{
		return usings;
	}
}
