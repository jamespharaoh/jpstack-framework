package wbs.framework.component.manager;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.logging.TaskLogger;

public
interface ComponentManager
	extends Closeable {

	Map <String, Pair <Class <?>, Object>> allSingletonComponents (
			TaskLogger parentTaskLogger);

	<ComponentType>
	Optional <ComponentProvider <ComponentType>> getComponentProvider (
			TaskLogger parentTaskLogger,
			String componentName,
			Class <ComponentType> componentClass,
			Boolean initialized);

	default <ComponentType>
	Optional <ComponentProvider <ComponentType>> getComponentProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		return getComponentProvider (
			parentTaskLogger,
			componentName,
			componentClass,
			true);

	}

	default <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull TaskLogger taskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Boolean initialized) {

		Optional <ComponentProvider <ComponentType>>
			componentProviderOptional =
				getComponentProvider (
					taskLogger,
					componentName,
					componentClass,
					initialized);

		if (
			optionalIsNotPresent (
				componentProviderOptional)
		) {
			return optionalAbsent ();
		}

		ComponentProvider <ComponentType> componentProvider =
			optionalGetRequired (
				componentProviderOptional);

		return optionalOf (
			componentProvider.provide (
				taskLogger));

	}

	default <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		return getComponent (
			parentTaskLogger,
			componentName,
			componentClass,
			true);

	}

	default <ComponentType>
	ComponentType getComponentRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Boolean initialise) {

		Optional <ComponentProvider <ComponentType>>
			componentProviderOptional =
				getComponentProvider (
					parentTaskLogger,
					componentName,
					componentClass,
					initialise);

		if (
			optionalIsNotPresent (
				componentProviderOptional)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such component %s of type %s",
					componentName,
					classNameSimple (
						componentClass)));

		}

		ComponentProvider <ComponentType> componentProvider =
			optionalGetRequired (
				componentProviderOptional);

		return componentProvider.provide (
			parentTaskLogger);

	}

	default <ComponentType>
	ComponentType getComponentRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		return getComponentRequired (
			parentTaskLogger,
			componentName,
			componentClass,
			true);

	}

	default <ComponentType>
	ComponentType getComponentOrElse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Supplier <ComponentType> orElse) {

		Optional <ComponentProvider <ComponentType>>
			componentProviderOptional =
				getComponentProvider (
					parentTaskLogger,
					componentName,
					componentClass);

		if (
			optionalIsNotPresent (
				componentProviderOptional)
		) {
			return orElse.get ();
		}

		ComponentProvider <ComponentType> componentProvider =
			optionalGetRequired (
				componentProviderOptional);

		return componentProvider.provide (
			parentTaskLogger);

	}

	default <ComponentType>
	ComponentProvider <ComponentType> getComponentProviderRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Boolean initialise) {

		Optional <ComponentProvider <ComponentType>> componentProvider =
			getComponentProvider (
				parentTaskLogger,
				componentName,
				componentClass);

		if (
			optionalIsNotPresent (
				componentProvider)
		) {
			throw new NoSuchElementException ();
		}

		return optionalGetRequired (
			componentProvider);

	}

	default <ComponentType>
	ComponentProvider <ComponentType> getComponentProviderRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		return getComponentProviderRequired (
			parentTaskLogger,
			componentName,
			componentClass,
			true);

	}

	List <String> requestComponentNames ();

	ComponentMetaData componentMetaData (
			Object component);

	void bootstrapComponent (
			Object component);

	void bootstrapComponent (
			Object component,
			String componentName);

	void initializeComponent (
			TaskLogger parentTaskLogger,
			Object component);

	@Override
	void close ();

}
