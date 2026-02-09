package org.amshove.natgen.commands;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natgen.generators.DefineDataGenerator;
import org.amshove.natgen.generators.ModuleGenerator;
import org.amshove.natgen.generators.ParseJsonFromJsonGenerator;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "parse-json")
public class ParseJsonCommand implements Callable<Integer>
{
	@Parameters(arity = "1")
	File jsonFile;

	@CommandLine.Option(names =
	{
		"--out-pda"
	}, description = "Generate the DEFINE DATA for the parsed json into the given PDA path")
	Path outputPda;

	@CommandLine.Option(names =
	{
		"--out-module"
	}, description = "Generate code in to the given file path")
	Path outModule;

	@Override
	public Integer call() throws Exception
	{
		System.out.println("/*********");
		System.out.printf("Generating code for %s%n", jsonFile);
		System.out.println("/*********");

		var generationSettings = new ParseJsonFromJsonGenerator.Settings();
		if (outputPda != null)
		{
			generationSettings.setParsedJsonGroupName(IFilesystem.filenameWithoutExtension(outputPda));
			generationSettings.setJsonSourceScope(VariableScope.PARAMETER);
		}

		var jsonContent = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
		var parseJsonGenerator = new ParseJsonFromJsonGenerator(generationSettings);
		var context = parseJsonGenerator.generate(jsonContent);

		CodeGenerationContext contextForDefineData = context;
		if (outputPda != null)
		{
			Variable jsonGroupVariable = null;
			for (var variable : context.variables())
			{
				if (variable.level() == 1 && variable.name().equals(generationSettings.parsedJsonGroupName()))
				{
					jsonGroupVariable = variable;
					break;
				}
			}

			Objects.requireNonNull(jsonGroupVariable);

			contextForDefineData = new CodeGenerationContext();
			contextForDefineData.addVariable(jsonGroupVariable);
			context.removeVariable(jsonGroupVariable);
			context.addUsing(VariableScope.PARAMETER, jsonGroupVariable.name());
		}

		printGeneratedDefineData(contextForDefineData, outputPda);
		printGeneratedCode(context, outModule);

		return 0;
	}

	private void printGeneratedDefineData(CodeGenerationContext context, Path filePath) throws IOException
	{
		if (filePath == null)
		{
			var defineDataGenerator = new DefineDataGenerator();
			var defineData = defineDataGenerator.generate(context);
			System.out.println(defineData);
			return;
		}

		var moduleGenerator = new ModuleGenerator();
		var code = moduleGenerator.generate(context, NaturalFileType.PDA);

		Files.writeString(filePath, code, StandardCharsets.UTF_8);
		System.out.printf("Generated %s%n", filePath);
	}

	private void printGeneratedCode(CodeGenerationContext context, Path filePath) throws IOException
	{
		if (filePath == null)
		{
			var codeBuilder = new CodeBuilder();
			context.statements().getFirst().generateInto(codeBuilder);
			System.out.println(codeBuilder);
			return;
		}

		if (!IFilesystem.hasExtension(filePath))
		{
			filePath = filePath.getParent().resolve(filePath.getFileName() + ".NSN");
		}

		var moduleGenerator = new ModuleGenerator();
		var generatedModule = moduleGenerator.generate(context, NaturalFileType.fromPath(filePath));
		Files.writeString(filePath, generatedModule, StandardCharsets.UTF_8);
		System.out.printf("Generated %s%n", filePath);
	}
}
