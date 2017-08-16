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
@DataClass ("enum-field")
@PrototypeComponent ("enumFieldSpec")
public
class EnumFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "type",
		required = true,
		format = StringFormat.hyphenated)
	String typeName;

	@DataAttribute (
		name = "default",
		format = StringFormat.hyphenated)
	String defaultValue;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

}
