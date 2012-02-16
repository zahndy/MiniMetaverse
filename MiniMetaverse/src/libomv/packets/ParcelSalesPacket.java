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

public class ParcelSalesPacket extends Packet
{
    public class ParcelDataBlock
    {
        public UUID ParcelID = null;
        public UUID BuyerID = null;

        public int getLength(){
            return 32;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            ParcelID = new UUID(bytes);
            BuyerID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ParcelID.GetBytes(bytes);
            BuyerID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += "BuyerID: " + BuyerID.toString() + "\n";
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
    public PacketType getType() { return PacketType.ParcelSales; }
    public ParcelDataBlock[] ParcelData;

    public ParcelSalesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)226);
        header.setReliable(true);
        ParcelData = new ParcelDataBlock[0];
    }

    public ParcelSalesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        ParcelData = new ParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParcelData[j] = new ParcelDataBlock(bytes);
        }
     }

    public ParcelSalesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        ParcelData = new ParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParcelData[j] = new ParcelDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < ParcelData.length; j++) { length += ParcelData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)ParcelData.length);
        for (int j = 0; j < ParcelData.length; j++) { ParcelData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelSales ---\n";
        for (int j = 0; j < ParcelData.length; j++)
        {
            output += ParcelData[j].toString() + "\n";
        }
        return output;
    }
}