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

import java.net.URI;
import java.util.Hashtable;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.nio.concurrent.FutureCallback;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsClient;
import libomv.capabilities.EventQueue;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * Capabilities is the name of the bi-directional HTTP REST protocol used to
 * communicate non real-time transactions such as teleporting or group messaging
 */
public class CapsManager
{
	/* Reference to the simulator this system is connected to */
	private Simulator _Simulator;

	private String _SeedCapsURI;
	private Hashtable<String, URI> _Capabilities = new Hashtable<String, URI>();

	private Future<OSD> _SeedRequest = null;
	private EventQueue _EventQueue = null;

	/* Capabilities URI this system was initialized with */
	public final String getSeedCapsURI()
	{
		return _SeedCapsURI;
	}

	/*
	 * Whether the capabilities event queue is connected and listening for
	 * incoming events
	 */
	public final boolean getIsEventQueueRunning()
	{
		if (_EventQueue != null)
		{
			return _EventQueue.getRunning();
		}
		return false;
	}

	/**
	 * Default constructor
	 * 
	 * @param simulator
	 * @param seedcaps
	 */
	public CapsManager(Simulator simulator, String seedcaps)
	{
		_Simulator = simulator;
		_SeedCapsURI = seedcaps;
		MakeSeedRequest();
	}

	public final void Disconnect(boolean immediate)
	{
		Logger.Log(
				String.format("Caps system for " + _Simulator.Name + " is "
						+ (immediate ? "aborting" : "disconnecting")), LogLevel.Info, _Simulator.getClient());

		if (_SeedRequest != null)
		{
			_SeedRequest.cancel(immediate);
		}

		if (_EventQueue != null)
		{
			_EventQueue.stop(immediate);
		}
	}

	/**
	 * Request the URI of a named capability
	 * 
	 * @param capability
	 *            Name of the capability to request
	 * @return The URI of the requested capability, or String. Empty if the
	 *         capability does not exist
	 */
	public final URI CapabilityURI(String capability)
	{
		return _Capabilities.get(capability);
	}

	private void MakeSeedRequest()
	{
		if (_Simulator == null || !_Simulator.getClient().Network.getConnected())
		{
			return;
		}

		// Create a request list
		OSDArray req = new OSDArray();
		// This list can be updated by using the following command to obtain a
		// current list of capabilities the official linden viewer supports:
		// wget -q -O -
		// http://svn.secondlife.com/svn/linden/branches/2010/viewer-external/indra/newview/llviewerregion.cpp
		// | grep 'capabilityNames.append' | sed 's/^[
		// \t]*//;s/capabilityNames.append("/req.Add("/'
		req.add(OSD.FromString("AttachmentResources"));
		req.add(OSD.FromString("AvatarPickerSearch"));
		req.add(OSD.FromString("ChatSessionRequest"));
		req.add(OSD.FromString("CopyInventoryFromNotecard"));
		req.add(OSD.FromString("DispatchRegionInfo"));
		req.add(OSD.FromString("EstateChangeInfo"));
		req.add(OSD.FromString("EventQueueGet"));
		req.add(OSD.FromString("FetchInventory"));
		req.add(OSD.FromString("FetchLib"));
		req.add(OSD.FromString("FetchLibDescendents"));
		req.add(OSD.FromString("GetDisplayNames"));
		req.add(OSD.FromString("GetTexture"));
		req.add(OSD.FromString("GroupProposalBallot"));
		req.add(OSD.FromString("HomeLocation"));
		req.add(OSD.FromString("LandResources"));
		req.add(OSD.FromString("MapLayer"));
		req.add(OSD.FromString("MapLayerGod"));
		req.add(OSD.FromString("NewFileAgentInventory"));
		req.add(OSD.FromString("ObjectMedia"));
		req.add(OSD.FromString("ObjectMediaNavigate"));
		req.add(OSD.FromString("ParcelPropertiesUpdate"));
		req.add(OSD.FromString("ParcelMediaURLFilterList"));
		req.add(OSD.FromString("ParcelNavigateMedia"));
		req.add(OSD.FromString("ParcelVoiceInfoRequest"));
		req.add(OSD.FromString("ProductInfoRequest"));
		req.add(OSD.FromString("ProvisionVoiceAccountRequest"));
		req.add(OSD.FromString("RemoteParcelRequest"));
		req.add(OSD.FromString("RequestTextureDownload"));
		req.add(OSD.FromString("SearchStatRequest"));
		req.add(OSD.FromString("SearchStatTracking"));
		req.add(OSD.FromString("SendPostcard"));
		req.add(OSD.FromString("SendUserReport"));
		req.add(OSD.FromString("SendUserReportWithScreenshot"));
		req.add(OSD.FromString("ServerReleaseNotes"));
		req.add(OSD.FromString("SimConsole"));
		req.add(OSD.FromString("SimulatorFeatures"));
		req.add(OSD.FromString("SetDisplayName"));
		req.add(OSD.FromString("SimConsoleAsync"));
		req.add(OSD.FromString("StartGroupProposal"));
		req.add(OSD.FromString("TextureStats"));
		req.add(OSD.FromString("UntrustedSimulatorMessage"));
		req.add(OSD.FromString("UpdateAgentInformation"));
		req.add(OSD.FromString("UpdateAgentLanguage"));
		req.add(OSD.FromString("UpdateGestureAgentInventory"));
		req.add(OSD.FromString("UpdateNotecardAgentInventory"));
		req.add(OSD.FromString("UpdateScriptAgent"));
		req.add(OSD.FromString("UpdateGestureTaskInventory"));
		req.add(OSD.FromString("UpdateNotecardTaskInventory"));
		req.add(OSD.FromString("UpdateScriptTask"));
		req.add(OSD.FromString("UploadBakedTexture"));
		req.add(OSD.FromString("ViewerStartAuction"));
		req.add(OSD.FromString("ViewerStats"));
		req.add(OSD.FromString("WebFetchInventoryDescendents"));

		try
		{
			CapsClient request = new CapsClient();
			request.setResultCallback(new SeedRequestHandler());
			_SeedRequest = request.executeHttpPost(new URI(_SeedCapsURI), req, OSD.OSDFormat.Xml, _Simulator.getClient().Settings.CAPS_TIMEOUT);
		}
		catch (Exception e)
		{

		}
	}

	private class SeedRequestHandler implements FutureCallback<OSD>
	{
		@Override
		public void completed(OSD result)
		{
			if (result != null && result.getType().equals(OSDType.Map))
			{
				// Our request succeeded, clear the Future as it is not needed
				// anymore
				_SeedRequest = null;

				OSDMap respTable = (OSDMap) result;
				synchronized (_Capabilities)
				{
					for (String cap : respTable.keySet())
					{
						_Capabilities.put(cap, respTable.get(cap).AsUri());
					}

					if (_Capabilities.containsKey("EventQueueGet"))
					{
						Logger.DebugLog("Starting event queue for " + _Simulator.Name, _Simulator.getClient());
						try
						{
							_EventQueue = new EventQueue(_Simulator, _Capabilities.get("EventQueueGet"));
							_EventQueue.start();
						}
						catch (Exception ex)
						{
							failed(ex);
						}
					}
				}
			}
			else
			{
				// The initial CAPS connection failed, try again
				MakeSeedRequest();
			}
		}

		@Override
		public void failed(Exception ex)
		{
			if (ex instanceof HttpResponseException
					&& ((HttpResponseException) ex).getStatusCode() == HttpStatus.SC_NOT_FOUND)
			{
				Logger.Log("Seed capability returned a 404 status, capability system is aborting", LogLevel.Error,
						_Simulator.getClient());
			}
			else
			{
				// The initial CAPS connection failed, try again
				MakeSeedRequest();
			}
		}

		@Override
		public void cancelled()
		{
			Logger.Log("Seed capability got cancelled, capability system is shutting down", LogLevel.Info,
					_Simulator.getClient());
		}
	}
}
