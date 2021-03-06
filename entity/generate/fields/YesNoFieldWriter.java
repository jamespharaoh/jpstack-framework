package wbs.framework.entity.generate.fields;

import static wbs.utils.string.StringUtils.hyphenToCamel;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.generate.ModelRecordGenerator;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.fields.YesNoFieldSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("yesNoFieldWriter")
@ModelWriter
public
class YesNoFieldWriter
	implements BuilderComponent {

	// singleton dependency

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	YesNoFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

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

			// write field

			JavaPropertyWriter propertyWriter =
				new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeClass (
					Boolean.class)

				.propertyName (
					hyphenToCamel (
						spec.name ()))

				.setUpdatedFieldName (
					ModelRecordGenerator.recordUpdatedFieldName)

			;

			if (spec.defaultValue () != null) {

				propertyWriter.defaultValueFormat (
					"%s",
					spec.defaultValue ()
						? "true"
						: "false");

			}

			propertyWriter.writeBlock (
				taskLogger,
				target.imports (),
				target.formatWriter ());

		}

	}

}
