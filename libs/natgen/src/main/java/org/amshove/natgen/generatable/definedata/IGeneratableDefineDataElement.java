package org.amshove.natgen.generatable.definedata;

import org.amshove.natgen.generatable.IGeneratable;

public sealed interface IGeneratableDefineDataElement extends IGeneratable permits Variable,Using
{}
