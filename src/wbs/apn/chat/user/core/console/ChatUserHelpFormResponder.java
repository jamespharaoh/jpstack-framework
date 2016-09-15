package wbs.apn.chat.user.core.console;

import static wbs.utils.string.StringUtils.stringFormat;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserHelpFormResponder")
public
class ChatUserHelpFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h2>Send help message</h2>\n");

		requestContext.flushNotices (
			formatWriter);

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		String userInfo =
			chatUser.getName () == null
				? chatUser.getCode ()
				: chatUser.getCode () + " " + chatUser.getName ();

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",

			"<td>%h</td>\n",
			userInfo,

			"</tr>\n");

		String charCountScript =
			stringFormat (
				"gsmCharCount (%s, %s, 149)",
				"document.getElementById ('text')",
				"document.getElementById ('chars')");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td><textarea",
			" id=\"text\"",
			" cols=\"64\"",
			" rows=\"4\"",
			" name=\"text\"",
			" onkeyup=\"%h\"",
			charCountScript,
			" onfocus=\"%h\"",
			charCountScript,
			"></textarea></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Chars</th>\n",

			"<td><span id=\"chars\">&nbsp;</span></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"send message\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",
			"%s;\n",
			charCountScript,
			"</script>\n");

	}

}