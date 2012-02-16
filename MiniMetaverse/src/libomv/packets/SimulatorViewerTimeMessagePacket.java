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
import libomv.types.Vector3;

public class SimulatorViewerTimeMessagePacket extends Packet
{
    public class TimeInfoBlock
    {
        public long UsecSinceStart = 0;
        public int SecPerDay = 0;
        public int SecPerYear = 0;
        public Vector3 SunDirection = null;
        public float SunPhase = 0;
        public Vector3 SunAngVelocity = null;

        public int getLength(){
            return 44;
        }

        public TimeInfoBlock() { }
        public TimeInfoBlock(ByteBuffer bytes)
        {
            UsecSinceStart = bytes.getLong();
            SecPerDay = bytes.getInt();
            SecPerYear = bytes.getInt();
            SunDirection = new Vector3(bytes);
            SunPhase = bytes.getFloat();
            SunAngVelocity = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(UsecSinceStart);
            bytes.putInt(SecPerDay);
            bytes.putInt(SecPerYear);
            SunDirection.GetBytes(bytes);
            bytes.putFloat(SunPhase);
            SunAngVelocity.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- TimeInfo --\n";
            try {
                output += "UsecSinceStart: " + Long.toString(UsecSinceStart) + "\n";
                output += "SecPerDay: " + Integer.toString(SecPerDay) + "\n";
                output += "SecPerYear: " + Integer.toString(SecPerYear) + "\n";
                output += "SunDirection: " + SunDirection.toString() + "\n";
                output += "SunPhase: " + Float.toString(SunPhase) + "\n";
                output += "SunAngVelocity: " + SunAngVelocity.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TimeInfoBlock createTimeInfoBlock() {
         return new TimeInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SimulatorViewerTimeMessage; }
    public TimeInfoBlock TimeInfo;

    public SimulatorViewerTimeMessagePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)150);
        header.setReliable(true);
        TimeInfo = new TimeInfoBlock();
    }

    public SimulatorViewerTimeMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TimeInfo = new TimeInfoBlock(bytes);
     }

    public SimulatorViewerTimeMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TimeInfo = new TimeInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TimeInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TimeInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SimulatorViewerTimeMessage ---\n";
        output += TimeInfo.toString() + "\n";
        return output;
    }
}
