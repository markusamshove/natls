package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefineDataGeneratorShould
{
	private final DefineDataGenerator sut = new DefineDataGenerator();

	@Test
	void generateASimpleDefineDataWithOneLocalVariable()
	{
		var context = new CodeGenerationContext();
		context.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				LOCAL
				1 #VARIABLE (A10)
				END-DEFINE""");
	}

	@Test
	void generateADefineDataWithTwoLocalVariables()
	{
		var context = new CodeGenerationContext();
		context.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		context.addVariable(VariableScope.LOCAL, "#SECOND", VariableType.alphanumericDynamic());

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				LOCAL
				1 #VARIABLE (A10)
				1 #SECOND (A) DYNAMIC
				END-DEFINE""");
	}

	@Test
	void generateADefineDataWithMixedScope()
	{
		var context = new CodeGenerationContext();
		context.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		context.addVariable(VariableScope.PARAMETER, "#PARAM", VariableType.alphanumericDynamic());

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				PARAMETER
				1 #PARAM (A) DYNAMIC
				LOCAL
				1 #VARIABLE (A10)
				END-DEFINE""");
	}

	@Test
	void generateScopesInCorrectOrder()
	{
		var context = new CodeGenerationContext();
		context.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		context.addVariable(VariableScope.INDEPENDENT, "+AIV-VARIABLE", VariableType.alphanumeric(10));
		context.addVariable(VariableScope.PARAMETER, "#PARAM", VariableType.alphanumericDynamic());
		context.addVariable(VariableScope.GLOBAL, "#G-GLOBAL", VariableType.alphanumeric(10));

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				GLOBAL
				1 #G-GLOBAL (A10)
				PARAMETER
				1 #PARAM (A) DYNAMIC
				LOCAL
				1 #VARIABLE (A10)
				INDEPENDENT
				1 +AIV-VARIABLE (A10)
				END-DEFINE""");
	}

	@Test
	void generateSubVariables()
	{

		var context = new CodeGenerationContext();
		var group = context.addVariable(VariableScope.LOCAL, "#GRP", VariableType.group());
		group.addVariable("#VAR", VariableType.integer(4));

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				LOCAL
				1 #GRP
				  2 #VAR (I4)
				END-DEFINE""");
	}
}
