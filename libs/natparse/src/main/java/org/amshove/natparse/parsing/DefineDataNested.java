package org.amshove.natparse.parsing;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IParameterDefinitionNode;
import org.amshove.natparse.natural.IScopeNode;
import org.amshove.natparse.natural.IStatementVisitor;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxNodeVisitor;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IUsingNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.ddm.IDdmField;
import org.jspecify.annotations.Nullable;

class DefineDataNested implements IDefineData
{

	private IDefineData moduleDefine;
	private IDefineData ruleDefine;
	// Items that DefineDataNode implements

	public DefineDataNested(IDefineData moduleDefine, IDefineData ruleDefine)
	{
		this.moduleDefine = moduleDefine;
		this.ruleDefine = ruleDefine;
	}

	@Override
	public List<IVariableNode> findVariablesWithName(String symbolName)
	{
		return Stream.concat(
			ruleDefine.findVariablesWithName(symbolName).stream(),
			moduleDefine.findVariablesWithName(symbolName).stream()
		).toList();
	}

	@Override
	public @Nullable IDdmField findDdmField(String symbolName)
	{
		var ddmField = ruleDefine.findDdmField(symbolName);
		if (ddmField == null)
		{
			ddmField = moduleDefine.findDdmField(symbolName);
		}
		return ddmField;
	}

	@Override
	public ReadOnlyList<IUsingNode> localUsings()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<IUsingNode> parameterUsings()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<IUsingNode> globalUsings()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<IParameterDefinitionNode> declaredParameterInOrder()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<IVariableNode> variables()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public @Nullable IVariableNode findVariable(String symbolName)
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public @Nullable IScopeNode findFirstScopeNode(VariableScope scope)
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public @Nullable ISyntaxNode findLastScopeNode(VariableScope scope)
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<IUsingNode> usings()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<ITypedVariableNode> effectiveParameterInOrder()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	// ISyntaxNode

	@Override
	public ISyntaxNode parent()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public IPosition diagnosticPosition()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public boolean isInFile(Path path)
	{
		throw new RuntimeException("Not implemented");
		// return false;
	}

	@Override
	public IPosition position()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public Iterator<ISyntaxNode> iterator()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public ReadOnlyList<? extends ISyntaxNode> descendants()
	{
		throw new RuntimeException("Not implemented");
		// return null;
	}

	@Override
	public void destroy()
	{
		throw new RuntimeException("Not implemented");

	}

	// ISyntaxTree

	@Override
	public void acceptNodeVisitor(ISyntaxNodeVisitor visitor)
	{
		throw new RuntimeException("Not implemented");

	}

	@Override
	public void acceptStatementVisitor(IStatementVisitor visitor)
	{
		throw new RuntimeException("Not implemented");
	}

}
