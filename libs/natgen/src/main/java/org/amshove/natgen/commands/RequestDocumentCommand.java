package org.amshove.natgen.commands;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.amshove.natgen.CliOutput;
import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.NatGen;
import org.amshove.natgen.NaturalGenerationException;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generators.ModuleGenerator;
import org.amshove.natgen.generators.RequestDocumentForOpenApiGenerator;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import picocli.CommandLine;

@CommandLine.Command(name = "request-document")
public class RequestDocumentCommand implements Callable<Integer>
{
	private static final CliOutput output = new CliOutput();
	private static final String OUTPUT_RESULT_FORMAT = "%s %s => %s";

	@CommandLine.Parameters(arity = "1")
	File openApiSchemaFile;

	@CommandLine.Option(names =
	{
		"--base-name"
	}, description = "Sets the base name for generated modules. Must be maximum 6 characters", required = true)
	String moduleBaseName;

	@CommandLine.Option(names = "--output-dir", description = "Path to the directory where generated modules are saved")
	Path outputDirectory;

	private int currentModuleNumber = 1;

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

		var moduleNumberCharacterAmount = 8 - moduleBaseName.length() - 1; // 1 = suffix I, O, ..
		var moduleNameFormat = moduleBaseName + "%0" + moduleNumberCharacterAmount + "d";

		for (var path : openApi.getPaths().entrySet())
		{
			for (var httpMethodOperation : path.getValue().readOperationsMap().entrySet())
			{
				try
				{
					generateHttpMethod(openApi, moduleNameFormat, path, httpMethodOperation);
				}
				catch (NaturalGenerationException e)
				{
					output.error("ERROR on %s %s (skipping): %s", httpMethodOperation.getKey(), path.getKey(), e.getMessage());
				}
			}
		}

		return 0;
	}

	private void generateHttpMethod(OpenAPI openApi, String moduleNameFormat, Entry<String, PathItem> path, Entry<HttpMethod, Operation> httpMethodOperation) throws IOException
	{
		var theMethod = httpMethodOperation.getKey().toString();
		var theOperation = httpMethodOperation.getValue();
		var moduleName = String.format(moduleNameFormat, currentModuleNumber++);

		var inputPdaContext = new CodeGenerationContext();
		var inputPdaName = moduleName + "I";
		var inputPdaGroup = inputPdaContext.addVariable(VariableScope.LOCAL, inputPdaName, VariableType.group());

		var outputPdaContext = new CodeGenerationContext();
		var outputPdaName = moduleName + "O";
		var outputPdaGroup = outputPdaContext.addVariable(VariableScope.LOCAL, outputPdaName, VariableType.group());

		var settings = new RequestDocumentForOpenApiGenerator.Settings();
		settings.setRequestBodyRootGroup(inputPdaGroup);
		settings.setResponseBodyRootGroup(outputPdaGroup);
		var subprogramContext = new RequestDocumentForOpenApiGenerator(openApi, settings)
			.generate(theMethod, path.getKey(), theOperation);

		var moduleGenerator = new ModuleGenerator();

		addOpenApiDocumentation(moduleGenerator, theOperation, theMethod, path.getKey());
		addGeneratorComment(moduleGenerator);

		if (!inputPdaGroup.children().isEmpty())
		{
			var pdaPath = outputDirectory.resolve(inputPdaName + ".NSA");
			generatePda(subprogramContext, inputPdaName, pdaPath, moduleGenerator, inputPdaContext);
			output.info(OUTPUT_RESULT_FORMAT, theMethod, path.getKey(), pdaPath);
		}

		if (!outputPdaGroup.children().isEmpty())
		{
			var pdaPath = outputDirectory.resolve(outputPdaName + ".NSA");
			generatePda(subprogramContext, outputPdaName, pdaPath, moduleGenerator, outputPdaContext);
			output.info(OUTPUT_RESULT_FORMAT, theMethod, path.getKey(), pdaPath);
		}

		var generatedModule = moduleGenerator.generate(subprogramContext, NaturalFileType.SUBPROGRAM);
		var subprogramPath = outputDirectory.resolve(moduleName + "N.NSN");
		output.info(OUTPUT_RESULT_FORMAT, theMethod, path.getKey(), subprogramPath);

		Files.writeString(subprogramPath, generatedModule, StandardCharsets.UTF_8);
	}

	private void generatePda(
		CodeGenerationContext subprogramContext, String outputPdaName, Path pdaPath,
		ModuleGenerator moduleGenerator, CodeGenerationContext pdaContext
	) throws IOException
	{
		subprogramContext.addUsing(VariableScope.PARAMETER, outputPdaName);

		var generatedPda = moduleGenerator.generate(pdaContext, NaturalFileType.PDA);
		Files.writeString(pdaPath, generatedPda, StandardCharsets.UTF_8);
	}

	private void addOpenApiDocumentation(
		ModuleGenerator moduleGenerator, Operation operation, String method,
		String path
	)
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
