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

public class UpdateMuteListEntryPacket extends Packet
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

    public class MuteDataBlock
    {
        public UUID MuteID = null;
        private byte[] _mutename;
        public byte[] getMuteName() {
            return _mutename;
        }

        public void setMuteName(byte[] value) throws Exception {
            if (value == null) {
                _mutename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _mutename = new byte[value.length];
                System.arraycopy(value, 0, _mutename, 0, value.length);
            }
        }

        public int MuteType = 0;
        public int MuteFlags = 0;

        public int getLength(){
            int length = 24;
            if (getMuteName() != null) { length += 1 + getMuteName().length; }
            return length;
        }

        public MuteDataBlock() { }
        public MuteDataBlock(ByteBuffer bytes)
        {
            int length;
            MuteID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _mutename = new byte[length];
            bytes.get(_mutename);
            MuteType = bytes.getInt();
            MuteFlags = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            MuteID.GetBytes(bytes);
            bytes.put((byte)_mutename.length);
            bytes.put(_mutename);
            bytes.putInt(MuteType);
            bytes.putInt(MuteFlags);
        }

        @Override
        public String toString()
        {
            String output = "-- MuteData --\n";
            try {
                output += "MuteID: " + MuteID.toString() + "\n";
                output += Helpers.FieldToString(_mutename, "MuteName") + "\n";
                output += "MuteType: " + Integer.toString(MuteType) + "\n";
                output += "MuteFlags: " + Integer.toString(MuteFlags) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MuteDataBlock createMuteDataBlock() {
         return new MuteDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UpdateMuteListEntry; }
    public AgentDataBlock AgentData;
    public MuteDataBlock MuteData;

    public UpdateMuteListEntryPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)263);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MuteData = new MuteDataBlock();
    }

    public UpdateMuteListEntryPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MuteData = new MuteDataBlock(bytes);
     }

    public UpdateMuteListEntryPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        MuteData = new MuteDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += MuteData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        MuteData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UpdateMuteListEntry ---\n";
        output += AgentData.toString() + "\n";
        output += MuteData.toString() + "\n";
        return output;
    }
}
