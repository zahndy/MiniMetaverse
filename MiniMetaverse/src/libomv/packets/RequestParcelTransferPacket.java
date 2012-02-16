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

public class RequestParcelTransferPacket extends Packet
{
    public class DataBlock
    {
        public UUID TransactionID = null;
        public int TransactionTime = 0;
        public UUID SourceID = null;
        public UUID DestID = null;
        public UUID OwnerID = null;
        public byte Flags = 0;
        public int TransactionType = 0;
        public int Amount = 0;
        public int BillableArea = 0;
        public int ActualArea = 0;
        public boolean Final = false;

        public int getLength(){
            return 86;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            TransactionID = new UUID(bytes);
            TransactionTime = bytes.getInt();
            SourceID = new UUID(bytes);
            DestID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            Flags = bytes.get();
            TransactionType = bytes.getInt();
            Amount = bytes.getInt();
            BillableArea = bytes.getInt();
            ActualArea = bytes.getInt();
            Final = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
            bytes.putInt(TransactionTime);
            SourceID.GetBytes(bytes);
            DestID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.put(Flags);
            bytes.putInt(TransactionType);
            bytes.putInt(Amount);
            bytes.putInt(BillableArea);
            bytes.putInt(ActualArea);
            bytes.put((byte)((Final) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "TransactionTime: " + Integer.toString(TransactionTime) + "\n";
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "DestID: " + DestID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
                output += "TransactionType: " + Integer.toString(TransactionType) + "\n";
                output += "Amount: " + Integer.toString(Amount) + "\n";
                output += "BillableArea: " + Integer.toString(BillableArea) + "\n";
                output += "ActualArea: " + Integer.toString(ActualArea) + "\n";
                output += "Final: " + Boolean.toString(Final) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    public class RegionDataBlock
    {
        public UUID RegionID = null;
        public int GridX = 0;
        public int GridY = 0;

        public int getLength(){
            return 24;
        }

        public RegionDataBlock() { }
        public RegionDataBlock(ByteBuffer bytes)
        {
            RegionID = new UUID(bytes);
            GridX = bytes.getInt();
            GridY = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RegionID.GetBytes(bytes);
            bytes.putInt(GridX);
            bytes.putInt(GridY);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionData --\n";
            try {
                output += "RegionID: " + RegionID.toString() + "\n";
                output += "GridX: " + Integer.toString(GridX) + "\n";
                output += "GridY: " + Integer.toString(GridY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionDataBlock createRegionDataBlock() {
         return new RegionDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RequestParcelTransfer; }
    public DataBlock Data;
    public RegionDataBlock RegionData;

    public RequestParcelTransferPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)220);
        header.setReliable(true);
        Data = new DataBlock();
        RegionData = new RegionDataBlock();
    }

    public RequestParcelTransferPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
        RegionData = new RegionDataBlock(bytes);
     }

    public RequestParcelTransferPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
        RegionData = new RegionDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        length += RegionData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        RegionData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RequestParcelTransfer ---\n";
        output += Data.toString() + "\n";
        output += RegionData.toString() + "\n";
        return output;
    }
}
