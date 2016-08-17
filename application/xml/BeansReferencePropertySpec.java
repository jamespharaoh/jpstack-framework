package wbs.framework.application.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.context.ComponentDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("reference-property")
public
class BeansReferencePropertySpec
	implements BeansBeanPropertySpec {

	@DataAncestor
	@Getter @Setter
	BeansSpec beans;

	@DataParent
	@Getter @Setter
	BeansBeanSpec bean;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String target;

	@Override
	public
	int register (
			ComponentDefinition beanDefinition) {

		beanDefinition.addReferenceProperty (
			name,
			target);

		return 0;

	}

}
