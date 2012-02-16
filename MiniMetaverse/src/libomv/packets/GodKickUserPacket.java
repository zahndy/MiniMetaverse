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

public class GodKickUserPacket extends Packet
{
    public class UserInfoBlock
    {
        public UUID GodID = null;
        public UUID GodSessionID = null;
        public UUID AgentID = null;
        public int KickFlags = 0;
        private byte[] _reason;
        public byte[] getReason() {
            return _reason;
        }

        public void setReason(byte[] value) throws Exception {
            if (value == null) {
                _reason = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _reason = new byte[value.length];
                System.arraycopy(value, 0, _reason, 0, value.length);
            }
        }


        public int getLength(){
            int length = 52;
            if (getReason() != null) { length += 2 + getReason().length; }
            return length;
        }

        public UserInfoBlock() { }
        public UserInfoBlock(ByteBuffer bytes)
        {
            int length;
            GodID = new UUID(bytes);
            GodSessionID = new UUID(bytes);
            AgentID = new UUID(bytes);
            KickFlags = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _reason = new byte[length];
            bytes.get(_reason);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            GodID.GetBytes(bytes);
            GodSessionID.GetBytes(bytes);
            AgentID.GetBytes(bytes);
            bytes.putInt(KickFlags);
            bytes.putShort((short)_reason.length);
            bytes.put(_reason);
        }

        @Override
        public String toString()
        {
            String output = "-- UserInfo --\n";
            try {
                output += "GodID: " + GodID.toString() + "\n";
                output += "GodSessionID: " + GodSessionID.toString() + "\n";
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "KickFlags: " + Integer.toString(KickFlags) + "\n";
                output += Helpers.FieldToString(_reason, "Reason") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public UserInfoBlock createUserInfoBlock() {
         return new UserInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GodKickUser; }
    public UserInfoBlock UserInfo;

    public GodKickUserPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)165);
        header.setReliable(true);
        UserInfo = new UserInfoBlock();
    }

    public GodKickUserPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        UserInfo = new UserInfoBlock(bytes);
     }

    public GodKickUserPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        UserInfo = new UserInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += UserInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        UserInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GodKickUser ---\n";
        output += UserInfo.toString() + "\n";
        return output;
    }
}
