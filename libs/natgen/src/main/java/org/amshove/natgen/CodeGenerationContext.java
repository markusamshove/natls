package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratableStatement;
import org.amshove.natgen.generatable.definedata.IGeneratableDefineDataElement;
import org.amshove.natgen.generatable.definedata.Using;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.*;

/// Holds the building blocks for code that is going to be generated.
public final class CodeGenerationContext implements IVariableAddable, IStatementAddable<CodeGenerationContext>
{
	private final List<Variable> variables = new ArrayList<>();
	private final EnumMap<VariableScope, Set<Using>> usings = new EnumMap<>(VariableScope.class);
	private final List<IGeneratableDefineDataElement> parameter = new ArrayList<>();
	private final List<IGeneratableStatement> statements = new ArrayList<>();
	private final Set<String> uniqueVariableNames = new HashSet<>();

	public Variable addParameter(String name, VariableType type)
	{
		return addVariable(VariableScope.PARAMETER, name, type);
	}

	public Variable addVariable(Variable variable)
	{
		ensureUniqueVariableName(variable);

		if (variable.scope().isParameter())
		{
			parameter.add(variable);
		}
		else
		{
			variables.add(variable);
		}
		return variable;
	}

	/// Add a new `LOCAL` variable
	public Variable addVariable(String name, VariableType type)
	{
		return addVariable(VariableScope.LOCAL, name, type);
	}

	public Variable addVariable(VariableScope scope, String name, VariableType type)
	{
		var variable = new Variable(1, scope, name, type);
		ensureUniqueVariableName(variable);

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

	public void removeVariable(Variable variable)
	{
		variables.remove(variable);
		uniqueVariableNames.remove(variable.name());
	}

	/// Returns all non-parameter scoped Variables added to this context
	public List<Variable> variables()
	{
		return variables;
	}

	///  Returns all DEFINE DATA elements with scope PARAMETER in the order
	///  they've been defined.
	public List<IGeneratableDefineDataElement> parameter()
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

	public CodeGenerationContext addStatementToFront(IGeneratableStatement statement)
	{
		statements.addFirst(statement);
		return this;
	}

	public CodeGenerationContext addStatement(IGeneratableStatement statement)
	{
		statements.add(statement);
		return this;
	}

	public CodeGenerationContext addStatements(Collection<IGeneratableStatement> statements)
	{
		this.statements.addAll(statements);
		return this;
	}

	public List<IGeneratableStatement> statements()
	{
		return statements;
	}

	/// Merge the passed context into this one
	public void consume(CodeGenerationContext other)
	{
		consumeExceptStatements(other);
		this.statements.addAll(other.statements);
	}

	/// Merge the passed context into this one but leave out statements
	public void consumeExceptStatements(CodeGenerationContext other)
	{
		for (var variable : other.variables)
		{
			addVariable(variable);
		}
		for (var param : other.parameter)
		{
			switch (param)
			{
				case Using u -> this.parameter.add(u);
				case Variable v -> addVariable(v);
			}
		}
		this.usings.putAll(other.usings);
	}

	private void ensureUniqueVariableName(Variable variable)
	{
		var variablename = variable.name();
		while (!uniqueVariableNames.add(variablename))
		{
			variable.setName("#" + variablename);
			variablename = variable.name();
		}
	}

	public void addStatements(List<IGeneratableStatement> statements)
	{
		this.statements.addAll(statements);
	}
}
