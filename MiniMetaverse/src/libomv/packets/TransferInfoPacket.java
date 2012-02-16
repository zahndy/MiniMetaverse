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

public class TransferInfoPacket extends Packet
{
    public class TransferInfoBlock
    {
        public UUID TransferID = null;
        public int ChannelType = 0;
        public int TargetType = 0;
        public int Status = 0;
        public int Size = 0;
        private byte[] _params;
        public byte[] getParams() {
            return _params;
        }

        public void setParams(byte[] value) throws Exception {
            if (value == null) {
                _params = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _params = new byte[value.length];
                System.arraycopy(value, 0, _params, 0, value.length);
            }
        }


        public int getLength(){
            int length = 32;
            if (getParams() != null) { length += 2 + getParams().length; }
            return length;
        }

        public TransferInfoBlock() { }
        public TransferInfoBlock(ByteBuffer bytes)
        {
            int length;
            TransferID = new UUID(bytes);
            ChannelType = bytes.getInt();
            TargetType = bytes.getInt();
            Status = bytes.getInt();
            Size = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _params = new byte[length];
            bytes.get(_params);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransferID.GetBytes(bytes);
            bytes.putInt(ChannelType);
            bytes.putInt(TargetType);
            bytes.putInt(Status);
            bytes.putInt(Size);
            bytes.putShort((short)_params.length);
            bytes.put(_params);
        }

        @Override
        public String toString()
        {
            String output = "-- TransferInfo --\n";
            try {
                output += "TransferID: " + TransferID.toString() + "\n";
                output += "ChannelType: " + Integer.toString(ChannelType) + "\n";
                output += "TargetType: " + Integer.toString(TargetType) + "\n";
                output += "Status: " + Integer.toString(Status) + "\n";
                output += "Size: " + Integer.toString(Size) + "\n";
                output += Helpers.FieldToString(_params, "Params") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransferInfoBlock createTransferInfoBlock() {
         return new TransferInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TransferInfo; }
    public TransferInfoBlock TransferInfo;

    public TransferInfoPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)154);
        header.setReliable(true);
        TransferInfo = new TransferInfoBlock();
    }

    public TransferInfoPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TransferInfo = new TransferInfoBlock(bytes);
     }

    public TransferInfoPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TransferInfo = new TransferInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TransferInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TransferInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TransferInfo ---\n";
        output += TransferInfo.toString() + "\n";
        return output;
    }
}
