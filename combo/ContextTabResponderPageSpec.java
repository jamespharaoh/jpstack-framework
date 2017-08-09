package wbs.console.combo;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-tab-responder-page")
@PrototypeComponent ("contextTabResponderPageSpec")
public
class ContextTabResponderPageSpec
	implements ConsoleSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "tab")
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute (
		name = "file")
	String fileName;

	@DataAttribute (
		name = "responder")
	String responderName;

	@DataAttribute (
		name = "title")
	String pageTitle;

	@DataAttribute (
		name = "page-part")
	String pagePartName;

	@DataAttribute
	Boolean hideTab = false;

}
