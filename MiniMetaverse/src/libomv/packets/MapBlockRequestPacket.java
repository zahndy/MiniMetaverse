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

public class MapBlockRequestPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public int Flags = 0;
        public int EstateID = 0;
        public boolean Godlike = false;

        public int getLength(){
            return 41;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            Flags = bytes.getInt();
            EstateID = bytes.getInt();
            Godlike = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            bytes.putInt(Flags);
            bytes.putInt(EstateID);
            bytes.put((byte)((Godlike) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "Godlike: " + Boolean.toString(Godlike) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class PositionDataBlock
    {
        public short MinX = 0;
        public short MaxX = 0;
        public short MinY = 0;
        public short MaxY = 0;

        public int getLength(){
            return 8;
        }

        public PositionDataBlock() { }
        public PositionDataBlock(ByteBuffer bytes)
        {
            MinX = bytes.getShort();
            MaxX = bytes.getShort();
            MinY = bytes.getShort();
            MaxY = bytes.getShort();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort(MinX);
            bytes.putShort(MaxX);
            bytes.putShort(MinY);
            bytes.putShort(MaxY);
        }

        @Override
        public String toString()
        {
            String output = "-- PositionData --\n";
            try {
                output += "MinX: " + Short.toString(MinX) + "\n";
                output += "MaxX: " + Short.toString(MaxX) + "\n";
                output += "MinY: " + Short.toString(MinY) + "\n";
                output += "MaxY: " + Short.toString(MaxY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public PositionDataBlock createPositionDataBlock() {
         return new PositionDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MapBlockRequest; }
    public AgentDataBlock AgentData;
    public PositionDataBlock PositionData;

    public MapBlockRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)407);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        PositionData = new PositionDataBlock();
    }

    public MapBlockRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        PositionData = new PositionDataBlock(bytes);
     }

    public MapBlockRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        PositionData = new PositionDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += PositionData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        PositionData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MapBlockRequest ---\n";
        output += AgentData.toString() + "\n";
        output += PositionData.toString() + "\n";
        return output;
    }
}
