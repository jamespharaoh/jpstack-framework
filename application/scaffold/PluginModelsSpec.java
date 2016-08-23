package wbs.framework.application.scaffold;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("models")
public
class PluginModelsSpec {

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataChildren (
		direct = true,
		childElement = "component-type")
	@Getter @Setter
	List <PluginComponentTypeSpec> componentTypes =
		new ArrayList <> ();

	@DataChildren (
		direct = true,
		childElement = "custom-type")
	@Getter @Setter
	List <PluginCustomTypeSpec> customTypes =
		new ArrayList <> ();

	@DataChildren (
		direct = true,
		childElement = "enum-type")
	@Getter @Setter
	List <PluginEnumTypeSpec> enumTypes =
		new ArrayList <> ();

	@DataChildren (
		direct = true,
		childElement = "model")
	@Getter @Setter
	List <PluginModelSpec> models =
		new ArrayList<> ();

}
