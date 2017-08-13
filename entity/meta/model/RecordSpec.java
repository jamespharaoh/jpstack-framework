package wbs.framework.entity.meta.model;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.naivePluralise;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.data.annotations.DataSetupMethod;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("record")
@PrototypeComponent ("recordSpec")
public
class RecordSpec
	implements ModelDataSpec {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// ancestry

	@DataParent
	PluginSpec plugin;

	// attributes

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		format = StringFormat.hyphenated)
	String oldName;

	@DataAttribute (
		name = "friendly-name")
	String friendlyNameSingular;

	@DataAttribute
	String friendlyNamePlural;

	@DataAttribute (
		name = "short-name")
	String shortNameSingular;

	@DataAttribute
	String shortNamePlural;

	@DataAttribute (
		required = true)
	ModelMetaType type;

	@DataAttribute (
		name = "table",
		format = StringFormat.snakeCase)
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// children

	@DataChild
	ModelPartitioningSpec partitioning;

	@DataChildren (
		childrenElement = "fields")
	List <ModelFieldSpec> fields =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "collections")
	List <ModelCollectionSpec> collections =
		new ArrayList<> ();

	@DataChild
	CachedViewSpec cachedView;

	@DataChildren (
		childrenElement = "dao-interfaces")
	List <ModelInterfaceSpec> daoInterfaces =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "record-interfaces")
	List <ModelInterfaceSpec> recordInterfaces =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "object-helper-interfaces")
	List <ModelInterfaceSpec> objectHelperInterfaces =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		excludeChildren = { "fields", "collections", "cached-view" })
	List <Object> children =
		new ArrayList<> ();

	// setup

	@DataSetupMethod
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			friendlyNameSingular =
				ifNull (
					friendlyNameSingular,
					hyphenToSpaces (
						name));

			friendlyNamePlural =
				ifNull (
					friendlyNamePlural,
					naivePluralise (
						friendlyNameSingular));

			shortNameSingular =
				ifNull (
					shortNameSingular,
					friendlyNameSingular);

			shortNamePlural =
				ifNull (
					shortNamePlural,
					friendlyNamePlural);

		}

	}

}
