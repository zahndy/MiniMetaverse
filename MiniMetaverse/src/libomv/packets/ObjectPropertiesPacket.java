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

public class ObjectPropertiesPacket extends Packet
{
    public class ObjectDataBlock
    {
        public UUID ObjectID = null;
        public UUID CreatorID = null;
        public UUID OwnerID = null;
        public UUID GroupID = null;
        public long CreationDate = 0;
        public int BaseMask = 0;
        public int OwnerMask = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;
        public int OwnershipCost = 0;
        public byte SaleType = 0;
        public int SalePrice = 0;
        public byte AggregatePerms = 0;
        public byte AggregatePermTextures = 0;
        public byte AggregatePermTexturesOwner = 0;
        public int Category = 0;
        public short InventorySerial = 0;
        public UUID ItemID = null;
        public UUID FolderID = null;
        public UUID FromTaskID = null;
        public UUID LastOwnerID = null;
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

        private byte[] _touchname;
        public byte[] getTouchName() {
            return _touchname;
        }

        public void setTouchName(byte[] value) throws Exception {
            if (value == null) {
                _touchname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _touchname = new byte[value.length];
                System.arraycopy(value, 0, _touchname, 0, value.length);
            }
        }

        private byte[] _sitname;
        public byte[] getSitName() {
            return _sitname;
        }

        public void setSitName(byte[] value) throws Exception {
            if (value == null) {
                _sitname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _sitname = new byte[value.length];
                System.arraycopy(value, 0, _sitname, 0, value.length);
            }
        }

        private byte[] _textureid;
        public byte[] getTextureID() {
            return _textureid;
        }

        public void setTextureID(byte[] value) throws Exception {
            if (value == null) {
                _textureid = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _textureid = new byte[value.length];
                System.arraycopy(value, 0, _textureid, 0, value.length);
            }
        }


        public int getLength(){
            int length = 174;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            if (getTouchName() != null) { length += 1 + getTouchName().length; }
            if (getSitName() != null) { length += 1 + getSitName().length; }
            if (getTextureID() != null) { length += 1 + getTextureID().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            ObjectID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            GroupID = new UUID(bytes);
            CreationDate = bytes.getLong();
            BaseMask = bytes.getInt();
            OwnerMask = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
            OwnershipCost = bytes.getInt();
            SaleType = bytes.get();
            SalePrice = bytes.getInt();
            AggregatePerms = bytes.get();
            AggregatePermTextures = bytes.get();
            AggregatePermTexturesOwner = bytes.get();
            Category = bytes.getInt();
            InventorySerial = bytes.getShort();
            ItemID = new UUID(bytes);
            FolderID = new UUID(bytes);
            FromTaskID = new UUID(bytes);
            LastOwnerID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
            length = bytes.get() & 0xFF;
            _touchname = new byte[length];
            bytes.get(_touchname);
            length = bytes.get() & 0xFF;
            _sitname = new byte[length];
            bytes.get(_sitname);
            length = bytes.get() & 0xFF;
            _textureid = new byte[length];
            bytes.get(_textureid);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.putLong(CreationDate);
            bytes.putInt(BaseMask);
            bytes.putInt(OwnerMask);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
            bytes.putInt(OwnershipCost);
            bytes.put(SaleType);
            bytes.putInt(SalePrice);
            bytes.put(AggregatePerms);
            bytes.put(AggregatePermTextures);
            bytes.put(AggregatePermTexturesOwner);
            bytes.putInt(Category);
            bytes.putShort(InventorySerial);
            ItemID.GetBytes(bytes);
            FolderID.GetBytes(bytes);
            FromTaskID.GetBytes(bytes);
            LastOwnerID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_description.length);
            bytes.put(_description);
            bytes.put((byte)_touchname.length);
            bytes.put(_touchname);
            bytes.put((byte)_sitname.length);
            bytes.put(_sitname);
            bytes.put((byte)_textureid.length);
            bytes.put(_textureid);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "CreationDate: " + Long.toString(CreationDate) + "\n";
                output += "BaseMask: " + Integer.toString(BaseMask) + "\n";
                output += "OwnerMask: " + Integer.toString(OwnerMask) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output += "OwnershipCost: " + Integer.toString(OwnershipCost) + "\n";
                output += "SaleType: " + Byte.toString(SaleType) + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += "AggregatePerms: " + Byte.toString(AggregatePerms) + "\n";
                output += "AggregatePermTextures: " + Byte.toString(AggregatePermTextures) + "\n";
                output += "AggregatePermTexturesOwner: " + Byte.toString(AggregatePermTexturesOwner) + "\n";
                output += "Category: " + Integer.toString(Category) + "\n";
                output += "InventorySerial: " + Short.toString(InventorySerial) + "\n";
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "FolderID: " + FolderID.toString() + "\n";
                output += "FromTaskID: " + FromTaskID.toString() + "\n";
                output += "LastOwnerID: " + LastOwnerID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output += Helpers.FieldToString(_touchname, "TouchName") + "\n";
                output += Helpers.FieldToString(_sitname, "SitName") + "\n";
                output += Helpers.FieldToString(_textureid, "TextureID") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ObjectDataBlock createObjectDataBlock() {
         return new ObjectDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ObjectProperties; }
    public ObjectDataBlock[] ObjectData;

    public ObjectPropertiesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)9);
        header.setReliable(true);
        ObjectData = new ObjectDataBlock[0];
    }

    public ObjectPropertiesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        int count = bytes.get() & 0xFF;
        ObjectData = new ObjectDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ObjectData[j] = new ObjectDataBlock(bytes);
        }
     }

    public ObjectPropertiesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        ObjectData = new ObjectDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ObjectData[j] = new ObjectDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < ObjectData.length; j++) { length += ObjectData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)ObjectData.length);
        for (int j = 0; j < ObjectData.length; j++) { ObjectData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ObjectProperties ---\n";
        for (int j = 0; j < ObjectData.length; j++)
        {
            output += ObjectData[j].toString() + "\n";
        }
        return output;
    }
}
