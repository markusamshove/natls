package org.amshove.natgen.generatable.definedata;

public sealed interface RedefinitionMember
{
	record VariableMember(Variable variable) implements RedefinitionMember
	{}

	record Filler(int size) implements RedefinitionMember
	{}
}
