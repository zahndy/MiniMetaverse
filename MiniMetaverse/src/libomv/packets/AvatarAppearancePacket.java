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

public class AvatarAppearancePacket extends Packet
{
    public class SenderBlock
    {
        public UUID ID = null;
        public boolean IsTrial = false;

        public int getLength(){
            return 17;
        }

        public SenderBlock() { }
        public SenderBlock(ByteBuffer bytes)
        {
            ID = new UUID(bytes);
            IsTrial = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ID.GetBytes(bytes);
            bytes.put((byte)((IsTrial) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- Sender --\n";
            try {
                output += "ID: " + ID.toString() + "\n";
                output += "IsTrial: " + Boolean.toString(IsTrial) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SenderBlock createSenderBlock() {
         return new SenderBlock();
    }

    public class ObjectDataBlock
    {
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


        public int getLength(){
            int length = 0;
            if (getTextureEntry() != null) { length += 2 + getTextureEntry().length; }
            return length;
        }

        public ObjectDataBlock() { }
        public ObjectDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.getShort() & 0xFFFF;
            _textureentry = new byte[length];
            bytes.get(_textureentry);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putShort((short)_textureentry.length);
            bytes.put(_textureentry);
        }

        @Override
        public String toString()
        {
            String output = "-- ObjectData --\n";
            try {
                output += Helpers.FieldToString(_textureentry, "TextureEntry") + "\n";
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
    public PacketType getType() { return PacketType.AvatarAppearance; }
    public SenderBlock Sender;
    public ObjectDataBlock ObjectData;
    public byte[] ParamValue;

    public AvatarAppearancePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)158);
        header.setReliable(true);
        Sender = new SenderBlock();
        ObjectData = new ObjectDataBlock();
        ParamValue = new byte[0];
    }

    public AvatarAppearancePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Sender = new SenderBlock(bytes);
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParamValue = new byte[count];
        bytes.get(ParamValue);
     }

    public AvatarAppearancePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Sender = new SenderBlock(bytes);
        ObjectData = new ObjectDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParamValue = new byte[count];
        bytes.get(ParamValue);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Sender.getLength();
        length += ObjectData.getLength();
        length++;
        length += ParamValue.length * 1;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Sender.ToBytes(bytes);
        ObjectData.ToBytes(bytes);
        bytes.put((byte)ParamValue.length);
        bytes.put(ParamValue);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AvatarAppearance ---\n";
        output += Sender.toString() + "\n";
        output += ObjectData.toString() + "\n";
        for (int j = 0; j < ParamValue.length; j++)
        {
            output += "ParamValue[" + j + "]: " + Byte.toString(ParamValue[j]) + "\n";
        }
        return output;
    }
}
