package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;

import java.util.ArrayList;
import java.util.List;

public final class Variable
{
	private final int level;
	private final VariableScope scope;
	private final String name;
	private final String type;
	private final List<Variable> childVariables = new ArrayList<>();

	public Variable(int level, VariableScope scope, String name, String type)
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

	public String type()
	{
		return type;
	}

	public void addVariable(String name, String type)
	{
		childVariables.add(new Variable(level + 1, scope, name, type));
	}
}
