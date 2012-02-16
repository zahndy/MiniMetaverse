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

public class FetchInventoryPacket extends Packet
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

    public class InventoryDataBlock
    {
        public UUID OwnerID = null;
        public UUID ItemID = null;

        public int getLength(){
            return 32;
        }

        public InventoryDataBlock() { }
        public InventoryDataBlock(ByteBuffer bytes)
        {
            OwnerID = new UUID(bytes);
            ItemID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            OwnerID.GetBytes(bytes);
            ItemID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- InventoryData --\n";
            try {
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "ItemID: " + ItemID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InventoryDataBlock createInventoryDataBlock() {
         return new InventoryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.FetchInventory; }
    public AgentDataBlock AgentData;
    public InventoryDataBlock[] InventoryData;

    public FetchInventoryPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)279);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        InventoryData = new InventoryDataBlock[0];
    }

    public FetchInventoryPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        InventoryData = new InventoryDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            InventoryData[j] = new InventoryDataBlock(bytes);
        }
     }

    public FetchInventoryPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        InventoryData = new InventoryDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            InventoryData[j] = new InventoryDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < InventoryData.length; j++) { length += InventoryData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)InventoryData.length);
        for (int j = 0; j < InventoryData.length; j++) { InventoryData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- FetchInventory ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < InventoryData.length; j++)
        {
            output += InventoryData[j].toString() + "\n";
        }
        return output;
    }
}