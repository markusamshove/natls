package org.amshove.natparse.lexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class LexerForSystemVariablesShould extends AbstractLexerTest
{

	@ParameterizedTest
	@CsvFileSource(resources = {
		"../app-variables.csv",
		"../date-time-variables.csv",
		"../input-output-variables.csv",
		"../json-variables.csv",
		"../natural-environment-variables.csv",
		"../system-environment-variables.csv",
		"../xml-variables.csv",
	}, delimiter = '\t')
	void lexAllVariables(String source, String type, String modify) {
		var sourceSansParams = source.replaceAll("\\(\\w+\\)$", "");
		var tokenName = "SV_" + sourceSansParams.substring(1).replaceAll("-", "_");
		var expected = token(SyntaxKind.valueOf(tokenName), sourceSansParams);
		assertTokens(sourceSansParams, expected);
	}

	@Test
	void lexTimX()
	{
		assertTokens("*TIMX", token(SyntaxKind.SV_TIMX, "*TIMX"));
	}

	@Test
	void lexSystemVariablesAndFunctionsCaseInsensitive()
	{
		assertTokens("*timX", token(SyntaxKind.SV_TIMX, "*timX"));
	}

	@Test
	void lexDatE()
	{
		assertTokens("*DATE", token(SyntaxKind.SV_DATE, "*DATE"));
	}

	@Test
	void lexDat4E()
	{
		assertTokens("*DAT4E", token(SyntaxKind.SV_DAT4E, "*DAT4E"));
	}

	@Test
	void lexDatX()
	{
		assertTokens("*DATX", token(SyntaxKind.SV_DATX, "*DATX"));
	}

	@Test
	void lexDatN()
	{
		assertTokens("*DATN", token(SyntaxKind.SV_DATN, "*DATN"));
	}

	@Test
	void lexLanguage()
	{
		assertTokens("*LANGUAGE", token(SyntaxKind.SV_LANGUAGE, "*LANGUAGE"));
	}

	@Test
	void lexProgram()
	{
		assertTokens("*PROGRAM", token(SyntaxKind.SV_PROGRAM, "*PROGRAM"));
	}

	@Test
	void lexGroup()
	{
		assertTokens("*GROUP", token(SyntaxKind.SV_GROUP, "*GROUP"));
	}

	@Test
	void lexUser()
	{
		assertTokens("*USER", token(SyntaxKind.SV_USER, "*USER"));
	}

	@Test
	void lexUserName()
	{
		assertTokens("*USER-NAME", token(SyntaxKind.SV_USER_NAME, "*USER-NAME"));
	}

	@Test
	void lexLibraryId()
	{
		assertTokens("*LIBRARY-ID", token(SyntaxKind.SV_LIBRARY_ID, "*LIBRARY-ID"));
	}

	@Test
	void lexLineX()
	{
		assertTokens("*LINEX", token(SyntaxKind.SV_LINEX, "*LINEX"));
	}

	@Test
	void lexCurrentUnit()
	{
		assertTokens("*CURRENT-UNIT", token(SyntaxKind.SV_CURRENT_UNIT, "*CURRENT-UNIT"));
	}

	@Test
	void lexOcc()
	{
		assertTokens("*OCC", token(SyntaxKind.SV_OCC, "*OCC"));
	}

	@Test
	void lexOccurence()
	{
		assertTokens("*OCCURRENCE", token(SyntaxKind.SV_OCCURRENCE, "*OCCURRENCE"));
	}

	@Test
	void lexErrorNr()
	{
		assertTokens("*ERROR-NR", token(SyntaxKind.SV_ERROR_NR, "*ERROR-NR"));
	}

	@Test
	void lexErrorLine()
	{
		assertTokens("*ERROR-LINE", token(SyntaxKind.SV_ERROR_LINE, "*ERROR-LINE"));
	}

	@Test
	void lexErrorTa()
	{
		assertTokens("*ERROR-TA", token(SyntaxKind.SV_ERROR_TA, "*ERROR-TA"));
	}

	@Test
	void lexErrorNoNr()
	{
		assertTokens("*ERROR", token(SyntaxKind.SV_ERROR, "*ERROR"));
	}

	@Test
	void lexInitUser()
	{
		assertTokens("*INIT-USER", token(SyntaxKind.SV_INIT_USER, "*INIT-USER"));
	}

	@Test
	void lexCounter()
	{
		assertTokens("*COUNTER", token(SyntaxKind.SV_COUNTER, "*COUNTER"));
	}

	@Test
	void lexLine()
	{
		assertTokens("*LINE", token(SyntaxKind.SV_LINE, "*LINE"));
	}

	@Test
	void lexTrim()
	{
		assertTokens("*TRIM", token(SyntaxKind.TRIM, "*TRIM"));
	}

	@Test
	void lexMinval()
	{
		assertTokens("*MINVAL", token(SyntaxKind.MINVAL, "*MINVAL"));
	}

	@Test
	void lexMaxval()
	{
		assertTokens("*MAXVAL", token(SyntaxKind.MAXVAL, "*MAXVAL"));
	}

	@Test
	void lexCursor()
	{
		assertTokens("*CURSOR", token(SyntaxKind.SV_CURSOR, "*CURSOR"));
	}

	@Test
	void lexCursLine()
	{
		assertTokens("*CURS-LINE", token(SyntaxKind.SV_CURS_LINE, "*CURS-LINE"));
	}

	@Test
	void lexCursCol()
	{
		assertTokens("*CURS-COL", token(SyntaxKind.SV_CURS_COL, "*CURS-COL"));
	}

	@Test
	void lexParseCol()
	{
		assertTokens("*PARSE-COL", token(SyntaxKind.SV_PARSE_COL, "*PARSE-COL"));
	}

	@Test
	void lexParseRow()
	{
		assertTokens("*PARSE-ROW", token(SyntaxKind.SV_PARSE_ROW, "*PARSE-ROW"));
	}

	@Test
	void lexParseLevel()
	{
		assertTokens("*PARSE-LEVEL", token(SyntaxKind.SV_PARSE_LEVEL, "*PARSE-LEVEL"));
	}

	@Test
	void lexParseType()
	{
		assertTokens("*PARSE-TYPE", token(SyntaxKind.SV_PARSE_TYPE, "*PARSE-TYPE"));
	}

	@Test
	void lexParseIndex()
	{
		assertTokens("*PARSE-INDEX", token(SyntaxKind.SV_PARSE_INDEX, "*PARSE-INDEX"));
	}

	@Test
	void lexParseNamespace()
	{
		assertTokens("*PARSE-NAMESPACE-URI", token(SyntaxKind.SV_PARSE_NAMESPACE_URI, "*PARSE-NAMESPACE-URI"));
	}

	@Test
	void lexPfKey()
	{
		assertTokens("*PF-KEY", token(SyntaxKind.SV_PF_KEY, "*PF-KEY"));
	}

	@Test
	void lexBrowserIo()
	{
		assertTokens("*BROWSER-IO", token(SyntaxKind.SV_BROWSER_IO, "*BROWSER-IO"));
	}

	@Test
	void lexDevice()
	{
		assertTokens("*DEVICE", token(SyntaxKind.SV_DEVICE, "*DEVICE"));
	}

	@Test
	void lexDatD()
	{
		assertTokens("*DATD", token(SyntaxKind.SV_DATD, "*DATD"));
	}

	@Test
	void lexTimN()
	{
		assertTokens("*TIMN", token(SyntaxKind.SV_TIMN, "*TIMN"));
	}

	@Test
	void lexTimeOut()
	{
		assertTokens("*TIME-OUT", token(SyntaxKind.SV_TIME_OUT, "*TIME-OUT"));
	}

	@Test
	void lexOpSys()
	{
		assertTokens("*OPSYS", token(SyntaxKind.SV_OPSYS, "*OPSYS"));
	}

	@Test
	void lexTpSys()
	{
		assertTokens("*TPSYS", token(SyntaxKind.SV_TPSYS, "*TPSYS"));
	}

	@Test
	void lexApplicId()
	{
		assertTokens("*APPLIC-ID", token(SyntaxKind.SV_APPLIC_ID, "*APPLIC-ID"));
	}

	@Test
	void lexApplicName()
	{
		assertTokens("*APPLIC-NAME", token(SyntaxKind.SV_APPLIC_NAME, "*APPLIC-NAME"));
	}

	@Test
	void lexStartup()
	{
		assertTokens("*STARTUP", token(SyntaxKind.SV_STARTUP, "*STARTUP"));
	}

	@Test
	void lexSteplib()
	{
		assertTokens("*STEPLIB", token(SyntaxKind.SV_STEPLIB, "*STEPLIB"));
	}

	@Test
	void lexPageNumber()
	{
		assertTokens("*PAGE-NUMBER", token(SyntaxKind.SV_PAGE_NUMBER, "*PAGE-NUMBER"));
	}

	@Test
	void lexPid()
	{
		assertTokens("*PID", token(SyntaxKind.SV_PID, "*PID"));
	}

	@Test
	void lexWindowPs()
	{
		assertTokens("*WINDOW-PS", token(SyntaxKind.SV_WINDOW_PS, "*WINDOW-PS"));
	}

	@Test
	void lexWindowPos()
	{
		assertTokens("*WINDOW-POS", token(SyntaxKind.SV_WINDOW_POS, "*WINDOW-POS"));
	}

	@Test
	void lexWindowLs()
	{
		assertTokens("*WINDOW-LS", token(SyntaxKind.SV_WINDOW_LS, "*WINDOW-LS"));
	}

	@Test
	void lexInitId()
	{
		assertTokens("*INIT-ID", token(SyntaxKind.SV_INIT_ID, "*INIT-ID"));
	}

	@Test
	void lexCom()
	{
		assertTokens("*COM", token(SyntaxKind.SV_COM, "*COM"));
	}

	@Test
	void lexDat4D()
	{
		assertTokens("*DAT4D", token(SyntaxKind.SV_DAT4D, "*DAT4D"));
	}

	@Test
	void lexCursField()
	{
		assertTokens("*CURS-FIELD", token(SyntaxKind.SV_CURS_FIELD, "*CURS-FIELD"));
	}

	@Test
	void lexTimestmpx()
	{
		assertTokens("*TIMESTMPX", token(SyntaxKind.SV_TIMESTMPX, "*TIMESTMPX"));
	}

	@Test
	void lexTimestmp()
	{
		assertTokens("*TIMESTMP", token(SyntaxKind.SV_TIMESTMP, "*TIMESTMP"));
	}

	@Test
	void lexData()
	{
		assertTokens("*DATA", token(SyntaxKind.SV_DATA, "*DATA"));
	}

	@Test
	void lexLevel()
	{
		assertTokens("*LEVEL", token(SyntaxKind.SV_LEVEL, "*LEVEL"));
	}

	@Test
	void lexNumber()
	{
		assertTokens("*NUMBER", token(SyntaxKind.SV_NUMBER, "*NUMBER"));
	}

	@Test
	void lexLength()
	{
		assertTokens("*LENGTH", token(SyntaxKind.SV_LENGTH, "*LENGTH"));
	}

	@Test
	void lexTranslate()
	{
		assertTokens("*TRANSLATE", token(SyntaxKind.TRANSLATE, "*TRANSLATE"));
	}

	@Test
	void lexIsn()
	{
		assertTokens("*ISN", token(SyntaxKind.SV_ISN, "*ISN"));
	}

	@Test
	void lexDatG()
	{
		assertTokens("*DATG", token(SyntaxKind.SV_DATG, "*DATG"));
	}

	@Test
	void lexLineCount()
	{
		assertTokens("*LINE-COUNT", token(SyntaxKind.SV_LINE_COUNT, "*LINE-COUNT"));
	}

	@Test
	void lexLineSize()
	{
		assertTokens("*LINESIZE", token(SyntaxKind.SV_LINESIZE, "*LINESIZE"));
	}

	@Test
	void lexNetUser()
	{
		assertTokens("*NET-USER", token(SyntaxKind.SV_NET_USER, "*NET-USER"));
	}

	@Test
	void lexHostName()
	{
		assertTokens("*HOSTNAME", token(SyntaxKind.SV_HOSTNAME, "*HOSTNAME"));
	}

	@Test
	void lexMachineClass()
	{
		assertTokens("*MACHINE-CLASS", token(SyntaxKind.SV_MACHINE_CLASS, "*MACHINE-CLASS"));
	}

	@Test
	void lexPageSize()
	{
		assertTokens("*PAGESIZE", token(SyntaxKind.SV_PAGESIZE, "*PAGESIZE"));
	}

	@Test
	void lexDat4I()
	{
		assertTokens("*DAT4I", token(SyntaxKind.SV_DAT4I, "*DAT4I"));
	}

	@Test
	void lexDatI()
	{
		assertTokens("*DATI", token(SyntaxKind.SV_DATI, "*DATI"));
	}

	@Test
	void lexTimD()
	{
		assertTokens("*TIMD", token(SyntaxKind.SV_TIMD, "*TIMD"));
	}

	@Test
	void lexCpuTime()
	{
		assertTokens("*CPU-TIME", token(SyntaxKind.SV_CPU_TIME, "*CPU-TIME"));
	}

	@Test
	void lexEtid()
	{
		assertTokens("*ETID", token(SyntaxKind.SV_ETID, "*ETID"));
	}

	@Test
	void lexInitProgram()
	{
		assertTokens("*INIT-PROGRAM", token(SyntaxKind.SV_INIT_PROGRAM, "*INIT-PROGRAM"));
	}

	@Test
	void lexLbound()
	{
		assertTokens("*LBOUND", token(SyntaxKind.SV_LBOUND, "*LBOUND"));
	}

	@Test
	void lexUbound()
	{
		assertTokens("*UBOUND", token(SyntaxKind.SV_UBOUND, "*UBOUND"));
	}

	@Test
	void lexServerType()
	{
		assertTokens("*SERVER-TYPE", token(SyntaxKind.SV_SERVER_TYPE, "*SERVER-TYPE"));
	}

	@Test
	void lexDat4J()
	{
		assertTokens("*DAT4J", token(SyntaxKind.SV_DAT4J, "*DAT4J"));
	}

	@Test
	void lexDatJ()
	{
		assertTokens("*DATJ", token(SyntaxKind.SV_DATJ, "*DATJ"));
	}

	@Test
	void lexDat4U()
	{
		assertTokens("*DAT4U", token(SyntaxKind.SV_DAT4U, "*DAT4U"));
	}

	@Test
	void lexDatU()
	{
		assertTokens("*DATU", token(SyntaxKind.SV_DATU, "*DATU"));
	}

	@Test
	void lexDatVS()
	{
		assertTokens("*DATVS", token(SyntaxKind.SV_DATVS, "*DATVS"));
	}

	@Test
	void lexDatV()
	{
		assertTokens("*DATV", token(SyntaxKind.SV_DATV, "*DATV"));
	}

	@Test
	void lexTime()
	{
		assertTokens("*TIME", token(SyntaxKind.SV_TIME, "*TIME"));
	}

	@Test
	void lexSubroutine()
	{
		assertTokens("*SUBROUTINE", token(SyntaxKind.SV_SUBROUTINE, "*SUBROUTINE"));
	}

	@Test
	void lexOutAttribute()
	{
		assertTokens("*OUT", token(SyntaxKind.OUT_ATTRIBUTE, "*OUT"));
	}

	@Test
	void lexInAttribute()
	{
		assertTokens("*IN", token(SyntaxKind.IN_ATTRIBUTE, "*IN"));
	}

	@Test
	void lexOutInAttribute()
	{
		assertTokens("*OUTIN", token(SyntaxKind.OUTIN_ATTRIBUTE, "*OUTIN"));
	}

	@Test
	void lexConvId()
	{
		assertTokens("*CONVID", token(SyntaxKind.SV_CONVID, "*CONVID"));
	}
}
