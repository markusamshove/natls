package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.VariableType;
import java.util.ArrayList;
import java.util.List;

public final class Redefinition
{
	private final Variable redefinedVariable;
	private final List<Variable> members = new ArrayList<>();

	Redefinition(Variable toRedefine)
	{
		this.redefinedVariable = toRedefine;
	}

	public Redefinition withMember(String name, VariableType type)
	{
		members.add(new Variable(redefinedVariable.level() + 1, redefinedVariable.scope(), name, type));
		return this;
	}

	public List<Variable> members()
	{
		return this.members;
	}

	public Variable redefinedVariable()
	{
		return redefinedVariable;
	}
}
