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

public class ErrorPacket extends Packet
{
    public class DataBlock
    {
        public int Code = 0;
        private byte[] _token;
        public byte[] getToken() {
            return _token;
        }

        public void setToken(byte[] value) throws Exception {
            if (value == null) {
                _token = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _token = new byte[value.length];
                System.arraycopy(value, 0, _token, 0, value.length);
            }
        }

        public UUID ID = null;
        private byte[] _system;
        public byte[] getSystem() {
            return _system;
        }

        public void setSystem(byte[] value) throws Exception {
            if (value == null) {
                _system = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _system = new byte[value.length];
                System.arraycopy(value, 0, _system, 0, value.length);
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

        private byte[] _data;
        public byte[] getData() {
            return _data;
        }

        public void setData(byte[] value) throws Exception {
            if (value == null) {
                _data = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _data = new byte[value.length];
                System.arraycopy(value, 0, _data, 0, value.length);
            }
        }


        public int getLength(){
            int length = 20;
            if (getToken() != null) { length += 1 + getToken().length; }
            if (getSystem() != null) { length += 1 + getSystem().length; }
            if (getMessage() != null) { length += 2 + getMessage().length; }
            if (getData() != null) { length += 2 + getData().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            Code = bytes.getInt();
            length = bytes.get() & 0xFF;
            _token = new byte[length];
            bytes.get(_token);
            ID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _system = new byte[length];
            bytes.get(_system);
            length = bytes.getShort() & 0xFFFF;
            _message = new byte[length];
            bytes.get(_message);
            length = bytes.getShort() & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Code);
            bytes.put((byte)_token.length);
            bytes.put(_token);
            ID.GetBytes(bytes);
            bytes.put((byte)_system.length);
            bytes.put(_system);
            bytes.putShort((short)_message.length);
            bytes.put(_message);
            bytes.putShort((short)_data.length);
            bytes.put(_data);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "Code: " + Integer.toString(Code) + "\n";
                output += Helpers.FieldToString(_token, "Token") + "\n";
                output += "ID: " + ID.toString() + "\n";
                output += Helpers.FieldToString(_system, "System") + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += Helpers.FieldToString(_data, "Data") + "\n";
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
    public PacketType getType() { return PacketType.Error; }
    public UUID AgentID;
    public DataBlock Data;

    public ErrorPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)423);
        header.setReliable(true);
        AgentID = new UUID();
        Data = new DataBlock();
    }

    public ErrorPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        Data = new DataBlock(bytes);
     }

    public ErrorPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- Error ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += Data.toString() + "\n";
        return output;
    }
}
