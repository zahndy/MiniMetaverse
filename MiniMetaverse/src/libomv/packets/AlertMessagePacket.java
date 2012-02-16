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

public class AlertMessagePacket extends Packet
{
    public class AlertDataBlock
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


        public int getLength(){
            int length = 0;
            if (getMessage() != null) { length += 1 + getMessage().length; }
            return length;
        }

        public AlertDataBlock() { }
        public AlertDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _message = new byte[length];
            bytes.get(_message);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_message.length);
            bytes.put(_message);
        }

        @Override
        public String toString()
        {
            String output = "-- AlertData --\n";
            try {
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AlertDataBlock createAlertDataBlock() {
         return new AlertDataBlock();
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
    public PacketType getType() { return PacketType.AlertMessage; }
    public AlertDataBlock AlertData;
    public AlertInfoBlock[] AlertInfo;

    public AlertMessagePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)134);
        header.setReliable(true);
        AlertData = new AlertDataBlock();
        AlertInfo = new AlertInfoBlock[0];
    }

    public AlertMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AlertData = new AlertDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        AlertInfo = new AlertInfoBlock[count];
        for (int j = 0; j < count; j++)
        {
            AlertInfo[j] = new AlertInfoBlock(bytes);
        }
     }

    public AlertMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AlertData = new AlertDataBlock(bytes);
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
        length += AlertData.getLength();
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
        AlertData.ToBytes(bytes);
        bytes.put((byte)AlertInfo.length);
        for (int j = 0; j < AlertInfo.length; j++) { AlertInfo[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AlertMessage ---\n";
        output += AlertData.toString() + "\n";
        for (int j = 0; j < AlertInfo.length; j++)
        {
            output += AlertInfo[j].toString() + "\n";
        }
        return output;
    }
}
