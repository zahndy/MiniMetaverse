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
import libomv.types.Quaternion;
import libomv.types.Vector3;

public class AgentUpdatePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public Quaternion BodyRotation = null;
        public Quaternion HeadRotation = null;
        public byte State = 0;
        public Vector3 CameraCenter = null;
        public Vector3 CameraAtAxis = null;
        public Vector3 CameraLeftAxis = null;
        public Vector3 CameraUpAxis = null;
        public float Far = 0;
        public int ControlFlags = 0;
        public byte Flags = 0;

        public int getLength(){
            return 114;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            BodyRotation = new Quaternion(bytes, true);
            HeadRotation = new Quaternion(bytes, true);
            State = bytes.get();
            CameraCenter = new Vector3(bytes);
            CameraAtAxis = new Vector3(bytes);
            CameraLeftAxis = new Vector3(bytes);
            CameraUpAxis = new Vector3(bytes);
            Far = bytes.getFloat();
            ControlFlags = bytes.getInt();
            Flags = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            BodyRotation.GetBytes(bytes);
            HeadRotation.GetBytes(bytes);
            bytes.put(State);
            CameraCenter.GetBytes(bytes);
            CameraAtAxis.GetBytes(bytes);
            CameraLeftAxis.GetBytes(bytes);
            CameraUpAxis.GetBytes(bytes);
            bytes.putFloat(Far);
            bytes.putInt(ControlFlags);
            bytes.put(Flags);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "BodyRotation: " + BodyRotation.toString() + "\n";
                output += "HeadRotation: " + HeadRotation.toString() + "\n";
                output += "State: " + Byte.toString(State) + "\n";
                output += "CameraCenter: " + CameraCenter.toString() + "\n";
                output += "CameraAtAxis: " + CameraAtAxis.toString() + "\n";
                output += "CameraLeftAxis: " + CameraLeftAxis.toString() + "\n";
                output += "CameraUpAxis: " + CameraUpAxis.toString() + "\n";
                output += "Far: " + Float.toString(Far) + "\n";
                output += "ControlFlags: " + Integer.toString(ControlFlags) + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
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
    public PacketType getType() { return PacketType.AgentUpdate; }
    public AgentDataBlock AgentData;

    public AgentUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)4);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
    }

    public AgentUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        AgentData = new AgentDataBlock(bytes);
     }

    public AgentUpdatePacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- AgentUpdate ---\n";
        output += AgentData.toString() + "\n";
        return output;
    }
}
