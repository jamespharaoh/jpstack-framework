package wbs.framework.schema.helper;

import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("schemaNamesHelper")
public
class SchemaNamesHelperImplementation
	implements SchemaNamesHelper {

	@Override
	public
	String tableName (
			@NonNull Class <?> entityClass) {

		return objectTypeCode (
			entityClass);

	}

	@Override
	public
	String columnName (
			@NonNull String fieldName) {

		return camelToUnderscore (
			fieldName);

	}

	@Override
	public
	String idColumnName (
			String fieldName) {

		return stringFormat (
			"%s_id",
			camelToUnderscore (
				fieldName));

	}

	@Override
	public
	String idColumnName (
			Class<?> objectClass) {

		return stringFormat (
			"%s_id",
			objectTypeCode (
				objectClass));

	}

	@Override
	public
	String idSequenceName (
			Class<?> objectClass) {

		return stringFormat (
			"%s_id_seq",
			objectTypeCode (
				objectClass));

	}

	@Override
	public
	String objectNameCamel (
			@NonNull Class <?> objectClass) {

		String className =
			objectClass.getSimpleName ();

		Matcher matcher =
			entityNamePattern.matcher (
				className);

		if (! matcher.matches ()) {

			throw new IllegalArgumentException (
				className);

		}

		return uncapitalise (
			matcher.group (1));

	}

	public final static
	Pattern entityNamePattern =
		Pattern.compile (
			"(.+)(Rec|View)");

}
