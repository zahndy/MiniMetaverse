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

public class GroupAccountTransactionsRequestPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID GroupID = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            GroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class MoneyDataBlock
    {
        public UUID RequestID = null;
        public int IntervalDays = 0;
        public int CurrentInterval = 0;

        public int getLength(){
            return 24;
        }

        public MoneyDataBlock() { }
        public MoneyDataBlock(ByteBuffer bytes)
        {
            RequestID = new UUID(bytes);
            IntervalDays = bytes.getInt();
            CurrentInterval = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RequestID.GetBytes(bytes);
            bytes.putInt(IntervalDays);
            bytes.putInt(CurrentInterval);
        }

        @Override
        public String toString()
        {
            String output = "-- MoneyData --\n";
            try {
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "IntervalDays: " + Integer.toString(IntervalDays) + "\n";
                output += "CurrentInterval: " + Integer.toString(CurrentInterval) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MoneyDataBlock createMoneyDataBlock() {
         return new MoneyDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupAccountTransactionsRequest; }
    public AgentDataBlock AgentData;
    public MoneyDataBlock MoneyData;

    public GroupAccountTransactionsRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)357);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MoneyData = new MoneyDataBlock();
    }

    public GroupAccountTransactionsRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
     }

    public GroupAccountTransactionsRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += MoneyData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        MoneyData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupAccountTransactionsRequest ---\n";
        output += AgentData.toString() + "\n";
        output += MoneyData.toString() + "\n";
        return output;
    }
}