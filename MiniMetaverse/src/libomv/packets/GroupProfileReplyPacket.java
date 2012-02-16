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

public class GroupProfileReplyPacket extends Packet
{
    public class GroupDataBlock
    {
        public UUID GroupID = null;
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

        private byte[] _charter;
        public byte[] getCharter() {
            return _charter;
        }

        public void setCharter(byte[] value) throws Exception {
            if (value == null) {
                _charter = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _charter = new byte[value.length];
                System.arraycopy(value, 0, _charter, 0, value.length);
            }
        }

        public boolean ShowInList = false;
        private byte[] _membertitle;
        public byte[] getMemberTitle() {
            return _membertitle;
        }

        public void setMemberTitle(byte[] value) throws Exception {
            if (value == null) {
                _membertitle = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _membertitle = new byte[value.length];
                System.arraycopy(value, 0, _membertitle, 0, value.length);
            }
        }

        public long PowersMask = 0;
        public UUID InsigniaID = null;
        public UUID FounderID = null;
        public int MembershipFee = 0;
        public boolean OpenEnrollment = false;
        public int Money = 0;
        public int GroupMembershipCount = 0;
        public int GroupRolesCount = 0;
        public boolean AllowPublish = false;
        public boolean MaturePublish = false;
        public UUID OwnerRole = null;

        public int getLength(){
            int length = 92;
            if (getName() != null) { length += 1 + getName().length; }
            if (getCharter() != null) { length += 2 + getCharter().length; }
            if (getMemberTitle() != null) { length += 1 + getMemberTitle().length; }
            return length;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            int length;
            GroupID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.getShort() & 0xFFFF;
            _charter = new byte[length];
            bytes.get(_charter);
            ShowInList = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = bytes.get() & 0xFF;
            _membertitle = new byte[length];
            bytes.get(_membertitle);
            PowersMask = bytes.getLong();
            InsigniaID = new UUID(bytes);
            FounderID = new UUID(bytes);
            MembershipFee = bytes.getInt();
            OpenEnrollment = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Money = bytes.getInt();
            GroupMembershipCount = bytes.getInt();
            GroupRolesCount = bytes.getInt();
            AllowPublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            MaturePublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            OwnerRole = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.putShort((short)_charter.length);
            bytes.put(_charter);
            bytes.put((byte)((ShowInList) ? 1 : 0));
            bytes.put((byte)_membertitle.length);
            bytes.put(_membertitle);
            bytes.putLong(PowersMask);
            InsigniaID.GetBytes(bytes);
            FounderID.GetBytes(bytes);
            bytes.putInt(MembershipFee);
            bytes.put((byte)((OpenEnrollment) ? 1 : 0));
            bytes.putInt(Money);
            bytes.putInt(GroupMembershipCount);
            bytes.putInt(GroupRolesCount);
            bytes.put((byte)((AllowPublish) ? 1 : 0));
            bytes.put((byte)((MaturePublish) ? 1 : 0));
            OwnerRole.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_charter, "Charter") + "\n";
                output += "ShowInList: " + Boolean.toString(ShowInList) + "\n";
                output += Helpers.FieldToString(_membertitle, "MemberTitle") + "\n";
                output += "PowersMask: " + Long.toString(PowersMask) + "\n";
                output += "InsigniaID: " + InsigniaID.toString() + "\n";
                output += "FounderID: " + FounderID.toString() + "\n";
                output += "MembershipFee: " + Integer.toString(MembershipFee) + "\n";
                output += "OpenEnrollment: " + Boolean.toString(OpenEnrollment) + "\n";
                output += "Money: " + Integer.toString(Money) + "\n";
                output += "GroupMembershipCount: " + Integer.toString(GroupMembershipCount) + "\n";
                output += "GroupRolesCount: " + Integer.toString(GroupRolesCount) + "\n";
                output += "AllowPublish: " + Boolean.toString(AllowPublish) + "\n";
                output += "MaturePublish: " + Boolean.toString(MaturePublish) + "\n";
                output += "OwnerRole: " + OwnerRole.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GroupDataBlock createGroupDataBlock() {
         return new GroupDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupProfileReply; }
    public UUID AgentID;
    public GroupDataBlock GroupData;

    public GroupProfileReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)352);
        header.setReliable(true);
        AgentID = new UUID();
        GroupData = new GroupDataBlock();
    }

    public GroupProfileReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
     }

    public GroupProfileReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += GroupData.getLength();
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
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupProfileReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += GroupData.toString() + "\n";
        return output;
    }
}
