package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.*;
import org.jspecify.annotations.Nullable;

class DefinePrototypeNode extends StatementNode implements IDefinePrototypeNode, IModuleReferencingNode
{

	private SyntaxToken prototypeName;
	private IVariableReferenceNode variableReference;
	private INaturalModule referencedFunction;

	@Override
	public SyntaxToken nameToken()
	{
		return variableReference != null ? variableReference.referencingToken() : prototypeName;
	}

	@Override
	public boolean isVariable()
	{
		return variableReference != null;
	}

	@Nullable
	@Override
	public IVariableReferenceNode variableReference()
	{
		return variableReference;
	}

	void setPrototype(SyntaxToken token)
	{
		prototypeName = token;
	}

	void setVariableReference(IVariableReferenceNode reference)
	{
		this.variableReference = reference;
	}

	void setReferencedFunction(INaturalModule module)
	{
		this.referencedFunction = module;
	}

	@Override
	public INaturalModule reference()
	{
		return referencedFunction;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return prototypeName;
	}

	@Override
	public ReadOnlyList<IOperandNode> providedParameter()
	{
		// A prototype definition does not *pass* parameter
		return ReadOnlyList.empty();
	}
}
