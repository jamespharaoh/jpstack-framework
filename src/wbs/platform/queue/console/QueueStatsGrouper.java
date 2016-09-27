package wbs.platform.queue.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsGrouper;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.queue.model.QueueRec;
import wbs.utils.string.FormatWriter;

@SingletonComponent ("queueStatsGrouper")
public
class QueueStatsGrouper
	implements StatsGrouper {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	QueueConsoleHelper queueHelper;

	// implementation

	@Override
	public
	Set <Object> getGroups (
			@NonNull StatsDataSet dataSet) {

		return new HashSet <Object> (
			dataSet.indexValues ().get ("queueId"));

	}

	@Override
	public
	void writeTdForGroup (
			@NonNull FormatWriter formatWriter,
			@NonNull Object group) {

		QueueRec queue =
			queueHelper.findRequired (
				(Long)
				group);

		consoleObjectManager.writeTdForObjectMiniLink (
			queue);

	}

	@Override
	public
	List <Object> sortGroups (
			Set <Object> groups) {

		List <QueueRec> queues =
			new ArrayList<> (
				groups.size ());

		for (
			Object group
				: groups
		) {

			Long queueId =
				(Long)
				group;

			queues.add (
				queueHelper.findRequired (
					queueId));

		}

		Collections.sort (
			queues);

		ArrayList<Object> queueIds =
			new ArrayList<Object> (
				queues.size ());

		for (QueueRec queue : queues)
			queueIds.add (queue.getId ());

		return queueIds;

	}

}
