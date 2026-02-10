package org.amshove.natgen;

import org.amshove.testhelpers.OutputCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ParseJsonCommandShould
{
	private NatGen natgen;

	@TempDir
	Path workingDirectory;

	@BeforeEach
	void setup()
	{
		natgen = new NatGen();
	}

	@Test
	void generateCodeToStdout() throws IOException
	{
		try (var outs = OutputCapture.captureStdStreams())
		{
			var jsonFile = workingDirectory.resolve("test.json");
			Files.writeString(jsonFile, "{ \"name\": \"natgen\" }");

			natgen.run("parse-json", jsonFile.toAbsolutePath().toString());

			assertThat(outs.stdOut())
				.contains("DEFINE DATA")
				.contains("PARSE JSON");
		}
	}

	@Test
	void generateCodeToFiles() throws IOException
	{
		try (var _ = OutputCapture.captureStdStreams())
		{
			var jsonFile = workingDirectory.resolve("test.json");
			Files.writeString(jsonFile, "{ \"name\": \"natgen\" }");

			var pdaFile = workingDirectory.resolve("MYPDA.NSA");
			var moduleFile = workingDirectory.resolve("MOD.NSP");

			natgen.run(
				"parse-json", jsonFile.toAbsolutePath().toString(), "--out-pda",
				pdaFile.toAbsolutePath().toString(), "--out-module", moduleFile.toAbsolutePath().toString()
			);

			var pdaContent = Files.readString(pdaFile, StandardCharsets.UTF_8);
			var moduleContent = Files.readString(moduleFile, StandardCharsets.UTF_8);

			// PDA name is used as group name
			assertThat(pdaContent).contains("1 MYPDA");

			assertThat(moduleContent)
				.contains("USING MYPDA")
				.containsIgnoringWhitespaces("MYPDA.#NAME := ##JSON-PARSING.#VALUE");
		}
	}

	@Test
	void generateToASubprogramWhenNoFileExtensionIsPresent() throws IOException
	{
		try (var _ = OutputCapture.captureStdStreams())
		{
			var jsonFile = workingDirectory.resolve("test.json");
			Files.writeString(jsonFile, "{ \"name\": \"natgen\" }");

			var moduleFile = workingDirectory.resolve("MOD");

			natgen.run("parse-json", jsonFile.toAbsolutePath().toString(), "--out-module", moduleFile.toAbsolutePath().toString());

			// .NSN is added by the generator
			var moduleContent = Files.readString(moduleFile.getParent().resolve("MOD.NSN"), StandardCharsets.UTF_8);

			assertThat(moduleContent)
				.containsIgnoringWhitespaces("##PARSED-JSON.#NAME := ##JSON-PARSING.#VALUE");
		}
	}
}
