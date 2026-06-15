package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

class ForGenerationShould extends CodeGenerationTest
{
	@Test
	void generateASimpleForLoop()
	{
		var iterator = new Variable(1, VariableScope.LOCAL, "#I", VariableType.integer(4));
		var size = new Variable(1, VariableScope.LOCAL, "#S", VariableType.integer(4));
		var startValue = NaturalCode.plain("1");

		assertGenerated(NaturalCode._for(iterator, startValue, size), """
			FOR #I := 1 TO #S
			  IGNORE
			END-FOR""");
	}
}
