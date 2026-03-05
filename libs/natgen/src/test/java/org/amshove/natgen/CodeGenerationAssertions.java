package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.IGeneratableDefineDataElement;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natgen.generators.DefineDataGenerator;
import org.amshove.natparse.natural.VariableScope;
import org.jspecify.annotations.NonNull;

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

	public CodeGenerationAssertions hasParameter(int level, String name, VariableType expectedType)
	{
		var variables = collectParameter();
		assertThat(variables)
			.as("No level <%d> Parameter with name <%s> and type <%s> found".formatted(level, name, expectedType))
			.anyMatch(v -> v.level() == level && v.scope() == VariableScope.PARAMETER && v.generate().equals(name) && v.type().toString().equals(expectedType.toString()));

		return this;
	}

	public CodeGenerationAssertions hasParameterByValue(int level, String name, VariableType expectedType)
	{
		var variables = collectParameter();
		assertThat(variables)
			.as("No level <%d> Parameter with name <%s> and type <%s> found".formatted(level, name, expectedType))
			.anyMatch(v -> v.level() == level && v.scope() == VariableScope.PARAMETER && v.generate().equals(name) && v.type().toString().equals(expectedType.toString()) && v.isByValue());

		return this;
	}

	public CodeGenerationAssertions hasVariable(int level, String name, VariableScope scope, VariableType expectedType)
	{
		var variables = collectVariables();
		assertThat(variables)
			.as("No level <%d> Variable with name <%s>, scope <%s> and type <%s> found".formatted(level, name, scope, expectedType))
			.anyMatch(v -> v.level() == level && v.scope() == scope && v.generate().equals(name) && v.type().toString().equals(expectedType.toString()));

		return this;
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
		var builder = generateStatementsIntoBuilder();
		assertThat(builder.toString())
			.isEqualToNormalizingNewlines(expectedSource);
		return this;
	}

	public CodeGenerationAssertions generatedStatementSourceContains(String expectedSource)
	{
		var builder = generateStatementsIntoBuilder();
		assertThat(builder.toString())
			.contains(expectedSource);
		return this;
	}

	private @NonNull CodeBuilder generateStatementsIntoBuilder()
	{
		var builder = new CodeBuilder();
		for (var statement : context.statements())
		{
			statement.generateInto(builder);
			builder.lineBreak();
		}
		return builder;
	}

	public CodeGenerationAssertions generatesDefineData(String expected)
	{
		var defineData = new DefineDataGenerator().generate(context);
		assertThat(defineData)
			.isEqualToNormalizingNewlines(expected);

		return this;
	}

	public CodeGenerationAssertions generatedDefineDataSourceContains(String expected)
	{
		var defineData = new DefineDataGenerator().generate(context);
		assertThat(defineData)
			.contains(expected);

		return this;
	}

	private Stream<Variable> collectParameter()
	{
		return context.parameter().stream().flatMap(this::collectParameter);
	}

	private Stream<Variable> collectParameter(IGeneratableDefineDataElement element)
	{
		return element instanceof Variable v
			? collectParameter(v)
			: Stream.empty();
	}

	private Stream<Variable> collectParameter(Variable parameter)
	{
		return Stream.concat(Stream.of(parameter), parameter.children().stream().flatMap(this::collectParameter));
	}

	private Stream<Variable> collectVariables()
	{
		return context.variables().stream().flatMap(this::collectVariables);
	}

	private Stream<Variable> collectVariables(Variable variable)
	{
		return Stream.concat(Stream.of(variable), variable.children().stream().flatMap(this::collectVariables));
	}
}
