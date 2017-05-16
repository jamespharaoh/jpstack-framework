package wbs.console.supervisor;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPageBuilder")
public
class SupervisorPageBuilder <
	ObjectType extends Record <ObjectType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <SupervisorPart> supervisorPart;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	SupervisorPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String name;
	String tabName;
	String tabLocation;
	String tabLabel;
	String fileName;
	String responderName;
	String title;

	PagePartFactory pagePartFactory;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			 OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildConsoleModule");

		) {

			setDefaults ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					resolvedExtensionPoint);

			}

			buildPagePartFactory ();

			buildResponder ();

		}

	}

	void buildContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				container.tabLocation (),
				contextTab.get ()
					.name (tabName)
					.defaultLabel (tabLabel)
					.localFile (fileName),
				extensionPoint.contextTypeNames ());

		}

	}

	void buildContextFile (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextFile (
			fileName,
			consoleFile.get ()
				.getResponderName (responderName),
			extensionPoint.contextTypeNames ());

	}

	void buildPagePartFactory () {

		pagePartFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePartFactory");

			) {

				return supervisorPart.get ()

					.fileName (
						fileName)

					.fixedSupervisorConfigName (
						spec.configName ());

			}

		};

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()

			.tab (
				tabName)

			.title (
				title)

			.pagePartFactory (
				pagePartFactory)

		);

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		name =
			ifNull (
				spec.name (),
				"supervisor");

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		tabLocation =
			container.tabLocation ();

		tabLabel =
			ifNull (
				spec.tabLabel (),
				capitalise (
					camelToSpaces (
						name)));

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (name)));

		title =
			ifNull (
				spec.title (),
				"Supervisor");

	}

}
