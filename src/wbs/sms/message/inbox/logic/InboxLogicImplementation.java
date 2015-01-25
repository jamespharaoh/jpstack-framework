package wbs.sms.message.inbox.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.core.model.MessageTypeObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptObjectHelper;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("inboxLogic")
public
class InboxLogicImplementation
	implements InboxLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	InboxAttemptObjectHelper inboxAttemptHelper;

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageTypeObjectHelper messageTypeHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NumberLogic numberLogic;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	ObjectManager objectManager;

	// implementation

	@Override
	public
	MessageRec inboxInsert (
			@NonNull Optional<String> otherId,
			@NonNull TextRec text,
			@NonNull Object numFrom,
			@NonNull String numTo,
			@NonNull RouteRec route,
			@NonNull Optional<NetworkRec> optionalNetwork,
			@NonNull Optional<Instant> networkTime,
			@NonNull List<MediaRec> medias,
			@NonNull Optional<String> avStatus,
			@NonNull Optional<String> subject) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup basics

		RootRec root =
			rootHelper.find (0);

		// lookup the number

		NumberRec number =
			numberLogic.objectToNumber (numFrom);

		NetworkRec network =
			optionalNetwork.or (
				networkHelper.find (0));

		if (! route.getCanReceive ()) {

			throw new RuntimeException (
				stringFormat (
					"Cannot receive on route %s",
					route.getId ()));

		}

		// see if this otherId already exists and return existing message if so

		if (otherId.isPresent ()) {

			MessageRec existingMessage =
				messageHelper.findByOtherId (
					MessageDirection.in,
					route,
					otherId.get ());

			if (existingMessage != null) {

				// check the details match

				if (
					existingMessage.getDirection () != MessageDirection.in
					|| existingMessage.getText () != text
					|| existingMessage.getNumber () != number
					|| ! existingMessage.getNumTo ().equals (numTo)
					|| existingMessage.getNetwork () != network
				) {

					log.error ("Trying to insert inbox with duplicated other id, but other details don't match");
					log.error ("Other id: " + otherId);

					log.error ("Existing text: " + existingMessage.getText ().getText ());
					log.error ("Existing num from: " + existingMessage.getNumFrom ());
					log.error ("Existing num to: " + existingMessage.getNumTo ());
					log.error ("Existing network: " + existingMessage.getNetwork ().getId ());

					log.error ("New text: " + text.getText ());
					log.error ("New num from: " + number.getNumber ());
					log.error ("New num to: " + numTo);
					log.error ("New network: " + network.getId ());

					throw new RuntimeException (
						"Duplicated other id but message details don't match: "
						+ otherId);

				}

				// and return it

				return existingMessage;

			}

		}

		// create the message

		AffiliateRec systemAffiliate =
			affiliateHelper.findByCode (
				root,
				"system");

		MessageRec message =
			new MessageRec ()

			.setCreatedTime (
				instantToDate (
					transaction.now ()))

			.setDirection (
				MessageDirection.in)

			.setStatus (
				MessageStatus.pending)

			.setOtherId (
				otherId.orNull ())

			.setText (
				text)

			.setNumber (
				number)

			.setNumFrom (
				number.getNumber ())

			.setNumTo (
				numTo)

			.setCharge (
				route.getInCharge ())

			.setRoute (
				route)

			.setNetwork (
				network)

			.setNetworkTime (
				networkTime.isPresent ()
					? instantToDate (networkTime.get ())
					: null)

			.setService (
				serviceHelper.findByCode (
					root,
					"system"))

			.setAffiliate (
				systemAffiliate)

			.setBatch (
				batchHelper.find (0))

			.setAdultVerified (
				avStatus.orNull ())

			.setMessageType (
				messageTypeHelper.findByCode (
					GlobalId.root,
					medias.isEmpty ()
						? "sms"
						: "mms"))

			.setSubjectText (
				subject.isPresent ()
					? textHelper.findOrCreate (
						subject.get ())
					: null);

		message.getMedias ().addAll (
			medias);

		objectManager.insert (
			message);

		// create the inbox entry

		inboxHelper.insert (
			new InboxRec ()

			.setMessage (
				message)

			.setCreatedTime (
				transaction.now ())

			.setState (
				InboxState.pending)

			.setNextAttempt (
				transaction.now ())

		);

		log.info (
			stringFormat (
				"SMS %s %s %s %s %s %s",
				message.getId (),
				route.getCode (),
				message.getOtherId (),
				message.getNumFrom (),
				message.getNumTo (),
				message.getText ().getText ()));

		// update the number

		setNetworkFromMessage (
			message);

		// return

		return message;

	}

	@Override
	public
	InboxAttemptRec inboxProcessed (
			@NonNull MessageRec message,
			@NonNull Optional<ServiceRec> service,
			@NonNull Optional<AffiliateRec> affiliate,
			@NonNull CommandRec command) {

		Transaction transaction =
			database.currentTransaction ();

		InboxRec inbox =
			inboxHelper.find (
				message.getId ());

		// sanity check

		if (message.getStatus () != MessageStatus.pending) {

			throw new RuntimeException (
				stringFormat (
					"Message %d status %s invalid",
					message.getId (),
					message.getStatus ()));

		}

		if (inbox == null)
			throw new RuntimeException ();

		if (inbox.getState () != InboxState.pending)
			throw new RuntimeException ();

		// create inbox attempt

		InboxAttemptRec inboxAttempt =
			inboxAttemptHelper.insert (
				new InboxAttemptRec ()

			.setInbox (
				inbox)

			.setIndex (
				inbox.getNumAttempts ())

			.setTimestamp (
				transaction.now ())

			.setResult (
				InboxState.processed)

		);

		// update inbox

		inbox

			.setState (
				InboxState.processed)

			.setNumAttempts (
				inbox.getNumAttempts () + 1)

			.setNextAttempt (
				null);

		// update message

		messageLogic.messageStatus (
			message,
			MessageStatus.processed);

		message

			.setProcessedTime (
				instantToDate (
					transaction.now ()))

			.setService (
				service.or (
					message.getService ()))

			.setAffiliate (
				affiliate.or (
					message.getAffiliate ()))

			.setCommand (
				command);

		// return

		return inboxAttempt;

	}

	@Override
	public
	InboxAttemptRec inboxNotProcessed (
			@NonNull MessageRec message,
			@NonNull Optional<ServiceRec> service,
			@NonNull Optional<AffiliateRec> affiliate,
			@NonNull Optional<CommandRec> command,
			@NonNull String information) {

		log.info (
			stringFormat (
				"Not processed message: %s",
				information));

		Transaction transaction =
			database.currentTransaction ();

		InboxRec inbox =
			inboxHelper.find (
				message.getId ());

		// sanity check

		if (message.getStatus () != MessageStatus.pending) {

			throw new RuntimeException (
				stringFormat (
					"Message %d status %s invalid",
					message.getId (),
					message.getStatus ()));

		}

		if (inbox == null)
			throw new RuntimeException ();

		if (inbox.getState () != InboxState.pending)
			throw new RuntimeException ();

		// create inbox attempt

		InboxAttemptRec inboxAttempt =
			inboxAttemptHelper.insert (
				new InboxAttemptRec ()

			.setInbox (
				inbox)

			.setIndex (
				inbox.getNumAttempts ())

			.setTimestamp (
				transaction.now ())

			.setResult (
				InboxState.notProcessed)

		);

		// update inbox

		inbox

			.setState (
				InboxState.notProcessed)

			.setNumAttempts (
				inbox.getNumAttempts () + 1)

			.setNextAttempt (
				null);

		// update message

		messageLogic.messageStatus (
			message,
			MessageStatus.notProcessed);

		message

			.setProcessedTime (
				instantToDate (
					transaction.now ()))

			.setService (
				service.isPresent ()
					? service.get ()
					: message.getService ())

			.setAffiliate (
				affiliate.isPresent ()
					? affiliate.get ()
					: message.getAffiliate ())

			.setCommand (
				command.isPresent ()
					? command.get ()
					: message.getCommand ());

		// return

		return inboxAttempt;

	}

	void setNetworkFromMessage (
			@NonNull MessageRec message) {

		// sanity check

		if (
			message.getDirection ()
				!= MessageDirection.in
		) {
			throw new RuntimeException ();
		}

		// tools

		NumberRec number =
			message.getNumber ();

		RouteRec route =
			message.getRoute ();

		NetworkRec oldNetwork =
			number.getNetwork ();

		NetworkRec newNetwork =
			message.getNetwork ();

		// ignore if new network unknown

		if (newNetwork.getId () == 0)
			return;

		// ignore if no change

		if (
			equal (
				oldNetwork,
				newNetwork)
		) {
			return;
		}

		// route network behaviour

		switch (route.getNetworkBehaviour ()) {

		case neverUpdate:

			return;

		case updateIfUnknown:

			if (number.getNetwork ().getId () != 0)
				return;

			break;

		case updateKeepingVirtual:

			if (equal (
					oldNetwork.getVirtualNetworkOfNetwork (),
					newNetwork))
				return;

			break;

		case alwaysUpdate:

			break;

		default:

			throw new RuntimeException ();

		}

		// update it

		number

			.setNetwork (
				newNetwork);

		// create event

		eventLogic.createEvent (
			"number_network_from_message",
			number,
			oldNetwork,
			newNetwork,
			message);

	}

}