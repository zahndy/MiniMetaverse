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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.ParseException;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDString;
import libomv.types.UUID;
import libomv.utils.Helpers;

public final class LLSDNotation
{
	private static final String baseIndent = "  ";

	private static final char[] newLine = { '\n' };

	private static final char undefNotationValue = '!';

	private static final char trueNotationValueOne = '1';
	private static final char trueNotationValueTwo = 't';
	private static final char[] trueNotationValueTwoFull = { 't', 'r', 'u', 'e' };
	private static final char trueNotationValueThree = 'T';
	private static final char[] trueNotationValueThreeFull = { 'T', 'R', 'U', 'E' };

	private static final char falseNotationValueOne = '0';
	private static final char falseNotationValueTwo = 'f';
	private static final char[] falseNotationValueTwoFull = { 'f', 'a', 'l', 's', 'e' };
	private static final char falseNotationValueThree = 'F';
	private static final char[] falseNotationValueThreeFull = { 'F', 'A', 'L', 'S', 'E' };

	private static final char integerNotationMarker = 'i';
	private static final char realNotationMarker = 'r';
	private static final char uuidNotationMarker = 'u';
	private static final char binaryNotationMarker = 'b';
	private static final char stringNotationMarker = 's';
	private static final char uriNotationMarker = 'l';
	private static final char dateNotationMarker = 'd';

	private static final char arrayBeginNotationMarker = '[';
	private static final char arrayEndNotationMarker = ']';

	private static final char mapBeginNotationMarker = '{';
	private static final char mapEndNotationMarker = '}';
	private static final char kommaNotationDelimiter = ',';
	private static final char keyNotationDelimiter = ':';

	private static final char sizeBeginNotationMarker = '(';
	private static final char sizeEndNotationMarker = ')';
	private static final char doubleQuotesNotationMarker = '"';
	private static final char singleQuotesNotationMarker = '\'';

	public static OSD parse(String string) throws ParseException, IOException
	{
		return parseElement(new LLSDReader(new StringReader(string)));
	}

	public static OSD parse(Reader reader) throws ParseException, IOException
	{
		return parseElement(new LLSDReader(reader));
	}

	public static void serialize(Writer writer, OSD osd) throws IOException
	{
		serializeElement(writer, osd);
	}

	public static String serializeToString(OSD osd) throws IOException
	{
		Writer writer = new StringWriter();
		serializeElement(writer, osd);
		return writer.toString();
	}

	public static void serializeFormatted(Writer writer, OSD osd) throws IOException
	{
		serializeElementFormatted(writer, "", osd);
	}

	/**
	 * Read the next LLSD data element in and return the OSD structure for it
	 * 
	 * @param reader
	 *            a pushback reader to read in data from
	 * @return the OSD data corresponding to the LLSD data element
	 * @throws IOException
	 */
	private static OSD parseElement(LLSDReader reader) throws ParseException, IOException
	{
		int character = skipWhitespace(reader);
		if (character <= 0)
		{
			return new OSD(); // server returned an empty file, so we're going
								// to pass along a null LLSD object
		}

		OSD osd;
		int matching;
		switch ((char) character)
		{
			case undefNotationValue:
				osd = new OSD();
				break;
			case trueNotationValueOne:
				osd = OSD.FromBoolean(true);
				break;
			case trueNotationValueTwo:
				matching = BufferCharactersEqual(reader, trueNotationValueTwoFull, 1);
				if (matching > 1 && matching < trueNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				osd = OSD.FromBoolean(true);
				break;
			case trueNotationValueThree:
				matching = BufferCharactersEqual(reader, trueNotationValueThreeFull, 1);
				if (matching > 1 && matching < trueNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				osd = OSD.FromBoolean(true);
				break;
			case falseNotationValueOne:
				osd = OSD.FromBoolean(false);
				break;
			case falseNotationValueTwo:
				matching = BufferCharactersEqual(reader, falseNotationValueTwoFull, 1);
				if (matching > 1 && matching < falseNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				osd = OSD.FromBoolean(false);
				break;
			case falseNotationValueThree:
				matching = BufferCharactersEqual(reader, falseNotationValueThreeFull, 1);
				if (matching > 1 && matching < falseNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				osd = OSD.FromBoolean(false);
				break;
			case integerNotationMarker:
				osd = parseInteger(reader);
				break;
			case realNotationMarker:
				osd = parseReal(reader);
				break;
			case uuidNotationMarker:
				char[] uuidBuf = new char[36];
				if (reader.read(uuidBuf, 0, 36) < 36)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in UUID.",
							reader.getBytePosition());
				}
				osd = OSD.FromUUID(new UUID(uuidBuf.toString()));
				break;
			case binaryNotationMarker:
				byte[] bytes = Helpers.EmptyBytes;
				int bChar = reader.read();
				if (bChar < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
							reader.getBytePosition());
				}
				else if (bChar == sizeBeginNotationMarker)
				{
					throw new ParseException("Notation LLSD parsing: Raw binary encoding not supported.",
							reader.getBytePosition());
				}
				else if (Character.isDigit((char) bChar))
				{
					char[] charsBaseEncoding = new char[2];
					if (reader.read(charsBaseEncoding, 0, 2) < 2)
					{
						throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
								reader.getBytePosition());
					}
					int baseEncoding = new Integer(new String(charsBaseEncoding));
					if (baseEncoding == 64)
					{
						if (reader.read() < 0)
						{
							throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
									reader.getBytePosition());
						}
						String bytes64 = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
						bytes = Base64.decodeBase64(bytes64);
					}
					else
					{
						throw new ParseException("Notation LLSD parsing: Encoding base" + baseEncoding
								+ " + not supported.", reader.getBytePosition());
					}
				}
				osd = OSD.FromBinary(bytes);
				break;
			case stringNotationMarker:
				int numChars = GetLengthInBrackets(reader);
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				char[] chars = new char[numChars];
				if (reader.read(chars, 0, numChars) < numChars)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				osd = OSD.FromString(new String(chars));
				break;
			case singleQuotesNotationMarker:
				String sOne = GetStringDelimitedBy(reader, singleQuotesNotationMarker);
				osd = OSD.FromString(sOne);
				break;
			case doubleQuotesNotationMarker:
				String sTwo = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
				osd = OSD.FromString(sTwo);
				break;
			case uriNotationMarker:
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				URI uri;
				try
				{
					uri = new URI(GetStringDelimitedBy(reader, doubleQuotesNotationMarker));
				}
				catch (Throwable t)
				{
					throw new ParseException("Notation LLSD parsing: Invalid Uri format detected.",
							reader.getBytePosition());
				}
				osd = OSD.FromUri(uri);
				break;
			case dateNotationMarker:
				if (reader.read() < 0)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in date.",
							reader.getBytePosition());
				}
				String date = GetStringDelimitedBy(reader, doubleQuotesNotationMarker);
				osd = OSD.FromDate(new OSDString(date).AsDate());
				break;
			case arrayBeginNotationMarker:
				osd = parseArray(reader);
				break;
			case mapBeginNotationMarker:
				osd = parseMap(reader);
				break;
			default:
				throw new ParseException("Notation LLSD parsing: Unknown type marker '" + (char) character + "'.",
						reader.getBytePosition());
		}
		return osd;
	}

	private static OSD parseInteger(LLSDReader reader) throws IOException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = reader.read()) > 0) && ((char) character == '-'))
		{
			s.append((char) character);
		}
		while (character >= 0 && Character.isDigit((char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (character >= 0)
		{
			reader.unread(character);
		}
		return OSD.FromInteger(new Integer(s.toString()));
	}

	private static OSD parseReal(LLSDReader reader) throws IOException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = reader.read()) > 0) && ((char) character == '-' && (char) character == '+'))
		{
			s.append((char) character);
		}
		while ((character >= 0)
				&& (Character.isDigit((char) character) || (char) character == '.' || (char) character == 'e'
						|| (char) character == 'E' || (char) character == '+' || (char) character == '-'))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (character >= 0)
		{
			reader.unread(character);
		}
		return OSD.FromReal(new Double(s.toString()));
	}

	private static OSD parseArray(LLSDReader reader) throws IOException, ParseException
	{
		int character;
		OSDArray osdArray = new OSDArray();
		while (((character = skipWhitespace(reader)) > 0) && ((char) character != arrayEndNotationMarker))
		{
			reader.unread(character);
			osdArray.add(parseElement(reader));

			character = skipWhitespace(reader);
			if (character < 0)
			{
				throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
						reader.getBytePosition());
			}
			else if ((char) character == arrayEndNotationMarker)
			{
				break;
			}
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
					reader.getBytePosition());
		}
		return osdArray;
	}

	private static OSD parseMap(LLSDReader reader) throws ParseException, IOException
	{
		int character;
		OSDMap osdMap = new OSDMap();
		while (((character = skipWhitespace(reader)) > 0) && ((char) character != mapEndNotationMarker))
		{
			reader.unread(character);
			OSD osdKey = parseElement(reader);
			if (osdKey.getType() != OSD.OSDType.String)
			{
				throw new ParseException("Notation LLSD parsing: Invalid key in map", reader.getBytePosition());
			}
			String key = osdKey.AsString();

			character = skipWhitespace(reader);
			if ((char) character != keyNotationDelimiter)
			{
				throw new ParseException("Notation LLSD parsing: Unexpected end of stream in map.",
						reader.getBytePosition());
			}
			if ((char) character != keyNotationDelimiter)
			{
				throw new ParseException("Notation LLSD parsing: Invalid delimiter in map.", reader.getBytePosition());
			}

			osdMap.put(key, parseElement(reader));
			character = skipWhitespace(reader);
			if (character < 0)
			{
				throw new ParseException("Notation LLSD parsing: Unexpected end of map discovered.",
						reader.getBytePosition());
			}
			else if ((char) character == mapEndNotationMarker)
			{
				break;
			}
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of map discovered.",
					reader.getBytePosition());
		}
		return osdMap;
	}

	private static void serializeElement(Writer writer, OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValueTwo);
				}
				else
				{
					writer.write(falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(singleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), singleQuotesNotationMarker));
				writer.write(singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(binaryNotationMarker);
				writer.write("64");
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(dateNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(uriNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), doubleQuotesNotationMarker));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Array:
				serializeArray(writer, (OSDArray) osd);
				break;
			case Map:
				serializeMap(writer, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");
		}
	}

	private static void serializeArray(Writer writer, OSDArray osdArray) throws IOException
	{
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			serializeElement(writer, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMap(Writer writer, OSDMap osdMap) throws IOException
	{
		writer.write(mapBeginNotationMarker);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(singleQuotesNotationMarker);
			writer.write(escapeCharacter(kvp.getKey(), singleQuotesNotationMarker));
			writer.write(singleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElement(writer, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
			idx++;
		}
		writer.write(mapEndNotationMarker);
	}

	private static void serializeElementFormatted(Writer writer, String indent, OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValueTwo);
				}
				else
				{
					writer.write(falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(singleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), singleQuotesNotationMarker));
				writer.write(singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(binaryNotationMarker);
				writer.write("64");
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(dateNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(uriNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), doubleQuotesNotationMarker));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Array:
				serializeArrayFormatted(writer, indent + baseIndent, (OSDArray) osd);
				break;
			case Map:
				serializeMapFormatted(writer, indent + baseIndent, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");

		}
	}

	private static void serializeArrayFormatted(Writer writer, String intend, OSDArray osdArray) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			OSD.OSDType type = osdArray.get(idx).getType();
			if (type != OSD.OSDType.Array && type != OSD.OSDType.Map)
			{
				writer.write(newLine);
			}
			writer.write(baseIndent + intend);
			serializeElementFormatted(writer, intend, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMapFormatted(Writer writer, String intend, OSDMap osdMap) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(mapBeginNotationMarker);
		writer.write(newLine);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(baseIndent + intend);
			writer.write(singleQuotesNotationMarker);
			writer.write(escapeCharacter(kvp.getKey(), singleQuotesNotationMarker));
			writer.write(singleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElementFormatted(writer, intend, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(newLine);
				writer.write(baseIndent + intend);
				writer.write(kommaNotationDelimiter);
				writer.write(newLine);
			}

			idx++;
		}
		writer.write(newLine);
		writer.write(intend);
		writer.write(mapEndNotationMarker);
	}

	public static int skipWhitespace(LLSDReader reader) throws IOException
	{
		int character;
		while ((character = reader.read()) >= 0)
		{
			char c = (char) character;
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r')
			{
				break;
			}
		}
		return character;
	}

	public static int GetLengthInBrackets(LLSDReader reader) throws IOException, ParseException
	{
		int character;
		StringBuilder s = new StringBuilder();
		if (((character = skipWhitespace(reader)) > 0) && ((char) character == sizeBeginNotationMarker))
			;
		while ((character >= 0) && Character.isDigit((char) character) && ((char) character != sizeEndNotationMarker))
		{
			s.append((char) character);
			reader.read();
		}
		if (character < 0)
		{
			throw new ParseException("Notation LLSD parsing: Can't parse length value cause unexpected end of stream.",
					reader.getBytePosition());
		}
		reader.unread(character);
		return new Integer(s.toString());
	}

	public static String GetStringDelimitedBy(LLSDReader reader, char delimiter) throws IOException, ParseException
	{
		int character;
		boolean foundEscape = false;
		StringBuilder s = new StringBuilder();
		while (((character = reader.read()) >= 0)
				&& (((char) character != delimiter) || ((char) character == delimiter && foundEscape)))
		{
			if (foundEscape)
			{
				foundEscape = false;
				switch ((char) character)
				{
					case 'a':
						s.append('\005');
						break;
					case 'b':
						s.append('\b');
						break;
					case 'f':
						s.append('\f');
						break;
					case 'n':
						s.append('\n');
						break;
					case 'r':
						s.append('\r');
						break;
					case 't':
						s.append('\t');
						break;
					case 'v':
						s.append('\013');
						break;
					default:
						s.append((char) character);
						break;
				}
			}
			else if ((char) character == '\\')
			{
				foundEscape = true;
			}
			else
			{
				s.append((char) character);
			}
		}
		if (character < 0)
		{
			throw new ParseException(
					"Notation LLSD parsing: Can't parse text because unexpected end of stream while expecting a '"
							+ delimiter + "' character.", reader.getBytePosition());
		}
		return s.toString();
	}

	public static int BufferCharactersEqual(LLSDReader reader, char[] buffer, int offset) throws IOException
	{

		boolean charactersEqual = true;
		int character;

		while ((character = reader.read()) >= 0 && offset < buffer.length && charactersEqual)
		{
			if (((char) character) != buffer[offset])
			{
				charactersEqual = false;
				reader.unread(character);
				break;
			}
			offset++;
		}
		return offset;
	}

	private static String escapeCharacter(String s, char c)
	{
		String oldOne = "" + c;
		String newOne = "\\" + c;

		String sOne = s.replace("\\", "\\\\").replace(oldOne, newOne);
		return sOne;
	}
}
