package wbs.services.messagetemplate.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeObjectHelperMethods;
import wbs.services.messagetemplate.model.MessageTemplateEntryTypeRec;

public
class MessageTemplateEntryTypeObjectHelperMethodsImplementation
	implements MessageTemplateEntryTypeObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	MessageTemplateEntryTypeObjectHelper messageTemplateEntryTypeHelper;

	// implementation

	@Override
	public
	MessageTemplateEntryTypeRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageTemplateDatabaseRec messageTemplateDatabase,
			@NonNull String code,
			@NonNull String name,
			@NonNull String description) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		Optional <MessageTemplateEntryTypeRec>
			existingMessageTemplateEntryType =
				messageTemplateEntryTypeHelper.findByCode (
					messageTemplateDatabase,
					code);

		if (
			optionalIsPresent (
				existingMessageTemplateEntryType)
		) {

			return optionalGetRequired (
				existingMessageTemplateEntryType);

		}

		MessageTemplateEntryTypeRec newMessageTemplateEntry =
			messageTemplateEntryTypeHelper.insert (
				taskLogger,
				messageTemplateEntryTypeHelper.createInstance ()

			.setMessageTemplateDatabase (
				messageTemplateDatabase)

			.setCode (
				code)

			.setName (
				name)

			.setDescription (
				description)

		);

		return newMessageTemplateEntry;

	}

}