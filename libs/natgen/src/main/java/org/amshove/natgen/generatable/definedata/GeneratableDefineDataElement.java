package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.generatable.IGeneratable;

public sealed interface GeneratableDefineDataElement extends IGeneratable permits Variable, Using
{
}
