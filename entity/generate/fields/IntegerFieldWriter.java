package wbs.framework.entity.generate.fields;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.IntegerFieldSpec;
import wbs.framework.utils.formatwriter.FormatWriter;

@PrototypeComponent ("integerFieldWriter")
@ModelWriter
public
class IntegerFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	IntegerFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		// write field

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"Long")

			.propertyNameFormat (
				"%s",
				spec.name ());

		if (spec.defaultValue () != null) {

			propertyWriter.defaultValueFormat (
				"%sl",
				Long.toString (
					spec.defaultValue ()));

		}

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
