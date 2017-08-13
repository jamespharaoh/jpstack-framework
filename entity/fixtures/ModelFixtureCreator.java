package wbs.framework.entity.fixtures;

import static wbs.framework.logging.TaskLogUtils.writeTaskLogToStandardError;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginMetaFixtureSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.fixtures.MetaFixtures;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class ModelFixtureCreator {

	// singleton dependencies

	@SingletonDependency
	List <BackgroundProcess> backgroundProcesses;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	PluginManager pluginManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <BuilderFactory <?, Transaction>> builderFactoryProvider;

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <MetaFixtures>> metaFixtureProviders;

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <ModelFixtureBuilderComponent>>
		modelFixtureBuilderProviders;

	// state

	Builder <Transaction> fixtureBuilder;

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

			createFixtureBuilder (
				taskLogger);

		}

	}

	// implementation

	private
	void createFixtureBuilder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtureBuilder");

		) {

			fixtureBuilder =
				builderFactoryProvider.provide (
					taskLogger)

				.contextClass (
					Transaction.class)

				.addBuilders (
					taskLogger,
					modelFixtureBuilderProviders)

				.create (
					taskLogger);

		}

	}

	public
	void runModelFixtureCreators (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runModelFixtureCreators");

		) {

			runMetaFixtureProviders (
				taskLogger,
				arguments);

			runModelFixtureBuilders (
				taskLogger,
				arguments);

		}

	}

	// private implementation

	private
	void runModelFixtureBuilders (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"runModelFixtureCreators");

		) {

			transaction.noticeFormat (
				"About to create model fixtures");

			for (
				RecordSpec spec
					: modelMetaLoader.allSpecs ().values ()
			) {

				Model <?> model =
					entityHelper.recordModelsByName ().get (
						spec.name ());

				fixtureBuilder.descend (
					transaction,
					spec,
					spec.children (),
					model,
					MissingBuilderBehaviour.error);

			}

			transaction.commit ();

			transaction.noticeFormat (
				"All model fixtures created successfully");

		}

	}

	private
	void runMetaFixtureProviders (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runMetaFixtureProviders");

		) {

			taskLogger.noticeFormat (
				"Disabling background processes");

			backgroundProcesses.forEach (process ->
				process.runAutomatically (
					false));

			taskLogger.noticeFormat (
				"About to run fixture providers");

			for (
				PluginSpec plugin
					: pluginManager.plugins ()
			) {

				for (
					PluginMetaFixtureSpec metaFixture
						: plugin.metaFixtures ()
				) {

					runMetaFixtureProvider (
						taskLogger,
						plugin,
						metaFixture);

				}

			}

			taskLogger.noticeFormat (
				"All fixtures providers run successfully");

		}

	}

	private
	void runMetaFixtureProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec,
			@NonNull PluginMetaFixtureSpec metaFixtureSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runMetaFixtureProvider",
					keyEqualsString (
						"pluginName",
						pluginSpec.name ()),
					keyEqualsString (
						"fixtureName",
						metaFixtureSpec.name ()));

		) {

			taskLogger.noticeFormat (
				"About to run fixture provider %s from %s",
				metaFixtureSpec.name (),
				pluginSpec.name ());

			String metaFixtureProviderClassName =
				stringFormat (
					"%s.fixture.%sMetaFixtures",
					pluginSpec.packageName (),
					hyphenToCamelCapitalise (
						metaFixtureSpec.name ()));

			Optional <Class <?>> metaFixtureProviderClassOptional =
				classForName (
					metaFixtureProviderClassName);

			if (
				optionalIsNotPresent (
					metaFixtureProviderClassOptional)
			) {

				taskLogger.errorFormat (
					"Can't find meta fixture provider of type %s for ",
					metaFixtureProviderClassName,
					"fixture %s ",
					metaFixtureSpec.name (),
					"from %s",
					pluginSpec.name ());

				return;

			}

			Class <?> metaFixtureProviderClass =
				optionalGetRequired (
					metaFixtureProviderClassOptional);

			ComponentProvider <MetaFixtures>
				metaFixtureProviderProvider =
					metaFixtureProviders.get (
						metaFixtureProviderClass);

			MetaFixtures metaFixture =
				metaFixtureProviderProvider.provide (
					taskLogger);

			runMetaFixtureProviderReal (
				taskLogger,
				pluginSpec,
				metaFixtureSpec,
				metaFixture);

		}

	}

	private
	void runMetaFixtureProviderReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec,
			@NonNull PluginMetaFixtureSpec metaFixtureSpec,
			@NonNull MetaFixtures metaFixture) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runFixtureProviderReal");

		) {

			try {

				metaFixture.createFixtures (
					taskLogger);

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error creating fixture %s from %s",
					metaFixtureSpec.name (),
					pluginSpec.name ());

				writeTaskLogToStandardError (
					taskLogger);

			}

		}

	}

}
