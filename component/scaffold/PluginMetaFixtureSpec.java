package wbs.framework.component.scaffold;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@DataClass ("meta-fixture")
public
class PluginMetaFixtureSpec {

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	@Getter @Setter
	String name;

}
