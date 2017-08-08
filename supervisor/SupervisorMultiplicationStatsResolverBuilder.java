package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.reporting.MultiplicationStatsResolver;
import wbs.console.reporting.StatsResolver;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("supervisorMultiplicationStatsResolverBuilder")
public
class SupervisorMultiplicationStatsResolverBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <MultiplicationStatsResolver>
		multiplicationStatsResolverProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorMultiplicationStatsResolverSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String name =
				spec.name ();

			List <SupervisorMultiplicationOperandSpec> operandSpecs =
				spec.operandSpecs ();

			MultiplicationStatsResolver multiplicationStatsResolver =
				this.multiplicationStatsResolverProvider.provide (
					taskLogger);

			for (
				SupervisorMultiplicationOperandSpec operandSpec
					: operandSpecs
			) {

				StatsResolver resolver = null;

				if (operandSpec.resolverName () != null) {

					resolver =
						supervisorConfigBuilder.statsResolversByName ().get (
							operandSpec.resolverName ());

					if (resolver == null) {

						throw new RuntimeException (
							stringFormat (
								"Stats resolver %s does not exist",
								operandSpec.resolverName ()));

					}

				}

				multiplicationStatsResolver.operands ().add (
					new MultiplicationStatsResolver.Operand ()

					.power (
						operandSpec.power ())

					.value (
						operandSpec.value ())

					.resolver (
						resolver)

				);

			}

			supervisorConfigBuilder.statsResolversByName ().put (
				name,
				multiplicationStatsResolver);

		}

	}

}
