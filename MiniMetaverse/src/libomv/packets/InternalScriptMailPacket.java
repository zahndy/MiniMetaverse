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

public class InternalScriptMailPacket extends Packet
{
    public class DataBlockBlock
    {
        private byte[] _from;
        public byte[] getFrom() {
            return _from;
        }

        public void setFrom(byte[] value) throws Exception {
            if (value == null) {
                _from = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _from = new byte[value.length];
                System.arraycopy(value, 0, _from, 0, value.length);
            }
        }

        public UUID To = null;
        private byte[] _subject;
        public byte[] getSubject() {
            return _subject;
        }

        public void setSubject(byte[] value) throws Exception {
            if (value == null) {
                _subject = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _subject = new byte[value.length];
                System.arraycopy(value, 0, _subject, 0, value.length);
            }
        }

        private byte[] _body;
        public byte[] getBody() {
            return _body;
        }

        public void setBody(byte[] value) throws Exception {
            if (value == null) {
                _body = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _body = new byte[value.length];
                System.arraycopy(value, 0, _body, 0, value.length);
            }
        }


        public int getLength(){
            int length = 16;
            if (getFrom() != null) { length += 1 + getFrom().length; }
            if (getSubject() != null) { length += 1 + getSubject().length; }
            if (getBody() != null) { length += 2 + getBody().length; }
            return length;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _from = new byte[length];
            bytes.get(_from);
            To = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _subject = new byte[length];
            bytes.get(_subject);
            length = bytes.getShort() & 0xFFFF;
            _body = new byte[length];
            bytes.get(_body);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_from.length);
            bytes.put(_from);
            To.GetBytes(bytes);
            bytes.put((byte)_subject.length);
            bytes.put(_subject);
            bytes.putShort((short)_body.length);
            bytes.put(_body);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += Helpers.FieldToString(_from, "From") + "\n";
                output += "To: " + To.toString() + "\n";
                output += Helpers.FieldToString(_subject, "Subject") + "\n";
                output += Helpers.FieldToString(_body, "Body") + "\n";
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
    public PacketType getType() { return PacketType.InternalScriptMail; }
    public DataBlockBlock DataBlock;

    public InternalScriptMailPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)16);
        header.setReliable(true);
        DataBlock = new DataBlockBlock();
    }

    public InternalScriptMailPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        DataBlock = new DataBlockBlock(bytes);
     }

    public InternalScriptMailPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- InternalScriptMail ---\n";
        output += DataBlock.toString() + "\n";
        return output;
    }
}
