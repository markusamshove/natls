package org.amshove.natlint.api;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;

public interface IAnalyzeContext
{
	INaturalModule getModule();

	void report(LinterDiagnostic diagnostic);

	/**
	 * Returns the .editorconfig setting for the given property matching the files path otherwise returns given
	 * defaultValue.
	 */
	String getConfiguration(NaturalFile forFile, String property, String defaultValue);

	/**
	 * Determines whether the file that is currently being analyzed has one of the specified types.
	 */
	default boolean isFiletype(NaturalFileType... types)
	{
		var fileType = getModule().file().getFiletype();
		for (var type : types)
		{
			if (fileType == type)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines whether the file that is currently being analyzed is a type that you would kind of "import" in your
	 * program via USING or INCLUDE.<br>
	 * This can be used to exclude for example PDAs if you want to have the raised diagnostics only be shown within the
	 * module that is including the PDA with USING.
	 */
	default boolean isIncludableFileType()
	{
		return isFiletype(NaturalFileType.COPYCODE, NaturalFileType.LDA, NaturalFileType.GDA, NaturalFileType.PDA, NaturalFileType.MAP);
	}
}
