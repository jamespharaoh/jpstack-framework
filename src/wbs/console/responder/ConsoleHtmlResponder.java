package wbs.console.responder;

import static wbs.framework.logging.TaskLogUtils.writeTaskLog;
import static wbs.utils.collection.CollectionUtils.listSorted;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.web.utils.HttpTimeUtils.httpTimestampString;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.BufferedTextResponder;

public abstract
class ConsoleHtmlResponder
	extends BufferedTextResponder {

	// singleton dependencies

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	UserPrivChecker privChecker;

	@SingletonDependency
	private
	ConsoleRequestContext requestContext;

	// details

	protected
	String getTitle () {
		return "Untitled";
	}

	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				myScriptRefs ())

			.build ();

	}

	protected
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.of ();

	}

	protected
	Set <HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink> builder ()

			.add (
				HtmlLink.applicationCssStyle (
					"/style/basic.css"))

			.add (
				HtmlLink.applicationIcon (
					"/favicon.ico"))

			.add (
				HtmlLink.applicationShortcutIcon (
					"/favicon.ico"))

			.addAll (
				myHtmlLinks ())

			.build ();

	}

	protected
	Set <HtmlLink> myHtmlLinks () {

		return ImmutableSet.of ();

	}

	@Override
	protected
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setHtmlHeaders");

		) {

			requestContext.contentType (
				"text/html",
				"utf-8");

			requestContext.setHeader (
				"Cache-Control",
				"no-cache");

			requestContext.setHeader (
				"Expiry",
				httpTimestampString (
					transaction.now ()));

		}

	}

	protected
	void renderHtmlDoctype (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<!DOCTYPE html>");

	}

	protected
	void renderHtmlStyleSheets (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<link",
			" rel=\"stylesheet\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/style/basic.css"),
			">");

	}

	protected
	void goMetaRefresh (
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	protected
	void goMeta (
			@NonNull FormatWriter formatWriter) {

		goMetaRefresh (
			formatWriter);

	}

	protected
	void renderHtmlTitle (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<title>%h</title>",
			getTitle ());

	}

	protected
	void renderHtmlScriptRefs (
			@NonNull FormatWriter formatWriter) {

		for (
			ScriptRef scriptRef
				: scriptRefs ()
		) {

			formatWriter.writeLineFormat (
				"<script",
				" type=\"%h\"",
				scriptRef.getType (),
				" src=\"%h\"",
				scriptRef.getUrl (
					requestContext),
				"></script>");

		}

	}

	protected
	void renderHtmlLinks (
			@NonNull FormatWriter formatWriter) {

		Set<? extends HtmlLink> links =
			htmlLinks ();

		if (links != null) {

			for (
				HtmlLink link
					: htmlLinks ()
			) {

				formatWriter.writeLineFormat (
					"%s",
					link.render (
						requestContext));

			}

		}

	}

	protected
	void renderHtmlHeadContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContents");

		) {

			renderHtmlTitle (
				formatWriter);

			renderHtmlScriptRefs (
				formatWriter);

			renderHtmlLinks (
				formatWriter);

			goMeta (
				formatWriter);

		}

	}

	protected
	void renderHtmlHead (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHead");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<head>");

			renderHtmlHeadContents (
				transaction,
				formatWriter);

			formatWriter.writeLineFormatDecreaseIndent (
				"</head>");

		}

	}

	protected
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	protected
	void renderHtmlBody (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBody");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<body>");

			renderHtmlBodyContents (
				transaction,
				formatWriter);

			formatWriter.writeLineFormatDecreaseIndent (
				"</body>");

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			renderHtmlDoctype (
				formatWriter);

			formatWriter.writeLineFormat (
				"<html>");

			renderHtmlHead (
				transaction,
				formatWriter);

			renderHtmlBody (
				transaction,
				formatWriter);

			formatWriter.writeLineFormat (
				"</html>");

			renderDebugInformation (
				transaction,
				formatWriter);

		}

	}

	private
	void renderDebugInformation (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderDebugInformation");

		) {

			if (
				! privChecker.canSimple (
					taskLogger,
					GlobalId.root,
					"debug")
			) {
				return;
			}

			formatWriter.writeLineFormatIncreaseIndent (
				"<!--");

			formatWriter.writeNewline ();

			if (
				optionalIsPresent (
					requestContext.consoleContext ())
			) {

				formatWriter.writeLineFormatIncreaseIndent (
					"Context data");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"Name: %s",
					requestContext.consoleContextRequired ().name ());

				formatWriter.writeLineFormat (
					"Type name: %s",
					requestContext.consoleContextRequired ().typeName ());

				formatWriter.writeLineFormat (
					"Path prefix: %s",
					requestContext.consoleContextRequired ().pathPrefix ());

				formatWriter.writeLineFormat (
					"Global: %s",
					booleanToYesNo (
						requestContext.consoleContextRequired ().global ()));

				if (
					isNotNull (
						requestContext.consoleContextRequired ().parentContextName ())
				) {

					formatWriter.writeLineFormat (
						"Parent context name: %s",
						requestContext.consoleContextRequired ().parentContextName ());

					formatWriter.writeLineFormat (
						"Parent context tab name: %s",
						requestContext.consoleContextRequired ().parentContextTabName ());

				}

				formatWriter.writeLineFormat ();

				if (
					optionalIsPresent (
						requestContext.foreignContextPath ())
				) {

					formatWriter.writeLineFormat (
						"Foreign context path: %s",
						requestContext.foreignContextPathRequired ());

				}

				if (
					optionalIsPresent (
						requestContext.changedContextPath ())
				) {

					formatWriter.writeLineFormat (
						"Changed context path: %s",
						requestContext.changedContextPathRequired ());

				}

				formatWriter.decreaseIndent ();

				formatWriter.writeNewline ();

			}

			if (
				optionalIsPresent (
					requestContext.consoleContextStuff ())
			) {

				formatWriter.writeLineFormatIncreaseIndent (
					"Context attributes");

				formatWriter.writeNewline ();

				for (
					Map.Entry <String, Object> attributeEntry
						: requestContext.consoleContextStuffRequired ()
							.attributes ()
							.entrySet ()
				) {

					formatWriter.writeLineFormat (
						"%s: %s",
						attributeEntry.getKey (),
						attributeEntry.getValue ().toString ());

				}

				formatWriter.decreaseIndent ();

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatIncreaseIndent (
					"Context privs");

				formatWriter.writeNewline ();

				for (
					String priv
						: listSorted (
							requestContext.consoleContextStuffRequired ().privs ())
				) {

					formatWriter.writeLineFormat (
						"%s",
						priv);

				}

				formatWriter.decreaseIndent ();

				formatWriter.writeNewline ();

			}

			formatWriter.writeLineFormatIncreaseIndent (
				"Task log");

			formatWriter.writeNewline ();

			writeTaskLog (
				formatWriter,
				taskLogger.getRoot ());

			formatWriter.decreaseIndent ();

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"-->");

		}

	}

}
