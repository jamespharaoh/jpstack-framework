package wbs.framework.logging;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalDo;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class TaskLogger
	implements TaskLogEvent {

	// state

	private final
	Optional <TaskLogger> parentOptional;

	private final
	LogTarget logTarget;

	private final
	String staticContext;

	private final
	String dynamicContext;

	private final
	long nesting;

	private final
	Boolean debugEnabled;

	LogSeverity severity =
		LogSeverity.trace;

	long errorCount;
	long warningCount;
	long noticeCount;
	long debugCount;

	boolean addedToParent;

	String firstError;
	String lastError = "Aborting";

	List <String> errorMessages =
		new ArrayList<> ();

	List <TaskLogEvent> events =
		new ArrayList<> ();

	// constructors

	public
	TaskLogger (
			@NonNull Optional <TaskLogger> parentOptional,
			@NonNull LogTarget logTarget,
			@NonNull String staticContext,
			@NonNull String dynamicContext,
			@NonNull Optional <Boolean> debugEnabled) {

		this.parentOptional =
			parentOptional;

		this.nesting =
			optionalMapRequiredOrDefault (
				parent ->
					parent.nesting + 1l,
				parentOptional,
				0l);

		this.logTarget =
			logTarget;

		this.staticContext =
			staticContext;

		this.dynamicContext =
			dynamicContext;

		if (
			optionalIsPresent (
				debugEnabled)
		) {

			this.debugEnabled =
				optionalGetRequired (
					debugEnabled);

		} else {

			this.debugEnabled =
				optionalMapRequiredOrDefault (
					TaskLogger::debugEnabled,
					parentOptional,
					true);

		}

		if (this.debugEnabled) {
			addToParent ();
		}

	}

	// accessors

	public
	TaskLogger findRoot () {

		TaskLogger currentTaskLogger =
			this;

		while (
			optionalIsPresent (
				currentTaskLogger.parentOptional)
		) {

			currentTaskLogger =
				optionalGetRequired (
					currentTaskLogger.parentOptional);

		}

		return currentTaskLogger;

	}

	public
	long errorCount () {

		return errorCount;

	}

	public
	boolean errors () {

		return moreThanZero (
			errorCount);

	}

	public
	void firstErrorFormat (
			@NonNull String ... arguments) {

		firstError =
			stringFormatArray (
				arguments);

	}

	public
	void lastErrorFormat (
			@NonNull String ... arguments) {

		lastError =
			stringFormatArray (
				arguments);

	}

	public
	void addChild (
			@NonNull TaskLogger child) {

		events.add (
			child);

	}

	// implementation

	public
	void logFormat (
			@NonNull LogSeverity severity,
			@NonNull String ... arguments) {

		switch (severity) {

		case fatal:

			fatalFormat (
				arguments);

			break;

		case error:

			errorFormat (
				arguments);

			break;

		case warning:

			warningFormat (
				arguments);

			break;

		case notice:

			noticeFormat (
				arguments);

			break;

		case trace:

			debugFormat (
				arguments);

			break;

		case debug:

			debugFormat (
				arguments);

			break;

		}

	}

	public
	void fatalFormat (
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.fatal,
			message,
			optionalAbsent ());

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.fatal,
				message));

		throw new FatalErrorException (
			this,
			message);

	}

	public
	void fatalFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.fatal,
			message,
			optionalOf (
				throwable));

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.fatal,
				message));

		throw new FatalErrorException (
			this,
			message,
			throwable);

	}

	public
	void errorFormat (
			@NonNull String ... arguments) {

		writeFirstError ();

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.error,
			message,
			optionalAbsent ());

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.error,
				message));

		increaseErrorCount ();

	}

	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		writeFirstError ();

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.error,
			message,
			optionalOf (
				throwable));

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.error,
				message));

		increaseErrorCount ();

	}

	public
	void warningFormat (
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.warning,
			message,
			optionalAbsent ());

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.warning,
				message));

		increaseWarningCount ();

	}

	public
	void warningFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.warning,
			message,
			optionalOf (
				throwable));

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.warning,
				message));

		increaseWarningCount ();

	}

	public
	void noticeFormat (
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.notice,
			message,
			optionalAbsent ());

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.notice,
				message));

		increaseNoticeCount ();

	}

	public
	void noticeFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.notice,
			message,
			optionalOf (
				throwable));

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.notice,
				message));

		increaseNoticeCount ();

	}

	public
	void debugFormat (
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.debug,
			message,
			optionalAbsent ());

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.debug,
				message));

		increaseDebugCount ();

	}

	public
	void debugFormatException (
			@NonNull Throwable throwable,
			@NonNull String ... arguments) {

		String message =
			stringFormatArray (
				arguments);

		logTarget.writeToLog (
			LogSeverity.debug,
			message,
			optionalOf (
				throwable));

		events.add (
			new TaskLogEntryEvent (
				LogSeverity.debug,
				message));

		increaseDebugCount ();

	}

	public
	RuntimeException makeException (
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					lastError,
					integerToDecimalString (
						errorCount));

			logTarget.writeToLog (
				LogSeverity.error,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					LogSeverity.error,
					message));

			throw exceptionSupplier.get ();

		} else {

			return new RuntimeException (
				"No errors");

		}

	}

	public
	RuntimeException makeException () {

		if (errors ()) {

			String message =
				stringFormat (
					"%s due to %s errors",
					lastError,
					integerToDecimalString (
						errorCount));

			logTarget.writeToLog (
				LogSeverity.error,
				message,
				optionalAbsent ());

			throw new LoggedErrorsException (
				this,
				message);

		} else {

			return new RuntimeException (
				"No errors");

		}

	}

	public <Type>
	Type wrap (
			@NonNull Function <TaskLogger, Type> function) {

		Type returnValue =
			function.apply (
				this);

		makeException ();

		return returnValue;

	}

	public
	void wrap (
			@NonNull Consumer <TaskLogger> function) {

		function.accept (
			this);

		makeException ();

	}

	public
	boolean debugEnabled () {

		return (
			logTarget.debugEnabled ()
			|| debugEnabled
		);

	}

	// private implementation

	private
	void writeFirstError () {

		// recurse up through parents

		if (
			optionalIsPresent (
				parentOptional)
		) {

			parentOptional.get ().writeFirstError ();

		}

		if (

			equalToZero (
				errorCount)

			&& isNotNull (
				firstError)

		) {

			// add to parent if we didn't already

			addToParent ();

			// write first error

			logTarget.writeToLog (
				LogSeverity.error,
				firstError,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					LogSeverity.error,
					firstError));

		}

	}

	private
	void increaseErrorCount () {

		errorCount ++;

		optionalDo (
			parentOptional,
			TaskLogger::increaseErrorCount);

	}

	private
	void increaseWarningCount () {

		warningCount ++;

		optionalDo (
			parentOptional,
			TaskLogger::increaseWarningCount);

	}

	private
	void increaseNoticeCount () {

		noticeCount ++;

		optionalDo (
			parentOptional,
			TaskLogger::increaseNoticeCount);

	}

	private
	void increaseDebugCount () {

		debugCount ++;

		optionalDo (
			parentOptional,
			TaskLogger::increaseDebugCount);

	}

	private
	void addToParent () {

		if (addedToParent) {
			return;
		}

		if (
			optionalIsPresent (
				parentOptional)
		) {

			parentOptional.get ().addChild (
				this);
		}

		addedToParent = true;

	}

	// task log event implementation

	@Override
	public
	LogSeverity eventSeverity () {

		return severity;

	}

	@Override
	public
	String eventText () {

		return stringFormat (
			"%s.%s",
			staticContext,
			dynamicContext);

	}

	@Override
	public
	List <TaskLogEvent> eventChildren () {

		return ImmutableList.copyOf (
			events);

	}

}
