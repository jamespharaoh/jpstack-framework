package wbs.integrations.broadcastsystems.api;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("broadcastSystemsApiServletModule")
public
class BroadcastSystemsApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	@Named
	WebFile broadcastSystemsReportFile;

	// ================================================================ entries

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

			@Override
			protected
			WebFile handle (
					Matcher matcher) {

				requestContext.request (
					"routeId",
					Integer.parseInt (
						matcher.group (1)));

				return defaultFiles.get (
					matcher.group (2));

			}

		};

	// =========================================================== path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ================================================================== files

	Map <String, WebFile> defaultFiles;

	// ========================================================= servlet module

	@Override
	public
	Map <String, PathHandler> paths () {

		return ImmutableMap.<String, PathHandler> builder ()

			.put (
				"/broadcastsystems",
				pathHandler)

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

	// ========================================================= servlet module

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		defaultFiles =
			ImmutableMap.<String,WebFile>builder ()

				.put (
					"report",
					broadcastSystemsReportFile)

				.build ();

	}

}
