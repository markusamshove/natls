package org.amshove.natgen.commands;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.generators.DefineDataGenerator;
import org.amshove.natgen.generators.ParseJsonFromJsonGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Command(name = "parse-json")
public class ParseJsonCommand implements Callable<Integer>
{
	@Parameters(arity = "1")
	File jsonFile;

	@Override
	public Integer call() throws Exception
	{
		var jsonContent = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
		var parseJsonGenerator = new ParseJsonFromJsonGenerator();
		var context = parseJsonGenerator.generate(jsonContent);

		var defineDataGenerator = new DefineDataGenerator();
		var defineData = defineDataGenerator.generate(context);
		System.out.println(defineData);
		System.out.println();
		var codeBuilder = new CodeBuilder();
		context.statements().getFirst().generateInto(codeBuilder);
		System.out.println(codeBuilder);

		return 0;
	}
}
