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

public class LandStatReplyPacket extends Packet
{
    public class RequestDataBlock
    {
        public int ReportType = 0;
        public int RequestFlags = 0;
        public int TotalObjectCount = 0;

        public int getLength(){
            return 12;
        }

        public RequestDataBlock() { }
        public RequestDataBlock(ByteBuffer bytes)
        {
            ReportType = bytes.getInt();
            RequestFlags = bytes.getInt();
            TotalObjectCount = bytes.getInt();
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(ReportType);
            bytes.putInt(RequestFlags);
            bytes.putInt(TotalObjectCount);
        }

        @Override
        public String toString()
        {
            String output = "-- RequestData --\n";
            try {
                output += "ReportType: " + Integer.toString(ReportType) + "\n";
                output += "RequestFlags: " + Integer.toString(RequestFlags) + "\n";
                output += "TotalObjectCount: " + Integer.toString(TotalObjectCount) + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public RequestDataBlock createRequestDataBlock() {
         return new RequestDataBlock();
    }

    public class ReportDataBlock
    {
        public int TaskLocalID = 0;
        public UUID TaskID = null;
        public float LocationX = 0;
        public float LocationY = 0;
        public float LocationZ = 0;
        public float Score = 0;
        private byte[] _taskname;
        public byte[] getTaskName() {
            return _taskname;
        }

        public void setTaskName(byte[] value) throws Exception {
            if (value == null) {
                _taskname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _taskname = new byte[value.length];
                System.arraycopy(value, 0, _taskname, 0, value.length);
            }
        }

        private byte[] _ownername;
        public byte[] getOwnerName() {
            return _ownername;
        }

        public void setOwnerName(byte[] value) throws Exception {
            if (value == null) {
                _ownername = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _ownername = new byte[value.length];
                System.arraycopy(value, 0, _ownername, 0, value.length);
            }
        }


        public int getLength(){
            int length = 36;
            if (getTaskName() != null) { length += 1 + getTaskName().length; }
            if (getOwnerName() != null) { length += 1 + getOwnerName().length; }
            return length;
        }

        public ReportDataBlock() { }
        public ReportDataBlock(ByteBuffer bytes)
        {
            int length;
            TaskLocalID = bytes.getInt();
            TaskID = new UUID(bytes);
            LocationX = bytes.getFloat();
            LocationY = bytes.getFloat();
            LocationZ = bytes.getFloat();
            Score = bytes.getFloat();
            length = bytes.get() & 0xFF;
            _taskname = new byte[length];
            bytes.get(_taskname);
            length = bytes.get() & 0xFF;
            _ownername = new byte[length];
            bytes.get(_ownername);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.putInt(TaskLocalID);
            TaskID.GetBytes(bytes);
            bytes.putFloat(LocationX);
            bytes.putFloat(LocationY);
            bytes.putFloat(LocationZ);
            bytes.putFloat(Score);
            bytes.put((byte)_taskname.length);
            bytes.put(_taskname);
            bytes.put((byte)_ownername.length);
            bytes.put(_ownername);
        }

        @Override
        public String toString()
        {
            String output = "-- ReportData --\n";
            try {
                output += "TaskLocalID: " + Integer.toString(TaskLocalID) + "\n";
                output += "TaskID: " + TaskID.toString() + "\n";
                output += "LocationX: " + Float.toString(LocationX) + "\n";
                output += "LocationY: " + Float.toString(LocationY) + "\n";
                output += "LocationZ: " + Float.toString(LocationZ) + "\n";
                output += "Score: " + Float.toString(Score) + "\n";
                output += Helpers.FieldToString(_taskname, "TaskName") + "\n";
                output += Helpers.FieldToString(_ownername, "OwnerName") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ReportDataBlock createReportDataBlock() {
         return new ReportDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.LandStatReply; }
    public RequestDataBlock RequestData;
    public ReportDataBlock[] ReportData;

    public LandStatReplyPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)422);
        header.setReliable(true);
        RequestData = new RequestDataBlock();
        ReportData = new ReportDataBlock[0];
    }

    public LandStatReplyPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        RequestData = new RequestDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ReportData = new ReportDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ReportData[j] = new ReportDataBlock(bytes);
        }
     }

    public LandStatReplyPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        RequestData = new RequestDataBlock(bytes);
        int count = bytes.get() & 0xFF;
        ReportData = new ReportDataBlock[count];
        for (int j = 0; j < count; j++)
        {
            ReportData[j] = new ReportDataBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += RequestData.getLength();
        length++;
        for (int j = 0; j < ReportData.length; j++) { length += ReportData[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        RequestData.ToBytes(bytes);
        bytes.put((byte)ReportData.length);
        for (int j = 0; j < ReportData.length; j++) { ReportData[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- LandStatReply ---\n";
        output += RequestData.toString() + "\n";
        for (int j = 0; j < ReportData.length; j++)
        {
            output += ReportData[j].toString() + "\n";
        }
        return output;
    }
}
