package org.amshove.natgen;

import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefineDataGeneratorShould extends CodeGenerationTest
{
	private final CodeGenerationContext context = new CodeGenerationContext();

	@Test
	void generateASimpleDefineDataWithOneLocalVariable()
	{
		context.addVariable(VariableScope.LOCAL, "#VARIABLE", VariableType.alphanumeric(10));

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData("""
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

		assertOn(context)
			.generatesDefineData(
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

		assertOn(context)
			.generatesDefineData(
				"""
				DEFINE DATA
				GLOBAL USING GDA
				GLOBAL
				1 #GLBL (A7)
				PARAMETER
				1 PARAM (I4)
				PARAMETER USING PDA
				LOCAL USING LDA
				LOCAL
				1 #LCL (A5)
				INDEPENDENT
				1 +INDE (A2)
				END-DEFINE"""
			);
	}

	@Test
	void retainTheInsertionOrderOfUsings()
	{
		context.addUsing(VariableScope.LOCAL, "ZZZ");
		context.addUsing(VariableScope.LOCAL, "AAA");
		context.addUsing(VariableScope.LOCAL, "HHH");
		context.addUsing(VariableScope.LOCAL, "AA01");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL USING ZZZ
				LOCAL USING AAA
				LOCAL USING HHH
				LOCAL USING AA01
				END-DEFINE""");
	}

	@Test
	void retainTheInsertionOrderOfParameter()
	{
		var group = context.addVariable(VariableScope.PARAMETER, "#P-GROUP", VariableType.group());
		group.addVariable("#P-SUB", VariableType.logical());
		context.addUsing(VariableScope.PARAMETER, "MYPDA");
		context.addVariable(VariableScope.LOCAL, "#LOCAL", VariableType.integer(4));
		context.addVariable(VariableScope.PARAMETER, "#PARM2", VariableType.control());
		context.addUsing(VariableScope.PARAMETER, "MYPDA2");

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				PARAMETER
				1 #P-GROUP
				  2 #P-SUB (L)
				PARAMETER USING MYPDA
				PARAMETER
				1 #PARM2 (C)
				PARAMETER USING MYPDA2
				LOCAL
				1 #LOCAL (I4)
				END-DEFINE""");
	}

	@Test
	void generateASingleVariable()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#MYVAR", VariableType.alphanumericDynamic());
		var sut = new DefineDataGenerator();
		assertThat(sut.generateVariableDeclarationWithoutScope(variable))
			.isEqualToIgnoringNewLines("1 #MYVAR (A) DYNAMIC");
	}

	@Test
	void generateASingleConstant()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#MYVAR", VariableType.alphanumericDynamic());
		var sut = new DefineDataGenerator();

		variable.withConstantValue("'Hello'");

		assertThat(sut.generateVariableDeclarationWithoutScope(variable))
			.isEqualToIgnoringNewLines("1 #MYVAR (A) DYNAMIC CONST<'Hello'>");
	}

	@Test
	void generateRedefinitions()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#TO-REDEFINE", VariableType.numeric(20));

		context.addVariable(variable);

		variable
			.newRedefine()
			.withMember("#HALF-A", VariableType.alphanumeric(10))
			.withMember("#HALF-N", VariableType.numeric(10));

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 #TO-REDEFINE (N20)
				1 REDEFINE #TO-REDEFINE
				  2 #HALF-A (A10)
				  2 #HALF-N (N10)
				END-DEFINE""");
	}

	@Test
	void beAbleToRedefineAVariableMultipleTimes()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#TO-REDEFINE", VariableType.numeric(20));

		context.addVariable(variable);

		variable
			.newRedefine()
			.withMember("#HALF-A", VariableType.alphanumeric(10));
		variable
			.newRedefine()
			.withMember("#HALF-N", VariableType.numeric(10));

		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 #TO-REDEFINE (N20)
				1 REDEFINE #TO-REDEFINE
				  2 #HALF-A (A10)
				1 REDEFINE #TO-REDEFINE
				  2 #HALF-N (N10)
				END-DEFINE""");
	}

	@Test
	void beAbleToGenerateArrays()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#ARR", VariableType.integer(4).withDimension(Dimension.upperUnbound()));
		context.addVariable(variable);
		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 #ARR (I4/1:*)
				END-DEFINE""");
	}

	@Test
	void beAbleToGenerateGroupArrays()
	{
		var group = new Variable(1, VariableScope.LOCAL, "#GRP", VariableType.group().withDimension(Dimension.upperUnbound()));

		group.addVariable("#SUB1", VariableType.alphanumericDynamic());
		group.addVariable("#SUB2", VariableType.logical());

		context.addVariable(group);
		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 #GRP (1:*)
				  2 #SUB1 (A) DYNAMIC
				  2 #SUB2 (L)
				END-DEFINE""");
	}

	@Test
	void beAbleToGenerateMultiDimensionArrays()
	{
		var arr = new Variable(1, VariableScope.LOCAL, "#ARR", VariableType.logical().withDimension(new Dimension(1, 10)).withDimension(new Dimension(15, 20)));
		context.addVariable(arr);
		assertOn(context)
			.generatesDefineData("""
				DEFINE DATA
				LOCAL
				1 #ARR (L/1:10, 15:20)
				END-DEFINE""");
	}
}
