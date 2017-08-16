package wbs.framework.entity.meta.identities;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("type-field")
@PrototypeComponent ("typeFieldSpec")
public
class TypeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.camelCase)
	String name;

	@DataAttribute (
		name = "type",
		format = StringFormat.hyphenated)
	String typeName;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

}
