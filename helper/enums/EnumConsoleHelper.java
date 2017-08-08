package wbs.console.helper.enums;

import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.uncapitalise;
import static wbs.web.utils.HtmlInputUtils.htmlSelect;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public
class EnumConsoleHelper <E extends Enum <E>> {

	@Getter
	Map <E, String> map =
		new LinkedHashMap<> ();

	@Getter
	Map <String, String> optionsMap =
		new LinkedHashMap<> ();

	@Getter
	Map <String, String> nullOptionsMap =
		new LinkedHashMap<> ();

	@Getter @Setter
	Class <E> enumClass;

	{

		optionsMap.put ("", "");

		nullOptionsMap.put ("", "");
		nullOptionsMap.put ("null", "none");

	}

	public
	void add (
			E value,
			String label) {

		map.put (value, label);
		optionsMap.put (value.name (), label);
		nullOptionsMap.put (value.name (), label);

	}

	public
	String toText (
			E value) {

		if (value == null)
			return "-";

		String ret =
			map.get (value);

		if (ret == null)
			throw new IllegalArgumentException ();

		return ret;

	}

	public
	String toTdReal (
			E value) {

		return "<td>" + toText (value) + "</td>";

	}

	@SuppressWarnings ("unchecked")
	public
	String toTd (
			Enum<?> value) {

		return toTdReal ((E) value);

	}

	@Deprecated
	public
	void writeSelect (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value) {

		htmlSelect (
			formatWriter,
			name,
			optionsMap,
			value);

	}

	@Deprecated
	public
	void writeSelectOptional (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value,
			@NonNull String noneName) {

		htmlSelect (
			formatWriter,
			name,
			nullOptionsMap,
			value);

	}

	public
	EnumConsoleHelper <E> auto () {

		for (
			E value
				: enumClass.getEnumConstants ()
		) {

			add (
				value,
				camelToSpaces (
					value.name ()));

		}

		return this;

	}

	public
	Optional <String> htmlClass (
			@NonNull E value) {

		return optionalOfFormat (
			"enum--%s--%s",
			camelToHyphen (
				uncapitalise (
					classNameSimple (
						enumClass))),
			enumNameHyphens (
				value));

	}

}
