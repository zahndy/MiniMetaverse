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

import libomv.utils.Helpers;
import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;
import libomv.types.OverflowException;

public class EstateCovenantReplyPacket extends Packet
{
    public class DataBlock
    {
        public UUID CovenantID = null;
        public int CovenantTimestamp = 0;
        private byte[] _estatename;
        public byte[] getEstateName() {
            return _estatename;
        }

        public void setEstateName(byte[] value) throws Exception {
            if (value == null) {
                _estatename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _estatename = new byte[value.length];
                System.arraycopy(value, 0, _estatename, 0, value.length);
            }
        }

        public UUID EstateOwnerID = null;

        public int getLength(){
            int length = 36;
            if (getEstateName() != null) { length += 1 + getEstateName().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            CovenantID = new UUID(bytes);
            CovenantTimestamp = bytes.getInt();
            length = bytes.get() & 0xFF;
            _estatename = new byte[length];
            bytes.get(_estatename);
            EstateOwnerID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            CovenantID.GetBytes(bytes);
            bytes.putInt(CovenantTimestamp);
            bytes.put((byte)_estatename.length);
            bytes.put(_estatename);
            EstateOwnerID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "CovenantID: " + CovenantID.toString() + "\n";
                output += "CovenantTimestamp: " + Integer.toString(CovenantTimestamp) + "\n";
                output += Helpers.FieldToString(_estatename, "EstateName") + "\n";
                output += "EstateOwnerID: " + EstateOwnerID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.EstateCovenantReply; }
    public DataBlock Data;

    public EstateCovenantReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)204);
        header.setReliable(true);
        Data = new DataBlock();
    }

    public EstateCovenantReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
     }

    public EstateCovenantReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- EstateCovenantReply ---\n";
        output += Data.toString() + "\n";
        return output;
    }
}
