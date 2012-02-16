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

public class ParcelPropertiesUpdatePacket extends Packet
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

    public class ParcelDataBlock
    {
        public int LocalID = 0;
        public int Flags = 0;
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

        public int getLength(){
            int length = 115;
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
            LocalID = bytes.getInt();
            Flags = bytes.getInt();
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
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(LocalID);
            bytes.putInt(Flags);
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
        }

        @Override
        public String toString()
        {
            String output = "-- ParcelData --\n";
            try {
                output += "LocalID: " + Integer.toString(LocalID) + "\n";
                output += "Flags: " + Integer.toString(Flags) + "\n";
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
    public PacketType getType() { return PacketType.ParcelPropertiesUpdate; }
    public AgentDataBlock AgentData;
    public ParcelDataBlock ParcelData;

    public ParcelPropertiesUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)198);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        ParcelData = new ParcelDataBlock();
    }

    public ParcelPropertiesUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
     }

    public ParcelPropertiesUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        ParcelData = new ParcelDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += ParcelData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        ParcelData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ParcelPropertiesUpdate ---\n";
        output += AgentData.toString() + "\n";
        output += ParcelData.toString() + "\n";
        return output;
    }
}
