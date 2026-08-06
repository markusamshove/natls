package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;

public interface IVariableReferenceNode extends ISymbolReferenceNode, IOperandNode, ITypeInferable
{
	ReadOnlyList<IOperandNode> dimensions();

	default boolean hasLabelReference()
	{
		return directDescendantsOfType(ITokenNode.class)
			.anyMatch(t -> t.token().kind() == SyntaxKind.LABEL_IDENTIFIER);
	}

	default ITokenNode labelReference()
	{
		var identifier = directDescendantsOfType(ITokenNode.class)
			.filter(t -> t.token().kind() == SyntaxKind.LABEL_IDENTIFIER)
			.findFirst();

		if (identifier.isPresent())
		{
			return identifier.get();
		}
		else
		{
			throw new NullPointerException("use hasLabelReference first");
		}
	}

}
