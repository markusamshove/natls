package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natlint.coverage.CoverageDiagnostic;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.PlainPosition;
import org.amshove.natparse.natural.IDecideForConditionBranchNode;
import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IDefinePrototypeNode;
import org.amshove.natparse.natural.IEndNode;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.IIfStatementNode;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.IStatementVisitor;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.project.NaturalFileType;

/**
 * This analyzer generates baseline LCOV output for the project.
 *
 * This is intended to be merged with the coverage output from the Natural coverage reporting, which only reports lines
 * that HAVE executed, not lines that COULD execute.
 */
public class CoverageAnalyzer extends AbstractAnalyzer
{

	public static final DiagnosticDescription COVERED = DiagnosticDescription.create(
		"NL000",
		"This line could be covered",
		DiagnosticSeverity.INFO
	);

	private static boolean enabled = false;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		CoverageAnalyzer.enabled = enabled;
	}

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(COVERED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ISyntaxNode.class, this::analyzeCoverage);
	}

	private static class CoverageVisitor implements IStatementVisitor
	{
		private IAnalyzeContext context;

		CoverageVisitor(IAnalyzeContext context)
		{
			this.context = context;
		}

		private void cover(IStatementNode node) {
			try {
				context.report(new CoverageDiagnostic(node.position()));
			} catch (IndexOutOfBoundsException inout) {
				// This happens for statement list nodes that are empty
				// TODO: Work out why this happens
			}
		}

		public void visit(IStatementNode node) {
			switch (node) {
				case IDecideOnBranchNode _: break;
				case IDecideForConditionBranchNode _: break;
				case IDefineData         _: break;
				case ISubroutineNode     _: break;
				default:
					cover(node);
					break;
			}
		}
	}

	private void analyzeCoverage(ISyntaxNode iSyntaxNode, IAnalyzeContext context)
	{
		var coverageVisitor = new CoverageVisitor(context);
		if (context.isFiletype(NaturalFileType.GDA)
			&& iSyntaxNode instanceof IDefineData)
		{
			var endDefinePosition = iSyntaxNode.descendants().last().position();
			var eofPosition = new PlainPosition(
				endDefinePosition.offset() + 1, 0,
				endDefinePosition.line() + 1, 0,
				endDefinePosition.filePath()
			);
			context.report(new CoverageDiagnostic(eofPosition));
			return;
		}
		iSyntaxNode.acceptStatementVisitor(coverageVisitor);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{

	}

	@Override
	public void afterAnalyzing(IAnalyzeContext context)
	{

	}
}
