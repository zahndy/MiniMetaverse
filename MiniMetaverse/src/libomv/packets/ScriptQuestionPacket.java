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

public class ScriptQuestionPacket extends Packet
{
    public class DataBlock
    {
        public UUID TaskID = null;
        public UUID ItemID = null;
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

        private byte[] _objectowner;
        public byte[] getObjectOwner() {
            return _objectowner;
        }

        public void setObjectOwner(byte[] value) throws Exception {
            if (value == null) {
                _objectowner = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _objectowner = new byte[value.length];
                System.arraycopy(value, 0, _objectowner, 0, value.length);
            }
        }

        public int Questions = 0;

        public int getLength(){
            int length = 36;
            if (getObjectName() != null) { length += 1 + getObjectName().length; }
            if (getObjectOwner() != null) { length += 1 + getObjectOwner().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            TaskID = new UUID(bytes);
            ItemID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _objectname = new byte[length];
            bytes.get(_objectname);
            length = bytes.get() & 0xFF;
            _objectowner = new byte[length];
            bytes.get(_objectowner);
            Questions = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TaskID.GetBytes(bytes);
            ItemID.GetBytes(bytes);
            bytes.put((byte)_objectname.length);
            bytes.put(_objectname);
            bytes.put((byte)_objectowner.length);
            bytes.put(_objectowner);
            bytes.putInt(Questions);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "TaskID: " + TaskID.toString() + "\n";
                output += "ItemID: " + ItemID.toString() + "\n";
                output += Helpers.FieldToString(_objectname, "ObjectName") + "\n";
                output += Helpers.FieldToString(_objectowner, "ObjectOwner") + "\n";
                output += "Questions: " + Integer.toString(Questions) + "\n";
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
    public PacketType getType() { return PacketType.ScriptQuestion; }
    public DataBlock Data;

    public ScriptQuestionPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)188);
        header.setReliable(true);
        Data = new DataBlock();
    }

    public ScriptQuestionPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
     }

    public ScriptQuestionPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- ScriptQuestion ---\n";
        output += Data.toString() + "\n";
        return output;
    }
}
