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
@DataClass ("parent-type-field")
@PrototypeComponent ("parentTypeFieldSpec")
public
class ParentTypeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

}
