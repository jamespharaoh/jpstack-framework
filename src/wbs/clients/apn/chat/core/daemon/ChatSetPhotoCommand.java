package wbs.clients.apn.chat.core.daemon;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("chatSetPhotoCommand")
public
class ChatSetPhotoCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandLogic commandLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String[] {
			"chat.set_photo",
			"chat_scheme.photo_set"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat;
		ChatSchemeRec chatScheme;

		Object parent =
			objectManager.getParent (
				command);

		if (parent instanceof ChatRec) {

			chat = (ChatRec) parent;
			chatScheme = null;

		} else if (parent instanceof ChatSchemeRec) {

			chatScheme = (ChatSchemeRec) parent;
			chat = chatScheme.getChat ();

		} else {

			throw new RuntimeException ();

		}

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		MessageRec message =
			inbox.getMessage ();

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// set chat scheme

		if (chatScheme != null) {

			chatUserLogic.setScheme (
				chatUser,
				chatScheme);

		}

		// try set photo

		ChatUserImageRec chatUserImage =
			chatUserLogic.setPhotoFromMessage (
				chatUser,
				message,
				false);

		if (chatUserImage != null) {

			// send confirmation

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.<Integer>absent (),
				"photo_confirm",
				commandHelper.findByCode (chat, "magic"),
				commandHelper.findByCode (chat, "help").getId (),
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			// auto join

			chatMiscLogic.userAutoJoin (
				chatUser,
				message,
				true);

		// try set video

		} else if (
			chatUserLogic.setVideo (
				chatUser,
				message,
				false)
		) {

			// send confirmation

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"video_set_pending",
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			// auto join

			chatMiscLogic.userAutoJoin (
				chatUser,
				message,
				true);

		} else {

			// send error

			chatSendLogic.sendSystemMmsFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"photo_error",
				commandHelper.findByCode (chat, "set_photo"),
				TemplateMissing.error);

		}

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}