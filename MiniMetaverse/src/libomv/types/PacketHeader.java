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

import java.nio.ByteBuffer;

import libomv.utils.Helpers;

public class PacketHeader
{
	public byte[] Data = null;
	public byte[] Extra = null;
	private final byte fixedLen = 6;
	private byte frequency;
	private byte length;

	public byte getFlags()
	{
		return Data[0];
	}

	public void setFlags(byte value)
	{
		Data[0] = value;
	}

	public boolean getReliable()
	{
		return (Data[0] & Helpers.MSG_RELIABLE) != 0;
	}

	public void setReliable(boolean value)
	{
		if (value)
		{
			Data[0] |= Helpers.MSG_RELIABLE;
		}
		else
		{
			Data[0] -= Helpers.MSG_RELIABLE;
		}
	}

	public boolean getResent()
	{
		return (Data[0] & Helpers.MSG_RESENT) != 0;
	}

	public void setResent(boolean value)
	{
		if (value)
		{
			Data[0] |= Helpers.MSG_RESENT;
		}
		else
		{
			Data[0] -= Helpers.MSG_RESENT;
		}
	}

	public boolean getZerocoded()
	{
		return (Data[0] & Helpers.MSG_ZEROCODED) != 0;
	}

	public void setZerocoded(boolean value)
	{
		if (value)
		{
			Data[0] |= Helpers.MSG_ZEROCODED;
		}
		else
		{
			Data[0] -= Helpers.MSG_ZEROCODED;
		}
	}

	public boolean getAppendedAcks()
	{
		return (Data[0] & Helpers.MSG_APPENDED_ACKS) != 0;
	}

	public int getSequence()
	{
		return (((Data[1] & 0xff) >> 24) + ((Data[2] & 0xff) << 16) + ((Data[3] & 0xff) << 8) + ((Data[4] & 0xff) << 0));
	}

	public int getExtraLength()
	{
		return Data[5];
	}

	public short getID()
	{
		switch (frequency)
		{
			case PacketFrequency.Low:
				return (short)(((Data[8 + getExtraLength()] & 0xFF) << 8) + ((Data[9 + getExtraLength()] & 0xff) << 0));
			case PacketFrequency.Medium:
				return Data[7];
			case PacketFrequency.High:
				return Data[6];
		}
		return 0;
	}

	public void setID(int value)
	{
		switch (frequency)
		{
			case PacketFrequency.Low:
				Data[8 + getExtraLength()] = (byte) ((value >> 8) & 0xFF);
				Data[9 + getExtraLength()] = (byte) ((value >> 0) & 0xFF);
				break;
			case PacketFrequency.Medium:
				Data[7] = (byte) (value & 0xFF);
				break;
			case PacketFrequency.High:
				Data[6] = (byte) (value & 0xFF);
				break;
		}
	}

	public byte getFrequency()
	{
		return frequency;
	}

	private void setFrequency(byte frequency)
	{
		this.frequency = frequency;
		switch (frequency)
		{
			case PacketFrequency.Low:
				this.length = 10;
				break;
			case PacketFrequency.Medium:
				this.length = 8;
				break;
			case PacketFrequency.High:
				this.length = 7;
				break;
		}

	}

	private void BuildHeader(ByteBuffer bytes) throws Exception
	{
		if (bytes.limit() < this.length)
		{
			throw new Exception("Not enough bytes for " + PacketFrequency.Names[frequency] + "Header");
		}
		Data = new byte[this.length];
		bytes.get(Data, 0, fixedLen);
		int extra = getExtraLength();
		if (extra > 0)
		{
			Extra = new byte[extra];
			bytes.get(Extra, 0, extra);
		}
		bytes.get(Data, fixedLen, getLength() - fixedLen);
	}

	// Constructors
	public PacketHeader(byte frequency)
	{
		setFrequency(frequency);
		Data = new byte[this.length];
		Data[5] = (byte) 0;
		switch (frequency)
		{
			case PacketFrequency.Low:
				Data[7] = (byte) 0xFF;
			case PacketFrequency.Medium:
				Data[6] = (byte) 0xFF;
		}
	}

	public PacketHeader(ByteBuffer bytes, byte frequency) throws Exception
	{
		setFrequency(frequency);
		BuildHeader(bytes);
		CreateAckList(bytes);
	}

	public PacketHeader(ByteBuffer bytes) throws Exception
	{
		if (bytes.get(6) == (byte) 0xFF)
		{
			if (bytes.get(7) == (byte) 0xFF)
			{
				setFrequency(PacketFrequency.Low);
			}
			else
			{
				setFrequency(PacketFrequency.Medium);
			}
		}
		else
		{
			setFrequency(PacketFrequency.High);
		}
		BuildHeader(bytes);
		CreateAckList(bytes);
	}

	public byte getLength()
	{
		return length;
	}

	public void ToBytes(ByteBuffer bytes)
	{
		bytes.put(Data, 0, fixedLen - 1);
		if (Extra == null)
		{
			bytes.put((byte) 0);
		}
		else
		{
			bytes.put((byte) (Extra.length & 0xFF));
			bytes.put(Extra);
		}
		bytes.put(Data, fixedLen, getLength() - fixedLen);
	}

	public int[] AckList = null;

	private void CreateAckList(ByteBuffer bytes)
	{
		if (getAppendedAcks())
		{
			int packetEnd = bytes.limit() - 1;
			AckList = new int[bytes.get(packetEnd)];
			byte[] array = bytes.array();

			for (int i = AckList.length; i > 0;)
			{
				packetEnd -= 4;
				AckList[--i] = (int) Helpers.BytesToUInt32B(array, packetEnd);
			}
			bytes.limit(packetEnd);
		}
	}
}
