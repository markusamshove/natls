package org.amshove.natgen.generators;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;

public class ModuleGenerator
{
	/// Generate the [CodeGenerationContext] into a module of the given [NaturalFileType].
	/// Natural Source Header will be included.
	public String generate(CodeGenerationContext context, NaturalFileType type)
	{
		return switch (type)
		{
			case LDA -> generateDataArea(context, VariableScope.LOCAL);
			case PDA -> generateDataArea(context, VariableScope.PARAMETER);
			case GDA -> generateDataArea(context, VariableScope.GLOBAL);
			case SUBPROGRAM, PROGRAM -> generateProgram(context);
			default -> throw new UnsupportedOperationException("Module generation for " + type + " is not supported yet.");
		};
	}

	private String generateProgram(CodeGenerationContext context)
	{
		var codeBuilder = new CodeBuilder();
		var defineDataGenerator = new DefineDataGenerator();
		codeBuilder
			.appendLine("/* >Natural Source Header 000000")
			.appendLine("/* :Mode S")
			.appendLine("/* :CP")
			.appendLine("/* <Natural Source Header")
			.append(defineDataGenerator.generate(context))
			.lineBreak()
			.lineBreak();

		context.statements().forEach(statement -> statement.generateInto(codeBuilder));

		codeBuilder
			.lineBreak()
			.appendLine("END");

		return codeBuilder.toString();
	}

	private String generateDataArea(CodeGenerationContext context, VariableScope scope)
	{
		var defineDataGenerator = new DefineDataGenerator();
		var codeBuilder = new CodeBuilder();
		codeBuilder
			.appendLine("DEFINE DATA ").append(scope.name())
			.appendLine("/* >Natural Source Header 000000")
			.appendLine("/* :Mode S")
			.appendLine("/* :CP")
			.appendLine("/* <Natural Source Header");
		for (var variable : context.variables())
		{
			codeBuilder.append(defineDataGenerator.generateVariableDeclarationWithoutScope(variable));
		}
		codeBuilder
			.lineBreak()
			.appendLine("END-DEFINE");

		return codeBuilder.toString();
	}
}
