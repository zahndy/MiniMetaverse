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

public class GroupRoleDataReplyPacket extends Packet
{
    public class GroupDataBlock
    {
        public UUID GroupID = null;
        public UUID RequestID = null;
        public int RoleCount = 0;

        public int getLength(){
            return 36;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            GroupID = new UUID(bytes);
            RequestID = new UUID(bytes);
            RoleCount = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            RequestID.GetBytes(bytes);
            bytes.putInt(RoleCount);
        }

        @Override
        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "RoleCount: " + Integer.toString(RoleCount) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GroupDataBlock createGroupDataBlock() {
         return new GroupDataBlock();
    }

    public class RoleDataBlock
    {
        public UUID RoleID = null;
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

        private byte[] _title;
        public byte[] getTitle() {
            return _title;
        }

        public void setTitle(byte[] value) throws Exception {
            if (value == null) {
                _title = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _title = new byte[value.length];
                System.arraycopy(value, 0, _title, 0, value.length);
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

        public long Powers = 0;
        public int Members = 0;

        public int getLength(){
            int length = 28;
            if (getName() != null) { length += 1 + getName().length; }
            if (getTitle() != null) { length += 1 + getTitle().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public RoleDataBlock() { }
        public RoleDataBlock(ByteBuffer bytes)
        {
            int length;
            RoleID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _title = new byte[length];
            bytes.get(_title);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
            Powers = bytes.getLong();
            Members = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RoleID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_title.length);
            bytes.put(_title);
            bytes.put((byte)_description.length);
            bytes.put(_description);
            bytes.putLong(Powers);
            bytes.putInt(Members);
        }

        @Override
        public String toString()
        {
            String output = "-- RoleData --\n";
            try {
                output += "RoleID: " + RoleID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_title, "Title") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output += "Powers: " + Long.toString(Powers) + "\n";
                output += "Members: " + Integer.toString(Members) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RoleDataBlock createRoleDataBlock() {
         return new RoleDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupRoleDataReply; }
    public UUID AgentID;
    public GroupDataBlock GroupData;
    public RoleDataBlock[] RoleData;

    public GroupRoleDataReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)372);
        header.setReliable(true);
        AgentID = new UUID();
        GroupData = new GroupDataBlock();
        RoleData = new RoleDataBlock[0];
    }

    public GroupRoleDataReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        RoleData = new RoleDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            RoleData[j] = new RoleDataBlock(bytes);
        }
     }

    public GroupRoleDataReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        RoleData = new RoleDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            RoleData[j] = new RoleDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += GroupData.getLength();
        length++;
        for (int j = 0; j < RoleData.length; j++) { length += RoleData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        GroupData.ToBytes(bytes);
        bytes.put((byte)RoleData.length);
        for (int j = 0; j < RoleData.length; j++) { RoleData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupRoleDataReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += GroupData.toString() + "\n";
        for (int j = 0; j < RoleData.length; j++)
        {
            output += RoleData[j].toString() + "\n";
        }
        return output;
    }
}
