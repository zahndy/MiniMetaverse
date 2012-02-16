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

public class AssetUploadCompletePacket extends Packet
{
    public class AssetBlockBlock
    {
        public UUID UUID = null;
        public byte Type = 0;
        public boolean Success = false;

        public int getLength(){
            return 18;
        }

        public AssetBlockBlock() { }
        public AssetBlockBlock(ByteBuffer bytes)
        {
            UUID = new UUID(bytes);
            Type = bytes.get();
            Success = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            UUID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put((byte)((Success) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- AssetBlock --\n";
            try {
                output += "UUID: " + UUID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "Success: " + Boolean.toString(Success) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AssetBlockBlock createAssetBlockBlock() {
         return new AssetBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AssetUploadComplete; }
    public AssetBlockBlock AssetBlock;

    public AssetUploadCompletePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)334);
        header.setReliable(true);
        AssetBlock = new AssetBlockBlock();
    }

    public AssetUploadCompletePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AssetBlock = new AssetBlockBlock(bytes);
     }

    public AssetUploadCompletePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AssetBlock = new AssetBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AssetBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AssetBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AssetUploadComplete ---\n";
        output += AssetBlock.toString() + "\n";
        return output;
    }
}
