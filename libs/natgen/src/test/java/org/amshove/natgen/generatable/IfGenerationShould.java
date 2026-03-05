package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.amshove.natgen.generatable.NaturalCode.*;
import static org.amshove.natgen.generatable.conditions.Conditions.*;

class IfGenerationShould extends CodeGenerationTest
{
	@Test
	void generateASimpleIf()
	{
		assertGenerated(_if(_true()), """
			IF TRUE
			  IGNORE
			END-IF""");
	}

	@Test
	void generateASimpleIfElse()
	{
		var _if = _if(_true());
		_if._else();

		assertGenerated(_if, """
			IF TRUE
			  IGNORE
			ELSE
			  IGNORE
			END-IF""");
	}

	@Test
	void generateElseIfs()
	{
		var _if = _if(_true());
		_if.elseIf(_false());
		_if._else();

		assertGenerated(_if, """
			IF TRUE
			  IGNORE
			ELSE IF FALSE
			  IGNORE
			ELSE
			  IGNORE
			END-IF""");
	}

	@Test
	void generateIfSpecified()
	{
		var variable = new Variable(1, VariableScope.PARAMETER, "#P-OPTIONAL", VariableType.alphanumeric(10));
		assertGenerated(_if(specified(variable)).addToBody(assignment(variable, stringLiteral("Hi"))), """
			IF #P-OPTIONAL SPECIFIED
			  #P-OPTIONAL := 'Hi'
			END-IF""");
	}

	@Test
	void generateIfNotSpecified()
	{
		var variable = new Variable(1, VariableScope.PARAMETER, "#P-OPTIONAL", VariableType.alphanumeric(10));
		assertGenerated(_if(notSpecified(variable)).addToBody(assignment(variable, stringLiteral("Hi"))), """
			IF #P-OPTIONAL NOT SPECIFIED
			  #P-OPTIONAL := 'Hi'
			END-IF""");
	}

	@Test
	void generateInvertedConditionals()
	{
		assertGenerated(_if(not(_false())), """
			IF NOT FALSE
			  IGNORE
			END-IF""");
	}

	@Test
	void generateEqualsCondition()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(equal(lhs, rhs)), """
			IF #VAR1 = #VAR2
			  IGNORE
			END-IF""");
	}

	@Test
	void generateNotEqualsCondition()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(notEqual(lhs, rhs)), """
			IF #VAR1 <> #VAR2
			  IGNORE
			END-IF""");
	}

	@Test
	void generateGreaterCondition()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(greaterThan(lhs, rhs)), """
			IF #VAR1 > #VAR2
			  IGNORE
			END-IF""");
	}

	@Test
	void generateGreaterEqualCondition()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(greaterEqual(lhs, rhs)), """
			IF #VAR1 >= #VAR2
			  IGNORE
			END-IF""");
	}

	@Test
	void generateLessThanConditional()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(lessThan(lhs, rhs)), """
			IF #VAR1 < #VAR2
			  IGNORE
			END-IF""");
	}

	@Test
	void generateLessEqualConditional()
	{
		var lhs = new Variable(1, VariableScope.LOCAL, "#VAR1", VariableType.alphanumericDynamic());
		var rhs = new Variable(1, VariableScope.LOCAL, "#VAR2", VariableType.alphanumericDynamic());
		assertGenerated(_if(lessEqual(lhs, rhs)), """
			IF #VAR1 <= #VAR2
			  IGNORE
			END-IF""");
	}
}
