package wbs.smsapps.forwarder.api;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcType;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderUnqueueExRpcHandler")
public
class ForwarderUnqueueExRpcHandler
	implements RpcHandler {

	@Inject
	Database database;

	@Inject
	ForwarderApiLogic forwarderApiLogic;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	ForwarderMessageOutReportObjectHelper forwarderMessageOutReportHelper;

	@Inject @Named
	RpcDefinition forwarderUnqueueExRequestDef;

	ForwarderRec forwarder;
	Boolean allowPartial;

	List<UnqueueExMessage> unqueueExMessages =
		new ArrayList<UnqueueExMessage>();

	List<UnqueueExReport> reports =
	new ArrayList<UnqueueExReport>();

	int numFailed = 0, numSuccess = 0;
	boolean cancel;

	List<String> statusMessages =
		new ArrayList<String>();

	@Override
	public
	RpcResult handle (
			RpcSource source) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ForwarderUnqueueExRpcHandler.handle (source)",
				this);

		// authenticate

		forwarder =
			forwarderApiLogic.rpcAuth (source);

		// get params

		getParams (source);

		// bail on any request-invalid classErrors

		if (statusMessages.size () > 0) {

			return Rpc.rpcError (
				"forwarder-unqueue-ex-response",
				Rpc.stRequestInvalid,
				"request-invalid",
				statusMessages);

		}

		// find unqueueExMessages and reports

		findMessages ();
		findReports ();

		if (numFailed > 0 && ! allowPartial)
			cancel = true;

		// unqueue them (unless we are cancelling)

		if (! cancel) {
			unqueueMessages ();
			unqueueReports ();
		}

		// return

		if (cancel)
			return makeCancelledResult ();

		transaction.commit ();

		return numFailed == 0
			? makeSuccessResult ()
			: makePartialResult ();

	}

	private
	void getParams (
			RpcSource source) {

		Map<String,Object> params =
			forwarderApiLogic.unsafeMapStringObject (
				source.obtain (
					forwarderUnqueueExRequestDef,
					statusMessages,
					true));

		allowPartial =
			(Boolean)
			params.get (
				"allow-partial");

		List <Map <String, Object>> messageParamsList =
			forwarderApiLogic.unsafeListMapStringObject (
				params.get (
					"unqueueExMessages"));

		if (messageParamsList != null) {

			for (
				Map <String, Object> messageParams
					: messageParamsList
			) {

				UnqueueExMessage unqueueExMessage =
					new UnqueueExMessage ();

				unqueueExMessage.serverId =
					(Long)
					messageParams.get (
						"server-id");

				unqueueExMessages.add (
					unqueueExMessage);

			}

		}

		List<Map<String,Object>> reportParamsList =
			forwarderApiLogic.unsafeListMapStringObject (
				params.get (
					"reports"));

		if (reportParamsList != null) {

			for (
				Map <String, Object> reportParams
					: reportParamsList
			) {

				UnqueueExReport unqueueExReport =
					new UnqueueExReport ();
				
				unqueueExReport.reportId =
					(Long)
					reportParams.get (
						"report-id");

				reports.add (
					unqueueExReport);

			}

		}

	}

	private
	void findMessages () {

		for (
			UnqueueExMessage unqueueExMessage
				: unqueueExMessages
		) {

			// lookup the message

			unqueueExMessage.forwarderMessageIn =
				optionalOrNull (
					forwarderMessageInHelper.find (
						unqueueExMessage.serverId));

			if (unqueueExMessage.forwarderMessageIn != null) {

				// if this report doesn't belong to this forwarder pretend
				// it doesn't exist

				if (unqueueExMessage.forwarderMessageIn.getForwarder() != forwarder)
					unqueueExMessage.forwarderMessageIn = null;

			}

			// keep track of whether any failed
			if (unqueueExMessage.forwarderMessageIn != null)
				numSuccess++;
			else
				numFailed++;
		}
	}

	private
	void findReports () {

		for (
			UnqueueExReport unqueueExReport
				: reports
		) {

			// lookup the report

			unqueueExReport.fmOutReport =
				forwarderMessageOutReportHelper.findRequired (
					unqueueExReport.reportId);

			if (
				isNotNull (
					unqueueExReport.fmOutReport)
			) {

				// if this report doesn't belong to this forwarder pretend
				// it doesn't exist

				if (
					notEqual (
						unqueueExReport.fmOutReport.getForwarderMessageOut ().getForwarder (),
						forwarder)
				) {
					unqueueExReport.fmOutReport = null;
				}

				// if this report is not pending yet then pretend it doesn't
				// exist
				else if (unqueueExReport.fmOutReport.getForwarderMessageOut()
						.getReportIndexPending() != null
						&& unqueueExReport.fmOutReport.getIndex() > unqueueExReport.fmOutReport
								.getForwarderMessageOut()
								.getReportIndexPending())
					unqueueExReport.fmOutReport = null;
			}

			// keep track of whether any failed
			if (unqueueExReport.fmOutReport != null)
				numSuccess++;
			else
				numFailed++;
		}
	}

	private
	void unqueueReports () {

		Transaction transaction =
			database.currentTransaction ();

		for (
			UnqueueExReport unqueueExReport
				: reports
		) {

			// skip any which weren't found

			if (unqueueExReport.fmOutReport == null)
				continue;

			// skip any which are already processed

			if (unqueueExReport.fmOutReport.getProcessedTime() != null)
				continue;

			unqueueExReport.fmOutReport

				.setProcessedTime (
					transaction.now ());

			ForwarderMessageOutRec forwarderMessageOut =
				unqueueExReport.fmOutReport
					.getForwarderMessageOut ();

			if (
				forwarderMessageOut.getReportIndexPending () + 1
					< forwarderMessageOut.getReportIndexNext ()
			) {

				forwarderMessageOut

					.setReportIndexPending (
						forwarderMessageOut.getReportIndexPending () + 1);

			} else {

				forwarderMessageOut

					.setReportIndexPending (
						null)

					.setReportRetryTime (
						null)

					.setReportTries (
						null);

			}

		}

	}

	private
	void unqueueMessages () {

		Transaction transaction =
			database.currentTransaction ();

		for (
			UnqueueExMessage unqueueExMessage
				: unqueueExMessages
		) {

			// skip any which weren't found

			if (unqueueExMessage.forwarderMessageIn == null)
				continue;

			// skip any which are already processed

			if (unqueueExMessage.forwarderMessageIn.getPending () == false)
				continue;

			// unqueue it

			unqueueExMessage.forwarderMessageIn

				.setProcessedTime (
					transaction.now ())

				.setPending (
					false)

				.setSendQueue (
					false)

				.setRetryTime (
					null);

		}

	}

	private
	RpcResult makePartialResult () {

		statusMessages.add ("Partial success");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			if (uem.forwarderMessageIn != null) {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							Rpc.stSuccess),

						Rpc.rpcElem (
							"status-code",
							"success"),

						Rpc.rpcElem (
							"status-message",
							"Success")));

			} else {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stMessageNotFound),

						Rpc.rpcElem (
							"status-code",
							"message-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Message not found")));

				statusMessages.add("Message " + i + ": Message not found");
			}
		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size (); i++) {

			UnqueueExReport uer =
				reports.get (i);

			if (uer.fmOutReport != null) {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							Rpc.stSuccess),

						Rpc.rpcElem (
							"status-code",
							"success"),

						Rpc.rpcElem (
							"status-message",
							"Success")));

			} else {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"report-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Report not found")));

				statusMessages.add (
					"Report " + i + ": Report not found");

			}

		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stPartialSuccess,
			"partial-success",
			statusMessages,

			Rpc.rpcElem (
				"unqueueExMessages",
				messagesPart),

			Rpc.rpcElem (
				"reports",
				reportsPart));

	}

	private
	RpcResult makeCancelledResult () {

		statusMessages.add ("Cancelled due to partial failure");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			if (uem.forwarderMessageIn != null) {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							Rpc.stCancelled),

						Rpc.rpcElem (
							"status-code",
							"cancelled"),

						Rpc.rpcElem (
							"status-message",
							"Cancelled due to other failure")));

				statusMessages.add (
					"Message " + i + ": Cancelled due to other failure");

			} else {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"message-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Message not found")));

				statusMessages.add (
					"Message " + i + ": Message not found");

			}
		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size(); i++) {
			UnqueueExReport uer = reports.get(i);

			if (uer.fmOutReport != null) {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							Rpc.stCancelled),

						Rpc.rpcElem (
							"status-code",
							"cancelled"),

						Rpc.rpcElem (
							"status-message",
							"Cancelled due to other failure")));

				statusMessages.add (
					"Report " + i + ": Cancelled due to other failure");

			} else {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"report-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Report not found")));

				statusMessages.add (
					"Report " + i + ": Report not found");

			}

		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stCancelled,
			"cancelled",
			statusMessages,
			messagesPart,
			reportsPart);

	}

	private
	RpcResult makeSuccessResult () {

		statusMessages.add ("Success");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			messagesPart.add (

				Rpc.rpcStruct (
					"message",

					Rpc.rpcElem (
						"server-id",
						uem.serverId),

					Rpc.rpcElem (
						"status",
						Rpc.stSuccess),

					Rpc.rpcElem (
						"status-code",
						"success"),

					Rpc.rpcElem (
						"status-message",
						"Success")));

		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size(); i++) {

			UnqueueExReport uer =
				reports.get (i);

			reportsPart.add (

				Rpc.rpcStruct (
					"report",

					Rpc.rpcElem (
						"report-id",
						uer.reportId),

					Rpc.rpcElem (
						"status",
						Rpc.stSuccess),

					Rpc.rpcElem (
						"status-code",
						"success"),

					Rpc.rpcElem (
						"status-message",
						"Success")));
		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stSuccess,
			"success",
			statusMessages,
			messagesPart,
			reportsPart);

	}

	static
	class UnqueueExMessage {
		Long serverId;
		ForwarderMessageInRec forwarderMessageIn;
	}

	static
	class UnqueueExReport {
		Long reportId;
		ForwarderMessageOutReportRec fmOutReport;
	}

}
