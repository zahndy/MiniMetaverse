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

public class ParcelMediaUpdatePacket extends Packet
{
    public class DataBlockBlock
    {
        private byte[] _mediaurl;
        public byte[] getMediaURL() {
            return _mediaurl;
        }

        public void setMediaURL(byte[] value) throws Exception {
            if (value == null) {
                _mediaurl = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _mediaurl = new byte[value.length];
                System.arraycopy(value, 0, _mediaurl, 0, value.length);
            }
        }

        public UUID MediaID = null;
        public byte MediaAutoScale = 0;

        public int getLength(){
            int length = 17;
            if (getMediaURL() != null) { length += 1 + getMediaURL().length; }
            return length;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _mediaurl = new byte[length];
            bytes.get(_mediaurl);
            MediaID = new UUID(bytes);
            MediaAutoScale = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_mediaurl.length);
            bytes.put(_mediaurl);
            MediaID.GetBytes(bytes);
            bytes.put(MediaAutoScale);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += Helpers.FieldToString(_mediaurl, "MediaURL") + "\n";
                output += "MediaID: " + MediaID.toString() + "\n";
                output += "MediaAutoScale: " + Byte.toString(MediaAutoScale) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlockBlock createDataBlockBlock() {
         return new DataBlockBlock();
    }

    public class DataBlockExtendedBlock
    {
        private byte[] _mediatype;
        public byte[] getMediaType() {
            return _mediatype;
        }

        public void setMediaType(byte[] value) throws Exception {
            if (value == null) {
                _mediatype = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _mediatype = new byte[value.length];
                System.arraycopy(value, 0, _mediatype, 0, value.length);
            }
        }

        private byte[] _mediadesc;
        public byte[] getMediaDesc() {
            return _mediadesc;
        }

        public void setMediaDesc(byte[] value) throws Exception {
            if (value == null) {
                _mediadesc = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _mediadesc = new byte[value.length];
                System.arraycopy(value, 0, _mediadesc, 0, value.length);
            }
        }

        public int MediaWidth = 0;
        public int MediaHeight = 0;
        public byte MediaLoop = 0;

        public int getLength(){
            int length = 9;
            if (getMediaType() != null) { length += 1 + getMediaType().length; }
            if (getMediaDesc() != null) { length += 1 + getMediaDesc().length; }
            return length;
        }

        public DataBlockExtendedBlock() { }
        public DataBlockExtendedBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _mediatype = new byte[length];
            bytes.get(_mediatype);
            length = bytes.get() & 0xFF;
            _mediadesc = new byte[length];
            bytes.get(_mediadesc);
            MediaWidth = bytes.getInt();
            MediaHeight = bytes.getInt();
            MediaLoop = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_mediatype.length);
            bytes.put(_mediatype);
            bytes.put((byte)_mediadesc.length);
            bytes.put(_mediadesc);
            bytes.putInt(MediaWidth);
            bytes.putInt(MediaHeight);
            bytes.put(MediaLoop);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlockExtended --\n";
            try {
                output += Helpers.FieldToString(_mediatype, "MediaType") + "\n";
                output += Helpers.FieldToString(_mediadesc, "MediaDesc") + "\n";
                output += "MediaWidth: " + Integer.toString(MediaWidth) + "\n";
                output += "MediaHeight: " + Integer.toString(MediaHeight) + "\n";
                output += "MediaLoop: " + Byte.toString(MediaLoop) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlockExtendedBlock createDataBlockExtendedBlock() {
         return new DataBlockExtendedBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ParcelMediaUpdate; }
    public DataBlockBlock DataBlock;
    public DataBlockExtendedBlock DataBlockExtended;

    public ParcelMediaUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)420);
        header.setReliable(true);
        DataBlock = new DataBlockBlock();
        DataBlockExtended = new DataBlockExtendedBlock();
    }

    public ParcelMediaUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        DataBlock = new DataBlockBlock(bytes);
        DataBlockExtended = new DataBlockExtendedBlock(bytes);
     }

    public ParcelMediaUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        DataBlock = new DataBlockBlock(bytes);
        DataBlockExtended = new DataBlockExtendedBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += DataBlock.getLength();
        length += DataBlockExtended.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        DataBlock.ToBytes(bytes);
        DataBlockExtended.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelMediaUpdate ---\n";
        output += DataBlock.toString() + "\n";
        output += DataBlockExtended.toString() + "\n";
        return output;
    }
}
