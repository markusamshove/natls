package org.amshove.natgen.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "generate")
public class GenerateCommand implements Callable<Integer>
{
	@Override
	public Integer call() throws Exception
	{
		return 0;
	}
}
