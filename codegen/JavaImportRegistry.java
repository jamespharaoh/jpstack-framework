package wbs.framework.codegen;

import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.NonNull;

public
interface JavaImportRegistry {

	String register (
			CharSequence className);

	default
	String register (
			@NonNull Class <?> classObject) {

		return register (
			classNameFull (
				classObject));

	}

	default
	String registerFormat (
			@NonNull CharSequence ... arguments) {

		return register (
			stringFormatArray (
				arguments));

	}

}
