package wbs.apn.chat.user.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.localDateNotEqual;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlColourFromObject;

import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserHelpPart")
public
class ChatUserHelpPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	Set <ChatHelpLogRec> chatHelpLogs;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		chatHelpLogs =
			new TreeSet<ChatHelpLogRec> (
				chatUser.getChatHelpLogs ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		String link =
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm");

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"<button",
			" onclick=\"%h\"",
			stringFormat (
				"top.frames['inbox'].location='%j';",
				link),
			">send message</button>");

		htmlParagraphClose ();

		if (
			collectionIsEmpty (
				chatHelpLogs)
		) {

			htmlParagraphWrite (
				"No history to display.");

			return;

		}

		// table open

		htmlTableOpenList ();

		// table headers

		htmlTableHeaderRowWrite (
			"",
			"Time",
			"Message",
			"Our number",
			"User");

		// table content

		LocalDate previousDate = null;

		DateTimeZone timezone =
			chatUserLogic.getTimezone (
				chatUser);

		for (
			ChatHelpLogRec chatHelpLog
				: chatHelpLogs
		) {

			LocalDate nextDate =
				chatHelpLog.getTimestamp ()

				.toDateTime (
					timezone)

				.toLocalDate ();

			if (

				isNull (
					previousDate)

				|| localDateNotEqual (
					nextDate,
					previousDate)

			) {

				previousDate =
					nextDate;

				htmlTableRowSeparatorWrite ();

				htmlTableRowOpen (
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"font-weight",
							"bold")));

				htmlTableCellWrite (
					timeFormatter.dateStringLong (
						chatUserLogic.getTimezone (
							chatUser),
						chatHelpLog.getTimestamp ()),
					htmlColumnSpanAttribute (5l));

				htmlTableRowClose ();

			}

			String rowClass =
				messageConsoleLogic.classForMessageDirection (
					chatHelpLog.getDirection ());

			htmlTableRowOpen (
				htmlClassAttribute (
					rowClass));

			htmlTableCellWriteHtml (
				"&nbsp;",
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"background",
						htmlColourFromObject (
							ifNull (
								chatHelpLog.getOurNumber (),
								0)))));

			htmlTableCellWrite (
				timeFormatter.timeString (
					chatUserLogic.getTimezone (
						chatUser),
					chatHelpLog.getTimestamp ()));


			htmlTableCellWrite (
				chatHelpLog.getText ());

			htmlTableCellWrite (
				chatHelpLog.getOurNumber ());

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatHelpLog.getUser (),
				() ->
					chatHelpLog.getUser ().getUsername ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
