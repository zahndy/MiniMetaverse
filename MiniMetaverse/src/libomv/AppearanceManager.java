/**
 * Copyright (c) 2006-2008, openmetaverse.org
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.NetworkManager.EventQueueRunningCallbackArgs;
import libomv.VisualParams.VisualAlphaParam;
import libomv.VisualParams.VisualColorParam;
import libomv.VisualParams.VisualParam;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetManager.AssetDownload;
import libomv.assets.AssetItem;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.assets.AssetWearable.WearableType;
import libomv.assets.TexturePipeline.TextureDownloadCallback;
import libomv.assets.TexturePipeline.TextureRequestState;
import libomv.imaging.Baker;
import libomv.inventory.InventoryAttachment;
import libomv.inventory.InventoryException;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryManager.InventorySortOrder;
import libomv.inventory.InventoryNode;
import libomv.inventory.InventoryObject;
import libomv.inventory.InventoryWearable;
import libomv.packets.AgentCachedTexturePacket;
import libomv.packets.AgentCachedTexturePacket.WearableDataBlock;
import libomv.packets.AgentCachedTextureResponsePacket;
import libomv.packets.AgentIsNowWearingPacket;
import libomv.packets.AgentSetAppearancePacket;
import libomv.packets.AgentWearablesRequestPacket;
import libomv.packets.AgentWearablesUpdatePacket;
import libomv.packets.DetachAttachmentIntoInvPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RebakeAvatarTexturesPacket;
import libomv.packets.RezMultipleAttachmentsFromInvPacket;
import libomv.packets.RezSingleAttachmentFromInvPacket;
import libomv.primitives.Avatar;
import libomv.primitives.Primitive.AttachmentPoint;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.PacketCallback;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Callback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;

public class AppearanceManager implements PacketCallback
{
	// Bake layers for avatar appearance
	public enum BakeType
	{
		Unknown, Head, UpperBody, LowerBody, Eyes, Skirt, Hair;

		public static BakeType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(BakeType value)
		{
			return (byte) (value.ordinal() - 1);
		}
	}

    // #region Constants
    // Mask for multiple attachments</summary>
    public static final byte ATTACHMENT_ADD = (byte) 0x80;
    // Mapping between BakeType and AvatarTextureIndex
    public static final byte[] BakeIndexToTextureIndex = new byte[] { 8, 9, 10, 11, 19, 20 };
    // Maximum number of concurrent downloads for wearable assets and textures 
    private static final int MAX_CONCURRENT_DOWNLOADS = 5;
    // Maximum number of concurrent uploads for baked textures 
    private static final int MAX_CONCURRENT_UPLOADS = 6;
    // Timeout for fetching inventory listings 
    private static final int INVENTORY_TIMEOUT = 1000 * 30;
    // Timeout for fetching a single wearable, or receiving a single packet response 
    private static final int WEARABLE_TIMEOUT = 1000 * 30;
    // Timeout for fetching a single texture 
    private static final int TEXTURE_TIMEOUT = 1000 * 120;
    // Timeout for uploading a single baked texture 
    private static final int UPLOAD_TIMEOUT = 1000 * 90;
    // Number of times to retry bake upload 
    private static final int UPLOAD_RETRIES = 2;
    // When changing outfit, kick off rebake after 20 seconds has passed since the last change 
    private static final int REBAKE_DELAY = 1000 * 20;

    // Total number of wearables for each avatar 
    public static final  int WEARABLE_COUNT = 16;
    // Total number of baked textures on each avatar 
    public static final  int BAKED_TEXTURE_COUNT = 6;
    // Total number of wearables per bake layer 
    public static final  int WEARABLES_PER_LAYER = 9;
    // Map of what wearables are included in each bake 
    public static final WearableType[][] WEARABLE_BAKE_MAP = new WearableType[][]
    {
        new WearableType[] { WearableType.Shape, WearableType.Skin,    WearableType.Tattoo,  WearableType.Hair,    WearableType.Alpha,   WearableType.Invalid, WearableType.Invalid,    WearableType.Invalid,      WearableType.Invalid },
        new WearableType[] { WearableType.Shape, WearableType.Skin,    WearableType.Tattoo,  WearableType.Shirt,   WearableType.Jacket,  WearableType.Gloves,  WearableType.Undershirt, WearableType.Alpha,        WearableType.Invalid },
        new WearableType[] { WearableType.Shape, WearableType.Skin,    WearableType.Tattoo,  WearableType.Pants,   WearableType.Shoes,   WearableType.Socks,   WearableType.Jacket,     WearableType.Underpants,   WearableType.Alpha   },
        new WearableType[] { WearableType.Eyes,  WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,    WearableType.Invalid,      WearableType.Invalid },
        new WearableType[] { WearableType.Skirt, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,    WearableType.Invalid,      WearableType.Invalid },
        new WearableType[] { WearableType.Hair,  WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid, WearableType.Invalid,    WearableType.Invalid,      WearableType.Invalid }
    };
    
    // Magic values to finalize the cache check hashes for each bake 
    public static final UUID[] BAKED_TEXTURE_HASH = new UUID[]
    {
        new UUID("18ded8d6-bcfc-e415-8539-944c0f5ea7a6"),
        new UUID("338c29e3-3024-4dbb-998d-7c04cf4fa88f"),
        new UUID("91b4a2c7-1b1a-ba16-9a16-1f8f8dcc1c3f"),
        new UUID("b2cf28af-b840-1071-3c6a-78085d8128b5"),
        new UUID("ea800387-ea1a-14e0-56cb-24f2022f969a"),
        new UUID("0af1ef7c-ad24-11dd-8790-001f5bf833e8")
    };
    // Default avatar texture, used to detect when a custom texture is not set for a face
    public static final UUID DEFAULT_AVATAR_TEXTURE = new UUID("c228d1cf-4b5d-4ba8-84f4-899a0796aa97");

    // #endregion Constants

    // #region Structs / Classes

    // Contains information about a wearable inventory item
    public class WearableData
    {
        //Inventory ItemID of the wearable</summary>
        public UUID ItemID;
        //AssetID of the wearable asset</summary>
        public UUID AssetID;
        //WearableType of the wearable</summary>
        public WearableType WearableType;
        //AssetType of the wearable</summary>
        public libomv.assets.AssetItem.AssetType AssetType;
        // Asset data for the wearable</summary>
        public AssetWearable Asset;

        @Override
		public String toString()
        {
            return String.format("ItemID: %s, AssetID: %s, WearableType: %s, AssetType: %s, Asset: %s",
                ItemID, AssetID, WearableType, AssetType, Asset != null ? Asset.Name : "(null)");
        }
    }

    // Data collected from visual params for each wearable needed for the calculation of the color
    private class ColorParamInfo
    {
        public VisualParam VisualParam;
        public VisualColorParam VisualColorParam;
        public float Value;
        public WearableType WearableType;
    }

    // Holds a texture assetID and the data needed to bake this layer into an outfit texture.
    // Used to keep track of currently worn textures and baking data
    public class TextureData
    {
        // A texture AssetID
        public UUID TextureID;
        // Asset data for the texture
        public AssetTexture Texture;
        // Collection of alpha masks that needs applying
        public HashMap<libomv.VisualParams.VisualAlphaParam, Float> AlphaMasks;
        // Tint that should be applied to the texture
        public Color4 Color;
        // Where on avatar does this texture belong
        public AvatarTextureIndex TextureIndex;

        @Override
		public String toString()
        {
            return String.format("TextureID: %s, Texture: %s",
                TextureID, Texture != null ? Texture.AssetData.length + " bytes" : "(null)");
        }
    }

    // #endregion Structs / Classes

    // #region Event delegates, Raise Events

    // Triggered when an AgentWearablesUpdate packet is received, telling us what our avatar is currently wearing
    // <see cref="RequestAgentWearables"/> request.
    public class AgentWearablesReplyCallbackArgs implements CallbackArgs
    {
        // Construct a new instance of the AgentWearablesReplyEventArgs class
        public AgentWearablesReplyCallbackArgs()
        {
        }
    }

    public CallbackHandler<AgentWearablesReplyCallbackArgs> OnAgentWearablesReply = new CallbackHandler<AgentWearablesReplyCallbackArgs>();

    
    // Raised when an AgentCachedTextureResponse packet is received, giving a list of cached bakes that were found
    // on the simulator <see cref="RequestCachedBakes"/> request.
    public class AgentCachedBakesReplyCallbackArgs implements CallbackArgs
    {
        // Construct a new instance of the AgentCachedBakesReplyEventArgs class
        public AgentCachedBakesReplyCallbackArgs()
        {
        }
    }

    public CallbackHandler<AgentCachedBakesReplyCallbackArgs> OnAgentCachedBakesReply = new CallbackHandler<AgentCachedBakesReplyCallbackArgs>();

    
    // Raised when appearance data is sent to the simulator, also indicates the main appearance thread is finished.
    // <see cref="RequestAgentSetAppearance"/> request.
    public class AppearanceSetCallbackArgs implements CallbackArgs
    {
        private final boolean m_success;

        // Indicates whether appearance setting was successful
        public boolean getSuccess() { return m_success; }
        /**
         * Triggered when appearance data is sent to the sim and the main appearance thread is done.
         *
         * @param success Indicates whether appearance setting was successful
         */
        public AppearanceSetCallbackArgs(boolean success)
        {
            this.m_success = success;
        }
    }

    public CallbackHandler<AppearanceSetCallbackArgs> OnAppearanceSet = new CallbackHandler<AppearanceSetCallbackArgs>();

    
    //  Triggered when the simulator requests the agent rebake its appearance. 
    // <see cref="RebakeAvatarRequest"/>
    public class RebakeAvatarTexturesCallbackArgs implements CallbackArgs
    {
        private final UUID m_textureID;

        // The ID of the Texture Layer to bake
        public UUID getTextureID() { return m_textureID; }

        /**
         * Triggered when the simulator sends a request for this agent to rebake its appearance
         * 
         * @param textureID The ID of the Texture Layer to bake
         */
        public RebakeAvatarTexturesCallbackArgs(UUID textureID)
        {
            this.m_textureID = textureID;
        }

    }

    public CallbackHandler<RebakeAvatarTexturesCallbackArgs> OnRebakeAvatarReply = new CallbackHandler<RebakeAvatarTexturesCallbackArgs>();

    // #endregion

    // #region Properties and public fields

    /** 
     * Returns true if AppearanceManager is busy and trying to set or change appearance will fail
     */ 
    public boolean getManagerBusy()
    {
        return AppearanceThreadRunning.get();
    }

    // Visual parameters last sent to the sim
    public byte[] MyVisualParameters = null;
    
    // Textures about this client sent to the sim
    public TextureEntry MyTextures = null;

    // #endregion Properties

    // #region Private Members

    // A cache of wearables currently being worn
    private HashMap<WearableType, WearableData> Wearables = new HashMap<WearableType, WearableData>();
    // A cache of textures currently being worn
    private TextureData[] Textures = new TextureData[AvatarTextureIndex.values().length];
    // Incrementing serial number for AgentCachedTexture packets
    private AtomicInteger CacheCheckSerialNum = new AtomicInteger(-1);
    // Incrementing serial number for AgentSetAppearance packets
    private AtomicInteger SetAppearanceSerialNum = new AtomicInteger();
    // Indicates whether or not the appearance thread is currently running, to prevent multiple
    // appearance threads from running simultaneously
    private AtomicBoolean AppearanceThreadRunning = new AtomicBoolean(false);
    // Reference to our agent
    private GridClient _Client;
    // 
    /// Timer used for delaying rebake on changing outfit
    /// 
    private Timer RebakeScheduleTimer;
    /// Main appearance thread
    private Thread AppearanceThread;
    // #endregion Private Members

    /**
     * Default constructor
     * 
     * @param client A reference to our agent
     */
    public AppearanceManager(GridClient client)
    {
        _Client = client;

        _Client.Network.RegisterCallback(PacketType.AgentWearablesUpdate, this);
        _Client.Network.RegisterCallback(PacketType.AgentCachedTextureResponse, this);
        _Client.Network.RegisterCallback(PacketType.RebakeAvatarTextures, this);

        _Client.Network.OnEventQueueRunning.add(new Network_OnEventQueueRunning());
        _Client.Network.OnDisconnected.add(new Network_OnDisconnected());
    }

    @Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
    {
    	switch (packet.getType())
    	{
    		case AgentWearablesUpdate:
    			HandleAgentWearablesUpdate(packet, simulator);
    			break;
    		case AgentCachedTextureResponse:
    			HandleAgentCachedTextureResponse(packet, simulator);
    			break;
    		case RebakeAvatarTextures:
    			HandleRebakeAvatarTextures(packet, simulator);
    			break;
    	}
   }
    
    // #region Publics Methods

    /**
     * Starts the appearance setting thread
     */ 
    public void RequestSetAppearance()
    {
        RequestSetAppearance(false);
    }

    /** 
     * Starts the appearance setting thread
     * 
     * @param forceRebake True to force rebaking, otherwise false
     */
    public void RequestSetAppearance(final boolean forceRebake)
    {
        if (!AppearanceThreadRunning.compareAndSet(false, true))
        {
            Logger.Log("Appearance thread is already running, skipping", LogLevel.Warning, _Client);
            return;
        }

        // If we have an active delayed scheduled appearance bake, we dispose of it
        if (RebakeScheduleTimer != null)
        {
            RebakeScheduleTimer.cancel();
            RebakeScheduleTimer = null;
        }

        // This is the first time setting appearance, run through the entire sequence
        AppearanceThread = new Thread()
        {
            @Override
			public void run()
            {
                boolean success = true;
                try
                {
                    if (forceRebake)
                    {
                        // Set all of the baked textures to UUID.Zero to force rebaking
                        for (int bakedIndex = 0; bakedIndex < BAKED_TEXTURE_COUNT; bakedIndex++)
                            Textures[AvatarTextureIndex.getValue(BakeTypeToAgentTextureIndex(BakeType.setValue(bakedIndex)))].TextureID = UUID.Zero;
                    }

                    if (SetAppearanceSerialNum.get() == 0)
                    {
                        // Fetch a list of the current agent wearables
                        if (!GetAgentWearables())
                        {
                            Logger.Log("Failed to retrieve a list of current agent wearables, appearance cannot be set",
                                LogLevel.Error, _Client);
                            throw new Exception("Failed to retrieve a list of current agent wearables, appearance cannot be set");
                        }
                    }

                    // Download and parse all of the agent wearables
                    success = DownloadWearables();
                    if (!success)
                    {
                        Logger.Log("One or more agent wearables failed to download, appearance will be incomplete",
                            LogLevel.Warning, _Client);
                    }

                    // If this is the first time setting appearance and we're not forcing rebakes, check the server
                    // for cached bakes
                    if (SetAppearanceSerialNum.get() == 0 && !forceRebake)
                    {
                        // Compute hashes for each bake layer and compare against what the simulator currently has
                        if (!GetCachedBakes())
                        {
                            Logger.Log("Failed to get a list of cached bakes from the simulator, appearance will be rebaked",
                                LogLevel.Warning, _Client);
                        }
                    }

                    // Download textures, compute bakes, and upload for any cache misses
                    if (!CreateBakes())
                    {
                        success = false;
                        Logger.Log("Failed to create or upload one or more bakes, appearance will be incomplete",
                            LogLevel.Warning, _Client);
                    }

                    // Send the appearance packet
                    RequestAgentSetAppearance();
                }
                catch (Exception ex)
                {
                    success = false;
                }
                finally
                {
                    OnAppearanceSet.dispatch(new AppearanceSetCallbackArgs(success));
                    AppearanceThreadRunning.set(false);
                }
            }
        };
        AppearanceThread.setName("Appearance");
        AppearanceThread.setDaemon(true);
        AppearanceThread.start();
    }

    /**
     * Ask the server what textures our agent is currently wearing
     * 
     * @throws Exception 
     */ 
    public void RequestAgentWearables() throws Exception
    {
        AgentWearablesRequestPacket request = new AgentWearablesRequestPacket();
        request.AgentData.AgentID = _Client.Self.getAgentID();
        request.AgentData.SessionID = _Client.Self.getSessionID();

        _Client.Network.SendPacket(request);
    }

    /**
     * Build hashes out of the texture assetIDs for each baking layer to
     * ask the simulator whether it has cached copies of each baked texture
     * 
     * @throws Exception 
     */ 
    public void RequestCachedBakes() throws Exception
    {
        ArrayList<AgentCachedTexturePacket.WearableDataBlock> hashes = new ArrayList<AgentCachedTexturePacket.WearableDataBlock>();
        AgentCachedTexturePacket cache = new AgentCachedTexturePacket();

        // Build hashes for each of the bake layers from the individual components
        synchronized (Wearables)
        {
            for (int bakedIndex = 0; bakedIndex < BAKED_TEXTURE_COUNT; bakedIndex++)
            {
                // Don't do a cache request for a skirt bake if we're not wearing a skirt
                if (bakedIndex == BakeType.getValue(BakeType.Skirt) && !Wearables.containsKey(WearableType.Skirt))
                    continue;

                // Build a hash of all the texture asset IDs in this baking layer
                UUID hash = UUID.Zero;
                for (int wearableIndex = 0; wearableIndex < WEARABLES_PER_LAYER; wearableIndex++)
                {
                    WearableType type = WEARABLE_BAKE_MAP[bakedIndex][wearableIndex];

                    if (type != WearableType.Invalid && Wearables.containsKey(type))
                        hash = UUID.XOr(hash, Wearables.get(type).AssetID);
                }

                if (!hash.equals(UUID.Zero))
                {
                    // Hash with our secret value for this baked layer
                    hash = UUID.XOr(hash, BAKED_TEXTURE_HASH[bakedIndex]);

                    // Add this to the list of hashes to send out
                    AgentCachedTexturePacket.WearableDataBlock block = cache.new WearableDataBlock();
                    block.ID = hash;
                    block.TextureIndex = (byte)bakedIndex;
                    hashes.add(block);

                    Logger.DebugLog("Checking cache for " + BakeType.setValue(bakedIndex) + ", hash = " + block.ID, _Client);
                }
            }
        }

        // Only send the packet out if there's something to check
        if (hashes.size() > 0)
        {
            cache.AgentData.AgentID = _Client.Self.getAgentID();
            cache.AgentData.SessionID = _Client.Self.getSessionID();
            cache.AgentData.SerialNum = CacheCheckSerialNum.incrementAndGet();

            cache.WearableData = (WearableDataBlock[])hashes.toArray();

            _Client.Network.SendPacket(cache);
        }
    }

    /**
     * Returns the AssetID of the asset that is currently being worn in a 
     * given WearableType slot
     * 
     * @param type WearableType slot to get the AssetID for 
     * @returnsThe UUID of the asset being worn in the given slot, or UUID.Zero if no wearable is attached
     *          to the given slot or wearables have not been downloaded yet
     */
    public UUID GetWearableAsset(WearableType type)
    {
        if (Wearables.containsKey(type))
            return Wearables.get(type).AssetID;
        return UUID.Zero;
    }

    /** 
     * Add a wearable to the current outfit and set appearance
     *
     * @param wearableItem Wearable to be added to the outfit
     * @throws Exception 
     */
    public void AddToOutfit(InventoryItem wearableItem) throws Exception
    {
        List<InventoryItem> wearableItems = new ArrayList<InventoryItem>();
        wearableItems.add(wearableItem);
        AddToOutfit(wearableItems);
    }

    /** 
     * Add a list of wearables to the current outfit and set appearance
     * 
     * @param wearableItems List of wearable inventory items to be added to the outfit
     * @throws Exception 
     */
    public void AddToOutfit(List<InventoryItem> wearableItems) throws Exception
    {
        List<InventoryWearable> wearables = new ArrayList<InventoryWearable>();
        List<InventoryItem> attachments = new ArrayList<InventoryItem>();

        for (int i = 0; i < wearableItems.size(); i++)
        {
            InventoryItem item = wearableItems.get(i);

            if (item instanceof InventoryWearable)
                wearables.add((InventoryWearable)item);
            else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
                attachments.add(item);
        }

        synchronized (Wearables)
        {
            // Add the given wearables to the wearables collection
            for (int i = 0; i < wearables.size(); i++)
            {
                InventoryWearable wearableItem = wearables.get(i);

                WearableData wd = new WearableData();
                wd.AssetID = wearableItem.AssetID;
                wd.AssetType = wearableItem.assetType;
                wd.ItemID = wearableItem.itemID;
                wd.WearableType = wearableItem.getWearableType();

                Wearables.put(wearableItem.getWearableType(), wd);
            }
        }

        if (attachments.size() > 0)
        {
            AddAttachments(attachments, false, false);
        }

        if (wearables.size() > 0)
        {
            SendAgentIsNowWearing();
            DelayedRequestSetAppearance();
        }
    }
    
    /** 
     * Remove a wearable from the current outfit and set appearance
     * 
     * @param wearableItem Wearable to be removed from the outfit
     * @throws Exception 
     */
    public void RemoveFromOutfit(InventoryItem wearableItem) throws Exception
    {
        List<InventoryItem> wearableItems = new ArrayList<InventoryItem>();
        wearableItems.add(wearableItem);
        RemoveFromOutfit(wearableItems);
    }


    /**
     * Removes a list of wearables from the current outfit and set appearance
     * 
     * @param wearableItems List of wearable inventory items to be removed from the outfit
     * @throws Exception 
     */
    public void RemoveFromOutfit(List<InventoryItem> wearableItems) throws Exception
    {
        List<InventoryWearable> wearables = new ArrayList<InventoryWearable>();
        List<InventoryItem> attachments = new ArrayList<InventoryItem>();

        for (int i = 0; i < wearableItems.size(); i++)
        {
            InventoryItem item = wearableItems.get(i);

            if (item instanceof InventoryWearable)
                wearables.add((InventoryWearable)item);
            else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
                attachments.add(item);
        }

        boolean needSetAppearance = false;
        synchronized (Wearables)
        {
            // Remove the given wearables from the wearables collection
            for (int i = 0; i < wearables.size(); i++)
            {
                InventoryWearable wearableItem = wearables.get(i);
                if (wearableItem.assetType != AssetType.Bodypart        // Remove if it's not a body part
                    && Wearables.containsKey(wearableItem.getWearableType()) // And we have that wearabe type
                    && Wearables.get(wearableItem.getWearableType()).ItemID.equals(wearableItem.itemID) // And we are wearing it
                   )
                {
                    Wearables.remove(wearableItem.getWearableType());
                    needSetAppearance = true;
                }
            }
        }

        for (int i = 0; i < attachments.size(); i++)
        {
            Detach(attachments.get(i).itemID);
        }

        if (needSetAppearance)
        {
            SendAgentIsNowWearing();
            DelayedRequestSetAppearance();
        }
    }

    /** 
     * Replace the current outfit with a list of wearables and set appearance
     * 
     * @param wearableItems List of wearable inventory items that define a new outfit
     * @throws Exception 
     */
    public void ReplaceOutfit(List<InventoryItem> wearableItems) throws Exception
    {
        ReplaceOutfit(wearableItems, true);
    }

    /**
     * Replace the current outfit with a list of wearables and set appearance
     * 
     * @param wearableItems List of wearable inventory items that define a new outfit
     * @param safe Check if we have all body parts, set this to false only if you know what you're doing
     * @throws Exception 
     */
    public void ReplaceOutfit(List<InventoryItem> wearableItems, boolean safe) throws Exception
    {
        List<InventoryWearable> wearables = new ArrayList<InventoryWearable>();
        List<InventoryItem> attachments = new ArrayList<InventoryItem>();

        for (int i = 0; i < wearableItems.size(); i++)
        {
            InventoryItem item = wearableItems.get(i);

            if (item instanceof InventoryWearable)
                wearables.add((InventoryWearable)item);
            else if (item instanceof InventoryAttachment || item instanceof InventoryObject)
                attachments.add(item);
        }

        if (safe)
        {
            // If we don't already have a the current agent wearables downloaded, updating to a
            // new set of wearables that doesn't have all of the bodyparts can leave the avatar
            // in an inconsistent state. If any bodypart entries are empty, we need to fetch the
            // current wearables first
            boolean needsCurrentWearables = false;
            synchronized (Wearables)
            {
                for (int i = 0; i < WEARABLE_COUNT; i++)
                {
                    WearableType wearableType = WearableType.setValue(i);
                    if (WearableTypeToAssetType(wearableType) == AssetType.Bodypart && !Wearables.containsKey(wearableType))
                    {
                        needsCurrentWearables = true;
                        break;
                    }
                }
            }

            if (needsCurrentWearables && !GetAgentWearables())
            {
                Logger.Log("Failed to fetch the current agent wearables, cannot safely replace outfit", LogLevel.Error, _Client);
                return;
            }
        }

        // Replace our local Wearables collection, send the packet(s) to update our
        // attachments, tell sim what we are wearing now, and start the baking process
        if (!safe)
        {
            SetAppearanceSerialNum.incrementAndGet();
        }
        ReplaceWearables(wearables);
        AddAttachments(attachments, true, false);
        SendAgentIsNowWearing();
        DelayedRequestSetAppearance();
    }

    /** 
     * Checks if an inventory item is currently being worn
     * 
     * @param item The inventory item to check against the agent wearables
     * @returnsThe WearableType slot that the item is being worn in, or WearableType.Invalid if it is not currently being worn
     */
    public WearableType IsItemWorn(InventoryItem item)
    {
        synchronized (Wearables)
        {
            for (Entry<WearableType, WearableData> entry : Wearables.entrySet())
            {
                if (entry.getValue().ItemID.equals(item.itemID))
                    return entry.getKey();
            }
        }
        return WearableType.Invalid;
    }

    /** 
     * Returns a copy of the agents currently worn wearables
     * Avoid calling this function multiple times as it will make a copy of all of the wearable data each time 
     * 
     * @returnsA copy of the agents currently worn wearables
     */ 
    public HashMap<WearableType, WearableData> GetWearables()
    {
        synchronized (Wearables)
        {
            return new HashMap<WearableType, WearableData>(Wearables);
        }
    }

    /**
     * Calls either <see cref="ReplaceOutfit"/> orb<see cref="AddToOutfit"/> depending on the value of replaceItems
     *
     * @param wearables List of wearable inventory items to add to the outfit or become a new outfit
     * @param replaceItems True to replace existing items with the new list of items, false to add these items to the existing outfit
     * @throws Exception 
     */
    public void WearOutfit(List<InventoryItem> wearables, boolean replaceItems) throws Exception
    {
        List<InventoryItem> wearableItems = new ArrayList<InventoryItem>(wearables.size());
        Iterator<InventoryItem> iter = wearables.iterator();
        while (iter.hasNext())
        {
            wearableItems.add(iter .next());
        }

        if (replaceItems)
            ReplaceOutfit(wearableItems);
        else
            AddToOutfit(wearableItems);
    }

    // #endregion Publics Methods

    // #region Attachments

    /**
     * Adds a list of attachments to our agent
     * 
     * @param attachments A List containing the attachments to add
     * @param removeExistingFirst If true, tells simulator to remove existing attachment first
     * @throws Exception 
     */
    public void AddAttachments(List<InventoryItem> attachments, boolean removeExistingFirst) throws Exception
    {
        AddAttachments(attachments, removeExistingFirst, true);
    }

    /**
     * Adds a list of attachments to our agent
     * 
     * @param attachments A List containing the attachments to add
     * @param removeExistingFirst If true, tells simulator to remove existing attachment first
     * @param replace If true replace existing attachment on this attachment point, otherwise add to it (multi-attachments)
     * @throws Exception 
     */
    public void AddAttachments(List<InventoryItem> attachments, boolean removeExistingFirst, boolean replace) throws Exception
    {
        // Use RezMultipleAttachmentsFromInv to clear out current attachments, and attach new ones
        RezMultipleAttachmentsFromInvPacket attachmentsPacket = new RezMultipleAttachmentsFromInvPacket();
        attachmentsPacket.AgentData.AgentID = _Client.Self.getAgentID();
        attachmentsPacket.AgentData.SessionID = _Client.Self.getSessionID();

        attachmentsPacket.HeaderData.CompoundMsgID = new UUID();
        attachmentsPacket.HeaderData.FirstDetachAll = removeExistingFirst;
        attachmentsPacket.HeaderData.TotalObjects = (byte)attachments.size();

        attachmentsPacket.ObjectData = new RezMultipleAttachmentsFromInvPacket.ObjectDataBlock[attachments.size()];
        for (int i = 0; i < attachments.size(); i++)
        {
            InventoryAttachment attachment = (InventoryAttachment)attachments.get(i);
            if (attachments.get(i) instanceof InventoryAttachment)
            {
                attachmentsPacket.ObjectData[i] = attachmentsPacket.new ObjectDataBlock();
                attachmentsPacket.ObjectData[i].AttachmentPt = attachment.getAttachmentPoint().getValue();
                attachmentsPacket.ObjectData[i].EveryoneMask = attachment.Permissions.EveryoneMask;
                attachmentsPacket.ObjectData[i].GroupMask = attachment.Permissions.GroupMask;
                attachmentsPacket.ObjectData[i].ItemFlags = attachment.ItemFlags;
                attachmentsPacket.ObjectData[i].ItemID = attachment.itemID;
                attachmentsPacket.ObjectData[i].setName(Helpers.StringToBytes(attachment.name));
                attachmentsPacket.ObjectData[i].setDescription(Helpers.StringToBytes(attachment.Description));
                attachmentsPacket.ObjectData[i].NextOwnerMask = attachment.Permissions.NextOwnerMask;
                attachmentsPacket.ObjectData[i].OwnerID = attachment.getOwnerID();
            }
            else if (attachments.get(i) instanceof InventoryObject)
            {
                attachmentsPacket.ObjectData[i] = attachmentsPacket.new ObjectDataBlock();
                attachmentsPacket.ObjectData[i].AttachmentPt = (byte)0;
                attachmentsPacket.ObjectData[i].EveryoneMask = attachment.Permissions.EveryoneMask;
                attachmentsPacket.ObjectData[i].GroupMask = attachment.Permissions.GroupMask;
                attachmentsPacket.ObjectData[i].ItemFlags = attachment.ItemFlags;
                attachmentsPacket.ObjectData[i].ItemID = attachment.itemID;
                attachmentsPacket.ObjectData[i].setName(Helpers.StringToBytes(attachment.name));
                attachmentsPacket.ObjectData[i].setDescription(Helpers.StringToBytes(attachment.Description));
                attachmentsPacket.ObjectData[i].NextOwnerMask = attachment.Permissions.NextOwnerMask;
                attachmentsPacket.ObjectData[i].OwnerID = attachment.getOwnerID();
            }
            else
            {
                Logger.Log("Cannot attach inventory item " + attachment.name, LogLevel.Warning, _Client);
            }
            if (!replace)
            	attachmentsPacket.ObjectData[i].AttachmentPt |= ATTACHMENT_ADD;
        }
        _Client.Network.SendPacket(attachmentsPacket);
    }

    /** 
     * Attach an item to our agent at a specific attach point
     * 
     * @param item A <seealso cref="OpenMetaverse.InventoryItem"/> to attach
     * @param attachPoint the <seealso cref="OpenMetaverse.AttachmentPoint"/> on the avatar to attach the item to
     * @throws Exception 
     */
    public void Attach(InventoryItem item, AttachmentPoint attachPoint) throws Exception
    {
        Attach(item.itemID, item.getOwnerID(), item.name, item.Description, item.Permissions, item.ItemFlags, attachPoint, true);
    }

    /** 
     * Attach an item to our agent at a specific attach point
     * 
     * @param item A <seealso cref="OpenMetaverse.InventoryItem"/> to attach
     * @param attachPoint the <seealso cref="OpenMetaverse.AttachmentPoint"/> on the avatar to attach the item to
     * @param replace If true replace existing attachment on this attachment point, otherwise add to it (multi-attachments)
     * @throws Exception 
     */
    public void Attach(InventoryItem item, AttachmentPoint attachPoint, boolean replace) throws Exception
    {
        Attach(item.itemID, item.getOwnerID(), item.name, item.Description, item.Permissions, item.ItemFlags, attachPoint, replace);
    }

    /** 
     * Attach an item to our agent specifying attachment details
     * 
     * @param itemID The <seealso cref="OpenMetaverse.UUID"/> of the item to attach
     * @param ownerID The <seealso cref="OpenMetaverse.UUID"/> attachments owner
     * @param name The name of the attachment
     * @param description The description of the attahment
     * @param perms The <seealso cref="OpenMetaverse.Permissions"/> to apply when attached
     * @param itemFlags The <seealso cref="OpenMetaverse.InventoryItemFlags"/> of the attachment
     * @param attachPoint The <seealso cref="OpenMetaverse.AttachmentPoint"/> on the agent to attach the item to
     * @throws Exception 
     */
    public void Attach(UUID itemID, UUID ownerID, String name, String description,
        Permissions perms, int itemFlags, AttachmentPoint attachPoint) throws Exception
    {
        Attach(itemID, ownerID, name, description, perms, itemFlags, attachPoint, true);
    }

    /** 
     * Attach an item to our agent specifying attachment details
     * 
     * @param itemID The <seealso cref="OpenMetaverse.UUID"/> of the item to attach
     * @param ownerID The <seealso cref="OpenMetaverse.UUID"/> attachments owner
     * @param name The name of the attachment
     * @param description The description of the attahment
     * @param perms The <seealso cref="OpenMetaverse.Permissions"/> to apply when attached
     * @param itemFlags The <seealso cref="OpenMetaverse.InventoryItemFlags"/> of the attachment
     * @param attachPoint The <seealso cref="OpenMetaverse.AttachmentPoint"/> on the agent to attach the item to
     * @param replace If true replace existing attachment on this attachment point, otherwise add to it (multi-attachments)
     * @throws Exception 
     */
    public void Attach(UUID itemID, UUID ownerID, String name, String description,
        Permissions perms, int itemFlags, AttachmentPoint attachPoint, boolean replace) throws Exception
    {
        // TODO: At some point it might be beneficial to have AppearanceManager track what we
        // are currently wearing for attachments to make enumeration and detachment easier
        RezSingleAttachmentFromInvPacket attach = new RezSingleAttachmentFromInvPacket();

        attach.AgentData.AgentID = _Client.Self.getAgentID();
        attach.AgentData.SessionID = _Client.Self.getSessionID();

        attach.ObjectData.AttachmentPt = replace ? attachPoint.getValue() : (byte)(attachPoint.getValue() | ATTACHMENT_ADD);
        attach.ObjectData.setDescription(Helpers.StringToBytes(description));
        attach.ObjectData.EveryoneMask = perms.EveryoneMask;
        attach.ObjectData.GroupMask = perms.GroupMask;
        attach.ObjectData.ItemFlags = itemFlags;
        attach.ObjectData.ItemID = itemID;
        attach.ObjectData.setName(Helpers.StringToBytes(name));
        attach.ObjectData.NextOwnerMask = perms.NextOwnerMask;
        attach.ObjectData.OwnerID = ownerID;

        _Client.Network.SendPacket(attach);
    }

    /** 
     * Detach an item from our agent using an <seealso cref="OpenMetaverse.InventoryItem"/> object
     * 
     * @param item An <see cref="OpenMetaverse.InventoryItem"/> object
     * @throws Exception 
     */
    public void Detach(InventoryItem item) throws Exception
    {
        Detach(item.itemID);
    }

    /**
     * Detach an item from our agent
     * 
     * @param itemID The inventory itemID of the item to detach
     * @throws Exception 
     */
    public void Detach(UUID itemID) throws Exception
    {
        DetachAttachmentIntoInvPacket detach = new DetachAttachmentIntoInvPacket();
        detach.ObjectData.AgentID = _Client.Self.getAgentID();
        detach.ObjectData.ItemID = itemID;

        _Client.Network.SendPacket(detach);
    }

    // #endregion Attachments

    // #region Appearance Helpers

    /**
     * Inform the sim which wearables are part of our current outfit
     * 
     * @throws Exception 
     */ 
    private void SendAgentIsNowWearing() throws Exception
    {
        AgentIsNowWearingPacket wearing = new AgentIsNowWearingPacket();
        wearing.AgentData.AgentID = _Client.Self.getAgentID();
        wearing.AgentData.SessionID = _Client.Self.getSessionID();
        wearing.WearableData = new AgentIsNowWearingPacket.WearableDataBlock[WEARABLE_COUNT];

        synchronized (Wearables)
        {
            for (int i = 0; i < WEARABLE_COUNT; i++)
            {
                WearableType type = WearableType.setValue(i);
                wearing.WearableData[i] = wearing.new WearableDataBlock();
                wearing.WearableData[i].WearableType = (byte)i;

                if (Wearables.containsKey(type))
                    wearing.WearableData[i].ItemID = Wearables.get(type).ItemID;
                else
                    wearing.WearableData[i].ItemID = UUID.Zero;
            }
        }
        _Client.Network.SendPacket(wearing);
    }

    /**
     * Replaces the Wearables collection with a list of new wearable items
     * 
     * @param wearableItems Wearable items to replace the Wearables collection with
     */
    private void ReplaceWearables(List<InventoryWearable> wearableItems)
    {
        HashMap<WearableType, WearableData> newWearables = new HashMap<WearableType, WearableData>();

        synchronized (Wearables)
        {
            // Preserve body parts from the previous set of wearables. They may be overwritten,
            // but cannot be missing in the new set
            for (Entry<WearableType, WearableData> entry : Wearables.entrySet())
            {
                if (entry.getValue().AssetType == AssetType.Bodypart)
                    newWearables.put(entry.getKey(), entry.getValue());
            }

            // Add the given wearables to the new wearables collection
            for (int i = 0; i < wearableItems.size(); i++)
            {
                InventoryWearable wearableItem = wearableItems.get(i);

                WearableData wd = new WearableData();
                wd.AssetID = wearableItem.AssetID;
                wd.AssetType = wearableItem.assetType;
                wd.ItemID = wearableItem.itemID;
                wd.WearableType = wearableItem.getWearableType();

                newWearables.put(wd.WearableType, wd);
            }

            // Replace the Wearables collection
            Wearables = newWearables;
        }
    }

    /** 
     * Calculates base color/tint for a specific wearable based on its params
     *
     * @param param All the color info gathered from wearable's VisualParams passed as list of ColorParamInfo tuples
     * @returns Base color/tint for the wearable
     */
    private Color4 GetColorFromParams(List<ColorParamInfo> param)
    {
        // Start off with a blank slate, black, fully transparent
        Color4 res = new Color4(0, 0, 0, 0);

        // Apply color modification from each color parameter
        Iterator<ColorParamInfo> iter = param.iterator();
        while (iter.hasNext())
        {
        	ColorParamInfo p = iter.next();
            int n = p.VisualColorParam.Colors.length;

            Color4 paramColor = new Color4(0, 0, 0, 0);

            if (n == 1)
            {
                // We got only one color in this param, use it for application
                // to the final color
                paramColor = p.VisualColorParam.Colors[0];
            }
            else if (n > 1)
            {
                // We have an array of colors in this parameter
                // First, we need to find out, based on param value
                // between which two elements of the array our value lands

                // Size of the step using which we iterate from Min to Max
                float step = (p.VisualParam.MaxValue - p.VisualParam.MinValue) / ((float)n - 1);

                // Our color should land inbetween colors in the array with index a and b
                int indexa = 0;
                int indexb = 0;

                int i = 0;

                for (float a = p.VisualParam.MinValue; a <= p.VisualParam.MaxValue; a += step)
                {
                    if (a <= p.Value)
                    {
                        indexa = i;
                    }
                    else
                    {
                        break;
                    }

                    i++;
                }

                // Sanity check that we don't go outside bounds of the array
                if (indexa > n - 1)
                    indexa = n - 1;

                indexb = (indexa == n - 1) ? indexa : indexa + 1;

                // How far is our value from Index A on the 
                // line from Index A to Index B
                float distance = p.Value - indexa * step;

                // We are at Index A (allowing for some floating point math fuzz),
                // use the color on that index
                if (distance < 0.00001f || indexa == indexb)
                {
                    paramColor = p.VisualColorParam.Colors[indexa];
                }
                else
                {
                    // Not so simple as being precisely on the index eh? No problem.
                    // We take the two colors that our param value places us between
                    // and then find the value for each ARGB element that is
                    // somewhere on the line between color1 and color2 at some
                    // distance from the first color
                    Color4 c1 = paramColor = p.VisualColorParam.Colors[indexa];
                    Color4 c2 = paramColor = p.VisualColorParam.Colors[indexb];

                    // Distance is some fraction of the step, use that fraction
                    // to find the value in the range from color1 to color2
                    paramColor = Color4.Lerp(c1, c2, distance / step);
                }

                // Please leave this fragment even if its commented out
                // might prove useful should ($deity forbid) there be bugs in this code
                //string carray = "";
                //foreach (Color c in p.VisualColorParam.Colors)
                //{
                //    carray += c.ToString() + " - ";
                //}
                //Logger.DebugLog("Calculating color for " + p.WearableType + " from " + p.VisualParam.Name + ", value is " + p.Value + " in range " + p.VisualParam.MinValue + " - " + p.VisualParam.MaxValue + " step " + step + " with " + n + " elements " + carray + " A: " + indexa + " B: " + indexb + " at distance " + distance);
            }

            // Now that we have calculated color from the scale of colors
            // that visual params provided, lets apply it to the result
            switch (p.VisualColorParam.Operation)
            {
                case Add:
                    res = Color4.add(res, paramColor);
                    break;
                case Multiply:
                    res = Color4.multiply(res, paramColor);
                    break;
                case Blend:
                    res = Color4.Lerp(res, paramColor, p.Value);
                    break;
            }
        }
        return res;
    }

    /**
     * Blocking method to populate the Wearables dictionary
     * 
     * @returns True on success, otherwise false
     * @throws Exception 
     */
    boolean GetAgentWearables() throws Exception
    {
        final TimeoutEvent<Boolean> wearablesEvent = new TimeoutEvent<Boolean>();
        Callback<AgentWearablesReplyCallbackArgs> wearablesCallback = new Callback<AgentWearablesReplyCallbackArgs>()
        {
        	@Override
			public boolean callback(AgentWearablesReplyCallbackArgs e)
        	{
        		wearablesEvent.set(true);
        		return false;
        	}
        };
        
        OnAgentWearablesReply.add(wearablesCallback);

        RequestAgentWearables();

        Boolean success = wearablesEvent.waitOne(WEARABLE_TIMEOUT);

        OnAgentWearablesReply.remove(wearablesCallback);

        return success != null ? success : false;
    }

    /** 
     * Blocking method to populate the Textures array with cached bakes
     *
     * @returns True on success, otherwise false
     * @throws Exception 
     */
    boolean GetCachedBakes() throws Exception
    {
    	final TimeoutEvent<Boolean> cacheCheckEvent = new TimeoutEvent<Boolean>();
        Callback<AgentCachedBakesReplyCallbackArgs> cacheCallback = new Callback<AgentCachedBakesReplyCallbackArgs>()
        {
        	@Override
			public boolean callback(AgentCachedBakesReplyCallbackArgs e)
        	{
        		cacheCheckEvent.set(true);
        		return false;
        	}
        };

        OnAgentCachedBakesReply.add(cacheCallback);

        RequestCachedBakes();

        Boolean success = cacheCheckEvent.waitOne(WEARABLE_TIMEOUT);

        OnAgentCachedBakesReply.remove(cacheCallback);

        return success != null ? success : false;
    }

    /** 
     * Populates textures and visual params from a decoded asset
     * 
     * @param wearable Wearable to decode
     */
    private void DecodeWearableParams(WearableData wearable)
    {
        HashMap<VisualAlphaParam, Float> alphaMasks = new HashMap<VisualAlphaParam, Float>();
        List<ColorParamInfo> colorParams = new ArrayList<ColorParamInfo>();

        // Populate collection of alpha masks from visual params
        // also add color tinting information
        for (Entry<Integer, Float> kvp : wearable.Asset.Params.entrySet())
        {
            if (!VisualParams.Params.containsKey(kvp.getKey())) continue;

            VisualParam p = VisualParams.Params.get(kvp.getKey());

            ColorParamInfo colorInfo = new ColorParamInfo();
            colorInfo.WearableType = wearable.WearableType;
            colorInfo.VisualParam = p;
            colorInfo.Value = kvp.getValue();

            // Color params
            if (p.ColorParams != null)
            {
                colorInfo.VisualColorParam = p.ColorParams;

                // If this is not skin, just add params directly
                if (wearable.WearableType != WearableType.Skin)
                {
                    colorParams.add(colorInfo);
                }
                else
                {
                    // For skin we skip makeup params for now and use only the 3
                    // that are used to determine base skin tone
                    // Param 108 - Rainbow Color
                    // Param 110 - Red Skin (Ruddiness)
                    // Param 111 - Pigment
                    if (kvp.getKey() == 108 || kvp.getKey() == 110 || kvp.getKey() == 111)
                    {
                        colorParams.add(colorInfo);
                    }
                }
            }

            // Add alpha mask
            if (p.AlphaParams != null && !p.AlphaParams.TGAFile.isEmpty() && !p.IsBumpAttribute && !alphaMasks.containsKey(p.AlphaParams))
            {
                alphaMasks.put(p.AlphaParams, kvp.getValue());
            }

            // Alhpa masks can also be specified in sub "driver" params
            if (p.Drivers != null)
            {
                for (int i = 0; i < p.Drivers.length; i++)
                {
                    if (VisualParams.Params.containsKey(p.Drivers[i]))
                    {
                        VisualParam driver = VisualParams.Params.get(p.Drivers[i]);
                        if (driver.AlphaParams != null && !driver.AlphaParams.TGAFile.isEmpty() && !driver.IsBumpAttribute && !alphaMasks.containsKey(driver.AlphaParams))
                        {
                            alphaMasks.put(driver.AlphaParams, kvp.getValue());
                        }
                    }
                }
            }
        }

        Color4 wearableColor = Color4.White; // Never actually used
        if (colorParams.size() > 0)
        {
            wearableColor = GetColorFromParams(colorParams);
            Logger.DebugLog("Setting tint " + wearableColor + " for " + wearable.WearableType);
        }

        // Loop through all of the texture IDs in this decoded asset and put them in our cache of worn textures
        for (Entry<AvatarTextureIndex, UUID> entry : wearable.Asset.Textures.entrySet())
        {
            int i = AvatarTextureIndex.getValue(entry.getKey());

            // Update information about color and alpha masks for this texture
            Textures[i].AlphaMasks = alphaMasks;
            Textures[i].Color = wearableColor;

            // If this texture changed, update the TextureID and clear out the old cached texture asset
            if (!Textures[i].TextureID.equals(entry.getValue()))
            {
                // Treat DEFAULT_AVATAR_TEXTURE as null
                if (!entry.getValue().equals(DEFAULT_AVATAR_TEXTURE))
                    Textures[i].TextureID = entry.getValue();
                else
                    Textures[i].TextureID = UUID.Zero;
                Logger.DebugLog("Set " + entry.getKey() + " to " + Textures[i].TextureID, _Client);

                Textures[i].Texture = null;
            }
        }
    }

    /** 
     * Blocking method to download and parse currently worn wearable assets
     * 
     * @returns True on success, otherwise false
     */
    private boolean DownloadWearables()
    {
        boolean success = true;

        // Make a copy of the wearables dictionary to enumerate over
        HashMap<WearableType, WearableData> wearables;
        synchronized (Wearables)
        {
            wearables = new HashMap<WearableType, WearableData>(Wearables);
        }
        
        // We will refresh the textures (zero out all non bake textures)
        for (int i = 0; i < Textures.length; i++)
        {
            boolean isBake = false;
            for (int j = 0; j < BakeIndexToTextureIndex.length; j++)
            {
                if (BakeIndexToTextureIndex[j] == i)
                {
                    isBake = true;
                    break;
                }
            }
            if (!isBake)
                Textures[i] = new TextureData();
        }

        final CountDownLatch latch = new CountDownLatch(wearables.size());
        for (WearableData wearable : wearables.values())
        {
            if (wearable.Asset != null)
            {
                DecodeWearableParams(wearable);
                latch.countDown();
            }
        }

        
        int pendingWearables = (int)latch.getCount();
        if (pendingWearables == 0)
            return true;

        Logger.DebugLog("Downloading " + pendingWearables + " wearable assets");

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(pendingWearables, MAX_CONCURRENT_DOWNLOADS));
        for (final WearableData wearable : wearables.values())
        {
            if (wearable.Asset == null)
            {
            	executor.submit(new Runnable()
            	{
            		@Override
					public void run()
            		{
            			// Fetch this wearable asset
            			try
						{
							_Client.Assets.RequestAsset(wearable.AssetID, wearable.AssetType, true, _Client.Assets.new AssetReceivedCallback()
							{
								@Override
								public void callback(AssetDownload transfer, AssetItem asset)
								{
							        if (transfer.Success && asset instanceof AssetWearable)
							        {
							            // Update this wearable with the freshly downloaded asset 
							            wearable.Asset = (AssetWearable)asset;

							            if (wearable.Asset.Decode())
							            {
							                DecodeWearableParams(wearable);
							                Logger.DebugLog("Downloaded wearable asset " + wearable.WearableType + " with " + wearable.Asset.Params.size() +
							                    " visual params and " + wearable.Asset.Textures.size() + " textures", _Client);

							            }
							            else
							            {
							                wearable.Asset = null;
							                try
											{
												Logger.Log("Failed to decode asset:\n" + Helpers.BytesToString(asset.AssetData), LogLevel.Error, _Client);
											}
											catch (UnsupportedEncodingException e) { }
							            }
							        }
							        else
							        {
							            Logger.Log("Wearable " + wearable.AssetID + "(" + wearable.WearableType + ") failed to download, " +
							                transfer.Status, LogLevel.Warning, _Client);
							        }
							        latch.countDown();
								}
							});
						}
						catch (Exception ex) { }
            		}
            	});
            }
        }

        try {
            success = latch.await(TEXTURE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {}
        executor.shutdown();
        return success;
    }

    /** 
     * Get a list of all of the textures that need to be downloaded for a single bake layer
     *
     * @param bakeType Bake layer to get texture AssetIDs for
     * @returns A list of texture AssetIDs to download
     */
    private List<UUID> GetTextureDownloadList(BakeType bakeType)
    {
        List<AvatarTextureIndex> indices = BakeTypeToTextures(bakeType);
        List<UUID> textures = new ArrayList<UUID>();

        for (int i = 0; i < indices.size(); i++)
        {
            AvatarTextureIndex index = indices.get(i);

            if (index == AvatarTextureIndex.Skirt && !Wearables.containsKey(WearableType.Skirt))
                continue;

            AddTextureDownload(index, textures);
        }
        return textures;
    }

    /** 
     * Helper method to lookup the TextureID for a single layer and add it to alist if it is not already present
     * 
     * @param index 
     * @param textures 
     */
    private void AddTextureDownload(AvatarTextureIndex index, List<UUID> textures)
    {
        TextureData textureData = Textures[index.getValue()];
        // Add the textureID to the list if this layer has a valid textureID set, it has not already
        // been downloaded, and it is not already in the download list
        if (textureData.TextureID != UUID.Zero && textureData.Texture == null && !textures.contains(textureData.TextureID))
            textures.add(textureData.TextureID);
    }

    /** 
     * Blocking method to download all of the textures needed for baking the given bake layers
     * No return value is given because the baking will happen whether or not all textures are successfully downloaded
     * 
     * @param bakeLayers A list of layers that need baking
     */
    private void DownloadTextures(List<BakeType> bakeLayers)
    {
        List<UUID> textureIDs = new ArrayList<UUID>();

        for (int i = 0; i < bakeLayers.size(); i++)
        {
            List<UUID> layerTextureIDs = GetTextureDownloadList(bakeLayers.get(i));

            for (int j = 0; j < layerTextureIDs.size(); j++)
            {
                UUID uuid = layerTextureIDs.get(j);
                if (!textureIDs.contains(uuid))
                    textureIDs.add(uuid);
            }
        }

        Logger.DebugLog("Downloading " + textureIDs.size() + " textures for baking");

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);
        final CountDownLatch latch = new CountDownLatch(textureIDs.size());
        for (final UUID textureID : textureIDs)
        {
        	executor.submit(new Runnable()
        	{
        		@Override
        		public void run()
        		{
        			_Client.Assets.RequestImage(textureID, new TextureDownloadCallback()
        			{
						@Override
						public void callback(TextureRequestState state, AssetTexture assetTexture)
                        {
                            if (state == TextureRequestState.Finished)
                            {
                                assetTexture.Decode();

                                for (int i = 0; i < Textures.length; i++)
                                {
                                    if (Textures[i].TextureID == textureID)
                                        Textures[i].Texture = assetTexture;
                                }
                            }
                            else
                            {
                                Logger.Log("Texture " + textureID + " failed to download, one or more bakes will be incomplete", LogLevel.Warning, _Client);
                            }
                            latch.countDown();
                        }
       			});
        		}
        	});
        }

        try {
            latch.await(TEXTURE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {}
        executor.shutdown();
    }

    /** 
     * Blocking method to create and upload baked textures for all of the missing bakes
     *
     * @returns True on success, otherwise false
     */
    private boolean CreateBakes()
    {
        List<BakeType> pendingBakes = new ArrayList<BakeType>();

        // Check each bake layer in the Textures array for missing bakes
        for (int bakedIndex = 0; bakedIndex < BAKED_TEXTURE_COUNT; bakedIndex++)
        {
        	BakeType type = BakeType.setValue(bakedIndex);
            AvatarTextureIndex textureIndex = BakeTypeToAgentTextureIndex(type);

            if (Textures[textureIndex.getValue()].TextureID.equals(UUID.Zero))
            {
                // If this is the skirt layer and we're not wearing a skirt then skip it
                if (type == BakeType.Skirt && !Wearables.containsKey(WearableType.Skirt))
                    continue;

                pendingBakes.add(BakeType.setValue(bakedIndex));
            }
        }

        final AtomicBoolean success = new AtomicBoolean(true);
        if (pendingBakes.size() > 0)
        {
            DownloadTextures(pendingBakes);

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(pendingBakes.size(), MAX_CONCURRENT_UPLOADS));
            for (final BakeType bakeType : pendingBakes)
            {
            	executor.submit(new Runnable()
            	{
            		@Override
            		public void run()
            		{
            			if (!CreateBake(bakeType))
            				success.set(false);
            		}
            	});
            }
        }

        // Free up all the textures we're holding on to
        for (int i = 0; i < Textures.length; i++)
        {
            Textures[i].Texture = null;
        }

        // We just allocated and freed a ridiculous amount of memory while baking. Signal to the GC to clean up
        Runtime.getRuntime().gc();

        return success.get();
    }

    /** 
     * Blocking method to create and upload a baked texture for a single bake layer
     *
     * @param bakeType Layer to bake
     * @returns True on success, otherwise false
     */
    private boolean CreateBake(BakeType bakeType)
    {
        List<AvatarTextureIndex> textureIndices = BakeTypeToTextures(bakeType);
        Baker oven = new Baker(bakeType);

        for (int i = 0; i < textureIndices.size(); i++)
        {
            AvatarTextureIndex textureIndex = textureIndices.get(i);
            TextureData texture = Textures[AvatarTextureIndex.getValue(textureIndex)];
            texture.TextureIndex = textureIndex;

            oven.AddTexture(texture);
        }

        long start = System.currentTimeMillis();;
        oven.Bake();
        Logger.DebugLog("Baking " + bakeType + " took " + (System.currentTimeMillis() - start) + "ms");

        UUID newAssetID = UUID.Zero;
        int retries = UPLOAD_RETRIES;

        while (newAssetID.equals(UUID.Zero) && retries > 0)
        {
            try
			{
				newAssetID = UploadBake(oven.getBakedTexture().AssetData);
			}
			catch (IOException e)
			{
				return false;
			}
			catch (InterruptedException e)
			{
				return false;
			}
            --retries;
        }

        Textures[AvatarTextureIndex.getValue(BakeTypeToAgentTextureIndex(bakeType))].TextureID = newAssetID;

        if (newAssetID == UUID.Zero)
        {
            Logger.Log("Failed uploading bake " + bakeType, LogLevel.Warning, _Client);
            return false;
        }
        return true;
    }

    /** 
     * Blocking method to upload a baked texture
     * 
     * @param textureData Five channel JPEG2000 texture data to upload
     * @returns UUID of the newly created asset on success, otherwise UUID.Zero
     * @throws IOException 
     * @throws InterruptedException 
     */
    private UUID UploadBake(byte[] textureData) throws IOException, InterruptedException
    {
        final TimeoutEvent<UUID> uploadEvent = new TimeoutEvent<UUID>();

        _Client.Assets.RequestUploadBakedTexture(textureData, _Client.Assets.new BakedTextureUploadedCallback()
        {
            @Override
			public void callback(UUID newAssetID)
            {
                uploadEvent.set(newAssetID);
            }
        });

        // FIXME: evalute the need for timeout here, RequestUploadBakedTexture() will
        // timout either on Client.Settings.TRANSFER_TIMEOUT or Client.Settings.CAPS_TIMEOUT
        // depending on which upload method is used.
        UUID bakeID = uploadEvent.waitOne(UPLOAD_TIMEOUT);
        return bakeID != null ? bakeID : UUID.Zero;
    }

    /**
     * Creates a dictionary of visual param values from the downloaded wearables
     * 
     * @returnsA dictionary of visual param indices mapping to visual param values for our agent that can be fed to the Baker class
     */
    private HashMap<Integer, Float> MakeParamValues()
    {
    	HashMap<Integer, Float> paramValues = new HashMap<Integer, Float>(VisualParams.Params.size());

        synchronized (Wearables)
        {
            for (Entry<Integer, VisualParam> kvp : VisualParams.Params.entrySet())
            {
                // Only Group-0 parameters are sent in AgentSetAppearance packets
                if (kvp.getValue().Group == 0)
                {
                    boolean found = false;
                    VisualParam vp = kvp.getValue();

                    // Try and find this value in our collection of downloaded wearables
                    for (WearableData data : Wearables.values())
                    {
                        if (data.Asset != null && data.Asset.Params.containsKey(vp.ParamID))
                        {
                            paramValues.put(vp.ParamID, data.Asset.Params.get(vp.ParamID));
                            found = true;
                            break;
                        }
                    }

                    // Use a default value if we don't have one set for it
                    if (!found) paramValues.put(vp.ParamID, vp.DefaultValue);
                }
            }
        }
        return paramValues;
    }

    /**
     * Create an AgentSetAppearance packet from Wearables data and the Textures array and send it
     * 
     * @throws Exception 
     */
    private void RequestAgentSetAppearance() throws Exception
    {
        AgentSetAppearancePacket set = new AgentSetAppearancePacket();
        set.AgentData.AgentID = _Client.Self.getAgentID();
        set.AgentData.SessionID = _Client.Self.getSessionID();
        set.AgentData.SerialNum = SetAppearanceSerialNum.incrementAndGet();

        // Visual params used in the agent height calculation
        float agentSizeVPHeight = 0.0f;
        float agentSizeVPHeelHeight = 0.0f;
        float agentSizeVPPlatformHeight = 0.0f;
        float agentSizeVPHeadSize = 0.5f;
        float agentSizeVPLegLength = 0.0f;
        float agentSizeVPNeckLength = 0.0f;
        float agentSizeVPHipLength = 0.0f;

        synchronized (Wearables)
        {
            // #region VisualParam

            int vpIndex = 0;
            int nrParams;
            boolean wearingPhysics = false;
            
            for (WearableData wearable : Wearables.values())
            {
                if (wearable.WearableType == WearableType.Physics)
                {
                    wearingPhysics = true;
                    break;
                }
            }

            if (wearingPhysics)
            {
                nrParams = 251;
            }
            else
            {
                nrParams = 218;
            }

            set.ParamValue = new byte[nrParams];

            for (Entry<Integer, VisualParam> kvp : VisualParams.Params.entrySet())
            {
                VisualParam vp = kvp.getValue();
                float paramValue = 0f;
                boolean found = false;

                // Try and find this value in our collection of downloaded wearables
                for (WearableData data : Wearables.values())
                {
                    if (data.Asset != null && data.Asset.Params.containsKey(vp.ParamID))
                    {
                    	paramValue = data.Asset.Params.get(vp.ParamID);
                        found = true;
                        break;
                    }
                }

                // Use a default value if we don't have one set for it
                if (!found)
                    paramValue = vp.DefaultValue;

                // Only Group-0 parameters are sent in AgentSetAppearance packets
                if (kvp.getValue().Group == 0)
                {
                    set.ParamValue[vpIndex] = Helpers.FloatToByte(paramValue, vp.MinValue, vp.MaxValue);
                    ++vpIndex;
                }

                // Check if this is one of the visual params used in the agent height calculation
                switch (vp.ParamID)
                {
                    case 33:
                        agentSizeVPHeight = paramValue;
                        break;
                    case 198:
                        agentSizeVPHeelHeight = paramValue;
                        break;
                    case 503:
                        agentSizeVPPlatformHeight = paramValue;
                        break;
                    case 682:
                        agentSizeVPHeadSize = paramValue;
                        break;
                    case 692:
                        agentSizeVPLegLength = paramValue;
                        break;
                    case 756:
                        agentSizeVPNeckLength = paramValue;
                        break;
                    case 842:
                        agentSizeVPHipLength = paramValue;
                        break;
                }

                if (vpIndex == nrParams) break;
            }

            MyVisualParameters = new byte[set.ParamValue.length];
            System.arraycopy(MyVisualParameters, 0, set.ParamValue, 0, set.ParamValue.length);
            // #endregion VisualParam

            // #region TextureEntry

            TextureEntry te = new TextureEntry(DEFAULT_AVATAR_TEXTURE);

            for (int i = 0; i < Textures.length; i++)
            {
                TextureEntry.TextureEntryFace face = te.CreateFace(i);
                if ((i == 0 || i == 5 || i == 6) && !_Client.Settings.CLIENT_IDENTIFICATION_TAG.equals(UUID.Zero))
                {
                    face.setTextureID(_Client.Settings.CLIENT_IDENTIFICATION_TAG);
                    Logger.DebugLog("Sending client identification tag: " + _Client.Settings.CLIENT_IDENTIFICATION_TAG, _Client);
                }
                else if (Textures[i].TextureID != UUID.Zero)
                {
                    face.setTextureID(Textures[i].TextureID);
                    Logger.DebugLog("Sending texture entry for " + i + " to " + Textures[i].TextureID, _Client);
                }
            }

            set.ObjectData.setTextureEntry(te.GetBytes());
            MyTextures = te;

            // #endregion TextureEntry

            // #region WearableData

            set.WearableData = new AgentSetAppearancePacket.WearableDataBlock[BAKED_TEXTURE_COUNT];

            // Build hashes for each of the bake layers from the individual components
            for (int bakedIndex = 0; bakedIndex < BAKED_TEXTURE_COUNT; bakedIndex++)
            {
                UUID hash = UUID.Zero;

                for (int wearableIndex = 0; wearableIndex < WEARABLES_PER_LAYER; wearableIndex++)
                {
                    WearableType type = WEARABLE_BAKE_MAP[bakedIndex][wearableIndex];

                    WearableData wearable;
                    if (type != WearableType.Invalid && Wearables.containsKey(type))
                    {
                    	wearable = Wearables.get(type);
                        hash = UUID.XOr(hash, wearable.AssetID);
                    }
                }

                if (!hash.equals(UUID.Zero))
                {
                    // Hash with our magic value for this baked layer
                    hash = UUID.XOr(hash, BAKED_TEXTURE_HASH[bakedIndex]);
                }

                // Tell the server what cached texture assetID to use for each bake layer
                set.WearableData[bakedIndex] = set.new WearableDataBlock();
                set.WearableData[bakedIndex].TextureIndex = BakeIndexToTextureIndex[bakedIndex];
                set.WearableData[bakedIndex].CacheID = hash;
                Logger.DebugLog("Sending TextureIndex " + BakeType.setValue(bakedIndex) + " with CacheID " + hash, _Client);
            }

            // #endregion WearableData

            // #region Agent Size

            // Takes into account the Shoe Heel/Platform offsets but not the HeadSize offset. Seems to work.
            double agentSizeBase = 1.706;

            // The calculation for the HeadSize scalar may be incorrect, but it seems to work
            double agentHeight = agentSizeBase + (agentSizeVPLegLength * .1918) + (agentSizeVPHipLength * .0375) +
                (agentSizeVPHeight * .12022) + (agentSizeVPHeadSize * .01117) + (agentSizeVPNeckLength * .038) +
                (agentSizeVPHeelHeight * .08) + (agentSizeVPPlatformHeight * .07);

            set.AgentData.Size = new Vector3(0.45f, 0.6f, (float)agentHeight);

            // #endregion Agent Size

            if (_Client.Settings.AVATAR_TRACKING)
            {
                Avatar me;
                if (_Client.Network.getCurrentSim().getObjectsAvatars().containsKey(_Client.Self.getLocalID()))
                {
                    me = _Client.Network.getCurrentSim().getObjectsAvatars().get(_Client.Self.getLocalID());
                	me.Textures = MyTextures;
                    me.VisualParameters = MyVisualParameters;
                }
            }
        }
        _Client.Network.SendPacket(set);
        Logger.DebugLog("Send AgentSetAppearance packet");
    }

    private void DelayedRequestSetAppearance()
    {
        if (RebakeScheduleTimer == null)
        {
            RebakeScheduleTimer = new Timer();
        }
       	RebakeScheduleTimer.schedule(new TimerTask()
       	{
			@Override
			public void run()
			{
		        RequestSetAppearance(true);
			}
       	}, REBAKE_DELAY);
    }
    // #endregion Appearance Helpers

    // #region Inventory Helpers

    private boolean GetFolderWearables(String[] folderPath, List<InventoryWearable> wearables, List<InventoryItem> attachments) throws Exception
    {
        UUID folder = _Client.Inventory.FindObjectByPath(
            _Client.Inventory.getRootNode(false).itemID, _Client.Self.getAgentID(), Helpers.join("/", folderPath), INVENTORY_TIMEOUT);

        if (folder != UUID.Zero)
        {
            return GetFolderWearables(folder, wearables, attachments);
        }

        Logger.Log("Failed to resolve outfit folder path " + folderPath, LogLevel.Error, _Client);
        return false;
    }

    private boolean GetFolderWearables(UUID folder, List<InventoryWearable> wearables, List<InventoryItem> attachments) throws InventoryException, Exception
    {
        List<InventoryNode> objects = _Client.Inventory.FolderContents(folder, _Client.Self.getAgentID(), false, true,
            InventorySortOrder.ByName, INVENTORY_TIMEOUT);

        if (objects != null)
        {
            for (InventoryNode ib : objects)
            {
                if (ib instanceof InventoryWearable)
                {
                    Logger.DebugLog("Adding wearable " + ib.name, _Client);
                    wearables.add((InventoryWearable)ib);
                }
                else if (ib instanceof InventoryAttachment)
                {
                    Logger.DebugLog("Adding attachment (attachment) " + ib.name, _Client);
                    attachments.add((InventoryItem)ib);
                }
                else if (ib instanceof InventoryObject)
                {
                    Logger.DebugLog("Adding attachment (object) " + ib.name, _Client);
                    attachments.add((InventoryItem)ib);
                }
                else
                {
                    Logger.DebugLog("Ignoring inventory item " + ib.name, _Client);
                }
            }
        }
        else
        {
            Logger.Log("Failed to download folder contents of + " + folder, LogLevel.Error, _Client);
            return false;
        }

        return true;
    }

    // #endregion Inventory Helpers

    // #region Callbacks

    private void HandleAgentWearablesUpdate(Packet packet, Simulator simulator)
    {
        boolean changed = false;
        AgentWearablesUpdatePacket update = (AgentWearablesUpdatePacket)packet;

        synchronized (Wearables)
        {
            // #region Test if anything changed in this update

            for (int i = 0; i < update.WearableData.length; i++)
            {
                AgentWearablesUpdatePacket.WearableDataBlock block = update.WearableData[i];

                if (!block.AssetID.equals(UUID.Zero))
                {
                    if (Wearables.containsKey(WearableType.setValue(block.WearableType)))
                    {
                        WearableData wearable = Wearables.get(WearableType.setValue(block.WearableType));
                        if (!wearable.AssetID.equals(block.AssetID) || !wearable.ItemID.equals(block.ItemID))
                        {
                            // A different wearable is now set for this index
                            changed = true;
                            break;
                        }
                    }
                    else
                    {
                        // A wearable is now set for this index
                        changed = true;
                        break;
                    }
                }
                else if (Wearables.containsKey(WearableType.setValue(block.WearableType)))
                {
                    // This index is now empty
                    changed = true;
                    break;
                }
            }

            // #endregion Test if anything changed in this update

            if (changed)
            {
                Logger.DebugLog("New wearables received in AgentWearablesUpdate");
                Wearables.clear();

                for (int i = 0; i < update.WearableData.length; i++)
                {
                    AgentWearablesUpdatePacket.WearableDataBlock block = update.WearableData[i];

                    if (block.AssetID != UUID.Zero)
                    {
                        WearableType type = WearableType.setValue(block.WearableType);

                        WearableData data = new WearableData();
                        data.Asset = null;
                        data.AssetID = block.AssetID;
                        data.AssetType = WearableTypeToAssetType(type);
                        data.ItemID = block.ItemID;
                        data.WearableType = type;

                        // Add this wearable to our collection
                        Wearables.put(type, data);
                    }
                }
            }
            else
            {
                Logger.DebugLog("Duplicate AgentWearablesUpdate received, discarding");
            }
        }

        if (changed)
        {
            // Fire the callback
            OnAgentWearablesReply.dispatch(new AgentWearablesReplyCallbackArgs());
        }
    }

    private void HandleRebakeAvatarTextures(Packet packet, Simulator simulator)
    {
        RebakeAvatarTexturesPacket rebake = (RebakeAvatarTexturesPacket)packet;

        // allow the library to do the rebake
        if (_Client.Settings.SEND_AGENT_APPEARANCE)
        {
            RequestSetAppearance(true);
        }

        OnRebakeAvatarReply.dispatch(new RebakeAvatarTexturesCallbackArgs(rebake.TextureID));
    }

    private void HandleAgentCachedTextureResponse(Packet packet, Simulator simulator) throws UnsupportedEncodingException
    {
        AgentCachedTextureResponsePacket response = (AgentCachedTextureResponsePacket)packet;

        for (int i = 0; i < response.WearableData.length; i++)
        {
            AgentCachedTextureResponsePacket.WearableDataBlock block = response.WearableData[i];
            BakeType bakeType = BakeType.setValue(block.TextureIndex);
            AvatarTextureIndex index = BakeTypeToAgentTextureIndex(bakeType);

            Logger.DebugLog("Cache response for " + bakeType + ", TextureID=" + block.TextureID, _Client);

            if (!block.TextureID.equals(UUID.Zero))
            {
                // A simulator has a cache of this bake layer

                // FIXME: Use this. Right now we don't bother to check if this is a foreign host
                String host = Helpers.BytesToString(block.getHostName());

                Textures[index.getValue()].TextureID = block.TextureID;
            }
            else
            {
                // The server does not have a cache of this bake layer
                // FIXME:
            }
        }

        OnAgentCachedBakesReply.dispatch(new AgentCachedBakesReplyCallbackArgs());
    }

    private class Network_OnEventQueueRunning implements Callback<EventQueueRunningCallbackArgs>
    {
		@Override
		public boolean callback(EventQueueRunningCallbackArgs e)
		{
	        if (e.getSimulator() == _Client.Network.getCurrentSim() && _Client.Settings.SEND_AGENT_APPEARANCE)
	        {
	            // Update appearance each time we enter a new sim and capabilities have been retrieved
	            RequestSetAppearance();
	        }
            return false;
		}
    }

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs>
	{
		@Override
		public boolean callback(DisconnectedCallbackArgs e)
		{
	        if (RebakeScheduleTimer != null)
	        {
	            RebakeScheduleTimer.cancel();
	            RebakeScheduleTimer = null;
	        }

	        if (AppearanceThread != null)
	        {
	            if (AppearanceThread.isAlive())
	            {
	                AppearanceThread.stop();
	            }
	            AppearanceThread = null;
	            AppearanceThreadRunning.set(false);
	        }
	        return false;
		}
    }

    // #endregion Callbacks

    // #region Static Helpers

    /**
     * Converts a WearableType to a bodypart or clothing WearableType
     * 
     * @param type A WearableType
     * @returns AssetType.Bodypart or AssetType.Clothing or AssetType.Unknown
     */
    public static AssetType WearableTypeToAssetType(WearableType type)
    {
        switch (type)
        {
            case Shape:
            case Skin:
            case Hair:
            case Eyes:
                return AssetType.Bodypart;
            case Shirt:
            case Pants:
            case Shoes:
            case Socks:
            case Jacket:
            case Gloves:
            case Undershirt:
            case Underpants:
            case Skirt:
            case Tattoo:
            case Alpha:
            case Physics:
                return AssetType.Clothing;
            default:
                return AssetType.Unknown;
        }
    }

    /** 
     * Converts a BakeType to the corresponding baked texture slot in AvatarTextureIndex
     * 
     * @param index A BakeType
     * @returns The AvatarTextureIndex slot that holds the given BakeType
     */
    public static AvatarTextureIndex BakeTypeToAgentTextureIndex(BakeType index)
    {
        switch (index)
        {
            case Head:
                return AvatarTextureIndex.HeadBaked;
            case UpperBody:
                return AvatarTextureIndex.UpperBaked;
            case LowerBody:
                return AvatarTextureIndex.LowerBaked;
            case Eyes:
                return AvatarTextureIndex.EyesBaked;
            case Skirt:
                return AvatarTextureIndex.SkirtBaked;
            case Hair:
                return AvatarTextureIndex.HairBaked;
            default:
                return AvatarTextureIndex.Unknown;
        }
    }

    /** 
     * Gives the layer number that is used for morph mask
     * 
     * @param bakeType >A BakeType
     * @returns Which layer number as defined in BakeTypeToTextures is used for morph mask
     */
    public static AvatarTextureIndex MorphLayerForBakeType(BakeType bakeType)
    {
        // Indexes return here correspond to those returned
        // in BakeTypeToTextures(), those two need to be in sync.
        // Which wearable layer is used for morph is defined in avatar_lad.xml
        // by looking for <layer> that has <morph_mask> defined in it, and
        // looking up which wearable is defined in that layer. Morph mask
        // is never combined, it's always a straight copy of one single clothing
        // item's alpha channel per bake.
        switch (bakeType)
        {
            case Head:
                return AvatarTextureIndex.Hair; // hair
            case UpperBody:
                return AvatarTextureIndex.UpperShirt; // shirt
            case LowerBody:
                return AvatarTextureIndex.LowerPants; // lower pants
            case Skirt:
                return AvatarTextureIndex.Skirt; // skirt
            case Hair:
                return AvatarTextureIndex.Hair; // hair
            default:
                return AvatarTextureIndex.Unknown;
        }
    }

    /**
     * Converts a BakeType to a list of the texture slots that make up that bake
     * 
     * @param bakeType A BakeType
     * @returns A list of texture slots that are inputs for the given bake
     */
    public static List<AvatarTextureIndex> BakeTypeToTextures(BakeType bakeType)
    {
        List<AvatarTextureIndex> textures = new ArrayList<AvatarTextureIndex>();

        switch (bakeType)
        {
            case Head:
                textures.add(AvatarTextureIndex.HeadBodypaint);
                textures.add(AvatarTextureIndex.HeadTattoo);
                textures.add(AvatarTextureIndex.Hair);
                textures.add(AvatarTextureIndex.HeadAlpha);
                break;
            case UpperBody:
                textures.add(AvatarTextureIndex.UpperBodypaint);
                textures.add(AvatarTextureIndex.UpperTattoo);
                textures.add(AvatarTextureIndex.UpperGloves);
                textures.add(AvatarTextureIndex.UpperUndershirt);
                textures.add(AvatarTextureIndex.UpperShirt);
                textures.add(AvatarTextureIndex.UpperJacket);
                textures.add(AvatarTextureIndex.UpperAlpha);
                break;
            case LowerBody:
                textures.add(AvatarTextureIndex.LowerBodypaint);
                textures.add(AvatarTextureIndex.LowerTattoo);
                textures.add(AvatarTextureIndex.LowerUnderpants);
                textures.add(AvatarTextureIndex.LowerSocks);
                textures.add(AvatarTextureIndex.LowerShoes);
                textures.add(AvatarTextureIndex.LowerPants);
                textures.add(AvatarTextureIndex.LowerJacket);
                textures.add(AvatarTextureIndex.LowerAlpha);
                break;
            case Eyes:
                textures.add(AvatarTextureIndex.EyesIris);
                textures.add(AvatarTextureIndex.EyesAlpha);
                break;
            case Skirt:
                textures.add(AvatarTextureIndex.Skirt);
                break;
            case Hair:
                textures.add(AvatarTextureIndex.Hair);
                textures.add(AvatarTextureIndex.HairAlpha);
                break;
        }
        return textures;
    }
    // #endregion Static Helpers
}