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

public class LogParcelChangesPacket extends Packet
{
    public class ParcelDataBlock
    {
        public UUID ParcelID = null;
        public UUID OwnerID = null;
        public boolean IsOwnerGroup = false;
        public int ActualArea = 0;
        public byte Action = 0;
        public UUID TransactionID = null;

        public int getLength(){
            return 54;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            ParcelID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            IsOwnerGroup = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            ActualArea = bytes.getInt();
            Action = bytes.get();
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ParcelID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.put((byte)((IsOwnerGroup) ? 1 : 0));
            bytes.putInt(ActualArea);
            bytes.put(Action);
            TransactionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "IsOwnerGroup: " + Boolean.toString(IsOwnerGroup) + "\n";
                output += "ActualArea: " + Integer.toString(ActualArea) + "\n";
                output += "Action: " + Byte.toString(Action) + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
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
    public PacketType getType() { return PacketType.LogParcelChanges; }
    public UUID AgentID;
    public long RegionHandle;
    public ParcelDataBlock[] ParcelData;

    public LogParcelChangesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)224);
        header.setReliable(true);
        AgentID = new UUID();
        ParcelData = new ParcelDataBlock[0];
    }

    public LogParcelChangesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        RegionHandle = bytes.getLong();
        int count = bytes.get() & 0xFF;
        ParcelData = new ParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParcelData[j] = new ParcelDataBlock(bytes);
        }
     }

    public LogParcelChangesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        RegionHandle = bytes.getLong();
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
        length += 16;
        length += 8;
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
        AgentID.GetBytes(bytes);
        bytes.putLong(RegionHandle);
        bytes.put((byte)ParcelData.length);
        for (int j = 0; j < ParcelData.length; j++) { ParcelData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LogParcelChanges ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
        for (int j = 0; j < ParcelData.length; j++)
        {
            output += ParcelData[j].toString() + "\n";
        }
        return output;
    }
}
