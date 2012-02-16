/**
 * Copyright (c) 2009, openmetaverse.org
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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import libomv.AgentManager.InstantMessageDialog;
import libomv.AgentManager.InstantMessageOnline;
import libomv.AgentManager.TeleportFlags;
import libomv.AvatarManager.AgentDisplayName;
import libomv.ObjectManager.SaleType;
import libomv.ParcelManager;
import libomv.ParcelManager.LandingTypeEnum;
import libomv.ParcelManager.ParcelCategory;
import libomv.ParcelManager.ParcelResult;
import libomv.Simulator.SimAccess;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem;
import libomv.assets.AssetItem.AssetType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.primitives.MediaEntry;
import libomv.primitives.PhysicsProperties;
import libomv.primitives.Primitive.AttachmentPoint;
import libomv.primitives.Primitive.ExtraParamType;
import libomv.primitives.Primitive.Material;
import libomv.primitives.Primitive.SculptType;
import libomv.primitives.TextureEntry.Bumpiness;
import libomv.types.Color4;
import libomv.types.Permissions.PermissionMask;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class CapsMessage implements IMessage
{
	public enum CapsEventType
	{
		Default, AgentGroupDataUpdate, AvatarGroupsReply, ParcelProperties, ParcelObjectOwnersReply, TeleportFinish, EnableSimulator, ParcelPropertiesUpdate, EstablishAgentCommunication, ChatterBoxInvitation, ChatterBoxSessionEventReply, ChatterBoxSessionStartReply, ChatterBoxSessionAgentListUpdates, RequiredVoiceVersion, MapLayer, ChatSessionRequest, CopyInventoryFromNotecard, ProvisionVoiceAccountRequest, Viewerstats, UpdateAgentLanguage, RemoteParcelRequest, UpdateScriptTask, UploadScriptTask, UpdateScriptAgent, SendPostcard, UpdateGestureAgentInventory, UpdateNotecardAgentInventory, LandStatReply, ParcelVoiceInfoRequest, ViewerStats, EventQueueGet, CrossedRegion, TeleportFailed, PlacesReply, UpdateAgentInformation, DirLandReply, ScriptRunningReply, SearchStatRequest, AgentDropGroup, ForceCloseChatterBoxSession, UploadBakedTexture, WebFetchInventoryDescendents, RegionInfo, UploadObjectAsset, ObjectPhysicsProperties, ObjectMediaNavigate, ObjectMedia, AttachmentResources, LandResources, ProductInfoRequest, DispatchRegionInfo, EstateChangeInfo, FetchInventoryDescendents, GroupProposalBallot, MapLayerGod, NewFileAgentInventory, BulkUpdateInventory, RequestTextureDownload, SearchStatTracking, SendUserReport, SendUserReportWithScreenshot, ServerReleaseNotes, StartGroupProposal, UpdateGestureTaskInventory, UpdateNotecardTaskInventory, ViewerStartAuction, UntrustedSimulatorMessage, GetDisplayNames, SetDisplayName, SetDisplayNameReply, DisplayNameUpdate,
	}

	@Override
	public CapsEventType getType()
	{
		return CapsEventType.Default;
	}

	// #region Teleport/Region/Movement Messages

	/* Sent to the client to indicate a teleport request has completed */
	public class TeleportFinishMessage implements IMessage
	{
		// The <see cref="UUID"/> of the agent
		public UUID AgentID;
		//
		public int LocationID;
		// The simulators handle the agent teleported to
		public long RegionHandle;
		// A URI which contains a list of Capabilities the simulator supports
		public URI SeedCapability;
		// Indicates the level of access required to access the simulator, or
		// the content rating, or the simulators map status
		public SimAccess SimAccess;
		// The IP Address of the simulator
		public InetAddress IP;
		// The UDP Port the simulator will listen for UDP traffic on
		public int Port;
		// Status flags indicating the state of the Agent upon arrival, Flying,
		// etc.
		public int Flags;

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.TeleportFinish;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);

			OSDArray infoArray = new OSDArray(1);

			OSDMap info = new OSDMap(8);
			info.put("AgentID", OSD.FromUUID(AgentID));
			info.put("LocationID", OSD.FromInteger(LocationID)); // Unused by
																	// the
																	// client
			info.put("RegionHandle", OSD.FromULong(RegionHandle));
			info.put("SeedCapability", OSD.FromUri(SeedCapability));
			info.put("SimAccess", OSD.FromInteger(SimAccess.getValue()));
			info.put("SimIP", OSD.FromBinary(IP.getAddress()));
			info.put("SimPort", OSD.FromInteger(Port));
			info.put("TeleportFlags", OSD.FromInteger(TeleportFlags.getValue(Flags)));

			infoArray.add(info);

			map.put("Info", infoArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray array = (OSDArray) map.get("Info");
			OSDMap blockMap = (OSDMap) array.get(0);

			AgentID = blockMap.get("AgentID").AsUUID();
			LocationID = blockMap.get("LocationID").AsInteger();
			RegionHandle = blockMap.get("RegionHandle").AsULong();
			SeedCapability = blockMap.get("SeedCapability").AsUri();
			SimAccess = libomv.Simulator.SimAccess.setValue(blockMap.get("SimAccess").AsInteger());
			IP = blockMap.get("SimIP").AsInetAddress();
			Port = blockMap.get("SimPort").AsInteger();
			Flags = TeleportFlags.setValue(blockMap.get("TeleportFlags").AsUInteger());
		}
	}

	// Sent to the viewer when a neighboring simulator is requesting the agent
	// make a connection to it.
	public class EstablishAgentCommunicationMessage implements IMessage
	{
		public UUID AgentID;
		public InetAddress Address;
		public int Port;
		public URI SeedCapability;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.EstablishAgentCommunication;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("agent-id", OSD.FromUUID(AgentID));
			map.put("sim-ip-and-port", OSD.FromString(String.format("%s:%d", Address.getHostAddress(), Port)));
			map.put("seed-capability", OSD.FromUri(SeedCapability));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			String ipAndPort = map.get("sim-ip-and-port").AsString();
			int i = ipAndPort.indexOf(':');

			AgentID = map.get("agent-id").AsUUID();
			Port = 2345; // FIXME: What default port should we use?
			try
			{
				if (i >= 0)
				{
					Address = InetAddress.getByName(ipAndPort.substring(0, i));
					Port = Integer.valueOf(ipAndPort.substring(i + 1));
				}
				else
				{
					Address = InetAddress.getByName(ipAndPort);
				}
			}
			catch (UnknownHostException e)
			{
				Address = null;
			}
			SeedCapability = map.get("seed-capability").AsUri();
		}
	}

	public class CrossedRegionMessage implements IMessage
	{
		public Vector3 LookAt;
		public Vector3 Position;
		public UUID AgentID;
		public UUID SessionID;
		public long RegionHandle;
		public URI SeedCapability;
		public InetAddress IP;
		public int Port;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.CrossedRegion;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			OSDArray infoArray = new OSDArray(1);
			OSDMap infoMap = new OSDMap(2);
			infoMap.put("LookAt", OSD.FromVector3(LookAt));
			infoMap.put("Position", OSD.FromVector3(Position));
			infoArray.add(infoMap);
			map.put("Info", infoArray);

			OSDArray agentDataArray = new OSDArray(1);
			OSDMap agentDataMap = new OSDMap(2);
			agentDataMap.put("AgentID", OSD.FromUUID(AgentID));
			agentDataMap.put("SessionID", OSD.FromUUID(SessionID));
			agentDataArray.add(agentDataMap);
			map.put("AgentData", agentDataArray);

			OSDArray regionDataArray = new OSDArray(1);
			OSDMap regionDataMap = new OSDMap(4);
			regionDataMap.put("RegionHandle", OSD.FromULong(RegionHandle));
			regionDataMap.put("SeedCapability", OSD.FromUri(SeedCapability));
			regionDataMap.put("SimIP", OSD.FromBinary(IP.getAddress()));
			regionDataMap.put("SimPort", OSD.FromInteger(Port));
			regionDataArray.add(regionDataMap);
			map.put("RegionData", regionDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDMap infoMap = (OSDMap) ((OSDArray) map.get("Info")).get(0);
			LookAt = infoMap.get("LookAt").AsVector3();
			Position = infoMap.get("Position").AsVector3();

			OSDMap agentDataMap = (OSDMap) ((OSDArray) map.get("AgentData")).get(0);
			AgentID = agentDataMap.get("AgentID").AsUUID();
			SessionID = agentDataMap.get("SessionID").AsUUID();

			OSDMap regionDataMap = (OSDMap) ((OSDArray) map.get("RegionData")).get(0);
			RegionHandle = regionDataMap.get("RegionHandle").AsULong();
			SeedCapability = regionDataMap.get("SeedCapability").AsUri();
			IP = regionDataMap.get("SimIP").AsInetAddress();
			Port = regionDataMap.get("SimPort").AsInteger();
		}
	}

	public class EnableSimulatorMessage implements IMessage
	{
		public class SimulatorInfoBlock
		{
			public long RegionHandle;
			public InetAddress IP;
			public int Port;
		}

		public SimulatorInfoBlock[] Simulators;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.EnableSimulator;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);

			OSDArray array = new OSDArray(Simulators.length);
			for (int i = 0; i < Simulators.length; i++)
			{
				SimulatorInfoBlock block = Simulators[i];

				OSDMap blockMap = new OSDMap(3);
				blockMap.put("Handle", OSD.FromULong(block.RegionHandle));
				blockMap.put("IP", OSD.FromBinary(block.IP));
				blockMap.put("Port", OSD.FromInteger(block.Port));
				array.add(blockMap);
			}

			map.put("SimulatorInfo", array);
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray array = (OSDArray) map.get("SimulatorInfo");
			Simulators = new SimulatorInfoBlock[array.size()];

			for (int i = 0; i < array.size(); i++)
			{
				OSDMap blockMap = (OSDMap) array.get(i);

				SimulatorInfoBlock block = new SimulatorInfoBlock();
				block.RegionHandle = blockMap.get("Handle").AsULong();
				block.IP = blockMap.get("IP").AsInetAddress();
				block.Port = blockMap.get("Port").AsInteger();
				Simulators[i] = block;
			}
		}
	}

	// A message sent to the client which indicates a teleport request has
	// failed
	// and contains some information on why it failed
	public class TeleportFailedMessage implements IMessage
	{
		//
		public String ExtraParams;
		// A string key of the reason the teleport failed e.g. CouldntTPCloser
		// Which could be used to look up a value in a dictionary or enum
		public String MessageKey;
		// The <see cref="UUID"/> of the Agent
		public UUID AgentID;
		// A string human readable message containing the reason
		// An example: Could not teleport closer to destination
		public String Reason;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.TeleportFailed;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);

			OSDMap alertInfoMap = new OSDMap(2);

			alertInfoMap.put("ExtraParams", OSD.FromString(ExtraParams));
			alertInfoMap.put("Message", OSD.FromString(MessageKey));
			OSDArray alertArray = new OSDArray();
			alertArray.add(alertInfoMap);
			map.put("AlertInfo", alertArray);

			OSDMap infoMap = new OSDMap(2);
			infoMap.put("AgentID", OSD.FromUUID(AgentID));
			infoMap.put("Reason", OSD.FromString(Reason));
			OSDArray infoArray = new OSDArray();
			infoArray.add(infoMap);
			map.put("Info", infoArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{

			OSDArray alertInfoArray = (OSDArray) map.get("AlertInfo");

			OSDMap alertInfoMap = (OSDMap) alertInfoArray.get(0);
			ExtraParams = alertInfoMap.get("ExtraParams").AsString();
			MessageKey = alertInfoMap.get("Message").AsString();

			OSDArray infoArray = (OSDArray) map.get("Info");
			OSDMap infoMap = (OSDMap) infoArray.get(0);
			AgentID = infoMap.get("AgentID").AsUUID();
			Reason = infoMap.get("Reason").AsString();
		}
	}

	public class LandStatReplyMessage implements IMessage
	{
		public int ReportType;
		public int RequestFlags;
		public int TotalObjectCount;

		public class ReportDataBlock
		{
			public Vector3 Location;
			public String OwnerName;
			public float Score;
			public UUID TaskID;
			public int TaskLocalID;
			public String TaskName;
			public float MonoScore;
			public Date TimeStamp;
		}

		public ReportDataBlock[] ReportDataBlocks;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.LandStatReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			OSDMap requestDataMap = new OSDMap(3);
			requestDataMap.put("ReportType", OSD.FromUInteger(this.ReportType));
			requestDataMap.put("RequestFlags", OSD.FromUInteger(this.RequestFlags));
			requestDataMap.put("TotalObjectCount", OSD.FromUInteger(this.TotalObjectCount));

			OSDArray requestDatArray = new OSDArray();
			requestDatArray.add(requestDataMap);
			map.put("RequestData", requestDatArray);

			OSDArray reportDataArray = new OSDArray();
			OSDArray dataExtendedArray = new OSDArray();
			for (int i = 0; i < ReportDataBlocks.length; i++)
			{
				OSDMap reportMap = new OSDMap(8);
				reportMap.put("LocationX", OSD.FromReal(ReportDataBlocks[i].Location.X));
				reportMap.put("LocationY", OSD.FromReal(ReportDataBlocks[i].Location.Y));
				reportMap.put("LocationZ", OSD.FromReal(ReportDataBlocks[i].Location.Z));
				reportMap.put("OwnerName", OSD.FromString(ReportDataBlocks[i].OwnerName));
				reportMap.put("Score", OSD.FromReal(ReportDataBlocks[i].Score));
				reportMap.put("TaskID", OSD.FromUUID(ReportDataBlocks[i].TaskID));
				reportMap.put("TaskLocalID", OSD.FromReal(ReportDataBlocks[i].TaskLocalID));
				reportMap.put("TaskName", OSD.FromString(ReportDataBlocks[i].TaskName));
				reportDataArray.add(reportMap);

				OSDMap extendedMap = new OSDMap(2);
				extendedMap.put("MonoScore", OSD.FromReal(ReportDataBlocks[i].MonoScore));
				extendedMap.put("TimeStamp", OSD.FromDate(ReportDataBlocks[i].TimeStamp));
				dataExtendedArray.add(extendedMap);
			}

			map.put("ReportData", reportDataArray);
			map.put("DataExtended", dataExtendedArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{

			OSDArray requestDataArray = (OSDArray) map.get("RequestData");
			OSDMap requestMap = (OSDMap) requestDataArray.get(0);

			this.ReportType = requestMap.get("ReportType").AsUInteger();
			this.RequestFlags = requestMap.get("RequestFlags").AsUInteger();
			this.TotalObjectCount = requestMap.get("TotalObjectCount").AsUInteger();

			if (TotalObjectCount < 1)
			{
				ReportDataBlocks = new ReportDataBlock[0];
				return;
			}

			OSDArray dataArray = (OSDArray) map.get("ReportData");
			OSDArray dataExtendedArray = (OSDArray) map.get("DataExtended");

			ReportDataBlocks = new ReportDataBlock[dataArray.size()];
			for (int i = 0; i < dataArray.size(); i++)
			{
				OSDMap blockMap = (OSDMap) dataArray.get(i);
				OSDMap extMap = (OSDMap) dataExtendedArray.get(i);
				ReportDataBlock block = new ReportDataBlock();
				block.Location = new Vector3((float) blockMap.get("LocationX").AsReal(), (float) blockMap.get(
						"LocationY").AsReal(), (float) blockMap.get("LocationZ").AsReal());
				block.OwnerName = blockMap.get("OwnerName").AsString();
				block.Score = (float) blockMap.get("Score").AsReal();
				block.TaskID = blockMap.get("TaskID").AsUUID();
				block.TaskLocalID = blockMap.get("TaskLocalID").AsUInteger();
				block.TaskName = blockMap.get("TaskName").AsString();
				block.MonoScore = (float) extMap.get("MonoScore").AsReal();
				block.TimeStamp = Helpers.UnixTimeToDateTime(extMap.get("TimeStamp").AsUInteger());

				ReportDataBlocks[i] = block;
			}
		}
	}

	// #region Parcel Messages

	/**
	 * Contains a list of prim owner information for a specific parcel in a
	 * simulator
	 * 
	 * A Simulator will always return at least 1 entry If agent does not have
	 * proper permission the OwnerID will be UUID.Zero If agent does not have
	 * proper permission OR there are no primitives on parcel the
	 * DataBlocksExtended map will not be sent from the simulator
	 */
	public class ParcelObjectOwnersReplyMessage implements IMessage
	{
		// Prim ownership information for a specified owner on a single parcel
		public class PrimOwner
		{
			// The <see cref="UUID"/> of the prim owner,
			// UUID.Zero if agent has no permission to view prim owner
			// information
			public UUID OwnerID;
			// The total number of prims
			public int Count;
			// True if the OwnerID is a <see cref="Group"/>
			public boolean IsGroupOwned;
			// True if the owner is online
			// This is no longer used by the LL Simulators
			public boolean OnlineStatus;
			// The date the most recent prim was rezzed
			public Date TimeStamp;
		}

		// An Array of <see cref="PrimOwner"/> objects
		public PrimOwner[] PrimOwnersBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ParcelObjectOwnersReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDArray dataArray = new OSDArray(PrimOwnersBlock.length);
			OSDArray dataExtendedArray = new OSDArray();

			for (int i = 0; i < PrimOwnersBlock.length; i++)
			{
				OSDMap dataMap = new OSDMap(4);
				dataMap.put("OwnerID", OSD.FromUUID(PrimOwnersBlock[i].OwnerID));
				dataMap.put("Count", OSD.FromInteger(PrimOwnersBlock[i].Count));
				dataMap.put("IsGroupOwned", OSD.FromBoolean(PrimOwnersBlock[i].IsGroupOwned));
				dataMap.put("OnlineStatus", OSD.FromBoolean(PrimOwnersBlock[i].OnlineStatus));
				dataArray.add(dataMap);

				OSDMap dataExtendedMap = new OSDMap(1);
				dataExtendedMap.put("TimeStamp", OSD.FromDate(PrimOwnersBlock[i].TimeStamp));
				dataExtendedArray.add(dataExtendedMap);
			}

			OSDMap map = new OSDMap();
			map.put("Data", dataArray);
			if (dataExtendedArray.size() > 0)
				map.put("DataExtended", dataExtendedArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray dataArray = (OSDArray) map.get("Data");

			// DataExtended is optional, will not exist of parcel contains zero
			// prims
			OSDArray dataExtendedArray;
			if (map.containsKey("DataExtended"))
			{
				dataExtendedArray = (OSDArray) map.get("DataExtended");
			}
			else
			{
				dataExtendedArray = new OSDArray();
			}

			PrimOwnersBlock = new PrimOwner[dataArray.size()];

			for (int i = 0; i < dataArray.size(); i++)
			{
				OSDMap dataMap = (OSDMap) dataArray.get(i);
				PrimOwner block = new PrimOwner();
				block.OwnerID = dataMap.get("OwnerID").AsUUID();
				block.Count = dataMap.get("Count").AsInteger();
				block.IsGroupOwned = dataMap.get("IsGroupOwned").AsBoolean();
				block.OnlineStatus = dataMap.get("OnlineStatus").AsBoolean(); // deprecated

				// if the agent has no permissions, or there are no prims, the
				// counts
				// should not match up, so we don't decode the DataExtended map
				if (dataExtendedArray.size() == dataArray.size())
				{
					OSDMap dataExtendedMap = (OSDMap) dataExtendedArray.get(i);
					block.TimeStamp = Helpers.UnixTimeToDateTime(dataExtendedMap.get("TimeStamp").AsUInteger());
				}
				PrimOwnersBlock[i] = block;
			}
		}
	}

	// The details of a single parcel in a region, also contains some regionwide
	// globals
	public class ParcelPropertiesMessage implements IMessage
	{
		// Simulator-local ID of this parcel
		public int LocalID;
		// Maximum corner of the axis-aligned bounding box for this parcel
		public Vector3 AABBMax;
		// Minimum corner of the axis-aligned bounding box for this parcel
		public Vector3 AABBMin;
		// Total parcel land area
		public int Area;
		//
		public int AuctionID;
		// Key of authorized buyer
		public UUID AuthBuyerID;
		// Bitmap describing land layout in 4x4m squares across the entire
		// region
		public byte[] Bitmap;
		//
		public ParcelCategory Category;
		// Date land was claimed
		public Date ClaimDate;
		// Appears to always be zero
		public int ClaimPrice;
		// Parcel Description
		public String Desc;
		//
		public int ParcelFlags;
		//
		public UUID GroupID;
		// Total number of primitives owned by the parcel group on this parcel
		public int GroupPrims;
		// Whether the land is deeded to a group or not
		public boolean IsGroupOwned;
		//
		public LandingTypeEnum LandingType;
		// Maximum number of primitives this parcel supports
		public int MaxPrims;
		// The Asset UUID of the Texture which when applied to a primitive will
		// display the media
		public UUID MediaID;
		// A URL which points to any Quicktime supported media type
		public String MediaURL;
		// A byte, if 0x1 viewer should auto scale media to fit object
		public boolean MediaAutoScale;
		// URL For Music Stream
		public String MusicURL;
		// Parcel Name
		public String Name;
		// Autoreturn value in minutes for others' objects
		public int OtherCleanTime;
		//
		public int OtherCount;
		// Total number of other primitives on this parcel
		public int OtherPrims;
		// UUID of the owner of this parcel
		public UUID OwnerID;
		// Total number of primitives owned by the parcel owner on this parcel
		public int OwnerPrims;
		//
		public float ParcelPrimBonus;
		// How long is pass valid for
		public float PassHours;
		// Price for a temporary pass
		public int PassPrice;
		//
		public int PublicCount;
        // Disallows people outside the parcel from being able to see in
        public boolean Privacy;
		//
		public boolean RegionDenyAnonymous;
		//
		public boolean RegionPushOverride;
		// This field is no longer used
		public int RentPrice;
		// The result of a request for parcel properties
		public ParcelResult RequestResult;
		// Sale price of the parcel, only useful if ForSale is set
		// The SalePrice will remain the same after an ownership transfer
		// (sale), so it can be used to see the purchase
		// price after a sale if the new owner has not changed it
		public int SalePrice;
		// Number of primitives your avatar is currently selecting and sitting
		// on in this parcel
		public int SelectedPrims;
		//
		public int SelfCount;
		// A number which increments by 1, starting at 0 for each
		// ParcelProperties request.
		// Can be overriden by specifying the sequenceID with the
		// ParcelPropertiesRequest being sent.
		// a Negative number indicates the action in {@link
		// ParcelPropertiesStatus} has occurred.
		public int SequenceID;
		// Maximum primitives across the entire simulator
		public int SimWideMaxPrims;
		// Total primitives across the entire simulator
		public int SimWideTotalPrims;
		//
		public boolean SnapSelection;
		// Key of parcel snapshot
		public UUID SnapshotID;
		// Parcel ownership status
		public ParcelManager.ParcelStatus Status;
		// Total number of primitives on this parcel
		public int TotalPrims;
		//
		public Vector3 UserLocation;
		//
		public Vector3 UserLookAt;
		// TRUE of region denies access to age unverified users
		public boolean RegionDenyAgeUnverified;
		// A description of the media
		public String MediaDesc;
		// An Integer which represents the height of the media
		public int MediaHeight;
		// An integer which represents the width of the media
		public int MediaWidth;
		// A boolean, if true the viewer should loop the media
		public boolean MediaLoop;
		// A string which contains the mime type of the media
		public String MediaType;
		// true to obscure (hide) media url
		public boolean ObscureMedia;
		// true to obscure (hide) music url
		public boolean ObscureMusic;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ParcelProperties;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			OSDArray dataArray = new OSDArray(1);
			OSDMap parcelDataMap = new OSDMap(47);
			parcelDataMap.put("LocalID", OSD.FromInteger(LocalID));
			parcelDataMap.put("AABBMax", OSD.FromVector3(AABBMax));
			parcelDataMap.put("AABBMin", OSD.FromVector3(AABBMin));
			parcelDataMap.put("Area", OSD.FromInteger(Area));
			parcelDataMap.put("AuctionID", OSD.FromInteger(AuctionID));
			parcelDataMap.put("AuthBuyerID", OSD.FromUUID(AuthBuyerID));
			parcelDataMap.put("Bitmap", OSD.FromBinary(Bitmap));
			parcelDataMap.put("Category", OSD.FromInteger(Category.getValue()));
			parcelDataMap.put("ClaimDate", OSD.FromDate(ClaimDate));
			parcelDataMap.put("ClaimPrice", OSD.FromInteger(ClaimPrice));
			parcelDataMap.put("Desc", OSD.FromString(Desc));
			parcelDataMap.put("ParcelFlags", OSD.FromUInteger(ParcelFlags));
			parcelDataMap.put("GroupID", OSD.FromUUID(GroupID));
			parcelDataMap.put("GroupPrims", OSD.FromInteger(GroupPrims));
			parcelDataMap.put("IsGroupOwned", OSD.FromBoolean(IsGroupOwned));
			parcelDataMap.put("LandingType", OSD.FromInteger(LandingType.getValue()));
			parcelDataMap.put("MaxPrims", OSD.FromInteger(MaxPrims));
			parcelDataMap.put("MediaID", OSD.FromUUID(MediaID));
			parcelDataMap.put("MediaURL", OSD.FromString(MediaURL));
			parcelDataMap.put("MediaAutoScale", OSD.FromBoolean(MediaAutoScale));
			parcelDataMap.put("MusicURL", OSD.FromString(MusicURL));
			parcelDataMap.put("Name", OSD.FromString(Name));
			parcelDataMap.put("OtherCleanTime", OSD.FromInteger(OtherCleanTime));
			parcelDataMap.put("OtherCount", OSD.FromInteger(OtherCount));
			parcelDataMap.put("OtherPrims", OSD.FromInteger(OtherPrims));
			parcelDataMap.put("OwnerID", OSD.FromUUID(OwnerID));
			parcelDataMap.put("OwnerPrims", OSD.FromInteger(OwnerPrims));
			parcelDataMap.put("ParcelPrimBonus", OSD.FromReal(ParcelPrimBonus));
			parcelDataMap.put("PassHours", OSD.FromReal(PassHours));
			parcelDataMap.put("PassPrice", OSD.FromInteger(PassPrice));
			parcelDataMap.put("PublicCount", OSD.FromInteger(PublicCount));
            parcelDataMap.put("Privacy", OSD.FromBoolean(Privacy));
			parcelDataMap.put("RegionDenyAnonymous", OSD.FromBoolean(RegionDenyAnonymous));
			parcelDataMap.put("RegionPushOverride", OSD.FromBoolean(RegionPushOverride));
			parcelDataMap.put("RentPrice", OSD.FromInteger(RentPrice));
			parcelDataMap.put("RequestResult", OSD.FromInteger(RequestResult.getValue()));
			parcelDataMap.put("SalePrice", OSD.FromInteger(SalePrice));
			parcelDataMap.put("SelectedPrims", OSD.FromInteger(SelectedPrims));
			parcelDataMap.put("SelfCount", OSD.FromInteger(SelfCount));
			parcelDataMap.put("SequenceID", OSD.FromInteger(SequenceID));
			parcelDataMap.put("SimWideMaxPrims", OSD.FromInteger(SimWideMaxPrims));
			parcelDataMap.put("SimWideTotalPrims", OSD.FromInteger(SimWideTotalPrims));
			parcelDataMap.put("SnapSelection", OSD.FromBoolean(SnapSelection));
			parcelDataMap.put("SnapshotID", OSD.FromUUID(SnapshotID));
			parcelDataMap.put("Status", OSD.FromInteger(Status.getValue()));
			parcelDataMap.put("TotalPrims", OSD.FromInteger(TotalPrims));
			parcelDataMap.put("UserLocation", OSD.FromVector3(UserLocation));
			parcelDataMap.put("UserLookAt", OSD.FromVector3(UserLookAt));
			dataArray.add(parcelDataMap);
			map.put("ParcelData", dataArray);

			OSDArray mediaDataArray = new OSDArray(1);
			OSDMap mediaDataMap = new OSDMap(7);
			mediaDataMap.put("MediaDesc", OSD.FromString(MediaDesc));
			mediaDataMap.put("MediaHeight", OSD.FromInteger(MediaHeight));
			mediaDataMap.put("MediaWidth", OSD.FromInteger(MediaWidth));
			mediaDataMap.put("MediaLoop", OSD.FromBoolean(MediaLoop));
			mediaDataMap.put("MediaType", OSD.FromString(MediaType));
			mediaDataMap.put("ObscureMedia", OSD.FromBoolean(ObscureMedia));
			mediaDataMap.put("ObscureMusic", OSD.FromBoolean(ObscureMusic));
			mediaDataArray.add(mediaDataMap);
			map.put("MediaData", mediaDataArray);

			OSDArray ageVerificationBlockArray = new OSDArray(1);
			OSDMap ageVerificationBlockMap = new OSDMap(1);
			ageVerificationBlockMap.put("RegionDenyAgeUnverified", OSD.FromBoolean(RegionDenyAgeUnverified));
			ageVerificationBlockArray.add(ageVerificationBlockMap);
			map.put("AgeVerificationBlock", ageVerificationBlockArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDMap parcelDataMap = (OSDMap) ((OSDArray) map.get("ParcelData")).get(0);
			LocalID = parcelDataMap.get("LocalID").AsInteger();
			AABBMax = parcelDataMap.get("AABBMax").AsVector3();
			AABBMin = parcelDataMap.get("AABBMin").AsVector3();
			Area = parcelDataMap.get("Area").AsInteger();
			AuctionID = parcelDataMap.get("AuctionID").AsInteger();
			AuthBuyerID = parcelDataMap.get("AuthBuyerID").AsUUID();
			Bitmap = parcelDataMap.get("Bitmap").AsBinary();
			Category = ParcelCategory.setValue(parcelDataMap.get("Category").AsInteger());
			ClaimDate = Helpers.UnixTimeToDateTime(parcelDataMap.get("ClaimDate").AsInteger());
			ClaimPrice = parcelDataMap.get("ClaimPrice").AsInteger();
			Desc = parcelDataMap.get("Desc").AsString();

			// LL sends this as binary, we'll convert it here
			if (parcelDataMap.get("ParcelFlags").getType() == OSDType.Binary)
			{
				byte[] bytes = parcelDataMap.get("ParcelFlags").AsBinary();
				ParcelFlags = ParcelManager.ParcelFlags.getValue((int) Helpers.BytesToUInt32B(bytes));
			}
			else
			{
				ParcelFlags = ParcelManager.ParcelFlags.getValue(parcelDataMap.get("ParcelFlags").AsUInteger());
			}
			GroupID = parcelDataMap.get("GroupID").AsUUID();
			GroupPrims = parcelDataMap.get("GroupPrims").AsInteger();
			IsGroupOwned = parcelDataMap.get("IsGroupOwned").AsBoolean();
			LandingType = ParcelManager.LandingTypeEnum.setValue(parcelDataMap.get("LandingType").AsInteger());
			MaxPrims = parcelDataMap.get("MaxPrims").AsInteger();
			MediaID = parcelDataMap.get("MediaID").AsUUID();
			MediaURL = parcelDataMap.get("MediaURL").AsString();
			MediaAutoScale = parcelDataMap.get("MediaAutoScale").AsBoolean(); // 0x1
																				// =
																				// yes
			MusicURL = parcelDataMap.get("MusicURL").AsString();
			Name = parcelDataMap.get("Name").AsString();
			OtherCleanTime = parcelDataMap.get("OtherCleanTime").AsInteger();
			OtherCount = parcelDataMap.get("OtherCount").AsInteger();
			OtherPrims = parcelDataMap.get("OtherPrims").AsInteger();
			OwnerID = parcelDataMap.get("OwnerID").AsUUID();
			OwnerPrims = parcelDataMap.get("OwnerPrims").AsInteger();
			ParcelPrimBonus = (float) parcelDataMap.get("ParcelPrimBonus").AsReal();
			PassHours = (float) parcelDataMap.get("PassHours").AsReal();
			PassPrice = parcelDataMap.get("PassPrice").AsInteger();
			PublicCount = parcelDataMap.get("PublicCount").AsInteger();
            Privacy = parcelDataMap.get("Privacy").AsBoolean();
			RegionDenyAnonymous = parcelDataMap.get("RegionDenyAnonymous").AsBoolean();
			RegionPushOverride = parcelDataMap.get("RegionPushOverride").AsBoolean();
			RentPrice = parcelDataMap.get("RentPrice").AsInteger();
			RequestResult = ParcelResult.setValue(parcelDataMap.get("RequestResult").AsInteger());
			SalePrice = parcelDataMap.get("SalePrice").AsInteger();
			SelectedPrims = parcelDataMap.get("SelectedPrims").AsInteger();
			SelfCount = parcelDataMap.get("SelfCount").AsInteger();
			SequenceID = parcelDataMap.get("SequenceID").AsInteger();
			SimWideMaxPrims = parcelDataMap.get("SimWideMaxPrims").AsInteger();
			SimWideTotalPrims = parcelDataMap.get("SimWideTotalPrims").AsInteger();
			SnapSelection = parcelDataMap.get("SnapSelection").AsBoolean();
			SnapshotID = parcelDataMap.get("SnapshotID").AsUUID();
			Status = ParcelManager.ParcelStatus.setValue(parcelDataMap.get("Status").AsInteger());
			TotalPrims = parcelDataMap.get("TotalPrims").AsInteger();
			UserLocation = parcelDataMap.get("UserLocation").AsVector3();
			UserLookAt = parcelDataMap.get("UserLookAt").AsVector3();

			if (map.containsKey("MediaData")) // temporary, OpenSim doesn't send
												// this block
			{
				OSDMap mediaDataMap = (OSDMap) ((OSDArray) map.get("MediaData")).get(0);
				MediaDesc = mediaDataMap.get("MediaDesc").AsString();
				MediaHeight = mediaDataMap.get("MediaHeight").AsInteger();
				MediaWidth = mediaDataMap.get("MediaWidth").AsInteger();
				MediaLoop = mediaDataMap.get("MediaLoop").AsBoolean();
				MediaType = mediaDataMap.get("MediaType").AsString();
				ObscureMedia = mediaDataMap.get("ObscureMedia").AsBoolean();
				ObscureMusic = mediaDataMap.get("ObscureMusic").AsBoolean();
			}

			OSDMap ageVerificationBlockMap = (OSDMap) ((OSDArray) map.get("AgeVerificationBlock")).get(0);
			RegionDenyAgeUnverified = ageVerificationBlockMap.get("RegionDenyAgeUnverified").AsBoolean();
		}
	}

	// A message sent from the viewer to the simulator to updated a specific
	// parcels settings
	public class ParcelPropertiesUpdateMessage implements IMessage
	{
		// The {@link UUID} of the agent authorized to purchase this parcel of
		// land or
		// a NULL {@link UUID} if the sale is authorized to anyone
		public UUID AuthBuyerID;
		// true to enable auto scaling of the parcel media
		public boolean MediaAutoScale;
		// The category of this parcel used when search is enabled to restrict
		// search results
		public ParcelCategory Category;
		// A string containing the description to set
		public String Desc;
		// The {@link UUID} of the {@link Group} which allows for additional
		// powers and restrictions.
		public UUID GroupID;
		// The {@link LandingType} which specifies how avatars which teleport to
		// this parcel are handled
		public LandingTypeEnum LandingType;
		// The LocalID of the parcel to update settings on
		public int LocalID;
		// A string containing the description of the media which can be played
		// to visitors
		public String MediaDesc;
		//
		public int MediaHeight;
		//
		public boolean MediaLoop;
		//
		public UUID MediaID;
		//
		public String MediaType;
		//
		public String MediaURL;
		//
		public int MediaWidth;
		//
		public String MusicURL;
		//
		public String Name;
		//
		public boolean ObscureMedia;
		//
		public boolean ObscureMusic;
		//
		public int ParcelFlags;
		//
		public float PassHours;
		//
		public int PassPrice;
		//
        public boolean Privacy;
		//
		public int SalePrice;
		//
		public UUID SnapshotID;
		//
		public Vector3 UserLocation;
		//
		public Vector3 UserLookAt;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ParcelPropertiesUpdate;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap();
			map.put("auth_buyer_id", OSD.FromUUID(AuthBuyerID));
			map.put("auto_scale", OSD.FromBoolean(MediaAutoScale));
			map.put("category", OSD.FromInteger(Category.getValue()));
			map.put("description", OSD.FromString(Desc));
			map.put("flags", OSD.FromBinary(Helpers.EmptyBytes));
			map.put("group_id", OSD.FromUUID(GroupID));
			map.put("landing_type", OSD.FromInteger(LandingType.getValue()));
			map.put("local_id", OSD.FromInteger(LocalID));
			map.put("media_desc", OSD.FromString(MediaDesc));
			map.put("media_height", OSD.FromInteger(MediaHeight));
			map.put("media_id", OSD.FromUUID(MediaID));
			map.put("media_loop", OSD.FromBoolean(MediaLoop));
			map.put("media_type", OSD.FromString(MediaType));
			map.put("media_url", OSD.FromString(MediaURL));
			map.put("media_width", OSD.FromInteger(MediaWidth));
			map.put("music_url", OSD.FromString(MusicURL));
			map.put("name", OSD.FromString(Name));
			map.put("obscure_media", OSD.FromBoolean(ObscureMedia));
			map.put("obscure_music", OSD.FromBoolean(ObscureMusic));
			map.put("parcel_flags", OSD.FromUInteger(ParcelFlags));
			map.put("pass_hours", OSD.FromReal(PassHours));
			map.put("pass_price", OSD.FromInteger(PassPrice));
            map.put("privacy", OSD.FromBoolean(Privacy));
			map.put("sale_price", OSD.FromInteger(SalePrice));
			map.put("snapshot_id", OSD.FromUUID(SnapshotID));
			map.put("user_location", OSD.FromVector3(UserLocation));
			map.put("user_look_at", OSD.FromVector3(UserLookAt));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			AuthBuyerID = map.get("auth_buyer_id").AsUUID();
			MediaAutoScale = map.get("auto_scale").AsBoolean();
			Category = ParcelCategory.setValue(map.get("category").AsInteger());
			Desc = map.get("description").AsString();
			GroupID = map.get("group_id").AsUUID();
			LandingType = LandingTypeEnum.setValue(map.get("landing_type").AsUInteger());
			LocalID = map.get("local_id").AsInteger();
			MediaDesc = map.get("media_desc").AsString();
			MediaHeight = map.get("media_height").AsInteger();
			MediaLoop = map.get("media_loop").AsBoolean();
			MediaID = map.get("media_id").AsUUID();
			MediaType = map.get("media_type").AsString();
			MediaURL = map.get("media_url").AsString();
			MediaWidth = map.get("media_width").AsInteger();
			MusicURL = map.get("music_url").AsString();
			Name = map.get("name").AsString();
			ObscureMedia = map.get("obscure_media").AsBoolean();
			ObscureMusic = map.get("obscure_music").AsBoolean();
			ParcelFlags = ParcelManager.ParcelFlags.setValue((map.get("parcel_flags").AsUInteger()));
			PassHours = (float) map.get("pass_hours").AsReal();
			PassPrice = map.get("pass_price").AsUInteger();
            Privacy = map.get("privacy").AsBoolean();
			SalePrice = map.get("sale_price").AsUInteger();
			SnapshotID = map.get("snapshot_id").AsUUID();
			UserLocation = map.get("user_location").AsVector3();
			UserLookAt = map.get("user_look_at").AsVector3();
		}
	}

	// Base class used for the RemoteParcelRequest message
	public abstract class RemoteParcelRequestBlock
	{
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	// A message sent from the viewer to the simulator to request information on
	// a remote parcel
	public class RemoteParcelRequestRequest extends RemoteParcelRequestBlock
	{
		// Local sim position of the parcel we are looking up
		public Vector3 Location;
		// Region handle of the parcel we are looking up
		public long RegionHandle;
		// Region <see cref="UUID"/> of the parcel we are looking up
		public UUID RegionID;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("location", OSD.FromVector3(Location));
			map.put("region_handle", OSD.FromULong(RegionHandle));
			map.put("region_id", OSD.FromUUID(RegionID));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Location = map.get("location").AsVector3();
			RegionHandle = map.get("region_handle").AsULong();
			RegionID = map.get("region_id").AsUUID();
		}
	}

	// A message sent from the simulator to the viewer in response to a <see
	// cref="RemoteParcelRequestRequest"/>
	public class RemoteParcelRequestReply extends RemoteParcelRequestBlock
	{
		// The grid-wide unique parcel ID
		public UUID ParcelID;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("parcel_id", OSD.FromUUID(ParcelID));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map == null || !map.containsKey("parcel_id"))
				ParcelID = UUID.Zero;
			else
				ParcelID = map.get("parcel_id").AsUUID();
		}
	}

	// A message containing a request for a remote parcel from a viewer, or a
	// response from the simulator to that request
	public class RemoteParcelRequestMessage implements IMessage
	{
		// The request or response details block
		public RemoteParcelRequestBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.RemoteParcelRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("parcel_id"))
			{
				Request = new RemoteParcelRequestReply();
				Request.Deserialize(map);
			}
			else if (map.containsKey("location"))
			{
				Request = new RemoteParcelRequestRequest();
				Request.Deserialize(map);
			}
			else
				Logger.Log(
						"Unable to deserialize RemoteParcelRequest: No message handler exists for method: "
								+ map.AsString(), LogLevel.Warning);
		}
	}

	// #endregion

	// #region Inventory Messages

	public class NewFileAgentInventoryMessage implements IMessage
	{
		public UUID FolderID;
		public AssetItem.AssetType AssetType;
		public InventoryItem.InventoryType InventoryType;
		public String Name;
		public String Description;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.NewFileAgentInventory;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(5);
			map.put("folder_id", OSD.FromUUID(FolderID));
			map.put("asset_type", OSD.FromString(AssetType.toString()));
			map.put("inventory_type", OSD.FromString(InventoryType.toString()));
			map.put("name", OSD.FromString(Name));
			map.put("description", OSD.FromString(Description));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			FolderID = map.get("folder_id").AsUUID();
			AssetType = AssetItem.AssetType.setValue(map.get("asset_type").AsString());
			InventoryType = InventoryItem.InventoryType.setValue(map.get("inventory_type").AsString());
			Name = map.get("name").AsString();
			Description = map.get("description").AsString();
		}
	}

    public class BulkUpdateInventoryMessage implements IMessage
    {
        public class FolderDataInfo
        {
            public UUID FolderID;
            public UUID ParentID;
            public String Name;
            public AssetType Type;

            public FolderDataInfo(OSDMap map)
            {
                FolderID = map.get("FolderID").AsUUID();
                ParentID = map.get("ParentID").AsUUID();
                Name = map.get("Name").AsString();
                Type = AssetType.setValue(map.get("Type").AsInteger());
             }
        }

        public class ItemDataInfo
        {
            public UUID ItemID;
            public int CallbackID;
            public UUID FolderID;
            public UUID CreatorID;
            public UUID OwnerID;
            public UUID GroupID;
            public int BaseMask;
            public int OwnerMask;
            public int GroupMask;
            public int EveryoneMask;
            public int NextOwnerMask;
            public boolean GroupOwned;
            public UUID AssetID;
            public AssetType Type;
            public InventoryType InvType;
            public int Flags;
            public SaleType saleType;
            public int SalePrice;
            public String Name;
            public String Description;
            public Date CreationDate;
            public int CRC;

    		public ItemDataInfo(OSDMap map)
            {
                ItemID = map.get("ItemID").AsUUID();
                CallbackID = map.get("CallbackID").AsUInteger();
                FolderID = map.get("FolderID").AsUUID();
                CreatorID = map.get("CreatorID").AsUUID();
                OwnerID = map.get("OwnerID").AsUUID();
                GroupID = map.get("GroupID").AsUUID();
                BaseMask = PermissionMask.setValue(map.get("BaseMask").AsUInteger());
                OwnerMask = PermissionMask.setValue(map.get("OwnerMask").AsUInteger());
                GroupMask = PermissionMask.setValue(map.get("GroupMask").AsUInteger());
                EveryoneMask = PermissionMask.setValue(map.get("EveryoneMask").AsUInteger());
                NextOwnerMask = PermissionMask.setValue(map.get("NextOwnerMask").AsUInteger());
                GroupOwned = map.get("GroupOwned").AsBoolean();
                AssetID = map.get("AssetID").AsUUID();
                Type = AssetType.setValue(map.get("Type").AsInteger());
                InvType = InventoryType.setValue(map.get("InvType").AsInteger());
                Flags = map.get("Flags").AsUInteger();
                saleType = SaleType.setValue(map.get("SaleType").AsInteger());
                SalePrice = map.get("SaleType").AsInteger();
                Name = map.get("Name").AsString();
                Description = map.get("Description").AsString();
                CreationDate = Helpers.UnixTimeToDateTime(map.get("CreationDate").AsReal());
                CRC = map.get("CRC").AsUInteger();
            }
        }

        public UUID AgentID;
        public UUID TransactionID;
        public FolderDataInfo[] FolderData;
        public ItemDataInfo[] ItemData;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.BulkUpdateInventory;
		}

		@Override
		public OSDMap Serialize()
        {
            throw new UnsupportedOperationException();
        }

        @Override
		public void Deserialize(OSDMap map)
        {
            if (map.get("AgentData") instanceof OSDArray)
            {
                OSDArray array = (OSDArray)map.get("AgentData");
                if (array.size() > 0)
                {
                    OSDMap adata = (OSDMap)array.get(0);
                    AgentID = adata.get("AgentID").AsUUID();
                    TransactionID = adata.get("TransactionID").AsUUID();
                }
            }
            
            if (map.get("FolderData") instanceof OSDArray)
            {
                OSDArray array = (OSDArray)map.get("FolderData");
                FolderData =  new FolderDataInfo[array.size()];
                for (int i = 0; i < array.size(); i++)
                {
                    FolderData[i] = new FolderDataInfo((OSDMap)array.get(i));
                }
            }
            else
            {
                FolderData = new FolderDataInfo[0];
            }

            if (map.get("ItemData") instanceof OSDArray)
            {
                OSDArray array = (OSDArray)map.get("ItemData");
                ItemData = new ItemDataInfo[array.size()];
                for (int i = 0; i < array.size(); i++)
                {
                    ItemData[i] = new ItemDataInfo((OSDMap)array.get(i));
                }
            }
            else
            {
                ItemData = new ItemDataInfo[0];
            }
        }
    }

    public class WebFetchInventoryDescendentsMessage implements IMessage
	{

		// public class Folder implements InventoryBase
		// {

		// }

		// public class Item implements InventoryBase
		// {

		// }
		// #region CapsMessage Members

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.WebFetchInventoryDescendents;
		}

		@Override
		public OSDMap Serialize()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			throw new UnsupportedOperationException();
		}
	}

	// #endregion

	// #region Agent Messages

	// A message sent from the simulator to an agent which contains the groups
	// the agent is in
	public class AgentGroupDataUpdateMessage implements IMessage
	{
		// The Agent receiving the message

		public UUID AgentID;

		// Group Details specific to the agent
		public class GroupData
		{
			// true of the agent accepts group notices
			public boolean AcceptNotices;
			// The agents tier contribution to the group
			public int Contribution;
			// The Groups {@link UUID}
			public UUID GroupID;
			// The {@link UUID} of the groups insignia
			public UUID GroupInsigniaID;
			// The name of the group
			public String GroupName;
			// The aggregate permissions the agent has in the group for all
			// roles the agent is assigned
			public long GroupPowers;
		}

		// An optional block containing additional agent specific information
		public class NewGroupData
		{
			// true of the agent allows this group to be listed in their profile
			public boolean ListInProfile;
		}

		// An array containing {@link GroupData} information
		// for each <see cref="Group"/> the agent is a member of
		public GroupData[] GroupDataBlock;
		// An array containing {@link NewGroupData} information
		// for each <see cref="Group"/> the agent is a member of
		public NewGroupData[] NewGroupDataBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.AgentGroupDataUpdate;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			OSDMap agent = new OSDMap(1);
			agent.put("AgentID", OSD.FromUUID(AgentID));

			OSDArray agentArray = new OSDArray();
			agentArray.add(agent);

			map.put("AgentData", agentArray);

			OSDArray groupDataArray = new OSDArray(GroupDataBlock.length);

			for (int i = 0; i < GroupDataBlock.length; i++)
			{
				OSDMap group = new OSDMap(6);
				group.put("AcceptNotices", OSD.FromBoolean(GroupDataBlock[i].AcceptNotices));
				group.put("Contribution", OSD.FromInteger(GroupDataBlock[i].Contribution));
				group.put("GroupID", OSD.FromUUID(GroupDataBlock[i].GroupID));
				group.put("GroupInsigniaID", OSD.FromUUID(GroupDataBlock[i].GroupInsigniaID));
				group.put("GroupName", OSD.FromString(GroupDataBlock[i].GroupName));
				group.put("GroupPowers", OSD.FromLong(GroupDataBlock[i].GroupPowers));
				groupDataArray.add(group);
			}

			map.put("GroupData", groupDataArray);

			OSDArray newGroupDataArray = new OSDArray(NewGroupDataBlock.length);

			for (int i = 0; i < NewGroupDataBlock.length; i++)
			{
				OSDMap group = new OSDMap(1);
				group.put("ListInProfile", OSD.FromBoolean(NewGroupDataBlock[i].ListInProfile));
				newGroupDataArray.add(group);
			}

			map.put("NewGroupData", newGroupDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray agentArray = (OSDArray) map.get("AgentData");
			OSDMap agentMap = (OSDMap) agentArray.get(0);
			AgentID = agentMap.get("AgentID").AsUUID();

			OSDArray groupArray = (OSDArray) map.get("GroupData");

			GroupDataBlock = new GroupData[groupArray.size()];

			for (int i = 0; i < groupArray.size(); i++)
			{
				OSDMap groupMap = (OSDMap) groupArray.get(i);

				GroupData groupData = new GroupData();

				groupData.GroupID = groupMap.get("GroupID").AsUUID();
				groupData.Contribution = groupMap.get("Contribution").AsInteger();
				groupData.GroupInsigniaID = groupMap.get("GroupInsigniaID").AsUUID();
				groupData.GroupName = groupMap.get("GroupName").AsString();
				groupData.GroupPowers = groupMap.get("GroupPowers").AsLong();
				groupData.AcceptNotices = groupMap.get("AcceptNotices").AsBoolean();
				GroupDataBlock[i] = groupData;
			}

			// If request for current groups came very close to login
			// the Linden sim will not include the NewGroupData block, but
			// it will instead set all ListInProfile fields to false
			if (map.containsKey("NewGroupData"))
			{
				OSDArray newGroupArray = (OSDArray) map.get("NewGroupData");

				NewGroupDataBlock = new NewGroupData[newGroupArray.size()];

				for (int i = 0; i < newGroupArray.size(); i++)
				{
					OSDMap newGroupMap = (OSDMap) newGroupArray.get(i);
					NewGroupData newGroupData = new NewGroupData();
					newGroupData.ListInProfile = newGroupMap.get("ListInProfile").AsBoolean();
					NewGroupDataBlock[i] = newGroupData;
				}
			}
			else
			{
				NewGroupDataBlock = new NewGroupData[GroupDataBlock.length];
				for (int i = 0; i < NewGroupDataBlock.length; i++)
				{
					NewGroupData newGroupData = new NewGroupData();
					newGroupData.ListInProfile = false;
					NewGroupDataBlock[i] = newGroupData;
				}
			}
		}
	}

	// A message sent from the viewer to the simulator which
	// specifies the language and permissions for others to detect the language
	// specified
	public class UpdateAgentLanguageMessage implements IMessage
	{
		// A string containng the default language to use for the agent
		public String Language;
		// true of others are allowed to know the language setting
		public boolean LanguagePublic;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateAgentLanguage;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);

			map.put("language", OSD.FromString(Language));
			map.put("language_is_public", OSD.FromBoolean(LanguagePublic));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			LanguagePublic = map.get("language_is_public").AsBoolean();
			Language = map.get("language").AsString();
		}
	}

	// An EventQueue message sent from the simulator to an agent when the agent
	// leaves a group
	public class AgentDropGroupMessage implements IMessage
	{
		// An object containing the Agents UUID, and the Groups UUID
		public class AgentData
		{
			// The ID of the Agent leaving the group
			public UUID AgentID;
			// The GroupID the Agent is leaving
			public UUID GroupID;
		}

		// An Array containing the AgentID and GroupID

		public AgentData[] AgentDataBlock;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.AgentDropGroup;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);

			OSDArray agentDataArray = new OSDArray(AgentDataBlock.length);

			for (int i = 0; i < AgentDataBlock.length; i++)
			{
				OSDMap agentMap = new OSDMap(2);
				agentMap.put("AgentID", OSD.FromUUID(AgentDataBlock[i].AgentID));
				agentMap.put("GroupID", OSD.FromUUID(AgentDataBlock[i].GroupID));
				agentDataArray.add(agentMap);
			}
			map.put("AgentData", agentDataArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");

			AgentDataBlock = new AgentData[agentDataArray.size()];

			for (int i = 0; i < agentDataArray.size(); i++)
			{
				OSDMap agentMap = (OSDMap) agentDataArray.get(i);
				AgentData agentData = new AgentData();

				agentData.AgentID = agentMap.get("AgentID").AsUUID();
				agentData.GroupID = agentMap.get("GroupID").AsUUID();

				AgentDataBlock[i] = agentData;
			}
		}
	}

	// Base class for Asset uploads/results via Capabilities
	public abstract class AssetUploaderBlock
	{
		// The request state
		public String State;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	// A message sent from the viewer to the simulator to request a temporary
	// upload capability
	// which allows an asset to be uploaded
	public class UploaderRequestUpload extends AssetUploaderBlock
	{
		// The Capability URL sent by the simulator to upload the baked texture
		// to
		public URI Url;

		public UploaderRequestUpload()
		{
			State = "upload";
		}

		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.FromString(State));
			map.put("uploader", OSD.FromUri(Url));

			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			Url = map.get("uploader").AsUri();
			State = map.get("state").AsString();
		}
	}

	// A message sent from the simulator that will inform the agent the upload
	// is complete, and the UUID of the uploaded asset
	public class UploaderRequestComplete extends AssetUploaderBlock
	{
		// The uploaded texture asset ID
		public UUID AssetID;

		public UploaderRequestComplete()
		{
			State = "complete";
		}

		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.FromString(State));
			map.put("new_asset", OSD.FromUUID(AssetID));

			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			AssetID = map.get("new_asset").AsUUID();
			State = map.get("state").AsString();
		}
	}

	// A message sent from the viewer to the simulator to request a temporary
	// capability URI which is used to upload an agents baked appearance
	// textures
	public class UploadBakedTextureMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UploadBakedTexture;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log(
							"Unable to deserialize UploadBakedTexture: No message handler exists for state " + value,
							LogLevel.Warning);
			}
		}
	}

	// #endregion

	// #region Voice Messages

	// A message sent from the simulator which indicates the minimum version
	// required for using voice chat
	public class RequiredVoiceVersionMessage implements IMessage
	{
		// Major Version Required
		public int MajorVersion;
		// Minor version required
		public int MinorVersion;
		// The name of the region sending the version requrements
		public String RegionName;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.RequiredVoiceVersion;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(4);
			map.put("major_version", OSD.FromInteger(MajorVersion));
			map.put("minor_version", OSD.FromInteger(MinorVersion));
			map.put("region_name", OSD.FromString(RegionName));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			MajorVersion = map.get("major_version").AsInteger();
			MinorVersion = map.get("minor_version").AsInteger();
			RegionName = map.get("region_name").AsString();
		}
	}

	// A message sent from the simulator to the viewer containing the voice
	// server URI
	public class ParcelVoiceInfoRequestMessage implements IMessage
	{
		// The Parcel ID which the voice server URI applies
		public int ParcelID;
		// The name of the region
		public String RegionName;
		// A uri containing the server/channel information which the viewer can
		// utilize to participate in voice conversations
		public URI SipChannelUri;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ParcelVoiceInfoRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("parcel_local_id", OSD.FromInteger(ParcelID));
			map.put("region_name", OSD.FromString(RegionName));

			OSDMap vcMap = new OSDMap(1);
			vcMap.put("channel_uri", OSD.FromUri(SipChannelUri));

			map.put("voice_credentials", vcMap);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ParcelID = map.get("parcel_local_id").AsInteger();
			RegionName = map.get("region_name").AsString();

			OSDMap vcMap = (OSDMap) map.get("voice_credentials");
			SipChannelUri = vcMap.get("channel_uri").AsUri();
		}
	}

	public class ProvisionVoiceAccountRequestMessage implements IMessage
	{
		//
		public String Password;
		//
		public String Username;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ProvisionVoiceAccountRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);

			map.put("username", OSD.FromString(Username));
			map.put("password", OSD.FromString(Password));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Username = map.get("username").AsString();
			Password = map.get("password").AsString();
		}
	}

	// #endregion

	// #region Script/Notecards Messages

	// A message sent by the viewer to the simulator to request a temporary
	// capability for a script contained with in a Tasks inventory to be updated
	public class UploadScriptTaskMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UploadScriptTask;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		public UploadScriptTaskMessage(String value)
		{
			if (value.equals("upload"))
				Request = new UploaderRequestUpload();
			else if (value.equals("complete"))
				Request = new UploaderRequestComplete();
			else
				Logger.Log("Unable to deserialize UploadScriptTask: No message handler exists for state " + value,
						LogLevel.Warning);
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log("Unable to deserialize UploadScriptTask: No message handler exists for state " + value,
							LogLevel.Warning);
			}
		}
	}

	// A message sent from the simulator to the viewer to indicate a Tasks
	// scripts status.
	public class ScriptRunningReplyMessage implements IMessage
	{
		// The Asset ID of the script
		public UUID ItemID;
		// True of the script is compiled/ran using the mono interpreter, false
		// indicates it
		// uses the older less efficient lsl2 interprter
		public boolean Mono;
		// The Task containing the scripts {@link UUID}
		public UUID ObjectID;
		// true of the script is in a running state
		public boolean Running;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ScriptRunningReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);

			OSDMap scriptMap = new OSDMap(4);
			scriptMap.put("ItemID", OSD.FromUUID(ItemID));
			scriptMap.put("Mono", OSD.FromBoolean(Mono));
			scriptMap.put("ObjectID", OSD.FromUUID(ObjectID));
			scriptMap.put("Running", OSD.FromBoolean(Running));

			OSDArray scriptArray = new OSDArray(1);
			scriptArray.add(scriptMap);

			map.put("Script", scriptArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray scriptArray = (OSDArray) map.get("Script");

			OSDMap scriptMap = (OSDMap) scriptArray.get(0);

			ItemID = scriptMap.get("ItemID").AsUUID();
			Mono = scriptMap.get("Mono").AsBoolean();
			ObjectID = scriptMap.get("ObjectID").AsUUID();
			Running = scriptMap.get("Running").AsBoolean();
		}
	}

	// A message containing the request/response used for updating a gesture
	// contained with an agents inventory
	public class UpdateGestureAgentInventoryMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateGestureAgentInventory;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("item_id"))
			{
				Request = new UpdateAgentInventoryRequestMessage();
				Request.Deserialize(map);
			}
			else if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log(
							"Unable to deserialize UpdateGestureAgentInventory: No message handler exists for state "
									+ value, LogLevel.Warning);
			}
			else
				Logger.Log("Unable to deserialize UpdateGestureAgentInventory: No message handler exists for message "
						+ map.AsString(), LogLevel.Warning);
		}
	}

	// A message request/response which is used to update a notecard contained
	// within a tasks inventory
	public class UpdateNotecardTaskInventoryMessage implements IMessage
	{
		// The {@link UUID} of the Task containing the notecard asset to update
		public UUID TaskID;
		// The notecard assets {@link UUID} contained in the tasks inventory
		public UUID ItemID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateNotecardTaskInventory;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("task_id", OSD.FromUUID(TaskID));
			map.put("item_id", OSD.FromUUID(ItemID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			TaskID = map.get("task_id").AsUUID();
			ItemID = map.get("item_id").AsUUID();
		}
	}

	// TODO: Add Test
	//
	// A reusable class containing a message sent from the viewer to the
	// simulator to request a temporary uploader capability
	// which is used to update an asset in an agents inventory
	public class UpdateAgentInventoryRequestMessage extends AssetUploaderBlock
	{
		// The Notecard AssetID to replace
		public UUID ItemID;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("item_id", OSD.FromUUID(ItemID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ItemID = map.get("item_id").AsUUID();
		}
	}

	// A message containing the request/response used for updating a notecard
	// contained with an agents inventory
	public class UpdateNotecardAgentInventoryMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateNotecardAgentInventory;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("item_id"))
			{
				Request = new UpdateAgentInventoryRequestMessage();
				Request.Deserialize(map);
			}
			else if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log(
							"Unable to deserialize UpdateNotecardAgentInventory: No message handler exists for state "
									+ value, LogLevel.Warning);
			}
			else
				Logger.Log("Unable to deserialize UpdateNotecardAgentInventory: No message handler exists for message "
						+ map.toString(), LogLevel.Warning);
		}
	}

	public class CopyInventoryFromNotecardMessage implements IMessage
	{
		public int CallbackID;
		public UUID FolderID;
		public UUID ItemID;
		public UUID NotecardID;
		public UUID ObjectID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.CopyInventoryFromNotecard;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(5);
			map.put("callback-id", OSD.FromInteger(CallbackID));
			map.put("folder-id", OSD.FromUUID(FolderID));
			map.put("item-id", OSD.FromUUID(ItemID));
			map.put("notecard-id", OSD.FromUUID(NotecardID));
			map.put("object-id", OSD.FromUUID(ObjectID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			CallbackID = map.get("callback-id").AsInteger();
			FolderID = map.get("folder-id").AsUUID();
			ItemID = map.get("item-id").AsUUID();
			NotecardID = map.get("notecard-id").AsUUID();
			ObjectID = map.get("object-id").AsUUID();
		}
	}

	// A message sent from the simulator to the viewer which indicates an error
	// occurred while attempting
	// to update a script in an agents or tasks inventory
	public class UploaderScriptRequestError extends AssetUploaderBlock
	{
		// true of the script was successfully compiled by the simulator
		public boolean Compiled;
		// A String containing the error which occured while trying to update
		// the script
		public String Error;
		// A new AssetID assigned to the script
		public UUID AssetID;

		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(4);
			map.put("state", OSD.FromString(State));
			map.put("new_asset", OSD.FromUUID(AssetID));
			map.put("compiled", OSD.FromBoolean(Compiled));

			OSDArray errorsArray = new OSDArray();
			errorsArray.add(OSD.FromString(Error));
			map.put("errors", errorsArray);
			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			AssetID = map.get("new_asset").AsUUID();
			Compiled = map.get("compiled").AsBoolean();
			State = map.get("state").AsString();

			OSDArray errorsArray = (OSDArray) map.get("errors");
			Error = errorsArray.get(0).AsString();
		}
	}

	// A message sent from the viewer to the simulator requesting the update of
	// an existing script contained
	// within a tasks inventory
	public class UpdateScriptTaskUpdateMessage extends AssetUploaderBlock
	{
		// if true, set the script mode to running
		public boolean ScriptRunning;
		// The scripts InventoryItem ItemID to update
		public UUID ItemID;
		// A lowercase string containing either "mono" or "lsl2" which specifies
		// the script is compiled
		// and ran on the mono runtime, or the older lsl runtime
		public String Target; // mono or lsl2
		// The tasks <see cref="UUID"/> which contains the script to update
		public UUID TaskID;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(4);
			map.put("is_script_running", OSD.FromBoolean(ScriptRunning));
			map.put("item_id", OSD.FromUUID(ItemID));
			map.put("target", OSD.FromString(Target));
			map.put("task_id", OSD.FromUUID(TaskID));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ScriptRunning = map.get("is_script_running").AsBoolean();
			ItemID = map.get("item_id").AsUUID();
			Target = map.get("target").AsString();
			TaskID = map.get("task_id").AsUUID();
		}
	}

	// A message containing either the request or response used in updating a
	// script inside a tasks inventory
	public class UpdateScriptTaskMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateScriptTask;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		public void GetMessageHandler(OSDMap map)
		{
			String value = map.get("method").AsString();
			if (value.equals("task_id"))
			{
				Request = new UpdateScriptTaskUpdateMessage();
			}
			else if (value.equals("upload"))
			{
				Request = new UploaderRequestUpload();
			}
			else if (value.equals("errors"))
			{
				Request = new UploaderScriptRequestError();
			}
			else if (value.equals("complete"))
			{
				Request = new UploaderRequestScriptComplete();
			}
			else
				Logger.Log("Unable to deserialize UpdateScriptTaskMessage: No message handler exists for state "
						+ value, LogLevel.Warning);
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("task_id"))
			{
				Request = new UpdateScriptTaskUpdateMessage();
				Request.Deserialize(map);
			}
			else if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete") && map.containsKey("errors"))
				{
					Request = new UploaderScriptRequestError();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestScriptComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log("Unable to deserialize UpdateScriptTaskMessage: No message handler exists for state "
							+ value, LogLevel.Warning);
			}
			else
				Logger.Log("Unable to deserialize UpdateScriptTaskMessage: No message handler exists for message "
						+ map.AsString(), LogLevel.Warning);
		}
	}

	// Response from the simulator to notify the viewer the upload is completed,
	// and the UUID of the script asset and its compiled status
	public class UploaderRequestScriptComplete extends AssetUploaderBlock
	{
		// The uploaded texture asset ID
		public UUID AssetID;
		// true of the script was compiled successfully
		public boolean Compiled;

		public UploaderRequestScriptComplete()
		{
			State = "complete";
		}

		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("state", OSD.FromString(State));
			map.put("new_asset", OSD.FromUUID(AssetID));
			map.put("compiled", OSD.FromBoolean(Compiled));
			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			AssetID = map.get("new_asset").AsUUID();
			Compiled = map.get("compiled").AsBoolean();
		}
	}

	// A message sent from a viewer to the simulator requesting a temporary
	// uploader capability used to update a script contained in an agents
	// inventory
	public class UpdateScriptAgentRequestMessage extends AssetUploaderBlock
	{
		// The existing asset if of the script in the agents inventory to
		// replace
		public UUID ItemID;
		// The language of the script
		// Defaults to lsl version 2, "mono" might be another possible option
		public String Target = "lsl2"; // lsl2

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("item_id", OSD.FromUUID(ItemID));
			map.put("target", OSD.FromString(Target));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ItemID = map.get("item_id").AsUUID();
			Target = map.get("target").AsString();
		}
	}

	// A message containing either the request or response used in updating a
	// script inside an agents inventory
	public class UpdateScriptAgentMessage implements IMessage
	{
		// Object containing request or response
		public AssetUploaderBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateScriptAgent;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("item_id"))
			{
				Request = new UpdateScriptAgentRequestMessage();
				Request.Deserialize(map);
			}
			else if (map.containsKey("errors"))
			{
				Request = new UploaderScriptRequestError();
				Request.Deserialize(map);
			}
			else if (map.containsKey("state"))
			{
				String value = map.get("state").AsString();
				if (value.equals("upload"))
				{
					Request = new UploaderRequestUpload();
					Request.Deserialize(map);
				}
				else if (value.equals("complete"))
				{
					Request = new UploaderRequestScriptComplete();
					Request.Deserialize(map);
				}
				else
					Logger.Log("Unable to deserialize UpdateScriptAgent: No message handler exists for state " + value,
							LogLevel.Warning);
			}
			else
				Logger.Log(
						"Unable to deserialize UpdateScriptAgent: No message handler exists for message "
								+ map.AsString(), LogLevel.Warning);
		}
	}

	public class SendPostcardMessage implements IMessage
	{
		public String FromEmail;
		public String Message;
		public String FromName;
		public Vector3 GlobalPosition;
		public String Subject;
		public String ToEmail;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.SendPostcard;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(6);
			map.put("from", OSD.FromString(FromEmail));
			map.put("msg", OSD.FromString(Message));
			map.put("name", OSD.FromString(FromName));
			map.put("pos-global", OSD.FromVector3(GlobalPosition));
			map.put("subject", OSD.FromString(Subject));
			map.put("to", OSD.FromString(ToEmail));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			FromEmail = map.get("from").AsString();
			Message = map.get("msg").AsString();
			FromName = map.get("name").AsString();
			GlobalPosition = map.get("pos-global").AsVector3();
			Subject = map.get("subject").AsString();
			ToEmail = map.get("to").AsString();
		}
	}

	// #endregion

	// #region Grid/Maps

	// Base class for Map Layers via Capabilities
	public abstract class MapLayerMessageBase
	{
		//
		public int Flags;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	// Sent by an agent to the capabilities server to request map layers
	public class MapLayerRequestVariant extends MapLayerMessageBase
	{
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("Flags", OSD.FromInteger(Flags));
			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			Flags = map.get("Flags").AsInteger();
		}
	}

	// A message sent from the simulator to the viewer which contains an array
	// of map images and their grid coordinates
	public class MapLayerReplyVariant extends MapLayerMessageBase
	{
		// An object containing map location details
		public class LayerData
		{
			// The Asset ID of the regions tile overlay
			public UUID ImageID;
			// The grid location of the southern border of the map tile
			public int Bottom;
			// The grid location of the western border of the map tile
			public int Left;
			// The grid location of the eastern border of the map tile
			public int Right;
			// The grid location of the northern border of the map tile
			public int Top;
		}

		// An array containing LayerData items
		public LayerData[] LayerDataBlocks;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			OSDMap agentMap = new OSDMap(1);
			agentMap.put("Flags", OSD.FromInteger(Flags));
			map.put("AgentData", agentMap);

			OSDArray layerArray = new OSDArray(LayerDataBlocks.length);

			for (int i = 0; i < LayerDataBlocks.length; i++)
			{
				OSDMap layerMap = new OSDMap(5);
				layerMap.put("ImageID", OSD.FromUUID(LayerDataBlocks[i].ImageID));
				layerMap.put("Bottom", OSD.FromInteger(LayerDataBlocks[i].Bottom));
				layerMap.put("Left", OSD.FromInteger(LayerDataBlocks[i].Left));
				layerMap.put("Top", OSD.FromInteger(LayerDataBlocks[i].Top));
				layerMap.put("Right", OSD.FromInteger(LayerDataBlocks[i].Right));

				layerArray.add(layerMap);
			}

			map.put("LayerData", layerArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDMap agentMap = (OSDMap) map.get("AgentData");
			Flags = agentMap.get("Flags").AsInteger();

			OSDArray layerArray = (OSDArray) map.get("LayerData");

			LayerDataBlocks = new LayerData[layerArray.size()];

			for (int i = 0; i < LayerDataBlocks.length; i++)
			{
				OSDMap layerMap = (OSDMap) layerArray.get(i);

				LayerData layer = new LayerData();
				layer.ImageID = layerMap.get("ImageID").AsUUID();
				layer.Top = layerMap.get("Top").AsInteger();
				layer.Right = layerMap.get("Right").AsInteger();
				layer.Left = layerMap.get("Left").AsInteger();
				layer.Bottom = layerMap.get("Bottom").AsInteger();

				LayerDataBlocks[i] = layer;
			}
		}
	}

	public class MapLayerMessage implements IMessage
	{
		// Object containing request or response
		public MapLayerMessageBase Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.MapLayer;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("LayerData"))
			{
				Request = new MapLayerReplyVariant();
				Request.Deserialize(map);
			}
			else if (map.containsKey("Flags"))
			{
				Request = new MapLayerRequestVariant();
				Request.Deserialize(map);
			}
			else
				Logger.Log("Unable to deserialize MapLayerMessage: No message handler exists", LogLevel.Warning);
		}
	}

	// #endregion

	// #region Session/Communication

	// New as of 1.23 RC1, no details yet.
	public class ProductInfoRequestMessage implements IMessage
	{
		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ProductInfoRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			throw new UnsupportedOperationException();
		}
	}

	// #region ChatSessionRequestMessage

	public abstract class SearchStatRequestBlock
	{
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	// variant A - the request to the simulator
	public class SearchStatRequestRequest extends SearchStatRequestBlock
	{
		public UUID ClassifiedID;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("classified_id", OSD.FromUUID(ClassifiedID));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ClassifiedID = map.get("classified_id").AsUUID();
		}
	}

	public class SearchStatRequestReply extends SearchStatRequestBlock
	{
		public int MapClicks;
		public int ProfileClicks;
		public int SearchMapClicks;
		public int SearchProfileClicks;
		public int SearchTeleportClicks;
		public int TeleportClicks;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(6);
			map.put("map_clicks", OSD.FromInteger(MapClicks));
			map.put("profile_clicks", OSD.FromInteger(ProfileClicks));
			map.put("search_map_clicks", OSD.FromInteger(SearchMapClicks));
			map.put("search_profile_clicks", OSD.FromInteger(SearchProfileClicks));
			map.put("search_teleport_clicks", OSD.FromInteger(SearchTeleportClicks));
			map.put("teleport_clicks", OSD.FromInteger(TeleportClicks));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			MapClicks = map.get("map_clicks").AsInteger();
			ProfileClicks = map.get("profile_clicks").AsInteger();
			SearchMapClicks = map.get("search_map_clicks").AsInteger();
			SearchProfileClicks = map.get("search_profile_clicks").AsInteger();
			SearchTeleportClicks = map.get("search_teleport_clicks").AsInteger();
			TeleportClicks = map.get("teleport_clicks").AsInteger();
		}
	}

	public class SearchStatRequestMessage implements IMessage
	{
		public SearchStatRequestBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.SearchStatRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("map_clicks"))
			{
				Request = new SearchStatRequestReply();
				Request.Deserialize(map);
			}
			else if (map.containsKey("classified_id"))
			{
				Request = new SearchStatRequestRequest();
				Request.Deserialize(map);
			}
			else
				Logger.Log(
						"Unable to deserialize SearchStatRequest: No message handler exists for method "
								+ map.get("method").AsString(), LogLevel.Warning);
		}
	}

	public abstract class ChatSessionRequestBlock
	{
		// A string containing the method used
		public String Method;

		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	/*
	 * A request sent from an agent to the Simulator to begin a new conference.
	 * Contains a list of Agents which will be included in the conference
	 */
	public class ChatSessionRequestStartConference extends ChatSessionRequestBlock
	{
		// An array containing the <see cref="UUID"/> of the agents invited to
		// this conference
		public UUID[] AgentsBlock;
		// The conferences Session ID
		public UUID SessionID;

		public ChatSessionRequestStartConference()
		{
			Method = "start conference";
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("method", OSD.FromString(Method));
			OSDArray agentsArray = new OSDArray();
			for (int i = 0; i < AgentsBlock.length; i++)
			{
				agentsArray.add(OSD.FromUUID(AgentsBlock[i]));
			}
			map.put("params", agentsArray);
			map.put("session-id", OSD.FromUUID(SessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Method = map.get("method").AsString();
			OSDArray agentsArray = (OSDArray) map.get("params");

			AgentsBlock = new UUID[agentsArray.size()];

			for (int i = 0; i < agentsArray.size(); i++)
			{
				AgentsBlock[i] = agentsArray.get(i).AsUUID();
			}

			SessionID = map.get("session-id").AsUUID();
		}
	}

	/*
	 * A moderation request sent from a conference moderator Contains an agent
	 * and an optional action to take
	 */
	public class ChatSessionRequestMuteUpdate extends ChatSessionRequestBlock
	{
		// The Session ID
		public UUID SessionID;
		public UUID AgentID;
		/*
		 * A list containing Key/Value pairs, known valid values: key: text
		 * value: true/false - allow/disallow specified agents ability to use
		 * text in session key: voice value: true/false - allow/disallow
		 * specified agents ability to use voice in session
		 * 
		 * "text" or "voice"
		 */
		public String RequestKey;
		public boolean RequestValue;

		public ChatSessionRequestMuteUpdate()
		{
			Method = "mute update";
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("method", OSD.FromString(Method));

			OSDMap muteMap = new OSDMap(1);
			muteMap.put(RequestKey, OSD.FromBoolean(RequestValue));

			OSDMap paramMap = new OSDMap(2);
			paramMap.put("agent_id", OSD.FromUUID(AgentID));
			paramMap.put("mute_info", muteMap);

			map.put("params", paramMap);
			map.put("session-id", OSD.FromUUID(SessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Method = map.get("method").AsString();
			SessionID = map.get("session-id").AsUUID();

			OSDMap paramsMap = (OSDMap) map.get("params");
			OSDMap muteMap = (OSDMap) paramsMap.get("mute_info");

			AgentID = paramsMap.get("agent_id").AsUUID();

			if (muteMap.containsKey("text"))
				RequestKey = "text";
			else if (muteMap.containsKey("voice"))
				RequestKey = "voice";

			RequestValue = muteMap.get(RequestKey).AsBoolean();
		}
	}

	// A message sent from the agent to the simulator which tells the simulator
	// we've accepted a conference invitation
	public class ChatSessionAcceptInvitation extends ChatSessionRequestBlock
	{
		// The conference SessionID
		public UUID SessionID;

		public ChatSessionAcceptInvitation()
		{
			Method = "accept invitation";
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("method", OSD.FromString(Method));
			map.put("session-id", OSD.FromUUID(SessionID));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Method = map.get("method").AsString();
			SessionID = map.get("session-id").AsUUID();
		}
	}

	public class ChatSessionRequestMessage implements IMessage
	{
		public ChatSessionRequestBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ChatSessionRequest;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("method"))
			{
				String value = map.get("method").AsString();
				if (value.equals("start conference"))
				{
					Request = new ChatSessionRequestStartConference();
					Request.Deserialize(map);
				}
				else if (value.equals("mute update"))
				{
					Request = new ChatSessionRequestMuteUpdate();
					Request.Deserialize(map);
				}
				else if (value.equals("accept invitation"))
				{
					Request = new ChatSessionAcceptInvitation();
					Request.Deserialize(map);
				}
				else
					Logger.Log("Unable to deserialize ChatSessionRequest: No message handler exists for method "
							+ value, LogLevel.Warning);
			}
			else
				Logger.Log(
						"Unable to deserialize ChatSessionRequest: No message handler exists for message "
								+ map.AsString(), LogLevel.Warning);
		}
	}

	// #endregion

	public class ChatterBoxSessionEventReplyMessage implements IMessage
	{
		public UUID SessionID;
		public boolean Success;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ChatterBoxSessionEventReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("success", OSD.FromBoolean(Success));
			map.put("session_id", OSD.FromUUID(SessionID)); // FIXME: Verify
															// this is correct
															// map name

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Success = map.get("success").AsBoolean();
			SessionID = map.get("session_id").AsUUID();
		}
	}

	public class ChatterBoxSessionStartReplyMessage implements IMessage
	{
		public UUID SessionID;
		public UUID TempSessionID;
		public boolean Success;

		public String SessionName;
		// FIXME: Replace int with an enum
		public int Type;
		public boolean VoiceEnabled;
		public boolean ModeratedVoice;

		// Is Text moderation possible?

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ChatterBoxSessionStartReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap moderatedMap = new OSDMap(1);
			moderatedMap.put("voice", OSD.FromBoolean(ModeratedVoice));

			OSDMap sessionMap = new OSDMap(4);
			sessionMap.put("type", OSD.FromInteger(Type));
			sessionMap.put("session_name", OSD.FromString(SessionName));
			sessionMap.put("voice_enabled", OSD.FromBoolean(VoiceEnabled));
			sessionMap.put("moderated_mode", moderatedMap);

			OSDMap map = new OSDMap(4);
			map.put("session_id", OSD.FromUUID(SessionID));
			map.put("temp_session_id", OSD.FromUUID(TempSessionID));
			map.put("success", OSD.FromBoolean(Success));
			map.put("session_info", sessionMap);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			SessionID = map.get("session_id").AsUUID();
			TempSessionID = map.get("temp_session_id").AsUUID();
			Success = map.get("success").AsBoolean();

			if (Success)
			{
				OSDMap sessionMap = (OSDMap) map.get("session_info");
				SessionName = sessionMap.get("session_name").AsString();
				Type = sessionMap.get("type").AsInteger();
				VoiceEnabled = sessionMap.get("voice_enabled").AsBoolean();

				OSDMap moderatedModeMap = (OSDMap) sessionMap.get("moderated_mode");
				ModeratedVoice = moderatedModeMap.get("voice").AsBoolean();
			}
		}
	}

	public class ChatterBoxInvitationMessage implements IMessage
	{
		// Key of sender
		public UUID FromAgentID;
		// Name of sender
		public String FromAgentName;
		// Key of destination avatar
		public UUID ToAgentID;
		// ID of originating estate
		public int ParentEstateID;
		// Key of originating region
		public UUID RegionID;
		// Coordinates in originating region
		public Vector3 Position;
		// Instant message type
		public InstantMessageDialog Dialog;
		// Group IM session toggle
		public boolean GroupIM;
		// Key of IM session, for Group Messages, the groups UUID
		public UUID IMSessionID;
		// Timestamp of the instant message
		public Date Timestamp;
		// Instant message text
		public String Message;
		// Whether this message is held for offline avatars
		public InstantMessageOnline Offline;
		// Context specific packed data
		public byte[] BinaryBucket;
		// Is this invitation for voice group/conference chat
		public boolean Voice;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ChatterBoxInvitation;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap dataMap = new OSDMap(3);
			dataMap.put("timestamp", OSD.FromDate(Timestamp));
			dataMap.put("type", OSD.FromInteger(Dialog.getValue()));
			dataMap.put("binary_bucket", OSD.FromBinary(BinaryBucket));

			OSDMap paramsMap = new OSDMap(11);
			paramsMap.put("from_id", OSD.FromUUID(FromAgentID));
			paramsMap.put("from_name", OSD.FromString(FromAgentName));
			paramsMap.put("to_id", OSD.FromUUID(ToAgentID));
			paramsMap.put("parent_estate_id", OSD.FromInteger(ParentEstateID));
			paramsMap.put("region_id", OSD.FromUUID(RegionID));
			paramsMap.put("position", OSD.FromVector3(Position));
			paramsMap.put("from_group", OSD.FromBoolean(GroupIM));
			paramsMap.put("id", OSD.FromUUID(IMSessionID));
			paramsMap.put("message", OSD.FromString(Message));
			paramsMap.put("offline", OSD.FromInteger(Offline.getValue()));

			paramsMap.put("data", dataMap);

			OSDMap imMap = new OSDMap(1);
			imMap.put("message_params", paramsMap);

			OSDMap map = new OSDMap(1);
			map.put("instantmessage", imMap);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("voice"))
			{
				FromAgentID = map.get("from_id").AsUUID();
				FromAgentName = map.get("from_name").AsString();
				IMSessionID = map.get("session_id").AsUUID();
				BinaryBucket = Helpers.StringToBytes(map.get("session_name").AsString());
				Voice = true;
			}
			else
			{
				OSDMap im = (OSDMap) map.get("instantmessage");
				OSDMap msg = (OSDMap) im.get("message_params");
				OSDMap msgdata = (OSDMap) msg.get("data");

				FromAgentID = msg.get("from_id").AsUUID();
				FromAgentName = msg.get("from_name").AsString();
				ToAgentID = msg.get("to_id").AsUUID();
				ParentEstateID = msg.get("parent_estate_id").AsInteger();
				RegionID = msg.get("region_id").AsUUID();
				Position = msg.get("position").AsVector3();
				GroupIM = msg.get("from_group").AsBoolean();
				IMSessionID = msg.get("id").AsUUID();
				Message = msg.get("message").AsString();
				Offline = InstantMessageOnline.setValue(msg.get("offline").AsInteger());
				Dialog = InstantMessageDialog.setValue(msgdata.get("type").AsInteger());
				BinaryBucket = msgdata.get("binary_bucket").AsBinary();
				Timestamp = msgdata.get("timestamp").AsDate();
				Voice = false;
			}
		}
	}

	public class RegionInfoMessage implements IMessage
	{
		public int ParcelLocalID;
		public String RegionName;
		public String ChannelUri;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.RegionInfo;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);
			map.put("parcel_local_id", OSD.FromInteger(ParcelLocalID));
			map.put("region_name", OSD.FromString(RegionName));
			OSDMap voiceMap = new OSDMap(1);
			voiceMap.put("channel_uri", OSD.FromString(ChannelUri));
			map.put("voice_credentials", voiceMap);
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			this.ParcelLocalID = map.get("parcel_local_id").AsInteger();
			this.RegionName = map.get("region_name").AsString();
			OSDMap voiceMap = (OSDMap) map.get("voice_credentials");
			this.ChannelUri = voiceMap.get("channel_uri").AsString();
		}
	}

	// Sent from the simulator to the viewer.
	//
	// When an agent initially joins a session the AgentUpdatesBlock object will
	// contain a list of session members including
	// a boolean indicating they can use voice chat in this session, a boolean
	// indicating they are allowed to moderate
	// this session, and lastly a string which indicates another agent is
	// entering the session with the Transition set to "ENTER"
	//
	// During the session lifetime updates on individuals are sent. During the
	// update the booleans sent during the initial join are
	// excluded with the exception of the Transition field. This indicates a new
	// user entering or exiting the session with
	// the string "ENTER" or "LEAVE" respectively.
	public class ChatterBoxSessionAgentListUpdatesMessage implements IMessage
	{
		// initial when agent joins session
		// <llsd><map><key>events</key><array><map><key>body</key><map><key>agent_updates</key><map><key>32939971-a520-4b52-8ca5-6085d0e39933</key><map><key>info</key><map><key>can_voice_chat</key><boolean>1</boolean><key>is_moderator</key><boolean>1</boolean></map><key>transition</key><string>ENTER</string></map><key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key><map><key>info</key><map><key>can_voice_chat</key><boolean>1</boolean><key>is_moderator</key><boolean>0</boolean></map><key>transition</key><string>ENTER</string></map></map><key>session_id</key><string>be7a1def-bd8a-5043-5d5b-49e3805adf6b</string><key>updates</key><map><key>32939971-a520-4b52-8ca5-6085d0e39933</key><string>ENTER</string><key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key><string>ENTER</string></map></map><key>message</key><string>ChatterBoxSessionAgentListUpdates</string></map><map><key>body</key><map><key>agent_updates</key><map><key>32939971-a520-4b52-8ca5-6085d0e39933</key><map><key>info</key><map><key>can_voice_chat</key><boolean>1</boolean><key>is_moderator</key><boolean>1</boolean></map></map></map><key>session_id</key><string>be7a1def-bd8a-5043-5d5b-49e3805adf6b</string><key>updates</key><map
		// /></map><key>message</key><string>ChatterBoxSessionAgentListUpdates</string></map></array><key>id</key><integer>5</integer></map></llsd>

		// a message containing only moderator updates
		// <llsd><map><key>events</key><array><map><key>body</key><map><key>agent_updates</key><map><key>ca00e3e1-0fdb-4136-8ed4-0aab739b29e8</key><map><key>info</key><map><key>mutes</key><map><key>text</key><boolean>1</boolean></map></map></map></map><key>session_id</key><string>be7a1def-bd8a-5043-5d5b-49e3805adf6b</string><key>updates</key><map
		// /></map><key>message</key><string>ChatterBoxSessionAgentListUpdates</string></map></array><key>id</key><integer>7</integer></map></llsd>

		public UUID SessionID;

		public class AgentUpdatesBlock
		{
			public UUID AgentID;

			public boolean CanVoiceChat;
			public boolean IsModerator;
			// transition "transition" = "ENTER" or "LEAVE"
			public String Transition; // TODO: switch to an enum "ENTER" or
										// "LEAVE"

			public boolean MuteText;
			public boolean MuteVoice;
		}

		public AgentUpdatesBlock[] Updates;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ChatterBoxSessionAgentListUpdates;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap();

			OSDMap agent_updatesMap = new OSDMap(1);
			for (int i = 0; i < Updates.length; i++)
			{
				OSDMap mutesMap = new OSDMap(2);
				mutesMap.put("text", OSD.FromBoolean(Updates[i].MuteText));
				mutesMap.put("voice", OSD.FromBoolean(Updates[i].MuteVoice));

				OSDMap infoMap = new OSDMap(4);
				infoMap.put("can_voice_chat", OSD.FromBoolean(Updates[i].CanVoiceChat));
				infoMap.put("is_moderator", OSD.FromBoolean(Updates[i].IsModerator));
				infoMap.put("mutes", mutesMap);

				OSDMap imap = new OSDMap(1);
				imap.put("info", infoMap);
				imap.put("transition", OSD.FromString(Updates[i].Transition));

				agent_updatesMap.put(Updates[i].AgentID.toString(), imap);
			}
			map.put("agent_updates", agent_updatesMap);
			map.put("session_id", OSD.FromUUID(SessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{

			OSDMap agent_updates = (OSDMap) map.get("agent_updates");
			SessionID = map.get("session_id").AsUUID();

			ArrayList<AgentUpdatesBlock> updatesList = new ArrayList<AgentUpdatesBlock>();

			for (Entry<String, OSD> kvp : agent_updates.entrySet())
			{

				if (kvp.getKey().equals("updates"))
				{
					// This appears to be redundant and duplicated by the info
					// block, more dumps will confirm this
					// <key>32939971-a520-4b52-8ca5-6085d0e39933</key>
					// <string>ENTER</string>
				}
				else if (kvp.getKey().equals("session_id"))
				{
					// I am making the assumption that each osdmap will contain
					// the information for a
					// single session. This is how the map appears to read
					// however more dumps should be taken
					// to confirm this.
					// <key>session_id</key>
					// <string>984f6a1e-4ceb-6366-8d5e-a18c6819c6f7</string>

				}
				else
				// key is an agent uuid (we hope!)
				{
					// should be the agents uuid as the key, and "info" as the
					// datablock
					// <key>32939971-a520-4b52-8ca5-6085d0e39933</key>
					// <map>
					// <key>info</key>
					// <map>
					// <key>can_voice_chat</key>
					// <boolean>1</boolean>
					// <key>is_moderator</key>
					// <boolean>1</boolean>
					// </map>
					// <key>transition</key>
					// <string>ENTER</string>
					// </map>
					AgentUpdatesBlock block = new AgentUpdatesBlock();
					block.AgentID = UUID.Parse(kvp.getKey());

					OSDMap infoMap = (OSDMap) kvp.getValue();

					OSDMap agentPermsMap = (OSDMap) infoMap.get("info");

					block.CanVoiceChat = agentPermsMap.get("can_voice_chat").AsBoolean();
					block.IsModerator = agentPermsMap.get("is_moderator").AsBoolean();

					block.Transition = infoMap.get("transition").AsString();

					if (agentPermsMap.containsKey("mutes"))
					{
						OSDMap mutesMap = (OSDMap) agentPermsMap.get("mutes");
						block.MuteText = mutesMap.get("text").AsBoolean();
						block.MuteVoice = mutesMap.get("voice").AsBoolean();
					}
					updatesList.add(block);
				}
			}

			Updates = new AgentUpdatesBlock[updatesList.size()];

			for (int i = 0; i < updatesList.size(); i++)
			{
				AgentUpdatesBlock block = new AgentUpdatesBlock();
				block.AgentID = updatesList.get(i).AgentID;
				block.CanVoiceChat = updatesList.get(i).CanVoiceChat;
				block.IsModerator = updatesList.get(i).IsModerator;
				block.MuteText = updatesList.get(i).MuteText;
				block.MuteVoice = updatesList.get(i).MuteVoice;
				block.Transition = updatesList.get(i).Transition;
				Updates[i] = block;
			}
		}
	}

	// An EventQueue message sent when the agent is forcibly removed from a
	// chatterbox session
	public class ForceCloseChatterBoxSessionMessage implements IMessage
	{
		// A string containing the reason the agent was removed
		public String Reason;
		// The ChatterBoxSession's SessionID
		public UUID SessionID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ForceCloseChatterBoxSession;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("reason", OSD.FromString(Reason));
			map.put("session_id", OSD.FromUUID(SessionID));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Reason = map.get("reason").AsString();
			SessionID = map.get("session_id").AsUUID();
		}
	}

	// #endregion

	// #region EventQueue

	public abstract class EventMessageBlock
	{
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	public class EventQueueAck extends EventMessageBlock
	{
		public int AckID;
		public boolean Done;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap();
			map.put("ack", OSD.FromInteger(AckID));
			map.put("done", OSD.FromBoolean(Done));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			AckID = map.get("ack").AsInteger();
			Done = map.get("done").AsBoolean();
		}
	}

	public class EventQueueEvent extends EventMessageBlock
	{
		public class QueueEvent
		{
			public CapsMessage EventMessage;
			public CapsEventType MessageKey;
		}

		public int Sequence;
		public QueueEvent[] MessageEvents;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);

			OSDArray eventsArray = new OSDArray();

			for (int i = 0; i < MessageEvents.length; i++)
			{
				OSDMap eventMap = new OSDMap(2);
				eventMap.put("body", MessageEvents[i].EventMessage.Serialize());
				eventMap.put("message", OSD.FromString(MessageEvents[i].MessageKey.toString()));
				eventsArray.add(eventMap);
			}

			map.put("events", eventsArray);
			map.put("id", OSD.FromInteger(Sequence));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			Sequence = map.get("id").AsInteger();
			OSDArray arrayEvents = (OSDArray) map.get("events");

			MessageEvents = new QueueEvent[arrayEvents.size()];

			for (int i = 0; i < arrayEvents.size(); i++)
			{
				OSDMap eventMap = (OSDMap) arrayEvents.get(i);
				QueueEvent ev = new QueueEvent();

				ev.MessageKey = CapsEventType.valueOf(eventMap.get("message").AsString());
				ev.EventMessage = (CapsMessage) DecodeEvent(ev.MessageKey, (OSDMap) eventMap.get("body"));
				MessageEvents[i] = ev;
			}
		}
	}

	public class EventQueueGetMessage implements IMessage
	{
		public EventMessageBlock Messages;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.EventQueueGet;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Messages.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("ack"))
			{
				Messages = new EventQueueAck();
				Messages.Deserialize(map);
			}
			else if (map.containsKey("events"))
			{
				Messages = new EventQueueEvent();
				Messages.Deserialize(map);
			}
			else
				Logger.Log("Unable to deserialize EventQueueGetMessage: No message handler exists for event",
						LogLevel.Warning);
		}
	}

	// #endregion

	// #region Stats Messages

	public class ViewerStatsMessage implements IMessage
	{
		public int AgentsInView;
		public float AgentFPS;
		public String AgentLanguage;
		public float AgentMemoryUsed;
		public float MetersTraveled;
		public float AgentPing;
		public int RegionsVisited;
		public float AgentRuntime;
		public float SimulatorFPS;
		public Date AgentStartTime;
		public String AgentVersion;

		public float object_kbytes;
		public float texture_kbytes;
		public float world_kbytes;

		public float MiscVersion;
		public boolean VertexBuffersEnabled;

		public UUID SessionID;

		public int StatsDropped;
		public int StatsFailedResends;
		public int FailuresInvalid;
		public int FailuresOffCircuit;
		public int FailuresResent;
		public int FailuresSendPacket;

		public int MiscInt1;
		public int MiscInt2;
		public String MiscString1;

		public int InCompressedPackets;
		public float InKbytes;
		public float InPackets;
		public float InSavings;

		public int OutCompressedPackets;
		public float OutKbytes;
		public float OutPackets;
		public float OutSavings;

		public String SystemCPU;
		public String SystemGPU;
		public int SystemGPUClass;
		public String SystemGPUVendor;
		public String SystemGPUVersion;
		public String SystemOS;
		public int SystemInstalledRam;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ViewerStats;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(5);
			map.put("session_id", OSD.FromUUID(SessionID));

			OSDMap agentMap = new OSDMap(11);
			agentMap.put("agents_in_view", OSD.FromInteger(AgentsInView));
			agentMap.put("fps", OSD.FromReal(AgentFPS));
			agentMap.put("language", OSD.FromString(AgentLanguage));
			agentMap.put("mem_use", OSD.FromReal(AgentMemoryUsed));
			agentMap.put("meters_traveled", OSD.FromReal(MetersTraveled));
			agentMap.put("ping", OSD.FromReal(AgentPing));
			agentMap.put("regions_visited", OSD.FromInteger(RegionsVisited));
			agentMap.put("run_time", OSD.FromReal(AgentRuntime));
			agentMap.put("sim_fps", OSD.FromReal(SimulatorFPS));
			agentMap.put("start_time", OSD.FromUInteger((int) (long) Helpers.DateTimeToUnixTime(AgentStartTime)));
			agentMap.put("version", OSD.FromString(AgentVersion));
			map.put("agent", agentMap);

			OSDMap downloadsMap = new OSDMap(3); // downloads
			downloadsMap.put("object_kbytes", OSD.FromReal(object_kbytes));
			downloadsMap.put("texture_kbytes", OSD.FromReal(texture_kbytes));
			downloadsMap.put("world_kbytes", OSD.FromReal(world_kbytes));
			map.put("downloads", downloadsMap);

			OSDMap miscMap = new OSDMap(2);
			miscMap.put("Version", OSD.FromReal(MiscVersion));
			miscMap.put("Vertex Buffers Enabled", OSD.FromBoolean(VertexBuffersEnabled));
			map.put("misc", miscMap);

			OSDMap statsMap = new OSDMap(2);

			OSDMap failuresMap = new OSDMap(6);
			failuresMap.put("dropped", OSD.FromInteger(StatsDropped));
			failuresMap.put("failed_resends", OSD.FromInteger(StatsFailedResends));
			failuresMap.put("invalid", OSD.FromInteger(FailuresInvalid));
			failuresMap.put("off_circuit", OSD.FromInteger(FailuresOffCircuit));
			failuresMap.put("resent", OSD.FromInteger(FailuresResent));
			failuresMap.put("send_packet", OSD.FromInteger(FailuresSendPacket));
			statsMap.put("failures", failuresMap);

			OSDMap statsMiscMap = new OSDMap(3);
			statsMiscMap.put("int_1", OSD.FromInteger(MiscInt1));
			statsMiscMap.put("int_2", OSD.FromInteger(MiscInt2));
			statsMiscMap.put("string_1", OSD.FromString(MiscString1));
			statsMap.put("misc", statsMiscMap);

			OSDMap netMap = new OSDMap(3);

			// in
			OSDMap netInMap = new OSDMap(4);
			netInMap.put("compressed_packets", OSD.FromInteger(InCompressedPackets));
			netInMap.put("kbytes", OSD.FromReal(InKbytes));
			netInMap.put("packets", OSD.FromReal(InPackets));
			netInMap.put("savings", OSD.FromReal(InSavings));
			netMap.put("in", netInMap);
			// out
			OSDMap netOutMap = new OSDMap(4);
			netOutMap.put("compressed_packets", OSD.FromInteger(OutCompressedPackets));
			netOutMap.put("kbytes", OSD.FromReal(OutKbytes));
			netOutMap.put("packets", OSD.FromReal(OutPackets));
			netOutMap.put("savings", OSD.FromReal(OutSavings));
			netMap.put("out", netOutMap);

			statsMap.put("net", netMap);

			// system
			OSDMap systemStatsMap = new OSDMap(7);
			systemStatsMap.put("cpu", OSD.FromString(SystemCPU));
			systemStatsMap.put("gpu", OSD.FromString(SystemGPU));
			systemStatsMap.put("gpu_class", OSD.FromInteger(SystemGPUClass));
			systemStatsMap.put("gpu_vendor", OSD.FromString(SystemGPUVendor));
			systemStatsMap.put("gpu_version", OSD.FromString(SystemGPUVersion));
			systemStatsMap.put("os", OSD.FromString(SystemOS));
			systemStatsMap.put("ram", OSD.FromInteger(SystemInstalledRam));
			map.put("system", systemStatsMap);

			map.put("stats", statsMap);
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			SessionID = map.get("session_id").AsUUID();

			OSDMap agentMap = (OSDMap) map.get("agent");
			AgentsInView = agentMap.get("agents_in_view").AsInteger();
			AgentFPS = (float) agentMap.get("fps").AsReal();
			AgentLanguage = agentMap.get("language").AsString();
			AgentMemoryUsed = (float) agentMap.get("mem_use").AsReal();
			MetersTraveled = agentMap.get("meters_traveled").AsInteger();
			AgentPing = (float) agentMap.get("ping").AsReal();
			RegionsVisited = agentMap.get("regions_visited").AsInteger();
			AgentRuntime = (float) agentMap.get("run_time").AsReal();
			SimulatorFPS = (float) agentMap.get("sim_fps").AsReal();
			AgentStartTime = Helpers.UnixTimeToDateTime(agentMap.get("start_time").AsUInteger());
			AgentVersion = agentMap.get("version").AsString();

			OSDMap downloadsMap = (OSDMap) map.get("downloads");
			object_kbytes = (float) downloadsMap.get("object_kbytes").AsReal();
			texture_kbytes = (float) downloadsMap.get("texture_kbytes").AsReal();
			world_kbytes = (float) downloadsMap.get("world_kbytes").AsReal();

			OSDMap miscMap = (OSDMap) map.get("misc");
			MiscVersion = (float) miscMap.get("Version").AsReal();
			VertexBuffersEnabled = miscMap.get("Vertex Buffers Enabled").AsBoolean();

			OSDMap statsMap = (OSDMap) map.get("stats");
			OSDMap failuresMap = (OSDMap) statsMap.get("failures");
			StatsDropped = failuresMap.get("dropped").AsInteger();
			StatsFailedResends = failuresMap.get("failed_resends").AsInteger();
			FailuresInvalid = failuresMap.get("invalid").AsInteger();
			FailuresOffCircuit = failuresMap.get("off_circuit").AsInteger();
			FailuresResent = failuresMap.get("resent").AsInteger();
			FailuresSendPacket = failuresMap.get("send_packet").AsInteger();

			OSDMap statsMiscMap = (OSDMap) statsMap.get("misc");
			MiscInt1 = statsMiscMap.get("int_1").AsInteger();
			MiscInt2 = statsMiscMap.get("int_2").AsInteger();
			MiscString1 = statsMiscMap.get("string_1").AsString();
			OSDMap netMap = (OSDMap) statsMap.get("net");
			// in
			OSDMap netInMap = (OSDMap) netMap.get("in");
			InCompressedPackets = netInMap.get("compressed_packets").AsInteger();
			InKbytes = netInMap.get("kbytes").AsInteger();
			InPackets = netInMap.get("packets").AsInteger();
			InSavings = netInMap.get("savings").AsInteger();
			// out
			OSDMap netOutMap = (OSDMap) netMap.get("out");
			OutCompressedPackets = netOutMap.get("compressed_packets").AsInteger();
			OutKbytes = netOutMap.get("kbytes").AsInteger();
			OutPackets = netOutMap.get("packets").AsInteger();
			OutSavings = netOutMap.get("savings").AsInteger();

			// system
			OSDMap systemStatsMap = (OSDMap) map.get("system");
			SystemCPU = systemStatsMap.get("cpu").AsString();
			SystemGPU = systemStatsMap.get("gpu").AsString();
			SystemGPUClass = systemStatsMap.get("gpu_class").AsInteger();
			SystemGPUVendor = systemStatsMap.get("gpu_vendor").AsString();
			SystemGPUVersion = systemStatsMap.get("gpu_version").AsString();
			SystemOS = systemStatsMap.get("os").AsString();
			SystemInstalledRam = systemStatsMap.get("ram").AsInteger();
		}
	}

	//
	public class PlacesReplyMessage implements IMessage
	{
		public UUID AgentID;
		public UUID QueryID;
		public UUID TransactionID;

		public class QueryData
		{
			public int ActualArea;
			public int BillableArea;
			public String Description;
			public float Dwell;
			public int Flags;
			public float GlobalX;
			public float GlobalY;
			public float GlobalZ;
			public String Name;
			public UUID OwnerID;
			public String SimName;
			public UUID SnapShotID;
			public String ProductSku;
			public int Price;
		}

		public QueryData[] QueryDataBlocks;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.PlacesReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			// add the AgentData map
			OSDMap agentIDmap = new OSDMap(2);
			agentIDmap.put("AgentID", OSD.FromUUID(AgentID));
			agentIDmap.put("QueryID", OSD.FromUUID(QueryID));

			OSDArray agentDataArray = new OSDArray();
			agentDataArray.add(agentIDmap);

			map.put("AgentData", agentDataArray);

			// add the QueryData map
			OSDArray dataBlocksArray = new OSDArray(QueryDataBlocks.length);
			for (int i = 0; i < QueryDataBlocks.length; i++)
			{
				OSDMap queryDataMap = new OSDMap(14);
				queryDataMap.put("ActualArea", OSD.FromInteger(QueryDataBlocks[i].ActualArea));
				queryDataMap.put("BillableArea", OSD.FromInteger(QueryDataBlocks[i].BillableArea));
				queryDataMap.put("Desc", OSD.FromString(QueryDataBlocks[i].Description));
				queryDataMap.put("Dwell", OSD.FromReal(QueryDataBlocks[i].Dwell));
				queryDataMap.put("Flags", OSD.FromInteger(QueryDataBlocks[i].Flags));
				queryDataMap.put("GlobalX", OSD.FromReal(QueryDataBlocks[i].GlobalX));
				queryDataMap.put("GlobalY", OSD.FromReal(QueryDataBlocks[i].GlobalY));
				queryDataMap.put("GlobalZ", OSD.FromReal(QueryDataBlocks[i].GlobalZ));
				queryDataMap.put("Name", OSD.FromString(QueryDataBlocks[i].Name));
				queryDataMap.put("OwnerID", OSD.FromUUID(QueryDataBlocks[i].OwnerID));
				queryDataMap.put("Price", OSD.FromInteger(QueryDataBlocks[i].Price));
				queryDataMap.put("SimName", OSD.FromString(QueryDataBlocks[i].SimName));
				queryDataMap.put("SnapshotID", OSD.FromUUID(QueryDataBlocks[i].SnapShotID));
				queryDataMap.put("ProductSKU", OSD.FromString(QueryDataBlocks[i].ProductSku));
				dataBlocksArray.add(queryDataMap);
			}

			map.put("QueryData", dataBlocksArray);

			// add the TransactionData map
			OSDMap transMap = new OSDMap(1);
			transMap.put("TransactionID", OSD.FromUUID(TransactionID));
			OSDArray transArray = new OSDArray();
			transArray.add(transMap);
			map.put("TransactionData", transArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");

			OSDMap agentDataMap = (OSDMap) agentDataArray.get(0);
			AgentID = agentDataMap.get("AgentID").AsUUID();
			QueryID = agentDataMap.get("QueryID").AsUUID();

			OSDArray dataBlocksArray = (OSDArray) map.get("QueryData");
			QueryDataBlocks = new QueryData[dataBlocksArray.size()];
			for (int i = 0; i < dataBlocksArray.size(); i++)
			{
				OSDMap dataMap = (OSDMap) dataBlocksArray.get(i);
				QueryData data = new QueryData();
				data.ActualArea = dataMap.get("ActualArea").AsInteger();
				data.BillableArea = dataMap.get("BillableArea").AsInteger();
				data.Description = dataMap.get("Desc").AsString();
				data.Dwell = (float) dataMap.get("Dwell").AsReal();
				data.Flags = dataMap.get("Flags").AsInteger();
				data.GlobalX = (float) dataMap.get("GlobalX").AsReal();
				data.GlobalY = (float) dataMap.get("GlobalY").AsReal();
				data.GlobalZ = (float) dataMap.get("GlobalZ").AsReal();
				data.Name = dataMap.get("Name").AsString();
				data.OwnerID = dataMap.get("OwnerID").AsUUID();
				data.Price = dataMap.get("Price").AsInteger();
				data.SimName = dataMap.get("SimName").AsString();
				data.SnapShotID = dataMap.get("SnapshotID").AsUUID();
				data.ProductSku = dataMap.get("ProductSKU").AsString();
				QueryDataBlocks[i] = data;
			}

			OSDArray transactionArray = (OSDArray) map.get("TransactionData");
			OSDMap transactionDataMap = (OSDMap) transactionArray.get(0);
			TransactionID = transactionDataMap.get("TransactionID").AsUUID();
		}
	}

	public class UpdateAgentInformationMessage implements IMessage
	{
		public String MaxAccess; // PG, A, or M

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UpdateAgentInformation;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			OSDMap prefsMap = new OSDMap(1);
			prefsMap.put("max", OSD.FromString(MaxAccess));
			map.put("access_prefs", prefsMap);
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDMap prefsMap = (OSDMap) map.get("access_prefs");
			MaxAccess = prefsMap.get("max").AsString();
		}
	}

	public class DirLandReplyMessage implements IMessage
	{
		public UUID AgentID;
		public UUID QueryID;

		public class QueryReply
		{
			public int ActualArea;
			public boolean Auction;
			public boolean ForSale;
			public String Name;
			public UUID ParcelID;
			public String ProductSku;
			public int SalePrice;
		}

		public QueryReply[] QueryReplies;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.DirLandReply;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			OSDMap agentMap = new OSDMap(1);
			agentMap.put("AgentID", OSD.FromUUID(AgentID));
			OSDArray agentDataArray = new OSDArray(1);
			agentDataArray.add(agentMap);
			map.put("AgentData", agentDataArray);

			OSDMap queryMap = new OSDMap(1);
			queryMap.put("QueryID", OSD.FromUUID(QueryID));
			OSDArray queryDataArray = new OSDArray(1);
			queryDataArray.add(queryMap);
			map.put("QueryData", queryDataArray);

			OSDArray queryReplyArray = new OSDArray();
			for (int i = 0; i < QueryReplies.length; i++)
			{
				OSDMap queryReply = new OSDMap(100);
				queryReply.put("ActualArea", OSD.FromInteger(QueryReplies[i].ActualArea));
				queryReply.put("Auction", OSD.FromBoolean(QueryReplies[i].Auction));
				queryReply.put("ForSale", OSD.FromBoolean(QueryReplies[i].ForSale));
				queryReply.put("Name", OSD.FromString(QueryReplies[i].Name));
				queryReply.put("ParcelID", OSD.FromUUID(QueryReplies[i].ParcelID));
				queryReply.put("ProductSKU", OSD.FromString(QueryReplies[i].ProductSku));
				queryReply.put("SalePrice", OSD.FromInteger(QueryReplies[i].SalePrice));

				queryReplyArray.add(queryReply);
			}
			map.put("QueryReplies", queryReplyArray);

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray agentDataArray = (OSDArray) map.get("AgentData");
			OSDMap agentDataMap = (OSDMap) agentDataArray.get(0);
			AgentID = agentDataMap.get("AgentID").AsUUID();

			OSDArray queryDataArray = (OSDArray) map.get("QueryData");
			OSDMap queryDataMap = (OSDMap) queryDataArray.get(0);
			QueryID = queryDataMap.get("QueryID").AsUUID();

			OSDArray queryRepliesArray = (OSDArray) map.get("QueryReplies");

			QueryReplies = new QueryReply[queryRepliesArray.size()];
			for (int i = 0; i < queryRepliesArray.size(); i++)
			{
				QueryReply reply = new QueryReply();
				OSDMap replyMap = (OSDMap) queryRepliesArray.get(i);
				reply.ActualArea = replyMap.get("ActualArea").AsInteger();
				reply.Auction = replyMap.get("Auction").AsBoolean();
				reply.ForSale = replyMap.get("ForSale").AsBoolean();
				reply.Name = replyMap.get("Name").AsString();
				reply.ParcelID = replyMap.get("ParcelID").AsUUID();
				reply.ProductSku = replyMap.get("ProductSKU").AsString();
				reply.SalePrice = replyMap.get("SalePrice").AsInteger();

				QueryReplies[i] = reply;
			}
		}
	}

	// #endregion

	// #region Object Messages

	public class UploadObjectAssetMessage implements IMessage
	{
		public class Object
		{
			public class Face
			{
				public Bumpiness Bump;
				public Color4 Color;
				public boolean Fullbright;
				public float Glow;
				public UUID ImageID;
				public float ImageRot;
				public int MediaFlags;
				public float OffsetS;
				public float OffsetT;
				public float ScaleS;
				public float ScaleT;

				public OSDMap Serialize()
				{
					OSDMap map = new OSDMap();
					map.put("bump", OSD.FromInteger(Bump.getValue()));
					map.put("colors", OSD.FromColor4(Color));
					map.put("fullbright", OSD.FromBoolean(Fullbright));
					map.put("glow", OSD.FromReal(Glow));
					map.put("imageid", OSD.FromUUID(ImageID));
					map.put("imagerot", OSD.FromReal(ImageRot));
					map.put("media_flags", OSD.FromInteger(MediaFlags));
					map.put("offsets", OSD.FromReal(OffsetS));
					map.put("offsett", OSD.FromReal(OffsetT));
					map.put("scales", OSD.FromReal(ScaleS));
					map.put("scalet", OSD.FromReal(ScaleT));

					return map;
				}

				public Face(OSDMap map)
				{
					Bump = Bumpiness.setValue(map.get("bump").AsInteger());
					Color = map.get("colors").AsColor4();
					Fullbright = map.get("fullbright").AsBoolean();
					Glow = (float) map.get("glow").AsReal();
					ImageID = map.get("imageid").AsUUID();
					ImageRot = (float) map.get("imagerot").AsReal();
					MediaFlags = map.get("media_flags").AsInteger();
					OffsetS = (float) map.get("offsets").AsReal();
					OffsetT = (float) map.get("offsett").AsReal();
					ScaleS = (float) map.get("scales").AsReal();
					ScaleT = (float) map.get("scalet").AsReal();
				}
			}

			public class ExtraParam
			{
				public ExtraParamType Type;
				public byte[] ExtraParamData;

				public OSDMap Serialize()
				{
					OSDMap map = new OSDMap();
					map.put("extra_parameter", OSD.FromInteger(Type.getValue()));
					map.put("param_data", OSD.FromBinary(ExtraParamData));

					return map;
				}

				public ExtraParam(OSDMap map)
				{
					Type = ExtraParamType.setValue(map.get("extra_parameter").AsInteger());
					ExtraParamData = map.get("param_data").AsBinary();
				}
			}

			public Face[] Faces;
			public ExtraParam[] ExtraParams;
			public UUID GroupID;
			public Material Material;
			public String Name;
			public Vector3 Position;
			public Quaternion Rotation;
			public Vector3 Scale;
			public float PathBegin;
			public int PathCurve;
			public float PathEnd;
			public float RadiusOffset;
			public float Revolutions;
			public float ScaleX;
			public float ScaleY;
			public float ShearX;
			public float ShearY;
			public float Skew;
			public float TaperX;
			public float TaperY;
			public float Twist;
			public float TwistBegin;
			public float ProfileBegin;
			public int ProfileCurve;
			public float ProfileEnd;
			public float ProfileHollow;
			public UUID SculptID;
			public SculptType SculptType;

			public OSDMap Serialize()
			{
				OSDMap map = new OSDMap();

				map.put("group-id", OSD.FromUUID(GroupID));
				map.put("material", OSD.FromInteger(Material.getValue()));
				map.put("name", OSD.FromString(Name));
				map.put("pos", OSD.FromVector3(Position));
				map.put("rotation", OSD.FromQuaternion(Rotation));
				map.put("scale", OSD.FromVector3(Scale));

				// Extra params
				OSDArray extraParams = new OSDArray();
				if (ExtraParams != null)
				{
					for (int i = 0; i < ExtraParams.length; i++)
						extraParams.add(ExtraParams[i].Serialize());
				}
				map.put("extra_parameters", extraParams);

				// Faces
				OSDArray faces = new OSDArray();
				if (Faces != null)
				{
					for (int i = 0; i < Faces.length; i++)
						faces.add(Faces[i].Serialize());
				}
				map.put("facelist", faces);

				// Shape
				OSDMap shape = new OSDMap();
				OSDMap path = new OSDMap();
				path.put("begin", OSD.FromReal(PathBegin));
				path.put("curve", OSD.FromInteger(PathCurve));
				path.put("end", OSD.FromReal(PathEnd));
				path.put("radius_offset", OSD.FromReal(RadiusOffset));
				path.put("revolutions", OSD.FromReal(Revolutions));
				path.put("scale_x", OSD.FromReal(ScaleX));
				path.put("scale_y", OSD.FromReal(ScaleY));
				path.put("shear_x", OSD.FromReal(ShearX));
				path.put("shear_y", OSD.FromReal(ShearY));
				path.put("skew", OSD.FromReal(Skew));
				path.put("taper_x", OSD.FromReal(TaperX));
				path.put("taper_y", OSD.FromReal(TaperY));
				path.put("twist", OSD.FromReal(Twist));
				path.put("twist_begin", OSD.FromReal(TwistBegin));
				shape.put("path", path);
				OSDMap profile = new OSDMap();
				profile.put("begin", OSD.FromReal(ProfileBegin));
				profile.put("curve", OSD.FromInteger(ProfileCurve));
				profile.put("end", OSD.FromReal(ProfileEnd));
				profile.put("hollow", OSD.FromReal(ProfileHollow));
				shape.put("profile", profile);
				OSDMap sculpt = new OSDMap();
				sculpt.put("id", OSD.FromUUID(SculptID));
				sculpt.put("type", OSD.FromInteger(SculptType.getValue()));
				shape.put("sculpt", sculpt);
				map.put("shape", shape);

				return map;
			}

			public Object(OSDMap map)
			{
				if (map != null)
				{
					GroupID = map.get("group-id").AsUUID();
					Material = libomv.primitives.Primitive.Material.setValue(map.get("material").AsInteger());
					Name = map.get("name").AsString();
					Position = map.get("pos").AsVector3();
					Rotation = map.get("rotation").AsQuaternion();
					Scale = map.get("scale").AsVector3();

					// Extra params
					OSDArray extraParams = (OSDArray) map.get("extra_parameters");
					if (extraParams != null)
					{
						ExtraParams = new ExtraParam[extraParams.size()];
						for (int i = 0; i < extraParams.size(); i++)
						{
							ExtraParams[i] = new ExtraParam((OSDMap) extraParams.get(i));
							;
						}
					}
					else
					{
						ExtraParams = new ExtraParam[0];
					}

					// Faces
					OSDArray faces = (OSDArray) map.get("facelist");
					if (faces != null)
					{
						Faces = new Face[faces.size()];
						for (int i = 0; i < faces.size(); i++)
						{
							Faces[i] = new Face((OSDMap) faces.get(i));
						}
					}
					else
					{
						Faces = new Face[0];
					}

					// Shape
					OSDMap shape = (OSDMap) map.get("shape");
					OSDMap path = (OSDMap) shape.get("path");
					PathBegin = (float) path.get("begin").AsReal();
					PathCurve = path.get("curve").AsInteger();
					PathEnd = (float) path.get("end").AsReal();
					RadiusOffset = (float) path.get("radius_offset").AsReal();
					Revolutions = (float) path.get("revolutions").AsReal();
					ScaleX = (float) path.get("scale_x").AsReal();
					ScaleY = (float) path.get("scale_y").AsReal();
					ShearX = (float) path.get("shear_x").AsReal();
					ShearY = (float) path.get("shear_y").AsReal();
					Skew = (float) path.get("skew").AsReal();
					TaperX = (float) path.get("taper_x").AsReal();
					TaperY = (float) path.get("taper_y").AsReal();
					Twist = (float) path.get("twist").AsReal();
					TwistBegin = (float) path.get("twist_begin").AsReal();

					OSDMap profile = (OSDMap) shape.get("profile");
					ProfileBegin = (float) profile.get("begin").AsReal();
					ProfileCurve = profile.get("curve").AsInteger();
					ProfileEnd = (float) profile.get("end").AsReal();
					ProfileHollow = (float) profile.get("hollow").AsReal();

					OSDMap sculpt = (OSDMap) shape.get("sculpt");
					if (sculpt != null)
					{
						SculptID = sculpt.get("id").AsUUID();
						SculptType = libomv.primitives.Primitive.SculptType.setValues(sculpt.get("type").AsInteger());
					}
					else
					{
						SculptID = UUID.Zero;
						SculptType = libomv.primitives.Primitive.SculptType.None;
					}
				}
			}
		}

		public Object[] Objects;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.UploadObjectAsset;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns Serialized OSD
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap();
			OSDArray array = new OSDArray();

			if (Objects != null)
			{
				for (int i = 0; i < Objects.length; i++)
					array.add(Objects[i].Serialize());
			}

			map.put("objects", array);
			return map;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray array = (OSDArray) map.get("objects");

			if (array != null)
			{
				Objects = new Object[array.size()];

				for (int i = 0; i < array.size(); i++)
				{
					Objects[i] = new Object((OSDMap) array.get(i));
				}
			}
			else
			{
				Objects = new Object[0];
			}
		}
	}

	// Event Queue message describing physics engine attributes of a list of
	// objects
	// Sim sends these when object is selected
	public class ObjectPhysicsPropertiesMessage implements IMessage
	{
		// Array with the list of physics properties
		public PhysicsProperties[] ObjectPhysicsProperties;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ObjectPhysicsProperties;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns Serialized OSD
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap ret = new OSDMap(1);
			OSDArray array = new OSDArray(ObjectPhysicsProperties.length);

			for (int i = 0; i < ObjectPhysicsProperties.length; i++)
			{
				array.add(ObjectPhysicsProperties[i].GetOSD());
			}
			ret.put("ObjectData", array);
			return ret;

		}

		/**
		 * Deseializes the message
		 * 
		 * @param name
		 *            Incoming data to deserialize
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray array = (OSDArray) map.get("ObjectData");
			if (array != null)
			{
				ObjectPhysicsProperties = new PhysicsProperties[array.size()];

				for (int i = 0; i < array.size(); i++)
				{
					ObjectPhysicsProperties[i] = new PhysicsProperties(array.get(i));
				}
			}
			else
			{
				ObjectPhysicsProperties = new PhysicsProperties[0];
			}
		}
	}

	// #endregion Object Messages

	// #region Object Media Messages
	// A message sent from the viewer to the simulator which specifies that the
	// user has changed current URL
	// of the specific media on a prim face
	public class ObjectMediaNavigateMessage implements IMessage
	{
		// New URL
		public String URL;

		// Prim UUID where navigation occured
		public UUID PrimID;

		// Face index
		public int Face;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ObjectMediaNavigate;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(3);

			map.put("current_url", OSD.FromString(URL));
			map.put("object_id", OSD.FromUUID(PrimID));
			map.put("texture_index", OSD.FromInteger(Face));

			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			URL = map.get("current_url").AsString();
			PrimID = map.get("object_id").AsUUID();
			Face = map.get("texture_index").AsInteger();
		}
	}

	// Base class used for the ObjectMedia message
	public abstract class ObjectMediaBlock
	{
		public abstract OSDMap Serialize();

		public abstract void Deserialize(OSDMap map);
	}

	// Message used to retrive prim media data
	public class ObjectMediaRequest extends ObjectMediaBlock
	{
		// Prim UUID
		public UUID PrimID;

		//
		// Requested operation, either GET or UPDATE
		public String Verb = "GET"; // "GET" or "UPDATE"

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.FromUUID(PrimID));
			map.put("verb", OSD.FromString(Verb));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			PrimID = map.get("object_id").AsUUID();
			Verb = map.get("verb").AsString();
		}
	}

	// Message used to update prim media data
	public class ObjectMediaResponse extends ObjectMediaBlock
	{
		// Prim UUID
		public UUID PrimID;

		// Array of media entries indexed by face number
		public MediaEntry[] FaceMedia;

		// Media version string
		public String Version; // String in this format:
								// x-mv:0000000016/00000000-0000-0000-0000-000000000000

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.FromUUID(PrimID));

			if (FaceMedia == null)
			{
				map.put("object_media_data", new OSDArray());
			}
			else
			{
				OSDArray mediaData = new OSDArray(FaceMedia.length);

				for (int i = 0; i < FaceMedia.length; i++)
				{
					if (FaceMedia[i] == null)
						mediaData.add(new OSD());
					else
						mediaData.add(FaceMedia[i].Serialize());
				}

				map.put("object_media_data", mediaData);
			}

			map.put("object_media_version", OSD.FromString(Version));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			PrimID = map.get("object_id").AsUUID();

			if (map.get("object_media_data").getType() == OSDType.Array)
			{
				OSDArray mediaData = (OSDArray) map.get("object_media_data");
				if (mediaData.size() > 0)
				{
					FaceMedia = new MediaEntry[mediaData.size()];
					for (int i = 0; i < mediaData.size(); i++)
					{
						if (mediaData.get(i).getType() == OSDType.Map)
						{
							FaceMedia[i] = new MediaEntry(mediaData.get(i));
						}
					}
				}
			}
			Version = map.get("object_media_version").AsString();
		}
	}

	// Message used to update prim media data
	public class ObjectMediaUpdate extends ObjectMediaBlock
	{
		// Prim UUID
		public UUID PrimID;

		// Array of media entries indexed by face number
		public MediaEntry[] FaceMedia;

		// Requested operation, either GET or UPDATE
		public String Verb = "UPDATE"; // "GET" or "UPDATE"

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(2);
			map.put("object_id", OSD.FromUUID(PrimID));

			if (FaceMedia == null)
			{
				map.put("object_media_data", new OSDArray());
			}
			else
			{
				OSDArray mediaData = new OSDArray(FaceMedia.length);

				for (int i = 0; i < FaceMedia.length; i++)
				{
					if (FaceMedia[i] == null)
						mediaData.add(new OSD());
					else
						mediaData.add(FaceMedia[i].Serialize());
				}

				map.put("object_media_data", mediaData);
			}

			map.put("verb", OSD.FromString(Verb));
			return map;
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			PrimID = map.get("object_id").AsUUID();

			if (map.get("object_media_data").getType() == OSDType.Array)
			{
				OSDArray mediaData = (OSDArray) map.get("object_media_data");
				if (mediaData.size() > 0)
				{
					FaceMedia = new MediaEntry[mediaData.size()];
					for (int i = 0; i < mediaData.size(); i++)
					{
						if (mediaData.get(i).getType() == OSDType.Map)
						{
							FaceMedia[i] = new MediaEntry(mediaData.get(i));
						}
					}
				}
			}
			Verb = map.get("verb").AsString();
		}
	}

	// Message for setting or getting per face MediaEntry
	public class ObjectMediaMessage implements IMessage
	{
		// The request or response details block
		public ObjectMediaBlock Request;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.ObjectMedia;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			return Request.Serialize();
		}

		/**
		 * Deserialize the message
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("verb"))
			{
				if (map.get("verb").AsString() == "GET")
				{
					Request = new ObjectMediaRequest();
					Request.Deserialize(map);
				}
				else if (map.get("verb").AsString() == "UPDATE")
				{
					Request = new ObjectMediaUpdate();
					Request.Deserialize(map);
				}
			}
			else if (map.containsKey("object_media_version"))
			{
				Request = new ObjectMediaResponse();
				Request.Deserialize(map);
			}
			else
				Logger.Log(
						"Unable to deserialize ObjectMedia: No message handler exists for method: " + map.AsString(),
						LogLevel.Warning);
		}
	}

	// #endregion Object Media Messages

	// #region Resource usage

	// Details about object resource usage
	public class ObjectResourcesDetail
	{
		// Object UUID
		public UUID ID;
		// Object name
		public String Name;
		// Indicates if object is group owned
		public boolean GroupOwned;
		// Locatio of the object
		public Vector3d Location;
		// Object owner
		public UUID OwnerID;
		// Resource usage, keys are resource names, values are resource usage
		// for that specific resource
		public HashMap<String, Integer> Resources;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public OSDMap Serialize()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param obj
		 *            An <see cref="OSDMap"/> containing the data
		 */
		public void Deserialize(OSDMap obj)
		{
			ID = obj.get("id").AsUUID();
			Name = obj.get("name").AsString();
			Location = obj.get("location").AsVector3d();
			GroupOwned = obj.get("is_group_owned").AsBoolean();
			OwnerID = obj.get("owner_id").AsUUID();
			OSDMap resources = (OSDMap) obj.get("resources");
			Resources = new HashMap<String, Integer>(resources.size());
			for (Entry<String, OSD> kvp : resources.entrySet())
			{
				Resources.put(kvp.getKey(), kvp.getValue().AsInteger());
			}
		}
	}

	// Details about parcel resource usage
	public class ParcelResourcesDetail
	{
		// Parcel UUID
		public UUID ID;
		// Parcel local ID
		public int LocalID;
		// Parcel name
		public String Name;
		// Indicates if parcel is group owned
		public boolean GroupOwned;
		// Parcel owner
		public UUID OwnerID;
		// Array of <see cref="ObjectResourcesDetail"/> containing per object
		// resource usage
		public ObjectResourcesDetail[] Objects;

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		public OSDMap Serialize()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		public void Deserialize(OSDMap map)
		{
			ID = map.get("id").AsUUID();
			LocalID = map.get("local_id").AsInteger();
			Name = map.get("name").AsString();
			GroupOwned = map.get("is_group_owned").AsBoolean();
			OwnerID = map.get("owner_id").AsUUID();

			OSDArray objectsOSD = (OSDArray) map.get("objects");
			Objects = new ObjectResourcesDetail[objectsOSD.size()];

			for (int i = 0; i < objectsOSD.size(); i++)
			{
				Objects[i] = new ObjectResourcesDetail();
				Objects[i].Deserialize((OSDMap) objectsOSD.get(i));
			}
		}
	}

	// Resource usage base class, both agent and parcel resource usage contains
	// summary information
	public abstract class BaseResourcesInfo implements IMessage
	{
		// Summary of available resources, keys are resource names, values are
		// resource usage for that specific resource
		public HashMap<String, Integer> SummaryAvailable;
		// Summary resource usage, keys are resource names, values are resource
		// usage for that specific resource
		public HashMap<String, Integer> SummaryUsed;

		@Override
		public OSDMap Serialize()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			SummaryAvailable = new HashMap<String, Integer>();
			SummaryUsed = new HashMap<String, Integer>();

			OSDMap summary = (OSDMap) map.get("summary");
			OSDArray available = (OSDArray) summary.get("available");
			OSDArray used = (OSDArray) summary.get("used");

			for (int i = 0; i < available.size(); i++)
			{
				OSDMap limit = (OSDMap) available.get(i);
				SummaryAvailable.put(limit.get("type").AsString(), limit.get("amount").AsInteger());
			}

			for (int i = 0; i < used.size(); i++)
			{
				OSDMap limit = (OSDMap) used.get(i);
				SummaryUsed.put(limit.get("type").AsString(), limit.get("amount").AsInteger());
			}
		}
	}

	public class AttachmentResourcesMessage extends BaseResourcesInfo
	{
		BaseResourcesInfo SummaryInfoBlock;

		// Per attachment point object resource usage
		public HashMap<AttachmentPoint, ObjectResourcesDetail[]> Attachments;

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.AttachmentResources;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = super.Serialize();

			return map;
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param osd
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map != null)
			{
				super.Deserialize(map);
				OSDArray attachments = (OSDArray) map.get("attachments");
				Attachments = new HashMap<AttachmentPoint, ObjectResourcesDetail[]>();

				for (int i = 0; i < attachments.size(); i++)
				{
					OSDMap attachment = (OSDMap) attachments.get(i);
					AttachmentPoint pt = AttachmentPoint.setValue(attachment.get("location").AsString());

					OSDArray objectsOSD = (OSDArray) attachment.get("objects");
					ObjectResourcesDetail[] objects = new ObjectResourcesDetail[objectsOSD.size()];

					for (int j = 0; j < objects.length; j++)
					{
						objects[j] = new ObjectResourcesDetail();
						objects[j].Deserialize((OSDMap) objectsOSD.get(j));
					}

					Attachments.put(pt, objects);
				}
			}
		}
	}

	// Request message for parcel resource usage
	public class LandResourcesRequest implements IMessage
	{
		// UUID of the parel to request resource usage info
		public UUID ParcelID;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			map.put("parcel_id", OSD.FromUUID(ParcelID));
			return map;
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			ParcelID = map.get("parcel_id").AsUUID();
		}
	}

	public class LandResourcesMessage implements IMessage
	{
		// URL where parcel resource usage details can be retrieved
		public URI ScriptResourceDetails;
		// URL where parcel resource usage summary can be retrieved
		public URI ScriptResourceSummary;

		/**
		 * @return the type of message
		 */
		@Override
		public CapsEventType getType()
		{
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap();
			if (ScriptResourceSummary != null)
			{
				map.put("ScriptResourceSummary", OSD.FromString(ScriptResourceSummary.toString()));
			}

			if (ScriptResourceDetails != null)
			{
				map.put("ScriptResourceDetails", OSD.FromString(ScriptResourceDetails.toString()));
			}
			return map;
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param map
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			try
			{
				if (map.containsKey("ScriptResourceSummary"))
				{
					ScriptResourceSummary = new URI(map.get("ScriptResourceSummary").AsString());
				}
				if (map.containsKey("ScriptResourceDetails"))
				{
					ScriptResourceDetails = new URI(map.get("ScriptResourceDetails").AsString());
				}
			}
			catch (URISyntaxException e)
			{
			}
		}
	}

	// Parcel resource usage
	public class LandResourcesInfo extends BaseResourcesInfo
	{
		// Array of <see cref="ParcelResourcesDetail"/> containing per percal
		// resource usage
		public ParcelResourcesDetail[] Parcels;

		@Override
		public CapsEventType getType()
		{
			// TODO Auto-generated method stub
			return CapsEventType.LandResources;
		}

		/**
		 * Serialize the object
		 * 
		 * @return An <see cref="OSDMap"/> containing the objects data
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap map = new OSDMap(1);
			if (Parcels != null)
			{
				OSDArray array = new OSDArray(Parcels.length);
				for (int i = 0; i < Parcels.length; i++)
				{
					array.add(Parcels[i].Serialize());
				}
				map.put("parcels", array);
			}
			return map;
		}

		/**
		 * Deserializes object from OSD
		 * 
		 * @param osd
		 *            An <see cref="OSDMap"/> containing the data
		 */
		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.containsKey("summary"))
			{
				super.Deserialize(map);
			}
			else if (map.containsKey("parcels"))
			{
				OSDArray parcelsOSD = (OSDArray) map.get("parcels");
				Parcels = new ParcelResourcesDetail[parcelsOSD.size()];
				for (int i = 0; i < parcelsOSD.size(); i++)
				{
					Parcels[i] = new ParcelResourcesDetail();
					Parcels[i].Deserialize((OSDMap) parcelsOSD.get(i));
				}
			}
		}
	}

	// #endregion Resource usage

	// #region Display names

	// Reply to request for bunch if display names
	public class GetDisplayNamesMessage implements IMessage
	{
		// Current display name
		public AgentDisplayName[] Agents = new AgentDisplayName[0];

		// Following UUIDs failed to return a valid display name
		public UUID[] BadIDs = new UUID[0];

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.GetDisplayNames;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDArray agents = new OSDArray();

			if (Agents != null && Agents.length > 0)
			{
				for (int i = 0; i < Agents.length; i++)
				{
					agents.add(Agents[i].GetOSD());
				}
			}

			OSDArray badIDs = new OSDArray();
			if (BadIDs != null && BadIDs.length > 0)
			{
				for (int i = 0; i < BadIDs.length; i++)
				{
					badIDs.add(OSD.FromUUID(BadIDs[i]));
				}
			}

			OSDMap ret = new OSDMap();
			ret.put("agents", agents);
			ret.put("bad_ids", badIDs);
			return ret;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			if (map.get("agents").getType() == OSDType.Array)
			{
				OSDArray osdAgents = (OSDArray) map.get("agents");

				if (osdAgents.size() > 0)
				{
					Agents = new AgentDisplayName[osdAgents.size()];

					for (int i = 0; i < osdAgents.size(); i++)
					{
						Agents[i].FromOSD(osdAgents.get(i));
					}
				}
			}

			if (map.get("bad_ids").getType() == OSDType.Array)
			{
				OSDArray osdBadIDs = (OSDArray) map.get("bad_ids");
				if (osdBadIDs.size() > 0)
				{
					BadIDs = new UUID[osdBadIDs.size()];

					for (int i = 0; i < osdBadIDs.size(); i++)
					{
						BadIDs[i] = osdBadIDs.get(i).AsUUID();
					}
				}
			}
		}
	}

	// Message sent when requesting change of the display name
	public class SetDisplayNameMessage implements IMessage
	{
		// Current display name
		public String OldDisplayName;

		// Desired new display name
		public String NewDisplayName;

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.SetDisplayName;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDArray names = new OSDArray(2);
			names.add(OSD.FromString(OldDisplayName));
			names.add(OSD.FromString(NewDisplayName));

			OSDMap name = new OSDMap();
			name.put("display_name", names);
			return name;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			OSDArray names = (OSDArray) map.get("display_name");
			OldDisplayName = names.get(0).AsString();
			NewDisplayName = names.get(1).AsString();
		}
	}

	// Message recieved in response to request to change display name
	public class SetDisplayNameReplyMessage implements IMessage
	{
		// New display name
		public AgentDisplayName DisplayName;

		// String message indicating the result of the operation
		public String Reason;

		// Numerical code of the result, 200 indicates success
		public int Status;

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.SetDisplayNameReply;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap ret = new OSDMap(3);
			ret.put("content", DisplayName.GetOSD());
			ret.put("reason", OSD.FromString(Reason));
			ret.put("status", OSD.FromInteger(Status));
			return ret;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			DisplayName.FromOSD(map.get("content"));
			Reason = map.get("reason").AsString();
			Status = map.get("status").AsInteger();
		}
	}

	// Message recieved when someone nearby changes their display name
	public class DisplayNameUpdateMessage implements IMessage
	{
		// Previous display name, empty string if default
		public String OldDisplayName;

		// New display name
		public AgentDisplayName DisplayName;

		@Override
		public CapsEventType getType()
		{
			return CapsEventType.DisplayNameUpdate;
		}

		/**
		 * Serializes the message
		 * 
		 * @returns OSD containting the messaage
		 */
		@Override
		public OSDMap Serialize()
		{
			OSDMap agent = (OSDMap) DisplayName.GetOSD();
			agent.put("old_display_name", OSD.FromString(OldDisplayName));
			OSDMap ret = new OSDMap();
			ret.put("agent", agent);
			return ret;
		}

		@Override
		public void Deserialize(OSDMap map)
		{
			OSDMap agent = (OSDMap) map.get("agent");
			DisplayName.FromOSD(agent);
			OldDisplayName = agent.get("old_display_name").AsString();
		}
	}

	// #endregion Display names

	/**
	 * Return a decoded capabilities message as a strongly typed object
	 * 
	 * @param eventName
	 *            A string containing the name of the capabilities message key
	 * @param map
	 *            An <see cref="OSDMap"/> to decode
	 * @return A strongly typed object containing the decoded information from
	 *         the capabilities message, or null if no existing Message object
	 *         exists for the specified event
	 */
	public IMessage DecodeEvent(CapsEventType eventType, OSDMap map)
	{
		IMessage message = null;
		switch (eventType)
		{
			case AgentGroupDataUpdate:
				message = new AgentGroupDataUpdateMessage();
				break;
			case AvatarGroupsReply: // OpenSim sends the above with the wrong key
				message = new AgentGroupDataUpdateMessage();
				break;
			case ParcelProperties:
				message = new ParcelPropertiesMessage();
				break;
			case ParcelObjectOwnersReply:
				message = new ParcelObjectOwnersReplyMessage();
				break;
			case TeleportFinish:
				message = new TeleportFinishMessage();
				break;
			case EnableSimulator:
				message = new EnableSimulatorMessage();
				break;
			case ParcelPropertiesUpdate:
				message = new ParcelPropertiesUpdateMessage();
				break;
			case EstablishAgentCommunication:
				message = new EstablishAgentCommunicationMessage();
				break;
			case ChatterBoxInvitation:
				message = new ChatterBoxInvitationMessage();
				break;
			case ChatterBoxSessionEventReply:
				message = new ChatterBoxSessionEventReplyMessage();
				break;
			case ChatterBoxSessionStartReply:
				message = new ChatterBoxSessionStartReplyMessage();
				break;
			case ChatterBoxSessionAgentListUpdates:
				message = new ChatterBoxSessionAgentListUpdatesMessage();
				break;
			case RequiredVoiceVersion:
				message = new RequiredVoiceVersionMessage();
				break;
			case MapLayer:
				message = new MapLayerMessage();
				break;
			case ChatSessionRequest:
				message = new ChatSessionRequestMessage();
				break;
			case CopyInventoryFromNotecard:
				message = new CopyInventoryFromNotecardMessage();
				break;
			case ProvisionVoiceAccountRequest:
				message = new ProvisionVoiceAccountRequestMessage();
				break;
			case Viewerstats:
				message = new ViewerStatsMessage();
				break;
			case UpdateAgentLanguage:
				message = new UpdateAgentLanguageMessage();
				break;
			case RemoteParcelRequest:
				message = new RemoteParcelRequestMessage();
				break;
			case UpdateScriptTask:
				message = new UpdateScriptTaskMessage();
				break;
			case UpdateScriptAgent:
				message = new UpdateScriptAgentMessage();
				break;
			case SendPostcard:
				message = new SendPostcardMessage();
				break;
			case UpdateGestureAgentInventory:
				message = new UpdateGestureAgentInventoryMessage();
				break;
			case UpdateNotecardAgentInventory:
				message = new UpdateNotecardAgentInventoryMessage();
				break;
			case LandStatReply:
				message = new LandStatReplyMessage();
				break;
			case ParcelVoiceInfoRequest:
				message = new ParcelVoiceInfoRequestMessage();
				break;
			case ViewerStats:
				message = new ViewerStatsMessage();
				break;
			case EventQueueGet:
				message = new EventQueueGetMessage();
				break;
			case CrossedRegion:
				message = new CrossedRegionMessage();
				break;
			case TeleportFailed:
				message = new TeleportFailedMessage();
				break;
			case PlacesReply:
				message = new PlacesReplyMessage();
				break;
			case UpdateAgentInformation:
				message = new UpdateAgentInformationMessage();
				break;
			case DirLandReply:
				message = new DirLandReplyMessage();
				break;
			case ScriptRunningReply:
				message = new ScriptRunningReplyMessage();
				break;
			case SearchStatRequest:
				message = new SearchStatRequestMessage();
				break;
			case AgentDropGroup:
				message = new AgentDropGroupMessage();
				break;
			case ForceCloseChatterBoxSession:
				message = new ForceCloseChatterBoxSessionMessage();
				break;
			case UploadBakedTexture:
				message = new UploadBakedTextureMessage();
				break;
			case WebFetchInventoryDescendents:
				message = new WebFetchInventoryDescendentsMessage();
				break;
			case RegionInfo:
				message = new RegionInfoMessage();
				break;
			case UploadObjectAsset:
				message = new UploadObjectAssetMessage();
				break;
			case ObjectPhysicsProperties:
				message = new ObjectPhysicsPropertiesMessage();
				break;
			case ObjectMediaNavigate:
				message = new ObjectMediaNavigateMessage();
				break;
			case ObjectMedia:
				message = new ObjectMediaMessage();
				break;
			case AttachmentResources:
				message = new AttachmentResourcesMessage();
				break;
			case LandResources:
				if (map.containsKey("parcel_id"))
				{
					message = new LandResourcesRequest();
				}
				else if (map.containsKey("ScriptResourceSummary"))
				{
					message = new LandResourcesMessage();
				}
				else if (map.containsKey("summary"))
				{
					message = new LandResourcesInfo();
				}
				break;
			case ProductInfoRequest:
				message = new ProductInfoRequestMessage();
				break;
			case GetDisplayNames:
				message = new GetDisplayNamesMessage();
				break;
			case SetDisplayName:
				message = new SetDisplayNameMessage();
				break;
			case SetDisplayNameReply:
				message = new SetDisplayNameReplyMessage();
				break;
			case DisplayNameUpdate:
				message = new DisplayNameUpdateMessage();
				break;
			case BulkUpdateInventory:
				message = new BulkUpdateInventoryMessage();
				break; 

			// Capabilities TODO:
			case DispatchRegionInfo:
			case EstateChangeInfo:
			case FetchInventoryDescendents:
			case GroupProposalBallot:
			case MapLayerGod:
			case NewFileAgentInventory:
			case RequestTextureDownload:
			case SearchStatTracking:
			case SendUserReport:
			case SendUserReportWithScreenshot:
			case ServerReleaseNotes:
			case StartGroupProposal:
			case UpdateGestureTaskInventory:
			case UpdateNotecardTaskInventory:
			case ViewerStartAuction:
			case UntrustedSimulatorMessage:
			default:
				Logger.Log("Unimplemented event " + eventType.toString(), LogLevel.Error);
		}
		if (message != null)
			message.Deserialize(map);
		return message;
	}

	@Override
	public OSDMap Serialize()
	{
		return null;
	}

	@Override
	public void Deserialize(OSDMap map)
	{
	}
}