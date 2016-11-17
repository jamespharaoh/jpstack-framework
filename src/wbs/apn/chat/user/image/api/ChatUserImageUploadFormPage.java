package wbs.apn.chat.user.image.api;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatUserImageUploadFormPage")
public
class ChatUserImageUploadFormPage
	extends PrintResponder {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	RequestContext requestContext;

	// state

	ChatUserImageUploadTokenRec imageUploadToken;
	ChatUserRec chatUser;

	String titleText;
	String introHtml;
	String submitLabel;

	// implementation

	@Override
	protected
	void prepare () {

		imageUploadToken =
			chatUserImageUploadTokenHelper.findByToken (
				requestContext.requestStringRequired (
					"chatUserImageUploadToken"));

		chatUser =
			imageUploadToken.getChatUser ();

		titleText =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_form_title",
				ImmutableMap.<String,String> of ());

		introHtml =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_form_intro",
				ImmutableMap.<String,String> of ());

		submitLabel =
			chatSendLogic.renderTemplate (
				chatUser,
				"web",
				"image_upload_form_submit",
				ImmutableMap.<String,String> of ());

	}

	@Override
	protected
	void goHeaders ()
		throws IOException {

		requestContext.addHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		printFormat (
			"<!DOCTYPE html>\n");

		printFormat (
			"<html>\n");

		goHead ();

		goBody ();

		printFormat (
			"</html>\n");

	}

	protected
	void goHead () {

		printFormat (
			"<head>\n");

		printFormat (
			"<title>%h</title>\n",
			titleText);

		printFormat (
			"</head>\n");

	}

	protected
	void goBody () {

		printFormat (
			"<body>\n");

		printFormat (
			"<h1>%h</h1>\n",
			titleText);

		printFormat (
			"%s\n",
			introHtml);

		printFormat (
			"<form",
			" method=\"post\"",
			" enctype=\"multipart/form-data\"",
			">\n");

		printFormat (
			"<p><input",
			" type=\"file\"",
			" name=\"file\"",
			"></p>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"%h\"",
			submitLabel,
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"</body>\n");

	}

}
