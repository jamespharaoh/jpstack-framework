package wbs.console.forms.object;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("parentFormFieldBuilder")
public
class ParentFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldInterfaceMapping>
		identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
		identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <ObjectFormFieldRenderer> objectFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <ParentFormFieldAccessor> parentFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <ParentFormFieldConstraintValidator>
		parentFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
		requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	ParentFormFieldSpec spec;

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

			ConsoleHelper consoleHelper =
				context.consoleHelper ();

			String name =
				ifNull (
					spec.name (),
					consoleHelper.parentExists ()
						? consoleHelper.parentFieldName ()
						: "parent");

			String label =
				ifNull (
					spec.label (),
					consoleHelper.parentExists ()
						? capitalise (consoleHelper.parentLabel ())
						: "Parent");

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					! consoleHelper.parentTypeIsFixed ());

			ConsoleHelper <?> parentHelper =
				consoleHelper.parentTypeIsFixed ()
					? objectManager.consoleHelperForClassRequired (
						consoleHelper.parentClassRequired ())
					: null;

			// accessor

			FormFieldAccessor accessor =
				ifThenElse (
					consoleHelper.canGetParent (),

				() -> simpleFormFieldAccessorProvider.provide (
					taskLogger)

					.name (
						consoleHelper.parentFieldName ())

					.nativeClass (
						consoleHelper.parentClassRequired ())

				,

				() -> parentFormFieldAccessorProvider.provide (
					taskLogger)

			);

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				identityFormFieldNativeMappingProvider.provide (
					taskLogger);

			// value validator

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.provide (
					taskLogger));

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				parentFormFieldValueConstraintValidatorProvider.provide (
					taskLogger)

				.consoleHelper (
					consoleHelper)

			;

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// renderer

			FormFieldRenderer renderer =
				objectFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.consoleHelper (
					parentHelper)

				.rootFieldName (
					spec.rootPath ())

				.mini (
					consoleHelper.parentTypeIsFixed ())

				.nullable (
					false)

			;

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// field

			if (! readOnly) {

				if (! consoleHelper.parentTypeIsFixed ())
					throw new RuntimeException ();

				// read only field

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

				if (
					spec.readOnly () != null
					&& ! spec.readOnly ()
				) {
					throw new RuntimeException ();
				}

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
