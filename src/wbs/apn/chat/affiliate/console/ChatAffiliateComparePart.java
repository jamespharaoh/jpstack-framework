package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethodAction;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.IntervalFormatter;
import wbs.utils.time.TextualInterval;

@PrototypeComponent ("chatAffiliateComparePart")
public
class ChatAffiliateComparePart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	IntervalFormatter intervalFormatter;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	String timePeriodString;
	List <ChatAffiliateWithNewUserCount> chatAffiliateWithNewUserCounts;

	// implementation

	@Override
	public
	void prepare () {

		// check units

		timePeriodString =
			requestContext.parameterOrDefault (
				"timePeriod",
				"7 days");

		Long timePeriodSeconds =
			intervalFormatter.parseIntervalStringSecondsRequired (
				timePeriodString);

		if (timePeriodSeconds == null) {

			requestContext.addError (
				"Invalid time period");

			return;

		}

		// get objects

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		// work out first join time

		DateTimeZone timeZone =
			DateTimeZone.forID (
				chat.getTimezone ());

		Instant firstJoinAfter =
			DateTime.now (
				timeZone)

			.minusSeconds (
				toJavaIntegerRequired (
					timePeriodSeconds))

			.toInstant ();

		// get all relevant users

		List <Long> newUserIds =
			chatUserHelper.searchIds (
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.firstJoin (
				TextualInterval.after (
					DateTimeZone.UTC,
					firstJoinAfter))

		);

		// count them grouping by affiliate

		Map <Long, ChatAffiliateWithNewUserCount> map =
			new HashMap<> ();

		for (
			Long chatUserId
				: newUserIds
		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			Long chatAffiliateId =
				chatUser.getChatAffiliate () != null
					? chatUser.getChatAffiliate ().getId ()
					: null;

			ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount =
				map.get (
					chatAffiliateId);

			if (chatAffiliateWithNewUserCount == null) {

				map.put (
					chatAffiliateId,
					chatAffiliateWithNewUserCount =
						new ChatAffiliateWithNewUserCount ()

							.chatAffiliate (
								chatUser.getChatAffiliate ())

				);

			}

			chatAffiliateWithNewUserCount.newUsers ++;

		}

		// now select and sort the ones we are allowed to see

		chatAffiliateWithNewUserCounts =
			new ArrayList<ChatAffiliateWithNewUserCount> ();

		if (
			privChecker.canRecursive (
				chat,
				"stats")
		) {

			chatAffiliateWithNewUserCounts.addAll (
				map.values ());

			for (
				ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
					: map.values ()
			) {

				ChatAffiliateRec chatAffiliate =
					chatAffiliateWithNewUserCount.chatAffiliate;

				if (! privChecker.canRecursive (chatAffiliate, "stats"))
					continue;

				chatAffiliateWithNewUserCounts.add (
					chatAffiliateWithNewUserCount);

			}

		}

		Collections.sort (
			chatAffiliateWithNewUserCounts);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		renderForm ();
		renderHistory ();

	}

	private
	void renderForm () {

		htmlFormOpenMethodAction (
			"get",
			requestContext.resolveLocalUrl (
				"/chatAffiliate.compare"));

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Time period<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"timePeriod\"",
			" value=\"%h\"",
			timePeriodString,
			"\">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	private
	void renderHistory () {

		if (
			isNull (
				chatAffiliateWithNewUserCounts)
		) {
			return;
		}

		htmlTableOpenList (); 

		htmlTableHeaderRowWrite (
			"Scheme",
			"Affiliate<",
			"Descriptionn",
			"New users");

		for (
			ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
				: chatAffiliateWithNewUserCounts
		) {

			ChatAffiliateRec chatAffiliate =
				chatAffiliateWithNewUserCount.chatAffiliate;

			htmlTableRowOpen ();

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getChatScheme ().getCode ()
					: "(no affiliate)");

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getCode ()
					: "(no affiliate)");

			htmlTableCellWrite (
				chatAffiliate != null
					? chatAffiliate.getDescription ()
					: "-");

			htmlTableCellWrite (
				integerToDecimalString (
					chatAffiliateWithNewUserCount.newUsers));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}