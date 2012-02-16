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

public class CopyInventoryItemPacket extends Packet
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
        public int CallbackID = 0;
        public UUID OldAgentID = null;
        public UUID OldItemID = null;
        public UUID NewFolderID = null;
        private byte[] _newname;
        public byte[] getNewName() {
            return _newname;
        }

        public void setNewName(byte[] value) throws Exception {
            if (value == null) {
                _newname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _newname = new byte[value.length];
                System.arraycopy(value, 0, _newname, 0, value.length);
            }
        }


        public int getLength(){
            int length = 52;
            if (getNewName() != null) { length += 1 + getNewName().length; }
            return length;
        }

        public InventoryDataBlock() { }
        public InventoryDataBlock(ByteBuffer bytes)
        {
            int length;
            CallbackID = bytes.getInt();
            OldAgentID = new UUID(bytes);
            OldItemID = new UUID(bytes);
            NewFolderID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _newname = new byte[length];
            bytes.get(_newname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(CallbackID);
            OldAgentID.GetBytes(bytes);
            OldItemID.GetBytes(bytes);
            NewFolderID.GetBytes(bytes);
            bytes.put((byte)_newname.length);
            bytes.put(_newname);
        }

        @Override
        public String toString()
        {
            String output = "-- InventoryData --\n";
            try {
                output += "CallbackID: " + Integer.toString(CallbackID) + "\n";
                output += "OldAgentID: " + OldAgentID.toString() + "\n";
                output += "OldItemID: " + OldItemID.toString() + "\n";
                output += "NewFolderID: " + NewFolderID.toString() + "\n";
                output += Helpers.FieldToString(_newname, "NewName") + "\n";
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
    public PacketType getType() { return PacketType.CopyInventoryItem; }
    public AgentDataBlock AgentData;
    public InventoryDataBlock[] InventoryData;

    public CopyInventoryItemPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)269);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        InventoryData = new InventoryDataBlock[0];
    }

    public CopyInventoryItemPacket(ByteBuffer bytes) throws Exception
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

    public CopyInventoryItemPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- CopyInventoryItem ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < InventoryData.length; j++)
        {
            output += InventoryData[j].toString() + "\n";
        }
        return output;
    }
}
