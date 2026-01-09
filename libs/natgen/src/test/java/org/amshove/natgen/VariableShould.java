package org.amshove.natgen;

import org.amshove.natgen.generatable.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class VariableShould
{
	@Test
	void generateItsName()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.alphanumeric(1));
		assertThat(variable.toString()).isEqualTo("#VAR");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariable()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.alphanumeric(1));
		var subVariable = variable.addVariable("SUB", VariableType.integer(4));
		assertThat(subVariable.toString()).isEqualTo("#VAR.SUB");
	}

	@Test
	void generateItsNameQualifiedWhenItIsASubvariableOnLevelGreaterThanTwo()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VAR", VariableType.group());
		var subVariable = variable.addVariable("SUB", VariableType.alphanumeric(10));
		var subsubVariable = subVariable.addVariable("SUBSUB", VariableType.integer(8));
		assertThat(subsubVariable.toString()).isEqualTo("#VAR.SUBSUB");
	}
}
