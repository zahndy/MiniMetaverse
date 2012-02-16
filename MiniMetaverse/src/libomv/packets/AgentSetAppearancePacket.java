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

public class AgentSetAppearancePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public int SerialNum = 0;
        public Vector3 Size = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            SerialNum = bytes.getInt();
            Size = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            bytes.putInt(SerialNum);
            Size.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "SerialNum: " + Integer.toString(SerialNum) + "\n";
                output += "Size: " + Size.toString() + "\n";
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
        public UUID CacheID = null;
        public byte TextureIndex = 0;

        public int getLength(){
            return 17;
        }

        public WearableDataBlock() { }
        public WearableDataBlock(ByteBuffer bytes)
        {
            CacheID = new UUID(bytes);
            TextureIndex = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            CacheID.GetBytes(bytes);
            bytes.put(TextureIndex);
        }

        @Override
        public String toString()
        {
            String output = "-- WearableData --\n";
            try {
                output += "CacheID: " + CacheID.toString() + "\n";
                output += "TextureIndex: " + Byte.toString(TextureIndex) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public WearableDataBlock createWearableDataBlock() {
         return new WearableDataBlock();
    }

    public class ObjectDataBlock
    {
        private byte[] _textureentry;
        public byte[] getTextureEntry() {
            return _textureentry;
        }

        public void setTextureEntry(byte[] value) throws Exception {
            if (value == null) {
                _textureentry = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _textureentry = new byte[value.length];
                System.arraycopy(value, 0, _textureentry, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getTextureEntry() != null) { length += 2 + getTextureEntry().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.getShort() & 0xFFFF;
            _textureentry = new byte[length];
            bytes.get(_textureentry);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_textureentry.length);
            bytes.put(_textureentry);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += Helpers.FieldToString(_textureentry, "TextureEntry") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ObjectDataBlock createObjectDataBlock() {
         return new ObjectDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AgentSetAppearance; }
    public AgentDataBlock AgentData;
    public WearableDataBlock[] WearableData;
    public ObjectDataBlock ObjectData;
    public byte[] ParamValue;

    public AgentSetAppearancePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)84);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        WearableData = new WearableDataBlock[0];
        ObjectData = new ObjectDataBlock();
        ParamValue = new byte[0];
    }

    public AgentSetAppearancePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        WearableData = new WearableDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            WearableData[j] = new WearableDataBlock(bytes);
        }
        ObjectData = new ObjectDataBlock(bytes);
        count = bytes.get() & 0xFF;
        ParamValue = new byte[count];
        bytes.get(ParamValue);
     }

    public AgentSetAppearancePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        WearableData = new WearableDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            WearableData[j] = new WearableDataBlock(bytes);
        }
        ObjectData = new ObjectDataBlock(bytes);
        count = bytes.get() & 0xFF;
        ParamValue = new byte[count];
        bytes.get(ParamValue);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ObjectData.getLength();
        length++;
        for (int j = 0; j < WearableData.length; j++) { length += WearableData[j].getLength(); }
        length++;
        length += ParamValue.length * 1;
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
        ObjectData.ToBytes(bytes);
        bytes.put((byte)ParamValue.length);
        bytes.put(ParamValue);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AgentSetAppearance ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < WearableData.length; j++)
        {
            output += WearableData[j].toString() + "\n";
        }
        output += ObjectData.toString() + "\n";
        for (int j = 0; j < ParamValue.length; j++)
        {
            output += "ParamValue[" + j + "]: " + Byte.toString(ParamValue[j]) + "\n";
        }
        return output;
    }
}
