package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("yes-no-field")
@PrototypeComponent ("yesNoFieldSpec")
public
class YesNoFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "default")
	Boolean defaultValue;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

	@DataAttribute
	Boolean hidden;

}
