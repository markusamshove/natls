package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecideOn implements IGeneratableStatement
{
	private final IGeneratable decideOnReference;
	private final DecideOnValueCheck valuesToCheck;
	private final List<DecideOnValueBranch> branches = new ArrayList<>();
	private final DecideOnValueBranch noneBranch = new DecideOnValueBranch("NONE");
	private final DecideOnValueBranch anyBranch = new DecideOnValueBranch("ANY");
	private final DecideOnValueBranch allBranch = new DecideOnValueBranch("ALL");

	DecideOn(IGeneratable decideOnReference, DecideOnValueCheck valuesToCheck)
	{
		this.decideOnReference = decideOnReference;
		this.valuesToCheck = valuesToCheck;
	}

	public DecideOnValueBranch addBranch(IGeneratable... valuesToCheck)
	{
		var branch = new DecideOnValueBranch(valuesToCheck);
		branches.add(branch);
		return branch;
	}

	public DecideOnValueBranch onAllValues()
	{
		return allBranch;
	}

	public DecideOnValueBranch onAnyValue()
	{
		return anyBranch;
	}

	public DecideOnValueBranch onNoneValue()
	{
		return noneBranch;
	}

	@Override
	public void generateInto(CodeBuilder code)
	{
		code.append("DECIDE ON ").append(valuesToCheck.name()).append(" VALUE OF ").append(decideOnReference);

		code
			.lineBreak()
			.indent();

		for (var branch : branches)
		{
			branch.generateInto(code);
		}

		if (!anyBranch.bodyParts.isEmpty())
		{
			anyBranch.generateInto(code);
		}

		if (!allBranch.bodyParts.isEmpty())
		{
			allBranch.generateInto(code);
		}

		noneBranch.generateInto(code);

		code.unindent();
		code.append("END-DECIDE");
	}

	public static class DecideOnValueBranch extends GeneratableWithBody<DecideOnValueBranch> implements IGeneratableStatement
	{
		private final IGeneratable[] valuesToCheck;
		private final String specialBranchName;

		DecideOnValueBranch(IGeneratable[] valuesToCheck)
		{
			this.valuesToCheck = valuesToCheck;
			this.specialBranchName = null;
		}

		DecideOnValueBranch(String specialBranchName)
		{
			this.valuesToCheck = new IGeneratable[0];
			this.specialBranchName = specialBranchName;
		}

		@Override
		public void generateInto(CodeBuilder code)
		{
			if (specialBranchName != null)
			{
				code
					.append(specialBranchName)
					.append(" VALUE");
			}
			else
			{
				code
					.append("VALUE ")
					.append(String.join(", ", Arrays.stream(valuesToCheck).map(IGeneratable::generate).toList()));
			}

			generateBody(code);
		}
	}

	enum DecideOnValueCheck
	{
		FIRST,
		EVERY
	}
}
