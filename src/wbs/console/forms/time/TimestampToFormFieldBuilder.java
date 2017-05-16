package wbs.console.forms.time;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("timestampToFormFieldBuilder")
public
class TimestampToFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototsype dependencies

	@PrototypeDependency
	Provider <DateFormFieldNativeMapping>
	dateFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TimestampToFormFieldInterfaceMapping>
	timestampToFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	TimestampToFormFieldSpec spec;

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

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			// accessor and native mapping

			Class<?> propertyClass =
				PropertyUtils.propertyClassForClass (
					context.containerClass (),
					name);

			FormFieldAccessor formFieldAccessor;
			ConsoleFormNativeMapping formFieldNativeMapping;

			if (propertyClass == Instant.class) {

				formFieldAccessor =
					simpleFormFieldAccessorProvider.get ()
						.name (name)
						.nativeClass (Instant.class);

				formFieldNativeMapping =
					identityFormFieldNativeMappingProvider.get ();

			} else if (propertyClass == Date.class) {

				formFieldAccessor =
					simpleFormFieldAccessorProvider.get ()
						.name (name)
						.nativeClass (Date.class);

				formFieldNativeMapping =
					dateFormFieldNativeMappingProvider.get ();

			} else {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map %s as timestamp for %s.%s",
						classNameSimple (
							propertyClass),
						classNameSimple (
							context.containerClass ()),
						name));

			}

			// value validators

			List<FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.get ());

			}

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldValueConstraintValidatorProvider.get ();

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				timestampToFormFieldInterfaceMappingProvider.get ()
					.name (name);

			// renderer

			FormFieldRenderer renderer =
				textFormFieldRendererProvider.get ()

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					context,
					context.containerClass (),
					name);

			// form field

			if (readOnly) {

				formFieldSet.addFormItem (

					readOnlyFormFieldProvider.get ()

					.name (
						name)

					.label (
						label)

					.accessor (
						formFieldAccessor)

					.nativeMapping (
						formFieldNativeMapping)

					.interfaceMapping (
						interfaceMapping)

					.renderer (
						renderer)

				);

			} else {

				formFieldSet.addFormItem (

					updatableFormFieldProvider.get ()

					.name (
						name)

					.label (
						label)

					.accessor (
						formFieldAccessor)

					.nativeMapping (
						formFieldNativeMapping)

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

			}

		}

	}

}
