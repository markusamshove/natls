package org.amshove.natgen;

import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class VariableTypeShould
{
	@TestFactory
	Stream<DynamicTest> beTranslatableFromDataTypeAndGenerateTheSameTypeAgain()
	{
		return Stream.of(
			"L",
			"A10",
			"N7,2",
			"N9",
			"I4",
			"C",
			"D",
			"T",
			"B4",
			"U10",
			"F8",
			"P8",
			"P12,7"
		).map(typeInCode ->
		{
			var parsedType = DataType.fromString(typeInCode);
			var convertedType = VariableType.fromDataType(parsedType);
			var generatedType = convertedType.toString();
			return DynamicTest.dynamicTest(typeInCode, () -> assertThat(generatedType).isEqualTo("(%s)".formatted(typeInCode)));
		});
	}

	@TestFactory
	Stream<DynamicTest> translateFromVariableDataTypesAndGenerateTheSameTypeAgain()
	{
		return Stream.of(
			DataFormat.ALPHANUMERIC,
			DataFormat.UNICODE,
			DataFormat.BINARY
		).map(format ->
		{
			var parsedType = DataType.ofDynamicLength(format);
			var convertedType = VariableType.fromDataType(parsedType);
			var generatedType = convertedType.toString();
			return DynamicTest.dynamicTest(format.name(), () -> assertThat(generatedType).isEqualTo("(%s) DYNAMIC".formatted(format.identifier())));
		});
	}

	@Test
	void beAbleToCreateAnUnboundArray()
	{
		assertThat(VariableType.alphanumericDynamic().withDimension(Dimension.upperUnbound()))
			.hasToString("(A/1:*) DYNAMIC");
	}

	@Test
	void beAbleToCreateAUpperBoundArray()
	{
		assertThat(VariableType.alphanumericDynamic().withDimension(Dimension.upperBound(10)))
			.hasToString("(A/1:10) DYNAMIC");
	}
}
