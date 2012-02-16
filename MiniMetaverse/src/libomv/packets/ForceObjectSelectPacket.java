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

public class ForceObjectSelectPacket extends Packet
{
    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ForceObjectSelect; }
    public boolean ResetList;
    public int[] LocalID;

    public ForceObjectSelectPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)205);
        header.setReliable(true);
        LocalID = new int[0];
    }

    public ForceObjectSelectPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ResetList = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        int count = bytes.get() & 0xFF;
        LocalID = new int[count];
        for (int j = 0; j < count; j++)
        {
            LocalID[j] = bytes.getInt();
        }
     }

    public ForceObjectSelectPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ResetList = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        int count = bytes.get() & 0xFF;
        LocalID = new int[count];
        for (int j = 0; j < count; j++)
        {
            LocalID[j] = bytes.getInt();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 1;
        length++;
        length += LocalID.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)((ResetList) ? 1 : 0));
        bytes.put((byte)LocalID.length);
        for (int j = 0; j < LocalID.length; j++)
        {
            bytes.putInt(LocalID[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ForceObjectSelect ---\n";
        output += "ResetList: " + Boolean.toString(ResetList) + "\n";
        for (int j = 0; j < LocalID.length; j++)
        {
            output += "LocalID[" + j + "]: " + Integer.toString(LocalID[j]) + "\n";
        }
        return output;
    }
}