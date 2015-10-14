package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

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
import wbs.framework.entity.meta.AnnotationWriter;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("parentFieldWriter")
@ModelWriter
public
class ParentFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ParentFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fieldName =
			ifNull (
				spec.name (),
				spec.typeName ());

		// write field annotation

		AnnotationWriter annotationWriter =
			new AnnotationWriter ()

			.name (
				"ParentField");

		if (spec.columnName () != null) {

			annotationWriter.addAttributeFormat (
				"column",
				"\"%s\"",
				spec.columnName ().replace ("\"", "\\\""));

		}

		annotationWriter.write (
			javaWriter,
			"\t");

		// write field

		javaWriter.write (
			"\t%s.model.%sRec %s;\n",
			fieldTypePlugin.packageName (),
			capitalise (spec.typeName ()),
			fieldName);

		javaWriter.write (
			"\n");

	}

}
