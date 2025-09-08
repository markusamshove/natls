package org.amshove.natls.languageserver.inputstructure;

import org.amshove.natls.viewer.InputStructure;
import org.amshove.natparse.natural.output.IOutputNewLineNode;
import org.amshove.natparse.natural.output.IOutputOperandNode;
import org.amshove.natparse.natural.output.ITabulatorElementNode;

import java.util.ArrayList;
import java.util.List;

public class InputStructureResponse
{
	private List<InputResponseElement> elements;

	private InputStructureResponse()
	{

	}

	public static InputStructureResponse fromInputStructure(InputStructure structure)
	{
		if (structure == null)
		{
			return null;
		}

		var response = new InputStructureResponse();
		response.elements = new ArrayList<>();
		int elementId = 1;
		var row = 1;
		var column = 1;
		for (var operand : structure.getOperands())
		{
			InputResponseElement responseElement = switch (operand) {
				case IOutputOperandNode operandNode -> {
					var element = new InputOperandElement(operandNode, row, column, structure.statementAttributes());
					column += element.getLength();
					yield element;
				}
				case IOutputNewLineNode ignored ->
				{
					row++;
					column=1;
					yield new InputResponseElement(InputStructureElementKind.NEW_LINE);
				}
				case InputSpaceElement spaceElement ->
				{
					column += spaceElement.getSpaces();
					yield new InputSpaceElement(spaceElement.getSpaces());
				}
				case ITabulatorElementNode tabElement ->{
					column = tabElement.tabs();
					yield new InputColumnPositionElement(tabElement.tabs());
				}
				default -> throw new RuntimeException("TODO");
			};

			responseElement.setId(elementId++);
			response.elements.add(responseElement);

		}
		return response;
	}

	public List<InputResponseElement> getElements()
	{
		return elements;
	}

	public void setElements(List<InputResponseElement> elements)
	{
		this.elements = elements;
	}
}
