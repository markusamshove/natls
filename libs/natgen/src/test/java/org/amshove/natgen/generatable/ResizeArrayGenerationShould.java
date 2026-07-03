package org.amshove.natgen.generatable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.junit.jupiter.api.Test;

class ResizeArrayGenerationShould extends CodeGenerationTest
{
	@Test
	void generateASimpleResizeArray()
	{
		var array = NaturalCode.newLocalVariable("#ARR", VariableType.alphanumeric(10).withDimension(Dimension.upperUnbound()));
		assertGenerated(
			new ResizeArray(array, Dimension.staticRange(1, 10)),
			"RESIZE ARRAY #ARR TO (1:10)"
		);
	}

	@Test
	void generateAResizeWithMultipleDimensions()
	{
		var array = NaturalCode.newLocalVariable(
			"#ARR", VariableType.alphanumeric(10)
				.withDimension(Dimension.upperUnbound())
				.withDimension(Dimension.upperUnbound())
		);

		assertGenerated(
			new ResizeArray(array, Dimension.staticRange(1, 10), Dimension.staticRange(5, 8)),
			"RESIZE ARRAY #ARR TO (1:10,5:8)"
		);
	}

	@Test
	void generateAResizeAndReset()
	{
		var array = NaturalCode.newLocalVariable("#ARR", VariableType.alphanumeric(10).withDimension(Dimension.upperUnbound()));
		var dim = NaturalCode.newLocalVariable("#I", VariableType.integer(4));

		assertGenerated(
			new ResizeArray(array, Dimension.range(5, dim)).andReset(),
			"RESIZE AND RESET ARRAY #ARR TO (5:#I)"
		);
	}

	@Test
	void throwAnExceptionWhenTryingToGenerateAResizeWithUnmatchedDimensions()
	{
		var array = NaturalCode.newLocalVariable(
			"#ARR", VariableType.alphanumeric(10)
				.withDimension(Dimension.upperUnbound())
				.withDimension(Dimension.upperUnbound())
		);

		assertThatThrownBy(
			() -> new ResizeArray(array, Dimension.staticRange(1, 10))
		)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Can not resize array with 2 dimension(s) with 1 dimension(s)");
	}
}
