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

public class AvatarPropertiesUpdatePacket extends Packet
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

    public class PropertiesDataBlock
    {
        public UUID ImageID = null;
        public UUID FLImageID = null;
        private byte[] _abouttext;
        public byte[] getAboutText() {
            return _abouttext;
        }

        public void setAboutText(byte[] value) throws Exception {
            if (value == null) {
                _abouttext = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _abouttext = new byte[value.length];
                System.arraycopy(value, 0, _abouttext, 0, value.length);
            }
        }

        private byte[] _flabouttext;
        public byte[] getFLAboutText() {
            return _flabouttext;
        }

        public void setFLAboutText(byte[] value) throws Exception {
            if (value == null) {
                _flabouttext = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _flabouttext = new byte[value.length];
                System.arraycopy(value, 0, _flabouttext, 0, value.length);
            }
        }

        public boolean AllowPublish = false;
        public boolean MaturePublish = false;
        private byte[] _profileurl;
        public byte[] getProfileURL() {
            return _profileurl;
        }

        public void setProfileURL(byte[] value) throws Exception {
            if (value == null) {
                _profileurl = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _profileurl = new byte[value.length];
                System.arraycopy(value, 0, _profileurl, 0, value.length);
            }
        }


        public int getLength(){
            int length = 34;
            if (getAboutText() != null) { length += 2 + getAboutText().length; }
            if (getFLAboutText() != null) { length += 1 + getFLAboutText().length; }
            if (getProfileURL() != null) { length += 1 + getProfileURL().length; }
            return length;
        }

        public PropertiesDataBlock() { }
        public PropertiesDataBlock(ByteBuffer bytes)
        {
            int length;
            ImageID = new UUID(bytes);
            FLImageID = new UUID(bytes);
            length = bytes.getShort() & 0xFFFF;
            _abouttext = new byte[length];
            bytes.get(_abouttext);
            length = bytes.get() & 0xFF;
            _flabouttext = new byte[length];
            bytes.get(_flabouttext);
            AllowPublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            MaturePublish = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = bytes.get() & 0xFF;
            _profileurl = new byte[length];
            bytes.get(_profileurl);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ImageID.GetBytes(bytes);
            FLImageID.GetBytes(bytes);
            bytes.putShort((short)_abouttext.length);
            bytes.put(_abouttext);
            bytes.put((byte)_flabouttext.length);
            bytes.put(_flabouttext);
            bytes.put((byte)((AllowPublish) ? 1 : 0));
            bytes.put((byte)((MaturePublish) ? 1 : 0));
            bytes.put((byte)_profileurl.length);
            bytes.put(_profileurl);
        }

        @Override
        public String toString()
        {
            String output = "-- PropertiesData --\n";
            try {
                output += "ImageID: " + ImageID.toString() + "\n";
                output += "FLImageID: " + FLImageID.toString() + "\n";
                output += Helpers.FieldToString(_abouttext, "AboutText") + "\n";
                output += Helpers.FieldToString(_flabouttext, "FLAboutText") + "\n";
                output += "AllowPublish: " + Boolean.toString(AllowPublish) + "\n";
                output += "MaturePublish: " + Boolean.toString(MaturePublish) + "\n";
                output += Helpers.FieldToString(_profileurl, "ProfileURL") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public PropertiesDataBlock createPropertiesDataBlock() {
         return new PropertiesDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AvatarPropertiesUpdate; }
    public AgentDataBlock AgentData;
    public PropertiesDataBlock PropertiesData;

    public AvatarPropertiesUpdatePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)174);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        PropertiesData = new PropertiesDataBlock();
    }

    public AvatarPropertiesUpdatePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        PropertiesData = new PropertiesDataBlock(bytes);
     }

    public AvatarPropertiesUpdatePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        PropertiesData = new PropertiesDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += PropertiesData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        PropertiesData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AvatarPropertiesUpdate ---\n";
        output += AgentData.toString() + "\n";
        output += PropertiesData.toString() + "\n";
        return output;
    }
}
