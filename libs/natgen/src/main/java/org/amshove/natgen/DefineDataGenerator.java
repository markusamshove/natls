package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.IGeneratableDefineDataElement;
import org.amshove.natgen.generatable.definedata.Using;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefineDataGenerator
{
	/// Generates a DEFINE DATA block with all variables
	/// from [CodeGenerationContext].
	public String generate(CodeGenerationContext context)
	{
		var code = new CodeBuilder();
		code.append("DEFINE DATA");
		code.appendLine(generateVariables(context));
		code.append("END-DEFINE");
		return code.toString();
	}

	/// Generates all variables from [CodeGenerationContext]
	/// without `DEFINE DATA` and `END-DEFINE` blocks.
	public String generateVariables(CodeGenerationContext context)
	{
		var code = new CodeBuilder();
		var groupedByScope = context.variables().stream().collect(Collectors.groupingBy(Variable::scope));

		generateUsings(code, VariableScope.GLOBAL, context.usings());
		generateScoped(code, VariableScope.GLOBAL, groupedByScope);

		generateParameter(code, context.parameter());

		generateUsings(code, VariableScope.LOCAL, context.usings());
		generateScoped(code, VariableScope.LOCAL, groupedByScope);

		generateScoped(code, VariableScope.INDEPENDENT, groupedByScope);

		return code.toString();
	}

	/// Generates the declaration of a variable without the scope token but
	/// with child variables.
	public String generateVariableDeclarationWithoutScope(Variable variable)
	{
		var code = new CodeBuilder();

		generateVariable(code, variable);

		return code.toString();
	}

	private void generateParameter(CodeBuilder code, List<IGeneratableDefineDataElement> parameter)
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
					code.append(System.lineSeparator());
					generateVariable(code, variable);
					needsScopeToStart = false;
				}
			}
		}
	}

	private void generateUsings(CodeBuilder code, VariableScope scope, Map<VariableScope, Set<Using>> usings)
	{
		var usingsOfScope = usings.get(scope);

		if (usingsOfScope == null)
		{
			return;
		}

		for (var using : usingsOfScope)
		{
			code.lineBreak().append(using.generate());
		}
	}

	private void generateScoped(
		CodeBuilder code, VariableScope scope,
		Map<VariableScope, List<Variable>> variablesByScope
	)
	{
		if (!variablesByScope.containsKey(scope))
		{
			return;
		}

		var variables = variablesByScope.get(scope);

		code.lineBreak().append(scope).lineBreak();
		for (var i = 0; i < variables.size(); i++)
		{
			generateVariable(code, variables.get(i));
			if (i < variables.size() - 1)
			{
				code.lineBreak();
			}
		}
	}

	private void generateVariable(CodeBuilder code, Variable variable)
	{
		if (variable.level() > 1)
		{
			code.indent();
		}

		code.append(variable.level()).append(" ").append(variable.name());
		var type = variable.type().toString();
		if (!type.isBlank())
		{
			code.append(" ").append(type);
		}

		if (variable.constValue() != null)
		{
			code.append(" CONST<").append(variable.constValue()).append(">");
		}

		for (var redefinition : variable.redefinitions())
		{
			code.lineBreak();
			code.append(variable.level()).append(" REDEFINE ").append(variable.name());
			for (var redefineChild : redefinition.members())
			{
				code.lineBreak();
				generateVariable(code, redefineChild);
			}
		}

		for (var child : variable.children())
		{
			code.lineBreak();
			generateVariable(code, child);
		}

		if (variable.level() > 1)
		{
			code.unindent();
		}
	}
}
