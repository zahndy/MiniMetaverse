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

public class UpdateSimulatorPacket extends Packet
{
    public class SimulatorInfoBlock
    {
        public UUID RegionID = null;
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

        public int EstateID = 0;
        public byte SimAccess = 0;

        public int getLength(){
            int length = 21;
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public SimulatorInfoBlock() { }
        public SimulatorInfoBlock(ByteBuffer bytes)
        {
            int length;
            RegionID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            EstateID = bytes.getInt();
            SimAccess = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RegionID.GetBytes(bytes);
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            bytes.putInt(EstateID);
            bytes.put(SimAccess);
        }

        @Override
        public String toString()
        {
            String output = "-- SimulatorInfo --\n";
            try {
                output += "RegionID: " + RegionID.toString() + "\n";
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimulatorInfoBlock createSimulatorInfoBlock() {
         return new SimulatorInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.UpdateSimulator; }
    public SimulatorInfoBlock SimulatorInfo;

    public UpdateSimulatorPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)17);
        header.setReliable(true);
        SimulatorInfo = new SimulatorInfoBlock();
    }

    public UpdateSimulatorPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        SimulatorInfo = new SimulatorInfoBlock(bytes);
     }

    public UpdateSimulatorPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        SimulatorInfo = new SimulatorInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += SimulatorInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        SimulatorInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UpdateSimulator ---\n";
        output += SimulatorInfo.toString() + "\n";
        return output;
    }
}
