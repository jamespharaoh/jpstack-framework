package wbs.console.module;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.apache.commons.io.FileUtils;

import wbs.console.context.ConsoleContextExtensionPoint;
import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextLink;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleMetaManagerImpl")
public
class ConsoleMetaManagerImplementation
	implements ConsoleMetaManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, ConsoleMetaModule> consoleMetaModules;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ResolvedConsoleContextLink> resolvedContextLinkProvider;

	// state

	Map <String, List <ConsoleContextLink>> contextLinks =
		new HashMap<> ();

	Map <String, List <ConsoleContextExtensionPoint>> extensionPoints =
		new HashMap<> ();

	Map <String, ConsoleContextHint> contextHints =
		new HashMap<> ();

	// init

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			// reset output dir

			try {

				FileUtils.deleteDirectory (
					new File (
						"work/console/meta-module"));

				FileUtils.forceMkdir (
					new File (
						"work/console/meta-module"));

			} catch (IOException exception) {

				taskLogger.errorFormatException (
					exception,
					"Error deleting contents of work/console/meta-module");

			}

			// collect stuff

			for (
				Map.Entry <String, ConsoleMetaModule> consoleMetaModuleEntry
					: consoleMetaModules.entrySet ()
			) {

				String consoleMetaModuleName =
					consoleMetaModuleEntry.getKey ();

				ConsoleMetaModule consoleMetaModule =
					consoleMetaModuleEntry.getValue ();

				// collect context links

				for (
					ConsoleContextLink contextLink
						: consoleMetaModule.contextLinks ()
				) {

					List<ConsoleContextLink> contextLinksForName =
						contextLinks.get (
							contextLink.linkName ());

					if (contextLinksForName == null) {

						contextLinksForName =
							new ArrayList<ConsoleContextLink> ();

						contextLinks.put (
							contextLink.linkName (),
							contextLinksForName);

					}

					contextLinksForName.add (
						contextLink);

				}

				// collect extension points

				for (
					ConsoleContextExtensionPoint extensionPoint
						: consoleMetaModule.extensionPoints ()
				) {

					List<ConsoleContextExtensionPoint> extensionPointsForName =
						extensionPoints.get (
							extensionPoint.name ());

					if (extensionPointsForName == null) {

						extensionPointsForName =
							new ArrayList<ConsoleContextExtensionPoint> ();

						extensionPoints.put (
							extensionPoint.name (),
							extensionPointsForName);

					}

					extensionPointsForName.add (
						extensionPoint);

				}

				// collect context hints

				for (
					ConsoleContextHint contextHint
						: consoleMetaModule.contextHints ()
				) {

					if (
						contains (
							contextHints,
							contextHint.linkName ())
					) {
						throw new RuntimeException ();
					}

					contextHints.put (
						contextHint.linkName (),
						contextHint);

				}

				// dump out data

				String outputFileName =
					stringFormat (
						"work/console/meta-module/%s.xml",
						camelToHyphen (
							consoleMetaModuleName));

				try {

					new DataToXml ().writeToFile (
						outputFileName,
						consoleMetaModule);

				} catch (Exception exception) {

					taskLogger.warningFormat (
						"Error writing %s",
						outputFileName);

				}

			}

			taskLogger.noticeFormat (
				"Console meta manager initialised %s ",
				integerToDecimalString (
					collectionSize (
						consoleMetaModules)),
				"meta modules");

		}

	}

	// implementation

	@Override
	public
	List <ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"resolveExtensionPoint");

		) {

			List <ResolvedConsoleContextExtensionPoint>
				resolvedExtensionPoints =
					new ArrayList<> ();

			List <ConsoleContextExtensionPoint> extensionPointsForName =
				ifNull (
					extensionPoints.get (name),
					emptyList ());

			for (
				ConsoleContextExtensionPoint extensionPoint
					: extensionPointsForName
			) {

				if (extensionPoint.root ()) {

					resolvedExtensionPoints.add (
						new ResolvedConsoleContextExtensionPoint ()

						.name (
							extensionPoint.name ())

						.parentContextNames (
							ImmutableList.<String>builder ()

							.addAll (
								extensionPoint.parentContextNames ())

							.addAll (
								parentContextNames (
									taskLogger,
									extensionPoint.contextLinkNames ()))

							.build ())

						.contextTypeNames (
							extensionPoint.contextTypeNames ())

						.contextLinkNames (
							extensionPoint.contextLinkNames ()));

				} else if (extensionPoint.nested ()) {

					if (
						stringEqualSafe (
							extensionPoint.name (),
							extensionPoint.parentExtensionPointName ())
					) {

						throw new RuntimeException (
							stringFormat (
								"Extension point %s is its own parent",
								extensionPoint.name ()));

					}

					resolvedExtensionPoints.addAll (
						resolveExtensionPoint (
							taskLogger,
							extensionPoint.parentExtensionPointName ()));

				} else {

					throw new RuntimeException ();

				}

			}

			return resolvedExtensionPoints;

		}

	}

	List <String> parentContextNames (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> contextLinkNames) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"parentContextNames");

		) {

			ImmutableList.Builder<String> parentContextNamesBuilder =
				ImmutableList.<String>builder ();

			for (
				String contextLinkName
					: contextLinkNames
			) {

				for (
					ResolvedConsoleContextLink resolvedContextLink
						: resolveContextLink (
							taskLogger,
							contextLinkName)
				) {

					for (
						String parentContextName
							: resolvedContextLink.parentContextNames ()
					) {

						ConsoleContextHint contextHint =
							contextHints.get (
								contextLinkName);

						if (
							isNull (
								contextHint)
						) {

							throw new RuntimeException (
								stringFormat (
									"No context hint for context link name: %s",
									contextLinkName));

						}

						if (contextHint.singular ()) {

							parentContextNamesBuilder.add (
								stringFormat (
									"%s.%s",
									parentContextName,
									resolvedContextLink.localName ()));

						}

						if (contextHint.plural ()) {

							parentContextNamesBuilder.add (
								stringFormat (
									"%s.%s",
									parentContextName,
									naivePluralise (
										resolvedContextLink.localName ())));

						}

					}

				}

			}

			return parentContextNamesBuilder.build ();

		}

	}

	@Override
	public
	List <ResolvedConsoleContextLink> resolveContextLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String contextLinkName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"resolveContextLink");

		) {

			List <ResolvedConsoleContextLink> resolvedContextLinks =
				new ArrayList<> ();

			List <ConsoleContextLink> contextLinksForName =
				ifNull (
					contextLinks.get (contextLinkName),
					Collections.<ConsoleContextLink>emptyList ());

			for (
				ConsoleContextLink contextLink
					: contextLinksForName
			) {

				for (
					ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
						: resolveExtensionPoint (
							taskLogger,
							contextLink.extensionPointName ())
				) {

					// generate

					resolvedContextLinks.add (
						resolvedContextLinkProvider.provide (
							taskLogger)

						.name (
							stringFormat (
								"%s.%s",
								resolvedExtensionPoint.name (),
								contextLink.localName ()))

						.localName (
							contextLink.localName ())

						.tabName (
							stringFormat (
								"%s.%s",
								resolvedExtensionPoint.name (),
								contextLink.localName ()))

						.tabLocation (
							contextLink.tabLocation ())

						.tabLabel (
							contextLink.label ())

						.tabPrivKey (
							contextLink.privKey ())

						.tabContextTypeNames (
							resolvedExtensionPoint.contextTypeNames ())

						.parentContextNames (
							resolvedExtensionPoint.parentContextNames ())

					);

				}

			}

			return resolvedContextLinks;

		}

	}

}
