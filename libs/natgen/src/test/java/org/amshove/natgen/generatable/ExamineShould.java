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
}
