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

public class DirPlacesQueryBackendPacket extends Packet
{
    public class QueryDataBlock
    {
        public UUID QueryID = null;
        private byte[] _querytext;
        public byte[] getQueryText() {
            return _querytext;
        }

        public void setQueryText(byte[] value) throws Exception {
            if (value == null) {
                _querytext = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _querytext = new byte[value.length];
                System.arraycopy(value, 0, _querytext, 0, value.length);
            }
        }

        public int QueryFlags = 0;
        public byte Category = 0;
        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }

        public int EstateID = 0;
        public boolean Godlike = false;
        public int QueryStart = 0;

        public int getLength(){
            int length = 30;
            if (getQueryText() != null) { length += 1 + getQueryText().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public QueryDataBlock() { }
        public QueryDataBlock(ByteBuffer bytes)
        {
            int length;
            QueryID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _querytext = new byte[length];
            bytes.get(_querytext);
            QueryFlags = bytes.getInt();
            Category = bytes.get();
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            EstateID = bytes.getInt();
            Godlike = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            QueryStart = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            QueryID.GetBytes(bytes);
            bytes.put((byte)_querytext.length);
            bytes.put(_querytext);
            bytes.putInt(QueryFlags);
            bytes.put(Category);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            bytes.putInt(EstateID);
            bytes.put((byte)((Godlike) ? 1 : 0));
            bytes.putInt(QueryStart);
        }

        @Override
        public String toString()
        {
            String output = "-- QueryData --\n";
            try {
                output += "QueryID: " + QueryID.toString() + "\n";
                output += Helpers.FieldToString(_querytext, "QueryText") + "\n";
                output += "QueryFlags: " + Integer.toString(QueryFlags) + "\n";
                output += "Category: " + Byte.toString(Category) + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "Godlike: " + Boolean.toString(Godlike) + "\n";
                output += "QueryStart: " + Integer.toString(QueryStart) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public QueryDataBlock createQueryDataBlock() {
         return new QueryDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.DirPlacesQueryBackend; }
    public UUID AgentID;
    public QueryDataBlock QueryData;

    public DirPlacesQueryBackendPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)34);
        header.setReliable(true);
        AgentID = new UUID();
        QueryData = new QueryDataBlock();
    }

    public DirPlacesQueryBackendPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        QueryData = new QueryDataBlock(bytes);
     }

    public DirPlacesQueryBackendPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        QueryData = new QueryDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += QueryData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        QueryData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- DirPlacesQueryBackend ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += QueryData.toString() + "\n";
        return output;
    }
}
