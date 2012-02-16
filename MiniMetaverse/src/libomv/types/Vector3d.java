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

public class Vector3d
{
	public double X;

	public double Y;

	public double Z;

	public Vector3d(double val)
	{
		X = Y = Z = val;
	}

	public Vector3d(double x, double y, double z)
	{
		X = x;
		Y = y;
		Z = z;
	}

	public Vector3d(byte[] bytes, int offset)
	{
		X = Helpers.BytesToDoubleL(bytes, offset);
		offset += 8;
		Y = Helpers.BytesToDoubleL(bytes, offset);
		offset += 8;
		Z = Helpers.BytesToDoubleL(bytes, offset);
	}

	public Vector3d(ByteBuffer byteArray)
	{
		X = byteArray.getDouble();
		Y = byteArray.getDouble();
		Z = byteArray.getDouble();
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector3d(XmlPullParser parser) throws XmlPullParserException, IOException
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
					X = Helpers.TryParseDouble(parser.nextText().trim());
				}
				else if (name.equals("Y"))
				{
					Y = Helpers.TryParseDouble(parser.nextText().trim());
				}
				else if (name.equals("Z"))
				{
					Z = Helpers.TryParseDouble(parser.nextText().trim());
				}
				else
				{
					Helpers.skipElement(parser);
				}
			}
		}
        while (parser.nextTag() == XmlPullParser.START_TAG);
    }

	public void GetBytes(ByteBuffer byteArray)
	{
		byteArray.putDouble(X);
		byteArray.putDouble(Y);
		byteArray.putDouble(Z);
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
		return ToBytes(dest, pos, false);
	}

	public int ToBytes(byte[] dest, int pos, boolean le)
	{
		if (le)
		{
			Helpers.DoubleToBytesL(X, dest, pos + 0);
			Helpers.DoubleToBytesL(Y, dest, pos + 4);
			Helpers.DoubleToBytesL(Z, dest, pos + 8);
		}
		else
		{
			Helpers.DoubleToBytesB(X, dest, pos + 0);
			Helpers.DoubleToBytesB(Y, dest, pos + 4);
			Helpers.DoubleToBytesB(Z, dest, pos + 8);
		}
		return 24;
	}

	static public Vector3d parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector3d(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(null, "X").text(Double.toString(X)).endTag(null, "X");
		writer.startTag(null, "Y").text(Double.toString(Y)).endTag(null, "Y");
		writer.startTag(null, "Z").text(Double.toString(Z)).endTag(null, "Z");
	}
	
	@Override
	public String toString()
	{
		return "" + X + " " + Y + " " + Z;
	}

	public boolean equals(Vector3d val)
	{
		return val != null && X == val.X && Y == val.Y && Z == val.Z;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Vector3d) ? equals((Vector3d) obj) : false;
	}

	@Override
	public int hashCode()
	{
		return (int) X + (int) Y + (int) Z;
	}

	/** A vector with a value of 0,0,0 */
	public final static Vector3d Zero = new Vector3d(0f);
	/** A vector with a value of 1,1,1 */
	public final static Vector3d One = new Vector3d(1d, 1d, 1d);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public final static Vector3d UnitX = new Vector3d(1d, 0d, 0d);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public final static Vector3d UnitY = new Vector3d(0d, 1d, 0d);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public final static Vector3d UnitZ = new Vector3d(0d, 0d, 1d);
}
