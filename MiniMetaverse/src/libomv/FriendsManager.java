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

import java.util.ArrayList;
import java.util.HashMap;

import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.InstantMessageDialog;
import libomv.AgentManager.InstantMessageOnline;
import libomv.LoginManager.BuddyListEntry;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.assets.AssetItem.AssetType;
import libomv.inventory.InventoryException;
import libomv.packets.AcceptFriendshipPacket;
import libomv.packets.ChangeUserRightsPacket;
import libomv.packets.DeclineFriendshipPacket;
import libomv.packets.FindAgentPacket;
import libomv.packets.GenericMessagePacket;
import libomv.packets.GrantUserRightsPacket;
import libomv.packets.OfflineNotificationPacket;
import libomv.packets.OnlineNotificationPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.TerminateFriendshipPacket;
import libomv.packets.TrackAgentPacket;
import libomv.packets.UUIDNameReplyPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * This class is used to add and remove avatars from your friends list and to
 * manage their permission.
 */
public class FriendsManager implements PacketCallback
{
	public static class FriendRights
	{
		/** The avatar has no rights */
		public static final byte None = 0;
		/** The avatar can see the online status of the target avatar */
		public static final byte CanSeeOnline = 1;
		/** The avatar can see the location of the target avatar on the map */
		public static final byte CanSeeOnMap = 2;
		/** The avatar can modify the ojects of the target avatar */
		public static final byte CanModifyObjects = 4;

		private static final String[] _names = new String[] { "None", "SeeOnline", "SeeOnMap", "ModifyObjects" };

		public static String toString(byte value)
		{
			if ((value & _mask) == 0)
				return _names[0];

			String rights = "";
			for (int i = 1; i < _names.length; i++)
			{
				if ((value & (1 << (i - 1))) != 0)
				{
					rights.concat(_names[i] + ", ");
				}
			}
			return rights.substring(0, rights.length() - 2);
		}

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(int value)
		{
			return value;
		}

		private static final byte _mask = 0x7;
	}

	/**
	 * This class holds information about an avatar in the friends list. There
	 * are two ways to interface to this class. The first is through the set of
	 * boolean properties. This is the typical way clients of this class will
	 * use it. The second interface is through two bitflag properties,
	 * TheirFriendsRights and MyFriendsRights
	 */
	public class FriendInfo
	{
		private UUID ID;
		private String name;
		private boolean isOnline;
		private byte myRights;
		private byte theirRights;

		/* System ID of the avatar */
		public final UUID getID()
		{
			return ID;
		}

		/* full name of the avatar */
		public final String getName()
		{
			return name;
		}

		public final void setName(String name)
		{
			this.name = name;
		}

		/* True if the avatar is online */
		public final boolean getIsOnline()
		{
			return isOnline;
		}

		public final void setIsOnline(boolean value)
		{
			isOnline = value;
		}

		/* True if the friend can see if I am online */
		public final boolean getCanSeeMeOnline()
		{
			return (myRights & FriendRights.CanSeeOnline) != 0;
		}

		public final void setCanSeeMeOnline(boolean value)
		{
			if (value)
			{
				myRights |= FriendRights.CanSeeOnline;
			}
			else
			{
				// if they can't see me online, then they also can't see me on
				// the map
				myRights &= ~(FriendRights.CanSeeOnline | FriendRights.CanSeeOnMap);
			}
		}

		/* True if the friend can see me on the map */
		public final boolean getCanSeeMeOnMap()
		{
			return (myRights & FriendRights.CanSeeOnMap) != 0;
		}

		public final void setCanSeeMeOnMap(boolean value)
		{
			if (value)
				myRights |= FriendRights.CanSeeOnMap;
			else
				myRights &= ~FriendRights.CanSeeOnMap;

		}

		/* True if the friend can modify my objects */
		public final boolean getCanModifyMyObjects()
		{
			return (myRights & FriendRights.CanModifyObjects) != 0;
		}

		public final void setCanModifyMyObjects(boolean value)
		{
			if (value)
				myRights |= FriendRights.CanModifyObjects;
			else
				myRights &= ~FriendRights.CanModifyObjects;
		}

		/* True if I can see if my friend is online */
		public final boolean getCanSeeThemOnline()
		{
			return (theirRights & FriendRights.CanSeeOnline) != 0;
		}

		/* True if I can see if my friend is on the map */
		public final boolean getCanSeeThemOnMap()
		{
			return (theirRights & FriendRights.CanSeeOnMap) != 0;
		}

		/* True if I can modify my friend's objects */
		public final boolean getCanModifyTheirObjects()
		{
			return (theirRights & FriendRights.CanSeeOnline) != 0;
		}

		/**
		 * Used internally when building the initial list of friends at login
		 * time
		 * 
		 * @param id
		 *            System ID of the avatar being prepesented
		 * @param buddy_rights_given
		 *            Rights the friend has to see you online and to modify your
		 *            objects
		 * @param buddy_rights_has
		 *            Rights you have to see your friend online and to modify
		 *            their objects
		 */
		public FriendInfo(UUID id, int buddy_rights_given, int buddy_rights_has)
		{
			ID = id;
			this.theirRights = (byte) buddy_rights_given;
			this.myRights = (byte) buddy_rights_has;
		}

		public boolean equals(FriendInfo o)
		{
			return ID.equals(o.getID());
		}

		@Override
		public boolean equals(Object o)
		{
			return (o != null && o instanceof FriendInfo) ? equals((FriendInfo) o) : false;
		}

		@Override
		public int hashCode()
		{
			return ID.hashCode();
		}

		/**
		 * FriendInfo represented as a string
		 * 
		 * @return A string reprentation of both my rights and my friends rights
		 */
		@Override
		public String toString()
		{
			return String.format("%f (Their Rights: %1x, My Rights: %1x)", getName(),
					FriendRights.toString(theirRights), FriendRights.toString(myRights));
		}
	}

	// #region callback handlers

	// Triggered whenever a friend comes online or goes offline
	public class FriendNotificationCallbackArgs implements CallbackArgs
	{
		private final UUID[] agentIDs;
		private final boolean online;

		public UUID[] getAgentID()
		{
			return agentIDs;
		}

		public boolean getOnline()
		{
			return online;
		}

		public FriendNotificationCallbackArgs(UUID[] agentIDs, boolean online)
		{
			this.agentIDs = agentIDs;
			this.online = online;
		}
	}

	public abstract class FriendNotificationCallback implements Callback<FriendNotificationCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendNotificationCallbackArgs params);
	}

	public final CallbackHandler<FriendNotificationCallbackArgs> OnFriendNotification = new CallbackHandler<FriendNotificationCallbackArgs>();

	// Triggered when a friends rights changed
	public class FriendRightsCallbackArgs implements CallbackArgs
	{
		private final FriendInfo friendInfo;

		public FriendInfo getFriendInfo()
		{
			return friendInfo;
		}

		public FriendRightsCallbackArgs(FriendInfo friendInfo)
		{
			this.friendInfo = friendInfo;
		}
	}

	public abstract class FriendRightsCallback implements Callback<FriendRightsCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendRightsCallbackArgs params);
	}

	public final CallbackHandler<FriendRightsCallbackArgs> OnFriendRights = new CallbackHandler<FriendRightsCallbackArgs>();

	// Triggered when a map request for a friend is answered
	public class FriendFoundReplyCallbackArgs implements CallbackArgs
	{
		private final UUID preyID;
		private final long regionHandle;
		private final Vector3 vector3;

		public UUID getPreyID()
		{
			return preyID;
		}

		public long getRegionHandle()
		{
			return regionHandle;
		}

		public Vector3 getVector3()
		{
			return vector3;
		}

		public FriendFoundReplyCallbackArgs(UUID preyID, long regionHandle, Vector3 vector3)
		{
			this.preyID = preyID;
			this.regionHandle = regionHandle;
			this.vector3 = vector3;
		}
	}

	public abstract class FriendFoundReplyCallback implements Callback<FriendFoundReplyCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendFoundReplyCallbackArgs params);
	}

	public CallbackHandler<FriendFoundReplyCallbackArgs> OnFriendFoundReply = new CallbackHandler<FriendFoundReplyCallbackArgs>();

	/* Triggered when friend rights packet is received */
	public class FriendshipOfferedCallbackArgs implements CallbackArgs
	{
		private final UUID friendID;
		private final String name;
		private final UUID sessionID;

		public UUID getFriendID()
		{
			return friendID;
		}

		public String getName()
		{
			return name;
		}

		public UUID getSessionID()
		{
			return sessionID;
		}

		public FriendshipOfferedCallbackArgs(UUID friendID, String name, UUID sessionID)
		{
			this.friendID = friendID;
			this.name = name;
			this.sessionID = sessionID;
		}
	}

	public abstract class FriendshipOfferedCallback implements Callback<FriendshipOfferedCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendshipOfferedCallbackArgs params);
	}

	public CallbackHandler<FriendshipOfferedCallbackArgs> OnFriendshipOffered = new CallbackHandler<FriendshipOfferedCallbackArgs>();

	/* Triggered when friend rights packet is received */
	public class FriendshipResponseCallbackArgs implements CallbackArgs
	{
		private final UUID agentID;
		private final String name;
		private final boolean accepted;

		public UUID getAgentID()
		{
			return agentID;
		}

		public String getName()
		{
			return name;
		}

		public boolean getAccepted()
		{
			return accepted;
		}

		public FriendshipResponseCallbackArgs(UUID agentID, String name, boolean accepted)
		{
			this.agentID = agentID;
			this.name = name;
			this.accepted = accepted;
		}
	}

	public abstract class FriendshipResponseCallback implements Callback<FriendshipResponseCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendshipResponseCallbackArgs params);
	}

	public CallbackHandler<FriendshipResponseCallbackArgs> OnFriendshipResponse = new CallbackHandler<FriendshipResponseCallbackArgs>();

	/* Triggered when friend rights packet is received */
	public class FriendshipTerminatedCallbackArgs implements CallbackArgs
	{
		private final UUID otherID;
		private final String name;

		public UUID getOtherID()
		{
			return otherID;
		}

		public String getName()
		{
			return name;
		}

		public FriendshipTerminatedCallbackArgs(UUID otherID, String name)
		{
			this.otherID = otherID;
			this.name = name;
		}
	}

	public abstract class FriendshipTerminatedCallback implements Callback<FriendshipTerminatedCallbackArgs>
	{
		@Override
		public abstract boolean callback(FriendshipTerminatedCallbackArgs params);
	}

	public CallbackHandler<FriendshipTerminatedCallbackArgs> OnFriendshipTerminated = new CallbackHandler<FriendshipTerminatedCallbackArgs>();
	// #endregion callback handlers

	private GridClient _Client;

	/**
	 * A dictionary of key/value pairs containing known friends of this avatar.
	 * 
	 * The Key is the {@link UUID} of the friend, the value is a
	 * {@link FriendInfo} object that contains detailed information including
	 * permissions you have and have given to the friend
	 */
	private HashMap<UUID, FriendInfo> _FriendList = new HashMap<UUID, FriendInfo>();

	public HashMap<UUID, FriendInfo> getFriendList()
	{
		return _FriendList;
	}
	
	/**
	 * A Dictionary of key/value pairs containing current pending friendship
	 * offers.
	 * 
	 * The key is the {@link UUID} of the avatar making the request, the value
	 * is the {@link UUID} of the request which is used to accept or decline the
	 * friendship offer
	 */
	private HashMap<UUID, UUID> _FriendRequests = new HashMap<UUID, UUID>();

	/**
	 * Internal constructor
	 * 
	 * @param client
	 *            A reference to the ClientManager Object
	 */
	public FriendsManager(GridClient client)
	{
		_Client = client;

		_Client.Self.OnInstantMessage.add(new Self_OnInstantMessage());

		_Client.Login.RegisterLoginProgressCallback(new Network_OnConnect(), new String[] { "buddy-list" }, false);

		_Client.Network.RegisterCallback(PacketType.OnlineNotification, this);
		_Client.Network.RegisterCallback(PacketType.OfflineNotification, this);
		_Client.Network.RegisterCallback(PacketType.ChangeUserRights, this);
		_Client.Network.RegisterCallback(PacketType.TerminateFriendship, this);
		_Client.Network.RegisterCallback(PacketType.FindAgent, this);
		_Client.Network.RegisterCallback(PacketType.UUIDNameReply, this);

	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case OnlineNotification:
			case OfflineNotification:
				FriendNotificationHandler(packet, simulator);
				break;
			case ChangeUserRights:
				ChangeUserRightsHandler(packet, simulator);
				break;
			case TerminateFriendship:
				TerminateFriendshipHandler(packet, simulator);
				break;
			case FindAgent:
				OnFindAgentReplyHandler(packet, simulator);
				break;
			case UUIDNameReply:
				UUIDNameReplyHandler(packet, simulator);
				break;
		}
	}

	/**
	 * Accept a friendship request
	 * 
	 * @param fromAgentID
	 *            agentID of avatatar to form friendship with
	 * @param imSessionID
	 *            imSessionID of the friendship request message
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void AcceptFriendship(UUID fromAgentID, UUID imSessionID) throws Exception, InventoryException
	{
		if (_Client.Inventory == null)
			throw new InventoryException(
					"Inventory not instantiated. Need to lookup CallingCard folder in oreder to accept a friendship request.");

		UUID callingCardFolder = _Client.Inventory.FindFolderForType(AssetType.CallingCard).itemID;

		AcceptFriendshipPacket request = new AcceptFriendshipPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.TransactionID = imSessionID;
		request.FolderID = new UUID[1];
		request.FolderID[0] = callingCardFolder;

		_Client.Network.SendPacket(request);

		FriendInfo friend = new FriendInfo(fromAgentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);

		synchronized (_FriendList)
		{
			if (!_FriendList.containsKey(fromAgentID))
			{
				_FriendList.put(friend.getID(), friend);
			}
		}
		_FriendRequests.remove(fromAgentID);

		_Client.Avatars.RequestAvatarName(fromAgentID, null);
	}

	/**
	 * Decline a friendship request
	 * 
	 * @param fromAgentID
	 *            {@link UUID} of friend
	 * @param imSessionID
	 *            imSessionID of the friendship request message
	 * @throws Exception
	 */
	public final void DeclineFriendship(UUID fromAgentID, UUID imSessionID) throws Exception
	{
		DeclineFriendshipPacket request = new DeclineFriendshipPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.TransactionID = imSessionID;
		_Client.Network.SendPacket(request);

		synchronized (_FriendRequests)
		{
			_FriendRequests.remove(fromAgentID);
		}
	}

	/**
	 * Overload: Offer friendship to an avatar.
	 * 
	 * @param agentID
	 *            System ID of the avatar you are offering friendship to
	 * @throws Exception
	 */
	public final void OfferFriendship(UUID agentID) throws Exception
	{
		OfferFriendship(agentID, "Do you want to be my friend?");
	}

	/**
	 * Offer friendship to an avatar.
	 * 
	 * @param agentID
	 *            System ID of the avatar you are offering friendship to
	 * @param message
	 *            A message to send with the request
	 * @throws Exception
	 */
	public final void OfferFriendship(UUID agentID, String message) throws Exception
	{
		UUID folderID = _Client.Inventory.FindFolderForType(AssetType.CallingCard).itemID;
		_Client.Self.InstantMessage(_Client.Self.getName(), agentID, message, folderID,
				InstantMessageDialog.FriendshipOffered, InstantMessageOnline.Online);
	}

	/**
	 * Terminate a friendship with an avatar
	 * 
	 * @param agentID
	 *            System ID of the avatar you are terminating the friendship
	 *            with
	 * @throws Exception
	 */
	public final void TerminateFriendship(UUID agentID) throws Exception
	{
		FriendInfo friend;
		
		synchronized (_FriendList)
		{
			friend = _FriendList.remove(agentID);
		}
		
		if (friend != null)
		{
			TerminateFriendshipPacket request = new TerminateFriendshipPacket();
			request.AgentData.AgentID = _Client.Self.getAgentID();
			request.AgentData.SessionID = _Client.Self.getSessionID();
			request.OtherID = agentID;

			_Client.Network.SendPacket(request);
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
	private void TerminateFriendshipHandler(Packet packet, Simulator simulator)
	{
		TerminateFriendshipPacket itsOver = (TerminateFriendshipPacket) packet;
		FriendInfo friend;
		
		synchronized (_FriendList)
		{
			friend = _FriendList.remove(itsOver.OtherID);
		}

		OnFriendshipTerminated.dispatch(new FriendshipTerminatedCallbackArgs(itsOver.OtherID,
				friend != null ? friend.getName() : null));
	}

	/**
	 * Change the rights of a friend avatar.
	 * 
	 * @param friendID
	 *            the {@link UUID} of the friend
	 * @param rights
	 *            the new rights to give the friend
	 * @throws Exception
	 * 
	 *             This method will implicitly set the rights to those passed in
	 *             the rights parameter.
	 */
	public final void GrantRights(UUID friendID, byte rights) throws Exception
	{
		GrantUserRightsPacket request = new GrantUserRightsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Rights = new GrantUserRightsPacket.RightsBlock[1];
		request.Rights[0] = request.new RightsBlock();
		request.Rights[0].AgentRelated = friendID;
		request.Rights[0].RelatedRights = rights;

		_Client.Network.SendPacket(request);
	}

	/**
	 * Use to map a friends location on the grid.
	 * 
	 * @param friendID
	 *            Friends UUID to find
	 * @throws Exception
	 * 
	 *             {@link E:OnFriendFound}
	 */
	public final void MapFriend(UUID friendID) throws Exception
	{
		FindAgentPacket stalk = new FindAgentPacket();
		stalk.AgentBlock.Hunter = _Client.Self.getAgentID();
		stalk.AgentBlock.Prey = friendID;
		stalk.AgentBlock.SpaceIP = 0; // Will be filled in by the simulator
		stalk.LocationBlock = new FindAgentPacket.LocationBlockBlock[1];
		stalk.LocationBlock[0] = stalk.new LocationBlockBlock();
		stalk.LocationBlock[0].GlobalX = 0.0; // Filled in by the simulator
		stalk.LocationBlock[0].GlobalY = 0.0;

		_Client.Network.SendPacket(stalk);
	}

	/**
	 * Use to track a friends movement on the grid
	 * 
	 * @param friendID
	 *            Friends Key
	 * @throws Exception
	 */
	public final void TrackFriend(UUID friendID) throws Exception
	{
		TrackAgentPacket stalk = new TrackAgentPacket();
		stalk.AgentData.AgentID = _Client.Self.getAgentID();
		stalk.AgentData.SessionID = _Client.Self.getSessionID();
		stalk.PreyID = friendID;

		_Client.Network.SendPacket(stalk);
	}

	/**
	 * Ask for a notification of friend's online status
	 * 
	 * @param friendID
	 *            Friend's UUID
	 * @throws Exception
	 */
	public final void RequestOnlineNotification(UUID friendID) throws Exception
	{
		GenericMessagePacket gmp = new GenericMessagePacket();
		gmp.AgentData.AgentID = _Client.Self.getAgentID();
		gmp.AgentData.SessionID = _Client.Self.getSessionID();
		gmp.AgentData.TransactionID = UUID.Zero;

		gmp.MethodData.setMethod(Helpers.StringToBytes("requestonlinenotification"));
		gmp.MethodData.Invoice = UUID.Zero;
		gmp.ParamList = new GenericMessagePacket.ParamListBlock[1];
		gmp.ParamList[0] = gmp.new ParamListBlock();
		gmp.ParamList[0].setParameter(Helpers.StringToBytes(friendID.toString()));

		_Client.Network.SendPacket(gmp);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the even the packet data is
	 */
	private void FriendNotificationHandler(Packet packet, Simulator simulator) throws Exception
	{
		ArrayList<UUID> requestids = new ArrayList<UUID>();
		FriendInfo friend = null;
		UUID[] agentIDs = null;
		boolean doNotify = false;

		if (packet.getType() == PacketType.OnlineNotification)
		{
			OnlineNotificationPacket notification = (OnlineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID)
			{
				synchronized (_FriendList)
				{
					if (!_FriendList.containsKey(agentID))
					{
						// Mark this friend for a name request
						requestids.add(agentID);
						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						_FriendList.put(agentID, friend);
					}
					else
					{
						friend = _FriendList.get(agentID);
					}
				}
				doNotify |= !friend.getIsOnline();
				friend.setIsOnline(true);
			}
		}
		else if (packet.getType() == PacketType.OfflineNotification)
		{
			OfflineNotificationPacket notification = (OfflineNotificationPacket) packet;
			agentIDs = notification.AgentID;
			for (UUID agentID : notification.AgentID)
			{
				synchronized (_FriendList)
				{
					if (!_FriendList.containsKey(agentID))
					{
						// Mark this friend for a name request
						requestids.add(agentID);

						friend = new FriendInfo(agentID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
						_FriendList.put(agentID, friend);
					}
					else
					{
						friend = _FriendList.get(agentID);
					}
				}
				doNotify |= friend.getIsOnline();
				friend.setIsOnline(false);
			}
		}

		// Only notify when there was a change in online status
		if (doNotify)
			OnFriendNotification.dispatch(new FriendNotificationCallbackArgs(agentIDs, packet.getType() == PacketType.OnlineNotification));

		if (requestids.size() > 0)
		{
			_Client.Avatars.RequestAvatarNames(requestids, null);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the even the packet data is
	 */
	private void ChangeUserRightsHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (packet.getType() == PacketType.ChangeUserRights)
		{
			FriendInfo friend;
			ChangeUserRightsPacket rights = (ChangeUserRightsPacket) packet;

			synchronized (_FriendList)
			{
				for (ChangeUserRightsPacket.RightsBlock block : rights.Rights)
				{
					if (_FriendList.containsKey(block.AgentRelated))
					{
						friend = _FriendList.get(block.AgentRelated);
						friend.theirRights = FriendRights.setValue(block.RelatedRights);

						OnFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
					}
					else if (block.AgentRelated.equals(_Client.Self.getAgentID()))
					{
						if (_FriendList.containsKey(rights.AgentID))
						{
							friend = _FriendList.get(rights.AgentID);
							friend.myRights = FriendRights.setValue(block.RelatedRights);

							OnFriendRights.dispatch(new FriendRightsCallbackArgs(friend));
						}
					}
				}
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The received packet data
	 * @param simulator
	 *            The simulator for which the packet data is
	 */
	private void OnFindAgentReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnFriendFoundReply.count() > 0)
		{
			FindAgentPacket reply = (FindAgentPacket) packet;

			UUID prey = reply.AgentBlock.Prey;
			float values[] = new float[2];
			long regionHandle = Helpers.GlobalPosToRegionHandle((float) reply.LocationBlock[0].GlobalX,
					(float) reply.LocationBlock[0].GlobalY, values);

			OnFriendFoundReply.dispatch(new FriendFoundReplyCallbackArgs(prey, regionHandle, new Vector3(values[0],
					values[1], 0f)));
		}
	}

	/**
	 * Process an incoming UUIDNameReply Packet and insert Full Names into the
	 * FriendList Dictionary
	 * 
	 * @param packet
	 *            Incoming Packet to process</param>
	 * @param simulator
	 *            Unused
	 */
	private void UUIDNameReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (_FriendList)
		{
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock)
			{
				FriendInfo friend;

				if (!_FriendList.containsKey(block.ID))
				{
					friend = new FriendInfo(block.ID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
					_FriendList.put(block.ID, friend);
				}
				else
				{
					friend = _FriendList.get(block.ID);
				}
				friend.setName(Helpers.BytesToString(block.getFirstName()) + " "
						+ Helpers.BytesToString(block.getLastName()));
			}
		}
	}

	private class Self_OnInstantMessage implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(InstantMessageCallbackArgs e)
		{
			UUID friendID = e.getIM().FromAgentID;
			String name = e.getIM().FromAgentName;

			switch (e.getIM().Dialog)
			{
				case FriendshipOffered:
					UUID sessionID = e.getIM().IMSessionID;
					synchronized (_FriendRequests)
					{
						_FriendRequests.put(friendID, sessionID);
					}
					OnFriendshipOffered.dispatch(new FriendshipOfferedCallbackArgs(friendID, name, sessionID));
					break;
				case FriendshipAccepted:
					FriendInfo friend = new FriendInfo(friendID, FriendRights.CanSeeOnline, FriendRights.CanSeeOnline);
					friend.setName(name);
					synchronized (_FriendList)
					{
						_FriendList.put(friendID, friend);
					}
					OnFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, true));
					try
					{
						RequestOnlineNotification(friendID);
					}
					catch (Exception ex)
					{
						Logger.Log("Error requesting online notification", LogLevel.Error, _Client, ex);
					}
					break;
				case FriendshipDeclined:
					OnFriendshipResponse.dispatch(new FriendshipResponseCallbackArgs(friendID, name, false));
			}
			return false;
		}
	}

	/**
	 * Populate FriendList {@link InternalDictionary} with data from the login
	 * reply
	 */
	private class Network_OnConnect implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				if (e.getReply().BuddyList != null)
				{
					synchronized (_FriendList)
					{
						for (BuddyListEntry buddy : e.getReply().BuddyList)
						{
							UUID bubid = UUID.Parse(buddy.buddy_id);
							if (!_FriendList.containsKey(bubid))
							{
								_FriendList.put(bubid, new FriendInfo(bubid, buddy.buddy_rights_given,
										buddy.buddy_rights_has));
							}
						}
					}
				}
				ArrayList<UUID> request = new ArrayList<UUID>();

				synchronized (_FriendList)
				{
					if (_FriendList.size() > 0)
					{
						for (FriendInfo kvp : _FriendList.values())
						{
							if (kvp.getName() == null || kvp.getName().isEmpty())
							{
								request.add(kvp.getID());
							}
						}
					}
				}
				
				if (request.size() > 0)
				{
					try
					{
						_Client.Avatars.RequestAvatarNames(request, null);
					}
					catch (Exception e1)
					{
					}
				}
			}
			return false;
		}
	}
}
