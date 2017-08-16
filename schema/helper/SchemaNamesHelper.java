package wbs.framework.schema.helper;

import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.camelToUnderscore;

import lombok.NonNull;

public
interface SchemaNamesHelper {

	String tableName (
			Class <?> entityClass);

	String columnName (
			String fieldName);

	String idColumnName (
			String fieldName);

	String idColumnName (
			Class <?> objectClass);

	String idSequenceName (
			Class <?> objectClass);

	String objectNameCamel (
			Class <?> objectClass);

	default
	String objectTypeHyphen (
			@NonNull Class <?> objectClass) {

		return camelToHyphen (
			objectNameCamel (
				objectClass));

	}

	default
	String objectTypeCode (
			@NonNull Class <?> objectClass) {

		return camelToUnderscore (
			objectNameCamel (
				objectClass));

	}

}
