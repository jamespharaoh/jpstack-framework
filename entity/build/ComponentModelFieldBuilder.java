package wbs.framework.entity.build;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.className;
import static wbs.utils.etc.TypeUtils.classNameFormat;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.hyphenToSpaces;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.meta.fields.ComponentFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ClassName;

@PrototypeComponent ("componentModelFieldBuilder")
@ModelBuilder
public
class ComponentModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelBuilderManager modelBuilderManager;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ComponentFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

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

			String fieldName =
				ifNull (
					spec.name (),
					spec.typeName ());

			ClassName fieldTypeName =
				className (
					hyphenToCamelCapitalise (
						spec.typeName ()));

			RecordSpec compositeModel =
				mapItemForKeyRequired (
					modelMetaLoader.compositeSpecs (),
					spec.typeName ());

			ClassName fullFieldTypeName =
				classNameFormat (
					"%s.model.%s",
					compositeModel.plugin ().packageName (),
					fieldTypeName);

			Class <?> fieldTypeClass =
				classForNameRequired (
					fullFieldTypeName);

			// create model field

			ModelField modelField =
				new ModelField ()

				.model (
					target.model ())

				.parentField (
					context.parentModelField ())

				.name (
					hyphenToCamel (
						fieldName))

				.label (
					hyphenToSpaces (
						fieldName))

				.type (
					ModelFieldType.component)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					fieldTypeClass)

				.nullable (
					false);

			// contained model field

			ModelFieldBuilderContext nextContext =
				new ModelFieldBuilderContext ()

				.modelMeta (
					context.modelMeta ())

				.modelClass (
					context.modelClass ())

				.parentModelField (
					modelField);

			ModelFieldBuilderTarget nextTarget =
				new ModelFieldBuilderTarget ()

				.model (
					target.model ())

				.fields (
					modelField.fields ())

				.fieldsByName (
					modelField.fieldsByName ());

			RecordSpec compositeMeta =
				modelMetaLoader.compositeSpecs ().get (
					spec.typeName ());

			if (
				isNull (
					compositeMeta)
			) {

				throw new RuntimeException ();

			}

			modelBuilderManager.build (
				taskLogger,
				nextContext,
				compositeModel.fields (),
				nextTarget);

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

		}

	}

}
