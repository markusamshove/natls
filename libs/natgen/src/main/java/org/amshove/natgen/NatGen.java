package org.amshove.natgen;

import org.amshove.natgen.commands.GenerateCommand;
import org.amshove.natgen.commands.GenerateParseJsonCommand;
import org.amshove.natgen.commands.RequestDocumentCommand;
import picocli.CommandLine;

public class NatGen
{
	public static final String VERSION = NatGen.class.getPackage().getImplementationVersion();

	static void main(String[] args)
	{
		System.exit(new NatGen().run(args));
	}

	public int run(String... args)
	{
		var cli = new CommandLine(new GenerateCommand())
			.addSubcommand(new GenerateParseJsonCommand())
			.addSubcommand(new RequestDocumentCommand())
			.setCaseInsensitiveEnumValuesAllowed(true); // Must come after subcommands

		return cli.execute(args);
	}
}
