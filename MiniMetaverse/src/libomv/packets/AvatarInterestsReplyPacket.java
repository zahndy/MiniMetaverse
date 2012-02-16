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

public class AvatarInterestsReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID AvatarID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            AvatarID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            AvatarID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "AvatarID: " + AvatarID.toString() + "\n";
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
        public int WantToMask = 0;
        private byte[] _wanttotext;
        public byte[] getWantToText() {
            return _wanttotext;
        }

        public void setWantToText(byte[] value) throws Exception {
            if (value == null) {
                _wanttotext = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _wanttotext = new byte[value.length];
                System.arraycopy(value, 0, _wanttotext, 0, value.length);
            }
        }

        public int SkillsMask = 0;
        private byte[] _skillstext;
        public byte[] getSkillsText() {
            return _skillstext;
        }

        public void setSkillsText(byte[] value) throws Exception {
            if (value == null) {
                _skillstext = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _skillstext = new byte[value.length];
                System.arraycopy(value, 0, _skillstext, 0, value.length);
            }
        }

        private byte[] _languagestext;
        public byte[] getLanguagesText() {
            return _languagestext;
        }

        public void setLanguagesText(byte[] value) throws Exception {
            if (value == null) {
                _languagestext = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _languagestext = new byte[value.length];
                System.arraycopy(value, 0, _languagestext, 0, value.length);
            }
        }


        public int getLength(){
            int length = 8;
            if (getWantToText() != null) { length += 1 + getWantToText().length; }
            if (getSkillsText() != null) { length += 1 + getSkillsText().length; }
            if (getLanguagesText() != null) { length += 1 + getLanguagesText().length; }
            return length;
        }

        public PropertiesDataBlock() { }
        public PropertiesDataBlock(ByteBuffer bytes)
        {
            int length;
            WantToMask = bytes.getInt();
            length = bytes.get() & 0xFF;
            _wanttotext = new byte[length];
            bytes.get(_wanttotext);
            SkillsMask = bytes.getInt();
            length = bytes.get() & 0xFF;
            _skillstext = new byte[length];
            bytes.get(_skillstext);
            length = bytes.get() & 0xFF;
            _languagestext = new byte[length];
            bytes.get(_languagestext);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(WantToMask);
            bytes.put((byte)_wanttotext.length);
            bytes.put(_wanttotext);
            bytes.putInt(SkillsMask);
            bytes.put((byte)_skillstext.length);
            bytes.put(_skillstext);
            bytes.put((byte)_languagestext.length);
            bytes.put(_languagestext);
        }

        @Override
        public String toString()
        {
            String output = "-- PropertiesData --\n";
            try {
                output += "WantToMask: " + Integer.toString(WantToMask) + "\n";
                output += Helpers.FieldToString(_wanttotext, "WantToText") + "\n";
                output += "SkillsMask: " + Integer.toString(SkillsMask) + "\n";
                output += Helpers.FieldToString(_skillstext, "SkillsText") + "\n";
                output += Helpers.FieldToString(_languagestext, "LanguagesText") + "\n";
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
    public PacketType getType() { return PacketType.AvatarInterestsReply; }
    public AgentDataBlock AgentData;
    public PropertiesDataBlock PropertiesData;

    public AvatarInterestsReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)172);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        PropertiesData = new PropertiesDataBlock();
    }

    public AvatarInterestsReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        PropertiesData = new PropertiesDataBlock(bytes);
     }

    public AvatarInterestsReplyPacket(PacketHeader head, ByteBuffer bytes)
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
        String output = "--- AvatarInterestsReply ---\n";
        output += AgentData.toString() + "\n";
        output += PropertiesData.toString() + "\n";
        return output;
    }
}
