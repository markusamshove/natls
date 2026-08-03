package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.ResizeAndResetAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IResetStatementNode;
import org.amshove.natparse.natural.IResizeArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class MergeResizeAndResetQuickFix extends AbstractQuickFix
{

	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(ResizeAndResetAnalyzer.RESIZE_AND_RESET_CAN_BE_MERGED, this::mergeResizeAndReset);
	}

	private CodeAction mergeResizeAndReset(QuickFixContext context)
	{
		var resizeStatement = (IResizeArrayNode) context.statementAtPosition();
		var nextStatement = NodeUtil.findFirstStatementAfter(resizeStatement);
		if (!(nextStatement instanceof IResetStatementNode resetStatement))
		{
			return null;
		}

		var editBuilder = new WorkspaceEditBuilder();

		var resizeKeyword = resizeStatement.descendants().first();
		editBuilder.changesText(resizeKeyword.position(), "RESIZE AND RESET");

		if (resetStatement.operands().size() == 1)
		{
			editBuilder.removesNode(resetStatement);
		}
		else
		{
			resetStatement.operands().stream()
				.filter(IVariableReferenceNode.class::isInstance)
				.map(IVariableReferenceNode.class::cast)
				.filter(vr -> vr.reference() == resizeStatement.arrayToResize().reference())
				.forEach(editBuilder::removesNode);
		}

		return new CodeActionBuilder("Merge RESIZE and RESET", CodeActionKind.QuickFix)
			.fixesDiagnostic(context.diagnostic())
			.appliesWorkspaceEdit(editBuilder.build())
			.build();
	}
}
