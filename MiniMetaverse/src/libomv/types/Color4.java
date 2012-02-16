package libomv.types;

/**
 * Copyright (c) 2008, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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

import java.io.IOException;
import java.lang.IllegalArgumentException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;

/** An 8-bit color structure including an alpha channel */
public final class Color4
{
	/** Red */
	public float R;
	/** Green */
	public float G;
	/** Blue */
	public float B;
	/** Alpha */
	public float A;

	/**
	 * Builds a color from four values
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public Color4(byte r, byte g, byte b, byte a)
	{
		final float quanta = 1.0f / 255.0f;

		R = r * quanta;
		G = g * quanta;
		B = b * quanta;
		A = a * quanta;
	}

	/**
	 * Builds a color from four values
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the values is outside 0 .. 1.0
	 */
	public Color4(float r, float g, float b, float a)
	{
		// Quick check to see if someone is doing something obviously wrong
		// like using float values from 0.0 - 255.0
		if (r > 1f || g > 1f || b > 1f || a > 1f)
		{
			throw new IllegalArgumentException(String.format(
					"Attempting to initialize Color4 with out of range values <%f,%f,%f,%f>", r, g, b, a));
		}

		// Valid range is from 0.0 to 1.0
		R = Helpers.Clamp(r, 0f, 1f);
		G = Helpers.Clamp(g, 0f, 1f);
		B = Helpers.Clamp(b, 0f, 1f);
		A = Helpers.Clamp(a, 0f, 1f);
	}

    /**
	 * Constructor, builds a Color4 from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Color4(XmlPullParser parser) throws XmlPullParserException, IOException
    {
    	if (parser.nextTag() != XmlPullParser.START_TAG)
    		throw new XmlPullParserException("Unexpected Tag: " + parser.getEventType(), parser, null);
		do
		{
			if (!parser.isEmptyElementTag())
			{
				String name = parser.getName();
				if (name.equals("R"))
				{
					R = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("G"))
				{
					G = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("B"))
				{
					B = Helpers.TryParseFloat(parser.nextText().trim());
				}
				else if (name.equals("A"))
				{
					A = Helpers.TryParseFloat(parser.nextText().trim());
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
	 * Builds a color from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise
	 *            false. For example the color black (fully opaque) inverted
	 *            would be 0xFF 0xFF 0xFF 0x00
	 */
	public Color4(byte[] byteArray, int pos, boolean inverted)
	{
		R = G = B = A = 0f;
		FromBytes(byteArray, pos, inverted);
	}

	/**
	 * Returns the raw bytes for this vector
	 * 
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise
	 *            false. For example the color black (fully opaque) inverted
	 *            would be 0xFF 0xFF 0xFF 0x00
	 * @param alphaInverted
	 *            True if the alpha value is inverted in addition to whatever
	 *            the inverted parameter is. Setting inverted true and
	 *            alphaInverted true will flip the alpha value back to
	 *            non-inverted, but keep the other color bytes inverted
	 * @return A 16 byte array containing R, G, B, and A
	 */
	public Color4(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted)
	{
		R = G = B = A = 0f;
		FromBytes(byteArray, pos, inverted, alphaInverted);
	}

	/**
	 * Copy constructor
	 * 
	 * @param color
	 *            Color to copy
	 */
	public Color4(Color4 color)
	{
		R = color.R;
		G = color.G;
		B = color.B;
		A = color.A;
	}

	/**
	 * CompareTo implementation
	 * 
	 * Sorting ends up like this: |--Grayscale--||--Color--|. Alpha is only used
	 * when the colors are otherwise equivalent
	 */
	public int CompareTo(Color4 color)
	{
		float thisHue = GetHue();
		float thatHue = color.GetHue();

		if (thisHue < 0f && thatHue < 0f)
		{
			// Both monochromatic
			if (R == color.R)
			{
				// Monochromatic and equal, compare alpha
				return ((Float) A).compareTo(color.A);
			}

			// Compare lightness
			return ((Float) R).compareTo(R);
		}

		if (thisHue == thatHue)
		{
			// RGB is equal, compare alpha
			return ((Float) A).compareTo(color.A);
		}

		// Compare hues
		return ((Float) thisHue).compareTo(thatHue);
	}

	public void FromBytes(byte[] byteArray, int pos, boolean inverted)
	{
		final float quanta = 1.0f / 255.0f;

		if (inverted)
		{
			R = (255 - byteArray[pos]) * quanta;
			G = (255 - byteArray[pos + 1]) * quanta;
			B = (255 - byteArray[pos + 2]) * quanta;
			A = (255 - byteArray[pos + 3]) * quanta;
		}
		else
		{
			R = byteArray[pos] * quanta;
			G = byteArray[pos + 1] * quanta;
			B = byteArray[pos + 2] * quanta;
			A = byteArray[pos + 3] * quanta;
		}
	}

	/**
	 * Builds a color from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing a 16 byte color
	 * @param pos
	 *            Beginning position in the byte array
	 * @param inverted
	 *            True if the byte array stores inverted values, otherwise
	 *            false. For example the color black (fully opaque) inverted
	 *            would be 0xFF 0xFF 0xFF 0x00
	 * @param alphaInverted
	 *            True if the alpha value is inverted in addition to whatever
	 *            the inverted parameter is. Setting inverted true and
	 *            alphaInverted true will flip the alpha value back to
	 *            non-inverted, but keep the other color bytes inverted
	 */
	public void FromBytes(byte[] byteArray, int pos, boolean inverted, boolean alphaInverted)
	{
		FromBytes(byteArray, pos, inverted);

		if (alphaInverted)
		{
			A = 1.0f - A;
		}
	}

	public byte[] GetBytes()
	{
		return GetBytes(false);
	}

	public byte[] GetBytes(boolean inverted)
	{
		byte[] byteArray = new byte[4];
		ToBytes(byteArray, 0, inverted);
		return byteArray;
	}

	public byte[] GetFloatBytes()
	{
		byte[] bytes = new byte[16];
		ToFloatBytesL(bytes, 0);
		return bytes;
	}

	/**
	 * Writes the raw bytes for this color to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int ToBytes(byte[] dest, int pos)
	{
		return ToBytes(dest, pos, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @param inverted
	 *            True to invert the output (1.0 becomes 0 instead of 255)
	 * @return number of bytes filled to the byte array
	 */
	public int ToBytes(byte[] dest, int pos, boolean inverted)
	{
		dest[pos + 0] = Helpers.FloatToByte(R, 0f, 1f);
		dest[pos + 1] = Helpers.FloatToByte(G, 0f, 1f);
		dest[pos + 2] = Helpers.FloatToByte(B, 0f, 1f);
		dest[pos + 3] = Helpers.FloatToByte(A, 0f, 1f);

		if (inverted)
		{
			dest[pos + 0] = (byte) (255 - dest[pos + 0]);
			dest[pos + 1] = (byte) (255 - dest[pos + 1]);
			dest[pos + 2] = (byte) (255 - dest[pos + 2]);
			dest[pos + 3] = (byte) (255 - dest[pos + 3]);
		}
		return 4;
	}

	/**
	 * Writes the raw bytes for this color to a byte array in little endian
	 * format
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int ToFloatBytesL(byte[] dest, int pos)
	{
		Helpers.FloatToBytesL(R, dest, pos + 0);
		Helpers.FloatToBytesL(G, dest, pos + 4);
		Helpers.FloatToBytesL(B, dest, pos + 8);
		Helpers.FloatToBytesL(A, dest, pos + 12);
		return 4;
	}

	public float GetHue()
	{
		final float HUE_MAX = 360f;

		float max = Math.max(Math.max(R, G), B);
		float min = Math.min(Math.min(R, B), B);

		if (max == min)
		{
			// Achromatic, hue is undefined
			return -1f;
		}
		else if (R == max)
		{
			float bDelta = (((max - B) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float gDelta = (((max - G) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return bDelta - gDelta;
		}
		else if (G == max)
		{
			float rDelta = (((max - R) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float bDelta = (((max - B) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return (HUE_MAX / 3f) + rDelta - bDelta;
		}
		else
		// B == max
		{
			float gDelta = (((max - G) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			float rDelta = (((max - R) * (HUE_MAX / 6f)) + ((max - min) / 2f)) / (max - min);
			return ((2f * HUE_MAX) / 3f) + gDelta - rDelta;
		}
	}

	/** Ensures that values are in range 0-1 */
	public void ClampValues()
	{
		if (R < 0f)
		{
			R = 0f;
		}
		if (G < 0f)
		{
			G = 0f;
		}
		if (B < 0f)
		{
			B = 0f;
		}
		if (A < 0f)
		{
			A = 0f;
		}
		if (R > 1f)
		{
			R = 1f;
		}
		if (G > 1f)
		{
			G = 1f;
		}
		if (B > 1f)
		{
			B = 1f;
		}
		if (A > 1f)
		{
			A = 1f;
		}
	}

	/**
	 * Create an RGB color from a hue, saturation, value combination
	 * 
	 * @param hue
	 *            Hue
	 * @param saturation
	 *            Saturation
	 * @param value
	 *            Value
	 * @return An fully opaque RGB color (alpha is 1.0)
	 */
	public static Color4 FromHSV(double hue, double saturation, double value)
	{
		double r = 0d;
		double g = 0d;
		double b = 0d;

		if (saturation == 0d)
		{
			// If s is 0, all colors are the same.
			// This is some flavor of gray.
			r = value;
			g = value;
			b = value;
		}
		else
		{
			double p;
			double q;
			double t;

			double fractionalSector;
			int sectorNumber;
			double sectorPos;

			// The color wheel consists of 6 sectors.
			// Figure out which sector you//re in.
			sectorPos = hue / 60d;
			sectorNumber = (int) (Math.floor(sectorPos));

			// get the fractional part of the sector.
			// That is, how many degrees into the sector
			// are you?
			fractionalSector = sectorPos - sectorNumber;

			// Calculate values for the three axes
			// of the color.
			p = value * (1d - saturation);
			q = value * (1d - (saturation * fractionalSector));
			t = value * (1d - (saturation * (1d - fractionalSector)));

			// Assign the fractional colors to r, g, and b
			// based on the sector the angle is in.
			switch (sectorNumber)
			{
				case 0:
					r = value;
					g = t;
					b = p;
					break;
				case 1:
					r = q;
					g = value;
					b = p;
					break;
				case 2:
					r = p;
					g = value;
					b = t;
					break;
				case 3:
					r = p;
					g = q;
					b = value;
					break;
				case 4:
					r = t;
					g = p;
					b = value;
					break;
				case 5:
					r = value;
					g = p;
					b = q;
					break;
			}
		}

		return new Color4((float) r, (float) g, (float) b, 1f);
	}

	/**
	 * Performs linear interpolation between two colors
	 * 
	 * @param value1
	 *            Color to start at
	 * @param value2
	 *            Color to end at
	 * @param amount
	 *            Amount to interpolate
	 * @return The interpolated color
	 */
	public static Color4 Lerp(Color4 value1, Color4 value2, float amount)
	{
		return new Color4(Helpers.Lerp(value1.R, value2.R, amount), Helpers.Lerp(value1.G, value2.G, amount),
				Helpers.Lerp(value1.B, value2.B, amount), Helpers.Lerp(value1.A, value2.A, amount));
	}

	static public Color4 parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Color4(parser);
	}
	
	public void serialize(XmlSerializer writer) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(null, "R").text(Float.toString(R)).endTag(null, "R");
		writer.startTag(null, "G").text(Float.toString(G)).endTag(null, "G");
		writer.startTag(null, "B").text(Float.toString(B)).endTag(null, "B");
		writer.startTag(null, "A").text(Float.toString(A)).endTag(null, "A");
	}

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f>", R, G, B, A);
	}

	public String ToRGBString()
	{
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f>", R, G, B);
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Color4) ? this == (Color4) obj : false;
	}

	public boolean equals(Color4 other)
	{
		return equals(this, other);
	}

	@Override
	public int hashCode()
	{
		return ((Float) R).hashCode() ^ ((Float) G).hashCode() ^ ((Float) B).hashCode() ^ ((Float) A).hashCode();
	}

	public static boolean equals(Color4 lhs, Color4 rhs)
	{
		return (lhs.R == rhs.R) && (lhs.G == rhs.G) && (lhs.B == rhs.B) && (lhs.A == rhs.A);
	}

	public static Color4 add(Color4 lhs, Color4 rhs)
	{
		lhs.R += rhs.R;
		lhs.G += rhs.G;
		lhs.B += rhs.B;
		lhs.A += rhs.A;
		lhs.ClampValues();

		return lhs;
	}

	public static Color4 minus(Color4 lhs, Color4 rhs)
	{
		lhs.R -= rhs.R;
		lhs.G -= rhs.G;
		lhs.B -= rhs.B;
		lhs.A -= rhs.A;
		lhs.ClampValues();

		return lhs;
	}

	public static Color4 multiply(Color4 lhs, Color4 rhs)
	{
		lhs.R *= rhs.R;
		lhs.G *= rhs.G;
		lhs.B *= rhs.B;
		lhs.A *= rhs.A;
		lhs.ClampValues();

		return lhs;
	}

	/** A Color4 with zero RGB values and fully opaque (alpha 1.0) */
	public final static Color4 Black = new Color4(0f, 0f, 0f, 1f);

	/** A Color4 with full RGB values (1.0) and fully opaque (alpha 1.0) */
	public final static Color4 White = new Color4(1f, 1f, 1f, 1f);
}
