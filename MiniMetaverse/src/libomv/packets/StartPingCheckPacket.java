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

public class StartPingCheckPacket extends Packet
{
    public class PingIDBlock
    {
        public byte PingID = 0;
        public int OldestUnacked = 0;

        public int getLength(){
            return 5;
        }

        public PingIDBlock() { }
        public PingIDBlock(ByteBuffer bytes)
        {
            PingID = bytes.get();
            OldestUnacked = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(PingID);
            bytes.putInt(OldestUnacked);
        }

        @Override
        public String toString()
        {
            String output = "-- PingID --\n";
            try {
                output += "PingID: " + Byte.toString(PingID) + "\n";
                output += "OldestUnacked: " + Integer.toString(OldestUnacked) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public PingIDBlock createPingIDBlock() {
         return new PingIDBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.StartPingCheck; }
    public PingIDBlock PingID;

    public StartPingCheckPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)1);
        header.setReliable(true);
        PingID = new PingIDBlock();
    }

    public StartPingCheckPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        PingID = new PingIDBlock(bytes);
     }

    public StartPingCheckPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        PingID = new PingIDBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += PingID.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        PingID.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- StartPingCheck ---\n";
        output += PingID.toString() + "\n";
        return output;
    }
}