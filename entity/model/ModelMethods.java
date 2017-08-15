package wbs.framework.entity.model;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.PropertyUtils.propertyGetAuto;
import static wbs.utils.etc.PropertyUtils.propertySetAuto;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface ModelMethods <DataType> {

	// identity

	Class <DataType> objectClass ();
	String objectClassName ();

	String objectTypeCode ();
	String objectTypeCamel ();
	String objectTypeHyphen ();

	String oldObjectName ();
	String tableName ();

	String friendlyNameSingular ();
	String friendlyNamePlural ();

	String shortNameSingular ();
	String shortNamePlural ();

	// fields

	ModelField activeField ();
	ModelField codeField ();
	ModelField deletedField ();
	ModelField descriptionField ();
	ModelField idField ();
	ModelField indexField ();
	ModelField masterField ();
	ModelField nameField ();
	ModelField parentField ();
	ModelField parentIdField ();
	ModelField parentTypeField ();
	ModelField timestampField ();
	ModelField typeCodeField ();
	ModelField typeField ();

	List <ModelField> fields ();
	Map <String, ModelField> fieldsByName ();

	List <ModelField> identityFields ();
	Set <ModelFieldType> identityFieldTypes ();

	ModelField field (
			String name);

	// misc parameters

	Boolean isRoot ();
	Boolean isRooted ();
	Boolean canGetParent ();
	Boolean parentTypeIsFixed ();

	Boolean create ();
	Boolean mutable ();

	Class <? extends Record <?>> parentClassRequired ();
	Class <? extends ObjectHelper <?>> helperClass ();

	// other information

	CachedViewSpec cachedView ();

	// property accessors

	default
	Long getId (
			@NonNull DataType object) {

		if (
			isNull (
				idField ())
		) {
			throw new UnsupportedOperationException ();
		}

		return (Long)
			propertyGetAuto (
				object,
				idField ().name ());

	}

	default
	Record <?> getParentOrNull (
			@NonNull DataType object) {

		if (! canGetParent ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent for %s",
					objectTypeHyphen ()));

		}

		return (Record<?>)
			propertyGetAuto (
				object,
				parentField ().name ());

	}

	default
	Record <?> getParentOrNullGeneric (
			Record <?> object) {

		return getParentOrNull (
			objectClass ().cast (
				object));

	}

	default
	Record <?> getParentType (
			@NonNull DataType object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent type for %s",
					objectTypeHyphen ()));

		}

		return (Record <?>)
			propertyGetAuto (
				object,
				parentTypeField ().name ());

	}

	default
	Record <?> getParentTypeGeneric (
			Record <?> object) {

		return genericCastUnchecked (
			objectClass ().cast (
				object));

	}

	default
	Long getParentId (
			@NonNull DataType object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent id for %s",
					objectTypeHyphen ()));

		}

		return (Long)
			propertyGetAuto (
				object,
				parentIdField ().name ());

	}

	default
	String getTypeCode (
			@NonNull DataType object) {

		if (
			isNull (
				typeCodeField ())
		) {
			throw new UnsupportedOperationException ();
		}

		return (String)
			propertyGetAuto (
				object,
				typeCodeField ().name ());

	}

	default
	String getTypeCodeGeneric (
			Record <?> object) {

		return getTypeCode (
			objectClass ().cast (
				object));

	}

	default
	String getCode (
			@NonNull DataType object) {

		if (codeField () != null) {

			return (String)
				propertyGetAuto (
					object,
					codeField ().name ());

		}

		return integerToDecimalString (
			getId (
				object));

	}


	default
	String getCodeGeneric (
			Record <?> record) {

		return getCode (
			objectClass ().cast (
				record));

	}

	default
	Long getIndex (
			@NonNull DataType object) {

		if (indexField () == null) {
			throw new UnsupportedOperationException ();
		}

		return (Long)
			propertyGetAuto (
				object,
				indexField ().name ());

	}

	default
	Long getIndexGeneric (
			@NonNull Record <?> record) {

		return getIndex (
			objectClass ().cast (
				record));

	}

	default
	void setIndex (
			@NonNull DataType object,
			@NonNull Long index) {

		if (indexField () == null) {
			throw new UnsupportedOperationException ();
		}

		propertySetAuto (
			object,
			indexField ().name (),
			index);

	}

	default
	Long setIndexGeneric (
			@NonNull Record <?> record) {

		return getIndex (
			objectClass ().cast (
				record));

	}

	default
	String getName (
			@NonNull DataType object) {

		if (
			isNull (
				nameField ())
		) {
			throw new UnsupportedOperationException ();
		}

		return (String)
			propertyGetAuto (
				object,
				nameField ().name ());

	}

	default
	String getDescription (
			@NonNull DataType object) {

		if (
			isNull (
				descriptionField ())
		) {
			throw new UnsupportedOperationException ();
		}

		return (String)
			propertyGetAuto (
				object,
				descriptionField ().name ());

	}

	default
	Boolean getDeleted (
			@NonNull DataType object) {

		if (
			isNull (
				deletedField ())
		) {
			throw new UnsupportedOperationException ();
		}

		return (Boolean)
			propertyGetAuto (
				object,
				deletedField ().name ());

	}

	default
	void setDeleted (
			@NonNull DataType object,
			@NonNull Boolean deleted) {

		if (deletedField () == null) {
			throw new UnsupportedOperationException ();
		}

		propertySetAuto (
			object,
			deletedField ().name (),
			deleted);

	}

}
