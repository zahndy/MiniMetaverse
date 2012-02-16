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
import libomv.types.Vector3;

public class ScriptTeleportRequestPacket extends Packet
{
    public class DataBlock
    {
        private byte[] _objectname;
        public byte[] getObjectName() {
            return _objectname;
        }

        public void setObjectName(byte[] value) throws Exception {
            if (value == null) {
                _objectname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _objectname = new byte[value.length];
                System.arraycopy(value, 0, _objectname, 0, value.length);
            }
        }

        private byte[] _simname;
        public byte[] getSimName() {
            return _simname;
        }

        public void setSimName(byte[] value) throws Exception {
            if (value == null) {
                _simname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simname = new byte[value.length];
                System.arraycopy(value, 0, _simname, 0, value.length);
            }
        }

        public Vector3 SimPosition = null;
        public Vector3 LookAt = null;

        public int getLength(){
            int length = 24;
            if (getObjectName() != null) { length += 1 + getObjectName().length; }
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _objectname = new byte[length];
            bytes.get(_objectname);
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            SimPosition = new Vector3(bytes);
            LookAt = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_objectname.length);
            bytes.put(_objectname);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            SimPosition.GetBytes(bytes);
            LookAt.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += Helpers.FieldToString(_objectname, "ObjectName") + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "SimPosition: " + SimPosition.toString() + "\n";
                output += "LookAt: " + LookAt.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ScriptTeleportRequest; }
    public DataBlock Data;

    public ScriptTeleportRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)195);
        header.setReliable(true);
        Data = new DataBlock();
    }

    public ScriptTeleportRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
     }

    public ScriptTeleportRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ScriptTeleportRequest ---\n";
        output += Data.toString() + "\n";
        return output;
    }
}
