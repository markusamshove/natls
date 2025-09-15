package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

class OperandChecker
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> checkOperands(Map<IOperandNode, EnumSet<OperandDefinition>> operandChecks)
	{
		for (var queuedCheck : operandChecks.entrySet())
		{
			// TODO: Type inference is also done in the TypeChecker, so at least twice now
			//       maybe the typechecker isn't needed anymore if all statements use the
			//       operator check queue
			var inferredType = TypeInference.inferType(queuedCheck.getKey());
			inferredType
				.ifPresent(type -> check(queuedCheck.getKey(), type, queuedCheck.getValue()));
		}
		return ReadOnlyList.from(diagnostics);
	}

	private void check(IOperandNode operand, IDataType type, EnumSet<OperandDefinition> definitionTable)
	{
		if (OperandDefinition.forDataFormat(type.format())instanceof OperandDefinition definition)
		{
			checkFormatDefinition(operand, definition, definitionTable);
		}

		var isScalar = true;
		var isArray = false;
		var isConst = operand instanceof ILiteralNode || isConstReference(operand);

		// TODO: Other array stuff? System vars/functions returning arrays?
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
			// TODO: no automated test
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
		EnumSet<OperandDefinition> definitionTable
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

	private static String formatAllowedDataFormats(EnumSet<OperandDefinition> definitionTable)
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

	private static String formatAllowedStructures(EnumSet<OperandDefinition> definitionTable)
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
}
