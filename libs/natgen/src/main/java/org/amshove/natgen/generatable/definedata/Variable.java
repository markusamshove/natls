package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Variable implements IGeneratableDefineDataElement
{
	private final int level;
	private final VariableScope scope;
	private final String name;
	private final VariableType type;
	private final List<Variable> childVariables = new ArrayList<>();
	private final List<Redefinition> redefitions = new ArrayList<>();
	private Variable parent;

	private String constValue = null;

	public Variable(int level, VariableScope scope, String name, VariableType type)
	{
		this.level = level;
		this.scope = scope;
		this.name = name;
		this.type = type;
	}

	/// CONST literal value to be generated within DEFINE DATA
	@Nullable
	public String constValue()
	{
		return constValue;
	}

	/// Sets the value that will be generated in a CONST block in DEFINE DATA
	public Variable withConstantValue(String value)
	{
		constValue = value;
		return this;
	}

	/// Creates a new `Redefinition` for this variable.
	/// Calling this method will *create a new* `Redefinition` every time!
	public Redefinition newRedefine()
	{
		var redefinition = new Redefinition(this);
		redefitions.add(redefinition);
		return redefinition;
	}

	/// Creates a Variable for code generation from a parsed Variable from Natural source.
	/// Child variables get added as well.
	public static Variable fromParsedVariable(IVariableNode variableNode)
	{
		Variable variable = switch (variableNode)
		{
			case ITypedVariableNode typedVar -> new Variable(variableNode.level(), variableNode.scope(), variableNode.name(), VariableType.fromDataType(typedVar.type()));
			case IGroupNode _ -> new Variable(variableNode.level(), variableNode.scope(), variableNode.name(), VariableType.group());

			default -> throw new IllegalStateException("Unexpected IVariableNode type: " + variableNode.getClass().getName());
		};

		addChildVariables(variable, variableNode);
		return variable;
	}

	private static void addChildVariables(Variable variable, IVariableNode variableNode)
	{
		if (!(variableNode.isGroup()))
		{
			return;
		}

		for (var childNode : ((IGroupNode) variableNode).variables())
		{
			variable.childVariables.add(fromParsedVariable(childNode));
		}
	}

	public int level()
	{
		return level;
	}

	public VariableScope scope()
	{
		return scope;
	}

	public String name()
	{
		return name;
	}

	public List<Variable> children()
	{
		return childVariables;
	}

	public List<Redefinition> redefinitions()
	{
		return redefitions;
	}

	public VariableType type()
	{
		return type;
	}

	public Variable addVariable(String name, VariableType type)
	{
		var variable = new Variable(level + 1, scope, name, type);
		variable.parent = this;
		childVariables.add(variable);
		return variable;
	}

	@Override
	public String generate()
	{
		if (level == 1)
		{
			return name;
		}

		var firstLevelVariable = this;
		while (firstLevelVariable.level > 1)
		{
			firstLevelVariable = firstLevelVariable.parent;
		}

		return "%s.%s".formatted(firstLevelVariable, name);
	}

	@Override
	public String toString()
	{
		return generate();
	}

	/// Generate a dimension access for this variable, e.g. `#ARR(1)`
	public IGeneratable arrayAccess(IGeneratable... dimensions)
	{
		var access = "(%s)".formatted(
			Arrays.stream(dimensions)
				// Dimension access can't use fully qualified variable names :-(
				.map(g -> g instanceof Variable v ? v.name() : g.generate())
				.collect(Collectors.joining(", "))
		);
		return NaturalCode.plain(generate() + access);
	}
}
