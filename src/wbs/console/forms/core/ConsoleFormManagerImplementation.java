package wbs.console.forms.core;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleFormManager")
public
class ConsoleFormManagerImplementation
	implements ConsoleFormManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, ConsoleFormType <?>> formTypes;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFormTypeImplementation <?>> consoleFormTypeProvider;

	// public implementation

	@Override
	public <Type>
	Optional <ConsoleFormType <Type>> getFormType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String consoleModuleName,
			@NonNull String name,
			@NonNull Class <Type> containerClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getFormType",
					keyEqualsString (
						"consoleModuleName",
						consoleModuleName),
					keyEqualsString (
						"name",
						name));

		) {

			String componentName =
				stringFormat (
					"%s%sFormType",
					hyphenToCamel (
						consoleModuleName),
					hyphenToCamelCapitalise (
						name));

			return genericCastUnchecked (
				mapItemForKey (
					formTypes,
					componentName));

		}

	}

	@Override
	public <Type>
	ConsoleFormType <Type> createFormType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleModule consoleModule,
			@NonNull String formName,
			@NonNull Class <Type> containerClass,
			@NonNull FormType formType,
			@NonNull Optional <String> columnFieldsNameOptional,
			@NonNull Optional <String> rowFieldsNameOptional) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFormType",
					keyEqualsString (
						"consoleModuleName",
						consoleModule.name ()),
					keyEqualsString (
						"formName",
						formName));

		) {

			return genericCastUnchecked (
				consoleFormTypeProvider.get ()

				.formName (
					formName)

				.containerClass (
					genericCastUnchecked (
						containerClass))

				.formType (
					formType)

				.columnFields (
					optionalOrNull (
						optionalMapRequired (
							columnFieldsNameOptional,
							columnFieldsName ->
								genericCastUnchecked (
									consoleModule.formFieldSetRequired (
										columnFieldsName)))))

				.rowFields (
					optionalOrNull (
						optionalMapRequired (
							rowFieldsNameOptional,
							rowFieldsName ->
								genericCastUnchecked (
									consoleModule.formFieldSetRequired (
										rowFieldsName)))))

			);

		}

	}

	@Override
	public <Type>
	ConsoleFormType <Type> createFormType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String formName,
			@NonNull Class <Type> containerClass,
			@NonNull FormType formType,
			@NonNull Optional <FormFieldSet <Type>> columnFieldsOptional,
			@NonNull Optional <FormFieldSet <Type>> rowFieldsOptional) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFormContextBuilder",
					keyEqualsString (
						"formName",
						formName));

		) {

			return genericCastUnchecked (
				consoleFormTypeProvider.get ()

				.formName (
					formName)

				.containerClass (
					genericCastUnchecked (
						containerClass))

				.formType (
					formType)

				.columnFields (
					genericCastUncheckedNullSafe (
						optionalOrNull (
							columnFieldsOptional)))

				.rowFields (
					genericCastUncheckedNullSafe (
						optionalOrNull (
							rowFieldsOptional)))

			);

		}

	}

}
