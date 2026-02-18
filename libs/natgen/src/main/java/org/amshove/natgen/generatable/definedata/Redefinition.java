package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.VariableType;
import java.util.ArrayList;
import java.util.List;

public final class Redefinition
{
	private final Variable redefinedVariable;
	private final List<RedefinitionMember> members = new ArrayList<>();

	Redefinition(Variable toRedefine)
	{
		this.redefinedVariable = toRedefine;
	}

	/// Add a variable to the redefinition and return the redefinition.
	public Redefinition withMember(String name, VariableType type)
	{
		addVariable(name, type);
		return this;
	}

	public Redefinition withFiller(int size)
	{
		members.add(new RedefinitionMember.Filler(size));
		return this;
	}

	public List<RedefinitionMember> members()
	{
		return this.members;
	}

	/// Add a variable to the redefinition and return the variable.
	public Variable addVariable(String name, VariableType type)
	{
		var variable = new Variable(redefinedVariable.level() + 1, redefinedVariable.scope(), name, type);
		variable.setParent(redefinedVariable());
		members.add(new RedefinitionMember.VariableMember(variable));
		return variable;
	}

	public Variable redefinedVariable()
	{
		return redefinedVariable;
	}

}
