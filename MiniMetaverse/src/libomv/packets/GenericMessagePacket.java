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

public class GenericMessagePacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public UUID TransactionID = null;

        public int getLength(){
            return 48;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            TransactionID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

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

        public int getLength(){
            int length = 16;
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
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_method.length);
            bytes.put(_method);
            Invoice.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- MethodData --\n";
            try {
                output += Helpers.FieldToString(_method, "Method") + "\n";
                output += "Invoice: " + Invoice.toString() + "\n";
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
    public PacketType getType() { return PacketType.GenericMessage; }
    public AgentDataBlock AgentData;
    public MethodDataBlock MethodData;
    public ParamListBlock[] ParamList;

    public GenericMessagePacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)261);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MethodData = new MethodDataBlock();
        ParamList = new ParamListBlock[0];
    }

    public GenericMessagePacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MethodData = new MethodDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ParamList = new ParamListBlock[count];
        for (int j = 0; j < count; j++)
        {
            ParamList[j] = new ParamListBlock(bytes);
        }
     }

    public GenericMessagePacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
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
        length += AgentData.getLength();
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
        AgentData.ToBytes(bytes);
        MethodData.ToBytes(bytes);
        bytes.put((byte)ParamList.length);
        for (int j = 0; j < ParamList.length; j++) { ParamList[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GenericMessage ---\n";
        output += AgentData.toString() + "\n";
        output += MethodData.toString() + "\n";
        for (int j = 0; j < ParamList.length; j++)
        {
            output += ParamList[j].toString() + "\n";
        }
        return output;
    }
}
