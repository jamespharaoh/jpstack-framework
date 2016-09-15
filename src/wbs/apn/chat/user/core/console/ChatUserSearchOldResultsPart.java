package wbs.apn.chat.user.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.html.HtmlLink;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.misc.PageBuilder;
import wbs.console.misc.Percentager;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserSearchOldResultsPart")
public
class ChatUserSearchOldResultsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserCreditModeConsoleHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// value dependencies

	@SingletonDependency
	@Named ("chatUserSearchItemsPerSubPage")
	Integer itemsPerSubPage;

	@SingletonDependency
	@Named ("chatUserSearchSubPagesPerPage")
	Integer subPagesPerPage;

	// state

	ConsoleContext targetContext;

	List <ChatUserRec> chatUsers;

	PageBuilder pageBuilders [] =
		new PageBuilder [] {
			new PageBuilder (),
			new PageBuilder ()
		};

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/page-builder.js"))

			.build ();

	}

	@Override
	public
	Set<HtmlLink> links () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.applicationCssStyle (
				"/style/chat.css")

		);

	}

	// implementation

	@Override
	public
	void prepare () {

		ConsoleContextType targetContextType =
			consoleManager.contextType (
				"chatUser:combo",
				true);

		targetContext =
			consoleManager.relatedContextRequired (
				requestContext.consoleContext (),
				targetContextType);

		@SuppressWarnings ("unchecked")
		List<Long> chatUserSearchResult =
			(List<Long>)
			requestContext.requestRequired (
				"chatUserSearchResult");

		chatUsers =
			new ArrayList<ChatUserRec> ();

		for (
			Long chatUserIdObject
				: chatUserSearchResult
		) {

			chatUsers.add (
				chatUserHelper.findRequired (
					chatUserIdObject));

		}

		prepare0 ();

		prepare1 ();

	}

	private
	void prepare0 () {

		PageBuilder pageBuilder =
			pageBuilders [0];

		pageBuilder.headWriter ().writeFormat (
			"<table class=\"list\">\n");

		pageBuilder.headWriter ().writeFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>T</th>\n",
			"<th>G</th>\n",
			"<th>O</th>\n",
			"<th>Name</th>\n",
			"<th>Pic</th>\n",
			"<th>Info</th>\n",
			"</tr>\n");

		int index = 0;

		for (
			ChatUserRec chatUser
				: chatUsers
		) {

			pageBuilder.writer ().writeFormat (
				"<tr",
				" class=\"magic-table-row\"",
				" data-target-href=\"%h\"",
				requestContext.resolveContextUrl (
					stringFormat (
						"%s",
						targetContext.pathPrefix (),
						"/%u",
						chatUser.getId ())),
				">\n");

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				chatUser.getCode ());

			chatConsoleLogic.writeTdForChatUserTypeShort (
				pageBuilder.writer (),
				chatUser);

			chatConsoleLogic.writeTdForChatUserGenderShort (
				pageBuilder.writer (),
				chatUser);

			chatConsoleLogic.writeTdForChatUserOrientShort (
				pageBuilder.writer (),
				chatUser);

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				ifNull (chatUser.getName (), "-"));

			if (
				collectionIsNotEmpty (
					chatUser.getChatUserImageList ())
			) {

				htmlTableCellOpen (
					pageBuilder.writer ());

				mediaConsoleLogic.writeMediaThumb32 (
					pageBuilder.writer (),
					chatUser.getChatUserImageList ().get (0).getMedia ());

				htmlTableCellClose (
					pageBuilder.writer ());

			} else {

				pageBuilder.writer ().writeFormat (
					"<td>-</td>\n");

			}

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				ifNull (
					chatUser.getInfoText (),
					"-"));

			pageBuilder.writer ().writeFormat (
				"</tr>\n");

			if (index ++ == itemsPerSubPage) {

				pageBuilder.endPage ();

				index = 0;

			}

		}

		pageBuilder.endPage ();

		pageBuilder.footWriter ().writeFormat (
			"</table>\n");

	}

	private
	void prepare1 () {

		PageBuilder pageBuilder =
			pageBuilders [1];

		pageBuilder.headWriter ().writeFormat (
			"<table class=\"list\">\n");

		pageBuilder.headWriter ().writeFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>T</th>\n",
			"<th>G</th>\n",
			"<th>O</th>\n",
			"<th>Spent</th>\n",
			"<th>Credit</th>\n",
			"<th>Mode</th>\n",
			"<th>Pend</th>\n",
			"<th>Succ</th>\n",
			"<th>Fail</th>\n",
			"<th>Scheme</th>\n",
			"<th>Last action</th>\n",
			"<th>U</th>\n",
			"<th>M</th>\n",
			"<th>T</th>\n",
			"<th>I</th>\n",
			"</tr>");

		int index = 0;

		for (
			ChatUserRec chatUser
				: chatUsers
		) {

			Percentager percentager =
				new Percentager ();

			percentager.add (
				chatUser.getUserMessageCharge ());

			percentager.add (
				chatUser.getMonitorMessageCharge ());

			percentager.add (
				chatUser.getTextProfileCharge ());

			percentager.add (
				chatUser.getImageProfileCharge ());

			Iterator<Long> percentagerIterator =
				percentager.work ().iterator ();

			pageBuilder.writer ().writeFormat (
				"<tr",
				" class=\"magic-table-row\"",
				" data-target-href=\"%h\"",
				requestContext.resolveContextUrl (
					stringFormat (
						"/chatUser",
						"/%u",
						chatUser.getId (),
						"/chatUser.summary")),
				">\n");

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				chatUser.getCode ());

			chatConsoleLogic.writeTdsForChatUserTypeGenderOrientShort (
				pageBuilder.writer (),
				chatUser);

			pageBuilder.writer ().writeFormat (
				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getValueSinceEver ()));

			pageBuilder.writer ().writeFormat (
				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCredit ()));

			pageBuilder.writer ().writeFormat (
				"%s\n",
				chatUserCreditModeConsoleHelper.toTd (
					chatUser.getCreditMode ()));

			pageBuilder.writer ().writeFormat (
				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					sum (
						+ chatUser.getCreditPending (),
						+ chatUser.getCreditPendingStrict ())));

			pageBuilder.writer ().writeFormat (
				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditSuccess ()));

			pageBuilder.writer ().writeFormat (
				"%s\n",
				currencyLogic.formatHtmlTd (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditFailed ()));

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				chatUser.getChatScheme () != null
					? chatUser.getChatScheme ().getCode ()
					: "-");

			pageBuilder.writer ().writeFormat (
				"<td>%h</td>\n",
				chatUser.getLastAction () != null
					? timeFormatter.dateStringShort (
						chatUserLogic.getTimezone (
							chatUser),
						chatUser.getLastAction ())
					: "-");

			pageBuilder.writer ().writeFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next ());

			pageBuilder.writer ().writeFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next ());

			pageBuilder.writer ().writeFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next ());

			pageBuilder.writer ().writeFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				percentagerIterator.next ());

			pageBuilder.writer ().writeFormat (
				"</tr>\n");

			if (index ++ == itemsPerSubPage) {

				pageBuilder.endPage ();

				index = 0;

			}

		}

		pageBuilder.endPage ();

		pageBuilder.footWriter ().writeFormat (
			"</table>");

	}

	private
	void goPages () {

		if (pageBuilders [0].pages () <= 1)
			return;

		printFormat (
			"<p",
			" class=\"links\"",
			">Select page\n");

		for (
			int page = 0;
			page < pageBuilders [0].pages ();
			page ++
		) {

			printFormat (
				"<a",
				" class=\"little-page-link-%s\"",
				page,
				" href=\"#\"",
				" onclick=\"%h\"",
				joinWithSpace (
					stringFormat (
						"pageBuilder.showLittlePage (%s);",
						page),
					"magicTable.setupMagicHandlers ($('#pageHolder'));"),
				">%s</a>\n",
				page + 1);

		}

		printFormat (
			"</p>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p",
			" class=\"links\"",
			">Select mode\n");

		printFormat (
			"<a",
			" class=\"big-page-link-0\"",
			" href=\"#\"",
			" onclick=\"%h\"",
			joinWithSpace (
				"pageBuilder.showBigPage (0);",
				"magicTable.setupMagicHandlers ($('#pageHolder'));"),
			">Normal</a>\n");

		printFormat (
			"<a",
			" class=\"big-page-link-1\"",
			" href=\"#\"",
			" onclick=\"%h\"",
			joinWithSpace (
				"pageBuilder.showBigPage (1);",
				"magicTable.setupMagicHandlers ($('#pageHolder'));"),
			">Credit</a></p>\n");

		goPages ();

		printFormat (
			"<div id=\"pageHolder\">Please wait...</div>\n");

		goPages ();

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"var pages = [ [\n");

		pageBuilders [0].goPages (
			formatWriter);

		printFormat (
			"], [\n");

		pageBuilders [1].goPages (
			formatWriter);

		printFormat (
			"] ];\n");

		printFormat (
			"var pageBuilder =\n",
			"\tnew PageBuilder (pages);\n");

		printFormat (
			"$(function () {\n",
			"\tpageBuilder.init ();\n",
			"\tmagicTable.setupMagicHandlers ($('#pageHolder'));\n",
			"});\n");

		printFormat (
			"</script>\n");

	}

}