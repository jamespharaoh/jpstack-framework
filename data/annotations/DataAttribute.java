package wbs.framework.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wbs.utils.string.StringFormat;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public
@interface DataAttribute {

	String name ()
	default "";

	String collection ()
	default "";

	boolean required ()
	default false;

	String valueMap ()
	default "";

	StringFormat format ()
	default StringFormat.text;

}
