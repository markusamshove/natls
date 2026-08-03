package org.amshove.testhelpers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class OutputCapture implements AutoCloseable
{
	private final PrintStream originalStdout;
	private final PrintStream originalStderr;

	private final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
	private final ByteArrayOutputStream capturedStdErr = new ByteArrayOutputStream();

	private OutputCapture()
	{
		originalStdout = System.out;
		originalStderr = System.err;

		System.setOut(new PrintStream(capturedStdOut));
		System.setErr(new PrintStream(capturedStdErr));
	}

	public static OutputCapture captureStdStreams()
	{
		return new OutputCapture();
	}

	public String stdOut()
	{
		return capturedStdOut.toString(StandardCharsets.UTF_8);
	}

	public String stdErr()
	{
		return capturedStdErr.toString(StandardCharsets.UTF_8);
	}

	@Override
	public void close()
	{
		System.setOut(originalStdout);
		System.setErr(originalStderr);
	}
}
