package wbs.framework.fixtures;

import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginFixtureSpec;
import wbs.framework.component.scaffold.PluginMetaFixtureSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("fixtureComponentPlugin")
public
class FixtureComponentPlugin
	implements ComponentPlugin {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			registerMetaFixtureComponents (
				taskLogger,
				componentRegistry,
				plugin);

			registerFixtureComponents (
				taskLogger,
				componentRegistry,
				plugin);

		}
	}

	// private implementation

	private
	void registerMetaFixtureComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerMetaFixtureComponents");

		) {

			for (
				PluginMetaFixtureSpec metaFixture
					: plugin.metaFixtures ()
			) {

				String metaFixturesComponentName =
					stringFormat (
						"%sMetaFixtures",
						metaFixture.name ());

				String metaFixturesClassName =
					stringFormat (
						"%s.fixture.%sMetaFixtures",
						plugin.packageName (),
						hyphenToCamelCapitalise (
							metaFixture.name ()));

				Class <?> metaFixtureProviderClass;

				try {

					metaFixtureProviderClass =
						Class.forName (
							metaFixturesClassName);

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"Can't find class %s ",
						metaFixturesClassName,
						"for meta fixture %s ",
						metaFixture.name (),
						"from %s",
						plugin.name ());

					continue;

				}

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						metaFixturesComponentName)

					.componentClass (
						metaFixtureProviderClass)

					.scope (
						"prototype"));

			}

		}

	}

	private
	void registerFixtureComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerFixtureComponents");

		) {

			for (
				PluginFixtureSpec fixture
					: plugin.fixtures ()
			) {

				String fixtureProviderComponentName =
					stringFormat (
						"%sFixtureProvider",
						fixture.name ());

				String fixtureProviderClassName =
					stringFormat (
						"%s.fixture.%sFixtureProvider",
						plugin.packageName (),
						hyphenToCamelCapitalise (
							fixture.name ()));

				Class<?> fixtureProviderClass;

				try {

					fixtureProviderClass =
						Class.forName (
							fixtureProviderClassName);

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"Can't find fixture provider of type %s ",
						fixtureProviderClassName,
						"for fixture %s ",
						fixture.name (),
						"from %s",
						plugin.name ());

					continue;

				}

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						fixtureProviderComponentName)

					.componentClass (
						fixtureProviderClass)

					.scope (
						"prototype"));

			}

		}

	}

}
