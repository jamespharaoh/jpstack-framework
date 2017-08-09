package wbs.console.priv;

import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.Misc.doNothing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
class NullUserPrivChecker
	implements UserPrivChecker {

	@Override
	public
	Long userIdRequired () {
		throw new RuntimeException ();
	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <? extends Record <?>> parentObjectClass,
			@NonNull Long parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> parentObject,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <Object, Collection <String>> map) {

		return false;

	}

	@Override
	public
	boolean canGrant (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return false;

	}

	@Override
	public
	void refresh (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	@Override
	public
	Set <Long> getObjectIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long parentTypeId) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Set <Long> getCanRecursiveObjectIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long parentTypeId,
			@NonNull String ... privCodes) {

		return emptySet ();

	}

}
