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

public class TeleportLureRequestPacket extends Packet
{
    public class InfoBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID LureID = null;
        public int TeleportFlags = 0;

        public int getLength(){
            return 52;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            LureID = new UUID(bytes);
            TeleportFlags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            LureID.GetBytes(bytes);
            bytes.putInt(TeleportFlags);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "LureID: " + LureID.toString() + "\n";
                output += "TeleportFlags: " + Integer.toString(TeleportFlags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlock createInfoBlock() {
         return new InfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TeleportLureRequest; }
    public InfoBlock Info;

    public TeleportLureRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)71);
        header.setReliable(true);
        Info = new InfoBlock();
    }

    public TeleportLureRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Info = new InfoBlock(bytes);
     }

    public TeleportLureRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Info = new InfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Info.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Info.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TeleportLureRequest ---\n";
        output += Info.toString() + "\n";
        return output;
    }
}