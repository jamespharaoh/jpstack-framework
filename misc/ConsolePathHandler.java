package wbs.console.misc;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.HiddenComponent;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.pathhandler.DelegatingPathHandler;

@SingletonComponent ("rootPathHandler")
@HiddenComponent
public
class ConsolePathHandler
	implements ComponentFactory <DelegatingPathHandler> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <DelegatingPathHandler> delegatingPathHandlerProvider;

	// implementation

	@Override
	public
	DelegatingPathHandler makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return delegatingPathHandlerProvider.provide (
				taskLogger);

		}

	}

}
