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

public class CreateTrustedCircuitPacket extends Packet
{
    public class DataBlockBlock
    {
        public UUID EndPointID = null;
        public byte[] Digest = null;

        public int getLength(){
            return 48;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            EndPointID = new UUID(bytes);
            Digest = new byte[32];
            bytes.get(Digest);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            EndPointID.GetBytes(bytes);
            bytes.put(Digest);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += "EndPointID: " + EndPointID.toString() + "\n";
                output += Helpers.FieldToString(Digest, "Digest") + "\n";
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
    public PacketType getType() { return PacketType.CreateTrustedCircuit; }
    public DataBlockBlock DataBlock;

    public CreateTrustedCircuitPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)392);
        header.setReliable(true);
        DataBlock = new DataBlockBlock();
    }

    public CreateTrustedCircuitPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        DataBlock = new DataBlockBlock(bytes);
     }

    public CreateTrustedCircuitPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        DataBlock = new DataBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += DataBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        DataBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- CreateTrustedCircuit ---\n";
        output += DataBlock.toString() + "\n";
        return output;
    }
}
