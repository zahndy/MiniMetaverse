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
import libomv.types.Vector3;

public class UpdateParcelPacket extends Packet
{
    public class ParcelDataBlock
    {
        public UUID ParcelID = null;
        public long RegionHandle = 0;
        public UUID OwnerID = null;
        public boolean GroupOwned = false;
        public byte Status = 0;
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

        private byte[] _description;
        public byte[] getDescription() {
            return _description;
        }

        public void setDescription(byte[] value) throws Exception {
            if (value == null) {
                _description = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _description = new byte[value.length];
                System.arraycopy(value, 0, _description, 0, value.length);
            }
        }

        private byte[] _musicurl;
        public byte[] getMusicURL() {
            return _musicurl;
        }

        public void setMusicURL(byte[] value) throws Exception {
            if (value == null) {
                _musicurl = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _musicurl = new byte[value.length];
                System.arraycopy(value, 0, _musicurl, 0, value.length);
            }
        }

        public float RegionX = 0;
        public float RegionY = 0;
        public int ActualArea = 0;
        public int BillableArea = 0;
        public boolean ShowDir = false;
        public boolean IsForSale = false;
        public byte Category = 0;
        public UUID SnapshotID = null;
        public Vector3 UserLocation = null;
        public int SalePrice = 0;
        public UUID AuthorizedBuyerID = null;
        public boolean AllowPublish = false;
        public boolean MaturePublish = false;

        public int getLength(){
            int length = 111;
            if (getName() != null) { length += 1 + getName().length; }
            if (getDescription() != null) { length += 1 + getDescription().length; }
            if (getMusicURL() != null) { length += 1 + getMusicURL().length; }
            return length;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            int length;
            ParcelID = new UUID(bytes);
            RegionHandle = bytes.getLong();
            OwnerID = new UUID(bytes);
            GroupOwned = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Status = bytes.get();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
            length = bytes.get() & 0xFF;
            _musicurl = new byte[length];
            bytes.get(_musicurl);
            RegionX = bytes.getFloat();
            RegionY = bytes.getFloat();
            ActualArea = bytes.getInt();
            BillableArea = bytes.getInt();
            ShowDir = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            IsForSale = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Category = bytes.get();
            SnapshotID = new UUID(bytes);
            UserLocation = new Vector3(bytes);
            SalePrice = bytes.getInt();
            AuthorizedBuyerID = new UUID(bytes);
            AllowPublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            MaturePublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ParcelID.GetBytes(bytes);
            bytes.putLong(RegionHandle);
            OwnerID.GetBytes(bytes);
            bytes.put((byte)((GroupOwned) ? 1 : 0));
            bytes.put(Status);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_description.length);
            bytes.put(_description);
            bytes.put((byte)_musicurl.length);
            bytes.put(_musicurl);
            bytes.putFloat(RegionX);
            bytes.putFloat(RegionY);
            bytes.putInt(ActualArea);
            bytes.putInt(BillableArea);
            bytes.put((byte)((ShowDir) ? 1 : 0));
            bytes.put((byte)((IsForSale) ? 1 : 0));
            bytes.put(Category);
            SnapshotID.GetBytes(bytes);
            UserLocation.GetBytes(bytes);
            bytes.putInt(SalePrice);
            AuthorizedBuyerID.GetBytes(bytes);
            bytes.put((byte)((AllowPublish) ? 1 : 0));
            bytes.put((byte)((MaturePublish) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "ParcelID: " + ParcelID.toString() + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "GroupOwned: " + Boolean.toString(GroupOwned) + "\n";
                output += "Status: " + Byte.toString(Status) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output += Helpers.FieldToString(_musicurl, "MusicURL") + "\n";
                output += "RegionX: " + Float.toString(RegionX) + "\n";
                output += "RegionY: " + Float.toString(RegionY) + "\n";
                output += "ActualArea: " + Integer.toString(ActualArea) + "\n";
                output += "BillableArea: " + Integer.toString(BillableArea) + "\n";
                output += "ShowDir: " + Boolean.toString(ShowDir) + "\n";
                output += "IsForSale: " + Boolean.toString(IsForSale) + "\n";
                output += "Category: " + Byte.toString(Category) + "\n";
                output += "SnapshotID: " + SnapshotID.toString() + "\n";
                output += "UserLocation: " + UserLocation.toString() + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += "AuthorizedBuyerID: " + AuthorizedBuyerID.toString() + "\n";
                output += "AllowPublish: " + Boolean.toString(AllowPublish) + "\n";
                output += "MaturePublish: " + Boolean.toString(MaturePublish) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ParcelDataBlock createParcelDataBlock() {
         return new ParcelDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UpdateParcel; }
    public ParcelDataBlock ParcelData;

    public UpdateParcelPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)221);
        header.setReliable(true);
        ParcelData = new ParcelDataBlock();
    }

    public UpdateParcelPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ParcelData = new ParcelDataBlock(bytes);
     }

    public UpdateParcelPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ParcelData = new ParcelDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ParcelData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ParcelData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UpdateParcel ---\n";
        output += ParcelData.toString() + "\n";
        return output;
    }
}
