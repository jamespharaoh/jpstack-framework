package wbs.console.part;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
public
class TextPartFactory
	implements PagePartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextPart> textPartProvider;

	// properties

	@Getter @Setter
	String text;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"buildPagePart");

		) {

			return textPartProvider.provide (
				transaction)

				.text (
					text)

			;

		}

	}

}
