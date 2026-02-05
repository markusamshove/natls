package org.amshove.natgen;

import org.amshove.natgen.generatable.IGeneratable;
import org.amshove.natgen.generatable.NaturalCode;
import org.junit.jupiter.api.Test;

class ExpandArrayShould extends CodeGenerationTest
{
	private final IGeneratable array = NaturalCode.plain("#ARR");

	@Test
	void generateASimpleExpand()
	{
		var expand = NaturalCode.expandArray(array, NaturalCode.plain("10"));
		assertGenerated(expand, "EXPAND ARRAY #ARR TO (1:10)");
	}

	@Test
	void generateASimpleExpandUpperToGeneratable()
	{
		var expand = NaturalCode.expandArray(array, NaturalCode.plain("#S-ARRAY"));
		assertGenerated(expand, "EXPAND ARRAY #ARR TO (1:#S-ARRAY)");
	}

	@Test
	void generateAnExpandForSpecificLowerAndUpperBound()
	{
		var expand = NaturalCode.expandArray(array, 5, 15);
		assertGenerated(expand, "EXPAND ARRAY #ARR TO (5:15)");
	}

	@Test
	void generateAnExpandForASpecificDimension()
	{
		var expand = NaturalCode.expandNthArrayDimension(array, 2, NaturalCode.plain("10"));
		assertGenerated(expand, "EXPAND ARRAY #ARR TO (*, 1:10)");
	}

	@Test
	void generateAnExpandForASpecificDimensionForSpecificLowerAndUpperBounds()
	{
		var expand = NaturalCode.expandNthArrayDimension(array, 3, 5, 10);
		assertGenerated(expand, "EXPAND ARRAY #ARR TO (*, *, 5:10)");
	}
}
