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
import libomv.types.Vector3;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class ChatPassPacket extends Packet
{
    public class ChatDataBlock
    {
        public int Channel = 0;
        public Vector3 Position = null;
        public UUID ID = null;
        public UUID OwnerID = null;
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

        public byte SourceType = 0;
        public byte Type = 0;
        public float Radius = 0;
        public byte SimAccess = 0;
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


        public int getLength(){
            int length = 55;
            if (getName() != null) { length += 1 + getName().length; }
            if (getMessage() != null) { length += 2 + getMessage().length; }
            return length;
        }

        public ChatDataBlock() { }
        public ChatDataBlock(ByteBuffer bytes)
        {
            int length;
            Channel = bytes.getInt();
            Position = new Vector3(bytes);
            ID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            SourceType = bytes.get();
            Type = bytes.get();
            Radius = bytes.getFloat();
            SimAccess = bytes.get();
            length = bytes.getShort() & 0xFFFF;
            _message = new byte[length];
            bytes.get(_message);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Channel);
            Position.GetBytes(bytes);
            ID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put(SourceType);
            bytes.put(Type);
            bytes.putFloat(Radius);
            bytes.put(SimAccess);
            bytes.putShort((short)_message.length);
            bytes.put(_message);
        }

        @Override
        public String toString()
        {
            String output = "-- ChatData --\n";
            try {
                output += "Channel: " + Integer.toString(Channel) + "\n";
                output += "Position: " + Position.toString() + "\n";
                output += "ID: " + ID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += "SourceType: " + Byte.toString(SourceType) + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "Radius: " + Float.toString(Radius) + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ChatDataBlock createChatDataBlock() {
         return new ChatDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ChatPass; }
    public ChatDataBlock ChatData;

    public ChatPassPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)239);
        header.setReliable(true);
        ChatData = new ChatDataBlock();
    }

    public ChatPassPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ChatData = new ChatDataBlock(bytes);
     }

    public ChatPassPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ChatData = new ChatDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ChatData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ChatData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ChatPass ---\n";
        output += ChatData.toString() + "\n";
        return output;
    }
}
