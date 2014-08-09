package wbs.framework.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wbs.framework.entity.annotations.meta.FieldMeta;
import wbs.framework.entity.annotations.meta.FieldMetaColumn;
import wbs.framework.entity.annotations.meta.FieldMetaField;
import wbs.framework.entity.model.ModelFieldType;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
@FieldMeta (
	modelFieldType = ModelFieldType.foreignId)
public
@interface ForeignIdField {

	@FieldMetaField
	String field ();

	@FieldMetaColumn
	String column ()
	default "";

}
