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

public class InviteGroupResponsePacket extends Packet
{
    public class InviteDataBlock
    {
        public UUID AgentID = null;
        public UUID InviteeID = null;
        public UUID GroupID = null;
        public UUID RoleID = null;
        public int MembershipFee = 0;

        public int getLength(){
            return 68;
        }

        public InviteDataBlock() { }
        public InviteDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            InviteeID = new UUID(bytes);
            GroupID = new UUID(bytes);
            RoleID = new UUID(bytes);
            MembershipFee = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            InviteeID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            RoleID.GetBytes(bytes);
            bytes.putInt(MembershipFee);
        }

        @Override
        public String toString()
        {
            String output = "-- InviteData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "InviteeID: " + InviteeID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "RoleID: " + RoleID.toString() + "\n";
                output += "MembershipFee: " + Integer.toString(MembershipFee) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InviteDataBlock createInviteDataBlock() {
         return new InviteDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.InviteGroupResponse; }
    public InviteDataBlock InviteData;

    public InviteGroupResponsePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)350);
        header.setReliable(true);
        InviteData = new InviteDataBlock();
    }

    public InviteGroupResponsePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        InviteData = new InviteDataBlock(bytes);
     }

    public InviteGroupResponsePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        InviteData = new InviteDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += InviteData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        InviteData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- InviteGroupResponse ---\n";
        output += InviteData.toString() + "\n";
        return output;
    }
}
