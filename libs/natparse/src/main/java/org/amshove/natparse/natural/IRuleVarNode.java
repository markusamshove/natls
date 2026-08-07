package org.amshove.natparse.natural;

public interface IRuleVarNode extends IStatementWithBodyNode
{
	public enum Type
	{
		FREE_RULE,
		DICTIONARY_RULE
	}

	public Type type();
}
