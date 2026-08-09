package org.amshove.natlint.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natlint.cli.sinks.FileStatusSink.MessageType;
import org.amshove.natlint.editorconfig.EditorConfigParser;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.*;
import org.amshove.natparse.parsing.NaturalParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.lang.management.ManagementFactory;

public abstract class ProjectAnalyzer
{
	private Path workingDirectory;
	protected final ActualFilesystem filesystem;
	protected final FileStatusSink fileStatusSink;
	protected final AtomicInteger filesChecked = new AtomicInteger();
	protected final AtomicLong linesOfCode = new AtomicLong();
	protected SlowestModule slowestLexedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
	protected SlowestModule slowestParsedModule = new SlowestModule(Long.MIN_VALUE, "NONE");
	protected final AtomicInteger totalDiagnostics = new AtomicInteger();
	protected Map<String, AtomicInteger> totalDiagnosticsById = new HashMap<>();
	protected final AtomicInteger exceptions = new AtomicInteger();
	private final AnalyzerOutputFlags outputFlags;

	private long maxMemoryInBytes = 0L;

	protected ProjectAnalyzer(Path workingDirectory, FileStatusSink fileStatusSink, AnalyzerOutputFlags outputFlags)
	{
		this.workingDirectory = workingDirectory;
		this.fileStatusSink = fileStatusSink;
		this.filesystem = new ActualFilesystem();
		this.outputFlags = outputFlags;
	}

	public int run()
	{
		while (!workingDirectory.getRoot().equals(workingDirectory) && filesystem.findNaturalProjectFile(workingDirectory).isEmpty())
		{
			workingDirectory = workingDirectory.getParent();
		}

		var projectFile = filesystem.findNaturalProjectFile(workingDirectory);

		if (workingDirectory.getRoot().equals(workingDirectory) || projectFile.isEmpty())
		{
			throw new RuntimeException("Project root could not be determined. .natural or _naturalBuild file not found");
		}

		var editorconfigPath = projectFile.get().getParent().resolve(".editorconfig");
		if (editorconfigPath.toFile().exists())
		{
			LinterContext.INSTANCE.updateEditorConfig(new EditorConfigParser().parse(filesystem.readFile(editorconfigPath)));
		}

		System.out.printf(
			"""
			     .@@@@@@@@@@@@@@@&
			    /@@@@@@@@@@@@@@@@@.          %s
			     @@@@@@@@*@@@@@@@@           Version: %s
			  ....###############            Project file: %s
			......###.@/##.@.####......      %s
			     .###############
			     .###############
			     .###############
			       ############.
			           ....
			%n""",
			CliAnalyzer.class.getPackage().getImplementationTitle(),
			CliAnalyzer.class.getPackage().getImplementationVersion(),
			projectFile.get().getFileName(),
			editorconfigPath.toFile().exists() ? ".editorconfig picked up" : ""
		);

		printUnderBanner();

		return analyze(projectFile.get());
	}

	private int analyze(Path projectFilePath)
	{
		var indexStartTime = System.currentTimeMillis();
		var project = new BuildFileProjectReader(filesystem).getNaturalProject(projectFilePath);
		new NaturalProjectFileIndexer().indexProject(project);
		var indexEndTime = System.currentTimeMillis();

		var startCheck = System.currentTimeMillis();
		for (var library : project.getLibraries())
		{
			library.files().parallelStream().forEach(file ->
			{

				if (!shouldAnalyzeFile(file))
				{
					return;
				}

				filesChecked.incrementAndGet();
				var allDiagnosticsInFile = new ArrayList<IDiagnostic>();

				var tokens = lex(file, allDiagnosticsInFile);
				if (tokens == null)
				{
					return;
				}

				var module = parse(file, tokens, allDiagnosticsInFile);

				analyzeFile(file, allDiagnosticsInFile, tokens, module);

			});
			var currentMemory = Runtime.getRuntime().totalMemory();
			if (currentMemory > maxMemoryInBytes)
			{
				maxMemoryInBytes = currentMemory;
			}
		}

		var endCheck = System.currentTimeMillis();

		var missingStartTime = System.currentTimeMillis();
		registerMissingFiles(project);
		var missingEndTime = System.currentTimeMillis();

		var indexTime = indexEndTime - indexStartTime;
		var checkTime = endCheck - startCheck;
		var missTime = missingEndTime - missingStartTime;
		var totalTime = indexTime + checkTime + missTime;

		var totalTimeSeconds = totalTime / 1000;
		System.out.println();
		System.out.println("Done.");
		System.out.printf("Index time: %d ms%n", indexTime);
		System.out.printf("Check time: %d ms%n", checkTime);
		System.out.printf("Miss time : %d ms%n", missTime);
		System.out.printf("Total: %d ms (%ds)%n", totalTime, totalTimeSeconds);
		System.out.println();
		System.out.printf("Files checked: %,d%n", filesChecked.get());
		System.out.printf("Lines of code: %,d%n", linesOfCode.get());
		System.out.printf("LoC/s: %,d%n", totalTimeSeconds > 0 ? (linesOfCode.get() / totalTimeSeconds) : linesOfCode.get());
		System.out.println();
		System.out.printf("Total diagnostics: %,d%n", totalDiagnostics.get());
		System.out.println("Exceptions: " + exceptions.get());
		System.out.println();
		System.out.println("Slowest lexed module: " + slowestLexedModule);
		System.out.println("Slowest parsed module: " + slowestParsedModule);
		// System.out.println("Slowest linted module: " + (disableLinting ? "disabled" : slowestLintedModule));
		// TODO: print specific stats
		System.out.println();
		System.out.printf("Peak memory usage: %.2f Mib%n", maxMemoryInBytes / 1024.0 / 1024.0);
		var gcs = 0L;
		var gcTime = 0L;
		for (var bean : ManagementFactory.getGarbageCollectorMXBeans())
		{
			gcs += bean.getCollectionCount();
			if (bean.getCollectionTime() > 0)
			{
				gcTime += bean.getCollectionTime();
			}
		}
		System.out.printf("Number of GCs: %d%n", gcs);
		System.out.printf("GC time: %ds%n", (gcTime / 1000));
		System.out.println();
		if (outputFlags.showDiagnosticStats())
		{
			System.out.println("Total diagnostics by ID");
			for (var diagnosticId : totalDiagnosticsById.keySet().stream().sorted().toList())
			{
				System.out.printf("%s: %,d%n", diagnosticId, totalDiagnosticsById.get(diagnosticId).get());
			}
			System.out.println();
		}

		return totalDiagnostics.get() > 0 ? 1 : 0;
	}

	private void registerMissingFiles(NaturalProject project)
	{
		if (!fileStatusSink.isEnabled())
		{
			return;
		}

		System.err.println("Started registration of missing files");
		var root = project.getRootPath().resolve("Natural-Libraries");
		System.out.println("Root: " + root.toString());

		try (var stream = Files.walk(root))
		{
			stream
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> fileStatusSink.printStatus(path, MessageType.FILE_MISSING));
			System.err.println("Finished registration of missing files");
		}
		catch (Exception e)
		{
			System.err.println("Registration of missing files failed");
			e.printStackTrace();
		}
	}

	private void countLinesOfCode(TokenList tokens)
	{
		var previousLine = -1;
		var totalLines = 0;
		for (var token : tokens)
		{
			if (token.line() != previousLine)
			{
				totalLines++;
				previousLine = token.line();
			}
		}

		linesOfCode.addAndGet(totalLines);
	}

	private TokenList lex(NaturalFile file, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var lexer = new Lexer();
			var lexStart = System.currentTimeMillis();
			var tokens = lexer.lex(filesystem.readFile(file.getPath()), file.getPath());
			var lexEnd = System.currentTimeMillis();
			countLinesOfCode(tokens);
			if (slowestLexedModule.milliseconds < lexEnd - lexStart)
			{
				slowestLexedModule = new SlowestModule(lexEnd - lexStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(tokens.diagnostics());
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.LEX_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return tokens;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.LEX_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

	private INaturalModule parse(NaturalFile file, TokenList tokens, ArrayList<IDiagnostic> allDiagnosticsInFile)
	{
		try
		{
			var parser = new NaturalParser();
			var parseStart = System.currentTimeMillis();
			var module = parser.parse(file, tokens);
			var parseEnd = System.currentTimeMillis();
			if (slowestParsedModule.milliseconds < parseEnd - parseStart)
			{
				slowestParsedModule = new SlowestModule(parseEnd - parseStart, file.getProjectRelativePath().toString());
			}

			var diagnostics = filterDiagnostics(module.diagnostics());
			fileStatusSink.printDiagnostics(file.getPath(), MessageType.PARSE_FAILED, diagnostics);
			allDiagnosticsInFile.addAll(diagnostics);
			return module;
		}
		catch (Exception e)
		{
			fileStatusSink.printError(file.getPath(), MessageType.PARSE_EXCEPTION, e);
			exceptions.incrementAndGet();
			System.out.println(file.getPath());
			e.printStackTrace();
			return null;
		}
	}

	protected abstract void analyzeFile(NaturalFile file, ArrayList<IDiagnostic> allDiagnosticsInFile, TokenList tokens, INaturalModule module);

	protected abstract void printUnderBanner();

	protected abstract boolean shouldAnalyzeFile(NaturalFile file);

	protected abstract List<IDiagnostic> filterDiagnostics(ReadOnlyList<IDiagnostic> diagnostics);

	public record SlowestModule(long milliseconds, String module)
	{
		@Override
		public String toString()
		{
			return "%dms (%s)".formatted(milliseconds, module);
		}
	}

}
