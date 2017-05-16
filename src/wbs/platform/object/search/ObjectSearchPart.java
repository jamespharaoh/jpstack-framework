package wbs.platform.object.search;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchPart")
public
class ObjectSearchPart <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable
>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	Class <SearchType> searchClass;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	ConsoleFormType <SearchType> searchFormContextBuilder;

	@Getter @Setter
	String fileName;

	@Getter @Setter

	// state

	SearchType cleanSearch;
	SearchType currentSearch;

	Map <String, Object> formHints;

	ConsoleForm <SearchType> searchFormContext;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

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

			ImmutableMap.Builder <String, Object> formHintsBuilder =
				ImmutableMap.builder ();

			if (consoleHelper.parentExists ()) {

				ConsoleHelper <?> parentHelper =
					objectManager.findConsoleHelperRequired (
						consoleHelper.parentClass ());

				Optional <Long> parentIdOptional =
					requestContext.stuffInteger (
						parentHelper.idKey ());

				if (
					optionalIsPresent (
						parentIdOptional)
				) {

					Record <?> parent =
						parentHelper.findRequired (
							transaction,
							optionalGetRequired (
								parentIdOptional));

					formHintsBuilder.put (
						consoleHelper.parentFieldName (),
						parent);

				}

			}

			formHints =
				formHintsBuilder.build ();

			searchFormContext =
				searchFormContextBuilder.buildResponse (
					transaction,
					formHints);

			currentSearch =
				searchFormContext.value ();

			cleanSearch =
				classInstantiate (
					searchFormContext.containerClass ());

			searchFormContext.setDefaults (
				transaction,
				cleanSearch);

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
					"/" + fileName));

			// search fields

			htmlTableOpenDetails ();

			searchFormContext.outputFormRows (
				transaction);

			htmlTableClose ();

			// form controls

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"search\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" value=\"reset form\"",
				" onclick=\"resetSearchForm (); return false;\"",
				">");

			htmlParagraphClose ();

			// form close

			htmlFormClose ();

			// form field scripts

			htmlScriptBlockOpen ();

			formatWriter.writeLineFormatIncreaseIndent (
				"function resetSearchForm () {");

			searchFormContext.outputFormReset (
				transaction,
				cleanSearch);

			formatWriter.writeLineFormatDecreaseIndent (
				"}");

			htmlScriptBlockClose ();

		}

	}

}
