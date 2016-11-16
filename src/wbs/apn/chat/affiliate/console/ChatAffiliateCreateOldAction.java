package wbs.apn.chat.affiliate.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivDataLoader;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.keyword.logic.KeywordLogic;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.console.ChatKeywordConsoleHelper;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.console.ChatSchemeConsoleHelper;
import wbs.apn.chat.scheme.console.ChatSchemeKeywordConsoleHelper;
import wbs.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("chatAffiliateCreateOldAction")
public
class ChatAffiliateCreateOldAction
	extends ConsoleAction {

	// dependencies

	@SingletonDependency
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatKeywordConsoleHelper chatKeywordHelper;

	@SingletonDependency
	ChatSchemeConsoleHelper chatSchemeHelper;

	@SingletonDependency
	ChatSchemeKeywordConsoleHelper chatSchemeKeywordHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserPrivDataLoader privDataLoader;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatAffiliateCreateResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		String name =
			requestContext.parameterRequired (
				"name");

		String code =
			simplifyToCodeRequired (
				name);

		Long chatSchemeId =
			Long.parseLong (
				requestContext.parameterRequired (
					"chatScheme"));

		// check keywords

		for (int i = 0; i < 3; i++) {

			String keyword =
				requestContext.parameterRequired (
					"keyword" + i);

			if (
				stringIsEmpty (
					keyword)
			) {
				continue;
			}

			if (! keywordLogic.checkKeyword (keyword)) {

				requestContext.addError (
					"keyword can only contain letters and numbers");

				return null;

			}

			if (
				stringIsEmpty (
					requestContext.parameterRequired (
						"joinType" + i))
			) {

				requestContext.addError (
					"Please specify a join type for each keyword");

				return null;

			}

		}

		Long chatAffiliateId;

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatAffiliateCreateOldAction.goReal ()",
					this);

		) {

			ChatRec chat =
				chatHelper.findRequired (
					requestContext.stuffInteger (
						"chatId"));

			ChatSchemeRec chatScheme =
				chatSchemeHelper.findRequired (
					chatSchemeId);

			if (chatScheme.getChat () != chat)
				throw new RuntimeException ();

			// check permissions

			if (! privChecker.canRecursive (
					chatScheme,
					"affiliate_create")) {

				requestContext.addError ("Access denied");

				return null;

			}

			// check uniqueness of code

			Optional <ChatAffiliateRec> existingChatAffiliateOptional =
				chatAffiliateHelper.findByCode (
					chatScheme,
					code);

			if (
				optionalIsPresent (
					existingChatAffiliateOptional)
			) {

				requestContext.addError (
					stringFormat (
						"That name is already in use."));

				return null;

			}

			// check uniqueness of keywords

			for (int i = 0; i < 3; i++) {

				String keyword =
					requestContext.parameterRequired (
						"keyword" + i);

				if (
					stringIsEmpty (
						keyword)
				) {
					continue;
				}

				Optional <ChatKeywordRec> existingChatKeywordOptional =
					chatKeywordHelper.findByCode (
						chat,
						keyword);

				if (
					optionalIsPresent (
						existingChatKeywordOptional)
				) {

					requestContext.addError (
						stringFormat (
							"Global keyword already exists: %s",
							keyword));

					return null;
				}

				Optional <ChatSchemeKeywordRec> existingChatSchemeKeywordOptional =
					chatSchemeKeywordHelper.findByCode (
						chatScheme,
						keyword);

				if (
					optionalIsPresent (
						existingChatSchemeKeywordOptional)
				) {

					requestContext.addError (
						stringFormat (
							"Keyword already exists: %s",
							keyword));

					return null;

				}

			}

			// create chat affiliate

			ChatAffiliateRec chatAffiliate =
				chatAffiliateHelper.insert (
					chatAffiliateHelper.createInstance ()

				.setChatScheme (
					chatScheme)

				.setName (
					name)

				.setCode (
					code)

				.setDescription (
					requestContext.parameterRequired (
						"description"))

			);

			chatAffiliateId =
				chatAffiliate.getId ();

			// create keywords

			for (int index = 0; index < 3; index ++) {

				String keyword =
					requestContext.parameterRequired (
						"keyword" + index);

				if (
					stringIsEmpty (
						keyword)
				) {
					continue;
				}

				chatSchemeKeywordHelper.insert (
					chatSchemeKeywordHelper.createInstance ()

					.setChatScheme (
						chatScheme)

					.setKeyword (
						keyword)

					.setJoinType (
						toEnum (
							ChatKeywordJoinType.class,
							requestContext.parameterRequired (
								"joinType" + index)))

					.setJoinGender (
						toEnum (
							Gender.class,
							requestContext.parameterRequired (
								"gender" + index)))

					.setJoinOrient (
						toEnum (
							Orient.class,
							requestContext.parameterRequired (
								"orient" + index)))

					.setJoinChatAffiliate (
						chatAffiliate)

				);

			}

			transaction.commit ();

		}

		// set up our new tab context

		requestContext.addNotice (
			"Chat affiliate created");

		requestContext.setEmptyFormData ();

		privChecker.refresh (
			taskLogger);

		ConsoleContext targetContext =
			consoleManager.context (
				"chatAffiliate",
				true);

		consoleManager.changeContext (
			taskLogger,
			targetContext,
			"/" + chatAffiliateId);

		return responder (
			"chatAffiliateSettingsResponder");

	}

}
