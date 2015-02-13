package wbs.imchat.core.api;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatMessageSendRequest {

	@DataAttribute
	String sessionSecret;

	@DataAttribute
	Long conversationId;

	@DataAttribute
	String messageText;

}