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
@DataClass ("integer-field")
@PrototypeComponent ("integerFieldSpec")
public
class IntegerFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "default")
	Long defaultValue;

	@DataAttribute (
		name = "minimum",
		format = StringFormat.integer)
	Long minimumValue;

	@DataAttribute (
		name = "maximum",
		format = StringFormat.integer)
	Long maximumValue;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

}
