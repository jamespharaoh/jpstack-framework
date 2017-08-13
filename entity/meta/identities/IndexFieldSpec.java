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
@DataClass ("index-field")
@PrototypeComponent ("indexFieldSpec")
public
class IndexFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "counter",
		format = StringFormat.hyphenated)
	String counterName;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

	@DataAttribute (
		name = "cacheable")
	Boolean cacheable;

}
