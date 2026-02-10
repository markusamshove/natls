package org.amshove.natgen;

public class CliOutput
{
	/// Prints to stdout. Used for results of the program.
	public void print(String message)
	{
		System.out.println(message);
	}

	/// Prints to stdout. Used for results of the program.
	public void print(String format, Object... args)
	{
		print(String.format(format, args));
	}

	/// Prints to stderr. Used for progress, errors, ...
	public void info(String message)
	{
		System.err.println(message);
	}

	/// Prints to stderr. Used for progress, errors, ...
	public void info(String format, Object... args)
	{
		info(String.format(format, args));
	}
}
