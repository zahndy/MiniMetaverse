/**
 * Copyright (c) 2007-2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.InstantMessageDialog;
import libomv.AgentManager.InstantMessageOnline;
import libomv.GroupManager.GroupAccountTransactions.TransactionEntry;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.LLSD.LLSDXml;
import libomv.assets.AssetItem.AssetType;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.AgentDropGroupMessage;
import libomv.capabilities.CapsMessage.AgentGroupDataUpdateMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.IMessage;
import libomv.packets.ActivateGroupPacket;
import libomv.packets.AgentDataUpdateRequestPacket;
import libomv.packets.AgentDropGroupPacket;
import libomv.packets.AgentGroupDataUpdatePacket;
import libomv.packets.CreateGroupReplyPacket;
import libomv.packets.CreateGroupRequestPacket;
import libomv.packets.EjectGroupMemberReplyPacket;
import libomv.packets.EjectGroupMemberRequestPacket;
import libomv.packets.GroupAccountDetailsReplyPacket;
import libomv.packets.GroupAccountDetailsRequestPacket;
import libomv.packets.GroupAccountSummaryReplyPacket;
import libomv.packets.GroupAccountSummaryRequestPacket;
import libomv.packets.GroupAccountTransactionsReplyPacket;
import libomv.packets.GroupActiveProposalItemReplyPacket;
import libomv.packets.GroupMembersReplyPacket;
import libomv.packets.GroupMembersRequestPacket;
import libomv.packets.GroupNoticeRequestPacket;
import libomv.packets.GroupNoticesListReplyPacket;
import libomv.packets.GroupNoticesListRequestPacket;
import libomv.packets.GroupProfileReplyPacket;
import libomv.packets.GroupProfileRequestPacket;
import libomv.packets.GroupRoleChangesPacket;
import libomv.packets.GroupRoleDataReplyPacket;
import libomv.packets.GroupRoleDataRequestPacket;
import libomv.packets.GroupRoleMembersReplyPacket;
import libomv.packets.GroupRoleMembersRequestPacket;
import libomv.packets.GroupRoleUpdatePacket;
import libomv.packets.GroupTitleUpdatePacket;
import libomv.packets.GroupTitlesReplyPacket;
import libomv.packets.GroupTitlesRequestPacket;
import libomv.packets.GroupVoteHistoryItemReplyPacket;
import libomv.packets.InviteGroupRequestPacket;
import libomv.packets.JoinGroupReplyPacket;
import libomv.packets.JoinGroupRequestPacket;
import libomv.packets.LeaveGroupReplyPacket;
import libomv.packets.LeaveGroupRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.SetGroupAcceptNoticesPacket;
import libomv.packets.SetGroupContributionPacket;
import libomv.packets.StartGroupProposalPacket;
import libomv.packets.UUIDGroupNameReplyPacket;
import libomv.packets.UUIDGroupNameRequestPacket;
import libomv.packets.UpdateGroupInfoPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.HashMapInt;
import libomv.utils.Helpers;

// Handles all network traffic related to reading and writing group
// information
public class GroupManager implements PacketCallback, CapsCallback
{
	// /#region Structs

	// Avatar group management
	public final class GroupMember
	{
		// Key of Group Member
		public UUID ID;
		// Total land contribution
		public int Contribution;
		// Online status information
		public String OnlineStatus;
		// Abilities that the Group Member has
		public long Powers;
		// Current group title
		public String Title;
		// Is a group owner
		public boolean IsOwner;

		public GroupMember(UUID agentID)
		{
			ID = agentID;
		}
	}

	// Role manager for a group
	public final class GroupRole
	{
		// Key of the group
		public UUID GroupID;
		// Key of Role
		public UUID ID;
		// Name of Role
		public String Name;
		// Group Title associated with Role
		public String Title;
		// Description of Role
		public String Description;
		// Abilities Associated with Role
		public long Powers;

		// Returns the role's title
		@Override
		public String toString()
		{
			return Name;
		}

		public GroupRole(UUID roleID)
		{
			ID = roleID;
		}
	}

	// Class to represent Group Title
	public final class GroupTitle
	{
		// Key of the group
		public UUID GroupID;
		// ID of the role title belongs to
		public UUID RoleID;
		// Group Title
		public String Title;
		// Whether title is Active
		public boolean Selected;

		// Returns group title
		@Override
		public String toString()
		{
			return Title;
		}

		public GroupTitle()
		{
		}
	}

	// Represents a group on the grid
	public final class Group
	{
		// Key of Group
		private UUID ID;
		// Key of Group Insignia
		public UUID InsigniaID;
		// Key of Group Founder
		public UUID FounderID;
		// Key of Group Role for Owners
		public UUID OwnerRole;
		// Name of Group
		private String Name;
		// Text of Group Charter
		public String Charter;
		// Title of "everyone" role
		public String MemberTitle;
		// Is the group open for enrolement to everyone
		public boolean OpenEnrollment;
		// Will group show up in search
		public boolean ShowInList;
		// GroupPowers flags
		public long Powers;
		//
		public boolean AcceptNotices;
		//
		public boolean AllowPublish;
		// Is the group Mature
		public boolean MaturePublish;
		// Cost of group membership
		public int MembershipFee;
		//
		public int Money;
		//
		public int Contribution;
		// The total number of current members this group has
		public int GroupMembershipCount;
		// The number of roles this group has configured
		public int GroupRolesCount;
		// Show this group in agent's profile
		public boolean ListInProfile;

		public UUID getID()
		{
			return ID;
		}
		
		public String getName()
		{
			return Name;
		}
		
		@Override
		public boolean equals(Object o)
		{
			return (o != null && o instanceof Group) ? equals((Group)o) : false;
		}

		public boolean equals(Group o)
		{
			return o != null ? ID.equals(o.ID) : false;
		}
		
		@Override
		public int hashCode()
		{
			return ID.hashCode();
		}
		
		// Returns the name of the group
		@Override
		public String toString()
		{
			return Name;
		}


		public Group(UUID id)
		{
			ID = id;
			InsigniaID = new UUID();
		}
	}

	// A group Vote
	public final class Vote
	{
		// Key of Avatar who created Vote
		public UUID Candidate;
		// Text of the Vote proposal
		public String VoteString;
		// Total number of votes
		public int NumVotes;
	}

	// A group proposal
	public final class GroupProposal
	{
		// The minimum number of members that must vote before proposal passes
		// or fails
		public int Quorum;
		// The required ration of yes/no votes required for vote to pass
		// The three options are Simple Majority, 2/3 Majority, and Unanimous
		// TODO: this should be an enum
		public float Majority;
		// The duration in days votes are accepted
		public int Duration;
		// The Text of the proposal
		String ProposalText;
	}

	// A group proposal
	public final class GroupProposalItem
	{
		public UUID VoteID;
		public UUID VoteInitiator;
		public String TerseDateID;
		public boolean AlreadyVoted;
		public String VoteCast;
		// The minimum number of members that must vote before proposal passes
		// or failes
		public int Quorum;
		// The required ration of yes/no votes required for vote to pass
		// The three options are Simple Majority, 2/3 Majority, and Unanimous
		// TODO: this should be an enum
		public float Majority;
		public Date StartDateTime;
		public Date EndDateTime;
		// The Text of the proposal
		public String ProposalText;
	}

	public final class GroupAccountSummary
	{
		//
		public int IntervalDays;
		//
		public int CurrentInterval;
		//
		public String StartDate;
		//
		public int Balance;
		//
		public int TotalCredits;
		//
		public int TotalDebits;
		//
		public int ObjectTaxCurrent;
		//
		public int LightTaxCurrent;
		//
		public int LandTaxCurrent;
		//
		public int GroupTaxCurrent;
		//
		public int ParcelDirFeeCurrent;
		//
		public int ObjectTaxEstimate;
		//
		public int LightTaxEstimate;
		//
		public int LandTaxEstimate;
		//
		public int GroupTaxEstimate;
		//
		public int ParcelDirFeeEstimate;
		//
		public int NonExemptMembers;
		//
		public String LastTaxDate;
		//
		public String TaxDate;
	}

	public class GroupAccountDetails
	{
		public int IntervalDays;

		public int CurrentInterval;

		public String StartDate;

		// <summary>A list of description/amount pairs making up the account
		// history</summary>
		// public List<KeyValuePair<string, int>> HistoryItems;
		// Still needs to implement the GroupAccount Details Handler and define
		// the data type
		public HashMapInt HistoryItems;
	}

	// Struct representing a group notice
	public final class GroupNotice
	{
		//
		public String Subject;
		//
		public String Message;
		//
		public UUID AttachmentID;
		//
		public UUID OwnerID;

		public byte[] SerializeAttachment() throws IOException
		{
			if (OwnerID.equals(UUID.Zero) || AttachmentID.equals(UUID.Zero))
			{
				return Helpers.EmptyBytes;
			}

			OSDMap att = new OSDMap();
			att.put("item_id", OSD.FromUUID(AttachmentID));
			att.put("owner_id", OSD.FromUUID(OwnerID));

			return LLSDXml.serializeToBytes(att, Helpers.UTF8_ENCODING);
		}
	}

	// Struct representing a group notice list entry
	public final class GroupNoticesListEntry
	{
		// Notice ID
		public UUID NoticeID;
		// Creation timestamp of notice
		// TODO: ORIGINAL LINE: public uint Timestamp;
		public int Timestamp;
		// Agent name who created notice
		public String FromName;
		// Notice subject
		public String Subject;
		// Is there an attachment?
		public boolean HasAttachment;
		// Attachment Type
		public AssetType AssetType;

	}

	public class GroupAccountTransactions
	{
		public class TransactionEntry
		{
			public String Time;
			public String Item;
			public String User;
			public int Type;
			public int Amount;
		}

		public int IntervalDays;

		public int CurrentInterval;

		public String StartDate;

		public TransactionEntry[] Transactions;
	}

	// Struct representing a member of a group chat session and their settings
	static public final class ChatSessionMember
	{
		// The <see cref="UUID"/> of the Avatar
		public UUID AvatarKey;
		// True if user has voice chat enabled
		public boolean CanVoiceChat;
		// True of Avatar has moderator abilities
		public boolean IsModerator;
		// True if a moderator has muted this avatars chat
		public boolean MuteText;
		// True if a moderator has muted this avatars voice
		public boolean MuteVoice;
	}

	// #endregion Structs

	// #region Enums

	// Role update flags
	public enum GroupRoleUpdate
	{
		//
		NoUpdate,
		//
		UpdateData,
		//
		UpdatePowers,
		//
		UpdateAll,
		//
		Create,
		//
		Delete;

		public GroupRoleUpdate setValue(byte value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// [Flags]
	public static class GroupPowers
	{
		//
		public static final long None = 0;

		// Membership
		// Can send invitations to groups default role
		public static final long Invite = 1L << 1;
		// Can eject members from group
		public static final long Eject = 1L << 2;
		// Can toggle 'Open Enrollment' and change 'Signup fee'
		public static final long ChangeOptions = 1L << 3;
		// Member is visible in the public member list
		public static final long MemberVisible = 1L << 47;

		// Roles
		// Can create new roles
		public static final long CreateRole = 1L << 4;
		// Can delete existing roles
		public static final long DeleteRole = 1L << 5;
		// Can change Role names, titles and descriptions
		public static final long RoleProperties = 1L << 6;
		// Can assign other members to assigners role
		public static final long AssignMemberLimited = 1L << 7;
		// Can assign other members to any role
		public static final long AssignMember = 1L << 8;
		// Can remove members from roles
		public static final long RemoveMember = 1L << 9;
		// Can assign and remove abilities in roles
		public static final long ChangeActions = 1L << 10;

		// Identity
		// Can change group Charter, Insignia, 'Publish on the web' and which
		// members are publicly visible in group member listings
		public static final long ChangeIdentity = 1L << 11;

		// Parcel management
		// Can buy land or deed land to group
		public static final long LandDeed = 1L << 12;
		// Can abandon group owned land to Governor Linden on mainland, or
		// Estate owner for private estates
		public static final long LandRelease = 1L << 13;
		// Can set land for-sale information on group owned parcels
		public static final long LandSetSale = 1L << 14;
		// Can subdivide and join parcels
		public static final long LandDivideJoin = 1L << 15;

		// Chat
		// Can join group chat sessions
		public static final long JoinChat = 1L << 16;
		// Can use voice chat in Group Chat sessions
		public static final long AllowVoiceChat = 1L << 27;
		// Can moderate group chat sessions
		public static final long ModerateChat = 1L << 37;

		// Parcel identity
		// Can toggle "Show in Find Places" and set search category
		public static final long FindPlaces = 1L << 17;
		// Can change parcel name, description, and 'Publish on web' settings
		public static final long LandChangeIdentity = 1L << 18;
		// Can set the landing point and teleport routing on group land
		public static final long SetLandingPoint = 1L << 19;

		// Parcel settings
		// Can change music and media settings
		public static final long ChangeMedia = 1L << 20;
		// Can toggle 'Edit Terrain' option in Land settings
		public static final long LandEdit = 1L << 21;
		// Can toggle various About Land > Options settings
		public static final long LandOptions = 1L << 22;

		// Parcel powers
		// Can always terraform land, even if parcel settings have it turned off
		public static final long AllowEditLand = 1L << 23;
		// Can always fly while over group owned land
		public static final long AllowFly = 1L << 24;
		// Can always rez objects on group owned land
		public static final long AllowRez = 1L << 25;
		// Can always create landmarks for group owned parcels
		public static final long AllowLandmark = 1L << 26;
		// Can set home location on any group owned parcel
		public static final long AllowSetHome = 1L << 28;

		// Parcel access
		// Can modify public access settings for group owned parcels
		public static final long LandManageAllowed = 1L << 29;
		// Can manager parcel ban lists on group owned land
		public static final long LandManageBanned = 1L << 30;
		// Can manage pass list sales information
		public static final long LandManagePasses = 1L << 31;
		// Can eject and freeze other avatars on group owned land
		public static final long LandEjectAndFreeze = 1L << 32;

		// Parcel content
		// Can return objects set to group
		public static final long ReturnGroupSet = 1L << 33;
		// Can return non-group owned/set objects
		public static final long ReturnNonGroup = 1L << 34;
		// Can return group owned objects
		public static final long ReturnGroupOwned = 1L << 48;

		// Can landscape using Linden plants
		public static final long LandGardening = 1L << 35;

		// Objects
		// Can deed objects to group
		public static final long DeedObject = 1L << 36;
		// Can move group owned objects
		public static final long ObjectManipulate = 1L << 38;
		// Can set group owned objects for-sale
		public static final long ObjectSetForSale = 1L << 39;

		// Pay group liabilities and receive group dividends
		public static final long Accountable = 1L << 40;

		// Notices and proposals
		// Can send group notices
		public static final long SendNotices = 1L << 42;
		// Can receive group notices
		public static final long ReceiveNotices = 1L << 43;
		// Can create group proposals
		public static final long StartProposal = 1L << 44;
		// Can vote on group proposals
		public static final long VoteOnProposal = 1L << 45;

		public static long setValue(long value)
		{
			return value & _mask;
		}

		public static long getValue(long value)
		{
			return value & _mask;
		}

		private static final long _mask = 0x3FFFFFFFFFFFL;
	}

	// #endregion Enums

	private GridClient Client;

	// Currently-active group members requests
	private ArrayList<UUID> GroupMembersRequests;
	// Currently-active group roles requests
	private ArrayList<UUID> GroupRolesRequests;
	// Currently-active group role-member requests
	private ArrayList<UUID> GroupRolesMembersRequests;
	// Dictionary keeping group members while request is in progress
	private HashMap<UUID, HashMap<UUID, GroupMember>> TempGroupMembers;
	// Dictionary keeping member/role mapping while request is in progress
	private HashMap<UUID, ArrayList<Entry<UUID, UUID>>> TempGroupRolesMembers;
	// Dictionary keeping GroupRole information while request is in progress
	private HashMap<UUID, HashMap<UUID, GroupRole>> TempGroupRoles;
	// Caches group name lookups
	public HashMap<UUID, String> GroupName2KeyCache;

	public CallbackHandler<CurrentGroupsCallbackArgs> OnCurrentGroups = new CallbackHandler<CurrentGroupsCallbackArgs>();

	public CallbackHandler<GroupNamesCallbackArgs> OnGroupNamesReply = new CallbackHandler<GroupNamesCallbackArgs>();

	public CallbackHandler<GroupProfileCallbackArgs> OnGroupProfile = new CallbackHandler<GroupProfileCallbackArgs>();

	public CallbackHandler<GroupMembersReplyCallbackArgs> OnGroupMembersReply = new CallbackHandler<GroupMembersReplyCallbackArgs>();

	public CallbackHandler<GroupRolesDataReplyCallbackArgs> OnGroupRoleDataReply = new CallbackHandler<GroupRolesDataReplyCallbackArgs>();

	public CallbackHandler<GroupRolesMembersReplyCallbackArgs> OnGroupRoleMembers = new CallbackHandler<GroupRolesMembersReplyCallbackArgs>();

	public CallbackHandler<GroupTitlesReplyCallbackArgs> OnGroupTitles = new CallbackHandler<GroupTitlesReplyCallbackArgs>();

	public CallbackHandler<GroupAccountSummaryReplyCallbackArgs> OnGroupAccountSummaryReply = new CallbackHandler<GroupAccountSummaryReplyCallbackArgs>();

	public CallbackHandler<GroupCreatedReplyCallbackArgs> OnGroupCreatedReply = new CallbackHandler<GroupCreatedReplyCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupJoinedReply = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupLeaveReply = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupDroppedCallbackArgs> OnGroupDropped = new CallbackHandler<GroupDroppedCallbackArgs>();

	public CallbackHandler<GroupOperationCallbackArgs> OnGroupMemberEjected = new CallbackHandler<GroupOperationCallbackArgs>();

	public CallbackHandler<GroupNoticesListReplyCallbackArgs> OnGroupNoticesListReply = new CallbackHandler<GroupNoticesListReplyCallbackArgs>();

	public CallbackHandler<GroupInvitationCallbackArgs> OnGroupInvitation = new CallbackHandler<GroupInvitationCallbackArgs>();

	public HashMap<UUID, Callback<GroupAccountDetails>> OnGroupAccountDetailsCallbacks = new HashMap<UUID, Callback<GroupAccountDetails>>();

	public HashMap<UUID, Callback<GroupAccountTransactions>> OnGroupAccountTransactionsCallbacks = new HashMap<UUID, Callback<GroupAccountTransactions>>();

	private class InstantMessageCallback implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(InstantMessageCallbackArgs e)
		{
			if (OnGroupInvitation.count() > 0 && e.getIM().Dialog == InstantMessageDialog.GroupInvitation)
			{
				byte[] bucket = e.getIM().BinaryBucket;
				int fee = -1;
				if (bucket.length == 20)
				{
					fee = Helpers.BytesToInt32B(bucket);
				}
				
				GroupInvitationCallbackArgs args = new GroupInvitationCallbackArgs(e.getSimulator(),
						e.getIM().FromAgentID, e.getIM().FromAgentName, e.getIM().Message, fee);
				OnGroupInvitation.dispatch(args);

				try
				{
					if (args.getAccept())
					{
						Client.Self.InstantMessage(Client.Self.getName(), e.getIM().FromAgentID, "message", e.getIM().IMSessionID,
								InstantMessageDialog.GroupInvitationAccept, InstantMessageOnline.Online);
					}
					else
					{
						Client.Self.InstantMessage(Client.Self.getName(), e.getIM().FromAgentID, "message", e.getIM().IMSessionID,
								InstantMessageDialog.GroupInvitationDecline, InstantMessageOnline.Online);
					}
				}
				catch (Exception ex) { }
			}
			return false;
		}
	}

	public GroupManager(GridClient client)
	{
		Client = client;

		TempGroupMembers = new HashMap<UUID, HashMap<UUID, GroupMember>>();
		GroupMembersRequests = new ArrayList<UUID>();
		TempGroupRoles = new HashMap<UUID, HashMap<UUID, GroupRole>>();
		GroupRolesRequests = new ArrayList<UUID>();
		TempGroupRolesMembers = new HashMap<UUID, ArrayList<Entry<UUID, UUID>>>();
		GroupRolesMembersRequests = new ArrayList<UUID>();
		GroupName2KeyCache = new HashMap<UUID, String>();

		Client.Self.OnInstantMessage.add(new InstantMessageCallback());

		Client.Network.RegisterCallback(CapsEventType.AgentGroupDataUpdate, this);
		// deprecated in simulator v1.27
		Client.Network.RegisterCallback(PacketType.AgentGroupDataUpdate, this);

		Client.Network.RegisterCallback(CapsEventType.AgentDropGroup, this);
		// deprecated in simulator v1.27
		Client.Network.RegisterCallback(PacketType.AgentDropGroup, this);

		Client.Network.RegisterCallback(PacketType.GroupTitlesReply, this);
		Client.Network.RegisterCallback(PacketType.GroupProfileReply, this);
		Client.Network.RegisterCallback(PacketType.GroupMembersReply, this);
		Client.Network.RegisterCallback(PacketType.GroupRoleDataReply, this);
		Client.Network.RegisterCallback(PacketType.GroupRoleMembersReply, this);
		Client.Network.RegisterCallback(PacketType.GroupActiveProposalItemReply, this);
		Client.Network.RegisterCallback(PacketType.GroupVoteHistoryItemReply, this);
		Client.Network.RegisterCallback(PacketType.GroupAccountSummaryReply, this);
		Client.Network.RegisterCallback(PacketType.GroupAccountDetailsReply, this);
		Client.Network.RegisterCallback(PacketType.GroupAccountTransactionsReply, this);
		Client.Network.RegisterCallback(PacketType.CreateGroupReply, this);
		Client.Network.RegisterCallback(PacketType.JoinGroupReply, this);
		Client.Network.RegisterCallback(PacketType.LeaveGroupReply, this);
		Client.Network.RegisterCallback(PacketType.UUIDGroupNameReply, this);
		Client.Network.RegisterCallback(PacketType.EjectGroupMemberReply, this);
		Client.Network.RegisterCallback(PacketType.GroupNoticesListReply, this);
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator) throws Exception
	{
		switch (message.getType())
		{
			case AgentGroupDataUpdate:
				HandleAgentGroupDataUpdateMessage(message, simulator);
			case AgentDropGroup:
				HandleAgentDropGroupMessage(message, simulator);
		}
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AgentGroupDataUpdate:
				HandleAgentGroupDataUpdate(packet, simulator);
				break;
			case AgentDropGroup:
				HandleAgentDropGroup(packet, simulator);
				break;
			case GroupTitlesReply:
				HandleGroupTitlesReply(packet, simulator);
				break;
			case GroupProfileReply:
				HandleGroupProfileReply(packet, simulator);
				break;
			case GroupMembersReply:
				HandleGroupMembers(packet, simulator);
				break;
			case GroupRoleDataReply:
				HandleGroupRoleDataReply(packet, simulator);
				break;
			case GroupRoleMembersReply:
				HandleGroupRoleMembersReply(packet, simulator);
				break;
			case GroupActiveProposalItemReply:
				HandleGroupActiveProposalItem(packet, simulator);
				break;
			case GroupVoteHistoryItemReply:
				HandleGroupVoteHistoryItem(packet, simulator);
				break;
			case GroupAccountSummaryReply:
				HandleGroupAccountSummaryReply(packet, simulator);
				break;
			case GroupAccountDetailsReply:
				HandleGroupAccountDetails(packet, simulator);
				break;
			case GroupAccountTransactionsReply:
				HandleGroupAccountTransactions(packet, simulator);
				break;
			case CreateGroupReply:
				HandleCreateGroupReply(packet, simulator);
				break;
			case JoinGroupReply:
				HandleJoinGroupReply(packet, simulator);
				break;
			case LeaveGroupReply:
				HandleLeaveGroupReply(packet, simulator);
				break;
			case EjectGroupMemberReply:
				HandleEjectGroupMemberReply(packet, simulator);
				break;
			case GroupNoticesListReply:
				HandleGroupNoticesListReply(packet, simulator);
				break;
			case UUIDGroupNameReply:
				HandleUUIDGroupNameReply(packet, simulator);
				break;
		}
	}

	/**
	 * Request a current list of groups the avatar is a member of.
	 * 
	 * CAPS Event Queue must be running for this to work since the results come
	 * across CAPS.
	 * 
	 * @throws Exception
	 */
	public final void RequestCurrentGroups() throws Exception
	{
		AgentDataUpdateRequestPacket request = new AgentDataUpdateRequestPacket();

		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();

		Client.Network.SendPacket(request);
	}

	/**
	 * Lookup name of group based on groupID
	 * 
	 * @param groupID
	 *            groupID of group to lookup name for.
	 * @throws Exception
	 */
	public final void RequestGroupName(UUID groupID) throws Exception
	{
		// if we already have this in the cache, return from cache instead of
		// making a request
		if (GroupName2KeyCache.containsKey(groupID))
		{
			HashMap<UUID, String> groupNames = new HashMap<UUID, String>();
			synchronized (GroupName2KeyCache)
			{
				groupNames.put(groupID, GroupName2KeyCache.get(groupID));
			}
			OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
		}

		else
		{
			UUIDGroupNameRequestPacket req = new UUIDGroupNameRequestPacket();
			req.ID = new UUID[1];
			req.ID[0] = groupID;
			Client.Network.SendPacket(req);
		}
	}

	/**
	 * Request lookup of multiple group names
	 * 
	 * @param groupIDs
	 *            List of group IDs to request.
	 * @throws Exception
	 */
	public final void RequestGroupNames(ArrayList<UUID> groupIDs) throws Exception
	{
		HashMap<UUID, String> groupNames = new HashMap<UUID, String>();
		synchronized (GroupName2KeyCache)
		{
			for (UUID groupID : groupIDs)
			{
				if (GroupName2KeyCache.containsKey(groupID))
				{
					groupNames.put(groupID, GroupName2KeyCache.get(groupID));
				}
			}
		}

		if (groupIDs.size() > 0)
		{
			UUIDGroupNameRequestPacket req = new UUIDGroupNameRequestPacket();
			req.ID = new UUID[groupIDs.size()];
			for (int i = 0; i < groupIDs.size(); i++)
			{
				req.ID[i] = groupIDs.get(i);
			}
			Client.Network.SendPacket(req);
		}

		// fire handler from cache
		OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
	}

	/**
	 * Lookup group profile data such as name, enrollment, founder, logo, etc
	 * Subscribe to <code>OnGroupProfile</code> event to receive the results.
	 * 
	 * @param group
	 *            @param group group ID (UUID)
	 * @throws Exception
	 */
	public final void RequestGroupProfile(UUID group) throws Exception
	{
		GroupProfileRequestPacket request = new GroupProfileRequestPacket();

		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.GroupID = group;

		Client.Network.SendPacket(request);
	}

	/**
	 * Request a list of group members. Subscribe to <code>OnGroupMembers</code>
	 * event to receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupMembers(UUID group) throws Exception
	{
		UUID requestID = new UUID();
		synchronized (GroupMembersRequests)
		{
			GroupMembersRequests.add(requestID);
		}

		GroupMembersRequestPacket request = new GroupMembersRequestPacket();

		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;

		Client.Network.SendPacket(request);
		return requestID;
	}

	/**
	 * Request group roles Subscribe to <code>OnGroupRoles</code> event to
	 * receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupRoles(UUID group) throws Exception
	{
		UUID requestID = new UUID();
		synchronized (GroupRolesRequests)
		{
			GroupRolesRequests.add(requestID);
		}

		GroupRoleDataRequestPacket request = new GroupRoleDataRequestPacket();

		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;

		Client.Network.SendPacket(request);
		return requestID;
	}

	/**
	 * Request members (members,role) role mapping for a group. Subscribe to
	 * <code>OnGroupRolesMembers</code> event to receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupRolesMembers(UUID group) throws Exception
	{
		UUID requestID = new UUID();
		synchronized (GroupRolesRequests)
		{
			GroupRolesMembersRequests.add(requestID);
		}

		GroupRoleMembersRequestPacket request = new GroupRoleMembersRequestPacket();
		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.GroupData.GroupID = group;
		request.GroupData.RequestID = requestID;
		Client.Network.SendPacket(request);
		return requestID;
	}

	/**
	 * Request a groups Titles Subscribe to <code>OnGroupTitles</code> event to
	 * receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @return UUID of the request, use to index into cache
	 * @throws Exception
	 */
	public final UUID RequestGroupTitles(UUID group) throws Exception
	{
		UUID requestID = new UUID();

		GroupTitlesRequestPacket request = new GroupTitlesRequestPacket();

		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.AgentData.GroupID = group;
		request.AgentData.RequestID = requestID;

		Client.Network.SendPacket(request);
		return requestID;
	}

	/**
	 * Begin to get the group account summary Subscribe to the
	 * <code>OnGroupAccountSummary</code> event to receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @param intervalDays
	 *            How long of an interval
	 * @param currentInterval
	 *            Which interval (0 for current, 1 for last)
	 * @throws Exception
	 */
	public final void RequestGroupAccountSummary(UUID group, int intervalDays, int currentInterval) throws Exception
	{
		GroupAccountSummaryRequestPacket p = new GroupAccountSummaryRequestPacket();
		p.AgentData.AgentID = Client.Self.getAgentID();
		p.AgentData.SessionID = Client.Self.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		Client.Network.SendPacket(p);
	}

	/**
	 * Begin to get the group account details Subscribe to the
	 * <code>OnGroupAccountDetails</code> event to receive the results.
	 * 
	 * @param group
	 *            group ID (UUID)
	 * @param intervalDays
	 *            How long of an interval
	 * @param currentInterval
	 *            Which interval (0 for current, 1 for last)
	 * @throws Exception
	 */
	public final void RequestGroupAccountDetails(UUID group, int intervalDays, int currentInterval) throws Exception
	{
		GroupAccountDetailsRequestPacket p = new GroupAccountDetailsRequestPacket();
		p.AgentData.AgentID = Client.Self.getAgentID();
		p.AgentData.SessionID = Client.Self.getSessionID();
		p.AgentData.GroupID = group;
		// TODO: Store request ID to identify the callback
		p.MoneyData.RequestID = new UUID();
		p.MoneyData.CurrentInterval = currentInterval;
		p.MoneyData.IntervalDays = intervalDays;
		Client.Network.SendPacket(p);
	}

	/**
	 * Invites a user to a group
	 * 
	 * @param group
	 *            The group to invite to
	 * @param roles
	 *            A list of roles to invite a person to
	 * @param personkey
	 *            Key of person to invite
	 * @throws Exception
	 */
	public final void Invite(UUID group, ArrayList<UUID> roles, UUID personkey) throws Exception
	{
		InviteGroupRequestPacket igp = new InviteGroupRequestPacket();

		igp.AgentData = igp.new AgentDataBlock();
		igp.AgentData.AgentID = Client.Self.getAgentID();
		igp.AgentData.SessionID = Client.Self.getSessionID();

		igp.GroupID = group;

		igp.InviteData = new InviteGroupRequestPacket.InviteDataBlock[roles.size()];

		for (int i = 0; i < roles.size(); i++)
		{
			igp.InviteData[i] = igp.new InviteDataBlock();
			igp.InviteData[i].InviteeID = personkey;
			igp.InviteData[i].RoleID = roles.get(i);
		}

		Client.Network.SendPacket(igp);
	}

	/**
	 * Set a group as the current active group
	 * 
	 * @param id
	 *            group ID (UUID)
	 * @throws Exception
	 */
	public final void ActivateGroup(UUID id) throws Exception
	{
		ActivateGroupPacket activate = new ActivateGroupPacket();
		activate.AgentData.AgentID = Client.Self.getAgentID();
		activate.AgentData.SessionID = Client.Self.getSessionID();
		activate.AgentData.GroupID = id;

		Client.Network.SendPacket(activate);
	}

	/**
	 * Change the role that determines your active title
	 * 
	 * @param group
	 *            Group ID to use
	 * @param role
	 *            Role ID to change to
	 * @throws Exception
	 */
	public final void ActivateTitle(UUID group, UUID role) throws Exception
	{
		GroupTitleUpdatePacket gtu = new GroupTitleUpdatePacket();
		gtu.AgentData.AgentID = Client.Self.getAgentID();
		gtu.AgentData.SessionID = Client.Self.getSessionID();
		gtu.AgentData.TitleRoleID = role;
		gtu.AgentData.GroupID = group;

		Client.Network.SendPacket(gtu);
	}

	/**
	 * Set this avatar's tier contribution
	 * 
	 * @param group
	 *            Group ID to change tier in
	 * @param contribution
	 *            amount of tier to donate
	 * @throws Exception
	 */
	public final void SetGroupContribution(UUID group, int contribution) throws Exception
	{
		SetGroupContributionPacket sgp = new SetGroupContributionPacket();
		sgp.AgentData.AgentID = Client.Self.getAgentID();
		sgp.AgentData.SessionID = Client.Self.getSessionID();
		sgp.Data.GroupID = group;
		sgp.Data.Contribution = contribution;

		Client.Network.SendPacket(sgp);
	}

	/**
	 * Save wheather agent wants to accept group notices and list this group in
	 * their profile
	 * 
	 * @param groupID
	 *            Group <see cref="UUID"/>
	 * @param acceptNotices
	 *            Accept notices from this group
	 * @param listInProfile
	 *            List this group in the profile
	 * @throws Exception
	 */
	public final void SetGroupAcceptNotices(UUID groupID, boolean acceptNotices, boolean listInProfile)
			throws Exception
	{
		SetGroupAcceptNoticesPacket p = new SetGroupAcceptNoticesPacket();
		p.AgentData.AgentID = Client.Self.getAgentID();
		p.AgentData.SessionID = Client.Self.getSessionID();
		p.Data.GroupID = groupID;
		p.Data.AcceptNotices = acceptNotices;
		p.ListInProfile = listInProfile;

		Client.Network.SendPacket(p);
	}

	/**
	 * Request to join a group
	 * 
	 * @param id
	 *            group ID (UUID) to join.
	 * @throws Exception
	 */
	public final void RequestJoinGroup(UUID id) throws Exception
	{
		JoinGroupRequestPacket join = new JoinGroupRequestPacket();
		join.AgentData.AgentID = Client.Self.getAgentID();
		join.AgentData.SessionID = Client.Self.getSessionID();

		join.GroupID = id;

		Client.Network.SendPacket(join);
	}

	/**
	 * Request to create a new group. If the group is successfully created,
	 * L$100 will automatically be deducted
	 * 
	 * Subscribe to <code>OnGroupCreated</code> event to receive confirmation.
	 * 
	 * @param group
	 *            Group struct containing the new group info
	 * @throws Exception
	 */
	public final void RequestCreateGroup(Group group) throws Exception
	{
		CreateGroupRequestPacket cgrp = new CreateGroupRequestPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = Client.Self.getAgentID();
		cgrp.AgentData.SessionID = Client.Self.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.AllowPublish = group.AllowPublish;
		cgrp.GroupData.setCharter(Helpers.StringToBytes(group.Charter));
		cgrp.GroupData.InsigniaID = group.InsigniaID;
		cgrp.GroupData.MaturePublish = group.MaturePublish;
		cgrp.GroupData.MembershipFee = group.MembershipFee;
		cgrp.GroupData.setName(Helpers.StringToBytes(group.Name));
		cgrp.GroupData.OpenEnrollment = group.OpenEnrollment;
		cgrp.GroupData.ShowInList = group.ShowInList;

		Client.Network.SendPacket(cgrp);
	}

	/**
	 * Update a group's profile and other information
	 * 
	 * @param id
	 *            Groups ID (UUID) to update.
	 * @param group
	 *            Group struct to update.
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void UpdateGroup(UUID id, Group group) throws Exception
	{
		UpdateGroupInfoPacket cgrp = new UpdateGroupInfoPacket();
		cgrp.AgentData = cgrp.new AgentDataBlock();
		cgrp.AgentData.AgentID = Client.Self.getAgentID();
		cgrp.AgentData.SessionID = Client.Self.getSessionID();

		cgrp.GroupData = cgrp.new GroupDataBlock();
		cgrp.GroupData.GroupID = id;
		cgrp.GroupData.AllowPublish = group.AllowPublish;
		cgrp.GroupData.setCharter(Helpers.StringToBytes(group.Charter));
		cgrp.GroupData.InsigniaID = group.InsigniaID;
		cgrp.GroupData.MaturePublish = group.MaturePublish;
		cgrp.GroupData.MembershipFee = group.MembershipFee;
		cgrp.GroupData.OpenEnrollment = group.OpenEnrollment;
		cgrp.GroupData.ShowInList = group.ShowInList;

		Client.Network.SendPacket(cgrp);
	}

	/**
	 * Eject a user from a group
	 * 
	 * @param group
	 *            Group ID to eject the user from
	 * @param member
	 *            Avatar's key to eject
	 * @throws Exception
	 */
	public final void EjectUser(UUID group, UUID member) throws Exception
	{
		EjectGroupMemberRequestPacket eject = new EjectGroupMemberRequestPacket();
		eject.AgentData = eject.new AgentDataBlock();
		eject.AgentData.AgentID = Client.Self.getAgentID();
		eject.AgentData.SessionID = Client.Self.getSessionID();

		eject.GroupID = group;

		eject.EjecteeID = new UUID[1];
		eject.EjecteeID[0] = member;

		Client.Network.SendPacket(eject);
	}

	/**
	 * Update role information
	 * 
	 * @param role
	 *            Modified role to be updated
	 * @throws Exception
	 */
	public final void UpdateRole(GroupRole role) throws Exception
	{
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = Client.Self.getAgentID();
		gru.AgentData.SessionID = Client.Self.getSessionID();
		gru.AgentData.GroupID = role.GroupID;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].setName(Helpers.StringToBytes(role.Name));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(role.Description));
		gru.RoleData[0].Powers = role.Powers;
		gru.RoleData[0].RoleID = role.ID;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(role.Title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.UpdateAll.getValue();
		Client.Network.SendPacket(gru);
	}

	/**
	 * Create a new group role
	 * 
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role to create
	 * @throws Exception
	 */
	public final void CreateRole(UUID group, GroupRole role) throws Exception
	{
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = Client.Self.getAgentID();
		gru.AgentData.SessionID = Client.Self.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = new UUID();
		gru.RoleData[0].setName(Helpers.StringToBytes(role.Name));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(role.Description));
		gru.RoleData[0].Powers = role.Powers;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(role.Title));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Create.getValue();
		Client.Network.SendPacket(gru);
	}

	/**
	 * Delete a group role
	 * 
	 * @param group
	 *            Group ID to update
	 * @param roleID
	 *            Role to delete
	 * @throws Exception
	 */
	public final void DeleteRole(UUID group, UUID roleID) throws Exception
	{
		GroupRoleUpdatePacket gru = new GroupRoleUpdatePacket();
		gru.AgentData.AgentID = Client.Self.getAgentID();
		gru.AgentData.SessionID = Client.Self.getSessionID();
		gru.AgentData.GroupID = group;
		gru.RoleData = new GroupRoleUpdatePacket.RoleDataBlock[1];
		gru.RoleData[0] = gru.new RoleDataBlock();
		gru.RoleData[0].RoleID = roleID;
		gru.RoleData[0].setName(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].setDescription(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].Powers = 0;
		gru.RoleData[0].setTitle(Helpers.StringToBytes(Helpers.EmptyString));
		gru.RoleData[0].UpdateType = GroupRoleUpdate.Delete.getValue();
		Client.Network.SendPacket(gru);
	}

	/**
	 * Remove an avatar from a role
	 * 
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role ID to be removed from
	 * @param member
	 *            Avatar's Key to remove
	 * @throws Exception
	 */
	public final void RemoveFromRole(UUID group, UUID role, UUID member) throws Exception
	{
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = Client.Self.getAgentID();
		grc.AgentData.SessionID = Client.Self.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 1 = Remove From Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 1;
		Client.Network.SendPacket(grc);
	}

	/**
	 * Assign an avatar to a role
	 * 
	 * @param group
	 *            Group ID to update
	 * @param role
	 *            Role ID to assign to
	 * @param member
	 *            Avatar's ID to assign to role
	 * @throws Exception
	 */
	public final void AddToRole(UUID group, UUID role, UUID member) throws Exception
	{
		GroupRoleChangesPacket grc = new GroupRoleChangesPacket();
		grc.AgentData.AgentID = Client.Self.getAgentID();
		grc.AgentData.SessionID = Client.Self.getSessionID();
		grc.AgentData.GroupID = group;
		grc.RoleChange = new GroupRoleChangesPacket.RoleChangeBlock[1];
		grc.RoleChange[0] = grc.new RoleChangeBlock();
		// Add to members and role
		grc.RoleChange[0].MemberID = member;
		grc.RoleChange[0].RoleID = role;
		// 0 = Add to Role TODO: this should be in an enum
		grc.RoleChange[0].Change = 0;
		Client.Network.SendPacket(grc);
	}

	/**
	 * Request the group notices list
	 * 
	 * @param group
	 *            Group ID to fetch notices for
	 * @throws Exception
	 */
	public final void RequestGroupNoticesList(UUID group) throws Exception
	{
		GroupNoticesListRequestPacket gnl = new GroupNoticesListRequestPacket();
		gnl.AgentData.AgentID = Client.Self.getAgentID();
		gnl.AgentData.SessionID = Client.Self.getSessionID();
		gnl.GroupID = group;
		Client.Network.SendPacket(gnl);
	}

	/**
	 * Request a group notice by key
	 * 
	 * @param noticeID
	 *            ID of group notice
	 * @throws Exception
	 */
	public final void RequestGroupNotice(UUID noticeID) throws Exception
	{
		GroupNoticeRequestPacket gnr = new GroupNoticeRequestPacket();
		gnr.AgentData.AgentID = Client.Self.getAgentID();
		gnr.AgentData.SessionID = Client.Self.getSessionID();
		gnr.GroupNoticeID = noticeID;
		Client.Network.SendPacket(gnr);
	}

	/**
	 * Send out a group notice
	 * 
	 * @param group
	 *            Group ID to update
	 * @param notice
	 *            <code>GroupNotice</code> structure containing notice data
	 * @throws Exception
	 */
	public final void SendGroupNotice(UUID group, GroupNotice notice) throws Exception
	{
		Client.Self.InstantMessage(Client.Self.getName(), group, notice.Subject + "|" + notice.Message, UUID.Zero,
				InstantMessageDialog.GroupNotice, InstantMessageOnline.Online, Vector3.Zero, UUID.Zero, 0,
				notice.SerializeAttachment());
	}

	/**
	 * Start a group proposal (vote)
	 * 
	 * @param group
	 *            The Group ID to send proposal to
	 * @param prop
	 *            <code>GroupProposal</code> structure containing the proposal
	 * @throws Exception
	 */
	public final void StartProposal(UUID group, GroupProposal prop) throws Exception
	{
		StartGroupProposalPacket p = new StartGroupProposalPacket();
		p.AgentData.AgentID = Client.Self.getAgentID();
		p.AgentData.SessionID = Client.Self.getSessionID();
		p.ProposalData.GroupID = group;
		p.ProposalData.setProposalText(Helpers.StringToBytes(prop.ProposalText));
		p.ProposalData.Quorum = prop.Quorum;
		p.ProposalData.Majority = prop.Majority;
		p.ProposalData.Duration = prop.Duration;
		Client.Network.SendPacket(p);
	}

	/**
	 * Request to leave a group
	 * 
	 * @param groupID
	 *            The group to leave
	 * @throws Exception
	 */
	public final void LeaveGroup(UUID groupID) throws Exception
	{
		LeaveGroupRequestPacket p = new LeaveGroupRequestPacket();
		p.AgentData.AgentID = Client.Self.getAgentID();
		p.AgentData.SessionID = Client.Self.getSessionID();
		p.GroupID = groupID;

		Client.Network.SendPacket(p);
	}

	// #endregion

	// #region Packet Handlers
	private final void HandleAgentGroupDataUpdateMessage(IMessage message, Simulator simulator)
	{
		if (OnCurrentGroups.count() > 0)
		{
			AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage) message;

			HashMap<UUID, Group> currentGroups = new HashMap<UUID, Group>();
			for (int i = 0; i < msg.GroupDataBlock.length; i++)
			{
				Group group = new Group(msg.GroupDataBlock[i].GroupID);
				group.InsigniaID = msg.GroupDataBlock[i].GroupInsigniaID;
				group.Name = msg.GroupDataBlock[i].GroupName;
				group.Contribution = msg.GroupDataBlock[i].Contribution;
				group.AcceptNotices = msg.GroupDataBlock[i].AcceptNotices;
				group.Powers = msg.GroupDataBlock[i].GroupPowers;
				group.ListInProfile = msg.NewGroupDataBlock[i].ListInProfile;

				currentGroups.put(group.ID, group);

				synchronized (GroupName2KeyCache)
				{
					if (!GroupName2KeyCache.containsKey(group.ID))
					{
						GroupName2KeyCache.put(group.ID, group.Name);
					}
				}
			}
			OnCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
		}
	}

	private final void HandleAgentGroupDataUpdate(Packet packet, Simulator simulator) throws Exception
	{
		if (OnCurrentGroups.count() > 0)
		{
			AgentGroupDataUpdatePacket update = (AgentGroupDataUpdatePacket) packet;

			HashMap<UUID, Group> currentGroups = new HashMap<UUID, Group>();

			for (AgentGroupDataUpdatePacket.GroupDataBlock block : update.GroupData)
			{
				Group group = new Group(block.GroupID);

				group.InsigniaID = block.GroupInsigniaID;
				group.Name = Helpers.BytesToString(block.getGroupName());
				group.Powers = block.GroupPowers;
				group.Contribution = block.Contribution;
				group.AcceptNotices = block.AcceptNotices;

				currentGroups.put(block.GroupID, group);
			}

			OnCurrentGroups.dispatch(new CurrentGroupsCallbackArgs(currentGroups));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleAgentDropGroupMessage(IMessage message, Simulator simulator)
	{
		if (OnGroupDropped.count() > 0)
		{
			AgentDropGroupMessage msg = (AgentDropGroupMessage) message;
			for (int i = 0; i < msg.AgentDataBlock.length; i++)
			{
				OnGroupDropped.dispatch(new GroupDroppedCallbackArgs(msg.AgentDataBlock[i].GroupID));
			}
		}
	}

	private final void HandleAgentDropGroup(Packet packet, Simulator simulator)
	{
		OnGroupDropped.dispatch(new GroupDroppedCallbackArgs(((AgentDropGroupPacket) packet).AgentData.GroupID));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupProfileReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		if (OnGroupProfile.count() > 0)
		{
			GroupProfileReplyPacket profile = (GroupProfileReplyPacket) packet;
			Group group = new Group(profile.GroupData.GroupID);

			group.AllowPublish = profile.GroupData.AllowPublish;
			group.Charter = Helpers.BytesToString(profile.GroupData.getCharter());
			group.FounderID = profile.GroupData.FounderID;
			group.GroupMembershipCount = profile.GroupData.GroupMembershipCount;
			group.GroupRolesCount = profile.GroupData.GroupRolesCount;
			group.InsigniaID = profile.GroupData.InsigniaID;
			group.MaturePublish = profile.GroupData.MaturePublish;
			group.MembershipFee = profile.GroupData.MembershipFee;
			group.MemberTitle = Helpers.BytesToString(profile.GroupData.getMemberTitle());
			group.Money = profile.GroupData.Money;
			group.Name = Helpers.BytesToString(profile.GroupData.getName());
			group.OpenEnrollment = profile.GroupData.OpenEnrollment;
			group.OwnerRole = profile.GroupData.OwnerRole;
			group.Powers = profile.GroupData.PowersMask;
			group.ShowInList = profile.GroupData.ShowInList;

			OnGroupProfile.dispatch(new GroupProfileCallbackArgs(group));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupNoticesListReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		if (OnGroupNoticesListReply.count() > 0)
		{
			GroupNoticesListReplyPacket reply = (GroupNoticesListReplyPacket) packet;

			ArrayList<GroupNoticesListEntry> notices = new ArrayList<GroupNoticesListEntry>();

			for (GroupNoticesListReplyPacket.DataBlock entry : reply.Data)
			{
				GroupNoticesListEntry notice = new GroupNoticesListEntry();
				notice.FromName = Helpers.BytesToString(entry.getFromName());
				notice.Subject = Helpers.BytesToString(entry.getSubject());
				notice.NoticeID = entry.NoticeID;
				notice.Timestamp = entry.Timestamp;
				notice.HasAttachment = entry.HasAttachment;
				notice.AssetType = AssetType.setValue(entry.AssetType);

				notices.add(notice);
			}
			OnGroupNoticesListReply.dispatch(new GroupNoticesListReplyCallbackArgs(reply.AgentData.GroupID, notices));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupTitlesReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		if (OnGroupTitles.count() > 0)
		{
			GroupTitlesReplyPacket titles = (GroupTitlesReplyPacket) packet;
			java.util.HashMap<UUID, GroupTitle> groupTitleCache = new java.util.HashMap<UUID, GroupTitle>();

			for (GroupTitlesReplyPacket.GroupDataBlock block : titles.GroupData)
			{
				GroupTitle groupTitle = new GroupTitle();

				groupTitle.GroupID = titles.AgentData.GroupID;
				groupTitle.RoleID = block.RoleID;
				groupTitle.Title = Helpers.BytesToString(block.getTitle());
				groupTitle.Selected = block.Selected;

				groupTitleCache.put(block.RoleID, groupTitle);
			}
			OnGroupTitles.dispatch(new GroupTitlesReplyCallbackArgs(titles.AgentData.RequestID,
					titles.AgentData.GroupID, groupTitleCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupMembers(Packet packet, Simulator simulator) throws UnsupportedEncodingException
	{
		GroupMembersReplyPacket members = (GroupMembersReplyPacket) packet;
		HashMap<UUID, GroupMember> groupMemberCache = null;

		synchronized (GroupMembersRequests)
		{
			// If nothing is registered to receive this RequestID drop the data
			if (GroupMembersRequests.contains(members.GroupData.RequestID))
			{
				synchronized (TempGroupMembers)
				{
					if (TempGroupMembers.containsKey(members.GroupData.RequestID))
					{
						groupMemberCache = TempGroupMembers.get(members.GroupData.RequestID);
					}
					else
					{
						groupMemberCache = new java.util.HashMap<UUID, GroupMember>();
						TempGroupMembers.put(members.GroupData.RequestID, groupMemberCache);
					}

					for (GroupMembersReplyPacket.MemberDataBlock block : members.MemberData)
					{
						GroupMember groupMember = new GroupMember(block.AgentID);

						groupMember.Contribution = block.Contribution;
						groupMember.IsOwner = block.IsOwner;
						groupMember.OnlineStatus = Helpers.BytesToString(block.getOnlineStatus());
						groupMember.Powers = block.AgentPowers;
						groupMember.Title = Helpers.BytesToString(block.getTitle());

						groupMemberCache.put(block.AgentID, groupMember);
					}

					if (groupMemberCache.size() >= members.GroupData.MemberCount)
					{
						GroupMembersRequests.remove(members.GroupData.RequestID);
						TempGroupMembers.remove(members.GroupData.RequestID);
					}
				}
			}
		}

		if (groupMemberCache != null && groupMemberCache.size() >= members.GroupData.MemberCount)
		{
			OnGroupMembersReply.dispatch(new GroupMembersReplyCallbackArgs(members.GroupData.RequestID,
					members.GroupData.GroupID, groupMemberCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupRoleDataReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		GroupRoleDataReplyPacket roles = (GroupRoleDataReplyPacket) packet;
		HashMap<UUID, GroupRole> groupRoleCache = null;

		synchronized (GroupRolesRequests)
		{
			// If nothing is registered to receive this RequestID drop the data
			if (GroupRolesRequests.contains(roles.GroupData.RequestID))
			{
				GroupRolesRequests.remove(roles.GroupData.RequestID);

				synchronized (TempGroupRoles)
				{
					if (TempGroupRoles.containsKey(roles.GroupData.RequestID))
					{
						groupRoleCache = TempGroupRoles.get(roles.GroupData.RequestID);
					}
					else
					{
						groupRoleCache = new java.util.HashMap<UUID, GroupRole>();
						TempGroupRoles.put(roles.GroupData.RequestID, groupRoleCache);
					}

					for (GroupRoleDataReplyPacket.RoleDataBlock block : roles.RoleData)
					{
						GroupRole groupRole = new GroupRole(roles.GroupData.GroupID);

						groupRole.ID = block.RoleID;
						groupRole.Description = Helpers.BytesToString(block.getDescription());
						groupRole.Name = Helpers.BytesToString(block.getName());
						groupRole.Powers = block.Powers;
						groupRole.Title = Helpers.BytesToString(block.getTitle());

						groupRoleCache.put(block.RoleID, groupRole);
					}

					if (groupRoleCache.size() >= roles.GroupData.RoleCount)
					{
						GroupRolesRequests.remove(roles.GroupData.RequestID);
						TempGroupRoles.remove(roles.GroupData.RequestID);
					}
				}
			}
		}

		if (groupRoleCache != null && groupRoleCache.size() >= roles.GroupData.RoleCount)
		{
			OnGroupRoleDataReply.dispatch(new GroupRolesDataReplyCallbackArgs(roles.GroupData.RequestID,
					roles.GroupData.GroupID, groupRoleCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleGroupRoleMembersReply(Packet packet, Simulator simulator)
	{
		GroupRoleMembersReplyPacket members = (GroupRoleMembersReplyPacket) packet;
		ArrayList<Entry<UUID, UUID>> groupRoleMemberCache = null;

		synchronized (GroupRolesMembersRequests)
		{
			// If nothing is registered to receive this RequestID drop the data
			if (GroupRolesMembersRequests.contains(members.AgentData.RequestID))
			{
				synchronized (TempGroupRolesMembers)
				{
					if (TempGroupRolesMembers.containsKey(members.AgentData.RequestID))
					{
						groupRoleMemberCache = TempGroupRolesMembers.get(members.AgentData.RequestID);
					}
					else
					{
						groupRoleMemberCache = new ArrayList<Entry<UUID, UUID>>();
						TempGroupRolesMembers.put(members.AgentData.RequestID, groupRoleMemberCache);
					}

					for (GroupRoleMembersReplyPacket.MemberDataBlock block : members.MemberData)
					{
						Entry<UUID, UUID> rolemember = new AbstractMap.SimpleEntry<UUID, UUID>(block.RoleID,
								block.MemberID);

						groupRoleMemberCache.add(rolemember);
					}

					if (groupRoleMemberCache.size() >= members.AgentData.TotalPairs)
					{
						GroupRolesMembersRequests.remove(members.AgentData.RequestID);
						TempGroupRolesMembers.remove(members.AgentData.RequestID);
					}
				}
			}
		}

		if (groupRoleMemberCache != null && groupRoleMemberCache.size() >= members.AgentData.TotalPairs)
		{
			OnGroupRoleMembers.dispatch(new GroupRolesMembersReplyCallbackArgs(members.AgentData.RequestID,
					members.AgentData.GroupID, groupRoleMemberCache));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupActiveProposalItem(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		GroupActiveProposalItemReplyPacket proposal = (GroupActiveProposalItemReplyPacket) packet;

		// UUID transactionID = proposal.TransactionData.TransactionID;

		ArrayList<GroupProposalItem> array = new ArrayList<GroupProposalItem>(proposal.ProposalData.length);
		for (GroupActiveProposalItemReplyPacket.ProposalDataBlock block : proposal.ProposalData)
		{
			GroupProposalItem p = new GroupProposalItem();

			p.VoteID = block.VoteID;
			p.VoteInitiator = block.VoteInitiator;
			p.TerseDateID = Helpers.BytesToString(block.getTerseDateID());
			p.StartDateTime = Helpers.StringToDate(Helpers.BytesToString(block.getStartDateTime()));
			p.EndDateTime = Helpers.StringToDate(Helpers.BytesToString(block.getEndDateTime()));
			p.AlreadyVoted = block.AlreadyVoted;
			p.VoteCast = Helpers.BytesToString(block.getVoteCast());
			p.Majority = block.Majority;
			p.Quorum = block.Quorum;
			p.ProposalText = Helpers.BytesToString(block.getProposalText());

			array.add(p);
		}
		// TODO: Create transactionID hashed event queue and dispatch the event
		// there
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleGroupVoteHistoryItem(Packet packet, Simulator simulator)
	{
		@SuppressWarnings("unused")
		GroupVoteHistoryItemReplyPacket history = (GroupVoteHistoryItemReplyPacket) packet;

		// TODO: This was broken in the official viewer when I was last trying
		// to work on it
		/*
		 * GroupProposalItem proposal = new GroupProposalItem();
		 * proposal.Majority = history.HistoryItemData.Majority; proposal.Quorum
		 * = history.HistoryItemData.Quorum; proposal.Duration =
		 * history.TransactionData.TotalNumItems; proposal.ProposalText = ;
		 * proposal.TerseDateID = proposal.VoteID =
		 * history.HistoryItemData.VoteID; proposal.VoteInitiator =
		 * history.HistoryItemData.VoteInitiator; for (int i = 0; i <
		 * history.VoteItem.length; i++) { history.VoteItem[i].CandidateID;
		 * history.VoteItem[i].NumVotes; }
		 */
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleGroupAccountSummaryReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		if (OnGroupAccountSummaryReply.count() > 0)
		{
			GroupAccountSummaryReplyPacket summary = (GroupAccountSummaryReplyPacket) packet;
			GroupAccountSummary account = new GroupAccountSummary();

			account.Balance = summary.MoneyData.Balance;
			account.CurrentInterval = summary.MoneyData.CurrentInterval;
			account.GroupTaxCurrent = summary.MoneyData.GroupTaxCurrent;
			account.GroupTaxEstimate = summary.MoneyData.GroupTaxEstimate;
			account.IntervalDays = summary.MoneyData.IntervalDays;
			account.LandTaxCurrent = summary.MoneyData.LandTaxCurrent;
			account.LandTaxEstimate = summary.MoneyData.LandTaxEstimate;
			account.LastTaxDate = Helpers.BytesToString(summary.MoneyData.getLastTaxDate());
			account.LightTaxCurrent = summary.MoneyData.LightTaxCurrent;
			account.LightTaxEstimate = summary.MoneyData.LightTaxEstimate;
			account.NonExemptMembers = summary.MoneyData.NonExemptMembers;
			account.ObjectTaxCurrent = summary.MoneyData.ObjectTaxCurrent;
			account.ObjectTaxEstimate = summary.MoneyData.ObjectTaxEstimate;
			account.ParcelDirFeeCurrent = summary.MoneyData.ParcelDirFeeCurrent;
			account.ParcelDirFeeEstimate = summary.MoneyData.ParcelDirFeeEstimate;
			account.StartDate = Helpers.BytesToString(summary.MoneyData.getStartDate());
			account.TaxDate = Helpers.BytesToString(summary.MoneyData.getTaxDate());
			account.TotalCredits = summary.MoneyData.TotalCredits;
			account.TotalDebits = summary.MoneyData.TotalDebits;

			OnGroupAccountSummaryReply.dispatch(new GroupAccountSummaryReplyCallbackArgs(summary.AgentData.GroupID,
					account));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private final void HandleCreateGroupReply(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		CreateGroupReplyPacket reply = (CreateGroupReplyPacket) packet;
		String message = Helpers.BytesToString(reply.ReplyData.getMessage());

		OnGroupCreatedReply.dispatch(new GroupCreatedReplyCallbackArgs(reply.ReplyData.GroupID,
				reply.ReplyData.Success, message));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleJoinGroupReply(Packet packet, Simulator simulator)
	{
		JoinGroupReplyPacket reply = (JoinGroupReplyPacket) packet;

		OnGroupJoinedReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleLeaveGroupReply(Packet packet, Simulator simulator)
	{
		LeaveGroupReplyPacket reply = (LeaveGroupReplyPacket) packet;

		OnGroupLeaveReply.dispatch(new GroupOperationCallbackArgs(reply.GroupData.GroupID, reply.GroupData.Success));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 * @throws UnsupportedEncodingException
	 */
	private void HandleUUIDGroupNameReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException
	{
		UUIDGroupNameReplyPacket reply = (UUIDGroupNameReplyPacket) packet;
		UUIDGroupNameReplyPacket.UUIDNameBlockBlock[] blocks = reply.UUIDNameBlock;

		java.util.HashMap<UUID, String> groupNames = new java.util.HashMap<UUID, String>();

		for (UUIDGroupNameReplyPacket.UUIDNameBlockBlock block : blocks)
		{
			String name = Helpers.BytesToString(block.getGroupName());
			groupNames.put(block.ID, name);
			if (!GroupName2KeyCache.containsKey(block.ID))
			{
				GroupName2KeyCache.put(block.ID, name);
			}
		}
		OnGroupNamesReply.dispatch(new GroupNamesCallbackArgs(groupNames));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The EventArgs object containing the packet data
	 */
	private final void HandleEjectGroupMemberReply(Packet packet, Simulator simulator)
	{
		EjectGroupMemberReplyPacket reply = (EjectGroupMemberReplyPacket) packet;

		// TODO: On Success remove the member from the cache(s)

		OnGroupMemberEjected.dispatch(new GroupOperationCallbackArgs(reply.GroupID, reply.Success));
	}

	// #endregion Packet Handlers

	private final void HandleGroupAccountDetails(Packet packet, Simulator simulator) throws Exception
	{
		GroupAccountDetailsReplyPacket details = (GroupAccountDetailsReplyPacket) packet;

		if (OnGroupAccountDetailsCallbacks.containsKey(details.AgentData.GroupID))
		{
			GroupAccountDetails account = new GroupAccountDetails();

			account.CurrentInterval = details.MoneyData.CurrentInterval;
			account.IntervalDays = details.MoneyData.IntervalDays;
			account.StartDate = Helpers.BytesToString(details.MoneyData.getStartDate());

			account.HistoryItems = new HashMapInt();

			for (int i = 0; i < details.HistoryData.length; i++)
			{
				GroupAccountDetailsReplyPacket.HistoryDataBlock block = details.HistoryData[i];
				account.HistoryItems.put(Helpers.BytesToString(block.getDescription()), block.Amount);
			}
			OnGroupAccountDetailsCallbacks.get(details.AgentData.GroupID).callback(account);
		}
	}

	private final void HandleGroupAccountTransactions(Packet packet, Simulator simulator) throws Exception
	{
		GroupAccountTransactionsReplyPacket transactions = (GroupAccountTransactionsReplyPacket) packet;

		if (OnGroupAccountTransactionsCallbacks.containsKey(transactions.AgentData.GroupID))
		{
			GroupAccountTransactions account = new GroupAccountTransactions();

			account.CurrentInterval = transactions.MoneyData.CurrentInterval;
			account.IntervalDays = transactions.MoneyData.IntervalDays;
			account.StartDate = Helpers.BytesToString(transactions.MoneyData.getStartDate());

			account.Transactions = new TransactionEntry[transactions.HistoryData.length];
			for (int i = 0; i < transactions.HistoryData.length; i++)
			{
				TransactionEntry entry = account.new TransactionEntry();
				GroupAccountTransactionsReplyPacket.HistoryDataBlock block = transactions.HistoryData[i];

				entry.Type = block.Type;
				entry.Amount = block.Amount;
				entry.Item = Helpers.BytesToString(block.getItem());
				entry.User = Helpers.BytesToString(block.getUser());
				entry.Time = Helpers.BytesToString(block.getTime());
				account.Transactions[i] = entry;
			}
			OnGroupAccountTransactionsCallbacks.get(transactions.AgentData.GroupID).callback(account);
		}
	}

	// #region CallbackArgs

	// Contains the current groups your agent is a member of
	public class CurrentGroupsCallbackArgs implements CallbackArgs
	{
		private final java.util.HashMap<UUID, Group> m_Groups;

		// Get the current groups your agent is a member of
		public final java.util.HashMap<UUID, Group> getGroups()
		{
			return m_Groups;
		}

		/**
		 * Construct a new instance of the CurrentGroupsCallbackArgs class
		 * 
		 * @param groups
		 *            The current groups your agent is a member of
		 */
		public CurrentGroupsCallbackArgs(java.util.HashMap<UUID, Group> groups)
		{
			this.m_Groups = groups;
		}
	}

	// A Dictionary of group names, where the Key is the groups ID and the value
	// is the groups name
	public class GroupNamesCallbackArgs implements CallbackArgs
	{
		private final HashMap<UUID, String> m_GroupNames;

		// Get the Group Names dictionary
		public final HashMap<UUID, String> getGroupNames()
		{
			return m_GroupNames;
		}

		/**
		 * Construct a new instance of the GroupNamesCallbackArgs class
		 * 
		 * @param groupNames
		 *            The Group names dictionary
		 */
		public GroupNamesCallbackArgs(HashMap<UUID, String> groupNames)
		{
			this.m_GroupNames = groupNames;
		}
	}

	// Represents the members of a group
	public class GroupMembersReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_RequestID;
		private final UUID m_GroupID;
		private final HashMap<UUID, GroupMember> m_Members;

		// Get the ID as returned by the request to correlate this result set
		// and the request
		public final UUID getRequestID()
		{
			return m_RequestID;
		}

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the dictionary of members
		public final HashMap<UUID, GroupMember> getMembers()
		{
			return m_Members;
		}

		/**
		 * Construct a new instance of the GroupMembersReplyCallbackArgs class
		 * 
		 * @param requestID
		 *            The ID of the request
		 * @param groupID
		 *            The ID of the group
		 * @param members
		 *            The membership list of the group
		 */
		public GroupMembersReplyCallbackArgs(UUID requestID, UUID groupID, HashMap<UUID, GroupMember> members)
		{
			this.m_RequestID = requestID;
			this.m_GroupID = groupID;
			this.m_Members = members;
		}
	}

	// Represents the roles associated with a group
	public class GroupRolesDataReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_RequestID;
		private final UUID m_GroupID;
		private final HashMap<UUID, GroupRole> m_Roles;

		// Get the ID as returned by the request to correlate this result set
		// and the request
		public final UUID getRequestID()
		{
			return m_RequestID;
		}

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the dictionary containing the roles
		public final java.util.HashMap<UUID, GroupRole> getRoles()
		{
			return m_Roles;
		}

		/**
		 * Construct a new instance of the GroupRolesDataReplyCallbackArgs class
		 * 
		 * @param requestID
		 *            The ID as returned by the request to correlate this result
		 *            set and the request
		 * @param groupID
		 *            The ID of the group
		 * @param roles
		 *            The dictionary containing the roles
		 */
		public GroupRolesDataReplyCallbackArgs(UUID requestID, UUID groupID, java.util.HashMap<UUID, GroupRole> roles)
		{
			this.m_RequestID = requestID;
			this.m_GroupID = groupID;
			this.m_Roles = roles;
		}
	}

	// Represents the Role to Member mappings for a group
	public class GroupRolesMembersReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_RequestID;
		private final UUID m_GroupID;
		private final ArrayList<Entry<UUID, UUID>> m_RolesMembers;

		// Get the ID as returned by the request to correlate this result set
		// and the request
		public final UUID getRequestID()
		{
			return m_RequestID;
		}

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the member to roles map
		public final ArrayList<Entry<UUID, UUID>> getRolesMembers()
		{
			return m_RolesMembers;
		}

		/**
		 * Construct a new instance of the GroupRolesMembersReplyCallbackArgs
		 * class
		 * 
		 * @param requestID
		 *            The ID as returned by the request to correlate this result
		 *            set and the request
		 * @param groupID
		 *            The ID of the group
		 * @param rolesMembers
		 *            The member to roles map
		 */
		public GroupRolesMembersReplyCallbackArgs(UUID requestID, UUID groupID,
				ArrayList<Entry<UUID, UUID>> rolesMembers)
		{
			this.m_RequestID = requestID;
			this.m_GroupID = groupID;
			this.m_RolesMembers = rolesMembers;
		}
	}

	// Represents the titles for a group
	public class GroupTitlesReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_RequestID;
		private final UUID m_GroupID;
		private final HashMap<UUID, GroupTitle> m_Titles;

		// Get the ID as returned by the request to correlate this result set
		// and the request
		public final UUID getRequestID()
		{
			return m_RequestID;
		}

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the titles
		public final HashMap<UUID, GroupTitle> getTitles()
		{
			return m_Titles;
		}

		/**
		 * Construct a new instance of the GroupTitlesReplyCallbackArgs class
		 * 
		 * @param requestID
		 *            The ID as returned by the request to correlate this result
		 *            set and the request
		 * @param groupID
		 *            The ID of the group
		 * @param titles
		 *            The titles
		 */
		public GroupTitlesReplyCallbackArgs(UUID requestID, UUID groupID, HashMap<UUID, GroupTitle> titles)
		{
			this.m_RequestID = requestID;
			this.m_GroupID = groupID;
			this.m_Titles = titles;
		}
	}

	// Represents the summary data for a group
	public class GroupAccountSummaryReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;
		private final GroupAccountSummary m_Summary;

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the summary data
		public final GroupAccountSummary getSummary()
		{
			return m_Summary;
		}

		/**
		 * Construct a new instance of the GroupAccountSummaryReplyCallbackArgs
		 * class
		 * 
		 * @param groupID
		 *            The ID of the group
		 * @param summary
		 *            The summary data
		 */
		public GroupAccountSummaryReplyCallbackArgs(UUID groupID, GroupAccountSummary summary)
		{
			this.m_GroupID = groupID;
			this.m_Summary = summary;
		}
	}

	// A response to a group create request
	public class GroupCreatedReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;
		private final boolean m_Success;
		private final String m_Message;

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// true of the group was created successfully
		public final boolean getSuccess()
		{
			return m_Success;
		}

		// A string containing the message
		public final String getMessage()
		{
			return m_Message;
		}

		/**
		 * Construct a new instance of the GroupCreatedReplyCallbackArgs class
		 * 
		 * @param groupID
		 *            The ID of the group
		 * @param success
		 *            the success or failure of the request
		 * @param messsage
		 *            A string containing additional information
		 */
		public GroupCreatedReplyCallbackArgs(UUID groupID, boolean success, String messsage)
		{
			this.m_GroupID = groupID;
			this.m_Success = success;
			this.m_Message = messsage;
		}
	}

	// Represents a response to a request
	public class GroupOperationCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;
		private final boolean m_Success;

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// true of the request was successful
		public final boolean getSuccess()
		{
			return m_Success;
		}

		/**
		 * Construct a new instance of the GroupOperationCallbackArgs class
		 * 
		 * @param groupID
		 *            The ID of the group
		 * @param success
		 *            true of the request was successful
		 */
		public GroupOperationCallbackArgs(UUID groupID, boolean success)
		{
			this.m_GroupID = groupID;
			this.m_Success = success;
		}
	}

	// Represents your agent leaving a group
	public class GroupDroppedCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		/**
		 * Construct a new instance of the GroupDroppedCallbackArgs class
		 * 
		 * @param groupID
		 *            The ID of the group
		 */
		public GroupDroppedCallbackArgs(UUID groupID)
		{
			m_GroupID = groupID;
		}
	}

	// Represents a list of active group notices
	public class GroupNoticesListReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;
		private final ArrayList<GroupNoticesListEntry> m_Notices;

		// Get the ID of the group
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// Get the notices list
		public final ArrayList<GroupNoticesListEntry> getNotices()
		{
			return m_Notices;
		}

		/**
		 * Construct a new instance of the GroupNoticesListReplyCallbackArgs
		 * class
		 * 
		 * @param groupID
		 *            The ID of the group
		 * @param notices
		 *            The list containing active notices
		 */
		public GroupNoticesListReplyCallbackArgs(UUID groupID, ArrayList<GroupNoticesListEntry> notices)
		{
			m_GroupID = groupID;
			m_Notices = notices;
		}
	}

	// Represents the profile of a group
	public class GroupProfileCallbackArgs implements CallbackArgs
	{
		private final Group m_Group;

		// Get the group profile
		public final Group getGroup()
		{
			return m_Group;
		}

		/**
		 * Construct a new instance of the GroupProfileCallbackArgs class
		 * 
		 * @param group
		 *            The group profile
		 */
		public GroupProfileCallbackArgs(Group group)
		{
			this.m_Group = group;
		}
	}

	/**
	 * Provides notification of a group invitation request sent by another
	 * Avatar
	 * 
	 * The <see cref="GroupInvitation"/> invitation is raised when another
	 * avatar makes an offer for our avatar to join a group.
	 */
	public class GroupInvitationCallbackArgs implements CallbackArgs
	{
		private final UUID m_GroupID;
		private final String m_FromName;
		private final Simulator m_Simulator;
		private final int m_Fee;
		private String m_Message;
		private boolean m_Accept;

		// The ID of the Avatar sending the group invitation
		public final UUID getGroupID()
		{
			return m_GroupID;
		}

		// The name of the Avatar sending the group invitation
		public final String getFromName()
		{
			return m_FromName;
		}

		// The fee joining this group costs
		public final int getFee()
		{
			return m_Fee;
		}

		// A message containing the request information which includes the name
		// of the group, the groups charter and the fee to join details
		public final String getMessage()
		{
			return m_Message;
		}

		public final void setMessage(String message)
		{
			m_Message = message;
		}

		// The Simulator
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Set to true to accept invitation, false to decline
		public final boolean getAccept()
		{
			return m_Accept;
		}

		public final void setAccept(boolean value)
		{
			m_Accept = value;
		}

		public GroupInvitationCallbackArgs(Simulator simulator, UUID groupID, String fromName, String message, int fee)
		{
			this.m_Simulator = simulator;
			this.m_GroupID = groupID;
			this.m_FromName = fromName;
			this.m_Message = message;
			this.m_Fee = fee;
		}
	}
	// #endregion CallbackArgs

}
