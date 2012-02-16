/**
 * Copyright (c) 2007-2008, openmetaverse.org
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

import libomv.packets.AgentThrottlePacket;
import libomv.utils.Helpers;

// Throttles the network traffic for various different traffic types.
// Access this class through GridClient.Throttle
public class AgentThrottle
{
	// Maximum bits per second for resending unacknowledged packets
	public final float getResend()
	{
		return resend;
	}

	public final void setResend(float value)
	{
		if (value > 150000.0f)
		{
			resend = 150000.0f;
		}
		else if (value < 10000.0f)
		{
			resend = 10000.0f;
		}
		else
		{
			resend = value;
		}
	}

	// Maximum bits per second for LayerData terrain
	public final float getLand()
	{
		return land;
	}

	public final void setLand(float value)
	{
		if (value > 170000.0f)
		{
			land = 170000.0f;
		}
		else if (value < 0.0f) // We don't have control of these so allow
								// throttling to 0
		{
			land = 0.0f;
		}
		else
		{
			land = value;
		}
	}

	// Maximum bits per second for LayerData wind data
	public final float getWind()
	{
		return wind;
	}

	public final void setWind(float value)
	{
		if (value > 34000.0f)
		{
			wind = 34000.0f;
		}
		else if (value < 0.0f) // We don't have control of these so allow
								// throttling to 0
		{
			wind = 0.0f;
		}
		else
		{
			wind = value;
		}
	}

	// Maximum bits per second for LayerData clouds
	public final float getCloud()
	{
		return cloud;
	}

	public final void setCloud(float value)
	{
		if (value > 34000.0f)
		{
			cloud = 34000.0f;
		}
		else if (value < 0.0f) // We don't have control of these so allow
								// throttling to 0
		{
			cloud = 0.0f;
		}
		else
		{
			cloud = value;
		}
	}

	// Unknown, includes object data
	public final float getTask()
	{
		return task;
	}

	public final void setTask(float value)
	{
		if (value > 446000.0f)
		{
			task = 446000.0f;
		}
		else if (value < 4000.0f)
		{
			task = 4000.0f;
		}
		else
		{
			task = value;
		}
	}

	// Maximum bits per second for textures
	public final float getTexture()
	{
		return texture;
	}

	public final void setTexture(float value)
	{
		if (value > 446000.0f)
		{
			texture = 446000.0f;
		}
		else if (value < 4000.0f)
		{
			texture = 4000.0f;
		}
		else
		{
			texture = value;
		}
	}

	// Maximum bits per second for downloaded assets
	public final float getAsset()
	{
		return asset;
	}

	public final void setAsset(float value)
	{
		if (value > 220000.0f)
		{
			asset = 220000.0f;
		}
		else if (value < 10000.0f)
		{
			asset = 10000.0f;
		}
		else
		{
			asset = value;
		}
	}

	// Maximum bits per second the entire connection, divided up between
	// invidiual streams using default multipliers
	public final float getTotal()
	{
		return resend + land + wind + cloud + task + texture + asset;
	}

	public final void setTotal(float value)
	{
		// Sane initial values
		resend = (value * 0.1f);
		land = (value * 0.52f / 3f);
		wind = (value * 0.05f);
		cloud = (value * 0.05f);
		task = (value * 0.704f / 3f);
		texture = (value * 0.704f / 3f);
		asset = (value * 0.484f / 3f);
	}

	private GridClient Client;
	private float resend;
	private float land;
	private float wind;
	private float cloud;
	private float task;
	private float texture;
	private float asset;

	// Default constructor, uses a default high total of 1500 KBps (1536000)
	public AgentThrottle(GridClient client)
	{
		Client = client;
		setTotal(1536000.0f);
	}

	/**
	 * Constructor that decodes an existing AgentThrottle packet in to
	 * individual values
	 * 
	 * @param data
	 *            Reference to the throttle data in an AgentThrottle packet
	 * @param pos
	 *            Offset position to start reading at in the throttle data This
	 *            is generally not needed in clients as the server will never
	 *            send a throttle packet to the client
	 */
	public AgentThrottle(byte[] data, int pos)
	{
		resend = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		land = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		wind = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		cloud = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		task = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		texture = Helpers.BytesToFloatL(data, pos);
		pos += 4;
		asset = Helpers.BytesToFloatL(data, pos);
	}

	/**
	 * Send an AgentThrottle packet to the current server using the current
	 * values
	 * 
	 * @throws Exception
	 */
	public final void Set() throws Exception
	{
		Set(null);
	}

	/**
	 * Send an AgentThrottle packet to the specified server using the current
	 * values
	 * 
	 * @param simulator
	 *            the simulator to which to send the packet
	 * @throws Exception
	 */
	public final void Set(Simulator simulator) throws Exception
	{
		AgentThrottlePacket throttle = new AgentThrottlePacket();
		throttle.AgentData.AgentID = Client.Self.getAgentID();
		throttle.AgentData.SessionID = Client.Self.getSessionID();
		throttle.AgentData.CircuitCode = Client.Network.getCircuitCode();
		throttle.Throttle.GenCounter = 0;
		throttle.Throttle.setThrottles(this.ToBytes());

		simulator.SendPacket(throttle);
	}

	/**
	 * Convert the current throttle values to a byte array that can be put in an
	 * AgentThrottle packet
	 * 
	 * @return Byte array containing all the throttle values
	 */
	public final byte[] ToBytes()
	{
		byte[] data = new byte[7 * 4];
		int i = Helpers.FloatToBytesL(resend, data, 0);
		i += Helpers.FloatToBytesL(land, data, i);
		i += Helpers.FloatToBytesL(wind, data, i);
		i += Helpers.FloatToBytesL(cloud, data, i);
		i += Helpers.FloatToBytesL(task, data, i);
		i += Helpers.FloatToBytesL(texture, data, i);
		i += Helpers.FloatToBytesL(asset, data, i);
		return data;
	}
}
