package wbs.platform.object.criteria;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@DataClass ("where-not-null")
@PrototypeComponent ("whereNotNullCriteriaSpec")
@ConsoleModuleData
public
class WhereNotNullCriteriaSpec
	implements CriteriaSpec {

	@DataAttribute (
		value = "field",
		required = true)
	@Getter @Setter
	String fieldName;

	@Override
	public boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		Object fieldValue =
			BeanLogic.getProperty (
				object,
				fieldName);

		return fieldValue != null;

	}

}