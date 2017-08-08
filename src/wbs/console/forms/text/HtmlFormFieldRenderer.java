package wbs.console.forms.text;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("htmlFormFieldRenderer")
@Accessors (fluent = true)
public
class HtmlFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormInput");

		) {

			out.writeFormat (
				"%s",
				interfaceValue.get ());

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional <String> interfaceValue,
			@NonNull String formName) {

		doNothing ();

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return false;

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			boolean link) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlSimple");

		) {

			htmlWriter.writeFormat (
				"%s",
				interfaceValue.or (""));

		}

	}

	@Override
	public
	void renderHtmlTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellProperties");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					propertiesAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

	@Override
	public
	void renderHtmlTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellList");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					listAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan),
				htmlClassAttribute (
					presentInstances (
						htmlClass (
							interfaceValue))));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

}
