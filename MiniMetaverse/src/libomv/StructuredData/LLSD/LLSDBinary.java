/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.StructuredData.LLSD;

//
// *
// * This implementation is based upon the description at
// *
// * http://wiki.secondlife.com/wiki/LLSD
// *
// * and (partially) tested against the (supposed) reference implementation at
// *
// * http://svn.secondlife.com/svn/linden/release/indra/lib/python/indra/base/osd.py
// *
//

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map.Entry;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;

public final class LLSDBinary
{
	private static final int int32Length = 4;
	private static final int doubleLength = 8;

	private static final byte[] llsdBinaryHead = { '<', '?', 'l', 'l', 's', 'd', '/', 'b', 'i', 'n', 'a', 'r', 'y',
			'?', '>', '\n' };
	private static final byte undefBinaryValue = (byte) '!';
	private static final byte trueBinaryValue = (byte) '1';
	private static final byte falseBinaryValue = (byte) '0';
	private static final byte integerBinaryMarker = (byte) 'i';
	private static final byte realBinaryMarker = (byte) 'r';
	private static final byte uuidBinaryMarker = (byte) 'u';
	private static final byte binaryBinaryMarker = (byte) 'b';
	private static final byte stringBinaryMarker = (byte) 's';
	private static final byte uriBinaryMarker = (byte) 'l';
	private static final byte dateBinaryMarker = (byte) 'd';
	private static final byte arrayBeginBinaryMarker = (byte) '[';
	private static final byte arrayEndBinaryMarker = (byte) ']';
	private static final byte mapBeginBinaryMarker = (byte) '{';
	private static final byte mapEndBinaryMarker = (byte) '}';
	private static final byte keyBinaryMarker = (byte) 'k';

	/**
	 * Creates an OSD (object structured data) object from a binary data stream
	 * 
	 * @param stream
	 *            The byte stream to read from
	 * @return and OSD object
	 * @throws IOException
	 *             , OSDException
	 */
	public static OSD parse(InputStream instr) throws IOException, ParseException
	{
		LLSDInputStream stream = new LLSDInputStream(instr);
		return parse(stream);
	}

	public static OSD parse(LLSDInputStream stream) throws IOException, ParseException
	{
		skipWhiteSpace(stream);
		boolean result = find(stream, llsdBinaryHead);
		if (!result)
		{
			throw new ParseException("Failed to decode binary LLSD", 0);
		}
		return parseElement(stream);
	}

	public static void serialize(OutputStream stream, OSD osd) throws IOException
	{
		stream.write(llsdBinaryHead);
		serializeLLSDBinaryElement(stream, osd);
	}

	private static void serializeLLSDBinaryElement(OutputStream stream, OSD osd) throws IOException
	{
		byte[] rawBinary;

		switch (osd.getType())
		{
			case Unknown:
				stream.write(undefBinaryValue);
				break;
			case Boolean:
				stream.write(osd.AsBinary(), 0, 1);
				break;
			case Integer:
				stream.write(integerBinaryMarker);
				stream.write(osd.AsBinary(), 0, int32Length);
				break;
			case Real:
				stream.write(realBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case UUID:
				stream.write(uuidBinaryMarker);
				stream.write(osd.AsBinary(), 0, 16);
				break;
			case String:
				rawBinary = osd.AsBinary();
				stream.write(stringBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Binary:
				rawBinary = osd.AsBinary();
				stream.write(binaryBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Date:
				stream.write(dateBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case URI:
				rawBinary = osd.AsBinary();
				stream.write(uriBinaryMarker);
				stream.write(Helpers.Int32ToBytesB(rawBinary.length));
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case Array:
				serializeLLSDBinaryArray(stream, (OSDArray) osd);
				break;
			case Map:
				serializeLLSDBinaryMap(stream, (OSDMap) osd);
				break;
			default:
				throw new IOException("Binary serialization: Not existing element discovered.");
		}
	}

	private static void serializeLLSDBinaryArray(OutputStream stream, OSDArray osdArray) throws IOException
	{
		stream.write(arrayBeginBinaryMarker);
		stream.write(Helpers.Int32ToBytesB(osdArray.size()));

		for (OSD osd : osdArray)
		{
			serializeLLSDBinaryElement(stream, osd);
		}
		stream.write(arrayEndBinaryMarker);
	}

	private static void serializeLLSDBinaryMap(OutputStream stream, OSDMap osdMap) throws IOException
	{
		stream.write(mapBeginBinaryMarker);
		stream.write(Helpers.Int32ToBytesB(osdMap.size()));

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			stream.write(keyBinaryMarker);
			stream.write(Helpers.Int32ToBytesB(kvp.getKey().length()));
			stream.write(kvp.getKey().getBytes(Helpers.UTF8_ENCODING), 0, kvp.getKey().length());
			serializeLLSDBinaryElement(stream, kvp.getValue());
		}
		stream.write(mapEndBinaryMarker);
	}

	private static OSD parseElement(LLSDInputStream stream) throws IOException, ParseException
	{
		skipWhiteSpace(stream);
		OSD osd;

		int marker = stream.read();
		if (marker < 0)
		{
			throw new ParseException("Binary LLSD parsing: Unexpected end of stream.", 1);
		}

		switch ((byte) marker)
		{
			case undefBinaryValue:
				osd = new OSD();
				break;
			case trueBinaryValue:
				osd = OSD.FromBoolean(true);
				break;
			case falseBinaryValue:
				osd = OSD.FromBoolean(false);
				break;
			case integerBinaryMarker:
				int integer = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromInteger(integer);
				break;
			case realBinaryMarker:
				double dbl = Helpers.BytesToDoubleB(consumeBytes(stream, doubleLength), 0);
				osd = OSD.FromReal(dbl);
				break;
			case uuidBinaryMarker:
				osd = OSD.FromUUID(new UUID(consumeBytes(stream, 16)));
				break;
			case binaryBinaryMarker:
				int binaryLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromBinary(consumeBytes(stream, binaryLength));
				break;
			case stringBinaryMarker:
				int stringLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				osd = OSD.FromString(new String(consumeBytes(stream, stringLength), Helpers.UTF8_ENCODING));
				break;
			case uriBinaryMarker:
				int uriLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
				URI uri;
				try
				{
					uri = new URI(new String(consumeBytes(stream, uriLength), Helpers.UTF8_ENCODING));
				}
				catch (URISyntaxException ex)
				{
					throw new ParseException("Binary LLSD parsing: Invalid Uri format detected: " + ex.getMessage(),
							stream.getBytePosition());
				}
				osd = OSD.FromUri(uri);
				break;
			case dateBinaryMarker:
				double timestamp = Helpers.BytesToDoubleB(consumeBytes(stream, doubleLength), 0);
				osd = OSD.FromDate(Helpers.UnixTimeToDateTime(timestamp));
				break;
			case arrayBeginBinaryMarker:
				osd = parseArray(stream);
				break;
			case mapBeginBinaryMarker:
				osd = parseMap(stream);
				break;
			default:
				throw new ParseException("Binary LLSD parsing: Unknown type marker.", stream.getBytePosition());
		}
		return osd;
	}

	private static OSD parseArray(LLSDInputStream stream) throws IOException, ParseException
	{
		int numElements = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
		int crrElement = 0;
		OSDArray osdArray = new OSDArray();
		while (crrElement < numElements)
		{
			osdArray.add(parseElement(stream));
			crrElement++;
		}
		if (!find(stream, arrayEndBinaryMarker))
		{
			throw new ParseException("Binary LLSD parsing: Missing end marker in array.", stream.getBytePosition());
		}

		return osdArray;
	}

	private static OSD parseMap(LLSDInputStream stream) throws IOException, ParseException
	{
		int numElements = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
		int crrElement = 0;
		OSDMap osdMap = new OSDMap();
		while (crrElement < numElements)
		{
			if (!find(stream, keyBinaryMarker))
			{
				throw new ParseException("Binary LLSD parsing: Missing key marker in map.", stream.getBytePosition());
			}
			int keyLength = Helpers.BytesToInt32B(consumeBytes(stream, int32Length));
			String key = new String(consumeBytes(stream, keyLength));
			osdMap.put(key, parseElement(stream));
			crrElement++;
		}
		if (!find(stream, mapEndBinaryMarker))
		{
			throw new ParseException("Binary LLSD parsing: Missing end marker in map.", stream.getBytePosition());
		}
		return osdMap;
	}

	/**
	 * @param stream
	 * @throws IOException
	 */
	private static void skipWhiteSpace(LLSDInputStream stream) throws IOException
	{
		int bt;
		while (((bt = stream.read()) > 0)
				&& ((byte) bt == ' ' || (byte) bt == '\t' || (byte) bt == '\n' || (byte) bt == '\r'))
			;
		stream.unread(bt);
	}

	private static boolean find(LLSDInputStream stream, byte toFind) throws IOException
	{
		int bt = stream.read();
		if (bt < 0)
		{
			return false;
		}
		if ((byte) bt == toFind)
		{
			return true;
		}
		stream.unread(bt);
		return false;
	}

	public static boolean find(LLSDInputStream stream, byte[] toFind) throws IOException
	{
		if (toFind.length == 0)
		{
			return false;
		}

		boolean found = true;
		int crrIndex = 0;
		int bt = 0;

		while (found && ((bt = stream.read()) > 0) && (crrIndex < toFind.length))
		{
			if (toFind[crrIndex] == (byte) bt)
			{
				found = true;
				crrIndex++;
			}
			else
			{
				found = false;
			}
		}

		if (found && crrIndex >= toFind.length)
		{
			stream.unread(bt);
			return true;
		}
		stream.unread(toFind, 0, crrIndex);
		return false;
	}

	private static byte[] consumeBytes(LLSDInputStream stream, int consumeBytes) throws IOException, ParseException
	{
		byte[] bytes = new byte[consumeBytes];
		if (stream.read(bytes, 0, consumeBytes) < consumeBytes)
		{
			throw new ParseException("Binary LLSD parsing: Unexpected end of stream.", stream.getBytePosition());
		}
		return bytes;
	}
}