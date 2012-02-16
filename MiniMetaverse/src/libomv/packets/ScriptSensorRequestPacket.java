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
import libomv.types.Quaternion;
import libomv.types.OverflowException;

public class ScriptSensorRequestPacket extends Packet
{
    public class RequesterBlock
    {
        public UUID SourceID = null;
        public UUID RequestID = null;
        public UUID SearchID = null;
        public Vector3 SearchPos = null;
        public Quaternion SearchDir = null;
        private byte[] _searchname;
        public byte[] getSearchName() {
            return _searchname;
        }

        public void setSearchName(byte[] value) throws Exception {
            if (value == null) {
                _searchname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _searchname = new byte[value.length];
                System.arraycopy(value, 0, _searchname, 0, value.length);
            }
        }

        public int Type = 0;
        public float Range = 0;
        public float Arc = 0;
        public long RegionHandle = 0;
        public byte SearchRegions = 0;

        public int getLength(){
            int length = 93;
            if (getSearchName() != null) { length += 1 + getSearchName().length; }
            return length;
        }

        public RequesterBlock() { }
        public RequesterBlock(ByteBuffer bytes)
        {
            int length;
            SourceID = new UUID(bytes);
            RequestID = new UUID(bytes);
            SearchID = new UUID(bytes);
            SearchPos = new Vector3(bytes);
            SearchDir = new Quaternion(bytes, true);
            length = bytes.get() & 0xFF;
            _searchname = new byte[length];
            bytes.get(_searchname);
            Type = bytes.getInt();
            Range = bytes.getFloat();
            Arc = bytes.getFloat();
            RegionHandle = bytes.getLong();
            SearchRegions = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            SourceID.GetBytes(bytes);
            RequestID.GetBytes(bytes);
            SearchID.GetBytes(bytes);
            SearchPos.GetBytes(bytes);
            SearchDir.GetBytes(bytes);
            bytes.put((byte)_searchname.length);
            bytes.put(_searchname);
            bytes.putInt(Type);
            bytes.putFloat(Range);
            bytes.putFloat(Arc);
            bytes.putLong(RegionHandle);
            bytes.put(SearchRegions);
        }

        @Override
        public String toString()
        {
            String output = "-- Requester --\n";
            try {
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "SearchID: " + SearchID.toString() + "\n";
                output += "SearchPos: " + SearchPos.toString() + "\n";
                output += "SearchDir: " + SearchDir.toString() + "\n";
                output += Helpers.FieldToString(_searchname, "SearchName") + "\n";
                output += "Type: " + Integer.toString(Type) + "\n";
                output += "Range: " + Float.toString(Range) + "\n";
                output += "Arc: " + Float.toString(Arc) + "\n";
                output += "RegionHandle: " + Long.toString(RegionHandle) + "\n";
                output += "SearchRegions: " + Byte.toString(SearchRegions) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RequesterBlock createRequesterBlock() {
         return new RequesterBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ScriptSensorRequest; }
    public RequesterBlock Requester;

    public ScriptSensorRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)247);
        header.setReliable(true);
        Requester = new RequesterBlock();
    }

    public ScriptSensorRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Requester = new RequesterBlock(bytes);
     }

    public ScriptSensorRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Requester = new RequesterBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Requester.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Requester.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ScriptSensorRequest ---\n";
        output += Requester.toString() + "\n";
        return output;
    }
}
