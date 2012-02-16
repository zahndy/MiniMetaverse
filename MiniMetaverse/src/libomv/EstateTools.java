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
package libomv;

import libomv.packets.EstateOwnerMessagePacket;
import libomv.types.UUID;
import libomv.utils.Helpers;

public class EstateTools
{
	private GridClient Client;

	// <param name="client"></param>
	public EstateTools(GridClient client)
	{
		Client = client;
	}

	// <param name="prey"></param>
	public void KickUser(UUID prey) throws Exception
	{
		EstateOwnerMessagePacket estate = new EstateOwnerMessagePacket();
		estate.AgentData.AgentID = Client.Self.getAgentID();
		estate.AgentData.SessionID = Client.Self.getSessionID();
		estate.MethodData.Invoice = UUID.GenerateUUID();
		estate.MethodData.setMethod(Helpers.StringToField("kick"));
		estate.ParamList = new EstateOwnerMessagePacket.ParamListBlock[2];
		estate.ParamList[0].setParameter(Helpers.StringToField(Client.Self.getAgentID().toString()));
		estate.ParamList[1].setParameter(Helpers.StringToField(prey.toString()));

		Client.Network.SendPacket(estate);
	}

	// <param name="prey"></param>
	public void BanUser(UUID prey)
	{
		// FIXME:
		// Client.Network.SendPacket(Packets.Estate.EstateBan(Client.Protocol,Client.Avatar.ID,Client.Network.SessionID,prey));
	}

	// <param name="prey"></param>
	public void UnBanUser(UUID prey)
	{
		// FIXME:
		// Client.Network.SendPacket(Packets.Estate.EstateUnBan(Client.Protocol,Client.Avatar.ID,Client.Network.SessionID,prey));
	}

	// <param name="prey"></param>
	public void TeleportHomeUser(UUID prey)
	{
		// FIXME:
		// Client.Network.SendPacket(Packets.Estate.EstateTeleportUser(Client.Protocol,Client.Avatar.ID,Client.Network.SessionID,prey));
	}
}
