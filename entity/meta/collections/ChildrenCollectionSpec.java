package wbs.framework.entity.meta.collections;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelCollectionSpec;
import wbs.framework.entity.meta.model.RecordSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("children-collection")
@PrototypeComponent ("childrenCollectionSpec")
public
class ChildrenCollectionSpec
	implements ModelCollectionSpec {

	@DataAncestor
	RecordSpec model;

	@DataAttribute (
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "type",
		required = true,
		format = StringFormat.hyphenated)
	String typeName;

	@DataAttribute (
		name = "join-column",
		format = StringFormat.snakeCase)
	String joinColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

}
