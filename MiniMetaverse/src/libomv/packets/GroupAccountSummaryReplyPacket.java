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

public class GroupAccountSummaryReplyPacket extends Packet
{
    public class AgentDataBlock
    {
        public UUID AgentID = null;
        public UUID GroupID = null;

        public int getLength(){
            return 32;
        }

        public AgentDataBlock() { }
        public AgentDataBlock(ByteBuffer bytes)
        {
            AgentID = new UUID(bytes);
            GroupID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            AgentID.GetBytes(bytes);
            GroupID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- AgentData --\n";
            try {
                output += "AgentID: " + AgentID.toString() + "\n";
                output += "GroupID: " + GroupID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AgentDataBlock createAgentDataBlock() {
         return new AgentDataBlock();
    }

    public class MoneyDataBlock
    {
        public UUID RequestID = null;
        public int IntervalDays = 0;
        public int CurrentInterval = 0;
        private byte[] _startdate;
        public byte[] getStartDate() {
            return _startdate;
        }

        public void setStartDate(byte[] value) throws Exception {
            if (value == null) {
                _startdate = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _startdate = new byte[value.length];
                System.arraycopy(value, 0, _startdate, 0, value.length);
            }
        }

        public int Balance = 0;
        public int TotalCredits = 0;
        public int TotalDebits = 0;
        public int ObjectTaxCurrent = 0;
        public int LightTaxCurrent = 0;
        public int LandTaxCurrent = 0;
        public int GroupTaxCurrent = 0;
        public int ParcelDirFeeCurrent = 0;
        public int ObjectTaxEstimate = 0;
        public int LightTaxEstimate = 0;
        public int LandTaxEstimate = 0;
        public int GroupTaxEstimate = 0;
        public int ParcelDirFeeEstimate = 0;
        public int NonExemptMembers = 0;
        private byte[] _lasttaxdate;
        public byte[] getLastTaxDate() {
            return _lasttaxdate;
        }

        public void setLastTaxDate(byte[] value) throws Exception {
            if (value == null) {
                _lasttaxdate = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _lasttaxdate = new byte[value.length];
                System.arraycopy(value, 0, _lasttaxdate, 0, value.length);
            }
        }

        private byte[] _taxdate;
        public byte[] getTaxDate() {
            return _taxdate;
        }

        public void setTaxDate(byte[] value) throws Exception {
            if (value == null) {
                _taxdate = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _taxdate = new byte[value.length];
                System.arraycopy(value, 0, _taxdate, 0, value.length);
            }
        }


        public int getLength(){
            int length = 80;
            if (getStartDate() != null) { length += 1 + getStartDate().length; }
            if (getLastTaxDate() != null) { length += 1 + getLastTaxDate().length; }
            if (getTaxDate() != null) { length += 1 + getTaxDate().length; }
            return length;
        }

        public MoneyDataBlock() { }
        public MoneyDataBlock(ByteBuffer bytes)
        {
            int length;
            RequestID = new UUID(bytes);
            IntervalDays = bytes.getInt();
            CurrentInterval = bytes.getInt();
            length = bytes.get() & 0xFF;
            _startdate = new byte[length];
            bytes.get(_startdate);
            Balance = bytes.getInt();
            TotalCredits = bytes.getInt();
            TotalDebits = bytes.getInt();
            ObjectTaxCurrent = bytes.getInt();
            LightTaxCurrent = bytes.getInt();
            LandTaxCurrent = bytes.getInt();
            GroupTaxCurrent = bytes.getInt();
            ParcelDirFeeCurrent = bytes.getInt();
            ObjectTaxEstimate = bytes.getInt();
            LightTaxEstimate = bytes.getInt();
            LandTaxEstimate = bytes.getInt();
            GroupTaxEstimate = bytes.getInt();
            ParcelDirFeeEstimate = bytes.getInt();
            NonExemptMembers = bytes.getInt();
            length = bytes.get() & 0xFF;
            _lasttaxdate = new byte[length];
            bytes.get(_lasttaxdate);
            length = bytes.get() & 0xFF;
            _taxdate = new byte[length];
            bytes.get(_taxdate);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            RequestID.GetBytes(bytes);
            bytes.putInt(IntervalDays);
            bytes.putInt(CurrentInterval);
            bytes.put((byte)_startdate.length);
            bytes.put(_startdate);
            bytes.putInt(Balance);
            bytes.putInt(TotalCredits);
            bytes.putInt(TotalDebits);
            bytes.putInt(ObjectTaxCurrent);
            bytes.putInt(LightTaxCurrent);
            bytes.putInt(LandTaxCurrent);
            bytes.putInt(GroupTaxCurrent);
            bytes.putInt(ParcelDirFeeCurrent);
            bytes.putInt(ObjectTaxEstimate);
            bytes.putInt(LightTaxEstimate);
            bytes.putInt(LandTaxEstimate);
            bytes.putInt(GroupTaxEstimate);
            bytes.putInt(ParcelDirFeeEstimate);
            bytes.putInt(NonExemptMembers);
            bytes.put((byte)_lasttaxdate.length);
            bytes.put(_lasttaxdate);
            bytes.put((byte)_taxdate.length);
            bytes.put(_taxdate);
        }

        @Override
        public String toString()
        {
            String output = "-- MoneyData --\n";
            try {
                output += "RequestID: " + RequestID.toString() + "\n";
                output += "IntervalDays: " + Integer.toString(IntervalDays) + "\n";
                output += "CurrentInterval: " + Integer.toString(CurrentInterval) + "\n";
                output += Helpers.FieldToString(_startdate, "StartDate") + "\n";
                output += "Balance: " + Integer.toString(Balance) + "\n";
                output += "TotalCredits: " + Integer.toString(TotalCredits) + "\n";
                output += "TotalDebits: " + Integer.toString(TotalDebits) + "\n";
                output += "ObjectTaxCurrent: " + Integer.toString(ObjectTaxCurrent) + "\n";
                output += "LightTaxCurrent: " + Integer.toString(LightTaxCurrent) + "\n";
                output += "LandTaxCurrent: " + Integer.toString(LandTaxCurrent) + "\n";
                output += "GroupTaxCurrent: " + Integer.toString(GroupTaxCurrent) + "\n";
                output += "ParcelDirFeeCurrent: " + Integer.toString(ParcelDirFeeCurrent) + "\n";
                output += "ObjectTaxEstimate: " + Integer.toString(ObjectTaxEstimate) + "\n";
                output += "LightTaxEstimate: " + Integer.toString(LightTaxEstimate) + "\n";
                output += "LandTaxEstimate: " + Integer.toString(LandTaxEstimate) + "\n";
                output += "GroupTaxEstimate: " + Integer.toString(GroupTaxEstimate) + "\n";
                output += "ParcelDirFeeEstimate: " + Integer.toString(ParcelDirFeeEstimate) + "\n";
                output += "NonExemptMembers: " + Integer.toString(NonExemptMembers) + "\n";
                output += Helpers.FieldToString(_lasttaxdate, "LastTaxDate") + "\n";
                output += Helpers.FieldToString(_taxdate, "TaxDate") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public MoneyDataBlock createMoneyDataBlock() {
         return new MoneyDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupAccountSummaryReply; }
    public AgentDataBlock AgentData;
    public MoneyDataBlock MoneyData;

    public GroupAccountSummaryReplyPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)354);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        MoneyData = new MoneyDataBlock();
    }

    public GroupAccountSummaryReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
     }

    public GroupAccountSummaryReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        MoneyData = new MoneyDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += MoneyData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        MoneyData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupAccountSummaryReply ---\n";
        output += AgentData.toString() + "\n";
        output += MoneyData.toString() + "\n";
        return output;
    }
}
