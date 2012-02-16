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

public class RezMultipleAttachmentsFromInvPacket extends Packet
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

    public class HeaderDataBlock
    {
        public UUID CompoundMsgID = null;
        public byte TotalObjects = 0;
        public boolean FirstDetachAll = false;

        public int getLength(){
            return 18;
        }

        public HeaderDataBlock() { }
        public HeaderDataBlock(ByteBuffer bytes)
        {
            CompoundMsgID = new UUID(bytes);
            TotalObjects = bytes.get();
            FirstDetachAll = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            CompoundMsgID.GetBytes(bytes);
            bytes.put(TotalObjects);
            bytes.put((byte)((FirstDetachAll) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- HeaderData --\n";
            try {
                output += "CompoundMsgID: " + CompoundMsgID.toString() + "\n";
                output += "TotalObjects: " + Byte.toString(TotalObjects) + "\n";
                output += "FirstDetachAll: " + Boolean.toString(FirstDetachAll) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public HeaderDataBlock createHeaderDataBlock() {
         return new HeaderDataBlock();
    }

    public class ObjectDataBlock
    {
        public UUID ItemID = null;
        public UUID OwnerID = null;
        public byte AttachmentPt = 0;
        public int ItemFlags = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;
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
            int length = 49;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            ItemID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            AttachmentPt = bytes.get();
            ItemFlags = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ItemID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.put(AttachmentPt);
            bytes.putInt(ItemFlags);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
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
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "AttachmentPt: " + Byte.toString(AttachmentPt) + "\n";
                output += "ItemFlags: " + Integer.toString(ItemFlags) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
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
    public PacketType getType() { return PacketType.RezMultipleAttachmentsFromInv; }
    public AgentDataBlock AgentData;
    public HeaderDataBlock HeaderData;
    public ObjectDataBlock[] ObjectData;

    public RezMultipleAttachmentsFromInvPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)396);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        HeaderData = new HeaderDataBlock();
        ObjectData = new ObjectDataBlock[0];
    }

    public RezMultipleAttachmentsFromInvPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        HeaderData = new HeaderDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectData = new ObjectDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ObjectData[j] = new ObjectDataBlock(bytes);
        }
     }

    public RezMultipleAttachmentsFromInvPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        HeaderData = new HeaderDataBlock(bytes);
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
        length += AgentData.getLength();
        length += HeaderData.getLength();
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
        AgentData.ToBytes(bytes);
        HeaderData.ToBytes(bytes);
        bytes.put((byte)ObjectData.length);
        for (int j = 0; j < ObjectData.length; j++) { ObjectData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RezMultipleAttachmentsFromInv ---\n";
        output += AgentData.toString() + "\n";
        output += HeaderData.toString() + "\n";
        for (int j = 0; j < ObjectData.length; j++)
        {
            output += ObjectData[j].toString() + "\n";
        }
        return output;
    }
}
