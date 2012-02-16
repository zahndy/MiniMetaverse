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
import libomv.types.Vector3;
import libomv.types.Quaternion;

public class TelehubInfoPacket extends Packet
{
    public class TelehubBlockBlock
    {
        public UUID ObjectID = null;
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

        public Vector3 TelehubPos = null;
        public Quaternion TelehubRot = null;

        public int getLength(){
            int length = 40;
            if (getObjectName() != null) { length += 1 + getObjectName().length; }
            return length;
        }

        public TelehubBlockBlock() { }
        public TelehubBlockBlock(ByteBuffer bytes)
        {
            int length;
            ObjectID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _objectname = new byte[length];
            bytes.get(_objectname);
            TelehubPos = new Vector3(bytes);
            TelehubRot = new Quaternion(bytes, true);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            bytes.put((byte)_objectname.length);
            bytes.put(_objectname);
            TelehubPos.GetBytes(bytes);
            TelehubRot.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- TelehubBlock --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += Helpers.FieldToString(_objectname, "ObjectName") + "\n";
                output += "TelehubPos: " + TelehubPos.toString() + "\n";
                output += "TelehubRot: " + TelehubRot.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TelehubBlockBlock createTelehubBlockBlock() {
         return new TelehubBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.TelehubInfo; }
    public TelehubBlockBlock TelehubBlock;
    public Vector3[] SpawnPointPos;

    public TelehubInfoPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)10);
        header.setReliable(true);
        TelehubBlock = new TelehubBlockBlock();
        SpawnPointPos = new Vector3[0];
    }

    public TelehubInfoPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TelehubBlock = new TelehubBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        SpawnPointPos = new Vector3[count];
        for (int j = 0; j < count; j++)
        {
            SpawnPointPos[j] = new Vector3(bytes);
        }
     }

    public TelehubInfoPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TelehubBlock = new TelehubBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        SpawnPointPos = new Vector3[count];
        for (int j = 0; j < count; j++)
        {
            SpawnPointPos[j] = new Vector3(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TelehubBlock.getLength();
        length++;
        length += SpawnPointPos.length * 12;
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TelehubBlock.ToBytes(bytes);
        bytes.put((byte)SpawnPointPos.length);
        for (int j = 0; j < SpawnPointPos.length; j++)
        {
            SpawnPointPos[j].GetBytes(bytes);
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- TelehubInfo ---\n";
        output += TelehubBlock.toString() + "\n";
        for (int j = 0; j < SpawnPointPos.length; j++)
        {
            output += "SpawnPointPos[" + j + "]: " + SpawnPointPos[j].toString() + "\n";
        }
        return output;
    }
}
