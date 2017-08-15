package wbs.framework.component.scaffold;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.IterableUtils.iterableOrderToList;
import static wbs.utils.etc.LogicUtils.notEqualWithClass;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.data.annotations.DataSetupMethod;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.JpStringComparator;

@Accessors (fluent = true)
@DataClass ("layer")
public
class PluginLayerSpec {

	// ancestry

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	// attributes

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	// children

	@DataChildren (
		direct = true,
		childElement = "bootstrap-component")
	@Getter @Setter
	List <PluginBootstrapComponentSpec> bootstrapComponents =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		childElement = "component")
	@Getter @Setter
	List <PluginComponentSpec> components =
		new ArrayList<> ();

	// setup

	@DataSetupMethod
	public
	void setup (
			@NonNull TaskLogger taskLogger) {

		List <String> bootstrapComponentClassNames =
			iterableMapToList (
				bootstrapComponents,
				PluginBootstrapComponentSpec::className);

		if (
			notEqualWithClass (
				List.class,
				bootstrapComponentClassNames,
				iterableOrderToList (
					bootstrapComponentClassNames,
					JpStringComparator.instance))
		) {

			taskLogger.errorFormat (
				"Bootstrap component classes not in order for plugin '%s' ",
				plugin.name (),
				"layer '%s', ",
				name (),
				"correct order is: %s",
				joinWithCommaAndSpace (
					iterableOrderToList (
						bootstrapComponentClassNames,
						JpStringComparator.instance)));

		}

		List <String> componentClassNames =
			iterableMapToList (
				components,
				PluginComponentSpec::className);

		if (
			notEqualWithClass (
				List.class,
				componentClassNames,
				iterableOrderToList (
					componentClassNames,
					JpStringComparator.instance))
		) {

			taskLogger.errorFormat (
				"Component classes not in order for plugin '%s' ",
				plugin.name (),
				"layer '%s', ",
				name (),
				"correct order is: %s",
				joinWithCommaAndSpace (
					iterableOrderToList (
						componentClassNames,
						JpStringComparator.instance)));

		}

	}

}
