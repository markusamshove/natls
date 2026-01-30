package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeGenerationAssertions
{
	private final CodeGenerationContext context;

	private CodeGenerationAssertions(CodeGenerationContext context)
	{
		this.context = context;
	}

	public static CodeGenerationAssertions assertOn(CodeGenerationContext context)
	{
		return new CodeGenerationAssertions(context);
	}

	public CodeGenerationAssertions hasVariable(int level, String name, VariableScope scope)
	{
		var variables = collectVariables();
		assertThat(variables)
			.as("No level <%d> Variable with name <%s> and scope <%s> found".formatted(level, name, scope))
			.anyMatch(v -> v.level() == level && v.scope() == scope && v.generate().equals(name));

		return this;
	}

	public CodeGenerationAssertions generatesStatements(String expectedSource)
	{
		assertThat(context.statements().stream().map(IGeneratable::generate).collect(Collectors.joining(System.lineSeparator())))
			.isEqualToIgnoringNewLines(expectedSource);
		return this;
	}

	private Stream<Variable> collectVariables()
	{
		return context.variables().stream().flatMap(v -> collectVariables(v));
	}

	private Stream<Variable> collectVariables(Variable variable)
	{
		return Stream.concat(Stream.of(variable), variable.children().stream().flatMap(v -> collectVariables(v)));
	}
}
