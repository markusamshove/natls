package org.amshove.natgen;

import org.amshove.testhelpers.OutputCapture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class NatGenTest
{
	protected NatGen natgen;

	@TempDir
	protected Path workingDirectory;

	protected OutputCapture output;

	@BeforeEach
	void setup()
	{
		natgen = new NatGen();
		output = OutputCapture.captureStdStreams();
	}

	@AfterEach
	void tearDown()
	{
		output.close();
	}

	protected void assertRunsSuccessful(String... args)
	{
		var exitCode = natgen.run(args);

		assertThat(exitCode)
			.as("NatGen did not finish with exit code 0. std err: %n%s".formatted(output.stdErr()))
			.isZero();
	}
}
