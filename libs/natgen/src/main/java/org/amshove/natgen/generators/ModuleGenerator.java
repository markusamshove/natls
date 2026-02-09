package org.amshove.natgen.generators;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.Subroutine;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.jspecify.annotations.Nullable;

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
			case FUNCTION -> throw new UnsupportedOperationException("Can't generate functions through invocation of ModuleGenerator::generate. Use ModuleGenerator::generateFunction instead.");
			default -> throw new UnsupportedOperationException("Module generation for " + type + " is not supported yet.");
		};
	}

	/// Generate the [CodeGenerationContext] into a Natural function.
	/// Natural Source Header will be included.
	public String generateFunction(CodeGenerationContext context, String name, @Nullable VariableType returnType)
	{
		var codeBuilder = new CodeBuilder();
		var defineDataGenerator = new DefineDataGenerator();
		appendSourceHeader(codeBuilder);
		codeBuilder
			.append("DEFINE FUNCTION ").appendLine(name);

		if (returnType != null)
		{
			codeBuilder.indent().append("RETURNS ").append(returnType).unindent().lineBreak();
		}

		codeBuilder
			.lineBreak()
			.append(defineDataGenerator.generate(context))
			.lineBreak();

		appendStatements(context, codeBuilder);

		codeBuilder
			.lineBreak()
			.lineBreak()
			.appendLine("END-FUNCTION")
			.appendLine("END");

		return codeBuilder.toString();
	}

	private String generateProgram(CodeGenerationContext context)
	{
		var codeBuilder = new CodeBuilder();
		var defineDataGenerator = new DefineDataGenerator();
		appendSourceHeader(codeBuilder);
		codeBuilder
			.append(defineDataGenerator.generate(context))
			.lineBreak();

		appendStatements(context, codeBuilder);

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
			.append("DEFINE DATA ").appendLine(scope.name());
		appendSourceHeader(codeBuilder);
		for (var variable : context.variables())
		{
			codeBuilder.append(defineDataGenerator.generateVariableDeclarationWithoutScope(variable));
		}
		codeBuilder
			.lineBreak()
			.appendLine("END-DEFINE");

		return codeBuilder.toString();
	}

	private void appendSourceHeader(CodeBuilder codeBuilder)
	{
		codeBuilder
			.appendLine("/* >Natural Source Header 000000")
			.appendLine("/* :Mode S")
			.appendLine("/* :CP")
			.appendLine("/* <Natural Source Header");
	}

	private void appendStatements(CodeGenerationContext context, CodeBuilder codeBuilder)
	{
		for (var statement : context.statements())
		{
			codeBuilder.lineBreak();

			if (statement instanceof Subroutine)
			{
				// Separate a subroutine declaration from
				// other statements to make it more visible
				codeBuilder.lineBreak();
			}

			statement.generateInto(codeBuilder);
		}
	}
}
