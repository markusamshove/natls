package org.amshove.natgen;

import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class VariableShould
{
	@Test
	void generateItsName()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", "(A1)");
		assertThat(variable.toString()).isEqualTo("#VAR");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariable()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", "(A1)");
		var subVariable = variable.addVariable("SUB", "(I4)");
		assertThat(subVariable.toString()).isEqualTo("#VAR.SUB");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariableOnLevelGreaterThanTwo()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", "(A1)");
		var subVariable = variable.addVariable("SUB", "(I4)");
		var subsubVariable = subVariable.addVariable("SUBSUB", "(I8)");
		assertThat(subsubVariable.toString()).isEqualTo("#VAR.SUBSUB");
	}
}
