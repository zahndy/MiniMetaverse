/**
 * Copyright (c) 2008, openmetaverse.org
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
package libomv.StructuredData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

import libomv.StructuredData.LLSD.LLSDBinary;
import libomv.StructuredData.LLSD.LLSDNotation;
import libomv.StructuredData.LLSD.LLSDXml;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.types.Quaternion;
import libomv.utils.Helpers;

public class OSD
{
	protected static final String FRACT_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss.SS'Z'";
	protected static final String WHOLE_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss'Z'";;
	private static final String LLSD_BINARY_HEADER = "<?llsd/binary?>";
	private static final String LLSD_XML_HEADER = "<llsd>";
	private static final String LLSD_XML_ALT_HEADER = "<?xml";
	private static final String LLSD_XML_ALT2_HEADER = "<?llsd/xml?>";

	public enum OSDType
	{
		Unknown, Boolean, Integer, Real, String, UUID, Date, URI, Binary, Map, Array
	}

	public enum OSDFormat
	{
		Xml, Json, Binary, Notation
	}

	/** The OSD class implementation */
	public OSDType getType()
	{
		return OSDType.Unknown;
	}

	public boolean AsBoolean()
	{
		return false;
	}

	public int AsInteger()
	{
		return 0;
	}

	public int AsUInteger()
	{
		return 0;
	}

	public long AsLong()
	{
		return 0;
	}

	public long AsULong()
	{
		return 0;
	}

	public double AsReal()
	{
		return 0d;
	}

	public String AsString()
	{
		return "";
	}

	public UUID AsUUID()
	{
		return UUID.Zero;
	}

	public Date AsDate()
	{
		return Helpers.Epoch;
	}

	public URI AsUri()
	{
		return null;
	}

	public byte[] AsBinary()
	{
		return Helpers.EmptyBytes;
	}

	public InetAddress AsInetAddress()
	{
		try
		{
			return InetAddress.getByName("0.0.0.0");
		}
		catch (UnknownHostException e)
		{
			return null;
		}
	}

	public Vector2 AsVector2()
	{
		return Vector2.Zero;
	}

	public Vector3 AsVector3()
	{
		return Vector3.Zero;
	}

	public Vector3d AsVector3d()
	{
		return Vector3d.Zero;
	}

	public Vector4 AsVector4()
	{
		return Vector4.Zero;
	}

	public Quaternion AsQuaternion()
	{
		return Quaternion.Identity;
	}

	public Color4 AsColor4()
	{
		return Color4.Black;
	}

	@Override
	public String toString()
	{
		return "undef";
	}

	public static OSD FromBoolean(boolean value)
	{
		return new OSDBoolean(value);
	}

	public static OSD FromInteger(short value)
	{
		return new OSDInteger(value);
	}

	public static OSD FromInteger(int value)
	{
		return new OSDInteger(value);
	}

	public static OSD FromUInteger(int value)
	{
		return new OSDBinary(value & 0xffffffff);
	}

	public static OSD FromLong(long value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromULong(long value)
	{
		return new OSDBinary(value & 0xffffffffffffffffl);
	}

	public static OSD FromReal(double value)
	{
		return new OSDReal(value);
	}

	public static OSD FromReal(float value)
	{
		return new OSDReal(value);
	}

	public static OSD FromString(String value)
	{
		return new OSDString(value);
	}

	public static OSD FromString(InetAddress value)
	{
		return new OSDString(value.getHostAddress());
	}

	public static OSD FromUUID(UUID value)
	{
		return new OSDUUID(value);
	}

	public static OSD FromDate(Date value)
	{
		return new OSDDate(value);
	}

	public static OSD FromUri(URI value)
	{
		return new OSDUri(value);
	}

	public static OSD FromBinary(byte[] value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromBinary(long value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromBinary(InetAddress value)
	{
		return new OSDBinary(value.getAddress());
	}

	public static OSD FromVector2(Vector2 value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.X));
		array.add(OSD.FromReal(value.Y));
		return array;
	}

	public static OSD FromVector3(Vector3 value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.X));
		array.add(OSD.FromReal(value.Y));
		array.add(OSD.FromReal(value.Z));
		return array;
	}

	public static OSD FromVector3d(Vector3d value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.X));
		array.add(OSD.FromReal(value.Y));
		array.add(OSD.FromReal(value.Z));
		return array;
	}

	public static OSD FromVector4(Vector4 value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.X));
		array.add(OSD.FromReal(value.Y));
		array.add(OSD.FromReal(value.Z));
		array.add(OSD.FromReal(value.S));
		return array;
	}

	public static OSD FromQuaternion(Quaternion value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.X));
		array.add(OSD.FromReal(value.Y));
		array.add(OSD.FromReal(value.Z));
		array.add(OSD.FromReal(value.W));
		return array;
	}

	public static OSD FromColor4(Color4 value)
	{
		OSDArray array = new OSDArray();
		array.add(OSD.FromReal(value.R));
		array.add(OSD.FromReal(value.G));
		array.add(OSD.FromReal(value.B));
		array.add(OSD.FromReal(value.A));
		return array;
	}

	public static OSD FromObject(Object value)
	{
		if (value == null)
		{
			return new OSD();
		}
		else if (value instanceof Boolean)
		{
			return new OSDBoolean((Boolean) value);
		}
		else if (value instanceof Integer)
		{
			return new OSDInteger((Integer) value);
		}
		else if (value instanceof Short)
		{
			return new OSDInteger(((Short) value).intValue());
		}
		else if (value instanceof Byte)
		{
			return new OSDInteger(((Byte) value).intValue());
		}
		else if (value instanceof Double)
		{
			return new OSDReal(((Double) value).doubleValue());
		}
		else if (value instanceof Float)
		{
			return new OSDReal(((Float) value).doubleValue());
		}
		else if (value instanceof String)
		{
			return new OSDString((String) value);
		}
		else if (value instanceof UUID)
		{
			return new OSDUUID((UUID) value);
		}
		else if (value instanceof Date)
		{
			return new OSDDate((Date) value);
		}
		else if (value instanceof URI)
		{
			return new OSDUri((URI) value);
		}
		else if (value instanceof byte[])
		{
			return new OSDBinary((byte[]) value);
		}
		else if (value instanceof Long)
		{
			return new OSDBinary((Long) value);
		}
		else if (value instanceof Vector2)
		{
			return FromVector2((Vector2) value);
		}
		else if (value instanceof Vector3)
		{
			return FromVector3((Vector3) value);
		}
		else if (value instanceof Vector3d)
		{
			return FromVector3d((Vector3d) value);
		}
		else if (value instanceof Vector4)
		{
			return FromVector4((Vector4) value);
		}
		else if (value instanceof Quaternion)
		{
			return FromQuaternion((Quaternion) value);
		}
		else if (value instanceof Color4)
		{
			return FromColor4((Color4) value);
		}
		else
		{
			return new OSD();
		}
	}

	public static Object ToObject(Type type, OSD value)
	{
		if (type == Long.class || type == long.class)
		{
			return value.AsLong();
		}
		else if (type == Integer.class || type == int.class)
		{
			return value.AsInteger();
		}
		else if (type == Short.class || type == short.class)
		{
			return (short) value.AsInteger();
		}
		else if (type == Byte.class || type == byte.class)
		{
			return (byte) value.AsInteger();
		}
		else if (type == String.class)
		{
			return value.AsString();
		}
		else if (type == Boolean.class || type == boolean.class)
		{
			return value.AsBoolean();
		}
		else if (type == Float.class || type == float.class)
		{
			return (float) value.AsReal();
		}
		else if (type == Double.class || type == double.class)
		{
			return value.AsReal();
		}
		else if (type == UUID.class)
		{
			return value.AsUUID();
		}
		else if (type == URI.class)
		{
			return value.AsUri();
		}
		else if (type == Vector3.class)
		{
			if (value.getType() == OSDType.Array)
			{
				return ((OSDArray) value).AsVector3();
			}
			return Vector3.Zero;
		}
		else if (type == Vector4.class)
		{
			if (value.getType() == OSDType.Array)
			{
				return ((OSDArray) value).AsVector4();
			}
			return Vector4.Zero;
		}
		else if (type == Quaternion.class)
		{
			if (value.getType() == OSDType.Array)
			{
				return ((OSDArray) value).AsQuaternion();
			}
			return Quaternion.Identity;
		}
		else
		{
			return null;
		}
	}

	public void serialize(Writer writer, OSDFormat type) throws IOException
	{
		switch (type)
		{
			case Binary:
				LLSDBinary.serialize(new WriterOutputStream(writer), this);
				break;
			case Notation:
				LLSDNotation.serialize(writer, this);
				break;
			case Xml:
				LLSDXml.serialize(writer, this);
				break;
			case Json:
				// Json.serialize(writer, this);
				break;
		}
	}

	public void serialize(OutputStream stream, OSDFormat type) throws IOException
	{
		switch (type)
		{
			case Binary:
				LLSDBinary.serialize(stream, this);
				break;
			case Notation:
				LLSDNotation.serialize(new OutputStreamWriter(stream), this);
				break;
			case Xml:
				LLSDXml.serialize(new OutputStreamWriter(stream), this);
				break;
			case Json:
				// Json.serialize(new OutputStreamWriter(stream), this);
				break;
		}
	}

	public String serializeToString(OSDFormat type) throws IOException
	{
		switch (type)
		{
			case Binary:
				OutputStream binary = new ByteArrayOutputStream();
				LLSDBinary.serialize(binary, this);
				return binary.toString();
			case Notation:
				return LLSDNotation.serializeToString(this);
			case Xml:
				return LLSDXml.serializeToString(this);
			case Json:
				Writer json = new StringWriter();
				// Json.serialize(json, this);
				return json.toString();
		}
		return null;
	}

	public byte[] serializeToBytes(OSDFormat type) throws IOException
	{
		ByteArrayOutputStream stream;
		switch (type)
		{
			case Binary:
				stream = new ByteArrayOutputStream();
				LLSDBinary.serialize(stream, this);
				return stream.toByteArray();
			case Notation:
				stream = new ByteArrayOutputStream();
				LLSDNotation.serialize(new OutputStreamWriter(stream, Helpers.UTF8_ENCODING), this);
				return stream.toByteArray();
			case Xml:
				return LLSDXml.serializeToBytes(this, Helpers.UTF8_ENCODING);
			case Json:
				stream = new ByteArrayOutputStream();
				// Json.serialize(new OutputStreamWriter(stream,
				// Helpers.UTF8_ENCODING), this);
				return stream.toByteArray();
		}
		return null;
	}

	public static OSD parse(Reader inread) throws IOException, ParseException
	{
		char[] head = new char[15];
		PushbackReader reader = new PushbackReader(inread, 15);
		int num = reader.read(head, 0, 15);
		reader.unread(head, 0, num);
		String string = new String(head, 0, num);

		if (string.toLowerCase().startsWith(LLSD_BINARY_HEADER))
		{
			return LLSDBinary.parse(new ReaderInputStream(reader, Helpers.ASCII_ENCODING));
		}
		else if (string.toLowerCase().startsWith(LLSD_XML_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT2_HEADER))
		{
			return LLSDXml.parse(reader);
		}
		return null;
	}

	public static OSD parse(InputStream instream, String encoding) throws IOException, ParseException
	{
		byte[] head = new byte[15];
		PushbackInputStream stream = new PushbackInputStream(instream, 15);
		int num = stream.read(head, 0, 15);
		String string = new String(head, 0, num, encoding);
		stream.unread(head, 0, num);

		if (string.toLowerCase().startsWith(LLSD_BINARY_HEADER))
		{
			return LLSDBinary.parse(stream);
		}
		else if (string.toLowerCase().startsWith(LLSD_XML_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT2_HEADER))
		{
			return LLSDXml.parse(stream, encoding);
		}
		return null;
	}

	public static OSD parse(String string) throws IOException, ParseException
	{
		if (string.toLowerCase().startsWith(LLSD_BINARY_HEADER))
		{
			InputStream stream = new ReaderInputStream(new StringReader(string));
			return LLSDBinary.parse(stream);
		}
		else if (string.toLowerCase().startsWith(LLSD_XML_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT2_HEADER))
		{
			return LLSDXml.parse(string);
		}
		return null;
	}

	public static OSD parse(byte[] data, String encoding) throws IOException, ParseException
	{
		String string = new String(data, 0, 15, encoding);
		if (string.toLowerCase().startsWith(LLSD_BINARY_HEADER))
		{
			return LLSDBinary.parse(new ByteArrayInputStream(data));
		}
		else if (string.toLowerCase().startsWith(LLSD_XML_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT_HEADER)
				|| string.toLowerCase().startsWith(LLSD_XML_ALT2_HEADER))
		{
			return LLSDXml.parse(data, encoding);
		}
		return null;
	}

	/**
	 * Uses reflection to create an SDMap from all of the SD serializable types
	 * in an object
	 * 
	 * @param obj
	 *            Class or struct containing serializable types
	 * @return An SDMap holding the serialized values from the container object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static OSDMap serializeMembers(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		Field[] fields = obj.getClass().getFields();
		OSDMap map = new OSDMap(fields.length);
		for (Field field : fields)
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				OSD serializedField = OSD.FromObject(field.get(obj));

				if (serializedField.getType() != OSDType.Unknown)
				{
					map.put(field.getName(), serializedField);
				}
			}
		}
		return map;
	}

	/**
	 * Uses reflection to deserialize member variables in an object from an
	 * SDMap
	 * 
	 * @param obj
	 *            Reference to an object to fill with deserialized values
	 * @param serialized
	 *            Serialized values to put in the target object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object deserializeMembers(Object obj, OSDMap serialized) throws IllegalArgumentException,
			IllegalAccessException
	{
		for (Field field : obj.getClass().getFields())
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				OSD serializedField = serialized.get(field.getName());
				if (serializedField != null)
				{
					field.set(obj, ToObject(field.getGenericType(), serializedField));
				}
			}
		}
		return obj;
	}
}