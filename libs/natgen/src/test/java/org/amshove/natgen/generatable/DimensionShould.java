package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeGenerationTest;
import org.amshove.natgen.VariableType;
import org.amshove.natgen.generatable.definedata.Variable;
import org.junit.jupiter.api.Test;

import static org.amshove.natgen.generatable.NaturalCode.newLocalVariable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DimensionShould extends CodeGenerationTest
{
	@Test
	void generateAStaticRange()
	{
		assertGenerated(
			Dimension.staticRange(1, 10),
			"1:10"
		);
	}

	@Test
	void throwAnExceptionIfLowerBoundIsLessThanOne()
	{
		assertThatThrownBy(() -> Dimension.staticRange(-1, 10))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Lower array bound can't be less than 1");
	}

	@Test
	void generateARangeWithVariables()
	{
		var dimension = Dimension.range(
			newLocalVariable("#LOW", VariableType.integer(4)),
			newLocalVariable("#UP", VariableType.integer(4))
		);
		assertGenerated(
			dimension,
			"#LOW:#UP"
		);
		assertThat(dimension.lowerBound()).isInstanceOf(Variable.class);
	}

	@Test
	void generateARangeWithStaticLowerAndVariableUpper()
	{
		var dimension = Dimension.range(1, newLocalVariable("#UP", VariableType.integer(4)));
		assertGenerated(
			dimension,
			"1:#UP"
		);
		assertThat(dimension.upperBound()).isInstanceOf(Variable.class);
	}

	@Test
	void generateARangeWithStaticLowerAndVariableUpperWhenOnlySpecifyingUpper()
	{
		var dimension = Dimension.upperBound(newLocalVariable("#UP", VariableType.integer(4)));
		assertGenerated(
			dimension,
			"1:#UP"
		);
		assertThat(dimension.isUpperUnbound()).isFalse();
		assertThat(dimension.isLowerUnbound()).isFalse();
	}

	@Test
	void generateAnUpperUnboundArray()
	{
		var dimension = Dimension.upperUnbound();
		assertGenerated(
			dimension,
			"1:*"
		);
		assertThat(dimension.isUpperUnbound()).isTrue();
		assertThat(dimension.isLowerUnbound()).isFalse();
	}
}
