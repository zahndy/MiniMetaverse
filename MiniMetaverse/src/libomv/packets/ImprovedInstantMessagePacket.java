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
import libomv.types.Vector3;
import libomv.types.OverflowException;

public class ImprovedInstantMessagePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class MessageBlockBlock
    {
        public boolean FromGroup = false;
        public UUID ToAgentID = null;
        public int ParentEstateID = 0;
        public UUID RegionID = null;
        public Vector3 Position = null;
        public byte Offline = 0;
        public byte Dialog = 0;
        public UUID ID = null;
        public int Timestamp = 0;
        private byte[] _fromagentname;
        public byte[] getFromAgentName() {
            return _fromagentname;
        }

        public void setFromAgentName(byte[] value) throws Exception {
            if (value == null) {
                _fromagentname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _fromagentname = new byte[value.length];
                System.arraycopy(value, 0, _fromagentname, 0, value.length);
            }
        }

        private byte[] _message;
        public byte[] getMessage() {
            return _message;
        }

        public void setMessage(byte[] value) throws Exception {
            if (value == null) {
                _message = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _message = new byte[value.length];
                System.arraycopy(value, 0, _message, 0, value.length);
            }
        }

        private byte[] _binarybucket;
        public byte[] getBinaryBucket() {
            return _binarybucket;
        }

        public void setBinaryBucket(byte[] value) throws Exception {
            if (value == null) {
                _binarybucket = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _binarybucket = new byte[value.length];
                System.arraycopy(value, 0, _binarybucket, 0, value.length);
            }
        }


        public int getLength(){
            int length = 71;
            if (getFromAgentName() != null) { length += 1 + getFromAgentName().length; }
            if (getMessage() != null) { length += 2 + getMessage().length; }
            if (getBinaryBucket() != null) { length += 2 + getBinaryBucket().length; }
            return length;
        }

        public MessageBlockBlock() { }
        public MessageBlockBlock(ByteBuffer bytes)
        {
            int length;
            FromGroup = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            ToAgentID = new UUID(bytes);
            ParentEstateID = bytes.getInt();
            RegionID = new UUID(bytes);
            Position = new Vector3(bytes);
            Offline = bytes.get();
            Dialog = bytes.get();
            ID = new UUID(bytes);
            Timestamp = bytes.getInt();
            length = bytes.get() & 0xFF;
            _fromagentname = new byte[length];
            bytes.get(_fromagentname);
            length = bytes.getShort() & 0xFFFF;
            _message = new byte[length];
            bytes.get(_message);
            length = bytes.getShort() & 0xFFFF;
            _binarybucket = new byte[length];
            bytes.get(_binarybucket);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((FromGroup) ? 1 : 0));
            ToAgentID.GetBytes(bytes);
            bytes.putInt(ParentEstateID);
            RegionID.GetBytes(bytes);
            Position.GetBytes(bytes);
            bytes.put(Offline);
            bytes.put(Dialog);
            ID.GetBytes(bytes);
            bytes.putInt(Timestamp);
            bytes.put((byte)_fromagentname.length);
            bytes.put(_fromagentname);
            bytes.putShort((short)_message.length);
            bytes.put(_message);
            bytes.putShort((short)_binarybucket.length);
            bytes.put(_binarybucket);
        }

        @Override
        public String toString()
        {
            String output = "-- MessageBlock --\n";
            try {
                output += "FromGroup: " + Boolean.toString(FromGroup) + "\n";
                output += "ToAgentID: " + ToAgentID.toString() + "\n";
                output += "ParentEstateID: " + Integer.toString(ParentEstateID) + "\n";
                output += "RegionID: " + RegionID.toString() + "\n";
                output += "Position: " + Position.toString() + "\n";
                output += "Offline: " + Byte.toString(Offline) + "\n";
                output += "Dialog: " + Byte.toString(Dialog) + "\n";
                output += "ID: " + ID.toString() + "\n";
                output += "Timestamp: " + Integer.toString(Timestamp) + "\n";
                output += Helpers.FieldToString(_fromagentname, "FromAgentName") + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += Helpers.FieldToString(_binarybucket, "BinaryBucket") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MessageBlockBlock createMessageBlockBlock() {
         return new MessageBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ImprovedInstantMessage; }
    public AgentDataBlock AgentData;
    public MessageBlockBlock MessageBlock;

    public ImprovedInstantMessagePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)254);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MessageBlock = new MessageBlockBlock();
    }

    public ImprovedInstantMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MessageBlock = new MessageBlockBlock(bytes);
     }

    public ImprovedInstantMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        MessageBlock = new MessageBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += MessageBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        MessageBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ImprovedInstantMessage ---\n";
        output += AgentData.toString() + "\n";
        output += MessageBlock.toString() + "\n";
        return output;
    }
}
