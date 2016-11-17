package wbs.console.priv;

import java.util.Collection;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "userPrivChecker",
	proxyInterface = UserPrivChecker.class)
public
class UserPrivCheckerImplementation
	implements UserPrivChecker {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivDataLoader privDataLoader;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <UserPrivCheckerBuilder> privCheckerBuilderProvider;

	// state

	UserPrivChecker target;

	// lifecycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		if (! consoleUserHelper.loggedIn ()) {

			target = null;

		} else {

			target =
				privCheckerBuilderProvider.get ()

				.userId (
					consoleUserHelper.loggedInUserIdRequired ())

				.build (
					taskLogger);

		}

	}

	// implementation

	@Override
	public
	boolean canRecursive (
			@NonNull Long privId) {

		return target.canRecursive (
			privId);

	}

	@Override
	public
	boolean canRecursive (
			GlobalId parentGlobalId,
			String... privCodes) {

		return target.canRecursive (
			parentGlobalId,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull Class<? extends Record<?>> parentClass,
			@NonNull Long parentId,
			@NonNull String... privCodes) {

		return target.canRecursive (
			parentClass,
			parentId,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull Record<?> parentObject,
			@NonNull String... privCodes) {

		return target.canRecursive (
			parentObject,
			privCodes);

	}

	@Override
	public
	boolean canSimple (
			GlobalId parentGlobalId,
			String... privCodes) {

		return target.canSimple (
			parentGlobalId,
			privCodes);

	}

	@Override
	public
	boolean canSimple (
			@NonNull Record<?> parentObject,
			@NonNull String... privCodes) {

		return target.canSimple (
			parentObject,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			Map<Object,Collection<String>> map) {

		return target.canRecursive (
			map);

	}

	@Override
	public
	boolean canGrant (
			@NonNull Long privId) {

		return target.canGrant (
			privId);

	}

	@Override
	public
	void refresh (
			@NonNull TaskLogger parentTaskLogger) {

		privDataLoader.refresh (
			parentTaskLogger);

		init (
			parentTaskLogger);

	}

}
