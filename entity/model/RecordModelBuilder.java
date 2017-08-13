package wbs.framework.entity.model;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrNull;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.classNameFormat;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIntern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginRecordModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.build.ModelBuilderManager;
import wbs.framework.entity.build.ModelFieldBuilderContext;
import wbs.framework.entity.build.ModelFieldBuilderTarget;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;

import wbs.utils.etc.ClassName;

@Accessors (fluent = true)
@PrototypeComponent ("recordModelBuilder")
public
class RecordModelBuilder <RecordType extends Record <RecordType>> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelBuilderManager modelBuilderManager;

	@SingletonDependency
	SchemaNamesHelper schemaNamesHelper;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	// properties

	@Getter @Setter
	RecordSpec recordSpec;

	// state

	PluginRecordModelSpec pluginModel;
	PluginSpec plugin;

	RecordModelImplementation <?> model;

	ClassName modelClassName;
	ClassName recordClassNameFull;
	Class <RecordType> modelClass;

	ClassName objectHelperClassName;
	ClassName objectHelperClassNameFull;
	Class <ObjectHelper <RecordType>> objectHelperClass;

	// implementation

	public
	Model <?> build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			plugin =
				recordSpec.plugin ();

			// record class

			modelClassName =
				classNameFormat (
					"%sRec",
					hyphenToCamelCapitalise (
						recordSpec.name ()));

			recordClassNameFull =
				classNameFormat (
					"%s.model.%s",
					plugin.packageName (),
					modelClassName);

			modelClass =
				genericCastUnchecked (
					classForNameRequired (
						recordClassNameFull));

			// object helper class

			objectHelperClassName =
				classNameFormat (
					"%sObjectHelper",
					hyphenToCamelCapitalise (
						recordSpec.name ()));

			objectHelperClassNameFull =
				classNameFormat (
					"%s.model.%s",
					plugin.packageName (),
					objectHelperClassName);

			Class <ObjectHelper <RecordType>> objectHelperClassTemp =
				genericCastUnchecked (
					classForNameRequired (
						objectHelperClassNameFull));

			objectHelperClass =
				objectHelperClassTemp;

			// model

			model =
				new RecordModelImplementation <RecordType> ()

				.objectClass (
					modelClass)

				.objectClassName (
					stringFormat (
						classNameSimple (
							modelClass)))

				.oldObjectName (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							recordSpec.oldName ()),
						stringIntern ()))

				.objectTypeCode (
					stringIntern (
						hyphenToUnderscore (
							ifNull (
								recordSpec.oldName (),
								recordSpec.name ()))))

				.objectTypeCamel (
					stringIntern (
						hyphenToCamel (
							recordSpec.name ())))

				.objectTypeHyphen (
					stringIntern (
						recordSpec.name ()))

				.friendlyNameSingular (
					stringIntern (
						recordSpec.friendlyNameSingular ()))

				.friendlyNamePlural (
					stringIntern (
						recordSpec.friendlyNamePlural ()))

				.shortNameSingular (
					stringIntern (
						recordSpec.shortNameSingular ()))

				.shortNamePlural (
					stringIntern (
						recordSpec.shortNamePlural ()))

				.tableName (
					stringIntern (
						ifNull (
							recordSpec.tableName (),
							schemaNamesHelper.tableName (
								modelClass))))

				.create (
					ifNull (
						recordSpec.create (),
						true))

				.mutable (
					ifNull (
						recordSpec.mutable (),
						true))

				.helperClass (
					objectHelperClass)

				.cachedView (
					recordSpec.cachedView ())

			;

			// model fields

			ModelFieldBuilderContext context =
				new ModelFieldBuilderContext ()

				.modelMeta (
					recordSpec)

				.modelClass (
					modelClass);

			ModelFieldBuilderTarget target =
				new ModelFieldBuilderTarget ()

				.model (
					model)

				.fields (
					model.fields ())

				.fieldsByName (
					model.fieldsByName ());

			modelBuilderManager.build (
				taskLogger,
				context,
				recordSpec.fields (),
				target);

			modelBuilderManager.build (
				taskLogger,
				context,
				recordSpec.collections (),
				target);

			// and return

			return model;

		}

	}

	public
	static Annotation findAnnotation (
			Annotation[] annotations,
			Class<? extends Annotation> metaAnnotation) {

		for (Annotation annotation
				: annotations) {

			if (annotation.annotationType ()
					.isAnnotationPresent (metaAnnotation))
				return annotation;

		}

		return null;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation.
	 */
	public
	Object annotationParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			Object defaultValue) {

		for (Method method
				: annotation.annotationType ().getMethods ()) {

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				return method.invoke (annotation);

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

	/**
	 * Retrieves the value of the annotation parameter which is annotated with
	 * the specified meta-annotation. Specifically for string values, this will
	 * return a default value if the meta annotation is not present or if the
	 * value is the empty string.
	 */
	public
	String annotationStringParam (
			Annotation annotation,
			Class<? extends Annotation> metaAnnotation,
			String defaultValue) {

		for (
			Method method
				: annotation.annotationType ().getMethods ()
		) {

			if (method.getReturnType () != String.class)
				continue;

			if (! method.isAnnotationPresent (metaAnnotation))
				continue;

			try {

				String value =
					(String)
					method.invoke (annotation);

				if (value.isEmpty ())
					return defaultValue;

				return value;

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

		}

		return defaultValue;

	}

}
