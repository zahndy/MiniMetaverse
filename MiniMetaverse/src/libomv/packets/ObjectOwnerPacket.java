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

public class ObjectOwnerPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class HeaderDataBlock
    {
        public boolean Override = false;
        public UUID OwnerID = null;
        public UUID GroupID = null;

        public int getLength(){
            return 33;
        }

        public HeaderDataBlock() { }
        public HeaderDataBlock(ByteBuffer bytes)
        {
            Override = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            OwnerID = new UUID(bytes);
            GroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((Override) ? 1 : 0));
            OwnerID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- HeaderData --\n";
            try {
                output += "Override: " + Boolean.toString(Override) + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public HeaderDataBlock createHeaderDataBlock() {
         return new HeaderDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ObjectOwner; }
    public AgentDataBlock AgentData;
    public HeaderDataBlock HeaderData;
    public int[] ObjectLocalID;

    public ObjectOwnerPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)100);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        HeaderData = new HeaderDataBlock();
        ObjectLocalID = new int[0];
    }

    public ObjectOwnerPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        HeaderData = new HeaderDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectLocalID = new int[count];
        for (int j = 0; j < count; j++)
        {
            ObjectLocalID[j] = bytes.getInt();
        }
     }

    public ObjectOwnerPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        HeaderData = new HeaderDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectLocalID = new int[count];
        for (int j = 0; j < count; j++)
        {
            ObjectLocalID[j] = bytes.getInt();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += HeaderData.getLength();
        length++;
        length += ObjectLocalID.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        HeaderData.ToBytes(bytes);
        bytes.put((byte)ObjectLocalID.length);
        for (int j = 0; j < ObjectLocalID.length; j++)
        {
            bytes.putInt(ObjectLocalID[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ObjectOwner ---\n";
        output += AgentData.toString() + "\n";
        output += HeaderData.toString() + "\n";
        for (int j = 0; j < ObjectLocalID.length; j++)
        {
            output += "ObjectLocalID[" + j + "]: " + Integer.toString(ObjectLocalID[j]) + "\n";
        }
        return output;
    }
}
