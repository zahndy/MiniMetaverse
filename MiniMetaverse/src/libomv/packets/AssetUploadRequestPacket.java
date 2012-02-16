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

public class AssetUploadRequestPacket extends Packet
{
    public class AssetBlockBlock
    {
        public UUID TransactionID = null;
        public byte Type = 0;
        public boolean Tempfile = false;
        public boolean StoreLocal = false;
        private byte[] _assetdata;
        public byte[] getAssetData() {
            return _assetdata;
        }

        public void setAssetData(byte[] value) throws Exception {
            if (value == null) {
                _assetdata = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _assetdata = new byte[value.length];
                System.arraycopy(value, 0, _assetdata, 0, value.length);
            }
        }


        public int getLength(){
            int length = 19;
            if (getAssetData() != null) { length += 2 + getAssetData().length; }
            return length;
        }

        public AssetBlockBlock() { }
        public AssetBlockBlock(ByteBuffer bytes)
        {
            int length;
            TransactionID = new UUID(bytes);
            Type = bytes.get();
            Tempfile = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            StoreLocal = (bytes.get() != 0) ? (boolean)true : (boolean)false;
            length = bytes.getShort() & 0xFFFF;
            _assetdata = new byte[length];
            bytes.get(_assetdata);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            TransactionID.GetBytes(bytes);
            bytes.put(Type);
            bytes.put((byte)((Tempfile) ? 1 : 0));
            bytes.put((byte)((StoreLocal) ? 1 : 0));
            bytes.putShort((short)_assetdata.length);
            bytes.put(_assetdata);
        }

        @Override
        public String toString()
        {
            String output = "-- AssetBlock --\n";
            try {
                output += "TransactionID: " + TransactionID.toString() + "\n";
                output += "Type: " + Byte.toString(Type) + "\n";
                output += "Tempfile: " + Boolean.toString(Tempfile) + "\n";
                output += "StoreLocal: " + Boolean.toString(StoreLocal) + "\n";
                output += Helpers.FieldToString(_assetdata, "AssetData") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public AssetBlockBlock createAssetBlockBlock() {
         return new AssetBlockBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.AssetUploadRequest; }
    public AssetBlockBlock AssetBlock;

    public AssetUploadRequestPacket()
    {
        hasVariableBlocks = false;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)333);
        header.setReliable(true);
        AssetBlock = new AssetBlockBlock();
    }

    public AssetUploadRequestPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        AssetBlock = new AssetBlockBlock(bytes);
     }

    public AssetUploadRequestPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        AssetBlock = new AssetBlockBlock(bytes);
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += AssetBlock.getLength();
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        AssetBlock.ToBytes(bytes);
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- AssetUploadRequest ---\n";
        output += AssetBlock.toString() + "\n";
        return output;
    }
}
