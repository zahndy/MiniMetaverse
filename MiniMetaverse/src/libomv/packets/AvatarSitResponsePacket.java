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
import libomv.types.Vector3;
import libomv.types.Quaternion;

public class AvatarSitResponsePacket extends Packet
{
    public class SitTransformBlock
    {
        public boolean AutoPilot = false;
        public Vector3 SitPosition = null;
        public Quaternion SitRotation = null;
        public Vector3 CameraEyeOffset = null;
        public Vector3 CameraAtOffset = null;
        public boolean ForceMouselook = false;

        public int getLength(){
            return 50;
        }

        public SitTransformBlock() { }
        public SitTransformBlock(ByteBuffer bytes)
        {
            AutoPilot = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            SitPosition = new Vector3(bytes);
            SitRotation = new Quaternion(bytes, true);
            CameraEyeOffset = new Vector3(bytes);
            CameraAtOffset = new Vector3(bytes);
            ForceMouselook = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((AutoPilot) ? 1 : 0));
            SitPosition.GetBytes(bytes);
            SitRotation.GetBytes(bytes);
            CameraEyeOffset.GetBytes(bytes);
            CameraAtOffset.GetBytes(bytes);
            bytes.put((byte)((ForceMouselook) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- SitTransform --\n";
            try {
                output += "AutoPilot: " + Boolean.toString(AutoPilot) + "\n";
                output += "SitPosition: " + SitPosition.toString() + "\n";
                output += "SitRotation: " + SitRotation.toString() + "\n";
                output += "CameraEyeOffset: " + CameraEyeOffset.toString() + "\n";
                output += "CameraAtOffset: " + CameraAtOffset.toString() + "\n";
                output += "ForceMouselook: " + Boolean.toString(ForceMouselook) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SitTransformBlock createSitTransformBlock() {
         return new SitTransformBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AvatarSitResponse; }
    public UUID ID;
    public SitTransformBlock SitTransform;

    public AvatarSitResponsePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)21);
        header.setReliable(true);
        ID = new UUID();
        SitTransform = new SitTransformBlock();
    }

    public AvatarSitResponsePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        ID = new UUID(bytes);
        SitTransform = new SitTransformBlock(bytes);
     }

    public AvatarSitResponsePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ID = new UUID(bytes);
        SitTransform = new SitTransformBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += SitTransform.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ID.GetBytes(bytes);
        SitTransform.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AvatarSitResponse ---\n";
        output += "ID: " + ID.toString() + "\n";
        output += SitTransform.toString() + "\n";
        return output;
    }
}
