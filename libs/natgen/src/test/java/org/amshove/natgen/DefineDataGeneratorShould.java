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
		var generator = new CodeGenerator();
		generator.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));

		assertThat(sut.generate(generator))
			.isEqualTo("""
				DEFINE DATA
				LOCAL
				1 #VARIABLE (A10)
				END-DEFINE""");
	}

	@Test
	void generateADefineDataWithTwoLocalVariables()
	{
		var generator = new CodeGenerator();
		generator.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		generator.addVariable(VariableScope.LOCAL, "#SECOND", VariableType.alphanumericDynamic());

		assertThat(sut.generate(generator))
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
		var generator = new CodeGenerator();
		generator.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		generator.addVariable(VariableScope.PARAMETER, "#PARAM", VariableType.alphanumericDynamic());

		assertThat(sut.generate(generator))
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
		var generator = new CodeGenerator();
		generator.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));
		generator.addVariable(VariableScope.INDEPENDENT, "+AIV-VARIABLE", VariableType.alphanumeric(10));
		generator.addVariable(VariableScope.PARAMETER, "#PARAM", VariableType.alphanumericDynamic());
		generator.addVariable(VariableScope.GLOBAL, "#G-GLOBAL", VariableType.alphanumeric(10));

		assertThat(sut.generate(generator))
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

		var generator = new CodeGenerator();
		var group = generator.addVariable(VariableScope.LOCAL, "#GRP", VariableType.group());
		group.addVariable("#VAR", VariableType.integer(4));

		assertThat(sut.generate(generator))
			.isEqualTo("""
				DEFINE DATA
				LOCAL
				1 #GRP
				  2 #VAR (I4)
				END-DEFINE""");
	}
}
