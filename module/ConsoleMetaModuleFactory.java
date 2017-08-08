package wbs.console.module;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextMetaBuilderContainer;

import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleMetaModuleFactory
	implements ComponentFactory <ConsoleMetaModule> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaModuleBuilder consoleMetaModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleMetaModuleImplementation>
		consoleMetaModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleModuleSpec;

	// implementation

	@Override
	public
	ConsoleMetaModule makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			ConsoleMetaModuleImplementation consoleMetaModule =
				consoleMetaModuleProvider.provide (
					taskLogger);

			ConsoleContextMetaBuilderContainer contextMetaBuilderContainer =
				new ConsoleContextMetaBuilderContainer ();

			consoleMetaModuleBuilder.descend (
				taskLogger,
				contextMetaBuilderContainer,
				consoleModuleSpec.builders (),
				consoleMetaModule,
				MissingBuilderBehaviour.ignore);

			return consoleMetaModule;

		}

	}

}
