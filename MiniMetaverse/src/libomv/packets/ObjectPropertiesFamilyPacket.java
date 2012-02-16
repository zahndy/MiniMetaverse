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

public class ObjectPropertiesFamilyPacket extends Packet
{
    public class ObjectDataBlock
    {
        public int RequestFlags = 0;
        public UUID ObjectID = null;
        public UUID OwnerID = null;
        public UUID GroupID = null;
        public int BaseMask = 0;
        public int OwnerMask = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;
        public int OwnershipCost = 0;
        public byte SaleType = 0;
        public int SalePrice = 0;
        public int Category = 0;
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


        public int getLength(){
            int length = 101;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            RequestFlags = bytes.getInt();
            ObjectID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            GroupID = new UUID(bytes);
            BaseMask = bytes.getInt();
            OwnerMask = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
            OwnershipCost = bytes.getInt();
            SaleType = bytes.get();
            SalePrice = bytes.getInt();
            Category = bytes.getInt();
            LastOwnerID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(RequestFlags);
            ObjectID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.putInt(BaseMask);
            bytes.putInt(OwnerMask);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
            bytes.putInt(OwnershipCost);
            bytes.put(SaleType);
            bytes.putInt(SalePrice);
            bytes.putInt(Category);
            LastOwnerID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_description.length);
            bytes.put(_description);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "RequestFlags: " + Integer.toString(RequestFlags) + "\n";
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "BaseMask: " + Integer.toString(BaseMask) + "\n";
                output += "OwnerMask: " + Integer.toString(OwnerMask) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output += "OwnershipCost: " + Integer.toString(OwnershipCost) + "\n";
                output += "SaleType: " + Byte.toString(SaleType) + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += "Category: " + Integer.toString(Category) + "\n";
                output += "LastOwnerID: " + LastOwnerID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
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
    public PacketType getType() { return PacketType.ObjectPropertiesFamily; }
    public ObjectDataBlock ObjectData;

    public ObjectPropertiesFamilyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)10);
        header.setReliable(true);
        ObjectData = new ObjectDataBlock();
    }

    public ObjectPropertiesFamilyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        ObjectData = new ObjectDataBlock(bytes);
     }

    public ObjectPropertiesFamilyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ObjectData = new ObjectDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ObjectData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ObjectData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ObjectPropertiesFamily ---\n";
        output += ObjectData.toString() + "\n";
        return output;
    }
}
