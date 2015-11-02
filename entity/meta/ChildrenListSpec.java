package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("children-list")
@PrototypeComponent ("childrenListSpec")
@ModelMetaData
public
class ChildrenListSpec
	implements ModelCollectionSpec {

	@DataAncestor
	ModelMetaSpec model;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

	@DataAttribute (
		name = "list-index-column")
	String listIndexColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

}
