package wbs.console.object;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleBuilderComponent;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("objectContextMetaBuilder")
public
class ObjectContextMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextRootExtensionPoint>
		rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// state

	String contextTypeName;
	String componentName;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List<String> listContextTypeNames;
	List<String> objectContextTypeNames;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();

			// extension points

			metaModule.addExtensionPoint (
				rootExtensionPointProvider.provide (
					taskLogger)

				.name (
					contextTypeName + ":list")

				.contextTypeNames (
					listContextTypeNames)

				.contextLinkNames (
					ImmutableList.of (
						contextTypeName))

				.parentContextNames (
					ImmutableList.of (
						naivePluralise (
							contextTypeName),
						contextTypeName))

			);

			metaModule.addExtensionPoint (
				rootExtensionPointProvider.provide (
					taskLogger)

				.name (
					contextTypeName + ":object")

				.contextTypeNames (
					objectContextTypeNames)

				.contextLinkNames (
					ImmutableList.<String>of (
						contextTypeName))

				.parentContextNames (
					ImmutableList.<String>of (
						contextTypeName,
						"link:" + contextTypeName))

			);

			// context hints

			metaModule.addContextHint (
				new ConsoleContextHint ()

				.linkName (
					contextTypeName)

				.singular (
					true)

				.plural (
					true)

			);

			// descend

			ConsoleContextMetaBuilderContainer listContainer =
				new ConsoleContextMetaBuilderContainer ()

				.structuralName (
					contextTypeName)

				.extensionPointName (
					contextTypeName + ":list");

			builder.descend (
				taskLogger,
				listContainer,
				spec.listChildren (),
				metaModule,
				MissingBuilderBehaviour.ignore);

			ConsoleContextMetaBuilderContainer objectContainer =
				new ConsoleContextMetaBuilderContainer ()

				.structuralName (
					contextTypeName)

				.extensionPointName (
					contextTypeName + ":object");

			builder.descend (
				taskLogger,
				objectContainer,
				spec.objectChildren (),
				metaModule,
				MissingBuilderBehaviour.ignore);

		}

	}

	// defaults

	void setDefaults () {

		contextTypeName =
			spec.name ();

		componentName =
			ifNull (
				spec.componentName (),
				contextTypeName);

		if (componentName.contains ("_")) {

			throw new RuntimeException (
				stringFormat (
					"Object context type name %s cannot be used as bean name",
					contextTypeName));

		}

		hasListChildren =
			! spec.listChildren ().isEmpty ();

		hasObjectChildren =
			! spec.objectChildren ().isEmpty ();

		// context type names

		listContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextTypeName + ":list")

			.add (
				contextTypeName + ":combo")

			.build ();

		objectContextTypeNames =
			ImmutableList.<String>builder ()

			.add (
				contextTypeName + ":combo")

			.add (
				contextTypeName + ":object")

			.build ();

	}

}
