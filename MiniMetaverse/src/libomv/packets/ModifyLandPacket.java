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

public class ModifyLandPacket extends Packet
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

    public class ModifyBlockBlock
    {
        public byte Action = 0;
        public byte BrushSize = 0;
        public float Seconds = 0;
        public float Height = 0;

        public int getLength(){
            return 10;
        }

        public ModifyBlockBlock() { }
        public ModifyBlockBlock(ByteBuffer bytes)
        {
            Action = bytes.get();
            BrushSize = bytes.get();
            Seconds = bytes.getFloat();
            Height = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(Action);
            bytes.put(BrushSize);
            bytes.putFloat(Seconds);
            bytes.putFloat(Height);
        }

        @Override
        public String toString()
        {
            String output = "-- ModifyBlock --\n";
            try {
                output += "Action: " + Byte.toString(Action) + "\n";
                output += "BrushSize: " + Byte.toString(BrushSize) + "\n";
                output += "Seconds: " + Float.toString(Seconds) + "\n";
                output += "Height: " + Float.toString(Height) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ModifyBlockBlock createModifyBlockBlock() {
         return new ModifyBlockBlock();
    }

    public class ParcelDataBlock
    {
        public int LocalID = 0;
        public float West = 0;
        public float South = 0;
        public float East = 0;
        public float North = 0;

        public int getLength(){
            return 20;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            LocalID = bytes.getInt();
            West = bytes.getFloat();
            South = bytes.getFloat();
            East = bytes.getFloat();
            North = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(LocalID);
            bytes.putFloat(West);
            bytes.putFloat(South);
            bytes.putFloat(East);
            bytes.putFloat(North);
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output += "West: " + Float.toString(West) + "\n";
                output += "South: " + Float.toString(South) + "\n";
                output += "East: " + Float.toString(East) + "\n";
                output += "North: " + Float.toString(North) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ParcelDataBlock createParcelDataBlock() {
         return new ParcelDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ModifyLand; }
    public AgentDataBlock AgentData;
    public ModifyBlockBlock ModifyBlock;
    public ParcelDataBlock[] ParcelData;
    public float[] BrushSize;

    public ModifyLandPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)124);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        ModifyBlock = new ModifyBlockBlock();
        ParcelData = new ParcelDataBlock[0];
        BrushSize = new float[0];
    }

    public ModifyLandPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        ModifyBlock = new ModifyBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParcelData = new ParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParcelData[j] = new ParcelDataBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        BrushSize = new float[count];
        for (int j = 0; j < count; j++)
        {
            BrushSize[j] = bytes.getFloat();
        }
     }

    public ModifyLandPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        ModifyBlock = new ModifyBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParcelData = new ParcelDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParcelData[j] = new ParcelDataBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        BrushSize = new float[count];
        for (int j = 0; j < count; j++)
        {
            BrushSize[j] = bytes.getFloat();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ModifyBlock.getLength();
        length++;
        for (int j = 0; j < ParcelData.length; j++) { length += ParcelData[j].getLength(); }
        length++;
        length += BrushSize.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        ModifyBlock.ToBytes(bytes);
        bytes.put((byte)ParcelData.length);
        for (int j = 0; j < ParcelData.length; j++) { ParcelData[j].ToBytes(bytes); }
        bytes.put((byte)BrushSize.length);
        for (int j = 0; j < BrushSize.length; j++)
        {
            bytes.putFloat(BrushSize[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ModifyLand ---\n";
        output += AgentData.toString() + "\n";
        output += ModifyBlock.toString() + "\n";
        for (int j = 0; j < ParcelData.length; j++)
        {
            output += ParcelData[j].toString() + "\n";
        }
        for (int j = 0; j < BrushSize.length; j++)
        {
            output += "BrushSize[" + j + "]: " + Float.toString(BrushSize[j]) + "\n";
        }
        return output;
    }
}
