package wbs.platform.priv.console;

import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWrite;
import static wbs.utils.web.HtmlInputUtils.htmlSelectClose;
import static wbs.utils.web.HtmlInputUtils.htmlSelectOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.platform.group.console.GroupConsoleHelper;
import wbs.platform.group.model.GroupRec;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("objectSummaryPrivPart")
public
class ObjectSummaryPrivPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	GroupConsoleHelper groupHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserObjectHelper userHelper;;

	// state

	Record <?> object;

	Map <String, UserPrivSets> userPrivs =
		new TreeMap<> ();

	Map <String, Set <String>> groupPrivs =
		new TreeMap<> ();

	Map <String, UserRec> users =
		new TreeMap<> ();

	Map <String, GroupRec> groups =
		new TreeMap<> ();

	@Override
	public
	void prepare () {

		@SuppressWarnings ("unchecked")
		ObjectLookup<? extends Record<?>> objectLookup =
			(ObjectLookup<? extends Record<?>>)
				requestContext.stuff ("dataObjectLookup");

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		List<PrivRec> privs =
			objectManager.getChildren (
				object,
				PrivRec.class);

		// for each priv

		for (
			PrivRec priv
				: privs
		) {

			// build user priv list

			for (
				UserPrivRec userPriv
					: priv.getUserPrivs ()
			) {

				String userPath =
					objectManager.objectPathMini (
						userPriv.getUser ());

				UserPrivSets userPrivSets =
					userPrivs.get (userPath);

				if (userPrivSets == null) {

					userPrivs.put (
						userPath,
						userPrivSets = new UserPrivSets ());

				}

				if (userPriv.getCan ()) {

					userPrivSets.canPrivCodes.add (
						priv.getCode ());

				}

				if (userPriv.getCanGrant ()) {

					userPrivSets.canGrantPrivCodes.add (
						priv.getCode ());

				}

			}

			// build group priv list

			for (
				GroupRec group
					: priv.getGroups ()
			) {

				String groupPath =
					objectManager.objectPathMini (
						group);

				Set<String> privCodes =
					groupPrivs.get (groupPath);

				if (privCodes == null) {

					privCodes =
						new TreeSet<String>();

					groupPrivs.put (
						groupPath,
						privCodes);

				}

				privCodes.add (
					priv.getCode ());

			}

		}

		for (UserRec user
				: userHelper.findAll ()) {

			if (! objectManager.canView (user))
				continue;

			users.put (
				objectManager.objectPathMini (
					user),
				user);

		}

		for (
			GroupRec group
				: groupHelper.findAll ()
		) {

			if (! objectManager.canView (group))
				continue;

			groups.put (
				objectManager.objectPathMini (
					group),
				group);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		renderUsers ();
		renderGroups ();

	}

	private
	void renderUsers () {

		htmlHeadingTwoWrite (
			"Users");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"User",
			"Privs",
			"Grant privs");

		if (userPrivs.size () == 0) {

			htmlTableCellWrite (
				"No user privs to show",
				htmlColumnSpanAttribute (3l));

		}

		for (
			Map.Entry <String, UserPrivSets> entry
				: userPrivs.entrySet ()
		) {

			String userPath =
				entry.getKey ();

			UserPrivSets userPrivSets =
				entry.getValue ();

			htmlTableRowOpen ();

			htmlTableCellWrite (
				userPath);

			htmlTableCellWrite (
				joinWithCommaAndSpace (
					userPrivSets.canPrivCodes));

			htmlTableCellWrite (
				joinWithCommaAndSpace (
					userPrivSets.canGrantPrivCodes));

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

		// form open

		htmlFormOpenGet ();

		// edit privs

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"Edit privs for user to user<br>");

		htmlSelectOpen (
			"userId");

		for (
			Map.Entry <String, UserRec> entry :
				users.entrySet ()
		) {

			htmlOptionWrite (
				entry.getValue ().getId ().toString (),
				entry.getKey ());

		}

		htmlSelectClose ();

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" value=\"go\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

	private void htmlFormOpenGet () {

		// TODO Auto-generated method stub
		
	}

	private
	void renderGroups () {

		// heading

		htmlHeadingTwoWrite (
			"Groups");

		// table open

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Group",
			"Privs");

		if (groupPrivs.size () == 0) {

			htmlTableCellWrite (
				"No user privs to show",
				htmlColumnSpanAttribute (3l));

		}

		for (
			Map.Entry <String, Set <String>> entry
				: groupPrivs.entrySet ()
		) {

			String groupPath =
				entry.getKey ();

			Set <String> privCodes =
				entry.getValue ();

			htmlTableRowOpen ();

			htmlTableCellWrite (
				groupPath);

			htmlTableCellWrite (
				joinWithCommaAndSpace (
					privCodes));

			htmlTableRowClose ();

		}

		// table close

		htmlTableClose ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UserPrivSets {

		Set<String> canPrivCodes =
			new TreeSet<String> ();

		Set<String> canGrantPrivCodes =
			new TreeSet<String> ();

	}

}
