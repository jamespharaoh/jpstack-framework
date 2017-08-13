package wbs.framework.entity.model;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataParent;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@ToString (of = { "name" })
@DataClass
public
class ModelField {

	@DataAncestor
	@Getter @Setter
	Model <?> model;

	@DataParent
	@Getter @Setter
	ModelField parentField;

	@DataName
	@Getter
	String name;

	@DataAttribute
	@Getter @Setter
	String label;

	@DataAttribute
	@Getter @Setter
	ModelFieldType type;

	@DataAttribute
	@Getter @Setter
	boolean parent;

	@DataAttribute
	@Getter @Setter
	boolean identity;

	@DataChildren
	@Getter @Setter
	List <String> columnNames =
		new ArrayList<> ();

	@DataAttribute
	@Getter @Setter
	List <String> columnSqlTypes =
		new ArrayList<> ();

	@DataAttribute
	@Getter @Setter
	Class <?> valueType;

	@DataAttribute
	@Getter @Setter
	ParameterizedType parameterizedType;

	@DataAttribute
	@Getter @Setter
	Class <?> collectionKeyType;

	@DataAttribute
	@Getter @Setter
	Class <?> collectionValueType;

	@DataAttribute
	@Getter @Setter
	Class <?> hibernateTypeHelper;

	@DataAttribute
	@Getter @Setter
	Boolean nullable;

	@DataAttribute
	@Getter @Setter
	Boolean cacheable;

	@DataAttribute
	@Getter @Setter
	String foreignFieldName;

	@DataAttribute
	@Getter @Setter
	String sequenceName;

	@DataAttribute
	@Getter @Setter
	String orderSql;

	@DataAttribute
	@Getter @Setter
	String whereSql;

	@DataAttribute
	@Getter @Setter
	Boolean owned;

	@DataAttribute
	@Getter @Setter
	String joinColumnName;

	@DataAttribute
	@Getter @Setter
	String foreignColumnName;

	@DataAttribute
	@Getter @Setter
	String listIndexColumnName;

	@DataAttribute
	@Getter @Setter
	String mappingKeyColumnName;

	@DataAttribute
	@Getter @Setter
	String associationTableName;

	@DataAttribute
	@Getter @Setter
	String valueColumnName;

	@DataAttribute
	@Getter @Setter
	String indexCounterFieldName;

	@DataChildren
	@Getter @Setter
	List <ModelField> fields =
		new ArrayList<> ();

	@DataChildrenIndex
	@Getter @Setter
	Map <String, ModelField> fieldsByName =
		new LinkedHashMap<> ();

	//Field field;
	//Annotation annotation;

	// accessors

	public
	ModelField name (
			@NonNull CharSequence nameCharSequence) {

		String name =
			nameCharSequence.toString ();

		StringFormat.camelCase.verifyParameterAndThrow (
			"name",
			name);

		if (
			isNotNull (
				this.name)
		) {
			throw new IllegalStateException ();
		}

		this.name = name;

		return this;

	}

	public
	String columnName () {

		if (columnNames.size () != 1) {

			throw new RuntimeException (
				stringFormat (
					"Field %s has %s columns",
					fullName (),
					integerToDecimalString (
						columnNames.size ())));

		}

		return columnNames.get (0);

	}

	public
	ModelField columnName (
			@NonNull CharSequence columnName) {

		StringFormat.snakeCase.verifyParameterAndThrow (
			"columnName",
			columnName);

		return columnNames (
			singletonList (
				columnName.toString ()));

	}

	public
	boolean id () {

		return enumInSafe (
			type,
			ModelFieldType.generatedId,
			ModelFieldType.assignedId,
			ModelFieldType.foreignId,
			ModelFieldType.compositeId);

	}

	public
	boolean value () {

		return enumInSafe (
			type,
			ModelFieldType.active,
			ModelFieldType.assignedId,
			ModelFieldType.code,
			ModelFieldType.deleted,
			ModelFieldType.description,
			ModelFieldType.foreignId,
			ModelFieldType.generatedId,
			ModelFieldType.identitySimple,
			ModelFieldType.index,
			ModelFieldType.name,
			ModelFieldType.parentId,
			ModelFieldType.simple,
			ModelFieldType.typeCode);

	}

	public
	boolean reference () {

		return enumInSafe (
			type,
			ModelFieldType.reference,
			ModelFieldType.parent,
			ModelFieldType.grandParent,
			ModelFieldType.greatGrandParent,
			ModelFieldType.parentType,
			ModelFieldType.type,
			ModelFieldType.identityReference);

	}

	public
	boolean composite () {

		return enumInSafe (
			type,
			ModelFieldType.compositeId,
			ModelFieldType.component);

	}

	public
	boolean generatedId () {

		return enumInSafe (
			type,
			ModelFieldType.generatedId);

	}

	public
	boolean assignedId () {

		return enumInSafe (
			type,
			ModelFieldType.assignedId);

	}

	public
	boolean foreignId () {

		return enumInSafe (
			type,
			ModelFieldType.foreignId);

	}

	public
	boolean partner () {

		return enumInSafe (
			type,
			ModelFieldType.master,
			ModelFieldType.slave);

	}

	public
	boolean collection () {

		return enumInSafe (
			type,
			ModelFieldType.collection);

	}

	public
	boolean link () {

		return enumInSafe (
			type,
			ModelFieldType.associative);

	}

	public
	boolean compositeId () {

		return enumInSafe (
			type,
			ModelFieldType.compositeId);

	}

	public
	boolean component () {

		return enumInSafe (
			type,
			ModelFieldType.component);

	}

	public
	String fullName () {

		if (parentField != null) {

			return stringFormat (
				"%s.%s",
				parentField.fullName (),
				name ());

		} else {

			return stringFormat (
				"%s.%s",
				model.objectTypeCamel (),
				name ());

		}

	}

}
