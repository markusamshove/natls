package org.amshove.natgen.generatable;

import org.amshove.natgen.CodeBuilder;
import org.amshove.natgen.generatable.conditions.IConditional;

import java.util.ArrayList;
import java.util.List;

public class If extends GeneratableWithBody<If> implements IGeneratableStatement
{
	private final List<IConditional> conditions = new ArrayList<>();
	private final List<ElseBranch> branches = new ArrayList<>();

	public If(IConditional firstCondition)
	{
		conditions.add(firstCondition);
	}

	public ElseBranch _else()
	{
		var branch = new ElseBranch();
		branches.add(branch);
		return branch;
	}

	public ElseBranch elseIf(IConditional conditional)
	{
		var branch = new ElseBranch();
		branch.conditionals.add(conditional);
		branches.add(branch);
		return branch;
	}

	@Override
	public void generateInto(CodeBuilder builder)
	{
		builder.append("IF ");

		for (var condition : conditions)
		{
			builder.append(condition);
		}

		generateBody(builder);

		for (var branch : branches)
		{
			branch.generateInto(builder);
		}

		builder.append("END-IF");
	}

	public static class ElseBranch extends GeneratableWithBody<ElseBranch> implements IGeneratableStatement
	{
		private final List<IConditional> conditionals = new ArrayList<>();

		@Override
		public void generateInto(CodeBuilder builder)
		{
			builder.append("ELSE");

			if (!conditionals.isEmpty())
			{
				builder.spaceOrBreak().append("IF ");
				for (var conditional : conditionals)
				{
					builder.append(conditional);
				}
			}

			generateBody(builder);
		}
	}
}
