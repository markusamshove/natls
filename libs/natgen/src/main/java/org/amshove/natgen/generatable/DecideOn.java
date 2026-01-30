package org.amshove.natgen.generatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecideOn implements IGeneratable
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
	public String generate()
	{
		var code = new StringBuilder();

		code.append("DECIDE ON ").append(valuesToCheck.name()).append(" VALUE OF ").append(decideOnReference.generate())
			.append(System.lineSeparator());

		branches.forEach(branch -> code.append("  ").append(branch.generate()).append(System.lineSeparator()));

		if (!anyBranch.bodyParts.isEmpty())
		{
			code.append("  ")
				.append(anyBranch.generate())
				.append(System.lineSeparator());
		}

		if (!allBranch.bodyParts.isEmpty())
		{
			code.append("  ")
				.append(allBranch.generate())
				.append(System.lineSeparator());
		}

		code.append("  ")
			.append(noneBranch.generate())
			.append(System.lineSeparator());

		code.append("END-DECIDE");

		return code.toString();
	}

	public static class DecideOnValueBranch extends GeneratableWithBody<DecideOnValueBranch> implements IGeneratable
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
		public String generate()
		{
			if (specialBranchName != null)
			{
				return "%s VALUE%n%s".formatted(
					specialBranchName,
					bodyWithIndentation("    ")
				);
			}

			return "VALUE %s%n%s".formatted(
				String.join(", ", Arrays.stream(valuesToCheck).map(IGeneratable::generate).toList()),
				bodyWithIndentation("    ")
			);
		}
	}

	enum DecideOnValueCheck
	{
		FIRST,
		EVERY
	}
}
