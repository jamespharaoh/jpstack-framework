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
@DataClass ("parent-field")
@PrototypeComponent ("parentFieldSpec")
public
class ParentFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "type",
		required = true,
		format = StringFormat.hyphenated)
	String typeName;

	@DataAttribute (
		name = "column")
	String columnName;

	@DataAttribute
	Boolean cacheable;

}
