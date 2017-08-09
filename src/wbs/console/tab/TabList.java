package wbs.console.tab;

import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("tabList")
public
class TabList {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// static

	List <TabRef> tabRefs =
		new ArrayList<> ();

	// constructors

	static
	class PreparedTab {
		String url;
		String label;
		boolean selected;

		PreparedTab (
				String newUrl,
				String newLabel,
				boolean newSelected) {

			url = newUrl;
			label = newLabel;
			selected = newSelected;

		}

	}

	public
	class Prepared {

		List<PreparedTab> preparedTabs =
			new ArrayList<PreparedTab> ();

		Prepared () {
		}

		public
		void go (
				@NonNull FormatWriter formatWriter) {

			htmlParagraphOpen (
				formatWriter,
				htmlClassAttribute (
					"links"));

			for (
				PreparedTab preparedTab
					: preparedTabs
			) {

				if (preparedTab.selected) {

					htmlLinkWrite (
						formatWriter,
						preparedTab.url,
						preparedTab.label,
						htmlClassAttribute (
							"selected"));

				} else {

					htmlLinkWrite (
						formatWriter,
						preparedTab.url,
						preparedTab.label);

				}

			}

			htmlParagraphClose (
				formatWriter);

		}

	}

	public
	TabList () {
	}

	/**
	 * Initialise with a list of TabRefs, supplied var-arg style.
	 */
	public
	TabList (
			TabRef... tabRefs) {

		for (TabRef tabRef : tabRefs)
			add (tabRef);

	}

	public
	TabList (
			Tab... tabs) {

		for (Tab tab : tabs)
			add (tab);

	}

	public
	void add (
			TabRef tabRef) {

		if (tabRef == null)
			throw new NullPointerException ();

		tabRefs.add (tabRef);

	}

	public
	void add (
			Tab tab) {

		add (
			new TabRef (tab));

	}

	public
	void add (
			Tab tab,
			String label) {

		add (
			new TabRef (
				tab,
				label));
	}

	public
	Prepared prepare (
			@NonNull Transaction parentTransaction,
			@NonNull Tab currentTab) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			Prepared prepared =
				new Prepared ();

			boolean foundCurrent = false;

			for (TabRef tabRef
					: tabRefs) {

				if (tabRef.getTab () == currentTab) {

					foundCurrent = true;

				} else if (! tabRef.getTab ().isAvailable ()) {

					continue;

				}

				prepared.preparedTabs.add (
					new PreparedTab (
						tabRef.getTab ().getUrl (
							transaction),
						tabRef.getLabel (),
						tabRef.getTab () == currentTab));

			}

			if (! foundCurrent) {

				prepared.preparedTabs.add (
					new PreparedTab (
						currentTab.getUrl (
							transaction),
						currentTab.getDefaultLabel (),
						true));

			}

			return prepared;

		}

	}

	public
	boolean contains (
			@NonNull Tab tab) {

		for (
			TabRef tabRef
				: tabRefs
		) {

			if (tabRef.getTab () == tab)
				return true;

		}

		return false;

	}

	public
	Collection<TabRef> getTabRefs () {

		return tabRefs;

	}

}
