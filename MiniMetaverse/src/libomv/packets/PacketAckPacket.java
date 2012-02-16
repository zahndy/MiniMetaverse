/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met;
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
package libomv.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;

public class PacketAckPacket extends Packet
{
    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.PacketAck; }
    public int[] ID;

    public PacketAckPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)65531);
        header.setReliable(true);
        ID = new int[0];
    }

    public PacketAckPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        ID = new int[count];
        for (int j = 0; j < count; j++)
        {
            ID[j] = bytes.getInt();
        }
     }

    public PacketAckPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        ID = new int[count];
        for (int j = 0; j < count; j++)
        {
            ID[j] = bytes.getInt();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        length += ID.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)ID.length);
        for (int j = 0; j < ID.length; j++)
        {
            bytes.putInt(ID[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PacketAck ---\n";
        for (int j = 0; j < ID.length; j++)
        {
            output += "ID[" + j + "]: " + Integer.toString(ID[j]) + "\n";
        }
        return output;
    }
}