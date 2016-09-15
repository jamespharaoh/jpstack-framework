package wbs.imchat.console;

import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageObjectHelper;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatTemplateRec;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("imChatPendingFormResponder")
public
class ImChatPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ImChatMessageObjectHelper imChatMessageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatRec imChat;
	List <ImChatTemplateRec> templates;

	String summaryUrl;

	boolean manager;

	// details

	@Override
	protected
	Set<HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.applicationCssStyle (
				"/styles/im-chat.css")

		);

	}

	@Override
	public
	Set<ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			JqueryScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/im-chat.js")

		);

	}

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		message =
			imChatMessageHelper.findRequired (
				requestContext.stuffInteger (
					"imChatMessageId"));

		conversation =
			message.getImChatConversation ();

		customer =
			conversation.getImChatCustomer ();

		imChat =
			customer.getImChat ();

		summaryUrl =
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChat.pending",
					"/%u",
					message.getId (),
					"/imChat.pending.summary"));

		ImmutableList.Builder<ImChatTemplateRec> templatesBuilder =
			ImmutableList.<ImChatTemplateRec>builder ();

		for (
			ImChatTemplateRec template
				: imChat.getTemplates ()
		) {

			if (template.getDeleted ())
				continue;

			templatesBuilder.add (
				template);

		}

		templates =
			templatesBuilder.build ();

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = 'about:blank';\n",
			"window.setTimeout (function () {\n",
			"\ttop.frames ['main'].location = '%j';\n",
			summaryUrl,
			"}, 1);\n");

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices (
			formatWriter);

		renderLinks ();

		printFormat (
			"<h2>Reply to IM chat</h2>\n");

		renderForm ();

	}

	private
	void renderForm () {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChat.pending",
					"/%u",
					message.getId (),
					"/imChat.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table",
			" id=\"templates\"",
			" class=\"list\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</td>\n",
			"<th>Name</th>\n",
			"<th>Message</th>\n",
			"<th>Action</th>\n",
			"</tr>\n");

		renderBilledTemplate ();
		renderFreeTemplate ();

		for (
			ImChatTemplateRec template
				: templates
		) {

			renderTemplate (
				template);

		}

		renderIgnore ();

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	void renderLinks () {

		printFormat (
			"<p",
			" class=\"links\"",
			">\n");

		printFormat (
			"<a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a>\n");

		printFormat (
			"<a",
			" href=\"%h\"",
			summaryUrl,
			" target=\"main\"",
			">Summary</a>\n");

		printFormat (
			"<a",
			" href=\"javascript:top.show_inbox (false);\"",
			">Close</a>\n");

		printFormat (
			"</p>\n");

	}

	void renderBilledTemplate () {

		if (

			! imChat.getBillMessageEnabled ()

			|| lessThan (
				customer.getBalance (),
				imChat.getMessageCost ())

		) {
			return;
		}

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"bill\"",
			" data-minimum=\"%h\"",
			imChat.getBillMessageMinChars (),
			" data-maximum=\"%h\"",
			imChat.getBillMessageMaxChars (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-bill\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"bill\"",
			"></td>\n");

		printFormat (
			"<td>Bill&nbsp;%s</td>\n",
			currencyLogic.formatHtml (
				imChat.getCreditCurrency (),
				imChat.getMessageCost ()));

		printFormat (
			"<td",
			" style=\"width: 100%%\"",
			"><textarea",
			" class=\"template-text\"",
			" name=\"message-bill\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>\n",
			requestContext.parameterOrEmptyString (
				"message-bill"),
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span></td>\n");

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

	void renderFreeTemplate () {

		if (! imChat.getFreeMessageEnabled ()) {
			return;
		}

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"free\"",
			" data-minimum=\"%h\"",
			imChat.getFreeMessageMinChars (),
			" data-maximum=\"%h\"",
			imChat.getFreeMessageMaxChars (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-free\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"free\"",
			"></td>\n");

		printFormat (
			"<td>Free</td>\n");

		printFormat (
			"<td",
			" style=\"width: 100%%\"",
			"><textarea",
			" class=\"template-text\"",
			" name=\"message-free\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>\n",
			requestContext.parameterOrEmptyString (
				"message-free"),
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span></td>\n");

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

	void renderTemplate (
			ImChatTemplateRec template) {

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"%h\"",
			template.getId (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-%h\"",
			template.getId (),
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"%h\"",
			template.getId (),
			"></td>\n");

		printFormat (
			"<td>%s</td>\n",
			HtmlUtils.htmlEncodeNonBreakingWhitespace (
				template.getName ()));

		printFormat (
			"<td>%h</td>\n",
			template.getText ());

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</td>\n");

	}

	void renderIgnore () {

		if (
			! privChecker.canRecursive (
				imChat,
				"supervisor")
		) {
			return;
		}

		printFormat (
			"<tr",
			" class=\"template\"",
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-ignore\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"ignore\"",
			"></td>\n");

		printFormat (
			"<td>Ignore</td>\n");

		printFormat (
			"<td></td>\n");

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"ignore\"",
			" value=\"Ignore\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</td>\n");

	}

}