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

public class DeRezObjectPacket extends Packet
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

    public class AgentBlockBlock
    {
        public UUID GroupID = null;
        public byte Destination = 0;
        public UUID DestinationID = null;
        public UUID TransactionID = null;
        public byte PacketCount = 0;
        public byte PacketNumber = 0;

        public int getLength(){
            return 51;
        }

        public AgentBlockBlock() { }
        public AgentBlockBlock(ByteBuffer bytes)
        {
            GroupID = new UUID(bytes);
            Destination = bytes.get();
            DestinationID = new UUID(bytes);
            TransactionID = new UUID(bytes);
            PacketCount = bytes.get();
            PacketNumber = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            bytes.put(Destination);
            DestinationID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
            bytes.put(PacketCount);
            bytes.put(PacketNumber);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentBlock --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "Destination: " + Byte.toString(Destination) + "\n";
                output += "DestinationID: " + DestinationID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "PacketCount: " + Byte.toString(PacketCount) + "\n";
                output += "PacketNumber: " + Byte.toString(PacketNumber) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentBlockBlock createAgentBlockBlock() {
         return new AgentBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.DeRezObject; }
    public AgentDataBlock AgentData;
    public AgentBlockBlock AgentBlock;
    public int[] ObjectLocalID;

    public DeRezObjectPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)291);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        AgentBlock = new AgentBlockBlock();
        ObjectLocalID = new int[0];
    }

    public DeRezObjectPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        AgentBlock = new AgentBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectLocalID = new int[count];
        for (int j = 0; j < count; j++)
        {
            ObjectLocalID[j] = bytes.getInt();
        }
     }

    public DeRezObjectPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        AgentBlock = new AgentBlockBlock(bytes);
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
        length += AgentBlock.getLength();
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
        AgentBlock.ToBytes(bytes);
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
        String output = "--- DeRezObject ---\n";
        output += AgentData.toString() + "\n";
        output += AgentBlock.toString() + "\n";
        for (int j = 0; j < ObjectLocalID.length; j++)
        {
            output += "ObjectLocalID[" + j + "]: " + Integer.toString(ObjectLocalID[j]) + "\n";
        }
        return output;
    }
}
