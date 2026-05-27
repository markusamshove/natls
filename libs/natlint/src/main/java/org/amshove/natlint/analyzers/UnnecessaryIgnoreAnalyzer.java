package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ICallnatNode;
import org.amshove.natparse.natural.IFunctionCallNode;
import org.amshove.natparse.natural.IIgnoreNode;
import org.amshove.natparse.natural.IPerformNode;
import org.amshove.natparse.natural.IResetStatementNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.ISyntaxNode;

public class UnnecessaryIgnoreAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNNECESSARY_IGNORE = DiagnosticDescription.create(
		"NL025",
		"IGNORE is unnecessary",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNNECESSARY_IGNORE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IIgnoreNode.class, this::analyzeIgnore);
	}

	private void analyzeIgnore(ISyntaxNode node, IAnalyzeContext context)
	{
		var ignore = (IIgnoreNode) node;

		if (!(NodeUtil.findFirstParentOfType(ignore, IStatementListNode.class)instanceof IStatementListNode parent))
		{
			return;
		}

		var statementList = parent.statements();
		var ignoreStatementIndex = parent.statements().indexOf(ignore);

		// The IGNORE might be neccessary as a spearator between e.g. a CALLNAT and a function call,
		// so that the function call is not interpreted as an argument to the callnat. The Natural
		// compiler also does not catch this case and needs an IGNORE in between.
		if (ignoreSeparatesOperandListFromOperand(statementList, ignoreStatementIndex))
		{
			return;
		}

		if (statementList.size() > 1)
		{
			context.report(UNNECESSARY_IGNORE.createDiagnostic(ignore));
		}
	}

	private boolean ignoreSeparatesOperandListFromOperand(ReadOnlyList<IStatementNode> statementList, int ignoreIndex)
	{
		// If the IGNORE is the first or last statement, it can't separate statements :-)
		if (ignoreIndex == 0 || statementList.size() - 1 == ignoreIndex)
		{
			return false;
		}

		var nextStatement = statementList.get(ignoreIndex + 1);
		if (!(nextStatement instanceof IFunctionCallNode))
		{
			return false;
		}

		var previousStatement = statementList.get(ignoreIndex - 1);
		return switch (previousStatement)
		{
			case ICallnatNode _, IPerformNode _, IResetStatementNode _ -> true;
			default -> false;
		};
	}
}
