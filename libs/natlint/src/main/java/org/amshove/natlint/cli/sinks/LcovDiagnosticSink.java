package org.amshove.natlint.cli.sinks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

/**
 * This sink emits only LcovDiagnostic items as lcov format.
 */
public class LcovDiagnosticSink implements IDiagnosticSink {

	Path filePath;
	CharSink outSink;

	public LcovDiagnosticSink(Path filePath) {
		this.filePath = filePath;
		try {
			if (filePath.toFile().exists()) {
				java.nio.file.Files.delete(filePath);
			}

			this.outSink = Files.asCharSink(filePath.toFile(), StandardCharsets.UTF_8, FileWriteMode.APPEND);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String sfRecord(IPosition diagnostic) {
		return String.format("SF:%s\n", filePath.getParent().relativize(diagnostic.filePath()));
	}

	private String daRecord(Integer line) {
		return String.format("DA:%s,0%n", line + 1);
	}

	private static final String END_RECORD = "end_of_record\n";

	@Override
	public synchronized void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics) {

		var diagnosticIterator = diagnostics.iterator();

		if (!diagnosticIterator.hasNext()) {
			return;
		}

		try (var out = outSink.openBufferedStream()) {
			// peel off first diagnostic
			IPosition currentPosition = diagnosticIterator.next();
			out.write(sfRecord(currentPosition));

			// Dedup the repeated hits
			SortedSet<Integer> diagnosticLines = new TreeSet<>();

			diagnosticLines.add(currentPosition.line());

			while (diagnosticIterator.hasNext()) {
				var nextPosition = diagnosticIterator.next();
				if (!currentPosition.isSameFileAs(nextPosition)) {
					for (var line : diagnosticLines) {
						out.write(daRecord(line));
					}
					out.write(END_RECORD);
					out.write(sfRecord(nextPosition));
					diagnosticLines = new TreeSet<>();
					currentPosition = nextPosition;
				}
				diagnosticLines.add(nextPosition.line());
			}

			for (var line : diagnosticLines) {
				out.write(daRecord(line));
			}
			out.write(END_RECORD);
		} catch (IOException ioex) {
			throw new RuntimeException("Error writing LCOV", ioex);
		}
	}
}
