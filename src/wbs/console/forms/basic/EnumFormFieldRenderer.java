package wbs.console.forms.basic;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.Misc.toEnumRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
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
import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("enumFormFieldRenderer")
public
class EnumFormFieldRenderer <Container, Interface extends Enum <Interface>>
	implements FormFieldRenderer <Container, Interface> {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	EnumConsoleHelper <Interface> enumConsoleHelper;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.isPresent ()
				? camelToHyphen (
					interfaceValue.get ().name ())
				: "none",
			">");

	}

	@Override
	public
	void renderFormInput (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormInput");

		) {

			Optional <Interface> currentValue =
				formValuePresent (
						submission,
						formName)
					? resultValueRequired (
						formToInterface (
							transaction,
							submission,
							formName))
					: interfaceValue;

			htmlWriter.writeLineFormatIncreaseIndent (
				"<select",
				" id=\"%h.%h\"",
				formName,
				name,
				" name=\"%h.%h\"",
				formName,
				name,
				">");

			if (

				nullable ()

				|| optionalIsNotPresent (
					currentValue)

				|| enumInSafe (
					formType,
					FormType.create,
					FormType.search,
					FormType.update)

			) {

				htmlWriter.writeLineFormat (
					"<option",
					" value=\"none\"",
					currentValue.isPresent ()
						? ""
						: " selected",
					">&mdash;</option>");

			}

			for (
				Map.Entry <Interface, String> optionEntry
					: enumConsoleHelper.map ().entrySet ()
			) {

				Interface optionValue =
					optionEntry.getKey ();

				String optionLabel =
					optionEntry.getValue ();

				htmlWriter.writeLineFormat (
					"<option",
					" value=\"%h\"",
					camelToHyphen (
						optionValue.name ()),
					optionValue == currentValue.orNull ()
						? " selected"
						: "",
					">%h</option>",
					optionLabel);

			}

			htmlWriter.writeLineFormatDecreaseIndent (
				"</select>");

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormReset");

		) {

			javascriptWriter.writeLineFormat (
				"$(\"%j\").val (\"%h\");",
				stringFormat (
					"#%s\\.%s",
					formName,
					name),
				interfaceValue.isPresent ()
					? camelToHyphen (
						interfaceValue.get ().name ())
					: "none");

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.hasParameter (
			stringFormat (
				"%s.%s",
				formName,
				name ()));

	}

	@Override
	public
	Either <Optional <Interface>, String> formToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"formToInterface");

		) {

			String parameterValue =
				submission.parameter (
					stringFormat (
						"%s.%s",
						formName,
						name ()));

			if (
				stringEqualSafe (
					parameterValue,
					"none")
			) {

				return successResult (
					optionalAbsent ());

			} else {

				return successResult (
					optionalOf (
						toEnumRequired (
							enumConsoleHelper.enumClass (),
							hyphenToCamel (
								submission.parameter (
									stringFormat (
										"%s.%s",
										formName,
										name ()))))));

			}

		}

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValueOptional,
			boolean link) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlSimple");

		) {

			htmlWriter.writeLineFormat (
				"%h",
				optionalMapRequiredOrDefault (
					interfaceValue ->
						enumNameSpaces (
							interfaceValue),
					interfaceValueOptional,
					"—"));

		}

	}

	@Override
	public
	Optional <String> htmlClass (
			@NonNull Optional <Interface> interfaceValueOptional) {

		return optionalMapOptional (
			interfaceValueOptional,
			enumConsoleHelper::htmlClass);

	}

	@Override
	public
	void renderHtmlTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
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
			@NonNull Optional <Interface> interfaceValue,
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
