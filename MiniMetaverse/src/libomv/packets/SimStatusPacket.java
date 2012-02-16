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

public class SimStatusPacket extends Packet
{
    public class SimStatusBlock
    {
        public boolean CanAcceptAgents = false;
        public boolean CanAcceptTasks = false;

        public int getLength(){
            return 2;
        }

        public SimStatusBlock() { }
        public SimStatusBlock(ByteBuffer bytes)
        {
            CanAcceptAgents = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            CanAcceptTasks = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((CanAcceptAgents) ? 1 : 0));
            bytes.put((byte)((CanAcceptTasks) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- SimStatus --\n";
            try {
                output += "CanAcceptAgents: " + Boolean.toString(CanAcceptAgents) + "\n";
                output += "CanAcceptTasks: " + Boolean.toString(CanAcceptTasks) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimStatusBlock createSimStatusBlock() {
         return new SimStatusBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SimStatus; }
    public SimStatusBlock SimStatus;

    public SimStatusPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Medium);
        header.setID((short)12);
        header.setReliable(true);
        SimStatus = new SimStatusBlock();
    }

    public SimStatusPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Medium);
        SimStatus = new SimStatusBlock(bytes);
     }

    public SimStatusPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        SimStatus = new SimStatusBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += SimStatus.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        SimStatus.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SimStatus ---\n";
        output += SimStatus.toString() + "\n";
        return output;
    }
}
