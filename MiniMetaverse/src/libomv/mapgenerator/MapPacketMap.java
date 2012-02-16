/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
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
package libomv.mapgenerator;

import java.util.Hashtable;
import java.util.Vector;

public class MapPacketMap
{
	public Vector<MapPacket> mapPackets;

	public IntHashMap<Integer, MapPacket> commandMapPacket;

	public Hashtable<String, MapPacket> nameMapPacket;

	public MapPacketMap(int size)
	{
		mapPackets = new Vector<MapPacket>(size);
		commandMapPacket = new IntHashMap<Integer, MapPacket>(size);
		nameMapPacket = new Hashtable<String, MapPacket>(size);
	}

	public MapPacket getMapPacketByName(String name)
	{
		return nameMapPacket.get(name);
	}

	public MapPacket getMapPacketByCommand(int command)
	{
		return commandMapPacket.get(command);
	}

	public void addPacket(int id, MapPacket packet)
	{
		mapPackets.addElement(packet);
		commandMapPacket.put(id, packet);
		nameMapPacket.put(packet.Name, packet);
	}
}
