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

public class RegionInfoPacket extends Packet
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
        public byte SimAccess = 0;
        public byte MaxAgents = 0;
        public float BillableFactor = 0;
        public float ObjectBonusFactor = 0;
        public float WaterHeight = 0;
        public float TerrainRaiseLimit = 0;
        public float TerrainLowerLimit = 0;
        public int PricePerMeter = 0;
        public int RedirectGridX = 0;
        public int RedirectGridY = 0;
        public boolean UseEstateSun = false;
        public float SunHour = 0;

        public int getLength(){
            int length = 51;
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
            SimAccess = bytes.get();
            MaxAgents = bytes.get();
            BillableFactor = bytes.getFloat();
            ObjectBonusFactor = bytes.getFloat();
            WaterHeight = bytes.getFloat();
            TerrainRaiseLimit = bytes.getFloat();
            TerrainLowerLimit = bytes.getFloat();
            PricePerMeter = bytes.getInt();
            RedirectGridX = bytes.getInt();
            RedirectGridY = bytes.getInt();
            UseEstateSun = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            SunHour = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            bytes.putInt(EstateID);
            bytes.putInt(ParentEstateID);
            bytes.putInt(RegionFlags);
            bytes.put(SimAccess);
            bytes.put(MaxAgents);
            bytes.putFloat(BillableFactor);
            bytes.putFloat(ObjectBonusFactor);
            bytes.putFloat(WaterHeight);
            bytes.putFloat(TerrainRaiseLimit);
            bytes.putFloat(TerrainLowerLimit);
            bytes.putInt(PricePerMeter);
            bytes.putInt(RedirectGridX);
            bytes.putInt(RedirectGridY);
            bytes.put((byte)((UseEstateSun) ? 1 : 0));
            bytes.putFloat(SunHour);
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
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output += "MaxAgents: " + Byte.toString(MaxAgents) + "\n";
                output += "BillableFactor: " + Float.toString(BillableFactor) + "\n";
                output += "ObjectBonusFactor: " + Float.toString(ObjectBonusFactor) + "\n";
                output += "WaterHeight: " + Float.toString(WaterHeight) + "\n";
                output += "TerrainRaiseLimit: " + Float.toString(TerrainRaiseLimit) + "\n";
                output += "TerrainLowerLimit: " + Float.toString(TerrainLowerLimit) + "\n";
                output += "PricePerMeter: " + Integer.toString(PricePerMeter) + "\n";
                output += "RedirectGridX: " + Integer.toString(RedirectGridX) + "\n";
                output += "RedirectGridY: " + Integer.toString(RedirectGridY) + "\n";
                output += "UseEstateSun: " + Boolean.toString(UseEstateSun) + "\n";
                output += "SunHour: " + Float.toString(SunHour) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionInfoBlock createRegionInfoBlock() {
         return new RegionInfoBlock();
    }

    public class RegionInfo2Block
    {
        private byte[] _productsku;
        public byte[] getProductSKU() {
            return _productsku;
        }

        public void setProductSKU(byte[] value) throws Exception {
            if (value == null) {
                _productsku = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _productsku = new byte[value.length];
                System.arraycopy(value, 0, _productsku, 0, value.length);
            }
        }

        private byte[] _productname;
        public byte[] getProductName() {
            return _productname;
        }

        public void setProductName(byte[] value) throws Exception {
            if (value == null) {
                _productname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _productname = new byte[value.length];
                System.arraycopy(value, 0, _productname, 0, value.length);
            }
        }

        public int MaxAgents32 = 0;
        public int HardMaxAgents = 0;
        public int HardMaxObjects = 0;

        public int getLength(){
            int length = 12;
            if (getProductSKU() != null) { length += 1 + getProductSKU().length; }
            if (getProductName() != null) { length += 1 + getProductName().length; }
            return length;
        }

        public RegionInfo2Block() { }
        public RegionInfo2Block(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _productsku = new byte[length];
            bytes.get(_productsku);
            length = bytes.get() & 0xFF;
            _productname = new byte[length];
            bytes.get(_productname);
            MaxAgents32 = bytes.getInt();
            HardMaxAgents = bytes.getInt();
            HardMaxObjects = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_productsku.length);
            bytes.put(_productsku);
            bytes.put((byte)_productname.length);
            bytes.put(_productname);
            bytes.putInt(MaxAgents32);
            bytes.putInt(HardMaxAgents);
            bytes.putInt(HardMaxObjects);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionInfo2 --\n";
            try {
                output += Helpers.FieldToString(_productsku, "ProductSKU") + "\n";
                output += Helpers.FieldToString(_productname, "ProductName") + "\n";
                output += "MaxAgents32: " + Integer.toString(MaxAgents32) + "\n";
                output += "HardMaxAgents: " + Integer.toString(HardMaxAgents) + "\n";
                output += "HardMaxObjects: " + Integer.toString(HardMaxObjects) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionInfo2Block createRegionInfo2Block() {
         return new RegionInfo2Block();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RegionInfo; }
    public AgentDataBlock AgentData;
    public RegionInfoBlock RegionInfo;
    public RegionInfo2Block RegionInfo2;

    public RegionInfoPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)142);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RegionInfo = new RegionInfoBlock();
        RegionInfo2 = new RegionInfo2Block();
    }

    public RegionInfoPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        RegionInfo = new RegionInfoBlock(bytes);
        RegionInfo2 = new RegionInfo2Block(bytes);
     }

    public RegionInfoPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RegionInfo = new RegionInfoBlock(bytes);
        RegionInfo2 = new RegionInfo2Block(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RegionInfo.getLength();
        length += RegionInfo2.getLength();
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
        RegionInfo2.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RegionInfo ---\n";
        output += AgentData.toString() + "\n";
        output += RegionInfo.toString() + "\n";
        output += RegionInfo2.toString() + "\n";
        return output;
    }
}
