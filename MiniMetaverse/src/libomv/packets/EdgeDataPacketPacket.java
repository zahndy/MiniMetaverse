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

public class EdgeDataPacketPacket extends Packet
{
    public class EdgeDataBlock
    {
        public byte LayerType = 0;
        public byte Direction = 0;
        private byte[] _layerdata;
        public byte[] getLayerData() {
            return _layerdata;
        }

        public void setLayerData(byte[] value) throws Exception {
            if (value == null) {
                _layerdata = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _layerdata = new byte[value.length];
                System.arraycopy(value, 0, _layerdata, 0, value.length);
            }
        }


        public int getLength(){
            int length = 2;
            if (getLayerData() != null) { length += 2 + getLayerData().length; }
            return length;
        }

        public EdgeDataBlock() { }
        public EdgeDataBlock(ByteBuffer bytes)
        {
            int length;
            LayerType = bytes.get();
            Direction = bytes.get();
            length = bytes.getShort() & 0xFFFF;
            _layerdata = new byte[length];
            bytes.get(_layerdata);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(LayerType);
            bytes.put(Direction);
            bytes.putShort((short)_layerdata.length);
            bytes.put(_layerdata);
        }

        @Override
        public String toString()
        {
            String output = "-- EdgeData --\n";
            try {
                output += "LayerType: " + Byte.toString(LayerType) + "\n";
                output += "Direction: " + Byte.toString(Direction) + "\n";
                output += Helpers.FieldToString(_layerdata, "LayerData") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public EdgeDataBlock createEdgeDataBlock() {
         return new EdgeDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.EdgeDataPacket; }
    public EdgeDataBlock EdgeData;

    public EdgeDataPacketPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.High);
        header.setID((short)24);
        header.setReliable(true);
        EdgeData = new EdgeDataBlock();
    }

    public EdgeDataPacketPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.High);
        EdgeData = new EdgeDataBlock(bytes);
     }

    public EdgeDataPacketPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        EdgeData = new EdgeDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += EdgeData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        EdgeData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- EdgeDataPacket ---\n";
        output += EdgeData.toString() + "\n";
        return output;
    }
}
