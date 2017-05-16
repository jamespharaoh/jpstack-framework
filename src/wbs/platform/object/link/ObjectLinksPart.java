package wbs.platform.object.link;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.booleanToTrueFalse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.html.HtmlTableCheckWriter;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.user.model.UserObjectHelper;

import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectLinksPart")
public
class ObjectLinksPart <
	ObjectType extends Record <ObjectType>,
	TargetType extends Record <TargetType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <HtmlTableCheckWriter> htmlTableCheckWriterProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	String contextLinksField;

	@Getter @Setter
	ConsoleHelper <TargetType> targetHelper;

	@Getter @Setter
	ConsoleFormType <TargetType> targetFormType;

	@Getter @Setter
	String localFile;

	// state

	Record <?> contextObject;
	Set <?> contextLinks;

	List <TargetType> targetObjects;

	ConsoleForm <TargetType> targetForm;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {
		return scriptRefs;
	}

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

			contextObject =
				consoleHelper.lookupObject (
					transaction,
					requestContext.consoleContextStuffRequired ());

			contextLinks =
				genericCastUnchecked (
					PropertyUtils.propertyGetAuto (
						contextObject,
						contextLinksField));

			targetObjects =
				targetHelper.findAll (
					transaction);

			Collections.sort (
				targetObjects);

			targetForm =
				targetFormType.buildResponse (
					transaction,
					emptyMap (),
					targetObjects);

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

			// form open

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/" + localFile));

			// form controls

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

			// table open

			htmlTableOpenList ();

			// table header

			htmlTableRowOpen ();

			targetForm.outputTableHeadings (
				transaction);

			htmlTableHeaderCellWrite (
				"Member");

			htmlTableRowClose ();

			// table content

			for (
				TargetType targetObject
					: targetObjects
			) {

				if (
					! privChecker.canRecursive (
						transaction,
						targetObject,
						"manage")
				) {
					continue;
				}

				htmlTableRowOpen ();

				targetForm.outputTableCellsList (
					transaction,
					targetObject,
					true);

				htmlTableCheckWriterProvider.get ()

					.name (
						"link_" + targetObject.getId ())

					.label (
						"member")

					.value (
						contextLinks.contains (
							targetObject))

					.write (
						formatWriter);

				htmlTableRowClose ();

			}

			// table close

			htmlTableClose ();

			// form controls

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

			// form hidden fields

			for (
				Record<?> targetObject
					: targetObjects
			) {

				if (
					! privChecker.canRecursive (
						transaction,
						targetObject,
						"manage")
				) {
					continue;
				}

				formatWriter.writeLineFormat (
					"<input",
					" type=\"hidden\"",
					" name=\"old_link\"",
					" value=\"%h,%h\"",
					integerToDecimalString (
						targetObject.getId ()),
					booleanToTrueFalse (
						contextLinks.contains (
							targetObject)),
					">");

			}

			// form close

			htmlFormClose ();

			// flush scripts

			requestContext.flushScripts ();

		}

	}

	// data

	static
	Set <ScriptRef> scriptRefs =
		ImmutableSet.<ScriptRef> of (

		ConsoleApplicationScriptRef.javascript (
			"/js/DOM.js"),

		ConsoleApplicationScriptRef.javascript (
			"/js/wbs.js")

	);

}
