package org.amshove.natgen;

import org.amshove.natgen.commands.GenerateCommand;
import org.amshove.natgen.commands.ParseJsonCommand;
import picocli.CommandLine;

public class App
{
	static void main(String[] args)
	{
		var cli = new CommandLine(new GenerateCommand())
			.addSubcommand(new ParseJsonCommand());
		System.exit(cli.execute(args));
	}
}
