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
import libomv.types.OverflowException;
import libomv.types.UUID;

public class ScriptMailRegistrationPacket extends Packet
{
    public class DataBlockBlock
    {
        private byte[] _targetip;
        public byte[] getTargetIP() {
            return _targetip;
        }

        public void setTargetIP(byte[] value) throws Exception {
            if (value == null) {
                _targetip = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _targetip = new byte[value.length];
                System.arraycopy(value, 0, _targetip, 0, value.length);
            }
        }

        public short TargetPort = 0;
        public UUID TaskID = null;
        public int Flags = 0;

        public int getLength(){
            int length = 22;
            if (getTargetIP() != null) { length += 1 + getTargetIP().length; }
            return length;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _targetip = new byte[length];
            bytes.get(_targetip);
            TargetPort = (short)((bytes.get() << 8) + bytes.get());
            TaskID = new UUID(bytes);
            Flags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_targetip.length);
            bytes.put(_targetip);
            bytes.put((byte)((TargetPort >> 8) % 256));
            bytes.put((byte)(TargetPort % 256));
            TaskID.GetBytes(bytes);
            bytes.putInt(Flags);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += Helpers.FieldToString(_targetip, "TargetIP") + "\n";
                output += "TargetPort: " + Short.toString(TargetPort) + "\n";
                output += "TaskID: " + TaskID.toString() + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlockBlock createDataBlockBlock() {
         return new DataBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ScriptMailRegistration; }
    public DataBlockBlock DataBlock;

    public ScriptMailRegistrationPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)418);
        header.setReliable(true);
        DataBlock = new DataBlockBlock();
    }

    public ScriptMailRegistrationPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        DataBlock = new DataBlockBlock(bytes);
     }

    public ScriptMailRegistrationPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        DataBlock = new DataBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += DataBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        DataBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ScriptMailRegistration ---\n";
        output += DataBlock.toString() + "\n";
        return output;
    }
}
