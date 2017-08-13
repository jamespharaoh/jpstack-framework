package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.classDoesNotExist;
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
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ClassName;
import wbs.utils.exception.RuntimeClassNotFoundException;

@SingletonComponent ("objectComponentPlugin")
public
class ObjectComponentPlugin
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

			plugin.models ().models ().forEach (
				projectModelSpec -> {

				registerObjectHooks (
					taskLogger,
					componentRegistry,
					projectModelSpec);

				registerObjectHelper (
					taskLogger,
					componentRegistry,
					projectModelSpec);

				registerObjectHelperMethodsImplementation (
					taskLogger,
					componentRegistry,
					projectModelSpec);

			});

		}

	}

	private
	void registerObjectHooks (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginRecordModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHooks");

		) {

			String objectHooksComponentName =
				stringFormat (
					"%sHooks",
					hyphenToCamel (
						model.name ()));

			ClassName objectHooksClassName =
				classNameFormat (
					"%s.logic.%sHooks",
					model.plugin ().packageName (),
					hyphenToCamelCapitalise (
						model.name ()));

			Class <?> objectHooksClass;

			try {

				objectHooksClass =
					classForNameRequired (
						objectHooksClassName);

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						objectHooksComponentName)

					.componentClass (
						objectHooksClass)

					.scope (
						"singleton")

				);

			} catch (RuntimeClassNotFoundException exception) {

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						objectHooksComponentName)

					.componentClass (
						ObjectHooks.DefaultImplementation.class)

					.scope (
						"singleton")

				);

			}

		}

	}

	private
	void registerObjectHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginRecordModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHelper");

		) {

			String objectHelperComponentName =
				stringFormat (
					"%sObjectHelper",
					hyphenToCamel (
						model.name ()));

			ClassName objectHelperImplementationClassName =
				classNameFormat (
					"%s.logic.%sObjectHelperImplementation",
					model.plugin ().packageName (),
					hyphenToCamelCapitalise (
						model.name ()));

			Class <?> objectHelperImplementationClass =
				classForNameRequired (
					objectHelperImplementationClassName);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					objectHelperComponentName)

				.componentClass (
					objectHelperImplementationClass)

				.scope (
					"singleton")

			);

		}

	}

	private
	void registerObjectHelperMethodsImplementation (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginRecordModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerObjectHelperMethodsImplementation");

		) {

			String objectHelperMethodsImplementationComponentName =
				stringFormat (
					"%sObjectHelperMethodsImplementation",
					hyphenToCamel (
						model.name ()));

			ClassName objectHelperMethodsImplementationClassName =
				classNameFormat (
					"%s.logic.%sObjectHelperMethodsImplementation",
					model.plugin ().packageName (),
					hyphenToCamelCapitalise (
						model.name ()));

			if (
				classDoesNotExist (
					objectHelperMethodsImplementationClassName)
			) {
				return;
			}

			Class <?> objectHelperMethodsImplementationClass =
				classForNameRequired (
					objectHelperMethodsImplementationClassName);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					objectHelperMethodsImplementationComponentName)

				.componentClass (
					objectHelperMethodsImplementationClass)

				.scope (
					"singleton")

			);

			return;

		}

	}

}
