package wbs.framework.entity.build;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToSpaces;

import com.google.common.collect.ImmutableList;

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
import wbs.framework.entity.meta.fields.BinaryFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.helper.SchemaNamesHelper;

@PrototypeComponent ("BinaryModelFieldBuilder")
@ModelBuilder
public
class BinaryModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SchemaNamesHelper schemaNamesHelper;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	BinaryFieldSpec spec;

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
				spec.name ();

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
					ModelFieldType.simple)

				.parent (
					false)

				.identity (
					false)

				.valueType (
					byte[].class)

				.nullable (
					ifNull (
						spec.nullable (),
						false))

				.columnNames (
					singletonList (
						ifNull (
							spec.columnName (),
							schemaNamesHelper.columnName (
								hyphenToCamel (
									fieldName)))))

				.columnSqlTypes (
					ImmutableList.of (
						"bytea"))

			;

			// store field

			target.fields ().add (
				modelField);

			target.fieldsByName ().put (
				modelField.name (),
				modelField);

		}

	}

}
