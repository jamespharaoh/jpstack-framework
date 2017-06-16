package wbs.integrations.fonix.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.utf8ToString;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromGeneric;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.fonix.logic.FonixLogic;
import wbs.integrations.fonix.model.FonixDeliveryStatusObjectHelper;
import wbs.integrations.fonix.model.FonixDeliveryStatusRec;
import wbs.integrations.fonix.model.FonixInboundLogObjectHelper;
import wbs.integrations.fonix.model.FonixInboundLogType;
import wbs.integrations.fonix.model.FonixRouteOutObjectHelper;
import wbs.integrations.fonix.model.FonixRouteOutRec;

import wbs.platform.text.web.TextResponder;

import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("fonixRouteReportApiLoggingAction")
public
class FonixRouteReportApiLoggingAction
	implements ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	FonixDeliveryStatusObjectHelper fonixDeliveryStatusHelper;

	@SingletonDependency
	FonixInboundLogObjectHelper fonixInboundLogHelper;

	@SingletonDependency
	FonixLogic fonixLogic;

	@SingletonDependency
	FonixRouteOutObjectHelper fonixRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SmsDeliveryReportLogic smsDeliveryReportLogic;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	FonixRouteReportRequest request;
	Boolean success = false;

	// implementation

	@Override
	public
	void processRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		// read and log request

		byte[] requestBytes =
			requestContext.requestBodyBytes ();

		debugWriter.writeString (
			"== REQUEST BODY ==\n\n");

		debugWriter.writeString (
			utf8ToString (
				requestBytes));

		debugWriter.writeString (
			"\n\n");

		// decode request

		request =
			new DataFromGeneric ()

			.fromMap (
				FonixRouteReportRequest.class,
				requestContext.parameterMapSimple ());

	}

	@Override
	public
	void updateDatabase (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"updateDatabase");

		) {

			// lookup route

			Optional <RouteRec> smsRouteOptional =
				smsRouteHelper.find (
					transaction,
					Long.parseLong (
						requestContext.requestStringRequired (
							"smsRouteId")));

			if (

				optionalIsNotPresent (
					smsRouteOptional)

				|| booleanEqual (
					smsRouteOptional.get ().getDeleted (),
					true)

				|| booleanEqual (
					smsRouteOptional.get ().getCanSend (),
					false)

				|| booleanEqual (
					smsRouteOptional.get ().getDeliveryReports (),
					false)

			) {

				throw new HttpNotFoundException (
					optionalAbsent (),
					emptyList ());

			}

			RouteRec smsRoute =
				optionalGetRequired (
					smsRouteOptional);

			// lookup fonix route in

			Optional <FonixRouteOutRec> fonixRouteOutOptional =
				fonixRouteOutHelper.find (
					transaction,
					smsRoute.getId ());

			if (

				optionalIsNotPresent (
					fonixRouteOutOptional)

				|| booleanEqual (
					fonixRouteOutOptional.get ().getDeleted (),
					true)

			) {

				throw new HttpNotFoundException (
					optionalAbsent (),
					emptyList ());

			}

			FonixRouteOutRec fonixRouteOut =
				optionalGetRequired (
					fonixRouteOutOptional);

			// process delivery report

			handleDeliveryReport (
				transaction,
				fonixRouteOut);

			// commit and return

			transaction.commit ();

			success = true;

		}

	}

	private
	void handleDeliveryReport (
			@NonNull Transaction parentTransaction,
			@NonNull FonixRouteOutRec fonixRouteOut) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handleDeliveryReport");

		) {

			// lookup the delivery status

			Optional <FonixDeliveryStatusRec> deliveryStatusOptional =
				fonixDeliveryStatusHelper.findByCode (
					transaction,
					fonixRouteOut.getFonixConfig (),
					lowercase (
						request.statusCode ()));

			if (
				optionalIsNotPresent (
					deliveryStatusOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Delivery status not recognised: %s",
						request.statusCode ()));

			}

			FonixDeliveryStatusRec deliveryStatus =
				optionalGetRequired (
					deliveryStatusOptional);

			// lookup the message

			Optional <MessageRec> smsMessageOptional =
				smsMessageLogic.findMessageByMangledId (
					transaction,
					request.requestId ());

			if (
				optionalIsNotPresent (
					smsMessageOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Message guid not recognised: %s",
						request.guid ()));

			}

			MessageRec smsMessage =
				optionalGetRequired (
					smsMessageOptional);

			// store the delivery report

			smsDeliveryReportLogic.deliveryReport (
				transaction,
				smsMessage,
				deliveryStatus.getMessageStatus (),
				optionalOf (
					request.statusCode ()),
				optionalOf  (
					request.statusText ()),
				optionalAbsent (),
				optionalOf (
					fonixLogic.stringToInstant (
						request.statusTime ())));

		}

	}

	@Override
	public
	WebResponder createResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		// encode response

		String responseString = "OK";

		// write to debug log

		debugWriter.writeString (
			"== RESPONSE BODY ==\n\n");

		debugWriter.writeString (
			responseString);

		debugWriter.writeString (
			"\n\n");

		// create responder

		return textResponderProvider.get ()

			.contentType (
				"text/plain")

			.text (
				responseString);

	}

	@Override
	public
	void storeLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String debugLog) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"storeLog");

		) {

			fonixInboundLogHelper.insert (
				transaction,
				fonixInboundLogHelper.createInstance ()

				.setRoute (
					smsRouteHelper.findRequired (
						transaction,
						parseIntegerRequired (
							requestContext.requestStringRequired (
								"smsRouteId"))))

				.setType (
					FonixInboundLogType.smsDelivery)

				.setTimestamp (
					transaction.now ())

				.setDetails (
					debugLog)

				.setSuccess (
					success)

			);

			transaction.commit ();

		}

	}

}