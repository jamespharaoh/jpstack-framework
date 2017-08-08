package wbs.console.forms.basic;

import static wbs.utils.etc.LogicUtils.booleanEqual;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("yesNoCsvFormFieldInterfaceMapping")
public
class YesNoCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,Boolean,String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Boolean nullable;

	// implementation

	@Override
	public
	Either <Optional <Boolean>, String> interfaceToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"interfaceToGeneric");

		) {

			String interfaceValue =
				optionalGetRequired (
					interfaceOptional);

			if (
				stringEqualSafe (
					interfaceValue,
					"yes")
			) {

				return successResult (
					optionalOf (
						true));

			} else if (
				stringEqualSafe (
					interfaceValue,
					"no")
			) {

				return successResult (
					Optional.of (
						false));

			} else if (
				stringEqualSafe (
					interfaceValue,
					"")
			) {

				if (nullable ()) {

					return successResult (
						Optional.absent ());

				} else {

					return errorResultFormat (
						"This is a required field");

				}

			} else {

				if (nullable ()) {

					return errorResultFormat (
						"This field must contain 'yes' or 'no', or be empty");

				} else {

					return errorResultFormat (
						"This field must contain 'yes' or 'no'");

				}

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return successResult (
					Optional.of (
						""));

			} else if (
				booleanEqual (
					optionalGetRequired (
						genericValue),
					true)
			) {

				return successResult (
					Optional.of (
						"yes"));

			} else if (
				booleanEqual (
					optionalGetRequired (
						genericValue),
					false)
			) {

				return successResult (
					Optional.of (
						"no"));

			} else {

				throw new IllegalArgumentException ();

			}

		}

	}

}
