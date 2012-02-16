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

public class ParcelInfoReplyPacket extends Packet
{
    public class DataBlock
    {
        public UUID ParcelID = null;
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
        public int SalePrice = 0;
        public int AuctionID = 0;

        public int getLength(){
            int length = 81;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDesc() != null) { length += 1 + getDesc().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            ParcelID = new UUID(bytes);
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
            SalePrice = bytes.getInt();
            AuctionID = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ParcelID.GetBytes(bytes);
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
            bytes.putInt(SalePrice);
            bytes.putInt(AuctionID);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "ParcelID: " + ParcelID.toString() + "\n";
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
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += "AuctionID: " + Integer.toString(AuctionID) + "\n";
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
    public PacketType getType() { return PacketType.ParcelInfoReply; }
    public UUID AgentID;
    public DataBlock Data;

    public ParcelInfoReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)55);
        header.setReliable(true);
        AgentID = new UUID();
        Data = new DataBlock();
    }

    public ParcelInfoReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        Data = new DataBlock(bytes);
     }

    public ParcelInfoReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelInfoReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += Data.toString() + "\n";
        return output;
    }
}
