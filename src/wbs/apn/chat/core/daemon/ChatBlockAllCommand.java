package wbs.apn.chat.core.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

@PrototypeComponent ("chatBlockAllCommand")
public
class ChatBlockAllCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.block_all"
		};

	}

	// implementation

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		ChatRec chat =
			chatHelper.find (
				command.getParentObjectId ());

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		// process inbox

		inboxLogic.inboxProcessed (
			message,
			defaultService,
			chatUserLogic.getAffiliate (chatUser),
			commandHelper.find (commandId));

		// send barred users to help

		/* disabled at sam wilson's request 10 april 2013

		if (! chatLogic.userSpendCheck (
				chatUser,
				true,
				message.getThreadId (),
				false)) {

			chatHelpLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			transaction.commit ();

			return null;
		}
		*/

		// call the block all function

		chatMiscLogic.blockAll (
			chatUser,
			message);

		transaction.commit ();

		return null;

	}

}