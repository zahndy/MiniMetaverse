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

public class ParcelMediaCommandMessagePacket extends Packet
{
    public class CommandBlockBlock
    {
        public int Flags = 0;
        public int Command = 0;
        public float Time = 0;

        public int getLength(){
            return 12;
        }

        public CommandBlockBlock() { }
        public CommandBlockBlock(ByteBuffer bytes)
        {
            Flags = bytes.getInt();
            Command = bytes.getInt();
            Time = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Flags);
            bytes.putInt(Command);
            bytes.putFloat(Time);
        }

        @Override
        public String toString()
        {
            String output = "-- CommandBlock --\n";
            try {
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output += "Command: " + Integer.toString(Command) + "\n";
                output += "Time: " + Float.toString(Time) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public CommandBlockBlock createCommandBlockBlock() {
         return new CommandBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ParcelMediaCommandMessage; }
    public CommandBlockBlock CommandBlock;

    public ParcelMediaCommandMessagePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)419);
        header.setReliable(true);
        CommandBlock = new CommandBlockBlock();
    }

    public ParcelMediaCommandMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        CommandBlock = new CommandBlockBlock(bytes);
     }

    public ParcelMediaCommandMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        CommandBlock = new CommandBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += CommandBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        CommandBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelMediaCommandMessage ---\n";
        output += CommandBlock.toString() + "\n";
        return output;
    }
}
