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
import libomv.types.OverflowException;

public class ImageDataPacket extends Packet
{
    public class ImageIDBlock
    {
        public UUID ID = null;
        public byte Codec = 0;
        public int Size = 0;
        public short Packets = 0;

        public int getLength(){
            return 23;
        }

        public ImageIDBlock() { }
        public ImageIDBlock(ByteBuffer bytes)
        {
            ID = new UUID(bytes);
            Codec = bytes.get();
            Size = bytes.getInt();
            Packets = bytes.getShort();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
            bytes.put(Codec);
            bytes.putInt(Size);
            bytes.putShort(Packets);
        }

        @Override
        public String toString()
        {
            String output = "-- ImageID --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output += "Codec: " + Byte.toString(Codec) + "\n";
                output += "Size: " + Integer.toString(Size) + "\n";
                output += "Packets: " + Short.toString(Packets) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ImageIDBlock createImageIDBlock() {
         return new ImageIDBlock();
    }

    public class ImageDataBlock
    {
        private byte[] _data;
        public byte[] getData() {
            return _data;
        }

        public void setData(byte[] value) throws Exception {
            if (value == null) {
                _data = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _data = new byte[value.length];
                System.arraycopy(value, 0, _data, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getData() != null) { length += 2 + getData().length; }
            return length;
        }

        public ImageDataBlock() { }
        public ImageDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.getShort() & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_data.length);
            bytes.put(_data);
        }

        @Override
        public String toString()
        {
            String output = "-- ImageData --\n";
            try {
                output += Helpers.FieldToString(_data, "Data") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ImageDataBlock createImageDataBlock() {
         return new ImageDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ImageData; }
    public ImageIDBlock ImageID;
    public ImageDataBlock ImageData;

    public ImageDataPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)9);
        header.setReliable(true);
        ImageID = new ImageIDBlock();
        ImageData = new ImageDataBlock();
    }

    public ImageDataPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        ImageID = new ImageIDBlock(bytes);
        ImageData = new ImageDataBlock(bytes);
     }

    public ImageDataPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ImageID = new ImageIDBlock(bytes);
        ImageData = new ImageDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ImageID.getLength();
        length += ImageData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ImageID.ToBytes(bytes);
        ImageData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ImageData ---\n";
        output += ImageID.toString() + "\n";
        output += ImageData.toString() + "\n";
        return output;
    }
}
