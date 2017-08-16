package wbs.framework.entity.model;

import static wbs.utils.collection.IterableUtils.iterableFilterMapToSet;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataReference;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.RootRecord;
import wbs.framework.object.ObjectHelper;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@Data
@DataClass
public
class RecordModelImplementation <RecordType extends Record <RecordType>>
	implements
		ModelImplementationMethods <
			RecordModelImplementation <RecordType>,
			RecordType
		>,
		RecordModel <RecordType> {

	// identity

	@DataAttribute
	Class <RecordType> objectClass;

	@DataAttribute
	String objectClassName;

	@DataAttribute
	String objectTypeCode;

	@DataAttribute
	String objectTypeHyphen;

	@DataAttribute
	String objectTypeCamel;

	@DataAttribute
	String friendlyNameSingular;

	@DataAttribute
	String friendlyNamePlural;

	@DataAttribute
	String shortNameSingular;

	@DataAttribute
	String shortNamePlural;

	// database stuff

	@DataAttribute
	String oldObjectName;

	@DataAttribute
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// fields

	@DataReference
	ModelField activeField;

	@DataReference
	ModelField codeField;

	@DataReference
	ModelField deletedField;

	@DataReference
	ModelField descriptionField;

	@DataReference
	ModelField idField;

	@DataReference
	ModelField indexField;

	@DataReference
	ModelField masterField;

	@DataReference
	ModelField nameField;

	@DataReference
	ModelField parentField;

	@DataReference
	ModelField parentTypeField;

	@DataReference
	ModelField parentIdField;

	@DataReference
	ModelField timestampField;

	@DataReference
	ModelField typeCodeField;

	@DataReference
	ModelField typeField;

	@DataChildren
	List <ModelField> fields =
		new ArrayList<> ();

	@DataChildrenIndex
	Map <String, ModelField> fieldsByName =
		new LinkedHashMap<> ();

	@DataChild
	CachedViewSpec cachedView;

	// helper

	@DataAttribute
	Class <? extends ObjectHelper<?>> helperClass;

	// methods

	public
	Long getId (
			@NonNull Record<?> object) {

		return object.getId ();

	}

	@Override
	public
	String getDescription (
			@NonNull RecordType object) {

		return (String)
			PropertyUtils.propertyGetAuto (
				object,
				descriptionField.name ());

	}

	@Override
	public
	Boolean isRoot () {

		return RootRecord.class.isAssignableFrom (
			objectClass ());

	}

	@Override
	public
	Boolean isRooted () {

		return ! isRoot ()
			&& parentTypeField == null
			&& parentField == null;

	}

	@Override
	public
	Boolean canGetParent () {

		return parentField != null;

	}

	@Override
	public
	Boolean parentTypeIsFixed () {

		return parentTypeField == null;

	}

	@Override
	public
	Class <? extends Record <?>> parentClassRequired () {

		if (parentTypeField != null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectTypeHyphen ()));

		} else if (parentField != null) {

			@SuppressWarnings ("unchecked")
			Class<? extends Record<?>> classTemp =
				(Class<? extends Record<?>>)
				parentField.valueType ();

			return classTemp;

		} else if (isRoot ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectTypeHyphen ()));

		} else if (isRooted ()) {

			throw new RuntimeException ("TODO");

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	ModelField field (
			@NonNull String name) {

		return mapItemForKeyRequired (
			fieldsByName,
			name);

	}

	@Override
	public
	List <ModelField> identityFields () {

		return iterableFilterToList (
			fields,
			ModelField::identity);

	}

	@Override
	public
	Set <ModelFieldType> identityFieldTypes () {

		return iterableFilterMapToSet (
			fields,
			ModelField::identity,
			ModelField::type);

	}

}
