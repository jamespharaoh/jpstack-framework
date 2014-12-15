package wbs.framework.web;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.log4j.Logger;

import wbs.framework.application.annotations.ProxiedRequestComponent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "requestContext",
	proxyInterface = RequestContext.class)
// TODO move this somewhere else
public
class RequestContextImpl
	implements RequestContext {

	public static
	ThreadLocal<ServletContext> servletContextThreadLocal =
		new ThreadLocal<ServletContext> ();

	public static
	ThreadLocal<HttpServletRequest> servletRequestThreadLocal =
		new ThreadLocal<HttpServletRequest> ();

	public static
	ThreadLocal<HttpServletResponse> servletResponseThreadLocal =
		new ThreadLocal<HttpServletResponse> ();

	@Override
	public
	HttpServletRequest request () {
		return servletRequestThreadLocal.get ();
	}

	@Override
	public
	HttpServletResponse response () {
		return servletResponseThreadLocal.get ();
	}

	@Override
	public
	ServletContext context () {
		return servletContextThreadLocal.get ();
	}

	@Override
	public
	String applicationPathPrefix () {
		return request ().getContextPath ();
	}

	@Override
	public
	InputStream inputStream ()
		throws IOException {

		return request ().getInputStream ();

	}

	@Override
	public
	String method () {
		return request ().getMethod ();
	}

	@Override
	public
	String parameter (
			String key) {

		return request ().getParameter (key);

	}

	@Override
	public
	String parameter (
			String key,
			String defaultValue) {

		return ifNull (
			parameter (key),
			defaultValue);

	}

	@Override
	public
	int parameterInt (
			String key) {

		return Integer.parseInt (
			parameter (key));

	}

	Map<String,List<String>> parameterMap;

	@Override
	public
	Map<String,List<String>> parameterMap () {

		if (parameterMap != null)
			return parameterMap;

		ImmutableMap.Builder<String,List<String>> parameterMapBuilder =
			ImmutableMap.<String,List<String>>builder ();

		for (Object entryObject
				: request ().getParameterMap ().entrySet ()) {

			Map.Entry<?,?> entry =
				(Map.Entry<?,?>) entryObject;

			String parameterName =
				(String) entry.getKey ();

			String[] parameterValuesArray =
				(String[]) entry.getValue ();

			List<String> parameterValues =
				ImmutableList.<String>copyOf (
					parameterValuesArray);

			parameterMapBuilder.put (
				parameterName,
				parameterValues);

		}

		return parameterMap =
			parameterMapBuilder.build ();

	}

	@Override
	public
	Map<String,String> parameterMapSimple () {

		Map<String,String> ret =
			new HashMap<String,String> ();

		for (Map.Entry<String,List<String>> ent
				: parameterMap ().entrySet ())
			ret.put (
				ent.getKey (),
				ent.getValue ().get (0));

		return ret;

	}

	@Override
	@SuppressWarnings ("unchecked")
	public
	Enumeration<String> parameterNames () {
		return request ().getParameterNames ();
	}

	@Override
	public
	boolean parameterOn (
			String key) {

		String value =
			parameter (key);

		if (value == null)
			return false;

		return value
			.equalsIgnoreCase ("on");

	}

	@Override
	public
	String[] parameterValues (
			String name) {

		return request ().getParameterValues (name);

	}

	@Override
	public
	String pathInfo () {
		return request ().getPathInfo ();
	}

	@Override
	public
	Reader reader ()
		throws IOException {

		return request ().getReader ();

	}

	@Override
	public
	Object request (
			String key) {

		return request ().getAttribute (key);

	}

	@Override
	public
	Integer requestInt (
			String key) {

		return (Integer)
			request (key);

	}

	@Override
	public
	int requestInt (
			String key,
			int defaultValue) {

		Integer intObject = (Integer)
			requestInt (key);

		if (intObject != null)
			return intObject.intValue ();

		return defaultValue;

	}

	@Override
	public
	RequestDispatcher requestDispatcher (
			String path) {

		return request ().getRequestDispatcher (path);

	}

	@Override
	public
	String requestUri () {
		return request ().getRequestURI ();
	}

	@Override
	public
	String servletPath () {
		return request ().getServletPath ();
	}

	@Override
	public
	HttpSession session () {
		return request ().getSession ();
	}

	@Override
	public
	Object session (
			@NonNull String key) {

		return session ()
			.getAttribute (key);

	}

	@Override
	public
	String sessionId () {

		return session ()
			.getId ();

	}

	@Override
	@SneakyThrows (IOException.class)
	public PrintWriter writer () {
		return response ().getWriter ();
	}

	@Override
	public
	void sendError (
			int statusCode)
		throws IOException {

		response ().sendError (statusCode);

	}

	@Override
	public
	void sendError (
			int statusCode,
			@NonNull String message)
		throws IOException {

		response ().sendError (
			statusCode,
			message);

	}

	@Override
	public
	void sendRedirect (
			@NonNull String location)
		throws IOException {

		response ().sendRedirect (
			location);

	}

	@Override
	public
	void request (
			@NonNull String key,
			Object value) {

		request ().setAttribute (
			key,
			value);

	}

	@Override
	public
	void session (
			@NonNull String key,
			Object value) {

		session ()
			.setAttribute (
				key,
				value);

	}

	@Override
	public
	Object context (
			@NonNull String key) {

		return context ().getAttribute (
			key);

	}

	@Override
	public
	boolean isCommitted () {

		return response ()
			.isCommitted ();

	}

	@Override
	public
	void status (
			int status) {

		response ().setStatus (status);

	}

	@Override
	public
	void setHeader (
			@NonNull String name,
			@NonNull String value) {

		response ().setHeader (
			name,
			value);

	}

	@Override
	public
	void addHeader (
			@NonNull String name,
			@NonNull String value) {

		response ().addHeader (
			name,
			value);

	}

	@Override
	public
	OutputStream outputStream ()
		throws IOException {

		return response ().getOutputStream ();

	}

	@Override
	public
	String requestPath () {

		return stringFormat (
			"%s%s%s",
			applicationPathPrefix (),
			servletPath (),
			pathInfo () != null
				? pathInfo ()
				: "");

	}

	@Override
	public
	boolean canGetWriter () {

		try {

			writer ();

			return true;

		} catch (IllegalStateException e) {

			return false;

		}

	}

	@Override
	public
	void debugParameters (
			@NonNull Logger logger) {

		for (Map.Entry<String,List<String>> entry
				: parameterMap ().entrySet ()) {

			String name =
				entry.getKey ();

			for (String value
					: entry.getValue ()) {

				logger.debug (
					"Parameter: " + name + " = \"" + value + "\"");

			}

		}

	}

	ServletRequestContext fileUploadervletRequestContext;

	@Override
	public
	ServletRequestContext getFileUploadServletRequestContext () {

		if (fileUploadervletRequestContext != null)
			return fileUploadervletRequestContext;

		fileUploadervletRequestContext =
			new ServletRequestContext (
				request ());

		return fileUploadervletRequestContext;

	}

	@Override
	public
	boolean isMultipart () {

		return FileUploadBase.isMultipartContent (
			getFileUploadServletRequestContext ());

	}

	List<FileItem> fileItems;

	@Override
	public
	List<FileItem> fileItems ()
		throws FileUploadException {

		if (! isMultipart ())
			return Collections.<FileItem>emptyList ();

		if (fileItems != null)
			return fileItems;

		ServletFileUpload fileUpload =
			new ServletFileUpload (
				new DiskFileItemFactory ());

		fileItems =
			fileUpload.parseRequest (
				request ());

		return fileItems;

	}

	@Override
	public
	FileItem fileItem (
			String fieldName)
		throws FileUploadException {

		for (
			FileItem fileItem
				: fileItems ()
		) {

			if (
				notEqual (
					fileItem.getFieldName (),
					fieldName)
			) {
				continue;
			}

			return fileItem;

		}

		return null;

	}

	@Override
	public
	String header (
			@NonNull String name) {

		return request ().getHeader (name);

	}

	@Override
	public
	void debugDump (
			@NonNull Logger logger) {

		debugDump (
			logger,
			true);

	}

	@Override
	public
	void debugDump (
			@NonNull Logger logger,
			boolean doFiles) {

		if (! logger.isDebugEnabled ())
			return;

		logger.debug (
			stringFormat (
				"REQUEST: %s %s",
				method (),
				requestUri ()));

		// output headers

		Enumeration<?> enumeration =
			request ().getHeaderNames ();

		while (enumeration.hasMoreElements ()) {

			String name =
				((String) enumeration.nextElement ());

			String value =
				request ().getHeader (name);

			logger.debug (
				stringFormat (
					"HEADER: %s = %s",
					name,
					value));

		}

		// output params

		for (Map.Entry<String,List<String>> entry
				: parameterMap ().entrySet ()) {

			for (String value
					: entry.getValue ()) {

				logger.debug (
					stringFormat (
						"PARAM: %s = %s",
						entry.getKey (),
						value));

			}

		}

		// output files

		if (doFiles) {

			try {

				for (FileItem fileItem
						: fileItems ()) {

					logger.debug (
						stringFormat (
							"FILE: %s = %s (%s)",
							fileItem.getFieldName (),
							fileItem.getContentType (),
							fileItem.getSize ()));

				}

			} catch (FileUploadException exception) {

				throw new RuntimeException (
					exception);

			}

		}

	}

	@Override
	public
	boolean post () {

		return equal (
			method (),
			"POST");

	}

	@Override
	public
	String realPath (
			@NonNull String path) {

		return context ().getRealPath (
			path);

	}

	@Override
	public
	InputStream resourceAsStream (
			@NonNull String path) {

		return context ().getResourceAsStream (
			path);

	}

	@Override
	public
	String resolveApplicationUrl (
			@NonNull String applicationUrl) {

		return joinWithoutSeparator (
			applicationPathPrefix (),
			applicationUrl);

	}

}
