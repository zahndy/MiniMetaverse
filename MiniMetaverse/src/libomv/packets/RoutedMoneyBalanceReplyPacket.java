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

public class RoutedMoneyBalanceReplyPacket extends Packet
{
    public class TargetBlockBlock
    {
        public int TargetIP = 0;
        public short TargetPort = 0;

        public int getLength(){
            return 6;
        }

        public TargetBlockBlock() { }
        public TargetBlockBlock(ByteBuffer bytes)
        {
            TargetIP = bytes.getInt();
            TargetPort = (short)((bytes.get() << 8) + bytes.get());
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(TargetIP);
            bytes.put((byte)((TargetPort >> 8) % 256));
            bytes.put((byte)(TargetPort % 256));
        }

        @Override
        public String toString()
        {
            String output = "-- TargetBlock --\n";
            try {
                output += "TargetIP: " + Integer.toString(TargetIP) + "\n";
                output += "TargetPort: " + Short.toString(TargetPort) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TargetBlockBlock createTargetBlockBlock() {
         return new TargetBlockBlock();
    }

    public class MoneyDataBlock
    {
        public UUID AgentID = null;
        public UUID TransactionID = null;
        public boolean TransactionSuccess = false;
        public int MoneyBalance = 0;
        public int SquareMetersCredit = 0;
        public int SquareMetersCommitted = 0;
        private byte[] _description;
        public byte[] getDescription() {
            return _description;
        }

        public void setDescription(byte[] value) throws Exception {
            if (value == null) {
                _description = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _description = new byte[value.length];
                System.arraycopy(value, 0, _description, 0, value.length);
            }
        }


        public int getLength(){
            int length = 45;
            if (getDescription() != null) { length += 1 + getDescription().length; }
            return length;
        }

        public MoneyDataBlock() { }
        public MoneyDataBlock(ByteBuffer bytes)
        {
            int length;
            AgentID = new UUID(bytes);
            TransactionID = new UUID(bytes);
            TransactionSuccess = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            MoneyBalance = bytes.getInt();
            SquareMetersCredit = bytes.getInt();
            SquareMetersCommitted = bytes.getInt();
            length = bytes.get() & 0xFF;
            _description = new byte[length];
            bytes.get(_description);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            TransactionID.GetBytes(bytes);
            bytes.put((byte)((TransactionSuccess) ? 1 : 0));
            bytes.putInt(MoneyBalance);
            bytes.putInt(SquareMetersCredit);
            bytes.putInt(SquareMetersCommitted);
            bytes.put((byte)_description.length);
            bytes.put(_description);
        }

        @Override
        public String toString()
        {
            String output = "-- MoneyData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "TransactionSuccess: " + Boolean.toString(TransactionSuccess) + "\n";
                output += "MoneyBalance: " + Integer.toString(MoneyBalance) + "\n";
                output += "SquareMetersCredit: " + Integer.toString(SquareMetersCredit) + "\n";
                output += "SquareMetersCommitted: " + Integer.toString(SquareMetersCommitted) + "\n";
                output += Helpers.FieldToString(_description, "Description") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MoneyDataBlock createMoneyDataBlock() {
         return new MoneyDataBlock();
    }

    public class TransactionInfoBlock
    {
        public int TransactionType = 0;
        public UUID SourceID = null;
        public boolean IsSourceGroup = false;
        public UUID DestID = null;
        public boolean IsDestGroup = false;
        public int Amount = 0;
        private byte[] _itemdescription;
        public byte[] getItemDescription() {
            return _itemdescription;
        }

        public void setItemDescription(byte[] value) throws Exception {
            if (value == null) {
                _itemdescription = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _itemdescription = new byte[value.length];
                System.arraycopy(value, 0, _itemdescription, 0, value.length);
            }
        }


        public int getLength(){
            int length = 42;
            if (getItemDescription() != null) { length += 1 + getItemDescription().length; }
            return length;
        }

        public TransactionInfoBlock() { }
        public TransactionInfoBlock(ByteBuffer bytes)
        {
            int length;
            TransactionType = bytes.getInt();
            SourceID = new UUID(bytes);
            IsSourceGroup = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            DestID = new UUID(bytes);
            IsDestGroup = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            Amount = bytes.getInt();
            length = bytes.get() & 0xFF;
            _itemdescription = new byte[length];
            bytes.get(_itemdescription);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(TransactionType);
            SourceID.GetBytes(bytes);
            bytes.put((byte)((IsSourceGroup) ? 1 : 0));
            DestID.GetBytes(bytes);
            bytes.put((byte)((IsDestGroup) ? 1 : 0));
            bytes.putInt(Amount);
            bytes.put((byte)_itemdescription.length);
            bytes.put(_itemdescription);
        }

        @Override
        public String toString()
        {
            String output = "-- TransactionInfo --\n";
            try {
                output += "TransactionType: " + Integer.toString(TransactionType) + "\n";
                output += "SourceID: " + SourceID.toString() + "\n";
                output += "IsSourceGroup: " + Boolean.toString(IsSourceGroup) + "\n";
                output += "DestID: " + DestID.toString() + "\n";
                output += "IsDestGroup: " + Boolean.toString(IsDestGroup) + "\n";
                output += "Amount: " + Integer.toString(Amount) + "\n";
                output += Helpers.FieldToString(_itemdescription, "ItemDescription") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransactionInfoBlock createTransactionInfoBlock() {
         return new TransactionInfoBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.RoutedMoneyBalanceReply; }
    public TargetBlockBlock TargetBlock;
    public MoneyDataBlock MoneyData;
    public TransactionInfoBlock TransactionInfo;

    public RoutedMoneyBalanceReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)315);
        header.setReliable(true);
        TargetBlock = new TargetBlockBlock();
        MoneyData = new MoneyDataBlock();
        TransactionInfo = new TransactionInfoBlock();
    }

    public RoutedMoneyBalanceReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        TargetBlock = new TargetBlockBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
        TransactionInfo = new TransactionInfoBlock(bytes);
     }

    public RoutedMoneyBalanceReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        TargetBlock = new TargetBlockBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
        TransactionInfo = new TransactionInfoBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += TargetBlock.getLength();
        length += MoneyData.getLength();
        length += TransactionInfo.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        TargetBlock.ToBytes(bytes);
        MoneyData.ToBytes(bytes);
        TransactionInfo.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- RoutedMoneyBalanceReply ---\n";
        output += TargetBlock.toString() + "\n";
        output += MoneyData.toString() + "\n";
        output += TransactionInfo.toString() + "\n";
        return output;
    }
}
