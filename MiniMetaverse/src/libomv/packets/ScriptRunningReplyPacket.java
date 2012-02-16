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

public class ScriptRunningReplyPacket extends Packet
{
    public class ScriptBlock
    {
        public UUID ObjectID = null;
        public UUID ItemID = null;
        public boolean Running = false;

        public int getLength(){
            return 33;
        }

        public ScriptBlock() { }
        public ScriptBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
            ItemID = new UUID(bytes);
            Running = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            ItemID.GetBytes(bytes);
            bytes.put((byte)((Running) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- Script --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "Running: " + Boolean.toString(Running) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ScriptBlock createScriptBlock() {
         return new ScriptBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ScriptRunningReply; }
    public ScriptBlock Script;

    public ScriptRunningReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)244);
        header.setReliable(true);
        Script = new ScriptBlock();
    }

    public ScriptRunningReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Script = new ScriptBlock(bytes);
     }

    public ScriptRunningReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Script = new ScriptBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Script.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Script.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ScriptRunningReply ---\n";
        output += Script.toString() + "\n";
        return output;
    }
}
