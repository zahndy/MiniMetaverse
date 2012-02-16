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

public class MeanCollisionAlertPacket extends Packet
{
    public class MeanCollisionBlock
    {
        public UUID Victim = null;
        public UUID Perp = null;
        public int Time = 0;
        public float Mag = 0;
        public byte Type = 0;

        public int getLength(){
            return 41;
        }

        public MeanCollisionBlock() { }
        public MeanCollisionBlock(ByteBuffer bytes)
        {
            Victim = new UUID(bytes);
            Perp = new UUID(bytes);
            Time = bytes.getInt();
            Mag = bytes.getFloat();
            Type = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            Victim.GetBytes(bytes);
            Perp.GetBytes(bytes);
            bytes.putInt(Time);
            bytes.putFloat(Mag);
            bytes.put(Type);
        }

        @Override
        public String toString()
        {
            String output = "-- MeanCollision --\n";
            try {
                output += "Victim: " + Victim.toString() + "\n";
                output += "Perp: " + Perp.toString() + "\n";
                output += "Time: " + Integer.toString(Time) + "\n";
                output += "Mag: " + Float.toString(Mag) + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MeanCollisionBlock createMeanCollisionBlock() {
         return new MeanCollisionBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MeanCollisionAlert; }
    public MeanCollisionBlock[] MeanCollision;

    public MeanCollisionAlertPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)136);
        header.setReliable(true);
        MeanCollision = new MeanCollisionBlock[0];
    }

    public MeanCollisionAlertPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        int count = bytes.get() & 0xFF;
        MeanCollision = new MeanCollisionBlock[count];
        for (int j = 0; j < count; j++)
        {
            MeanCollision[j] = new MeanCollisionBlock(bytes);
        }
     }

    public MeanCollisionAlertPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        int count = bytes.get() & 0xFF;
        MeanCollision = new MeanCollisionBlock[count];
        for (int j = 0; j < count; j++)
        {
            MeanCollision[j] = new MeanCollisionBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length++;
        for (int j = 0; j < MeanCollision.length; j++) { length += MeanCollision[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put((byte)MeanCollision.length);
        for (int j = 0; j < MeanCollision.length; j++) { MeanCollision[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MeanCollisionAlert ---\n";
        for (int j = 0; j < MeanCollision.length; j++)
        {
            output += MeanCollision[j].toString() + "\n";
        }
        return output;
    }
}
