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

public class TeleportFinishPacket extends Packet
{
    public class InfoBlock
    {
        public UUID AgentID = null;
        public int LocationID = 0;
        public int SimIP = 0;
        public short SimPort = 0;
        public long RegionHandle = 0;
        private byte[] _seedcapability;
        public byte[] getSeedCapability() {
            return _seedcapability;
        }

        public void setSeedCapability(byte[] value) throws Exception {
            if (value == null) {
                _seedcapability = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _seedcapability = new byte[value.length];
                System.arraycopy(value, 0, _seedcapability, 0, value.length);
            }
        }

        public byte SimAccess = 0;
        public int TeleportFlags = 0;

        public int getLength(){
            int length = 39;
            if (getSeedCapability() != null) { length += 2 + getSeedCapability().length; }
            return length;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            LocationID = bytes.getInt();
            SimIP = bytes.getInt();
            SimPort = (short)((bytes.get() << 8) + bytes.get());
            RegionHandle = bytes.getLong();
            length = bytes.getShort() & 0xFFFF;
            _seedcapability = new byte[length];
            bytes.get(_seedcapability);
            SimAccess = bytes.get();
            TeleportFlags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.putInt(LocationID);
            bytes.putInt(SimIP);
            bytes.put((byte)((SimPort >> 8) % 256));
            bytes.put((byte)(SimPort % 256));
            bytes.putLong(RegionHandle);
            bytes.putShort((short)_seedcapability.length);
            bytes.put(_seedcapability);
            bytes.put(SimAccess);
            bytes.putInt(TeleportFlags);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "LocationID: " + Integer.toString(LocationID) + "\n";
                output += "SimIP: " + Integer.toString(SimIP) + "\n";
                output += "SimPort: " + Short.toString(SimPort) + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += Helpers.FieldToString(_seedcapability, "SeedCapability") + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output += "TeleportFlags: " + Integer.toString(TeleportFlags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlock createInfoBlock() {
         return new InfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TeleportFinish; }
    public InfoBlock Info;

    public TeleportFinishPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)69);
        header.setReliable(true);
        Info = new InfoBlock();
    }

    public TeleportFinishPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Info = new InfoBlock(bytes);
     }

    public TeleportFinishPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Info = new InfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Info.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Info.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TeleportFinish ---\n";
        output += Info.toString() + "\n";
        return output;
    }
}
