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

import libomv.utils.Helpers;
import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class MoneyTransferRequestPacket extends Packet
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

    public class MoneyDataBlock
    {
        public UUID SourceID = null;
        public UUID DestID = null;
        public byte Flags = 0;
        public int Amount = 0;
        public byte AggregatePermNextOwner = 0;
        public byte AggregatePermInventory = 0;
        public int TransactionType = 0;
        private byte[] _description;
        public byte[] getDescription() {
            return _description;
        }

        public void setDescription(byte[] value) throws Exception {
            if (value == null) {
                _description = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _description = new byte[value.length];
                System.arraycopy(value, 0, _description, 0, value.length);
            }
        }


        public int getLength(){
            int length = 43;
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public MoneyDataBlock() { }
        public MoneyDataBlock(ByteBuffer bytes)
        {
            int length;
            SourceID = new UUID(bytes);
            DestID = new UUID(bytes);
            Flags = bytes.get();
            Amount = bytes.getInt();
            AggregatePermNextOwner = bytes.get();
            AggregatePermInventory = bytes.get();
            TransactionType = bytes.getInt();
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            SourceID.GetBytes(bytes);
            DestID.GetBytes(bytes);
            bytes.put(Flags);
            bytes.putInt(Amount);
            bytes.put(AggregatePermNextOwner);
            bytes.put(AggregatePermInventory);
            bytes.putInt(TransactionType);
            bytes.put((byte)_description.length);
            bytes.put(_description);
        }

        @Override
        public String toString()
        {
            String output = "-- MoneyData --\n";
            try {
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "DestID: " + DestID.toString() + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
                output += "Amount: " + Integer.toString(Amount) + "\n";
                output += "AggregatePermNextOwner: " + Byte.toString(AggregatePermNextOwner) + "\n";
                output += "AggregatePermInventory: " + Byte.toString(AggregatePermInventory) + "\n";
                output += "TransactionType: " + Integer.toString(TransactionType) + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
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
    public PacketType getType() { return PacketType.MoneyTransferRequest; }
    public AgentDataBlock AgentData;
    public MoneyDataBlock MoneyData;

    public MoneyTransferRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)311);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MoneyData = new MoneyDataBlock();
    }

    public MoneyTransferRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
     }

    public MoneyTransferRequestPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- MoneyTransferRequest ---\n";
        output += AgentData.toString() + "\n";
        output += MoneyData.toString() + "\n";
        return output;
    }
}
