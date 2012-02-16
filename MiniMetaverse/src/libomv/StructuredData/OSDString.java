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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import libomv.types.UUID;
import libomv.utils.Helpers;

public class OSDString extends OSD
{
	private String value;

	@Override
	public OSDType getType()
	{
		return OSDType.String;
	}

	public OSDString(String value)
	{
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = Helpers.EmptyString;
	}

	@Override
	public boolean AsBoolean()
	{
		if (value == null || value.isEmpty())
			return false;

		if (Double.parseDouble(value) == 0.0 || value.toLowerCase().equals("false"))
			return false;

		return true;
	}

	@Override
	public int AsInteger()
	{
		try
		{
			return (int) Math.floor(Double.parseDouble(value));
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public int AsUInteger()
	{
		try
		{
			return ((int) Math.floor(Double.parseDouble(value)) & 0xffffffff);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public long AsLong()
	{
		try
		{
			return (long) Math.floor(Double.parseDouble(value));
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public long AsULong()
	{
		try
		{
			return ((long) Math.floor(Double.parseDouble(value)) & 0xffffffffffffffffl);
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public double AsReal()
	{
		try
		{
			return Math.floor(Double.parseDouble(value));
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public String AsString()
	{
		return value;
	}

	@Override
	public byte[] AsBinary()
	{
		return Helpers.StringToBytes(value);
	}

	@Override
	public InetAddress AsInetAddress()
	{
		try
		{
			int i = value.indexOf(':');
			if (i < 0)
				return InetAddress.getByName(value);
			return InetAddress.getByName(value.substring(0, i));
		}
		catch (UnknownHostException ex)
		{
			return null;
		}
	}

	@Override
	public UUID AsUUID()
	{
		return new UUID(value);
	}

	@Override
	public Date AsDate()
	{
		SimpleDateFormat df = new SimpleDateFormat(FRACT_DATE_FMT);
		try
		{
			return df.parse(value);
		}
		catch (ParseException ex1)
		{
			try
			{
				df.applyPattern(WHOLE_DATE_FMT);
				return df.parse(value);
			}
			catch (ParseException ex2)
			{
				return Helpers.Epoch;
			}
		}
	}

	@Override
	public URI AsUri()
	{
		try
		{
			return new URI(value);
		}
		catch (URISyntaxException ex)
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		return AsString();
	}
}
