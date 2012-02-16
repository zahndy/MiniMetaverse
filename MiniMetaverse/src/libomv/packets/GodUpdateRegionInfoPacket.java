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

public class GodUpdateRegionInfoPacket extends Packet
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

    public class RegionInfoBlock
    {
        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }

        public int EstateID = 0;
        public int ParentEstateID = 0;
        public int RegionFlags = 0;
        public float BillableFactor = 0;
        public int PricePerMeter = 0;
        public int RedirectGridX = 0;
        public int RedirectGridY = 0;

        public int getLength(){
            int length = 28;
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public RegionInfoBlock() { }
        public RegionInfoBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            EstateID = bytes.getInt();
            ParentEstateID = bytes.getInt();
            RegionFlags = bytes.getInt();
            BillableFactor = bytes.getFloat();
            PricePerMeter = bytes.getInt();
            RedirectGridX = bytes.getInt();
            RedirectGridY = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            bytes.putInt(EstateID);
            bytes.putInt(ParentEstateID);
            bytes.putInt(RegionFlags);
            bytes.putFloat(BillableFactor);
            bytes.putInt(PricePerMeter);
            bytes.putInt(RedirectGridX);
            bytes.putInt(RedirectGridY);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionInfo --\n";
            try {
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "ParentEstateID: " + Integer.toString(ParentEstateID) + "\n";
                output += "RegionFlags: " + Integer.toString(RegionFlags) + "\n";
                output += "BillableFactor: " + Float.toString(BillableFactor) + "\n";
                output += "PricePerMeter: " + Integer.toString(PricePerMeter) + "\n";
                output += "RedirectGridX: " + Integer.toString(RedirectGridX) + "\n";
                output += "RedirectGridY: " + Integer.toString(RedirectGridY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionInfoBlock createRegionInfoBlock() {
         return new RegionInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GodUpdateRegionInfo; }
    public AgentDataBlock AgentData;
    public RegionInfoBlock RegionInfo;

    public GodUpdateRegionInfoPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)143);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RegionInfo = new RegionInfoBlock();
    }

    public GodUpdateRegionInfoPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        RegionInfo = new RegionInfoBlock(bytes);
     }

    public GodUpdateRegionInfoPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RegionInfo = new RegionInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RegionInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        RegionInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GodUpdateRegionInfo ---\n";
        output += AgentData.toString() + "\n";
        output += RegionInfo.toString() + "\n";
        return output;
    }
}
