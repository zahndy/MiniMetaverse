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

public class RezScriptPacket extends Packet
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

    public class UpdateBlockBlock
    {
        public int ObjectLocalID = 0;
        public boolean Enabled = false;

        public int getLength(){
            return 5;
        }

        public UpdateBlockBlock() { }
        public UpdateBlockBlock(ByteBuffer bytes)
        {
            ObjectLocalID = bytes.getInt();
            Enabled = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(ObjectLocalID);
            bytes.put((byte)((Enabled) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- UpdateBlock --\n";
            try {
                output += "ObjectLocalID: " + Integer.toString(ObjectLocalID) + "\n";
                output += "Enabled: " + Boolean.toString(Enabled) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public UpdateBlockBlock createUpdateBlockBlock() {
         return new UpdateBlockBlock();
    }

    public class InventoryBlockBlock
    {
        public UUID ItemID = null;
        public UUID FolderID = null;
        public UUID CreatorID = null;
        public UUID OwnerID = null;
        public UUID GroupID = null;
        public int BaseMask = 0;
        public int OwnerMask = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;
        public boolean GroupOwned = false;
        public UUID TransactionID = null;
        public byte Type = 0;
        public byte InvType = 0;
        public int Flags = 0;
        public byte SaleType = 0;
        public int SalePrice = 0;
        private byte[] _name;
        public byte[] getName() {
            return _name;
        }

        public void setName(byte[] value) throws Exception {
            if (value == null) {
                _name = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _name = new byte[value.length];
                System.arraycopy(value, 0, _name, 0, value.length);
            }
        }

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

        public int CreationDate = 0;
        public int CRC = 0;

        public int getLength(){
            int length = 136;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public InventoryBlockBlock() { }
        public InventoryBlockBlock(ByteBuffer bytes)
        {
            int length;
            ItemID = new UUID(bytes);
            FolderID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            GroupID = new UUID(bytes);
            BaseMask = bytes.getInt();
            OwnerMask = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
            GroupOwned = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            TransactionID = new UUID(bytes);
            Type = bytes.get();
            InvType = bytes.get();
            Flags = bytes.getInt();
            SaleType = bytes.get();
            SalePrice = bytes.getInt();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
            CreationDate = bytes.getInt();
            CRC = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ItemID.GetBytes(bytes);
            FolderID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.putInt(BaseMask);
            bytes.putInt(OwnerMask);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
            bytes.put((byte)((GroupOwned) ? 1 : 0));
            TransactionID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put(InvType);
            bytes.putInt(Flags);
            bytes.put(SaleType);
            bytes.putInt(SalePrice);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_description.length);
            bytes.put(_description);
            bytes.putInt(CreationDate);
            bytes.putInt(CRC);
        }

        @Override
        public String toString()
        {
            String output = "-- InventoryBlock --\n";
            try {
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "FolderID: " + FolderID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "BaseMask: " + Integer.toString(BaseMask) + "\n";
                output += "OwnerMask: " + Integer.toString(OwnerMask) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output += "GroupOwned: " + Boolean.toString(GroupOwned) + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "InvType: " + Byte.toString(InvType) + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output += "SaleType: " + Byte.toString(SaleType) + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output += "CreationDate: " + Integer.toString(CreationDate) + "\n";
                output += "CRC: " + Integer.toString(CRC) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InventoryBlockBlock createInventoryBlockBlock() {
         return new InventoryBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RezScript; }
    public AgentDataBlock AgentData;
    public UpdateBlockBlock UpdateBlock;
    public InventoryBlockBlock InventoryBlock;

    public RezScriptPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)304);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        UpdateBlock = new UpdateBlockBlock();
        InventoryBlock = new InventoryBlockBlock();
    }

    public RezScriptPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        UpdateBlock = new UpdateBlockBlock(bytes);
        InventoryBlock = new InventoryBlockBlock(bytes);
     }

    public RezScriptPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        UpdateBlock = new UpdateBlockBlock(bytes);
        InventoryBlock = new InventoryBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += UpdateBlock.getLength();
        length += InventoryBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        UpdateBlock.ToBytes(bytes);
        InventoryBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RezScript ---\n";
        output += AgentData.toString() + "\n";
        output += UpdateBlock.toString() + "\n";
        output += InventoryBlock.toString() + "\n";
        return output;
    }
}
