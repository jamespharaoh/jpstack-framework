package wbs.framework.schema.builder;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.helper.SchemaNamesHelper;
import wbs.framework.schema.helper.SchemaTypesHelper;
import wbs.framework.schema.model.SchemaColumn;
import wbs.framework.schema.model.SchemaForeignKey;
import wbs.framework.schema.model.SchemaIndex;
import wbs.framework.schema.model.SchemaPrimaryKey;
import wbs.framework.schema.model.SchemaSequence;
import wbs.framework.schema.model.SchemaTable;
import wbs.framework.sql.SqlLogicImplementation;

@Accessors (fluent = true)
@PrototypeComponent ("prototype")
public
class SchemaTableFromModel {

	// singleton dependencies

	@SingletonDependency
	SchemaNamesHelper schemaNamesHelper;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	@SingletonDependency
	SqlLogicImplementation sqlLogic;

	// properties

	@Getter @Setter
	TaskLogger taskLog;

	@Getter @Setter
	Map <Class <?>, Model> modelsByClass;

	@Getter @Setter
	Model model;

	// state

	SchemaTable schemaTable;

	String parentColumn;
	String parentTypeColumn;
	String parentIdColumn;
	String typeCodeColumn;
	String codeColumn;
	String indexColumn;

	List <String> parentColumnNames =
		new ArrayList<> ();

	List <String> identityColumnNames =
		new ArrayList<> ();

	// implementation

	public
	SchemaTable build () {

		if (! model.create ())
			return null;

		schemaTable =
			new SchemaTable ()

			.name (
				model.tableName ());

		for (
			ModelField modelField
				: model.fields ()
		) {

			forModelField (
				modelField);

		}

		if (! identityColumnNames.isEmpty ()) {

			List<String> treeColumnNames =
				ImmutableList.<String>builder ()

				.addAll (
					parentColumnNames)

				.addAll (
					identityColumnNames)

				.build ();

			String treeIndexName =
				stringFormat (
					"%s_tree_key",
					model.tableName ());

			schemaTable.addIndex (
				new SchemaIndex ()

				.name (
					treeIndexName)

				.unique (
					true)

				.columns (
					treeColumnNames)

			);

		}

		if (schemaTable.primaryKey () == null
				|| schemaTable.primaryKey ().columns ().isEmpty ()) {

			taskLog.errorFormat (
				"No primary key for %s",
				model.objectName ());

			return null;

		}

		return schemaTable;

	}

	public
	void forModelField (
			@NonNull ModelField modelField) {

		switch (modelField.type ()) {

		case generatedId:

			forGeneratedId (
				modelField);

			break;

		case assignedId:

			forAssignedId (
				modelField);

			break;

		case foreignId:

			forForeignId (
				modelField);

			break;

		case simple:
		case name:
		case description:
		case deleted:
		case typeCode:
		case code:
		case index:
		case parentId:
		case identitySimple:

			forField (
				modelField);

			break;

		case reference:
		case parent:
		case grandParent:
		case greatGrandParent:
		case parentType:
		case type:
		case identityReference:

			forReference (
				modelField);

			break;

		case compositeId:

			forCompositeId (
				modelField);

			break;

		case component:

			forComponent (
				modelField);

			break;

		case collection:
		case associative:
		case master:
		case slave:

			doNothing ();

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Unrecognised type %s for model field %s",
					modelField.type (),
					modelField.fullName ()));

		}

		if (modelField.parent ()) {

			parentColumnNames.addAll (
				modelField.columnNames ());

		}

		if (modelField.identity ()) {

			identityColumnNames.addAll (
				modelField.columnNames ());

		}

	}

	void forGeneratedId (
			@NonNull ModelField modelField) {

		String sequenceName =
			stringFormat (
				"%s_id_seq",
				schemaTable.name ());

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					modelField.columnName ())

				.type (
					"integer")

				.nullable (
					false))

			.addSequence (
				new SchemaSequence ()

				.name (
					sequenceName))

			.primaryKey (
				new SchemaPrimaryKey ()

				.addColumn (
					modelField.columnName ()));

	}

	void forAssignedId (
			@NonNull ModelField modelField) {

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					modelField.columnName ())

				.type (
					"integer")

				.nullable (
					false))

			.primaryKey (
				new SchemaPrimaryKey ()

				.addColumn (
					modelField.columnName ()));

	}

	void forForeignId (
			@NonNull ModelField modelField) {

		ModelField foreignModelField =
			model.fieldsByName ().get (
				modelField.foreignFieldName ());

		if (foreignModelField == null) {

			taskLog.errorFormat (
				"Foreign model field %s doesn't exist",
				modelField.fullName ());

			return;

		}

		Model targetModel =
			modelsByClass.get (
				foreignModelField.valueType ());

		if (targetModel == null) {

			taskLog.errorFormat (
				"Can't find model for type %s specified by %s",
				classNameSimple (
					modelField.valueType ()),
				modelField.fullName ());

			return;

		}

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					modelField.columnName ())

				.type (
					"integer")

				.nullable (
					false))

			.primaryKey (
				new SchemaPrimaryKey ()

				.addColumn (
					modelField.columnName ()))

			.addForeignKey (
				new SchemaForeignKey ()

				.addSourceColumn (
					modelField.columnName ())

				.targetTable (
					targetModel.tableName ()));

	}

	void forField (
			@NonNull ModelField modelField) {

		List<String> typeNames;

		if (modelField.sqlType () != null) {

			typeNames =
				Collections.singletonList (
					modelField.sqlType ());

		} else {

			typeNames =
				schemaTypesHelper.fieldTypeNames ().get (
					modelField.valueType ());

			if (typeNames == null) {

				taskLog.errorFormat (
					"Can't map type %s for %s",
					modelField.valueType ().getSimpleName (),
					modelField.fullName ());

				return;

			}

		}

		if (
			integerNotEqualSafe (
				collectionSize (
					modelField.columnNames ()),
				collectionSize (
					typeNames))
		) {

			taskLog.errorFormat (
				"Expected %s columns for %s at %s, not %s",
				integerToDecimalString (
					typeNames.size ()),
				classNameSimple (
					modelField.valueType ()),
				modelField.fullName (),
				integerToDecimalString (
					modelField.columnNames ().size ()));

			return;

		}

		for (
			int column = 0;
			column < modelField.columnNames ().size ();
			column ++
		) {

			String columnName =
				modelField.columnNames ().get (column);

			String typeName =
				typeNames.get (column);

			schemaTable

				.addColumn (
					new SchemaColumn ()

					.name (
						columnName)

					.type (
						typeName)

					.nullable (
						modelField.nullable ()));

		}

	}

	void forReference (
			@NonNull ModelField modelField) {

		Model targetModel =
			modelsByClass.get (
				modelField.valueType ());

		if (targetModel == null) {

			taskLog.errorFormat (
				"Can't find model for type %s specified by %s",
				classNameSimple (
					modelField.valueType ()),
				modelField.fullName ());

			return;

		}

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					modelField.columnName ())

				.type (
					"integer")

				.nullable (
					modelField.nullable ()))

			.addForeignKey (
				new SchemaForeignKey ()

				.addSourceColumn (
					modelField.columnName ())

				.targetTable (
					targetModel.tableName ()));

	}

	void forCompositeId (
			ModelField modelField) {

		if (schemaTable.primaryKey () != null) {

			taskLog.errorFormat (
				"More than one primary key for %s",
				model.objectName ());

			return;

		}

		schemaTable.primaryKey (
			new SchemaPrimaryKey ());

		for (
			ModelField compositeIdModelField
				: modelField.fields ()
		) {

			forCompositeIdField (
				modelField,
				compositeIdModelField);

		}

	}

	void forCompositeIdField (
			@NonNull ModelField modelField,
			@NonNull ModelField compositeIdModelField) {

		if (compositeIdModelField.reference ()) {

			forCompositeIdReference (
				modelField,
				compositeIdModelField);

		} else if (compositeIdModelField.value ()) {

			forCompositeIdValue (
				modelField,
				compositeIdModelField);

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map field of type %s at %s",
					compositeIdModelField.type (),
					compositeIdModelField.fullName ()));

		}

	}

	void forCompositeIdReference (
			@NonNull ModelField modelField,
			@NonNull ModelField compositeIdModelField) {

		Model targetModel =
			modelsByClass.get (
				compositeIdModelField.valueType ());

		if (targetModel == null) {

			taskLog.errorFormat (
				"Can't find model for type %s specified by %s",
				compositeIdModelField.valueType ().getSimpleName (),
				compositeIdModelField.fullName ());

			return;

		}

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					compositeIdModelField.columnName ())

				.type (
					"integer")

				.nullable (
					false))

			.addForeignKey (
				new SchemaForeignKey ()

				.addSourceColumn (
					compositeIdModelField.columnName ())

				.targetTable (
					targetModel.tableName ()));

		schemaTable.primaryKey ()

			.addColumn (
				compositeIdModelField.columnName ());

	}

	void forCompositeIdValue (
			@NonNull ModelField modelField,
			@NonNull ModelField compositeIdModelField) {

		List<String> typeNames =
			schemaTypesHelper.fieldTypeNames ().get (
				compositeIdModelField.valueType ());

		if (typeNames == null)
			throw new RuntimeException ();

		if (typeNames.size () != 1)
			throw new RuntimeException ();

		schemaTable

			.addColumn (
				new SchemaColumn ()
					.name (compositeIdModelField.columnName ())
					.type (typeNames.get (0))
					.nullable (false));

		schemaTable.primaryKey ()

			.addColumn (
				compositeIdModelField.columnName ());

	}

	void forComponent (
			@NonNull ModelField modelField) {

		for (
			ModelField componentModelField
				: modelField.fields ()
		) {

			if (componentModelField.value ()) {

				forComponentField (
					modelField,
					componentModelField);

			} else if (componentModelField.reference ()) {

				forComponentReference (
					modelField,
					componentModelField);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map field of type %s at %s",
						componentModelField.type (),
						componentModelField.fullName ()));

			}

		}

	}

	void forComponentField (
			@NonNull ModelField modelField,
			@NonNull ModelField componentModelField) {

		List<String> typeNames =
			schemaTypesHelper.fieldTypeNames ().get (
				componentModelField.valueType ());

		if (typeNames == null) {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s at %s",
					componentModelField.valueType (),
					componentModelField.fullName ()));

		}

		if (typeNames.size () != 1) {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s at %s",
					componentModelField.valueType (),
					componentModelField.fullName ()));

		}

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					componentModelField.columnName ())

				.type (
					typeNames.get (0))

				.nullable (
					componentModelField.nullable ()));

	}

	void forComponentReference (
			@NonNull ModelField modelField,
			@NonNull ModelField componentModelField) {

		Model targetModel =
			modelsByClass.get (
				componentModelField.valueType ());

		if (targetModel == null) {

			taskLog.errorFormat (
				"Can't find model for type %s specified by %s",
				classNameSimple (
					componentModelField.valueType ()),
				componentModelField.fullName ());

			return;

		}

		schemaTable

			.addColumn (
				new SchemaColumn ()

				.name (
					componentModelField.columnName ())

				.type (
					"integer")

				.nullable (
					false))

			.addForeignKey (
				new SchemaForeignKey ()

				.addSourceColumn (
					componentModelField.columnName ())

				.targetTable (
					targetModel.tableName ()));

	}

}
