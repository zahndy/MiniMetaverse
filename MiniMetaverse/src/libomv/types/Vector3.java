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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.io.input.SwappedDataInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Vector3
{
	public float X;

	public float Y;

	public float Z;

	public Vector3(float val)
	{
		X = Y = Z = val;
	}

	public Vector3(Vector3 v)
	{
		X = v.X;
		Y = v.Y;
		Z = v.Z;
	}

	public Vector3(Vector3d vector)
	{
		X = (float) vector.X;
		Y = (float) vector.Y;
		Z = (float) vector.Z;
	}

	public Vector3(ByteBuffer byteArray)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
		Z = byteArray.getFloat();
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector3(XmlPullParser parser) throws XmlPullParserException, IOException
    {
    	if (parser.nextTag() != XmlPullParser.START_TAG)
    		throw new XmlPullParserException("Unexpected Tag: " + parser.getEventType(), parser, null);
		do
		{
			if (!parser.isEmptyElementTag())
			{
				String name = parser.getName();
				if (name.equals("X"))
				{
					X = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("Y"))
				{
					Y = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("Z"))
				{
					Z = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else
				{
					Helpers.skipElement(parser);
				}
			}
		}
        while (parser.nextTag() == XmlPullParser.START_TAG);
    }

    /**
	 * Constructor, builds a vector from a data stream
	 * 
	 * @param is
	 *            Data stream to read the binary data from
     * @throws IOException 
	 */
    public Vector3(DataInputStream is) throws IOException
    {
		X = Y = Z = 0f;
		fromBytes(is);
    }
    
    public Vector3(SwappedDataInputStream is) throws IOException
    {
		X = Y = Z = 0f;
		fromBytes(is);
    }

    /**
	 * Constructor, builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing three four-byte floats
	 * @param pos
	 *            Beginning position in the byte array
	 * @param le
	 *            is the byte array in little endian format
	 */
	public Vector3(byte[] byteArray, int pos)
	{
		X = Y = Z = 0f;
		fromBytes(byteArray, pos, false);
	}

	public Vector3(byte[] byteArray, int pos, boolean le)
	{
		X = Y = Z = 0f;
		fromBytes(byteArray, pos, le);
	}

	public Vector3(float x, float y, float z)
	{
		X = x;
		Y = y;
		Z = z;
	}

	public Vector3(String value)
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the raw bytes for this vector
	 * 
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] GetBytes()
	{
		byte[] byteArray = new byte[12];
		toBytes(byteArray, 0, false);
		return byteArray;
	}

	public void GetBytes(ByteBuffer byteArray)
	{
		byteArray.putFloat(X);
		byteArray.putFloat(Y);
		byteArray.putFloat(Z);
	}

	static public Vector3 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector3(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(null, "X").text(Float.toString(X)).endTag(null, "X");
		writer.startTag(null, "Y").text(Float.toString(Y)).endTag(null, "Y");
		writer.startTag(null, "Z").text(Float.toString(Z)).endTag(null, "Z");
	}

	@Override
	public String toString()
	{
		return "" + X + " " + Y + " " + Z;
	}

	@Override
	public int hashCode()
	{
		int x = (int) X;
		int y = (int) Y;
		int z = (int) Z;

		return (x ^ y ^ z);
	}

	/**
	 * Builds a vector from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing a 12 byte vector
	 * @param pos
	 *            Beginning position in the byte array
	 * @param le
	 *            is the byte array in little endian format
	 */
	public void fromBytes(byte[] bytes, int pos, boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			X = Helpers.BytesToFloatL(bytes, pos + 0);
			Y = Helpers.BytesToFloatL(bytes, pos + 4);
			Z = Helpers.BytesToFloatL(bytes, pos + 8);
		}
		else
		{
			X = Helpers.BytesToFloatB(bytes, pos + 0);
			Y = Helpers.BytesToFloatB(bytes, pos + 4);
			Z = Helpers.BytesToFloatB(bytes, pos + 8);
		}
	}

	/**
	 * Builds a vector from a data stream
	 * 
	 * @param is
	 *            DataInputStream to read the vector from
	 * @throws IOException 
	 */
	public void fromBytes(DataInputStream is) throws IOException
	{
		X = is.readFloat();
		Y = is.readFloat();
		Z = is.readFloat();
	}

	public void fromBytes(SwappedDataInputStream is) throws IOException
	{
		X = is.readFloat();
		Y = is.readFloat();
		Z = is.readFloat();
	}
	
	/**
	 * Writes the raw bytes for this vector to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 12 bytes before the end of the array
	 */
	public void toBytes(byte[] dest, int pos)
	{
		toBytes(dest, pos, false);
	}

	public void toBytes(byte[] dest, int pos, boolean le)
	{
		if (le)
		{
			Helpers.FloatToBytesL(X, dest, pos + 0);
			Helpers.FloatToBytesL(Y, dest, pos + 4);
			Helpers.FloatToBytesL(Z, dest, pos + 8);
		}
		else
		{
			Helpers.FloatToBytesB(X, dest, pos + 0);
			Helpers.FloatToBytesB(Y, dest, pos + 4);
			Helpers.FloatToBytesB(Z, dest, pos + 8);
		}
	}

	public float Length()
	{
		return (float) Math.sqrt(DistanceSquared(this, Zero));
	}

	public float LengthSquared()
	{
		return DistanceSquared(this, Zero);
	}

	public void Normalize()
	{
		Vector3 val = Normalize(this);
		X = val.X;
		Y = val.Y;
		Z = val.Z;
	}

	/**
	 * Test if this vector is equal to another vector, within a given tolerance
	 * range
	 * 
	 * @param vec
	 *            Vector to test against
	 * @param tolerance
	 *            The acceptable magnitude of difference between the two vectors
	 * @return True if the magnitude of difference between the two vectors is
	 *         less than the given tolerance, otherwise false
	 */
	public boolean ApproxEquals(Vector3 vec, float tolerance)
	{
		Vector3 diff = this.subtract(vec);
		return (diff.LengthSquared() <= tolerance * tolerance);
	}

	public int CompareTo(Vector3 vector)
	{
		return ((Float) Length()).compareTo(vector.Length());
	}

	/** Test if this vector is composed of all finite numbers */
	public boolean IsFinite()
	{
		return (Helpers.IsFinite(X) && Helpers.IsFinite(Y) && Helpers.IsFinite(Z));
	}

	public Vector3 Clamp(Vector3 min, Vector3 max)
	{
		return new Vector3(Helpers.Clamp(X, min.X, max.X), Helpers.Clamp(Y, min.Y, max.Y), Helpers.Clamp(Z, min.Z,
				max.Z));
	}

	public static Vector3 Cross(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1.Y * value2.Z - value2.Y * value1.Z, value1.Z * value2.X - value2.Z * value1.X,
				value1.X * value2.Y - value2.X * value1.Y);
	}

	public static float Distance(Vector3 value1, Vector3 value2)
	{
		return (float) Math.sqrt(DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(Vector3 value1, Vector3 value2)
	{
		return (value1.X - value2.X) * (value1.X - value2.X) + (value1.Y - value2.Y) * (value1.Y - value2.Y)
				+ (value1.Z - value2.Z) * (value1.Z - value2.Z);
	}

	public static float Dot(Vector3 value1, Vector3 value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y + value1.Z * value2.Z;
	}

	public static Vector3 Lerp(Vector3 value1, Vector3 value2, float amount)
	{

		return new Vector3(Helpers.Lerp(value1.X, value2.X, amount), Helpers.Lerp(value1.Y, value2.Y, amount),
				Helpers.Lerp(value1.Z, value2.Z, amount));
	}

	public static float Mag(Vector3 value)
	{
		return (float) Math.sqrt((value.X * value.X) + (value.Y * value.Y) + (value.Z * value.Z));
	}

	public static Vector3 Max(Vector3 value1, Vector3 value2)
	{
		return new Vector3(Math.max(value1.X, value2.X), Math.max(value1.Y, value2.Y), Math.max(value1.Z, value2.Z));
	}

	public static Vector3 Min(Vector3 value1, Vector3 value2)
	{
		return new Vector3(Math.min(value1.X, value2.X), Math.min(value1.Y, value2.Y), Math.min(value1.Z, value2.Z));
	}

	public static Vector3 Normalize(Vector3 value)
	{
		float factor = Distance(value, Zero);
		if (factor > Helpers.FLOAT_MAG_THRESHOLD)
		{
			factor = 1f / factor;
			value.X *= factor;
			value.Y *= factor;
			value.Z *= factor;
		}
		else
		{
			value.X = 0f;
			value.Y = 0f;
			value.Z = 0f;
		}
		return value;
	}

	/**
	 * Calculate the rotation between two vectors
	 * 
	 * @param a
	 *            Normalized directional vector (such as 1,0,0 for forward
	 *            facing)
	 * @param b
	 *            Normalized target vector
	 */
	public static Quaternion RotationBetween(Vector3 a, Vector3 b)
	{
		float dotProduct = Dot(a, b);
		Vector3 crossProduct = Cross(a, b);
		float magProduct = a.Length() * b.Length();
		double angle = Math.acos(dotProduct / magProduct);
		Vector3 axis = Normalize(crossProduct);
		float s = (float) Math.sin(angle / 2d);

		return new Quaternion(axis.X * s, axis.Y * s, axis.Z * s, (float) Math.cos(angle / 2d));
	}

	/** Interpolates between two vectors using a cubic equation */
	public static Vector3 SmoothStep(Vector3 value1, Vector3 value2, float amount)
	{
		return new Vector3(Helpers.SmoothStep(value1.X, value2.X, amount), Helpers.SmoothStep(value1.Y, value2.Y,
				amount), Helpers.SmoothStep(value1.Z, value2.Z, amount));
	}

	public static Vector3 Transform(Vector3 position, Matrix4 matrix)
	{
		return new Vector3((position.X * matrix.M11) + (position.Y * matrix.M21) + (position.Z * matrix.M31)
				+ matrix.M41, (position.X * matrix.M12) + (position.Y * matrix.M22) + (position.Z * matrix.M32)
				+ matrix.M42, (position.X * matrix.M13) + (position.Y * matrix.M23) + (position.Z * matrix.M33)
				+ matrix.M43);
	}

	public static Vector3 TransformNormal(Vector3 position, Matrix4 matrix)
	{
		return new Vector3((position.X * matrix.M11) + (position.Y * matrix.M21) + (position.Z * matrix.M31),
				(position.X * matrix.M12) + (position.Y * matrix.M22) + (position.Z * matrix.M32),
				(position.X * matrix.M13) + (position.Y * matrix.M23) + (position.Z * matrix.M33));
	}

	/**
	 * Parse a vector from a string
	 * 
	 * @param val
	 *            A string representation of a 3D vector, enclosed in arrow
	 *            brackets and separated by commas
	 */
	public static Vector3 Parse(String val)
	{
		String splitChar = ",";
		String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		return new Vector3(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()));
	}

	public static Vector3 TryParse(String val)
	{
		try
		{
			return Parse(val);
		}
		catch (Throwable t)
		{
			return Vector3.Zero;
		}
	}

	public static boolean TryParse(String val, RefObject<Vector3> result)
	{
		try
		{
			result.argvalue = Parse(val);
			return true;
		}
		catch (Throwable t)
		{
			result.argvalue = Vector3.Zero;
			return false;
		}
	}

	public boolean equals(Vector3 val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Vector3) ? equals((Vector3) obj) : false;
	}

	public static Vector3 negate(Vector3 value)
	{
		value.X = -value.X;
		value.Y = -value.Y;
		value.Z = -value.Z;
		return value;
	}

	public Vector3 add(Vector3 val)
	{
		return new Vector3(X + val.X, Y + val.Y, Z + val.Z);
	}

	public Vector3 subtract(Vector3 val)
	{
		return new Vector3(X - val.X, Y - val.X, Z - val.X);
	}

	public static Vector3 add(Vector3 val1, Vector3 val2)
	{
		val1.X += val2.X;
		val1.Y += val2.Y;
		val1.Z += val2.Z;
		return val1;
	}

	public static Vector3 subtract(Vector3 val1, Vector3 val2)
	{
		val1.X -= val2.X;
		val1.Y -= val2.Y;
		val1.Z -= val2.Z;
		return val1;
	}

	public static Vector3 multiply(Vector3 value1, Vector3 value2)
	{
		value1.X *= value2.X;
		value1.Y *= value2.Y;
		value1.Z *= value2.Z;
		return value1;
	}

	public static Vector3 multiply(Vector3 value1, float scaleFactor)
	{
		value1.X *= scaleFactor;
		value1.Y *= scaleFactor;
		value1.Z *= scaleFactor;
		return value1;
	}

	public static Vector3 multiply(Vector3 vec, Quaternion rot)
	{
		Vector3 vec2 = new Vector3(0f);
		vec2.X = rot.W * rot.W * vec.X + 2f * rot.Y * rot.W * vec.Z - 2f * rot.Z * rot.W * vec.Y + rot.X * rot.X
				* vec.X + 2f * rot.Y * rot.X * vec.Y + 2f * rot.Z * rot.X * vec.Z - rot.Z * rot.Z * vec.X - rot.Y
				* rot.Y * vec.X;
		vec2.Y = 2f * rot.X * rot.Y * vec.X + rot.Y * rot.Y * vec.Y + 2f * rot.Z * rot.Y * vec.Z + 2f * rot.W * rot.Z
				* vec.X - rot.Z * rot.Z * vec.Y + rot.W * rot.W * vec.Y - 2f * rot.X * rot.W * vec.Z - rot.X * rot.X
				* vec.Y;
		vec2.Z = 2f * rot.X * rot.Z * vec.X + 2f * rot.Y * rot.Z * vec.Y + rot.Z * rot.Z * vec.Z - 2f * rot.W * rot.Y
				* vec.X - rot.Y * rot.Y * vec.Z + 2f * rot.W * rot.X * vec.Y - rot.X * rot.X * vec.Z + rot.W * rot.W
				* vec.Z;
		return vec2;
	}

	public static Vector3 multiply(Vector3 vector, Matrix4 matrix)
	{
		return Transform(vector, matrix);
	}

	public static Vector3 divide(Vector3 value1, Vector3 value2)
	{
		value1.X /= value2.X;
		value1.Y /= value2.Y;
		value1.Z /= value2.Z;
		return value1;
	}

	public static Vector3 divide(Vector3 value, float divider)
	{
		float factor = 1f / divider;
		value.X *= factor;
		value.Y *= factor;
		value.Z *= factor;
		return value;
	}

	/** A vector with a value of 0,0,0 */
	public final static Vector3 Zero = new Vector3(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector3 One = new Vector3(1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public final static Vector3 UnitX = new Vector3(1f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public final static Vector3 UnitY = new Vector3(0f, 1f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public final static Vector3 UnitZ = new Vector3(0f, 0f, 1f);
}
