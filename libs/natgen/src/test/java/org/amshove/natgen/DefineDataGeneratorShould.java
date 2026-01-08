package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefineDataGeneratorShould
{
	private final DefineDataGenerator sut = new DefineDataGenerator();
	private final CodeGenerationContext context = new CodeGenerationContext();

	@Test
	void generateASimpleDefineDataWithOneLocalVariable()
	{
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

	@ParameterizedTest
	@ValueSource(strings =
	{
		"LOCAL", "GLOBAL", "PARAMETER"
	})
	void generateUsings(String scope)
	{
		var theScope = VariableScope.valueOf(scope);
		context.addUsing(theScope, "MYUSING");

		assertThat(sut.generate(context))
			.isEqualTo("""
				DEFINE DATA
				%s USING MYUSING
				END-DEFINE""".formatted(scope));
	}

	@Test
	void generateUsingsInCorrectOrder()
	{
		context.addUsing(VariableScope.LOCAL, "LDA");
		context.addUsing(VariableScope.PARAMETER, "PDA");
		context.addUsing(VariableScope.GLOBAL, "GDA");

		assertThat(sut.generate(context))
			.isEqualTo(
				"""
				DEFINE DATA
				GLOBAL USING GDA
				PARAMETER USING PDA
				LOCAL USING LDA
				END-DEFINE"""
			);
	}

	@Test
	void generateAMixOfUsingsAndVariables()
	{
		context.addVariable(VariableScope.PARAMETER, "PARAM", VariableType.integer(4));
		context.addUsing(VariableScope.LOCAL, "LDA");
		context.addUsing(VariableScope.PARAMETER, "PDA");
		context.addVariable(VariableScope.LOCAL, "#LCL", VariableType.alphanumeric(5));
		context.addUsing(VariableScope.GLOBAL, "GDA");
		context.addVariable(VariableScope.GLOBAL, "#GLBL", VariableType.alphanumeric(7));
		context.addVariable(VariableScope.INDEPENDENT, "+INDE", VariableType.alphanumeric(2));

		assertThat(sut.generate(context))
			.isEqualTo(
				"""
				DEFINE DATA
				GLOBAL USING GDA
				GLOBAL
				1 #GLBL (A7)
				PARAMETER USING PDA
				PARAMETER
				1 PARAM (I4)
				LOCAL USING LDA
				LOCAL
				1 #LCL (A5)
				INDEPENDENT
				1 +INDE (A2)
				END-DEFINE"""
			);
	}
}
