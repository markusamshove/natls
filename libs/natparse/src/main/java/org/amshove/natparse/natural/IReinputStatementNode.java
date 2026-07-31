package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;

/**
 *
 * REINPUT [FULL] [(statement-parameters)] { USING HELP | WITH-TEXT-option } [MARK-option] [ALARM-option]
 *
 * WITH-TEXT-option : [WITH] [TEXT] { *operand1 | operand2 } [(attributes)] [,operand3]...7
 *
 * MARK-option : MARK [POSITION operand4 [IN]] [FIELD] { {operand5 | *fieldname} [(attributes)] }...
 *
 * ALARM-OPTION : [AND] [SOUND] ALARM
 */
public interface IReinputStatementNode extends IStatementNode
{
	boolean isUsingHelp();

	ReadOnlyList<IAttributeNode> statementAttributes();

}
