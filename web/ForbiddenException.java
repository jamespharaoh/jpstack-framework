package wbs.framework.web;

public
class ForbiddenException
	extends RuntimeException {

	public
	ForbiddenException () {
	}

	public
	ForbiddenException (
			String message) {

		super (
			message);

	}

}
