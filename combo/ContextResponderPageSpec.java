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
@DataClass ("context-responder-page")
@PrototypeComponent ("contextResponderPageSpec")
public
class ContextResponderPageSpec
	implements ConsoleSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String beanName;

	@DataAttribute (
		name = "file")
	String fileName;

	@DataAttribute (
		name = "responder")
	String responderName;

	/*
	@DataAttribute (
		name = "responder-bean")
	String responderBeanName;
	*/

}
