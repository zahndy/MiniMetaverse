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

public class LoadURLPacket extends Packet
{
    public class DataBlock
    {
        private byte[] _objectname;
        public byte[] getObjectName() {
            return _objectname;
        }

        public void setObjectName(byte[] value) throws Exception {
            if (value == null) {
                _objectname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _objectname = new byte[value.length];
                System.arraycopy(value, 0, _objectname, 0, value.length);
            }
        }

        public UUID ObjectID = null;
        public UUID OwnerID = null;
        public boolean OwnerIsGroup = false;
        private byte[] _message;
        public byte[] getMessage() {
            return _message;
        }

        public void setMessage(byte[] value) throws Exception {
            if (value == null) {
                _message = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _message = new byte[value.length];
                System.arraycopy(value, 0, _message, 0, value.length);
            }
        }

        private byte[] _url;
        public byte[] getURL() {
            return _url;
        }

        public void setURL(byte[] value) throws Exception {
            if (value == null) {
                _url = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _url = new byte[value.length];
                System.arraycopy(value, 0, _url, 0, value.length);
            }
        }


        public int getLength(){
            int length = 33;
            if (getObjectName() != null) { length += 1 + getObjectName().length; }
            if (getMessage() != null) { length += 1 + getMessage().length; }
            if (getURL() != null) { length += 1 + getURL().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _objectname = new byte[length];
            bytes.get(_objectname);
            ObjectID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            OwnerIsGroup = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = bytes.get() & 0xFF;
            _message = new byte[length];
            bytes.get(_message);
            length = bytes.get() & 0xFF;
            _url = new byte[length];
            bytes.get(_url);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_objectname.length);
            bytes.put(_objectname);
            ObjectID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.put((byte)((OwnerIsGroup) ? 1 : 0));
            bytes.put((byte)_message.length);
            bytes.put(_message);
            bytes.put((byte)_url.length);
            bytes.put(_url);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += Helpers.FieldToString(_objectname, "ObjectName") + "\n";
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "OwnerIsGroup: " + Boolean.toString(OwnerIsGroup) + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += Helpers.FieldToString(_url, "URL") + "\n";
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
    public PacketType getType() { return PacketType.LoadURL; }
    public DataBlock Data;

    public LoadURLPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)194);
        header.setReliable(true);
        Data = new DataBlock();
    }

    public LoadURLPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
     }

    public LoadURLPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LoadURL ---\n";
        output += Data.toString() + "\n";
        return output;
    }
}
