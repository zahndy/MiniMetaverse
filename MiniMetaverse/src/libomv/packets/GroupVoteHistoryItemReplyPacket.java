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

public class GroupVoteHistoryItemReplyPacket extends Packet
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

    public class TransactionDataBlock
    {
        public UUID TransactionID = null;
        public int TotalNumItems = 0;

        public int getLength(){
            return 20;
        }

        public TransactionDataBlock() { }
        public TransactionDataBlock(ByteBuffer bytes)
        {
            TransactionID = new UUID(bytes);
            TotalNumItems = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
            bytes.putInt(TotalNumItems);
        }

        @Override
        public String toString()
        {
            String output = "-- TransactionData --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "TotalNumItems: " + Integer.toString(TotalNumItems) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public TransactionDataBlock createTransactionDataBlock() {
         return new TransactionDataBlock();
    }

    public class HistoryItemDataBlock
    {
        public UUID VoteID = null;
        private byte[] _tersedateid;
        public byte[] getTerseDateID() {
            return _tersedateid;
        }

        public void setTerseDateID(byte[] value) throws Exception {
            if (value == null) {
                _tersedateid = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _tersedateid = new byte[value.length];
                System.arraycopy(value, 0, _tersedateid, 0, value.length);
            }
        }

        private byte[] _startdatetime;
        public byte[] getStartDateTime() {
            return _startdatetime;
        }

        public void setStartDateTime(byte[] value) throws Exception {
            if (value == null) {
                _startdatetime = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _startdatetime = new byte[value.length];
                System.arraycopy(value, 0, _startdatetime, 0, value.length);
            }
        }

        private byte[] _enddatetime;
        public byte[] getEndDateTime() {
            return _enddatetime;
        }

        public void setEndDateTime(byte[] value) throws Exception {
            if (value == null) {
                _enddatetime = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _enddatetime = new byte[value.length];
                System.arraycopy(value, 0, _enddatetime, 0, value.length);
            }
        }

        public UUID VoteInitiator = null;
        private byte[] _votetype;
        public byte[] getVoteType() {
            return _votetype;
        }

        public void setVoteType(byte[] value) throws Exception {
            if (value == null) {
                _votetype = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _votetype = new byte[value.length];
                System.arraycopy(value, 0, _votetype, 0, value.length);
            }
        }

        private byte[] _voteresult;
        public byte[] getVoteResult() {
            return _voteresult;
        }

        public void setVoteResult(byte[] value) throws Exception {
            if (value == null) {
                _voteresult = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _voteresult = new byte[value.length];
                System.arraycopy(value, 0, _voteresult, 0, value.length);
            }
        }

        public float Majority = 0;
        public int Quorum = 0;
        private byte[] _proposaltext;
        public byte[] getProposalText() {
            return _proposaltext;
        }

        public void setProposalText(byte[] value) throws Exception {
            if (value == null) {
                _proposaltext = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _proposaltext = new byte[value.length];
                System.arraycopy(value, 0, _proposaltext, 0, value.length);
            }
        }


        public int getLength(){
            int length = 40;
            if (getTerseDateID() != null) { length += 1 + getTerseDateID().length; }
            if (getStartDateTime() != null) { length += 1 + getStartDateTime().length; }
            if (getEndDateTime() != null) { length += 1 + getEndDateTime().length; }
            if (getVoteType() != null) { length += 1 + getVoteType().length; }
            if (getVoteResult() != null) { length += 1 + getVoteResult().length; }
            if (getProposalText() != null) { length += 2 + getProposalText().length; }
            return length;
        }

        public HistoryItemDataBlock() { }
        public HistoryItemDataBlock(ByteBuffer bytes)
        {
            int length;
            VoteID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _tersedateid = new byte[length];
            bytes.get(_tersedateid);
            length = bytes.get() & 0xFF;
            _startdatetime = new byte[length];
            bytes.get(_startdatetime);
            length = bytes.get() & 0xFF;
            _enddatetime = new byte[length];
            bytes.get(_enddatetime);
            VoteInitiator = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _votetype = new byte[length];
            bytes.get(_votetype);
            length = bytes.get() & 0xFF;
            _voteresult = new byte[length];
            bytes.get(_voteresult);
            Majority = bytes.getFloat();
            Quorum = bytes.getInt();
            length = bytes.getShort() & 0xFFFF;
            _proposaltext = new byte[length];
            bytes.get(_proposaltext);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            VoteID.GetBytes(bytes);
            bytes.put((byte)_tersedateid.length);
            bytes.put(_tersedateid);
            bytes.put((byte)_startdatetime.length);
            bytes.put(_startdatetime);
            bytes.put((byte)_enddatetime.length);
            bytes.put(_enddatetime);
            VoteInitiator.GetBytes(bytes);
            bytes.put((byte)_votetype.length);
            bytes.put(_votetype);
            bytes.put((byte)_voteresult.length);
            bytes.put(_voteresult);
            bytes.putFloat(Majority);
            bytes.putInt(Quorum);
            bytes.putShort((short)_proposaltext.length);
            bytes.put(_proposaltext);
        }

        @Override
        public String toString()
        {
            String output = "-- HistoryItemData --\n";
            try {
                output += "VoteID: " + VoteID.toString() + "\n";
                output += Helpers.FieldToString(_tersedateid, "TerseDateID") + "\n";
                output += Helpers.FieldToString(_startdatetime, "StartDateTime") + "\n";
                output += Helpers.FieldToString(_enddatetime, "EndDateTime") + "\n";
                output += "VoteInitiator: " + VoteInitiator.toString() + "\n";
                output += Helpers.FieldToString(_votetype, "VoteType") + "\n";
                output += Helpers.FieldToString(_voteresult, "VoteResult") + "\n";
                output += "Majority: " + Float.toString(Majority) + "\n";
                output += "Quorum: " + Integer.toString(Quorum) + "\n";
                output += Helpers.FieldToString(_proposaltext, "ProposalText") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public HistoryItemDataBlock createHistoryItemDataBlock() {
         return new HistoryItemDataBlock();
    }

    public class VoteItemBlock
    {
        public UUID CandidateID = null;
        private byte[] _votecast;
        public byte[] getVoteCast() {
            return _votecast;
        }

        public void setVoteCast(byte[] value) throws Exception {
            if (value == null) {
                _votecast = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _votecast = new byte[value.length];
                System.arraycopy(value, 0, _votecast, 0, value.length);
            }
        }

        public int NumVotes = 0;

        public int getLength(){
            int length = 20;
            if (getVoteCast() != null) { length += 1 + getVoteCast().length; }
            return length;
        }

        public VoteItemBlock() { }
        public VoteItemBlock(ByteBuffer bytes)
        {
            int length;
            CandidateID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _votecast = new byte[length];
            bytes.get(_votecast);
            NumVotes = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            CandidateID.GetBytes(bytes);
            bytes.put((byte)_votecast.length);
            bytes.put(_votecast);
            bytes.putInt(NumVotes);
        }

        @Override
        public String toString()
        {
            String output = "-- VoteItem --\n";
            try {
                output += "CandidateID: " + CandidateID.toString() + "\n";
                output += Helpers.FieldToString(_votecast, "VoteCast") + "\n";
                output += "NumVotes: " + Integer.toString(NumVotes) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public VoteItemBlock createVoteItemBlock() {
         return new VoteItemBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupVoteHistoryItemReply; }
    public AgentDataBlock AgentData;
    public TransactionDataBlock TransactionData;
    public HistoryItemDataBlock HistoryItemData;
    public VoteItemBlock[] VoteItem;

    public GroupVoteHistoryItemReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)362);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        TransactionData = new TransactionDataBlock();
        HistoryItemData = new HistoryItemDataBlock();
        VoteItem = new VoteItemBlock[0];
    }

    public GroupVoteHistoryItemReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        HistoryItemData = new HistoryItemDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        VoteItem = new VoteItemBlock[count];
        for (int j = 0; j < count; j++)
        {
            VoteItem[j] = new VoteItemBlock(bytes);
        }
     }

    public GroupVoteHistoryItemReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        HistoryItemData = new HistoryItemDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        VoteItem = new VoteItemBlock[count];
        for (int j = 0; j < count; j++)
        {
            VoteItem[j] = new VoteItemBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += TransactionData.getLength();
        length += HistoryItemData.getLength();
        length++;
        for (int j = 0; j < VoteItem.length; j++) { length += VoteItem[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentData.ToBytes(bytes);
        TransactionData.ToBytes(bytes);
        HistoryItemData.ToBytes(bytes);
        bytes.put((byte)VoteItem.length);
        for (int j = 0; j < VoteItem.length; j++) { VoteItem[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupVoteHistoryItemReply ---\n";
        output += AgentData.toString() + "\n";
        output += TransactionData.toString() + "\n";
        output += HistoryItemData.toString() + "\n";
        for (int j = 0; j < VoteItem.length; j++)
        {
            output += VoteItem[j].toString() + "\n";
        }
        return output;
    }
}
