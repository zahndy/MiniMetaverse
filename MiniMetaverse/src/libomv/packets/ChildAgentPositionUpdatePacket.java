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

public class ChildAgentPositionUpdatePacket extends Packet
{
    public class AgentDataBlock
    {
        public long RegionHandle = 0;
        public int ViewerCircuitCode = 0;
        public UUID AgentID = null;
        public UUID SessionID = null;
        public Vector3 AgentPos = null;
        public Vector3 AgentVel = null;
        public Vector3 Center = null;
        public Vector3 Size = null;
        public Vector3 AtAxis = null;
        public Vector3 LeftAxis = null;
        public Vector3 UpAxis = null;
        public boolean ChangedGrid = false;

        public int getLength(){
            return 129;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            RegionHandle = bytes.getLong();
            ViewerCircuitCode = bytes.getInt();
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            AgentPos = new Vector3(bytes);
            AgentVel = new Vector3(bytes);
            Center = new Vector3(bytes);
            Size = new Vector3(bytes);
            AtAxis = new Vector3(bytes);
            LeftAxis = new Vector3(bytes);
            UpAxis = new Vector3(bytes);
            ChangedGrid = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(RegionHandle);
            bytes.putInt(ViewerCircuitCode);
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            AgentPos.GetBytes(bytes);
            AgentVel.GetBytes(bytes);
            Center.GetBytes(bytes);
            Size.GetBytes(bytes);
            AtAxis.GetBytes(bytes);
            LeftAxis.GetBytes(bytes);
            UpAxis.GetBytes(bytes);
            bytes.put((byte)((ChangedGrid) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "ViewerCircuitCode: " + Integer.toString(ViewerCircuitCode) + "\n";
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "AgentPos: " + AgentPos.toString() + "\n";
                output += "AgentVel: " + AgentVel.toString() + "\n";
                output += "Center: " + Center.toString() + "\n";
                output += "Size: " + Size.toString() + "\n";
                output += "AtAxis: " + AtAxis.toString() + "\n";
                output += "LeftAxis: " + LeftAxis.toString() + "\n";
                output += "UpAxis: " + UpAxis.toString() + "\n";
                output += "ChangedGrid: " + Boolean.toString(ChangedGrid) + "\n";
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
    public PacketType getType() { return PacketType.ChildAgentPositionUpdate; }
    public AgentDataBlock AgentData;

    public ChildAgentPositionUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)27);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
    }

    public ChildAgentPositionUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        AgentData = new AgentDataBlock(bytes);
     }

    public ChildAgentPositionUpdatePacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- ChildAgentPositionUpdate ---\n";
        output += AgentData.toString() + "\n";
        return output;
    }
}
