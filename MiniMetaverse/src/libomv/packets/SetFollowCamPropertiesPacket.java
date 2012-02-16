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

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;
import libomv.types.UUID;

public class SetFollowCamPropertiesPacket extends Packet
{
    public class CameraPropertyBlock
    {
        public int Type = 0;
        public float Value = 0;

        public int getLength(){
            return 8;
        }

        public CameraPropertyBlock() { }
        public CameraPropertyBlock(ByteBuffer bytes)
        {
            Type = bytes.getInt();
            Value = bytes.getFloat();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Type);
            bytes.putFloat(Value);
        }

        @Override
        public String toString()
        {
            String output = "-- CameraProperty --\n";
            try {
                output += "Type: " + Integer.toString(Type) + "\n";
                output += "Value: " + Float.toString(Value) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public CameraPropertyBlock createCameraPropertyBlock() {
         return new CameraPropertyBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SetFollowCamProperties; }
    public UUID ObjectID;
    public CameraPropertyBlock[] CameraProperty;

    public SetFollowCamPropertiesPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)159);
        header.setReliable(true);
        ObjectID = new UUID();
        CameraProperty = new CameraPropertyBlock[0];
    }

    public SetFollowCamPropertiesPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ObjectID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        CameraProperty = new CameraPropertyBlock[count];
        for (int j = 0; j < count; j++)
        {
            CameraProperty[j] = new CameraPropertyBlock(bytes);
        }
     }

    public SetFollowCamPropertiesPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ObjectID = new UUID(bytes);
        int count = bytes.get() & 0xFF;
        CameraProperty = new CameraPropertyBlock[count];
        for (int j = 0; j < count; j++)
        {
            CameraProperty[j] = new CameraPropertyBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length++;
        for (int j = 0; j < CameraProperty.length; j++) { length += CameraProperty[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ObjectID.GetBytes(bytes);
        bytes.put((byte)CameraProperty.length);
        for (int j = 0; j < CameraProperty.length; j++) { CameraProperty[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SetFollowCamProperties ---\n";
        output += "ObjectID: " + ObjectID.toString() + "\n";
        for (int j = 0; j < CameraProperty.length; j++)
        {
            output += CameraProperty[j].toString() + "\n";
        }
        return output;
    }
}
