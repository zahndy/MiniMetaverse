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
import libomv.types.Vector3d;

public class EventInfoReplyPacket extends Packet
{
    public class EventDataBlock
    {
        public int EventID = 0;
        private byte[] _creator;
        public byte[] getCreator() {
            return _creator;
        }

        public void setCreator(byte[] value) throws Exception {
            if (value == null) {
                _creator = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _creator = new byte[value.length];
                System.arraycopy(value, 0, _creator, 0, value.length);
            }
        }

        private byte[] _name;
        public byte[] getName() {
            return _name;
        }

        public void setName(byte[] value) throws Exception {
            if (value == null) {
                _name = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _name = new byte[value.length];
                System.arraycopy(value, 0, _name, 0, value.length);
            }
        }

        private byte[] _category;
        public byte[] getCategory() {
            return _category;
        }

        public void setCategory(byte[] value) throws Exception {
            if (value == null) {
                _category = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _category = new byte[value.length];
                System.arraycopy(value, 0, _category, 0, value.length);
            }
        }

        private byte[] _desc;
        public byte[] getDesc() {
            return _desc;
        }

        public void setDesc(byte[] value) throws Exception {
            if (value == null) {
                _desc = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _desc = new byte[value.length];
                System.arraycopy(value, 0, _desc, 0, value.length);
            }
        }

        private byte[] _date;
        public byte[] getDate() {
            return _date;
        }

        public void setDate(byte[] value) throws Exception {
            if (value == null) {
                _date = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _date = new byte[value.length];
                System.arraycopy(value, 0, _date, 0, value.length);
            }
        }

        public int DateUTC = 0;
        public int Duration = 0;
        public int Cover = 0;
        public int Amount = 0;
        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }

        public Vector3d GlobalPos = null;
        public int EventFlags = 0;

        public int getLength(){
            int length = 48;
            if (getCreator() != null) { length += 1 + getCreator().length; }
            if (getName() != null) { length += 1 + getName().length; }
            if (getCategory() != null) { length += 1 + getCategory().length; }
            if (getDesc() != null) { length += 2 + getDesc().length; }
            if (getDate() != null) { length += 1 + getDate().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public EventDataBlock() { }
        public EventDataBlock(ByteBuffer bytes)
        {
            int length;
            EventID = bytes.getInt();
            length = bytes.get() & 0xFF;
            _creator = new byte[length];
            bytes.get(_creator);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _category = new byte[length];
            bytes.get(_category);
            length = bytes.getShort() & 0xFFFF;
            _desc = new byte[length];
            bytes.get(_desc);
            length = bytes.get() & 0xFF;
            _date = new byte[length];
            bytes.get(_date);
            DateUTC = bytes.getInt();
            Duration = bytes.getInt();
            Cover = bytes.getInt();
            Amount = bytes.getInt();
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            GlobalPos = new Vector3d(bytes);
            EventFlags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(EventID);
            bytes.put((byte)_creator.length);
            bytes.put(_creator);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_category.length);
            bytes.put(_category);
            bytes.putShort((short)_desc.length);
            bytes.put(_desc);
            bytes.put((byte)_date.length);
            bytes.put(_date);
            bytes.putInt(DateUTC);
            bytes.putInt(Duration);
            bytes.putInt(Cover);
            bytes.putInt(Amount);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            GlobalPos.GetBytes(bytes);
            bytes.putInt(EventFlags);
        }

        @Override
        public String toString()
        {
            String output = "-- EventData --\n";
            try {
                output += "EventID: " + Integer.toString(EventID) + "\n";
                output += Helpers.FieldToString(_creator, "Creator") + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_category, "Category") + "\n";
                output += Helpers.FieldToString(_desc, "Desc") + "\n";
                output += Helpers.FieldToString(_date, "Date") + "\n";
                output += "DateUTC: " + Integer.toString(DateUTC) + "\n";
                output += "Duration: " + Integer.toString(Duration) + "\n";
                output += "Cover: " + Integer.toString(Cover) + "\n";
                output += "Amount: " + Integer.toString(Amount) + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "GlobalPos: " + GlobalPos.toString() + "\n";
                output += "EventFlags: " + Integer.toString(EventFlags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public EventDataBlock createEventDataBlock() {
         return new EventDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.EventInfoReply; }
    public UUID AgentID;
    public EventDataBlock EventData;

    public EventInfoReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)180);
        header.setReliable(true);
        AgentID = new UUID();
        EventData = new EventDataBlock();
    }

    public EventInfoReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        EventData = new EventDataBlock(bytes);
     }

    public EventInfoReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        EventData = new EventDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += EventData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        EventData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- EventInfoReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += EventData.toString() + "\n";
        return output;
    }
}
