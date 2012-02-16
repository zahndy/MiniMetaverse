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
import libomv.types.Vector3;
import libomv.types.OverflowException;

public class UserReportInternalPacket extends Packet
{
    public class ReportDataBlock
    {
        public byte ReportType = 0;
        public byte Category = 0;
        public UUID ReporterID = null;
        public Vector3 ViewerPosition = null;
        public Vector3 AgentPosition = null;
        public UUID ScreenshotID = null;
        public UUID ObjectID = null;
        public UUID OwnerID = null;
        public UUID LastOwnerID = null;
        public UUID CreatorID = null;
        public UUID RegionID = null;
        public UUID AbuserID = null;
        private byte[] _abuseregionname;
        public byte[] getAbuseRegionName() {
            return _abuseregionname;
        }

        public void setAbuseRegionName(byte[] value) throws Exception {
            if (value == null) {
                _abuseregionname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _abuseregionname = new byte[value.length];
                System.arraycopy(value, 0, _abuseregionname, 0, value.length);
            }
        }

        public UUID AbuseRegionID = null;
        private byte[] _summary;
        public byte[] getSummary() {
            return _summary;
        }

        public void setSummary(byte[] value) throws Exception {
            if (value == null) {
                _summary = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _summary = new byte[value.length];
                System.arraycopy(value, 0, _summary, 0, value.length);
            }
        }

        private byte[] _details;
        public byte[] getDetails() {
            return _details;
        }

        public void setDetails(byte[] value) throws Exception {
            if (value == null) {
                _details = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _details = new byte[value.length];
                System.arraycopy(value, 0, _details, 0, value.length);
            }
        }

        private byte[] _versionstring;
        public byte[] getVersionString() {
            return _versionstring;
        }

        public void setVersionString(byte[] value) throws Exception {
            if (value == null) {
                _versionstring = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _versionstring = new byte[value.length];
                System.arraycopy(value, 0, _versionstring, 0, value.length);
            }
        }


        public int getLength(){
            int length = 170;
            if (getAbuseRegionName() != null) { length += 1 + getAbuseRegionName().length; }
            if (getSummary() != null) { length += 1 + getSummary().length; }
            if (getDetails() != null) { length += 2 + getDetails().length; }
            if (getVersionString() != null) { length += 1 + getVersionString().length; }
            return length;
        }

        public ReportDataBlock() { }
        public ReportDataBlock(ByteBuffer bytes)
        {
            int length;
            ReportType = bytes.get();
            Category = bytes.get();
            ReporterID = new UUID(bytes);
            ViewerPosition = new Vector3(bytes);
            AgentPosition = new Vector3(bytes);
            ScreenshotID = new UUID(bytes);
            ObjectID = new UUID(bytes);
            OwnerID = new UUID(bytes);
            LastOwnerID = new UUID(bytes);
            CreatorID = new UUID(bytes);
            RegionID = new UUID(bytes);
            AbuserID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _abuseregionname = new byte[length];
            bytes.get(_abuseregionname);
            AbuseRegionID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _summary = new byte[length];
            bytes.get(_summary);
            length = bytes.getShort() & 0xFFFF;
            _details = new byte[length];
            bytes.get(_details);
            length = bytes.get() & 0xFF;
            _versionstring = new byte[length];
            bytes.get(_versionstring);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put(ReportType);
            bytes.put(Category);
            ReporterID.GetBytes(bytes);
            ViewerPosition.GetBytes(bytes);
            AgentPosition.GetBytes(bytes);
            ScreenshotID.GetBytes(bytes);
            ObjectID.GetBytes(bytes);
            OwnerID.GetBytes(bytes);
            LastOwnerID.GetBytes(bytes);
            CreatorID.GetBytes(bytes);
            RegionID.GetBytes(bytes);
            AbuserID.GetBytes(bytes);
            bytes.put((byte)_abuseregionname.length);
            bytes.put(_abuseregionname);
            AbuseRegionID.GetBytes(bytes);
            bytes.put((byte)_summary.length);
            bytes.put(_summary);
            bytes.putShort((short)_details.length);
            bytes.put(_details);
            bytes.put((byte)_versionstring.length);
            bytes.put(_versionstring);
        }

        @Override
        public String toString()
        {
            String output = "-- ReportData --\n";
            try {
                output += "ReportType: " + Byte.toString(ReportType) + "\n";
                output += "Category: " + Byte.toString(Category) + "\n";
                output += "ReporterID: " + ReporterID.toString() + "\n";
                output += "ViewerPosition: " + ViewerPosition.toString() + "\n";
                output += "AgentPosition: " + AgentPosition.toString() + "\n";
                output += "ScreenshotID: " + ScreenshotID.toString() + "\n";
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += "OwnerID: " + OwnerID.toString() + "\n";
                output += "LastOwnerID: " + LastOwnerID.toString() + "\n";
                output += "CreatorID: " + CreatorID.toString() + "\n";
                output += "RegionID: " + RegionID.toString() + "\n";
                output += "AbuserID: " + AbuserID.toString() + "\n";
                output += Helpers.FieldToString(_abuseregionname, "AbuseRegionName") + "\n";
                output += "AbuseRegionID: " + AbuseRegionID.toString() + "\n";
                output += Helpers.FieldToString(_summary, "Summary") + "\n";
                output += Helpers.FieldToString(_details, "Details") + "\n";
                output += Helpers.FieldToString(_versionstring, "VersionString") + "\n";
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
    public PacketType getType() { return PacketType.UserReportInternal; }
    public ReportDataBlock ReportData;

    public UserReportInternalPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)21);
        header.setReliable(true);
        ReportData = new ReportDataBlock();
    }

    public UserReportInternalPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        ReportData = new ReportDataBlock(bytes);
     }

    public UserReportInternalPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        ReportData = new ReportDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += ReportData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        ReportData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- UserReportInternal ---\n";
        output += ReportData.toString() + "\n";
        return output;
    }
}
