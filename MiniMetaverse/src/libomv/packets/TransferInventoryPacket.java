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

public class TransferInventoryPacket extends Packet
{
    public class InfoBlockBlock
    {
        public UUID SourceID = null;
        public UUID DestID = null;
        public UUID TransactionID = null;

        public int getLength(){
            return 48;
        }

        public InfoBlockBlock() { }
        public InfoBlockBlock(ByteBuffer bytes)
        {
            SourceID = new UUID(bytes);
            DestID = new UUID(bytes);
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            SourceID.GetBytes(bytes);
            DestID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- InfoBlock --\n";
            try {
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "DestID: " + DestID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlockBlock createInfoBlockBlock() {
         return new InfoBlockBlock();
    }

    public class InventoryBlockBlock
    {
        public UUID InventoryID = null;
        public byte Type = 0;

        public int getLength(){
            return 17;
        }

        public InventoryBlockBlock() { }
        public InventoryBlockBlock(ByteBuffer bytes)
        {
            InventoryID = new UUID(bytes);
            Type = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            InventoryID.GetBytes(bytes);
            bytes.put(Type);
        }

        @Override
        public String toString()
        {
            String output = "-- InventoryBlock --\n";
            try {
                output += "InventoryID: " + InventoryID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InventoryBlockBlock createInventoryBlockBlock() {
         return new InventoryBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TransferInventory; }
    public InfoBlockBlock InfoBlock;
    public InventoryBlockBlock[] InventoryBlock;

    public TransferInventoryPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)295);
        header.setReliable(true);
        InfoBlock = new InfoBlockBlock();
        InventoryBlock = new InventoryBlockBlock[0];
    }

    public TransferInventoryPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        InfoBlock = new InfoBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        InventoryBlock = new InventoryBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            InventoryBlock[j] = new InventoryBlockBlock(bytes);
        }
     }

    public TransferInventoryPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        InfoBlock = new InfoBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        InventoryBlock = new InventoryBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            InventoryBlock[j] = new InventoryBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += InfoBlock.getLength();
        length++;
        for (int j = 0; j < InventoryBlock.length; j++) { length += InventoryBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        InfoBlock.ToBytes(bytes);
        bytes.put((byte)InventoryBlock.length);
        for (int j = 0; j < InventoryBlock.length; j++) { InventoryBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TransferInventory ---\n";
        output += InfoBlock.toString() + "\n";
        for (int j = 0; j < InventoryBlock.length; j++)
        {
            output += InventoryBlock[j].toString() + "\n";
        }
        return output;
    }
}
