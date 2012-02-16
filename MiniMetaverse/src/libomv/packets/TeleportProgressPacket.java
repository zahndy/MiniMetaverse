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

public class TeleportProgressPacket extends Packet
{
    public class InfoBlock
    {
        public int TeleportFlags = 0;
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


        public int getLength(){
            int length = 4;
            if (getMessage() != null) { length += 1 + getMessage().length; }
            return length;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            int length;
            TeleportFlags = bytes.getInt();
            length = bytes.get() & 0xFF;
            _message = new byte[length];
            bytes.get(_message);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(TeleportFlags);
            bytes.put((byte)_message.length);
            bytes.put(_message);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "TeleportFlags: " + Integer.toString(TeleportFlags) + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlock createInfoBlock() {
         return new InfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TeleportProgress; }
    public UUID AgentID;
    public InfoBlock Info;

    public TeleportProgressPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)66);
        header.setReliable(true);
        AgentID = new UUID();
        Info = new InfoBlock();
    }

    public TeleportProgressPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        Info = new InfoBlock(bytes);
     }

    public TeleportProgressPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        Info = new InfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += Info.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        Info.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TeleportProgress ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += Info.toString() + "\n";
        return output;
    }
}