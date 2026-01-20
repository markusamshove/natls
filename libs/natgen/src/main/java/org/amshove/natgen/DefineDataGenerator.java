package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.IGeneratableDefineDataElement;
import org.amshove.natgen.generatable.definedata.Using;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.VariableScope;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefineDataGenerator
{
	/// Generates a DEFINE DATA block with all variables
	/// from {@code CodeGenerationContext}.
	public String generate(CodeGenerationContext context)
	{
		return """
			DEFINE DATA%s
			END-DEFINE""".formatted(generateVariables(context));
	}

	/// Generates all variables from {@code CodeGenerationContext}
	/// without `DEFINE DATA` and `END-DEFINE` blocks.
	public String generateVariables(CodeGenerationContext context)
	{
		var code = new StringBuilder();

		var groupedByScope = context.variables().stream().collect(Collectors.groupingBy(Variable::scope));

		generateUsings(code, VariableScope.GLOBAL, context.usings());
		generateScoped(code, VariableScope.GLOBAL, groupedByScope);

		generateParameter(code, context.parameter());

		generateUsings(code, VariableScope.LOCAL, context.usings());
		generateScoped(code, VariableScope.LOCAL, groupedByScope);

		generateScoped(code, VariableScope.INDEPENDENT, groupedByScope);

		return code.toString();
	}

	private void generateParameter(StringBuilder code, List<IGeneratableDefineDataElement> parameter)
	{
		if (parameter.isEmpty())
		{
			return;
		}

		var needsScopeToStart = true;
		for (var element : parameter)
		{
			switch (element)
			{
				case Using using -> {
					code.append(System.lineSeparator()).append(using.generate());
					needsScopeToStart = true;
				}
				case Variable variable ->
				{
					if (needsScopeToStart)
					{
						code.append(System.lineSeparator()).append("PARAMETER");
					}
					generateSubVariables(code, List.of(variable));
					needsScopeToStart = false;
				}
			}
		}
	}

	private void generateUsings(StringBuilder code, VariableScope scope, Map<VariableScope, Set<Using>> usings)
	{
		var usingsOfScope = usings.get(scope);

		if (usingsOfScope == null)
		{
			return;
		}

		for (var using : usingsOfScope)
		{
			code.append(System.lineSeparator()).append(using.generate());
		}
	}

	private void generateScoped(
		StringBuilder code, VariableScope scope,
		Map<VariableScope, List<Variable>> variablesByScope
	)
	{
		if (!variablesByScope.containsKey(scope))
		{
			return;
		}

		var variables = variablesByScope.get(scope);

		code.append(System.lineSeparator()).append(scope);
		generateSubVariables(code, variables);
	}

	private void generateSubVariables(StringBuilder code, List<Variable> variables)
	{
		if (variables.isEmpty())
		{
			return;
		}

		code.append(System.lineSeparator());
		for (var i = 0; i < variables.size(); i++)
		{
			var variable = variables.get(i);
			if (variable.level() > 1)
			{
				code.append(" ".repeat(variable.level()));
			}

			code.append(variable.level()).append(" ").append(variable.name());
			if (variable.type().format() != DataFormat.NONE)
			{
				code.append(" ").append(variable.type());
			}

			if (i < variables.size() - 1)
			{
				code.append(System.lineSeparator());
			}

			generateSubVariables(code, variable.children());
		}
	}
}
