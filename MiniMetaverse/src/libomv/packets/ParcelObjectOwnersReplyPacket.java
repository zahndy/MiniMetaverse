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

public class ParcelObjectOwnersReplyPacket extends Packet
{
    public class DataBlock
    {
        public UUID OwnerID = null;
        public boolean IsGroupOwned = false;
        public int Count = 0;
        public boolean OnlineStatus = false;

        public int getLength(){
            return 22;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            OwnerID = new UUID(bytes);
            IsGroupOwned = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Count = bytes.getInt();
            OnlineStatus = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            OwnerID.GetBytes(bytes);
            bytes.put((byte)((IsGroupOwned) ? 1 : 0));
            bytes.putInt(Count);
            bytes.put((byte)((OnlineStatus) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "IsGroupOwned: " + Boolean.toString(IsGroupOwned) + "\n";
                output += "Count: " + Integer.toString(Count) + "\n";
                output += "OnlineStatus: " + Boolean.toString(OnlineStatus) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ParcelObjectOwnersReply; }
    public DataBlock[] Data;

    public ParcelObjectOwnersReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)57);
        header.setReliable(true);
        Data = new DataBlock[0];
    }

    public ParcelObjectOwnersReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        {
            Data[j] = new DataBlock(bytes);
        }
     }

    public ParcelObjectOwnersReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        {
            Data[j] = new DataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < Data.length; j++) { length += Data[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)Data.length);
        for (int j = 0; j < Data.length; j++) { Data[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelObjectOwnersReply ---\n";
        for (int j = 0; j < Data.length; j++)
        {
            output += Data[j].toString() + "\n";
        }
        return output;
    }
}
