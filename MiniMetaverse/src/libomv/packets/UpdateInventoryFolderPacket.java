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

public class UpdateInventoryFolderPacket extends Packet
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

    public class FolderDataBlock
    {
        public UUID FolderID = null;
        public UUID ParentID = null;
        public byte Type = 0;
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


        public int getLength(){
            int length = 33;
            if (getName() != null) { length += 1 + getName().length; }
            return length;
        }

        public FolderDataBlock() { }
        public FolderDataBlock(ByteBuffer bytes)
        {
            int length;
            FolderID = new UUID(bytes);
            ParentID = new UUID(bytes);
            Type = bytes.get();
            length = bytes.get() & 0xFF;
            _name = new byte[length];
            bytes.get(_name);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            FolderID.GetBytes(bytes);
            ParentID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put((byte)_name.length);
            bytes.put(_name);
        }

        @Override
        public String toString()
        {
            String output = "-- FolderData --\n";
            try {
                output += "FolderID: " + FolderID.toString() + "\n";
                output += "ParentID: " + ParentID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += Helpers.FieldToString(_name, "Name") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FolderDataBlock createFolderDataBlock() {
         return new FolderDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UpdateInventoryFolder; }
    public AgentDataBlock AgentData;
    public FolderDataBlock[] FolderData;

    public UpdateInventoryFolderPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)274);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        FolderData = new FolderDataBlock[0];
    }

    public UpdateInventoryFolderPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            FolderData[j] = new FolderDataBlock(bytes);
        }
     }

    public UpdateInventoryFolderPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        FolderData = new FolderDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            FolderData[j] = new FolderDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length++;
        for (int j = 0; j < FolderData.length; j++) { length += FolderData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        bytes.put((byte)FolderData.length);
        for (int j = 0; j < FolderData.length; j++) { FolderData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UpdateInventoryFolder ---\n";
        output += AgentData.toString() + "\n";
        for (int j = 0; j < FolderData.length; j++)
        {
            output += FolderData[j].toString() + "\n";
        }
        return output;
    }
}
