package wbs.framework.exception;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface GenericExceptionLogger <Resolution> {

	Record <?> logSimple (
			TaskLogger parentTaskLogger,
			CharSequence typeCode,
			CharSequence source,
			CharSequence summary,
			CharSequence dump,
			Optional <Long> userId,
			Resolution resolution);

	Record <?> logThrowable (
			TaskLogger parentTaskLogger,
			CharSequence typeCode,
			CharSequence source,
			Throwable throwable,
			Optional <Long> userId,
			Resolution resolution);

	Record <?> logThrowableWithSummary (
			TaskLogger parentTaskLogger,
			CharSequence typeCode,
			CharSequence source,
			CharSequence summary,
			Throwable throwable,
			Optional <Long> userId,
			Resolution resolution);

}
