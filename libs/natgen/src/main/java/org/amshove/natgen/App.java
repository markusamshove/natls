package org.amshove.natgen;

public class App
{
	public static void main(String[] args)
	{
		var json = """
[
    {
        "temperatur": 34.11,
        "beschreibung": "warm"
    },
    {
        "temperatur": 23.86,
        "beschreibung": "warm"
    },
    {
        "temperatur": 16.87,
        "beschreibung": "warm"
    },
    {
        "temperatur": 5.14,
        "beschreibung": "mild"
    },
    {
        "temperatur": 19.36,
        "beschreibung": "warm"
    }
]
""";

		var source = new ParseJsonGenerator().generate(json);
		System.out.println(source);
	}

}
