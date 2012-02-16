/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.capabilities;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;

import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSD.OSDType;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.packets.Packet;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class EventQueue extends CapsClient
{
	public final int REQUEST_TIMEOUT = 1000 * 120;

	private URI address;
	private static Random random = new Random();
	private int errorCount;
	private boolean Done;

	private Future<OSD> Request;

	public boolean getRunning()
	{
		synchronized (random)
		{
			return Request != null;
		}
	}

	private Simulator Simulator;

	public EventQueue(Simulator sim, URI eventQueueLocation) throws IOReactorException
	{
		super();
		this.address = eventQueueLocation;
		this.Simulator = sim;
	}

	public void start()
	{
		Done = false;
		// Startup the event queue for the first time
		resume(true, 0, null);
	}

	public void stop(boolean immediate)
	{
		synchronized (random)
		{
			Done = true;
			if (Request != null)
				Request.cancel(immediate);
		}
	}

	private void resume(boolean first, int ack, OSDArray events)
	{
		if (errorCount > 0)
		{
			try
			{
				Thread.sleep(random.nextInt(500 + (int) Math.pow(2, errorCount)));
			}
			catch (InterruptedException e)
			{
			}
		}

		OSDMap osdRequest = new OSDMap();
		osdRequest.put("ack", (ack != 0) ? OSD.FromInteger(ack) : new OSD());
		osdRequest.put("done", OSD.FromBoolean(Done));

		try
		{
			byte[] postData = osdRequest.serializeToBytes(OSD.OSDFormat.Xml);
			// Start or resume the connection
			setResultCallback(new EventClientCallback(first));
			Request = executeHttpPost(address, postData, "application/xml", REQUEST_TIMEOUT);
		}
		catch (IOException e)
		{
		}

		synchronized (random)
		{
			if (Done)
			{
				Request = null;
				Logger.DebugLog("Sent event queue shutdown message");
			}
		}

		// #region Handle incoming events
		if (events != null && events.size() > 0)
		{
			// Fire callbacks for each event received
			while (events.listIterator().hasNext())
			{
				OSDMap evt = (OSDMap) events.listIterator().next();
				OSD osd = evt.get("body");
				if (osd.getType().equals(OSDType.Map))
				{
					OSDMap body = (OSDMap) osd;
					String name = evt.get("message").AsString();
					CapsEventType capsKey = CapsEventType.valueOf(name);

					IMessage message = Simulator.getClient().Messages.DecodeEvent(capsKey, body);
					if (message != null)
					{
						Simulator.getClient().Network.DistributeCaps(Simulator, message);

						// #region Stats Tracking
						if (Simulator.getClient().Settings.TRACK_UTILIZATION)
						{
							/* TODO add Stats support to Client manager */
							// Simulator.getClient().Stats.Update(eventName,
							// libomv.Stats.Type.Message, 0,
							// body.ToString().Length);
						}
					}
					else
					{
						Logger.Log("No Message handler exists for event " + name
								+ ". Unable to decode. Will try Generic Handler next", LogLevel.Warning,
								Simulator.getClient());
						Logger.Log("Please report this information to http://jira.openmv.org/: \n" + body,
								LogLevel.Debug, Simulator.getClient());

						// try generic decoder next which takes a caps event and
						// tries to match it to an existing packet
						Packet packet = CapsToPacket.BuildPacket(name, body);
						if (packet != null)
						{
							Logger.DebugLog("Serializing " + packet.getType() + " capability with generic handler",
									Simulator.getClient());
							Simulator.getClient().Network.DistributePacket(Simulator, packet);
						}
						else
						{
							Logger.Log("No Packet or Message handler exists for " + name, LogLevel.Warning,
									Simulator.getClient());
						}
					}
				}
			}
		}
	}

	public class EventClientCallback implements FutureCallback<OSD>
	{
		private final boolean first;

		public EventClientCallback(boolean first)
		{
			this.first = first;
		}

		@Override
		public void completed(OSD result)
		{
			if (first)
				Simulator.getClient().Network.RaiseConnectedEvent(Simulator);

			if (result != null && result instanceof OSDMap)
			{
				errorCount = 0;
				OSDMap map = (OSDMap) result;
				OSDArray events = (OSDArray) ((map.get("events") instanceof OSDArray) ? map.get("events") : null);
				int ack = map.get("id").AsInteger();
				resume(false, ack, events);
			}
			else
			{
				++errorCount;
				Logger.Log("Got an unparseable response from the event queue!", LogLevel.Warning, Simulator.getClient());
				resume(false, 0, null);
			}
		}

		@Override
		public void failed(Exception ex)
		{
			if (ex instanceof HttpResponseException)
			{
				int status = ((HttpResponseException) ex).getStatusCode();
				if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE)
				{
					synchronized (random)
					{
						Request = null;
					}
					Logger.Log(String.format("Closing event queue at %s due to missing caps URI", address),
							LogLevel.Info, Simulator.getClient());
					return;
				}
				else if (status == HttpStatus.SC_BAD_GATEWAY)
				{
					// This is not good (server) protocol design, but it's
					// normal.
					// The EventQueue server is a proxy that connects to a Squid
					// cache which will time out periodically. The EventQueue
					// server
					// interprets this as a generic error and returns a 502 to
					// us
					// that we ignore
				}
				else
				{
					++errorCount;

					// Try to log a meaningful error message
					if (status != HttpStatus.SC_OK)
					{
						Logger.Log(String.format("Unrecognized caps connection problem from %s: %d",
								address, status), LogLevel.Warning, Simulator.getClient());
					}
					else if (ex.getCause() != null)
					{
						Logger.Log(String.format("Unrecognized internal caps exception from %s: %s",
								address, ex.getCause().getMessage()), LogLevel.Warning, Simulator.getClient());
					}
					else
					{
						Logger.Log(
								String.format("Unrecognized caps exception from %s: %s", address,
										ex.getMessage()), LogLevel.Warning, Simulator.getClient());
					}
				}
			}
			else
			{
				++errorCount;

				Logger.Log("No response from the event queue but no reported error either", LogLevel.Warning,
						Simulator.getClient());
			}
			resume(false, 0, null);
		}

		@Override
		public void cancelled()
		{
			resume(false, 0, null);
		}
	}
}
