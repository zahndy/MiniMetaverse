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

public class ClassifiedInfoReplyPacket extends Packet
{
    public class DataBlock
    {
        public UUID ClassifiedID = null;
        public UUID CreatorID = null;
        public int CreationDate = 0;
        public int ExpirationDate = 0;
        public int Category = 0;
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

        public UUID ParcelID = null;
        public int ParentEstate = 0;
        public UUID SnapshotID = null;
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

        public Vector3d PosGlobal = null;
        private byte[] _parcelname;
        public byte[] getParcelName() {
            return _parcelname;
        }

        public void setParcelName(byte[] value) throws Exception {
            if (value == null) {
                _parcelname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _parcelname = new byte[value.length];
                System.arraycopy(value, 0, _parcelname, 0, value.length);
            }
        }

        public byte ClassifiedFlags = 0;
        public int PriceForListing = 0;

        public int getLength(){
            int length = 109;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDesc() != null) { length += 2 + getDesc().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            if (getParcelName() != null) { length += 1 + getParcelName().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            ClassifiedID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            CreationDate = bytes.getInt();
            ExpirationDate = bytes.getInt();
            Category = bytes.getInt();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.getShort() & 0xFFFF;
            _desc = new byte[length];
            bytes.get(_desc);
            ParcelID = new UUID(bytes);
            ParentEstate = bytes.getInt();
            SnapshotID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            PosGlobal = new Vector3d(bytes);
            length = bytes.get() & 0xFF;
            _parcelname = new byte[length];
            bytes.get(_parcelname);
            ClassifiedFlags = bytes.get();
            PriceForListing = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ClassifiedID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            bytes.putInt(CreationDate);
            bytes.putInt(ExpirationDate);
            bytes.putInt(Category);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.putShort((short)_desc.length);
            bytes.put(_desc);
            ParcelID.GetBytes(bytes);
            bytes.putInt(ParentEstate);
            SnapshotID.GetBytes(bytes);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            PosGlobal.GetBytes(bytes);
            bytes.put((byte)_parcelname.length);
            bytes.put(_parcelname);
            bytes.put(ClassifiedFlags);
            bytes.putInt(PriceForListing);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "ClassifiedID: " + ClassifiedID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "CreationDate: " + Integer.toString(CreationDate) + "\n";
                output += "ExpirationDate: " + Integer.toString(ExpirationDate) + "\n";
                output += "Category: " + Integer.toString(Category) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_desc, "Desc") + "\n";
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += "ParentEstate: " + Integer.toString(ParentEstate) + "\n";
                output += "SnapshotID: " + SnapshotID.toString() + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "PosGlobal: " + PosGlobal.toString() + "\n";
                output += Helpers.FieldToString(_parcelname, "ParcelName") + "\n";
                output += "ClassifiedFlags: " + Byte.toString(ClassifiedFlags) + "\n";
                output += "PriceForListing: " + Integer.toString(PriceForListing) + "\n";
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
    public PacketType getType() { return PacketType.ClassifiedInfoReply; }
    public UUID AgentID;
    public DataBlock Data;

    public ClassifiedInfoReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)44);
        header.setReliable(true);
        AgentID = new UUID();
        Data = new DataBlock();
    }

    public ClassifiedInfoReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        Data = new DataBlock(bytes);
     }

    public ClassifiedInfoReplyPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- ClassifiedInfoReply ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += Data.toString() + "\n";
        return output;
    }
}
