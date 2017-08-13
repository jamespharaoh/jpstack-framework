package wbs.framework.entity.meta.ids;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("assigned-id-field")
@PrototypeComponent ("assignedIdFieldSpec")
public
class AssignedIdFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		name = "column",
		format = StringFormat.hyphenated)
	String columnName;

}
