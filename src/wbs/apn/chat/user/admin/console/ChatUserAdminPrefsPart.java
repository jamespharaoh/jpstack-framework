package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWriteSelected;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

@PrototypeComponent ("chatUserAdminPrefsPart")
public
class ChatUserAdminPrefsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	ChatUserRec chatUser;

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (

				isNull (
					chatUser)

				|| isNull (
					chatUser.getGender ())

				|| isNull (
					chatUser.getOrient ())

			) {

				requestContext.addError (
					"Cannot change prefs for this user");

				requestContext.flushNotices ();

				return;

			}

			// form open

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chatUser.admin.prefs"));

			// table open

			htmlTableOpenDetails ();

			// table contents

			htmlTableDetailsRowWrite (
				"Code",
				chatUser.getCode ());

			htmlTableDetailsRowWriteHtml (
				"Gender",
				() -> {

				htmlSelectOpen (
					"gender");

				if (chatUser.getGender () == Gender.male) {

					htmlOptionWrite (
						"male",
						true,
						"male");

					htmlOptionWrite (
						"female",
						false,
						"female");

				} else if (chatUser.getGender () == Gender.female) {

					htmlOptionWrite (
						"male",
						false,
						"male");

					htmlOptionWrite (
						"female",
						true,
						"female");

				} else if (chatUser.getGender () == null) {

					htmlOptionWrite (
						"male",
						false,
						"male");

					htmlOptionWrite (
						"female",
						false,
						"female");

				} else {

					shouldNeverHappen ();

				}

				htmlSelectClose ();

			});

			htmlTableDetailsRowWriteHtml (
				"Orient",
				() -> {

				htmlSelectOpen (
					"orient");

				if (chatUser.getOrient () == Orient.gay) {

					htmlOptionWriteSelected (
						"gay");

					htmlOptionWrite (
						"bi");

					htmlOptionWrite (
						"straight");

				} else if (chatUser.getOrient () == Orient.bi) {

					htmlOptionWrite (
						"gay");

					htmlOptionWriteSelected (
						"bi");

					htmlOptionWrite (
						"straight");

				} else if (chatUser.getOrient () == Orient.straight) {

					htmlOptionWrite (
						"gay");

					htmlOptionWrite (
						"bi");

					htmlOptionWriteSelected (
						"straight");

				} else {

					htmlOptionWrite (
						"—");

					htmlOptionWrite (
						"gay");

					htmlOptionWrite (
						"bi");

					htmlOptionWrite (
						"straight");

				}

				htmlSelectClose ();

			});

			// table close

			htmlTableClose ();

			// form controls

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" value=\"update prefs\"",
				">");

			htmlParagraphClose ();

			// form close

			htmlFormClose ();

		}

	}

}
