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

public class PlacesReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID QueryID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            QueryID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            QueryID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "QueryID: " + QueryID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class QueryDataBlock
    {
        public UUID OwnerID = null;
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

        private byte[] _desc;
        public byte[] getDesc() {
            return _desc;
        }

        public void setDesc(byte[] value) throws Exception {
            if (value == null) {
                _desc = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _desc = new byte[value.length];
                System.arraycopy(value, 0, _desc, 0, value.length);
            }
        }

        public int ActualArea = 0;
        public int BillableArea = 0;
        public byte Flags = 0;
        public float GlobalX = 0;
        public float GlobalY = 0;
        public float GlobalZ = 0;
        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }

        public UUID SnapshotID = null;
        public float Dwell = 0;
        public int Price = 0;

        public int getLength(){
            int length = 61;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDesc() != null) { length += 1 + getDesc().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public QueryDataBlock() { }
        public QueryDataBlock(ByteBuffer bytes)
        {
            int length;
            OwnerID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _desc = new byte[length];
            bytes.get(_desc);
            ActualArea = bytes.getInt();
            BillableArea = bytes.getInt();
            Flags = bytes.get();
            GlobalX = bytes.getFloat();
            GlobalY = bytes.getFloat();
            GlobalZ = bytes.getFloat();
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            SnapshotID = new UUID(bytes);
            Dwell = bytes.getFloat();
            Price = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            OwnerID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_desc.length);
            bytes.put(_desc);
            bytes.putInt(ActualArea);
            bytes.putInt(BillableArea);
            bytes.put(Flags);
            bytes.putFloat(GlobalX);
            bytes.putFloat(GlobalY);
            bytes.putFloat(GlobalZ);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            SnapshotID.GetBytes(bytes);
            bytes.putFloat(Dwell);
            bytes.putInt(Price);
        }

        @Override
        public String toString()
        {
            String output = "-- QueryData --\n";
            try {
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_desc, "Desc") + "\n";
                output += "ActualArea: " + Integer.toString(ActualArea) + "\n";
                output += "BillableArea: " + Integer.toString(BillableArea) + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
                output += "GlobalX: " + Float.toString(GlobalX) + "\n";
                output += "GlobalY: " + Float.toString(GlobalY) + "\n";
                output += "GlobalZ: " + Float.toString(GlobalZ) + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "SnapshotID: " + SnapshotID.toString() + "\n";
                output += "Dwell: " + Float.toString(Dwell) + "\n";
                output += "Price: " + Integer.toString(Price) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public QueryDataBlock createQueryDataBlock() {
         return new QueryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.PlacesReply; }
    public AgentDataBlock AgentData;
    public UUID TransactionID;
    public QueryDataBlock[] QueryData;

    public PlacesReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)30);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        TransactionID = new UUID();
        QueryData = new QueryDataBlock[0];
    }

    public PlacesReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        TransactionID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        QueryData = new QueryDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            QueryData[j] = new QueryDataBlock(bytes);
        }
     }

    public PlacesReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        TransactionID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        QueryData = new QueryDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            QueryData[j] = new QueryDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += 16;
        length++;
        for (int j = 0; j < QueryData.length; j++) { length += QueryData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        TransactionID.GetBytes(bytes);
        bytes.put((byte)QueryData.length);
        for (int j = 0; j < QueryData.length; j++) { QueryData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PlacesReply ---\n";
        output += AgentData.toString() + "\n";
        output += "TransactionID: " + TransactionID.toString() + "\n";
        for (int j = 0; j < QueryData.length; j++)
        {
            output += QueryData[j].toString() + "\n";
        }
        return output;
    }
}
