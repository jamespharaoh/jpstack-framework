package wbs.console.forms.time;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.text.TextFormFieldRenderer;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("secondsFormFieldBuilder")
public
class SecondsFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DurationFormFieldInterfaceMapping>
		durationFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <NullFormFieldConstraintValidator>
		nullFormFieldConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
		requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <SecondsFormFieldNativeMapping>
		secondsFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <SecondsFormFieldValueValidator>
		secondsFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <TextFormFieldRenderer> textFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	SecondsFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String name =
				spec.name ();

			String label =
				ifNull (
					spec.label (),
					capitalise (
						camelToSpaces (
							name)));

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			DurationFormFieldInterfaceMapping.Format format =
				ifNull (
					spec.format (),
					DurationFormFieldInterfaceMapping.Format.textual);

			Class<?> propertyClass =
				PropertyUtils.propertyClassForClass (
					context.containerClass (),
					name);

			// accessor

			FormFieldAccessor accessor =
				simpleFormFieldAccessorProvider.provide (
					taskLogger)

				.name (
					name)

				.nativeClass (
					propertyClass);

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				secondsFormFieldNativeMappingProvider.provide (
					taskLogger);

			// value validators

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.provide (
						taskLogger));

			}

			valueValidators.add (
				secondsFormFieldValueValidatorProvider.provide (
					taskLogger));

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldConstraintValidatorProvider.provide (
					taskLogger);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				durationFormFieldInterfaceMappingProvider.provide (
					taskLogger)

				.label (
					label)

				.format (
					format);

			// renderer

			FormFieldRenderer renderer =
				textFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable)

				.listAlign (
					FormField.Align.right);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// field

			if (! readOnly) {

				formFieldSet.addFormItem (
					updatableFormFieldProvider.provide (
						taskLogger)

					.name (
						name)

					.label (
						label)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.valueValidators (
						valueValidators)

					.constraintValidator (
						constraintValidator)

					.interfaceMapping (
						interfaceMapping)

					.renderer (
						renderer)

					.updateHook (
						updateHook)

				);

			} else {

				formFieldSet.addFormItem (
					readOnlyFormFieldProvider.provide (
						taskLogger)

					.name (
						name)

					.label (
						label)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.interfaceMapping (
						interfaceMapping)

					.renderer (
						renderer)

				);

			}

		}

	}

}
