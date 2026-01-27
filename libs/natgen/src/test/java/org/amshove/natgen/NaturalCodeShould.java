package org.amshove.natgen;

import org.amshove.natgen.generatable.NaturalCode;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NaturalCodeShould
{
	@Test
	void generateAnAssignmentWithNestedVal()
	{
		var variable = new Variable(1, VariableScope.LOCAL, "#VARNUM", VariableType.integer(4));
		var alpha = new Variable(1, VariableScope.LOCAL, "#VARALPH", VariableType.alphanumericDynamic());
		assertGenerated(
			NaturalCode.assignment(
				variable, NaturalCode.val(alpha)
			),
			"#VARNUM := VAL(#VARALPH)"
		);
	}

	private static void assertGenerated(NaturalCode code, String expectation)
	{
		assertThat(code.generate()).isEqualTo(expectation);
	}
}
