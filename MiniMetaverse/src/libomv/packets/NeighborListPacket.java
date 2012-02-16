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

public class NeighborListPacket extends Packet
{
    public class NeighborBlockBlock
    {
        public int IP = 0;
        public short Port = 0;
        public int PublicIP = 0;
        public short PublicPort = 0;
        public UUID RegionID = null;
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

        public byte SimAccess = 0;

        public int getLength(){
            int length = 29;
            if (getName() != null) { length += 1 + getName().length; }
            return length;
        }

        public NeighborBlockBlock() { }
        public NeighborBlockBlock(ByteBuffer bytes)
        {
            int length;
            IP = bytes.getInt();
            Port = (short)((bytes.get() << 8) + bytes.get());
            PublicIP = bytes.getInt();
            PublicPort = (short)((bytes.get() << 8) + bytes.get());
            RegionID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
            SimAccess = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(IP);
            bytes.put((byte)((Port >> 8) % 256));
            bytes.put((byte)(Port % 256));
            bytes.putInt(PublicIP);
            bytes.put((byte)((PublicPort >> 8) % 256));
            bytes.put((byte)(PublicPort % 256));
            RegionID.GetBytes(bytes);
            bytes.put((byte)_name.length);
            bytes.put(_name);
            bytes.put(SimAccess);
        }

        @Override
        public String toString()
        {
            String output = "-- NeighborBlock --\n";
            try {
                output += "IP: " + Integer.toString(IP) + "\n";
                output += "Port: " + Short.toString(Port) + "\n";
                output += "PublicIP: " + Integer.toString(PublicIP) + "\n";
                output += "PublicPort: " + Short.toString(PublicPort) + "\n";
                output += "RegionID: " + RegionID.toString() + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NeighborBlockBlock createNeighborBlockBlock() {
         return new NeighborBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.NeighborList; }
    public NeighborBlockBlock[] NeighborBlock;

    public NeighborListPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)3);
        header.setReliable(true);
        NeighborBlock = new NeighborBlockBlock[4];
    }

    public NeighborListPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
     }

    public NeighborListPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        for (int j = 0; j < 4; j++) { length += NeighborBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        for (int j = 0; j < 4; j++) { NeighborBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- NeighborList ---\n";
        for (int j = 0; j < 4; j++)
        {
            output += NeighborBlock[j].toString() + "\n";
        }
        return output;
    }
}
