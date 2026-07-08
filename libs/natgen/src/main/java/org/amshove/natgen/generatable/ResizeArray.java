package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.generatable.definedata.Variable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ResizeArray implements IGeneratableStatement
{
	private final IGeneratable array;
	private final Dimension[] newDimensions;
	private boolean andReset;

	public ResizeArray(IGeneratable array, Dimension... newDimensions)
	{
		if (newDimensions.length == 0)
		{
			throw new IllegalArgumentException("Dimensions need to be specified for RESIZE ARRAY");
		}

		if (array instanceof Variable v && v.type().numberOfDimensions() != newDimensions.length)
		{
			throw new IllegalArgumentException("Can not resize array with %d dimension(s) with %d dimension(s)".formatted(v.type().numberOfDimensions(), newDimensions.length));
		}

		this.array = array;
		this.newDimensions = newDimensions;
	}

	public ResizeArray andReset()
	{
		andReset = true;
		return this;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder
			.append("RESIZE")
			.appendIf(andReset, " AND RESET")
			.append(" ARRAY ")
			.append(array)
			.append(" TO ")
			.append("(")
			.append(
				Arrays.stream(newDimensions)
					.map(IGeneratable::generate)
					.collect(Collectors.joining(","))
			)
			.append(")");
	}
}
