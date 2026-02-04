package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.IGeneratableStatement;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class CodeGenerationTest
{
	protected final CodeBuilder codeBuilder = new CodeBuilder();

	protected void assertGenerated(IGeneratableStatement statement, String expected)
	{
		statement.generateInto(codeBuilder);
		assertThat(codeBuilder.toString())
			.isEqualToIgnoringNewLines(expected);
	}

	protected void assertGenerated(IGeneratable generatable, String expected)
	{
		assertThat(generatable.generate()).isEqualTo(expected);
	}

	protected CodeGenerationAssertions assertOn(CodeGenerationContext context)
	{
		return CodeGenerationAssertions.assertOn(context);
	}
}
