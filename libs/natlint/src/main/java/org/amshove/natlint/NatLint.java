package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import org.amshove.natlint.cli.CoverageCommand;
import picocli.CommandLine;
import picocli.CommandLine.UnmatchedArgumentException;

public class NatLint
{
	public static void main(String[] args)
	{
		CommandLine coverageCommandLine = new CommandLine(new CoverageCommand());
		CommandLine analyzeCommandLine = new CommandLine(new AnalyzeCommand());

		try {
			coverageCommandLine.parseArgs(args);
			System.exit(coverageCommandLine.execute(args));
		}
		catch (UnmatchedArgumentException _)
		{
			// This is normal;
		}

		System.exit(analyzeCommandLine.execute(args));
	}
}
