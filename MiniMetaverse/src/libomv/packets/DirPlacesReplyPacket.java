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

public class DirPlacesReplyPacket extends Packet
{
    public class QueryRepliesBlock
    {
        public UUID ParcelID = null;
        private byte[] _name;
        public byte[] getName() {
            return _name;
        }

        public void setName(byte[] value) throws Exception {
            if (value == null) {
                _name = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _name = new byte[value.length];
                System.arraycopy(value, 0, _name, 0, value.length);
            }
        }

        public boolean ForSale = false;
        public boolean Auction = false;
        public float Dwell = 0;

        public int getLength(){
            int length = 22;
            if (getName() != null) { length += 1 + getName().length; }
            return length;
        }

        public QueryRepliesBlock() { }
        public QueryRepliesBlock(ByteBuffer bytes)
        {
            int length;
            ParcelID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            ForSale = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Auction = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Dwell = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ParcelID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)((ForSale) ? 1 : 0));
            bytes.put((byte)((Auction) ? 1 : 0));
            bytes.putFloat(Dwell);
        }

        @Override
        public String toString()
        {
            String output = "-- QueryReplies --\n";
            try {
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += "ForSale: " + Boolean.toString(ForSale) + "\n";
                output += "Auction: " + Boolean.toString(Auction) + "\n";
                output += "Dwell: " + Float.toString(Dwell) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public QueryRepliesBlock createQueryRepliesBlock() {
         return new QueryRepliesBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.DirPlacesReply; }
    public UUID AgentID;
    public UUID[] QueryID;
    public QueryRepliesBlock[] QueryReplies;
    public int[] Status;

    public DirPlacesReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)35);
        header.setReliable(true);
        AgentID = new UUID();
        QueryID = new UUID[0];
        QueryReplies = new QueryRepliesBlock[0];
        Status = new int[0];
    }

    public DirPlacesReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        QueryID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            QueryID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        QueryReplies = new QueryRepliesBlock[count];
        for (int j = 0; j < count; j++)
        {
            QueryReplies[j] = new QueryRepliesBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        Status = new int[count];
        for (int j = 0; j < count; j++)
        {
            Status[j] = bytes.getInt();
        }
     }

    public DirPlacesReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        QueryID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            QueryID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        QueryReplies = new QueryRepliesBlock[count];
        for (int j = 0; j < count; j++)
        {
            QueryReplies[j] = new QueryRepliesBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        Status = new int[count];
        for (int j = 0; j < count; j++)
        {
            Status[j] = bytes.getInt();
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        length += QueryID.length * 16;
        length++;
        for (int j = 0; j < QueryReplies.length; j++) { length += QueryReplies[j].getLength(); }
        length++;
        length += Status.length * 4;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        bytes.put((byte)QueryID.length);
        for (int j = 0; j < QueryID.length; j++)
        {
            QueryID[j].GetBytes(bytes);
        }
        bytes.put((byte)QueryReplies.length);
        for (int j = 0; j < QueryReplies.length; j++) { QueryReplies[j].ToBytes(bytes); }
        bytes.put((byte)Status.length);
        for (int j = 0; j < Status.length; j++)
        {
            bytes.putInt(Status[j]);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- DirPlacesReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        for (int j = 0; j < QueryID.length; j++)
        {
            output += "QueryID[" + j + "]: " + QueryID[j].toString() + "\n";
        }
        for (int j = 0; j < QueryReplies.length; j++)
        {
            output += QueryReplies[j].toString() + "\n";
        }
        for (int j = 0; j < Status.length; j++)
        {
            output += "Status[" + j + "]: " + Integer.toString(Status[j]) + "\n";
        }
        return output;
    }
}