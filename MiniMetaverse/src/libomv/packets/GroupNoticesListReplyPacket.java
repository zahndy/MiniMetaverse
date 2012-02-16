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

public class GroupNoticesListReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID GroupID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            GroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class DataBlock
    {
        public UUID NoticeID = null;
        public int Timestamp = 0;
        private byte[] _fromname;
        public byte[] getFromName() {
            return _fromname;
        }

        public void setFromName(byte[] value) throws Exception {
            if (value == null) {
                _fromname = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _fromname = new byte[value.length];
                System.arraycopy(value, 0, _fromname, 0, value.length);
            }
        }

        private byte[] _subject;
        public byte[] getSubject() {
            return _subject;
        }

        public void setSubject(byte[] value) throws Exception {
            if (value == null) {
                _subject = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _subject = new byte[value.length];
                System.arraycopy(value, 0, _subject, 0, value.length);
            }
        }

        public boolean HasAttachment = false;
        public byte AssetType = 0;

        public int getLength(){
            int length = 22;
            if (getFromName() != null) { length += 2 + getFromName().length; }
            if (getSubject() != null) { length += 2 + getSubject().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            NoticeID = new UUID(bytes);
            Timestamp = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _fromname = new byte[length];
            bytes.get(_fromname);
            length = bytes.getShort() & 0xFFFF;
            _subject = new byte[length];
            bytes.get(_subject);
            HasAttachment = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            AssetType = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            NoticeID.GetBytes(bytes);
            bytes.putInt(Timestamp);
            bytes.putShort((short)_fromname.length);
            bytes.put(_fromname);
            bytes.putShort((short)_subject.length);
            bytes.put(_subject);
            bytes.put((byte)((HasAttachment) ? 1 : 0));
            bytes.put(AssetType);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "NoticeID: " + NoticeID.toString() + "\n";
                output += "Timestamp: " + Integer.toString(Timestamp) + "\n";
                output += Helpers.FieldToString(_fromname, "FromName") + "\n";
                output += Helpers.FieldToString(_subject, "Subject") + "\n";
                output += "HasAttachment: " + Boolean.toString(HasAttachment) + "\n";
                output += "AssetType: " + Byte.toString(AssetType) + "\n";
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
    public PacketType getType() { return PacketType.GroupNoticesListReply; }
    public AgentDataBlock AgentData;
    public DataBlock[] Data;

    public GroupNoticesListReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)59);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        Data = new DataBlock[0];
    }

    public GroupNoticesListReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        {
            Data[j] = new DataBlock(bytes);
        }
     }

    public GroupNoticesListReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Data = new DataBlock[count];
        for (int j = 0; j < count; j++)
        {
            Data[j] = new DataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < Data.length; j++) { length += Data[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)Data.length);
        for (int j = 0; j < Data.length; j++) { Data[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupNoticesListReply ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < Data.length; j++)
        {
            output += Data[j].toString() + "\n";
        }
        return output;
    }
}
