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
import libomv.types.Vector3;
import libomv.types.OverflowException;

public class AgentMovementCompletePacket extends Packet
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

    public class DataBlock
    {
        public Vector3 Position = null;
        public Vector3 LookAt = null;
        public long RegionHandle = 0;
        public int Timestamp = 0;

        public int getLength(){
            return 36;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            Position = new Vector3(bytes);
            LookAt = new Vector3(bytes);
            RegionHandle = bytes.getLong();
            Timestamp = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            Position.GetBytes(bytes);
            LookAt.GetBytes(bytes);
            bytes.putLong(RegionHandle);
            bytes.putInt(Timestamp);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "Position: " + Position.toString() + "\n";
                output += "LookAt: " + LookAt.toString() + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "Timestamp: " + Integer.toString(Timestamp) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    public class SimDataBlock
    {
        private byte[] _channelversion;
        public byte[] getChannelVersion() {
            return _channelversion;
        }

        public void setChannelVersion(byte[] value) throws Exception {
            if (value == null) {
                _channelversion = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _channelversion = new byte[value.length];
                System.arraycopy(value, 0, _channelversion, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getChannelVersion() != null) { length += 2 + getChannelVersion().length; }
            return length;
        }

        public SimDataBlock() { }
        public SimDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.getShort() & 0xFFFF;
            _channelversion = new byte[length];
            bytes.get(_channelversion);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_channelversion.length);
            bytes.put(_channelversion);
        }

        @Override
        public String toString()
        {
            String output = "-- SimData --\n";
            try {
                output += Helpers.FieldToString(_channelversion, "ChannelVersion") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimDataBlock createSimDataBlock() {
         return new SimDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AgentMovementComplete; }
    public AgentDataBlock AgentData;
    public DataBlock Data;
    public SimDataBlock SimData;

    public AgentMovementCompletePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)250);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        Data = new DataBlock();
        SimData = new SimDataBlock();
    }

    public AgentMovementCompletePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
        SimData = new SimDataBlock(bytes);
     }

    public AgentMovementCompletePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
        SimData = new SimDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += Data.getLength();
        length += SimData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        Data.ToBytes(bytes);
        SimData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AgentMovementComplete ---\n";
        output += AgentData.toString() + "\n";
        output += Data.toString() + "\n";
        output += SimData.toString() + "\n";
        return output;
    }
}
