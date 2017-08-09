package wbs.console.module;

import static wbs.utils.etc.Misc.contains;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;

@Accessors (fluent = true)
@DataClass ("console-module")
@PrototypeComponent ("consoleModuleImpl")
public
class ConsoleModuleImplementation
	implements ConsoleModule {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@DataAttribute
	@Getter @Setter
	String name;

	@DataChildren
	@Getter @Setter
	List <ConsoleContextType> contextTypes =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <ConsoleContext> contexts =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <ConsoleContextTab> tabs =
		new ArrayList<> ();

	Set <String> tabNames =
		new HashSet<> ();

	@DataChildren
	@Getter @Setter
	Map <String, List <ContextTabPlacement>> tabPlacementsByContextType =
		new LinkedHashMap <> ();

	@DataChildren
	@Getter @Setter
	Map <String, WebFile> contextFiles =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, List <String>> contextFilesByContextType =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, PathHandler> paths =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, WebFile> files =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, FormFieldSet <?>> formFieldSets =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, SupervisorConfig> supervisorConfigs =
		new LinkedHashMap<> ();

	// builder tools

	public
	void addContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String tabLocation,
			@NonNull ConsoleContextTab tab,
			@NonNull List <String> contextTypes) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addContextTab");

		) {

			if (tab.name ().isEmpty ())
				throw new RuntimeException ();

			if (
				contains (
					tabNames,
					tab.name ())
			) {

				taskLogger.errorFormat (
					"Duplicated tab name '%s' ",
					tab.name (),
					"in console module '%s'",
					name ());

					return;

			}

			tabs.add (
				tab);

			tabNames.add (
				tab.name ());

			for (
				String contextTypeName
					: contextTypes
			) {

				List<ContextTabPlacement> tabPlacements =
					tabPlacementsByContextType.get (
						contextTypeName);

				if (tabPlacements == null) {

					tabPlacements =
						new ArrayList<ContextTabPlacement> ();

					tabPlacementsByContextType.put (
						contextTypeName,
						tabPlacements);

				}

				tabPlacements.add (
					new ContextTabPlacement ()

					.tabLocation (
						tabLocation)

					.tabName (
						tab.name ()));

			}

		}

	}

	public
	void addContextFile (
			@NonNull String name,
			@NonNull WebFile file,
			@NonNull List <String> contextTypeNames) {

		if (
			contextFiles.containsKey (
				name)
		) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated context file name: %s",
					name));

		}

		contextFiles.put (
			name,
			file);

		for (String contextType
				: contextTypeNames) {

			List<String> contextTypeFiles =
				contextFilesByContextType.get (contextType);

			if (contextTypeFiles == null) {

				contextTypeFiles =
					new ArrayList<String> ();

				contextFilesByContextType.put (
					contextType,
					contextTypeFiles);

			}

			contextTypeFiles.add (name);

		}

	}

	public
	void addContextType (
			@NonNull ConsoleContextType contextType) {

		contextTypes.add (
			contextType);

	}

	public
	void addContext (
			@NonNull ConsoleContext consoleContext) {

		contexts.add (
			consoleContext);

	}

	public
	void addFile (
			@NonNull String path,
			@NonNull WebFile file) {

		if (
			files.containsKey (
			path)
		) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated file path: %s",
					path));

		}

		files.put (
			path,
			file);

	}

	public
	void addPath (
			@NonNull String path,
			@NonNull PathHandler pathHandler) {

		paths.put (
			path,
			pathHandler);

	}

	public
	void addTabLocation (
			@NonNull String insertLocationName,
			@NonNull String newLocationName,
			@NonNull List <String> contextTypeNames) {

		for (
			String contextTypeName
				: contextTypeNames
		) {

			List<ContextTabPlacement> tabPlacementsForContextType =
				tabPlacementsByContextType.get (
					contextTypeName);

			if (tabPlacementsForContextType == null) {

				tabPlacementsForContextType =
					new ArrayList<ContextTabPlacement> ();

				tabPlacementsByContextType.put (
					contextTypeName,
					tabPlacementsForContextType);

			}

			tabPlacementsForContextType.add (
				new ContextTabPlacement ()
					.tabLocation (
						insertLocationName)
					.tabName (
						"+" + newLocationName));

		}

	}

	public
	void addFormFieldSet (
			@NonNull String name,
			@NonNull FormFieldSet <?> formFieldSet) {

		if (formFieldSets.containsKey (
				name)) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated form field set name: %s",
					name));

		}

		formFieldSets.put (
			name,
			formFieldSet);

	}

	public
	void addSupervisorConfig (
			SupervisorConfig supervisorConfig) {

		if (
			supervisorConfigs.containsKey (
				supervisorConfig.name ())
		) {

			throw new RuntimeException ();

		}

		supervisorConfigs.put (
			supervisorConfig.name (),
			supervisorConfig);

	}

	@Override
	public
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		return paths;

	}

	@Override
	public
	Map <String, WebFile> webModuleFiles (
			@NonNull TaskLogger parentTaskLogger) {

		return files;

	}

}
