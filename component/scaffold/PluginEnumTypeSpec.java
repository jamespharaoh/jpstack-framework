package wbs.framework.component.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@DataClass ("enum-type")
public
class PluginEnumTypeSpec {

	/*
	@DataAncestor
	@Getter @Setter
	ProjectSpec project;
	*/

	@DataAncestor
	@Getter @Setter
	PluginSpec plugin;

	@DataParent
	@Getter @Setter
	PluginModelsSpec models;

	@DataAttribute (
		format = StringFormat.hyphenated)
	@Getter @Setter
	String name;

}
