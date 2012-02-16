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

public class LiveHelpGroupReplyPacket extends Packet
{
    public class ReplyDataBlock
    {
        public UUID RequestID = null;
        public UUID GroupID = null;
        private byte[] _selection;
        public byte[] getSelection() {
            return _selection;
        }

        public void setSelection(byte[] value) throws Exception {
            if (value == null) {
                _selection = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _selection = new byte[value.length];
                System.arraycopy(value, 0, _selection, 0, value.length);
            }
        }


        public int getLength(){
            int length = 32;
            if (getSelection() != null) { length += 1 + getSelection().length; }
            return length;
        }

        public ReplyDataBlock() { }
        public ReplyDataBlock(ByteBuffer bytes)
        {
            int length;
            RequestID = new UUID(bytes);
            GroupID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _selection = new byte[length];
            bytes.get(_selection);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RequestID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
            bytes.put((byte)_selection.length);
            bytes.put(_selection);
        }

        @Override
        public String toString()
        {
            String output = "-- ReplyData --\n";
            try {
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output += Helpers.FieldToString(_selection, "Selection") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ReplyDataBlock createReplyDataBlock() {
         return new ReplyDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.LiveHelpGroupReply; }
    public ReplyDataBlock ReplyData;

    public LiveHelpGroupReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)380);
        header.setReliable(true);
        ReplyData = new ReplyDataBlock();
    }

    public LiveHelpGroupReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ReplyData = new ReplyDataBlock(bytes);
     }

    public LiveHelpGroupReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ReplyData = new ReplyDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ReplyData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ReplyData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LiveHelpGroupReply ---\n";
        output += ReplyData.toString() + "\n";
        return output;
    }
}
