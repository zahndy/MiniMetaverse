/**
 * Copyright (c) 2006-2009, openmetaverse.org
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
import java.lang.reflect.Modifier;
import java.util.HashMap;

import libomv.packets.AttachedSoundGainChangePacket;
import libomv.packets.AttachedSoundPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PreloadSoundPacket;
import libomv.packets.SoundTriggerPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;

public class SoundManager implements PacketCallback
{
    // pre-defined built in sounds
    // From http://wiki.secondlife.com/wiki/Client_sounds
    public static final class Sounds
    {
        public final static UUID BELL_TING = new UUID("ed124764-705d-d497-167a-182cd9fa2e6c");

        public final static UUID CLICK = new UUID("4c8c3c77-de8d-bde2-b9b8-32635e0fd4a6");

        public final static UUID HEALTH_REDUCTION_FEMALE = new UUID("219c5d93-6c09-31c5-fb3f-c5fe7495c115");

        public final static UUID HEALTH_REDUCTION_MALE = new UUID("e057c244-5768-1056-c37e-1537454eeb62");

        public final static UUID IM_START = new UUID("c825dfbc-9827-7e02-6507-3713d18916c1");

        // 2 bells
        public final static UUID INSTANT_MESSAGE_NOTIFICATION = new UUID("67cc2844-00f3-2b3c-b991-6418d01e1bb7");

        public final static UUID INVALID_OPERATION = new UUID("4174f859-0d3d-c517-c424-72923dc21f65");

        public final static UUID KEYBOARD_LOOP = new UUID("5e191c7b-8996-9ced-a177-b2ac32bfea06");

        // coins
        public final static UUID MONEY_REDUCTION_COINS = new UUID("77a018af-098e-c037-51a6-178f05877c6f");

        // cash register bell
        public final static UUID MONEY_INCREASE_CASH_REGISTER_BELL = new UUID("104974e3-dfda-428b-99ee-b0d4e748d3a3");

        public final static UUID NULL_KEYSTROKE = new UUID("2ca849ba-2885-4bc3-90ef-d4987a5b983a");

        public final static UUID OBJECT_COLLISION = new UUID("be582e5d-b123-41a2-a150-454c39e961c8");

        // rubber
        public final static UUID OBJECT_COLLISION_RUBBER = new UUID("212b6d1e-8d9c-4986-b3aa-f3c6df8d987d");

        // plastic
        public final static UUID OBJECT_COLLISION_PLASTIC = new UUID("d55c7f3c-e1c3-4ddc-9eff-9ef805d9190e");

        // flesh
        public final static UUID OBJECT_COLLISION_FLESH = new UUID("2d8c6f51-149e-4e23-8413-93a379b42b67");

        // wood splintering?
        public final static UUID OBJECT_COLLISION_WOOD_SPLINTERING = new UUID("6f00669f-15e0-4793-a63e-c03f62fee43a");

        // glass break
        public final static UUID OBJECT_COLLISION_GLASS_BREAK = new UUID("85cda060-b393-48e6-81c8-2cfdfb275351");

        // metal clunk
        public final static UUID OBJECT_COLLISION_METAL_CLUNK = new UUID("d1375446-1c4d-470b-9135-30132433b678");

        // whoosh
        public final static UUID OBJECT_CREATE_WHOOSH = new UUID("3c8fc726-1fd6-862d-fa01-16c5b2568db6");

        // shake
        public final static UUID OBJECT_DELETE_SHAKE = new UUID("0cb7b00a-4c10-6948-84de-a93c09af2ba9");

        public final static UUID OBJECT_REZ = new UUID("f4a0660f-5446-dea2-80b7-6482a082803c");

        // ding
        public final static UUID PIE_MENU_APPEAR_DING = new UUID("8eaed61f-92ff-6485-de83-4dcc938a478e");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT = new UUID("d9f73cf8-17b4-6f7a-1565-7951226c305d");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT1 = new UUID("f6ba9816-dcaf-f755-7b67-51b31b6233e5");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT2 = new UUID("7aff2265-d05b-8b72-63c7-dbf96dc2f21f");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT3 = new UUID("09b2184e-8601-44e2-afbb-ce37434b8ba1");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT4 = new UUID("bbe4c7fc-7044-b05e-7b89-36924a67593c");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT5 = new UUID("d166039b-b4f5-c2ec-4911-c85c727b016c");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT6 = new UUID("242af82b-43c2-9a3b-e108-3b0c7e384981");

        public final static UUID PIE_MENU_SLICE_HIGHLIGHT7 = new UUID("c1f334fb-a5be-8fe7-22b3-29631c21cf0b");

        public final static UUID SNAPSHOT = new UUID("3d09f582-3851-c0e0-f5ba-277ac5c73fb4");

        public final static UUID TELEPORT_TEXTURE_APPLY = new UUID("d7a9a565-a013-2a69-797d-5332baa1a947");

        public final static UUID THUNDER = new UUID("e95c96a5-293c-bb7a-57ad-ce2e785ad85f");

        public final static UUID WINDOW_CLOSE = new UUID("2c346eda-b60c-ab33-1119-b8941916a499");

        public final static UUID WINDOW_OPEN = new UUID("c80260ba-41fd-8a46-768a-6bf236360e3a");

        public final static UUID ZIPPER = new UUID("6cf2be26-90cb-2669-a599-f5ab7698225f");


        /**
         * A dictionary containing all pre-defined sounds
         * 
         * @return A dictionary containing the pre-defined sounds, where the key is the sounds ID,
         * and the value is a string containing a name to identify the purpose of the sound
         */
        public static HashMap<UUID, String> toDictionary()
        {
            HashMap<UUID, String> dict = new HashMap<UUID, String>();
            for (Field field : Sounds.class.getDeclaredFields())
            {
            	if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC))
            	{
            		try
					{
						dict.put((UUID)field.get(null), field.getName());
					}
					catch (IllegalArgumentException e) { }
					catch (IllegalAccessException e) { }
            	}
            }
            return dict;
        }
    }

	// #region Private Members
    private final GridClient _Client;
    // #endregion

    // #region Event Callback Handling
    
    /**
     * Provides data for the <see cref="SoundManager.AttachedSound"/> event
     *
     * The <see cref="SoundManager.AttachedSound"/> event occurs when the simulator sends
     * the sound data which emits from an agents attachment
     */
    public class AttachedSoundCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;
        private final float m_Gain;
        private final byte m_Flags;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the volume level
        public final float getGain()
        {
            return m_Gain;
        }
        // Get the <see cref="SoundFlags"/>
        public final byte getFlags()
        {
            return m_Flags;
        }
        
        public AttachedSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, float gain, byte flags)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
            this.m_Gain = gain;
            this.m_Flags = flags;
        }
    }
    
    public CallbackHandler<AttachedSoundCallbackArgs> OnAttachedSound = new CallbackHandler<AttachedSoundCallbackArgs>();

    
    /**
     * Provides data for the <see cref="SoundManager.SoundTrigger"/> event
     * <p>The <see cref="SoundManager.SoundTrigger"/> event occurs when the simulator forwards
     * a request made by yourself or another agent to play either an asset sound or a built in sound</p>
     *
     * <p>Requests to play sounds where the <see cref="SoundTriggerEventArgs.SoundID"/> is not one of the built-in
     * <see cref="Sounds"/> will require sending a request to download the sound asset before it can be played</p>
     */
    public class SoundTriggerCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;
        private final UUID m_ParentID;
        private final float m_Gain;
        private final long m_RegionHandle;
        private final Vector3 m_Position;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the ID of the objects parent
        public final UUID getParentID()
        {
            return m_ParentID;
        }
        // Get the volume level
        public final float getGain()
        {
            return m_Gain;
        }
        // Get the regionhandle
        public final long getRegionHandle()
        {
            return m_RegionHandle;
        }
        // Get the source position
        public final Vector3 getPosition()
        {
            return m_Position;
        }

        /** 
         * Construct a new instance of the SoundTriggerEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param soundID The sound asset id
         * @param ownerID The ID of the owner
         * @param objectID The ID of the object
         * @param parentID The ID of the objects parent
         * @param gain The volume level
         * @param regionHandle The regionhandle
         * @param position The source position
         */
        public SoundTriggerCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID, UUID parentID, float gain, long regionHandle, Vector3 position)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
            this.m_ParentID = parentID;
            this.m_Gain = gain;
            this.m_RegionHandle = regionHandle;
            this.m_Position = position;
        }
    }
    
    public CallbackHandler<SoundTriggerCallbackArgs> OnSoundTrigger = new CallbackHandler<SoundTriggerCallbackArgs>();
    
    
    /**
     * Provides data for the <see cref="SoundManager.AttachedSoundGainChange"/> event
     *
     * The <see cref="SoundManager.AttachedSoundGainChange"/> event occurs when an attached sound
     * changes its volume level
     */
    public class AttachedSoundGainChangeCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_ObjectID;
        private final float m_Gain;

        // Simulator where the event originated
        //Tangible_doc_comment_end
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the ID of the Object
        //Tangible_doc_comment_end
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }
        // Get the volume level
        //Tangible_doc_comment_end
        public final float getGain()
        {
            return m_Gain;
        }

        /** 
         * Construct a new instance of the AttachedSoundGainChangedEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param objectID The ID of the Object
         * @param gain The new volume level
         */
        public AttachedSoundGainChangeCallbackArgs(Simulator sim, UUID objectID, float gain)
        {
            this.m_Simulator = sim;
            this.m_ObjectID = objectID;
            this.m_Gain = gain;
        }
    }
   
    public CallbackHandler<AttachedSoundGainChangeCallbackArgs> OnAttachedSoundGainChange = new CallbackHandler<AttachedSoundGainChangeCallbackArgs>();


    /**
     * Provides data for the <see cref="SoundManager.PreloadSound"/> event
     *
     * The <see cref="SoundManager.PreloadSound"/> event occurs when an attached sound
     * changes its volume level
     */
    public class PreloadSoundCallbackArgs implements CallbackArgs
    {
        private final Simulator m_Simulator;
        private final UUID m_SoundID;
        private final UUID m_OwnerID;
        private final UUID m_ObjectID;

        // Simulator where the event originated
        public final Simulator getSimulator()
        {
            return m_Simulator;
        }
        // Get the sound asset id
        public final UUID getSoundID()
        {
            return m_SoundID;
        }
        // Get the ID of the owner
        public final UUID getOwnerID()
        {
            return m_OwnerID;
        }
        // Get the ID of the Object
        public final UUID getObjectID()
        {
            return m_ObjectID;
        }

        /** 
         * Construct a new instance of the PreloadSoundEventArgs class
         * 
         * @param sim Simulator where the event originated
         * @param soundID The sound asset id
         * @param ownerID The ID of the owner
         * @param objectID The ID of the object
         */
        public PreloadSoundCallbackArgs(Simulator sim, UUID soundID, UUID ownerID, UUID objectID)
        {
            this.m_Simulator = sim;
            this.m_SoundID = soundID;
            this.m_OwnerID = ownerID;
            this.m_ObjectID = objectID;
        }
    }

    public CallbackHandler<PreloadSoundCallbackArgs> OnPreloadSound = new CallbackHandler<PreloadSoundCallbackArgs>();

    // #endregion

    /** 
     * Construct a new instance of the SoundManager class, used for playing and receiving
     * sound assets
     * 
     * @param client A reference to the current GridClient instance
     */
    public SoundManager(GridClient client)
    {
        _Client = client;

        _Client.Network.RegisterCallback(PacketType.AttachedSound, this);
        _Client.Network.RegisterCallback(PacketType.AttachedSoundGainChange, this);
        _Client.Network.RegisterCallback(PacketType.PreloadSound, this);
        _Client.Network.RegisterCallback(PacketType.SoundTrigger, this);
    }

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case AttachedSound:
				HandleAttachedSound(packet, simulator);
				break;
			case AttachedSoundGainChange:
				HandleAttachedSoundGainChange(packet, simulator);
				break;
			case PreloadSound:
				HandlePreloadSound(packet, simulator);
				break;
			case SoundTrigger:
				HandleSoundTrigger(packet, simulator);
				break;
		}
	}

    // #region public methods

    /**
     * Plays a sound in the current region at full volume from avatar position
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @throws Exception 
     */
    public final void PlaySound(UUID soundID) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), _Client.Self.getSimPosition(), 1.0f);
    }

    /**
     * Plays a sound in the current region at full volume
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Vector3 position) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), position, 1.0f);
    }

    /**
     * Plays a sound in the current region
     *
     * @param soundID UUID of the sound to be played
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Vector3 position, float gain) throws Exception
    {
        SendSoundTrigger(soundID, _Client.Network.getCurrentSim().getHandle(), position, gain);
    }
    
    /**
     * Plays a sound in the specified sim
     *
     * @param soundID UUID of the sound to be played
     * @param sim simulator where to play the sound in
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, Simulator sim, Vector3 position, float gain) throws Exception
    {
        SendSoundTrigger(soundID, sim.getHandle(), position, gain);
    }

    /**
     * Play a sound asset
     *
     * @param soundID UUID of the sound to be played
     * @param handle handle id for the sim to be played in
     * @param position position for the sound to be played at. Normally the avatar
     * @param gain volume of the sound, from 0.0 to 1.0
     * @throws Exception 
     */
    public final void SendSoundTrigger(UUID soundID, long handle, Vector3 position, float gain) throws Exception
    {
        SoundTriggerPacket soundtrigger = new SoundTriggerPacket();
        soundtrigger.SoundData = soundtrigger.new SoundDataBlock();
        soundtrigger.SoundData.SoundID = soundID;
        soundtrigger.SoundData.ObjectID = UUID.Zero;
        soundtrigger.SoundData.OwnerID = UUID.Zero;
        soundtrigger.SoundData.ParentID = UUID.Zero;
        soundtrigger.SoundData.Handle = handle;
        soundtrigger.SoundData.Position = position;
        soundtrigger.SoundData.Gain = gain;

        _Client.Network.SendPacket(soundtrigger);
    }
    // #endregion

    // #region Packet Handlers
    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandleAttachedSound(Packet packet, Simulator simulator)
    {
        AttachedSoundPacket sound = (AttachedSoundPacket)packet;

        OnAttachedSound.dispatch(new AttachedSoundCallbackArgs(simulator, sound.DataBlock.SoundID, sound.DataBlock.OwnerID, sound.DataBlock.ObjectID, sound.DataBlock.Gain, sound.DataBlock.Flags));
    }

    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandleAttachedSoundGainChange(Packet packet, Simulator simulator)
    {
        AttachedSoundGainChangePacket change = (AttachedSoundGainChangePacket)packet;
        OnAttachedSoundGainChange.dispatch(new AttachedSoundGainChangeCallbackArgs(simulator, change.DataBlock.ObjectID, change.DataBlock.Gain));
    }

    /**
     * Process an incoming packet and raise the appropriate events
     */
    private final void HandlePreloadSound(Packet packet, Simulator simulator)
    {
        PreloadSoundPacket preload = (PreloadSoundPacket)packet;

        for (PreloadSoundPacket.DataBlockBlock data : preload.DataBlock)
        {
            OnPreloadSound.dispatch(new PreloadSoundCallbackArgs(simulator, data.SoundID, data.OwnerID, data.ObjectID));
        }
    }

    /**
     * Process an incoming <see cref="SoundTriggerPacket"/> packet
     * 
     * @param packet The <see cref="SoundTriggerPacket"/> packet containing the data
     * @param simulator The simulator the packet originated from
     */
    private final void HandleSoundTrigger(Packet packet, Simulator simulator)
    {
        SoundTriggerPacket trigger = (SoundTriggerPacket)packet;
        OnSoundTrigger.dispatch(new SoundTriggerCallbackArgs(simulator, trigger.SoundData.SoundID, trigger.SoundData.OwnerID, trigger.SoundData.ObjectID, trigger.SoundData.ParentID, trigger.SoundData.Gain, trigger.SoundData.Handle, trigger.SoundData.Position));
    }
    // #endregion
}

