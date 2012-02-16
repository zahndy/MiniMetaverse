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

public class InitiateDownloadPacket extends Packet
{
    public class FileDataBlock
    {
        private byte[] _simfilename;
        public byte[] getSimFilename() {
            return _simfilename;
        }

        public void setSimFilename(byte[] value) throws Exception {
            if (value == null) {
                _simfilename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _simfilename = new byte[value.length];
                System.arraycopy(value, 0, _simfilename, 0, value.length);
            }
        }

        private byte[] _viewerfilename;
        public byte[] getViewerFilename() {
            return _viewerfilename;
        }

        public void setViewerFilename(byte[] value) throws Exception {
            if (value == null) {
                _viewerfilename = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _viewerfilename = new byte[value.length];
                System.arraycopy(value, 0, _viewerfilename, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getSimFilename() != null) { length += 1 + getSimFilename().length; }
            if (getViewerFilename() != null) { length += 1 + getViewerFilename().length; }
            return length;
        }

        public FileDataBlock() { }
        public FileDataBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _simfilename = new byte[length];
            bytes.get(_simfilename);
            length = bytes.get() & 0xFF;
            _viewerfilename = new byte[length];
            bytes.get(_viewerfilename);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_simfilename.length);
            bytes.put(_simfilename);
            bytes.put((byte)_viewerfilename.length);
            bytes.put(_viewerfilename);
        }

        @Override
        public String toString()
        {
            String output = "-- FileData --\n";
            try {
                output += Helpers.FieldToString(_simfilename, "SimFilename") + "\n";
                output += Helpers.FieldToString(_viewerfilename, "ViewerFilename") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public FileDataBlock createFileDataBlock() {
         return new FileDataBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.InitiateDownload; }
    public UUID AgentID;
    public FileDataBlock FileData;

    public InitiateDownloadPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)403);
        header.setReliable(true);
        AgentID = new UUID();
        FileData = new FileDataBlock();
    }

    public InitiateDownloadPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AgentID = new UUID(bytes);
        FileData = new FileDataBlock(bytes);
     }

    public InitiateDownloadPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AgentID = new UUID(bytes);
        FileData = new FileDataBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += 16;
        length += FileData.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AgentID.GetBytes(bytes);
        FileData.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- InitiateDownload ---\n";
        output += "AgentID: " + AgentID.toString() + "\n";
        output += FileData.toString() + "\n";
        return output;
    }
}
