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
import libomv.types.OverflowException;
import libomv.types.UUID;

public class FeatureDisabledPacket extends Packet
{
    public class FailureInfoBlock
    {
        private byte[] _errormessage;
        public byte[] getErrorMessage() {
            return _errormessage;
        }

        public void setErrorMessage(byte[] value) throws Exception {
            if (value == null) {
                _errormessage = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _errormessage = new byte[value.length];
                System.arraycopy(value, 0, _errormessage, 0, value.length);
            }
        }

        public UUID AgentID = null;
        public UUID TransactionID = null;

        public int getLength(){
            int length = 32;
            if (getErrorMessage() != null) { length += 1 + getErrorMessage().length; }
            return length;
        }

        public FailureInfoBlock() { }
        public FailureInfoBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _errormessage = new byte[length];
            bytes.get(_errormessage);
            AgentID = new UUID(bytes);
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_errormessage.length);
            bytes.put(_errormessage);
            AgentID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- FailureInfo --\n";
            try {
                output += Helpers.FieldToString(_errormessage, "ErrorMessage") + "\n";
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FailureInfoBlock createFailureInfoBlock() {
         return new FailureInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.FeatureDisabled; }
    public FailureInfoBlock FailureInfo;

    public FeatureDisabledPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)19);
        header.setReliable(true);
        FailureInfo = new FailureInfoBlock();
    }

    public FeatureDisabledPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        FailureInfo = new FailureInfoBlock(bytes);
     }

    public FeatureDisabledPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        FailureInfo = new FailureInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += FailureInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        FailureInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- FeatureDisabled ---\n";
        output += FailureInfo.toString() + "\n";
        return output;
    }
}
