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

public class MuteListUpdatePacket extends Packet
{
    public class MuteDataBlock
    {
        public UUID AgentID = null;
        private byte[] _filename;
        public byte[] getFilename() {
            return _filename;
        }

        public void setFilename(byte[] value) throws Exception {
            if (value == null) {
                _filename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _filename = new byte[value.length];
                System.arraycopy(value, 0, _filename, 0, value.length);
            }
        }


        public int getLength(){
            int length = 16;
            if (getFilename() != null) { length += 1 + getFilename().length; }
            return length;
        }

        public MuteDataBlock() { }
        public MuteDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _filename = new byte[length];
            bytes.get(_filename);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.put((byte)_filename.length);
            bytes.put(_filename);
        }

        @Override
        public String toString()
        {
            String output = "-- MuteData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += Helpers.FieldToString(_filename, "Filename") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MuteDataBlock createMuteDataBlock() {
         return new MuteDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.MuteListUpdate; }
    public MuteDataBlock MuteData;

    public MuteListUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)318);
        header.setReliable(true);
        MuteData = new MuteDataBlock();
    }

    public MuteListUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        MuteData = new MuteDataBlock(bytes);
     }

    public MuteListUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        MuteData = new MuteDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += MuteData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        MuteData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- MuteListUpdate ---\n";
        output += MuteData.toString() + "\n";
        return output;
    }
}
