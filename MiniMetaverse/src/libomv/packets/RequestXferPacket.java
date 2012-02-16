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
import libomv.types.OverflowException;
import libomv.types.UUID;

public class RequestXferPacket extends Packet
{
    public class XferIDBlock
    {
        public long ID = 0;
        private byte[] _filename;
        public byte[] getFilename() {
            return _filename;
        }

        public void setFilename(byte[] value) throws Exception {
            if (value == null) {
                _filename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _filename = new byte[value.length];
                System.arraycopy(value, 0, _filename, 0, value.length);
            }
        }

        public byte FilePath = 0;
        public boolean DeleteOnCompletion = false;
        public boolean UseBigPackets = false;
        public UUID VFileID = null;
        public short VFileType = 0;

        public int getLength(){
            int length = 29;
            if (getFilename() != null) { length += 1 + getFilename().length; }
            return length;
        }

        public XferIDBlock() { }
        public XferIDBlock(ByteBuffer bytes)
        {
            int length;
            ID = bytes.getLong();
            length = bytes.get() & 0xFF;
            _filename = new byte[length];
            bytes.get(_filename);
            FilePath = bytes.get();
            DeleteOnCompletion = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            UseBigPackets = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            VFileID = new UUID(bytes);
            VFileType = bytes.getShort();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(ID);
            bytes.put((byte)_filename.length);
            bytes.put(_filename);
            bytes.put(FilePath);
            bytes.put((byte)((DeleteOnCompletion) ? 1 : 0));
            bytes.put((byte)((UseBigPackets) ? 1 : 0));
            VFileID.GetBytes(bytes);
            bytes.putShort(VFileType);
        }

        @Override
        public String toString()
        {
            String output = "-- XferID --\n";
            try {
                output += "ID: " + Long.toString(ID) + "\n";
                output += Helpers.FieldToString(_filename, "Filename") + "\n";
                output += "FilePath: " + Byte.toString(FilePath) + "\n";
                output += "DeleteOnCompletion: " + Boolean.toString(DeleteOnCompletion) + "\n";
                output += "UseBigPackets: " + Boolean.toString(UseBigPackets) + "\n";
                output += "VFileID: " + VFileID.toString() + "\n";
                output += "VFileType: " + Short.toString(VFileType) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public XferIDBlock createXferIDBlock() {
         return new XferIDBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RequestXfer; }
    public XferIDBlock XferID;

    public RequestXferPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)156);
        header.setReliable(true);
        XferID = new XferIDBlock();
    }

    public RequestXferPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        XferID = new XferIDBlock(bytes);
     }

    public RequestXferPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        XferID = new XferIDBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += XferID.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        XferID.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RequestXfer ---\n";
        output += XferID.toString() + "\n";
        return output;
    }
}
