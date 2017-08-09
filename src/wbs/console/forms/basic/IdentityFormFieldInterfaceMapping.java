package wbs.console.forms.basic;

import static wbs.utils.etc.ResultUtils.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

import fj.data.Either;

@PrototypeComponent ("identityFormFieldInterfaceMapping")
public
class IdentityFormFieldInterfaceMapping <Container, Type>
	implements FormFieldInterfaceMapping <Container, Type, Type> {

	@Override
	public
	Either <Optional <Type>, String> interfaceToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Type> value) {

		return successResult (
			value);

	}

	@Override
	public
	Either <Optional <Type>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Type> value) {

		return successResult (
			value);

	}

}
