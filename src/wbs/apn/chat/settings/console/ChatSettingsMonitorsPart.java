package wbs.apn.chat.settings.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.extern.log4j.Log4j;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@PrototypeComponent ("chatSettingsMonitorsPart")
public
class ChatSettingsMonitorsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

	// state

	private
	int
		gayMale,
		gayFemale,
		biMale,
		biFemale,
		straightMale,
		straightFemale;

	// implementation

	@Override
	public
	void prepare () {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		List<Long> onlineMonitorIds =
			chatUserHelper.searchIds (
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.type (
				ChatUserType.monitor)

			.online (
				true)

		);

		log.debug ("Got " + onlineMonitorIds.size ());

		for (
			Long monitorId
				: onlineMonitorIds
		) {

			ChatUserRec monitor =
				chatUserHelper.findRequired (
					monitorId);

			log.debug (
				stringFormat (
					"Orient %s, gender %s",
					monitor.getOrient (),
					monitor.getGender ()));

			switch (monitor.getOrient ()) {

			case gay:

				switch (monitor.getGender ()) {

				case male:
					gayMale ++;
					continue;

				case female:
					gayFemale++;
					continue;

				}

				throw new RuntimeException ();

			case bi:

				switch (monitor.getGender ()) {

				case male:
					biMale ++;
					continue;

				case female:
					biFemale ++;
					continue;

				}

				throw new RuntimeException ();

			case straight:

				switch (monitor.getGender ()) {

				case male:
					straightMale ++;
					continue;

				case female:
					straightFemale ++;
					continue;

				}

				throw new RuntimeException ();

			}

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chat.settings.monitors"),
			">\n");

		printFormat (
			"<table",
			" class=\"list\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Orient</th>\n",
			"<th>Male</th>\n",
			"<th>Female</th>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Gay</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"gayMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("gayMale"),
				gayMale),
			"></td>",

			"<td><input",
			" type=\"text\"",
			" name=\"gayFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("gayFemale"),
				gayFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Bi</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"biMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("biMale"),
				biMale),
			"></td>",

			"<td><input",
			" type=\"text\"",
			" name=\"biFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("biFemale"),
				biFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td>Straight</td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"straightMale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("straightMale"),
				straightMale),
			"></td>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"straightFemale\"",
			" size=\"6\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("straightFemale"),
				straightFemale),
			"></td>",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}