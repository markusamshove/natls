package org.amshove.natls.languageserver.inputstructure;

public class InputResponseElement
{
	private final String kind;
	private int id;

	protected InputResponseElement(String kind)
	{
		this.kind = kind;
	}

	public String getKind()
	{
		return kind;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}
