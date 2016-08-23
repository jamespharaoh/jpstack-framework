package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.entity.meta.ReferenceFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("referenceFieldWriter")
@ModelWriter
public
class ReferenceFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ReferenceFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		if (
			isNull (
				fieldTypePluginModel)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field type %s ",
					spec.typeName (),
					"does not exist while building reference field %s.%s",
					context.modelMeta ().name (),
					ifNull (
						spec.name (),
						spec.typeName ())));

		}

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()));

		// write field

		PropertyWriter propertyWriter =
			new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"%s",
				fullFieldTypeName)

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (),
					spec.typeName ()));

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
