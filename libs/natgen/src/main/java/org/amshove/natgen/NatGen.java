package org.amshove.natgen;

import org.amshove.natgen.commands.GenerateCommand;
import org.amshove.natgen.commands.ParseJsonCommand;
import picocli.CommandLine;

public class NatGen
{
	static void main(String[] args)
	{
		System.exit(new NatGen().run(args));
	}

	public int run(String... args)
	{
		var cli = new CommandLine(new GenerateCommand())
			.addSubcommand(new ParseJsonCommand());

		return cli.execute(args);
	}
}
