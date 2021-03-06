package wbs.framework.entity.build;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.classNameFormat;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

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
import wbs.framework.component.scaffold.PluginCustomTypeSpec;
import wbs.framework.component.scaffold.PluginEnumTypeSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.identities.IdentityEnumFieldSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.helper.SchemaTypesHelper;

import wbs.utils.etc.ClassName;

@PrototypeComponent ("identityEnumModelFieldBuilder")
@ModelBuilder
public
class IdentityEnumModelFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	IdentityEnumFieldSpec spec;

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

			String fieldTypePackageName;

			PluginEnumTypeSpec fieldTypePluginEnumType =
				pluginManager.pluginEnumTypesByName ().get (
					spec.typeName ());

			PluginCustomTypeSpec fieldTypePluginCustomType =
				pluginManager.pluginCustomTypesByName ().get (
					spec.typeName ());

			if (fieldTypePluginEnumType != null) {

				PluginSpec fieldTypePlugin =
					fieldTypePluginEnumType.plugin ();

				fieldTypePackageName =
					fieldTypePlugin.packageName ();

			} else if (fieldTypePluginCustomType != null) {

				PluginSpec fieldTypePlugin =
					fieldTypePluginCustomType.plugin ();

				fieldTypePackageName =
					fieldTypePlugin.packageName ();

			} else {

				throw new RuntimeException (
					stringFormat (
						"No such enum or custom type: %s",
						spec.typeName ()));

			}

			String fieldName =
				spec.name ();

			ClassName fullFieldTypeName =
				classNameFormat (
					"%s.model.%s",
					fieldTypePackageName,
					hyphenToCamelCapitalise (
						spec.typeName ()));

			Class <?> valueType =
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
					ModelFieldType.identitySimple)

				.parent (
					false)

				.identity (
					true)

				.valueType (
					valueType)

				.nullable (
					false)

				.columnNames (
					singletonList (
						ifNull (
							spec.columnName (),
							hyphenToUnderscore (
								fieldName))))

				.columnSqlTypes (
					mapItemForKeyRequired (
						schemaTypesHelper.fieldTypeNames (),
						valueType))

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
