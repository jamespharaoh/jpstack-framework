package wbs.framework.entity.generate.collections;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classNameFormat;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

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
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.collections.ChildrenListSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ClassName;

@PrototypeComponent ("childrenListWriter")
@ModelWriter
public
class ChildrenListWriter
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ChildrenListSpec spec;

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

			String fieldName =
				ifNull (
					spec.name (),
					naivePluralise (
						spec.typeName ()));

			PluginRecordModelSpec fieldTypePluginModel =
				mapItemForKeyOrThrow (
					pluginManager.pluginRecordModelsByName (),
					spec.typeName (),
					() -> new RuntimeException (
						stringFormat (
							"Field %s.%s has invalid type %s",
							context.recordClassName (),
							fieldName,
							spec.typeName ())));

			PluginSpec fieldTypePlugin =
				fieldTypePluginModel.plugin ();

			ClassName fullFieldTypeName =
				classNameFormat (
					"%s.model.%sRec",
					fieldTypePlugin.packageName (),
					hyphenToCamelCapitalise (
						spec.typeName ()));

			// write field

			new JavaPropertyWriter ()

				.thisClassNameFormat (
					"%s.model.%s",
					context.modelMeta ().plugin ().packageName (),
					context.recordClassName ())

				.typeName (
					imports ->
						stringFormat (
							"%s <%s>",
							imports.register (
								List.class),
							imports.register (
								fullFieldTypeName)))

				.propertyName (
					hyphenToCamel (
						fieldName))

				.defaultValue (
					imports ->
						stringFormat (
							"new %s <%s> ()",
							imports.register (
								ArrayList.class),
							imports.register (
								fullFieldTypeName)))

				.writeBlock (
					taskLogger,
					target.imports (),
					target.formatWriter ());

		}

	}

}
