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

public class PreloadSoundPacket extends Packet
{
    public class DataBlockBlock
    {
        public UUID ObjectID = null;
        public UUID OwnerID = null;
        public UUID SoundID = null;

        public int getLength(){
            return 48;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            SoundID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            SoundID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "SoundID: " + SoundID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlockBlock createDataBlockBlock() {
         return new DataBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.PreloadSound; }
    public DataBlockBlock[] DataBlock;

    public PreloadSoundPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)15);
        header.setReliable(true);
        DataBlock = new DataBlockBlock[0];
    }

    public PreloadSoundPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        int count = bytes.get() & 0xFF;
        DataBlock = new DataBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            DataBlock[j] = new DataBlockBlock(bytes);
        }
     }

    public PreloadSoundPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        DataBlock = new DataBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            DataBlock[j] = new DataBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < DataBlock.length; j++) { length += DataBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)DataBlock.length);
        for (int j = 0; j < DataBlock.length; j++) { DataBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PreloadSound ---\n";
        for (int j = 0; j < DataBlock.length; j++)
        {
            output += DataBlock[j].toString() + "\n";
        }
        return output;
    }
}
