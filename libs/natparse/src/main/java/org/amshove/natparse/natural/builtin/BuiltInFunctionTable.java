package org.amshove.natparse.natural.builtin;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.DataType;

import java.util.Arrays;
import java.util.Map;

import static org.amshove.natparse.natural.DataFormat.*;

public class BuiltInFunctionTable
{
	private static final Map<SyntaxKind, IBuiltinFunctionDefinition> TABLE;

	static
	{
		TABLE = Map.ofEntries(
			unmodifiableVariable(SyntaxKind.SV_APPLIC_ID, "Returns the ID of the current library", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_APPLIC_NAME, """
				If Natural Security is installed, the variable contains the
				name of library to which the user is logged on.

				If the user is logged on via a special link and nothing is stated to the contrary,
				it contains the link name instead.

				If Natural Security is not installed, variable contains the name 'SYSTEM'.
				""", ALPHANUMERIC, 32.0),
			unmodifiableVariable(SyntaxKind.SV_INIT_ID, "Returns the ID of the device that Natural invoked", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_TIME, "Returns the current time of the day as A10 in format HH:II:SS.T", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.SV_TIMX, "Returns the current time of the day as builtin time format", TIME, 0.0),
			unmodifiableVariable(SyntaxKind.SV_TIMESTMP, "Returns the machine-internal clock value", BINARY, 8.0),
			unmodifiableVariable(SyntaxKind.SV_TIMESTMPX, "Returns the extended machine-internal clock value", BINARY, 16.0),
			unmodifiableVariable(SyntaxKind.SV_TIME_OUT, "Contains the number of seconds remaining before the current transaction will be timed out (Natural Security only).", NUMERIC, 5.0),
			unmodifiableVariable(SyntaxKind.SV_TIMN, "Returns the current time of the day as numeric format", NUMERIC, 7.0),
			function(
				SyntaxKind.SV_TIMD, """
					Returns the time passed since the `SET TIME` statement which is referred to by the first parameter.

					The format returned is: `HHISST` (hour hour minute second second tenth-second).

					Example:

					```
					T1. SET TIME
					PERFORM EXPENSIVE-COMPUTATION
					WRITE 'Computation took' *TIMD(T1.)
					```
					""",
				NUMERIC,
				7.0,
				labelParameter(true)
			),
			unmodifiableVariable(SyntaxKind.SV_DATD, "Returns the current date in the format `DD.MM.YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DATE, "Returns the current date in the format `DD/MM/YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DAT4E, "Returns the current date in the format `DD/MM/YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.SV_DATG, "Returns the current date in gregorian format `DDmonthnameYYYY`", ALPHANUMERIC, 15.0),
			unmodifiableVariable(SyntaxKind.SV_DAT4D, "Returns the current date in the format `DD.MM.YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.SV_DATI, "Returns the current date in the format `YY-MM-DD`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DAT4I, "Returns the current date in the format `YYYY-MM-DD`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.SV_DATJ, "Returns the current date in the format `YYJJJ` (Julian date)", ALPHANUMERIC, 5.0),
			unmodifiableVariable(SyntaxKind.SV_DAT4J, "Returns the current date in the format `YYYYJJJ` (Julian date)", ALPHANUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.SV_DATX, "Returns the current date as internal date for mat", DATE, 0.0),
			unmodifiableVariable(SyntaxKind.SV_DATN, "Returns the current date in the format `YYYYMMDD`", NUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DATU, "Returns the current date in the format `MM/DD/YY`", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_DAT4U, "Returns the current date in the format `MM/DD/YYYY`", ALPHANUMERIC, 10.0),
			unmodifiableVariable(SyntaxKind.SV_DATV, "Returns the current date in the format `DD-MON-YY`, where MON is the name of the month", ALPHANUMERIC, 11.0),
			unmodifiableVariable(SyntaxKind.SV_DATVS, "Returns the current date in the format `DDMONYYYY`, where MON is the name of the month", ALPHANUMERIC, 9.0),
			unmodifiableVariable(SyntaxKind.SV_LINESIZE, "Returns the physical line size of the I/O device Natural was started with. For vertical look at `*PAGESIZE`", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.SV_PAGESIZE, "Returns the physical page size of the I/O device Natural was started with. For horizontal look at `*LINESIZE`", NUMERIC, 7.0),
			unmodifiableVariable(SyntaxKind.SV_NET_USER, """
				Contains the complete user ID of an authenticated client request.
				The ID consists of the domain name and the actual user ID.

				The default value is the value of *USER.
				When a NaturalX server receives an authenticated request,
				the user ID of this request is passed to the server and placed
				into *NET-USER. After the request, *NET-USER is reset to the value of *USER.
				""", ALPHANUMERIC, 253.0),
			unmodifiableVariable(SyntaxKind.SV_PARM_USER, "Returns the name of the parameter file in use", ALPHANUMERIC, 253),
			unmodifiableVariable(SyntaxKind.SV_NATVERS, """
				Returns the Natural version e.g. 06.02.01

				See also : `*PATCH-LEVEL`
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_PATCH_LEVEL, """
				Returns the current cumulative fix number

				See also : `*NATVERS`
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_HARDWARE, "Returns the name of the hardware platform Natural is running on", ALPHANUMERIC, 16),
			unmodifiableVariable(SyntaxKind.SV_HOSTNAME, "Name of the machine Natural runs on", ALPHANUMERIC, 64.0),
			unmodifiableVariable(SyntaxKind.SV_LOCALE, "Returns the language and country of the current locale", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_MACHINE_CLASS, """
				Returns the name of the machine class Natural was started on

				Possible return values:

				- `MAINFRAME`
				- `PC`
				- `UNIX`
				- `VMS`
				""", ALPHANUMERIC, 16.0),
			modifiableVariable(SyntaxKind.SV_LANGUAGE, "Returns the language code, e.g. 1 for english, 2 for german etc.", INTEGER, 1.0),
			modifiableVariable(SyntaxKind.SV_STARTUP, "Get or set the name of the program which will be executed when Natural would show the command prompt", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_STEPLIB, "Returns the name of the current steplib", ALPHANUMERIC, 8.0),
			modifiableVariable(SyntaxKind.SV_PAGE_NUMBER, "Get or set the current page number of an report", PACKED, 5.0),
			unmodifiableVariable(SyntaxKind.SV_LINE_COUNT, "Returns the line number of the current pages's line.", PACKED, 5.0),
			unmodifiableVariable(SyntaxKind.SV_WINDOW_LS, "Returns the line size of the logical window (without the frame)", NUMERIC, 3.0),
			unmodifiableVariable(SyntaxKind.SV_WINDOW_PS, "Returns the page size of the logical window (without the frame)", NUMERIC, 3.0),
			unmodifiableVariable(SyntaxKind.SV_WINDOW_POS, "Returns the position of the upper left corner of the window (from `DEFINE WINDOW`)", NUMERIC, 6.0),
			unmodifiableVariable(SyntaxKind.SV_LIBRARY_ID, "Returns the ID the the current library. This returns the same as *APPLIC-ID", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.TRANSLATE, """
				Converts the characters passed as first argument into either `LOWER` or `UPPER` case.

				Accepts an operand of type `A`, `B` or `U`.

				Usage:

				```
				#UPPER := *TRANSLATE(#VAR2, UPPER)
				#LOWER := *TRANSLATE(#VAR2, LOWER)
				```
				""", ALPHANUMERIC, 0),
			modifiableVariable(SyntaxKind.SV_NUMBER, "Get or set the number of record a FIND or HISTOGRAM statement. Uses the innermost statement if no label identifier is passed.", PACKED, 10),
			modifiableVariable(SyntaxKind.SV_LENGTH, "This system variable returns the currently used length of a field defined as dynamic variable in terms of code units; for A and B format the size of one code unit is 1 byte and for U format the size of one code unit is 2 bytes (UTF-16). *LENGTH(field) applies to dynamic variables only.", INTEGER, 4),
			unmodifiableVariable(SyntaxKind.SV_SUBROUTINE, "Returns the name of the current external subroutine. Content will aways be upper case.", ALPHANUMERIC, 32),
			// TODO: Support HANDLE OF OBJECT
			unmodifiableVariable(SyntaxKind.SV_THIS_OBJECT, "", NONE, 0),
			unmodifiableVariable(SyntaxKind.SV_TYPE, """
				Contains the type of Natural object currently executing.

				- PROGRAM
				- FUNCTION
				- SUBPROGRAM
				- SUBROUTINE
				- HELPROUTINE
				- MAP
				- ADAPTER
				""", ALPHANUMERIC, 32),
			unmodifiableVariable(SyntaxKind.SV_LINEX, """
				Returns the line number of the invocation of this variable.
				When this variable is used within copycodes, it contains the line numbers of all includes leading to this variable.

				If this variable is not used within a copy code, it returns the same value as `*LINE`.

				Example:

				```
				0100 INCLUDE FIRSTCC
				  0200 INCLUDE SCNDCC
				    0300 PRINT *LINEX
				```

				In this case the variable returns `0100/0200/0300`.
				""".stripIndent(), ALPHANUMERIC, 100),
			unmodifiableVariable(SyntaxKind.SV_LOAD_LIBRARY_ID, """
				Returns the library ID where the current executing object was loaded.
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_CURRENT_UNIT, "Returns the name of the current executing unit.", ALPHANUMERIC, 32.0),
			modifiableVariable(SyntaxKind.SV_ERROR, "Short form of *ERROR-NR (discouraged)", NUMERIC, 7.0),
			modifiableVariable(SyntaxKind.SV_ERROR_NR, """
				Get or set the current error number.

				This contains the number of the ERROR that triggered the `ON ERROR` block.

				If this is manually set, it terminates execution to the next `ON ERROR` block within the stack.

				**Not** modifiable within an `ON ERROR` block.

				Value can only range from 0 to 9999.
				""", NUMERIC, 7.0),
			modifiableVariable(SyntaxKind.SV_ERROR_TA, "Get or set the name of the error transaction program which receives control if an error occurs", ALPHANUMERIC, 8.0),
			unmodifiableVariable(SyntaxKind.SV_LINE, """
				Returns the number of the line where this variable is used.

				If this is inside a copycode, it will return the number within the copycode, not the `INCLUDE`.
				If you want to get all line numbers, including the `INCLUDE`s, use `*LINEX` instead.
				""", INTEGER, 4.0),
			unmodifiableVariable(SyntaxKind.SV_ERROR_LINE, "Returns the line of the statement that raised an error", NUMERIC, 4),
			unmodifiableVariable(SyntaxKind.CURSOR, """
				Position of cursor on input screen at time ENTER or function key is pressed.

				Note: It is recommended that the system variables *CURS-LINE and *CURS-COL be used instead of the *CURSOR command.
				""", NUMERIC, 6),
			unmodifiableVariable(SyntaxKind.SV_CURS_FIELD, """
				Returns the identification of the field in which the cursor is positioned" +
				Can only be used together withe the `POS` function.
				""", INTEGER, 4),
			modifiableVariable(SyntaxKind.SV_CURS_LINE, """
				Get or set the number of the line where the cursor is positioned.
				To get the cursor column, use `*CURS-COL`.

				When setting the value, it has to be > 0.

				`*CURS_LINE` may return the following special values:

				 - 0: On the top **or** bottom line of a window
				- -1: On the message line
				- -2: On the info/statistics line
				- -3: On the upper function-key line
				- -4: On the lower function-key line

				""", PACKED, 3),
			modifiableVariable(SyntaxKind.SV_CURS_COL, """
				Get or set the number of the column where the current cursor is located
				""", PACKED, 3),
			unmodifiableVariable(SyntaxKind.SV_CURSOR, "Returns the position of the cursor on the input screen at the time ENTER or PF is pressed", NUMERIC, 6),
			function(SyntaxKind.SV_PARSE_COL, "Column where the parser is currently working at", INTEGER, 4, labelParameter(false)),
			function(SyntaxKind.SV_PARSE_ROW, "Row where the parser is currently working at", INTEGER, 4, labelParameter(false)),
			function(SyntaxKind.SV_PARSE_LEVEL, "Contains the current nesting level when parsing XML or JSON", INTEGER, 4, labelParameter(false)),
			function(SyntaxKind.SV_PARSE_TYPE, "Contains the current syntactic type of the XML or JSON structure", ALPHANUMERIC, 1, labelParameter(false)),
			function(SyntaxKind.SV_PARSE_INDEX, "Contains the element index within the JSON array being parsed", INTEGER, 4, labelParameter(false)),
			function(SyntaxKind.SV_PARSE_NAMESPACE_URI, "Contains the element index within the JSON array being parsed", ALPHANUMERIC, 255, labelParameter(false)),
			unmodifiableVariable(SyntaxKind.SV_BROWSER_IO, """
					Returns whether the application is running in a web browser via WebIO or Natural for Ajax.

					It can contain one of the following values:

					empty     : The application is not running in a browser
					`WEB`     : The application is running in WebIO
					`RICHGUI` : The application is running in Natural for Ajax (e.g. HA)
					""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_DEVICE, """
				Returns the type or mode of the device from which Natural was started.

				It can contain one of the following values:

				`BATCH`: Natural was started with `BATCH` parameter
				`VIDEO`: With a screen (PC screen, VT, X-Term, ...)
				`TTY`: With a teletype or other start/stop device
				`PC`: Natural connection with profile parameter `PC=ON` or terminal command `%+`
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_CPU_TIME, """
				Returns the CPU time currently used by the Natural process in units of 10 ms.
				""", INTEGER, 4),
			unmodifiableVariable(SyntaxKind.SV_ETID, """
				Returns the current identifier of transaction data for Adabas.

				The default value is one of the following:

				- the value of the Natural profile parameter ETID,
				- the value from the security profile of the currently active user (applies only under Natural Security).
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_INIT_PROGRAM, """
				Return the name of program (transaction) currently executing as Natural.
				""", ALPHANUMERIC, 8),
			function(SyntaxKind.SV_LBOUND, """
				Returns the current lower boundary (index value) of an array for the specified dimension(s) (1, 2 or 3) or for all dimensions (asterisk (*) notation).
				""", INTEGER, 4),
			function(SyntaxKind.SV_UBOUND, """
				Returns the current upper boundary (index value) of an array for the specified dimension(s) (1, 2 or 3) or for all dimensions (asterisk (*) notation).
				""", INTEGER, 4),
			unmodifiableVariable(SyntaxKind.SV_SERVER_TYPE, """
				This system variable indicates the server type Natural has been started as. It can contain one of the following values:

				- DB2-SP	Natural DB2 Stored Procedures server
				- DEVELOP	Natural development server
				- RPC		Natural RPC server
				- WEBIO		Natural Web I/O Interface server

				If Natural is not started as a server, *SERVER-TYPE is set to blanks.
				""", ALPHANUMERIC, 32),
			unmodifiableVariable(SyntaxKind.SV_TP, "Returns the name of the TP subsystem under which Natural is running", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_TPSYS, """
				Returns the Natural name of the TP monitor or environment.

				Will return `NONE` on Windows, UNIX and OpenVMS platforms.

				Can contain one of the following values:

				```
				NONE
				AIM/DC
				CICS
				COMPLETE
				IMS/DC
				OS/400
				SERVSTUB (Natural Development Server)
				TIAM
				TSO
				TSS
				UTM
				```
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_OPSYS, """
				Returns the Natural name of the operating system.

				More in depth information can be retrieved with a combination of `MACHINE-CLASS`, `*HARDWARE` and `*OS`.
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_OS, "Returns the name of the operating system Natural is running under", ALPHANUMERIC, 32),
			unmodifiableVariable(SyntaxKind.SV_OSVERS, ",Returns the version number of the operating system Natural is running under", ALPHANUMERIC, 16),
			unmodifiableVariable(SyntaxKind.SV_PROGRAM, "Returns the name of the current Natural object", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_REINPUT_TYPE, """
				Returns whether the app is in a state that allows `REINPUT` or `PROCESS PAGE UPDATE`.

				- (blanks)	Cannot perform `REINPUT` or `PROCESS PAGE UPDATE`
				- REINPUT	Can perform `REINPUT`
				- UPDATE	Can perform `PROCESS PAGE UPDATE`
				""", ALPHANUMERIC, 16),
			unmodifiableVariable(SyntaxKind.SV_ROWCOUNT, "Returns the number of rows affected by the last SQL statement", ALPHANUMERIC, 16),
			unmodifiableVariable(SyntaxKind.SV_GROUP, "Returns Group ID or user's own ID taken from Natural Security logon", ALPHANUMERIC, 8),
			modifiableVariable(SyntaxKind.SV_HARDCOPY, "Returns the name of the hardcopy device that would be used for %H command", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_USER_NAME, "Returns the user name of the current user, as taken from Natural Security", ALPHANUMERIC, 32),
			unmodifiableVariable(SyntaxKind.SV_CODEPAGE, """
				Returns the IANA name of the current default codepage.

				Used for conversions to and from Unicode. Set by profile parameter `CP`.
				""", ALPHANUMERIC, 64),
			unmodifiableVariable(SyntaxKind.SV_UI, "Returns the type of user interface being used (`CHARACTER` | `GUI`)", ALPHANUMERIC, 16),
			unmodifiableVariable(SyntaxKind.SV_USER, "Returns the user id of the current user, as taken from Natural Security", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_INIT_USER, """
				Returns the value of the profile parameter `USER`.

				If the profile parameter is not specified it will return the UNIX uid of the current user.
				""", ALPHANUMERIC, 8),
			unmodifiableVariable(SyntaxKind.SV_PF_KEY, """
				Returns the name of the sensitive key that was pressed last.

				If no sensitive key was pressed, will return `ENTR`.

				Possible return values:

				- `PA1 to PA3`: Program attention keys 1 to 3
				- `PF1 to PF48`: Program function keys 1 to 48
				- `ENTR`: The `ENTER` or `RETURN` key.
				- `CLR`: The `CLEAR` key
				- `PEN`: Light pen
				- `PGDN`: `PAGE DOWN` key
				- `PGUP`: `PAGE UP` key

				Notes:

				- If a page break occurs, the value changes to `ENTR`.
				""", ALPHANUMERIC, 4),
			unmodifiableVariable(SyntaxKind.SV_PF_NAME, """
				Returns the name of the function key pressed last.

				This allows you to process keys based on their NAMED value.
				""", ALPHANUMERIC, 10),
			unmodifiableVariable(SyntaxKind.SV_PID, "Returns the current process ID as a string", ALPHANUMERIC, 32),
			function(SyntaxKind.SV_ISN, """
				Gets or sets the internal sequence number of the current Adabas record initiated by `FIND` or `READ`.

				Usage:
				```natural
				#ISN := *ISN
				#ISN := *ISN(R1.)
				```
				""", PACKED, 10, labelParameter(false)),
			unmodifiableVariable(SyntaxKind.SV_SCREEN_IO, "Indicates if screen I/O is currently possible", LOGIC, 1),
			function(SyntaxKind.SV_COUNTER, """
				Returns the number of times a processing loop initiated by `FIND`, `READ`, `HISTOGRAM` or `PARSE` has been entered.

				If a record is rejected through a `WHERE`-clause, `*COUNTER` is not incremented.
				If a record is rejected through `ACCEPT` or `REJECT`, `*COUNTER` is incremented.


				Usage:
				```natural
				#I := *COUNTER
				#I := *COUNTER(RD.)
				```
				""", PACKED, 10, labelParameter(false)),
			function(
				SyntaxKind.SV_OCCURRENCE, "See `*OCC`", INTEGER, 4,
				new BuiltInFunctionParameter("array", new DataType(DataFormat.NONE, 1), true),
				new BuiltInFunctionParameter("dimension", new DataType(DataFormat.NONE, 1), false)
			),
			function(
				SyntaxKind.SV_OCC, """
					Returns the current length of an array.

					The optional `dimension` parameter handles for which dimension the length is returned. Defaults to 1 if not specified.

					Possible value of `dimension`:

					- `1`: One-dimensional array (**default**)
					- `2`: Two-dimensional array
					- `3`: Three-dimensional array
					- `*`: All dimensions defined for the corresponding array apply

					Example:

					```natural
					DEFINE DATA LOCAL
					1 #LENGTH (I4)
					1 #ARRAY (A10/1:*,1:*)
					1 #DIMENSIONS (I4/1:3)
					END-DEFINE

					EXPAND ARRAY #ARRAY TO (1:10,1:20)
					#LENGTH := *OCC(#ARRAY) /* #LENGTH = 10, first dimension
					#LENGTH := *OCC(#ARRAY, 1) /* #LENGTH = 10, first dimension
					#LENGTH := *OCC(#ARRAY, 2) /* #LENGTH = 20, second dimension
					#DIMENSIONS(1:2) := *OCC(#ARRAY, *) /* #DIMENSIONS(1) = 10; #DIMENSIONS(2) = 20
					```
					""", INTEGER, 4,
				new BuiltInFunctionParameter("array", new DataType(DataFormat.NONE, 1), true),
				new BuiltInFunctionParameter("dimension", new DataType(DataFormat.NONE, 1), false)
			),
			unmodifiableVariable(SyntaxKind.SV_PAGE_EVENT, """
				Returns the name of the current event delivered from Natural for Ajax

				Used for rich GUI programming with `PROCESS PAGE`.
				""", UNICODE, DataType.DYNAMIC_LENGTH),
			unmodifiableVariable(SyntaxKind.SV_PAGE_LEVEL, """
				Returns the level of `PROCESS PAGE MODAL` statement blocks

				Returns 0 if no modal is active. No output to Report 0 is possible if value is > 0
				""", INTEGER, 4),
			function(
				SyntaxKind.MINVAL, """
					Returns the minimal value of all given operand values.

					The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.

					If an array is passed, this function returns the minimum value of all arrays values.

					If a binary or alphanumeric value is passed, this function returns the minimum length of the operands.
					""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(
				SyntaxKind.MAXVAL, """
					Returns the maximum value of all given operand values.

					The result type can be optionally specified with `(IR=`, e.g. `(IR=F8)`. Otherwise the biggest data type of the operands is chosen.

					If an array is passed, this function returns the maximum value of all arrays values.

					If a binary or alphanumeric value is passed, this function returns the maximum length of the operands.
					""", FLOAT, 8,
				new BuiltInFunctionParameter("operand1", new DataType(NONE, 1), true),
				new BuiltInFunctionParameter("operand2", new DataType(NONE, 1), false),
				new BuiltInFunctionParameter("operand3", new DataType(NONE, 1), false)
			),
			function(
				SyntaxKind.TRIM, """
					Remove all leading and trailing whitespace from an alphanumeric or binary string.

					The content of the passed variable is not modified.

					`LEADING` or `TRIALING` can be specified if only one of them should be trimmed.

					Example:

					```natural
					#NO-LEADING-TRAILING := *TRIM(#ALPHA)
					#NO-LEADING := *TRIM(#ALPHA, LEADING)
					#NO-TRAILING := *TRIM(#ALPHA, TRAILING)
					""", ALPHANUMERIC, DataType.DYNAMIC_LENGTH,
				new BuiltInFunctionParameter("operand", new DataType(ALPHANUMERIC, DataType.DYNAMIC_LENGTH), true)
			),
			modifiableVariable(SyntaxKind.SV_COM, """
				Get or set the value of the communication area which can be used to process data from outside a screen window.

				When a window is active, no data can be entered outside the window.
				If a map contains *COM as modifiable field, it will be available for the user to enter data even though a window is currently active on the screen.
				""", ALPHANUMERIC, 128),
			unmodifiableVariable(SyntaxKind.SV_DATA, """
				Returns the number of elements in the Natural stack available for next `INPUT`.

				`0` is returned if the stack is empty.
				`-1` is returned if the next value in the stack is a command or name of a transaction
				""", NUMERIC, 3),
			unmodifiableVariable(SyntaxKind.SV_EDITOR, """
				Returns whether the Natural editors are enabled on the server

				`TRUE` if they are enabled
				`FALSE` if they are not
				""", LOGIC, 1),
			unmodifiableVariable(SyntaxKind.SV_LEVEL, """
				Returns the level number of the current program, dialog, ... which is currently active.

				Level 1 is the main program.
				""", NUMERIC, 2),
			modifiableVariable(SyntaxKind.SV_CONVID, """
				Contains the ID of the current RPC conversion. Automatically set by OPEN CONVERSATION.

				*CONVID can be modified to switch between multiple open conversations.
				""", INTEGER, 4)
		);
	}

	public static IBuiltinFunctionDefinition getDefinition(SyntaxKind kind)
	{
		return TABLE.get(kind);
	}

	private static BuiltInFunctionParameter labelParameter(boolean mandatory)
	{
		return new BuiltInFunctionParameter("label", new DataType(DataFormat.NONE, 1), mandatory);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> unmodifiableVariable(SyntaxKind kind, String documentation, DataFormat format, double length)
	{
		return variable(kind, documentation, format, length, false);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> modifiableVariable(SyntaxKind kind, String documentation, DataFormat format, double length)
	{
		return variable(kind, documentation, format, length, true);
	}

	private static Map.Entry<SyntaxKind, SystemVariableDefinition> variable(SyntaxKind kind, String documentation, DataFormat format, double length, boolean modifiable)
	{
		return Map.entry(kind, new SystemVariableDefinition(kind.toString(), documentation, new DataType(format, length), modifiable));
	}

	private static Map.Entry<SyntaxKind, SystemFunctionDefinition> function(SyntaxKind kind, String documentation, DataFormat format, double length, BuiltInFunctionParameter... parameter)
	{
		return Map.entry(kind, new SystemFunctionDefinition(kind.toString(), documentation, new DataType(format, length), Arrays.asList(parameter)));
	}

	private static String getName(SyntaxKind kind)
	{
		return kind.toString().replace("_", "-").replace("SV-", "");
	}

	private BuiltInFunctionTable()
	{}
}
