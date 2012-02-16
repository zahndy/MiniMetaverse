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
import libomv.types.UUID;

public class MergeParcelPacket extends Packet
{
    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MergeParcel; }
    public UUID MasterID;
    public UUID[] SlaveID;

    public MergeParcelPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)223);
        header.setReliable(true);
        MasterID = new UUID();
        SlaveID = new UUID[0];
    }

    public MergeParcelPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        MasterID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        SlaveID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            SlaveID[j] = new UUID(bytes);
        }
     }

    public MergeParcelPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        MasterID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        SlaveID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            SlaveID[j] = new UUID(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        length += SlaveID.length * 16;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        MasterID.GetBytes(bytes);
        bytes.put((byte)SlaveID.length);
        for (int j = 0; j < SlaveID.length; j++)
        {
            SlaveID[j].GetBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MergeParcel ---\n";
        output += "MasterID: " + MasterID.toString() + "\n";
        for (int j = 0; j < SlaveID.length; j++)
        {
            output += "SlaveID[" + j + "]: " + SlaveID[j].toString() + "\n";
        }
        return output;
    }
}
