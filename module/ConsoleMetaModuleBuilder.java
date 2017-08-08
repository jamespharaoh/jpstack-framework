package wbs.console.module;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

@SingletonComponent ("consoleMetaModuleBuilder")
public
class ConsoleMetaModuleBuilder
	implements Builder <TaskLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <ConsoleMetaModuleBuilderComponent>>
		consoleMetaModuleBuilders;

	// state

	Builder <TaskLogger> builder;

	// init

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			builder =
				builderFactoryProvider.provide (
					taskLogger)

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					consoleMetaModuleBuilders)

				.create (
					taskLogger)

			;

		}

	}

	// builder

	@Override
	public
	void descend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		builder.descend (
			parentTaskLogger,
			parentObject,
			childObjects,
			targetObject,
			missingBuilderBehaviour);

	}

}
