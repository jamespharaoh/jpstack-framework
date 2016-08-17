package wbs.clients.apn.chat.user.core.console;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.clients.apn.chat.help.logic.ChatHelpLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.gsm.GsmUtils;

@PrototypeComponent ("chatUserHelpFormAction")
public
class ChatUserHelpFormAction
	extends ConsoleAction {

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserHelpFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		// get parameters

		String text =
			requestContext.parameterRequired (
				"text");

		// check parameters

		if (text.length() == 0) {
			requestContext.addError("Please type a message");
			return null;
		}

		if (!GsmUtils.isValidGsm(text)) {
			requestContext.addError("Reply contains invalid characters");
			return null;
		}

		if (GsmUtils.length(text) > 149) {
			requestContext.addError("Text is too long!");
			return null;
		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserHelpFormAction.goReal ()",
				this);

		// get objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		// send message

		chatHelpLogic.sendHelpMessage (
			userConsoleLogic.userRequired (),
			chatUser,
			text,
			Optional.<Long>absent (),
			Optional.<ChatHelpLogRec>absent ());

		transaction.commit ();

		// return

		requestContext.addNotice (
			"Message sent");

		return null;

	}

}
