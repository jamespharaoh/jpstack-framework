package wbs.framework.entity.meta;

import static wbs.framework.utils.etc.Misc.stringFormatArray;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
public
class AnnotationWriter {

	@Getter @Setter
	String name;

	@Getter @Setter
	Map<String,String> attributes =
		new LinkedHashMap<String,String> ();

	public
	AnnotationWriter addAttributeFormat (
			String name,
			String... valueFormat) {

		attributes.put (
			name,
			stringFormatArray (
				valueFormat));

		return this;

	}

	public
	void write (
			FormatWriter javaWriter,
			String indent)
		throws IOException {

		// write name

		javaWriter.write (
			"%s@%s",
			indent,
			name);

		// write attributes

		if (! attributes.isEmpty ()) {

			javaWriter.write (
				" (");

			boolean first = true;

			for (
				Map.Entry<String,String> attributeEntry
					: attributes.entrySet ()
			) {

				if (! first) {

					javaWriter.write (
						",");

				}

				javaWriter.write (
					"\n%s\t%s = %s",
					indent,
					attributeEntry.getKey (),
					attributeEntry.getValue ());

				first = false;

			}

			javaWriter.write (
				")");

		}

		javaWriter.write (
			"\n");

	}

}
