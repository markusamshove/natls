package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.amshove.natparse.natural.VariableScope;
import org.junit.jupiter.api.Test;

import static org.amshove.natgen.generatable.NaturalCode.*;

class ExamineShould extends CodeGenerationTest
{
	private final Variable examined = new Variable(1, VariableScope.LOCAL, "#EXAMINED", VariableType.alphanumericDynamic());

	@Test
	void generateAnExamineReplace()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineReplaceFirst()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.replaceFirstWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' REPLACE FIRST WITH 'World'""");
	}

	@Test
	void generateAnExamineDelete()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.delete();

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' DELETE""");
	}

	@Test
	void generateAnExamineDeleteFirst()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.deleteFirst();

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' DELETE FIRST""");
	}

	@Test
	void generateAnExamineReplaceForPattern()
	{
		var examine = examine(examined)
			.forPattern(stringLiteral(".?_"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR PATTERN '.?_' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineFullReplace()
	{
		var examine = examineFull(examined)
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE FULL #EXAMINED FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithDirectionForward()
	{
		var examine = examine(examined)
			.forward()
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE DIRECTION FORWARD #EXAMINED FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithDirectionBackward()
	{
		var examine = examine(examined)
			.backward()
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE DIRECTION BACKWARD #EXAMINED FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithDirectionVariable()
	{
		var direction = new Variable(1, VariableScope.LOCAL, "#DIRECTION", VariableType.alphanumeric(10));
		var examine = examine(examined)
			.direction(direction)
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE DIRECTION #DIRECTION #EXAMINED FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithDefaultDelimiters()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.withDelimiter()
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' WITH DELIMITER REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithSpecificDelimiters()
	{
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.withDelimiter(stringLiteral("-"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' WITH DELIMITER '-' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithStartPosition()
	{
		var examine = examine(examined)
			.startingFrom(plain("5"))
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED STARTING FROM 5 FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateAnExamineWithStartAndEndPosition()
	{
		var examine = examine(examined)
			.startingFromEndingAt(plain("5"), plain("10"))
			._for(stringLiteral("Hello"))
			.replaceWith(stringLiteral("World"));

		assertGenerated(examine, """
			EXAMINE #EXAMINED STARTING FROM 5 ENDING AT 10 FOR 'Hello' REPLACE WITH 'World'""");
	}

	@Test
	void generateASimpleExamineGiving()
	{
		var number = new Variable(1, VariableScope.LOCAL, "#NUMBER", VariableType.integer(4));
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.givingNumber(number);

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' GIVING NUMBER #NUMBER""");
	}

	@Test
	void generateAnExamineGivingPosition()
	{
		var position = new Variable(1, VariableScope.LOCAL, "#POSITION", VariableType.integer(4));
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.givingPosition(position);

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' GIVING POSITION #POSITION""");
	}

	@Test
	void generateAnExamineGivingLength()
	{
		var length = new Variable(1, VariableScope.LOCAL, "#LENGTH", VariableType.integer(4));
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.givingLength(length);

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' GIVING LENGTH #LENGTH""");
	}

	@Test
	void generateAnExamineGivingIndex()
	{
		var index = new Variable(1, VariableScope.LOCAL, "#INDEX", VariableType.integer(4));
		var examine = examine(examined)
			._for(stringLiteral("Hello"))
			.givingIndex(index);

		assertGenerated(examine, """
			EXAMINE #EXAMINED FOR 'Hello' GIVING INDEX #INDEX""");
	}

	@Test
	void generateAndFormatABigExamine()
	{
		var number = new Variable(1, VariableScope.LOCAL, "#NUMBER", VariableType.integer(4));
		var length = new Variable(1, VariableScope.LOCAL, "#LENGTH", VariableType.integer(4));
		var position = new Variable(1, VariableScope.LOCAL, "#POSITION", VariableType.integer(4));

		var examine = examineFull(examined)
			.forPattern(stringLiteral("._"))
			.forward()
			.givingPosition(position)
			.givingNumber(number)
			.givingLength(length)
			.replaceFirstWith(stringLiteral("Hello"));

		assertGenerated(examine, """
				EXAMINE DIRECTION FORWARD FULL #EXAMINED FOR PATTERN '._' REPLACE FIRST WITH 'Hello'
				  GIVING NUMBER #NUMBER GIVING POSITION #POSITION GIVING LENGTH #LENGTH""");
	}
}
