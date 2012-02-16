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
package libomv.types;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

// A 128-bit Universally Unique Identifier, used throughout SL and OpenSim
public class UUID implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Get a byte array of the 16 raw bytes making up the UUID
	public byte[] getData()
	{
		return data;
	}

	private byte[] data;

	private static byte[] makeNewGuid()
	{
		Random rand = new Random();
		byte[] guid = new byte[16];
		rand.nextBytes(guid);
		return guid;
	}

	/**
	 * Constructor that creates a new random UUID representation
	 */
	public UUID()
	{
		data = makeNewGuid();
	}

	/**
	 * Constructor that takes a string UUID representation
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            <example>UUID("11f8aa9c-b071-4242-836b-13b7abe0d489"
	 *            )</example>
	 */
	public UUID(String string)
	{
		FromString(string);
	}

	/**
	 * Constructor that takes a ByteBuffer containing a UUID
	 * 
	 * @param source
	 *            ByteBuffer containing a 16 byte UUID
	 */
	public UUID(ByteBuffer byteArray)
	{
		data = new byte[16];
		byteArray.get(data);
	}

	/**
	 * Constructor that takes a byte array containing a UUID
	 * 
	 * @param source
	 *            Byte array containing a 16 byte UUID
	 * @param pos
	 *            Beginning offset in the array
	 */
	public UUID(byte[] byteArray)
	{
		this(byteArray, 0);
	}

	public UUID(byte[] byteArray, int pos)
	{
		data = new byte[16];
		System.arraycopy(byteArray, pos, data, 0, 16);
	}

	/**
	 * Constructor that takes an unsigned 64-bit unsigned integer to convert to
	 * a UUID
	 * 
	 * @param val
	 *            64-bit unsigned integer to convert to a UUID
	 */
	public UUID(long value)
	{
		this(value, false);
	}

	public UUID(long value, boolean le)
	{
		data = new byte[16];
		if (le)
		{
			Helpers.UInt64ToBytesL(value, data, 8);
		}
		else
		{
			Helpers.UInt64ToBytesB(value, data, 8);
		}
	}

	public UUID(boolean randomize)
	{
		if (randomize)
		{
			data = makeNewGuid();
		}
		else
		{
			data = new byte[16];
		}
	}

	public UUID(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, null);
		if (!parser.isEmptyElementTag())
		{
			String name = parser.getName();
			if (name.equals("Guid") || name.equals("UUID"))
			{
				FromString(parser.nextText());
			}
		}
		parser.nextTag();
	}

	/**
	 * Copy constructor
	 * 
	 * @param val
	 *            UUID to copy
	 */
	public UUID(UUID val)
	{
		data = new byte[16];
		System.arraycopy(data, 0, val.data, 0, 16);
	}

	/**
	 * Assigns this UUID from 16 bytes out of a byte array
	 * 
	 * @param source
	 *            Byte array containing the UUID to assign this UUID to
	 * @param pos
	 *            Starting position of the UUID in the byte array
	 * @return true when successful, false otherwise
	 */
	public boolean FromBytes(byte[] source, int pos)
	{
		if (source.length >= pos + 16)
		{
			System.arraycopy(source, pos, data, 0, 16);
			return true;
		}
		return false;
	}

	/**
	 * Parses a string UUID representation and assigns its value to the object
	 * <example
	 * >uuid.FromString("11f8aa9c-b071-4242-836b-13b7abe0d489")</example>
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @return true when successful, false otherwise
	 */
	public boolean FromString(String string)
	{
		if (string.length() >= 38 && string.charAt(0) == '{' && string.charAt(37) == '}')
		{
			string = string.substring(1, 37);
		}
		else if (string.length() > 36)
		{
			string = string.substring(0, 36);
		}

		if (string.length() == 36)
		{
			string = string.substring(0, 36).replaceAll("-", "");
		}

		if (string.length() != 32)
		{
			string = string.substring(0, 32);
		}

		// Always create new data array to prevent overwriting byref data
		data = new byte[16];
		for (int i = 0; i < 16; ++i)
		{
			data[i] = (byte) Integer.parseInt(string.substring(i * 2, (i * 2) + 2), 16);
		}
		return true;
	}

	/**
	 * Returns a copy of the raw bytes for this UUID
	 * 
	 * @return A 16 byte array containing this UUID
	 */
	public byte[] GetBytes()
	{
		return data;
	}

	/**
	 * Copies the raw bytes for this UUID into a ByteBuffer
	 * 
	 * @param bytes
	 *            The ByteBuffer in which the 16 byte of this UUID are copied
	 */
	public void GetBytes(ByteBuffer bytes)
	{
		bytes.put(data);
	}

	/**
	 * Writes the raw bytes for this UUID to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writeing. Must be
	 *            at least 16 bytes before the end of the array
	 */
	public int ToBytes(byte[] dest, int pos)
	{
		System.arraycopy(data, 0, dest, pos, data.length);
		return data.length;
	}

	public long AsLong()
	{
		return AsLong(false);
	}

	public long AsLong(boolean le)
	{
		if (le)
			return Helpers.BytesToUInt64L(data);

		return Helpers.BytesToUInt64B(data);
	}

	/**
	 * Calculate an LLCRC (cyclic redundancy check) for this LLUUID
	 * 
	 * @returns The CRC checksum for this UUID
	 */
	public long CRC()
	{
		long retval = 0;

		retval += ((data[3] << 24) + (data[2] << 16) + (data[1] << 8) + data[0]);
		retval += ((data[7] << 24) + (data[6] << 16) + (data[5] << 8) + data[4]);
		retval += ((data[11] << 24) + (data[10] << 16) + (data[9] << 8) + data[8]);
		retval += ((data[15] << 24) + (data[14] << 16) + (data[13] << 8) + data[12]);

		return retval;
	}

	public static UUID GenerateUUID()
	{
		return new UUID(makeNewGuid());
	}
	
	static public UUID parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new UUID(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
	    writer.startTag(null, "UUID").text(toString()).endTag(null, "UUID");
	}

	/**
	 * Return a hash code for this UUID, used by .NET for hash tables
	 * 
	 * @return An integer composed of all the UUID bytes XORed together
	 */
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	/**
	 * Comparison function
	 * 
	 * @param o
	 *            An object to compare to this UUID
	 * @return True if the object is a UUID and both UUIDs are equal
	 */
	@Override
	public boolean equals(Object o)
	{
		if ((o.getClass() != this.getClass()))
		{
			return false;
		}
		UUID uuid = (UUID) o;
		for (int i = 0; i < 16; ++i)
		{
			if (data[i] != uuid.data[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Comparison function
	 * 
	 * @param uuid
	 *            UUID to compare to
	 * @return True if the UUIDs are equal, otherwise false
	 */
	public boolean equals(UUID uuid)
	{
		for (int i = 0; i < 16; ++i)
		{
			if (data[i] != uuid.data[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            <example>UUID.Parse("11f8aa9c-b071-4242-836b-13b7abe0d489"
	 *            )</example>
	 */
	public static UUID Parse(String val)
	{
		return new UUID(val);
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @param result
	 *            Will contain the parsed UUID if successful, otherwise null
	 * @return True if the string was successfully parse, otherwise false
	 *         <example>UUID.TryParse("11f8aa9c-b071-4242-836b-13b7abe0d489",
	 *         result)</example>
	 */
	public static boolean TryParse(String val, RefObject<UUID> result)
	{
		if (val == null || val.length() == 0 || (val.charAt(0) == '{' && val.length() != 38)
				|| (val.length() != 36 && val.length() != 32))
		{
			result.argvalue = UUID.Zero;
			return false;
		}

		try
		{
			result.argvalue = Parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = UUID.Zero;
			return false;
		}
	}

	/**
	 * Combine two UUIDs together by taking the MD5 hash of a byte array
	 * containing both UUIDs
	 * 
	 * @param first
	 *            First UUID to combine
	 * @param second
	 *            Second UUID to combine
	 * @return The UUID product of the combination
	 */
	public static UUID Combine(UUID first, UUID second)
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}

		// Construct the buffer that MD5ed
		byte[] input = new byte[32];
		first.ToBytes(input, 0);
		second.ToBytes(input, 16);
		return new UUID(md.digest(input));
	}

	/**
	 * XOR two UUIDs together
	 * 
	 * @param uuid
	 *            UUID to combine
	 */
	public void XOr(UUID uuid)
	{
		int i = 0;
		for (byte b : uuid.GetBytes())
		{
			data[i++] ^= b;
		}
	}

	public static UUID XOr(UUID first, UUID second)
	{
		byte[] res = new byte[16];
		byte[] sec = second.GetBytes();
		int i = 0;
		for (byte b : first.GetBytes())
		{
			res[i] = (byte) (b ^ sec[i]);
			i++;
		}
		return new UUID(res);
	}

	/**
	 * Get a hyphenated string representation of this UUID
	 * 
	 * @return A string representation of this UUID, lowercase and with hyphens
	 *         <example>11f8aa9c-b071-4242-836b-13b7abe0d489</example>
	 */
	@Override
	public String toString()
	{
		if (data == null)
		{
			return ZeroString;
		}

		StringBuffer uuid = new StringBuffer(36);

		for (int i = 0; i < 16; ++i)
		{
			byte value = data[i];
			uuid.append(String.format("%02x", value & 0xFF));
			if (i == 3 || i == 5 || i == 7 || i == 9)
			{
				uuid.append("-");
			}
		}
		return uuid.toString();
	}

	/** An UUID with a value of all zeroes */
	public final static UUID Zero = new UUID(false);

	/** A cache of UUID.Zero as a string to optimize a common path */
	private static final String ZeroString = Zero.toString();
}
