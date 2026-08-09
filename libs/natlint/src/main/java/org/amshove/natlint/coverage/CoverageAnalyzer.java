package org.amshove.natlint.coverage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.cli.AnalyzerOutputFlags;
import org.amshove.natlint.cli.DiagnosticSinkType;
import org.amshove.natlint.cli.ProjectAnalyzer;
import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natlint.cli.sinks.IDiagnosticSink;
import org.amshove.natlint.cli.sinks.FileStatusSink.MessageType;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.PlainPosition;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.IDecideForConditionBranchNode;
import org.amshove.natparse.natural.IDecideOnBranchNode;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.IStatementVisitor;
import org.amshove.natparse.natural.ISubroutineNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ISyntaxTree;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;

/**
 * This analyzer generates baseline LCOV output for the project.
 *
 * This is intended to be merged with the coverage output from the Natural coverage reporting, which only reports lines
 * that HAVE executed, not lines that COULD execute.
 */
public class CoverageAnalyzer extends ProjectAnalyzer
{

	private IDiagnosticSink coverageSink;

	public CoverageAnalyzer(Path workingDirectory, FileStatusSink fileStatusSink, AnalyzerOutputFlags outputFlags)
	{
		super(
			workingDirectory,
			fileStatusSink,
			outputFlags
		);
		coverageSink = DiagnosticSinkType.LCOV.createSink(workingDirectory);
	}

	@Override
	protected List<IDiagnostic> filterDiagnostics(ReadOnlyList<IDiagnostic> diagnostics)
	{
		return List.of();
	}

	@Override
	protected boolean shouldAnalyzeFile(NaturalFile file)
	{

		if (file.getFiletype() == NaturalFileType.DDM)
		{
			// DdmParser isn't called in the CLI. DDMs will be parsed on demand.
			return false;
		}

		if (file.isFailedOnInit())
		{
			fileStatusSink.printError(file.getPath(), MessageType.INDEX_EXCEPTION, file.getInitException());
			return false;
		}

		return true;
	}

	@Override
	protected void printUnderBanner()
	{
		System.out.println("Generating coverage ...");
	}

	@Override
	protected void analyzeFile(NaturalFile file, ArrayList<IDiagnostic> allDiagnosticsInFile, TokenList tokens, INaturalModule module)
	{
		var context = new CoverageContext(module, allDiagnosticsInFile);
		analyze(module.syntaxTree(), context);

		coverageSink.printDiagnostics(0, file.getPath(), allDiagnosticsInFile);
	}

	private static class CoverageContext implements IAnalyzeContext
	{

		private INaturalModule module;
		private List<IDiagnostic> diagnostics;

		CoverageContext(INaturalModule module, List<IDiagnostic> diagnostics)
		{
			this.module = module;
			this.diagnostics = diagnostics;
		}

		@Override
		public String getConfiguration(NaturalFile forFile, String property, String defaultValue)
		{
			return "none";
		}

		@Override
		public INaturalModule getModule()
		{
			return module;
		}

		@Override
		public void report(IDiagnostic diagnostic)
		{
			diagnostics.add(diagnostic);
		}
	}

	private void analyze(ISyntaxTree syntaxTree, IAnalyzeContext context)
	{
		for (var descendant : syntaxTree.descendants())
		{
			analyzeCoverage(descendant, context);
			if (!(descendant instanceof ITokenNode))
			{
				analyze(descendant, context);
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

	private static class CoverageVisitor implements IStatementVisitor
	{
		private IAnalyzeContext context;

		CoverageVisitor(IAnalyzeContext context)
		{
			this.context = context;
		}

		private void cover(IStatementNode node)
		{
			try
			{
				context.report(new CoverageDiagnostic(node.position()));
			}
			catch (IndexOutOfBoundsException inout)
			{
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

}
