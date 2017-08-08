package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.responder.ConsoleFile;

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
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simpleActionPageBuilder")
public
class SimpleActionPageBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleActionPageSpec simpleActionPageSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String path;
	String actionName;
	String responderName;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();

			buildFile (
				taskLogger);

		}

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFile");

		) {

			consoleModule.addFile (
				path,
				consoleFileProvider.provide (
					taskLogger)

				.getResponderName (
					taskLogger,
					responderName)

				.postActionName (
					taskLogger,
					actionName)

			);

		}

	}

	void setDefaults () {

		name =
			simpleActionPageSpec.name ();

		path =
			simpleActionPageSpec.path ();

		actionName =
			ifNull (
				simpleActionPageSpec.actionName (),
				stringFormat (
					"%s%sAction",
					simpleContainerSpec.existingBeanNamePrefix (),
					capitalise (name)));

		responderName =
			ifNull (
				simpleActionPageSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

		/*
		responderBeanName =
			ifNull (
				simpleActionPageSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.existingBeanNamePrefix (),
					capitalise (name)));
		*/

	}

}
