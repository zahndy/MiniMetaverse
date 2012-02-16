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

public class ScriptDialogPacket extends Packet
{
    public class DataBlock
    {
        public UUID ObjectID = null;
        private byte[] _firstname;
        public byte[] getFirstName() {
            return _firstname;
        }

        public void setFirstName(byte[] value) throws Exception {
            if (value == null) {
                _firstname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _firstname = new byte[value.length];
                System.arraycopy(value, 0, _firstname, 0, value.length);
            }
        }

        private byte[] _lastname;
        public byte[] getLastName() {
            return _lastname;
        }

        public void setLastName(byte[] value) throws Exception {
            if (value == null) {
                _lastname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _lastname = new byte[value.length];
                System.arraycopy(value, 0, _lastname, 0, value.length);
            }
        }

        private byte[] _objectname;
        public byte[] getObjectName() {
            return _objectname;
        }

        public void setObjectName(byte[] value) throws Exception {
            if (value == null) {
                _objectname = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _objectname = new byte[value.length];
                System.arraycopy(value, 0, _objectname, 0, value.length);
            }
        }

        private byte[] _message;
        public byte[] getMessage() {
            return _message;
        }

        public void setMessage(byte[] value) throws Exception {
            if (value == null) {
                _message = null;
            }
            else if (value.length > 1024) {
                throw new OverflowException("Value exceeds 1024 characters");
            }
            else {
                _message = new byte[value.length];
                System.arraycopy(value, 0, _message, 0, value.length);
            }
        }

        public int ChatChannel = 0;
        public UUID ImageID = null;

        public int getLength(){
            int length = 36;
            if (getFirstName() != null) { length += 1 + getFirstName().length; }
            if (getLastName() != null) { length += 1 + getLastName().length; }
            if (getObjectName() != null) { length += 1 + getObjectName().length; }
            if (getMessage() != null) { length += 2 + getMessage().length; }
            return length;
        }

        public DataBlock() { }
        public DataBlock(ByteBuffer bytes)
        {
            int length;
            ObjectID = new UUID(bytes);
            length = bytes.get() & 0xFF;
            _firstname = new byte[length];
            bytes.get(_firstname);
            length = bytes.get() & 0xFF;
            _lastname = new byte[length];
            bytes.get(_lastname);
            length = bytes.get() & 0xFF;
            _objectname = new byte[length];
            bytes.get(_objectname);
            length = bytes.getShort() & 0xFFFF;
            _message = new byte[length];
            bytes.get(_message);
            ChatChannel = bytes.getInt();
            ImageID = new UUID(bytes);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            ObjectID.GetBytes(bytes);
            bytes.put((byte)_firstname.length);
            bytes.put(_firstname);
            bytes.put((byte)_lastname.length);
            bytes.put(_lastname);
            bytes.put((byte)_objectname.length);
            bytes.put(_objectname);
            bytes.putShort((short)_message.length);
            bytes.put(_message);
            bytes.putInt(ChatChannel);
            ImageID.GetBytes(bytes);
        }

        @Override
        public String toString()
        {
            String output = "-- Data --\n";
            try {
                output += "ObjectID: " + ObjectID.toString() + "\n";
                output += Helpers.FieldToString(_firstname, "FirstName") + "\n";
                output += Helpers.FieldToString(_lastname, "LastName") + "\n";
                output += Helpers.FieldToString(_objectname, "ObjectName") + "\n";
                output += Helpers.FieldToString(_message, "Message") + "\n";
                output += "ChatChannel: " + Integer.toString(ChatChannel) + "\n";
                output += "ImageID: " + ImageID.toString() + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public DataBlock createDataBlock() {
         return new DataBlock();
    }

    public class ButtonsBlock
    {
        private byte[] _buttonlabel;
        public byte[] getButtonLabel() {
            return _buttonlabel;
        }

        public void setButtonLabel(byte[] value) throws Exception {
            if (value == null) {
                _buttonlabel = null;
            }
            else if (value.length > 255) {
                throw new OverflowException("Value exceeds 255 characters");
            }
            else {
                _buttonlabel = new byte[value.length];
                System.arraycopy(value, 0, _buttonlabel, 0, value.length);
            }
        }


        public int getLength(){
            int length = 0;
            if (getButtonLabel() != null) { length += 1 + getButtonLabel().length; }
            return length;
        }

        public ButtonsBlock() { }
        public ButtonsBlock(ByteBuffer bytes)
        {
            int length;
            length = bytes.get() & 0xFF;
            _buttonlabel = new byte[length];
            bytes.get(_buttonlabel);
        }

        public void ToBytes(ByteBuffer bytes) throws Exception
        {
            bytes.put((byte)_buttonlabel.length);
            bytes.put(_buttonlabel);
        }

        @Override
        public String toString()
        {
            String output = "-- Buttons --\n";
            try {
                output += Helpers.FieldToString(_buttonlabel, "ButtonLabel") + "\n";
                output = output.trim();
            }
            catch(Exception e){}
            return output;
        }
    }

    public ButtonsBlock createButtonsBlock() {
         return new ButtonsBlock();
    }

    private PacketHeader header;
    @Override
    public PacketHeader getHeader() { return header; }
    @Override
    public void setHeader(PacketHeader value) { header = value; }
    @Override
    public PacketType getType() { return PacketType.ScriptDialog; }
    public DataBlock Data;
    public ButtonsBlock[] Buttons;

    public ScriptDialogPacket()
    {
        hasVariableBlocks = true;
        header = new PacketHeader(PacketFrequency.Low);
        header.setID((short)190);
        header.setReliable(true);
        Data = new DataBlock();
        Buttons = new ButtonsBlock[0];
    }

    public ScriptDialogPacket(ByteBuffer bytes) throws Exception
    {
        header = new PacketHeader(bytes, PacketFrequency.Low);
        Data = new DataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Buttons = new ButtonsBlock[count];
        for (int j = 0; j < count; j++)
        {
            Buttons[j] = new ButtonsBlock(bytes);
        }
     }

    public ScriptDialogPacket(PacketHeader head, ByteBuffer bytes)
    {
        header = head;
        Data = new DataBlock(bytes);
        int count = bytes.get() & 0xFF;
        Buttons = new ButtonsBlock[count];
        for (int j = 0; j < count; j++)
        {
            Buttons[j] = new ButtonsBlock(bytes);
        }
    }

    @Override
    public int getLength()
    {
        int length = header.getLength();
        length += Data.getLength();
        length++;
        for (int j = 0; j < Buttons.length; j++) { length += Buttons[j].getLength(); }
        return length;
    }

    @Override
    public ByteBuffer ToBytes() throws Exception
    {
        ByteBuffer bytes = ByteBuffer.allocate(getLength());
        header.ToBytes(bytes);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        Data.ToBytes(bytes);
        bytes.put((byte)Buttons.length);
        for (int j = 0; j < Buttons.length; j++) { Buttons[j].ToBytes(bytes); }
        return bytes;
    }

    @Override
    public String toString()
    {
        String output = "--- ScriptDialog ---\n";
        output += Data.toString() + "\n";
        for (int j = 0; j < Buttons.length; j++)
        {
            output += Buttons[j].toString() + "\n";
        }
        return output;
    }
}
