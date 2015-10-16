package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("description-field")
@PrototypeComponent ("descriptionFieldSpec")
@ModelMetaData
public
class DescriptionFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

}