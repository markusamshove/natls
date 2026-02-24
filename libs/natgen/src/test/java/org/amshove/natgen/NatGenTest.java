package org.amshove.natgen;

import org.amshove.testhelpers.OutputCapture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

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

}
