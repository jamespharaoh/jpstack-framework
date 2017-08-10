package wbs.framework.apiclient;

import static wbs.utils.collection.ArrayUtils.arrayStream;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKeyAndValueGroup;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;
import static wbs.utils.string.StringUtils.utf8ToString;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.compress.utils.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeInterruptedIoException;
import wbs.utils.io.RuntimeIoException;

import wbs.web.exceptions.HttpClientException;
import wbs.web.misc.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("genericHttpSender")
public
class GenericHttpSender <Request, Response> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	ComponentProvider <GenericHttpSenderHelper <Request, Response>>
		helperProvider;

	// state

	@Getter
	GenericHttpSenderHelper <Request, Response> helper;

	State state =
		State.init;

	CloseableHttpClient httpClient;

	HttpRequestBase httpRequest;

	HttpResponse httpResponse;
	String responseBody;

	@Getter
	JsonObject requestTrace;

	@Getter
	JsonObject responseTrace;

	@Getter
	Optional <String> errorMessage =
		optionalAbsent ();

	@Getter
	Boolean success;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			helper =
				helperProvider.provide (
					taskLogger);

		}

	}

	// property accessors

	public
	GenericHttpSender <Request, Response> request (
			@NonNull Request request) {

		helper.request (
			request);

		return this;

	}

	public
	Response response () {

		return helper.response ();

	}

	// public implementation

	public
	GenericHttpSender <Request, Response> encode (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"encode");

		) {

			// check and set temporary state

			if (
				enumNotEqualSafe (
					state,
					State.init)
			) {
				throw new IllegalStateException ();
			}

			// ask helper to verify

			state =
				State.verifyError;

			helper.verify ();

			// delegate to helper

			state =
				State.encodeError;

			helper.encode ();

			// work out url

			UrlParams urlParams =
				new UrlParams ();

			helper.requestParameters ().forEach (
				(key, values) ->

				values.forEach (
					value ->

					urlParams.add (
						key,
						value)
				)

			);

			String urlWithParams =
				urlParams.toUrl (
					helper.url ());

			// create post

			switch (helper.method ()) {

			case delete:

				httpRequest =
					new HttpDelete (
						urlWithParams);

				break;

			case get:

				httpRequest =
					new HttpGet (
						urlWithParams);

				break;

			case post:

				httpRequest =
					new HttpPost (
						urlWithParams);

				break;

			case put:

				httpRequest =
					new HttpPut (
						urlWithParams);

				break;

			default:

				throw shouldNeverHappen ();

			}

			// configure request

			httpRequest.setConfig (
				RequestConfig.custom ()

				.setConnectionRequestTimeout (
					toJavaIntegerRequired (
						helper.connectionRequestTimeout ().getMillis ()))

				.setConnectTimeout (
					toJavaIntegerRequired (
						helper.connectTimeout ().getMillis ()))

				.setSocketTimeout (
					toJavaIntegerRequired (
						helper.socketTimeout ().getMillis ()))

				.build ()

			);

			// set default headers

			httpRequest.setHeader (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			// set headers from helper

			for (
				Map.Entry <String, String> requestHeaderEntry
					: helper.requestHeaders ().entrySet ()
			) {

				httpRequest.setHeader (
					requestHeaderEntry.getKey (),
					requestHeaderEntry.getValue ());

			}

			// set body

			switch (helper.method ()) {

			case delete:
			case get:

				doNothing ();

				break;

			case post:
			case put:

				byte[] requestData =
					stringToUtf8 (
						helper.requestBody ());

				HttpEntityEnclosingRequest httpEntityRequest =
					(HttpEntityEnclosingRequest)
					httpRequest;

				httpEntityRequest.setEntity (
					new ByteArrayEntity (
						requestData));

				break;

			default:

				throw shouldNeverHappen ();

			}

			// create debug trace

			requestTrace =
				new JsonObject ();

			requestTrace.addProperty (
				"url",
				httpRequest.getURI ().toString ());

			requestTrace.addProperty (
				"method",
				httpRequest.getMethod ());

			JsonObject requestHeadersTrace =
				new JsonObject ();

			for (
				Map.Entry <String, List <String>> requestHeaderEntry
					: mapWithDerivedKeyAndValueGroup (
						Arrays.asList (
							httpRequest.getAllHeaders ()),
						Header::getName,
						Header::getValue
					).entrySet ()
			) {

				JsonArray requestHeaderTrace =
					new JsonArray ();

				for (
					String requestHeaderValue
						: requestHeaderEntry.getValue ()
				) {

					requestHeaderTrace.add (
						new JsonPrimitive (
							requestHeaderValue));

				}

				requestHeadersTrace.add (
					requestHeaderEntry.getKey (),
					requestHeaderTrace);

			}

			requestTrace.add (
				"headers",
				requestHeadersTrace);

			requestTrace.addProperty (
				"body",
				emptyStringIfNull (
					helper.requestBody ()));

			// update state and return

			state =
				State.encoded;

			return this;

		}

	}

	public
	GenericHttpSender <Request, Response> send (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"send");

		) {

			// check and set temporary state

			if (
				enumNotEqualSafe (
					state,
					State.encoded)
			) {
				throw new IllegalStateException ();
			}

			state =
				State.sendError;

			// perform api call

			httpClient =
				HttpClientBuilder.create ()
					.build ();

			try {

				httpResponse =
					httpClient.execute (
						httpRequest);

			} catch (InterruptedIOException interruptedIoException) {

				throw new RuntimeInterruptedIoException (
					interruptedIoException);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			state =
				State.sent;

			return this;

		}

	}

	public
	GenericHttpSender <Request, Response> receive (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"receive");

		) {

			// check and set temporary state

			if (
				enumNotEqualSafe (
					state,
					State.sent)
			) {
				throw new IllegalStateException ();
			}

			state =
				State.receiveError;

			// receive responsea

			try {

				responseBody =
					utf8ToString (
						IOUtils.toByteArray (
							httpResponse.getEntity ().getContent ()));

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

			// store raw response

			responseTrace =
				new JsonObject ();

			responseTrace.addProperty (
				"statusCode",
				httpResponse.getStatusLine ().getStatusCode ());

			responseTrace.addProperty (
				"statusMessage",
				httpResponse.getStatusLine ().getReasonPhrase ());

			JsonObject responseHeadersTrace =
				new JsonObject ();

			for (
				Map.Entry <String, List <String>> responseHeaderEntry
					: mapWithDerivedKeyAndValueGroup (
						Arrays.asList (
							httpResponse.getAllHeaders ()),
						Header::getName,
						Header::getValue
					).entrySet ()
			) {

				JsonArray responseHeaderTrace =
					new JsonArray ();

				for (
					String responseHeaderValue
						: responseHeaderEntry.getValue ()
				) {

					responseHeaderTrace.add (
						new JsonPrimitive (
							responseHeaderValue));

				}

				responseHeadersTrace.add (
					responseHeaderEntry.getKey (),
					responseHeaderTrace);

			}

			responseTrace.addProperty (
				"body",
				emptyStringIfNull (
					responseBody));

			// update state and return

			state =
				State.received;

			return this;

		}

	}

	public
	GenericHttpSender <Request, Response> decode (
			@NonNull TaskLogger parentTaskLogger) {

		// check and set temporary state

		if (
			enumNotEqualSafe (
				state,
				State.received)
		) {
			throw new IllegalStateException ();
		}

		state =
			State.receiveError;

		// check response

		if (
			doesNotContain (
				helper.validStatusCodes (),
				fromJavaInteger (
					httpResponse.getStatusLine ().getStatusCode ()))
		) {

			// invalid status code

			errorMessage =
				optionalOf (
					stringFormat (
						"Server returned %s: %s",
						integerToDecimalString (
							httpResponse.getStatusLine ().getStatusCode ()),
						httpResponse.getStatusLine ().getReasonPhrase ()));

		} else {

			// decode response

			helper

				.responseStatusCode (
					fromJavaInteger (
						httpResponse.getStatusLine ().getStatusCode ()))

				.responseStatusReason (
					httpResponse.getStatusLine ().getReasonPhrase ())

				.responseHeaders (

					arrayStream (
						httpResponse.getAllHeaders ())

					.collect (
						Collectors.groupingBy (
							Header::getName,
							Collectors.mapping (
								Header::getValue,
								Collectors.toList ())))

				)

				.responseBody (
					responseBody)

				.decode ();

		}

		// update state and return

		state =
			State.decoded;

		return this;

	}

	public
	void close () {

		if (
			enumEqualSafe (
				state,
				State.closed)
		) {
			return;
		}

		if (
			isNotNull (
				httpClient)
		) {

			try {

				httpClient.close ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		state =
			State.closed;

	}

	public
	Response allInOne (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Request request,
			@NonNull Consumer <GenericHttpSender <Request, Response>>
				postProcessor) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"allInOne");

		) {

			success = false;

			try {

				request (
					request);

				encode (
					taskLogger);

				send (
					taskLogger);

				receive (
					taskLogger);

				if (
					optionalIsPresent (
						errorMessage)
				) {

					throw new RuntimeException (
						optionalGetRequired (
							errorMessage));

				}

				decode (
					taskLogger);

				if (
					optionalIsPresent (
						errorMessage)
				) {

					if (

						doesNotContain (
							helper.validStatusCodes (),
							fromJavaInteger (
								httpResponse.getStatusLine ().getStatusCode ()))

						&& mapContainsKey (
							HttpClientException.exceptionClassesByStatus,
							fromJavaInteger (
								httpResponse.getStatusLine ().getStatusCode ()))

					) {

						throw HttpClientException.forStatus (
							fromJavaInteger (
								httpResponse.getStatusLine ().getStatusCode ()),
							optionalGetRequired (
								errorMessage));

					} else {

						Gson gson =
							new GsonBuilder ().create ();

						taskLogger.errorFormat (
							"Decode error: %s\n",
							optionalGetRequired (
								errorMessage),
							"Trace: %s\n",
							gson.toJson (
								requestTrace),
							"Response: %s",
							gson.toJson (
								responseTrace));

						throw new RuntimeException (
							optionalGetRequired (
								errorMessage));

					}

				}

				success = true;

				return response ();

			} finally {

				postProcessor.accept (
					this);

			}

		}

	}

	public
	Response allInOne (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Request request) {

		return allInOne (
			parentTaskLogger,
			request,
			httpSender ->
				doNothing ());

	}

	// inner classes

	public static
	enum State {
		init,
		verifyError,
		encodeError,
		encoded,
		sendError,
		sent,
		receiveError,
		received,
		decoded,
		error,
		closed;
	}

}
