package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenLayout;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.web.utils.HtmlUtils.htmlLinkWriteHtml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.spendlimit.logic.SmsSpendLimitLogic;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

import wbs.utils.time.TextualInterval;

import wbs.web.utils.HtmlUtils;

@PrototypeComponent ("manualResponderRequestPendingSummaryPart")
public
class ManualResponderRequestPendingSummaryPart
	extends AbstractPagePart {

	final
	long maxResults = 1000l;

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderNumberConsoleHelper manualResponderNumberHelper;

	@SingletonDependency
	ManualResponderRequestConsoleHelper manualResponderRequestHelper;

	@SingletonDependency
	@NamedDependency ("manualResponderRequestPendingCustomerFormType")
	ConsoleFormType <SmsCustomerRec> customerFormType;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@SingletonDependency
	SmsSpendLimitLogic smsSpendLimitLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	ConsoleForm <SmsCustomerRec> customerForm;

	ManualResponderRequestRec manualResponderRequest;
	ManualResponderNumberRec manualResponderNumber;
	ManualResponderRec manualResponder;

	SmsCustomerRec smsCustomer;
	SmsCustomerSessionRec smsCustomerSession;

	Optional <Long> spendAvailable;

	MessageRec message;
	NumberRec number;
	NetworkRec network;

	List <ManualResponderRequestRec> oldRequests;
	List <RouteBillInfo> routeBillInfos;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				JqueryEditableScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/manual-responder.js"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			manualResponderRequest =
				manualResponderRequestHelper.findFromContextRequired (
					transaction);

			manualResponderNumber =
				manualResponderRequest.getManualResponderNumber ();

			manualResponder =
				manualResponderNumber.getManualResponder ();

			message =
				manualResponderRequest.getMessage ();

			number =
				manualResponderRequest.getNumber ();

			network =
				number.getNetwork ();

			if (
				isNotNull (
					manualResponderNumber)
			) {

				smsCustomer =
					manualResponderNumber.getSmsCustomer ();

			}

			if (

				isNotNull (
					smsCustomer)

				&& moreThanZero (
					smsCustomer.getNumSessions ())

			) {

				smsCustomerSession =
					smsCustomerSessionHelper.findByIndexRequired (
						transaction,
						smsCustomer,
						smsCustomer.getNumSessions () - 1);

			}

			if (
				isNotNull (
					smsCustomer)
			) {

				customerForm =
					customerFormType.buildResponse (
						transaction,
						emptyMap (),
						smsCustomer);

			}

			// get routes

			Set <RouteRec> routes =
				new HashSet<> ();

			for (
				ManualResponderTemplateRec manualResponderTemplate
					: manualResponder.getTemplates ()
			) {

				RouteRec route =
					routerLogic.resolveRouter (
						transaction,
						manualResponderTemplate.getRouter ());

				if (route == null)
					continue;

				if (route.getOutCharge () == 0)
					continue;

				routes.add (
					route);

			}

			// get bill counts per route

			Instant startOfToday =
				LocalDate.now ()
					.toDateTimeAtStartOfDay ()
					.toInstant ();

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					transaction,
					manualResponder,
					"default");

			routeBillInfos =
				new ArrayList <RouteBillInfo> ();

			for (
				RouteRec route
					: routes
			) {

				long total = 0l;
				long thisService = 0l;

				MessageSearch messageSearch =
					new MessageSearch ()

					.numberId (
						number.getId ())

					.routeId (
						route.getId ())

					.createdTime (
						TextualInterval.after (
							userConsoleLogic.timezone (
								transaction),
							startOfToday))

					.direction (
						MessageDirection.out);

				List <MessageRec> messages =
					messageHelper.search (
						transaction,
						messageSearch);

				for (
					MessageRec message
						: messages
				) {

					if (
						enumInSafe (
							message.getStatus (),
							MessageStatus.undelivered,
							MessageStatus.cancelled,
							MessageStatus.reportTimedOut)
					) {

						continue;

					}

					if (
						enumNotInSafe (
							message.getStatus (),
							MessageStatus.pending,
							MessageStatus.sent,
							MessageStatus.submitted,
							MessageStatus.delivered)
					) {

						transaction.errorFormat (
							"Counting message in unknown state \"%s\"",
							enumNameSpaces (
								message.getStatus ()));

					}

					total +=
						route.getOutCharge ();

					if (
						referenceEqualWithClass (
							ServiceRec.class,
							message.getService (),
							defaultService)
					) {

						thisService +=
							route.getOutCharge ();

					}

				}

				routeBillInfos.add (
					new RouteBillInfo ()

					.route (
						route)

					.total (
						total)

					.thisService (
						thisService)

				);

			}

			Collections.sort (
				routeBillInfos);

			// check spend limit

			if (
				isNotNull (
					manualResponder.getSmsSpendLimiter ())
			) {

				spendAvailable =
					smsSpendLimitLogic.spendCheck (
						transaction,
						manualResponder.getSmsSpendLimiter (),
						manualResponderRequest.getNumber ());

			} else {

				spendAvailable =
					optionalAbsent ();

			}

			// get request history

			oldRequests =
				manualResponderRequestHelper.findRecentLimit (
					transaction,
					manualResponder,
					manualResponderRequest.getNumber (),
					maxResults + 1);

			Collections.sort (
				oldRequests);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (manualResponderRequest == null) {

				formatWriter.writeLineFormat (
					"<p>Not found</p>");

				return;

			}

			htmlDivOpen (
				htmlClassAttribute (
					"manual-responder-request-pending-summary"));

			htmlDivOpen (
				htmlClassAttribute (
					"layout-container"));

			htmlTableOpenLayout ();

			htmlTableRowOpen ();

			htmlTableCellWriteHtml (
				() -> goRequestDetails (
					transaction),
				htmlAttribute (
					"style",
					"width: 50%"));

			htmlTableCellWriteHtml (
				() -> {
					goCustomerDetails (
						transaction);
					goNotes ();
				},
				htmlAttribute (
					"style",
					"width: 50%"));

			htmlTableRowClose ();

			htmlTableClose ();

			htmlDivClose ();

			goBillHistory (
				transaction);

			goOperatorInfo (
				transaction);

			goSessionDetails (
				transaction);

			goRequestHistory (
				transaction);

			htmlDivClose ();

		}

	}

	void goRequestDetails (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goRequestDetails");

		) {

			htmlTableOpenDetails ();

			htmlTableDetailsRowWriteRaw (
				"Manual responder",
				() -> objectManager.writeTdForObjectMiniLink (
					transaction,
					manualResponder));

			htmlTableDetailsRowWrite (
				"Description",
				manualResponder.getDescription ());

			if (
				privChecker.canRecursive (
					transaction,
					manualResponder,
					"number")
			) {

				htmlTableDetailsRowWriteRaw (
					"Number",
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						number));

			}

			if (
				optionalIsPresent (
					spendAvailable)
			) {

				htmlTableDetailsRowWrite (
					"Daily limit",
					currencyLogic.formatText (
						manualResponder
							.getSmsSpendLimiter ()
								.getCurrency (),
						manualResponder
							.getSmsSpendLimiter ()
							.getDailySpendLimitAmount ()));

				htmlTableDetailsRowWrite (
					"Available to spend",
					currencyLogic.formatText (
						manualResponder
							.getSmsSpendLimiter ()
								.getCurrency (),
						spendAvailable.get ()));

			}

			htmlTableDetailsRowWriteRaw (
				"Network",
				() -> objectManager.writeTdForObjectMiniLink (
					transaction,
					network));

			htmlTableDetailsRowWriteRaw (
				"Message",
				() -> objectManager.writeTdForObjectMiniLink (
					transaction,
					message));

			htmlTableDetailsRowWrite (
				"Message text",
				message.getText ().getText (),
				htmlClassAttribute (
					"bigger"));

			for (
				MediaRec media
					: message.getMedias ()
			) {

				if (
					mediaLogic.isText (
						media)
				) {

					htmlTableDetailsRowWriteHtml (
						"Text media",
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							media));

				} else if (
					mediaLogic.isImage (
						media)
				) {

					htmlTableDetailsRowWriteRaw (
						"Image media",
						() -> htmlLinkWriteHtml (
							mediaConsoleLogic.mediaUrlScaled (
								transaction,
								media,
								600,
								600),
							() -> mediaConsoleLogic.writeMediaThumb100 (
								transaction,
								media)));

				} else {

					htmlTableDetailsRowWrite (
						"Media",
						"(unsupported media type)");

				}

			}

			htmlTableClose ();

		}

	}

	void goCustomerDetails (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCustomerDetails");

		) {

			htmlHeadingThreeWrite (
				"Customer details");

			if (
				isNull (
					smsCustomer)
			) {

				formatWriter.writeLineFormat (
					"<p>Customer management is not configured for this manual ",
					"responder service.</p>");

				return;

			}

			customerForm.outputDetailsTable (
				transaction);

		}

	}

	void goNotes () {

		htmlHeadingThreeWrite (
			"Notes");

		String notes;

		if (

			isNotNull (
				manualResponderNumber)

			&& isNotNull (
				manualResponderNumber.getNotesText ())

		) {

			notes =
				manualResponderNumber.getNotesText ().getText ();

		} else if (

			isNotNull (
				smsCustomer)

			&& isNotNull (
				smsCustomer.getNotesText ())

		) {

			notes =
				smsCustomer.getNotesText ().getText ();

		} else {

			notes = "";

		}

		formatWriter.writeLineFormat (
			"<p",
			" id=\"%h\"",
			stringFormat (
				"manualResponderNumberNote%s",
				integerToDecimalString (
					manualResponderRequest.getNumber ().getId ())),
			" class=\"mrNumberNoteEditable\"",
			">%s</p>",
			HtmlUtils.newlineToBr (
				HtmlUtils.htmlEncode (
					notes)));

	}

	void goBillHistory (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goBillHistory");

		) {

			if (! manualResponder.getShowDailyBillInfo ())
				return;

			if (routeBillInfos.isEmpty ())
				return;

			htmlHeadingTwoWrite (
				"Bill history for today");

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"Route",
				"All services",
				"This service");

			for (
				RouteBillInfo routeBillInfo
					: routeBillInfos
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					routeBillInfo.route ().getCode ());

				htmlTableCellWriteHtml (
					currencyLogic.formatHtml (
						manualResponder.getCurrency (),
						routeBillInfo.total ()));

				htmlTableCellWriteHtml (
					currencyLogic.formatHtml (
						manualResponder.getCurrency (),
						routeBillInfo.thisService ()));

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

	void goOperatorInfo (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goOperatorInfo");

		) {

			// TODO should be nullable to avoid this...

			if (manualResponder.getInfoText () == null)
				return;

			if (manualResponder.getInfoText ().getText ().length () == 0)
				return;

			htmlHeadingTwoWrite (
				"Operator info");

			formatWriter.writeString (
				manualResponder.getInfoText ().getText ());

		}

	}

	private
	void goSessionDetails (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goSessionDetails");

		) {

			if (smsCustomerSession == null)
				return;

			if (
				smsCustomerSession.getWelcomeMessage () == null
				&& smsCustomerSession.getWarningMessage () == null
			) {
				return;
			}

			htmlHeadingTwoWrite (
				"Customer session");

			htmlTableOpenDetails ();

			htmlTableDetailsRowWrite (
				"Start time",
				userConsoleLogic.timestampWithTimezoneString (
					transaction,
					smsCustomerSession.getStartTime ()));

			if (
				isNotNull (
					smsCustomerSession.getEndTime ())
			) {

				htmlTableDetailsRowWrite (
					"End time",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						smsCustomerSession.getStartTime ()));

			}

			if (smsCustomerSession.getWelcomeMessage () != null) {

				htmlTableDetailsRowWrite (
					"Welcome message",
					stringFormat (
						"%s (sent %s)",
						smsCustomerSession
							.getWelcomeMessage ()
							.getText ()
							.getText (),
						userConsoleLogic.timestampWithTimezoneString (
							transaction,
							smsCustomerSession
								.getWelcomeMessage ()
								.getCreatedTime ())));

			}

			if (smsCustomerSession.getWarningMessage () != null) {

				htmlTableDetailsRowWrite (
					"Warning message",
					stringFormat (
						"%s (sent %s)",
						smsCustomerSession
							.getWarningMessage ()
							.getText ()
							.getText (),
						userConsoleLogic.timestampWithTimezoneString (
							transaction,
							smsCustomerSession
								.getWarningMessage ()
								.getCreatedTime ())));

			}

			htmlTableClose ();

		}

	}

	private
	void goRequestHistory (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goRequestHistory");

		) {

			if (oldRequests.isEmpty ()) {
				return;
			}

			// title

			htmlHeadingTwoWrite (
				"Request history");

			// warning if we have omitted old requests

			if (oldRequests.size () > maxResults) {

				formatWriter.writeLineFormat (
					"<p class=\"warning\">%h</p>",
					stringFormat (
						"Only showing the first %s results.",
						integerToDecimalString (
							maxResults)));

			}

			// begin table

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"Timestamp",
				null,
				"Message",
				"Media",
				"User");

			// iterate requests

			for (
				ManualResponderRequestRec oldRequest
					: oldRequests
			) {

				htmlTableRowSeparatorWrite ();

				// iterate replies, which are shown before their requests

				for (
					ManualResponderReplyRec oldReply
						: oldRequest.getReplies ()
				) {

					// print reply

					htmlTableRowOpen (
						htmlClassAttribute (
							"message-out"));

					htmlTableCellWriteHtml (
						"&nbsp;");

					htmlTableCellWriteHtml (
						userConsoleLogic.timestampWithTimezoneString (
							transaction,
							oldReply.getTimestamp ()));

					htmlTableCellWriteHtml (
						oldReply.getText ().getText ());

					htmlTableCellWrite (
						"");

					htmlTableCellWrite (
						ifNotNullThenElseEmDash (
							oldReply.getUser (),
							() -> oldReply.getUser ().getUsername ()));

					htmlTableCellClose ();

				}

				// print request

				htmlTableRowOpen (
					htmlClassAttribute (
						"message-in"));

				htmlTableCellWrite (
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						oldRequest.getTimestamp ()),
					htmlColumnSpanAttribute (2l));

				htmlTableCellWrite (
					oldRequest.getMessage ().getText ().getText ());

				// print request medias

				htmlTableCellWriteHtml (
					() -> oldRequest.getMessage ().getMedias ().forEach (
						media -> {

					if (
						mediaLogic.isImage (
							media)
					) {

						mediaConsoleLogic.writeMediaThumb32 (
							transaction,
							media);

					}

				}));

				// leave request user blank

				htmlTableCellWrite (
					"");

				htmlTableRowClose ();

			}

			// close table

			htmlTableClose ();

		}

	}

	@Accessors (fluent = true)
	@Data
	static
	class RouteBillInfo
		implements Comparable <RouteBillInfo> {

		RouteRec route;

		Long total = 0l;
		Long thisService = 0l;

		@Override
		public
		int compareTo (
				RouteBillInfo other) {

			return new CompareToBuilder ()
				.append (route (), other.route ())
				.toComparison ();

		}

	}

}
