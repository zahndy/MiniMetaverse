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

import libomv.utils.Helpers;

public class OSDReal extends OSD
{
	private double value;

	@Override
	public OSDType getType()
	{
		return OSDType.Real;
	}

	public OSDReal(double value)
	{
		this.value = value;
	}

	@Override
	public boolean AsBoolean()
	{
		return (!Double.isNaN(value) && value != 0d);
	}

	@Override
	public int AsInteger()
	{
		if (Double.isNaN(value))
		{
			return 0;
		}
		if (value > Integer.MAX_VALUE)
		{
			return Integer.MAX_VALUE;
		}
		if (value < Integer.MIN_VALUE)
		{
			return Integer.MIN_VALUE;
		}
		return (int) Math.round(value);
	}

	@Override
	public int AsUInteger()
	{
		if (Double.isNaN(value))
		{
			return 0;
		}
		if (value > (2 * (double) Integer.MAX_VALUE + 1))
		{
			return (0xffffffff);
		}
		if (value < 0.0f)
		{
			return 0;
		}
		return (int) Math.round(value);
	}

	@Override
	public long AsLong()
	{
		if (Double.isNaN(value))
		{
			return 0;
		}
		if (value > Long.MAX_VALUE)
		{
			return Long.MAX_VALUE;
		}
		if (value < Long.MIN_VALUE)
		{
			return Long.MIN_VALUE;
		}
		return Math.round(value);
	}

	@Override
	public long AsULong()
	{
		if (Double.isNaN(value))
		{
			return 0;
		}
		if (value > (2 * (double) Long.MAX_VALUE + 1))
		{
			return 0xffffffffffl;
		}
		if (value < 0.0d)
		{
			return 0;
		}
		return Math.round(value);
	}

	@Override
	public double AsReal()
	{
		return value;
	}

	@Override
	public String AsString()
	{
		return Double.toString(value);
	}

	@Override
	public byte[] AsBinary()
	{
		return Helpers.DoubleToBytesB(value);
	}

	@Override
	public String toString()
	{
		return AsString();
	}
}
