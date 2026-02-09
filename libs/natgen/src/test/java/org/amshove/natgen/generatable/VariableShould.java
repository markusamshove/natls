package org.amshove.natgen.generatable;

import org.amshove.natgen.Dimension;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class VariableShould
{
	@Test
	void generateItsName()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.alphanumeric(1));
		assertThat(variable).hasToString("#VAR");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariable()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.alphanumeric(1));
		var subVariable = variable.addVariable("SUB", VariableType.integer(4));
		assertThat(subVariable).hasToString("#VAR.SUB");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariableOnLevelGreaterThanTwo()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.group());
		var subVariable = variable.addVariable("SUB", VariableType.alphanumeric(10));
		var subsubVariable = subVariable.addVariable("SUBSUB", VariableType.integer(8));
		assertThat(subsubVariable).hasToString("#VAR.SUBSUB");
	}

	@Test
	void generateAnArrayAccessWithOneDimension()
	{
		var variable = new Variable(
			1, VariableScope.LOCAL, "#ARR", VariableType.alphanumericDynamic().withDimension(
				Dimension.upperUnbound()
			)
		);
		assertThat(variable.arrayAccess(NaturalCode.plain("1")).generate())
			.isEqualTo("#ARR(1)");
	}

	@Test
	void generateAnArrayAccessWithMultipleDimensions()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#ARR", VariableType.alphanumericDynamic().withDimension(Dimension.upperUnbound()));
		assertThat(variable.arrayAccess(NaturalCode.plain("1"), NaturalCode.plain("2")).generate())
			.isEqualTo("#ARR(1, 2)");
	}

	@Test
	void generateAnArrayAccessWithVariablesAsAccessor()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#ARR", VariableType.alphanumericDynamic().withDimension(Dimension.upperUnbound()));
		var indexVariable = new Variable(1, VariableScope.LOCAL, "#I-ARR", VariableType.integer(4));
		assertThat(variable.arrayAccess(indexVariable).generate())
			.isEqualTo("#ARR(#I-ARR)");
	}
}
