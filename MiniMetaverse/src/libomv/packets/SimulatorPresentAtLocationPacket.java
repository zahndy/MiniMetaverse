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
import libomv.types.Vector3;

public class SimulatorPresentAtLocationPacket extends Packet
{
    public class SimulatorPublicHostBlockBlock
    {
        public short Port = 0;
        public int SimulatorIP = 0;
        public int GridX = 0;
        public int GridY = 0;

        public int getLength(){
            return 14;
        }

        public SimulatorPublicHostBlockBlock() { }
        public SimulatorPublicHostBlockBlock(ByteBuffer bytes)
        {
            Port = (short)((bytes.get() << 8) + bytes.get());
            SimulatorIP = bytes.getInt();
            GridX = bytes.getInt();
            GridY = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((Port >> 8) % 256));
            bytes.put((byte)(Port % 256));
            bytes.putInt(SimulatorIP);
            bytes.putInt(GridX);
            bytes.putInt(GridY);
        }

        @Override
        public String toString()
        {
            String output = "-- SimulatorPublicHostBlock --\n";
            try {
                output += "Port: " + Short.toString(Port) + "\n";
                output += "SimulatorIP: " + Integer.toString(SimulatorIP) + "\n";
                output += "GridX: " + Integer.toString(GridX) + "\n";
                output += "GridY: " + Integer.toString(GridY) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimulatorPublicHostBlockBlock createSimulatorPublicHostBlockBlock() {
         return new SimulatorPublicHostBlockBlock();
    }

    public class NeighborBlockBlock
    {
        public int IP = 0;
        public short Port = 0;

        public int getLength(){
            return 6;
        }

        public NeighborBlockBlock() { }
        public NeighborBlockBlock(ByteBuffer bytes)
        {
            IP = bytes.getInt();
            Port = (short)((bytes.get() << 8) + bytes.get());
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(IP);
            bytes.put((byte)((Port >> 8) % 256));
            bytes.put((byte)(Port % 256));
        }

        @Override
        public String toString()
        {
            String output = "-- NeighborBlock --\n";
            try {
                output += "IP: " + Integer.toString(IP) + "\n";
                output += "Port: " + Short.toString(Port) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NeighborBlockBlock createNeighborBlockBlock() {
         return new NeighborBlockBlock();
    }

    public class SimulatorBlockBlock
    {
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

        public byte SimAccess = 0;
        public int RegionFlags = 0;
        public UUID RegionID = null;
        public int EstateID = 0;
        public int ParentEstateID = 0;

        public int getLength(){
            int length = 29;
            if (getSimName() != null) { length += 1 + getSimName().length; }
            return length;
        }

        public SimulatorBlockBlock() { }
        public SimulatorBlockBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _simname = new byte[length];
            bytes.get(_simname);
            SimAccess = bytes.get();
            RegionFlags = bytes.getInt();
            RegionID = new UUID(bytes);
            EstateID = bytes.getInt();
            ParentEstateID = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_simname.length);
            bytes.put(_simname);
            bytes.put(SimAccess);
            bytes.putInt(RegionFlags);
            RegionID.GetBytes(bytes);
            bytes.putInt(EstateID);
            bytes.putInt(ParentEstateID);
        }

        @Override
        public String toString()
        {
            String output = "-- SimulatorBlock --\n";
            try {
                output += Helpers.FieldToString(_simname, "SimName") + "\n";
                output += "SimAccess: " + Byte.toString(SimAccess) + "\n";
                output += "RegionFlags: " + Integer.toString(RegionFlags) + "\n";
                output += "RegionID: " + RegionID.toString() + "\n";
                output += "EstateID: " + Integer.toString(EstateID) + "\n";
                output += "ParentEstateID: " + Integer.toString(ParentEstateID) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimulatorBlockBlock createSimulatorBlockBlock() {
         return new SimulatorBlockBlock();
    }

    public class TelehubBlockBlock
    {
        public boolean HasTelehub = false;
        public Vector3 TelehubPos = null;

        public int getLength(){
            return 13;
        }

        public TelehubBlockBlock() { }
        public TelehubBlockBlock(ByteBuffer bytes)
        {
            HasTelehub = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            TelehubPos = new Vector3(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)((HasTelehub) ? 1 : 0));
            TelehubPos.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- TelehubBlock --\n";
            try {
                output += "HasTelehub: " + Boolean.toString(HasTelehub) + "\n";
                output += "TelehubPos: " + TelehubPos.toString() + "\n";
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
    public PacketType getType() { return PacketType.SimulatorPresentAtLocation; }
    public SimulatorPublicHostBlockBlock SimulatorPublicHostBlock;
    public NeighborBlockBlock[] NeighborBlock;
    public SimulatorBlockBlock SimulatorBlock;
    public TelehubBlockBlock[] TelehubBlock;

    public SimulatorPresentAtLocationPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)11);
        header.setReliable(true);
        SimulatorPublicHostBlock = new SimulatorPublicHostBlockBlock();
        NeighborBlock = new NeighborBlockBlock[4];
        SimulatorBlock = new SimulatorBlockBlock();
        TelehubBlock = new TelehubBlockBlock[0];
    }

    public SimulatorPresentAtLocationPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        SimulatorPublicHostBlock = new SimulatorPublicHostBlockBlock(bytes);
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
        SimulatorBlock = new SimulatorBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        TelehubBlock = new TelehubBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            TelehubBlock[j] = new TelehubBlockBlock(bytes);
        }
     }

    public SimulatorPresentAtLocationPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        SimulatorPublicHostBlock = new SimulatorPublicHostBlockBlock(bytes);
        NeighborBlock = new NeighborBlockBlock[4];
        for (int j = 0; j < 4; j++)
        {
            NeighborBlock[j] = new NeighborBlockBlock(bytes);
        }
        SimulatorBlock = new SimulatorBlockBlock(bytes);
        int count = bytes.get() & 0xFF;
        TelehubBlock = new TelehubBlockBlock[count];
        for (int j = 0; j < count; j++)
        {
            TelehubBlock[j] = new TelehubBlockBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += SimulatorPublicHostBlock.getLength();
        length += SimulatorBlock.getLength();
        for (int j = 0; j < 4; j++) { length += NeighborBlock[j].getLength(); }
        length++;
        for (int j = 0; j < TelehubBlock.length; j++) { length += TelehubBlock[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        SimulatorPublicHostBlock.ToBytes(bytes);
        for (int j = 0; j < 4; j++) { NeighborBlock[j].ToBytes(bytes); }
        SimulatorBlock.ToBytes(bytes);
        bytes.put((byte)TelehubBlock.length);
        for (int j = 0; j < TelehubBlock.length; j++) { TelehubBlock[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SimulatorPresentAtLocation ---\n";
        output += SimulatorPublicHostBlock.toString() + "\n";
        for (int j = 0; j < 4; j++)
        {
            output += NeighborBlock[j].toString() + "\n";
        }
        output += SimulatorBlock.toString() + "\n";
        for (int j = 0; j < TelehubBlock.length; j++)
        {
            output += TelehubBlock[j].toString() + "\n";
        }
        return output;
    }
}
