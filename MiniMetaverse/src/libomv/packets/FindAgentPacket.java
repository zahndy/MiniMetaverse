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

public class FindAgentPacket extends Packet
{
    public class AgentBlockBlock
    {
        public UUID Hunter = null;
        public UUID Prey = null;
        public int SpaceIP = 0;

        public int getLength(){
            return 36;
        }

        public AgentBlockBlock() { }
        public AgentBlockBlock(ByteBuffer bytes)
        {
            Hunter = new UUID(bytes);
            Prey = new UUID(bytes);
            SpaceIP = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            Hunter.GetBytes(bytes);
            Prey.GetBytes(bytes);
            bytes.putInt(SpaceIP);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentBlock --\n";
            try {
                output += "Hunter: " + Hunter.toString() + "\n";
                output += "Prey: " + Prey.toString() + "\n";
                output += "SpaceIP: " + Integer.toString(SpaceIP) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentBlockBlock createAgentBlockBlock() {
         return new AgentBlockBlock();
    }

    public class LocationBlockBlock
    {
        public double GlobalX = 0;
        public double GlobalY = 0;

        public int getLength(){
            return 16;
        }

        public LocationBlockBlock() { }
        public LocationBlockBlock(ByteBuffer bytes)
        {
            GlobalX = bytes.getDouble();
            GlobalY = bytes.getDouble();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putDouble(GlobalX);
            bytes.putDouble(GlobalY);
        }

        @Override
        public String toString()
        {
            String output = "-- LocationBlock --\n";
            try {
                output += "GlobalX: " + Double.toString(GlobalX) + "\n";
                output += "GlobalY: " + Double.toString(GlobalY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public LocationBlockBlock createLocationBlockBlock() {
         return new LocationBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.FindAgent; }
    public AgentBlockBlock AgentBlock;
    public LocationBlockBlock[] LocationBlock;

    public FindAgentPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)256);
        header.setReliable(true);
        AgentBlock = new AgentBlockBlock();
        LocationBlock = new LocationBlockBlock[0];
    }

    public FindAgentPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentBlock = new AgentBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        LocationBlock = new LocationBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            LocationBlock[j] = new LocationBlockBlock(bytes);
        }
     }

    public FindAgentPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentBlock = new AgentBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        LocationBlock = new LocationBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            LocationBlock[j] = new LocationBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentBlock.getLength();
        length++;
        for (int j = 0; j < LocationBlock.length; j++) { length += LocationBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentBlock.ToBytes(bytes);
        bytes.put((byte)LocationBlock.length);
        for (int j = 0; j < LocationBlock.length; j++) { LocationBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- FindAgent ---\n";
        output += AgentBlock.toString() + "\n";
        for (int j = 0; j < LocationBlock.length; j++)
        {
            output += LocationBlock[j].toString() + "\n";
        }
        return output;
    }
}
