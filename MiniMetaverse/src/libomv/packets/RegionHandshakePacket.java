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
import libomv.types.OverflowException;
import libomv.types.UUID;

public class RegionHandshakePacket extends Packet
{
    public class RegionInfoBlock
    {
        public int RegionFlags = 0;
        public byte SimAccess = 0;
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

        public UUID SimOwner = null;
        public boolean IsEstateManager = false;
        public float WaterHeight = 0;
        public float BillableFactor = 0;
        public UUID CacheID = null;
        public UUID TerrainBase0 = null;
        public UUID TerrainBase1 = null;
        public UUID TerrainBase2 = null;
        public UUID TerrainBase3 = null;
        public UUID TerrainDetail0 = null;
        public UUID TerrainDetail1 = null;
        public UUID TerrainDetail2 = null;
        public UUID TerrainDetail3 = null;
        public float TerrainStartHeight00 = 0;
        public float TerrainStartHeight01 = 0;
        public float TerrainStartHeight10 = 0;
        public float TerrainStartHeight11 = 0;
        public float TerrainHeightRange00 = 0;
        public float TerrainHeightRange01 = 0;
        public float TerrainHeightRange10 = 0;
        public float TerrainHeightRange11 = 0;

        public int getLength(){
            int length = 206;
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public RegionInfoBlock() { }
        public RegionInfoBlock(ByteBuffer bytes)
        {
            int length;
            RegionFlags = bytes.getInt();
            SimAccess = bytes.get();
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            SimOwner = new UUID(bytes);
            IsEstateManager = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            WaterHeight = bytes.getFloat();
            BillableFactor = bytes.getFloat();
            CacheID = new UUID(bytes);
            TerrainBase0 = new UUID(bytes);
            TerrainBase1 = new UUID(bytes);
            TerrainBase2 = new UUID(bytes);
            TerrainBase3 = new UUID(bytes);
            TerrainDetail0 = new UUID(bytes);
            TerrainDetail1 = new UUID(bytes);
            TerrainDetail2 = new UUID(bytes);
            TerrainDetail3 = new UUID(bytes);
            TerrainStartHeight00 = bytes.getFloat();
            TerrainStartHeight01 = bytes.getFloat();
            TerrainStartHeight10 = bytes.getFloat();
            TerrainStartHeight11 = bytes.getFloat();
            TerrainHeightRange00 = bytes.getFloat();
            TerrainHeightRange01 = bytes.getFloat();
            TerrainHeightRange10 = bytes.getFloat();
            TerrainHeightRange11 = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(RegionFlags);
            bytes.put(SimAccess);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            SimOwner.GetBytes(bytes);
            bytes.put((byte)((IsEstateManager) ? 1 : 0));
            bytes.putFloat(WaterHeight);
            bytes.putFloat(BillableFactor);
            CacheID.GetBytes(bytes);
            TerrainBase0.GetBytes(bytes);
            TerrainBase1.GetBytes(bytes);
            TerrainBase2.GetBytes(bytes);
            TerrainBase3.GetBytes(bytes);
            TerrainDetail0.GetBytes(bytes);
            TerrainDetail1.GetBytes(bytes);
            TerrainDetail2.GetBytes(bytes);
            TerrainDetail3.GetBytes(bytes);
            bytes.putFloat(TerrainStartHeight00);
            bytes.putFloat(TerrainStartHeight01);
            bytes.putFloat(TerrainStartHeight10);
            bytes.putFloat(TerrainStartHeight11);
            bytes.putFloat(TerrainHeightRange00);
            bytes.putFloat(TerrainHeightRange01);
            bytes.putFloat(TerrainHeightRange10);
            bytes.putFloat(TerrainHeightRange11);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionInfo --\n";
            try {
                output += "RegionFlags: " + Integer.toString(RegionFlags) + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "SimOwner: " + SimOwner.toString() + "\n";
                output += "IsEstateManager: " + Boolean.toString(IsEstateManager) + "\n";
                output += "WaterHeight: " + Float.toString(WaterHeight) + "\n";
                output += "BillableFactor: " + Float.toString(BillableFactor) + "\n";
                output += "CacheID: " + CacheID.toString() + "\n";
                output += "TerrainBase0: " + TerrainBase0.toString() + "\n";
                output += "TerrainBase1: " + TerrainBase1.toString() + "\n";
                output += "TerrainBase2: " + TerrainBase2.toString() + "\n";
                output += "TerrainBase3: " + TerrainBase3.toString() + "\n";
                output += "TerrainDetail0: " + TerrainDetail0.toString() + "\n";
                output += "TerrainDetail1: " + TerrainDetail1.toString() + "\n";
                output += "TerrainDetail2: " + TerrainDetail2.toString() + "\n";
                output += "TerrainDetail3: " + TerrainDetail3.toString() + "\n";
                output += "TerrainStartHeight00: " + Float.toString(TerrainStartHeight00) + "\n";
                output += "TerrainStartHeight01: " + Float.toString(TerrainStartHeight01) + "\n";
                output += "TerrainStartHeight10: " + Float.toString(TerrainStartHeight10) + "\n";
                output += "TerrainStartHeight11: " + Float.toString(TerrainStartHeight11) + "\n";
                output += "TerrainHeightRange00: " + Float.toString(TerrainHeightRange00) + "\n";
                output += "TerrainHeightRange01: " + Float.toString(TerrainHeightRange01) + "\n";
                output += "TerrainHeightRange10: " + Float.toString(TerrainHeightRange10) + "\n";
                output += "TerrainHeightRange11: " + Float.toString(TerrainHeightRange11) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionInfoBlock createRegionInfoBlock() {
         return new RegionInfoBlock();
    }

    public class RegionInfo3Block
    {
        public int CPUClassID = 0;
        public int CPURatio = 0;
        private byte[] _coloname;
        public byte[] getColoName() {
            return _coloname;
        }

        public void setColoName(byte[] value) throws Exception {
            if (value == null) {
                _coloname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _coloname = new byte[value.length];
                System.arraycopy(value, 0, _coloname, 0, value.length);
            }
        }

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


        public int getLength(){
            int length = 8;
            if (getColoName() != null) { length += 1 + getColoName().length; }
            if (getProductSKU() != null) { length += 1 + getProductSKU().length; }
            if (getProductName() != null) { length += 1 + getProductName().length; }
            return length;
        }

        public RegionInfo3Block() { }
        public RegionInfo3Block(ByteBuffer bytes)
        {
            int length;
            CPUClassID = bytes.getInt();
            CPURatio = bytes.getInt();
            length = bytes.get() & 0xFF;
            _coloname = new byte[length];
            bytes.get(_coloname);
            length = bytes.get() & 0xFF;
            _productsku = new byte[length];
            bytes.get(_productsku);
            length = bytes.get() & 0xFF;
            _productname = new byte[length];
            bytes.get(_productname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(CPUClassID);
            bytes.putInt(CPURatio);
            bytes.put((byte)_coloname.length);
            bytes.put(_coloname);
            bytes.put((byte)_productsku.length);
            bytes.put(_productsku);
            bytes.put((byte)_productname.length);
            bytes.put(_productname);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionInfo3 --\n";
            try {
                output += "CPUClassID: " + Integer.toString(CPUClassID) + "\n";
                output += "CPURatio: " + Integer.toString(CPURatio) + "\n";
                output += Helpers.FieldToString(_coloname, "ColoName") + "\n";
                output += Helpers.FieldToString(_productsku, "ProductSKU") + "\n";
                output += Helpers.FieldToString(_productname, "ProductName") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionInfo3Block createRegionInfo3Block() {
         return new RegionInfo3Block();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RegionHandshake; }
    public RegionInfoBlock RegionInfo;
    public UUID RegionID;
    public RegionInfo3Block RegionInfo3;

    public RegionHandshakePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)148);
        header.setReliable(true);
        RegionInfo = new RegionInfoBlock();
        RegionID = new UUID();
        RegionInfo3 = new RegionInfo3Block();
    }

    public RegionHandshakePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        RegionInfo = new RegionInfoBlock(bytes);
        RegionID = new UUID(bytes);
        RegionInfo3 = new RegionInfo3Block(bytes);
     }

    public RegionHandshakePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        RegionInfo = new RegionInfoBlock(bytes);
        RegionID = new UUID(bytes);
        RegionInfo3 = new RegionInfo3Block(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += RegionInfo.getLength();
        length += 16;
        length += RegionInfo3.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        RegionInfo.ToBytes(bytes);
        RegionID.GetBytes(bytes);
        RegionInfo3.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RegionHandshake ---\n";
        output += RegionInfo.toString() + "\n";
        output += "RegionID: " + RegionID.toString() + "\n";
        output += RegionInfo3.toString() + "\n";
        return output;
    }
}
