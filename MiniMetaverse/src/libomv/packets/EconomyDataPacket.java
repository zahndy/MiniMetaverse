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

public class EconomyDataPacket extends Packet
{
    public class InfoBlock
    {
        public int ObjectCapacity = 0;
        public int ObjectCount = 0;
        public int PriceEnergyUnit = 0;
        public int PriceObjectClaim = 0;
        public int PricePublicObjectDecay = 0;
        public int PricePublicObjectDelete = 0;
        public int PriceParcelClaim = 0;
        public float PriceParcelClaimFactor = 0;
        public int PriceUpload = 0;
        public int PriceRentLight = 0;
        public int TeleportMinPrice = 0;
        public float TeleportPriceExponent = 0;
        public float EnergyEfficiency = 0;
        public float PriceObjectRent = 0;
        public float PriceObjectScaleFactor = 0;
        public int PriceParcelRent = 0;
        public int PriceGroupCreate = 0;

        public int getLength(){
            return 68;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            ObjectCapacity = bytes.getInt();
            ObjectCount = bytes.getInt();
            PriceEnergyUnit = bytes.getInt();
            PriceObjectClaim = bytes.getInt();
            PricePublicObjectDecay = bytes.getInt();
            PricePublicObjectDelete = bytes.getInt();
            PriceParcelClaim = bytes.getInt();
            PriceParcelClaimFactor = bytes.getFloat();
            PriceUpload = bytes.getInt();
            PriceRentLight = bytes.getInt();
            TeleportMinPrice = bytes.getInt();
            TeleportPriceExponent = bytes.getFloat();
            EnergyEfficiency = bytes.getFloat();
            PriceObjectRent = bytes.getFloat();
            PriceObjectScaleFactor = bytes.getFloat();
            PriceParcelRent = bytes.getInt();
            PriceGroupCreate = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(ObjectCapacity);
            bytes.putInt(ObjectCount);
            bytes.putInt(PriceEnergyUnit);
            bytes.putInt(PriceObjectClaim);
            bytes.putInt(PricePublicObjectDecay);
            bytes.putInt(PricePublicObjectDelete);
            bytes.putInt(PriceParcelClaim);
            bytes.putFloat(PriceParcelClaimFactor);
            bytes.putInt(PriceUpload);
            bytes.putInt(PriceRentLight);
            bytes.putInt(TeleportMinPrice);
            bytes.putFloat(TeleportPriceExponent);
            bytes.putFloat(EnergyEfficiency);
            bytes.putFloat(PriceObjectRent);
            bytes.putFloat(PriceObjectScaleFactor);
            bytes.putInt(PriceParcelRent);
            bytes.putInt(PriceGroupCreate);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "ObjectCapacity: " + Integer.toString(ObjectCapacity) + "\n";
                output += "ObjectCount: " + Integer.toString(ObjectCount) + "\n";
                output += "PriceEnergyUnit: " + Integer.toString(PriceEnergyUnit) + "\n";
                output += "PriceObjectClaim: " + Integer.toString(PriceObjectClaim) + "\n";
                output += "PricePublicObjectDecay: " + Integer.toString(PricePublicObjectDecay) + "\n";
                output += "PricePublicObjectDelete: " + Integer.toString(PricePublicObjectDelete) + "\n";
                output += "PriceParcelClaim: " + Integer.toString(PriceParcelClaim) + "\n";
                output += "PriceParcelClaimFactor: " + Float.toString(PriceParcelClaimFactor) + "\n";
                output += "PriceUpload: " + Integer.toString(PriceUpload) + "\n";
                output += "PriceRentLight: " + Integer.toString(PriceRentLight) + "\n";
                output += "TeleportMinPrice: " + Integer.toString(TeleportMinPrice) + "\n";
                output += "TeleportPriceExponent: " + Float.toString(TeleportPriceExponent) + "\n";
                output += "EnergyEfficiency: " + Float.toString(EnergyEfficiency) + "\n";
                output += "PriceObjectRent: " + Float.toString(PriceObjectRent) + "\n";
                output += "PriceObjectScaleFactor: " + Float.toString(PriceObjectScaleFactor) + "\n";
                output += "PriceParcelRent: " + Integer.toString(PriceParcelRent) + "\n";
                output += "PriceGroupCreate: " + Integer.toString(PriceGroupCreate) + "\n";
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
    public PacketType getType() { return PacketType.EconomyData; }
    public InfoBlock Info;

    public EconomyDataPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)25);
        header.setReliable(true);
        Info = new InfoBlock();
    }

    public EconomyDataPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Info = new InfoBlock(bytes);
     }

    public EconomyDataPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Info = new InfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Info.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Info.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- EconomyData ---\n";
        output += Info.toString() + "\n";
        return output;
    }
}
