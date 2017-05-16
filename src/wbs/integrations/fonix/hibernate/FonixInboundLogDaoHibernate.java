package wbs.integrations.fonix.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.fonix.model.FonixInboundLogDaoMethods;
import wbs.integrations.fonix.model.FonixInboundLogRec;
import wbs.integrations.fonix.model.FonixInboundLogSearch;

public
class FonixInboundLogDaoHibernate
	extends HibernateDao
	implements FonixInboundLogDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull FonixInboundLogSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					FonixInboundLogRec.class,
					"_fonixInboundLog");

			// restrict by route

			if (
				isNotNull (
					search.routeId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_fonixInboundLog.route.id",
						search.routeId ()));

			}

			// restrict by timestamp

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_fonixInboundLog.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_fonixInboundLog.timestamp",
						search.timestamp ().end ()));

			}

			// restrict by details

			if (
				isNotNull (
					search.details ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_fonixInboundLog.details",
						"%" + search.details () + "%"));

			}

			// restrict by type

			if (
				isNotNull (
					search.type ())
			) {

				criteria.add (
					Restrictions.eq (
						"_fonixInboundLog.type",
						search.type ()));

			}

			// restrict by success

			if (
				isNotNull (
					search.success ())
			) {

				criteria.add (
					Restrictions.eq (
						"_fonixInboundLog.success",
						search.success ()));

			}

			// add default order

			criteria

				.addOrder (
					Order.desc (
						"id"));

			// set to return ids only

			criteria

				.setProjection (
					Projections.id ());

			// perform and return

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
