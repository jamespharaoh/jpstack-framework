package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imageFormFieldRenderer")
@Accessors (fluent = true)
public
class ImageFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,MediaRec> {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Integer size;

	@Getter @Setter
	Boolean nullable;

	// details

	@Getter
	boolean fileUpload = true;

	// implementation

	@Override
	public
	void renderTableCell (
			PrintWriter out,
			Container container,
			MediaRec interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
				"<td>%s</td>\n",
				interfaceToHtml (
					container,
					interfaceValue,
					link)));

	}

	@Override
	public
	void renderTableRow (
			PrintWriter out,
			Container container,
			MediaRec interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label ()));

		renderTableCell (
			out,
			container,
			interfaceValue,
			link);

		out.write (
			stringFormat (
				"</tr>\n"));

	}

	@Override
	public
	void renderFormRow (
			PrintWriter out,
			Container container,
			MediaRec interfaceValue) {

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label (),
				"<td>"));

		renderFormInput (
			out,
			container,
			interfaceValue);

		out.write (
			stringFormat (
				"</td>\n",
				"</tr>\n"));

	}

	@Override
	public
	void renderFormInput (
			PrintWriter out,
			Container container,
			MediaRec interfaceValue) {

		if (interfaceValue != null) {

			out.write (
				stringFormat (
					"%s<br>\n",
					interfaceToHtml (
						container,
						interfaceValue,
						true)));

		}

		out.write (
			stringFormat (
				"<input",
				" type=\"file\"",
				" size=\"%h\"",
				size (),
				" name=\"%h\"",
				name (),
				" value=\"%h\"",
				formValuePresent ()
					? formValue ()
					: interfaceValue,
				"><br>\n"));

		if (
			interfaceValue != null
			&& nullable ()
		) {

			out.write (
				stringFormat (
					"<input",
					" type=\"submit\"",
					" name=\"%h-remove\"",
					name (),
					" value=\"remove image\"",
					">\n"));

		}

	}

	@Override
	public
	boolean formValuePresent () {

		if (
			isNotNull (
				requestContext.getForm (
					stringFormat (
						"%s-remove",
						name ())))
		) {
			return true;
		}

		if (! requestContext.isMultipart ())
			return false;

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		return fileItem != null
			&& fileItem.getSize () > 0;

	}

	@SneakyThrows ({
		IOException.class
	})
	MediaRec formValue () {

		if (
			isNotNull (
				requestContext.getForm (
					stringFormat (
						"%s-remove",
						name ())))
		) {
			return null;
		}

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		if (
			fileItem == null
			|| fileItem.getSize () == 0
		) {
			throw new IllegalStateException ();
		}

		byte[] data =
			IOUtils.toByteArray (
				fileItem.getInputStream ());

		return mediaLogic.createMediaFromImage (
			data,
			"image/jpeg",
			fileItem.getName ());

	}

	@Override
	public
	MediaRec formToInterface (
			List<String> errors) {

		return formValue ();

	}

	@Override
	public
	String interfaceToHtml (
			Container container,
			MediaRec interfaceValue,
			boolean link) {

		if (interfaceValue == null)
			return "";

		return stringFormat (
			"%s<br>\n",
			mediaConsoleLogic.mediaThumb100 (
				interfaceValue),
			"%h (%h bytes)",
			interfaceValue.getFilename (),
			interfaceValue.getContent ().getData ().length);

	}

}