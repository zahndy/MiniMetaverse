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

public class AgentCachedTextureResponsePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public int SerialNum = 0;

        public int getLength(){
            return 36;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            SerialNum = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            bytes.putInt(SerialNum);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "SerialNum: " + Integer.toString(SerialNum) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class WearableDataBlock
    {
        public UUID TextureID = null;
        public byte TextureIndex = 0;
        private byte[] _hostname;
        public byte[] getHostName() {
            return _hostname;
        }

        public void setHostName(byte[] value) throws Exception {
            if (value == null) {
                _hostname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _hostname = new byte[value.length];
                System.arraycopy(value, 0, _hostname, 0, value.length);
            }
        }


        public int getLength(){
            int length = 17;
            if (getHostName() != null) { length += 1 + getHostName().length; }
            return length;
        }

        public WearableDataBlock() { }
        public WearableDataBlock(ByteBuffer bytes)
        {
            int length;
            TextureID = new UUID(bytes);
            TextureIndex = bytes.get();
            length = bytes.get() & 0xFF;
            _hostname = new byte[length];
            bytes.get(_hostname);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TextureID.GetBytes(bytes);
            bytes.put(TextureIndex);
            bytes.put((byte)_hostname.length);
            bytes.put(_hostname);
        }

        @Override
        public String toString()
        {
            String output = "-- WearableData --\n";
            try {
                output += "TextureID: " + TextureID.toString() + "\n";
                output += "TextureIndex: " + Byte.toString(TextureIndex) + "\n";
                output += Helpers.FieldToString(_hostname, "HostName") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public WearableDataBlock createWearableDataBlock() {
         return new WearableDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AgentCachedTextureResponse; }
    public AgentDataBlock AgentData;
    public WearableDataBlock[] WearableData;

    public AgentCachedTextureResponsePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)385);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        WearableData = new WearableDataBlock[0];
    }

    public AgentCachedTextureResponsePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        WearableData = new WearableDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            WearableData[j] = new WearableDataBlock(bytes);
        }
     }

    public AgentCachedTextureResponsePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        WearableData = new WearableDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            WearableData[j] = new WearableDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < WearableData.length; j++) { length += WearableData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)WearableData.length);
        for (int j = 0; j < WearableData.length; j++) { WearableData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AgentCachedTextureResponse ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < WearableData.length; j++)
        {
            output += WearableData[j].toString() + "\n";
        }
        return output;
    }
}
