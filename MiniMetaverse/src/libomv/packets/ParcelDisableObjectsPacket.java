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

public class ParcelDisableObjectsPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class ParcelDataBlock
    {
        public int LocalID = 0;
        public int ReturnType = 0;

        public int getLength(){
            return 8;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            LocalID = bytes.getInt();
            ReturnType = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(LocalID);
            bytes.putInt(ReturnType);
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output += "ReturnType: " + Integer.toString(ReturnType) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ParcelDataBlock createParcelDataBlock() {
         return new ParcelDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ParcelDisableObjects; }
    public AgentDataBlock AgentData;
    public ParcelDataBlock ParcelData;
    public UUID[] TaskID;
    public UUID[] OwnerID;

    public ParcelDisableObjectsPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)201);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        ParcelData = new ParcelDataBlock();
        TaskID = new UUID[0];
        OwnerID = new UUID[0];
    }

    public ParcelDisableObjectsPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        TaskID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            TaskID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        OwnerID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            OwnerID[j] = new UUID(bytes);
        }
     }

    public ParcelDisableObjectsPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        TaskID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            TaskID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        OwnerID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            OwnerID[j] = new UUID(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ParcelData.getLength();
        length++;
        length += TaskID.length * 16;
        length++;
        length += OwnerID.length * 16;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        ParcelData.ToBytes(bytes);
        bytes.put((byte)TaskID.length);
        for (int j = 0; j < TaskID.length; j++)
        {
            TaskID[j].GetBytes(bytes);
        }
        bytes.put((byte)OwnerID.length);
        for (int j = 0; j < OwnerID.length; j++)
        {
            OwnerID[j].GetBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelDisableObjects ---\n";
        output += AgentData.toString() + "\n";
        output += ParcelData.toString() + "\n";
        for (int j = 0; j < TaskID.length; j++)
        {
            output += "TaskID[" + j + "]: " + TaskID[j].toString() + "\n";
        }
        for (int j = 0; j < OwnerID.length; j++)
        {
            output += "OwnerID[" + j + "]: " + OwnerID[j].toString() + "\n";
        }
        return output;
    }
}
