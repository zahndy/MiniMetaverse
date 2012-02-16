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

public class TransferPacketPacket extends Packet
{
    public class TransferDataBlock
    {
        public UUID TransferID = null;
        public int ChannelType = 0;
        public int Packet = 0;
        public int Status = 0;
        private byte[] _data;
        public byte[] getData() {
            return _data;
        }

        public void setData(byte[] value) throws Exception {
            if (value == null) {
                _data = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _data = new byte[value.length];
                System.arraycopy(value, 0, _data, 0, value.length);
            }
        }


        public int getLength(){
            int length = 28;
            if (getData() != null) { length += 2 + getData().length; }
            return length;
        }

        public TransferDataBlock() { }
        public TransferDataBlock(ByteBuffer bytes)
        {
            int length;
            TransferID = new UUID(bytes);
            ChannelType = bytes.getInt();
            Packet = bytes.getInt();
            Status = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransferID.GetBytes(bytes);
            bytes.putInt(ChannelType);
            bytes.putInt(Packet);
            bytes.putInt(Status);
            bytes.putShort((short)_data.length);
            bytes.put(_data);
        }

        @Override
        public String toString()
        {
            String output = "-- TransferData --\n";
            try {
                output += "TransferID: " + TransferID.toString() + "\n";
                output += "ChannelType: " + Integer.toString(ChannelType) + "\n";
                output += "Packet: " + Integer.toString(Packet) + "\n";
                output += "Status: " + Integer.toString(Status) + "\n";
                output += Helpers.FieldToString(_data, "Data") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransferDataBlock createTransferDataBlock() {
         return new TransferDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TransferPacket; }
    public TransferDataBlock TransferData;

    public TransferPacketPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)17);
        header.setReliable(true);
        TransferData = new TransferDataBlock();
    }

    public TransferPacketPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        TransferData = new TransferDataBlock(bytes);
     }

    public TransferPacketPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TransferData = new TransferDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TransferData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TransferData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TransferPacket ---\n";
        output += TransferData.toString() + "\n";
        return output;
    }
}
