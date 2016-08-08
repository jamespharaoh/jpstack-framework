package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;

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
import wbs.framework.entity.meta.ChildrenMappingSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("childrenMappingWriter")
@ModelWriter
public
class ChildrenMappingWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	@Inject
	ModelMetaLoader modelMetaLoader;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldName =
			ifNull (
				spec.name (),
				naivePluralise (
					spec.typeName ()));

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		if (
			isNull (
				fieldTypePluginModel)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field '%s.%s' ",
					context.modelMeta ().name (),
					fieldName,
					"has type '%s' ",
					spec.typeName (),
					"which does not exist"));


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

		new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"Map<%s,%s>",
				capitalise (
					spec.mapType ()),
				fullFieldTypeName)

			.propertyNameFormat (
				"%s",
				fieldName)

			.defaultValueFormat (
				"new LinkedHashMap<%s,%s> ()",
				capitalise (
					spec.mapType ()),
				fullFieldTypeName)

			.write (
				javaWriter,
				"\t");


	}

}
