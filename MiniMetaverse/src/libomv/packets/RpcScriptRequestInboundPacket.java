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

public class RpcScriptRequestInboundPacket extends Packet
{
    public class TargetBlockBlock
    {
        public int GridX = 0;
        public int GridY = 0;

        public int getLength(){
            return 8;
        }

        public TargetBlockBlock() { }
        public TargetBlockBlock(ByteBuffer bytes)
        {
            GridX = bytes.getInt();
            GridY = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(GridX);
            bytes.putInt(GridY);
        }

        @Override
        public String toString()
        {
            String output = "-- TargetBlock --\n";
            try {
                output += "GridX: " + Integer.toString(GridX) + "\n";
                output += "GridY: " + Integer.toString(GridY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TargetBlockBlock createTargetBlockBlock() {
         return new TargetBlockBlock();
    }

    public class DataBlockBlock
    {
        public UUID TaskID = null;
        public UUID ItemID = null;
        public UUID ChannelID = null;
        public int IntValue = 0;
        private byte[] _stringvalue;
        public byte[] getStringValue() {
            return _stringvalue;
        }

        public void setStringValue(byte[] value) throws Exception {
            if (value == null) {
                _stringvalue = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _stringvalue = new byte[value.length];
                System.arraycopy(value, 0, _stringvalue, 0, value.length);
            }
        }


        public int getLength(){
            int length = 52;
            if (getStringValue() != null) { length += 2 + getStringValue().length; }
            return length;
        }

        public DataBlockBlock() { }
        public DataBlockBlock(ByteBuffer bytes)
        {
            int length;
            TaskID = new UUID(bytes);
            ItemID = new UUID(bytes);
            ChannelID = new UUID(bytes);
            IntValue = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _stringvalue = new byte[length];
            bytes.get(_stringvalue);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TaskID.GetBytes(bytes);
            ItemID.GetBytes(bytes);
            ChannelID.GetBytes(bytes);
            bytes.putInt(IntValue);
            bytes.putShort((short)_stringvalue.length);
            bytes.put(_stringvalue);
        }

        @Override
        public String toString()
        {
            String output = "-- DataBlock --\n";
            try {
                output += "TaskID: " + TaskID.toString() + "\n";
                output += "ItemID: " + ItemID.toString() + "\n";
                output += "ChannelID: " + ChannelID.toString() + "\n";
                output += "IntValue: " + Integer.toString(IntValue) + "\n";
                output += Helpers.FieldToString(_stringvalue, "StringValue") + "\n";
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
    public PacketType getType() { return PacketType.RpcScriptRequestInbound; }
    public TargetBlockBlock TargetBlock;
    public DataBlockBlock DataBlock;

    public RpcScriptRequestInboundPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)415);
        header.setReliable(true);
        TargetBlock = new TargetBlockBlock();
        DataBlock = new DataBlockBlock();
    }

    public RpcScriptRequestInboundPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TargetBlock = new TargetBlockBlock(bytes);
        DataBlock = new DataBlockBlock(bytes);
     }

    public RpcScriptRequestInboundPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TargetBlock = new TargetBlockBlock(bytes);
        DataBlock = new DataBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TargetBlock.getLength();
        length += DataBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TargetBlock.ToBytes(bytes);
        DataBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RpcScriptRequestInbound ---\n";
        output += TargetBlock.toString() + "\n";
        output += DataBlock.toString() + "\n";
        return output;
    }
}
