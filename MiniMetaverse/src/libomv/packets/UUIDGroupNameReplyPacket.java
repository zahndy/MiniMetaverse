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

public class UUIDGroupNameReplyPacket extends Packet
{
    public class UUIDNameBlockBlock
    {
        public UUID ID = null;
        private byte[] _groupname;
        public byte[] getGroupName() {
            return _groupname;
        }

        public void setGroupName(byte[] value) throws Exception {
            if (value == null) {
                _groupname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _groupname = new byte[value.length];
                System.arraycopy(value, 0, _groupname, 0, value.length);
            }
        }


        public int getLength(){
            int length = 16;
            if (getGroupName() != null) { length += 1 + getGroupName().length; }
            return length;
        }

        public UUIDNameBlockBlock() { }
        public UUIDNameBlockBlock(ByteBuffer bytes)
        {
            int length;
            ID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _groupname = new byte[length];
            bytes.get(_groupname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
            bytes.put((byte)_groupname.length);
            bytes.put(_groupname);
        }

        @Override
        public String toString()
        {
            String output = "-- UUIDNameBlock --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output += Helpers.FieldToString(_groupname, "GroupName") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public UUIDNameBlockBlock createUUIDNameBlockBlock() {
         return new UUIDNameBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UUIDGroupNameReply; }
    public UUIDNameBlockBlock[] UUIDNameBlock;

    public UUIDGroupNameReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)238);
        header.setReliable(true);
        UUIDNameBlock = new UUIDNameBlockBlock[0];
    }

    public UUIDGroupNameReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        UUIDNameBlock = new UUIDNameBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            UUIDNameBlock[j] = new UUIDNameBlockBlock(bytes);
        }
     }

    public UUIDGroupNameReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        UUIDNameBlock = new UUIDNameBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            UUIDNameBlock[j] = new UUIDNameBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < UUIDNameBlock.length; j++) { length += UUIDNameBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)UUIDNameBlock.length);
        for (int j = 0; j < UUIDNameBlock.length; j++) { UUIDNameBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UUIDGroupNameReply ---\n";
        for (int j = 0; j < UUIDNameBlock.length; j++)
        {
            output += UUIDNameBlock[j].toString() + "\n";
        }
        return output;
    }
}
