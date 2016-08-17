package wbs.smsapps.broadcast.console;

import static wbs.framework.utils.etc.Misc.shouldNeverHappen;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.experimental.Accessors;
import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.list.console.NumberListNumberConsoleHelper;
import wbs.smsapps.broadcast.logic.BroadcastLogic;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

@Accessors (fluent = true)
@PrototypeComponent ("broadcastNumbersAction")
public
class BroadcastNumbersAction
	extends ConsoleAction {

	// dependencies

	@Inject
	BroadcastConsoleHelper broadcastHelper;

	@Inject
	BroadcastLogic broadcastLogic;

	@Inject
	BroadcastNumberConsoleHelper broadcastNumberHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	NumberListNumberConsoleHelper numberListNumberHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return responder ("broadcastNumbersResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"BroadcastNumbersAction.goReal ()",
				this);

		BroadcastRec broadcast =
			broadcastHelper.findRequired (
				requestContext.stuffInteger (
					"broadcastId"));

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		// parse numbers

		List<String> numbers;

		try {

			numbers =
				numberFormatLogic.parseLines (
					broadcastConfig.getNumberFormat (),
					requestContext.parameterRequired (
						"numbers"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addNotice (
				"Invalid number format");

			return null;

		}

		// check permissions

		if (! privChecker.canRecursive (
				broadcastConfig,
				"manage"))
			throw new RuntimeException ();

		// check state

		if (broadcast.getState () != BroadcastState.unsent) {

			requestContext.addError (
				stringFormat (
					"Can't modify numbers for broadcast in %s state",
					broadcast.getState ()));

			return null;

		}

		// add numbers

		BroadcastLogic.AddResult addResult;

		if (
			isPresent (
				requestContext.parameter (
					"add"))
		) {

			addResult =
				broadcastLogic.addNumbers (
					broadcast,
					numbers,
					userConsoleLogic.userRequired ());

		} else {

			addResult =
				new BroadcastLogic.AddResult ();

		}

		// remove numbers

		int numAlreadyRemoved = 0;
		int numRemoved = 0;

		if (
			isPresent (
				requestContext.parameter (
					"remove"))
		) {

			for (
				String numberString
					: numbers
			) {

				NumberRec numberRecord =
					numberHelper.findOrCreate (
						numberString);

				BroadcastNumberRec broadcastNumber =
					broadcastNumberHelper.find (
						broadcast,
						numberRecord);

				if (broadcastNumber == null) {

					numAlreadyRemoved ++;

					continue;

				}

				switch (broadcastNumber.getState ()) {

				case removed:

					numAlreadyRemoved ++;

					break;

				case accepted:

					broadcastNumber

						.setState (
							BroadcastNumberState.removed)

						.setRemovedByUser (
							userConsoleLogic.userRequired ());

					broadcast

						.setNumAccepted (
							broadcast.getNumAccepted () - 1)

						.setNumRemoved (
							broadcast.getNumRemoved () + 1)

						.setNumTotal (
							broadcast.getNumTotal () - 1);

					numRemoved ++;

					break;

				case rejected:

					broadcastNumber

						.setState (
							BroadcastNumberState.removed)

						.setRemovedByUser (
							userConsoleLogic.userRequired ());

					broadcast

						.setNumRejected (
							broadcast.getNumRejected () - 1)

						.setNumRemoved (
							broadcast.getNumRemoved () + 1);

					numRemoved ++;

					break;

				case sent:

					shouldNeverHappen ();

				}

			}

		}

		// events

		if (addResult.numAdded () > 0) {

			eventLogic.createEvent (
				"broadcast_numbers_added",
				userConsoleLogic.userRequired (),
				addResult.numAdded (),
				broadcast);

		}

		if (numRemoved > 0) {

			eventLogic.createEvent (
				"broadcast_numbers_removed",
				userConsoleLogic.userRequired (),
				numRemoved,
				broadcast);

		}

		// commit

		transaction.commit ();

		// report changes

		if (addResult.numAlreadyAdded () > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers already added",
					addResult.numAlreadyAdded ()));

		}

		if (addResult.numAlreadyRejected () > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers already rejected",
					addResult.numAlreadyRejected ()));

		}

		if (addResult.numAdded () > 0) {

			requestContext.addNotice (
				stringFormat (
					"%s numbers added",
					addResult.numAdded ()));

		}

		if (addResult.numRejected () > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers rejected",
					addResult.numRejected ()));

		}

		if (numAlreadyRemoved > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers already removed or never added",
					numAlreadyRemoved));

		}

		if (numRemoved > 0) {

			requestContext.addNotice (
				stringFormat (
					"%s numbers removed",
					numRemoved));

		}

		return null;

	}

}
