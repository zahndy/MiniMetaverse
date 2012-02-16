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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDString;
import libomv.types.UUID;
import libomv.utils.Helpers;

public final class LLSDXml
{
	private static final String LLSD_TAG = "llsd";
	private static final String UNDEF_TAG = "undef";
	private static final String BOOLEAN_TAG = "boolean";
	private static final String INTEGER_TAG = "integer";
	private static final String REAL_TAG = "real";
	private static final String STRING_TAG = "string";
	private static final String UUID_TAG = "uuid";
	private static final String DATE_TAG = "date";
	private static final String URI_TAG = "uri";
	private static final String BINARY_TAG = "binary";
	private static final String MAP_TAG = "map";
	private static final String KEY_TAG = "key";
	private static final String ARRAY_TAG = "array";

	/*
	 * private static String LastXmlErrors = Helpers.EmptyString; private static
	 * Object XmlValidationLock = new Object();
	 * 
	 * "\r\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
	 * "<xs:schema elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
	 * + "\r\n" +
	 * "  <xs:import schemaLocation=\"xml.xsd\" namespace=\"http://www.w3.org/XML/1998/namespace\" />"
	 * + "\r\n  <xs:element name=\"uri\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"uuid\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"KEYDATA\">\r\n    <xs:complexType>" +
	 * "\r\n      <xs:sequence>\r\n" +
	 * "        <xs:element ref=\"key\" />\r\n" +
	 * "        <xs:element ref=\"DATA\" />\r\n      </xs:sequence>" +
	 * "\r\n    </xs:complexType>\r\n  </xs:element>\r\n" +
	 * "  <xs:element name=\"date\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"key\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"boolean\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"undef\">\r\n    <xs:complexType>" +
	 * "\r\n      <xs:sequence>\r\n" +
	 * "        <xs:element ref=\"EMPTY\" />\r\n      </xs:sequence>"
	 * + "\r\n    </xs:complexType>\r\n  </xs:element>\r\n"
	 * + "  <xs:element name=\"map\">\r\n    <xs:complexType>" +
	 * "\r\n      <xs:sequence>\r\n" +
	 * "        <xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" ref=\"KEYDATA\" />"
	 * + "\r\n      </xs:sequence>\r\n    </xs:complexType>" +
	 * "\r\n  </xs:element>\r\n" +
	 * "  <xs:element name=\"real\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"ATOMIC\">\r\n    <xs:complexType>" +
	 * "\r\n      <xs:choice>\r\n" +
	 * "        <xs:element ref=\"undef\" />\r\n" +
	 * "        <xs:element ref=\"boolean\" />\r\n" +
	 * "        <xs:element ref=\"integer\" />\r\n" +
	 * "        <xs:element ref=\"real\" />\r\n" +
	 * "        <xs:element ref=\"uuid\" />\r\n" +
	 * "        <xs:element ref=\"string\" />\r\n" +
	 * "        <xs:element ref=\"date\" />\r\n" +
	 * "        <xs:element ref=\"uri\" />\r\n" +
	 * "        <xs:element ref=\"binary\" />\r\n      </xs:choice>" +
	 * "\r\n    </xs:complexType>\r\n  </xs:element>\r\n" +
	 * "  <xs:element name=\"DATA\">\r\n    <xs:complexType>\r\n"
	 * + "      <xs:choice>\r\n        <xs:element ref=\"ATOMIC\" />"
	 * + "\r\n        <xs:element ref=\"map\" />\r\n" +
	 * "        <xs:element ref=\"array\" />\r\n      </xs:choice>" +
	 * "\r\n    </xs:complexType>\r\n  </xs:element>\r\n" +
	 * "  <xs:element name=\"llsd\">\r\n    <xs:complexType>\r\n"
	 * + "      <xs:sequence>\r\n        <xs:element ref=\"DATA\" />"
	 * + "\r\n      </xs:sequence>\r\n    </xs:complexType>" +
	 * "\r\n  </xs:element>\r\n  <xs:element name=\"binary\">" +
	 * "\r\n    <xs:complexType>\r\n      <xs:simpleContent>" +
	 * "\r\n        <xs:extension base=\"xs:string\">\r\n" +
	 * "          <xs:attribute default=\"base64\" name=\"encoding\" type=\"xs:string\" />"
	 * + "\r\n        </xs:extension>\r\n" +
	 * "      </xs:simpleContent>\r\n    </xs:complexType>\r\n" +
	 * "  </xs:element>\r\n  <xs:element name=\"array\">\r\n" +
	 * "    <xs:complexType>\r\n      <xs:sequence>\r\n" +
	 * "        <xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" ref=\"DATA\" />"
	 * + "\r\n      </xs:sequence>\r\n    </xs:complexType>" +
	 * "\r\n  </xs:element>\r\n" +
	 * "  <xs:element name=\"integer\" type=\"xs:string\" />\r\n" +
	 * "  <xs:element name=\"string\">\r\n    <xs:complexType>" +
	 * "\r\n      <xs:simpleContent>\r\n" +
	 * "        <xs:extension base=\"xs:string\">\r\n" +
	 * "          <xs:attribute ref=\"xml:space\" />\r\n" +
	 * "        </xs:extension>\r\n      </xs:simpleContent>\r\n"
	 * + "    </xs:complexType>\r\n  </xs:element>\r\n" +
	 * "</xs:schema>\r\n" + \"; MemoryStream stream = new
	 * MemoryStream(Encoding.ASCII.GetBytes(schemaText)); XmlSchema = new
	 * XmlSchema(); XmlSchema = XmlSchema.Read(stream, new
	 * ValidationEventHandler(LLSDXmlSchemaValidationHandler)); } } private
	 * static void LLSDXmlSchemaValidationHandler(Object sender,
	 * ValidationEventArgs args) { String error =
	 * String.format("Line: %d - Position: %d - %s", XmlTextReader.LineNumber,
	 * XmlTextReader.LinePosition, args.Message); if
	 * (LastXmlErrors.equals(String.Empty)) LastXmlErrors = error; else
	 * LastXmlErrors += Helpers.NewLine + error; }
	 */

	/**
	 * Parse an OSD XML string and convert it into an hierarchical OSD object
	 * 
	 * @param string
	 *            the OSD XML string to parse
	 * @return hierarchical OSD object
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws OSDException
	 */
	public static OSD parse(String string) throws IOException, ParseException
	{
		try
		{
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(new StringReader(string));
			return parse(parser);
		}
		catch (XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Parse an OSD XML byte array and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param data
	 *            the OSD XML byte array to parse
	 * @param encoding
	 *            the text encoding to use when converting the stream to text
	 * @return hierarchical OSD object
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws OSDException
	 */
	public static OSD parse(byte[] data, String encoding) throws IOException, ParseException
	{
		try
		{
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(new ByteArrayInputStream(data), encoding);
			return parse(parser);
		}
		catch (XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Parse an OSD XML reader and convert it into an hierarchical OSD object
	 * 
	 * @param reader
	 *            the OSD XML reader to parse
	 * @return hierarchical OSD object
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws OSDException
	 */
	public static OSD parse(Reader reader) throws IOException, ParseException
	{
		try
		{
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			return parse(parser);
		}
		catch (XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Parse an OSD XML byte stream and convert it into an hierarchical OSD
	 * object
	 * 
	 * @param stream
	 *            the OSD XML byte stream to parse
	 * @param encoding
	 *            the text encoding to use when converting the stream to text
	 * @return hierarchical OSD object
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws OSDException
	 */
	public static OSD parse(InputStream stream, String encoding) throws IOException, ParseException
	{
		try
		{
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(stream, encoding);
			return parse(parser);
		}
		catch (XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML string
	 * 
	 * @param data
	 *            The hierarchical OSD object
	 * @return an OSD XML formatted string
	 * @throws IOException
	 * @throws OSDException
	 */
	public static String serializeToString(OSD data) throws IOException
	{
		try
		{
			XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(new StringWriter());
			serialize(xmlWriter, data);
			return xmlWriter.toString();
		}
		catch (XmlPullParserException ex)
		{
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML byte array
	 * 
	 * @param data
	 *            The hierarchical OSD object
	 * @param encoding
	 *            The text encoding to use when converting the text into a byte
	 *            array
	 * @return an OSD XML formatted byte array
	 * @throws IOException
	 * @throws OSDException
	 */
	public static byte[] serializeToBytes(OSD data, String encoding) throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try
		{
			XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(stream, encoding);
			serialize(xmlWriter, data);
		}
		catch (XmlPullParserException ex)
		{
			new IOException(ex.getMessage());
		}
		return stream.toByteArray();
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML writer
	 * 
	 * @param writer
	 *            The writer to format the serialized data into
	 * @param data
	 *            The hierarchical OSD object
	 * @return an OSD XML formatted string
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public static void serialize(Writer writer, OSD data) throws IOException
	{
		try
		{
			XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(writer);
			serialize(xmlWriter, data);
		}
		catch (XmlPullParserException ex)
		{
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML string
	 * 
	 * @param stream
	 *            The hierarchical OSD object byte stream
	 * @param encoding
	 *            the text encoding to use when converting the text into a byte
	 *            stream
	 * @return an OSD XML formatted string
	 * @throws IOException
	 * @throws OSDException
	 */
	public static void serialize(OutputStream stream, OSD data, String encoding) throws IOException
	{
		try
		{
			XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(stream, encoding);
			serialize(xmlWriter, data);
		}
		catch (XmlPullParserException ex)
		{
			throw new IOException(ex.getMessage());
		}
	}

	private static void serialize(XmlSerializer writer, OSD data) throws IOException
	{
		writer.startDocument(null, null);
		writer.startTag(null, LLSD_TAG);
		serializeElement(writer, data);
		writer.endTag(null, LLSD_TAG);
		writer.endDocument();
		writer.flush();
	}

	private static void serializeElement(XmlSerializer writer, OSD data) throws IOException
	{
		switch (data.getType())
		{
			case Unknown:
				writer.startTag(null, UNDEF_TAG).endTag(null, UNDEF_TAG);
				break;
			case Boolean:
				writer.startTag(null, BOOLEAN_TAG).text(data.AsString()).endTag(null, BOOLEAN_TAG);
				break;
			case Integer:
				writer.startTag(null, INTEGER_TAG).text(data.AsString()).endTag(null, INTEGER_TAG);
				break;
			case Real:
				writer.startTag(null, REAL_TAG).text(data.AsString()).endTag(null, REAL_TAG);
				break;
			case String:
				writer.startTag(null, STRING_TAG).text(data.AsString()).endTag(null, STRING_TAG);
				break;
			case UUID:
				writer.startTag(null, UUID_TAG).text(data.AsString()).endTag(null, UUID_TAG);
				break;
			case Date:
				writer.startTag(null, DATE_TAG).text(data.AsString()).endTag(null, DATE_TAG);
				break;
			case URI:
				writer.startTag(null, URI_TAG).text(data.AsString()).endTag(null, URI_TAG);
				break;
			case Binary:
				writer.startTag(null, BINARY_TAG).attribute(null, "encoding", "base64").text(data.AsString())
						.endTag(null, BINARY_TAG);
				break;
			case Map:
				OSDMap map = (OSDMap) data;
				writer.startTag(null, MAP_TAG);
				for (Entry<String, OSD> kvp : map.entrySet())
				{
					writer.startTag(null, KEY_TAG).text(kvp.getKey()).endTag(null, KEY_TAG);
					serializeElement(writer, kvp.getValue());
				}
				writer.endTag(null, MAP_TAG);
				break;
			case Array:
				OSDArray array = (OSDArray) data;
				writer.startTag(null, ARRAY_TAG);
				for (OSD osd : array)
				{
					serializeElement(writer, osd);
				}
				writer.endTag(null, ARRAY_TAG);
				break;
		}
	}

	private static OSD parse(XmlPullParser parser) throws IOException, XmlPullParserException
	{
		OSD ret = null;
		// lets start pulling...
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, LLSD_TAG);
		if (!parser.isEmptyElementTag())
		{
			parser.nextTag();
			ret = parseElement(parser);
		}
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, null, LLSD_TAG);
		return ret;
	}

	private static OSD parseElement(XmlPullParser parser) throws IOException, XmlPullParserException
	{
		parser.require(XmlPullParser.START_TAG, null, null);
		String s = null, name = parser.getName();
		boolean notEmpty = !parser.isEmptyElementTag();
		OSD ret = null;

		if (name.equals(BOOLEAN_TAG))
		{
			boolean bool = false;
			if (notEmpty)
			{
				s = parser.nextText().trim();
				bool = (s != null && !s.isEmpty() && (s.toLowerCase().equals("true") || s.equals("1")));
			}
			ret = OSD.FromBoolean(bool);
		}
		else if (name.equals(INTEGER_TAG))
		{
			int value = 0;
			if (notEmpty)
			{
				value = Helpers.TryParseInt(parser.nextText());
			}
			ret = OSD.FromInteger(value);
		}
		else if (name.equals(REAL_TAG))
		{
			double real = 0d;
			if (notEmpty)
			{
				real = Helpers.TryParseDouble(parser.nextText());
			}
			ret = OSD.FromReal(real);
		}
		else if (name.equals(UUID_TAG))
		{
			UUID uuid = UUID.Zero;
			if (notEmpty)
			{
				uuid = new UUID(parser.nextText());
			}
			ret = OSD.FromUUID(uuid);
		}
		else if (name.equals(DATE_TAG))
		{
			Date date = Helpers.Epoch;
			if (notEmpty)
			{
				date = new OSDString(parser.nextText()).AsDate();
			}
			ret = OSD.FromDate(date);
		}
		else if (name.equals(STRING_TAG))
		{
			if (notEmpty)
			{
				s = parser.nextText();
			}
			ret = OSD.FromString(s);
		}
		else if (name.equals(BINARY_TAG))
		{
			s = parser.getAttributeValue(null, "encoding");
			if (s != null && !s.equals("base64"))
			{
				throw new XmlPullParserException("Unsupported binary encoding: " + s + " encoding", parser, null);
			}
			byte[] data = Helpers.EmptyBytes;
			if (notEmpty)
			{
				data = Base64.decodeBase64(parser.nextText());
			}
			ret = OSD.FromBinary(data);
		}
		else if (name.equals(URI_TAG))
		{
			try
			{
				URI uri = new URI(Helpers.EmptyString);
				if (notEmpty)
				{
					uri = new URI(parser.nextText());
				}
				ret = OSD.FromUri(uri);
			}
			catch (URISyntaxException ex)
			{
				new ParseException("Error parsing URI: " + ex.getMessage(), parser.getLineNumber());
			}
		}
		else if (name.equals(MAP_TAG))
		{
			ret = new OSDMap();
			if (notEmpty)
			{
				parseMap(parser, (OSDMap) ret);
			}
		}
		else if (name.equals(ARRAY_TAG))
		{
			ret = new OSDArray();
			if (notEmpty)
			{
				parseArray(parser, (OSDArray) ret);
			}
		}
		else
		{
			if (name.equals(UNDEF_TAG))
			{
				ret = new OSD();
			}
			parser.nextTag();
			notEmpty = true;
		}
		if (!notEmpty)
		{
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, name);
		return ret;
	}

	private static void parseMap(XmlPullParser parser, OSDMap map) throws IOException, XmlPullParserException
	{
		while (parser.nextTag() != XmlPullParser.END_TAG)
		{
			parser.require(XmlPullParser.START_TAG, null, KEY_TAG);
			String key = parser.nextText();
			parser.require(XmlPullParser.END_TAG, null, KEY_TAG);
			parser.nextTag();
			map.put(key, parseElement(parser));
		}
	}

	private static void parseArray(XmlPullParser parser, OSDArray array) throws IOException, XmlPullParserException
	{
		while (parser.nextTag() != XmlPullParser.END_TAG)
		{
			array.add(parseElement(parser));
		}
	}
}

/*
 * public static boolean validate(XmlPullParser xmlData, RefObject<String>
 * error) { synchronized (XmlValidationLock) { LastXmlErrors =
 * Helpers.EmptyString; XmlTextReader = xmlData;
 * 
 * createSchema();
 * 
 * XmlReaderSettings readerSettings = new XmlReaderSettings();
 * readerSettings.ValidationType = ValidationType.Schema;
 * readerSettings.Schemas.add(XmlSchema); // TODO TASK: Java has no equivalent
 * to C#-style event wireups: readerSettings.ValidationEventHandler += new
 * ValidationEventHandler(LLSDXmlSchemaValidationHandler);
 * 
 * XmlReader reader = XmlReader.Create(xmlData, readerSettings);
 * 
 * try { while (reader.Read()) { } } catch (XmlException t) { error.argvalue =
 * LastXmlErrors; return false; }
 * 
 * if (LastXmlErrors.equals(Helpers.EmptyString)) { error.argvalue = null;
 * return true; } else { error.argvalue = LastXmlErrors; return false; } } }
 */