package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.parsing.operandcheck.OperandCheck;
import org.amshove.natparse.parsing.operandcheck.OperandCheck.BinaryCheck;
import org.amshove.natparse.parsing.operandcheck.OperandCheck.DefinitionCheck;
import org.amshove.natparse.parsing.operandcheck.OperandDefinition;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

class OperandChecker
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> checkOperands(List<OperandCheck> operandChecks)
	{
		for (var queuedCheck : operandChecks)
		{
			var inferredType = TypeInference.inferType(queuedCheck.lhs());
			inferredType
				.ifPresent(type -> check(queuedCheck, type));
		}
		return ReadOnlyList.from(diagnostics);
	}

	private void check(OperandCheck operandCheck, IDataType type)
	{
		if (operandCheck instanceof DefinitionCheck definitionCheck)
		{
			check(definitionCheck.lhs(), type, definitionCheck.definitionTable());
		}
		else
			if (operandCheck instanceof BinaryCheck binaryCheck)
			{
				checkBinary(binaryCheck, type);
			}
	}

	private void checkBinary(BinaryCheck binaryCheck, IDataType lhsType)
	{
		var inferedRhsType = inferType(binaryCheck.rhs());

		if (inferedRhsType.format() != DataFormat.NONE && !inferedRhsType.hasCompatibleFormat(lhsType))
		{
			diagnostics.add(
				ParserErrors.typeMismatch(
					"Type mismatch between left and right operand. Left is %s, right is %s"
						.formatted(lhsType, inferedRhsType),
					binaryCheck.rhs()
				)
			);
		}
	}

	private void check(IOperandNode operand, IDataType type, Set<OperandDefinition> definitionTable)
	{
		if (OperandDefinition.forDataFormat(type.format())instanceof OperandDefinition definition)
		{
			checkFormatDefinition(operand, definition, definitionTable);
		}

		var isScalar = true;
		var isArray = false;
		var isConst = operand instanceof ILiteralNode || isConstReference(operand);

		if (operand instanceof IVariableReferenceNode varRef && varRef.dimensions().hasItems())
		{
			isArray = varRef.dimensions().stream().anyMatch(IRangedArrayAccessNode.class::isInstance);
			isScalar = !isArray;
		}

		// The reference is to an array. This is special for RESIZE, REDUCE and EXPAND because
		// they're the only statements that don't use index access but a plain reference to the array.
		var isPlainArrayRefStatement = operand.parent() instanceof IResizeArrayNode || operand.parent() instanceof IExpandArrayNode || operand.parent() instanceof IReduceArrayNode;
		if (isPlainArrayRefStatement && operand instanceof IVariableReferenceNode varRef && varRef.reference()instanceof IVariableNode variable && variable.isArray())
		{
			isArray = true;
			isScalar = false;
		}

		if (isScalar && !definitionTable.contains(OperandDefinition.STRUCTURE_SCALAR))
		{
			diagnostics.add(
				ParserErrors.typeMismatch(
					"Operand can not be a scalar value. %s".formatted(formatAllowedStructures(definitionTable)),
					operand
				)
			);
		}

		if (isArray && !definitionTable.contains(OperandDefinition.STRUCTURE_ARRAY))
		{
			diagnostics.add(
				ParserErrors.typeMismatch(
					"Operand can not be an array. %s".formatted(formatAllowedStructures(definitionTable)),
					operand
				)
			);
		}

		if (isConst && !definitionTable.contains(OperandDefinition.STRUCTURE_CONSTANT))
		{
			diagnostics.add(
				ParserErrors.referenceNotMutable(
					"Operand can not be a constant value. %s".formatted(formatAllowedStructures(definitionTable)),
					operand
				)
			);
		}
	}

	private boolean isConstReference(IOperandNode operand)
	{
		if (!(operand instanceof IVariableReferenceNode varRef) || !(varRef.reference()instanceof ITypedVariableNode typedVar))
		{
			return false;
		}

		return typedVar.type().isConstant();
	}

	private void checkFormatDefinition(
		IOperandNode operand, OperandDefinition definition,
		Set<OperandDefinition> definitionTable
	)
	{
		if (!definitionTable.contains(OperandDefinition.ALL_FORMATS) && !definitionTable.contains(definition))
		{
			diagnostics.add(
				ParserErrors.typeMismatch(
					"Operand can't be of format %s. Allowed formats: %s".formatted(definition.shortform(), formatAllowedDataFormats(definitionTable)),
					operand
				)
			);
		}
	}

	private static String formatAllowedDataFormats(Set<OperandDefinition> definitionTable)
	{
		var builder = new StringBuilder();
		var separator = "";

		for (var definition : OperandDefinition.FORMAT_DEFINITIONS)
		{
			if (definitionTable.contains(definition))
			{
				builder.append(separator);
				builder.append(definition.shortform());
				separator = ", ";
			}
		}

		return builder.toString();
	}

	private static String formatAllowedStructures(Set<OperandDefinition> definitionTable)
	{
		var builder = new StringBuilder("It must be one of: ");
		var separator = "";
		var wantedSeparator = ", ";

		if (definitionTable.contains(OperandDefinition.STRUCTURE_CONSTANT))
		{
			builder
				.append(separator)
				.append("a constant");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_SCALAR))
		{
			builder
				.append(separator)
				.append("a scalar value");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_ARRAY))
		{
			builder
				.append(separator)
				.append("an array");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_GROUP))
		{
			builder
				.append(separator)
				.append("a group");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_SYSTEM_VARIABLE))
		{
			builder
				.append(separator)
				.append("an unmodifiable system variable");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_MODIFIABLE_SYSTEM_VARIABLE_ONLY))
		{
			builder
				.append(separator)
				.append("a modifiable system variable");
			separator = wantedSeparator;
		}

		if (definitionTable.contains(OperandDefinition.STRUCTURE_ARITHMETIC_EXPRESSION))
		{
			builder
				.append(separator)
				.append("an arithmetic expression");
		}

		return builder.toString();
	}

	private IDataType inferType(IOperandNode operand)
	{
		return TypeInference.inferType(operand)
			.orElse(new DataType(DataFormat.NONE, IDataType.ONE_GIGABYTE)); // couldn't infer, don't raise something yet
	}
}
