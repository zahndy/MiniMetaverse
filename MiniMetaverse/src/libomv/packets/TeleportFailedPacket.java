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

public class TeleportFailedPacket extends Packet
{
    public class InfoBlock
    {
        public UUID AgentID = null;
        private byte[] _reason;
        public byte[] getReason() {
            return _reason;
        }

        public void setReason(byte[] value) throws Exception {
            if (value == null) {
                _reason = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _reason = new byte[value.length];
                System.arraycopy(value, 0, _reason, 0, value.length);
            }
        }


        public int getLength(){
            int length = 16;
            if (getReason() != null) { length += 1 + getReason().length; }
            return length;
        }

        public InfoBlock() { }
        public InfoBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _reason = new byte[length];
            bytes.get(_reason);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            bytes.put((byte)_reason.length);
            bytes.put(_reason);
        }

        @Override
        public String toString()
        {
            String output = "-- Info --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += Helpers.FieldToString(_reason, "Reason") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public InfoBlock createInfoBlock() {
         return new InfoBlock();
    }

    public class AlertInfoBlock
    {
        private byte[] _message;
        public byte[] getMessage() {
            return _message;
        }

        public void setMessage(byte[] value) throws Exception {
            if (value == null) {
                _message = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _message = new byte[value.length];
                System.arraycopy(value, 0, _message, 0, value.length);
            }
        }

        private byte[] _extraparams;
        public byte[] getExtraParams() {
            return _extraparams;
        }

        public void setExtraParams(byte[] value) throws Exception {
            if (value == null) {
                _extraparams = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _extraparams = new byte[value.length];
                System.arraycopy(value, 0, _extraparams, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getMessage() != null) { length += 1 + getMessage().length; }
            if (getExtraParams() != null) { length += 1 + getExtraParams().length; }
            return length;
        }

        public AlertInfoBlock() { }
        public AlertInfoBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _message = new byte[length];
            bytes.get(_message);
            length = bytes.get() & 0xFF;
            _extraparams = new byte[length];
            bytes.get(_extraparams);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_message.length);
            bytes.put(_message);
            bytes.put((byte)_extraparams.length);
            bytes.put(_extraparams);
        }

        @Override
        public String toString()
        {
            String output = "-- AlertInfo --\n";
            try {
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += Helpers.FieldToString(_extraparams, "ExtraParams") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AlertInfoBlock createAlertInfoBlock() {
         return new AlertInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TeleportFailed; }
    public InfoBlock Info;
    public AlertInfoBlock[] AlertInfo;

    public TeleportFailedPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)74);
        header.setReliable(true);
        Info = new InfoBlock();
        AlertInfo = new AlertInfoBlock[0];
    }

    public TeleportFailedPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Info = new InfoBlock(bytes);
        int count = bytes.get() & 0xFF;
        AlertInfo = new AlertInfoBlock[count];
        for (int j = 0; j < count; j++)
        {
            AlertInfo[j] = new AlertInfoBlock(bytes);
        }
     }

    public TeleportFailedPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Info = new InfoBlock(bytes);
        int count = bytes.get() & 0xFF;
        AlertInfo = new AlertInfoBlock[count];
        for (int j = 0; j < count; j++)
        {
            AlertInfo[j] = new AlertInfoBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Info.getLength();
        length++;
        for (int j = 0; j < AlertInfo.length; j++) { length += AlertInfo[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Info.ToBytes(bytes);
        bytes.put((byte)AlertInfo.length);
        for (int j = 0; j < AlertInfo.length; j++) { AlertInfo[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TeleportFailed ---\n";
        output += Info.toString() + "\n";
        for (int j = 0; j < AlertInfo.length; j++)
        {
            output += AlertInfo[j].toString() + "\n";
        }
        return output;
    }
}
