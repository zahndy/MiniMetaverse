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
import java.nio.ByteBuffer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;

public class Vector4
{
	public float X;

	public float Y;

	public float Z;

	public float S;

	public Vector4()
	{
		X = Y = Z = S = 0;
	}

	public Vector4(float val)
	{
		X = Y = Z = S = val;
	}

	public Vector4(ByteBuffer byteArray)
	{
		X = byteArray.getFloat();
		Y = byteArray.getFloat();
		Z = byteArray.getFloat();
		S = byteArray.getFloat();
	}

	public Vector4(float x, float y, float z, float s)
	{
		X = x;
		Y = y;
		Z = z;
		S = s;
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector4(XmlPullParser parser) throws XmlPullParserException, IOException
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
				else if (name.equals("S"))
				{
					S = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else
				{
					Helpers.skipElement(parser);
				}
			}
		}
        while (parser.nextTag() == XmlPullParser.START_TAG);
    }

	public Vector4(byte[] objectData, int pos)
	{
		this(objectData, pos, false);
	}

	public Vector4(byte[] objectData, int pos, boolean bigEndian)
	{
		if (objectData.length >= (pos + 16))
		{
			if (bigEndian)
			{
				X = Helpers.BytesToFloatB(objectData, pos);
				Y = Helpers.BytesToFloatB(objectData, pos + 4);
				Z = Helpers.BytesToFloatB(objectData, pos + 8);
				S = Helpers.BytesToFloatB(objectData, pos + 12);
			}
			else
			{
				X = Helpers.BytesToFloatL(objectData, pos);
				Y = Helpers.BytesToFloatL(objectData, pos + 4);
				Z = Helpers.BytesToFloatL(objectData, pos + 8);
				S = Helpers.BytesToFloatL(objectData, pos + 12);
			}
		}
		else
		{
			X = Y = Z = S = 0.0F;
		}

	}

	public Vector4(Vector4 v)
	{
		X = v.X;
		Y = v.Y;
		Z = v.Z;
		S = v.S;
	}

	// <returns></returns>
	public void GetBytes(ByteBuffer byteArray)
	{
		byteArray.putFloat(X);
		byteArray.putFloat(Y);
		byteArray.putFloat(Z);
		byteArray.putFloat(S);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int ToBytes(byte[] bytes)
	{
		return ToBytes(bytes, 0);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int ToBytes(byte[] dest, int pos)
	{
		pos += Helpers.FloatToBytesL(X, dest, pos);
		pos += Helpers.FloatToBytesL(Y, dest, pos);
		pos += Helpers.FloatToBytesL(Z, dest, pos);
		pos += Helpers.FloatToBytesL(S, dest, pos);
		return 16;
	}

	static public Vector4 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector4(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(null, "X").text(Float.toString(X)).endTag(null, "X");
		writer.startTag(null, "Y").text(Float.toString(Y)).endTag(null, "Y");
		writer.startTag(null, "Z").text(Float.toString(Z)).endTag(null, "Z");
		writer.startTag(null, "S").text(Float.toString(S)).endTag(null, "S");
	}

	@Override
	public String toString()
	{
		return "" + X + " " + Y + " " + Z + " " + S;
	}

	public boolean equals(Vector4 val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z && S == val.S;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Vector4) ? equals((Vector4) obj) : false;
	}

	@Override
	public int hashCode()
	{
		return (int) X + (int) Y + (int) Z + (int) S;
	}

	/** A vector with a value of 0,0,0,0 */
	public final static Vector4 Zero = new Vector4(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector4 One = new Vector4(1f, 1f, 1f, 1f);
	/** A unit vector facing forward (X axis), value 1,0,0,0 */
	public final static Vector4 UnitX = new Vector4(1f, 0f, 0f, 0f);
	/** A unit vector facing left (Y axis), value 0,1,0,0 */
	public final static Vector4 UnitY = new Vector4(0f, 1f, 0f, 0f);
	/** A unit vector facing up (Z axis), value 0,0,1,0 */
	public final static Vector4 UnitZ = new Vector4(0f, 0f, 1f, 0f);
	/** A unit vector facing up (S axis), value 0,0,0,1 */
	public final static Vector4 UnitS = new Vector4(0f, 0f, 0f, 1f);
}
