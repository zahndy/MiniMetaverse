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
import libomv.types.Vector3d;

public class PickInfoUpdatePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class DataBlock
    {
        public UUID PickID = null;
        public UUID CreatorID = null;
        public boolean TopPick = false;
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

        private byte[] _desc;
        public byte[] getDesc() {
            return _desc;
        }

        public void setDesc(byte[] value) throws Exception {
            if (value == null) {
                _desc = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _desc = new byte[value.length];
                System.arraycopy(value, 0, _desc, 0, value.length);
            }
        }

        public UUID SnapshotID = null;
        public Vector3d PosGlobal = null;
        public int SortOrder = 0;
        public boolean Enabled = false;

        public int getLength(){
            int length = 94;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDesc() != null) { length += 2 + getDesc().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            PickID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            TopPick = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            ParcelID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.getShort() & 0xFFFF;
            _desc = new byte[length];
            bytes.get(_desc);
            SnapshotID = new UUID(bytes);
            PosGlobal = new Vector3d(bytes);
            SortOrder = bytes.getInt();
            Enabled = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            PickID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            bytes.put((byte)((TopPick) ? 1 : 0));
            ParcelID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.putShort((short)_desc.length);
            bytes.put(_desc);
            SnapshotID.GetBytes(bytes);
            PosGlobal.GetBytes(bytes);
            bytes.putInt(SortOrder);
            bytes.put((byte)((Enabled) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "PickID: " + PickID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "TopPick: " + Boolean.toString(TopPick) + "\n";
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_desc, "Desc") + "\n";
                output += "SnapshotID: " + SnapshotID.toString() + "\n";
                output += "PosGlobal: " + PosGlobal.toString() + "\n";
                output += "SortOrder: " + Integer.toString(SortOrder) + "\n";
                output += "Enabled: " + Boolean.toString(Enabled) + "\n";
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
    public PacketType getType() { return PacketType.PickInfoUpdate; }
    public AgentDataBlock AgentData;
    public DataBlock Data;

    public PickInfoUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)185);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        Data = new DataBlock();
    }

    public PickInfoUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
     }

    public PickInfoUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- PickInfoUpdate ---\n";
        output += AgentData.toString() + "\n";
        output += Data.toString() + "\n";
        return output;
    }
}
