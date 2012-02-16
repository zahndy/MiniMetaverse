/**
 Copyright (c) 2006, Second Life Reverse Engineering Team
 Portions Copyright (c) 2006, Lateral Arts Limited
 All rights reserved.

 - Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Neither the name of the Second Life Reverse Engineering Team or Lateral Arts Limited
 nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 $Id: InventoryManager.java,v 1.1 2007/01/28 14:08:51 nuage Exp $
 */
package libomv.inventory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.tree.TreeModel;

import org.apache.http.client.HttpResponseException;
import org.apache.http.nio.concurrent.FutureCallback;

import libomv.AgentManager.InstantMessage;
import libomv.AgentManager.InstantMessageCallbackArgs;
import libomv.AgentManager.InstantMessageDialog;
import libomv.AgentManager.InstantMessageOnline;
import libomv.GridClient;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginResponseData;
import libomv.LoginManager.LoginStatus;
import libomv.ObjectManager.SaleType;
import libomv.Simulator;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetManager.XferReceivedCallbackArgs;
import libomv.assets.AssetWearable.WearableType;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsClient;
import libomv.capabilities.CapsMessage.BulkUpdateInventoryMessage;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.CopyInventoryFromNotecardMessage;
import libomv.capabilities.CapsMessage.ScriptRunningReplyMessage;
import libomv.capabilities.CapsMessage.UpdateScriptTaskUpdateMessage;
import libomv.capabilities.IMessage;
import libomv.inventory.InventoryFolder;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.packets.BulkUpdateInventoryPacket;
import libomv.packets.CopyInventoryFromNotecardPacket;
import libomv.packets.CopyInventoryItemPacket;
import libomv.packets.CreateInventoryFolderPacket;
import libomv.packets.CreateInventoryItemPacket;
import libomv.packets.DeRezObjectPacket;
import libomv.packets.FetchInventoryDescendentsPacket;
import libomv.packets.FetchInventoryPacket;
import libomv.packets.FetchInventoryReplyPacket;
import libomv.packets.GetScriptRunningPacket;
import libomv.packets.ImprovedInstantMessagePacket;
import libomv.packets.InventoryDescendentsPacket;
import libomv.packets.MoveInventoryFolderPacket;
import libomv.packets.MoveInventoryItemPacket;
import libomv.packets.MoveTaskInventoryPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PurgeInventoryDescendentsPacket;
import libomv.packets.RemoveInventoryObjectsPacket;
import libomv.packets.RemoveTaskInventoryPacket;
import libomv.packets.ReplyTaskInventoryPacket;
import libomv.packets.RequestTaskInventoryPacket;
import libomv.packets.RezObjectPacket;
import libomv.packets.RezRestoreToWorldPacket;
import libomv.packets.RezScriptPacket;
import libomv.packets.SaveAssetIntoInventoryPacket;
import libomv.packets.SetScriptRunningPacket;
import libomv.packets.UpdateCreateInventoryItemPacket;
import libomv.packets.UpdateInventoryFolderPacket;
import libomv.packets.UpdateInventoryItemPacket;
import libomv.packets.UpdateTaskInventoryPacket;
import libomv.packets.LinkInventoryItemPacket;
import libomv.types.Permissions;
import libomv.types.Permissions.PermissionMask;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.RefObject;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;

/* Tools for dealing with agents inventory */
public class InventoryManager implements PacketCallback, CapsCallback
{
	// [Flags]
	public static class InventorySortOrder
	{
		/* Sort by name */
		public static final byte ByName = 0;
		/* Sort by date */
		public static final byte ByDate = 1;
		/*
		 * Sort folders by name, regardless of whether items are sorted by name
		 * or date
		 */
		public static final byte FoldersByName = 2;
		/* Place system folders at the top */
		public static final byte SystemFoldersToTop = 4;

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

	/* Possible destinations for DeRezObject request */
	public enum DeRezDestination
	{
		/* */
		AgentInventorySave,
		/* Copy from in-world to agent inventory */
		AgentInventoryCopy,
		/* Derez to TaskInventory */
		TaskInventory,
		/* */
		Attachment,
		/* Take Object */
		AgentInventoryTake,
		/* */
		ForceToGodInventory,
		/* Delete Object */
		TrashFolder,
		/* Put an avatar attachment into agent inventory */
		AttachmentToInventory,
		/* */
		AttachmentExists,
		/* Return an object back to the owner's inventory */
		ReturnToOwner,
		/* Return a deeded object back to the last owner's inventory */
		ReturnToLastOwner;

		public static DeRezDestination setValue(int value)
		{
			return values()[value];
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/* Used for converting shadow_id to asset_id */
	public static final UUID MAGIC_ID = new UUID("3c115e51-04f4-523c-9fa6-98aff1034730");

	protected final class InventorySearch
	{
		public UUID Folder;
		public UUID Owner;
		public String[] Path;
		public int Level;
	}

	// #region Delegates

	public CallbackHandler<ItemReceivedCallbackArgs> OnItemReceived = new CallbackHandler<ItemReceivedCallbackArgs>();

	public CallbackHandler<FolderUpdatedCallbackArgs> OnFolderUpdated = new CallbackHandler<FolderUpdatedCallbackArgs>();

	public CallbackHandler<InventoryObjectOfferedCallbackArgs> OnInventoryObjectOffered = new CallbackHandler<InventoryObjectOfferedCallbackArgs>();

	public CallbackHandler<TaskItemReceivedCallbackArgs> OnTaskItemReceived = new CallbackHandler<TaskItemReceivedCallbackArgs>();

	public CallbackHandler<FindObjectByPathReplyCallbackArgs> OnFindObjectByPathReply = new CallbackHandler<FindObjectByPathReplyCallbackArgs>();

	public CallbackHandler<TaskInventoryReplyCallbackArgs> OnTaskInventoryReply = new CallbackHandler<TaskInventoryReplyCallbackArgs>();

	public CallbackHandler<SaveAssetToInventoryCallbackArgs> OnSaveAssetToInventory = new CallbackHandler<SaveAssetToInventoryCallbackArgs>();

	public CallbackHandler<ScriptRunningReplyCallbackArgs> OnScriptRunningReply = new CallbackHandler<ScriptRunningReplyCallbackArgs>();

	/**
	 * Callback for inventory item creation finishing
	 * 
	 * @param success
	 *            Whether the request to create an inventory item succeeded or
	 *            not
	 * @param item
	 *            Inventory item being created. If success is false this will be
	 *            null
	 */
	public CallbackHandler<ItemCreatedCallbackArgs> OnItemCreatedCallback = new CallbackHandler<ItemCreatedCallbackArgs>();

	public class ItemCreatedCallbackArgs implements CallbackArgs
	{
		boolean success;
		InventoryItem item;

		public boolean getSuccess()
		{
			return success;
		}

		public InventoryItem getInventoryItem()
		{
			return item;
		}

		public ItemCreatedCallbackArgs(boolean success, InventoryItem item)
		{
			this.success = success;
			this.item = item;
		}
	}

	private Hashtable<Integer, Callback<ItemCreatedCallbackArgs>> _ItemCreatedCallbacks = new Hashtable<Integer, Callback<ItemCreatedCallbackArgs>>();

	/**
	 * Callback for an inventory item being create from an uploaded asset
	 * 
	 * @param success
	 *            true if inventory item creation was successful
	 * @param status
	 * @param itemID
	 * @param assetID
	 */
	public CallbackHandler<ItemCreatedFromAssetCallbackArgs> OnItemCreatedFromAssetCallback = new CallbackHandler<ItemCreatedFromAssetCallbackArgs>();

	public class ItemCreatedFromAssetCallbackArgs implements CallbackArgs
	{
		boolean success;
		String status;
		UUID itemID;
		UUID assetID;

		public boolean getSuccess()
		{
			return success;
		}

		public String getStatus()
		{
			return status;
		}

		public UUID getItemID()
		{
			return itemID;
		}

		public UUID getAssetID()
		{
			return assetID;
		}

		public ItemCreatedFromAssetCallbackArgs(boolean success, String status, UUID itemID, UUID assetID)
		{
			this.success = success;
			this.status = status;
			this.itemID = itemID;
			this.assetID = assetID;
		}
	}

	/**
	 * Callback for an inventory item copying finished
	 * 
	 * @param item
	 *            InventoryItem being copied
	 */
	public CallbackHandler<ItemCopiedCallbackArgs> OnItemCopiedCallback = new CallbackHandler<ItemCopiedCallbackArgs>();

	public class ItemCopiedCallbackArgs implements CallbackArgs
	{
		InventoryItem item;

		public InventoryItem getInventoryItem()
		{
			return item;
		}

		public ItemCopiedCallbackArgs(InventoryItem item)
		{
			this.item = item;
		}
	}

	private Hashtable<Integer, Callback<ItemCopiedCallbackArgs>> _ItemCopiedCallbacks = new Hashtable<Integer, Callback<ItemCopiedCallbackArgs>>();

	private Object _CallbacksLock = new Object();
	private int _CallbackPos;
	// #endregion Delegates

	// #region String Arrays

	/* Partial mapping of AssetTypes to folder names */
	private static final String[] _NewFolderNames = new String[] { "Textures", "Sounds", "Calling Cards", "Landmarks",
			"Scripts", "Clothing", "Objects", "Notecards", "New Folder", "Inventory", "Scripts", "Scripts",
			"Uncompressed Images", "Body Parts", "Trash", "Photo Album", "Lost And Found", "Uncompressed Sounds",
			"Uncompressed Images", "Uncompressed Images", "Animations", "Gestures" };

	// #endregion String Arrays

	private GridClient _Client;
	private InventoryStore _Store;
	// private Random _RandNumbers = new Random();
	private ArrayList<InventorySearch> _Searches = new ArrayList<InventorySearch>();

	// #region Properties


	/* Get this agents Inventory data */
	public final InventoryFolder getRootNode(boolean library)
	{
		if (library)
			return _Store.getLibraryFolder();

		return _Store.getInventoryFolder();
	}

	public final TreeModel getTreeModel()
	{
		return _Store;
	}
	
	// #endregion Properties

	private Callback<InstantMessageCallbackArgs> instantMessageCallback;
	private Callback<LoginProgressCallbackArgs> loginProgressCallback;

	/**
	 * Default constructor
	 * 
	 * @param client
	 *            Reference to the GridClient object
	 */
	public InventoryManager(GridClient client)
	{
		_Client = client;

		// Watch for inventory given to us through instant message
		instantMessageCallback = new Self_InstantMessage();
		_Client.Self.OnInstantMessage.add(instantMessageCallback, false);

		// Register extra parameters with login and parse the inventory data
		// that comes back
		loginProgressCallback = new Network_OnLoginProgress();
		_Client.Login.RegisterLoginProgressCallback(loginProgressCallback, new String[] { "inventory-root",
				"inventory-skeleton", "inventory-lib-root", "inventory-lib-owner", "inventory-skel-lib" }, false);

		_Client.Network.RegisterCallback(PacketType.UpdateCreateInventoryItem, this);
		_Client.Network.RegisterCallback(PacketType.SaveAssetIntoInventory, this);
		_Client.Network.RegisterCallback(PacketType.BulkUpdateInventory, this);
        _Client.Network.RegisterCallback(CapsEventType.BulkUpdateInventory, this);
		_Client.Network.RegisterCallback(PacketType.MoveInventoryItem, this);
		_Client.Network.RegisterCallback(PacketType.InventoryDescendents, this);
		_Client.Network.RegisterCallback(PacketType.FetchInventoryReply, this);
		_Client.Network.RegisterCallback(PacketType.ReplyTaskInventory, this);

		_Client.Network.RegisterCallback(CapsEventType.ScriptRunningReply, this);
	}

	@Override
	protected void finalize() throws Throwable
	{
		_Client.Self.OnInstantMessage.remove(instantMessageCallback);
		_Client.Login.UnregisterLoginProgressCallback(loginProgressCallback);
		super.finalize();
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case UpdateCreateInventoryItem:
				HandleUpdateCreateInventoryItem(packet, simulator);
				break;
			case SaveAssetIntoInventory:
				HandleSaveAssetIntoInventory(packet, simulator);
				break;
			case BulkUpdateInventory:
				HandleBulkUpdateInventory(packet, simulator);
				break;
			case MoveInventoryItem:
				HandleMoveInventoryItem(packet, simulator);
				break;
			case InventoryDescendents:
				HandleInventoryDescendents(packet, simulator);
				break;
			case FetchInventoryReply:
				HandleFetchInventoryReply(packet, simulator);
				break;
			case ReplyTaskInventory:
				HandleReplyTaskInventory(packet, simulator);
				break;
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator) throws Exception
	{
		switch (message.getType())
		{
			case ScriptRunningReply:
				HandleScriptRunningReply(message, simulator);
			case BulkUpdateInventory:
				HandleBulkUpdateInventory(message, simulator);
		}
	}

	// #region Fetch

	/**
	 * Fetch an inventory item from the dataserver Items will also be sent to
	 * the {@link InventoryManager.OnItemReceived} event
	 * 
	 * @param itemID
	 *            The items {@link UUID}
	 * @param ownerID
	 *            The item Owners {@link OpenMetaverse.UUID}
	 * @param timeout
	 *            a integer representing the number of milliseconds to wait for
	 *            results
	 * @return An {@link InventoryItem} object on success, or null if no item
	 *         was found
	 * @throws Exception
	 */
	public final InventoryItem FetchItem(final UUID itemID, UUID ownerID, int timeout) throws Exception
	{
		final TimeoutEvent<InventoryItem> fetchEvent = new TimeoutEvent<InventoryItem>();

		final class FetchedItemsCallback implements Callback<ItemReceivedCallbackArgs>
		{
			@Override
			public boolean callback(ItemReceivedCallbackArgs e)
			{
				if (e.getItem().itemID.equals(itemID))
				{
					fetchEvent.set(e.getItem());
				}
				return false;
			}
		}

		Callback<ItemReceivedCallbackArgs> callback = new FetchedItemsCallback();

		OnItemReceived.add(callback, true);
		RequestFetchInventory(itemID, ownerID);
		InventoryItem item = fetchEvent.waitOne(timeout);
		OnItemReceived.remove(callback);
		return item;
	}

	/**
	 * Request A single inventory item {@link InventoryManager.OnItemReceived}
	 * 
	 * @param itemID
	 *            The items {@link OpenMetaverse.UUID}
	 * @param ownerID
	 *            The item Owners {@link OpenMetaverse.UUID}
	 * @throws Exception
	 */
	public final void RequestFetchInventory(UUID itemID, UUID ownerID) throws Exception
	{
		FetchInventoryPacket fetch = new FetchInventoryPacket();
		fetch.AgentData = fetch.new AgentDataBlock();
		fetch.AgentData.AgentID = _Client.Self.getAgentID();
		fetch.AgentData.SessionID = _Client.Self.getSessionID();

		fetch.InventoryData = new FetchInventoryPacket.InventoryDataBlock[1];
		fetch.InventoryData[0] = fetch.new InventoryDataBlock();
		fetch.InventoryData[0].ItemID = itemID;
		fetch.InventoryData[0].OwnerID = ownerID;

		_Client.Network.SendPacket(fetch);
	}

	/**
	 * Request inventory items {@link InventoryManager.OnItemReceived}
	 * 
	 * @param itemIDs
	 *            Inventory items to request
	 * @param ownerIDs
	 *            Owners of the inventory items
	 * @throws Exception
	 */
	public final void RequestFetchInventory(ArrayList<UUID> itemIDs, ArrayList<UUID> ownerIDs) throws Exception
	{
		if (itemIDs.size() != ownerIDs.size())
		{
			throw new IllegalArgumentException("itemIDs and ownerIDs must contain the same number of entries");
		}

		FetchInventoryPacket fetch = new FetchInventoryPacket();
		fetch.AgentData = fetch.new AgentDataBlock();
		fetch.AgentData.AgentID = _Client.Self.getAgentID();
		fetch.AgentData.SessionID = _Client.Self.getSessionID();

		fetch.InventoryData = new FetchInventoryPacket.InventoryDataBlock[itemIDs.size()];
		for (int i = 0; i < itemIDs.size(); i++)
		{
			fetch.InventoryData[i] = fetch.new InventoryDataBlock();
			fetch.InventoryData[i].ItemID = itemIDs.get(i);
			fetch.InventoryData[i].OwnerID = ownerIDs.get(i);
		}
		_Client.Network.SendPacket(fetch);
	}

	/**
	 * Get contents of a folder {@link InventoryManager.OnRequestFolderContents}
	 * InventoryFolder.DescendentCount will only be accurate if both folders and
	 * items are requested
	 * 
	 * @param folder
	 *            The {@link UUID} of the folder to search
	 * @param owner
	 *            The {@link UUID} of the folders owner
	 * @param folders
	 *            true to retrieve folders
	 * @param items
	 *            true to retrieve items
	 * @param order
	 *            sort order to return results in
	 * @param timeout
	 *            a integer representing the number of milliseconds to wait for
	 *            results
	 * @return A list of inventory items matching search criteria within folder
	 * @throws InventoryException
	 * @throws Exception
	 */
	public final ArrayList<InventoryNode> FolderContents(final UUID folderID, UUID ownerID, boolean folders, boolean items,
			byte order, int timeout) throws InventoryException, Exception
	{
		final TimeoutEvent<ArrayList<InventoryNode>> fetchEvent = new TimeoutEvent<ArrayList<InventoryNode>>();

		Callback<FolderUpdatedCallbackArgs> callback = new Callback<FolderUpdatedCallbackArgs>()
		{
			@Override
			public boolean callback(FolderUpdatedCallbackArgs e)
			{
				if (e.getFolderID().equals(folderID))
				{
					synchronized (_Store)
					{
						InventoryFolder folder = _Store.getFolder(folderID);
						ArrayList<InventoryNode> contents = folder.getContents();
						// FIXME: InventoryDescendentsHandler only stores correct
						// descendendCount if both folders and items are fetched.
						if (contents.size() >= folder.descendentCount)
						{
							fetchEvent.set(contents);
							return false;
						}
					}
				}
				return false;
			}
		};

		OnFolderUpdated.add(callback, true);
		RequestFolderContents(folderID, ownerID, folders, items, order);
		ArrayList<InventoryNode> contents = fetchEvent.waitOne(timeout);
		OnFolderUpdated.remove(callback);
		return contents;
	}

	/**
	 * Request the contents of an inventory folder
	 * {@link InventoryManager.FolderContents}
	 * 
	 * @param folder
	 *            The folder to search
	 * @param owner
	 *            The folder owner {@link UUID}
	 * @param folders
	 *            true to return {@link InventoryManager.InventoryFolder} s
	 *            contained in folder
	 * @param items
	 *            true to return {@link InventoryManager.InventoryItem} s
	 *            containd in folder
	 * @param order
	 *            the sort order to return items in
	 * @throws Exception
	 */
	public final void RequestFolderContents(UUID folder, UUID owner, boolean folders, boolean items, int order)
			throws Exception
	{
		FetchInventoryDescendentsPacket fetch = new FetchInventoryDescendentsPacket();
		fetch.AgentData.AgentID = _Client.Self.getAgentID();
		fetch.AgentData.SessionID = _Client.Self.getSessionID();

		fetch.InventoryData.FetchFolders = folders;
		fetch.InventoryData.FetchItems = items;
		fetch.InventoryData.FolderID = folder;
		fetch.InventoryData.OwnerID = owner;
		fetch.InventoryData.SortOrder = order;

		_Client.Network.SendPacket(fetch);
	}

    /**
     * Request the contents of an inventory folder using HTTP capabilities
     *
	 * @param folderID The folder to search
	 * @param ownerID The folder owners {@link libomv.types.UUID}
	 * @param fetchFolders true to return {@link InventoryManager.InventoryFolder}s contained in folder
	 * @param fetchItems true to return {@link  InventoryManager.InventoryItem}s contained in folder
	 * @param order the sort order to return items in {@link InvnetoryManager.InventorySortOrder}
	 * {@link InventoryManager.FolderContents}
	 */
    public final void RequestFolderContentsCap(final UUID folderID, UUID ownerID, boolean fetchFolders, boolean fetchItems, byte order)
    {
        URI url = _Client.Network.getCapabilityURI("FetchInventoryDescendents2");

        if (url == null)
        {
            Logger.Log("FetchInventoryDescendents2 capability not available in the current sim", LogLevel.Warning, _Client);
            return;
        }

        try
        {
            CapsClient request = new CapsClient();
            
            final class CapsCallback implements FutureCallback<OSD>
            {
            	@Override
            	public void completed(OSD result)
            	{
                    try
                    {
                        OSDArray fetchedFolders = (OSDArray)((OSDMap)result).get("folders");
                        UUID parentID = folderID;
                        InventoryFolder folder = null;
                        for (int i = 0; i < fetchedFolders.size(); i++)
                        {
                            OSDMap res = (OSDMap)fetchedFolders.get(i);
                            UUID folderID = res.get("folder_id").AsUUID();

                            folder = SafeCreateInventoryFolder(folderID, parentID, res.get("owner_id").AsUUID());
                            folder.descendentCount = res.get("descendents").AsInteger();
                            folder.version = res.get("version").AsInteger();
                            _Store.add(folder);

                            // Do we have any descendants
                            if (folder.descendentCount > 0)
                            {
                                // Fetch descendent folders
                                if (res.containsKey("categories"))
                                {
                                    InventoryFolder category = null;
                                    OSDArray folders = (OSDArray)res.get("categories");
                                    for (int j = 0; j < folders.size(); j++)
                                    {
                                        OSDMap descFolder = (OSDMap)folders.get(j);
                                        parentID = descFolder.get("parent_id").AsUUID();
                                        folderID = descFolder.get("category_id").AsUUID();
                                        category = SafeCreateInventoryFolder(folderID, parentID, descFolder.get("agent_id").AsUUID());
                                        category.name = descFolder.get("name").AsString();
                                        category.version = descFolder.get("version").AsInteger();
                                        category.preferredType = AssetType.setValue(descFolder.get("type_default").AsInteger());
                                        _Store.add(folder);
                                    }
                                }
                                // Fetch descendent items
                                if (res.containsKey("items"))
                                {
                                    OSDArray items = (OSDArray)res.get("items");
                                    for (int j = 0; j < items.size(); j++)
                                    {
                                        OSDMap descItem = (OSDMap)items.get(j);
                                        InventoryType type = InventoryType.setValue(descItem.get("inv_type").AsInteger());
                                        if (type == InventoryType.Texture && AssetType.setValue(descItem.get("type").AsInteger()) == AssetType.Object)
                                        {
                                            type = InventoryType.Attachment;
                                        }
                                        parentID = descItem.get("parent_id").AsUUID();
                                        InventoryItem item = SafeCreateInventoryItem(type, descItem.get("item_id").AsUUID(), parentID, descItem.get("agent_id").AsUUID());

                                        item.name = descItem.get("name").AsString();
                                        item.Description = descItem.get("desc").AsString();
                                        item.AssetID = descItem.get("asset_id").AsUUID();
                                        item.assetType = AssetType.setValue(descItem.get("type").AsInteger());
                                        item.CreationDate =  Helpers.UnixTimeToDateTime(descItem.get("created_at").AsReal());
                                        item.ItemFlags = descItem.get("flags").AsInteger();

                                        OSDMap perms = (OSDMap)descItem.get("permissions");
                                        item.CreatorID = perms.get("creator_id").AsUUID();
                                        item.LastOwnerID = perms.get("last_owner_id").AsUUID();
                                        item.Permissions = new Permissions(perms.get("base_mask").AsInteger(), perms.get("everyone_mask").AsInteger(), perms.get("group_mask").AsInteger(), perms.get("next_owner_mask").AsInteger(), perms.get("owner_mask").AsInteger());
                                        item.GroupOwned = perms.get("is_owner_group").AsBoolean();
                                        item.GroupID = perms.get("group_id").AsUUID();

                                        OSDMap sale = (OSDMap)descItem.get("sale_info");
                                        item.SalePrice = sale.get("sale_price").AsInteger();
                                        item.saleType = SaleType.setValue(sale.get("sale_type").AsInteger());

                                        _Store.add(item);
                                    }
                                }
                            }
                            OnFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(folderID));
                        }
                    }
                    catch (Exception ex)
                    {
                        Logger.Log("Failed to fetch inventory descendants for folder id " + folderID, LogLevel.Warning, _Client, ex);
                    }
            	}

				@Override
				public void cancelled()
				{
                    Logger.Log("Fetch inventory descendants canceled for folder id " + folderID, LogLevel.Warning, _Client);
				}

				@Override
				public void failed(Exception ex)
				{
                    Logger.Log("Failed to fetch inventory descendants for folder id " + folderID, LogLevel.Warning, _Client, ex);
				}
            };

            // Construct request
            OSDMap requestedFolder = new OSDMap(1);
            requestedFolder.put("folder_id", OSD.FromUUID(folderID));
            requestedFolder.put("owner_id", OSD.FromUUID(ownerID));
            requestedFolder.put("fetch_folders", OSD.FromBoolean(fetchFolders));
            requestedFolder.put("fetch_items", OSD.FromBoolean(fetchItems));
            requestedFolder.put("sort_order", OSD.FromInteger(InventorySortOrder.getValue(order)));

            OSDArray requestedFolders = new OSDArray(1);
            requestedFolders.add(requestedFolder);
            OSDMap req = new OSDMap(1);
            req.put("folders", requestedFolders);

            request.setResultCallback(new CapsCallback());
            request.executeHttpPost(url, req, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
        }
        catch (Exception ex)
        {
            Logger.Log("Failed to fetch inventory descendants for folder id " + folderID, LogLevel.Warning, _Client, ex);
            return;
        }
    }

    // #endregion Fetch

	// #region Find

	/**
	 * Returns the UUID of the folder (category) that defaults to containing
	 * 'type'. The folder is not necessarily only for that type
	 * 
	 * This will return the root folder if one does not exist
	 * 
	 * @param type
	 * @return The UUID of the desired folder if found, the UUID of the
	 *         RootFolder if not found, or UUID.Zero on failure
	 * @throws InventoryException
	 */
	public final InventoryFolder FindFolderForType(AssetType type) throws InventoryException
	{
		if (_Store == null)
		{
			Logger.Log("Inventory is null, FindFolderForType() lookup cannot continue", LogLevel.Error, _Client);
			return null;
		}

		synchronized (_Store)
		{
			// Folders go in the root
			if (type == AssetType.Folder)
			{
				return _Store.getInventoryFolder();
			}

			// Loop through each top-level directory and check if PreferredType
			// matches the requested type
			
			Iterator<InventoryNode> iter = _Store.getInventoryFolder().children.iterator();
			while (iter.hasNext())
			{
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder)
				{
					InventoryFolder folder = (InventoryFolder)node;

					if (folder.preferredType == type)
					{
						return folder;
					}
				}
			}

			// No match found, return Root Folder ID
			return _Store.getInventoryFolder();
		}
	}

	/**
	 * Find an object in inventory using a specific path to search
	 * 
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param inventoryOwner
	 *            The object owners {@link UUID}
	 * @param path
	 *            A string path to search
	 * @param timeout
	 *            milliseconds to wait for a reply
	 * @return Found items {@link UUID} or {@link UUID.Zero} if timeout occurs
	 *         or item is not found
	 * @throws Exception
	 */
	public final UUID FindObjectByPath(UUID baseFolder, UUID inventoryOwner, final String path, int timeout)
			throws Exception
	{
		final TimeoutEvent<UUID> findEvent = new TimeoutEvent<UUID>();

		Callback<FindObjectByPathReplyCallbackArgs> callback = new Callback<FindObjectByPathReplyCallbackArgs>()
		{
			@Override
			public boolean callback(FindObjectByPathReplyCallbackArgs e)
			{
				if (e.getPath() == path)
				{
					findEvent.set(e.getInventoryObjectID());
				}
				return false;
			}
		};

		OnFindObjectByPathReply.add(callback, true);
		RequestFindObjectByPath(baseFolder, inventoryOwner, path);
		UUID foundItem = findEvent.waitOne(timeout);
		OnFindObjectByPathReply.remove(callback);

		return foundItem == null ? UUID.Zero : foundItem;
	}

	/**
	 * Find inventory items by path
	 * 
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param inventoryOwner
	 *            The object owners {@link UUID}
	 * @param path
	 *            A string path to search, folders/objects separated by a '/'
	 *            Results are sent to the
	 *            {@link InventoryManager.OnFindObjectByPath} event
	 */
	public final void RequestFindObjectByPath(UUID baseFolder, UUID inventoryOwner, String path) throws Exception
	{
		if (path == null || path.length() == 0)
		{
			throw new IllegalArgumentException("Empty path is not supported");
		}

		// Store this search
		InventorySearch search = new InventorySearch();
		search.Folder = baseFolder;
		search.Owner = inventoryOwner;
		search.Path = path.split("/");
		search.Level = 0;
		synchronized (_Searches)
		{
			_Searches.add(search);
		}

		// Start the search
		RequestFolderContents(baseFolder, inventoryOwner, true, true, InventorySortOrder.ByName);
	}

	/**
	 * Search inventory Store object for an item or folder
	 * 
	 * @param baseFolder
	 *            The folder to begin the search in
	 * @param path
	 *            An array which creates a path to search
	 * @param level
	 *            Number of levels below baseFolder to conduct searches
	 * @param firstOnly
	 *            if True, will stop searching after first match is found
	 * @return A list of inventory items found
	 * @throws InventoryException
	 */
	public final ArrayList<InventoryNode> LocalFind(UUID baseFolder, String[] path, int level, boolean firstOnly)
			throws InventoryException
	{
		ArrayList<InventoryNode> objects = new ArrayList<InventoryNode>();
		synchronized (_Store)
		{
			ArrayList<InventoryNode> contents = _Store.getContents(baseFolder);

			for (InventoryNode inv : contents)
			{
				if (inv.name.equals(path[level]))
				{
					if (level == path.length - 1)
					{
						objects.add(inv);
						if (firstOnly)
						{
							return objects;
						}
					}
					else if (inv instanceof InventoryFolder)
					{
						objects.addAll(LocalFind(inv.itemID, path, level + 1, firstOnly));
					}
				}
			}
			return objects;
		}
	}

	// #endregion Find

	// #region Move/Rename

	/**
	 * Move an inventory item or folder to a new location
	 * 
	 * @param item
	 *            The {@link T:InventoryBase} item or folder to move
	 * @param newParent
	 *            The {@link T:InventoryFolder} to move item or folder to
	 * @throws Exception
	 */
	public final void Move(InventoryNode item, InventoryFolder newParent) throws Exception
	{
		if (item instanceof InventoryFolder)
		{
			MoveFolder(item.itemID, newParent.itemID);
		}
		else
		{
			MoveItem(item.itemID, newParent.itemID);
		}
	}

	/**
	 * Move an inventory item or folder to a new location and change its name
	 * 
	 * @param item
	 *            The {@link T:InventoryBase} item or folder to move
	 * @param newParent
	 *            The {@link T:InventoryFolder} to move item or folder to
	 * @param newName
	 *            The name to change the item or folder to
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void Move(InventoryNode item, InventoryFolder newParent, String newName)
			throws UnsupportedEncodingException, Exception
	{
		if (item instanceof InventoryFolder)
		{
			MoveFolder(item.itemID, newParent.itemID, newName);
		}
		else
		{
			MoveItem(item.itemID, newParent.itemID, newName);
		}
	}

	/**
	 * Move and rename a folder
	 * 
	 * @param folderID
	 *            The source folders {@link UUID}
	 * @param newparentID
	 *            The destination folders {@link UUID}
	 * @param newName
	 *            The name to change the folder to
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void MoveFolder(UUID folderID, UUID newparentID, String newName) throws UnsupportedEncodingException,
			Exception
	{
		UpdateFolderProperties(folderID, newparentID, newName, AssetType.Unknown);
	}

	/**
	 * Update folder properties
	 * 
	 * @param folderID
	 *            {@link UUID} of the folder to update
	 * @param parentID
	 *            Sets folder's parent to {@link UUID}
	 * @param name
	 *            Folder name
	 * @param type
	 *            Folder type
	 * @throws Exception
	 */
	public final void UpdateFolderProperties(UUID folderID, UUID parentID, String name, AssetType type)
			throws Exception
	{
		synchronized (_Store)
		{
			if (_Store.containsFolder(folderID))
			{
				InventoryFolder inv = _Store.getFolder(folderID);
				inv.name = name;
				inv.preferredType = type;
				_Store.add(parentID, inv);
			}
		}

		UpdateInventoryFolderPacket invFolder = new UpdateInventoryFolderPacket();
		invFolder.AgentData.AgentID = _Client.Self.getAgentID();
		invFolder.AgentData.SessionID = _Client.Self.getSessionID();
		invFolder.FolderData = new UpdateInventoryFolderPacket.FolderDataBlock[1];
		invFolder.FolderData[0] = invFolder.new FolderDataBlock();
		invFolder.FolderData[0].FolderID = folderID;
		invFolder.FolderData[0].ParentID = parentID;
		invFolder.FolderData[0].setName(Helpers.StringToBytes(name));
		invFolder.FolderData[0].Type = type.getValue();

		_Client.Network.SendPacket(invFolder);
	}

	/**
	 * Move a folder
	 * 
	 * @param folderID
	 *            The source folders {@link UUID}
	 * @param newParentID
	 *            The destination folders {@link UUID}
	 * @throws Exception
	 */
	public final void MoveFolder(UUID folderID, UUID newParentID) throws Exception
	{
		MoveInventoryFolderPacket move = new MoveInventoryFolderPacket();
		move.AgentData.AgentID = _Client.Self.getAgentID();
		move.AgentData.SessionID = _Client.Self.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryFolderPacket.InventoryDataBlock[1];
		move.InventoryData[0] = move.new InventoryDataBlock();
		move.InventoryData[0].FolderID = folderID;
		move.InventoryData[0].ParentID = newParentID;

		_Client.Network.SendPacket(move);

		synchronized (_Store)
		{
			if (_Store.containsFolder(folderID))
			{
				_Store.add(newParentID, _Store.getItem(folderID));
			}
		}
	}

	/**
	 * Move multiple folders, the keys in the Dictionary parameter, to a new
	 * parents, the value of that folder's key.
	 * 
	 * @param foldersNewParents
	 *            A Dictionary containing the {@link UUID} of the source as the
	 *            key, and the {@link UUID} of the destination as the value
	 * @throws Exception
	 */
	public final void MoveFolders(Hashtable<UUID, UUID> foldersNewParents) throws Exception
	{
		// TODO: Test if this truly supports multiple-folder move
		MoveInventoryFolderPacket move = new MoveInventoryFolderPacket();
		move.AgentData.AgentID = _Client.Self.getAgentID();
		move.AgentData.SessionID = _Client.Self.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryFolderPacket.InventoryDataBlock[foldersNewParents.size()];

		int index = 0;
		for (Entry<UUID, UUID> folder : foldersNewParents.entrySet())
		{
			MoveInventoryFolderPacket.InventoryDataBlock block = move.new InventoryDataBlock();
			block.FolderID = folder.getKey();
			block.ParentID = folder.getValue();
			move.InventoryData[index++] = block;
		}
		_Client.Network.SendPacket(move);

		// FIXME: Use two List<UUID> to stay consistent
		synchronized (_Store)
		{
			for (Entry<UUID, UUID> entry : foldersNewParents.entrySet())
			{
				if (_Store.containsFolder(entry.getKey()))
				{
					_Store.add(entry.getValue(), _Store.getItem(entry.getKey()));
				}
			}
		}		
	}

	/**
	 * Move an inventory item to a new folder
	 * 
	 * @param itemID
	 *            The {@link UUID} of the source item to move
	 * @param folderID
	 *            The {@link UUID} of the destination folder
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public final void MoveItem(UUID itemID, UUID folderID) throws UnsupportedEncodingException, Exception
	{
		MoveItem(itemID, folderID, Helpers.EmptyString);
	}

	/**
	 * Move and rename an inventory item
	 * 
	 * @param itemID
	 *            The {@link UUID} of the source item to move
	 * @param folderID
	 *            The {@link UUID} of the destination folder
	 * @param newName
	 *            The name to change the folder to
	 * @throws Exception
	 */
	public final void MoveItem(UUID itemID, UUID folderID, String newName) throws Exception
	{
		MoveInventoryItemPacket move = new MoveInventoryItemPacket();
		move.AgentData.AgentID = _Client.Self.getAgentID();
		move.AgentData.SessionID = _Client.Self.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryItemPacket.InventoryDataBlock[1];
		move.InventoryData[0] = move.new InventoryDataBlock();
		move.InventoryData[0].ItemID = itemID;
		move.InventoryData[0].FolderID = folderID;
		move.InventoryData[0].setNewName(Helpers.StringToBytes(newName));

		_Client.Network.SendPacket(move);

		// Update our local copy
		synchronized (_Store)
		{
			if (_Store.containsItem(itemID))
			{
				InventoryNode inv = _Store.getItem(itemID);
				inv.name = newName;
				_Store.add(folderID, inv);
			}
		}
	}

	/**
	 * Move multiple inventory items to new locations
	 * 
	 * @param itemsNewParents
	 *            A Dictionary containing the {@link UUID} of the source item as
	 *            the key, and the {@link UUID} of the destination folder as the
	 *            value
	 * @throws Exception
	 */
	public final void MoveItems(java.util.Hashtable<UUID, UUID> itemsNewParents) throws Exception
	{
		MoveInventoryItemPacket move = new MoveInventoryItemPacket();
		move.AgentData.AgentID = _Client.Self.getAgentID();
		move.AgentData.SessionID = _Client.Self.getSessionID();
		move.AgentData.Stamp = false; // FIXME: ??

		move.InventoryData = new MoveInventoryItemPacket.InventoryDataBlock[itemsNewParents.size()];

		int index = 0;
		for (Entry<UUID, UUID> entry : itemsNewParents.entrySet())
		{
			MoveInventoryItemPacket.InventoryDataBlock block = move.new InventoryDataBlock();
			block.ItemID = entry.getKey();
			block.FolderID = entry.getValue();
			block.setNewName(Helpers.EmptyBytes);
			move.InventoryData[index++] = block;
		}

		_Client.Network.SendPacket(move);

		// Update our local copy
		synchronized (_Store)
		{
			for (Entry<UUID, UUID> entry : itemsNewParents.entrySet())
			{
				if (_Store.containsItem(entry.getKey()))
				{
					_Store.add(entry.getValue(), _Store.getItem(entry.getKey()));
				}
			}
		}
	}

	// #endregion Move

	// #region Remove

	/**
	 * Remove descendants of a folder, basically emptying the folder
	 * 
	 * @param folder
	 *            The {@link UUID} of the folder
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void RemoveDescendants(UUID folderID) throws Exception, InventoryException
	{
		PurgeInventoryDescendentsPacket purge = new PurgeInventoryDescendentsPacket();
		purge.AgentData.AgentID = _Client.Self.getAgentID();
		purge.AgentData.SessionID = _Client.Self.getSessionID();
		purge.FolderID = folderID;
		_Client.Network.SendPacket(purge);

		// Update our local copy
		synchronized (_Store)
		{
			if (_Store.containsFolder(folderID))
			{
				Iterator<InventoryNode> iter = _Store.getFolder(folderID).children.iterator();
				while (iter.hasNext())
				{
					_Store.remove(iter.next());
				}
			}
		}
	}

	/**
	 * Remove a single item from inventory
	 * 
	 * @param item
	 *            The {@link UUID} of the inventory item to remove
	 * @throws Exception
	 */
	public final void RemoveItem(UUID item) throws Exception
	{
		ArrayList<UUID> items = new ArrayList<UUID>(1);
		items.add(item);

		Remove(items, null);
	}

	/**
	 * Remove a folder from inventory
	 * 
	 * @param folder
	 *            The {@link UUID} of the folder to remove
	 * @throws Exception
	 */
	public final void RemoveFolder(UUID folder) throws Exception
	{
		ArrayList<UUID> folders = new ArrayList<UUID>(1);
		folders.add(folder);

		Remove(null, folders);
	}

	/**
	 * Remove multiple items or folders from inventory
	 * 
	 * @param items
	 *            A List containing the {@link UUID} s of items to remove
	 * @param folders
	 *            A List containing the {@link UUID} s of the folders to remove
	 * @throws Exception
	 */
	public final void Remove(ArrayList<UUID> items, ArrayList<UUID> folders) throws Exception
	{
		if ((items == null || items.isEmpty()) && (folders == null || folders.isEmpty()))
		{
			return;
		}

		RemoveInventoryObjectsPacket rem = new RemoveInventoryObjectsPacket();
		rem.AgentData.AgentID = _Client.Self.getAgentID();
		rem.AgentData.SessionID = _Client.Self.getSessionID();

		if (items == null || items.isEmpty())
		{
			// To indicate that we want no items removed:
			rem.ItemID = new UUID[1];
			rem.ItemID[0] = UUID.Zero;
		}
		else
		{
			synchronized (_Store)
			{
				rem.ItemID = new UUID[items.size()];
				for (int i = 0; i < items.size(); i++)
				{
					UUID uuid = items.get(i);
					rem.ItemID[i] = uuid;

					// Update local copy
					if (_Store.containsItem(uuid))
					{
						_Store.remove(_Store.getItem(uuid));
					}
				}
			}
		}

		if (folders == null || folders.isEmpty())
		{
			// To indicate we want no folders removed:
			rem.FolderID = new UUID[1];
			rem.FolderID[0] = UUID.Zero;
		}
		else
		{
			synchronized (_Store)
			{
				rem.FolderID = new UUID[folders.size()];
				for (int i = 0; i < folders.size(); i++)
				{
					UUID uuid = folders.get(i);
					rem.FolderID[i] = uuid;

					// Update local copy
					if (_Store.containsFolder(uuid))
					{
						_Store.remove(_Store.getFolder(uuid));
					}
				}
			}
		}
		_Client.Network.SendPacket(rem);
	}

	/**
	 * Empty the Lost and Found folder
	 * 
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void EmptyLostAndFound() throws InventoryException, Exception
	{
		EmptySystemFolder(AssetType.LostAndFoundFolder);
	}

	/**
	 * Empty the Trash folder
	 * 
	 * @throws Exception
	 * @throws InventoryException
	 */
	public final void EmptyTrash() throws InventoryException, Exception
	{
		EmptySystemFolder(AssetType.TrashFolder);
	}

	/**
	 * Empty the Lost and Found folder
	 * 
	 * @param folderType
	 *            The type of folder to empty
	 * @throws Exception
	 * @throws InventoryException
	 */
	private void EmptySystemFolder(AssetType folderType) throws Exception, InventoryException
	{
		synchronized (_Store)
		{
			Iterator<InventoryNode> iter = _Store.getInventoryFolder().children.iterator();
			InventoryFolder folder = null;
			while (iter.hasNext())
			{
				InventoryNode node = iter.next();
				if (node.getType() == InventoryType.Folder)
				{
					if (((InventoryFolder)node).preferredType == folderType)
					{
						folder = (InventoryFolder)node;
					}
				}
			}

			if (folder != null)
			{
				iter = folder.children.iterator();

				ArrayList<UUID> remItems = new ArrayList<UUID>();
				ArrayList<UUID> remFolders = new ArrayList<UUID>();
				while (iter.hasNext())
				{
					InventoryNode node = iter.next();
					if (node.getType() == InventoryType.Folder)
					{
						remFolders.add(node.itemID);
					}
					else
					{
						remItems.add(node.itemID);
					}
				}
				Remove(remItems, remFolders);
			}
		}
	}

	// #endregion Remove

	// /#region Create

	/**
	 * 
	 * 
	 * @param parentFolder
	 * @param name
	 * @param description
	 * @param type
	 * @param assetTransactionID
	 *            Proper use is to upload the inventory's asset first, then
	 *            provide the Asset's TransactionID here.
	 * @param invType
	 * @param nextOwnerMask
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestCreateItem(UUID parentFolder, String name, String description, AssetType type,
			UUID assetTransactionID, InventoryType invType, int nextOwnerMask,
			Callback<ItemCreatedCallbackArgs> callback) throws Exception
	{
		// Even though WearableType.Shape, in this context it is treated as NOT_WEARABLE
		RequestCreateItem(parentFolder, name, description, type, assetTransactionID, invType, WearableType.Shape,
				nextOwnerMask, callback);
	}

	/**
	 * 
	 * 
	 * @param parentFolder
	 * @param name
	 * @param description
	 * @param type
	 * @param assetTransactionID
	 *            Proper use is to upload the inventory's asset first, then
	 *            provide the Asset's TransactionID here.
	 * @param invType
	 * @param wearableType
	 * @param nextOwnerMask
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestCreateItem(UUID parentFolder, String name, String description, AssetType type,
			UUID assetTransactionID, InventoryType invType, WearableType wearableType, int nextOwnerMask,
			Callback<ItemCreatedCallbackArgs> callback) throws Exception
	{
		CreateInventoryItemPacket create = new CreateInventoryItemPacket();
		create.AgentData.AgentID = _Client.Self.getAgentID();
		create.AgentData.SessionID = _Client.Self.getSessionID();

		create.InventoryBlock.CallbackID = RegisterItemCreatedCallback(callback);
		create.InventoryBlock.FolderID = parentFolder;
		create.InventoryBlock.TransactionID = assetTransactionID;
		create.InventoryBlock.NextOwnerMask = nextOwnerMask;
		create.InventoryBlock.Type = type.getValue();
		create.InventoryBlock.InvType = invType.getValue();
		create.InventoryBlock.WearableType = WearableType.getValue(wearableType);
		create.InventoryBlock.setName(Helpers.StringToBytes(name));
		create.InventoryBlock.setDescription(Helpers.StringToBytes(description));

		_Client.Network.SendPacket(create);
	}

	/**
	 * Creates a new inventory folder
	 * 
	 * @param parentID
	 *            ID of the folder to put this folder in
	 * @param name
	 *            Name of the folder to create
	 * @return The UUID of the newly created folder
	 * @throws Exception
	 */
	public final UUID CreateFolder(UUID parentID, String name) throws Exception
	{
		return CreateFolder(parentID, name, AssetType.Unknown);
	}

	/**
	 * Creates a new inventory folder If you specify a preferred type of
	 * <code>AsseType.Folder</code> it will create a new root folder which may
	 * likely cause all sorts of strange problems
	 * 
	 * @param parentID
	 *            ID of the folder to put this folder in
	 * @param name
	 *            Name of the folder to create
	 * @param preferredType
	 *            Sets this folder as the default folder for new assets of the
	 *            specified type. Use <code>AssetType.Unknown</code> to create a
	 *            normal folder, otherwise it will likely create a duplicate of
	 *            an existing folder type
	 * @return The UUID of the newly created folder
	 * @throws Exception
	 */
	public final UUID CreateFolder(UUID parentID, String name, AssetType preferredType) throws Exception
	{
		UUID id = new UUID();

		// Assign a folder name if one is not already set
		if (Helpers.isEmpty(name))
		{
			if (preferredType.getValue() >= AssetType.Texture.getValue()
					&& preferredType.getValue() <= AssetType.Gesture.getValue())
			{
				name = _NewFolderNames[preferredType.getValue()];
			}
			else
			{
				name = "New Folder";
			}
		}

		// Create the new folder locally
		InventoryFolder newFolder = new InventoryFolder(id, parentID, _Client.Self.getAgentID());
		newFolder.version = 1;
		newFolder.preferredType = preferredType;
		newFolder.name = name;

		// Update the local store
		_Store.add(newFolder);

		// Create the create folder packet and send it
		CreateInventoryFolderPacket create = new CreateInventoryFolderPacket();
		create.AgentData.AgentID = _Client.Self.getAgentID();
		create.AgentData.SessionID = _Client.Self.getSessionID();

		create.FolderData.FolderID = id;
		create.FolderData.ParentID = parentID;
		create.FolderData.Type = preferredType.getValue();
		create.FolderData.setName(Helpers.StringToBytes(name));

		_Client.Network.SendPacket(create);

		return id;
	}

	/**
	 * Create an inventory item and upload asset data
	 * 
	 * @param data
	 *            Asset data
	 * @param name
	 *            Inventory item name
	 * @param description
	 *            Inventory item description
	 * @param assetType
	 *            Asset type
	 * @param invType
	 *            Inventory type
	 * @param folderID
	 *            Put newly created inventory in this folder
	 * @param callback
	 *            Callback that will receive feedback on success or failure
	 * @throws Exception
	 */
	public final void RequestCreateItemFromAsset(byte[] data, String name, String description, AssetType assetType,
			InventoryType invType, UUID folderID, Callback<ItemCreatedFromAssetCallbackArgs> callback) throws Exception
	{
		Permissions permissions = new Permissions();
		permissions.EveryoneMask = PermissionMask.None;
		permissions.GroupMask = PermissionMask.None;
		permissions.NextOwnerMask = PermissionMask.All;

		RequestCreateItemFromAsset(data, name, description, assetType, invType, folderID, permissions, callback);
	}

	/**
	 * Create an inventory item and upload asset data
	 * 
	 * @param data
	 *            Asset data
	 * @param name
	 *            Inventory item name
	 * @param description
	 *            Inventory item description
	 * @param assetType
	 *            Asset type
	 * @param invType
	 *            Inventory type
	 * @param folderID
	 *            Put newly created inventory in this folder
	 * @param permissions
	 *            Permission of the newly created item (EveryoneMask, GroupMask,
	 *            and NextOwnerMask of Permissions struct are supported)
	 * @param callback
	 *            Delegate that will receive feedback on success or failure
	 * @throws Exception
	 */
	public final void RequestCreateItemFromAsset(byte[] data, String name, String description, AssetType assetType,
			InventoryType invType, UUID folderID, Permissions permissions,
			Callback<ItemCreatedFromAssetCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("NewFileAgentInventory");
		if (url != null)
		{
			OSDMap query = new OSDMap();
			query.put("folder_id", OSD.FromUUID(folderID));
			query.put("asset_type", OSD.FromString(assetType.toString()));
			query.put("inventory_type", OSD.FromString(invType.toString()));
			query.put("name", OSD.FromString(name));
			query.put("description", OSD.FromString(description));
			query.put("everyone_mask", OSD.FromInteger(permissions.EveryoneMask));
			query.put("group_mask", OSD.FromInteger(permissions.GroupMask));
			query.put("next_owner_mask", OSD.FromInteger(permissions.NextOwnerMask));
			query.put("expected_upload_cost", OSD.FromInteger(_Client.Settings.getUPLOAD_COST()));

			// Make the request
			CapsClient request = new CapsClient();
			request.setResultCallback(new CreateItemFromAssetResponse(callback, data, _Client.Settings.CAPS_TIMEOUT, query));
			request.executeHttpPost(url, query, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			throw new Exception("NewFileAgentInventory capability is not currently available");
		}
	}

	/**
	 * Creates inventory link to another inventory item or folder
	 * 
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param bse
	 *            Inventory item or folder
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void CreateLink(UUID folderID, InventoryNode bse, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception
	{
		if (bse instanceof InventoryFolder)
		{
			InventoryFolder folder = (InventoryFolder) bse;
			CreateLink(folderID, folder, callback);
		}
		else if (bse instanceof InventoryItem)
		{
			InventoryItem item = (InventoryItem) bse;
			CreateLink(folderID, item.itemID, item.name, item.Description, AssetType.Link, item.getType(), new UUID(), callback);
		}
	}

	/**
	 * Creates inventory link to another inventory item
	 * 
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param item
	 *            Original inventory item
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void CreateLink(UUID folderID, InventoryItem item, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception
	{
		CreateLink(folderID, item.itemID, item.name, item.Description, AssetType.Link, item.getType(), new UUID(), callback);
	}

	/**
	 * Creates inventory link to another inventory folder
	 * 
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param folder
	 *            Original inventory folder
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void CreateLink(UUID folderID, InventoryFolder folder, Callback<ItemCreatedCallbackArgs> callback)
			throws Exception
	{
		CreateLink(folderID, folder.itemID, folder.name, "", AssetType.LinkFolder, InventoryType.Folder, new UUID(), callback);
	}

	/**
	 * Creates inventory link to another inventory item or folder
	 * 
	 * @param folderID
	 *            Put newly created link in folder with this UUID
	 * @param itemID
	 *            Original item's UUID
	 * @param name
	 *            Name
	 * @param description
	 *            Description
	 * @param assetType
	 *            Asset Type
	 * @param invType
	 *            Inventory Type
	 * @param transactionID
	 *            Transaction UUID
	 * @param callback
	 *            Method to call upon creation of the link
	 * @throws Exception
	 */
	public final void CreateLink(UUID folderID, UUID itemID, String name, String description, AssetType assetType,
			InventoryType invType, UUID transactionID, Callback<ItemCreatedCallbackArgs> callback) throws Exception
	{
		LinkInventoryItemPacket create = new LinkInventoryItemPacket();
		create.AgentData.AgentID = _Client.Self.getAgentID();
		create.AgentData.SessionID = _Client.Self.getSessionID();

		create.InventoryBlock.CallbackID = RegisterItemCreatedCallback(callback);
		create.InventoryBlock.FolderID = folderID;
		create.InventoryBlock.TransactionID = transactionID;
		create.InventoryBlock.OldItemID = itemID;
		create.InventoryBlock.Type = assetType.getValue();
		create.InventoryBlock.InvType = invType.getValue();
		create.InventoryBlock.setName(Helpers.StringToBytes(name));
		create.InventoryBlock.setDescription(Helpers.StringToBytes(description));

		_Client.Network.SendPacket(create);
	}

	// #endregion Create

	// #region Copy

	/**
	 * Copy an item to a new location (folder)
	 * 
	 * @param item
	 *            The UUID of the item to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newName
	 *            An optional name to assign to the new item
	 *            Can be null if the existing name should be used.
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void RequestCopyItem(UUID item, UUID newParent, String newName,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception
	{
		RequestCopyItem(item, newParent, newName, _Client.Self.getAgentID(), callback);
	}

	/**
	 * Copy an item to a new location (folder)
	 * 
	 * @param item
	 *            The UUID of the item to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newName
	 *            An optional name to assign to the new item
	 *            Can be null if the existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the item
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void RequestCopyItem(UUID item, UUID newParent, String newName, UUID oldOwnerID,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception
	{
		ArrayList<UUID> items = new ArrayList<UUID>(1);
		items.add(item);

		ArrayList<UUID> folders = new ArrayList<UUID>(1);
		folders.add(newParent);

		if (newName != null)
		{
			ArrayList<String> newNames = new ArrayList<String>(1);
			newNames.add(newName);

			RequestCopyItems(items, folders, newNames, oldOwnerID, callback);
		}
		else
		{
			RequestCopyItems(items, folders, null, oldOwnerID, callback);
		}
	}

	/**
	 * Copy one or more items to a new location (folder)
	 * 
	 * @param items
	 *            The UUIDs of the items to copy
	 * @param newParent
	 *            The UUID of the folder to copy the item to
	 * @param newNames
	 *            An optional array of names to assign to the new items.
	 *            Can be null if the existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the items
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void RequestCopyItems(ArrayList<UUID> items, UUID newParent, ArrayList<String> newNames,
			UUID oldOwnerID, Callback<ItemCopiedCallbackArgs> callback) throws Exception
	{
		ArrayList<UUID> folders = new ArrayList<UUID>(1);
		folders.add(newParent);

		RequestCopyItems(items, folders, newNames, oldOwnerID, callback);
	}

	/**
	 * Copy one or more items to new locations (folders)
	 * 
	 * @param items
	 *            The UUIDs of the items to copy
	 * @param targetFolders
	 *            The UUIDs of the folders to copy the items to
	 * @param newNames
	 *            An optional array of names to assign to the new items
	 *            Can be null if the existing name should be used.
	 * @param oldOwnerID
	 *            The previous owner of the items
	 * @param callback
	 *            The callback to call on completion
	 * @throws Exception
	 */
	public final void RequestCopyItems(ArrayList<UUID> items, ArrayList<UUID> targetFolders,
			ArrayList<String> newNames, UUID oldOwnerID, Callback<ItemCopiedCallbackArgs> callback) throws Exception
	{
		if (newNames != null && items.size() != newNames.size())
		{
			throw new IllegalArgumentException("All list arguments must have an equal number of entries");
		}

		int callbackID = RegisterItemsCopiedCallback(callback);
		int lastTarget = targetFolders.size() - 1;

		CopyInventoryItemPacket copy = new CopyInventoryItemPacket();
		copy.AgentData.AgentID = _Client.Self.getAgentID();
		copy.AgentData.SessionID = _Client.Self.getSessionID();

		copy.InventoryData = new CopyInventoryItemPacket.InventoryDataBlock[items.size()];
		for (int i = 0; i < items.size(); ++i)
		{
			copy.InventoryData[i] = copy.new InventoryDataBlock();
			copy.InventoryData[i].CallbackID = callbackID;
			copy.InventoryData[i].NewFolderID = targetFolders.get(lastTarget > i ? i : lastTarget);
			copy.InventoryData[i].OldAgentID = oldOwnerID;
			copy.InventoryData[i].OldItemID = items.get(i);

			if (newNames != null && !Helpers.isEmpty(newNames.get(i)))
			{
				copy.InventoryData[i].setNewName(Helpers.StringToBytes(newNames.get(i)));
			}
			else
			{
				copy.InventoryData[i].setNewName(Helpers.EmptyBytes);
			}
		}
		_Client.Network.SendPacket(copy);
	}

	/**
	 * Request a copy of an asset embedded within a notecard
	 * 
	 * @param objectID
	 *            Usually UUID.Zero for copying an asset from a notecard
	 * @param notecardID
	 *            UUID of the notecard to request an asset from
	 * @param folderID
	 *            Target folder for asset to go to in your inventory
	 * @param itemID
	 *            UUID of the embedded asset
	 * @param callback
	 *            callback to run when item is copied to inventory
	 * @throws Exception
	 */
	public final void RequestCopyItemFromNotecard(UUID objectID, UUID notecardID, UUID folderID, UUID itemID,
			Callback<ItemCopiedCallbackArgs> callback) throws Exception
	{
		_ItemCopiedCallbacks.put(0, callback); // Notecards always use callback ID 0

		URI url = _Client.Network.getCapabilityURI("CopyInventoryFromNotecard");
		if (url != null)
		{
			CopyInventoryFromNotecardMessage message = _Client.Messages.new CopyInventoryFromNotecardMessage();
			message.CallbackID = 0;
			message.FolderID = folderID;
			message.ItemID = itemID;
			message.NotecardID = notecardID;
			message.ObjectID = objectID;

			new CapsClient().executeHttpPost(url, message, _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			CopyInventoryFromNotecardPacket copy = new CopyInventoryFromNotecardPacket();
			copy.AgentData.AgentID = _Client.Self.getAgentID();
			copy.AgentData.SessionID = _Client.Self.getSessionID();

			copy.NotecardData.ObjectID = objectID;
			copy.NotecardData.NotecardItemID = notecardID;

			copy.InventoryData = new CopyInventoryFromNotecardPacket.InventoryDataBlock[1];
			copy.InventoryData[0] = copy.new InventoryDataBlock();
			copy.InventoryData[0].FolderID = folderID;
			copy.InventoryData[0].ItemID = itemID;

			_Client.Network.SendPacket(copy);
		}
	}

	// #endregion Copy

	// /#region Update

	/**
	 * 
	 * 
	 * @param item
	 * @throws Exception
	 */
	public final UUID RequestUpdateItem(InventoryItem item) throws Exception
	{
		ArrayList<InventoryItem> items = new ArrayList<InventoryItem>(1);
		items.add(item);

		return RequestUpdateItems(items, new UUID());
	}

	/**
	 * 
	 * 
	 * @param items
	 * @throws Exception
	 */
	public final UUID RequestUpdateItems(ArrayList<InventoryItem> items) throws Exception
	{
		return RequestUpdateItems(items, new UUID());
	}

	/**
	 * 
	 * 
	 * @param items
	 * @param items
	 * @param transactionID
	 * @throws Exception
	 */
	public final UUID RequestUpdateItems(ArrayList<InventoryItem> items, UUID transactionID) throws Exception
	{
		UpdateInventoryItemPacket update = new UpdateInventoryItemPacket();
		update.AgentData.AgentID = _Client.Self.getAgentID();
		update.AgentData.SessionID = _Client.Self.getSessionID();
		update.AgentData.TransactionID = transactionID;

		update.InventoryData = new UpdateInventoryItemPacket.InventoryDataBlock[items.size()];
		for (int i = 0; i < items.size(); i++)
		{
			InventoryItem item = items.get(i);

			UpdateInventoryItemPacket.InventoryDataBlock block = update.new InventoryDataBlock();
			block.BaseMask = item.Permissions.BaseMask;
			block.CRC = ItemCRC(item);
			block.CreationDate = (int) Helpers.DateTimeToUnixTime(item.CreationDate);
			block.CreatorID = item.CreatorID;
			block.setDescription(Helpers.StringToBytes(item.Description));
			block.EveryoneMask = item.Permissions.EveryoneMask;
			block.Flags = item.ItemFlags;
			block.FolderID = item.parent.itemID;
			block.GroupID = item.GroupID;
			block.GroupMask = item.Permissions.GroupMask;
			block.GroupOwned = item.GroupOwned;
			block.InvType = item.getType().getValue();
			block.ItemID = item.itemID;
			block.setName(Helpers.StringToBytes(item.name));
			block.NextOwnerMask = item.Permissions.NextOwnerMask;
			block.OwnerID = _Store.getOwnerID();
			block.OwnerMask = item.Permissions.OwnerMask;
			block.SalePrice = item.SalePrice;
			block.SaleType = item.saleType.getValue();
			block.TransactionID = transactionID;
			block.Type = item.assetType.getValue();

			update.InventoryData[i] = block;
		}
		_Client.Network.SendPacket(update);
		return transactionID;
	}

	/**
	 * 
	 * 
	 * @param data
	 * @param notecardID
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestUpdateNotecardAgentInventory(byte[] data, UUID notecardID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("UpdateNotecardAgentInventory");
		if (url != null)
		{
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.FromUUID(notecardID));

			// Make the request
			CapsClient request = new CapsClient();
			request.setResultCallback(new UploadInventoryAssetComplete(callback, data, notecardID));
			request.executeHttpPost(url, query, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			throw new Exception("UpdateNotecardAgentInventory capability is not currently available");
		}
	}

	/**
	 * Save changes to notecard embedded in object contents
	 * 
	 * @param data
	 *            Encoded notecard asset data
	 * @param notecardID
	 *            Notecard UUID
	 * @param taskID
	 *            Object's UUID
	 * @param callback
	 *            Called upon finish of the upload with status information
	 * @throws Exception
	 */
	public final void RequestUpdateNotecardTaskInventory(byte[] data, UUID notecardID, UUID taskID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("UpdateNotecardTaskInventory");
		if (url != null)
		{
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.FromUUID(notecardID));
			query.put("task_id", OSD.FromUUID(taskID));

			// Make the request
			CapsClient request = new CapsClient();
			request.setResultCallback(new UploadInventoryAssetComplete(callback, data, notecardID));
			request.executeHttpPost(url, query, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);		
		}
		else
		{
			throw new Exception("UpdateNotecardTaskInventory capability is not currently available");
		}
	}

	/**
	 * Upload new gesture asset for an inventory gesture item
	 * 
	 * @param data
	 *            Encoded gesture asset
	 * @param gestureID
	 *            Gesture inventory UUID
	 * @param callback
	 *            Callback whick will be called when upload is complete
	 * @throws Exception
	 */
	public final void RequestUpdateGestureAgentInventory(byte[] data, UUID gestureID,
			Callback<InventoryUploadedAssetCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("UpdateGestureAgentInventory");
		if (url != null)
		{
			OSDMap query = new OSDMap();
			query.put("item_id", OSD.FromUUID(gestureID));

			// Make the request
			CapsClient request = new CapsClient();
			request.setResultCallback(new UploadInventoryAssetComplete(callback, data, gestureID));
			request.executeHttpPost(url, query, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);				
		}
		else
		{
			throw new Exception("UpdateGestureAgentInventory capability is not currently available");
		}
	}

	/**
	 * Update an existing script in an agents Inventory
	 * 
	 * @param data
	 *            A byte[] array containing the encoded scripts contents
	 * @param itemID
	 *            the itemID of the script
	 * @param mono
	 *            if true, sets the script content to run on the mono
	 *            interpreter
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestUpdateScriptAgent(byte[] data, UUID itemID, boolean mono,
			Callback<ScriptUpdatedCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("UpdateScriptAgent");
		if (url != null)
		{
			OSDMap map = new OSDMap(2);
			map.put("item_id", OSD.FromUUID(itemID));
			map.put("target", OSD.FromString(mono ? "mono" : "lsl2"));

			CapsClient request = new CapsClient();
			request.setResultCallback(new UpdateScriptAgentInventoryResponse(callback, data, itemID));
			request.executeHttpPost(url, map, OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
					
		}
		else
		{
			throw new Exception("UpdateScriptAgent capability is not currently available");
		}
	}

	/**
	 * Update an existing script in an task Inventory
	 * 
	 * @param data
	 *            A byte[] array containing the encoded scripts contents
	 * @param itemID
	 *            the itemID of the script
	 * @param taskID
	 *            UUID of the prim containting the script
	 * @param mono
	 *            if true, sets the script content to run on the mono
	 *            interpreter
	 * @param running
	 *            if true, sets the script to running
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestUpdateScriptTask(byte[] data, UUID itemID, UUID taskID, boolean mono, boolean running,
			Callback<ScriptUpdatedCallbackArgs> callback) throws Exception
	{
		URI url = _Client.Network.getCapabilityURI("UpdateScriptTask");
		if (url != null)
		{
			UpdateScriptTaskUpdateMessage msg = _Client.Messages.new UpdateScriptTaskUpdateMessage();
			msg.ItemID = itemID;
			msg.TaskID = taskID;
			msg.ScriptRunning = running;
			msg.Target = mono ? "mono" : "lsl2";

			CapsClient request = new CapsClient();
			request.setResultCallback(new UpdateScriptAgentInventoryResponse(callback, data, itemID));
			request.executeHttpPost(url, msg.Serialize(), OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			throw new Exception("UpdateScriptTask capability is not currently available");
		}
	}

	// #endregion Update

	// #region Rez/Give

	/**
	 * Rez an object from inventory
	 * 
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @throws Exception
	 */
	public final UUID RequestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item) throws Exception
	{
		return RequestRezFromInventory(simulator, rotation, position, item, _Client.Self.getActiveGroup(), new UUID(),
				true);
	}

	/**
	 * Rez an object from inventory
	 * 
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @param groupOwner
	 *            UUID of group to own the object
	 * @throws Exception
	 */
	public final UUID RequestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item, UUID groupOwner) throws Exception
	{
		return RequestRezFromInventory(simulator, rotation, position, item, groupOwner, new UUID(), true);
	}

	/**
	 * Rez an object from inventory
	 * 
	 * @param simulator
	 *            Simulator to place object in
	 * @param rotation
	 *            Rotation of the object when rezzed
	 * @param position
	 *            Vector of where to place object
	 * @param item
	 *            InventoryItem object containing item details
	 * @param groupOwner
	 *            UUID of group to own the object
	 * @param queryID
	 *            User defined queryID to correlate replies
	 * @param rezSelected
	 *            If set to true, the CreateSelected flag will be set on the
	 *            rezzed object
	 * @throws Exception
	 */
	public final UUID RequestRezFromInventory(Simulator simulator, Quaternion rotation, Vector3 position,
			InventoryItem item, UUID groupOwner, UUID queryID, boolean rezSelected) throws Exception
	{
		RezObjectPacket add = new RezObjectPacket();

		add.AgentData.AgentID = _Client.Self.getAgentID();
		add.AgentData.SessionID = _Client.Self.getSessionID();
		add.AgentData.GroupID = groupOwner;

		add.RezData.FromTaskID = UUID.Zero;
		add.RezData.BypassRaycast = 1;
		add.RezData.RayStart = position;
		add.RezData.RayEnd = position;
		add.RezData.RayTargetID = UUID.Zero;
		add.RezData.RayEndIsIntersection = false;
		add.RezData.RezSelected = rezSelected;
		add.RezData.RemoveItem = false;
		add.RezData.ItemFlags = item.ItemFlags;
		add.RezData.GroupMask = item.Permissions.GroupMask;
		add.RezData.EveryoneMask = item.Permissions.EveryoneMask;
		add.RezData.NextOwnerMask = item.Permissions.NextOwnerMask;

		add.InventoryData.ItemID = item.itemID;
		add.InventoryData.FolderID = item.parent.itemID;
		add.InventoryData.CreatorID = item.CreatorID;
		add.InventoryData.OwnerID = item.ownerID;
		add.InventoryData.GroupID = item.GroupID;
		add.InventoryData.BaseMask = item.Permissions.BaseMask;
		add.InventoryData.OwnerMask = item.Permissions.OwnerMask;
		add.InventoryData.GroupMask = item.Permissions.GroupMask;
		add.InventoryData.EveryoneMask = item.Permissions.EveryoneMask;
		add.InventoryData.NextOwnerMask = item.Permissions.NextOwnerMask;
		add.InventoryData.GroupOwned = item.GroupOwned;
		add.InventoryData.TransactionID = queryID;
		add.InventoryData.Type = item.getType().getValue();
		add.InventoryData.InvType = item.getType().getValue();
		add.InventoryData.Flags = item.ItemFlags;
		add.InventoryData.SaleType = item.saleType.getValue();
		add.InventoryData.SalePrice = item.SalePrice;
		add.InventoryData.setName(Helpers.StringToBytes(item.name));
		add.InventoryData.setDescription(Helpers.StringToBytes(item.Description));
		add.InventoryData.CreationDate = (int) Helpers.DateTimeToUnixTime(item.CreationDate);

		simulator.SendPacket(add);

		return queryID;
	}

	/**
	 * DeRez an object from the simulator to the agents Objects folder in the
	 * agents Inventory
	 * 
	 * @param objectLocalID
	 *            The simulator Local ID of the object If objectLocalID is a
	 *            child primitive in a linkset, the entire linkset will be
	 *            derezzed
	 * @throws Exception
	 */
	public final void RequestDeRezToInventory(int objectLocalID) throws Exception
	{
		RequestDeRezToInventory(objectLocalID, DeRezDestination.AgentInventoryTake,
				FindFolderForType(AssetType.Object).itemID, new UUID());
	}

	/**
	 * DeRez an object from the simulator and return to inventory
	 * 
	 * @param objectLocalID
	 *            The simulator Local ID of the object
	 * @param destType
	 *            The type of destination from the {@link DeRezDestination} enum
	 * @param destFolder
	 *            The destination inventory folders {@link UUID} -or- if
	 *            DeRezzing object to a tasks Inventory, the Tasks {@link UUID}
	 * @param transactionID
	 *            The transaction ID for this request which can be used to
	 *            correlate this request with other packets. If objectLocalID is
	 *            a child primitive in a linkset, the entire linkset will be
	 *            derezzed
	 * @throws Exception
	 */
	public final void RequestDeRezToInventory(int objectLocalID, DeRezDestination destType, UUID destFolder,
			UUID transactionID) throws Exception
	{
		DeRezObjectPacket take = new DeRezObjectPacket();

		take.AgentData.AgentID = _Client.Self.getAgentID();
		take.AgentData.SessionID = _Client.Self.getSessionID();
		take.AgentBlock = take.new AgentBlockBlock();
		take.AgentBlock.GroupID = UUID.Zero;
		take.AgentBlock.Destination = destType.getValue();
		take.AgentBlock.DestinationID = destFolder;
		take.AgentBlock.PacketCount = 1;
		take.AgentBlock.PacketNumber = 1;
		take.AgentBlock.TransactionID = transactionID;

		take.ObjectLocalID = new int[1];
		take.ObjectLocalID[0] = objectLocalID;

		_Client.Network.SendPacket(take);
	}

	/**
	 * Rez an item from inventory to its previous simulator location
	 * 
	 * @param simulator
	 * @param item
	 * @param queryID
	 * @return
	 * @throws Exception
	 */
	public final UUID RequestRestoreRezFromInventory(Simulator simulator, InventoryItem item, UUID queryID)
			throws Exception
	{
		RezRestoreToWorldPacket add = new RezRestoreToWorldPacket();

		add.AgentData.AgentID = _Client.Self.getAgentID();
		add.AgentData.SessionID = _Client.Self.getSessionID();

		add.InventoryData.ItemID = item.itemID;
		add.InventoryData.FolderID = item.parent.itemID;
		add.InventoryData.CreatorID = item.CreatorID;
		add.InventoryData.OwnerID = item.ownerID;
		add.InventoryData.GroupID = item.GroupID;
		add.InventoryData.BaseMask = item.Permissions.BaseMask;
		add.InventoryData.OwnerMask = item.Permissions.OwnerMask;
		add.InventoryData.GroupMask = item.Permissions.GroupMask;
		add.InventoryData.EveryoneMask = item.Permissions.EveryoneMask;
		add.InventoryData.NextOwnerMask = item.Permissions.NextOwnerMask;
		add.InventoryData.GroupOwned = item.GroupOwned;
		add.InventoryData.TransactionID = queryID;
		add.InventoryData.Type = item.getType().getValue();
		add.InventoryData.InvType = item.getType().getValue();
		add.InventoryData.Flags = item.ItemFlags;
		add.InventoryData.SaleType = item.saleType.getValue();
		add.InventoryData.SalePrice = item.SalePrice;
		add.InventoryData.setName(Helpers.StringToBytes(item.name));
		add.InventoryData.setDescription(Helpers.StringToBytes(item.Description));
		add.InventoryData.CreationDate = (int) Helpers.DateTimeToUnixTime(item.CreationDate);

		simulator.SendPacket(add);

		return queryID;
	}

	/**
	 * Give an inventory item to another avatar
	 * 
	 * @param itemID
	 *            The {@link UUID} of the item to give
	 * @param itemName
	 *            The name of the item
	 * @param assetType
	 *            The type of the item from the {@link AssetType} enum
	 * @param recipient
	 *            The {@link UUID} of the recipient
	 * @param doEffect
	 *            true to generate a beameffect during transfer
	 * @throws Exception
	 */
	public final void GiveItem(UUID itemID, String itemName, AssetType assetType, UUID recipient, boolean doEffect)
			throws Exception
	{
		byte[] bucket;

		bucket = new byte[17];
		bucket[0] = assetType.getValue();
		itemID.ToBytes(bucket, 1);

		_Client.Self.InstantMessage(_Client.Self.getName(), recipient, itemName, new UUID(),
				InstantMessageDialog.InventoryOffered, InstantMessageOnline.Online, null, null, 0, bucket);

		if (doEffect)
		{
			_Client.Self.BeamEffect(_Client.Self.getAgentID(), recipient, Vector3d.Zero,
					_Client.Settings.DEFAULT_EFFECT_COLOR, 1f, new UUID());
		}
	}

	/**
	 * Give an inventory Folder with contents to another avatar
	 * 
	 * @param folderID
	 *            The {@link UUID} of the Folder to give
	 * @param folderName
	 *            The name of the folder
	 * @param assetType
	 *            The type of the item from the {@link AssetType} enum
	 * @param recipient
	 *            The {@link UUID} of the recipient
	 * @param doEffect
	 *            true to generate a beameffect during transfer
	 * @throws Exception
	 */
	public final void GiveFolder(UUID folderID, String folderName, AssetType assetType, UUID recipient, boolean doEffect)
			throws Exception
	{
		byte[] bucket;

		ArrayList<InventoryItem> folderContents = new ArrayList<InventoryItem>();
		ArrayList<InventoryNode> ibl = FolderContents(folderID, _Client.Self.getAgentID(), false, true,
				InventorySortOrder.ByDate, 1000 * 15);
		for (InventoryNode ib : ibl)
		{
			folderContents.add(FetchItem(ib.itemID, _Client.Self.getAgentID(), 1000 * 10));
		}
		bucket = new byte[17 * (folderContents.size() + 1)];

		// Add parent folder (first item in bucket)
		bucket[0] = assetType.getValue();
		folderID.ToBytes(bucket, 1);

		// Add contents to bucket after folder
		for (int i = 1; i <= folderContents.size(); ++i)
		{
			bucket[i * 17] = folderContents.get(i - 1).assetType.getValue();
			folderContents.get(i - 1).itemID.ToBytes(bucket, i * 17 + 1);
		}
		_Client.Self.InstantMessage(_Client.Self.getName(), recipient, folderName, new UUID(),
				InstantMessageDialog.InventoryOffered, InstantMessageOnline.Online, null, null, 0, bucket);

		if (doEffect)
		{
			_Client.Self.BeamEffect(_Client.Self.getAgentID(), recipient, Vector3d.Zero,
					_Client.Settings.DEFAULT_EFFECT_COLOR, 1f, new UUID());
		}
	}

	// #endregion Rez/Give

	// /#region Task

	/**
	 * Copy or move an <see cref="InventoryItem"/> from agent inventory to a
	 * task (primitive) inventory
	 * 
	 * @param objectLocalID
	 *            The target object
	 * @param item
	 *            The item to copy or move from inventory
	 * @return For items with copy permissions a copy of the item is placed in
	 *         the tasks inventory, for no-copy items the object is moved to the
	 *         tasks inventory TODO: what does the return UUID correlate to if
	 *         anything?
	 * @throws Exception
	 */
	public final UUID UpdateTaskInventory(int objectLocalID, InventoryItem item) throws Exception
	{
		UUID transactionID = new UUID();

		UpdateTaskInventoryPacket update = new UpdateTaskInventoryPacket();
		update.AgentData.AgentID = _Client.Self.getAgentID();
		update.AgentData.SessionID = _Client.Self.getSessionID();
		update.UpdateData.Key = 0;
		update.UpdateData.LocalID = objectLocalID;

		update.InventoryData.ItemID = item.itemID;
		update.InventoryData.FolderID = item.parent.itemID;
		update.InventoryData.CreatorID = item.CreatorID;
		update.InventoryData.OwnerID = item.ownerID;
		update.InventoryData.GroupID = item.GroupID;
		update.InventoryData.BaseMask = item.Permissions.BaseMask;
		update.InventoryData.OwnerMask = item.Permissions.OwnerMask;
		update.InventoryData.GroupMask = item.Permissions.GroupMask;
		update.InventoryData.EveryoneMask = item.Permissions.EveryoneMask;
		update.InventoryData.NextOwnerMask = item.Permissions.NextOwnerMask;
		update.InventoryData.GroupOwned = item.GroupOwned;
		update.InventoryData.TransactionID = transactionID;
		update.InventoryData.Type = item.assetType.getValue();
		update.InventoryData.InvType = item.getType().getValue();
		update.InventoryData.Flags = item.ItemFlags;
		update.InventoryData.SaleType = item.saleType.getValue();
		update.InventoryData.SalePrice = item.SalePrice;
		update.InventoryData.setName(Helpers.StringToBytes(item.name));
		update.InventoryData.setDescription(Helpers.StringToBytes(item.Description));
		update.InventoryData.CreationDate = (int) Helpers.DateTimeToUnixTime(item.CreationDate);
		update.InventoryData.CRC = ItemCRC(item);

		_Client.Network.SendPacket(update);

		return transactionID;
	}

	/**
	 * Retrieve a listing of the items contained in a task (Primitive) This
	 * request blocks until the response from the simulator arrives or timeoutMS
	 * is exceeded
	 * 
	 * NOTE: This requires the asset manager to be instantiated in order for
	 * this function to succeed
	 * 
	 * @param objectID
	 *            The tasks {@link UUID}
	 * @param objectLocalID
	 *            The tasks simulator local ID
	 * @param timeout
	 *            milliseconds to wait for reply from simulator
	 * @return A list containing the inventory items inside the task or null if
	 *         a timeout occurs
	 * @throws Exception
	 */
	public final ArrayList<InventoryNode> GetTaskInventory(final UUID objectID, int objectLocalID, int timeout)
			throws Exception
	{
		if (_Client.Assets == null)
			throw new RuntimeException("Can't get task inventory without the asset manager being instantiated.");

		final TimeoutEvent<String> taskReplyEvent = new TimeoutEvent<String>();
		Callback<TaskInventoryReplyCallbackArgs> callback = new Callback<TaskInventoryReplyCallbackArgs>()
		{
			@Override
			public boolean callback(TaskInventoryReplyCallbackArgs e)
			{
				if (e.getItemID().equals(objectID))
				{
					taskReplyEvent.set(e.getAssetFilename());
				}
				return false;
			}
		};
		OnTaskInventoryReply.add(callback, true);
		RequestTaskInventory(objectLocalID);
		String filename = taskReplyEvent.waitOne(timeout);
		OnTaskInventoryReply.remove(callback);

		if (filename != null)
		{
			if (!filename.isEmpty())
			{
				final TimeoutEvent<String> taskDownloadEvent = new TimeoutEvent<String>();
				final long xferID = 0;

				Callback<XferReceivedCallbackArgs> xferCallback = new Callback<XferReceivedCallbackArgs>()
				{
					@Override
					public boolean callback(XferReceivedCallbackArgs e)
					{
						if (e.getXfer().XferID == xferID)
						{
							try
							{
								taskDownloadEvent.set(Helpers.BytesToString(e.getXfer().AssetData));
							}
							catch (UnsupportedEncodingException e1)
							{
								taskDownloadEvent.set(Helpers.EmptyString);
							}
						}
						return false;
					}
				};
				// Start the actual asset xfer
				_Client.Assets.OnXferReceived.add(xferCallback, true);
				_Client.Assets.RequestAssetXfer(filename, true, false, UUID.Zero, AssetType.Unknown, true);
				String taskList = taskDownloadEvent.waitOne(timeout);
				_Client.Assets.OnXferReceived.remove(xferCallback);
				if (taskList != null && !taskList.isEmpty())
				{
					return ParseTaskInventory(taskList);
				}

				Logger.Log("Timed out waiting for task inventory download for " + filename, LogLevel.Warning, _Client);
				return null;
			}

			Logger.DebugLog("Task is empty for " + objectLocalID, _Client);
			return new ArrayList<InventoryNode>(0);
		}

		Logger.Log("Timed out waiting for task inventory reply for " + objectLocalID, LogLevel.Warning, _Client);
		return null;
	}

	/**
	 * Request the contents of a tasks (primitives) inventory from the current
	 * simulator {@link TaskInventoryReply}
	 * 
	 * @param objectLocalID
	 *            The LocalID of the object
	 * @throws Exception
	 */
	public final void RequestTaskInventory(int objectLocalID) throws Exception
	{
		RequestTaskInventory(objectLocalID, null);
	}

	/**
	 * Request the contents of a tasks (primitives) inventory
	 * {@link TaskInventoryReply}
	 * 
	 * @param objectLocalID
	 *            The simulator Local ID of the object
	 * @param simulator
	 *            A reference to the simulator object that contains the object
	 * @throws Exception
	 */
	public final void RequestTaskInventory(int objectLocalID, Simulator simulator) throws Exception
	{
		RequestTaskInventoryPacket request = new RequestTaskInventoryPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.LocalID = objectLocalID;

		simulator.SendPacket(request);
	}

	/**
	 * Move an item from a tasks (Primitive) inventory to the specified folder
	 * in the avatars inventory Raises the <see cref="OnTaskItemReceived"/>
	 * event
	 * 
	 * @param objectLocalID
	 *            LocalID of the object in the simulator
	 * @param taskItemID
	 *            UUID of the task item to move
	 * @param inventoryFolderID
	 *            The ID of the destination folder in this agents inventory
	 * @param simulator
	 *            Simulator Object
	 * @throws Exception
	 */
	public final void MoveTaskInventory(int objectLocalID, UUID taskItemID, UUID inventoryFolderID, Simulator simulator)
			throws Exception
	{
		MoveTaskInventoryPacket request = new MoveTaskInventoryPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();

		request.AgentData.FolderID = inventoryFolderID;

		request.InventoryData.ItemID = taskItemID;
		request.InventoryData.LocalID = objectLocalID;

		simulator.SendPacket(request);
	}

	/**
	 * Remove an item from an objects (Prim) Inventory You can confirm the
	 * removal by comparing the tasks inventory serial before and after the
	 * request with the <see cref="RequestTaskInventory"/> request combined with
	 * the {@link TaskInventoryReply} event
	 * 
	 * @param objectLocalID
	 *            LocalID of the object in the simulator
	 * @param taskItemID
	 *            UUID of the task item to remove
	 * @param simulator
	 *            Simulator Object
	 * @throws Exception
	 */
	public final void RemoveTaskInventory(int objectLocalID, UUID taskItemID, Simulator simulator) throws Exception
	{
		RemoveTaskInventoryPacket remove = new RemoveTaskInventoryPacket();
		remove.AgentData.AgentID = _Client.Self.getAgentID();
		remove.AgentData.SessionID = _Client.Self.getSessionID();

		remove.InventoryData.ItemID = taskItemID;
		remove.InventoryData.LocalID = objectLocalID;

		simulator.SendPacket(remove);
	}

	/**
	 * Copy an InventoryScript item from the Agents Inventory into a primitives
	 * task inventory
	 * 
	 * @param objectLocalID
	 *            An unsigned integer representing a primitive being simulated
	 * @param item
	 *            An {@link InventoryItem} which represents a script object from
	 *            the agents inventory
	 * @param enableScript
	 *            true to set the scripts running state to enabled
	 * @return A Unique Transaction ID <example> The following example shows the
	 *         basic steps necessary to copy a script from the agents inventory
	 *         into a tasks inventory and assumes the script exists in the
	 *         agents inventory. <code>
	 *    int primID = 95899503; // Fake prim ID
	 *    UUID scriptID = UUID.Parse("92a7fe8a-e949-dd39-a8d8-1681d8673232"); // Fake Script UUID in Inventory
	 * 
	 *    _Client.Inventory.FolderContents(_Client.Inventory.FindFolderForType(AssetType.LSLText), _Client.Self.AgentID,
	 *        false, true, InventorySortOrder.ByName, 10000);
	 * 
	 *    _Client.Inventory.RezScript(primID, (InventoryItem)_Client.Inventory.getStore().get(scriptID));
	 * </code> </example> TODO: what does the return UUID correlate to if
	 *         anything?
	 * @throws Exception
	 */
	public final UUID CopyScriptToTask(int objectLocalID, InventoryItem item, boolean enableScript) throws Exception
	{
		UUID transactionID = new UUID();

		RezScriptPacket ScriptPacket = new RezScriptPacket();
		ScriptPacket.AgentData.AgentID = _Client.Self.getAgentID();
		ScriptPacket.AgentData.SessionID = _Client.Self.getSessionID();

		ScriptPacket.UpdateBlock.ObjectLocalID = objectLocalID;
		ScriptPacket.UpdateBlock.Enabled = enableScript;

		ScriptPacket.InventoryBlock.ItemID = item.itemID;
		ScriptPacket.InventoryBlock.FolderID = item.parent.itemID;
		ScriptPacket.InventoryBlock.CreatorID = item.CreatorID;
		ScriptPacket.InventoryBlock.OwnerID = item.ownerID;
		ScriptPacket.InventoryBlock.GroupID = item.GroupID;
		ScriptPacket.InventoryBlock.BaseMask = item.Permissions.BaseMask;
		ScriptPacket.InventoryBlock.OwnerMask = item.Permissions.OwnerMask;
		ScriptPacket.InventoryBlock.GroupMask = item.Permissions.GroupMask;
		ScriptPacket.InventoryBlock.EveryoneMask = item.Permissions.EveryoneMask;
		ScriptPacket.InventoryBlock.NextOwnerMask = item.Permissions.NextOwnerMask;
		ScriptPacket.InventoryBlock.GroupOwned = item.GroupOwned;
		ScriptPacket.InventoryBlock.TransactionID = transactionID;
		ScriptPacket.InventoryBlock.Type = item.assetType.getValue();
		ScriptPacket.InventoryBlock.InvType = item.getType().getValue();
		ScriptPacket.InventoryBlock.Flags = item.ItemFlags;
		ScriptPacket.InventoryBlock.SaleType = item.saleType.getValue();
		ScriptPacket.InventoryBlock.SalePrice = item.SalePrice;
		ScriptPacket.InventoryBlock.setName(Helpers.StringToBytes(item.name));
		ScriptPacket.InventoryBlock.setDescription(Helpers.StringToBytes(item.Description));
		ScriptPacket.InventoryBlock.CreationDate = (int) Helpers.DateTimeToUnixTime(item.CreationDate);
		ScriptPacket.InventoryBlock.CRC = ItemCRC(item);

		_Client.Network.SendPacket(ScriptPacket);

		return transactionID;
	}

	/**
	 * Request the running status of a script contained in a task (primitive)
	 * inventory The <see cref="ScriptRunningReply"/> event can be used to
	 * obtain the results of the request {@link ScriptRunningReply}
	 * 
	 * @param objectID
	 *            The ID of the primitive containing the script
	 * @param scriptID
	 *            The ID of the script
	 * @throws Exception
	 */
	public final void RequestGetScriptRunning(UUID objectID, UUID scriptID) throws Exception
	{
		GetScriptRunningPacket request = new GetScriptRunningPacket();
		request.Script.ObjectID = objectID;
		request.Script.ItemID = scriptID;

		_Client.Network.SendPacket(request);
	}

	/**
	 * Send a request to set the running state of a script contained in a task
	 * (primitive) inventory To verify the change you can use the <see
	 * cref="RequestGetScriptRunning"/> method combined with the <see
	 * cref="ScriptRunningReply"/> event
	 * 
	 * @param objectID
	 *            The ID of the primitive containing the script
	 * @param scriptID
	 *            The ID of the script
	 * @param running
	 *            true to set the script running, false to stop a running script
	 * @throws Exception
	 */
	public final void RequestSetScriptRunning(UUID objectID, UUID scriptID, boolean running) throws Exception
	{
		SetScriptRunningPacket request = new SetScriptRunningPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.Script.Running = running;
		request.Script.ItemID = scriptID;
		request.Script.ObjectID = objectID;

		_Client.Network.SendPacket(request);
	}

	// #endregion Task

	// #region Helper Functions

	private int RegisterItemCreatedCallback(Callback<ItemCreatedCallbackArgs> callback)
	{
		return RegisterItemCreatedCallback(callback, -1);
	}

	private int RegisterItemCreatedCallback(Callback<ItemCreatedCallbackArgs> callback, int id)
	{
		synchronized (_CallbacksLock)
		{
			if (id < 0)
			{
				if (_CallbackPos == Integer.MAX_VALUE)
				{
					_CallbackPos = 0;
				}
				id = ++_CallbackPos;
				if (_ItemCreatedCallbacks.containsKey(id))
				{
					Logger.Log("Overwriting an existing ItemCreatedCallback", LogLevel.Warning, _Client);
				}
				_ItemCreatedCallbacks.put(id, callback);
			}
			OnItemCreatedCallback.add(callback);
			return id;
		}
	}

	private int RegisterItemsCopiedCallback(Callback<ItemCopiedCallbackArgs> callback)
	{
		return RegisterItemsCopiedCallback(callback, -1);
	}

	private int RegisterItemsCopiedCallback(Callback<ItemCopiedCallbackArgs> callback, int id)
	{
		synchronized (_CallbacksLock)
		{
			if (id <= 0)
			{
				if (_CallbackPos == Integer.MAX_VALUE)
				{
					_CallbackPos = 0;
				}
				id = ++_CallbackPos;

				if (_ItemCopiedCallbacks.containsKey(id))
				{
					Logger.Log("Overwriting an existing ItemsCopiedCallback", LogLevel.Warning, _Client);
				}
				_ItemCopiedCallbacks.put(id, callback);
			}
			OnItemCopiedCallback.add(callback);
			return id;
		}
	}

	/**
	 * Create a CRC from an InventoryItem
	 * 
	 * @param iitem
	 *            The source InventoryItem
	 * @return A int representing the source InventoryItem as a CRC
	 */
	public static int ItemCRC(InventoryItem iitem)
	{
		int CRC = 0;

		// IDs
		CRC += iitem.AssetID.CRC(); // AssetID
		CRC += iitem.parent.itemID.CRC(); // FolderID
		CRC += iitem.itemID.CRC(); // ItemID

		// Permission stuff
		CRC += iitem.CreatorID.CRC(); // CreatorID
		CRC += iitem.ownerID.CRC(); // OwnerID
		CRC += iitem.GroupID.CRC(); // GroupID

		// CRC += another 4 words which always seem to be zero -- unclear if
		// this is a UUID or what
		CRC += iitem.Permissions.OwnerMask; // owner_mask; // Either owner_mask
											// or next_owner_mask may need to be
		CRC += iitem.Permissions.NextOwnerMask; // next_owner_mask; // switched
												// with base_mask -- 2 values go
												// here and in my
		CRC += iitem.Permissions.EveryoneMask; // everyone_mask; // study item,
												// the three were identical.
		CRC += iitem.Permissions.GroupMask; // group_mask;

		// The rest of the CRC fields
		CRC += iitem.ItemFlags; // Flags
		CRC += iitem.getType().getValue(); // InvType
		CRC += iitem.assetType.getValue(); // Type
		CRC += Helpers.DateTimeToUnixTime(iitem.CreationDate); // CreationDate
		CRC += iitem.SalePrice; // SalePrice
		CRC += (iitem.saleType.getValue() * 0x07073096); // SaleType

		return CRC;
	}

	/**
	 * Reverses a cheesy XORing with a fixed UUID to convert a shadow_id to an
	 * asset_id
	 * 
	 * @param shadowID
	 *            Obfuscated shadow_id value
	 * @return Deobfuscated asset_id value
	 */
	public static UUID DecryptShadowID(UUID shadowID)
	{
		UUID uuid = new UUID(shadowID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}

	/**
	 * Does a cheesy XORing with a fixed UUID to convert an asset_id to a
	 * shadow_id
	 * 
	 * @param assetID
	 *            asset_id value to obfuscate
	 * @return Obfuscated shadow_id value
	 */
	public static UUID EncryptAssetID(UUID assetID)
	{
		UUID uuid = new UUID(assetID);
		uuid.XOr(MAGIC_ID);
		return uuid;
	}


	private InventoryFolder SafeCreateInventoryFolder(UUID folderID, UUID parentID, UUID ownerID)
	{
		synchronized (_Store)
		{
			if (_Store.containsFolder(folderID))
			{
				return _Store.getFolder(folderID);
			}
			// TODO: store the parent relation
			return new InventoryFolder(folderID, parentID, ownerID);
		}
	}

	private InventoryItem SafeCreateInventoryItem(InventoryType type, UUID itemID, UUID parentID, UUID ownerID)
	{
		synchronized (_Store)
		{
			if (_Store.containsItem(itemID))
			{
				return _Store.getItem(itemID);
			}
			// TODO: store the parent relation
			return InventoryItem.create(type, itemID, parentID, ownerID);
		}
	}

	private static boolean ParseLine(String line, RefObject<String> key, RefObject<String> value)
	{
		// Clean up and convert tabs to spaces
		line = line.trim();
		line = line.replace('\t', ' ');

		// Shrink all whitespace down to single spaces
		while (line.indexOf("  ") > 0)
		{
			line = line.replace("  ", " ");
		}

		if (line.length() > 2)
		{
			int sep = line.indexOf(' ');
			if (sep > 0)
			{
				key.argvalue = line.substring(0, sep);
				value.argvalue = line.substring(sep + 1);

				return true;
			}
		}
		else if (line.length() == 1)
		{
			key.argvalue = line;
			value.argvalue = Helpers.EmptyString;
			return true;
		}

		key.argvalue = null;
		value.argvalue = null;
		return false;
	}

	/**
	 * Parse the results of a RequestTaskInventory() response
	 * 
	 * @param taskData
	 *            A string which contains the data from the task reply
	 * @return A List containing the items contained within the tasks inventory
	 */
	public ArrayList<InventoryNode> ParseTaskInventory(String taskData)
	{
		ArrayList<InventoryNode> items = new ArrayList<InventoryNode>();
		int lineNum = 0;
		String[] lines = taskData.replace("\r\n", "\n").split("\n");
		String key = Helpers.EmptyString;
		String val = Helpers.EmptyString;
		RefObject<String> keyref = new RefObject<String>(key), valref = new RefObject<String>(val);

		while (lineNum < lines.length)
		{
			if (ParseLine(lines[lineNum++], keyref, valref))
			{
				UUID itemID = UUID.Zero;
				UUID parentID = UUID.Zero;
				String name = Helpers.EmptyString;
				AssetType assetType = AssetType.Unknown;

				if (key.equals("inv_object"))
				{
					// In practice this appears to only be used for folders

					while (lineNum < lines.length)
					{
						if (ParseLine(lines[lineNum++], keyref, valref))
						{
							if (key.equals("{"))
							{
								continue;
							}
							else if (key.equals("}"))
							{
								break;
							}
							else if (key.equals("obj_id"))
							{
								itemID = UUID.Parse(val);
							}
							else if (key.equals("parent_id"))
							{
								parentID = UUID.Parse(val);
							}
							else if (key.equals("type"))
							{
								assetType = AssetType.setValue(val);
							}
							else if (key.equals("name"))
							{
								name = val.substring(0, val.indexOf('|'));
							}
						}
					}

					InventoryFolder folder = new InventoryFolder(itemID, parentID, _Client.Self.getAgentID());
					folder.name = name;
					folder.preferredType = assetType;
					items.add(folder);
				}
				else if (key.equals("inv_item"))
				{
					// Any inventory item that links to an assetID, has
					// permissions, etc
					UUID assetID = UUID.Zero;
					UUID creatorID = UUID.Zero;
					UUID ownerID = UUID.Zero;
					UUID lastOwnerID = UUID.Zero;
					UUID groupID = UUID.Zero;
					boolean groupOwned = false;
					String desc = Helpers.EmptyString;
					InventoryType inventoryType = InventoryType.Unknown;
					Date creationDate = Helpers.Epoch;
					int flags = 0;
					Permissions perms = Permissions.NoPermissions;
					SaleType saleType = SaleType.Not;
					int salePrice = 0;

					while (lineNum < lines.length)
					{
						if (ParseLine(lines[lineNum++], keyref, valref))
						{
							if (key.equals("{"))
							{
								continue;
							}
							else if (key.equals("}"))
							{
								break;
							}
							else if (key.equals("item_id"))
							{
								itemID = UUID.Parse(val);
							}
							else if (key.equals("parent_id"))
							{
								parentID = UUID.Parse(val);
							}
							else if (key.equals("permissions"))
							{
								while (lineNum < lines.length)
								{
									if (ParseLine(lines[lineNum++], keyref, valref))
									{
										if (key.equals("{"))
										{
											continue;
										}
										else if (key.equals("}"))
										{
											break;
										}
										else if (key.equals("creator_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.BaseMask = i;
											}
										}
										else if (key.equals("base_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.BaseMask = i;
											}
										}
										else if (key.equals("owner_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.OwnerMask = i;
											}
										}
										else if (key.equals("group_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.GroupMask = i;
											}
										}
										else if (key.equals("everyone_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.EveryoneMask = i;
											}
										}
										else if (key.equals("next_owner_mask"))
										{
											int i = (int) Helpers.TryParseHex(val);
											if (i != 0)
											{
												perms.NextOwnerMask = i;
											}
										}
										else if (key.equals("creator_id"))
										{
											creatorID.FromString(val);
										}
										else if (key.equals("owner_id"))
										{
											ownerID.FromString(val);
										}
										else if (key.equals("last_owner_id"))
										{
											lastOwnerID.FromString(val);
										}
										else if (key.equals("group_id"))
										{
											groupID.FromString(val);
										}
										else if (key.equals("group_owned"))
										{
											long i = Helpers.TryParseLong(val);
											if (i != 0)
											{
												groupOwned = (i != 0);
											}
										}
									}
								}
							}
							else if (key.equals("sale_info"))
							{
								while (lineNum < lines.length)
								{
									if (ParseLine(lines[lineNum++], keyref, valref))
									{
										if (key.equals("{"))
										{
											continue;
										}
										else if (key.equals("}"))
										{
											break;
										}
										else if (key.equals("sale_type"))
										{
											saleType = SaleType.setValue(val);
										}
										else if (key.equals("sale_price"))
										{
											salePrice = Helpers.TryParseInt(val);
										}
									}
								}
							}
							else if (key.equals("shadow_id"))
							{
								assetID = DecryptShadowID(new UUID(val));
							}
							else if (key.equals("asset_id"))
							{
								assetID = new UUID(val);
							}
							else if (key.equals("type"))
							{
								assetType = AssetType.valueOf(val);
							}
							else if (key.equals("inv_type"))
							{
								inventoryType = InventoryType.valueOf(val);
							}
							else if (key.equals("flags"))
							{
								flags = (int) Helpers.TryParseLong(val);
							}
							else if (key.equals("name"))
							{
								name = val.substring(0, val.indexOf('|'));
							}
							else if (key.equals("desc"))
							{
								desc = val.substring(0, val.indexOf('|'));
							}
							else if (key.equals("creation_date"))
							{
								int timestamp = Helpers.TryParseInt(val);
								if (timestamp != 0)
								{
									creationDate = Helpers.UnixTimeToDateTime(timestamp);
								}
								else
								{
									Logger.Log("Failed to parse creation_date " + val, LogLevel.Warning);
								}
							}
						}
					}

					InventoryItem item = InventoryItem.create(inventoryType, itemID, parentID, ownerID);
					item.AssetID = assetID;
					item.assetType = assetType;
					item.CreationDate = creationDate;
					item.CreatorID = creatorID;
					item.Description = desc;
					item.ItemFlags = flags;
					item.GroupID = groupID;
					item.GroupOwned = groupOwned;
					item.name = name;
					item.LastOwnerID = lastOwnerID;
					item.Permissions = perms;
					item.SalePrice = salePrice;
					item.saleType = saleType;

					items.add(item);
					// #endregion inv_item
				}
				else
				{
					Logger.Log("Unrecognized token " + key + " in: " + taskData, LogLevel.Error);
				}
			}
		}
		return items;
	}

	// /#endregion Helper Functions

	// /#region Internal Callbacks

	private class Self_InstantMessage implements Callback<InstantMessageCallbackArgs>
	{
		@Override
		public boolean callback(InstantMessageCallbackArgs e)
		{
			// TODO: MainAvatar.InstantMessageDialog.GroupNotice can also be an
			// inventory offer, should we handle it here?

			if (OnInventoryObjectOffered != null
					&& (e.getIM().Dialog == InstantMessageDialog.InventoryOffered || e.getIM().Dialog == InstantMessageDialog.TaskInventoryOffered))
			{
				AssetType type = AssetType.Unknown;
				UUID objectID = UUID.Zero;
				boolean fromTask = false;

				if (e.getIM().Dialog == InstantMessageDialog.InventoryOffered)
				{
					if (e.getIM().BinaryBucket.length == 17)
					{
						type = AssetType.setValue(e.getIM().BinaryBucket[0]);
						objectID = new UUID(e.getIM().BinaryBucket, 1);
						fromTask = false;
					}
					else
					{
						Logger.Log("Malformed inventory offer from agent", LogLevel.Warning, _Client);
						return false;
					}
				}
				else if (e.getIM().Dialog == InstantMessageDialog.TaskInventoryOffered)
				{
					if (e.getIM().BinaryBucket.length == 1)
					{
						type = AssetType.setValue(e.getIM().BinaryBucket[0]);
						fromTask = true;
					}
					else
					{
						Logger.Log("Malformed inventory offer from object", LogLevel.Warning, _Client);
						return false;
					}
				}

				// Fire the callback
				try
				{
					// Find the folder where this is going to go
					InventoryFolder parent = FindFolderForType(type);
					ImprovedInstantMessagePacket imp = new ImprovedInstantMessagePacket();
					imp.AgentData.AgentID = _Client.Self.getAgentID();
					imp.AgentData.SessionID = _Client.Self.getSessionID();
					imp.MessageBlock.FromGroup = false;
					imp.MessageBlock.ToAgentID = e.getIM().FromAgentID;
					imp.MessageBlock.Offline = 0;
					imp.MessageBlock.ID = e.getIM().IMSessionID;
					imp.MessageBlock.Timestamp = 0;
					imp.MessageBlock.setFromAgentName(Helpers.StringToBytes(_Client.Self.getName()));
					imp.MessageBlock.setMessage(Helpers.EmptyBytes);
					imp.MessageBlock.ParentEstateID = 0;
					imp.MessageBlock.RegionID = UUID.Zero;
					imp.MessageBlock.Position = _Client.Self.getSimPosition();

					InventoryObjectOfferedCallbackArgs args = new InventoryObjectOfferedCallbackArgs(e.getIM(), type,
							objectID, fromTask, parent.itemID);

					OnInventoryObjectOffered.dispatch(args);

					if (args.getAccept())
					{
						// Accept the inventory offer
						switch (e.getIM().Dialog)
						{
							case InventoryOffered:
								imp.MessageBlock.Dialog = InstantMessageDialog.InventoryAccepted.getValue();
								break;
							case TaskInventoryOffered:
								imp.MessageBlock.Dialog = InstantMessageDialog.TaskInventoryAccepted.getValue();
								break;
							case GroupNotice:
								imp.MessageBlock.Dialog = InstantMessageDialog.GroupNoticeInventoryAccepted.getValue();
								break;
						}
						imp.MessageBlock.setBinaryBucket(args.getFolderID().GetBytes());
					}
					else
					{
						// Decline the inventory offer
						switch (e.getIM().Dialog)
						{
							case InventoryOffered:
								imp.MessageBlock.Dialog = InstantMessageDialog.InventoryDeclined.getValue();
								break;
							case TaskInventoryOffered:
								imp.MessageBlock.Dialog = InstantMessageDialog.TaskInventoryDeclined.getValue();
								break;
							case GroupNotice:
								imp.MessageBlock.Dialog = InstantMessageDialog.GroupNoticeInventoryDeclined.getValue();
								break;
						}
						imp.MessageBlock.setBinaryBucket(Helpers.EmptyBytes);
					}

					e.getSimulator().SendPacket(imp);
				}
				catch (Exception ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}
			return false;
		}
	}

	private class CreateItemFromAssetResponse implements FutureCallback<OSD>
	{
		private final Callback<ItemCreatedFromAssetCallbackArgs> callback;
		private final byte[] itemData;
		private final long timeout;
		private final OSDMap request;

		public CreateItemFromAssetResponse(Callback<ItemCreatedFromAssetCallbackArgs> callback, byte[] data,
				long timeout, OSDMap query)
		{
			this.callback = callback;
			this.itemData = data;
			this.timeout = timeout;
			this.request = query;
		}

		@Override
		public void completed(OSD result)
		{
			OSDMap contents = (OSDMap) result;

			String status = contents.get("state").AsString().toLowerCase();

			if (status.equals("upload"))
			{
				String uploadURL = contents.get("uploader").AsString();

				Logger.DebugLog("CreateItemFromAsset: uploading to " + uploadURL);

				// This makes the assumption that all uploads go to CurrentSim,
				// to avoid the problem of HttpRequestState not knowing anything
				// about simulators
				try
				{
					CapsClient upload = new CapsClient();
					upload.setResultCallback(new CreateItemFromAssetResponse(callback, itemData, timeout, request));
					upload.executeHttpPost(new URI(uploadURL), itemData, "application/octet-stream", timeout);
				}
				catch (Exception ex)
				{
					failed(ex);
				}
			}
			else if (status.equals("complete"))
			{
				Logger.DebugLog("CreateItemFromAsset: completed");

				if (contents.containsKey("new_inventory_item") && contents.containsKey("new_asset"))
				{
					UUID item = contents.get("new_inventory_item").AsUUID();
					UUID asset = contents.get("new_asset").AsUUID();
					// Request full update on the item in order to update the
					// local store
					try
					{
						RequestFetchInventory(item, _Client.Self.getAgentID());
					}
					catch (Exception ex)
					{
					}

					if (callback != null)
						callback.callback(new ItemCreatedFromAssetCallbackArgs(true, Helpers.EmptyString, item, asset));
				}
				else
				{
					if (callback != null)
						callback.callback(new ItemCreatedFromAssetCallbackArgs(false,
								"Failed to parse asset and item UUIDs", UUID.Zero, UUID.Zero));
				}
			}
			else
			{
				// Failure
				if (callback != null)
					callback.callback(new ItemCreatedFromAssetCallbackArgs(false, status, UUID.Zero, UUID.Zero));
			}
		}

		@Override
		public void failed(Exception ex)
		{
			if (callback != null)
				callback.callback(new ItemCreatedFromAssetCallbackArgs(false, ex.getMessage(), UUID.Zero, UUID.Zero));
		}

		@Override
		public void cancelled()
		{
			if (callback != null)
				callback.callback(new ItemCreatedFromAssetCallbackArgs(false, "Operation canceled", UUID.Zero,
						UUID.Zero));
		}
	}

	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				LoginResponseData replyData = e.getReply();
				// Initialize the store here so we know who owns it:
				_Store = new InventoryStore(_Client);

				Logger.DebugLog("Setting InventoryRoot to " + replyData.InventoryRoot.toString(), _Client);
				synchronized (_Store)
				{
					_Store.setInventoryFolder(replyData.InventoryRoot);
					for (int i = 0; i < replyData.InventorySkeleton.length; i++)
					{
						_Store.add(replyData.InventorySkeleton[i]);
					}

					_Store.setLibraryFolder(replyData.LibraryRoot, replyData.LibraryOwner);
					for (int i = 0; i < replyData.LibrarySkeleton.length; i++)
					{
						_Store.add(replyData.LibrarySkeleton[i]);
					}
				}
				_Store.printUnresolved();
			}
			return false;
		}
	}

	/**
	 * Reply received when uploading an inventory asset
	 * 
	 * @param success
	 *            Has upload been successful
	 * @param status
	 *            Error message if upload failed
	 * @param itemID
	 *            Inventory asset UUID
	 * @param assetID
	 *            New asset UUID
	 */
	public class InventoryUploadedAssetCallbackArgs implements CallbackArgs
	{
		public boolean success;
		public String status;
		public UUID itemID;
		public UUID assetID;

		public InventoryUploadedAssetCallbackArgs(boolean success, String status, UUID itemID, UUID assetID)
		{
			this.success = success;
			this.status = status;
			this.itemID = itemID;
			this.assetID = assetID;
		}

	}

	public class UploadInventoryAssetComplete implements FutureCallback<OSD>
	{
		private final Callback<InventoryUploadedAssetCallbackArgs> callback;
		private final byte[] itemData;
		private final UUID assetID;

		public UploadInventoryAssetComplete(Callback<InventoryUploadedAssetCallbackArgs> callback, byte[] itemData,
				UUID assetID)
		{
			this.callback = callback;
			this.itemData = itemData;
			this.assetID = assetID;
		}

		@Override
		public void completed(OSD result)
		{
			OSDMap contents = (OSDMap) ((result instanceof OSDMap) ? result : null);
			if (contents != null)
			{
				String status = contents.get("state").AsString();
				if (status.equals("upload"))
				{
					URI uploadURL = contents.get("uploader").AsUri();
					if (uploadURL != null)
					{
						// This makes the assumption that all uploads go to
						// CurrentSim, to avoid the problem of HttpRequestState
						// not knowing anything about simulators
						try
						{
							CapsClient upload = new CapsClient();
							upload.setResultCallback(new UploadInventoryAssetComplete(callback, itemData, assetID));
							upload.executeHttpPost(uploadURL, itemData, "application/octet-stream", _Client.Settings.CAPS_TIMEOUT);
						}
						catch (Exception ex)
						{
							failed(ex);
						}
					}
					else
					{
						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(false, "Missing uploader URL",
									UUID.Zero, UUID.Zero));
					}
				}
				else if (status.equals("complete"))
				{
					if (contents.containsKey("new_asset"))
					{
						UUID new_asset = contents.get("new_asset").AsUUID();
						// Request full item update so we keep store in sync
						try
						{
							RequestFetchInventory(assetID, new_asset);
						}
						catch (Exception ex)
						{
						}

						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(true, Helpers.EmptyString,
									assetID, new_asset));
					}
					else
					{
						if (callback != null)
							callback.callback(new InventoryUploadedAssetCallbackArgs(false,
									"Failed to parse asset and item UUIDs", UUID.Zero, UUID.Zero));
					}
				}
				else
				{
					if (callback != null)
						callback.callback(new InventoryUploadedAssetCallbackArgs(false, status, UUID.Zero, UUID.Zero));
				}
			}
			else
			{
				if (callback != null)
					callback.callback(new InventoryUploadedAssetCallbackArgs(false, "Unrecognized or empty response",
							UUID.Zero, UUID.Zero));
			}
		}

		@Override
		public void failed(Exception ex)
		{
			if (callback != null)
			{
				String message;
				if (ex instanceof HttpResponseException)
					message = String.format("HTTP Status: %d, %s", ((HttpResponseException) ex).getStatusCode(),
							ex.getMessage());
				else
					message = ex.getMessage();

				callback.callback(new InventoryUploadedAssetCallbackArgs(false, message, UUID.Zero, UUID.Zero));
			}
		}

		@Override
		public void cancelled()
		{
			if (callback != null)
				callback.callback(new InventoryUploadedAssetCallbackArgs(false, "Operation cancelled", UUID.Zero,
						UUID.Zero));
		}
	}

	public class ScriptUpdatedCallbackArgs implements CallbackArgs
	{
		public boolean success;
		public String message;
		public boolean compiled;
		public ArrayList<String> errors;
		public UUID itemID;
		public UUID assetID;

		public ScriptUpdatedCallbackArgs(boolean success, String message, boolean compiled, ArrayList<String> errors,
				UUID itemID, UUID assetID)
		{
			this.success = success;
			this.message = message;
			this.compiled = compiled;
			this.errors = errors;
			this.itemID = itemID;
			this.assetID = assetID;
		}
	}

	public class UpdateScriptAgentInventoryResponse implements FutureCallback<OSD>
	{
		private final Callback<ScriptUpdatedCallbackArgs> callback;
		private final byte[] itemData;
		private final UUID scriptID;

		public UpdateScriptAgentInventoryResponse(Callback<ScriptUpdatedCallbackArgs> callback, byte[] itemData,
				UUID scriptID)
		{
			this.callback = callback;
			this.itemData = itemData;
			this.scriptID = scriptID;
		}

		@Override
		public void completed(OSD result)
		{
			OSDMap contents = (OSDMap) result;
			String status = contents.get("state").AsString();
			if (status.equals("upload"))
			{
				String uploadURL = contents.get("uploader").AsString();

				try
				{
					CapsClient upload = new CapsClient();
					upload.setResultCallback(new UpdateScriptAgentInventoryResponse(callback, itemData, scriptID));
					upload.executeHttpPost(new URI(uploadURL), itemData, "application/octet-stream", _Client.Settings.CAPS_TIMEOUT);		
				}
				catch (Exception ex)
				{
					failed(ex);
				}
			}
			else if (status.equals("complete"))
			{
				if (contents.containsKey("new_asset"))
				{
					UUID new_asset = contents.get("new_asset").AsUUID();
					// Request full item update so we keep store in sync
					try
					{
						RequestFetchInventory(scriptID, new_asset);
					}
					catch (Exception ex)
					{
					}

					ArrayList<String> compileErrors = null;

					if (contents.containsKey("errors"))
					{
						OSDArray errors = (OSDArray) contents.get("errors");
						compileErrors = new ArrayList<String>(errors.size());

						for (int i = 0; i < errors.size(); i++)
						{
							compileErrors.add(errors.get(i).AsString());
						}
					}
					if (callback != null)
						callback.callback(new ScriptUpdatedCallbackArgs(true, status, contents.get("compiled")
								.AsBoolean(), compileErrors, scriptID, new_asset));
				}
				else
				{
					if (callback != null)
						callback.callback(new ScriptUpdatedCallbackArgs(false, "Failed to parse asset UUID", false,
								null, UUID.Zero, UUID.Zero));
				}
			}
		}

		@Override
		public void failed(Exception ex)
		{
			if (callback != null)
				callback.callback(new ScriptUpdatedCallbackArgs(false, ex.getMessage(), false, null, UUID.Zero,
						UUID.Zero));
		}

		@Override
		public void cancelled()
		{
			if (callback != null)
				callback.callback(new ScriptUpdatedCallbackArgs(false, "Operation cancelled", false, null, UUID.Zero,
						UUID.Zero));
		}
	}

	// #endregion Internal Handlers

	// #region Packet Handlers
	private final void HandleSaveAssetIntoInventory(Packet packet, Simulator simulator) throws Exception
	{
		SaveAssetIntoInventoryPacket save = (SaveAssetIntoInventoryPacket) packet;
		OnSaveAssetToInventory.dispatch(new SaveAssetToInventoryCallbackArgs(save.InventoryData.ItemID,
				save.InventoryData.NewAssetID));
	}

	private final void HandleInventoryDescendents(Packet packet, Simulator simulator) throws Exception
	{
		InventoryDescendentsPacket reply = (InventoryDescendentsPacket) packet;

		synchronized (_Store)
		{
			if (reply.AgentData.Descendents > 0)
			{
				// Iterate folders in this packet
				for (int i = 0; i < reply.FolderData.length; i++)
				{
					// InventoryDescendantsReply sends a null folder if the
					// parent doesnt contain any folders
					if (reply.FolderData[0].FolderID.equals(UUID.Zero))
					{
						break;
					}
					// If folder already exists then ignore, we assume the version cache
					// logic is working and if the folder is stale then it should not be present.
					else if (!_Store.containsFolder(reply.FolderData[i].FolderID))
					{
						InventoryFolder folder = new InventoryFolder(reply.FolderData[i].FolderID, reply.FolderData[i].ParentID, reply.AgentData.OwnerID);
						folder.name = Helpers.BytesToString(reply.FolderData[i].getName());
						folder.preferredType = AssetType.setValue(reply.FolderData[i].Type);
						_Store.add(folder);
					}
				}

				// Iterate items in this packet
				for (int i = 0; i < reply.ItemData.length; i++)
				{
					// InventoryDescendantsReply sends a null item if the parent
					// doesnt contain any items.
					if (reply.ItemData[i].ItemID.equals(UUID.Zero))
					{
						break;
					}

					InventoryItem item;
					/*
					 * Objects that have been attached in-world prior to being
					 * stored on the asset server are stored with the
					 * InventoryType of 0 (Texture) instead of 17 (Attachment)
					 * 
					 * This corrects that behavior by forcing Object Asset types
					 * that have an invalid InventoryType with the proper
					 * InventoryType of Attachment.
					 */
					InventoryType invType = InventoryType.setValue(reply.ItemData[i].InvType);
					if (AssetType.Object.equals(AssetType.setValue(reply.ItemData[i].Type)) && InventoryType.Texture.equals(invType))
					{
						invType = InventoryType.Attachment;
					}
					item = InventoryItem.create(invType, reply.ItemData[i].ItemID, reply.ItemData[i].FolderID, reply.AgentData.OwnerID);
					item.name = Helpers.BytesToString(reply.ItemData[i].getName());
					item.CreatorID = reply.ItemData[i].CreatorID;
					item.assetType = AssetType.setValue(reply.ItemData[i].Type);
					item.AssetID = reply.ItemData[i].AssetID;
					item.CreationDate = Helpers.UnixTimeToDateTime(reply.ItemData[i].CreationDate);
					item.Description = Helpers.BytesToString(reply.ItemData[i].getDescription());
					item.ItemFlags = reply.ItemData[i].Flags;
					item.GroupID = reply.ItemData[i].GroupID;
					item.GroupOwned = reply.ItemData[i].GroupOwned;
					item.Permissions = new Permissions(reply.ItemData[i].BaseMask, reply.ItemData[i].EveryoneMask,
							reply.ItemData[i].GroupMask, reply.ItemData[i].NextOwnerMask, reply.ItemData[i].OwnerMask);
					item.SalePrice = reply.ItemData[i].SalePrice;
					item.saleType = SaleType.setValue(reply.ItemData[i].SaleType);
					_Store.add(item);
				}
			}

			InventoryFolder parent = null;

			if (_Store.containsFolder(reply.AgentData.FolderID))
			{
				parent = _Store.getFolder(reply.AgentData.FolderID);
			}

			if (parent == null)
			{
				Logger.Log("Don't have a reference to FolderID " + reply.AgentData.FolderID.toString()
						+ " or it is not a folder", LogLevel.Error, _Client);
				return;
			}

			if (reply.AgentData.Version < parent.version)
			{
				Logger.Log("Got an outdated InventoryDescendents packet for folder " + parent.name
						+ ", this version = " + reply.AgentData.Version + ", latest version = " + parent.version,
						LogLevel.Warning, _Client);
				return;
			}

			parent.version = reply.AgentData.Version;
			// FIXME: reply.AgentData.Descendants is not parentFolder.DescendentCount
			// if we didn't request items and folders
			parent.descendentCount = reply.AgentData.Descendents;

			// #region FindObjectByPath Handling

			if (_Searches.size() > 0)
			{
				ArrayList<InventorySearch> remaining = new ArrayList<InventorySearch>();

				synchronized (_Searches)
				{
					// Iterate over all of the outstanding searches
					for (int i = 0; i < _Searches.size(); i++)
					{
						InventorySearch search = _Searches.get(i);
						ArrayList<InventoryNode> folderContents;
						folderContents = _Store.getContents(search.Folder);

						// Iterate over all of the inventory objects in the base
						// search folder
						for (int j = 0; j < folderContents.size(); j++)
						{
							// Check if this inventory object matches the
							// current path node
							if (folderContents.get(j).name.equals(search.Path[search.Level]))
							{
								String string = "";
								int k = 0;
								for (; k < search.Path.length - 1; k++)
								{
									string.concat(search.Path[k] + "/");
								}
								string.concat(search.Path[k]);

								if (search.Level == search.Path.length - 1)
								{
									Logger.DebugLog("Finished path search of " + string, _Client);

									// This is the last node in the path, fire
									// the callback and clean up
									OnFindObjectByPathReply.dispatch(new FindObjectByPathReplyCallbackArgs(string,
											folderContents.get(j).itemID));
									break;
								}

								// We found a match but it is not the end of the
								// path, request the next level
								Logger.DebugLog(String.format("Matched level %d/%d in a path search of %s",
										search.Level, search.Path.length - 1, string), _Client);

								search.Folder = folderContents.get(j).itemID;
								search.Level++;
								remaining.add(search);

								RequestFolderContents(search.Folder, search.Owner, true, true,
										InventorySortOrder.ByName);
							}
						}
					}
					_Searches = remaining;
				}
			}
			// #endregion FindObjectByPath Handling

			// Callback for inventory folder contents being updated
			OnFolderUpdated.dispatch(new FolderUpdatedCallbackArgs(parent.itemID));
		}
	}

	/**
	 * UpdateCreateInventoryItem packets are received when a new inventory item
	 * is created. This may occur when an object that's rezzed in world is taken
	 * into inventory, when an item is created using the CreateInventoryItem
	 * packet, or when an object is purchased
	 * 
	 * @param sender
	 *            The sender
	 * @param e
	 *            The CallbackArgs object containing the packet data
	 */
	private final void HandleUpdateCreateInventoryItem(Packet packet, Simulator simulator) throws Exception
	{
		UpdateCreateInventoryItemPacket reply = (UpdateCreateInventoryItemPacket) packet;

		for (UpdateCreateInventoryItemPacket.InventoryDataBlock dataBlock : reply.InventoryData)
		{
			if (dataBlock.InvType == InventoryType.Folder.getValue())
			{
				Logger.Log("Received InventoryFolder in an UpdateCreateInventoryItem packet, this should not happen!",
						LogLevel.Error, _Client);
				continue;
			}

			InventoryItem item = InventoryItem.create(InventoryType.setValue(dataBlock.InvType), dataBlock.ItemID,
					                                  dataBlock.FolderID, dataBlock.OwnerID);
			item.name = Helpers.BytesToString(dataBlock.getName());
			item.assetType = AssetType.setValue(dataBlock.Type);
			item.AssetID = dataBlock.AssetID;
			item.CreationDate = Helpers.UnixTimeToDateTime(dataBlock.CreationDate);
			item.CreatorID = dataBlock.CreatorID;
			item.Description = Helpers.BytesToString(dataBlock.getDescription());
			item.ItemFlags = dataBlock.Flags;
			item.GroupID = dataBlock.GroupID;
			item.GroupOwned = dataBlock.GroupOwned;
			item.Permissions = new Permissions(dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
					dataBlock.NextOwnerMask, dataBlock.OwnerMask);
			item.SalePrice = dataBlock.SalePrice;
			item.saleType = SaleType.setValue(dataBlock.SaleType);

			/*
			 * When attaching new objects, an UpdateCreateInventoryItem packet
			 * will be returned by the server that has a FolderID/ParentUUID of
			 * zero. It is up to the client to make sure that the item gets a
			 * good folder, otherwise it will end up inaccessible in inventory.
			 */
			if (dataBlock.FolderID.equals(UUID.Zero))
			{
				// assign default folder for type
				item.parent = FindFolderForType(item.assetType);

				Logger.Log("Received an item through UpdateCreateInventoryItem with no parent folder, assigning to folder "
								+ item.parent.itemID, LogLevel.Info);

				// send update to the sim
				RequestUpdateItem(item);
			}
			
			synchronized (_Store)
			{
				// Update the local copy
				_Store.add(item);
			}

			// Look for an "item created" callback

			if (_ItemCreatedCallbacks.containsKey(dataBlock.CallbackID))
			{
				Callback<ItemCreatedCallbackArgs> callback = _ItemCreatedCallbacks.remove(dataBlock.CallbackID);

				try
				{
					callback.callback(new ItemCreatedCallbackArgs(true, item));
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}

			// TODO: Is this callback even triggered when items are copied?
			// Look for an "item copied" callback

			if (_ItemCopiedCallbacks.containsKey(dataBlock.CallbackID))
			{
				Callback<ItemCopiedCallbackArgs> callback = _ItemCopiedCallbacks.remove(dataBlock.CallbackID);

				try
				{
					callback.callback(new ItemCopiedCallbackArgs(item));
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}

			// This is triggered when an item is received from a task
			OnTaskItemReceived.dispatch(new TaskItemReceivedCallbackArgs(item.itemID, dataBlock.FolderID, item.CreatorID, item.AssetID, item.getType()));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleMoveInventoryItem(Packet packet, Simulator simulator) throws Exception
	{
		MoveInventoryItemPacket move = (MoveInventoryItemPacket) packet;

		for (MoveInventoryItemPacket.InventoryDataBlock block : move.InventoryData)
		{
			InventoryNode node = _Store.getNode(block.ItemID);
			if (block.getNewName().length > 0)
				node.name = Helpers.BytesToString(block.getNewName());
			_Store.add(block.FolderID, node);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleBulkUpdateInventory(Packet packet, Simulator simulator) throws Exception
	{
		BulkUpdateInventoryPacket update = (BulkUpdateInventoryPacket) packet;
		
		if (update.FolderData.length > 0 && !update.FolderData[0].FolderID.equals(UUID.Zero))
		{
			synchronized (_Store)
			{
				for (BulkUpdateInventoryPacket.FolderDataBlock dataBlock : update.FolderData)
				{
                    InventoryFolder folder;
					if (!_Store.containsFolder(dataBlock.FolderID))
					{
						Logger.Log("Received BulkUpdate for unknown folder: " + dataBlock.FolderID, LogLevel.Debug, _Client);
                        folder = new InventoryFolder(dataBlock.FolderID, dataBlock.ParentID, update.AgentData.AgentID);
                    }
                    else
                    {
                        folder = _Store.getFolder(dataBlock.FolderID);
                    }
					if (dataBlock.getName() != null)
						folder.name = Helpers.BytesToString(dataBlock.getName());
					_Store.add(folder);
				}
			}
		}

		if (update.ItemData.length > 0 && !update.ItemData[0].ItemID.equals(UUID.Zero))
		{
			synchronized (_Store)
			{
				for (int i = 0; i < update.ItemData.length; i++)
				{
					BulkUpdateInventoryPacket.ItemDataBlock dataBlock = update.ItemData[i];

					InventoryItem item = SafeCreateInventoryItem(InventoryType.setValue(dataBlock.InvType), dataBlock.ItemID,
							                                     dataBlock.FolderID, dataBlock.OwnerID);

					item.assetType = AssetType.setValue(dataBlock.Type);
					if (!dataBlock.AssetID.equals(UUID.Zero))
					{
						item.AssetID = dataBlock.AssetID;
					}
					item.CreationDate = Helpers.UnixTimeToDateTime(dataBlock.CreationDate);
					item.CreatorID = dataBlock.CreatorID;
					item.Description = Helpers.BytesToString(dataBlock.getDescription());
					item.ItemFlags = dataBlock.Flags;
					item.GroupID = dataBlock.GroupID;
					item.GroupOwned = dataBlock.GroupOwned;
					item.name = Helpers.BytesToString(dataBlock.getName());
					item.Permissions = new Permissions(dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
							dataBlock.NextOwnerMask, dataBlock.OwnerMask);
					item.SalePrice = dataBlock.SalePrice;
					item.saleType = SaleType.setValue(dataBlock.SaleType);

					_Store.add(item);

					// Look for an "item created" callback
					if (_ItemCreatedCallbacks.containsKey(dataBlock.CallbackID))
					{
						Callback<ItemCreatedCallbackArgs> callback = _ItemCreatedCallbacks.remove(dataBlock.CallbackID);

						try
						{
							callback.callback(new ItemCreatedCallbackArgs(true, item));
						}
						catch (Throwable ex)
						{
							Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
						}
					}

					// Look for an "item copied" callback
					if (_ItemCopiedCallbacks.containsKey(dataBlock.CallbackID))
					{
						Callback<ItemCopiedCallbackArgs> callback = _ItemCopiedCallbacks.remove(dataBlock.CallbackID);

						try
						{
							callback.callback(new ItemCopiedCallbackArgs(item));
						}
						catch (Throwable ex)
						{
							Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
						}
					}
				}
			}
		}
	}

	private final void HandleBulkUpdateInventory(IMessage message, Simulator simulator)
	{
		BulkUpdateInventoryMessage msg = (BulkUpdateInventoryMessage) message;

		for (BulkUpdateInventoryMessage.FolderDataInfo newFolder : msg.FolderData)
        {
            if (newFolder.FolderID == UUID.Zero) continue;

            InventoryFolder folder;
            if (!_Store.containsFolder(newFolder.FolderID))
            {
                folder = new InventoryFolder(newFolder.FolderID);
            }
            else
            {
                folder = _Store.getFolder(newFolder.FolderID);
            }

            folder.name = newFolder.Name;
            folder.parentID = newFolder.ParentID;
            folder.preferredType = newFolder.Type;
            _Store.add(folder);
        }

        for (BulkUpdateInventoryMessage.ItemDataInfo newItem : msg.ItemData)
        {
            if (newItem.ItemID == UUID.Zero) continue;

			InventoryItem item = SafeCreateInventoryItem(newItem.InvType, newItem.ItemID, newItem.FolderID, newItem.OwnerID);

            item.assetType = newItem.Type;
            item.AssetID = newItem.AssetID;
            item.CreationDate = newItem.CreationDate;
            item.CreatorID = newItem.CreatorID;
            item.Description = newItem.Description;
            item.ItemFlags = newItem.Flags;
            item.GroupID = newItem.GroupID;
            item.GroupOwned = newItem.GroupOwned;
            item.name = newItem.Name;
            item.Permissions.BaseMask = newItem.BaseMask;
            item.Permissions.EveryoneMask = newItem.EveryoneMask;
            item.Permissions.GroupMask = newItem.GroupMask;
            item.Permissions.NextOwnerMask = newItem.NextOwnerMask;
            item.Permissions.OwnerMask = newItem.OwnerMask;
            item.SalePrice = newItem.SalePrice;
            item.saleType = newItem.saleType;

            _Store.add(item);

			// Look for an "item created" callback
			if (_ItemCreatedCallbacks.containsKey(newItem.CallbackID))
			{
				Callback<ItemCreatedCallbackArgs> callback = _ItemCreatedCallbacks.remove(newItem.CallbackID);

				try
				{
					callback.callback(new ItemCreatedCallbackArgs(true, item));
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}

			// Look for an "item copied" callback
			if (_ItemCopiedCallbacks.containsKey(newItem.CallbackID))
			{
				Callback<ItemCopiedCallbackArgs> callback = _ItemCopiedCallbacks.remove(newItem.CallbackID);

				try
				{
					callback.callback(new ItemCopiedCallbackArgs(item));
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}
        }
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleFetchInventoryReply(Packet packet, Simulator simulator) throws Exception
	{
		FetchInventoryReplyPacket reply = (FetchInventoryReplyPacket) packet;

		for (FetchInventoryReplyPacket.InventoryDataBlock dataBlock : reply.InventoryData)
		{
			if (dataBlock.InvType == InventoryType.Folder.getValue())
			{
				Logger.Log("Received FetchInventoryReply for an inventory folder, this should not happen!",
						LogLevel.Error, _Client);
				continue;
			}

			InventoryItem item = InventoryItem.create(InventoryType.setValue(dataBlock.InvType), dataBlock.ItemID,
					                                  dataBlock.FolderID, dataBlock.OwnerID);
			item.assetType = AssetType.setValue(dataBlock.Type);
			item.AssetID = dataBlock.AssetID;
			item.CreationDate = Helpers.UnixTimeToDateTime(dataBlock.CreationDate);
			item.CreatorID = dataBlock.CreatorID;
			item.Description = Helpers.BytesToString(dataBlock.getDescription());
			item.ItemFlags = dataBlock.Flags;
			item.GroupID = dataBlock.GroupID;
			item.GroupOwned = dataBlock.GroupOwned;
			item.name = Helpers.BytesToString(dataBlock.getName());
			item.Permissions = new Permissions(dataBlock.BaseMask, dataBlock.EveryoneMask, dataBlock.GroupMask,
					dataBlock.NextOwnerMask, dataBlock.OwnerMask);
			item.SalePrice = dataBlock.SalePrice;
			item.saleType = SaleType.setValue(dataBlock.SaleType);

			_Store.add(item);

			// Fire the callback for an item being fetched
			OnItemReceived.dispatch(new ItemReceivedCallbackArgs(item));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleReplyTaskInventory(Packet packet, Simulator simulator) throws Exception
	{
		ReplyTaskInventoryPacket reply = (ReplyTaskInventoryPacket) packet;
		OnTaskInventoryReply.dispatch(new TaskInventoryReplyCallbackArgs(reply.InventoryData.TaskID,
				reply.InventoryData.Serial, Helpers.BytesToString(reply.InventoryData.getFilename())));
	}

	private final void HandleScriptRunningReply(IMessage message, Simulator simulator)
	{
		ScriptRunningReplyMessage msg = (ScriptRunningReplyMessage) message;
		OnScriptRunningReply.dispatch(new ScriptRunningReplyCallbackArgs(msg.ObjectID, msg.ItemID, msg.Mono,
				msg.Running));
	}

	// #endregion Packet Handlers

	// #region CallbackArgs

	public class InventoryObjectOfferedCallbackArgs implements CallbackArgs
	{
		private final InstantMessage m_Offer;
		private final AssetType m_AssetType;
		private final UUID m_ObjectID;
		private final boolean m_FromTask;
		private boolean m_Accept;
		private UUID m_FolderID;

		/*
		 * Set to true to accept offer, false to decline it
		 */
		public final boolean getAccept()
		{
			return m_Accept;
		}

		public final void setAccept(boolean value)
		{
			m_Accept = value;
		}

		/*
		 * The folder to accept the inventory into, if null default folder for
		 * <see cref="AssetType"/> will be used
		 */
		public final UUID getFolderID()
		{
			return m_FolderID;
		}

		public final void setFolderID(UUID value)
		{
			m_FolderID = value;
		}

		public final InstantMessage getOffer()
		{
			return m_Offer;
		}

		public final AssetType getAssetType()
		{
			return m_AssetType;
		}

		public final UUID getObjectID()
		{
			return m_ObjectID;
		}

		public final boolean getFromTask()
		{
			return m_FromTask;
		}

		public InventoryObjectOfferedCallbackArgs(InstantMessage offerDetails, AssetType type, UUID objectID,
				boolean fromTask, UUID folderID)
		{
			this.m_Accept = false;
			this.m_FolderID = folderID;
			this.m_Offer = offerDetails;
			this.m_AssetType = type;
			this.m_ObjectID = objectID;
			this.m_FromTask = fromTask;
		}
	}

	public class FolderUpdatedCallbackArgs implements CallbackArgs
	{
		private final UUID m_FolderID;

		public final UUID getFolderID()
		{
			return m_FolderID;
		}

		public FolderUpdatedCallbackArgs(UUID folderID)
		{
			this.m_FolderID = folderID;
		}
	}

	public class ItemReceivedCallbackArgs implements CallbackArgs
	{
		private final InventoryItem m_Item;

		public final InventoryItem getItem()
		{
			return m_Item;
		}

		public ItemReceivedCallbackArgs(InventoryItem item)
		{
			this.m_Item = item;
		}
	}

	public class FindObjectByPathReplyCallbackArgs implements CallbackArgs
	{
		private final String m_Path;
		private final UUID m_InventoryObjectID;

		public final String getPath()
		{
			return m_Path;
		}

		public final UUID getInventoryObjectID()
		{
			return m_InventoryObjectID;
		}

		public FindObjectByPathReplyCallbackArgs(String path, UUID inventoryObjectID)
		{
			this.m_Path = path;
			this.m_InventoryObjectID = inventoryObjectID;
		}
	}

	/**
	 * Callback when an inventory object is accepted and received from a task
	 * inventory. This is the callback in which you actually get the ItemID, as
	 * in ObjectOfferedCallback it is null when received from a task.
	 */
	public class TaskItemReceivedCallbackArgs implements CallbackArgs
	{
		private final UUID m_ItemID;
		private final UUID m_FolderID;
		private final UUID m_CreatorID;
		private final UUID m_AssetID;
		private final InventoryType m_Type;

		public final UUID getItemID()
		{
			return m_ItemID;
		}

		public final UUID getFolderID()
		{
			return m_FolderID;
		}

		public final UUID getCreatorID()
		{
			return m_CreatorID;
		}

		public final UUID getAssetID()
		{
			return m_AssetID;
		}

		public final InventoryType getType()
		{
			return m_Type;
		}

		public TaskItemReceivedCallbackArgs(UUID itemID, UUID folderID, UUID creatorID, UUID assetID, InventoryType type)
		{
			this.m_ItemID = itemID;
			this.m_FolderID = folderID;
			this.m_CreatorID = creatorID;
			this.m_AssetID = assetID;
			this.m_Type = type;
		}
	}

	public class TaskInventoryReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_ItemID;
		private final short m_Serial;
		private final String m_AssetFilename;

		public final UUID getItemID()
		{
			return m_ItemID;
		}

		public final short getSerial()
		{
			return m_Serial;
		}

		public final String getAssetFilename()
		{
			return m_AssetFilename;
		}

		public TaskInventoryReplyCallbackArgs(UUID itemID, short serial, String assetFilename)
		{
			this.m_ItemID = itemID;
			this.m_Serial = serial;
			this.m_AssetFilename = assetFilename;
		}
	}

	public class SaveAssetToInventoryCallbackArgs implements CallbackArgs
	{
		private final UUID m_ItemID;
		private final UUID m_NewAssetID;

		public final UUID getItemID()
		{
			return m_ItemID;
		}

		public final UUID getNewAssetID()
		{
			return m_NewAssetID;
		}

		public SaveAssetToInventoryCallbackArgs(UUID itemID, UUID newAssetID)
		{
			this.m_ItemID = itemID;
			this.m_NewAssetID = newAssetID;
		}
	}

	public class ScriptRunningReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_ObjectID;
		private final UUID m_ScriptID;
		private final boolean m_IsMono;
		private final boolean m_IsRunning;

		public final UUID getObjectID()
		{
			return m_ObjectID;
		}

		public final UUID getScriptID()
		{
			return m_ScriptID;
		}

		public final boolean getIsMono()
		{
			return m_IsMono;
		}

		public final boolean getIsRunning()
		{
			return m_IsRunning;
		}

		public ScriptRunningReplyCallbackArgs(UUID objectID, UUID sctriptID, boolean isMono, boolean isRunning)
		{
			this.m_ObjectID = objectID;
			this.m_ScriptID = sctriptID;
			this.m_IsMono = isMono;
			this.m_IsRunning = isRunning;
		}
	}
}
