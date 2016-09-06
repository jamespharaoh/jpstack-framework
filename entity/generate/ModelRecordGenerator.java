package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.FileUtils.directoryCreateWithParents;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaClassWriter;
import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.CodeFieldSpec;
import wbs.framework.entity.meta.DateFieldSpec;
import wbs.framework.entity.meta.IdentityIntegerFieldSpec;
import wbs.framework.entity.meta.IdentityReferenceFieldSpec;
import wbs.framework.entity.meta.IdentityStringFieldSpec;
import wbs.framework.entity.meta.IndexFieldSpec;
import wbs.framework.entity.meta.IntegerFieldSpec;
import wbs.framework.entity.meta.MasterFieldSpec;
import wbs.framework.entity.meta.ModelFieldSpec;
import wbs.framework.entity.meta.ModelImplementsInterfaceSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ModelMetaType;
import wbs.framework.entity.meta.ParentFieldSpec;
import wbs.framework.entity.meta.ParentIdFieldSpec;
import wbs.framework.entity.meta.ParentTypeFieldSpec;
import wbs.framework.entity.meta.ReferenceFieldSpec;
import wbs.framework.entity.meta.TimestampFieldSpec;
import wbs.framework.entity.meta.TypeCodeFieldSpec;
import wbs.framework.entity.record.CommonRecord;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.EventRecord;
import wbs.framework.entity.record.MajorRecord;
import wbs.framework.entity.record.MinorRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.RecordComponent;
import wbs.framework.entity.record.RootRecord;
import wbs.framework.entity.record.TypeRecord;
import wbs.framework.utils.formatwriter.AtomicFileWriter;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("modelRecordGenerator")
public
class ModelRecordGenerator {

	// singleton dependencies

	@SingletonDependency
	ModelWriterManager modelWriterBuilder;

	// properties

	@Getter @Setter
	PluginSpec plugin;

	@Getter @Setter
	ModelMetaSpec modelMeta;

	// state

	String recordClassName;

	// implementation

	public
	void generateRecord () {

		if (modelMeta.type ().record ()) {

			recordClassName =
				stringFormat (
					"%sRec",
					capitalise (
						modelMeta.name ()));

		} else if (modelMeta.type ().component ()) {

			recordClassName =
				capitalise (
					modelMeta.name ());

		} else {

			throw new RuntimeException ();

		}

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/model",
				plugin.packageName ().replace ('.', '/'));

		directoryCreateWithParents (
			directory);

		// write interface

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				recordClassName);

		@Cleanup
		FormatWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			new JavaClassUnitWriter ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.model",
				plugin.packageName ());

		JavaClassWriter modelWriter =
			new JavaClassWriter ()

			.className (
				recordClassName)

			.addClassModifier (
				"public")

			.addImplements (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							modelInterfacesByType.get (
								modelMeta.type ())),
						imports.registerFormat (
							"%s.model.%s",
							modelMeta.plugin ().packageName (),
							recordClassName)));

		for (
			ModelImplementsInterfaceSpec implementsInterface
				: modelMeta.implementsInterfaces ()
		) {

			modelWriter.addImplementsFormat (
				"%s.%s",
				implementsInterface.packageName (),
				implementsInterface.name ());

		}

		modelWriter

			.addBlock (
				this::writeConstructor)

			.addBlock (
				this::writeFields)

			.addBlock (
				this::writeCollections)

			.addBlock (
				this::writeEquals);

		if (modelMeta.type ().record ()) {

			modelWriter

				.addBlock (
					this::writeCompareTo)

				.addBlock (
					this::writeToString);

		}

		classUnitWriter.addBlock (
			modelWriter);

		classUnitWriter.write ();

	}

	public final static
	Map <ModelMetaType, Class <?>> modelInterfacesByType =
		ImmutableMap.<ModelMetaType, Class <?>> builder ()

		.put (
			ModelMetaType.common,
			CommonRecord.class)

		.put (
			ModelMetaType.component,
			RecordComponent.class)

		.put (
			ModelMetaType.ephemeral,
			EphemeralRecord.class)

		.put (
			ModelMetaType.event,
			EventRecord.class)

		.put (
			ModelMetaType.major,
			MajorRecord.class)

		.put (
			ModelMetaType.minor,
			MinorRecord.class)

		.put (
			ModelMetaType.root,
			RootRecord.class)

		.put (
			ModelMetaType.type,
			TypeRecord.class)

		.build ();

	public
	void writeConstructor (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// comment

		formatWriter.writeLineFormat (
			"// constructor");

		formatWriter.writeNewline ();

		// open function

		if (modelMeta.type ().record ()) {

			formatWriter.writeLineFormat (
				"@Deprecated");

		}

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"%s () {",
			recordClassName);

		formatWriter.writeNewline ();

		// close function

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	private
	void writeFields (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (modelMeta.fields ().isEmpty ()) {
			return;
		}

		formatWriter.writeLineFormat (
			"// fields");

		formatWriter.writeNewline ();

		ModelFieldWriterContext nextContext =
			new ModelFieldWriterContext ()

			.modelMeta (
				modelMeta)

			.recordClassName (
				recordClassName);

		ModelFieldWriterTarget nextTarget =
			new ModelFieldWriterTarget ()

			.imports (
				imports)

			.formatWriter (
				formatWriter);

		modelWriterBuilder.write (
			nextContext,
			modelMeta.fields (),
			nextTarget);

	}

	private
	void writeCollections (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (modelMeta.collections ().isEmpty ()) {
			return;
		}

		formatWriter.writeLineFormat (
			"// collections");

		formatWriter.writeNewline ();

		ModelFieldWriterContext nextContext =
			new ModelFieldWriterContext ()

			.modelMeta (
				modelMeta)

			.recordClassName (
				recordClassName);

		ModelFieldWriterTarget nextTarget =
			new ModelFieldWriterTarget ()

			.imports (
				imports)

			.formatWriter (
				formatWriter);

		modelWriterBuilder.write (
			nextContext,
			modelMeta.collections (),
			nextTarget);

	}

	private
	void writeEquals (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write comment

		formatWriter.writeLineFormat (
			"// equals");

		formatWriter.writeNewline ();

		// write override annotation

		formatWriter.writeLineFormat (
			"@Override");

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"boolean equals (");

		formatWriter.writeLineFormat (
			"\t\t%s otherObject) {",
			imports.register (
				Object.class));

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write class comparison

		formatWriter.writeLineFormat (
			"if (! (otherObject instanceof %s)) {",
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeLineFormat (
			"\treturn false;");

		formatWriter.writeLineFormat (
			"}\n");

		formatWriter.writeNewline ();

		// write cast

		formatWriter.writeLineFormat (
			"%s other =",
			recordClassName);

		formatWriter.writeLineFormat (
			"\t(%s)",
			recordClassName);

		formatWriter.writeLineFormat (
			"\totherObject;");

		formatWriter.writeNewline ();

		if (modelMeta.type ().record ()) {

			// check for null id

			formatWriter.writeLineFormat (
				"if (getId () == null || other.getId () == null) {");

			formatWriter.writeLineFormat (
				"\treturn false;");

			formatWriter.writeLineFormat (
				"}");

			formatWriter.writeNewline ();

			// compare id

			formatWriter.writeLineFormat (
				"return getId () == other.getId ();");

			formatWriter.writeNewline ();

		} else if (modelMeta.type ().component ()) {

			// TODO this is not nice at all

			for (
				ModelFieldSpec modelField
					: modelMeta.fields ()
			) {

				String fieldName;

				if (modelField instanceof ReferenceFieldSpec) {

					ReferenceFieldSpec referenceField =
						(ReferenceFieldSpec)
						modelField;

					fieldName =
						ifNull (
							referenceField.name (),
							referenceField.typeName ());

				} else if (modelField instanceof DateFieldSpec) {

					DateFieldSpec dateField =
						(DateFieldSpec)
						modelField;

					fieldName =
						dateField.name ();

				} else if (modelField instanceof IntegerFieldSpec) {

					IntegerFieldSpec integerField =
						(IntegerFieldSpec)
						modelField;

					fieldName =
						integerField.name ();

				} else {

					throw new RuntimeException (
						stringFormat (
							"Don't know how to get name of component field ",
							"type %s",
							modelField.getClass ().getSimpleName ()));

				}

				formatWriter.writeLineFormat (
					"if (");

				formatWriter.writeLineFormat (
					"\t! (");

				formatWriter.writeLineFormat (
					"\t\tget%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t&& other.get%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t) && (");

				formatWriter.writeLineFormat (
					"\t\tget%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t|| other.get%s () == null",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t|| ! get%s ().equals (",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t\t\tother.get%s ())",
					capitalise (
						fieldName));

				formatWriter.writeLineFormat (
					"\t)");

				formatWriter.writeLineFormat (
					") {");

				formatWriter.writeLineFormat (
					"\treturn false;");

				formatWriter.writeLineFormat (
					"}");

				formatWriter.writeNewline ();

			}

			formatWriter.writeLineFormat (
				"return true;");

			formatWriter.writeNewline ();

		} else {

			throw new RuntimeException ();

		}

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	void writeCompareTo (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// TODO generate better code

		// write comment

		formatWriter.writeLineFormat (
			"// compare to");

		formatWriter.writeNewline ();

		// write override annotation

		formatWriter.writeLineFormat (
			"@Override");

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"int compareTo (");

		formatWriter.writeLineFormat (
			"\t\t%s <%s> otherRecord) {",
			imports.register (
				Record.class),
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write cast to concrete type

		formatWriter.writeLineFormat (
			"%s other =",
			recordClassName);

		formatWriter.writeLineFormat (
			"\t(%s) otherRecord;",
			imports.registerFormat (
				"%s.model.%s",
				modelMeta.plugin ().packageName (),
				recordClassName));

		formatWriter.writeNewline ();

		// create compare to builder

		formatWriter.writeLineFormat (
			"return new %s ()",
			imports.register (
				CompareToBuilder.class));

		formatWriter.writeNewline ();

		// scan fields

		ParentFieldSpec parentField = null;
		ParentTypeFieldSpec parentTypeField = null;
		ParentIdFieldSpec parentIdField = null;
		MasterFieldSpec masterField = null;

		TypeCodeFieldSpec typeCodeField = null;
		CodeFieldSpec codeField = null;
		IndexFieldSpec indexField = null;

		List <IdentityReferenceFieldSpec> identityReferenceFields =
			new ArrayList<> ();

		List <IdentityIntegerFieldSpec> identityIntegerFields =
			new ArrayList<> ();

		List <IdentityStringFieldSpec> identityStringFields =
			new ArrayList<> ();

		TimestampFieldSpec timestampField = null;

		boolean gotName = false;

		for (
			ModelFieldSpec modelField
				: modelMeta.fields ()
		) {

			if (modelField instanceof ParentFieldSpec) {

				parentField =
					(ParentFieldSpec)
					modelField;

			}

			if (modelField instanceof ParentTypeFieldSpec) {

				parentTypeField =
					(ParentTypeFieldSpec)
					modelField;

			}

			if (modelField instanceof ParentIdFieldSpec) {

				parentIdField =
					(ParentIdFieldSpec)
					modelField;

			}

			if (modelField instanceof MasterFieldSpec) {

				masterField =
					(MasterFieldSpec)
					modelField;

			}

			if (modelField instanceof TypeCodeFieldSpec) {

				typeCodeField =
					(TypeCodeFieldSpec)
					modelField;

			}

			if (modelField instanceof CodeFieldSpec) {

				codeField =
					(CodeFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IndexFieldSpec) {

				indexField =
					(IndexFieldSpec)
					modelField;

				gotName = true;

			}

			if (modelField instanceof IdentityReferenceFieldSpec) {

				identityReferenceFields.add (
					(IdentityReferenceFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityIntegerFieldSpec) {

				identityIntegerFields.add (
					(IdentityIntegerFieldSpec)
					modelField);

				gotName = true;

			}

			if (modelField instanceof IdentityStringFieldSpec) {

				identityStringFields.add (
					(IdentityStringFieldSpec)
					modelField);

				gotName = true;

			}

			if (
				modelField instanceof TimestampFieldSpec
				&& timestampField == null
			) {

				timestampField =
					(TimestampFieldSpec)
					modelField;

			}

		}

		// write comparisons

		if (modelMeta.type () == ModelMetaType.event) {

			if (timestampField == null) {
				throw new RuntimeException ();
			}

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.get%s (),",
				capitalise (
					timestampField.name ()));

			formatWriter.writeLineFormat (
				"\t\tget%s ())",
				capitalise (
					timestampField.name ()));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.getId (),");

			formatWriter.writeLineFormat (
				"\t\tgetId ())");

			formatWriter.writeNewline ();

		} else if (gotName || masterField != null) {

			if (parentField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							parentField.name (),
							parentField.typeName ())));

				formatWriter.writeNewline ();

			}

			if (parentTypeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							parentTypeField.name (),
							"parentType")));

				formatWriter.writeNewline ();

			}

			if (parentIdField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tgetParentId (),");

				formatWriter.writeLineFormat (
					"\t\tother.getParentId ())");

				formatWriter.writeNewline ();

			}

			if (masterField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							masterField.name (),
							masterField.typeName ())));

				formatWriter.writeNewline ();

			}

			if (typeCodeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							typeCodeField.name (),
							"type")));

				formatWriter.writeNewline ();

			}

			if (codeField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							codeField.name (),
							"code")));

				formatWriter.writeNewline ();

			}

			if (indexField != null) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							indexField.name (),
							"index")));

				formatWriter.writeNewline ();

			}

			for (
				IdentityReferenceFieldSpec identityReferenceField
					: identityReferenceFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						ifNull (
							identityReferenceField.name (),
							identityReferenceField.typeName ())));

				formatWriter.writeNewline ();

			}

			for (
				IdentityIntegerFieldSpec identityIntegerField
					: identityIntegerFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						identityIntegerField.name ()));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						identityIntegerField.name ()));

				formatWriter.writeNewline ();

			}

			for (
				IdentityStringFieldSpec identityStringField
					: identityStringFields
			) {

				formatWriter.writeLineFormat (
					"\t.append (");

				formatWriter.writeLineFormat (
					"\t\tget%s (),",
					capitalise (
						identityStringField.name ()));

				formatWriter.writeLineFormat (
					"\t\tother.get%s ())",
					capitalise (
						identityStringField.name ()));

				formatWriter.writeNewline ();

			}

		} else {

			formatWriter.writeLineFormat (
				"\t.append (");

			formatWriter.writeLineFormat (
				"\t\tother.getId (),");

			formatWriter.writeLineFormat (
				"\t\tgetId ())");

			formatWriter.writeNewline ();

		}

		// write converstion to return value

		formatWriter.writeLineFormat (
			"\t.toComparison ();");

		formatWriter.writeNewline ();

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	public
	void writeToString (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write comment

		formatWriter.writeLineFormat (
			"// to string");

		formatWriter.writeNewline ();

		// write function definition

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"String toString () {");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// write function body

		if (
			stringEqualSafe (
				modelMeta.name (),
				"text")
		) {

			formatWriter.writeLineFormat (
				"return getText ();");

		} else {

			formatWriter.writeLineFormat (
				"return \"%s(id=\" + getId () + \")\";",
				recordClassName);

		}

		formatWriter.writeNewline ();

		// write end of function

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

}
