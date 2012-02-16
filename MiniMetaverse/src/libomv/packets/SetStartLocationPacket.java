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
import libomv.types.Vector3;

public class SetStartLocationPacket extends Packet
{
    public class StartLocationDataBlock
    {
        public UUID AgentID = null;
        public UUID RegionID = null;
        public int LocationID = 0;
        public long RegionHandle = 0;
        public Vector3 LocationPos = null;
        public Vector3 LocationLookAt = null;

        public int getLength(){
            return 68;
        }

        public StartLocationDataBlock() { }
        public StartLocationDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            RegionID = new UUID(bytes);
            LocationID = bytes.getInt();
            RegionHandle = bytes.getLong();
            LocationPos = new Vector3(bytes);
            LocationLookAt = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            RegionID.GetBytes(bytes);
            bytes.putInt(LocationID);
            bytes.putLong(RegionHandle);
            LocationPos.GetBytes(bytes);
            LocationLookAt.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- StartLocationData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "RegionID: " + RegionID.toString() + "\n";
                output += "LocationID: " + Integer.toString(LocationID) + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "LocationPos: " + LocationPos.toString() + "\n";
                output += "LocationLookAt: " + LocationLookAt.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public StartLocationDataBlock createStartLocationDataBlock() {
         return new StartLocationDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SetStartLocation; }
    public StartLocationDataBlock StartLocationData;

    public SetStartLocationPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)325);
        header.setReliable(true);
        StartLocationData = new StartLocationDataBlock();
    }

    public SetStartLocationPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        StartLocationData = new StartLocationDataBlock(bytes);
     }

    public SetStartLocationPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        StartLocationData = new StartLocationDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += StartLocationData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        StartLocationData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SetStartLocation ---\n";
        output += StartLocationData.toString() + "\n";
        return output;
    }
}
