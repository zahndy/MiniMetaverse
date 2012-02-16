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

public class BulkUpdateInventoryPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID TransactionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class FolderDataBlock
    {
        public UUID FolderID = null;
        public UUID ParentID = null;
        public byte Type = 0;
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


        public int getLength(){
            int length = 33;
            if (getName() != null) { length += 1 + getName().length; }
            return length;
        }

        public FolderDataBlock() { }
        public FolderDataBlock(ByteBuffer bytes)
        {
            int length;
            FolderID = new UUID(bytes);
            ParentID = new UUID(bytes);
            Type = bytes.get();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            FolderID.GetBytes(bytes);
            ParentID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put((byte)_name.length);
            bytes.put(_name);
        }

        @Override
        public String toString()
        {
            String output = "-- FolderData --\n";
            try {
                output += "FolderID: " + FolderID.toString() + "\n";
                output += "ParentID: " + ParentID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FolderDataBlock createFolderDataBlock() {
         return new FolderDataBlock();
    }

    public class ItemDataBlock
    {
        public UUID ItemID = null;
        public int CallbackID = 0;
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
        public UUID AssetID = null;
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
            int length = 140;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public ItemDataBlock() { }
        public ItemDataBlock(ByteBuffer bytes)
        {
            int length;
            ItemID = new UUID(bytes);
            CallbackID = bytes.getInt();
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
            AssetID = new UUID(bytes);
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
            bytes.putInt(CallbackID);
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
            AssetID.GetBytes(bytes);
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
            String output = "-- ItemData --\n";
            try {
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "CallbackID: " + Integer.toString(CallbackID) + "\n";
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
                output += "AssetID: " + AssetID.toString() + "\n";
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

    public ItemDataBlock createItemDataBlock() {
         return new ItemDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.BulkUpdateInventory; }
    public AgentDataBlock AgentData;
    public FolderDataBlock[] FolderData;
    public ItemDataBlock[] ItemData;

    public BulkUpdateInventoryPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)281);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        FolderData = new FolderDataBlock[0];
        ItemData = new ItemDataBlock[0];
    }

    public BulkUpdateInventoryPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            FolderData[j] = new FolderDataBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        ItemData = new ItemDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ItemData[j] = new ItemDataBlock(bytes);
        }
     }

    public BulkUpdateInventoryPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            FolderData[j] = new FolderDataBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        ItemData = new ItemDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ItemData[j] = new ItemDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < FolderData.length; j++) { length += FolderData[j].getLength(); }
        length++;
        for (int j = 0; j < ItemData.length; j++) { length += ItemData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)FolderData.length);
        for (int j = 0; j < FolderData.length; j++) { FolderData[j].ToBytes(bytes); }
        bytes.put((byte)ItemData.length);
        for (int j = 0; j < ItemData.length; j++) { ItemData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- BulkUpdateInventory ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < FolderData.length; j++)
        {
            output += FolderData[j].toString() + "\n";
        }
        for (int j = 0; j < ItemData.length; j++)
        {
            output += ItemData[j].toString() + "\n";
        }
        return output;
    }
}
