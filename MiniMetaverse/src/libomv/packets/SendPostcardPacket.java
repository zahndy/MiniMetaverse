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
import libomv.types.Vector3d;
import libomv.types.OverflowException;

public class SendPostcardPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID AssetID = null;
        public Vector3d PosGlobal = null;
        private byte[] _to;
        public byte[] getTo() {
            return _to;
        }

        public void setTo(byte[] value) throws Exception {
            if (value == null) {
                _to = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _to = new byte[value.length];
                System.arraycopy(value, 0, _to, 0, value.length);
            }
        }

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

        private byte[] _msg;
        public byte[] getMsg() {
            return _msg;
        }

        public void setMsg(byte[] value) throws Exception {
            if (value == null) {
                _msg = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _msg = new byte[value.length];
                System.arraycopy(value, 0, _msg, 0, value.length);
            }
        }

        public boolean AllowPublish = false;
        public boolean MaturePublish = false;

        public int getLength(){
            int length = 74;
            if (getTo() != null) { length += 1 + getTo().length; }
            if (getFrom() != null) { length += 1 + getFrom().length; }
            if (getName() != null) { length += 1 + getName().length; }
            if (getSubject() != null) { length += 1 + getSubject().length; }
            if (getMsg() != null) { length += 2 + getMsg().length; }
            return length;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            AssetID = new UUID(bytes);
            PosGlobal = new Vector3d(bytes);
            length = bytes.get() & 0xFF;
            _to = new byte[length];
            bytes.get(_to);
            length = bytes.get() & 0xFF;
            _from = new byte[length];
            bytes.get(_from);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _subject = new byte[length];
            bytes.get(_subject);
            length = bytes.getShort() & 0xFFFF;
            _msg = new byte[length];
            bytes.get(_msg);
            AllowPublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            MaturePublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            AssetID.GetBytes(bytes);
            PosGlobal.GetBytes(bytes);
            bytes.put((byte)_to.length);
            bytes.put(_to);
            bytes.put((byte)_from.length);
            bytes.put(_from);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_subject.length);
            bytes.put(_subject);
            bytes.putShort((short)_msg.length);
            bytes.put(_msg);
            bytes.put((byte)((AllowPublish) ? 1 : 0));
            bytes.put((byte)((MaturePublish) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "AssetID: " + AssetID.toString() + "\n";
                output += "PosGlobal: " + PosGlobal.toString() + "\n";
                output += Helpers.FieldToString(_to, "To") + "\n";
                output += Helpers.FieldToString(_from, "From") + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_subject, "Subject") + "\n";
                output += Helpers.FieldToString(_msg, "Msg") + "\n";
                output += "AllowPublish: " + Boolean.toString(AllowPublish) + "\n";
                output += "MaturePublish: " + Boolean.toString(MaturePublish) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SendPostcard; }
    public AgentDataBlock AgentData;

    public SendPostcardPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)412);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
    }

    public SendPostcardPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
     }

    public SendPostcardPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SendPostcard ---\n";
        output += AgentData.toString() + "\n";
        return output;
    }
}
