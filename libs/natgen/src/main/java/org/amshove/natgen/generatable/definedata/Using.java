package org.amshove.natgen.generatable.definedata;

import org.amshove.natparse.natural.VariableScope;

public record Using(VariableScope scope, String name) implements IGeneratableDefineDataElement
{
	@Override
	public String generate()
	{
		return "%s USING %s".formatted(scope.name(), name);
	}
}
