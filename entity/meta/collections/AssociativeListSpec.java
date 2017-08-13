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
@DataClass ("associative-list")
@PrototypeComponent ("associativeListSpec")
public
class AssociativeListSpec
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
		name = "table",
		required = true,
		format = StringFormat.snakeCase)
	String tableName;

	@DataAttribute (
		name = "join-column",
		format = StringFormat.snakeCase)
	String joinColumnName;

	@DataAttribute (
		name = "index-column",
		format = StringFormat.snakeCase)
	String indexColumnName;

	@DataAttribute (
		name = "value-column",
		format = StringFormat.snakeCase)
	String valueColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

	@DataAttribute
	Boolean owned;

}
