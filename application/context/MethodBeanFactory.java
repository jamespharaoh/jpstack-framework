package wbs.framework.application.context;

import org.apache.commons.lang3.reflect.MethodUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class MethodBeanFactory
	implements ComponentFactory {

	@Getter @Setter
	Object factoryBean;

	@Getter @Setter
	String factoryMethodName;

	@Getter @Setter
	Boolean initialized;

	@Override
	@SneakyThrows (Exception.class)
	public
	Object makeComponent () {

		return MethodUtils.invokeMethod (
			factoryBean,
			factoryMethodName);

	}

}
