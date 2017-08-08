package wbs.console.forms.object;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.successOrElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanOne;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.ifPresentThenElse;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualWithClass;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import wbs.utils.data.ComparablePair;
import wbs.utils.data.Pair;
import wbs.utils.etc.OptionalUtils;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldRenderer")
public
class ObjectFormFieldRenderer <Container, Interface extends Record <Interface>>
	implements FormFieldRenderer <Container, Interface> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	String rootFieldName;

	@Getter @Setter
	ConsoleHelper <Interface> consoleHelper;

	@Getter @Setter
	Boolean mini;

	@Getter @Setter
	String optionLabel;

	@Getter @Setter
	Boolean search = false;

	@Getter @Setter
	Integer size = FormField.defaultSize;

	// public implementation

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
				? integerToDecimalString (
					interfaceValue.get ().getId ())
				: "none",
			">");

	}

	@Override
	public
	void renderFormInput (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter formatWriter,
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

			// lookup root

			Optional <Record <?>> root;

			if (
				isNotNull (
					rootFieldName)
			) {

				root =
					genericCastUnchecked (
						objectManager.dereference (
							transaction,
							container,
							rootFieldName,
							hints));

				if (
					optionalIsNotPresent (
						root)
				) {

					throw new RuntimeException (
						stringFormat (
							"Failed to look up root for %s: %s",
							name (),
							rootFieldName));

				}

			} else {

				root =
					optionalAbsent ();

			}

			// get current option

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

			// render control

			if (search) {

				renderFormInputSearch (
					transaction,
					formatWriter,
					interfaceValue,
					formType,
					formName,
					currentValue,
					root);

			} else {

				renderFormInputSelect (
					transaction,
					formatWriter,
					interfaceValue,
					formType,
					formName,
					currentValue,
					root);

			}

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
					? integerToDecimalString (
						interfaceValue.get ().getId ())
					: "none");

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return (

			submission.hasParameter (
				stringFormat (
					"%s.%s",
					formName,
					name ()))

		);

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

			String param =
				submission.parameter (
					stringFormat (
						"%s.%s",
						formName,
						name ()));

			if (
				stringEqualSafe (
					param,
					"none")
			) {

				return successResult (
					optionalAbsent ());

			} else {

				Long objectId =
					parseIntegerRequired (
						param);

				Interface interfaceValue =
					consoleHelper.findRequired (
						transaction,
						objectId);

				return successResult (
					optionalOf (
						interfaceValue));

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
			@NonNull Optional <Interface> interfaceValue,
			boolean link) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlSimple");

		) {

			// work out root

			Optional <Record <?>> root;

			if (rootFieldName != null) {

				root =
					genericCastUnchecked (
						objectManager.dereferenceRequired (
							transaction,
							container,
							rootFieldName));

			} else {

				root =
					optionalAbsent ();

			}

			// render object path

			if (
				OptionalUtils.optionalIsPresent (
					interfaceValue)
			) {

				htmlWriter.writeLineFormat (
					"%h",
					objectManager.objectPath (
						transaction,
						interfaceValue.get (),
						root,
						true,
						false));

			} else {

				htmlWriter.writeLineFormat (
					"&mdash;");

			}

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
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellList");

		) {

			// work out root

			Optional <Record <?>> rootOptional;

			if (

				optionalIsPresent (
					interfaceValue)

				&& isNotNull (
					rootFieldName)

			) {

				rootOptional =
					optionalOf (
						(Record <?>)
						objectManager.dereferenceRequired (
							transaction,
							container,
							rootFieldName));

			} else {

				rootOptional =
					optionalAbsent ();

			}

			// render table cell

			if (
				optionalIsPresent (
					interfaceValue)
			) {

				objectManager.writeTdForObject (
					transaction,
					formatWriter,
					privChecker,
					interfaceValue.orNull (),
					rootOptional,
					mini,
					link,
					columnSpan);

			} else if (
				moreThanOne (
					columnSpan)
			) {

				formatWriter.writeLineFormat (
					"<td colspan=\"%h\">—</td>",
					integerToDecimalString (
						columnSpan));

			} else {

				formatWriter.writeLineFormat (
					"<td>—</td>");

			}

		}

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
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellProperties");

		) {

			// work out root

			Optional <Record <?>> rootOptional;

			if (

				optionalIsPresent (
					interfaceValue)

				&& isNotNull (
					rootFieldName)

			) {

				rootOptional =
					optionalOf (
						(Record <?>)
						objectManager.dereferenceRequired (
							transaction,
							container,
							rootFieldName));

			} else {

				rootOptional =
					optionalAbsent ();

			}

			// render table cell

			if (
				optionalIsPresent (
					interfaceValue)
			) {

				objectManager.writeTdForObject (
					transaction,
					formatWriter,
					privChecker,
					interfaceValue.orNull (),
					rootOptional,
					mini,
					link,
					columnSpan);

			} else if (
				moreThanOne (
					columnSpan)
			) {

				formatWriter.writeLineFormat (
					"<td colspan=\"%h\">&mdash;</td>",
					integerToDecimalString (
						columnSpan));

			} else {

				formatWriter.writeLineFormat (
					"<td>&mdash;</td>");

			}

		}

	}

	// private implementation

	private
	void renderFormInputSelect (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName,
			@NonNull Optional <Interface> currentValue,
			@NonNull Optional <Record <?>> root) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormInputSelect");

		) {

			// get a list of options

			Collection <Interface> allOptions =
				consoleHelper.findNotDeleted (
					transaction);

			// filter visible options

			List <Interface> filteredOptions =
				allOptions.stream ()

				.filter (
					root.isPresent ()
						? item -> objectManager.isAncestor (
							transaction,
							item,
							root.get ())
						: item -> true)

				.filter (
					item ->

					(

						successOrElse (
							consoleHelper.getNotDeletedOrErrorCheckParents (
								transaction,
								item),
							error -> true)

						&& objectManager.canView (
							transaction,
							privChecker,
							item)

					) || (

						optionalIsPresent (
							interfaceValue)

						&& referenceEqualWithClass (
							consoleHelper.objectClass (),
							item,
							interfaceValue.get ())

					)

				)

				.collect (
					Collectors.toList ());

			// sort options by path

			Map <Pair <String, Long>, Record <?>> sortedOptions =
				new TreeMap<> ();

			for (
				Record <?> option
					: filteredOptions
			) {

				ComparablePair <String, Long> key =
					ComparablePair.of (
						ifNotNullThenElse (
							optionLabel,
							() -> dynamicCastRequired (
								String.class,
								objectManager.dereferenceRequired (
									transaction,
									option,
									optionLabel)),
							() -> objectManager.objectPathMiniPreload (
								transaction,
								option,
								root)),
						option.getId ());


				sortedOptions.put (
					key,
					option);

			}

			formatWriter.writeLineFormat (
				"<select",
				" id=\"%h.%h\"",
				formName,
				name,
				" name=\"%h.%h\"",
				formName,
				name,
				">");

			// none option

			if (

				nullable ()

				|| optionalIsNotPresent (
					currentValue)

				|| enumInSafe (
					formType,
					FormType.create,
					FormType.perform,
					FormType.search)

			) {

				formatWriter.writeLineFormat (
					"<option",
					" value=\"none\"",
					currentValue.isPresent ()
						? ""
						: " selected",
					">&mdash;</option>");

			}

			// value options

			for (
				Map.Entry <Pair <String, Long>, Record <?>> optionEntry
					: sortedOptions.entrySet ()
			) {

				String optionLabel =
					optionEntry.getKey ().left ();

				Record <?> optionValue =
					optionEntry.getValue ();

				ObjectHelper <?> objectHelper =
					objectManager.objectHelperForObjectRequired (
						optionValue);

				boolean selected =
					optionalValueEqualWithClass (
						objectHelper.objectClass (),
						genericCastUnchecked (
							currentValue),
						genericCastUnchecked (
							optionValue));

				if (

					! selected

					&& objectHelper.getDeleted (
						transaction,
						genericCastUnchecked (
							optionValue),
						true)

				) {
					continue;
				}

				formatWriter.writeLineFormat (
					"<option",
					" value=\"%h\"",
					integerToDecimalString (
						optionValue.getId ()),
					selected
						? " selected"
						: "",
					">%h</option>",
					optionLabel);

			}

			formatWriter.writeLineFormat (
				"</select>");

		}

	}

	private
	void renderFormInputSearch (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName,
			@NonNull Optional <Interface> currentValue,
			@NonNull Optional <Record <?>> rootOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormInputSearch");

		) {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" id=\"%h.%h\"",
				formName,
				name,
				" name=\"%h.%h\"",
				formName,
				name,
				" value=\"%h\"",
				interfaceValue.isPresent ()
					? integerToDecimalString (
						interfaceValue.get ().getId ())
					: "none",
				">");

			formatWriter.writeIndent ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" id=\"%h.%h.search\"",
				formName,
				name,
				" class=\"form-object-search-field\"",
				" size=\"%h\"",
				integerToDecimalString (
					size),
				" data-search-field-id=\"%h.%h\"",
				formName,
				name,
				" data-search-object-type-id=\"%h\"",
				integerToDecimalString (
					consoleHelper.objectTypeId ()));

			if (
				optionalIsPresent (
					rootOptional)
			) {

				Record <?> root =
					optionalGetRequired (
						rootOptional);

				ConsoleHelper <?> rootHelper =
					objectManager.consoleHelperForObjectRequired (
						genericCastUnchecked (
							root));

				formatWriter.writeFormat (
					" data-search-root-object-type-id=\"%h\"",
					integerToDecimalString (
						rootHelper.objectTypeId ()),
					" data-search-root-object-id=\"%h\"",
					integerToDecimalString (
						root.getId ()));

			}

			formatWriter.writeFormat (
				" value=\"%h\"",
				ifPresentThenElse (
					interfaceValue,
					() -> objectManager.objectPathMini (
						transaction,
						optionalGetRequired (
							interfaceValue),
						rootOptional),
					() -> ""),
				">");

			formatWriter.writeNewline ();

		}

	}

}
