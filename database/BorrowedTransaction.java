package wbs.framework.database;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.ParentTaskLogger;
import wbs.framework.logging.RealTaskLogger;
import wbs.framework.logging.TaskLoggerDefault;

public
class BorrowedTransaction
	implements
		TaskLoggerDefault,
		Transaction {

	private
	OwnedTransaction ownedTransaction;

	public
	BorrowedTransaction (
			@NonNull OwnedTransaction ownedTransaction) {

		this.ownedTransaction =
			ownedTransaction;

	}

	@Override
	public
	long transactionId () {
		return ownedTransaction.transactionId ();
	}

	@Override
	public
	void commit () {
		throw new IllegalAccessError ();
	}

	@Override
	public
	void flush () {
		ownedTransaction.flush ();
	}

	@Override
	public
	void refresh (
			Object... objects) {

		ownedTransaction.refresh (
			objects);

	}

	@Override
	public
	void setMeta (
			String key,
			Object value) {

		ownedTransaction.setMeta (
			key,
			value);

	}

	@Override
	public
	Object getMeta (
			String key) {

		return ownedTransaction.getMeta (
			key);

	}

	@Override
	public
	void fetch (
			Object... objects) {

		ownedTransaction.fetch (
			objects);

	}

	@Override
	public
	boolean contains (
			Object... objects) {

		return ownedTransaction.contains (
			objects);

	}

	@Override
	public
	RealTaskLogger realTaskLogger () {

		return ownedTransaction.realTaskLogger ();

	}

	@Override
	public
	ParentTaskLogger parentTaskLogger () {

		return ownedTransaction.realTaskLogger ();

	}

	@Override
	public
	OwnedTransaction ownedTransaction () {

		return ownedTransaction;

	}

	@Override
	public
	NestedTransaction nestTransaction (
			@NonNull LogContext logContext,
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			@NonNull Optional <Boolean> debugEnabled) {

		return ownedTransaction.nestTransaction (
			logContext,
			dynamicContextName,
			dynamicContextParameters,
			debugEnabled);

	}

}
