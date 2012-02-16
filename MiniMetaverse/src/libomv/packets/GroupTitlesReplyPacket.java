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

public class GroupTitlesReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID GroupID = null;
        public UUID RequestID = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            GroupID = new UUID(bytes);
            RequestID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            RequestID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "RequestID: " + RequestID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class GroupDataBlock
    {
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

        public UUID RoleID = null;
        public boolean Selected = false;

        public int getLength(){
            int length = 17;
            if (getTitle() != null) { length += 1 + getTitle().length; }
            return length;
        }

        public GroupDataBlock() { }
        public GroupDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _title = new byte[length];
            bytes.get(_title);
            RoleID = new UUID(bytes);
            Selected = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_title.length);
            bytes.put(_title);
            RoleID.GetBytes(bytes);
            bytes.put((byte)((Selected) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- GroupData --\n";
            try {
                output += Helpers.FieldToString(_title, "Title") + "\n";
                output += "RoleID: " + RoleID.toString() + "\n";
                output += "Selected: " + Boolean.toString(Selected) + "\n";
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
    public PacketType getType() { return PacketType.GroupTitlesReply; }
    public AgentDataBlock AgentData;
    public GroupDataBlock[] GroupData;

    public GroupTitlesReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)376);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        GroupData = new GroupDataBlock[0];
    }

    public GroupTitlesReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        GroupData = new GroupDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            GroupData[j] = new GroupDataBlock(bytes);
        }
     }

    public GroupTitlesReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
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
        length += AgentData.getLength();
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
        AgentData.ToBytes(bytes);
        bytes.put((byte)GroupData.length);
        for (int j = 0; j < GroupData.length; j++) { GroupData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupTitlesReply ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < GroupData.length; j++)
        {
            output += GroupData[j].toString() + "\n";
        }
        return output;
    }
}
