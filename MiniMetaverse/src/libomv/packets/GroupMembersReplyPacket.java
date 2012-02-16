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

public class GroupMembersReplyPacket extends Packet
{
    public class GroupDataBlock
    {
        public UUID GroupID = null;
        public UUID RequestID = null;
        public int MemberCount = 0;

        public int getLength(){
            return 36;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            GroupID = new UUID(bytes);
            RequestID = new UUID(bytes);
            MemberCount = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            RequestID.GetBytes(bytes);
            bytes.putInt(MemberCount);
        }

        @Override
        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "MemberCount: " + Integer.toString(MemberCount) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public GroupDataBlock createGroupDataBlock() {
         return new GroupDataBlock();
    }

    public class MemberDataBlock
    {
        public UUID AgentID = null;
        public int Contribution = 0;
        private byte[] _onlinestatus;
        public byte[] getOnlineStatus() {
            return _onlinestatus;
        }

        public void setOnlineStatus(byte[] value) throws Exception {
            if (value == null) {
                _onlinestatus = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _onlinestatus = new byte[value.length];
                System.arraycopy(value, 0, _onlinestatus, 0, value.length);
            }
        }

        public long AgentPowers = 0;
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

        public boolean IsOwner = false;

        public int getLength(){
            int length = 29;
            if (getOnlineStatus() != null) { length += 1 + getOnlineStatus().length; }
            if (getTitle() != null) { length += 1 + getTitle().length; }
            return length;
        }

        public MemberDataBlock() { }
        public MemberDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            Contribution = bytes.getInt();
            length = bytes.get() & 0xFF;
            _onlinestatus = new byte[length];
            bytes.get(_onlinestatus);
            AgentPowers = bytes.getLong();
            length = bytes.get() & 0xFF;
            _title = new byte[length];
            bytes.get(_title);
            IsOwner = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.putInt(Contribution);
            bytes.put((byte)_onlinestatus.length);
            bytes.put(_onlinestatus);
            bytes.putLong(AgentPowers);
            bytes.put((byte)_title.length);
            bytes.put(_title);
            bytes.put((byte)((IsOwner) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- MemberData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "Contribution: " + Integer.toString(Contribution) + "\n";
                output += Helpers.FieldToString(_onlinestatus, "OnlineStatus") + "\n";
                output += "AgentPowers: " + Long.toString(AgentPowers) + "\n";
                output += Helpers.FieldToString(_title, "Title") + "\n";
                output += "IsOwner: " + Boolean.toString(IsOwner) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MemberDataBlock createMemberDataBlock() {
         return new MemberDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupMembersReply; }
    public UUID AgentID;
    public GroupDataBlock GroupData;
    public MemberDataBlock[] MemberData;

    public GroupMembersReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)367);
        header.setReliable(true);
        AgentID = new UUID();
        GroupData = new GroupDataBlock();
        MemberData = new MemberDataBlock[0];
    }

    public GroupMembersReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        MemberData = new MemberDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            MemberData[j] = new MemberDataBlock(bytes);
        }
     }

    public GroupMembersReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        GroupData = new GroupDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        MemberData = new MemberDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            MemberData[j] = new MemberDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += GroupData.getLength();
        length++;
        for (int j = 0; j < MemberData.length; j++) { length += MemberData[j].getLength(); }
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
        bytes.put((byte)MemberData.length);
        for (int j = 0; j < MemberData.length; j++) { MemberData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupMembersReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += GroupData.toString() + "\n";
        for (int j = 0; j < MemberData.length; j++)
        {
            output += MemberData[j].toString() + "\n";
        }
        return output;
    }
}
