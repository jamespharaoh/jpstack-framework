package wbs.console.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import com.google.common.collect.ImmutableMap;

import wbs.console.request.ConsoleRequestContext;

public
class ParamCheckerSet {

	@Getter
	Map<String,ParamChecker<?>> paramCheckers;

	public
	ParamCheckerSet (
			Map<String,ParamChecker<?>> paramCheckers) {

		this.paramCheckers =
			ImmutableMap.copyOf (
				paramCheckers);

	}

	public
	Map<String,Object> apply (
			ConsoleRequestContext requestContext) {

		Map<String,Object> ret =
			new HashMap<String,Object> ();

		List<String> errors =
			new ArrayList<String> ();

		for (
			Map.Entry<String,ParamChecker<?>> ent
				: paramCheckers.entrySet ()
		) {

			String key = ent.getKey ();
			ParamChecker<?> paramChecker = ent.getValue ();

			try {

				Object value =
					paramChecker.get (
						requestContext.parameterOrNull (key));

				if (ret != null)
					ret.put (key, value);

			} catch (ParamFormatException e) {

				errors.add (e.getMessage ());
				ret = null;

			}

		}

		if (ret == null) {

			for (String error : errors)
				requestContext.addError (error);

		}

		return ret;

	}

}