package wbs.framework.component.config;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("genericConfigLoader")
public
class GenericConfigLoader {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype depdendencies

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.get ()

				.registerBuilderClasses (
					GenericConfigSpec.class,
					GenericConfigItemSpec.class)

				.build ();

		}

	}

	// public implementation

	public <Config extends AbstractGenericConfig <Config>>
	AbstractGenericConfig <?> load (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String configFilePath) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"load");

		) {

			return new AbstractGenericConfig <Config> ()

				.genericConfigSpec (
					loadSpec (
						taskLogger,
						configFilePath));

		}

	}

	public
	GenericConfigSpec loadSpec (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String configFilePath) {

		return (GenericConfigSpec)
			dataFromXml.readFilenameRequired (
				parentTaskLogger,
				configFilePath);

	}

}
