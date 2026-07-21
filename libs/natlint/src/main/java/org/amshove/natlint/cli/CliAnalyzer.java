package org.amshove.natlint.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natlint.cli.sinks.FileStatusSink.MessageType;
import org.amshove.natlint.cli.sinks.IDiagnosticSink;
import org.amshove.natlint.editorconfig.EditorConfigParser;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natlint.linter.NaturalLinter;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.*;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CliAnalyzer extends ProjectAnalyzer
{
	private final IDiagnosticSink diagnosticSink;
	private final boolean disableLinting;
	private final AnalyzerPredicates predicates;
	private SlowestModule slowestLintedModule = new SlowestModule(Long.MIN_VALUE, "NONE");

	public CliAnalyzer(Path workingDirectory, IDiagnosticSink sink, FileStatusSink fileStatusSink, AnalyzerPredicates predicates, boolean disableLinting, AnalyzerOutputFlags outputFlags, String sourceEncoding)
	{
		super(workingDirectory, fileStatusSink, outputFlags);
		this.predicates = predicates;
		diagnosticSink = sink;
		this.disableLinting = disableLinting;
	}

	@Override
	protected void printUnderBanner()
	{
		predicates.printSettings();
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

		if (!predicates.shouldAnalyzeFile(file))
		{
			fileStatusSink.printStatus(file.getPath(), MessageType.FILE_EXCLUDED);
			return false;
		}

		return true;
	}

	@Override
	protected void analyzeFile(NaturalFile file, ArrayList<IDiagnostic> allDiagnosticsInFile, TokenList tokens, INaturalModule module)
	{
		if (module == null)
		{
			diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
			return;
		}

		if (tokens.sourceHeader().getProgrammingMode() == NaturalProgrammingMode.REPORTING)
		{
			fileStatusSink.printStatus(file.getPath(), MessageType.REPORTING_TYPE);
			diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
			return;
		}

		if (!disableLinting && module.programmingMode() != NaturalProgrammingMode.REPORTING)
		{
			var linterDiagnostics = lint(file, module, allDiagnosticsInFile);
			if (linterDiagnostics == null)
			{
				return;
			}
		}

		var totalDiagnosticsInFileById = allDiagnosticsInFile.stream()
			.collect(Collectors.groupingBy(IDiagnostic::id, Collectors.counting()));
		for (var diagnosticId : totalDiagnosticsInFileById.keySet())
		{
			if (!totalDiagnosticsById.containsKey(diagnosticId))
			{
				totalDiagnosticsById.computeIfAbsent(diagnosticId, _ -> new AtomicInteger())
					.addAndGet(totalDiagnosticsInFileById.get(diagnosticId).intValue());
			}
		}

		totalDiagnostics.addAndGet(allDiagnosticsInFile.size());
		diagnosticSink.printDiagnostics(filesChecked.get(), file.getPath(), allDiagnosticsInFile);
		fileStatusSink.printStatus(file.getPath(), MessageType.SUCCESS);
	}

	@Override
	protected List<IDiagnostic> filterDiagnostics(ReadOnlyList<IDiagnostic> diagnostics)
	{
		return diagnostics.stream().filter(predicates::shouldPrintDiagnostic).toList();
	}

	private ReadOnlyList<IDiagnostic> lint(NaturalFile file, INaturalModule module, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var linter = new NaturalLinter();
			var lintStart = System.currentTimeMillis();
			var linterDiagnostics = linter.lint(module);
			var lintEnd = System.currentTimeMillis();
			if (slowestLintedModule.milliseconds() < lintEnd - lintStart)
			{
				slowestLintedModule = new SlowestModule(lintEnd - lintStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(linterDiagnostics);
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.LINT_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return linterDiagnostics;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.LINT_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

}
