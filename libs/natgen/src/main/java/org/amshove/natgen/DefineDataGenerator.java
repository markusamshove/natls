package org.amshove.natgen;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.VariableScope;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefineDataGenerator
{
	public String generate(CodeGenerator generator)
	{
		return """
			DEFINE DATA%s
			END-DEFINE""".formatted(generateVariables(generator));
	}

	private String generateVariables(CodeGenerator generator)
	{
		var code = new StringBuilder();

		var groupedByScope = generator.variables().stream().collect(Collectors.groupingBy(Variable::scope));

		generateScoped(code, VariableScope.GLOBAL, groupedByScope);
		generateScoped(code, VariableScope.PARAMETER, groupedByScope);
		generateScoped(code, VariableScope.LOCAL, groupedByScope);
		generateScoped(code, VariableScope.INDEPENDENT, groupedByScope);

		return code.toString();
	}

	private void generateScoped(StringBuilder code, VariableScope scope, Map<VariableScope, List<Variable>> variablesByScope)
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
