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

public class AgentGroupDataUpdatePacket extends Packet
{
    public class GroupDataBlock
    {
        public UUID GroupID = null;
        public long GroupPowers = 0;
        public boolean AcceptNotices = false;
        public UUID GroupInsigniaID = null;
        public int Contribution = 0;
        private byte[] _groupname;
        public byte[] getGroupName() {
            return _groupname;
        }

        public void setGroupName(byte[] value) throws Exception {
            if (value == null) {
                _groupname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _groupname = new byte[value.length];
                System.arraycopy(value, 0, _groupname, 0, value.length);
            }
        }


        public int getLength(){
            int length = 45;
            if (getGroupName() != null) { length += 1 + getGroupName().length; }
            return length;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            int length;
            GroupID = new UUID(bytes);
            GroupPowers = bytes.getLong();
            AcceptNotices = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            GroupInsigniaID = new UUID(bytes);
            Contribution = bytes.getInt();
            length = bytes.get() & 0xFF;
            _groupname = new byte[length];
            bytes.get(_groupname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GroupID.GetBytes(bytes);
            bytes.putLong(GroupPowers);
            bytes.put((byte)((AcceptNotices) ? 1 : 0));
            GroupInsigniaID.GetBytes(bytes);
            bytes.putInt(Contribution);
            bytes.put((byte)_groupname.length);
            bytes.put(_groupname);
        }

        @Override
        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "GroupPowers: " + Long.toString(GroupPowers) + "\n";
                output += "AcceptNotices: " + Boolean.toString(AcceptNotices) + "\n";
                output += "GroupInsigniaID: " + GroupInsigniaID.toString() + "\n";
                output += "Contribution: " + Integer.toString(Contribution) + "\n";
                output += Helpers.FieldToString(_groupname, "GroupName") + "\n";
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
    public PacketType getType() { return PacketType.AgentGroupDataUpdate; }
    public UUID AgentID;
    public GroupDataBlock[] GroupData;

    public AgentGroupDataUpdatePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)389);
        header.setReliable(true);
        AgentID = new UUID();
        GroupData = new GroupDataBlock[0];
    }

    public AgentGroupDataUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            GroupData[j] = new GroupDataBlock(bytes);
        }
     }

    public AgentGroupDataUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            GroupData[j] = new GroupDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        for (int j = 0; j < GroupData.length; j++) { length += GroupData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        bytes.put((byte)GroupData.length);
        for (int j = 0; j < GroupData.length; j++) { GroupData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AgentGroupDataUpdate ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        for (int j = 0; j < GroupData.length; j++)
        {
            output += GroupData[j].toString() + "\n";
        }
        return output;
    }
}
