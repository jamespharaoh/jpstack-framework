package wbs.framework.entity.build;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.meta.identities.CodeFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("codeModelFieldBuilder")
@ModelBuilder
public
class CodeModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	CodeFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@Override
	@BuildMethod
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
					"code");

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
					ModelFieldType.code)

				.parent (
					false)

				.identity (
					true)

				.valueType (
					String.class)

				.nullable (
					false)

				.cacheable (
					ifNull (
						spec.cacheable (),
						false))

				.columnNames (
					singletonList (
						ifNull (
							spec.columnName (),
							hyphenToUnderscore (
								fieldName))))

				.columnSqlTypes (
					singletonList (
						"text"))

			;

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

			if (target.model ().codeField () != null)
				throw new RuntimeException ();

			target.model ().codeField (
				modelField);

		}

	}

}
