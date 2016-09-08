package wbs.framework.web;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;

@Accessors (fluent = true)
@PrototypeComponent ("actionRequestHandler")
public
class ActionRequestHandler
	implements RequestHandler {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	// properties

	@Getter @Setter
	Provider <Action> actionProvider;

	// utils

	public
	ActionRequestHandler action (
			final Action action) {

		actionProvider =
			new Provider<Action> () {

			@Override
			public
			Action get () {
				return action;
			}

		};

		return this;

	}

	public
	ActionRequestHandler actionName (
			String actionName) {

		return actionProvider (
			componentManager.getComponentProviderRequired (
				actionName,
				Action.class));

	}

	// implementation

	@Override
	public
	void handle ()
		throws
			ServletException,
			IOException {

		Action action =
			actionProvider.get ();

		Responder responder =
			action.handle ();

		if (responder == null)
			throw new NullPointerException ();

		responder.execute ();

	}

}
