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

public class GroupDataUpdatePacket extends Packet
{
    public class AgentGroupDataBlock
    {
        public UUID AgentID = null;
        public UUID GroupID = null;
        public long AgentPowers = 0;
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


        public int getLength(){
            int length = 40;
            if (getGroupTitle() != null) { length += 1 + getGroupTitle().length; }
            return length;
        }

        public AgentGroupDataBlock() { }
        public AgentGroupDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            GroupID = new UUID(bytes);
            AgentPowers = bytes.getLong();
            length = bytes.get() & 0xFF;
            _grouptitle = new byte[length];
            bytes.get(_grouptitle);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.putLong(AgentPowers);
            bytes.put((byte)_grouptitle.length);
            bytes.put(_grouptitle);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentGroupData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "AgentPowers: " + Long.toString(AgentPowers) + "\n";
                output += Helpers.FieldToString(_grouptitle, "GroupTitle") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentGroupDataBlock createAgentGroupDataBlock() {
         return new AgentGroupDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupDataUpdate; }
    public AgentGroupDataBlock[] AgentGroupData;

    public GroupDataUpdatePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)388);
        header.setReliable(true);
        AgentGroupData = new AgentGroupDataBlock[0];
    }

    public GroupDataUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        AgentGroupData = new AgentGroupDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            AgentGroupData[j] = new AgentGroupDataBlock(bytes);
        }
     }

    public GroupDataUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        AgentGroupData = new AgentGroupDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            AgentGroupData[j] = new AgentGroupDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < AgentGroupData.length; j++) { length += AgentGroupData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)AgentGroupData.length);
        for (int j = 0; j < AgentGroupData.length; j++) { AgentGroupData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupDataUpdate ---\n";
        for (int j = 0; j < AgentGroupData.length; j++)
        {
            output += AgentGroupData[j].toString() + "\n";
        }
        return output;
    }
}
