package wbs.framework.logging;

import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;

import java.io.OutputStreamWriter;

import lombok.NonNull;

import wbs.utils.io.SafeWriter;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public
class TaskLogUtils {

	public static
	void writeTaskLogToStandardError (
			@NonNull TaskLogEvent taskLogEvent) {

		try (

			SafeWriter writer =
				new SafeWriter (
					new OutputStreamWriter (
						System.err));

			FormatWriter formatWriter =
				new WriterFormatWriter (
					writer);

		) {

			formatWriter.indentString (
				"  ");

			writeTaskLog (
				formatWriter,
				taskLogEvent);

		}

	}

	public static
	void writeTaskLog (
			@NonNull FormatWriter formatWriter,
			@NonNull TaskLogEvent taskLogEvent) {

		formatWriter.writeLineFormatIncreaseIndent (
			"%s %s",
			enumName (
				taskLogEvent.eventSeverity ()),
			taskLogEvent.eventText ());

		taskLogEvent.eventChildren ().forEach (
			childEvent ->
				writeTaskLog (
					formatWriter,
					childEvent));

		formatWriter.decreaseIndent ();

	}

	public static
	String taskLogToString (
			@NonNull String indent,
			@NonNull TaskLogEvent taskLogEvent) {

		return formatWriterConsumerToString (
			indent,
			formatWriter ->
				writeTaskLog (
					formatWriter,
					taskLogEvent));

	}

}
