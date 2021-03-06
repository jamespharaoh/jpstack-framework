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
@DataClass ("timestamp-field")
@PrototypeComponent ("timestampFieldSpec")
public
class TimestampFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		required = true)
	ColumnType columnType;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "column",
		format = StringFormat.snakeCase)
	String columnName;

	public static
	enum ColumnType {
		unix,       // time since epoch
		iso,        // iso datetime string
		postgresql; // postgresql timestamp with time zone (java date)
	}

}
