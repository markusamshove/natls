package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IIncDirNode;

/**
 * INCDIR is always generated when you put a database field in a map
 *
 * CHKRULE=ON means the DDM and field name are checked to exist on build Not sure if this statement inlines rules from
 * Predict into the compiled code, or whether it just inserts a callout to rules
 */
class IncDirNode extends StatementNode implements IIncDirNode
{
	private SyntaxToken ddmName;
	private SyntaxToken fieldName;

	public void setDdmName(SyntaxToken ddmName)
	{
		this.ddmName = ddmName;
	}

	public void setFieldName(SyntaxToken fieldName)
	{
		this.fieldName = fieldName;
	}

	@Override
	public SyntaxToken ddmName()
	{
		return this.ddmName;
	}

	@Override
	public SyntaxToken fieldName()
	{
		return this.fieldName;
	}

}
