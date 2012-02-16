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

public class ViewerEffectPacket extends Packet
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

    public class EffectBlock
    {
        public UUID ID = null;
        public UUID AgentID = null;
        public byte Type = 0;
        public float Duration = 0;
        public byte[] Color = null;
        private byte[] _typedata;
        public byte[] getTypeData() {
            return _typedata;
        }

        public void setTypeData(byte[] value) throws Exception {
            if (value == null) {
                _typedata = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _typedata = new byte[value.length];
                System.arraycopy(value, 0, _typedata, 0, value.length);
            }
        }


        public int getLength(){
            int length = 41;
            if (getTypeData() != null) { length += 1 + getTypeData().length; }
            return length;
        }

        public EffectBlock() { }
        public EffectBlock(ByteBuffer bytes)
        {
            int length;
            ID = new UUID(bytes);
            AgentID = new UUID(bytes);
            Type = bytes.get();
            Duration = bytes.getFloat();
            Color = new byte[4];
            bytes.get(Color);
            length = bytes.get() & 0xFF;
            _typedata = new byte[length];
            bytes.get(_typedata);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
            AgentID.GetBytes(bytes);
            bytes.put(Type);
            bytes.putFloat(Duration);
            bytes.put(Color);
            bytes.put((byte)_typedata.length);
            bytes.put(_typedata);
        }

        @Override
        public String toString()
        {
            String output = "-- Effect --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "Duration: " + Float.toString(Duration) + "\n";
                output += Helpers.FieldToString(Color, "Color") + "\n";
                output += Helpers.FieldToString(_typedata, "TypeData") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public EffectBlock createEffectBlock() {
         return new EffectBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ViewerEffect; }
    public AgentDataBlock AgentData;
    public EffectBlock[] Effect;

    public ViewerEffectPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)17);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        Effect = new EffectBlock[0];
    }

    public ViewerEffectPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Effect = new EffectBlock[count];
        for (int j = 0; j < count; j++)
        {
            Effect[j] = new EffectBlock(bytes);
        }
     }

    public ViewerEffectPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Effect = new EffectBlock[count];
        for (int j = 0; j < count; j++)
        {
            Effect[j] = new EffectBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < Effect.length; j++) { length += Effect[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)Effect.length);
        for (int j = 0; j < Effect.length; j++) { Effect[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ViewerEffect ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < Effect.length; j++)
        {
            output += Effect[j].toString() + "\n";
        }
        return output;
    }
}
