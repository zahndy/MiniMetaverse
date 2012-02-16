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

public class AgentDataUpdatePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        private byte[] _firstname;
        public byte[] getFirstName() {
            return _firstname;
        }

        public void setFirstName(byte[] value) throws Exception {
            if (value == null) {
                _firstname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _firstname = new byte[value.length];
                System.arraycopy(value, 0, _firstname, 0, value.length);
            }
        }

        private byte[] _lastname;
        public byte[] getLastName() {
            return _lastname;
        }

        public void setLastName(byte[] value) throws Exception {
            if (value == null) {
                _lastname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _lastname = new byte[value.length];
                System.arraycopy(value, 0, _lastname, 0, value.length);
            }
        }

        private byte[] _grouptitle;
        public byte[] getGroupTitle() {
            return _grouptitle;
        }

        public void setGroupTitle(byte[] value) throws Exception {
            if (value == null) {
                _grouptitle = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _grouptitle = new byte[value.length];
                System.arraycopy(value, 0, _grouptitle, 0, value.length);
            }
        }

        public UUID ActiveGroupID = null;
        public long GroupPowers = 0;
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
            int length = 40;
            if (getFirstName() != null) { length += 1 + getFirstName().length; }
            if (getLastName() != null) { length += 1 + getLastName().length; }
            if (getGroupTitle() != null) { length += 1 + getGroupTitle().length; }
            if (getGroupName() != null) { length += 1 + getGroupName().length; }
            return length;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _firstname = new byte[length];
            bytes.get(_firstname);
            length = bytes.get() & 0xFF;
            _lastname = new byte[length];
            bytes.get(_lastname);
            length = bytes.get() & 0xFF;
            _grouptitle = new byte[length];
            bytes.get(_grouptitle);
            ActiveGroupID = new UUID(bytes);
            GroupPowers = bytes.getLong();
            length = bytes.get() & 0xFF;
            _groupname = new byte[length];
            bytes.get(_groupname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.put((byte)_firstname.length);
            bytes.put(_firstname);
            bytes.put((byte)_lastname.length);
            bytes.put(_lastname);
            bytes.put((byte)_grouptitle.length);
            bytes.put(_grouptitle);
            ActiveGroupID.GetBytes(bytes);
            bytes.putLong(GroupPowers);
            bytes.put((byte)_groupname.length);
            bytes.put(_groupname);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += Helpers.FieldToString(_firstname, "FirstName") + "\n";
                output += Helpers.FieldToString(_lastname, "LastName") + "\n";
                output += Helpers.FieldToString(_grouptitle, "GroupTitle") + "\n";
                output += "ActiveGroupID: " + ActiveGroupID.toString() + "\n";
                output += "GroupPowers: " + Long.toString(GroupPowers) + "\n";
                output += Helpers.FieldToString(_groupname, "GroupName") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AgentDataUpdate; }
    public AgentDataBlock AgentData;

    public AgentDataUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)387);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
    }

    public AgentDataUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
     }

    public AgentDataUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AgentDataUpdate ---\n";
        output += AgentData.toString() + "\n";
        return output;
    }
}
