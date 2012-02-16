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

public class LandStatRequestPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class RequestDataBlock
    {
        public int ReportType = 0;
        public int RequestFlags = 0;
        private byte[] _filter;
        public byte[] getFilter() {
            return _filter;
        }

        public void setFilter(byte[] value) throws Exception {
            if (value == null) {
                _filter = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _filter = new byte[value.length];
                System.arraycopy(value, 0, _filter, 0, value.length);
            }
        }

        public int ParcelLocalID = 0;

        public int getLength(){
            int length = 12;
            if (getFilter() != null) { length += 1 + getFilter().length; }
            return length;
        }

        public RequestDataBlock() { }
        public RequestDataBlock(ByteBuffer bytes)
        {
            int length;
            ReportType = bytes.getInt();
            RequestFlags = bytes.getInt();
            length = bytes.get() & 0xFF;
            _filter = new byte[length];
            bytes.get(_filter);
            ParcelLocalID = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(ReportType);
            bytes.putInt(RequestFlags);
            bytes.put((byte)_filter.length);
            bytes.put(_filter);
            bytes.putInt(ParcelLocalID);
        }

        @Override
        public String toString()
        {
            String output = "-- RequestData --\n";
            try {
                output += "ReportType: " + Integer.toString(ReportType) + "\n";
                output += "RequestFlags: " + Integer.toString(RequestFlags) + "\n";
                output += Helpers.FieldToString(_filter, "Filter") + "\n";
                output += "ParcelLocalID: " + Integer.toString(ParcelLocalID) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RequestDataBlock createRequestDataBlock() {
         return new RequestDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.LandStatRequest; }
    public AgentDataBlock AgentData;
    public RequestDataBlock RequestData;

    public LandStatRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)421);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        RequestData = new RequestDataBlock();
    }

    public LandStatRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        RequestData = new RequestDataBlock(bytes);
     }

    public LandStatRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        RequestData = new RequestDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += RequestData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        RequestData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LandStatRequest ---\n";
        output += AgentData.toString() + "\n";
        output += RequestData.toString() + "\n";
        return output;
    }
}
