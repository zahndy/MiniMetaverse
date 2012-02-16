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

public class AvatarAnimationPacket extends Packet
{
    public class AnimationListBlock
    {
        public UUID AnimID = null;
        public int AnimSequenceID = 0;

        public int getLength(){
            return 20;
        }

        public AnimationListBlock() { }
        public AnimationListBlock(ByteBuffer bytes)
        {
            AnimID = new UUID(bytes);
            AnimSequenceID = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AnimID.GetBytes(bytes);
            bytes.putInt(AnimSequenceID);
        }

        @Override
        public String toString()
        {
            String output = "-- AnimationList --\n";
            try {
                output += "AnimID: " + AnimID.toString() + "\n";
                output += "AnimSequenceID: " + Integer.toString(AnimSequenceID) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AnimationListBlock createAnimationListBlock() {
         return new AnimationListBlock();
    }

    public class PhysicalAvatarEventListBlock
    {
        private byte[] _typedata;
        public byte[] getTypeData() {
            return _typedata;
        }

        public void setTypeData(byte[] value) throws Exception {
            if (value == null) {
                _typedata = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _typedata = new byte[value.length];
                System.arraycopy(value, 0, _typedata, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getTypeData() != null) { length += 1 + getTypeData().length; }
            return length;
        }

        public PhysicalAvatarEventListBlock() { }
        public PhysicalAvatarEventListBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _typedata = new byte[length];
            bytes.get(_typedata);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_typedata.length);
            bytes.put(_typedata);
        }

        @Override
        public String toString()
        {
            String output = "-- PhysicalAvatarEventList --\n";
            try {
                output += Helpers.FieldToString(_typedata, "TypeData") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public PhysicalAvatarEventListBlock createPhysicalAvatarEventListBlock() {
         return new PhysicalAvatarEventListBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AvatarAnimation; }
    public UUID ID;
    public AnimationListBlock[] AnimationList;
    public UUID[] ObjectID;
    public PhysicalAvatarEventListBlock[] PhysicalAvatarEventList;

    public AvatarAnimationPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)20);
        header.setReliable(true);
        ID = new UUID();
        AnimationList = new AnimationListBlock[0];
        ObjectID = new UUID[0];
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[0];
    }

    public AvatarAnimationPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        ID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        AnimationList = new AnimationListBlock[count];
        for (int j = 0; j < count; j++)
        {
            AnimationList[j] = new AnimationListBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        ObjectID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            ObjectID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[count];
        for (int j = 0; j < count; j++)
        {
            PhysicalAvatarEventList[j] = new PhysicalAvatarEventListBlock(bytes);
        }
     }

    public AvatarAnimationPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        AnimationList = new AnimationListBlock[count];
        for (int j = 0; j < count; j++)
        {
            AnimationList[j] = new AnimationListBlock(bytes);
        }
        count = bytes.get() & 0xFF;
        ObjectID = new UUID[count];
        for (int j = 0; j < count; j++)
        {
            ObjectID[j] = new UUID(bytes);
        }
        count = bytes.get() & 0xFF;
        PhysicalAvatarEventList = new PhysicalAvatarEventListBlock[count];
        for (int j = 0; j < count; j++)
        {
            PhysicalAvatarEventList[j] = new PhysicalAvatarEventListBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        for (int j = 0; j < AnimationList.length; j++) { length += AnimationList[j].getLength(); }
        length++;
        length += ObjectID.length * 16;
        length++;
        for (int j = 0; j < PhysicalAvatarEventList.length; j++) { length += PhysicalAvatarEventList[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ID.GetBytes(bytes);
        bytes.put((byte)AnimationList.length);
        for (int j = 0; j < AnimationList.length; j++) { AnimationList[j].ToBytes(bytes); }
        bytes.put((byte)ObjectID.length);
        for (int j = 0; j < ObjectID.length; j++)
        {
            ObjectID[j].GetBytes(bytes);
        }
        bytes.put((byte)PhysicalAvatarEventList.length);
        for (int j = 0; j < PhysicalAvatarEventList.length; j++) { PhysicalAvatarEventList[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AvatarAnimation ---\n";
        output += "ID: " + ID.toString() + "\n";
        for (int j = 0; j < AnimationList.length; j++)
        {
            output += AnimationList[j].toString() + "\n";
        }
        for (int j = 0; j < ObjectID.length; j++)
        {
            output += "ObjectID[" + j + "]: " + ObjectID[j].toString() + "\n";
        }
        for (int j = 0; j < PhysicalAvatarEventList.length; j++)
        {
            output += PhysicalAvatarEventList[j].toString() + "\n";
        }
        return output;
    }
}
