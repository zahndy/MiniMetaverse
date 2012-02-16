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

public class ObjectUpdatePacket extends Packet
{
    public class RegionDataBlock
    {
        public long RegionHandle = 0;
        public short TimeDilation = 0;

        public int getLength(){
            return 10;
        }

        public RegionDataBlock() { }
        public RegionDataBlock(ByteBuffer bytes)
        {
            RegionHandle = bytes.getLong();
            TimeDilation = bytes.getShort();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putLong(RegionHandle);
            bytes.putShort(TimeDilation);
        }

        @Override
        public String toString()
        {
            String output = "-- RegionData --\n";
            try {
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "TimeDilation: " + Short.toString(TimeDilation) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RegionDataBlock createRegionDataBlock() {
         return new RegionDataBlock();
    }

    public class ObjectDataBlock
    {
        public int ID = 0;
        public byte State = 0;
        public UUID FullID = null;
        public int CRC = 0;
        public byte PCode = 0;
        public byte Material = 0;
        public byte ClickAction = 0;
        public Vector3 Scale = null;
        private byte[] _objectdata;
        public byte[] getObjectData() {
            return _objectdata;
        }

        public void setObjectData(byte[] value) throws Exception {
            if (value == null) {
                _objectdata = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _objectdata = new byte[value.length];
                System.arraycopy(value, 0, _objectdata, 0, value.length);
            }
        }

        public int ParentID = 0;
        public int UpdateFlags = 0;
        public byte PathCurve = 0;
        public byte ProfileCurve = 0;
        public short PathBegin = 0;
        public short PathEnd = 0;
        public byte PathScaleX = 0;
        public byte PathScaleY = 0;
        public byte PathShearX = 0;
        public byte PathShearY = 0;
        public byte PathTwist = 0;
        public byte PathTwistBegin = 0;
        public byte PathRadiusOffset = 0;
        public byte PathTaperX = 0;
        public byte PathTaperY = 0;
        public byte PathRevolutions = 0;
        public byte PathSkew = 0;
        public short ProfileBegin = 0;
        public short ProfileEnd = 0;
        public short ProfileHollow = 0;
        private byte[] _textureentry;
        public byte[] getTextureEntry() {
            return _textureentry;
        }

        public void setTextureEntry(byte[] value) throws Exception {
            if (value == null) {
                _textureentry = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _textureentry = new byte[value.length];
                System.arraycopy(value, 0, _textureentry, 0, value.length);
            }
        }

        private byte[] _textureanim;
        public byte[] getTextureAnim() {
            return _textureanim;
        }

        public void setTextureAnim(byte[] value) throws Exception {
            if (value == null) {
                _textureanim = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _textureanim = new byte[value.length];
                System.arraycopy(value, 0, _textureanim, 0, value.length);
            }
        }

        private byte[] _namevalue;
        public byte[] getNameValue() {
            return _namevalue;
        }

        public void setNameValue(byte[] value) throws Exception {
            if (value == null) {
                _namevalue = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _namevalue = new byte[value.length];
                System.arraycopy(value, 0, _namevalue, 0, value.length);
            }
        }

        private byte[] _data;
        public byte[] getData() {
            return _data;
        }

        public void setData(byte[] value) throws Exception {
            if (value == null) {
                _data = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _data = new byte[value.length];
                System.arraycopy(value, 0, _data, 0, value.length);
            }
        }

        private byte[] _text;
        public byte[] getText() {
            return _text;
        }

        public void setText(byte[] value) throws Exception {
            if (value == null) {
                _text = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _text = new byte[value.length];
                System.arraycopy(value, 0, _text, 0, value.length);
            }
        }

        public byte[] TextColor = null;
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

        private byte[] _psblock;
        public byte[] getPSBlock() {
            return _psblock;
        }

        public void setPSBlock(byte[] value) throws Exception {
            if (value == null) {
                _psblock = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _psblock = new byte[value.length];
                System.arraycopy(value, 0, _psblock, 0, value.length);
            }
        }

        private byte[] _extraparams;
        public byte[] getExtraParams() {
            return _extraparams;
        }

        public void setExtraParams(byte[] value) throws Exception {
            if (value == null) {
                _extraparams = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _extraparams = new byte[value.length];
                System.arraycopy(value, 0, _extraparams, 0, value.length);
            }
        }

        public UUID Sound = null;
        public UUID OwnerID = null;
        public float Gain = 0;
        public byte Flags = 0;
        public float Radius = 0;
        public byte JointType = 0;
        public Vector3 JointPivot = null;
        public Vector3 JointAxisOrAnchor = null;

        public int getLength(){
            int length = 141;
            if (getObjectData() != null) { length += 1 + getObjectData().length; }
            if (getTextureEntry() != null) { length += 2 + getTextureEntry().length; }
            if (getTextureAnim() != null) { length += 1 + getTextureAnim().length; }
            if (getNameValue() != null) { length += 2 + getNameValue().length; }
            if (getData() != null) { length += 2 + getData().length; }
            if (getText() != null) { length += 1 + getText().length; }
            if (getMediaURL() != null) { length += 1 + getMediaURL().length; }
            if (getPSBlock() != null) { length += 1 + getPSBlock().length; }
            if (getExtraParams() != null) { length += 1 + getExtraParams().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            ID = bytes.getInt();
            State = bytes.get();
            FullID = new UUID(bytes);
            CRC = bytes.getInt();
            PCode = bytes.get();
            Material = bytes.get();
            ClickAction = bytes.get();
            Scale = new Vector3(bytes);
            length = bytes.get() & 0xFF;
            _objectdata = new byte[length];
            bytes.get(_objectdata);
            ParentID = bytes.getInt();
            UpdateFlags = bytes.getInt();
            PathCurve = bytes.get();
            ProfileCurve = bytes.get();
            PathBegin = bytes.getShort();
            PathEnd = bytes.getShort();
            PathScaleX = bytes.get();
            PathScaleY = bytes.get();
            PathShearX = bytes.get();
            PathShearY = bytes.get();
            PathTwist = bytes.get();
            PathTwistBegin = bytes.get();
            PathRadiusOffset = bytes.get();
            PathTaperX = bytes.get();
            PathTaperY = bytes.get();
            PathRevolutions = bytes.get();
            PathSkew = bytes.get();
            ProfileBegin = bytes.getShort();
            ProfileEnd = bytes.getShort();
            ProfileHollow = bytes.getShort();
            length = bytes.getShort() & 0xFFFF;
            _textureentry = new byte[length];
            bytes.get(_textureentry);
            length = bytes.get() & 0xFF;
            _textureanim = new byte[length];
            bytes.get(_textureanim);
            length = bytes.getShort() & 0xFFFF;
            _namevalue = new byte[length];
            bytes.get(_namevalue);
            length = bytes.getShort() & 0xFFFF;
            _data = new byte[length];
            bytes.get(_data);
            length = bytes.get() & 0xFF;
            _text = new byte[length];
            bytes.get(_text);
            TextColor = new byte[4];
            bytes.get(TextColor);
            length = bytes.get() & 0xFF;
            _mediaurl = new byte[length];
            bytes.get(_mediaurl);
            length = bytes.get() & 0xFF;
            _psblock = new byte[length];
            bytes.get(_psblock);
            length = bytes.get() & 0xFF;
            _extraparams = new byte[length];
            bytes.get(_extraparams);
            Sound = new UUID(bytes);
            OwnerID = new UUID(bytes);
            Gain = bytes.getFloat();
            Flags = bytes.get();
            Radius = bytes.getFloat();
            JointType = bytes.get();
            JointPivot = new Vector3(bytes);
            JointAxisOrAnchor = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(ID);
            bytes.put(State);
            FullID.GetBytes(bytes);
            bytes.putInt(CRC);
            bytes.put(PCode);
            bytes.put(Material);
            bytes.put(ClickAction);
            Scale.GetBytes(bytes);
            bytes.put((byte)_objectdata.length);
            bytes.put(_objectdata);
            bytes.putInt(ParentID);
            bytes.putInt(UpdateFlags);
            bytes.put(PathCurve);
            bytes.put(ProfileCurve);
            bytes.putShort(PathBegin);
            bytes.putShort(PathEnd);
            bytes.put(PathScaleX);
            bytes.put(PathScaleY);
            bytes.put(PathShearX);
            bytes.put(PathShearY);
            bytes.put(PathTwist);
            bytes.put(PathTwistBegin);
            bytes.put(PathRadiusOffset);
            bytes.put(PathTaperX);
            bytes.put(PathTaperY);
            bytes.put(PathRevolutions);
            bytes.put(PathSkew);
            bytes.putShort(ProfileBegin);
            bytes.putShort(ProfileEnd);
            bytes.putShort(ProfileHollow);
            bytes.putShort((short)_textureentry.length);
            bytes.put(_textureentry);
            bytes.put((byte)_textureanim.length);
            bytes.put(_textureanim);
            bytes.putShort((short)_namevalue.length);
            bytes.put(_namevalue);
            bytes.putShort((short)_data.length);
            bytes.put(_data);
            bytes.put((byte)_text.length);
            bytes.put(_text);
            bytes.put(TextColor);
            bytes.put((byte)_mediaurl.length);
            bytes.put(_mediaurl);
            bytes.put((byte)_psblock.length);
            bytes.put(_psblock);
            bytes.put((byte)_extraparams.length);
            bytes.put(_extraparams);
            Sound.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            bytes.putFloat(Gain);
            bytes.put(Flags);
            bytes.putFloat(Radius);
            bytes.put(JointType);
            JointPivot.GetBytes(bytes);
            JointAxisOrAnchor.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += "ID: " + Integer.toString(ID) + "\n";
                output += "State: " + Byte.toString(State) + "\n";
                output += "FullID: " + FullID.toString() + "\n";
                output += "CRC: " + Integer.toString(CRC) + "\n";
                output += "PCode: " + Byte.toString(PCode) + "\n";
                output += "Material: " + Byte.toString(Material) + "\n";
                output += "ClickAction: " + Byte.toString(ClickAction) + "\n";
                output += "Scale: " + Scale.toString() + "\n";
                output += Helpers.FieldToString(_objectdata, "ObjectData") + "\n";
                output += "ParentID: " + Integer.toString(ParentID) + "\n";
                output += "UpdateFlags: " + Integer.toString(UpdateFlags) + "\n";
                output += "PathCurve: " + Byte.toString(PathCurve) + "\n";
                output += "ProfileCurve: " + Byte.toString(ProfileCurve) + "\n";
                output += "PathBegin: " + Short.toString(PathBegin) + "\n";
                output += "PathEnd: " + Short.toString(PathEnd) + "\n";
                output += "PathScaleX: " + Byte.toString(PathScaleX) + "\n";
                output += "PathScaleY: " + Byte.toString(PathScaleY) + "\n";
                output += "PathShearX: " + Byte.toString(PathShearX) + "\n";
                output += "PathShearY: " + Byte.toString(PathShearY) + "\n";
                output += "PathTwist: " + Byte.toString(PathTwist) + "\n";
                output += "PathTwistBegin: " + Byte.toString(PathTwistBegin) + "\n";
                output += "PathRadiusOffset: " + Byte.toString(PathRadiusOffset) + "\n";
                output += "PathTaperX: " + Byte.toString(PathTaperX) + "\n";
                output += "PathTaperY: " + Byte.toString(PathTaperY) + "\n";
                output += "PathRevolutions: " + Byte.toString(PathRevolutions) + "\n";
                output += "PathSkew: " + Byte.toString(PathSkew) + "\n";
                output += "ProfileBegin: " + Short.toString(ProfileBegin) + "\n";
                output += "ProfileEnd: " + Short.toString(ProfileEnd) + "\n";
                output += "ProfileHollow: " + Short.toString(ProfileHollow) + "\n";
                output += Helpers.FieldToString(_textureentry, "TextureEntry") + "\n";
                output += Helpers.FieldToString(_textureanim, "TextureAnim") + "\n";
                output += Helpers.FieldToString(_namevalue, "NameValue") + "\n";
                output += Helpers.FieldToString(_data, "Data") + "\n";
                output += Helpers.FieldToString(_text, "Text") + "\n";
                output += Helpers.FieldToString(TextColor, "TextColor") + "\n";
                output += Helpers.FieldToString(_mediaurl, "MediaURL") + "\n";
                output += Helpers.FieldToString(_psblock, "PSBlock") + "\n";
                output += Helpers.FieldToString(_extraparams, "ExtraParams") + "\n";
                output += "Sound: " + Sound.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "Gain: " + Float.toString(Gain) + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
                output += "Radius: " + Float.toString(Radius) + "\n";
                output += "JointType: " + Byte.toString(JointType) + "\n";
                output += "JointPivot: " + JointPivot.toString() + "\n";
                output += "JointAxisOrAnchor: " + JointAxisOrAnchor.toString() + "\n";
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
    public PacketType getType() { return PacketType.ObjectUpdate; }
    public RegionDataBlock RegionData;
    public ObjectDataBlock[] ObjectData;

    public ObjectUpdatePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)12);
        header.setReliable(true);
        RegionData = new RegionDataBlock();
        ObjectData = new ObjectDataBlock[0];
    }

    public ObjectUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        RegionData = new RegionDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectData = new ObjectDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ObjectData[j] = new ObjectDataBlock(bytes);
        }
     }

    public ObjectUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        RegionData = new RegionDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ObjectData = new ObjectDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ObjectData[j] = new ObjectDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += RegionData.getLength();
        length++;
        for (int j = 0; j < ObjectData.length; j++) { length += ObjectData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        RegionData.ToBytes(bytes);
        bytes.put((byte)ObjectData.length);
        for (int j = 0; j < ObjectData.length; j++) { ObjectData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ObjectUpdate ---\n";
        output += RegionData.toString() + "\n";
        for (int j = 0; j < ObjectData.length; j++)
        {
            output += ObjectData[j].toString() + "\n";
        }
        return output;
    }
}
