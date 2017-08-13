package wbs.framework.hibernate;

import static wbs.utils.collection.IterableUtils.iterableChainArguments;
import static wbs.utils.etc.TypeUtils.classExists;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.classNameFormat;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ClassName;

@SingletonComponent ("hibernateComponentPlugin")
public
class HibernateComponentPlugin
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
					"registerHibernateLayerComponents");

		) {

			iterableChainArguments (
				plugin.models ().models (),
				plugin.models ().compositeTypes ()
			).forEach (
				projectModelSpec ->
					registerDaoHibernate (
						taskLogger,
						componentRegistry,
						projectModelSpec));

		}

	}

	// private implementation

	private
	void registerDaoHibernate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec pluginModelSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerDaoHibernate");

		) {

			String daoComponentName =
				stringFormat (
					"%sDao",
					hyphenToCamel (
						pluginModelSpec.name ()));

			ClassName daoInterfaceClassName =
				classNameFormat (
					"%s.model.%sDao",
					pluginModelSpec.plugin ().packageName (),
					hyphenToCamelCapitalise (
						pluginModelSpec.name ()));

			boolean daoInterfaceClass =
				classExists (
					daoInterfaceClassName);

			ClassName daoHibernateClassName =
				classNameFormat (
					"%s.hibernate.%sDaoHibernate",
					pluginModelSpec.plugin ().packageName (),
					hyphenToCamelCapitalise (
						pluginModelSpec.name ()));

			Class <?> daoHibernateClass = null;

			boolean daoHibernateClassExists =
				classExists (
					daoHibernateClassName);

			if (daoHibernateClassExists) {

				daoHibernateClass =
					classForNameRequired (
						daoHibernateClassName);

			}

			if (
				! daoInterfaceClass
				&& ! daoHibernateClassExists
			) {
				return;
			}

			if (
				! daoInterfaceClass
				|| ! daoHibernateClassExists
			) {

				taskLogger.errorFormat (
					"DAO methods or implementation missing for %s in %s",
					pluginModelSpec.name (),
					pluginModelSpec.plugin ().name ());

				return;

			}

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					daoComponentName)

				.componentClass (
					daoHibernateClass)

				.scope (
					"singleton")

			);

			return;

		}

	}

}
