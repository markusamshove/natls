package org.amshove.natls.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NaturalWorkspaceService implements WorkspaceService
{
	private static final Logger log = Logger.getAnonymousLogger();
	private NaturalLanguageService languageService;

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params)
	{
		var settings = (JsonObject) params.getSettings();
		var jsonObject = settings.getAsJsonObject("natls");
		var configuration = new Gson().fromJson(jsonObject, LSConfiguration.class);
		NaturalLanguageService.setConfiguration(configuration);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willRenameFiles(RenameFilesParams params)
	{
		return CompletableFuture.supplyAsync(() -> languageService.willRenameFiles(params.getFiles()));
	}

	@Override
	public void didCreateFiles(CreateFilesParams params)
	{
		for (var file : params.getFiles())
		{
			languageService.fileCreated(file.getUri());
		}
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params)
	{
		log.fine("didChangeWatchedFiles start");
		var changedModules = 0;
		for (var change : params.getChanges())
		{
			try
			{
				var filepath = LspUtil.uriToPath(change.getUri());

				var isNaturalModule = NaturalFileType.isNaturalFile(filepath);
				if (isNaturalModule)
				{
					changedModules++;
					handleWatchedFileEvent(filepath, change);
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Error during changed watched file changed (%s), skipping file %s".formatted(change.getType(), change.getUri()), e);
			}
		}

		if (changedModules > 0)
		{
			languageService.reparseOpenFiles();
		}

		log.fine(() -> "didChangeWatchedFiles end");
	}

	/**
	 * Handles events for watched modules. This should only be used for externally changed files.</br>
	 * Files that are changed or created by the client itself will be handled by the
	 * {@link org.eclipse.lsp4j.services.TextDocumentService} ({@link NaturalDocumentService}.</br>
	 * The job of this handler is to keep the project up to date.
	 */
	private void handleWatchedFileEvent(Path filepath, FileEvent change)
	{
		log.fine(() -> "Handling watched natural module change [%s]: %s".formatted(change.getType(), filepath));
		switch (change.getType())
		{
			case Created -> languageService.fileCreated(change.getUri());
			case Deleted -> languageService.fileDeleted(filepath);
			case Changed -> languageService.fileExternallyChanged(filepath);
		}
	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params)
	{
		return CompletableFutures.computeAsync(cancelChecker -> Either.forLeft(languageService.findWorkspaceSymbols(params.getQuery(), cancelChecker)));
	}

	public void setLanguageService(NaturalLanguageService languageService)
	{
		this.languageService = languageService;
	}
}
