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
import libomv.types.Vector3;
import libomv.types.OverflowException;

public class RezObjectPacket extends Packet
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

    public class RezDataBlock
    {
        public UUID FromTaskID = null;
        public byte BypassRaycast = 0;
        public Vector3 RayStart = null;
        public Vector3 RayEnd = null;
        public UUID RayTargetID = null;
        public boolean RayEndIsIntersection = false;
        public boolean RezSelected = false;
        public boolean RemoveItem = false;
        public int ItemFlags = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;

        public int getLength(){
            return 76;
        }

        public RezDataBlock() { }
        public RezDataBlock(ByteBuffer bytes)
        {
            FromTaskID = new UUID(bytes);
            BypassRaycast = bytes.get();
            RayStart = new Vector3(bytes);
            RayEnd = new Vector3(bytes);
            RayTargetID = new UUID(bytes);
            RayEndIsIntersection = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RezSelected = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RemoveItem = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            ItemFlags = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            FromTaskID.GetBytes(bytes);
            bytes.put(BypassRaycast);
            RayStart.GetBytes(bytes);
            RayEnd.GetBytes(bytes);
            RayTargetID.GetBytes(bytes);
            bytes.put((byte)((RayEndIsIntersection) ? 1 : 0));
            bytes.put((byte)((RezSelected) ? 1 : 0));
            bytes.put((byte)((RemoveItem) ? 1 : 0));
            bytes.putInt(ItemFlags);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
        }

        @Override
        public String toString()
        {
            String output = "-- RezData --\n";
            try {
                output += "FromTaskID: " + FromTaskID.toString() + "\n";
                output += "BypassRaycast: " + Byte.toString(BypassRaycast) + "\n";
                output += "RayStart: " + RayStart.toString() + "\n";
                output += "RayEnd: " + RayEnd.toString() + "\n";
                output += "RayTargetID: " + RayTargetID.toString() + "\n";
                output += "RayEndIsIntersection: " + Boolean.toString(RayEndIsIntersection) + "\n";
                output += "RezSelected: " + Boolean.toString(RezSelected) + "\n";
                output += "RemoveItem: " + Boolean.toString(RemoveItem) + "\n";
                output += "ItemFlags: " + Integer.toString(ItemFlags) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RezDataBlock createRezDataBlock() {
         return new RezDataBlock();
    }

    public class InventoryDataBlock
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

        public InventoryDataBlock() { }
        public InventoryDataBlock(ByteBuffer bytes)
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
            String output = "-- InventoryData --\n";
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

    public InventoryDataBlock createInventoryDataBlock() {
         return new InventoryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RezObject; }
    public AgentDataBlock AgentData;
    public RezDataBlock RezData;
    public InventoryDataBlock InventoryData;

    public RezObjectPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)293);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RezData = new RezDataBlock();
        InventoryData = new InventoryDataBlock();
    }

    public RezObjectPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        RezData = new RezDataBlock(bytes);
        InventoryData = new InventoryDataBlock(bytes);
     }

    public RezObjectPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RezData = new RezDataBlock(bytes);
        InventoryData = new InventoryDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RezData.getLength();
        length += InventoryData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        RezData.ToBytes(bytes);
        InventoryData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RezObject ---\n";
        output += AgentData.toString() + "\n";
        output += RezData.toString() + "\n";
        output += InventoryData.toString() + "\n";
        return output;
    }
}
