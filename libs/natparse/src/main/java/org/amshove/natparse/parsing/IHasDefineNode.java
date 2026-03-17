package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IDefineData;

/**
 * Interface for nodes that can have a define node nested in them Ok, this is just INCDIC
 */
interface IHasDefineNode
{
	/**
	 * Return true if the node actually has a define node
	 */
	public boolean hasDefineNode();

	/**
	 * Return the define node
	 */
	public IDefineData defineNode();
}
