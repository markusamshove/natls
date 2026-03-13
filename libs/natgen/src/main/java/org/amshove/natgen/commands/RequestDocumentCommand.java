package org.amshove.natgen.commands;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.amshove.natgen.CliOutput;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.NatGen;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generators.ModuleGenerator;
import org.amshove.natgen.generators.RequestDocumentForOpenApiGenerator;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import picocli.CommandLine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "request-document")
public class RequestDocumentCommand implements Callable<Integer>
{
	private static final CliOutput output = new CliOutput();

	@CommandLine.Parameters(arity = "1")
	File openApiSchemaFile;

	@CommandLine.Option(names =
	{
		"--base-name"
	}, description = "Sets the base name for generated modules. Must be maximum 6 characters", required = true)
	String moduleBaseName;

	@CommandLine.Option(names = "--output-dir", description = "Path to the directory where generated modules are saved")
	Path outputDirectory;

	@Override
	public Integer call() throws Exception
	{
		outputDirectory = Objects.requireNonNullElse(outputDirectory, Path.of("."));

		if (!openApiSchemaFile.exists())
		{
			output.error("OpenAPI file %s not found", openApiSchemaFile.getAbsolutePath());
			return 1;
		}

		var options = new ParseOptions();
		options.setFlatten(true);
		var openApiParseResult = new OpenAPIParser().readLocation(openApiSchemaFile.getAbsolutePath(), null, options);
		var openApi = openApiParseResult.getOpenAPI();

		if (openApiParseResult.getMessages() != null && !openApiParseResult.getMessages().isEmpty())
		{
			for (var message : openApiParseResult.getMessages())
			{
				output.error("Open API: %s", message);
			}
		}

		var moduleNumberCharacterAmount = 8 - moduleBaseName.length() - 1; // 1 = suffix
		var moduleNameFormat = moduleBaseName + "%0" + moduleNumberCharacterAmount + "d";
		var currentModuleNumber = 1;

		for (var path : openApi.getPaths().entrySet())
		{
			for (var httpMethodOperation : path.getValue().readOperationsMap().entrySet())
			{
				var theMethod = httpMethodOperation.getKey().toString();
				var theOperation = httpMethodOperation.getValue();
				// TODO: Validate possible number of modules according to paths/operations
				var moduleName = String.format(moduleNameFormat, currentModuleNumber++);

				var pdaContext = new CodeGenerationContext();
				var outputPdaName = moduleName + "O";
				var pdaGroup = pdaContext.addVariable(VariableScope.LOCAL, outputPdaName, VariableType.group());

				var settings = new RequestDocumentForOpenApiGenerator.Settings();
				settings.setReturnBodyRootGroup(pdaGroup);
				var subprogramContext = new RequestDocumentForOpenApiGenerator(openApi, settings)
					.generate(theMethod, path.getKey(), theOperation);

				var moduleGenerator = new ModuleGenerator();

				addOpenApiDocumentation(moduleGenerator, theOperation, theMethod, path.getKey());
				addGeneratorComment(moduleGenerator);

				if (!pdaGroup.children().isEmpty())
				{
					subprogramContext.addUsing(VariableScope.PARAMETER, outputPdaName);

					var generatedPda = moduleGenerator.generate(pdaContext, NaturalFileType.PDA);
					var pdaPath = outputDirectory.resolve(outputPdaName + ".NSA");
					output.info("%s %s => %s", theMethod, path.getKey(), pdaPath);
					Files.writeString(pdaPath, generatedPda, StandardCharsets.UTF_8);
				}

				var generatedModule = moduleGenerator.generate(subprogramContext, NaturalFileType.SUBPROGRAM);
				var subprogramPath = outputDirectory.resolve(moduleName + "N.NSN");
				output.info("%s %s => %s", theMethod, path.getKey(), subprogramPath);

				Files.writeString(subprogramPath, generatedModule, StandardCharsets.UTF_8);
			}
		}

		return 0;
	}

	private void addOpenApiDocumentation(ModuleGenerator moduleGenerator, Operation operation, String method, String path)
	{
		moduleGenerator.addDocumentationLine("%s %s".formatted(method, path));
		if (operation.getSummary() != null)
		{
			moduleGenerator.addDocumentationLine("Summary: %s".formatted(operation.getSummary()));
		}

		if (operation.getDescription() != null)
		{
			for (var line : operation.getDescription().split("[\r\n]+"))
			{
				moduleGenerator.addDocumentationLine(line);
			}
		}

		if (operation.getTags() != null && !operation.getTags().isEmpty())
		{
			moduleGenerator.addDocumentationLine("");
			moduleGenerator.addDocumentationLine("Tags:");
			for (var tag : operation.getTags())
			{
				moduleGenerator.addDocumentationLine("- %s".formatted(tag));
			}
		}
	}

	private void addGeneratorComment(ModuleGenerator moduleGenerator)
	{
		moduleGenerator.addDocumentationLine("");
		moduleGenerator.addDocumentationLine("Generated by NatGen version %s".formatted(NatGen.VERSION));
	}
}
