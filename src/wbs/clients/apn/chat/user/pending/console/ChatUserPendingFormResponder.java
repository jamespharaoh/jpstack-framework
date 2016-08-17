package wbs.clients.apn.chat.user.pending.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.help.console.ChatHelpTemplateConsoleHelper;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.etc.Html;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserPendingFormResponder")
public
class ChatUserPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	// state

	ChatUserRec chatUser;

	List<ChatHelpTemplateRec> chatHelpTemplates =
		Collections.emptyList ();

	PendingMode mode;

	// implementation

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.build ();

	}

	@Override
	protected
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		if (
			isNotNull (
				chatUser.getNewChatUserName ())
		) {

			mode =
				PendingMode.name;

		} else if (
			isNotNull (
				chatUser.getNewChatUserInfo ())
		) {

			mode =
				PendingMode.info;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.image))
		) {

			mode =
				PendingMode.image;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.video))
		) {

			mode =
				PendingMode.video;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.audio))
		) {

			mode =
				PendingMode.audio;

		} else {

			mode =
				PendingMode.none;

		}

		if (mode.rejectType () != null) {

			chatHelpTemplates =
				chatHelpTemplateHelper.findByParentAndType (
					chatUser.getChat (),
					mode.rejectType ());

		}

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"var chatHelpTemplates = new Array ();\n");

		for (ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates) {

			printFormat (
				"chatHelpTemplates[%s] = '%j';\n",
				chatHelpTemplate.getId (),
				chatHelpTemplate.getText ());

		}

		printFormat (
			"function useTemplate () {\n",
			"  var templateId = document.getElementById ('templateId');\n",
			"  var text = document.getElementById ('message');\n",
			"  if (templateId.value == '') return;\n",
			"  var template = chatHelpTemplates[templateId.value];\n",
			"  if (template) text.value = template;\n",
			"}\n");

		printFormat (
			"function showPhoto () {\n",
			"  try {\n",
			"    document.getElementById ('photoRow').style.display = 'table-row';\n",
			"  } catch (e) {\n",
			"    document.getElementById ('photoRow').style.display = 'block';\n",
			"  }\n",
			"  document.getElementById ('templateRow').style.display = 'none';\n",
			"  document.getElementById ('messageRow').style.display = 'none';\n",
			"  document.getElementById ('approveButton').style.display = 'inline';\n",
			"  document.getElementById ('rejectButton').style.display = 'none';\n",
			"  $('#classificationRow').show ();\n",
			"}\n");

		printFormat (
			"function showReject () {\n",
			"  document.getElementById ('photoRow').style.display = 'none';\n",
			"  try {\n",
			"    document.getElementById ('templateRow').style.display = 'table-row';\n",
			"    document.getElementById ('messageRow').style.display = 'table-row';\n",
			"  } catch (e) {\n",
			"    document.getElementById ('templateRow').style.display = 'block';\n",
			"    document.getElementById ('messageRow').style.display = 'block';\n",
			"  }\n",
			"  document.getElementById ('approveButton').style.display = 'none';\n",
			"  document.getElementById ('rejectButton').style.display = 'inline';\n",
			"  $('#classificationRow').hide ();\n",
			"}\n");

		printFormat (
			"top.show_inbox (true);\n");

		printFormat (
			"top.frames['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatUser.pending",
					"/%u",
					chatUser.getId (),
					"/chatUser.pending.summary")));

		printFormat (
			"</script>\n");

	}

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>Chat user&mdash;approve info</h1>\n");

		requestContext.flushNotices (
			printWriter);

		printFormat (
			"<form",
			" method=\"post\"",

			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatUser.pending",
					"/%u",
					chatUser.getId (),
					"/chatUser.pending.form")),

			">");

		if (mode == PendingMode.none) {

			printFormat (
				"<p>No info to approve</p>\n");

			if (
				privChecker.canRecursive (
					GlobalId.root,
					"manage")
			) {

				printFormat (
					"<p><input",
					" type=\"submit\"",
					" name=\"chatUserDismiss\"",
					" value=\"dismiss queue item\"",
					"></p>\n");

			}

		} else {

			printFormat (

				"<table class=\"list\">\n");

			printFormat (

				"<tr>\n",

				"<th>User</th>\n",

				"<td>%h</td>\n",
				stringFormat (
					"%s/%s",
					chatUser.getChat ().getCode (),
					chatUser.getCode ()),

				"</tr>\n");

			printFormat (

				"<tr>\n",

				"<th>Options</th>\n",

				"<td><input",
				" type=\"button\"",
				" value=\"approve\"",
				" onclick=\"showPhoto ()\"",
				">",

				"<input",
				" type=\"button\"",
				" value=\"reject\"",
				" onclick=\"showReject ()\"",
				"></td>\n",

				"</tr>\n");

			switch (mode) {

			case name:

				printFormat (
					"<tr id=\"photoRow\">\n",
					"<th>Name</th>\n",

					"<td><textarea",
					" name=\"name\"",
					" rows=\"4\"",
					" cols=\"48\"",
					">%h</textarea></td>\n",
					chatUser
						.getNewChatUserName ()
						.getOriginalName (),

					"</tr>\n");

				break;

			case info:

				printFormat (
					"<tr id=\"photoRow\">\n",

					"<th>Info</th>\n",

					"<td><textarea",
					" name=\"info\"",
					" rows=\"4\"",
					" cols=\"48\"",
					">%h</textarea></td>\n",
					chatUser
						.getNewChatUserInfo ()
						.getOriginalText ()
						.getText (),

					"</tr>\n");

				break;

			case image:

				ChatUserImageRec image =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						ChatUserImageType.image);

				printFormat (
					"<tr id=\"photoRow\">\n",
					"<th>Photo</th>\n",

					"<td>%s</td>\n",
					mediaConsoleLogic.mediaThumb100 (
						image.getMedia ()),

					"</tr>\n");

				break;

			case video:

				ChatUserImageRec video =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						ChatUserImageType.video);

				printFormat (
					"<tr id=\"photoRow\">\n",
					"<th>Video</th>\n",

					"<td>%s</td>\n",
					mediaConsoleLogic.mediaThumb100 (
						video.getMedia ()),

					"</tr>\n");

				break;

			case audio:

				printFormat (
					"<tr id=\"photoRow\">\n",
					"<th>Audio</th>\n",

					"<td>(audio)</td>\n",

					"</tr>\n");

				break;

			default:
				// do nothing

			}

			if (
				in (
					mode,
					PendingMode.image,
					PendingMode.video,
					PendingMode.audio)
			) {

				ChatUserImageRec chatUserImage =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						chatUserLogic.imageTypeForMode (mode));

				printFormat (
					"<tr id=\"classificationRow\">\n",
					"<th>Classification</th>\n",

					"<td><select name=\"classification\">\n",

					"%s\n",
					Html.option (
						"primary",
						"primary",
						requestContext.getForm ("classification")));

				if (
					chatUserImage.getAppend ()
					|| equal (
						chatUser.getChat ().getCode (),
						"adult")
				) {

					printFormat (
						"%s\n",
						Html.option (
							"secondary",
							"secondary",
							requestContext.getForm ("classification")));

				}

				if (
					chatUserImage.getAppend ()
				) {

					printFormat (
						"%s\n",
						Html.option (
							"landscape",
							"landscape",
							requestContext.getForm ("classification")));

				}

				printFormat (
					"</select>\n",
					"</td>\n",
					"</tr>\n");

			}

			printFormat (

				"<tr",
				" id=\"templateRow\"",
				" style=\"display: none\">\n",

				"<th>Template</th>\n",

				"<td><select",
				" id=\"templateId\"",
				">\n",

				"<option>\n");

			for (ChatHelpTemplateRec chatelpTemplate
					: chatHelpTemplates) {

				printFormat (
					"<option",
					" value=\"%h\"",
					chatelpTemplate.getId (),
					">%h</option>\n",
					chatelpTemplate.getCode ());

			}

			printFormat (
				"</select>\n",

				"<input",
				" type=\"button\"",
				" onclick=\"useTemplate ()\"",
				" value=\"ok\"",
				"></td>\n",

				"</tr>\n");

			printFormat (
				"<tr",
				" id=\"messageRow\"",
				" style=\"display: none\"",
				">\n",

				"<th>Message</th>\n",

				"<td><textarea",
				" id=\"message\"",
				" name=\"message\"",
				" rows=\"4\"",
				" cols=\"48\"",
				"></textarea></td>\n",

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Actions</th>\n",
				"<td>");

			switch (mode) {

			case name:

				printFormat (

					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserNameApprove\"",
					" value=\"approve name\"",
					">\n",

					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserNameReject\"",
					"value=\"reject name and send warning\"",
					">\n");

				break;

			case info:

				printFormat (

					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserInfoApprove\"",
					" value=\"approve info\"",
					">\n",

					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserInfoReject\"",
					" value=\"reject info and send warning\"",
					">\n");

				break;

			case image:

				printFormat (

					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserImageApprove\"",
					" value=\"approve photo\"",
					">\n",

					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserImageReject\"",
					" value=\"reject photo and send warning\"",
					">\n");

				break;

			case video:

				printFormat (

					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserVideoApprove\"",
					" value=\"approve video\"",
					">\n",

					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserVideoReject\"",
					" value=\"reject video and send warning\"",
					">\n");

				break;

			case audio:

				printFormat (

					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserAudioApprove\"",
					" value=\"approve audio\"",
					">\n",

					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserAudioReject\"",
					" value=\"reject audio and send warning\"",
					">\n");

				break;

			default:

				// do nothing

			}

			printFormat (
				"</td>\n",

				"</tr>\n");

			printFormat (
				"</table>\n");

		}

		printFormat (
			"</form>\n");

	}
}