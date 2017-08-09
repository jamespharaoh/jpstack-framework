package wbs.console.tab;

import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("tabbedResponder")
public
class TabbedResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	Tab tab;

	@Getter @Setter
	String title;

	@Getter @Setter
	PagePart pagePart;

	// state

	Throwable pagePartThrew;

	List <MyLayer> myLayers =
		new ArrayList<> ();

	// details

	@Override
	protected
	Set <HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink> builder ()

			.addAll (
				super.htmlLinks ())

			.addAll (
				pagePart.links ())

			.build ();

	}

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		if (
			isNotNull (
				pagePart)
		) {

			return pagePart.scriptRefs ();

		} else {

			return emptySet ();

		}

	}

	@Override
	protected
	String getTitle () {

		return title;

	}

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			prepareTabs (
				transaction);

			preparePagePart (
				transaction);

		}

	}

	private
	void prepareTabs (
			@NonNull Transaction parentTransaction) {

		try (
			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareTabs");

		) {

			TabContext tabContext =
				requestContext.tabContextRequired ();

			MyLayer myLayer1 = null;

			for (
				TabContext.Layer tabContextLayer
					: tabContext.getLayers ()
			) {

				myLayers.add (
					myLayer1 =
						new MyLayer ()

					.title (
						tabContextLayer.title ())

					.tabList (
						tabContextLayer.tabList ())

					.tab (
						tabContextLayer.tab ())

				);

			}

			if (myLayer1 == null) {
				throw new RuntimeException ();
			}

			myLayer1.tab (tab);

		}

	}

	private
	void preparePagePart (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"preparePagePart");

		) {

			if (
				isNull (
					pagePart)
			) {
				return;
			}

			try {

				pagePart.prepare (
					transaction);

			} catch (RuntimeException exception) {

				String path =
					joinWithoutSeparator (
						requestContext.servletPath (),
						optionalOrEmptyString (
							requestContext.pathInfo ()));

				// log the exception

				transaction.warningFormatException (
					exception,
					"Exception while reponding to: %s",
					path);

				// record the exception

				exceptionLogger.logThrowable (
					transaction,
					"console",
					path,
					exception,
					consoleUserHelper.loggedInUserId (),
					GenericExceptionResolution.ignoreWithUserWarning);

				// and remember we had a problem

				pagePartThrew =
					exception;

				requestContext.addError (
					"Internal error");

			}

		}

	}

	@Override
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

			super.renderHtmlHeadContents (
				transaction,
				formatWriter);

			if (

				isNotNull (
					pagePart)

				&& isNull (
					pagePartThrew)

			) {

				pagePart.renderHtmlHeadContent (
					transaction,
					formatWriter);

			}

			htmlScriptBlockOpen (
				formatWriter);

			formatWriter.writeLineFormatIncreaseIndent (
				"function toggleHead (elem) {");

			formatWriter.writeLineFormatIncreaseIndent (
				"while (elem.nodeName.toLowerCase () != 'table') {");

			formatWriter.writeLineFormat (
				"elem = elem.parentNode;");

			formatWriter.writeLineFormatDecreaseIndent (
				"}");

			formatWriter.writeLineFormatIncreaseIndent (
				"if (elem.className == 'head-1-big') {");

			formatWriter.writeLineFormat (
				"elem.className = 'head-1-small';");

			formatWriter.writeLineFormatDecreaseIncreaseIndent (
				"} else if (elem.className == 'head-1-small') {");

			formatWriter.writeLineFormat (
				"elem.className = 'head-1-big';");

			formatWriter.writeLineFormatDecreaseIndent (
				"}");

			formatWriter.writeLineFormatDecreaseIndent (
				"}");

			htmlScriptBlockClose (
				formatWriter);

		}

	}

	protected
	void goTab () {
	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContents");

		) {

			htmlHeadingOneWrite (
				formatWriter,
				title);

			renderTabs (
				transaction,
				formatWriter);

			requestContext.flushNotices (
				formatWriter);

			renderPagePart (
				transaction,
				formatWriter);

		}

	}

	// private implementation

	private
	void renderTabs (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTabs");

		) {

			for (
				MyLayer myLayer
					: myLayers
			) {

				htmlTableOpen (
					formatWriter,
					htmlClassAttribute (
						"head-1-big"));

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					myLayer.title,
					htmlClassAttribute (
						"h"),
					htmlAttribute (
						"onclick",
						"toggleHead (this)"));

				htmlTableCellOpen (
					formatWriter,
					htmlClassAttribute (
						"l"));

				for (
					TabRef tabRef
						: myLayer.tabList.getTabRefs ()
				) {

					if (! tabRef.getTab ().isAvailable ())
						continue;

					if (tabRef.getTab () == myLayer.tab) {

						htmlLinkWrite (
							formatWriter,
							tabRef.getTab ().getUrl (
								transaction),
							tabRef.getLabel (),
							htmlClassAttribute (
								"selected"));

					} else {

						htmlLinkWrite (
							formatWriter,
							tabRef.getTab ().getUrl (
								transaction),
							tabRef.getLabel ());

					}

				}

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				htmlTableClose (
					formatWriter);

			}

			htmlDivWrite (
				formatWriter,
				"",
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"clear",
						"both"),
					htmlStyleRuleEntry (
						"border-top",
						"1px solid white"),
					htmlStyleRuleEntry (
						"margin-bottom",
						"1ex")));

		}

	}

	private
	void renderPagePart (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderPagePart");

		) {

			if (
				isNotNull (
					pagePartThrew)
			) {

				htmlParagraphWrite (
					formatWriter,
					"Unable to show page contents.");

				if (
					privChecker.canSimple (
						transaction,
						GlobalId.root,
						"debug")
				) {

					htmlParagraphWriteHtml (
						formatWriter,
						stringFormat (
							"<pre>%h</pre>",
							exceptionLogic.throwableDump (
								transaction,
								pagePartThrew)));

				}

			} else if (
				isNotNull (
					pagePart)
			) {

				pagePart.renderHtmlBodyContent (
					transaction,
					formatWriter);

			}

		}

	}

	@Accessors (fluent = true)
	@Data
	private static
	class MyLayer {
		String title;
		TabList tabList;
		Tab tab;
	}

}
