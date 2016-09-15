package wbs.apn.chat.adult.daemon;

import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.delivery.model.DeliveryTypeRec;

@PrototypeComponent ("chatAdultDeliveryHandler")
public
class ChatAdultDeliveryHandler
	implements DeliveryHandler {

	// dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatJoiner> joinerProvider;

	// implementation

	@Override
	public
	void handle (
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatAdultDeliveryHandler.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		DeliveryTypeRec deliveryType =
			message.getDeliveryType ();

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				delivery.getMessage ().getRef ());

		// work out if it is a join

		boolean join;

		if (
			stringEqualSafe (
				deliveryType.getCode (),
				"chat_adult")
		) {

			join = false;

		} else if (
			stringEqualSafe (
				deliveryType.getCode (),
				"chat_adult_join")
		) {

			join = true;

		} else {

			throw new RuntimeException (
				deliveryType.getCode ());

		}

		// ensure we are going to a successful delivery

		if (
			delivery.getOldMessageStatus ().isGoodType ()
			|| ! delivery.getNewMessageStatus ().isGoodType ()
		) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// find and update the chat user

		chatUserLogic.adultVerify (
			chatUser);

		// stop now if we are joining but there is no join type saved

		if (join
				&& chatUser.getNextJoinType () == null) {

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// send a confirmation message if we are not joining

		if (! join) {

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (delivery.getMessage ().getThreadId ()),
				"adult_confirm",
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			deliveryHelper.remove (
				delivery);

			transaction.commit ();

			return;

		}

		// joins are handled by the big nasty join command handler

		JoinType joinType =
			ChatJoiner.convertJoinType (
				chatUser.getNextJoinType ());

		joinerProvider.get ()

			.chatId (
				chatUser.getChat ().getId ())

			.joinType (
				joinType)

			.handleSimple ();

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"chat_adult",
			"chat_adult_join");

	}

}