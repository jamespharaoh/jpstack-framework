package wbs.console.forms.text;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.ConsoleNamedFormFieldSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@Data
@DataClass ("text-area-field")
@PrototypeComponent ("textAreaFormFieldSpec")
public
class TextAreaFormFieldSpec
	implements ConsoleNamedFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Integer rows;

	@DataAttribute
	Integer cols;

	@DataAttribute
	String charCountFunction;

	@DataAttribute
	String charCountData;

	@Getter @Setter
	boolean dynamic;

	@Getter @Setter
	Record<?> parent;

	@DataAttribute (
		name = "update-hook-bean")
	String updateHookBeanName;

}
