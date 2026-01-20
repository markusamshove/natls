package org.amshove.natls.codemutation;

import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.natural.*;

public class FileEdits
{
	private static final CodeInsertionPlacer rangeFinder = new CodeInsertionPlacer();

	private FileEdits()
	{}

	public static FileEdit addVariable(LanguageServerFile file, String variableName, String variableType, VariableScope scope)
	{
		if (variableName.contains("."))
		{
			var split = variableName.split("\\.");
			var groupPart = split[0];
			var variablePart = split[1];
			return addVariableToGroup(file, groupPart, variablePart, variableType, scope);
		}
		var variableInsert = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return variableInsert.toFileEdit("%d %s %s".formatted(1, variableName, variableType));
	}

	private static FileEdit addVariableToGroup(LanguageServerFile file, String groupPart, String variablePart, String variableType, VariableScope scope)
	{
		var group = ((IHasDefineData) file.module()).defineData().findVariable(groupPart);
		if (group instanceof IGroupNode groupNode)
		{
			var insertion = rangeFinder.insertInNextLineAfter(groupNode.variables().last());
			return insertion.toFileEdit("2 %s %s".formatted(variablePart, variableType));
		}

		var insertion = rangeFinder.findInsertionPositionToInsertVariable(file, scope);
		return insertion.toFileEdit("1 %s%n2 %s %s".formatted(groupPart, variablePart, variableType));
	}

	public static FileEdit addUsing(LanguageServerFile file, UsingToAdd neededUsing)
	{
		if (alreadyHasUsing(neededUsing.name(), file))
		{
			return null;
		}

		return createUsingInsert(neededUsing, file);
	}

	public static FileEdit addSubroutine(LanguageServerFile file, String name, String source)
	{

		var subroutine = """
			/***********************************************************************
			DEFINE SUBROUTINE %s
			/***********************************************************************

			%s

			END-SUBROUTINE
			""".formatted(name, source);

		var insertion = rangeFinder.findInsertionPositionForStatementAtEnd(file);
		return insertion.toFileEdit(subroutine);
	}

	public static FileEdit addPrototype(LanguageServerFile inFile, IFunction calledFunction)
	{
		var insertion = rangeFinder.findInsertionPositionForStatementAtStart(inFile);

		var codegenContext = new CodeGenerationContext();

		var returnType = calledFunction.returnType();
		var parameter = calledFunction.defineData().declaredParameterInOrder();
		parameter.forEach(p ->
		{
			if (p instanceof IUsingNode using)
			{
				codegenContext.addUsing(VariableScope.PARAMETER, using.target().symbolName());
				return;
			}

			var variable = (IVariableNode) p;
			if (variable.level() == 1)
			{
				// fromParsedVariable already adds the child variables
				codegenContext.addVariable(Variable.fromParsedVariable(variable));
			}
		});

		var type = returnType == null ? null : VariableType.fromDataType(returnType);
		return insertion.toFileEdit(
			NaturalCode.definePrototype(NaturalCode.plain(calledFunction.name()), type, codegenContext).generate()
				+ System.lineSeparator()
		);
	}

	private static FileEdit createUsingInsert(UsingToAdd using, LanguageServerFile file)
	{
		var insertion = rangeFinder.findInsertionPositionToInsertUsing(file, using.scope());
		return insertion.toFileEdit("%s USING %s".formatted(using.scope(), using.name()));
	}

	private static boolean alreadyHasUsing(String using, LanguageServerFile file)
	{
		return ((IHasDefineData) file.module()).defineData().usings().stream().anyMatch(u -> u.target().symbolName().equals(using));
	}

}
