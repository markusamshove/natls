package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.GeneratableDefineDataElement;
import org.amshove.natgen.generatable.definedata.Using;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.*;

/// Holds the building blocks for code that is going to be generated.
/// TODO: call different methods to generate variables, subroutines (and statements?)
public class CodeGenerationContext
{
	private final List<Variable> variables = new ArrayList<>();
	private final Map<VariableScope, Set<Using>> usings = new HashMap<>();

	private final List<GeneratableDefineDataElement> parameter = new ArrayList<>();

	public Variable addParameter(String name, VariableType type)
	{
		return addVariable(VariableScope.PARAMETER, name, type);
	}

	public Variable addVariable(VariableScope scope, String name, VariableType type)
	{
		var variable = new Variable(1, scope, name, type);
		if (scope.isParameter())
		{
			parameter.add(variable);
		}
		else
		{
			variables.add(variable);
		}
		return variable;
	}

	/// Returns all non-parameter scoped Variables added to this context
	public List<Variable> variables()
	{
		return variables;
	}

	///  Returns all DEFINE DATA elements with scope PARAMETER in the order
	///  they've been defined.
	public List<GeneratableDefineDataElement> parameter()
	{
		return parameter;
	}

	/// Adds a USING to the generator. Insertion order of USINGs are preserved within their scope
	public void addUsing(VariableScope scope, String name)
	{
		if (scope.isParameter())
		{
			parameter.add(new Using(scope, name));
		}
		else
		{
			usings
				.computeIfAbsent(scope, _ -> new LinkedHashSet<>())
				.add(new Using(scope, name));
		}
	}

	/// Returns all non-parameter scoped Usings added to this context
	public Map<VariableScope, Set<Using>> usings()
	{
		return usings;
	}
}
