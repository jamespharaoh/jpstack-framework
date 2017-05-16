package wbs.imchat.api;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatConversationObjectHelper;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageObjectHelper;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatMessageListAction")
public
class ImChatMessageListAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatConversationObjectHelper imChatConversationHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@SingletonDependency
	ImChatMessageObjectHelper imChatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			JSONObject jsonValue =
				(JSONObject)
				JSONValue.parse (
					requestContext.reader ());

			ImChatMessageListRequest request =
				dataFromJson.fromJson (
					ImChatMessageListRequest.class,
					jsonValue);

			// lookup session

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					request.sessionSecret ());

			if (
				session == null
				|| ! session.getActive ()
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"session-invalid")

					.message (
						"The session secret is invalid or the session is no " +
						"longer active");

				return jsonResponderProvider.get ()

					.value (
						failureResponse);

			}

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			// find conversation

			ImChatConversationRec conversation =
				imChatConversationHelper.findByIndexRequired (
					transaction,
					customer,
					request.conversationIndex ());

			if (
				isNotNull (
					conversation.getEndTime ())
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"conversation-ended")

					.message (
						"This conversation has already ended");

				return jsonResponderProvider.get ()

					.value (
						failureResponse);

			}

			// retrieve messages

			List<ImChatMessageRec> allMessages =
				ImmutableList.copyOf (
					conversation.getMessages ());

			List<ImChatMessageRec> newMessages =
				ImmutableList.copyOf (
					allMessages.subList (
						toJavaIntegerRequired (
							request.messageIndex ()),
						allMessages.size ()));

			// create response

			ImChatMessageListSuccess messageListSuccessResponse =
				new ImChatMessageListSuccess ()

				.customer (
					imChatApiLogic.customerData (
						transaction,
						customer))

				.conversation (
					imChatApiLogic.conversationData (
						transaction,
						conversation));

			for (
				ImChatMessageRec message
					: newMessages
			) {

				messageListSuccessResponse.messages.add (
					imChatApiLogic.messageData (
						transaction,
						message));

			}

			return jsonResponderProvider.get ()

				.value (
					messageListSuccessResponse);

		}

	}

}