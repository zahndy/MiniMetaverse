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

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;

public class GroupRoleChangesPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID GroupID = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            GroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class RoleChangeBlock
    {
        public UUID RoleID = null;
        public UUID MemberID = null;
        public int Change = 0;

        public int getLength(){
            return 36;
        }

        public RoleChangeBlock() { }
        public RoleChangeBlock(ByteBuffer bytes)
        {
            RoleID = new UUID(bytes);
            MemberID = new UUID(bytes);
            Change = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RoleID.GetBytes(bytes);
            MemberID.GetBytes(bytes);
            bytes.putInt(Change);
        }

        @Override
        public String toString()
        {
            String output = "-- RoleChange --\n";
            try {
                output += "RoleID: " + RoleID.toString() + "\n";
                output += "MemberID: " + MemberID.toString() + "\n";
                output += "Change: " + Integer.toString(Change) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RoleChangeBlock createRoleChangeBlock() {
         return new RoleChangeBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupRoleChanges; }
    public AgentDataBlock AgentData;
    public RoleChangeBlock[] RoleChange;

    public GroupRoleChangesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)342);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RoleChange = new RoleChangeBlock[0];
    }

    public GroupRoleChangesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        RoleChange = new RoleChangeBlock[count];
        for (int j = 0; j < count; j++)
        {
            RoleChange[j] = new RoleChangeBlock(bytes);
        }
     }

    public GroupRoleChangesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        RoleChange = new RoleChangeBlock[count];
        for (int j = 0; j < count; j++)
        {
            RoleChange[j] = new RoleChangeBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < RoleChange.length; j++) { length += RoleChange[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)RoleChange.length);
        for (int j = 0; j < RoleChange.length; j++) { RoleChange[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupRoleChanges ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < RoleChange.length; j++)
        {
            output += RoleChange[j].toString() + "\n";
        }
        return output;
    }
}
