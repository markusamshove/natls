package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

public final class Variable implements IGeneratable, IGeneratableDefineDataElement
{
	private final int level;
	private final VariableScope scope;
	private final String name;
	private final VariableType type;
	private final List<Variable> childVariables = new ArrayList<>();
	private Variable parent;

	public Variable(int level, VariableScope scope, String name, VariableType type)
	{
		this.level = level;
		this.scope = scope;
		this.name = name;
		this.type = type;
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
}
