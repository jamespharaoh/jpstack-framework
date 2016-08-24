package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.CollectionUtils.collectionSize;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;

@Log4j
public
class ObjectHelperGeneratorTool {

	// dependencies

	@Inject
	EntityHelper entityHelper;

	// prototype dependencies

	@Inject
	Provider <ObjectHelperGenerator> objectHelperGeneratorProvider;

	// implementation

	public
	void generateObjectHelpers (
			@NonNull List <String> params) {

		List <Model> models =
			entityHelper.models ();

		log.info (
			stringFormat (
				"About to generate %s object helpers",
				collectionSize (
					models)));

		long numSuccess = 0;
		long numFailures = 0;

		for (
			Model model
				: models
		) {

			try {

				objectHelperGeneratorProvider.get ()

					.model (
						model)

					.generateHelper ();

				numSuccess ++;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error writing object helper for %s",
						model.objectName ()),
					exception);

				numFailures ++;

			}

		}

		log.info (
			stringFormat (
				"Successfully generated %s object helpers",
				numSuccess));

		if (numFailures > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					numFailures));

		}

	}

}
