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
@DataClass ("active-field")
@PrototypeComponent ("activeFieldSpec")
public
class ActiveFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		format = StringFormat.snakeCase)
	String columnName;

}
