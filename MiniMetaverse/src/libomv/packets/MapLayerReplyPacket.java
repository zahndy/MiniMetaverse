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

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;

public class MapLayerReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public int Flags = 0;

        public int getLength(){
            return 20;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            Flags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.putInt(Flags);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class LayerDataBlock
    {
        public int Left = 0;
        public int Right = 0;
        public int Top = 0;
        public int Bottom = 0;
        public UUID ImageID = null;

        public int getLength(){
            return 32;
        }

        public LayerDataBlock() { }
        public LayerDataBlock(ByteBuffer bytes)
        {
            Left = bytes.getInt();
            Right = bytes.getInt();
            Top = bytes.getInt();
            Bottom = bytes.getInt();
            ImageID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Left);
            bytes.putInt(Right);
            bytes.putInt(Top);
            bytes.putInt(Bottom);
            ImageID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- LayerData --\n";
            try {
                output += "Left: " + Integer.toString(Left) + "\n";
                output += "Right: " + Integer.toString(Right) + "\n";
                output += "Top: " + Integer.toString(Top) + "\n";
                output += "Bottom: " + Integer.toString(Bottom) + "\n";
                output += "ImageID: " + ImageID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public LayerDataBlock createLayerDataBlock() {
         return new LayerDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MapLayerReply; }
    public AgentDataBlock AgentData;
    public LayerDataBlock[] LayerData;

    public MapLayerReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)406);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        LayerData = new LayerDataBlock[0];
    }

    public MapLayerReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        LayerData = new LayerDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            LayerData[j] = new LayerDataBlock(bytes);
        }
     }

    public MapLayerReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        LayerData = new LayerDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            LayerData[j] = new LayerDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < LayerData.length; j++) { length += LayerData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)LayerData.length);
        for (int j = 0; j < LayerData.length; j++) { LayerData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MapLayerReply ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < LayerData.length; j++)
        {
            output += LayerData[j].toString() + "\n";
        }
        return output;
    }
}
