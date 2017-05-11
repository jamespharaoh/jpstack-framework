package wbs.framework.schema.tool;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.SimpleExceptionLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("exceptionLogger")
public class SchemaToolExceptionLogger
	implements ComponentFactory <ExceptionLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <SimpleExceptionLogger> simpleExceptionLoggerProvider;

	// implementation

	@Override
	public
	ExceptionLogger makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return simpleExceptionLoggerProvider.get ();

		}

	}

}
