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

public class SystemMessagePacket extends Packet
{
    public class MethodDataBlock
    {
        private byte[] _method;
        public byte[] getMethod() {
            return _method;
        }

        public void setMethod(byte[] value) throws Exception {
            if (value == null) {
                _method = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _method = new byte[value.length];
                System.arraycopy(value, 0, _method, 0, value.length);
            }
        }

        public UUID Invoice = null;
        public byte[] Digest = null;

        public int getLength(){
            int length = 48;
            if (getMethod() != null) { length += 1 + getMethod().length; }
            return length;
        }

        public MethodDataBlock() { }
        public MethodDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _method = new byte[length];
            bytes.get(_method);
            Invoice = new UUID(bytes);
            Digest = new byte[32];
            bytes.get(Digest);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_method.length);
            bytes.put(_method);
            Invoice.GetBytes(bytes);
            bytes.put(Digest);
        }

        @Override
        public String toString()
        {
            String output = "-- MethodData --\n";
            try {
                output += Helpers.FieldToString(_method, "Method") + "\n";
                output += "Invoice: " + Invoice.toString() + "\n";
                output += Helpers.FieldToString(Digest, "Digest") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MethodDataBlock createMethodDataBlock() {
         return new MethodDataBlock();
    }

    public class ParamListBlock
    {
        private byte[] _parameter;
        public byte[] getParameter() {
            return _parameter;
        }

        public void setParameter(byte[] value) throws Exception {
            if (value == null) {
                _parameter = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _parameter = new byte[value.length];
                System.arraycopy(value, 0, _parameter, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getParameter() != null) { length += 1 + getParameter().length; }
            return length;
        }

        public ParamListBlock() { }
        public ParamListBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _parameter = new byte[length];
            bytes.get(_parameter);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_parameter.length);
            bytes.put(_parameter);
        }

        @Override
        public String toString()
        {
            String output = "-- ParamList --\n";
            try {
                output += Helpers.FieldToString(_parameter, "Parameter") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ParamListBlock createParamListBlock() {
         return new ParamListBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SystemMessage; }
    public MethodDataBlock MethodData;
    public ParamListBlock[] ParamList;

    public SystemMessagePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)404);
        header.setReliable(true);
        MethodData = new MethodDataBlock();
        ParamList = new ParamListBlock[0];
    }

    public SystemMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        MethodData = new MethodDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParamList = new ParamListBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParamList[j] = new ParamListBlock(bytes);
        }
     }

    public SystemMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        MethodData = new MethodDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParamList = new ParamListBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParamList[j] = new ParamListBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += MethodData.getLength();
        length++;
        for (int j = 0; j < ParamList.length; j++) { length += ParamList[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        MethodData.ToBytes(bytes);
        bytes.put((byte)ParamList.length);
        for (int j = 0; j < ParamList.length; j++) { ParamList[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SystemMessage ---\n";
        output += MethodData.toString() + "\n";
        for (int j = 0; j < ParamList.length; j++)
        {
            output += ParamList[j].toString() + "\n";
        }
        return output;
    }
}
