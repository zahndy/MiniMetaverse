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

public class SetSimStatusInDatabasePacket extends Packet
{
    public class DataBlock
    {
        public UUID RegionID = null;
        private byte[] _hostname;
        public byte[] getHostName() {
            return _hostname;
        }

        public void setHostName(byte[] value) throws Exception {
            if (value == null) {
                _hostname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _hostname = new byte[value.length];
                System.arraycopy(value, 0, _hostname, 0, value.length);
            }
        }

        public int X = 0;
        public int Y = 0;
        public int PID = 0;
        public int AgentCount = 0;
        public int TimeToLive = 0;
        private byte[] _status;
        public byte[] getStatus() {
            return _status;
        }

        public void setStatus(byte[] value) throws Exception {
            if (value == null) {
                _status = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _status = new byte[value.length];
                System.arraycopy(value, 0, _status, 0, value.length);
            }
        }


        public int getLength(){
            int length = 36;
            if (getHostName() != null) { length += 1 + getHostName().length; }
            if (getStatus() != null) { length += 1 + getStatus().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            RegionID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _hostname = new byte[length];
            bytes.get(_hostname);
            X = bytes.getInt();
            Y = bytes.getInt();
            PID = bytes.getInt();
            AgentCount = bytes.getInt();
            TimeToLive = bytes.getInt();
            length = bytes.get() & 0xFF;
            _status = new byte[length];
            bytes.get(_status);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RegionID.GetBytes(bytes);
            bytes.put((byte)_hostname.length);
            bytes.put(_hostname);
            bytes.putInt(X);
            bytes.putInt(Y);
            bytes.putInt(PID);
            bytes.putInt(AgentCount);
            bytes.putInt(TimeToLive);
            bytes.put((byte)_status.length);
            bytes.put(_status);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "RegionID: " + RegionID.toString() + "\n";
                output += Helpers.FieldToString(_hostname, "HostName") + "\n";
                output += "X: " + Integer.toString(X) + "\n";
                output += "Y: " + Integer.toString(Y) + "\n";
                output += "PID: " + Integer.toString(PID) + "\n";
                output += "AgentCount: " + Integer.toString(AgentCount) + "\n";
                output += "TimeToLive: " + Integer.toString(TimeToLive) + "\n";
                output += Helpers.FieldToString(_status, "Status") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SetSimStatusInDatabase; }
    public DataBlock Data;

    public SetSimStatusInDatabasePacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)22);
        header.setReliable(true);
        Data = new DataBlock();
    }

    public SetSimStatusInDatabasePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
     }

    public SetSimStatusInDatabasePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SetSimStatusInDatabase ---\n";
        output += Data.toString() + "\n";
        return output;
    }
}
