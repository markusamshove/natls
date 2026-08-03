package org.amshove.natlint.cli;

import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.amshove.natlint.cli.sinks.FileStatusSink;
import org.amshove.natlint.cli.sinks.LcovDiagnosticSink;
import org.amshove.natlint.coverage.CoverageAnalyzer;

@CommandLine.Command(name = "coverage", description = "Generates a negative coverage baseline for the project in the working dir")
public class CoverageCommand implements Callable<Integer>
{

	@CommandLine.Option(names =
	{
		"--coverage"
	}, description = "Analyzer will generate a negative coverage map for your source. You can merge this with positive coverage.", defaultValue = "false")
	boolean coverageMode;

	@CommandLine.Option(names =
	{
		"-w", "--workdir"
	}, description = "Sets the working directory to a different path than the current one")
	String workingDirectory;

	private DiagnosticSinkType sinkType = DiagnosticSinkType.LCOV;

	@Override
	public Integer call() throws Exception {
		var workingDirectoryPath = workingDirectory != null ? workingDirectory : System.getProperty("user.dir");
		var theWorkingDirectory = Paths.get(workingDirectoryPath);
		var analyzer = new CoverageAnalyzer(
			theWorkingDirectory,
			FileStatusSink.dummy(),
			new AnalyzerOutputFlags(false)
		);
		return analyzer.run();
	}
}
