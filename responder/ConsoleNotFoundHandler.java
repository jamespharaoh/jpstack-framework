package wbs.console.responder;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.NotFoundPart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabbedResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebNotFoundHandler;

@SingletonComponent ("notFoundHandler")
public
class ConsoleNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TabbedResponder> tabbedPageProvider;

	@PrototypeDependency
	ComponentProvider <NotFoundResponder> notFoundPageProvider;

	@PrototypeDependency
	ComponentProvider <NotFoundPart> notFoundPartProvider;

	// implementation

	private final
	Tab notFoundTab =
		new Tab ("Not found") {

		@Override
		public
		String getUrl (
				@NonNull Transaction parentTransaction) {

			return requestContext.requestPath ();

		}

	};

	@Override
	public
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleNotFound");

		) {

			// log it the old fashioned way

			taskLogger.errorFormat (
				"Path not found: %s",
				requestContext.requestUri ());

			// make an exception log of this calamity

			try {

				String path =
					stringFormat (
						"%s%s",
						requestContext.servletPath (),
						optionalOrEmptyString (
							requestContext.pathInfo ()));

				exceptionLogger.logSimple (
					taskLogger,
					"console",
					path,
					"Not found",
					"The specified path was not found",
					consoleUserHelper.loggedInUserId (),
					GenericExceptionResolution.ignoreWithUserWarning);

			} catch (RuntimeException exception) {

				taskLogger.fatalFormatException (
					exception,
					"Error creating not found log: %s",
					 exception.getMessage ());

			}

			// show the not found page

			Optional <TabContext> tabContextOptional =
				requestContext.tabContext ();

			if (
				optionalIsPresent (
					tabContextOptional)
			) {

				tabbedPageProvider.provide (
					taskLogger)

					.tab (
						notFoundTab)

					.title (
						"Page not found")

					.pagePart (
						notFoundPartProvider.provide (
							taskLogger))

					.execute (
						taskLogger)

				;

			} else {

				notFoundPageProvider.provide (
					taskLogger)

					.execute (
						taskLogger)

				;

			}

		}

	}

}
