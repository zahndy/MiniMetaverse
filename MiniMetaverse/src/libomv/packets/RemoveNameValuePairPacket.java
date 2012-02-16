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

public class RemoveNameValuePairPacket extends Packet
{
    public class NameValueDataBlock
    {
        private byte[] _nvpair;
        public byte[] getNVPair() {
            return _nvpair;
        }

        public void setNVPair(byte[] value) throws Exception {
            if (value == null) {
                _nvpair = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _nvpair = new byte[value.length];
                System.arraycopy(value, 0, _nvpair, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getNVPair() != null) { length += 2 + getNVPair().length; }
            return length;
        }

        public NameValueDataBlock() { }
        public NameValueDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.getShort() & 0xFFFF;
            _nvpair = new byte[length];
            bytes.get(_nvpair);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_nvpair.length);
            bytes.put(_nvpair);
        }

        @Override
        public String toString()
        {
            String output = "-- NameValueData --\n";
            try {
                output += Helpers.FieldToString(_nvpair, "NVPair") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NameValueDataBlock createNameValueDataBlock() {
         return new NameValueDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RemoveNameValuePair; }
    public UUID ID;
    public NameValueDataBlock[] NameValueData;

    public RemoveNameValuePairPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)330);
        header.setReliable(true);
        ID = new UUID();
        NameValueData = new NameValueDataBlock[0];
    }

    public RemoveNameValuePairPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        NameValueData = new NameValueDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            NameValueData[j] = new NameValueDataBlock(bytes);
        }
     }

    public RemoveNameValuePairPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        NameValueData = new NameValueDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            NameValueData[j] = new NameValueDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        for (int j = 0; j < NameValueData.length; j++) { length += NameValueData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ID.GetBytes(bytes);
        bytes.put((byte)NameValueData.length);
        for (int j = 0; j < NameValueData.length; j++) { NameValueData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RemoveNameValuePair ---\n";
        output += "ID: " + ID.toString() + "\n";
        for (int j = 0; j < NameValueData.length; j++)
        {
            output += NameValueData[j].toString() + "\n";
        }
        return output;
    }
}
