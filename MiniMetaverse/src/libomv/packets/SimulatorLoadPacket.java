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

import libomv.types.PacketHeader;
import libomv.types.PacketFrequency;

public class SimulatorLoadPacket extends Packet
{
    public class SimulatorLoadBlock
    {
        public float TimeDilation = 0;
        public int AgentCount = 0;
        public boolean CanAcceptAgents = false;

        public int getLength(){
            return 9;
        }

        public SimulatorLoadBlock() { }
        public SimulatorLoadBlock(ByteBuffer bytes)
        {
            TimeDilation = bytes.getFloat();
            AgentCount = bytes.getInt();
            CanAcceptAgents = (bytes.get() != 0) ? (boolean)true : (boolean)false;
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putFloat(TimeDilation);
            bytes.putInt(AgentCount);
            bytes.put((byte)((CanAcceptAgents) ? 1 : 0));
        }

        @Override
        public String toString()
        {
            String output = "-- SimulatorLoad --\n";
            try {
                output += "TimeDilation: " + Float.toString(TimeDilation) + "\n";
                output += "AgentCount: " + Integer.toString(AgentCount) + "\n";
                output += "CanAcceptAgents: " + Boolean.toString(CanAcceptAgents) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public SimulatorLoadBlock createSimulatorLoadBlock() {
         return new SimulatorLoadBlock();
    }

    public class AgentListBlock
    {
        public int CircuitCode = 0;
        public byte X = 0;
        public byte Y = 0;

        public int getLength(){
            return 6;
        }

        public AgentListBlock() { }
        public AgentListBlock(ByteBuffer bytes)
        {
            CircuitCode = bytes.getInt();
            X = bytes.get();
            Y = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(CircuitCode);
            bytes.put(X);
            bytes.put(Y);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentList --\n";
            try {
                output += "CircuitCode: " + Integer.toString(CircuitCode) + "\n";
                output += "X: " + Byte.toString(X) + "\n";
                output += "Y: " + Byte.toString(Y) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentListBlock createAgentListBlock() {
         return new AgentListBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.SimulatorLoad; }
    public SimulatorLoadBlock SimulatorLoad;
    public AgentListBlock[] AgentList;

    public SimulatorLoadPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)12);
        header.setReliable(true);
        SimulatorLoad = new SimulatorLoadBlock();
        AgentList = new AgentListBlock[0];
    }

    public SimulatorLoadPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        SimulatorLoad = new SimulatorLoadBlock(bytes);
        int count = bytes.get() & 0xFF;
        AgentList = new AgentListBlock[count];
        for (int j = 0; j < count; j++)
        {
            AgentList[j] = new AgentListBlock(bytes);
        }
     }

    public SimulatorLoadPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        SimulatorLoad = new SimulatorLoadBlock(bytes);
        int count = bytes.get() & 0xFF;
        AgentList = new AgentListBlock[count];
        for (int j = 0; j < count; j++)
        {
            AgentList[j] = new AgentListBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += SimulatorLoad.getLength();
        length++;
        for (int j = 0; j < AgentList.length; j++) { length += AgentList[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        SimulatorLoad.ToBytes(bytes);
        bytes.put((byte)AgentList.length);
        for (int j = 0; j < AgentList.length; j++) { AgentList[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- SimulatorLoad ---\n";
        output += SimulatorLoad.toString() + "\n";
        for (int j = 0; j < AgentList.length; j++)
        {
            output += AgentList[j].toString() + "\n";
        }
        return output;
    }
}