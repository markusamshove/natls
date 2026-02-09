package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationContext;
import org.amshove.natgen.VariableType;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefinePrototypeGenerationShould
{
	private final CodeGenerationContext context = new CodeGenerationContext();

	@Test
	void generateAPrototypeWithoutReturnTypeAndParameter()
	{
		assertThat(
			NaturalCode.definePrototype(NaturalCode.plain("FUNC"), null, context)
				.generate()
		).isEqualToNormalizingNewlines("""
			DEFINE PROTOTYPE FUNC
			END-PROTOTYPE""");
	}

	@Test
	void generateAPrototypeWithReturnType()
	{
		assertThat(
			NaturalCode.definePrototype(NaturalCode.plain("FUNC"), VariableType.logical(), context)
				.generate()
		).isEqualToNormalizingNewlines("""
			DEFINE PROTOTYPE FUNC RETURNS (L)
			END-PROTOTYPE""");
	}

	@Test
	void generateAPrototypeWithReturnTypeAndMultipleParameter()
	{
		context.addVariable(VariableScope.PARAMETER, "P-PARAM", VariableType.alphanumericDynamic());
		context.addVariable(VariableScope.PARAMETER, "P-PARAM-2", VariableType.integer(4));

		assertThat(
			NaturalCode.definePrototype(NaturalCode.plain("FUNC"), VariableType.logical(), context)
				.generate()
		).isEqualToNormalizingNewlines("""
			DEFINE PROTOTYPE FUNC RETURNS (L)
			DEFINE DATA
			PARAMETER
			1 P-PARAM (A) DYNAMIC
			1 P-PARAM-2 (I4)
			END-DEFINE
			END-PROTOTYPE""");
	}
}
