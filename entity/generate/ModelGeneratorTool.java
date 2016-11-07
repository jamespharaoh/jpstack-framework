package wbs.framework.entity.generate;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.Iterables;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.logging.TaskLogger;

@Log4j
public
class ModelGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	Provider <ModelRecordGenerator> modelRecordGeneratorProvider;

	@PrototypeDependency
	Provider <ModelInterfacesGenerator> modelInterfacesGeneratorProvider;

	// implementation

	public
	void generateModels (
			@NonNull TaskLogger taskLogger,
			@NonNull List <String> params) {

		taskLogger =
			taskLogger.nest (
				this,
				"generateModels",
				log);

		taskLogger.noticeFormat (
			"About to generate %s models",
			integerToDecimalString (
				modelMetaLoader.modelMetas ().size ()));

		StatusCounters statusCounters =
			new StatusCounters ();

		for (
			ModelMetaSpec modelMeta
				: Iterables.concat (
					modelMetaLoader.modelMetas ().values (),
					modelMetaLoader.componentMetas ().values ())
		) {

			PluginSpec plugin =
				modelMeta.plugin ();

			try {

				modelRecordGeneratorProvider.get ()

					.plugin (
						plugin)

					.modelMeta (
						modelMeta)

					.generateRecord (
						taskLogger);

				statusCounters.recordSuccessCount ++;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error writing model record for %s",
						modelMeta.name ()),
					exception);

				statusCounters.recordErrorCount ++;

			}

			if (modelMeta.type ().record ()) {

				try {

					modelInterfacesGeneratorProvider.get ()

						.plugin (
							plugin)

						.modelMeta (
							modelMeta)

						.generateInterfaces (
							taskLogger);

					statusCounters.interfacesSuccessCount ++;

				} catch (Exception exception) {

					log.error (
						stringFormat (
							"Error writing model interfaces for %s",
							modelMeta.name ()),
						exception);

					statusCounters.interfacesErrorCount ++;

				}

			}

		}

		taskLogger.noticeFormat (
			"Successfully generated %s records and %s interfaces",
			integerToDecimalString (
				statusCounters.recordSuccessCount),
			integerToDecimalString (
				statusCounters.interfacesSuccessCount));

		if (
			statusCounters.recordErrorCount > 0
			|| statusCounters.interfacesErrorCount > 0
		) {

			taskLogger.errorFormat (
				"Aborting due to %s errors",
				integerToDecimalString (
					+ statusCounters.recordErrorCount
					+ statusCounters.interfacesErrorCount));

		}

	}

	// data structures

	class StatusCounters {

		int recordSuccessCount = 0;
		int recordErrorCount = 0;

		int interfacesSuccessCount = 0;
		int interfacesErrorCount = 0;

	}

}
