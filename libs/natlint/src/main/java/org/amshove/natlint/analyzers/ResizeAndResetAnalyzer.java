package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IRangedArrayAccessNode;
import org.amshove.natparse.natural.IResetStatementNode;
import org.amshove.natparse.natural.IResizeArrayNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

public class ResizeAndResetAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription RESIZE_AND_RESET_CAN_BE_MERGED = DiagnosticDescription.create(
		"NL042",
		"RESIZE and RESET on arrays can be merged to RESIZE AND RESET",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(RESIZE_AND_RESET_CAN_BE_MERGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IResizeArrayNode.class, this::analyzeResize);
	}

	private void analyzeResize(ISyntaxNode node, IAnalyzeContext context)
	{
		var resize = (IResizeArrayNode) node;

		var statementAfterResize = NodeUtil.findFirstStatementAfter(resize);

		// Only apply this if RESET directly follows
		if (!(statementAfterResize instanceof IResetStatementNode reset))
		{
			return;
		}

		var arrayReferencesInResize = reset.operands().stream()
			.filter(IVariableReferenceNode.class::isInstance)
			.map(o -> (IVariableReferenceNode) o)
			.filter(vr -> vr.reference() == resize.arrayToResize().reference())
			.toList();

		if (arrayReferencesInResize.isEmpty())
		{
			return;
		}

		var diagnostic = RESIZE_AND_RESET_CAN_BE_MERGED.createDiagnostic(resize);

		for (var reference : arrayReferencesInResize)
		{
			if (reference.dimensions().stream().allMatch(d -> d instanceof IRangedArrayAccessNode dim && dim.isUnbound()))
			{
				diagnostic.addAdditionalInfo(new AdditionalDiagnosticInfo("Reset here", reference.position()));
			}
		}

		if (diagnostic.additionalInfo().hasItems())
		{
			context.report(diagnostic);
		}
	}
}
