package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ConsoleModule chatUserImageConsoleModule;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// state

	FormFieldSet formFieldSet;

	ChatUserImageType chatUserImageType;

	ChatUserImageUploadForm uploadForm;

	// implementation

	@Override
	public
	void prepare () {

		formFieldSet =
			chatUserImageConsoleModule.formFieldSets ().get (
				"uploadForm");

		chatUserImageType =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

		uploadForm =
			new ChatUserImageUploadForm ();

		if (requestContext.post ()) {

			formFieldLogic.update (
				requestContext,
				formFieldSet,
				uploadForm,
				ImmutableMap.of (),
				"upload");

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>Please upload the photo or video.</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				stringFormat (
					"/chatUser.%s.upload",
					chatUserImageType.name ())),
			" enctype=\"multipart/form-data\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			Optional.absent (),
			uploadForm,
			ImmutableMap.of (),
			FormType.perform,
			"upload");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"upload file\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}