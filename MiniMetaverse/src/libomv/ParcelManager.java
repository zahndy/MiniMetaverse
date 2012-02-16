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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.nio.concurrent.FutureCallback;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsClient;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.LandResourcesMessage;
import libomv.capabilities.CapsMessage.LandResourcesInfo;
import libomv.capabilities.CapsMessage.LandResourcesRequest;
import libomv.capabilities.CapsMessage.ParcelObjectOwnersReplyMessage;
import libomv.capabilities.CapsMessage.ParcelPropertiesMessage;
import libomv.capabilities.CapsMessage.ParcelPropertiesUpdateMessage;
import libomv.capabilities.CapsMessage.RemoteParcelRequestMessage;
import libomv.capabilities.CapsMessage.RemoteParcelRequestReply;
import libomv.capabilities.CapsMessage.RemoteParcelRequestRequest;
import libomv.capabilities.IMessage;
import libomv.packets.EjectUserPacket;
import libomv.packets.ForceObjectSelectPacket;
import libomv.packets.FreezeUserPacket;
import libomv.packets.ModifyLandPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.ParcelAccessListReplyPacket;
import libomv.packets.ParcelAccessListRequestPacket;
import libomv.packets.ParcelBuyPacket;
import libomv.packets.ParcelDeedToGroupPacket;
import libomv.packets.ParcelDividePacket;
import libomv.packets.ParcelDwellReplyPacket;
import libomv.packets.ParcelDwellRequestPacket;
import libomv.packets.ParcelInfoReplyPacket;
import libomv.packets.ParcelInfoRequestPacket;
import libomv.packets.ParcelJoinPacket;
import libomv.packets.ParcelMediaCommandMessagePacket;
import libomv.packets.ParcelMediaUpdatePacket;
import libomv.packets.ParcelObjectOwnersRequestPacket;
import libomv.packets.ParcelOverlayPacket;
import libomv.packets.ParcelPropertiesRequestByIDPacket;
import libomv.packets.ParcelPropertiesRequestPacket;
import libomv.packets.ParcelPropertiesUpdatePacket;
import libomv.packets.ParcelReclaimPacket;
import libomv.packets.ParcelReleasePacket;
import libomv.packets.ParcelReturnObjectsPacket;
import libomv.packets.ParcelSelectObjectsPacket;
import libomv.packets.ParcelSetOtherCleanTimePacket;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

public class ParcelManager implements PacketCallback, CapsCallback
{
	// #region Enums

	/** Type of return to use when returning objects from a parcel */
	// [Flags]
	public static class ObjectReturnType
	{
		//
		public static final byte None = 0;
		// Return objects owned by parcel owner
		public static final byte Owner = 1 << 1;
		// Return objects set to group
		public static final byte Group = 1 << 2;
		// Return objects not owned by parcel owner or set to group
		public static final byte Other = 1 << 3;
		// Return a specific list of objects on parcel
		public static final byte List = 1 << 4;
		// Return objects that are marked for-sale
		public static final byte Sell = 1 << 5;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(byte value)
		{
			return value & _mask;
		}

		private static final byte _mask = 0x1F;
	}

	/** Blacklist/Whitelist flags used in parcels Access List */
	public enum ParcelAccessFlags
	{
		// Agent is denied access
		NoAccess,
		// Agent is granted access
		Access;

		public static ParcelAccessFlags setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/** The result of a request for parcel properties */
	public enum ParcelResult
	{
		// No matches were found for the request
		NoData(-1),
		// Request matched a single parcel
		Single(0),
		// Request matched multiple parcels
		Multiple(1);

		public static ParcelResult setValue(int value)
		{
			for (ParcelResult e : values())
			{
				if (e._value == value)
					return e;
			}
			return NoData;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelResult(int value)
		{
			this._value = (byte) value;
		}
	}

	/**
	 * Flags used in the ParcelAccessListRequest packet to specify whether we
	 * want the access list (whitelist), ban list (blacklist), or both
	 */
	// [Flags]
	public static class AccessList
	{
		// Request the access list
		public static final byte Access = 0x1;
		// Request the ban list
		public static final byte Ban = 0x2;
		// Request both White and Black lists
		public static final byte Both = 0x3;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(byte value)
		{
			return value & _mask;
		}

		private static final byte _mask = 0x3;
	}

	/**
	 * Sequence ID in ParcelPropertiesReply packets (sent when avatar tries to
	 * cross a parcel border)
	 */
	public enum ParcelPropertiesStatus
	{
		None(0),
		// Parcel is currently selected
		ParcelSelected(-10000),
		// Parcel restricted to a group the avatar is not a member of
		CollisionNotInGroup(-20000),
		// Avatar is banned from the parcel
		CollisionBanned(-30000),
		// Parcel is restricted to an access list that the avatar is not on
		CollisionNotOnAccessList(-40000),
		// Response to hovering over a parcel
		HoveredOverParcel(-50000);

		public static ParcelPropertiesStatus setValue(int value)
		{
			for (ParcelPropertiesStatus e : values())
			{
				if (e._value == value)
					return e;
			}
			return None;
		}

		public int getValue()
		{
			return _value;
		}

		private int _value;

		ParcelPropertiesStatus(int value)
		{
			this._value = value;
		}
	}

	/** The tool to use when modifying terrain levels */
	public enum TerraformAction
	{
		// Level the terrain
		Level,
		// Raise the terrain
		Raise,
		// Lower the terrain
		Lower,
		// Smooth the terrain
		Smooth,
		// Add random noise to the terrain
		Noise,
		// Revert terrain to simulator default
		Revert;

		public static TerraformAction setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/** The tool size to use when changing terrain levels */
	// [Flags]
	public static class TerraformBrushSize
	{
		// Small
		public static final byte Small = 1 << 0;
		// Medium
		public static final byte Medium = 1 << 1;
		// Large
		public static final byte Large = 1 << 2;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(byte value)
		{
			return value & _mask;
		}

		private static final byte _mask = 0x7;
	}

	/** Reasons agent is denied access to a parcel on the simulator */
	public enum AccessDeniedReason
	{
		// Agent is not denied, access is granted
		NotDenied,
		// Agent is not a member of the group set for the parcel, or which owns
		// the parcel
		NotInGroup,
		// Agent is not on the parcels specific allow list
		NotOnAllowList,
		// Agent is on the parcels ban list
		BannedFromParcel,
		// Unknown
		NoAccess,
		// Agent is not age verified and parcel settings deny access to non age
		// verified avatars
		NotAgeVerified;

		public static AccessDeniedReason setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/**
	 * Parcel overlay type. This is used primarily for highlighting and coloring
	 * which is why it is a single integer instead of a set of flags
	 * 
	 * These values seem to be poorly thought out. The first three bits
	 * represent a single value, not flags. For example Auction (0x05) is not a
	 * combination of OwnedByOther (0x01) and ForSale(0x04). However, the
	 * BorderWest and BorderSouth values are bit flags that get attached to the
	 * value stored in the first three bits. Bits four, five, and six are unused
	 */
	public enum ParcelOverlayType
	{
		// Public land
		Public(0),
		// Land is owned by another avatar
		OwnedByOther(1),
		// Land is owned by a group
		OwnedByGroup(2),
		// Land is owned by the current avatar
		OwnedBySelf(3),
		// Land is for sale
		ForSale(4),
		// Land is being auctioned
		Auction(5),
        // Land is private
        Private(32),
		// To the west of this area is a parcel border
		BorderWest(64),
		// To the south of this area is a parcel border
		BorderSouth(128);

		public static ParcelOverlayType setValue(int value)
		{
			for (ParcelOverlayType e : values())
			{
				if (e._value == value)
					return e;
			}
			return Public;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelOverlayType(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Various parcel properties */
	public static class ParcelFlags
	{
		// No flags set
		public static final int None = 0;
		// Allow avatars to fly = a client-side only restriction)
		public static final int AllowFly = 1 << 0;
		// Allow foreign scripts to run
		public static final int AllowOtherScripts = 1 << 1;
		// This parcel is for sale
		public static final int ForSale = 1 << 2;
		// Allow avatars to create a landmark on this parcel
		public static final int AllowLandmark = 1 << 3;
		// Allows all avatars to edit the terrain on this parcel
		public static final int AllowTerraform = 1 << 4;
		// Avatars have health and can take damage on this parcel.
		// If set, avatars can be killed and sent home here
		public static final int AllowDamage = 1 << 5;
		// Foreign avatars can create objects here
		public static final int CreateObjects = 1 << 6;
		// All objects on this parcel can be purchased
		public static final int ForSaleObjects = 1 << 7;
		// Access is restricted to a group
		public static final int UseAccessGroup = 1 << 8;
		// Access is restricted to a whitelist
		public static final int UseAccessList = 1 << 9;
		// Ban blacklist is enabled
		public static final int UseBanList = 1 << 10;
		// Unknown
		public static final int UsePassList = 1 << 11;
		// List this parcel in the search directory
		public static final int ShowDirectory = 1 << 12;
		// Allow personally owned parcels to be deeded to group
		public static final int AllowDeedToGroup = 1 << 13;
		// If Deeded, owner contributes required tier to group parcel is deeded
		// to
		public static final int ContributeWithDeed = 1 << 14;
		// Restrict sounds originating on this parcel to the
		// parcel boundaries
		public static final int SoundLocal = 1 << 15;
		// Objects on this parcel are sold when the land is purchsaed
		public static final int SellParcelObjects = 1 << 16;
		// Allow this parcel to be published on the web
		public static final int AllowPublish = 1 << 17;
		// The information for this parcel is mature content
		public static final int MaturePublish = 1 << 18;
		// The media URL is an HTML page
		public static final int UrlWebPage = 1 << 19;
		// The media URL is a raw HTML string
		public static final int UrlRawHtml = 1 << 20;
		// Restrict foreign object pushes
		public static final int RestrictPushObject = 1 << 21;
		// Ban all non identified/transacted avatars
		public static final int DenyAnonymous = 1 << 22;
		// Ban all identified avatars [OBSOLETE]</summary>
		// [Obsolete]
		// This was obsoleted in 1.19.0 but appears to be recycled and is used
		// on linden homes parcels
		public static final int LindenHome = 1 << 23;
		// Ban all transacted avatars [OBSOLETE]</summary>
		// [Obsolete]
		// DenyTransacted = 1 << 24;
		// Allow group-owned scripts to run
		public static final int AllowGroupScripts = 1 << 25;
		// Allow object creation by group members or group objects
		public static final int CreateGroupObjects = 1 << 26;
		// Allow all objects to enter this parcel
		public static final int AllowAPrimitiveEntry = 1 << 27;
		// Only allow group and owner objects to enter this parcel
		public static final int AllowGroupObjectEntry = 1 << 28;
		// Voice Enabled on this parcel
		public static final int AllowVoiceChat = 1 << 29;
		// Use Estate Voice channel for Voice on this parcel
		public static final int UseEstateVoiceChan = 1 << 30;
		// Deny Age Unverified Users
		public static final int DenyAgeUnverified = 1 << 31;

		public static int setValue(int value)
		{
			return value & _mask;
		}

		public static int getValue(int value)
		{
			return value & _mask;
		}

		private static final int _mask = 0xFFFFFFFF;
	}

	/** Parcel ownership status */
	public enum ParcelStatus
	{
		// Placeholder
		None(-1),
		// Parcel is leased (owned) by an avatar or group
		Leased(0),
		// Parcel is in process of being leased (purchased) by an avatar or
		// group
		LeasePending(1),
		// Parcel has been abandoned back to Governor Linden
		Abandoned(2);

		public static ParcelStatus setValue(int value)
		{
			for (ParcelStatus e : values())
			{
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelStatus(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Category parcel is listed in under search */
	public enum ParcelCategory
	{
		// No assigned category
		None(0),
		// Linden Infohub or public area
		Linden(1),
		// Adult themed area
		Adult(2),
		// Arts and Culture
		Arts(3),
		// Business
		Business(4),
		// Educational
		Educational(5),
		// Gaming
		Gaming(6),
		// Hangout or Club
		Hangout(7),
		// Newcomer friendly
		Newcomer(8),
		// Parks and Nature
		Park(9),
		// Residential
		Residential(10),
		// Shopping
		Shopping(11),
		// Not Used?
		Stage(12),
		// Other
		Other(13),
		// Not an actual category, only used for queries
		Any(-1);

		public static ParcelCategory setValue(int value)
		{
			for (ParcelCategory e : values())
			{
				if (e._value == value)
					return e;
			}
			return None;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ParcelCategory(int value)
		{
			this._value = (byte) value;
		}
	}

	/** Type of teleport landing for a parcel */
	public enum LandingTypeEnum
	{
		// Unset, simulator default
		None,
		// Specific landing point set for this parcel
		LandingPoint,
		// No landing point set, direct teleports enabled for this parcel
		Direct;

		public static LandingTypeEnum setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/** Parcel Media Command used in ParcelMediaCommandMessage */
	public enum ParcelMediaCommand
	{
		// Stop the media stream and go back to the first frame
		Stop,
		// Pause the media stream (stop playing but stay on current frame)
		Pause,
		// Start the current media stream playing and stop when the end is
		// reached
		Play,
		// Start the current media stream playing, loop to the beginning
		// when the end is reached and continue to play
		Loop,
		// Specifies the texture to replace with video. If passing the key of a
		// texture,
		// it must be explicitly typecast as a key, not just passed within
		// double quotes.
		Texture,
		// Specifies the movie URL (254 characters max)
		URL,
		// Specifies the time index at which to begin playing
		Time,
		// Specifies a single agent to apply the media command to
		Agent,
		// Unloads the stream. While the stop command sets the texture to the
		// first frame of the
		// movie, unload resets it to the real texture that the movie was
		// replacing.
		Unload,
		// Turn on/off the auto align feature, similar to the auto align
		// checkbox in the parcel
		// media properties.
		// (NOT to be confused with the "align" function in the textures view of
		// the editor!)
		// Takes TRUE or FALSE as parameter.
		AutoAlign,
		// Allows a Web page or image to be placed on a prim (1.19.1 RC0 and
		// later only).
		// Use "text/html" for HTML.
		Type,
		// Resizes a Web page to fit on x, y pixels (1.19.1 RC0 and later only).
		// This might still not be working
		Size,
		// Sets a description for the media being displayed (1.19.1 RC0 and
		// later only).
		Desc;

		public static ParcelMediaCommand setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// #region Structs

	// Parcel information retrieved from a simulator
	public class Parcel
	{
		// The total number of contiguous 4x4 meter blocks your agent owns
		// within this parcel
		public int SelfCount;
		// The total number of contiguous 4x4 meter blocks contained in this
		// parcel owned by a group or agent other than your own
		public int OtherCount;
		// Deprecated, Value appears to always be 0
		public int PublicCount;
		// Simulator-local ID of this parcel
		public int LocalID;
		// UUID of the owner of this parcel
		public UUID OwnerID;
		// Whether the land is deeded to a group or not
		public boolean IsGroupOwned;
		//
		public int AuctionID;
		// Date land was claimed
		public Date ClaimDate;
		// Appears to always be zero
		public int ClaimPrice;
		// This field is no longer used
		public int RentPrice;
		// Minimum corner of the axis-aligned bounding box for this
		// Tangible_doc_comment_body parcel
		public Vector3 AABBMin;
		// Maximum corner of the axis-aligned bounding box for this
		// Tangible_doc_comment_body parcel
		public Vector3 AABBMax;
		// Bitmap describing land layout in 4x4m squares across the
		// Tangible_doc_comment_body entire region
		public byte[] Bitmap;
		// Total parcel land area
		public int Area;
		//
		public ParcelStatus Status;
		// Maximum primitives across the entire simulator owned by the same
		// agent or group that owns this parcel that can be used
		public int SimWideMaxPrims;
		// Total primitives across the entire simulator calculated by combining
		// the allowed prim counts for each parcel
		// Tangible_doc_comment_body owned by the agent or group that owns this
		// parcel
		public int SimWideTotalPrims;
		// Maximum number of primitives this parcel supports
		public int MaxPrims;
		// Total number of primitives on this parcel
		public int TotalPrims;
		// For group-owned parcels this indicates the total number of prims
		// deeded to the group,
		// for parcels owned by an individual this inicates the number of prims
		// owned by the individual
		public int OwnerPrims;
		// Total number of primitives owned by the parcel group on this parcel,
		// or for parcels owned by an individual with a group set the total
		// number of prims set to that group.
		public int GroupPrims;
		// Total number of prims owned by other avatars that are not set to
		// group, or not the parcel owner
		public int OtherPrims;
		// A bonus multiplier which allows parcel prim counts to go over times
		// this amount, this does not affect
		// the max prims per simulator. e.g: 117 prim parcel limit x 1.5 bonus =
		// 175 allowed
		public float ParcelPrimBonus;
		// Autoreturn value in minutes for others' objects
		public int OtherCleanTime;
		//
		public int Flags;
		// Sale price of the parcel, only useful if ForSale is set
		// The SalePrice will remain the same after an ownership transfer
		// (sale), so it can be used to
		// see the purchase price after a sale if the new owner has not changed
		// it
		public int SalePrice;
		// Parcel Name
		public String Name;
		// Parcel Description
		public String Desc;
		// URL For Music Stream
		public String MusicURL;
		//
		public UUID GroupID;
		// Price for a temporary pass
		public int PassPrice;
		// How long is pass valid for
		public float PassHours;
		//
		public ParcelCategory Category;
		// Key of authorized buyer
		public UUID AuthBuyerID;
		// Key of parcel snapshot
		public UUID SnapshotID;
		// The landing point location
		public Vector3 UserLocation;
		// The landing point LookAt
		public Vector3 UserLookAt;
		// The type of landing enforced from the <see cref="LandingType"/> enum
		public LandingTypeEnum Landing;
		//
		public float Dwell;
		//
		public boolean RegionDenyAnonymous;
		//
		public boolean RegionPushOverride;
		// Access list of who is whitelisted on this
		// Tangible_doc_comment_body parcel
		public ArrayList<ParcelManager.ParcelAccessEntry> AccessWhiteList;
		// Access list of who is blacklisted on this
		// Tangible_doc_comment_body parcel
		public ArrayList<ParcelManager.ParcelAccessEntry> AccessBlackList;
		// TRUE of region denies access to age unverified users
		public boolean RegionDenyAgeUnverified;
		// true to obscure (hide) media url
		public boolean ObscureMedia;
		// true to obscure (hide) music url
		public boolean ObscureMusic;
		// A struct containing media details
		public ParcelMedia Media;

		/**
		 * Displays a parcel object in string format
		 * 
		 * @return string containing key=value pairs of a parcel object
		 */
		@Override
		public String toString()
		{
			String result = "";
			Class<? extends Parcel> parcelType = this.getClass();
			Field[] fields = parcelType.getFields();
			for (Field field : fields)
			{
				try
				{
					result += (field.getName() + " = " + field.get(this) + " ");
				}
				catch (Exception ex)
				{
				}
			}
			return result;
		}

		/**
		 * Default constructor
		 * 
		 * @param localID
		 *            Local ID of this parcel
		 */
		public Parcel(int localID)
		{
			LocalID = localID;
			ClaimDate = Helpers.Epoch;
			Bitmap = Helpers.EmptyBytes;
			Name = Helpers.EmptyString;
			Desc = Helpers.EmptyString;
			MusicURL = Helpers.EmptyString;
			AccessWhiteList = new ArrayList<ParcelManager.ParcelAccessEntry>(0);
			AccessBlackList = new ArrayList<ParcelManager.ParcelAccessEntry>(0);
			Media = new ParcelMedia();
		}

		/**
		 * Update the simulator with any local changes to this Parcel object
		 * 
		 * @param simulator
		 *            Simulator to send updates to
		 * @param wantReply
		 *            Whether we want the simulator to confirm the update with a
		 *            reply packet or not
		 * @throws Exception
		 */
		public final void Update(Simulator simulator, boolean wantReply) throws Exception
		{
			URI url = simulator.getClient().Network.getCapabilityURI("ParcelPropertiesUpdate");
			if (url != null)
			{
				ParcelPropertiesUpdateMessage req = simulator.getClient().Messages.new ParcelPropertiesUpdateMessage();
				req.AuthBuyerID = this.AuthBuyerID;
				req.Category = this.Category;
				req.Desc = this.Desc;
				req.GroupID = this.GroupID;
				req.LandingType = this.Landing;
				req.LocalID = this.LocalID;
				req.MediaAutoScale = this.Media.MediaAutoScale;
				req.MediaDesc = this.Media.MediaDesc;
				req.MediaHeight = this.Media.MediaHeight;
				req.MediaID = this.Media.MediaID;
				req.MediaLoop = this.Media.MediaLoop;
				req.MediaType = this.Media.MediaType;
				req.MediaURL = this.Media.MediaURL;
				req.MediaWidth = this.Media.MediaWidth;
				req.MusicURL = this.MusicURL;
				req.Name = this.Name;
				req.ObscureMedia = this.ObscureMedia;
				req.ObscureMusic = this.ObscureMusic;
				req.ParcelFlags = this.Flags;
				req.PassHours = this.PassHours;
				req.PassPrice = this.PassPrice;
				req.SalePrice = this.SalePrice;
				req.SnapshotID = this.SnapshotID;
				req.UserLocation = this.UserLocation;
				req.UserLookAt = this.UserLookAt;

				OSDMap body = req.Serialize();

				new CapsClient().executeHttpPost(url, body, OSDFormat.Xml, simulator.getClient().Settings.CAPS_TIMEOUT);
			}
			else
			{
				ParcelPropertiesUpdatePacket request = new ParcelPropertiesUpdatePacket();

				request.AgentData.AgentID = simulator.getClient().Self.getAgentID();
				request.AgentData.SessionID = simulator.getClient().Self.getSessionID();

				request.ParcelData.LocalID = this.LocalID;

				request.ParcelData.AuthBuyerID = this.AuthBuyerID;
				request.ParcelData.Category = this.Category.getValue();
				request.ParcelData.setDesc(Helpers.StringToBytes(this.Desc));
				request.ParcelData.GroupID = this.GroupID;
				request.ParcelData.LandingType = this.Landing.getValue();
				request.ParcelData.MediaAutoScale = (this.Media.MediaAutoScale) ? (byte) 0x1 : (byte) 0x0;
				request.ParcelData.MediaID = this.Media.MediaID;
				request.ParcelData.setMediaURL(Helpers.StringToBytes(this.Media.MediaURL.toString()));
				request.ParcelData.setMusicURL(Helpers.StringToBytes(this.MusicURL.toString()));
				request.ParcelData.setName(Helpers.StringToBytes(this.Name));
				if (wantReply)
				{
					request.ParcelData.Flags = 1;
				}
				request.ParcelData.ParcelFlags = this.Flags;
				request.ParcelData.PassHours = this.PassHours;
				request.ParcelData.PassPrice = this.PassPrice;
				request.ParcelData.SalePrice = this.SalePrice;
				request.ParcelData.SnapshotID = this.SnapshotID;
				request.ParcelData.UserLocation = this.UserLocation;
				request.ParcelData.UserLookAt = this.UserLookAt;

				simulator.SendPacket(request);
			}
			UpdateOtherCleanTime(simulator);
		}

		/**
		 * Set Autoreturn time
		 * 
		 * @param simulator
		 *            Simulator to send the update to
		 * @throws Exception
		 */
		public final void UpdateOtherCleanTime(Simulator simulator) throws Exception
		{
			ParcelSetOtherCleanTimePacket request = new ParcelSetOtherCleanTimePacket();
			request.AgentData.AgentID = simulator.getClient().Self.getAgentID();
			request.AgentData.SessionID = simulator.getClient().Self.getSessionID();
			request.ParcelData.LocalID = this.LocalID;
			request.ParcelData.OtherCleanTime = this.OtherCleanTime;

			simulator.SendPacket(request);
		}
	}

	// Some information about a parcel of land returned from a DirectoryManager
	// search
	public final class ParcelInfo
	{
		// Global Key of record
		public UUID ID;
		// Parcel Owners {@link UUID}
		public UUID OwnerID;
		// Name field of parcel, limited to 128 characters
		public String Name;
		// Description field of parcel, limited to 256 characters
		public String Description;
		// Total Square meters of parcel
		public int ActualArea;
		// Total area billable as Tier, for group owned land this will be 10%
		// less than ActualArea
		public int BillableArea;
		// True of parcel is in Mature simulator
		public boolean Mature;
		// Grid global X position of parcel
		public float GlobalX;
		// Grid global Y position of parcel
		public float GlobalY;
		// Grid global Z position of parcel (not used)
		public float GlobalZ;
		// Name of simulator parcel is located in
		public String SimName;
		// Texture {@link T:OpenMetaverse.UUID} of parcels display picture
		public UUID SnapshotID;
		// Float representing calculated traffic based on time spent on parcel
		// by avatars
		public float Dwell;
		// Sale price of parcel (not used)
		public int SalePrice;
		// Auction ID of parcel
		public int AuctionID;
	}

	// Parcel Media Information
	public final class ParcelMedia
	{
		// A byte, if 0x1 viewer should auto scale media to fit object
		public boolean MediaAutoScale;
		// A boolean, if true the viewer should loop the media
		public boolean MediaLoop;
		// The Asset UUID of the Texture which when applied to a primitive will
		// display the media
		public UUID MediaID;
		// A URL which points to any Quicktime supported media type
		public String MediaURL;
		// A description of the media
		public String MediaDesc;
		// An Integer which represents the height of the media
		public int MediaHeight;
		// An integer which represents the width of the media
		public int MediaWidth;
		// A string which contains the mime type of the media
		public String MediaType;
	}

	public final class ParcelAccessEntry
	{
		// Agents {@link T:OpenMetaverse.UUID}
		public UUID AgentID;
		//
		public Date Time;
		// Flags for specific entry in white/black lists
		public byte Flags;
	}

	// Owners of primitives on parcel
	public final class ParcelPrimOwners
	{
		// Prim Owners {@link T:OpenMetaverse.UUID}
		public UUID OwnerID;
		// True of owner is group
		public boolean IsGroupOwned;
		// Total count of prims owned by OwnerID
		public int Count;
		// true of OwnerID is currently online and is not a group
		public boolean OnlineStatus;
		// The date of the most recent prim left by OwnerID
		public Date NewestPrim;
	}

	// #endregion Structs

	// Contains a parcels dwell data returned from the simulator in response to
	// an <see cref="RequestParcelDwell"/>
	public class ParcelDwellReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_ParcelID;
		private final int m_LocalID;
		private final float m_Dwell;

		// Tangible_doc_comment_start Get the global ID of the parcel
		public final UUID getParcelID()
		{
			return m_ParcelID;
		}

		// Tangible_doc_comment_start Get the simulator specific ID of the
		// parcel
		public final int getLocalID()
		{
			return m_LocalID;
		}

		// Tangible_doc_comment_start Get the calculated dwell
		public final float getDwell()
		{
			return m_Dwell;
		}

		/**
		 * Construct a new instance of the ParcelDwellReplyCallbackArgs class
		 * 
		 * @param parcelID
		 *            The global ID of the parcel
		 * @param localID
		 *            The simulator specific ID of the parcel
		 * @param dwell
		 *            The calculated dwell for the parcel
		 */
		public ParcelDwellReplyCallbackArgs(UUID parcelID, int localID, float dwell)
		{
			this.m_ParcelID = parcelID;
			this.m_LocalID = localID;
			this.m_Dwell = dwell;
		}
	}

	public CallbackHandler<ParcelDwellReplyCallbackArgs> OnParcelDwellReply = new CallbackHandler<ParcelDwellReplyCallbackArgs>();

	// Contains basic parcel information data returned from the simulator in
	// response to an <see cref="RequestParcelInfo"/> request
	public class ParcelInfoReplyCallbackArgs implements CallbackArgs
	{
		private final ParcelInfo m_Parcel;

		// Get the <see cref="ParcelInfo"/> object containing basic parcel info
		public final ParcelInfo getParcel()
		{
			return m_Parcel;
		}

		/**
		 * Construct a new instance of the ParcelInfoReplyCallbackArgs class
		 * 
		 * @param parcel
		 *            The <see cref="ParcelInfo"/> object containing basic
		 *            parcel info
		 */
		public ParcelInfoReplyCallbackArgs(ParcelInfo parcel)
		{
			this.m_Parcel = parcel;
		}
	}

	public CallbackHandler<ParcelInfoReplyCallbackArgs> OnParcelInfoReply = new CallbackHandler<ParcelInfoReplyCallbackArgs>();

	// Contains basic parcel information data returned from the simulator in
	// response to an <see cref="RequestParcelInfo"/> request
	public class ParcelPropertiesCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private Parcel m_Parcel;
		private final ParcelResult m_Result;
		private final int m_SelectedPrims;
		private final int m_SequenceID;
		private final boolean m_SnapSelection;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the <see cref="Parcel"/> object containing the details. If Result
		// is NoData, this object will not contain valid data
		public final Parcel getParcel()
		{
			return m_Parcel;
		}

		// Get the result of the request
		public final ParcelResult getResult()
		{
			return m_Result;
		}

		// Get the number of primitieves your agent is currently selecting and
		// or sitting on in this parcel
		public final int getSelectedPrims()
		{
			return m_SelectedPrims;
		}

		// Get the user assigned ID used to correlate a request with these
		// results
		public final int getSequenceID()
		{
			return m_SequenceID;
		}

		// TODO:
		public final boolean getSnapSelection()
		{
			return m_SnapSelection;
		}

		/**
		 * Construct a new instance of the ParcelPropertiesCallbackArgs class
		 * 
		 * @param simulator
		 *            The <see cref="Parcel"/> object containing the details
		 * @param parcel
		 *            The <see cref="Parcel"/> object containing the details
		 * @param result
		 *            The result of the request
		 * @param selectedPrims
		 *            The number of primitieves your agent is currently
		 *            selecting and or sitting on in this parcel
		 * @param sequenceID
		 *            The user assigned ID used to correlate a request with
		 *            these results
		 * @param snapSelection
		 *            TODO:
		 */
		public ParcelPropertiesCallbackArgs(Simulator simulator, Parcel parcel, ParcelResult result, int selectedPrims,
				int sequenceID, boolean snapSelection)
		{
			this.m_Simulator = simulator;
			this.m_Parcel = parcel;
			this.m_Result = result;
			this.m_SelectedPrims = selectedPrims;
			this.m_SequenceID = sequenceID;
			this.m_SnapSelection = snapSelection;
		}
	}

	public CallbackHandler<ParcelPropertiesCallbackArgs> OnParcelProperties = new CallbackHandler<ParcelPropertiesCallbackArgs>();

	// Contains blacklist and whitelist data returned from the simulator in
	// response to an <see cref="RequestParcelAccesslist"/> request
	public class ParcelAccessListReplyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final int m_SequenceID;
		private final int m_LocalID;
		private final int m_Flags;
		private final ArrayList<ParcelManager.ParcelAccessEntry> m_AccessList;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the user assigned ID used to correlate a request with these
		// results
		public final int getSequenceID()
		{
			return m_SequenceID;
		}

		// Get the simulator specific ID of the parcel
		public final int getLocalID()
		{
			return m_LocalID;
		}

		// TODO:
		public final int getFlags()
		{
			return m_Flags;
		}

		// Get the list containing the white/blacklisted agents for the parcel
		public final ArrayList<ParcelManager.ParcelAccessEntry> getAccessList()
		{
			return m_AccessList;
		}

		/**
		 * Construct a new instance of the ParcelAccessListReplyCallbackArgs
		 * class
		 * 
		 * @param simulator
		 *            The simulator the parcel is located in
		 * @param sequenceID
		 *            The user assigned ID used to correlate a request with
		 *            these results
		 * @param localID
		 *            The simulator specific ID of the parcel
		 * @param flags
		 *            TODO:
		 * @param accessEntries
		 *            The list containing the white/blacklisted agents for the
		 *            parcel
		 */
		public ParcelAccessListReplyCallbackArgs(Simulator simulator, int sequenceID, int localID, int flags,
				ArrayList<ParcelManager.ParcelAccessEntry> accessEntries)
		{
			this.m_Simulator = simulator;
			this.m_SequenceID = sequenceID;
			this.m_LocalID = localID;
			this.m_Flags = flags;
			this.m_AccessList = accessEntries;
		}
	}

	public CallbackHandler<ParcelAccessListReplyCallbackArgs> OnParcelAccessListReply = new CallbackHandler<ParcelAccessListReplyCallbackArgs>();

	// Contains blacklist and whitelist data returned from the simulator in
	// response to an <see cref="RequestParcelAccesslist"/> request
	public class ParcelObjectOwnersReplyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final java.util.ArrayList<ParcelManager.ParcelPrimOwners> m_Owners;

		// Get the simulator the parcel is located in
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the list containing prim ownership counts
		public final java.util.ArrayList<ParcelManager.ParcelPrimOwners> getPrimOwners()
		{
			return m_Owners;
		}

		/**
		 * Construct a new instance of the ParcelObjectOwnersReplyCallbackArgs
		 * class
		 * 
		 * @param simulator
		 *            The simulator the parcel is located in
		 * @param primOwners
		 *            The list containing prim ownership counts
		 */
		public ParcelObjectOwnersReplyCallbackArgs(Simulator simulator,
				java.util.ArrayList<ParcelManager.ParcelPrimOwners> primOwners)
		{
			this.m_Simulator = simulator;
			this.m_Owners = primOwners;
		}
	}

	public CallbackHandler<ParcelObjectOwnersReplyCallbackArgs> OnParcelObjectOwnersReply = new CallbackHandler<ParcelObjectOwnersReplyCallbackArgs>();

	// Contains the data returned when all parcel data has been retrieved from a
	// simulator
	public class SimParcelsDownloadedCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final HashMap<Integer, Parcel> m_Parcels;
		private final int[] m_ParcelMap;

		// Get the simulator the parcel data was retrieved from
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// A dictionary containing the parcel data where the key correlates to
		// the ParcelMap entry
		public final HashMap<Integer, Parcel> getParcels()
		{
			return m_Parcels;
		}

		// Get the multidimensional array containing a x,y grid mapped to each
		// 64x64 parcel's LocalID.
		public final int[] getParcelMap()
		{
			return m_ParcelMap;
		}

		/**
		 * Construct a new instance of the SimParcelsDownloadedCallbackArgs
		 * class
		 * 
		 * @param simulator
		 *            The simulator the parcel data was retrieved from
		 * @param simParcels
		 *            The dictionary containing the parcel data
		 * @param is
		 *            The multidimensional array containing a x,y grid mapped to
		 *            each 64x64 parcel's LocalID.
		 */
		public SimParcelsDownloadedCallbackArgs(Simulator simulator, HashMap<Integer, Parcel> simParcels, int[] is)
		{
			this.m_Simulator = simulator;
			this.m_Parcels = simParcels;
			this.m_ParcelMap = is;
		}
	}

	public CallbackHandler<SimParcelsDownloadedCallbackArgs> OnSimParcelsDownloaded = new CallbackHandler<SimParcelsDownloadedCallbackArgs>();

	// Contains the data returned when a <see cref="RequestForceSelectObjects"/>
	// request
	public class ForceSelectObjectsReplyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final int[] m_ObjectIDs;
		private final boolean m_ResetList;

		// Get the simulator the parcel data was retrieved from
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the list of primitive IDs
		public final int[] getObjectIDs()
		{
			return m_ObjectIDs;
		}

		// true if the list is clean and contains the information only for a
		// given request
		public final boolean getResetList()
		{
			return m_ResetList;
		}

		/**
		 * Construct a new instance of the ForceSelectObjectsReplyCallbackArgs
		 * class
		 * 
		 * @param simulator
		 *            The simulator the parcel data was retrieved from
		 * @param objectIDs
		 *            The list of primitive IDs
		 * @param resetList
		 *            true if the list is clean and contains the information
		 *            only for a given request
		 */
		public ForceSelectObjectsReplyCallbackArgs(Simulator simulator, int[] objectIDs, boolean resetList)
		{
			this.m_Simulator = simulator;
			this.m_ObjectIDs = objectIDs;
			this.m_ResetList = resetList;
		}
	}

	public CallbackHandler<ForceSelectObjectsReplyCallbackArgs> OnForceSelectObjectsReply = new CallbackHandler<ForceSelectObjectsReplyCallbackArgs>();

	// Contains data when the media data for a parcel the avatar is on changes
	public class ParcelMediaUpdateReplyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final ParcelMedia m_ParcelMedia;

		// Get the simulator the parcel media data was updated in
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the updated media information
		public final ParcelMedia getMedia()
		{
			return m_ParcelMedia;
		}

		/**
		 * Construct a new instance of the ParcelMediaUpdateReplyCallbackArgs
		 * class
		 * 
		 * @param simulator
		 *            the simulator the parcel media data was updated in
		 * @param media
		 *            The updated media information
		 */
		public ParcelMediaUpdateReplyCallbackArgs(Simulator simulator, ParcelMedia media)
		{
			this.m_Simulator = simulator;
			this.m_ParcelMedia = media;
		}
	}

	public CallbackHandler<ParcelMediaUpdateReplyCallbackArgs> OnParcelMediaUpdateReply = new CallbackHandler<ParcelMediaUpdateReplyCallbackArgs>();

	// Contains the media command for a parcel the agent is currently on
	public class ParcelMediaCommandCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final int m_Sequence;
		private final int m_ParcelFlags;
		private final ParcelMediaCommand m_MediaCommand;
		private final float m_Time;

		// Get the simulator the parcel media command was issued in
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public final int getSequence()
		{
			return m_Sequence;
		}

		public final int getParcelFlags()
		{
			return m_ParcelFlags;
		}

		// Get the media command that was sent
		public final ParcelMediaCommand getMediaCommand()
		{
			return m_MediaCommand;
		}

		public final float getTime()
		{
			return m_Time;
		}

		/**
		 * Construct a new instance of the ParcelMediaCommandCallbackArgs class
		 * 
		 * @param simulator
		 *            The simulator the parcel media command was issued in
		 * @param sequence
		 * @param flags
		 * @param command
		 *            The media command that was sent
		 * @param time
		 */
		public ParcelMediaCommandCallbackArgs(Simulator simulator, int sequence, int flags, ParcelMediaCommand command,
				float time)
		{
			this.m_Simulator = simulator;
			this.m_Sequence = sequence;
			this.m_ParcelFlags = flags;
			this.m_MediaCommand = command;
			this.m_Time = time;
		}
	}

	public CallbackHandler<ParcelMediaCommandCallbackArgs> OnParcelMediaCommand = new CallbackHandler<ParcelMediaCommandCallbackArgs>();
	// #endregion

	private GridClient _Client;
	private TimeoutEvent<Boolean> WaitForSimParcel;

	public ParcelManager(GridClient client)
	{
		_Client = client;

		// Setup the callbacks
		_Client.Network.RegisterCallback(CapsEventType.ParcelObjectOwnersReply, this);
		_Client.Network.RegisterCallback(CapsEventType.ParcelProperties, this);

		_Client.Network.RegisterCallback(PacketType.ParcelInfoReply, this);
		_Client.Network.RegisterCallback(PacketType.ParcelDwellReply, this);
		_Client.Network.RegisterCallback(PacketType.ParcelAccessListReply, this);
		_Client.Network.RegisterCallback(PacketType.ForceObjectSelect, this);
		_Client.Network.RegisterCallback(PacketType.ParcelMediaUpdate, this);
		_Client.Network.RegisterCallback(PacketType.ParcelOverlay, this);
		_Client.Network.RegisterCallback(PacketType.ParcelMediaCommandMessage, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case ParcelInfoReply:
				HandleParcelInfoReply(packet, simulator);
				break;
			case ParcelDwellReply:
				HandleParcelDwellReply(packet, simulator);
				break;
			case ParcelAccessListReply:
				HandleParcelAccessListReply(packet, simulator);
				break;
			case ForceObjectSelect:
				HandleSelectParcelObjectsReply(packet, simulator);
				break;
			case ParcelMediaUpdate:
				HandleParcelMediaUpdate(packet, simulator);
				break;
			case ParcelOverlay:
				HandleParcelOverlay(packet, simulator);
				break;
			case ParcelMediaCommandMessage:
				HandleParcelMediaCommandMessagePacket(packet, simulator);
				break;
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator) throws Exception
	{
		switch (message.getType())
		{
			case ParcelObjectOwnersReply:
				HandleParcelObjectOwnersReply(message, simulator);
				break;
			case ParcelProperties:
				HandleParcelPropertiesReply(message, simulator);
				break;
		}
	}

	// #region Public Methods

	/**
	 * Request basic information for a single parcel
	 * 
	 * @param parcelID
	 *            Simulator-local ID of the parcel
	 * @throws Exception
	 */
	public final void RequestParcelInfo(UUID parcelID) throws Exception
	{
		ParcelInfoRequestPacket request = new ParcelInfoRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ParcelID = parcelID;

		_Client.Network.SendPacket(request);
	}

	/**
	 * Request properties of a single parcel
	 * 
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelProperties reply, useful for distinguishing between
	 *            multiple simultaneous requests
	 * @throws Exception
	 */
	public final void RequestParcelProperties(Simulator simulator, int localID, int sequenceID) throws Exception
	{
		ParcelPropertiesRequestByIDPacket request = new ParcelPropertiesRequestByIDPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.ParcelData.LocalID = localID;
		request.ParcelData.SequenceID = sequenceID;

		simulator.SendPacket(request);
	}

	/**
	 * Request the access list for a single parcel
	 * 
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelAccessList reply, useful for distinguishing between
	 *            multiple simultaneous requests
	 * @param flags
	 * @throws Exception
	 */
	public final void RequestParcelAccessList(Simulator simulator, int localID, byte flags, int sequenceID)
			throws Exception
	{
		ParcelAccessListRequestPacket request = new ParcelAccessListRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Data.LocalID = localID;
		request.Data.Flags = AccessList.setValue(flags);
		request.Data.SequenceID = sequenceID;

		simulator.SendPacket(request);
	}

	/**
	 * Request properties of parcels using a bounding box selection
	 * 
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param north
	 *            Northern boundary of the parcel selection
	 * @param east
	 *            Eastern boundary of the parcel selection
	 * @param south
	 *            Southern boundary of the parcel selection
	 * @param west
	 *            Western boundary of the parcel selection
	 * @param sequenceID
	 *            An arbitrary integer that will be returned with the
	 *            ParcelProperties reply, useful for distinguishing between
	 *            different types of parcel property requests
	 * @param snapSelection
	 *            A boolean that is returned with the ParcelProperties reply,
	 *            useful for snapping focus to a single parcel
	 * @throws Exception
	 */
	public final void RequestParcelProperties(Simulator simulator, float north, float east, float south, float west,
			int sequenceID, boolean snapSelection) throws Exception
	{
		ParcelPropertiesRequestPacket request = new ParcelPropertiesRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ParcelData.North = north;
		request.ParcelData.East = east;
		request.ParcelData.South = south;
		request.ParcelData.West = west;
		request.ParcelData.SequenceID = sequenceID;
		request.ParcelData.SnapSelection = snapSelection;

		simulator.SendPacket(request);
	}

	/**
	 * Request all simulator parcel properties (used for populating the
	 * <code>Simulator.Parcels</code> dictionary)
	 * 
	 * @param simulator
	 *            Simulator to request parcels from (must be connected)
	 * @throws Exception
	 */
	public final void RequestAllSimParcels(Simulator simulator) throws Exception
	{
		RequestAllSimParcels(simulator, false, 750);
	}

	/**
	 * Request all simulator parcel properties (used for populating the
	 * <code>Simulator.Parcels</code> dictionary)
	 * 
	 * @param simulator
	 *            Simulator to request parcels from (must be connected)
	 * @param refresh
	 *            If TRUE, will force a full refresh
	 * @param msDelay
	 *            Number of milliseconds to pause in between each request
	 * @throws Exception
	 */
	public final void RequestAllSimParcels(final Simulator simulator, boolean refresh, final int msDelay)
			throws Exception
	{
		if (simulator.getDownloadingParcelMap())
		{
			Logger.Log("Already downloading parcels in " + simulator.getName(), LogLevel.Info, _Client);
			return;
		}
		simulator.setDownloadingParcelMap(true);
		WaitForSimParcel = new TimeoutEvent<Boolean>();

		if (refresh)
		{
			simulator.clearParcelMap();
		}

		// Wait the given amount of time for a reply before sending the next
		// request
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				if (!_Client.Network.getConnected())
				{
					return;
				}

				int count = 0, timeouts = 0, y, x;
				for (y = 0; y < 64; y++)
				{
					for (x = 0; x < 64; x++)
					{

						if (simulator.getParcelMap(y, x) == 0)
						{
							try
							{
								RequestParcelProperties(simulator, (y + 1) * 4.0f, (x + 1) * 4.0f, y * 4.0f, x * 4.0f,
										Integer.MAX_VALUE, false);
								if (WaitForSimParcel.waitOne(msDelay) == null)
								{
									++timeouts;
								}
							}
							catch (Exception e)
							{
							}
							++count;
						}
					}
				}
				Logger.Log(
						String.format(
								"Full simulator parcel information retrieved. Sent %d parcel requests. Current outgoing queue: %d, Retry Count %d",
								count, _Client.Network.getOutboxCount(), timeouts), LogLevel.Info, _Client);
				WaitForSimParcel = null;
				simulator.setDownloadingParcelMap(false);
			}
		};
		thread.start();
	}

	/**
	 * Request the dwell value for a parcel
	 * 
	 * @param simulator
	 *            Simulator containing the parcel
	 * @param localID
	 *            Simulator-local ID of the parcel
	 * @throws Exception
	 */
	public final void RequestDwell(Simulator simulator, int localID) throws Exception
	{
		ParcelDwellRequestPacket request = new ParcelDwellRequestPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Data.LocalID = localID;
		request.Data.ParcelID = UUID.Zero; // Not used by clients

		simulator.SendPacket(request);
	}

	/**
	 * Send a request to Purchase a parcel of land
	 * 
	 * @param simulator
	 *            The Simulator the parcel is located in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param forGroup
	 *            true if this parcel is being purchased by a group
	 * @param groupID
	 *            The groups {@link T:OpenMetaverse.UUID}
	 * @param removeContribution
	 *            true to remove tier contribution if purchase is successful
	 * @param parcelArea
	 *            The parcels size
	 * @param parcelPrice
	 *            The purchase price of the parcel
	 * @throws Exception
	 */
	public final void Buy(Simulator simulator, int localID, boolean forGroup, UUID groupID, boolean removeContribution,
			int parcelArea, int parcelPrice) throws Exception
	{
		ParcelBuyPacket request = new ParcelBuyPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.Data.Final = true;
		request.Data.GroupID = groupID;
		request.Data.LocalID = localID;
		request.Data.IsGroupOwned = forGroup;
		request.Data.RemoveContribution = removeContribution;

		request.ParcelData.Area = parcelArea;
		request.ParcelData.Price = parcelPrice;

		simulator.SendPacket(request);
	}

	/**
	 * Reclaim a parcel of land
	 * 
	 * @param simulator
	 *            The simulator the parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @throws Exception
	 */
	public final void Reclaim(Simulator simulator, int localID) throws Exception
	{
		ParcelReclaimPacket request = new ParcelReclaimPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.LocalID = localID;

		simulator.SendPacket(request);
	}

	/**
	 * Deed a parcel to a group
	 * 
	 * @param simulator
	 *            The simulator the parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param groupID
	 *            The groups {@link T:OpenMetaverse.UUID}
	 * @throws Exception
	 */
	public final void DeedToGroup(Simulator simulator, int localID, UUID groupID) throws Exception
	{
		ParcelDeedToGroupPacket request = new ParcelDeedToGroupPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.Data.LocalID = localID;
		request.Data.GroupID = groupID;

		simulator.SendPacket(request);
	}

	/**
	 * Request prim owners of a parcel of land.
	 * 
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @throws Exception
	 */
	public final void RequestObjectOwners(Simulator simulator, int localID) throws Exception
	{
		ParcelObjectOwnersRequestPacket request = new ParcelObjectOwnersRequestPacket();

		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.LocalID = localID;
		simulator.SendPacket(request);
	}

	/**
	 * Return objects from a parcel
	 * 
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            The parcels region specific local ID
	 * @param type
	 *            the type of objects to return, {@link T
	 *            :OpenMetaverse.ObjectReturnType}
	 * @param ownerIDs
	 *            A list containing object owners {@link OpenMetaverse.UUID} s
	 *            to return
	 * @throws Exception
	 */
	public final void ReturnObjects(Simulator simulator, int localID, byte type, UUID[] ownerIDs) throws Exception
	{
		ParcelReturnObjectsPacket request = new ParcelReturnObjectsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.ParcelData.LocalID = localID;
		request.ParcelData.ReturnType = ObjectReturnType.setValue(type);

		// A single null TaskID is (not) used for parcel object returns
		request.TaskID = new UUID[1];
		request.TaskID[0] = UUID.Zero;

		// Convert the list of owner UUIDs to packet blocks if a list is given
		if (ownerIDs != null)
		{
			request.OwnerID = new UUID[ownerIDs.length];

			for (int i = 0; i < ownerIDs.length; i++)
			{
				request.OwnerID[i] = ownerIDs[i];
			}
		}
		else
		{
			request.OwnerID = new UUID[0];
		}

		simulator.SendPacket(request);
	}

	/**
	 * Subdivide (split) a parcel
	 * 
	 * @param simulator
	 * @param west
	 * @param south
	 * @param east
	 * @param north
	 * @throws Exception
	 */
	public final void ParcelSubdivide(Simulator simulator, float west, float south, float east, float north)
			throws Exception
	{
		ParcelDividePacket divide = new ParcelDividePacket();
		divide.AgentData.AgentID = _Client.Self.getAgentID();
		divide.AgentData.SessionID = _Client.Self.getSessionID();
		divide.ParcelData.East = east;
		divide.ParcelData.North = north;
		divide.ParcelData.South = south;
		divide.ParcelData.West = west;

		simulator.SendPacket(divide);
	}

	/**
	 * Join two parcels of land creating a single parcel
	 * 
	 * @param simulator
	 * @param west
	 * @param south
	 * @param east
	 * @param north
	 * @throws Exception
	 */
	public final void ParcelJoin(Simulator simulator, float west, float south, float east, float north)
			throws Exception
	{
		ParcelJoinPacket join = new ParcelJoinPacket();
		join.AgentData.AgentID = _Client.Self.getAgentID();
		join.AgentData.SessionID = _Client.Self.getSessionID();
		join.ParcelData.East = east;
		join.ParcelData.North = north;
		join.ParcelData.South = south;
		join.ParcelData.West = west;

		simulator.SendPacket(join);
	}

	/**
	 * Get a parcels LocalID
	 * 
	 * @param simulator
	 *            Simulator parcel is in
	 * @param position
	 *            Vector3 position in simulator (Z not used)
	 * @return 0 on failure, or parcel LocalID on success. A call to
	 *         <code>Parcels.RequestAllSimParcels</code> is required to populate
	 *         map and dictionary.
	 */
	public final int GetParcelLocalID(Simulator simulator, Vector3 position)
	{
		int value = simulator.getParcelMap((byte) position.Y / 4, (byte) position.X / 4);
		if (value > 0)
		{
			return value;
		}

		Logger.Log(
				String.format(
						"ParcelMap returned an default/invalid value for location %d/%d Did you use RequestAllSimParcels() to populate the dictionaries?",
						(byte) position.Y / 4, (byte) position.X / 4), LogLevel.Warning);
		return 0;
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 * 
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must
	 *         be true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(Simulator simulator, int localID, TerraformAction action, byte brushSize)
			throws Exception
	{
		return Terraform(simulator, localID, 0f, 0f, 0f, 0f, action, brushSize, 1);
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 * 
	 * @param simulator
	 *            Simulator land area is in.
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must
	 *         be true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(Simulator simulator, float west, float south, float east, float north,
			TerraformAction action, byte brushSize) throws Exception
	{
		return Terraform(simulator, -1, west, south, east, north, action, brushSize, 1);
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 * 
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @param seconds
	 *            How many meters + or - to lower, 1 = 1 meter
	 * @return true on successful request sent. Settings.STORE_LAND_PATCHES must
	 *         be true, Parcel information must be downloaded using
	 *         <code>RequestAllSimParcels()</code>
	 * @throws Exception
	 */
	public final boolean Terraform(Simulator simulator, int localID, float west, float south, float east, float north,
			TerraformAction action, byte brushSize, int seconds) throws Exception
	{
		float height = 0f;
		int x, y;
		if (localID == -1)
		{
			x = (int) east - (int) west / 2;
			y = (int) north - (int) south / 2;
		}
		else
		{
			Parcel p;
			if (!simulator.Parcels.containsKey(localID))
			{
				Logger.Log(String.format("Can't find parcel %d in simulator %s", localID, simulator), LogLevel.Warning,
						_Client);
				return false;
			}
			p = simulator.Parcels.get(localID);
			x = (int) p.AABBMax.X - (int) p.AABBMin.X / 2;
			y = (int) p.AABBMax.Y - (int) p.AABBMin.Y / 2;
		}
		RefObject<Float> ref = new RefObject<Float>(height);
		if (!simulator.TerrainHeightAtPoint(x, y, ref))
		{
			Logger.Log("Land Patch not stored for location", LogLevel.Warning, _Client);
			return false;
		}

		Terraform(simulator, localID, west, south, east, north, action, brushSize, seconds, ref.argvalue);
		return true;
	}

	/**
	 * Terraform (raise, lower, etc) an area or whole parcel of land
	 * 
	 * @param simulator
	 *            Simulator land area is in.
	 * @param localID
	 *            LocalID of parcel, or -1 if using bounding box
	 * @param west
	 *            west border of area to modify
	 * @param south
	 *            south border of area to modify
	 * @param east
	 *            east border of area to modify
	 * @param north
	 *            north border of area to modify
	 * @param action
	 *            From Enum, Raise, Lower, Level, Smooth, Etc.
	 * @param brushSize
	 *            Size of area to modify
	 * @param seconds
	 *            How many meters + or - to lower, 1 = 1 meter
	 * @param height
	 *            Height at which the terraform operation is acting at
	 * @throws Exception
	 */
	public final void Terraform(Simulator simulator, int localID, float west, float south, float east, float north,
			TerraformAction action, byte brushSize, int seconds, float height) throws Exception
	{
		ModifyLandPacket land = new ModifyLandPacket();
		land.AgentData.AgentID = _Client.Self.getAgentID();
		land.AgentData.SessionID = _Client.Self.getSessionID();

		land.ModifyBlock.Action = action.getValue();
		land.ModifyBlock.BrushSize = brushSize;
		land.ModifyBlock.Seconds = seconds;
		land.ModifyBlock.Height = height;

		land.ParcelData = new ModifyLandPacket.ParcelDataBlock[1];
		land.ParcelData[0] = land.new ParcelDataBlock();
		land.ParcelData[0].LocalID = localID;
		land.ParcelData[0].West = west;
		land.ParcelData[0].South = south;
		land.ParcelData[0].East = east;
		land.ParcelData[0].North = north;

		land.BrushSize = new float[1];
		land.BrushSize[0] = brushSize;

		simulator.SendPacket(land);
	}

	/**
	 * Sends a request to the simulator to return a list of objects owned by
	 * specific owners
	 * 
	 * @param localID
	 *            Simulator local ID of parcel
	 * @param selectType
	 *            Owners, Others, Etc
	 * @param ownerID
	 *            List containing keys of avatars objects to select; if List is
	 *            null will return Objects of type <c>selectType</c> Response
	 *            data is returned in the event {@link E
	 *            :OnParcelSelectedObjects}
	 */
	public final void RequestSelectObjects(int localID, byte selectType, UUID ownerID) throws Exception
	{
		ParcelSelectObjectsPacket select = new ParcelSelectObjectsPacket();
		select.AgentData.AgentID = _Client.Self.getAgentID();
		select.AgentData.SessionID = _Client.Self.getSessionID();

		select.ParcelData.LocalID = localID;
		select.ParcelData.ReturnType = selectType;

		select.ReturnID = new UUID[1];
		select.ReturnID[0] = ownerID;

		_Client.Network.SendPacket(select);
	}

	/**
	 * Eject and optionally ban a user from a parcel
	 * 
	 * @param targetID
	 *            target key of avatar to eject
	 * @param ban
	 *            true to also ban target
	 */
	public final void EjectUser(UUID targetID, boolean ban) throws Exception
	{
		EjectUserPacket eject = new EjectUserPacket();
		eject.AgentData.AgentID = _Client.Self.getAgentID();
		eject.AgentData.SessionID = _Client.Self.getSessionID();
		eject.Data.TargetID = targetID;
		if (ban)
		{
			eject.Data.Flags = 1;
		}
		else
		{
			eject.Data.Flags = 0;
		}
		_Client.Network.SendPacket(eject);
	}

	/**
	 * Freeze or unfreeze an avatar over your land
	 * 
	 * @param targetID
	 *            target key to freeze
	 * @param freeze
	 *            true to freeze, false to unfreeze
	 */
	public final void FreezeUser(UUID targetID, boolean freeze) throws Exception
	{
		FreezeUserPacket frz = new FreezeUserPacket();
		frz.AgentData.AgentID = _Client.Self.getAgentID();
		frz.AgentData.SessionID = _Client.Self.getSessionID();
		frz.Data.TargetID = targetID;
		if (freeze)
		{
			frz.Data.Flags = 0;
		}
		else
		{
			frz.Data.Flags = 1;
		}

		_Client.Network.SendPacket(frz);
	}

	/**
	 * Abandon a parcel of land
	 * 
	 * @param simulator
	 *            Simulator parcel is in
	 * @param localID
	 *            Simulator local ID of parcel
	 */
	public final void ReleaseParcel(Simulator simulator, int localID) throws Exception
	{
		ParcelReleasePacket abandon = new ParcelReleasePacket();
		abandon.AgentData.AgentID = _Client.Self.getAgentID();
		abandon.AgentData.SessionID = _Client.Self.getSessionID();
		abandon.LocalID = localID;

		simulator.SendPacket(abandon);
	}

	/**
	 * Requests the UUID of the parcel in a remote region at a specified
	 * location
	 * 
	 * @param location
	 *            Location of the parcel in the remote region
	 * @param regionHandle
	 *            Remote region handle
	 * @param regionID
	 *            Remote region UUID
	 * @return If successful UUID of the remote parcel, UUID.Zero otherwise
	 */
	public final UUID RequestRemoteParcelID(Vector3 location, long regionHandle, UUID regionID)
	{
		URI url = _Client.Network.getCapabilityURI("RemoteParcelRequest");
		if (url != null)
		{
			RemoteParcelRequestRequest req = _Client.Messages.new RemoteParcelRequestRequest();
			req.Location = location;
			req.RegionHandle = regionHandle;
			req.RegionID = regionID;

			try
			{
				OSD result = new CapsClient().GetResponse(url, req.Serialize(), OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
				RemoteParcelRequestMessage response = (RemoteParcelRequestMessage) _Client.Messages.DecodeEvent(
						CapsEventType.RemoteParcelRequest, (OSDMap) result);
				return ((RemoteParcelRequestReply) response.Request).ParcelID;
			}
			catch (Throwable t)
			{
				Logger.Log("Failed to fetch remote parcel ID", LogLevel.Debug, _Client);
			}
		}

		return UUID.Zero;

	}

	public interface LandResourcesInfoCallback
	{
		public void callback(boolean success, LandResourcesInfo info);
	}

	/**
	 * Retrieves information on resources used by the parcel
	 * 
	 * @param parcelID
	 *            UUID of the parcel
	 * @param getDetails
	 *            Should per object resource usage be requested
	 * @param callback
	 *            Callback invoked when the request failed or is complete
	 */
	public final void GetParcelResouces(UUID parcelID, boolean getDetails, LandResourcesInfoCallback callback)
	{
		try
		{
			URI url = _Client.Network.getCapabilityURI("LandResources");
			CapsClient request = new CapsClient();
			LandResourcesRequest req = _Client.Messages.new LandResourcesRequest();
			req.ParcelID = parcelID;
			request.setResultCallback(new LandResourcesMessageHandler(getDetails, callback));
			request.executeHttpPost(url, req, _Client.Settings.CAPS_TIMEOUT);

		}
		catch (Exception ex)
		{
			Logger.Log("Failed fetching land resources:", LogLevel.Error, _Client, ex);
			callback.callback(false, null);
		}
	}

	// #endregion Public Methods

	private class LandResourcesMessageHandler implements FutureCallback<OSD>
	{
		private final LandResourcesInfoCallback callback;
		private final boolean getDetails;

		public LandResourcesMessageHandler(boolean getDetails, LandResourcesInfoCallback callback)
		{
			this.getDetails = getDetails;
			this.callback = callback;
		}

		@Override
		public void completed(OSD result)
		{
			try
			{
				if (result == null)
				{
					callback.callback(false, null);
				}
				LandResourcesMessage response = _Client.Messages.new LandResourcesMessage();
				response.Deserialize((OSDMap) result);
				OSD osd = new CapsClient().GetResponse(response.ScriptResourceSummary, Helpers.EmptyString, _Client.Settings.CAPS_TIMEOUT);

				LandResourcesInfo info = _Client.Messages.new LandResourcesInfo();
				info.Deserialize((OSDMap) osd);
				if (response.ScriptResourceDetails != null && getDetails)
				{
					osd = new CapsClient().GetResponse(response.ScriptResourceDetails, Helpers.EmptyString, _Client.Settings.CAPS_TIMEOUT);
					info.Deserialize((OSDMap) osd);
				}
				callback.callback(true, info);
			}
			catch (Exception ex)
			{
				failed(ex);
			}
		}

		@Override
		public void cancelled()
		{
			Logger.Log("Fetching land resources was cancelled", LogLevel.Error, _Client);
			callback.callback(false, null);
		}

		@Override
		public void failed(Exception ex)
		{
			Logger.Log("Failed fetching land resources", LogLevel.Error, _Client, ex);
			callback.callback(false, null);
		}

	}

	private final void HandleParcelDwellReply(Packet packet, Simulator simulator)
	{
		ParcelDwellReplyPacket dwell = (ParcelDwellReplyPacket) packet;

		synchronized (simulator.Parcels)
		{
			if (dwell.Data.Dwell != 0.0F && simulator.Parcels.containsKey(dwell.Data.LocalID))
			{
				simulator.Parcels.get(dwell.Data.LocalID).Dwell = dwell.Data.Dwell;
			}
		}
		OnParcelDwellReply.dispatch(new ParcelDwellReplyCallbackArgs(dwell.Data.ParcelID, dwell.Data.LocalID,
				dwell.Data.Dwell));
	}

	private final void HandleParcelPropertiesReply(IMessage message, Simulator simulator) throws Exception
	{
		if (OnParcelProperties.count() > 0 || _Client.Settings.PARCEL_TRACKING == true)
		{
			ParcelPropertiesMessage msg = (ParcelPropertiesMessage) message;

			Parcel parcel = new Parcel(msg.LocalID);

			parcel.AABBMax = msg.AABBMax;
			parcel.AABBMin = msg.AABBMin;
			parcel.Area = msg.Area;
			parcel.AuctionID = msg.AuctionID;
			parcel.AuthBuyerID = msg.AuthBuyerID;
			parcel.Bitmap = msg.Bitmap;
			parcel.Category = msg.Category;
			parcel.ClaimDate = msg.ClaimDate;
			parcel.ClaimPrice = msg.ClaimPrice;
			parcel.Desc = msg.Desc;
			parcel.Flags = msg.ParcelFlags;
			parcel.GroupID = msg.GroupID;
			parcel.GroupPrims = msg.GroupPrims;
			parcel.IsGroupOwned = msg.IsGroupOwned;
			parcel.Landing = msg.LandingType;
			parcel.MaxPrims = msg.MaxPrims;
			parcel.Media.MediaAutoScale = msg.MediaAutoScale;
			parcel.Media.MediaID = msg.MediaID;
			parcel.Media.MediaURL = msg.MediaURL;
			parcel.MusicURL = msg.MusicURL;
			parcel.Name = msg.Name;
			parcel.OtherCleanTime = msg.OtherCleanTime;
			parcel.OtherCount = msg.OtherCount;
			parcel.OtherPrims = msg.OtherPrims;
			parcel.OwnerID = msg.OwnerID;
			parcel.OwnerPrims = msg.OwnerPrims;
			parcel.ParcelPrimBonus = msg.ParcelPrimBonus;
			parcel.PassHours = msg.PassHours;
			parcel.PassPrice = msg.PassPrice;
			parcel.PublicCount = msg.PublicCount;
			parcel.RegionDenyAgeUnverified = msg.RegionDenyAgeUnverified;
			parcel.RegionDenyAnonymous = msg.RegionDenyAnonymous;
			parcel.RegionPushOverride = msg.RegionPushOverride;
			parcel.RentPrice = msg.RentPrice;
			ParcelResult result = msg.RequestResult;
			parcel.SalePrice = msg.SalePrice;
			int selectedPrims = msg.SelectedPrims;
			parcel.SelfCount = msg.SelfCount;
			int sequenceID = msg.SequenceID;
			parcel.SimWideMaxPrims = msg.SimWideMaxPrims;
			parcel.SimWideTotalPrims = msg.SimWideTotalPrims;
			boolean snapSelection = msg.SnapSelection;
			parcel.SnapshotID = msg.SnapshotID;
			parcel.Status = msg.Status;
			parcel.TotalPrims = msg.TotalPrims;
			parcel.UserLocation = msg.UserLocation;
			parcel.UserLookAt = msg.UserLookAt;
			parcel.Media.MediaDesc = msg.MediaDesc;
			parcel.Media.MediaHeight = msg.MediaHeight;
			parcel.Media.MediaWidth = msg.MediaWidth;
			parcel.Media.MediaLoop = msg.MediaLoop;
			parcel.Media.MediaType = msg.MediaType;
			parcel.ObscureMedia = msg.ObscureMedia;
			parcel.ObscureMusic = msg.ObscureMusic;

			if (_Client.Settings.PARCEL_TRACKING)
			{
				synchronized (simulator.Parcels)
				{
					simulator.Parcels.put(parcel.LocalID, parcel);
				}

				boolean set = false;
				int y, x, index, bit;
				for (y = 0; y < 64; y++)
				{
					for (x = 0; x < 64; x++)
					{
						index = (y * 64) + x;
						bit = index % 8;
						index >>= 3;

						if ((parcel.Bitmap[index] & (1 << bit)) != 0)
						{
							simulator.setParcelMap(y, x, parcel.LocalID);
							set = true;
						}
					}
				}

				if (!set)
				{
					Logger.Log("Received a parcel with a bitmap that did not map to any locations", LogLevel.Warning);
				}
			}

			if (((Integer) sequenceID).equals(Integer.MAX_VALUE) && WaitForSimParcel != null)
			{
				WaitForSimParcel.set(true);
			}

			// auto request acl, will be stored in parcel tracking dictionary if
			// enabled
			if (_Client.Settings.ALWAYS_REQUEST_PARCEL_ACL)
			{
				RequestParcelAccessList(simulator, parcel.LocalID, AccessList.Both, sequenceID);
			}

			// auto request dwell, will be stored in parcel tracking dictionary
			// if enables
			if (_Client.Settings.ALWAYS_REQUEST_PARCEL_DWELL)
			{
				RequestDwell(simulator, parcel.LocalID);
			}

			// Fire the callback for parcel properties being received
			if (OnParcelProperties != null)
			{
				OnParcelProperties.dispatch(new ParcelPropertiesCallbackArgs(simulator, parcel, result, selectedPrims,
						sequenceID, snapSelection));
			}

			// Check if all of the simulator parcels have been retrieved, if so
			// fire another callback
			if (simulator.IsParcelMapFull() && OnSimParcelsDownloaded.count() > 0)
			{
				OnSimParcelsDownloaded.dispatch(new SimParcelsDownloadedCallbackArgs(simulator, simulator.Parcels,
						simulator.getParcelMap()));
			}
		}
	}

	private final void HandleParcelInfoReply(Packet packet, Simulator simulator) throws Exception
	{
		ParcelInfoReplyPacket info = (ParcelInfoReplyPacket) packet;

		ParcelInfo parcelInfo = new ParcelInfo();

		parcelInfo.ActualArea = info.Data.ActualArea;
		parcelInfo.AuctionID = info.Data.AuctionID;
		parcelInfo.BillableArea = info.Data.BillableArea;
		parcelInfo.Description = Helpers.BytesToString(info.Data.getDesc());
		parcelInfo.Dwell = info.Data.Dwell;
		parcelInfo.GlobalX = info.Data.GlobalX;
		parcelInfo.GlobalY = info.Data.GlobalY;
		parcelInfo.GlobalZ = info.Data.GlobalZ;
		parcelInfo.ID = info.Data.ParcelID;
		parcelInfo.Mature = ((info.Data.Flags & 1) != 0) ? true : false;
		parcelInfo.Name = Helpers.BytesToString(info.Data.getName());
		parcelInfo.OwnerID = info.Data.OwnerID;
		parcelInfo.SalePrice = info.Data.SalePrice;
		parcelInfo.SimName = Helpers.BytesToString(info.Data.getSimName());
		parcelInfo.SnapshotID = info.Data.SnapshotID;

		OnParcelInfoReply.dispatch(new ParcelInfoReplyCallbackArgs(parcelInfo));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * Raises the <see cref="ParcelAccessListReply"/> event
	 */
	private final void HandleParcelAccessListReply(Packet packet, Simulator simulator)
	{
		if (OnParcelAccessListReply.count() > 0 || _Client.Settings.ALWAYS_REQUEST_PARCEL_ACL)
		{
			ParcelAccessListReplyPacket reply = (ParcelAccessListReplyPacket) packet;

			ArrayList<ParcelAccessEntry> accessList = new ArrayList<ParcelAccessEntry>(reply.List.length);

			for (int i = 0; i < reply.List.length; i++)
			{
				ParcelAccessEntry pae = new ParcelAccessEntry();
				pae.AgentID = reply.List[i].ID;
				pae.Time = Helpers.UnixTimeToDateTime(reply.List[i].Time);
				pae.Flags = AccessList.setValue(reply.List[i].Flags);

				accessList.add(pae);
			}

			synchronized (simulator.Parcels)
			{
				if (simulator.Parcels.containsKey(reply.Data.LocalID))
				{
					Parcel parcel = simulator.Parcels.get(reply.Data.LocalID);
					if (reply.Data.Flags == AccessList.Ban)
					{
						parcel.AccessBlackList = accessList;
					}
					else
					{
						parcel.AccessWhiteList = accessList;
					}

					simulator.Parcels.put(reply.Data.LocalID, parcel);
				}
			}
			OnParcelAccessListReply.dispatch(new ParcelAccessListReplyCallbackArgs(simulator, reply.Data.SequenceID,
					reply.Data.LocalID, reply.Data.Flags, accessList));
		}
	}

	private final void HandleParcelObjectOwnersReply(IMessage message, Simulator simulator)
	{
		if (OnParcelObjectOwnersReply.count() > 0)
		{
			ArrayList<ParcelPrimOwners> primOwners = new ArrayList<ParcelPrimOwners>();

			ParcelObjectOwnersReplyMessage msg = (ParcelObjectOwnersReplyMessage) message;

			for (int i = 0; i < msg.PrimOwnersBlock.length; i++)
			{
				ParcelPrimOwners primOwner = new ParcelPrimOwners();
				primOwner.OwnerID = msg.PrimOwnersBlock[i].OwnerID;
				primOwner.Count = msg.PrimOwnersBlock[i].Count;
				primOwner.IsGroupOwned = msg.PrimOwnersBlock[i].IsGroupOwned;
				primOwner.OnlineStatus = msg.PrimOwnersBlock[i].OnlineStatus;
				primOwner.NewestPrim = msg.PrimOwnersBlock[i].TimeStamp;

				primOwners.add(primOwner);
			}
			OnParcelObjectOwnersReply.dispatch(new ParcelObjectOwnersReplyCallbackArgs(simulator, primOwners));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * Raises the <see cref="ForceSelectObjectsReply"/> event
	 */
	private final void HandleSelectParcelObjectsReply(Packet packet, Simulator simulator)
	{
		if (OnForceSelectObjectsReply.count() > 0)
		{
			ForceObjectSelectPacket reply = (ForceObjectSelectPacket) packet;
			int[] objectIDs = new int[reply.LocalID.length];

			for (int i = 0; i < reply.LocalID.length; i++)
			{
				objectIDs[i] = reply.LocalID[i];
			}
			OnForceSelectObjectsReply.dispatch(new ForceSelectObjectsReplyCallbackArgs(simulator, objectIDs, reply.ResetList));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * Raises the <see cref="ParcelMediaUpdateReply"/> event
	 * 
	 * @throws Exception
	 */
	private final void HandleParcelMediaUpdate(Packet packet, Simulator simulator) throws Exception
	{
		if (OnParcelMediaUpdateReply != null)
		{
			ParcelMediaUpdatePacket reply = (ParcelMediaUpdatePacket) packet;
			ParcelMedia media = new ParcelMedia();

			media.MediaAutoScale = (reply.DataBlock.MediaAutoScale == (byte) 0x1) ? true : false;
			media.MediaID = reply.DataBlock.MediaID;
			media.MediaDesc = Helpers.BytesToString(reply.DataBlockExtended.getMediaDesc());
			media.MediaHeight = reply.DataBlockExtended.MediaHeight;
			media.MediaLoop = ((reply.DataBlockExtended.MediaLoop & 1) != 0) ? true : false;
			media.MediaType = Helpers.BytesToString(reply.DataBlockExtended.getMediaType());
			media.MediaWidth = reply.DataBlockExtended.MediaWidth;
			media.MediaURL = Helpers.BytesToString(reply.DataBlock.getMediaURL());

			OnParcelMediaUpdateReply.dispatch(new ParcelMediaUpdateReplyCallbackArgs(simulator, media));
		}
	}

	private final void HandleParcelOverlay(Packet packet, Simulator simulator)
	{
		final int OVERLAY_COUNT = 4;
		ParcelOverlayPacket overlay = (ParcelOverlayPacket) packet;

		if (overlay.ParcelData.SequenceID >= 0 && overlay.ParcelData.SequenceID < OVERLAY_COUNT)
		{
			int length = overlay.ParcelData.getData().length;

			System.arraycopy(overlay.ParcelData.getData(), 0, simulator.ParcelOverlay, overlay.ParcelData.SequenceID
					* length, length);
			simulator.ParcelOverlaysReceived++;

			if (simulator.ParcelOverlaysReceived >= OVERLAY_COUNT)
			{
				// TODO: ParcelOverlaysReceived should become internal, and
				// reset to zero every time it hits four. Also need a callback
				// here
				Logger.Log("Finished building the " + simulator.Name + " parcel overlay", LogLevel.Info);
			}
		}
		else
		{
			Logger.Log("Parcel overlay with sequence ID of " + overlay.ParcelData.SequenceID + " received from "
					+ simulator.toString(), LogLevel.Warning, _Client);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * Raises the <see cref="ParcelMediaCommand"/> event
	 */
	private final void HandleParcelMediaCommandMessagePacket(Packet packet, Simulator simulator)
	{
		if (OnParcelMediaCommand != null)
		{
			ParcelMediaCommandMessagePacket pmc = (ParcelMediaCommandMessagePacket) packet;
			ParcelMediaCommandMessagePacket.CommandBlockBlock block = pmc.CommandBlock;

			OnParcelMediaCommand.dispatch(new ParcelMediaCommandCallbackArgs(simulator, pmc.getHeader().getSequence(),
					block.Flags, ParcelMediaCommand.setValue(block.Command), block.Time));
		}
	}
}
