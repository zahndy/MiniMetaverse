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

public class ViewerStatsPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID SessionID = null;
        public int IP = 0;
        public int StartTime = 0;
        public float RunTime = 0;
        public float SimFPS = 0;
        public float FPS = 0;
        public byte AgentsInView = 0;
        public float Ping = 0;
        public double MetersTraveled = 0;
        public int RegionsVisited = 0;
        public int SysRAM = 0;
        private byte[] _sysos;
        public byte[] getSysOS() {
            return _sysos;
        }

        public void setSysOS(byte[] value) throws Exception {
            if (value == null) {
                _sysos = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _sysos = new byte[value.length];
                System.arraycopy(value, 0, _sysos, 0, value.length);
            }
        }

        private byte[] _syscpu;
        public byte[] getSysCPU() {
            return _syscpu;
        }

        public void setSysCPU(byte[] value) throws Exception {
            if (value == null) {
                _syscpu = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _syscpu = new byte[value.length];
                System.arraycopy(value, 0, _syscpu, 0, value.length);
            }
        }

        private byte[] _sysgpu;
        public byte[] getSysGPU() {
            return _sysgpu;
        }

        public void setSysGPU(byte[] value) throws Exception {
            if (value == null) {
                _sysgpu = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _sysgpu = new byte[value.length];
                System.arraycopy(value, 0, _sysgpu, 0, value.length);
            }
        }


        public int getLength(){
            int length = 73;
            if (getSysOS() != null) { length += 1 + getSysOS().length; }
            if (getSysCPU() != null) { length += 1 + getSysCPU().length; }
            if (getSysGPU() != null) { length += 1 + getSysGPU().length; }
            return length;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            SessionID = new UUID(bytes);
            IP = bytes.getInt();
            StartTime = bytes.getInt();
            RunTime = bytes.getFloat();
            SimFPS = bytes.getFloat();
            FPS = bytes.getFloat();
            AgentsInView = bytes.get();
            Ping = bytes.getFloat();
            MetersTraveled = bytes.getDouble();
            RegionsVisited = bytes.getInt();
            SysRAM = bytes.getInt();
            length = bytes.get() & 0xFF;
            _sysos = new byte[length];
            bytes.get(_sysos);
            length = bytes.get() & 0xFF;
            _syscpu = new byte[length];
            bytes.get(_syscpu);
            length = bytes.get() & 0xFF;
            _sysgpu = new byte[length];
            bytes.get(_sysgpu);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            SessionID.GetBytes(bytes);
            bytes.putInt(IP);
            bytes.putInt(StartTime);
            bytes.putFloat(RunTime);
            bytes.putFloat(SimFPS);
            bytes.putFloat(FPS);
            bytes.put(AgentsInView);
            bytes.putFloat(Ping);
            bytes.putDouble(MetersTraveled);
            bytes.putInt(RegionsVisited);
            bytes.putInt(SysRAM);
            bytes.put((byte)_sysos.length);
            bytes.put(_sysos);
            bytes.put((byte)_syscpu.length);
            bytes.put(_syscpu);
            bytes.put((byte)_sysgpu.length);
            bytes.put(_sysgpu);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "SessionID: " + SessionID.toString() + "\n";
                output += "IP: " + Integer.toString(IP) + "\n";
                output += "StartTime: " + Integer.toString(StartTime) + "\n";
                output += "RunTime: " + Float.toString(RunTime) + "\n";
                output += "SimFPS: " + Float.toString(SimFPS) + "\n";
                output += "FPS: " + Float.toString(FPS) + "\n";
                output += "AgentsInView: " + Byte.toString(AgentsInView) + "\n";
                output += "Ping: " + Float.toString(Ping) + "\n";
                output += "MetersTraveled: " + Double.toString(MetersTraveled) + "\n";
                output += "RegionsVisited: " + Integer.toString(RegionsVisited) + "\n";
                output += "SysRAM: " + Integer.toString(SysRAM) + "\n";
                output += Helpers.FieldToString(_sysos, "SysOS") + "\n";
                output += Helpers.FieldToString(_syscpu, "SysCPU") + "\n";
                output += Helpers.FieldToString(_sysgpu, "SysGPU") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class DownloadTotalsBlock
    {
        public int World = 0;
        public int Objects = 0;
        public int Textures = 0;

        public int getLength(){
            return 12;
        }

        public DownloadTotalsBlock() { }
        public DownloadTotalsBlock(ByteBuffer bytes)
        {
            World = bytes.getInt();
            Objects = bytes.getInt();
            Textures = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(World);
            bytes.putInt(Objects);
            bytes.putInt(Textures);
        }

        @Override
        public String toString()
        {
            String output = "-- DownloadTotals --\n";
            try {
                output += "World: " + Integer.toString(World) + "\n";
                output += "Objects: " + Integer.toString(Objects) + "\n";
                output += "Textures: " + Integer.toString(Textures) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DownloadTotalsBlock createDownloadTotalsBlock() {
         return new DownloadTotalsBlock();
    }

    public class NetStatsBlock
    {
        public int Bytes = 0;
        public int Packets = 0;
        public int Compressed = 0;
        public int Savings = 0;

        public int getLength(){
            return 16;
        }

        public NetStatsBlock() { }
        public NetStatsBlock(ByteBuffer bytes)
        {
            Bytes = bytes.getInt();
            Packets = bytes.getInt();
            Compressed = bytes.getInt();
            Savings = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Bytes);
            bytes.putInt(Packets);
            bytes.putInt(Compressed);
            bytes.putInt(Savings);
        }

        @Override
        public String toString()
        {
            String output = "-- NetStats --\n";
            try {
                output += "Bytes: " + Integer.toString(Bytes) + "\n";
                output += "Packets: " + Integer.toString(Packets) + "\n";
                output += "Compressed: " + Integer.toString(Compressed) + "\n";
                output += "Savings: " + Integer.toString(Savings) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public NetStatsBlock createNetStatsBlock() {
         return new NetStatsBlock();
    }

    public class FailStatsBlock
    {
        public int SendPacket = 0;
        public int Dropped = 0;
        public int Resent = 0;
        public int FailedResends = 0;
        public int OffCircuit = 0;
        public int Invalid = 0;

        public int getLength(){
            return 24;
        }

        public FailStatsBlock() { }
        public FailStatsBlock(ByteBuffer bytes)
        {
            SendPacket = bytes.getInt();
            Dropped = bytes.getInt();
            Resent = bytes.getInt();
            FailedResends = bytes.getInt();
            OffCircuit = bytes.getInt();
            Invalid = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(SendPacket);
            bytes.putInt(Dropped);
            bytes.putInt(Resent);
            bytes.putInt(FailedResends);
            bytes.putInt(OffCircuit);
            bytes.putInt(Invalid);
        }

        @Override
        public String toString()
        {
            String output = "-- FailStats --\n";
            try {
                output += "SendPacket: " + Integer.toString(SendPacket) + "\n";
                output += "Dropped: " + Integer.toString(Dropped) + "\n";
                output += "Resent: " + Integer.toString(Resent) + "\n";
                output += "FailedResends: " + Integer.toString(FailedResends) + "\n";
                output += "OffCircuit: " + Integer.toString(OffCircuit) + "\n";
                output += "Invalid: " + Integer.toString(Invalid) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FailStatsBlock createFailStatsBlock() {
         return new FailStatsBlock();
    }

    public class MiscStatsBlock
    {
        public int Type = 0;
        public double Value = 0;

        public int getLength(){
            return 12;
        }

        public MiscStatsBlock() { }
        public MiscStatsBlock(ByteBuffer bytes)
        {
            Type = bytes.getInt();
            Value = bytes.getDouble();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(Type);
            bytes.putDouble(Value);
        }

        @Override
        public String toString()
        {
            String output = "-- MiscStats --\n";
            try {
                output += "Type: " + Integer.toString(Type) + "\n";
                output += "Value: " + Double.toString(Value) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MiscStatsBlock createMiscStatsBlock() {
         return new MiscStatsBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ViewerStats; }
    public AgentDataBlock AgentData;
    public DownloadTotalsBlock DownloadTotals;
    public NetStatsBlock[] NetStats;
    public FailStatsBlock FailStats;
    public MiscStatsBlock[] MiscStats;

    public ViewerStatsPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)131);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        DownloadTotals = new DownloadTotalsBlock();
        NetStats = new NetStatsBlock[2];
        FailStats = new FailStatsBlock();
        MiscStats = new MiscStatsBlock[0];
    }

    public ViewerStatsPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        DownloadTotals = new DownloadTotalsBlock(bytes);
        NetStats = new NetStatsBlock[2];
        for (int j = 0; j < 2; j++)
        {
            NetStats[j] = new NetStatsBlock(bytes);
        }
        FailStats = new FailStatsBlock(bytes);
        int count = bytes.get() & 0xFF;
        MiscStats = new MiscStatsBlock[count];
        for (int j = 0; j < count; j++)
        {
            MiscStats[j] = new MiscStatsBlock(bytes);
        }
     }

    public ViewerStatsPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        DownloadTotals = new DownloadTotalsBlock(bytes);
        NetStats = new NetStatsBlock[2];
        for (int j = 0; j < 2; j++)
        {
            NetStats[j] = new NetStatsBlock(bytes);
        }
        FailStats = new FailStatsBlock(bytes);
        int count = bytes.get() & 0xFF;
        MiscStats = new MiscStatsBlock[count];
        for (int j = 0; j < count; j++)
        {
            MiscStats[j] = new MiscStatsBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += DownloadTotals.getLength();
        length += FailStats.getLength();
        for (int j = 0; j < 2; j++) { length += NetStats[j].getLength(); }
        length++;
        for (int j = 0; j < MiscStats.length; j++) { length += MiscStats[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        DownloadTotals.ToBytes(bytes);
        for (int j = 0; j < 2; j++) { NetStats[j].ToBytes(bytes); }
        FailStats.ToBytes(bytes);
        bytes.put((byte)MiscStats.length);
        for (int j = 0; j < MiscStats.length; j++) { MiscStats[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ViewerStats ---\n";
        output += AgentData.toString() + "\n";
        output += DownloadTotals.toString() + "\n";
        for (int j = 0; j < 2; j++)
        {
            output += NetStats[j].toString() + "\n";
        }
        output += FailStats.toString() + "\n";
        for (int j = 0; j < MiscStats.length; j++)
        {
            output += MiscStats[j].toString() + "\n";
        }
        return output;
    }
}
