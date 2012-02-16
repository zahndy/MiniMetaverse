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

public class PayPriceReplyPacket extends Packet
{
    public class ObjectDataBlock
    {
        public UUID ObjectID = null;
        public int DefaultPayPrice = 0;

        public int getLength(){
            return 20;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            ObjectID = new UUID(bytes);
            DefaultPayPrice = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            bytes.putInt(DefaultPayPrice);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "DefaultPayPrice: " + Integer.toString(DefaultPayPrice) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ObjectDataBlock createObjectDataBlock() {
         return new ObjectDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.PayPriceReply; }
    public ObjectDataBlock ObjectData;
    public int[] PayButton;

    public PayPriceReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)162);
        header.setReliable(true);
        ObjectData = new ObjectDataBlock();
        PayButton = new int[0];
    }

    public PayPriceReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        PayButton = new int[count];
        for (int j = 0; j < count; j++)
        {
            PayButton[j] = bytes.getInt();
        }
     }

    public PayPriceReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        PayButton = new int[count];
        for (int j = 0; j < count; j++)
        {
            PayButton[j] = bytes.getInt();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ObjectData.getLength();
        length++;
        length += PayButton.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ObjectData.ToBytes(bytes);
        bytes.put((byte)PayButton.length);
        for (int j = 0; j < PayButton.length; j++)
        {
            bytes.putInt(PayButton[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PayPriceReply ---\n";
        output += ObjectData.toString() + "\n";
        for (int j = 0; j < PayButton.length; j++)
        {
            output += "PayButton[" + j + "]: " + Integer.toString(PayButton[j]) + "\n";
        }
        return output;
    }
}
