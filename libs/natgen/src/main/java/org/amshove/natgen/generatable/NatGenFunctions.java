package org.amshove.natgen.generatable;

/// Contains modules that are used by NatGen and need to
/// be present within the Natural project
public class NatGenFunctions
{
	private NatGenFunctions()
	{}

	public static IGeneratable jsonBooleanToLogical(IGeneratable jsonBoolean)
	{
		return NaturalCode.functionCall("JBOOL2L", jsonBoolean);
	}

	public static IGeneratable logicalToJsonBoolean(IGeneratable logical)
	{
		return NaturalCode.functionCall("L2JBOOL", logical);
	}
}
