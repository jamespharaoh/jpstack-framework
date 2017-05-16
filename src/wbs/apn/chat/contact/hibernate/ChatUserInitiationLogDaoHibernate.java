package wbs.apn.chat.contact.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.contact.model.ChatUserInitiationLogDao;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.apn.chat.core.model.ChatRec;

public
class ChatUserInitiationLogDaoHibernate
	extends HibernateDao
	implements ChatUserInitiationLogDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatUserInitiationLogRec> findByTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByTimestamp");

		) {

			return findMany (
				transaction,
				ChatUserInitiationLogRec.class,

				createCriteria (
					transaction,
					ChatUserInitiationLogRec.class,
					"_chatUserInitiationLog")

				.createAlias (
					"_chatUserInitiationLog.chatUser",
					"_chatUser")

				.add (
					Restrictions.eq (
						"_chatUser.chat",
						chat))

				.add (
					Restrictions.ge (
						"_chatUserInitiationLog.timestamp",
						timestamp.getStart ()))

				.add (
					Restrictions.lt (
						"_chatUserInitiationLog.timestamp",
						timestamp.getEnd ()))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserInitiationLogSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ChatUserInitiationLogRec.class,
					"_chatUserInitiationLog")

				.createAlias (
					"_chatUserInitiationLog.chatUser",
					"_chatUser")

				.createAlias (
					"_chatUser.chat",
					"_chat");

			if (
				isNotNull (
					search.chatId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_chatUserInitiationLog.chat.id",
						search.chatId ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_chatUserInitiationLog.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_chatUserInitiationLog.timestamp",
						search.timestamp ().end ()));

			}

			if (
				isNotNull (
					search.reason ())
			) {

				criteria.add (
					Restrictions.eq (
						"_chatUserInitiationLog.reason",
						search.reason ()));

			}

			if (
				isNotNull (
					search.monitorUserId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_chatUserInitiationLog.monitorUser.id",
						search.monitorUserId ()));

			}

			if (search.filter ()) {

				List <Criterion> filterCriteria =
					new ArrayList<> ();

				if (
					collectionIsNotEmpty (
						search.filterChatIds ())
				) {

					filterCriteria.add (
						Restrictions.in (
							"_chat.id",
							search.filterChatIds ()));

				}

				criteria.add (
					Restrictions.or (
						filterCriteria.toArray (
							new Criterion [] {})));

			}

			criteria.addOrder (
				Order.desc (
					"_chatUserInitiationLog.timestamp"));

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
