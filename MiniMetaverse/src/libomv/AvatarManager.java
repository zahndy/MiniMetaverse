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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import libomv.AgentManager.EffectType;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.AgentGroupDataUpdateMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.DisplayNameUpdateMessage;
import libomv.capabilities.IMessage;
import libomv.packets.AvatarAnimationPacket;
import libomv.packets.AvatarAppearancePacket;
import libomv.packets.AvatarGroupsReplyPacket;
import libomv.packets.AvatarInterestsReplyPacket;
import libomv.packets.AvatarPickerReplyPacket;
import libomv.packets.AvatarPropertiesReplyPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.UUIDNameReplyPacket;
import libomv.packets.UUIDNameRequestPacket;
import libomv.packets.ViewerEffectPacket;
import libomv.primitives.Avatar;
import libomv.primitives.Avatar.ProfileFlags;
import libomv.primitives.TextureEntry;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class AvatarManager implements PacketCallback, CapsCallback
{
	// Information about agents display name
	public class AgentDisplayName
	{
		// Agent UUID
		public UUID ID;
		// Username
		public String UserName;
		// Display name
		public String DisplayName;
		// First name (legacy)
		public String LegacyFirstName;
		// Last name (legacy)
		public String LegacyLastName;
		// Full name (legacy)
		public String LegacyFullName;
		// Is display name default display name </summary>
		public boolean IsDefaultDisplayName;
		// Cache display name until
		public Date NextUpdate;

		public String getLegacyFullName()
		{
			return String.format("%s %s", LegacyFirstName, LegacyLastName);
		}

		/**
		 * Creates AgentDisplayName object from OSD
		 * 
		 * @param data
		 *            Incoming OSD data AgentDisplayName object
		 */
		public AgentDisplayName FromOSD(OSD data)
		{
			AgentDisplayName ret = new AgentDisplayName();

			OSDMap map = (OSDMap) data;
			ret.ID = map.get("id").AsUUID();
			ret.UserName = map.get("username").AsString();
			ret.DisplayName = map.get("display_name").AsString();
			ret.LegacyFirstName = map.get("legacy_first_name").AsString();
			ret.LegacyLastName = map.get("legacy_last_name").AsString();
			ret.IsDefaultDisplayName = map.get("is_display_name_default").AsBoolean();
			ret.NextUpdate = map.get("display_name_next_update").AsDate();

			return ret;
		}

		/**
		 * Return object as OSD map
		 * 
		 * @returns OSD containing agent's display name data
		 */
		public OSD GetOSD()
		{
			OSDMap map = new OSDMap();

			map.put("id", OSD.FromUUID(ID));
			map.put("username", OSD.FromString(UserName));
			map.put("display_name", OSD.FromString(DisplayName));
			map.put("legacy_first_name", OSD.FromString(LegacyFirstName));
			map.put("legacy_last_name", OSD.FromString(LegacyLastName));
			map.put("is_display_name_default", OSD.FromBoolean(IsDefaultDisplayName));
			map.put("display_name_next_update", OSD.FromDate(NextUpdate));

			return map;
		}

		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/**
	 * Contains an animation currently being played by an agent
	 */
	public class Animation
	{
		// The ID of the animation asset
		public UUID AnimationID;
		// A number to indicate start order of currently playing animations
		// On Linden Grids this number is unique per region, with OpenSim it is
		// per client
		public int AnimationSequence;
		//
		public UUID AnimationSourceObjectID;
	}

	/**
	 * Holds group information on an individual profile pick
	 */
	public class ProfilePick
	{
		public UUID PickID;
		public UUID CreatorID;
		public boolean TopPick;
		public UUID ParcelID;
		public String Name;
		public String Desc;
		public UUID SnapshotID;
		public String User;
		public String OriginalName;
		public String SimName;
		public Vector3d PosGlobal;
		public int SortOrder;
		public boolean Enabled;
	}

	public class ClassifiedAd
	{
		public UUID ClassifiedID;
		public int Catagory;
		public UUID ParcelID;
		public int ParentEstate;
		public UUID SnapShotID;
		public Vector3d Position;
		public byte ClassifiedFlags;
		public int Price;
		public String Name;
		public String Desc;
	}

	/**
	 * Holds group information for Avatars such as those you might find in a
	 * profile
	 */
	public final class AvatarGroup
	{
		/* true of Avatar accepts group notices */
		public boolean AcceptNotices;
		/* Groups Key */
		public UUID GroupID;
		/* Texture Key for groups insignia */
		public UUID GroupInsigniaID;
		/* Name of the group */
		public String GroupName;
		/* Powers avatar has in the group */
		public long GroupPowers;
		/* Avatars Currently selected title */
		public String GroupTitle;
		/* true of Avatar has chosen to list this in their profile */
		public boolean ListInProfile;
	}

	private GridClient _Client;

	/* HashMap containing all known avatars to this client */ 
	private HashMap<UUID, Avatar> _Avatars;

	public class AgentNamesCallbackArgs implements CallbackArgs
	{
		private HashMap<UUID, String> names;

		public HashMap<UUID, String> getNames()
		{
			return names;
		}

		public AgentNamesCallbackArgs(HashMap<UUID, String> names)
		{
			this.names = names;
		}
	}

	public CallbackHandler<AgentNamesCallbackArgs> OnAgentNames = new CallbackHandler<AgentNamesCallbackArgs>();

	/**
	 * Event args class for display name notification messages
	 */
	public class DisplayNameUpdateCallbackArgs implements CallbackArgs
	{
		private String oldDisplayName;
		private AgentDisplayName displayName;

		public String getOldDisplayName()
		{
			return oldDisplayName;
		}

		public AgentDisplayName getDisplayName()
		{
			return displayName;
		}

		public DisplayNameUpdateCallbackArgs(String oldDisplayName, AgentDisplayName displayName)
		{
			this.oldDisplayName = oldDisplayName;
			this.displayName = displayName;
		}
	}

	public CallbackHandler<DisplayNameUpdateCallbackArgs> OnDisplayNameUpdate = new CallbackHandler<DisplayNameUpdateCallbackArgs>();


	public class AvatarAnimationCallbackArgs implements CallbackArgs
	{
		private UUID agentID;
		private ArrayList<Animation> animations;

		public UUID getAgentID()
		{
			return agentID;
		}

		public ArrayList<Animation> getAnimations()
		{
			return animations;
		}

		public AvatarAnimationCallbackArgs(UUID agentID, ArrayList<Animation> animations)
		{
			this.agentID = agentID;
			this.animations = animations;
		}
	}

	public CallbackHandler<AvatarAnimationCallbackArgs> OnAvatarAnimation = new CallbackHandler<AvatarAnimationCallbackArgs>();

	
	
	public class AvatarAppearanceCallbackArgs implements  CallbackArgs
	{
		private Simulator simulator;
		private UUID id;
		private boolean isTrial;
		private TextureEntry.TextureEntryFace defaultTexture;
		private TextureEntry.TextureEntryFace[] faceTextures;
		private byte[] parameters;

		public Simulator getSimulator()
		{
			return simulator;
		}
		
		public UUID getId()
		{
			return id;
		}
		
		public boolean getIsTrial()
		{
			return isTrial;
		}
		
		public TextureEntry.TextureEntryFace getDefaultTexture()
		{
			return defaultTexture;
		}
		
		public TextureEntry.TextureEntryFace[] getFaceTextures()
		{
			return faceTextures;
		}
		
		public byte[] getVisualParameters()
		{
			return parameters;
		}
		
		public AvatarAppearanceCallbackArgs(Simulator simulator, UUID id, boolean isTrial,
	    		TextureEntry.TextureEntryFace defaultTexture, TextureEntry.TextureEntryFace[] faceTextures, byte[] parameters)
	    {
	    	this.simulator = simulator;
	    	this.id = id;
	    	this.isTrial = isTrial;
	    	this.defaultTexture = defaultTexture;
	    	this.faceTextures = faceTextures;
	    	this.parameters = parameters;
	    }
	}
	
	public CallbackHandler<AvatarAppearanceCallbackArgs> OnAvatarAppearance = new CallbackHandler<AvatarAppearanceCallbackArgs>();
	
	
	public class AvatarInterestsReplyCallbackArgs implements  CallbackArgs
	{
		private Avatar avatar;
		
		public Avatar getAvatar()
		{
			return avatar;
		}

		public AvatarInterestsReplyCallbackArgs(Avatar avatar)
		{
			this.avatar = avatar;
		}
	}
	
	public CallbackHandler<AvatarInterestsReplyCallbackArgs> OnAvatarInterestsReply = new CallbackHandler<AvatarInterestsReplyCallbackArgs>();

	
	public class AvatarPropertiesReplyCallbackArgs implements  CallbackArgs
	{
		private Avatar avatar;
		
		public Avatar getAvatar()
		{
			return avatar;
		}
		
		public AvatarPropertiesReplyCallbackArgs(Avatar avatar)
		{
			this.avatar = avatar;
		}
	}
	
	public CallbackHandler<AvatarPropertiesReplyCallbackArgs> OnAvatarPropertiesReply = new CallbackHandler<AvatarPropertiesReplyCallbackArgs>();
	
	
	public class AvatarGroupsReplyCallbackArgs implements CallbackArgs
	{
		private UUID avatarID;
		private ArrayList<AvatarGroup> avatarGroups;
		
		public UUID getAvatarID()
		{
			return avatarID;
		}
		
		public ArrayList<AvatarGroup> getAvatarGroups()
		{
			return avatarGroups;
		}
		
		public AvatarGroupsReplyCallbackArgs(UUID avatarID, ArrayList<AvatarGroup> avatarGroups)
		{
			this.avatarID = avatarID;
			this.avatarGroups = avatarGroups;
		}
	}
	
	public CallbackHandler<AvatarGroupsReplyCallbackArgs> OnAvatarGroupsReply = new CallbackHandler<AvatarGroupsReplyCallbackArgs>();


	public class AvatarPickerReplyCallbackArgs implements CallbackArgs
	{
    	private UUID queryID;
        private HashMap<UUID, String> avatars;
        
        public UUID getQueryID()
        {
        	return queryID;
        }

        public HashMap<UUID, String> getAvatars()
        {
        	return avatars;
        }
        
        public AvatarPickerReplyCallbackArgs(UUID queryID, HashMap<UUID, String> avatars)
        {
        	this.queryID = queryID;
        	this.avatars = avatars;
        }
	}
        
	public CallbackHandler<AvatarPickerReplyCallbackArgs> OnAvatarPickerReply = new CallbackHandler<AvatarPickerReplyCallbackArgs>();
	
	public class ViewerEffectCallbackArgs implements CallbackArgs
	{
		private EffectType type;
		private Simulator simulator;
		private UUID sourceAvatar;
		private UUID targetObject;
		private Vector3d targetPos;
		private byte target;
		private float duration;
		private UUID dataID;

		public EffectType getType()
		{
			return type;
		}
		
		public Simulator getSimulator()
		{
			return simulator;
		}
		
		public UUID getSourceAvatar()
		{
			return sourceAvatar;
		}

		public UUID getTargetObject()
		{
			return targetObject;
		}

		public Vector3d getTargetPos()
		{
			return targetPos;
		}

		public byte getTarget()
		{
			return target;
		}

		public float getDuration()
		{
			return duration;
		}

		public UUID getDataID()
		{
			return dataID;
		}

        public ViewerEffectCallbackArgs(EffectType type, Simulator simulator, UUID sourceAvatar, UUID targetObject, Vector3d targetPos,
				                        byte target, float duration, UUID dataID)
		{
			this.type = type;
			this.simulator = simulator;
			this.sourceAvatar = sourceAvatar;
			this.targetObject = targetObject;
			this.targetPos = targetPos;
			this.target = target;
			this.duration = duration;
			this.dataID = dataID;
		}
	}

	public CallbackHandler<ViewerEffectCallbackArgs> OnViewerEffect = new CallbackHandler<ViewerEffectCallbackArgs>();

	
	public AvatarManager(GridClient client)
	{
		_Client = client;
		_Avatars = new HashMap<UUID, Avatar>();

		// Avatar appearance callback
		_Client.Network.RegisterCallback(PacketType.AvatarAppearance, this);

		// Avatar profile callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPropertiesReply, this);
		// Client.Network.RegisterCallback(PacketType.AvatarStatisticsReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarInterestsReply, this);

		// Avatar group callback
		_Client.Network.RegisterCallback(PacketType.AvatarGroupsReply, this);
		_Client.Network.RegisterCallback(CapsEventType.AgentGroupDataUpdate, this);
		_Client.Network.RegisterCallback(CapsEventType.AvatarGroupsReply, this);

		// Viewer effect callback
		_Client.Network.RegisterCallback(PacketType.ViewerEffect, this);

		// Other callbacks
		_Client.Network.RegisterCallback(PacketType.UUIDNameReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarPickerReply, this);
		_Client.Network.RegisterCallback(PacketType.AvatarAnimation, this);
		_Client.Network.RegisterCallback(CapsEventType.DisplayNameUpdate, this);

		// Picks callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarPicksReply, this);
		_Client.Network.RegisterCallback(PacketType.PickInfoReply, this);

		// Classifieds callbacks
		_Client.Network.RegisterCallback(PacketType.AvatarClassifiedReply, this);
		_Client.Network.RegisterCallback(PacketType.ClassifiedInfoReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AvatarAppearance:
				HandleAvatarAppearance(packet, simulator);
				break;
			case AvatarPropertiesReply:
				HandleAvatarProperties(packet, simulator);
				break;
			case AvatarInterestsReply:
				HandleAvatarInterests(packet, simulator);
				break;
			case AvatarGroupsReply:
				HandleAvatarGroupsReply(packet, simulator);
				break;
			case ViewerEffect:
				HandleViewerEffect(packet, simulator);
				break;
			case UUIDNameReply:
				HandleUUIDNameReply(packet, simulator);
				break;
			case AvatarPickerReply:
				HandleAvatarPickerReply(packet, simulator);
				break;
			case AvatarAnimation:
				HandleAvatarAnimation(packet, simulator);
				break;
//			case AvatarPicksReply:
//				// HandleAvatarPicksReply(packet, simulator);
//				break;
//			case PickInfoReply:
//				// HandlePickInfoReply(packet, simulator);
//				break;
//			case AvatarClassifiedReply:
//				// HandleAvatarClassifiedReply(packet, simulator);
//				break;
//			case ClassifiedInfoReply:
//				// HandleClassifiedInfoReply(packet, simulator);
//				break;
			default:
				Logger.Log("AvatarManager: Unhandled packet" + packet.getType().toString(), LogLevel.Warning, _Client);
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator)
	{
		switch (message.getType())
		{
			case DisplayNameUpdate:
				HandleDisplayNameUpdate(message, simulator);
				break;
//			case AgentGroupDataUpdate:
//				// HandleAgentGroupDataUpdate(message, simulator);
//				break;
			case AvatarGroupsReply:
				HandleAvatarGroupsReply(message, simulator);
				break;
			default:
				Logger.Log("AvatarManager: Unhandled message" + message.getType().toString(), LogLevel.Warning, _Client);
		}
	}

	/**
	 * Add an Avatar into the Avatars Dictionary
	 * 
	 * @param avatar
	 *            Filled-out Avatar class to insert
	 */
	public void add(Avatar avatar)
	{ 
		synchronized (_Avatars)
		{
			_Avatars.put(avatar.ID, avatar);
		}
	}

	public boolean contains(UUID id)
	{
		synchronized (_Avatars)
		{
			return _Avatars.containsKey(id);
		}
	}

	/**
	 * This function will only check if the avatar name exists locally, it will
	 * not do any networking calls to fetch the name
	 * 
	 * @param id
	 *            The uuid of the avatar to get the name for
	 * @return The avatar name, or an empty String if it's not found
	 */
	public String LocalAvatarNameLookup(UUID id)
	{
		synchronized (_Avatars)
		{
			Avatar avatar = _Avatars.get(id);
			if (avatar != null)
				return avatar.getName();
		}
		return Helpers.EmptyString;
	}

	/**
	 * Request a name update for an avatar
	 * 
	 * @param id
	 *            The uuid of the avatar to get the name for
	 * @param anc
	 *            A callback being called when a name request is answered
	 * @throws Exception
	 */
	public void RequestAvatarName(UUID id, Callback<AgentNamesCallbackArgs> anc) throws Exception
	{
		// TODO: BeginGetAvatarNames is pretty bulky, rewrite a simple version
		// here
		ArrayList<UUID> ids = new ArrayList<UUID>();
		ids.add(id);

		RequestAvatarNames(ids, anc);
	}

	//
	// <param name="ids"></param>
	public void RequestAvatarNames(ArrayList<UUID> ids, Callback<AgentNamesCallbackArgs> anc) throws Exception
	{
		if (anc != null)
		{
			OnAgentNames.add(anc);
		}

		HashMap<UUID, String> havenames = new HashMap<UUID, String>();
		ArrayList<UUID> neednames = new ArrayList<UUID>();

		synchronized (_Avatars)
		{
			// Fire callbacks for the ones we already have cached
			Iterator<UUID> iter = ids.listIterator();
			while (iter.hasNext())
			{
				UUID id = iter.next();
				if (_Avatars.containsKey(id))
				{
					havenames.put(id, _Avatars.get(id).getName());
				}
				else
				{
					neednames.add(id);
				}
			}
		}
		
		if (havenames.size() > 0)
		{
			OnAgentNames.dispatch(new AgentNamesCallbackArgs(havenames));
		}

		if (neednames.size() > 0)
		{
			UUIDNameRequestPacket request = new UUIDNameRequestPacket();

			request.ID = new UUID[neednames.size()];

			for (int i = 0; i < neednames.size(); i++)
			{
				request.ID[i] = neednames.get(i);
			}
			_Client.Network.SendPacket(request);
		}
		else
		{
			OnAgentNames.remove(anc);			
		}
	}

	/**
	 * Process an incoming UUIDNameReply Packet and insert Full Names into the
	 * 
	 * @param packet
	 *            Incoming Packet to process
	 * @param simulator
	 *            Unused
	 * @throws Exception
	 */
	private void HandleUUIDNameReply(Packet packet, Simulator simulator) throws Exception
	{
		HashMap<UUID, String> names = new HashMap<UUID, String>();
		UUIDNameReplyPacket reply = (UUIDNameReplyPacket) packet;

		synchronized (_Avatars)
		{
			for (UUIDNameReplyPacket.UUIDNameBlockBlock block : reply.UUIDNameBlock)
			{
				if (!_Avatars.containsKey(block.ID))
				{
					_Avatars.put(block.ID, new Avatar(block.ID));
				}

				_Avatars.get(block.ID).setNames(Helpers.BytesToString(block.getFirstName()),
						Helpers.BytesToString(block.getLastName()));
				names.put(block.ID, _Avatars.get(block.ID).getName());
			}
		}
		OnAgentNames.dispatch(new AgentNamesCallbackArgs(names));
	}

    private void HandleAvatarAnimation(Packet packet, Simulator simulator) throws Exception
    {
        if (OnAvatarAnimation.count() > 0)
        {
            AvatarAnimationPacket data = (AvatarAnimationPacket)packet;

            ArrayList<Animation> signaledAnimations = new ArrayList<Animation>(data.AnimationList.length);

            for (int i = 0; i < data.AnimationList.length; i++)
            {
                Animation animation = new Animation();
                animation.AnimationID = data.AnimationList[i].AnimID;
                animation.AnimationSequence = data.AnimationList[i].AnimSequenceID;
                if (i < data.ObjectID.length)
                {
                    animation.AnimationSourceObjectID = data.ObjectID[i];
                }

                signaledAnimations.add(animation);
            }

            OnAvatarAnimation.dispatch(new AvatarAnimationCallbackArgs(data.ID, signaledAnimations));
        }
    }
	    
    private void HandleAvatarAppearance(Packet packet, Simulator simulator) throws Exception
    {
        if (OnAvatarAppearance.count() > 0 || _Client.Settings.AVATAR_TRACKING)
        {
            AvatarAppearancePacket appearance = (AvatarAppearancePacket)packet;

            TextureEntry textureEntry = new TextureEntry(appearance.ObjectData.getTextureEntry());

            TextureEntry.TextureEntryFace defaultTexture = textureEntry.defaultTexture;
            TextureEntry.TextureEntryFace[] faceTextures = textureEntry.faceTextures;

            Avatar av = simulator.findAvatar(appearance.Sender.ID);
           	if (av != null)
        	{
                av.Textures = textureEntry;
                av.VisualParameters = appearance.ParamValue;
            }

            OnAvatarAppearance.dispatch(new AvatarAppearanceCallbackArgs(simulator, appearance.Sender.ID, appearance.Sender.IsTrial,
                defaultTexture, faceTextures, appearance.ParamValue));
        }
    }

    private void HandleAvatarProperties(Packet packet, Simulator simulator) throws Exception
    {
        if (OnAvatarPropertiesReply.count() > 0)
        {
            AvatarPropertiesReplyPacket reply = (AvatarPropertiesReplyPacket)packet;
            Avatar av = simulator.findAvatar(reply.AgentData.AvatarID);
            if (av == null)
            	av = _Avatars.get(reply.AgentData.AvatarID);
            av.ProfileProperties = av.new AvatarProperties();

            av.ProfileProperties.ProfileImage = reply.PropertiesData.ImageID;
            av.ProfileProperties.FirstLifeImage = reply.PropertiesData.FLImageID;
            av.ProfileProperties.Partner = reply.PropertiesData.PartnerID;
            av.ProfileProperties.AboutText = Helpers.BytesToString(reply.PropertiesData.getAboutText());
            av.ProfileProperties.FirstLifeText = Helpers.BytesToString(reply.PropertiesData.getFLAboutText());
            av.ProfileProperties.BornOn = Helpers.BytesToString(reply.PropertiesData.getBornOn());
            long charter = Helpers.BytesToUInt32L(reply.PropertiesData.getCharterMember());
            if (charter == 0)
            {
            	av.ProfileProperties.CharterMember = "Resident";
            }
            else if (charter == 2)
            {
            	av.ProfileProperties.CharterMember = "Charter";
            }
            else if (charter == 3)
            {
            	av.ProfileProperties.CharterMember = "Linden";
            }
            else
            {
            	av.ProfileProperties.CharterMember = Helpers.BytesToString(reply.PropertiesData.getCharterMember());
            }
            av.ProfileProperties.Flags = ProfileFlags.setValue(reply.PropertiesData.Flags);
            av.ProfileProperties.ProfileURL = Helpers.BytesToString(reply.PropertiesData.getProfileURL());

            OnAvatarPropertiesReply.dispatch(new AvatarPropertiesReplyCallbackArgs(av));
        }
    }

    private void HandleAvatarInterests(Packet packet, Simulator simulator) throws Exception
    {
        if (OnAvatarInterestsReply.count() > 0)
        {
            AvatarInterestsReplyPacket airp = (AvatarInterestsReplyPacket)packet;
            Avatar av = simulator.findAvatar(airp.AgentData.AvatarID);
            if (av == null)
            	av = _Avatars.get(airp.AgentData.AvatarID);
            av.ProfileInterests = av.new Interests();

            av.ProfileInterests.WantToMask = airp.PropertiesData.WantToMask;
            av.ProfileInterests.WantToText = Helpers.BytesToString(airp.PropertiesData.getWantToText());
            av.ProfileInterests.SkillsMask = airp.PropertiesData.SkillsMask;
            av.ProfileInterests.SkillsText = Helpers.BytesToString(airp.PropertiesData.getSkillsText());
            av.ProfileInterests.LanguagesText = Helpers.BytesToString(airp.PropertiesData.getLanguagesText());

            OnAvatarInterestsReply.dispatch(new AvatarInterestsReplyCallbackArgs(av));
        }
    }

    /**
	 * EQ Message fired when someone nearby changes their display name
	 */
	private void HandleDisplayNameUpdate(IMessage message, Simulator simulator)
	{
		DisplayNameUpdateMessage msg = (DisplayNameUpdateMessage) message;
		synchronized (_Avatars)
		{
			UUID id = msg.DisplayName.ID;
			if (!_Avatars.containsKey(id))
			{
				_Avatars.put(id, new Avatar(id));
			}
			_Avatars.get(id).setDisplayName(msg.DisplayName.DisplayName);
		}
		OnDisplayNameUpdate.dispatch(new DisplayNameUpdateCallbackArgs(msg.OldDisplayName, msg.DisplayName));
	}

    private void HandleAvatarGroupsReply(IMessage message, Simulator simulator)
    {
        if (OnAvatarGroupsReply.count() > 0)
        {
            AgentGroupDataUpdateMessage msg = (AgentGroupDataUpdateMessage)message;
            ArrayList<AvatarGroup> avatarGroups = new ArrayList<AvatarGroup>(msg.GroupDataBlock.length);
            for (int i = 0; i < msg.GroupDataBlock.length; i++)
            {
                AvatarGroup avatarGroup = new AvatarGroup();
                avatarGroup.AcceptNotices = msg.GroupDataBlock[i].AcceptNotices;
                avatarGroup.GroupID = msg.GroupDataBlock[i].GroupID;
                avatarGroup.GroupInsigniaID = msg.GroupDataBlock[i].GroupInsigniaID;
                avatarGroup.GroupName = msg.GroupDataBlock[i].GroupName;
                avatarGroup.GroupPowers = msg.GroupDataBlock[i].GroupPowers;
                avatarGroup.ListInProfile = msg.NewGroupDataBlock[i].ListInProfile;

                avatarGroups.add(avatarGroup);
            }

            OnAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(msg.AgentID, avatarGroups));
        }
    }

    private void HandleAvatarGroupsReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        if (OnAvatarGroupsReply.count() > 0)
        {
            AvatarGroupsReplyPacket groups = (AvatarGroupsReplyPacket)packet;
            ArrayList<AvatarGroup> avatarGroups = new ArrayList<AvatarGroup>(groups.GroupData.length);

            for (int i = 0; i < groups.GroupData.length; i++)
            {
                AvatarGroup avatarGroup = new AvatarGroup();

                avatarGroup.AcceptNotices = groups.GroupData[i].AcceptNotices;
                avatarGroup.GroupID = groups.GroupData[i].GroupID;
                avatarGroup.GroupInsigniaID = groups.GroupData[i].GroupInsigniaID;
                avatarGroup.GroupName = Helpers.BytesToString(groups.GroupData[i].getGroupName());
                avatarGroup.GroupPowers = groups.GroupData[i].GroupPowers;
                avatarGroup.GroupTitle = Helpers.BytesToString(groups.GroupData[i].getGroupTitle());
                avatarGroup.ListInProfile = groups.ListInProfile;

                avatarGroups.add(avatarGroup);
            }
            OnAvatarGroupsReply.dispatch(new AvatarGroupsReplyCallbackArgs(groups.AgentData.AvatarID, avatarGroups));
        }
    }

    private void HandleAvatarPickerReply(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        if (OnAvatarPickerReply.count() > 0)
        {
            AvatarPickerReplyPacket reply = (AvatarPickerReplyPacket)packet;
            HashMap<UUID, String> avatars = new HashMap<UUID, String>();

            for (AvatarPickerReplyPacket.DataBlock block : reply.Data)
            {
                avatars.put(block.AvatarID, Helpers.BytesToString(block.getFirstName()) +
                    " " + Helpers.BytesToString(block.getLastName()));
            }
            OnAvatarPickerReply.dispatch(new AvatarPickerReplyCallbackArgs(reply.AgentData.QueryID, avatars));
        }
    }

    /**
     * Process an incoming packet and raise the appropriate events</summary>
     */
    private void HandleViewerEffect(Packet packet, Simulator simulator)
    {
        ViewerEffectPacket effect = (ViewerEffectPacket)packet;

        for (ViewerEffectPacket.EffectBlock block : effect.Effect)
        {
            EffectType type = EffectType.setValue(block.Type);

            // Each ViewerEffect type uses it's own custom binary format for additional data. Fun eh?
            switch (type)
            {
                case Beam:
                case Point:
                case Trail:
                case Sphere:
                case Spiral:
                case Edit:
                    if (block.getTypeData().length == 56)
                    {
                        UUID sourceAvatar = new UUID(block.getTypeData(), 0);
                        UUID targetObject = new UUID(block.getTypeData(), 16);
                        Vector3d targetPos = new Vector3d(block.getTypeData(), 32);
                        OnViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject, targetPos, (byte)0, block.Duration, block.ID));
                    }
                    else
                    {
                        Logger.Log("Received a " + type.toString() + " ViewerEffect with an incorrect TypeData size of " +
                                block.getTypeData().length + " bytes", Logger.LogLevel.Warning, _Client);
                    }
                    break;
                case LookAt:
                case PointAt:
                    if (block.getTypeData().length == 57)
                    {
                        UUID sourceAvatar = new UUID(block.getTypeData(), 0);
                        UUID targetObject = new UUID(block.getTypeData(), 16);
                        Vector3d targetPos = new Vector3d(block.getTypeData(), 32);

                        OnViewerEffect.dispatch(new ViewerEffectCallbackArgs(type, simulator, sourceAvatar, targetObject, targetPos, block.getTypeData()[56], block.Duration, block.ID));
                    }
                    else
                    {
                        Logger.Log("Received a LookAt " + type.toString() + " ViewerEffect with an incorrect TypeData size of " +
                                   block.getTypeData().length + " bytes", Logger.LogLevel.Warning, _Client);
                    }
                    break;
                case Text:
                case Icon:
                case Connector:
                case FlexibleObject:
                case AnimalControls:
                case AnimationObject:
                case Cloth:
                case Glow:
                default:
                    Logger.Log("Received a ViewerEffect with an unknown type " + type.toString() + " and length " +
                    		   block.getTypeData().length + " bytes", Logger.LogLevel.Warning, _Client);
                    break;
            }
        }
    }
}
