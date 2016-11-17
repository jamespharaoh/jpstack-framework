package wbs.console.responder;

import java.io.IOException;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("redirectResponder")
public
class RedirectResponder
	implements
		Provider <Responder>,
		Responder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String targetUrl;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		requestContext.sendRedirect (
			targetUrl);

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
