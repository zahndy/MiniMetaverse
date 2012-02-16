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

public class ChangeUserRightsPacket extends Packet
{
    public class RightsBlock
    {
        public UUID AgentRelated = null;
        public int RelatedRights = 0;

        public int getLength(){
            return 20;
        }

        public RightsBlock() { }
        public RightsBlock(ByteBuffer bytes)
        {
            AgentRelated = new UUID(bytes);
            RelatedRights = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentRelated.GetBytes(bytes);
            bytes.putInt(RelatedRights);
        }

        @Override
        public String toString()
        {
            String output = "-- Rights --\n";
            try {
                output += "AgentRelated: " + AgentRelated.toString() + "\n";
                output += "RelatedRights: " + Integer.toString(RelatedRights) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RightsBlock createRightsBlock() {
         return new RightsBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ChangeUserRights; }
    public UUID AgentID;
    public RightsBlock[] Rights;

    public ChangeUserRightsPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)321);
        header.setReliable(true);
        AgentID = new UUID();
        Rights = new RightsBlock[0];
    }

    public ChangeUserRightsPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        Rights = new RightsBlock[count];
        for (int j = 0; j < count; j++)
        {
            Rights[j] = new RightsBlock(bytes);
        }
     }

    public ChangeUserRightsPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        Rights = new RightsBlock[count];
        for (int j = 0; j < count; j++)
        {
            Rights[j] = new RightsBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        for (int j = 0; j < Rights.length; j++) { length += Rights[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        bytes.put((byte)Rights.length);
        for (int j = 0; j < Rights.length; j++) { Rights[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ChangeUserRights ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        for (int j = 0; j < Rights.length; j++)
        {
            output += Rights[j].toString() + "\n";
        }
        return output;
    }
}
