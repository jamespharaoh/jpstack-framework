package wbs.framework.utils.etc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;

// TODO lots to deprecate here
public
class Misc {

	private final static
	TimeZone gmt =
		TimeZone.getTimeZone ("gmt");

	public final static
	SimpleDateFormat timestampFormatSeconds =
		new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

	private
	Misc () {
		// never instantiated
	}

	public static
	<Type> Type ifNull (
			Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	public static
	<Type> Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	public static
	<Type> Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

	public static
	String emptyStringIfNull (
			String string) {

		return ifNull (
			string,
			"");

	}

	public static
	String nullIfEmptyString (
			String string) {

		return nullIf (
			string,
			"");

	}

	public static
	<Type> Type ifEq (
			Type value,
			Type lookFor,
			Type replaceWith) {

		return equal (value, lookFor)
			? replaceWith
			: value;

	}

	private final static
	Pattern intPattern =
		Pattern.compile ("[0-9]+");

	public static
	boolean isInt (
			String string) {

		if (string == null)
			return false;

		return intPattern
			.matcher (string)
			.matches ();

	}

	public static Integer toInteger (
			String string) {

		if (string == null)
			return null;

		if (isInt (string))
			return Integer.parseInt (string);

		return null;

	}

	public static <Type extends Enum<Type>>
	Type toEnum (
			Class<Type> enumType,
			String name) {

		if (name == null || name.length () == 0)
			return null;

		if (equal (name, "null"))
			return null;

		return Enum.valueOf (enumType, name);

	}

	public static
	Boolean toBoolean (
			String string) {

		if (string == null)
			return null;

		if (string.equals ("true"))
			return true;

		if (string.equals ("false"))
			return false;

		return null;

	}

	public static
	String toStringNull (
			Object object) {

		if (object == null)
			return null;

		return object.toString ();

	}

	public static
	String urlEncode (
			String string) {

		try {

			return URLEncoder.encode (
				string,
				"utf8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	byte[] stringToBytes (
			String string,
			String charset) {

		try {

			return string.getBytes (
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	String bytesToString (
			byte[] bytes,
			String charset) {

		try {

			return new String (
				bytes,
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	private final static
	Pattern nonAlphanumericWordsPattern =
		Pattern.compile ("[^a-z0-9]+");

	public static
	String codify (
			String string) {

		return string
			.toLowerCase ()
			.replaceAll ("[^a-z0-9]+", " ")
			.trim ()
			.replaceAll (" ", "_");

	}

	public static String simplify(String s) {
		return nonAlphanumericWordsPattern.matcher(s.toLowerCase()).replaceAll(
				" ").trim();
	}

	public static RuntimeException rethrow(Throwable t) {
		if (t instanceof Error)
			throw (Error) t;
		if (t instanceof RuntimeException)
			throw (RuntimeException) t;
		throw new RuntimeException(t);
	}

	public static String implode(String glue,
			Collection<? extends Object> pieces) {
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		for (Object piece : pieces) {
			if (first)
				first = false;
			else
				sb.append(glue);
			sb.append(piece.toString());
		}
		return sb.toString();
	}

	public static String implode(String glue, Object... pieces) {
		return implode(glue, Arrays.asList(pieces));
	}

	public static String implode(String glue, String... pieces) {
		return implode(glue, Arrays.asList(pieces));
	}

	static SimpleDateFormat isoDateFormat = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		isoDateFormat.setTimeZone (gmt);
	}

	public static String isoDate (Date date) {
		return isoDateFormat.format (date);
	}

	/**
	 * Does a null-safe equals.
	 */
	public static
	boolean equal (
			Object object1,
			Object object2) {

		if (object1 == object2)
			return true;

		if (object1 == null || object2 == null)
			return false;

		return object1.equals (object2);

	}

	public static
	boolean notEqual (
			Object left,
			Object right) {

		if (left == right)
			return false;

		if (left == null || right == null)
			return true;

		return ! left.equals (right);

	}

	/**
	 * Does a equalsIgnoreCase between any number of strings.
	 */
	public static
	boolean equalIgnoreCase (
			String... strings) {

		if (strings.length == 0)
			return true;

		String firstString =
			strings [0];

		for (String string
				: strings) {

			if (string == firstString)
				continue;

			if (! firstString.equalsIgnoreCase (string))
				return false;

		}

		return true;

	}

	public static
	String pluralise (
			long num,
			String singularNoun,
			String pluralNoun) {

		return num == 1L
			? "" + num + " " + singularNoun
			: "" + num + " " + pluralNoun;

	}

	public static
	String pluralise (
			long num,
			String singularNoun) {

		return num == 1L
			? "" + num + " " + singularNoun
			: "" + num + " " + singularNoun + "s";

	}

	public static <T>
	boolean in (
			T left,
			T... rights) {

		for (T right
				: rights) {

			if (equal (
					left,
					right))
				return true;

		}

		return false;

	}

	public static
	Date dateAddMs (
			Date date,
			int milliseconds) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			date);

		calendar.add (
			Calendar.MILLISECOND,
			milliseconds);

		return calendar.getTime ();

	}

	public static
	Date dateAddSecs (
			Date date,
			int seconds) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			date);

		calendar.add (
			Calendar.SECOND,
			seconds);

		return calendar.getTime ();

	}

	// TODO use LocalDate instead
	public static
	int age (
			TimeZone timeZone,
			long birth,
			long when) {

		Calendar calendar =
			new GregorianCalendar (timeZone);

		calendar.setTime (
			new Date (birth));

		int birthYear =
			calendar.get (Calendar.YEAR);

		int birthMonth =
			calendar.get (Calendar.MONTH);

		int birthDay =
			calendar.get (Calendar.DAY_OF_MONTH);

		calendar.setTime (
			new Date (when));

		int whenYear =
			calendar.get (Calendar.YEAR);

		int whenMonth =
			calendar.get (Calendar.MONTH);

		int whenDay =
			calendar.get (Calendar.DAY_OF_MONTH);

		int age =
			whenYear - birthYear;

		if (whenMonth < birthMonth)
			age --;

		if (whenMonth == birthMonth
				&& whenDay < birthDay)
			age --;

		return age;

	}

	public static
	int min (
			int... params) {

		int ret =
			params [0];

		for (int param : params) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	String capitalise (
			String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toUpperCase (string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String uncapitalise (
			String string) {

		if (string.length () == 0)
			return "";

		return Character.toLowerCase (string.charAt (0))
			+ string.substring (1);

	}

	public static
	void disallowNulls (
			Object... objects) {

		for (Object object
				: objects) {

			if (object == null)
				throw new NullPointerException ();

		}

	}

	public static
	String prettyHour (
			int hour) {

		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException ();

		if (hour == 0)
			return "12am";

		if (hour < 12)
			return "" + hour + "am";

		if (hour == 12)
			return "12pm";

		return "" + (hour - 12) + "pm";

	}

	public static
	String fixNewlines (
			String input) {

		return input
			.replaceAll ("\r\n", "\n")
			.replaceAll ("\r", "\n");

	}

	public static <T>
	Iterable<T> iterable (
			final Iterator<T> iterator) {

		return new Iterable<T> () {

			@Override
			public
			Iterator<T> iterator () {
				return iterator;
			}

		};

	}

	private static
	Pattern[] datePatterns = {
		Pattern.compile ("([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])"),
		Pattern.compile ("([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3]):([0-5][0-9])"),
		Pattern.compile ("([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3])"),
		Pattern.compile ("([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01])"),
		Pattern.compile ("([0-9]{4})-(0?[1-9]|1[0-2])"),
		Pattern.compile ("([0-9]{4})"),
		Pattern.compile("")
	};

	public static
	Date parseTimeAfter (
			String string) {

		int year = 0, month = 1, date = 1, hour = 0, minute = 0, second = 0;

		for (int i = 0; i < datePatterns.length; i++) {

			Matcher matcher =
				datePatterns[i].matcher (string);

			if (! matcher.matches ())
				continue;

			int groupCount =
				matcher.groupCount ();

			if (groupCount >= 1)
				year = Integer.parseInt (matcher.group (1));

			if (groupCount >= 2)
				month = Integer.parseInt (matcher.group (2)) - 1;

			if (groupCount >= 3)
				date = Integer.parseInt (matcher.group (3));

			if (groupCount >= 4)
				hour = Integer.parseInt (matcher.group (4));

			if (groupCount >= 5)
				minute = Integer.parseInt (matcher.group (5));

			if (groupCount >= 6)
				second = Integer.parseInt (matcher.group (6));

			Calendar calendar =
				new GregorianCalendar (
					year,
					month,
					date,
					hour,
					minute,
					second);

			return calendar.getTime ();

		}

		throw new TimeFormatException (
			"Date/time format not recognised");

	}

	public static
	Date parseTimeBefore (
			String string) {

		int year = 0, month = 1, date = 1, hour = 0, minute = 0, second = 0;

		for (int i = 0; i < datePatterns.length; i++) {

			Matcher matcher =
				datePatterns [i].matcher (string);

			if (! matcher.matches ())
				continue;

			int groupCount =
				matcher.groupCount ();

			if (groupCount >= 1)
				year = Integer.parseInt (matcher.group (1));

			if (groupCount >= 2)
				month = Integer.parseInt (matcher.group (2)) - 1;

			if (groupCount >= 3)
				date = Integer.parseInt (matcher.group (3));

			if (groupCount >= 4)
				hour = Integer.parseInt (matcher.group (4));

			if (groupCount >= 5)
				minute = Integer.parseInt (matcher.group (5));

			if (groupCount >= 6)
				second = Integer.parseInt (matcher.group (6));

			Calendar calendar =
				new GregorianCalendar (
					year,
					month,
					date,
					hour,
					minute,
					second);

			if (groupCount == 0)
				calendar.add (Calendar.YEAR, 10000);

			else if (groupCount == 1)
				calendar.add (Calendar.YEAR, 1);

			else if (groupCount == 2)
				calendar.add (Calendar.MONTH, 1);

			else if (groupCount == 3)
				calendar.add (Calendar.DATE, 1);

			else if (groupCount == 4)
				calendar.add (Calendar.HOUR, 1);

			else if (groupCount == 5)
				calendar.add (Calendar.MINUTE, 1);

			else
				calendar.add (Calendar.SECOND, 1);

			return calendar.getTime ();

		}

		throw new TimeFormatException (
			"Date/time format not recognised");

	}

	public static
	String stringFormat (
			Object... args) {

		return StringFormatter.standard (
			args);

	}

	public static
	String stringFormatArray (
			Object[] args) {

		return stringFormat (
			args);

	}

	@Deprecated
	public static
	void autoClose (
			Object object) {

		try {

			if (object == null)
				return;

			if (object instanceof File) {

				((File) object).delete ();

			} else if (object instanceof OutputStream) {

				((OutputStream) object).close ();

			} else if (object instanceof InputStream) {

				((InputStream) object).close ();

			} else {

				throw new RuntimeException (
					"Can't auto close " + object.getClass ());

			}

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	void autoClose  (
			Object... objects) {

		for (Object object : objects)
			autoClose (object);

	}

	public static
	File createTempFile (
			byte[] data,
			String extension)
		throws IOException {

		File file =
			File.createTempFile (
				"wbs-temp-",
				extension);

		boolean success = false;

		try {

			OutputStream outputStream =
				new FileOutputStream (file);

			outputStream.write (data);
			outputStream.close ();

			success = true;

		} finally {

			if (! success)
				file.delete ();

		}

		return file;

	}

	public static
	int runCommand (
			Logger logger,
			String... command)
		throws IOException {

		logger.info (
			stringFormat (
				"Executing %s",
				implode (" ", command)));

		Process process =
			Runtime.getRuntime ().exec (command);

		InputStream inputStream = null;

		try {

			// copy the error output to our standard error

			inputStream =
				process.getErrorStream ();

			BufferedReader bufferedReader =
				new BufferedReader (
					new InputStreamReader (
						inputStream,
						"utf-8"));

			String line;

			while ((line = bufferedReader.readLine ()) != null)
				logger.debug (line);

			bufferedReader.close ();

			return process.waitFor ();

		} catch (InterruptedException exception) {

			throw new RuntimeException (exception);

		} finally {

			autoClose (inputStream);

			try {
				process.waitFor ();
			} catch (Exception exception) { }

		}

	}

	public static
	byte[] runFilter (
			Logger logger,
			byte[] data,
			String inExt,
			String outExt,
			String... command) {

		return runFilterAdvanced (
			logger,
			data,
			inExt,
			outExt,
			ImmutableList.<List<String>>of (
				ImmutableList.<String>copyOf (
					command)));
	}

	public static
	byte[] runFilterAdvanced (
			Logger logger,
			byte[] data,
			String inExt,
			String outExt,
			List<List<String>> commands) {

		File inFile = null;
		File outFile = null;

		try {

			// create the input and output files

			inFile =
				createTempFile (
					data,
					inExt);

			outFile =
				File.createTempFile (
					"wbs-",
					outExt);

			// stick the filenames into the command

			for (List<String> command
					: commands) {

				String[] newCommand =
					new String [command.size ()];

				for (
					int index = 0;
					index < command.size ();
					index ++
				) {

					if (equal (command.get (index), "<in>")) {

						newCommand [index] =
							inFile.getPath ();

					} else if (equal (command.get (index), "<out>")) {

						newCommand [index] =
							outFile.getPath ();

					} else {

						newCommand [index] =
							command.get (index);

					}

				}

				// run the command

				int status =
					runCommand (
						logger,
						newCommand);

				if (status != 0)
					throw new RuntimeException ("Command returned " + status);

			}

			// read the output file

			return FileUtils.readFileToByteArray (outFile);

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		} finally {

			autoClose (
				inFile,
				outFile);

		}

	}

	public static
	byte[] fromHex (
			String hex) {

		byte[] bytes =
			new byte [hex.length () / 2];

		for (
			int index = 0;
			index < bytes.length;
			index ++
		) {

			bytes [index] = (byte)
				Integer.parseInt (
					hex.substring (
						2 * index,
						2 * index + 2),
					16);

		}

		return bytes;

	}

	static final
	byte[] HEX_CHAR_TABLE = {
		(byte) '0', (byte) '1', (byte) '2', (byte) '3',
		(byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
	};

	public static
	String toHex (
			byte[] byteValues) {

		byte[] hex =
			new byte [2 * byteValues.length];

		int index = 0;

		for (byte byteValue : byteValues) {

			int intValue =
				byteValue & 0xFF;

			hex [index ++] =
				HEX_CHAR_TABLE [intValue >>> 4];

			hex [index ++] =
				HEX_CHAR_TABLE [intValue & 0xf];

		}

		return bytesToString (
			hex,
			"ASCII");

	}

	public static
	String substring (
			Object object,
			int start,
			int end) {

		String string =
			object.toString ();

		if (start < 0) start = 0;

		if (end > string.length ())
			end = string.length ();

		return string.substring (start, end);

	}

	public static
	String hyphenToCamel (
			String string) {

		return delimitedToCamel (
			string,
			"-");

	}

	public static
	String underscoreToCamel (
			String string) {

		return delimitedToCamel (
			string,
			"_");

	}

	public static
	String delimitedToCamel (
			String string,
			String delimiter) {

		String[] parts =
			string.split (delimiter);

		StringBuilder ret =
			new StringBuilder (parts [0]);

		for (
			int index = 1;
			index < parts.length;
			index ++
		) {

			ret.append (
				Character.toUpperCase (
					parts [index].charAt (0)));

			ret.append (
				parts [index].substring (1));

		}

		return ret.toString ();

	}

	public static
	String camelToUnderscore (
			String string) {

		return camelToDelimited (
			string,
			"_");

	}

	public static
	String camelToHyphen (
			String string) {

		return camelToDelimited (
			string,
			"-");

	}

	public static
	String camelToSpaces (
			String string) {

		return camelToDelimited (
			string,
			" ");

	}

	public static
	String camelToDelimited (
			String string,
			String delimiter) {

		if (string == null)
			return null;

		StringBuilder stringBuilder =
			new StringBuilder (
				string.length () * 2);

		stringBuilder.append (
			Character.toLowerCase (
				string.charAt (0)));

		for (
			int pos = 1;
			pos < string.length ();
			pos ++
		) {

			char ch =
				string.charAt (pos);

			if (Character.isUpperCase (ch)) {

				stringBuilder.append (
					delimiter);

				stringBuilder.append (
					Character.toLowerCase (ch));

			} else {

				stringBuilder.append (ch);

			}

		}

		return stringBuilder.toString ();

	}

	private static final
	char[] lowercaseLetters =
		new char [26];

	private static final
	char[] digits =
		new char [10];

	static {

		for (
			int index = 0;
			index < 26;
			index ++
		) {

			lowercaseLetters [index] =
				(char) ('a' + index);

		}

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			digits [index] =
				(char) ('0' + index);

		}

	}

	private static final
	Random random =
		new Random ();

	public static
	String genRandom (
			char[] allSymbols,
			int requiredLength) {

		char[] buffer =
			new char [requiredLength];

		for (
			int index = 0;
			index < buffer.length;
			index ++
		) {

			buffer [index] =
				allSymbols [
					random.nextInt (
						allSymbols.length)];

		}

		return new String (
			buffer);

	}

	public static
	String generateTenCharacterToken () {

		return genRandom (
			lowercaseLetters,
			10);

	}

	public static
	String generateSixDigitCode () {

		return genRandom (
			digits,
			6);

	}

	public static <Type>
	Type pickRandom (
			Random random,
			Type[] options) {

		int index =
			random.nextInt (
				options.length);

		return options [index];

	}

	public static
	String joinWithSeparator (
		String separator,
		String prefix,
		Collection<String> parts,
		String suffix) {

		StringBuilder ret =
			new StringBuilder ();

		for (String part : parts) {

			if (ret.length () > 0)
				ret.append (separator);

			ret.append (prefix);
			ret.append (part);
			ret.append (suffix);

		}

		return ret.toString ();

	}

	public static
	String joinWithoutSeparator (
			Collection<String> parts) {

		return joinWithSeparator (
			"",
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			String separator,
			Collection<String> parts) {

		return joinWithSeparator (
			separator,
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			String separator,
			String... parts) {

		return joinWithSeparator (
			separator,
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithoutSeparator (
			String... parts) {

		return joinWithSeparator (
			"",
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String prettySize (
			int bytes) {

		int limit = 2;

		if (bytes < limit * 1024)
			return "" + bytes + " bytes";

		if (bytes < limit * 1024 * 1024)
			return "" + (bytes / 1024) + " kilobytes";

		if (bytes < limit * 1024 * 1024 * 1024)
			return "" + (bytes / 1024 / 1024) + " megabytes";

		return "" + (bytes / 1024 / 1024 / 1024) + " gigabytes";

	}

	public static
	RuntimeException todo () {
		return new RuntimeException ("TODO");
	}

	@SneakyThrows (NoSuchAlgorithmException.class)
	public static
	String hashSha1 (
			String string) {

		MessageDigest messageDigest =
			MessageDigest.getInstance ("SHA-1");

		messageDigest.update (
			string.getBytes ());

		return Base64.encodeBase64String (
			messageDigest.digest ());

	}

	public static
	String spacify (
			String input) {

		return spacify (
			input,
			32);

	}

	private final static
	Pattern nonWhitespaceWordsPattern =
		Pattern.compile ("\\S+");

	/**
	 * Returns a transformed version of a string, with all whitespace replaced
	 * by a single space and all words longer than wordLength split into
	 * wordLength or less.
	 *
	 * @param input
	 *            the input string
	 * @param wordLength
	 *            the maximum word length in the output string
	 * @return the transformed string
	 */
	public static
	String spacify (
			String input,
			int wordLength) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher matcher =
			nonWhitespaceWordsPattern.matcher (input);

		while (matcher.find ()) {

			String string =
				matcher.group (0);

			int position = 0;

			int length =
				string.length ();

			while (true) {

				if (stringBuilder.length () > 0)
					stringBuilder.append (' ');

				if (position + wordLength > length) {

					stringBuilder.append (
						string,
						position,
						length);

					break;

				}

				stringBuilder.append (
					string,
					position,
					position + wordLength);

				position += wordLength;

			}

		}

		return stringBuilder.toString ();

	}

	public static
	Instant dateToInstant (
			Date date) {

		if (date == null)
			return null;

		return new Instant (date);

	}

	public static
	Date instantToDate (
			Instant instant) {

		if (instant == null)
			return null;

		return instant.toDate ();

	}

	public static
	Instant millisToInstant (
			long millis) {

		return new Instant (millis);

	}

	public static
	String booleanToString (
			Boolean value,
			String yesString,
			String noString,
			String nullString) {

		if (value == null)
			return nullString;

		if (value == true)
			return yesString;

		if (value == false)
			return noString;

		throw new RuntimeException ();

	}

	public static
	Boolean stringToBoolean (
			@NonNull String string,
			@NonNull String yesString,
			@NonNull String noString,
			@NonNull String nullString) {

		if (equal (
				string,
				yesString))
			return true;

		if (equal (
				string,
				noString))
			return false;

		if (equal (
				string,
				nullString))
			return null;

		throw new RuntimeException (
			stringFormat (
				"Invalid boolean string: \"%s\"",
				string));

	}

	public static
	Boolean stringToBoolean (
			String value) {

		return stringToBoolean (
			value,
			"yes",
			"no",
			"");

	}

	public static
	List<String> split (
			String source,
			String regex) {

		return Arrays.asList (
			source.split (regex));

	}

	public static
	void doNothing () {

	}

	public static <Type>
	List<Type> maybeList (
			List<Type> value) {

		return value != null
			? value
			: Collections.<Type>emptyList ();

	}

	public static <Type>
	List<Type> maybeList (
			boolean condition,
			Type value) {

		return condition

			? Collections.<Type>singletonList (
				value)

			: Collections.<Type>emptyList ();

	}

	public static
	boolean isNull (
			Object object) {

		return object == null;

	}

	public static
	boolean isNotNull (
			Object object) {

		return object != null;

	}

	public static
	boolean lessThan (
			int left,
			int right) {

		return left < right;

	}

	public static
	boolean moreThan (
			int left,
			int right) {

		return left > right;

	}

	public static
	boolean doesNotStartWith (
			String string,
			String prefix) {

		return ! string.startsWith (prefix);

	}

	public static
	int sum (
			int value0,
			int value1) {

		return value0 + value1;

	}

	public static
	boolean allOf (
			boolean... values) {

		for (boolean value : values) {

			if (! value)
				return false;

		}

		return true;

	}

	public static
	boolean anyOf (
			boolean... values) {

		for (boolean value : values) {

			if (value)
				return true;

		}

		return false;

	}

	public static
	boolean not (
			boolean value) {

		return ! value;

	}

	public static
	boolean allFalse (
			boolean... values) {

		for (boolean value : values) {

			if (value)
				return false;

		}

		return true;

	}

	public static
	boolean isEmpty (
			Collection<?> collection) {

		return collection.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			String string) {

		if (string == null)
			return false;

		return ! string.isEmpty ();

	}

	public static
	URL stringToUrl (
			String urlString) {

		try {

			return new URL (
				urlString);

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Instant earliest (
			Instant... instants) {

		Instant earliest =
			null;

		for (Instant instant
				: instants) {

			if (

				earliest == null

				|| instant.isBefore (
					earliest)

			) {

				earliest =
					instant;

			}

		}

		return earliest;

	}

}