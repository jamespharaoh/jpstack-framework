package wbs.platform.postgresql.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.postgresql.model.PostgresqlMaintenanceFrequency;
import wbs.platform.postgresql.model.PostgresqlMaintenanceRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.etc.NullUtils;

@PrototypeComponent ("postgresqlMaintenanceListPart")
public
class PostgresqlMaintenanceListPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PostgresqlMaintenanceConsoleHelper postgresqlMaintenanceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	Map <
		PostgresqlMaintenanceFrequency,
		Set <PostgresqlMaintenanceRec>
	>
	maintenancesByFrequency =
		new TreeMap <> ();

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			for (
				PostgresqlMaintenanceFrequency frequency
					: PostgresqlMaintenanceFrequency.values ()
			) {

				maintenancesByFrequency.put (
					frequency,
					new TreeSet <PostgresqlMaintenanceRec> ());

			}

			for (
				PostgresqlMaintenanceRec maintenance
					: postgresqlMaintenanceHelper.findAll (
						transaction)
			) {

				maintenancesByFrequency
					.get (maintenance.getFrequency ())
					.add (maintenance);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"Seq",
				"Command",
				"Last run",
				"Last duration");

			for (
				PostgresqlMaintenanceFrequency frequency
					: PostgresqlMaintenanceFrequency.values ()
			) {

				htmlTableRowSeparatorWrite ();

				Duration totalDuration =
					maintenancesByFrequency.get (
						frequency)

					.stream ()

					.map (
						PostgresqlMaintenanceRec::getLastDuration)

					.filter (
						NullUtils::isNotNull)

					.map (
						Duration::new)

					.reduce ((left, right) ->
						left.plus (right))

					.orElse (
						Duration.ZERO);

				htmlTableRowOpen (
					htmlStyleRuleEntry (
						"font-weight",
						"bold"));

				htmlTableCellWrite (
					frequency.getDescription (),
					htmlColumnSpanAttribute (3l));

				htmlTableCellWrite (
					userConsoleLogic.prettyDuration (
						transaction,
						totalDuration));

				htmlTableRowClose ();

				for (
					PostgresqlMaintenanceRec postgresqlMaintenance
						: maintenancesByFrequency.get (
							frequency)
				) {

					htmlTableRowOpen (
						htmlClassAttribute (
							"magic-table-row"),
						htmlDataAttribute (
							"target-href",
							requestContext.resolveContextUrlFormat (
								"/postgresqlMaintenance",
								"/%u",
								integerToDecimalString (
									postgresqlMaintenance.getId ()),
								"/postgresqlMaintenance.summary")));

					htmlTableCellWrite (
						integerToDecimalString (
							postgresqlMaintenance.getSequence ()));

					htmlTableCellWrite (
						postgresqlMaintenance.getCommand ());

					htmlTableCellWrite (
						ifNotNullThenElseEmDash (
							postgresqlMaintenance.getLastRun (),
							() -> userConsoleLogic.timestampWithTimezoneString (
								transaction,
								postgresqlMaintenance.getLastRun ())));

					htmlTableCellWrite (
						ifNotNullThenElseEmDash (
							postgresqlMaintenance.getLastDuration (),
							() -> userConsoleLogic.prettyDuration (
								transaction,
								new Duration (
									postgresqlMaintenance.getLastDuration ()))));

					htmlTableRowClose ();

				}

			}

			htmlTableClose ();

		}

	}

}
