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
import libomv.types.Vector3;
import libomv.types.OverflowException;

public class ParcelPropertiesPacket extends Packet
{
    public class ParcelDataBlock
    {
        public int RequestResult = 0;
        public int SequenceID = 0;
        public boolean SnapSelection = false;
        public int SelfCount = 0;
        public int OtherCount = 0;
        public int PublicCount = 0;
        public int LocalID = 0;
        public UUID OwnerID = null;
        public boolean IsGroupOwned = false;
        public int AuctionID = 0;
        public int ClaimDate = 0;
        public int ClaimPrice = 0;
        public int RentPrice = 0;
        public Vector3 AABBMin = null;
        public Vector3 AABBMax = null;
        private byte[] _bitmap;
        public byte[] getBitmap() {
            return _bitmap;
        }

        public void setBitmap(byte[] value) throws Exception {
            if (value == null) {
                _bitmap = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _bitmap = new byte[value.length];
                System.arraycopy(value, 0, _bitmap, 0, value.length);
            }
        }

        public int Area = 0;
        public byte Status = 0;
        public int SimWideMaxPrims = 0;
        public int SimWideTotalPrims = 0;
        public int MaxPrims = 0;
        public int TotalPrims = 0;
        public int OwnerPrims = 0;
        public int GroupPrims = 0;
        public int OtherPrims = 0;
        public int SelectedPrims = 0;
        public float ParcelPrimBonus = 0;
        public int OtherCleanTime = 0;
        public int ParcelFlags = 0;
        public int SalePrice = 0;
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

        private byte[] _mediaurl;
        public byte[] getMediaURL() {
            return _mediaurl;
        }

        public void setMediaURL(byte[] value) throws Exception {
            if (value == null) {
                _mediaurl = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _mediaurl = new byte[value.length];
                System.arraycopy(value, 0, _mediaurl, 0, value.length);
            }
        }

        public UUID MediaID = null;
        public byte MediaAutoScale = 0;
        public UUID GroupID = null;
        public int PassPrice = 0;
        public float PassHours = 0;
        public byte Category = 0;
        public UUID AuthBuyerID = null;
        public UUID SnapshotID = null;
        public Vector3 UserLocation = null;
        public Vector3 UserLookAt = null;
        public byte LandingType = 0;
        public boolean RegionPushOverride = false;
        public boolean RegionDenyAnonymous = false;
        public boolean RegionDenyIdentified = false;
        public boolean RegionDenyTransacted = false;

        public int getLength(){
            int length = 238;
            if (getBitmap() != null) { length += 2 + getBitmap().length; }
            if (getName() != null) { length += 1 + getName().length; }
            if (getDesc() != null) { length += 1 + getDesc().length; }
            if (getMusicURL() != null) { length += 1 + getMusicURL().length; }
            if (getMediaURL() != null) { length += 1 + getMediaURL().length; }
            return length;
        }

        public ParcelDataBlock() { }
        public ParcelDataBlock(ByteBuffer bytes)
        {
            int length;
            RequestResult = bytes.getInt();
            SequenceID = bytes.getInt();
            SnapSelection = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            SelfCount = bytes.getInt();
            OtherCount = bytes.getInt();
            PublicCount = bytes.getInt();
            LocalID = bytes.getInt();
            OwnerID = new UUID(bytes);
            IsGroupOwned = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            AuctionID = bytes.getInt();
            ClaimDate = bytes.getInt();
            ClaimPrice = bytes.getInt();
            RentPrice = bytes.getInt();
            AABBMin = new Vector3(bytes);
            AABBMax = new Vector3(bytes);
            length = bytes.getShort() & 0xFFFF;
            _bitmap = new byte[length];
            bytes.get(_bitmap);
            Area = bytes.getInt();
            Status = bytes.get();
            SimWideMaxPrims = bytes.getInt();
            SimWideTotalPrims = bytes.getInt();
            MaxPrims = bytes.getInt();
            TotalPrims = bytes.getInt();
            OwnerPrims = bytes.getInt();
            GroupPrims = bytes.getInt();
            OtherPrims = bytes.getInt();
            SelectedPrims = bytes.getInt();
            ParcelPrimBonus = bytes.getFloat();
            OtherCleanTime = bytes.getInt();
            ParcelFlags = bytes.getInt();
            SalePrice = bytes.getInt();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            length = bytes.get() & 0xFF;
            _desc = new byte[length];
            bytes.get(_desc);
            length = bytes.get() & 0xFF;
            _musicurl = new byte[length];
            bytes.get(_musicurl);
            length = bytes.get() & 0xFF;
            _mediaurl = new byte[length];
            bytes.get(_mediaurl);
            MediaID = new UUID(bytes);
            MediaAutoScale = bytes.get();
            GroupID = new UUID(bytes);
            PassPrice = bytes.getInt();
            PassHours = bytes.getFloat();
            Category = bytes.get();
            AuthBuyerID = new UUID(bytes);
            SnapshotID = new UUID(bytes);
            UserLocation = new Vector3(bytes);
            UserLookAt = new Vector3(bytes);
            LandingType = bytes.get();
            RegionPushOverride = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RegionDenyAnonymous = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RegionDenyIdentified = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            RegionDenyTransacted = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(RequestResult);
            bytes.putInt(SequenceID);
            bytes.put((byte)((SnapSelection) ? 1 : 0));
            bytes.putInt(SelfCount);
            bytes.putInt(OtherCount);
            bytes.putInt(PublicCount);
            bytes.putInt(LocalID);
            OwnerID.GetBytes(bytes);
            bytes.put((byte)((IsGroupOwned) ? 1 : 0));
            bytes.putInt(AuctionID);
            bytes.putInt(ClaimDate);
            bytes.putInt(ClaimPrice);
            bytes.putInt(RentPrice);
            AABBMin.GetBytes(bytes);
            AABBMax.GetBytes(bytes);
            bytes.putShort((short)_bitmap.length);
            bytes.put(_bitmap);
            bytes.putInt(Area);
            bytes.put(Status);
            bytes.putInt(SimWideMaxPrims);
            bytes.putInt(SimWideTotalPrims);
            bytes.putInt(MaxPrims);
            bytes.putInt(TotalPrims);
            bytes.putInt(OwnerPrims);
            bytes.putInt(GroupPrims);
            bytes.putInt(OtherPrims);
            bytes.putInt(SelectedPrims);
            bytes.putFloat(ParcelPrimBonus);
            bytes.putInt(OtherCleanTime);
            bytes.putInt(ParcelFlags);
            bytes.putInt(SalePrice);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put((byte)_desc.length);
            bytes.put(_desc);
            bytes.put((byte)_musicurl.length);
            bytes.put(_musicurl);
            bytes.put((byte)_mediaurl.length);
            bytes.put(_mediaurl);
            MediaID.GetBytes(bytes);
            bytes.put(MediaAutoScale);
            GroupID.GetBytes(bytes);
            bytes.putInt(PassPrice);
            bytes.putFloat(PassHours);
            bytes.put(Category);
            AuthBuyerID.GetBytes(bytes);
            SnapshotID.GetBytes(bytes);
            UserLocation.GetBytes(bytes);
            UserLookAt.GetBytes(bytes);
            bytes.put(LandingType);
            bytes.put((byte)((RegionPushOverride) ? 1 : 0));
            bytes.put((byte)((RegionDenyAnonymous) ? 1 : 0));
            bytes.put((byte)((RegionDenyIdentified) ? 1 : 0));
            bytes.put((byte)((RegionDenyTransacted) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "RequestResult: " + Integer.toString(RequestResult) + "\n";
                output += "SequenceID: " + Integer.toString(SequenceID) + "\n";
                output += "SnapSelection: " + Boolean.toString(SnapSelection) + "\n";
                output += "SelfCount: " + Integer.toString(SelfCount) + "\n";
                output += "OtherCount: " + Integer.toString(OtherCount) + "\n";
                output += "PublicCount: " + Integer.toString(PublicCount) + "\n";
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "IsGroupOwned: " + Boolean.toString(IsGroupOwned) + "\n";
                output += "AuctionID: " + Integer.toString(AuctionID) + "\n";
                output += "ClaimDate: " + Integer.toString(ClaimDate) + "\n";
                output += "ClaimPrice: " + Integer.toString(ClaimPrice) + "\n";
                output += "RentPrice: " + Integer.toString(RentPrice) + "\n";
                output += "AABBMin: " + AABBMin.toString() + "\n";
                output += "AABBMax: " + AABBMax.toString() + "\n";
                output += Helpers.FieldToString(_bitmap, "Bitmap") + "\n";
                output += "Area: " + Integer.toString(Area) + "\n";
                output += "Status: " + Byte.toString(Status) + "\n";
                output += "SimWideMaxPrims: " + Integer.toString(SimWideMaxPrims) + "\n";
                output += "SimWideTotalPrims: " + Integer.toString(SimWideTotalPrims) + "\n";
                output += "MaxPrims: " + Integer.toString(MaxPrims) + "\n";
                output += "TotalPrims: " + Integer.toString(TotalPrims) + "\n";
                output += "OwnerPrims: " + Integer.toString(OwnerPrims) + "\n";
                output += "GroupPrims: " + Integer.toString(GroupPrims) + "\n";
                output += "OtherPrims: " + Integer.toString(OtherPrims) + "\n";
                output += "SelectedPrims: " + Integer.toString(SelectedPrims) + "\n";
                output += "ParcelPrimBonus: " + Float.toString(ParcelPrimBonus) + "\n";
                output += "OtherCleanTime: " + Integer.toString(OtherCleanTime) + "\n";
                output += "ParcelFlags: " + Integer.toString(ParcelFlags) + "\n";
                output += "SalePrice: " + Integer.toString(SalePrice) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += Helpers.FieldToString(_desc, "Desc") + "\n";
                output += Helpers.FieldToString(_musicurl, "MusicURL") + "\n";
                output += Helpers.FieldToString(_mediaurl, "MediaURL") + "\n";
                output += "MediaID: " + MediaID.toString() + "\n";
                output += "MediaAutoScale: " + Byte.toString(MediaAutoScale) + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += "PassPrice: " + Integer.toString(PassPrice) + "\n";
                output += "PassHours: " + Float.toString(PassHours) + "\n";
                output += "Category: " + Byte.toString(Category) + "\n";
                output += "AuthBuyerID: " + AuthBuyerID.toString() + "\n";
                output += "SnapshotID: " + SnapshotID.toString() + "\n";
                output += "UserLocation: " + UserLocation.toString() + "\n";
                output += "UserLookAt: " + UserLookAt.toString() + "\n";
                output += "LandingType: " + Byte.toString(LandingType) + "\n";
                output += "RegionPushOverride: " + Boolean.toString(RegionPushOverride) + "\n";
                output += "RegionDenyAnonymous: " + Boolean.toString(RegionDenyAnonymous) + "\n";
                output += "RegionDenyIdentified: " + Boolean.toString(RegionDenyIdentified) + "\n";
                output += "RegionDenyTransacted: " + Boolean.toString(RegionDenyTransacted) + "\n";
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
    public PacketType getType() { return PacketType.ParcelProperties; }
    public ParcelDataBlock ParcelData;
    public boolean RegionDenyAgeUnverified;

    public ParcelPropertiesPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)23);
        header.setReliable(true);
        ParcelData = new ParcelDataBlock();
    }

    public ParcelPropertiesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        ParcelData = new ParcelDataBlock(bytes);
        RegionDenyAgeUnverified = (bytes.get() != 0) ? (boolean)true : (boolean)false;
     }

    public ParcelPropertiesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ParcelData = new ParcelDataBlock(bytes);
        RegionDenyAgeUnverified = (bytes.get() != 0) ? (boolean)true : (boolean)false;
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ParcelData.getLength();
        length += 1;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ParcelData.ToBytes(bytes);
        bytes.put((byte)((RegionDenyAgeUnverified) ? 1 : 0));
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelProperties ---\n";
        output += ParcelData.toString() + "\n";
        output += "RegionDenyAgeUnverified: " + Boolean.toString(RegionDenyAgeUnverified) + "\n";
        return output;
    }
}
