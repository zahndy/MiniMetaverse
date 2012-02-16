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
import libomv.types.Vector3;

public class RezObjectFromNotecardPacket extends Packet
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

    public class RezDataBlock
    {
        public UUID FromTaskID = null;
        public byte BypassRaycast = 0;
        public Vector3 RayStart = null;
        public Vector3 RayEnd = null;
        public UUID RayTargetID = null;
        public boolean RayEndIsIntersection = false;
        public boolean RezSelected = false;
        public boolean RemoveItem = false;
        public int ItemFlags = 0;
        public int GroupMask = 0;
        public int EveryoneMask = 0;
        public int NextOwnerMask = 0;

        public int getLength(){
            return 76;
        }

        public RezDataBlock() { }
        public RezDataBlock(ByteBuffer bytes)
        {
            FromTaskID = new UUID(bytes);
            BypassRaycast = bytes.get();
            RayStart = new Vector3(bytes);
            RayEnd = new Vector3(bytes);
            RayTargetID = new UUID(bytes);
            RayEndIsIntersection = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RezSelected = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RemoveItem = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            ItemFlags = bytes.getInt();
            GroupMask = bytes.getInt();
            EveryoneMask = bytes.getInt();
            NextOwnerMask = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            FromTaskID.GetBytes(bytes);
            bytes.put(BypassRaycast);
            RayStart.GetBytes(bytes);
            RayEnd.GetBytes(bytes);
            RayTargetID.GetBytes(bytes);
            bytes.put((byte)((RayEndIsIntersection) ? 1 : 0));
            bytes.put((byte)((RezSelected) ? 1 : 0));
            bytes.put((byte)((RemoveItem) ? 1 : 0));
            bytes.putInt(ItemFlags);
            bytes.putInt(GroupMask);
            bytes.putInt(EveryoneMask);
            bytes.putInt(NextOwnerMask);
        }

        @Override
        public String toString()
        {
            String output = "-- RezData --\n";
            try {
                output += "FromTaskID: " + FromTaskID.toString() + "\n";
                output += "BypassRaycast: " + Byte.toString(BypassRaycast) + "\n";
                output += "RayStart: " + RayStart.toString() + "\n";
                output += "RayEnd: " + RayEnd.toString() + "\n";
                output += "RayTargetID: " + RayTargetID.toString() + "\n";
                output += "RayEndIsIntersection: " + Boolean.toString(RayEndIsIntersection) + "\n";
                output += "RezSelected: " + Boolean.toString(RezSelected) + "\n";
                output += "RemoveItem: " + Boolean.toString(RemoveItem) + "\n";
                output += "ItemFlags: " + Integer.toString(ItemFlags) + "\n";
                output += "GroupMask: " + Integer.toString(GroupMask) + "\n";
                output += "EveryoneMask: " + Integer.toString(EveryoneMask) + "\n";
                output += "NextOwnerMask: " + Integer.toString(NextOwnerMask) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RezDataBlock createRezDataBlock() {
         return new RezDataBlock();
    }

    public class NotecardDataBlock
    {
        public UUID NotecardItemID = null;
        public UUID ObjectID = null;

        public int getLength(){
            return 32;
        }

        public NotecardDataBlock() { }
        public NotecardDataBlock(ByteBuffer bytes)
        {
            NotecardItemID = new UUID(bytes);
            ObjectID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            NotecardItemID.GetBytes(bytes);
            ObjectID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- NotecardData --\n";
            try {
                output += "NotecardItemID: " + NotecardItemID.toString() + "\n";
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NotecardDataBlock createNotecardDataBlock() {
         return new NotecardDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RezObjectFromNotecard; }
    public AgentDataBlock AgentData;
    public RezDataBlock RezData;
    public NotecardDataBlock NotecardData;
    public UUID[] ItemID;

    public RezObjectFromNotecardPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)294);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RezData = new RezDataBlock();
        NotecardData = new NotecardDataBlock();
        ItemID = new UUID[0];
    }

    public RezObjectFromNotecardPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        RezData = new RezDataBlock(bytes);
        NotecardData = new NotecardDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ItemID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            ItemID[j] = new UUID(bytes);
        }
     }

    public RezObjectFromNotecardPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RezData = new RezDataBlock(bytes);
        NotecardData = new NotecardDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ItemID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            ItemID[j] = new UUID(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RezData.getLength();
        length += NotecardData.getLength();
        length++;
        length += ItemID.length * 16;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        RezData.ToBytes(bytes);
        NotecardData.ToBytes(bytes);
        bytes.put((byte)ItemID.length);
        for (int j = 0; j < ItemID.length; j++)
        {
            ItemID[j].GetBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RezObjectFromNotecard ---\n";
        output += AgentData.toString() + "\n";
        output += RezData.toString() + "\n";
        output += NotecardData.toString() + "\n";
        for (int j = 0; j < ItemID.length; j++)
        {
            output += "ItemID[" + j + "]: " + ItemID[j].toString() + "\n";
        }
        return output;
    }
}
