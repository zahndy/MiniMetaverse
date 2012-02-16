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

public class GroupActiveProposalItemReplyPacket extends Packet
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

    public class ProposalDataBlock
    {
        public UUID VoteID = null;
        public UUID VoteInitiator = null;
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

        public boolean AlreadyVoted = false;
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
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _proposaltext = new byte[value.length];
                System.arraycopy(value, 0, _proposaltext, 0, value.length);
            }
        }


        public int getLength(){
            int length = 41;
            if (getTerseDateID() != null) { length += 1 + getTerseDateID().length; }
            if (getStartDateTime() != null) { length += 1 + getStartDateTime().length; }
            if (getEndDateTime() != null) { length += 1 + getEndDateTime().length; }
            if (getVoteCast() != null) { length += 1 + getVoteCast().length; }
            if (getProposalText() != null) { length += 1 + getProposalText().length; }
            return length;
        }

        public ProposalDataBlock() { }
        public ProposalDataBlock(ByteBuffer bytes)
        {
            int length;
            VoteID = new UUID(bytes);
            VoteInitiator = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _tersedateid = new byte[length];
            bytes.get(_tersedateid);
            length = bytes.get() & 0xFF;
            _startdatetime = new byte[length];
            bytes.get(_startdatetime);
            length = bytes.get() & 0xFF;
            _enddatetime = new byte[length];
            bytes.get(_enddatetime);
            AlreadyVoted = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = bytes.get() & 0xFF;
            _votecast = new byte[length];
            bytes.get(_votecast);
            Majority = bytes.getFloat();
            Quorum = bytes.getInt();
            length = bytes.get() & 0xFF;
            _proposaltext = new byte[length];
            bytes.get(_proposaltext);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            VoteID.GetBytes(bytes);
            VoteInitiator.GetBytes(bytes);
            bytes.put((byte)_tersedateid.length);
            bytes.put(_tersedateid);
            bytes.put((byte)_startdatetime.length);
            bytes.put(_startdatetime);
            bytes.put((byte)_enddatetime.length);
            bytes.put(_enddatetime);
            bytes.put((byte)((AlreadyVoted) ? 1 : 0));
            bytes.put((byte)_votecast.length);
            bytes.put(_votecast);
            bytes.putFloat(Majority);
            bytes.putInt(Quorum);
            bytes.put((byte)_proposaltext.length);
            bytes.put(_proposaltext);
        }

        @Override
        public String toString()
        {
            String output = "-- ProposalData --\n";
            try {
                output += "VoteID: " + VoteID.toString() + "\n";
                output += "VoteInitiator: " + VoteInitiator.toString() + "\n";
                output += Helpers.FieldToString(_tersedateid, "TerseDateID") + "\n";
                output += Helpers.FieldToString(_startdatetime, "StartDateTime") + "\n";
                output += Helpers.FieldToString(_enddatetime, "EndDateTime") + "\n";
                output += "AlreadyVoted: " + Boolean.toString(AlreadyVoted) + "\n";
                output += Helpers.FieldToString(_votecast, "VoteCast") + "\n";
                output += "Majority: " + Float.toString(Majority) + "\n";
                output += "Quorum: " + Integer.toString(Quorum) + "\n";
                output += Helpers.FieldToString(_proposaltext, "ProposalText") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ProposalDataBlock createProposalDataBlock() {
         return new ProposalDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.GroupActiveProposalItemReply; }
    public AgentDataBlock AgentData;
    public TransactionDataBlock TransactionData;
    public ProposalDataBlock[] ProposalData;

    public GroupActiveProposalItemReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)360);
        header.setReliable(true);
        AgentData = new AgentDataBlock();
        TransactionData = new TransactionDataBlock();
        ProposalData = new ProposalDataBlock[0];
    }

    public GroupActiveProposalItemReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ProposalData = new ProposalDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ProposalData[j] = new ProposalDataBlock(bytes);
        }
     }

    public GroupActiveProposalItemReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentData = new AgentDataBlock(bytes);
        TransactionData = new TransactionDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ProposalData = new ProposalDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ProposalData[j] = new ProposalDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AgentData.getLength();
        length += TransactionData.getLength();
        length++;
        for (int j = 0; j < ProposalData.length; j++) { length += ProposalData[j].getLength(); }
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
        bytes.put((byte)ProposalData.length);
        for (int j = 0; j < ProposalData.length; j++) { ProposalData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- GroupActiveProposalItemReply ---\n";
        output += AgentData.toString() + "\n";
        output += TransactionData.toString() + "\n";
        for (int j = 0; j < ProposalData.length; j++)
        {
            output += ProposalData[j].toString() + "\n";
        }
        return output;
    }
}
