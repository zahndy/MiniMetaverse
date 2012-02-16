/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import libomv.types.UUID;

public class Helpers
{
	// This header flag signals that ACKs are appended to the packet
	public final static byte MSG_APPENDED_ACKS = 0x10;

	// This header flag signals that this packet has been sent before
	public final static byte MSG_RESENT = 0x20;

	// This header flags signals that an ACK is expected for this packet
	public final static byte MSG_RELIABLE = 0x40;

	// This header flag signals that the message is compressed using zerocoding
	public final static byte MSG_ZEROCODED = (byte) 0x80;

	public static final double DOUBLE_MAG_THRESHOLD = 1E-14f;
	public static final float FLOAT_MAG_THRESHOLD = 1E-7f;
	public static final float E = (float) Math.E;
	public static final float LOG10E = 0.4342945f;
	public static final float LOG2E = 1.442695f;
	public static final float PI = (float) Math.PI;
	public static final float TWO_PI = (float) (Math.PI * 2.0d);
	public static final float PI_OVER_TWO = (float) (Math.PI / 2.0d);
	public static final float PI_OVER_FOUR = (float) (Math.PI / 4.0d);

	/** Used for converting radians to degrees */
	public static final float RAD_TO_DEG = (float) (180.0d / Math.PI);

	/**
	 * Provide a single instance of the Locale class to help parsing in
	 * situations where the grid assumes an en-us culture
	 */
	public static final Locale EnUsCulture = new Locale("en", "us");

	/** Default encoding (UTF-8) */
	public static final String UTF8_ENCODING = "UTF-8";
	public static final String ASCII_ENCODING = "ASCII";

	public static final byte[] EmptyBytes = new byte[0];
	public static final String EmptyString = new String();

	public static final String NewLine = System.getProperty("line.separator");
	/** UNIX epoch in DateTime format */
	public static final Date Epoch = new Date(0);

	protected static final String FRACT_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss.SS'Z'";
	protected static final String WHOLE_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss'Z'";;

	/**
	 * Calculate the MD5 hash of a given string
	 * 
	 * @param password
	 *            The password to hash
	 * @return An MD5 hash in string format, with $1$ prepended
	 */
	public static String MD5Password(String password)
	{
		StringBuilder digest = new StringBuilder(32);
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(password.getBytes("ASCII"));

			// Convert the hash to a hex string
			for (byte b : hash)
			{
				digest.append(String.format(EnUsCulture, "%02x", b));
			}
			return "$1$" + digest.toString();
		}
		catch (Exception e)
		{
		}
		return EmptyString;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static float Clamp(float value, float min, float max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static double Clamp(double value, double min, double max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static int Clamp(int value, int min, int max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Round a floating-point value to the nearest integer
	 * 
	 * @param val
	 *            Floating point number to round
	 * @return Integer
	 */
	public static int Round(float val)
	{
		return (int) Math.floor(val + 0.5f);
	}

	/** Test if a single precision float is a finite number */
	public static boolean IsFinite(float value)
	{
		return !(Float.isNaN(value) || Float.isInfinite(value));
	}

	/** Test if a double precision float is a finite number */
	public static boolean IsFinite(double value)
	{
		return !(Double.isNaN(value) || Double.isInfinite(value));
	}

	/**
	 * Get the distance between two floating-point values
	 * 
	 * @param value1
	 *            First value
	 * @param value2
	 *            Second value
	 * @return The distance between the two values
	 */
	public static float Distance(float value1, float value2)
	{
		return Math.abs(value1 - value2);
	}

	public static float Hermite(float value1, float tangent1, float value2, float tangent2, float amount)
	{
		// All transformed to double not to lose precision
		// Otherwise, for high numbers of param:amount the result is NaN instead
		// of Infinity
		double v1 = value1, v2 = value2, t1 = tangent1, t2 = tangent2, s = amount, result;
		double sCubed = s * s * s;
		double sSquared = s * s;

		if (amount == 0f)
		{
			result = value1;
		}
		else if (amount == 1f)
		{
			result = value2;
		}
		else
		{
			result = (2d * v1 - 2d * v2 + t2 + t1) * sCubed + (3d * v2 - 3d * v1 - 2d * t1 - t2) * sSquared + t1 * s
					+ v1;
		}
		return (float) result;
	}

	public static double Hermite(double value1, double tangent1, double value2, double tangent2, double amount)
	{
		// All transformed to double not to lose precision
		// Otherwise, for high numbers of param:amount the result is NaN instead
		// of Infinity
		double v1 = value1, v2 = value2, t1 = tangent1, t2 = tangent2, s = amount, result;
		double sCubed = s * s * s;
		double sSquared = s * s;

		if (amount == 0d)
		{
			result = value1;
		}
		else if (amount == 1f)
		{
			result = value2;
		}
		else
		{
			result = (2d * v1 - 2d * v2 + t2 + t1) * sCubed + (3d * v2 - 3d * v1 - 2d * t1 - t2) * sSquared + t1 * s
					+ v1;
		}
		return result;
	}

	public static float Lerp(float value1, float value2, float amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static double Lerp(double value1, double value2, double amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static float SmoothStep(float value1, float value2, float amount)
	{
		// It is expected that 0 < amount < 1
		// If amount < 0, return value1
		// If amount > 1, return value2
		float result = Clamp(amount, 0f, 1f);
		return Hermite(value1, 0f, value2, 0f, result);
	}

	public static double SmoothStep(double value1, double value2, double amount)
	{
		// It is expected that 0 < amount < 1
		// If amount < 0, return value1
		// If amount > 1, return value2
		double result = Clamp(amount, 0f, 1f);
		return Hermite(value1, 0f, value2, 0f, result);
	}

	public static float ToDegrees(float radians)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = 180 / pi
		return (float) (radians * 57.295779513082320876798154814105);
	}

	public static float ToRadians(float degrees)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = pi / 180
		return (float) (degrees * 0.017453292519943295769236907684886);
	}

	// Packs to 32-bit unsigned integers in to a 64-bit unsigned integer
	//
	// <param name="a">The left-hand (or X) value</param>
	// <param name="b">The right-hand (or Y) value</param>
	// <returns>A 64-bit integer containing the two 32-bit input
	// values</returns>
	public static long IntsToLong(int a, int b)
	{
		return (((long) a << 32) + b);
	}

	// // Unpacks two 32-bit unsigned integers from a 64-bit unsigned integer//
	// // <param name="a">The 64-bit input integer</param>// <param name="b">The
	// left-hand (or X) output value</param>// <param name="c">The right-hand
	// (or Y) output value</param>
	public static void LongToUInts(long a, int[] b)
	{
		b[0] = (int) (a >> 32);
		b[1] = (int) (a & 0x00000000FFFFFFFF);
	}

	/**
	 * Converts a floating point number to a terse string format used for
	 * transmitting numbers in wearable asset files
	 * 
	 * @param val
	 *            Floating point number to convert to a string
	 * @return A terse string representation of the input number
	 */
	public static String FloatToTerseString(float val)
	{
		if (val == 0)
		{
			return ".00";
		}
		String s = String.format("%f", val);

		// Trim trailing zeroes
		int i = s.length();
		while (s.charAt(i - 1) == '0')
			i--;
		s = s.substring(0, i);

		// Remove superfluous decimal places after the trim
		if (s.charAt(i - 1) == '.')
		{
			s = s.substring(0, --i);
		}
		// Remove leading zeroes after a negative sign
		else if (s.charAt(0) == '-' && s.charAt(1) == '0')
		{
			s = "-" + s.substring(2, i);
		}
		// Remove leading zeroes in positive numbers
		else if (s.charAt(0) == '0')
		{
			s = s.substring(1, i);
		}
		return s;
	}

	// Convert a variable length field (byte array) to a String.
	//
	// <remarks>If the byte array has unprintable characters in it, a
	// hex dump will be put in the String instead</remarks>
	// <param name="bytes">The byte array to convert to a String</param>
	// <returns>A UTF8 String, minus the null terminator</returns>
	public static String FieldToString(byte[] bytes) throws Exception
	{
		return FieldToString(bytes, "");
	}

	// Convert a variable length field (byte array) to a String, with a
	// field name prepended to each line of the output.
	//
	// <remarks>If the byte array has unprintable characters in it, a
	// hex dump will be put in the String instead</remarks>
	// <param name="bytes">The byte array to convert to a String</param>
	// <param name="fieldName">A field name to prepend to each line of
	// output</param>
	// <returns>A UTF8 String, minus the null terminator</returns>
	public static String FieldToString(byte[] bytes, String fieldName) throws Exception
	{
		String output = "";
		boolean printable = true;

		for (byte element : bytes)
		{
			// Check if there are any unprintable characters in the array
			if ((element < 0x20 || element > 0x7E) && element != 0x09 && element != 0x0D && element != 0x0A
					&& element != 0x00)
			{
				printable = false;
				break;
			}
		}

		if (printable)
		{
			int length = bytes.length;
			if (length > 0)
			{
				output += fieldName + ": ";
			}
			if (bytes[length - 1] == 0)
				output += new String(bytes, 0, length - 1, UTF8_ENCODING);
			else
				output += new String(bytes, UTF8_ENCODING);
		}
		else
		{
			for (int i = 0; i < bytes.length; i += 16)
			{
				if (i != 0)
				{
					output += "\n";
				}
				if (fieldName != "")
				{
					output += fieldName + ": ";
				}

				for (int j = 0; j < 16; j++)
				{
					if ((i + j) < bytes.length)
					{
						String s = Integer.toHexString(bytes[i + j]);
						// String s = String.Format("{0:X} ", bytes[i + j]);
						if (s.length() == 2)
						{
							s = "0" + s;
						}

						output += s;
					}
					else
					{
						output += "   ";
					}
				}

				for (int j = 0; j < 16 && (i + j) < bytes.length; j++)
				{
					if (bytes[i + j] >= 0x20 && bytes[i + j] < 0x7E)
					{
						output += (char) bytes[i + j];
					}
					else
					{
						output += ".";
					}
				}
			}
		}

		return output;
	}

	/**
	 * Convert a UTF8 String to a byte array
	 * 
	 * @param str
	 *            The String to convert to a byte array
	 * @return A null-terminated byte array
	 * @throws Exception
	 */
	public static byte[] StringToField(String str) throws Exception
	{
		if (!str.endsWith("\0"))
		{
			str += "\0";
		}
		return str.getBytes(UTF8_ENCODING);
	}

	/**
	 * Converts a struct or class object containing fields only into a key value
	 * separated string
	 * 
	 * @param t
	 *            The struct object
	 * @return A string containing the struct fields as the keys, and the field
	 *         value as the value separated <example> <code>
	 *  // Add the following code to any struct or class containing only fields to override the toString()
	 *  // method to display the values of the passed object
	 * 
	 *  /** Print the struct data as a string
	 *   *  @return A string containing the field names, and field values
	 *   * /
	 * @Override public override string toString() { return
	 *           Helpers.StructToString(this); } </code> </example>
	 */
	public static String StructToString(Object t)
	{
		StringBuilder result = new StringBuilder();
		java.lang.Class<?> structType = t.getClass();

		for (Field field : structType.getDeclaredFields())
		{
			try
			{
				result.append(field.getName() + ": " + field.get(t).toString() + " ");
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		result.append("\n");
		return result.toString().trim();
	}

	public static double GetUnixTime()
	{
		return Epoch.getTime() / 1000.0;
	}

	public static double DateTimeToUnixTime(Date date)
	{
		return date.getTime() / 1000.0;
	}

	public static Date UnixTimeToDateTime(double time)
	{
		return new Date(Math.round(time * 1000.0));
	}

	public static Date StringToDate(String string)
	{
		SimpleDateFormat df = new SimpleDateFormat(FRACT_DATE_FMT);
		try
		{
			return df.parse(string);
		}
		catch (ParseException ex1)
		{
			try
			{
				df.applyPattern(WHOLE_DATE_FMT);
				return df.parse(string);
			}
			catch (ParseException ex2)
			{
			}
		}
		return Helpers.Epoch;
	}

	/**
	 * Encode a byte array with zerocoding. Used to compress packets marked with
	 * the zerocoded flag. Any zeroes in the array are compressed down to a
	 * single zero byte followed by a count of how many zeroes to expand out. A
	 * single zero becomes 0x00 0x01, two zeroes becomes 0x00 0x02, three zeroes
	 * becomes 0x00 0x03, etc. The first four bytes are copied directly to the
	 * output buffer.
	 * 
	 * @param src
	 *            The byte buffer to encode
	 * @param dest
	 *            The output byte array to encode to
	 * @return The length of the output buffer
	 */
	public static int ZeroEncode(ByteBuffer src, byte[] dest)
	{
		int bodylen, zerolen = 6 + src.get(5);
		byte zerocount = 0;
		int srclen = src.position();

		src.position(0);
		src.get(dest, 0, zerolen);

		if ((src.get(0) & MSG_APPENDED_ACKS) == 0)
		{
			bodylen = srclen;
		}
		else
		{
			bodylen = srclen - src.get(srclen - 1) * 4 - 1;
		}

		int i;
		for (i = zerolen; i < bodylen; i++)
		{
			if (src.get(i) == 0x00)
			{
				zerocount++;

				if (zerocount == 0)
				{
					dest[zerolen++] = 0x00;
					dest[zerolen++] = (byte) 0xff;
					zerocount++;
				}
			}
			else
			{
				if (zerocount != 0)
				{
					dest[zerolen++] = 0x00;
					dest[zerolen++] = zerocount;
					zerocount = 0;
				}
				dest[zerolen++] = src.get(i);
			}
		}

		if (zerocount != 0)
		{
			dest[zerolen++] = 0x00;
			dest[zerolen++] = zerocount;
		}
		// copy appended ACKs
		for (; i < srclen; i++)
		{
			dest[zerolen++] = src.get(i);
		}
		return zerolen;
	}

	/**
	 * Given an X/Y location in absolute (grid-relative) terms, a region handle
	 * is returned along with the local X/Y location in that region
	 * 
	 * @param globalX
	 *            The absolute X location, a number such as 255360.35
	 * @param globalY
	 *            The absolute Y location, a number such as 255360.35
	 * @param locals
	 *            [0] The returened sim-local X position of the global X
	 * @param locals
	 *            [1] The returned sim-local Y position of the global Y
	 * @return A 64-bit region handle that can be used to teleport to
	 */
	public static long GlobalPosToRegionHandle(float globalX, float globalY, float[] locals)
	{
		int x = ((int) globalX >> 8) << 8;
		int y = ((int) globalY >> 8) << 8;
		locals[0] = globalX - x;
		locals[1] = globalY - y;
		return IntsToLong(x, y);
	}

	// Calculates the CRC (cyclic redundancy check) needed to upload inventory.
	//
	// <param name="creationDate">Creation date</param>
	// <param name="saleType">Sale type</param>
	// <param name="invType">Inventory type</param>
	// <param name="type">Type</param>
	// <param name="assetID">Asset ID</param>
	// <param name="groupID">Group ID</param>
	// <param name="salePrice">Sale price</param>
	// <param name="ownerID">Owner ID</param>
	// <param name="creatorID">Creator ID</param>
	// <param name="itemID">Item ID</param>
	// <param name="folderID">Folder ID</param>
	// <param name="everyoneMask">Everyone mask (permissions)</param>
	// <param name="flags">Flags</param>
	// <param name="nextOwnerMask">Next owner mask (permissions)</param>
	// <param name="groupMask">Group mask (permissions)</param>
	// <param name="ownerMask">Owner mask (permisions)</param>
	// <returns>The calculated CRC</returns>
	public static int InventoryCRC(int creationDate, byte saleType, byte invType, byte type, UUID assetID,
			UUID groupID, int salePrice, UUID ownerID, UUID creatorID, UUID itemID, UUID folderID, int everyoneMask,
			int flags, int nextOwnerMask, int groupMask, int ownerMask)
	{
		int CRC = 0;

		// IDs
		CRC += assetID.CRC(); // AssetID
		CRC += folderID.CRC(); // FolderID
		CRC += itemID.CRC(); // ItemID

		// Permission stuff
		CRC += creatorID.CRC(); // CreatorID
		CRC += ownerID.CRC(); // OwnerID
		CRC += groupID.CRC(); // GroupID

		// CRC += another 4 words which always seem to be zero -- unclear if
		// this is a LLUUID or what
		CRC += ownerMask;
		CRC += nextOwnerMask;
		CRC += everyoneMask;
		CRC += groupMask;

		// The rest of the CRC fields
		CRC += flags; // Flags
		CRC += invType; // InvType
		CRC += type; // Type
		CRC += creationDate; // CreationDate
		CRC += salePrice; // SalePrice
		CRC += (saleType * 0x07073096); // SaleType

		return CRC;
	}

	public static String toHexText(byte[] raw_digest)
	{
		// and convert it to hex-text
		StringBuffer checksum = new StringBuffer(raw_digest.length * 2);
		for (byte element : raw_digest)
		{
			int b = element & 0xFF;
			String hex_value = Integer.toHexString(b);
			if (hex_value.length() == 1)
			{
				// the Java function returns a single digit if hex is < 0x10
				checksum.append("0");
			}
			checksum.append(hex_value);
		}
		return checksum.toString();
	}

	public static Vector<String> split(String s, String c)
	{
		Vector<String> v = new Vector<String>();
		StringTokenizer tokens = new StringTokenizer(s, c);
		while (tokens.hasMoreTokens())
		{
			v.addElement(tokens.nextToken());
		}
		return v;
	}

	public static String replaceAll(String line, String from, String to)
	{
		if (line == null)
		{
			return null;
		}
		if (from == null || from.equals(""))
		{
			return "";
		}
		StringBuffer buf = new StringBuffer();
		int line_pos = 0;
		do
		{
			int pos = line.indexOf(from, line_pos);
			if (pos == -1)
			{
				pos = line.length();
			}
			String chunk = line.substring(line_pos, pos);
			buf.append(chunk);
			if (pos != line.length())
			{
				buf.append(to);
			}
			line_pos += chunk.length() + from.length();
		} while (line_pos < line.length());
		return buf.toString();
	}

	public static String join(String delimiter, String[] strings)
	{
		if (strings.length == 0)
			return EmptyString;
		int capacity = (strings.length - 1) * delimiter.length();
		for (String s : strings)
		{
			capacity += s.length();
		}

		StringBuilder buffer = new StringBuilder(capacity);
		for (String s : strings)
		{
			if (capacity < 0)
				buffer.append(delimiter);
			buffer.append(s);
			capacity = -1;
		}
		return buffer.toString();
	}

	/**
	 * Convert the first two bytes starting in the byte array in little endian
	 * ordering to a signed short integer
	 * 
	 * @param bytes
	 *            An array two bytes or longer
	 * @return A signed short integer, will be zero if a short can't be read at
	 *         the given position
	 */
	public static short BytesToInt16L(byte[] bytes)
	{
		return BytesToInt16L(bytes, 0);
	}

	/**
	 * Convert the first two bytes starting at the given position in little
	 * endian ordering to a signed short integer
	 * 
	 * @param bytes
	 *            An array two bytes or longer
	 * @param pos
	 *            Position in the array to start reading
	 * @return A signed short integer, will be zero if a short can't be read at
	 *         the given position
	 */
	public static short BytesToInt16L(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return (short) (((bytes[pos + 0] & 0xff) << 0) + ((bytes[pos + 1] & 0xff) << 8));
	}

	public static short BytesToInt16B(byte[] bytes)
	{
		return BytesToInt16B(bytes, 0);
	}

	public static short BytesToInt16B(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return (short) (((bytes[pos + 0] & 0xff) << 8) + ((bytes[pos + 1] & 0xff) << 0));
	}

	/**
	 * Convert the first four bytes of the given array in little endian ordering
	 * to a signed integer
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @return A signed integer, will be zero if the array contains less than
	 *         four bytes
	 */
	public static int BytesToInt32L(byte[] bytes)
	{
		return BytesToInt32L(bytes, 0);
	}

	/**
	 * Convert the first four bytes starting at the given position in little
	 * endian ordering to a signed integer
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @param pos
	 *            Position to start reading the int from
	 * @return A signed integer, will be zero if an int can't be read at the
	 *         given position
	 */
	public static int BytesToInt32L(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		return (((bytes[pos + 0] & 0xff) >> 0) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((bytes[pos + 3] & 0xff) << 24));
	}

	public static int BytesToInt32B(byte[] bytes)
	{
		return BytesToInt32B(bytes, 0);
	}

	public static int BytesToInt32B(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		return (((bytes[pos + 0] & 0xff) >> 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + ((bytes[pos + 3] & 0xff) << 0));
	}

	/**
	 * Convert the first eight bytes of the given array in little endian
	 * ordering to a signed long integer
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return A signed long integer, will be zero if the array contains less
	 *         than eight bytes
	 */
	public static long BytesToInt64L(byte[] bytes)
	{
		return BytesToInt64L(bytes, 0);
	}

	/**
	 * Convert the first eight bytes starting at the given position in little
	 * endian ordering to a signed long integer
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @param pos
	 *            Position to start reading the long from
	 * @return A signed long integer, will be zero if a long can't be read at
	 *         the given position
	 */
	public static long BytesToInt64L(byte[] bytes, int pos)
	{
		if (bytes.length < 8)
		{
			return 0;
		}
		long low = (((bytes[pos + 0] & 0xff) >> 0) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((bytes[pos + 3] & 0xff) << 24));
		long high = (((bytes[pos + 4] & 0xff) >> 0) + ((bytes[pos + 5] & 0xff) << 8) + ((bytes[pos + 6] & 0xff) << 16) + ((bytes[pos + 7] & 0xff) << 24));
		return (high << 32) + (low & 0xffffffff);
	}

	public static long BytesToInt64B(byte[] bytes)
	{
		return BytesToInt64B(bytes, 0);
	}

	public static long BytesToInt64B(byte[] bytes, int pos)
	{
		if (bytes.length < 8)
		{
			return 0;
		}
		long high = (((bytes[pos + 0] & 0xff) >> 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + ((bytes[pos + 3] & 0xff) << 0));
		long low = (((bytes[pos + 4] & 0xff) >> 24) + ((bytes[pos + 5] & 0xff) << 16) + ((bytes[pos + 6] & 0xff) << 8) + ((bytes[pos + 7] & 0xff) << 0));
		return (high << 32) + (low & 0xffffffff);
	}

	/**
	 * Convert two bytes in little endian ordering to an int
	 * 
	 * @param bytes
	 *            Byte array containing the ushort
	 * @return An int, will be zero if a ushort can't be read
	 */
	public static int BytesToUInt16L(byte[] bytes)
	{
		return BytesToUInt16L(bytes, 0);
	}

	/**
	 * Convert the first two bytes starting at the given position in little
	 * endian ordering to an int
	 * 
	 * @param bytes
	 *            Byte array containing the ushort
	 * @param pos
	 *            Position to start reading the ushort from
	 * @return An int, will be zero if a ushort can't be read at the given
	 *         position
	 */
	public static int BytesToUInt16L(byte[] bytes, int pos)
	{
		if (bytes.length <= pos + 1)
		{
			return 0;
		}
		return (((bytes[pos + 0] & 0xff) << 0) + ((bytes[pos + 1] & 0xff) << 8));
	}

	public static int BytesToUInt16B(byte[] bytes)
	{
		return BytesToUInt16B(bytes, 0);
	}

	public static int BytesToUInt16B(byte[] bytes, int pos)
	{
		if (bytes.length <= pos + 1)
		{
			return 0;
		}
		return (((bytes[pos + 0] & 0xff) << 8) + ((bytes[pos + 1] & 0xff) << 0));
	}

	/**
	 * Convert the first four bytes of the given array in little endian ordering
	 * to long
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @return An unsigned integer, will be zero if the array contains less than
	 *         four bytes
	 */
	public static long BytesToUInt32L(byte[] bytes)
	{
		return BytesToUInt32L(bytes, 0);
	}

	/**
	 * Convert the first four bytes starting at the given position in little
	 * endian ordering to a long
	 * 
	 * @param bytes
	 *            Byte array containing the uint
	 * @param pos
	 *            Position to start reading the uint from
	 * @return An unsigned integer, will be zero if a uint can't be read at the
	 *         given position
	 */
	public static long BytesToUInt32L(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		long low = (((bytes[pos + 0] & 0xff) << 0) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16));
		long high = bytes[pos + 3] & 0xff;
		return (high << 24) + (0xffffffffL & low);
	}

	public static long BytesToUInt32B(byte[] bytes)
	{
		return BytesToUInt32B(bytes, 0);
	}

	public static long BytesToUInt32B(byte[] bytes, int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		long low = (((bytes[pos + 3] & 0xff) << 0) + ((bytes[pos + 2] & 0xff) << 8) + ((bytes[pos + 1] & 0xff) << 16));
		long high = bytes[pos + 0] & 0xff;
		return (high << 24) + (0xffffffffL & low);
	}

	/**
	 * Convert the first eight bytes of the given array in little endian
	 * ordering to a constrained long
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return An unsigned 64-bit integer, will be zero if the array contains
	 *         less than eight bytes
	 */
	public static long BytesToUInt64L(byte[] bytes)
	{
		return BytesToUInt64L(bytes, 0);
	}

	/**
	 * Convert the first eight bytes starting at the given position in little
	 * endian ordering to a constrained long
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return A long integer, will be zero if the array contains less than
	 *         eight bytes and 0x7fffffff if the resulting value would exceed
	 *         the positive limit of a long
	 */
	public static long BytesToUInt64L(byte[] bytes, int pos)
	{
		if (bytes.length < 8)
		{
			return 0;
		}

		if ((bytes[pos + 7] & 0xff) < 0)
		{
			return 0x7fffffff;
		}
		long low = (((bytes[pos + 0] & 0xff) >> 0) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((bytes[pos + 3] & 0xff) << 24));
		long high = (((bytes[pos + 4] & 0xff) >> 0) + ((bytes[pos + 5] & 0xff) << 8) + ((bytes[pos + 6] & 0xff) << 16) + ((bytes[pos + 7] & 0xff) << 24));
		return (high << 32) + (low & 0xffffffff);
	}

	public static long BytesToUInt64B(byte[] bytes)
	{
		return BytesToUInt64B(bytes, 0);
	}

	public static long BytesToUInt64B(byte[] bytes, int pos)
	{
		if (bytes.length < 8)
		{
			return 0;
		}

		if ((bytes[pos + 0] & 0xff) < 0)
		{
			return 0x7fffffff;
		}
		long high = (((bytes[pos + 0] & 0xff) >> 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + ((bytes[pos + 3] & 0xff) << 0));
		long low = (((bytes[pos + 4] & 0xff) >> 24) + ((bytes[pos + 5] & 0xff) << 16) + ((bytes[pos + 6] & 0xff) << 8) + ((bytes[pos + 7] & 0xff) << 0));
		return (high << 32) + (low & 0xffffffff);
	}

	/**
	 * Convert four bytes starting at the given position in little endian
	 * ordering to a floating point value
	 * 
	 * @param bytes
	 *            Byte array containing a little ending floating point value
	 * @param pos
	 *            Starting position of the floating point value in the byte
	 *            array
	 * @return Single precision value
	 */
	public static float BytesToFloatL(byte[] bytes, int pos)
	{
		return Float.intBitsToFloat(BytesToInt32L(bytes, pos));
	}

	public static float BytesToFloatB(byte[] bytes, int pos)
	{
		return Float.intBitsToFloat(BytesToInt32B(bytes, pos));
	}

	/**
	 * Convert eight bytes starting at the given position in little endian
	 * ordering to a double floating point value
	 * 
	 * @param bytes
	 *            Byte array containing a little ending double floating point
	 *            value
	 * @param pos
	 *            Starting position of the double floating point value in the
	 *            byte array
	 * @return Double precision value
	 */
	public static double BytesToDoubleL(byte[] bytes, int pos)
	{
		return Double.longBitsToDouble(BytesToInt64L(bytes, pos));
	}

	public static double BytesToDoubleB(byte[] bytes, int pos)
	{
		return Double.longBitsToDouble(BytesToInt64B(bytes, pos));
	}

	private static float FixedToFloat(float fixedVal, boolean signed, int intBits, int fracBits)
	{
		int minVal;
		int maxVal;

		if (signed)
		{
			minVal = 1 << intBits;
			minVal *= -1;
		}
		maxVal = 1 << intBits;
		fixedVal /= (1 << fracBits);

		if (signed)
		{
			fixedVal -= maxVal;
		}
		return fixedVal;
	}

	public static float BytesToFixedL(byte[] bytes, int pos, boolean signed, int intBits, int fracBits)
	{
		int totalBits = intBits + fracBits;
		;
		float fixedVal;

		if (signed)
		{
			totalBits++;
		}

		if (totalBits <= 8)
		{
			fixedVal = bytes[pos];
		}
		else if (totalBits <= 16)
		{
			fixedVal = BytesToUInt16L(bytes, pos);
		}
		else if (totalBits <= 32)
		{
			fixedVal = BytesToUInt32L(bytes, pos);
		}
		else
		{
			fixedVal = BytesToUInt64L(bytes, pos);
		}
		return FixedToFloat(fixedVal, signed, intBits, fracBits);
	}

	public static float BytesToFixedB(byte[] bytes, int pos, boolean signed, int intBits, int fracBits)
	{
		int totalBits = intBits + fracBits;
		;
		float fixedVal;

		if (signed)
		{
			totalBits++;
		}

		if (totalBits <= 8)
		{
			fixedVal = bytes[pos];
		}
		else if (totalBits <= 16)
		{
			fixedVal = BytesToUInt16B(bytes, pos);
		}
		else if (totalBits <= 32)
		{
			fixedVal = BytesToUInt32B(bytes, pos);
		}
		else
		{
			fixedVal = BytesToUInt64B(bytes, pos);
		}
		return FixedToFloat(fixedVal, signed, intBits, fracBits);
	}

	/**
	 * Convert a short to a byte array in little endian format
	 * 
	 * @param value
	 *            The short to convert
	 * @return A four byte little endian array
	 */
	public static byte[] Int16ToBytesL(short value)
	{
		byte[] bytes = new byte[2];
		Int16ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static void Int16ToBytesL(short value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
	}

	public static byte[] Int16ToBytesB(short value)
	{
		byte[] bytes = new byte[2];
		Int16ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int16ToBytesB(short value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 8) & 0xff);
		dest[pos + 1] = (byte) ((value >> 0) & 0xff);
		return 2;
	}

	public static byte[] UInt16ToBytesL(int value)
	{
		byte[] bytes = new byte[2];
		UInt16ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt16ToBytesL(int value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		return 2;
	}

	public static byte[] UInt16ToBytesB(int value)
	{
		byte[] bytes = new byte[2];
		UInt16ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt16ToBytesB(int value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 8) & 0xff);
		dest[pos + 1] = (byte) ((value >> 0) & 0xff);
		return 2;
	}

	/**
	 * Convert an integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The integer to convert
	 * @return A four byte little endian array
	 */
	public static byte[] Int32ToBytesL(int value)
	{
		byte[] bytes = new byte[4];
		Int32ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int Int32ToBytesL(int value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		return 4;
	}

	public static byte[] Int32ToBytesB(int value)
	{
		byte[] bytes = new byte[4];
		Int32ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int32ToBytesB(int value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 24) & 0xff);
		dest[pos + 1] = (byte) ((value >> 16) & 0xff);
		dest[pos + 2] = (byte) ((value >> 8) & 0xff);
		dest[pos + 3] = (byte) ((value >> 0) & 0xff);
		return 4;
	}

	public static byte[] UInt32ToBytesL(long value)
	{
		byte[] bytes = new byte[4];
		UInt32ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt32ToBytesL(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		return 4;
	}

	public static byte[] UInt32ToBytesB(long value)
	{
		byte[] bytes = new byte[4];
		UInt32ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt32ToBytesB(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 24) & 0xff);
		dest[pos + 1] = (byte) ((value >> 16) & 0xff);
		dest[pos + 2] = (byte) ((value >> 8) & 0xff);
		dest[pos + 3] = (byte) ((value >> 0) & 0xff);
		return 4;
	}

	/**
	 * Convert a 64-bit integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The value to convert
	 * @return An 8 byte little endian array
	 */
	public static byte[] Int64ToBytesL(long value)
	{
		byte[] bytes = new byte[8];
		Int64ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int Int64ToBytesL(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		dest[pos + 4] = (byte) ((value >> 32) & 0xff);
		dest[pos + 5] = (byte) ((value >> 40) & 0xff);
		dest[pos + 6] = (byte) ((value >> 48) & 0xff);
		dest[pos + 7] = (byte) ((value >> 56) & 0xff);
		return 8;
	}

	public static byte[] Int64ToBytesB(long value)
	{
		byte[] bytes = new byte[8];
		Int64ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int64ToBytesB(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 56) & 0xff);
		dest[pos + 1] = (byte) ((value >> 48) & 0xff);
		dest[pos + 2] = (byte) ((value >> 40) & 0xff);
		dest[pos + 3] = (byte) ((value >> 32) & 0xff);
		dest[pos + 4] = (byte) ((value >> 24) & 0xff);
		dest[pos + 5] = (byte) ((value >> 16) & 0xff);
		dest[pos + 6] = (byte) ((value >> 8) & 0xff);
		dest[pos + 7] = (byte) ((value >> 0) & 0xff);
		return 8;
	}

	/**
	 * Convert a 64-bit unsigned integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The value to convert
	 * @return An 8 byte little endian array
	 */
	public static byte[] UInt64ToBytesL(long value)
	{
		byte[] bytes = new byte[8];
		UInt64ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt64ToBytesL(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		dest[pos + 4] = (byte) ((value >> 32) & 0xff);
		dest[pos + 5] = (byte) ((value >> 40) & 0xff);
		dest[pos + 6] = (byte) ((value >> 48) & 0xff);
		dest[pos + 7] = (byte) ((value >> 56) & 0xff);
		return 8;
	}

	public static byte[] UInt64ToBytesB(long value)
	{
		byte[] bytes = new byte[8];
		UInt64ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt64ToBytesB(long value, byte[] dest, int pos)
	{
		dest[pos + 0] = (byte) ((value >> 56) & 0xff);
		dest[pos + 1] = (byte) ((value >> 48) & 0xff);
		dest[pos + 2] = (byte) ((value >> 40) & 0xff);
		dest[pos + 3] = (byte) ((value >> 32) & 0xff);
		dest[pos + 4] = (byte) ((value >> 24) & 0xff);
		dest[pos + 5] = (byte) ((value >> 16) & 0xff);
		dest[pos + 6] = (byte) ((value >> 8) & 0xff);
		dest[pos + 7] = (byte) ((value >> 0) & 0xff);
		return 8;
	}

	/**
	 * Convert a floating point value to four bytes in little endian ordering
	 * 
	 * @param value
	 *            A floating point value
	 * @return A four byte array containing the value in little endian ordering
	 */
	public static byte[] FloatToBytesL(float value)
	{
		byte[] bytes = new byte[4];
		Int32ToBytesL(Float.floatToIntBits(value), bytes, 0);
		return bytes;
	}

	public static int FloatToBytesL(float value, byte[] dest, int pos)
	{
		return Int32ToBytesL(Float.floatToIntBits(value), dest, pos);
	}

	public static byte[] FloatToBytesB(float value)
	{
		byte[] bytes = new byte[4];
		Int32ToBytesB(Float.floatToIntBits(value), bytes, 0);
		return bytes;
	}

	public static int FloatToBytesB(float value, byte[] dest, int pos)
	{
		return Int32ToBytesB(Float.floatToIntBits(value), dest, pos);
	}

	public static byte[] DoubleToBytesL(double value)
	{
		byte[] bytes = new byte[8];
		Int64ToBytesL(Double.doubleToLongBits(value), bytes, 0);
		return bytes;
	}

	public static int DoubleToBytesL(double value, byte[] dest, int pos)
	{
		return Int64ToBytesL(Double.doubleToLongBits(value), dest, pos);
	}

	public static byte[] DoubleToBytesB(double value)
	{
		byte[] bytes = new byte[8];
		Int64ToBytesB(Double.doubleToLongBits(value), bytes, 0);
		return bytes;
	}

	public static int DoubleToBytesB(double value, byte[] dest, int pos)
	{
		return Int64ToBytesB(Double.doubleToLongBits(value), dest, pos);
	}

	private static float FloatToFixed(float data, boolean isSigned, int intBits, int fracBits)
	{
		int min, max;

		if (isSigned)
		{
			min = 1 << intBits;
			min *= -1;
		}
		else
		{
			min = 0;
		}

		max = 1 << intBits;

		float fixedVal = Clamp(data, min, max);
		if (isSigned)
		{
			fixedVal += max;
		}
		fixedVal *= 1 << fracBits;
		return fixedVal;
	}

	public static int FixedToBytesL(byte[] dest, int pos, float data, boolean isSigned, int intBits, int fracBits)
	{
		int totalBits = intBits + fracBits;
		if (isSigned)
		{
			totalBits++;
		}

		if (totalBits <= 8)
		{
			dest[pos] = (byte) FloatToFixed(data, isSigned, intBits, fracBits);
			return 1;
		}
		else if (totalBits <= 16)
		{
			UInt16ToBytesL((int) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 2;
		}
		else if (totalBits <= 31)
		{
			UInt32ToBytesL((long) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 4;
		}
		else
		{
			UInt64ToBytesL((long) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 8;
		}
	}

	public static int FixedToBytesB(byte[] dest, int pos, float data, boolean isSigned, int intBits, int fracBits)
	{
		int totalBits = intBits + fracBits;
		if (isSigned)
		{
			totalBits++;
		}

		if (totalBits <= 8)
		{
			dest[pos] = (byte) FloatToFixed(data, isSigned, intBits, fracBits);
			return 1;
		}
		else if (totalBits <= 16)
		{
			UInt16ToBytesB((int) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 2;
		}
		else if (totalBits <= 31)
		{
			UInt32ToBytesB((long) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 4;
		}
		else
		{
			UInt64ToBytesB((long) FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 8;
		}
	}

	/**
	 * Packs two 32-bit unsigned integers in to a 64-bit unsigned integer
	 * 
	 * @param a
	 *            The left-hand (or X) value
	 * @param b
	 *            The right-hand (or Y) value
	 * @return A 64-bit integer containing the two 32-bit input values
	 */
	public static long UIntsToLong(int a, int b)
	{
		return ((long) a << 32) | b;
	}

	/**
	 * Unpacks two 32-bit unsigned integers from a 64-bit unsigned integer
	 * 
	 * @param a
	 *            The 64-bit input integer
	 * @param b
	 *            The left-hand (or X) output value
	 * @param c
	 *            The right-hand (or Y) output value
	 */
	public static void LongToUInts(long a, RefObject<Integer> b, RefObject<Integer> c)
	{
		b.argvalue = (int) (a >> 32);
		c.argvalue = (int) (a & 0x00000000FFFFFFFF);
	}

	/**
	 * Swaps the high and low nibbles in a byte. Converts aaaabbbb to bbbbaaaa
	 * 
	 * @param value
	 *            Byte to swap the nibbles in
	 * @return Byte value with the nibbles swapped
	 */
	public static byte SwapNibbles(byte value)
	{
		return (byte) (((value & 0xF0) >> 4) | ((value & 0x0F) << 4));
	}

	/**
	 * Converts an unsigned integer to a hexadecimal string
	 * 
	 * @param i
	 *            An unsigned integer to convert to a string
	 * @return A hexadecimal string 10 characters long
	 *         <example>0x7fffffff</example>
	 */
	public static String UInt32ToHexString(long i)
	{
		return String.format("%08x", i);
	}

	/**
	 * read a variable length UTF8 byte array to a string, consuming  len characters
	 * 
	 * @param bytes
	 *            The UTF8 encoded byte array to convert
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String readString(InputStream is, int len) throws IOException
	{
		byte[] bytes = new byte[len];
		is.read(bytes);
		return BytesToString(bytes, 0, len);
	}
	
	/**
	 * Convert a variable length UTF8 byte array to a string
	 * 
	 * @param bytes
	 *            The UTF8 encoded byte array to convert
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String BytesToString(byte[] bytes) throws UnsupportedEncodingException
	{
		return BytesToString(bytes, 0, bytes.length);
	}

	/**
	 * Convert a variable length UTF8 byte array to a string
	 * 
	 * @param bytes
	 *            The UTF8 encoded byte array to convert
	 * @param offset
	 *            The offset into the byte array from which to start
	 * @param length
	 *            The number of bytes to consume < 0 will search for null
	 *            terminating byte starting from offset
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String BytesToString(byte[] bytes, int offset, int length) throws UnsupportedEncodingException
	{
		if (length < 0)
		{
			/* Search for the null terminating byte */
			for (length = 0; bytes[offset + length] != 0; length++)
				;
		}
		else if (length > 0)
		{
			/* Backtrack possible null terminating bytes */
			for (; length > 0 && bytes[offset + length - 1] == 0; length--);
		}

		if (length == 0)
			return EmptyString;

		return new String(bytes, offset, length, UTF8_ENCODING);
	}

	/**
	 * Converts a byte array to a string containing hexadecimal characters
	 * 
	 * @param bytes
	 *            The byte array to convert to a string
	 * @param fieldName
	 *            The name of the field to prepend to each line of the string
	 * @return A string containing hexadecimal characters on multiple lines.
	 *         Each line is prepended with the field name
	 */
	public static String BytesToHexString(byte[] bytes, String fieldName)
	{
		return BytesToHexString(bytes, 0, bytes.length, fieldName);
	}

	/**
	 * Converts a byte array to a string containing hexadecimal characters
	 * 
	 * @param bytes
	 *            The byte array to convert to a string
	 * @param offset
	 *            The offset into the byte array from which to start
	 * @param length
	 *            Number of bytes in the array to parse
	 * @param fieldName
	 *            A string to prepend to each line of the hex dump
	 * @return A string containing hexadecimal characters on multiple lines.
	 *         Each line is prepended with the field name
	 */
	public static String BytesToHexString(byte[] bytes, int offset, int length, String fieldName)
	{
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < length; i += 16)
		{
			if (i != 0)
			{
				output.append('\n');
			}

			if (fieldName.length() > 0)
			{
				output.append(fieldName);
				output.append(": ");
			}

			for (int j = 0; j < 16; j++)
			{
				if ((i + j) < length)
				{
					if (j != 0)
					{
						output.append(' ');
					}
					output.append(String.format("%2x", bytes[offset + i + j]));
				}
			}
		}
		return output.toString();
	}

	/**
	 * Convert a string to a UTF8 encoded byte array
	 * 
	 * @param str
	 *            The string to convert
	 * @return A null-terminated UTF8 byte array
	 */
	public static byte[] StringToBytes(String str)
	{
		if (str.length() == 0)
		{
			return Helpers.EmptyBytes;
		}

		try
		{
			return str.getBytes(UTF8_ENCODING);
		}
		catch (UnsupportedEncodingException ex)
		{
			return new byte[0];
		}
	}

	/**
	 * Converts a string containing hexadecimal characters to a byte array
	 * 
	 * @param hexString
	 *            String containing hexadecimal characters
	 * @param handleDirty
	 *            If true, gracefully handles null, empty and uneven strings as
	 *            well as stripping unconvertable characters
	 * @return The converted byte array
	 * @throws Exception
	 */
	public static byte[] HexStringToBytes(String hexString, boolean handleDirty) throws Exception
	{
		if (handleDirty)
		{
			if (hexString.isEmpty())
			{
				return Helpers.EmptyBytes;
			}

			StringBuilder stripped = new StringBuilder(hexString.length());
			char c;

			// remove all non A-F, 0-9, characters
			for (int i = 0; i < hexString.length(); i++)
			{
				c = hexString.charAt(i);
				if (IsHexDigit(c))
				{
					stripped.append(c);
				}
			}

			hexString = stripped.toString();

			// if odd number of characters, discard last character
			if (hexString.length() % 2 != 0)
			{
				hexString = hexString.substring(0, hexString.length() - 1);
			}
		}

		int byteLength = hexString.length() / 2;
		byte[] bytes = new byte[byteLength];
		int j = 0;

		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = HexToByte(hexString.substring(j, 2));
			j += 2;
		}
		return bytes;
	}

	/**
	 * Returns true if c is a hexadecimal digit (A-F, a-f, 0-9)
	 * 
	 * @param c
	 *            Character to test
	 * @return true if hex digit, false if not
	 */
	private static boolean IsHexDigit(char c)
	{
		return Character.digit(c, 16) >= 0;
	}

	/**
	 * Converts 1 or 2 character string into equivalant byte value
	 * 
	 * @param hex
	 *            1 or 2 character string
	 * @return byte
	 * @throws Exception
	 */
	private static byte HexToByte(String hex) throws Exception
	{
		if (hex.length() > 2 || hex.length() <= 0)
		{
			throw new Exception("hex must be 1 or 2 characters in length");
		}
		return Byte.parseByte(hex, 16);
	}

	/**
	 * Convert a float value to a byte given a minimum and maximum range
	 * 
	 * @param val
	 *            Value to convert to a byte
	 * @param lower
	 *            Minimum value range
	 * @param upper
	 *            Maximum value range
	 * @return A single byte representing the original float value
	 */
	public static byte FloatToByte(float val, float lower, float upper)
	{
		val = Helpers.Clamp(val, lower, upper);
		// Normalize the value
		val -= lower;
		val /= (upper - lower);

		return (byte) Math.floor(val * Byte.MAX_VALUE);
	}

	/**
	 * Convert a byte to a float value given a minimum and maximum range
	 * 
	 * @param bytes
	 *            Byte array to get the byte from
	 * @param pos
	 *            Position in the byte array the desired byte is at
	 * @param lower
	 *            Minimum value range
	 * @param upper
	 *            Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float ByteToFloat(byte[] bytes, int pos, float lower, float upper)
	{
		if (bytes.length <= pos)
		{
			return 0;
		}
		return ByteToFloat(bytes[pos], lower, upper);
	}

	/**
	 * Convert a byte to a float value given a minimum and maximum range
	 * 
	 * @param val
	 *            Byte to convert to a float value
	 * @param lower
	 *            Minimum value range
	 * @param upper
	 *            Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float ByteToFloat(byte val, float lower, float upper)
	{
		final float ONE_OVER_BYTEMAX = 1.0f / Byte.MAX_VALUE;

		float fval = val * ONE_OVER_BYTEMAX;
		float delta = (upper - lower);
		fval *= delta;
		fval += lower;

		// Test for values very close to zero
		float error = delta * ONE_OVER_BYTEMAX;
		if (Math.abs(fval) < error)
		{
			fval = 0.0f;
		}

		return fval;
	}

	public static float UInt16ToFloatL(byte[] bytes, int pos, float lower, float upper)
	{
		int val = BytesToUInt16L(bytes, pos);
		return UInt16ToFloat(val, lower, upper);
	}

	public static float UInt16ToFloat(int val, float lower, float upper)
	{
		final float ONE_OVER_U16_MAX = 1.0f / (2 ^ 16 - 1);

		float fval = val * ONE_OVER_U16_MAX;
		float delta = upper - lower;
		fval *= delta;
		fval += lower;

		// Make sure zeroes come through as zero
		float maxError = delta * ONE_OVER_U16_MAX;
		if (Math.abs(fval) < maxError)
		{
			fval = 0.0f;
		}

		return fval;
	}

	public static int FloatToUInt16(float value, float lower, float upper)
	{
		float delta = upper - lower;
		value -= lower;
		value /= delta;
		value *= (2 ^ 16 - 1);

		return (int) value;
	}

	public static short TEOffsetShort(float offset)
	{
		offset = Helpers.Clamp(offset, -1.0f, 1.0f);
		offset *= 32767.0f;
		return (short) Math.round(offset);
	}

	public static float TEOffsetFloat(byte[] bytes, int pos)
	{
		float offset = BytesToInt16L(bytes, pos);
		return offset / 32767.0f;
	}

	public static short TERotationShort(float rotation)
	{
		final double TWO_PI = Math.PI * 2.0d;
		double remainder = Math.IEEEremainder(rotation, TWO_PI);
		return (short) Math.round((remainder / TWO_PI) * 32767.0d);
	}

	public static float TERotationFloat(byte[] bytes, int pos)
	{
		final float TWO_PI = (float) (Math.PI * 2.0d);
		return ((bytes[pos] | (bytes[pos + 1] << 8)) / 32767.0f) * TWO_PI;
	}

	public static byte TEGlowByte(float glow)
	{
		return (byte) (glow * 255.0f);
	}

	public static float TEGlowFloat(byte[] bytes, int pos)
	{
		return bytes[pos] / 255.0f;
	}

	public static void skipElement(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		int depth = 1;
		do
		{
			int tag = parser.next();
			if (tag == XmlPullParser.START_TAG)
			{
				depth++;
			}
			else if (tag == XmlPullParser.END_TAG)
			{
				depth--;
			}
		} while (depth > 0);
	}

	public static boolean TryParseBoolean(String s)
	{
		if (s != null && !s.isEmpty())
		{
			try
			{
				return Boolean.parseBoolean(s);
			}
			catch (Throwable t)
			{
			}
		}
		return false;
	}

	public static int TryParseInt(String s)
	{
		if (s == null || s.isEmpty())
		{
			return 0;
		}
		try
		{
			return Integer.parseInt(s);
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public static float TryParseFloat(String s)
	{
		if (s == null || s.isEmpty())
		{
			return 0.0f;
		}
		try
		{
			return Float.parseFloat(s);
		}
		catch (Throwable t)
		{
			return 0.0f;
		}
	}

	public static double TryParseDouble(String s)
	{
		if (s == null || s.isEmpty())
		{
			return 0.0d;
		}
		try
		{
			return Double.parseDouble(s);
		}
		catch (Throwable t)
		{
			return 0.0d;
		}
	}

	/**
	 * Tries to parse an unsigned 32-bit integer from a hexadecimal string
	 * 
	 * @param s
	 *            String to parse
	 * @param result
	 *            Resulting integer
	 * @return True if the parse was successful, otherwise false
	 */
	public static long TryParseHex(String s)
	{
		if (s == null || s.isEmpty())
		{
			return 0L;
		}
		try
		{
			return Long.parseLong(s, 16);
		}
		catch (Throwable t)
		{
			return 0L;
		}
	}

	public static long TryParseLong(String s)
	{
		if (s == null || s.isEmpty())
		{
			return 0L;
		}
		try
		{
			return Long.parseLong(s, 10);
		}
		catch (Throwable t)
		{
			return 0L;
		}
	}

	/**
	 * Returns text specified in EnumInfo attribute of the enumerator To add the
	 * text use [EnumInfo(Text = "Some nice text here")] before declaration of
	 * enum values
	 * 
	 * @param value
	 *            Enum value
	 * @return Text representation of the enum
	 */
	public static String EnumToText(Enum<?> value)
	{
		// Get the type
		Class<?> type = value.getClass();
		if (!type.isEnum())
		{
			return "";
		}
		return value.toString();
	}

	/**
	 * <p>
	 * Find the first index of any of a set of potential substrings.
	 * </p>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @param searchStrs
	 *            the Strings to search for, may be null
	 * @return the first index of any of the searchStrs in str, -1 if no match
	 */
	public static int indexOfAny(String str, String[] searchStrs)
	{
		if ((str == null) || (searchStrs == null))
		{
			return -1;
		}
		int sz = searchStrs.length;

		// String's can't have a MAX_VALUEth index.
		int ret = Integer.MAX_VALUE;

		int tmp = 0;
		for (int i = 0; i < sz; i++)
		{
			String search = searchStrs[i];
			if (search == null)
			{
				continue;
			}
			tmp = str.indexOf(search);
			if (tmp == -1)
			{
				continue;
			}

			if (tmp < ret)
			{
				ret = tmp;
			}
		}

		return (ret == Integer.MAX_VALUE) ? -1 : ret;
	}

	/**
	 * <p>
	 * Find the first index of any of a set of potential chars.
	 * </p>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @param searchStrs
	 *            the Strings to search for, may be null
	 * @return the first index of any of the searchStrs in str, -1 if no match
	 */
	public static int indexOfAny(String str, char[] searchChars)
	{
		if ((str == null) || (searchChars == null))
		{
			return -1;
		}
		int sz = searchChars.length;

		// String's can't have a MAX_VALUEth index.
		int ret = Integer.MAX_VALUE;

		int tmp = 0;
		for (int i = 0; i < sz; i++)
		{
			tmp = str.indexOf(searchChars[i]);
			if (tmp == -1)
			{
				continue;
			}

			if (tmp < ret)
			{
				ret = tmp;
			}
		}

		return (ret == Integer.MAX_VALUE) ? -1 : ret;
	}

	/**
	 * <p>
	 * Checks if an array of Objects is empty or <code>null</code>.
	 * </p>
	 * 
	 * @param array
	 *            the array to test
	 * @return <code>true</code> if the array is empty or <code>null</code>
	 */
	public static boolean isEmpty(char[] array)
	{
		if (array == null || array.length == 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Checks if a String is empty ("") or null.
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}

	/**
	 * Get current OS
	 * 
	 * @return Either "Win" or "Linux"
	 */
	public static String GetPlatform()
	{
		return System.getProperty("os.name");
	}

	/**
	 * Get clients default Mac Address
	 * 
	 * @return A string containing the first found Mac Address
	 * @throws SocketException
	 */
	public static String GetMAC()
	{
		try
		{
			StringBuilder sb = new StringBuilder("");

			/*
			 * Extract each array of mac address and convert it to hexa with the
			 * following format 08:00:27:DC:4A:9E.
			 */
			for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();)
			{
				NetworkInterface ni = nis.nextElement();
				if (ni != null)
				{
					byte[] mac = ni.getHardwareAddress();
					if (mac != null && mac.length >= 6)
					{
						for (int i = 0; i < mac.length; i++)
						{
							sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
						}
						break;
					}
				}
			}
			return sb.toString();
		}
		catch (SocketException ex)
		{
			return EmptyString;
		}
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also JARs.
	 * 
	 * @author Greg Briggs
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param regex A regex which the resource names must match or null to return all file names 
	 * @param excludeDirs If true only file entries will be returned	
	 * @param path The relative directory path to enumerate. Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static String[] getResourceListing(Class<?> clazz, final String regex, final boolean excludeDirs, String path) throws URISyntaxException, IOException
	{
		// File filter checking the file names to be part of the hostname
		FileFilter filter = new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				if (excludeDirs && file.isDirectory())
					return false;
				return regex == null || file.getName().matches(regex);
			}
		};

		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file"))
		{
			/* A file path: easy enough */
			if (excludeDirs && regex == null)
				return new File(dirURL.toURI()).list();
			
			File[] files = new File(dirURL.toURI()).listFiles(filter);
			String[] names = new String[files.length];
			for (int i = 0; i < files.length; i++)
			{
				names[i] = files[i].getName();
			}
			return names;
		}

		if (dirURL == null)
		{
			/*
			 * In case of a jar file, we can't actually find a directory. Have
			 * to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar"))
		{
			/* A JAR path, strip out only the JAR file */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); 
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
			Set<String> result = new HashSet<String>();    // avoid duplicates in case it is a subdirectory
			while (entries.hasMoreElements())
			{
				String name = entries.nextElement().getName();
				if (name.startsWith(path))
				{ // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0)
					{
						if (excludeDirs)
							continue;
						
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					if (regex == null || entry.matches(regex))
						result.add(entry);
				}
			}
			return result.toArray(new String[result.size()]);
		}
		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	/**
	 * Retrieves the default keystore
	 * 
	 * @return The current KeyStore
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyStore GetExtendedKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException
	{
		KeyStore ks = null;

		File file = new File("jssecacerts");
		if (file.isFile() == false)
		{
			char SEP = File.separatorChar;
			File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
			file = new File(dir, "jssecacerts");
			if (file.isFile() == false)
			{
				file = new File(dir, "cacerts");
			}
		}

		ks = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in = new FileInputStream(file);
		try
		{
			ks.load(in, null);
		}
		catch (IOException ex)
		{
			throw ex;
		}
		catch (NoSuchAlgorithmException ex)
		{
			throw ex;
		}
		catch (CertificateException ex)
		{
			throw ex;
		}
		finally
		{
			in.close();
		}
		return ks;
	}

	public static X509Certificate GetCertificate(final String hostname) throws CertificateException, IOException,
			URISyntaxException
	{
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = null;
		String[] names = getResourceListing(Helpers.class, ".+\\.cert", true, "res/");

		for (String name : names)
		{
			if (hostname.contains(name.substring(0, name.length() - 5)))
			{
				InputStream is = Helpers.class.getClassLoader().getResourceAsStream("res/" + name);
				BufferedInputStream bis = new BufferedInputStream(is);
				try
				{
					cert = (X509Certificate) cf.generateCertificate(bis);
				}
				catch (CertificateException ex)
				{
					throw ex;
				}
				finally
				{
					bis.close();
					is.close();
				}
				break;
			}
		}
		return cert;
	}
}
