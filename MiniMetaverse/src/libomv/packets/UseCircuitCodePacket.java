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

public class UseCircuitCodePacket extends Packet
{
    public class CircuitCodeBlock
    {
        public int Code = 0;
        public UUID SessionID = null;
        public UUID ID = null;

        public int getLength(){
            return 36;
        }

        public CircuitCodeBlock() { }
        public CircuitCodeBlock(ByteBuffer bytes)
        {
            Code = bytes.getInt();
            SessionID = new UUID(bytes);
            ID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Code);
            SessionID.GetBytes(bytes);
            ID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- CircuitCode --\n";
            try {
                output += "Code: " + Integer.toString(Code) + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "ID: " + ID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public CircuitCodeBlock createCircuitCodeBlock() {
         return new CircuitCodeBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UseCircuitCode; }
    public CircuitCodeBlock CircuitCode;

    public UseCircuitCodePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)3);
        header.setReliable(true);
        CircuitCode = new CircuitCodeBlock();
    }

    public UseCircuitCodePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        CircuitCode = new CircuitCodeBlock(bytes);
     }

    public UseCircuitCodePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        CircuitCode = new CircuitCodeBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += CircuitCode.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        CircuitCode.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UseCircuitCode ---\n";
        output += CircuitCode.toString() + "\n";
        return output;
    }
}
