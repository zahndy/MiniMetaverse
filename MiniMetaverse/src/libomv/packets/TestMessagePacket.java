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

public class TestMessagePacket extends Packet
{
    public class NeighborBlockBlock
    {
        public int Test0 = 0;
        public int Test1 = 0;
        public int Test2 = 0;

        public int getLength(){
            return 12;
        }

        public NeighborBlockBlock() { }
        public NeighborBlockBlock(ByteBuffer bytes)
        {
            Test0 = bytes.getInt();
            Test1 = bytes.getInt();
            Test2 = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Test0);
            bytes.putInt(Test1);
            bytes.putInt(Test2);
        }

        @Override
        public String toString()
        {
            String output = "-- NeighborBlock --\n";
            try {
                output += "Test0: " + Integer.toString(Test0) + "\n";
                output += "Test1: " + Integer.toString(Test1) + "\n";
                output += "Test2: " + Integer.toString(Test2) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NeighborBlockBlock createNeighborBlockBlock() {
         return new NeighborBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TestMessage; }
    public int Test1;
    public NeighborBlockBlock[] NeighborBlock;

    public TestMessagePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)1);
        header.setReliable(true);
        NeighborBlock = new NeighborBlockBlock[4];
    }

    public TestMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Test1 = bytes.getInt();
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
     }

    public TestMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Test1 = bytes.getInt();
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 4;
        for (int j = 0; j < 4; j++) { length += NeighborBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.putInt(Test1);
        for (int j = 0; j < 4; j++) { NeighborBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TestMessage ---\n";
        output += "Test1: " + Integer.toString(Test1) + "\n";
        for (int j = 0; j < 4; j++)
        {
            output += NeighborBlock[j].toString() + "\n";
        }
        return output;
    }
}
