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
import libomv.types.Vector3;

public class CrossedRegionPacket extends Packet
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

    public class RegionDataBlock
    {
        public int SimIP = 0;
        public short SimPort = 0;
        public long RegionHandle = 0;
        private byte[] _seedcapability;
        public byte[] getSeedCapability() {
            return _seedcapability;
        }

        public void setSeedCapability(byte[] value) throws Exception {
            if (value == null) {
                _seedcapability = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _seedcapability = new byte[value.length];
                System.arraycopy(value, 0, _seedcapability, 0, value.length);
            }
        }


        public int getLength(){
            int length = 14;
            if (getSeedCapability() != null) { length += 2 + getSeedCapability().length; }
            return length;
        }

        public RegionDataBlock() { }
        public RegionDataBlock(ByteBuffer bytes)
        {
            int length;
            SimIP = bytes.getInt();
            SimPort = (short)((bytes.get() << 8) + bytes.get());
            RegionHandle = bytes.getLong();
            length = bytes.getShort() & 0xFFFF;
            _seedcapability = new byte[length];
            bytes.get(_seedcapability);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(SimIP);
            bytes.put((byte)((SimPort >> 8) % 256));
            bytes.put((byte)(SimPort % 256));
            bytes.putLong(RegionHandle);
            bytes.putShort((short)_seedcapability.length);
            bytes.put(_seedcapability);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionData --\n";
            try {
                output += "SimIP: " + Integer.toString(SimIP) + "\n";
                output += "SimPort: " + Short.toString(SimPort) + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += Helpers.FieldToString(_seedcapability, "SeedCapability") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionDataBlock createRegionDataBlock() {
         return new RegionDataBlock();
    }

    public class InfoBlock
    {
        public Vector3 Position = null;
        public Vector3 LookAt = null;

        public int getLength(){
            return 24;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            Position = new Vector3(bytes);
            LookAt = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            Position.GetBytes(bytes);
            LookAt.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "Position: " + Position.toString() + "\n";
                output += "LookAt: " + LookAt.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlock createInfoBlock() {
         return new InfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.CrossedRegion; }
    public AgentDataBlock AgentData;
    public RegionDataBlock RegionData;
    public InfoBlock Info;

    public CrossedRegionPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)7);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RegionData = new RegionDataBlock();
        Info = new InfoBlock();
    }

    public CrossedRegionPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        AgentData = new AgentDataBlock(bytes);
        RegionData = new RegionDataBlock(bytes);
        Info = new InfoBlock(bytes);
     }

    public CrossedRegionPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RegionData = new RegionDataBlock(bytes);
        Info = new InfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RegionData.getLength();
        length += Info.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        RegionData.ToBytes(bytes);
        Info.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- CrossedRegion ---\n";
        output += AgentData.toString() + "\n";
        output += RegionData.toString() + "\n";
        output += Info.toString() + "\n";
        return output;
    }
}
