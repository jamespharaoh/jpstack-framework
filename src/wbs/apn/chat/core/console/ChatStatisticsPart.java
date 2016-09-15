package wbs.apn.chat.core.console;

import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.moreThanZero;

import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateUsersSummaryObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateUsersSummaryRec;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUsersSummaryObjectHelper;
import wbs.apn.chat.user.core.model.ChatUsersSummaryRec;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatStatisticsPart")
public
class ChatStatisticsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatAffiliateUsersSummaryObjectHelper chatAffiliateUsersSummaryHelper;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUsersSummaryObjectHelper chatUsersSummaryHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	ChatRec chat;
	ChatUsersSummaryRec users;

	int numAffiliates;

	int numUsersGayMale;
	int numUsersGayFemale;
	int numUsersBiMale;
	int numUsersBiFemale;
	int numUsersStraightMale;
	int numUsersStraightFemale;

	int numJoinedLastDay;
	int numJoinedLastWeek;
	int numJoinedLastMonth;

	int numOnlineLastDay;
	int numOnlineLastWeek;
	int numOnlineLastMonth;

	@Override
	public
	void prepare () {

		chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		List<ChatAffiliateUsersSummaryRec> chatAffiliateUsersSummaries =
			chatAffiliateUsersSummaryHelper.findByParent (
				chat);

		for (ChatAffiliateUsersSummaryRec chatAffiliateUsersSummary
				: chatAffiliateUsersSummaries) {

			if (! privChecker.canRecursive (
					chatAffiliateUsersSummary.getChatAffiliate (),
					"chat_user_view"))
				continue;

			numAffiliates ++;

			numUsersGayMale +=
				chatAffiliateUsersSummary.getNumUsersGayMale ();

			numUsersGayFemale +=
				chatAffiliateUsersSummary.getNumUsersGayFemale ();

			numUsersBiMale +=
				chatAffiliateUsersSummary.getNumUsersBiMale ();

			numUsersBiFemale +=
				chatAffiliateUsersSummary.getNumUsersBiFemale ();

			numUsersStraightMale +=
				chatAffiliateUsersSummary.getNumUsersStraightMale ();

			numUsersStraightFemale +=
				chatAffiliateUsersSummary.getNumUsersStraightFemale ();

			numJoinedLastDay +=
				chatAffiliateUsersSummary.getNumJoinedLastDay ();

			numJoinedLastWeek +=
				chatAffiliateUsersSummary.getNumJoinedLastWeek ();

			numJoinedLastMonth +=
				chatAffiliateUsersSummary.getNumJoinedLastMonth ();

			numOnlineLastDay +=
				chatAffiliateUsersSummary.getNumOnlineLastDay ();

			numOnlineLastWeek +=
				chatAffiliateUsersSummary.getNumOnlineLastWeek ();

			numOnlineLastMonth +=
				chatAffiliateUsersSummary.getNumOnlineLastMonth ();

		}

		users =
			chatUsersSummaryHelper.findRequired (
				chat.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (

			equalToZero (
				numAffiliates)

			&& ! privChecker.canRecursive (
				chat,
				"monitor")

		) {
			return;
		}

		if (
			moreThanZero (
				numAffiliates)
		) {

			printFormat (
				"<p>Number of affiliates: %h</p>\n",
				numAffiliates);

		}

		if (
			! privChecker.canRecursive (
				chat,
				"monitor")
		) {

			printFormat (
				"<h2>Users</h2>\n");

			printFormat (
				"<table class=\"list\">\n");

			printFormat (
				"<tr>\n",
				"<th>Orient</th>\n",
				"<th>Gender</th>\n",
				"<th>Users</th>\n",
				"</tr>\n");

			printFormat (
				"<tr> <td>Gay</td> <td>Male</td> <td>%h</td> </tr>\n",
				numUsersGayMale);

			printFormat (
				"<tr> <td>Gay</td> <td>Female</td> <td>%h</td> </tr>\n",
				numUsersGayFemale);

			printFormat (
				"<tr> <td>Bi</td> <td>Male</td> <td>%h</td> </tr>\n",
				numUsersBiMale);

			printFormat (
				"<tr> <td>Bi</td> <td>Female</td> <td>%h</td> </tr>\n",
				numUsersBiFemale);

			printFormat (
				"<tr> <td>Straight</td> <td>Male</td> <td>%h</td> </tr>\n",
				numUsersStraightMale);

			printFormat (
				"<tr>\n",
				"<td>Straight</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				numUsersStraightFemale,

				"</tr>\n");

			printFormat (
				"</table>\n");

		} else if (numAffiliates == 0) {

			printFormat (
				"<h2>Users</h2>\n");

			printFormat (
				"<table class=\"list\">\n");

			printFormat (
				"<tr>\n",
				"<th>Orient</th>\n",
				"<th>Gender</th>\n",
				"<th>Monitors</th>\n",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Gay</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				users.getNumMonitorsGayFemale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Bi</td>\n",
				"<td>Male</td>\n",

				"<td>%h</td>\n",
				users.getNumMonitorsBiMale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Bi</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				users.getNumMonitorsBiFemale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Straight</td>\n",
				"<td>Male</td>\n",

				"<td>%h</td>\n",
				users.getNumMonitorsStraightMale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Straight</td>\n",
				"<td>Female</td>\n",
				"<td>%h</td>\n",
				users.getNumMonitorsStraightFemale (),
				"</tr>\n");

			printFormat (
				"</table>\n");

		} else {

			printFormat (
				"<h2>Users</h2>\n");

			printFormat (
				"<table class=\"list\">\n");

			printFormat (
				"<tr>\n",
				"<th>Orient</th>\n",
				"<th>Gender</th>\n",
				"<th>Users</th>\n",
				"<th>Monitors</th>\n",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Gay</td>\n",
				"<td>Male</td>\n",

				"<td>%h</td>\n",
				numUsersGayMale,

				"<td>%h</td>\n",
				users.getNumMonitorsGayMale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Gay</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				numUsersGayFemale,

				"<td>%h</td>\n",
				users.getNumMonitorsGayFemale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Bi</td>\n",
				"<td>Male</td>\n",

				"<td>%h</td>\n",
				numUsersBiMale,

				"<td>%h</td>\n",
				users.getNumMonitorsBiMale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Bi</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				numUsersBiFemale,

				"<td>%h</td>\n",
				users.getNumMonitorsBiFemale (),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Straight</td>\n",
				"<td>Male</td>\n",

				"<td>%h</td>\n",
				numUsersStraightMale,

				"<td>%h</td>\n",
				users.getNumMonitorsStraightMale (),

				"</tr>");

			printFormat (
				"<tr>\n",
				"<td>Straight</td>\n",
				"<td>Female</td>\n",

				"<td>%h</td>\n",
				numUsersStraightFemale,

				"<td>%h</td>\n",
				users.getNumMonitorsStraightFemale (),

				"</tr>\n");

			printFormat (
				"</table>\n");

		}

		if (numAffiliates > 0) {

			printFormat (
				"<h2>User activity</h2>\n");

			printFormat (
				"<table class=\"list\">\n");

			printFormat (
				"<tr>\n",
				"<th>Timescale</th>\n",
				"<th>Joined</th>\n",
				"<th>Online</th>\n",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Last day</td>\n",

				"<td>%h</td>\n",
				numJoinedLastDay,

				"<td>%h</td>\n",
				numOnlineLastDay,

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Last week</td>\n",

				"<td>%h</td>\n",
				numJoinedLastWeek,

				"<td>%h</td>\n",
				numOnlineLastWeek,

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<td>Last month</td>\n",

				"<td>%h</td>\n",
				numJoinedLastMonth,

				"<td>%h</td>\n",
				numOnlineLastMonth,

				"</tr>\n");

			printFormat (
				"</table>\n");

		}

	}

}