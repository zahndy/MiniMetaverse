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
import libomv.types.UUID;

public class LogFailedMoneyTransactionPacket extends Packet
{
    public class TransactionDataBlock
    {
        public UUID TransactionID = null;
        public int TransactionTime = 0;
        public int TransactionType = 0;
        public UUID SourceID = null;
        public UUID DestID = null;
        public byte Flags = 0;
        public int Amount = 0;
        public int SimulatorIP = 0;
        public int GridX = 0;
        public int GridY = 0;
        public byte FailureType = 0;

        public int getLength(){
            return 74;
        }

        public TransactionDataBlock() { }
        public TransactionDataBlock(ByteBuffer bytes)
        {
            TransactionID = new UUID(bytes);
            TransactionTime = bytes.getInt();
            TransactionType = bytes.getInt();
            SourceID = new UUID(bytes);
            DestID = new UUID(bytes);
            Flags = bytes.get();
            Amount = bytes.getInt();
            SimulatorIP = bytes.getInt();
            GridX = bytes.getInt();
            GridY = bytes.getInt();
            FailureType = bytes.get();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
            bytes.putInt(TransactionTime);
            bytes.putInt(TransactionType);
            SourceID.GetBytes(bytes);
            DestID.GetBytes(bytes);
            bytes.put(Flags);
            bytes.putInt(Amount);
            bytes.putInt(SimulatorIP);
            bytes.putInt(GridX);
            bytes.putInt(GridY);
            bytes.put(FailureType);
        }

        @Override
        public String toString()
        {
            String output = "-- TransactionData --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "TransactionTime: " + Integer.toString(TransactionTime) + "\n";
                output += "TransactionType: " + Integer.toString(TransactionType) + "\n";
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "DestID: " + DestID.toString() + "\n";
                output += "Flags: " + Byte.toString(Flags) + "\n";
                output += "Amount: " + Integer.toString(Amount) + "\n";
                output += "SimulatorIP: " + Integer.toString(SimulatorIP) + "\n";
                output += "GridX: " + Integer.toString(GridX) + "\n";
                output += "GridY: " + Integer.toString(GridY) + "\n";
                output += "FailureType: " + Byte.toString(FailureType) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransactionDataBlock createTransactionDataBlock() {
         return new TransactionDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.LogFailedMoneyTransaction; }
    public TransactionDataBlock TransactionData;

    public LogFailedMoneyTransactionPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)20);
        header.setReliable(true);
        TransactionData = new TransactionDataBlock();
    }

    public LogFailedMoneyTransactionPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TransactionData = new TransactionDataBlock(bytes);
     }

    public LogFailedMoneyTransactionPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TransactionData = new TransactionDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TransactionData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TransactionData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LogFailedMoneyTransaction ---\n";
        output += TransactionData.toString() + "\n";
        return output;
    }
}
