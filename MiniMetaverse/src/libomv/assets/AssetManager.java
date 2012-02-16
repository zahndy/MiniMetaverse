/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
 * Portions Copyright (c) 2006, Lateral Arts Limited
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
package libomv.assets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.nio.concurrent.FutureCallback;

import libomv.DownloadManager;
import libomv.GridClient;
import libomv.Settings;
import libomv.Simulator;
import libomv.DownloadManager.DownloadRequest;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDMap;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.TexturePipeline.TextureDownloadCallback;
import libomv.assets.TexturePipeline.TextureRequestState;
import libomv.capabilities.AsyncHTTPClient;
import libomv.capabilities.CapsClient;
import libomv.capabilities.CapsMessage.UploadBakedTextureMessage;
import libomv.capabilities.CapsMessage.UploaderRequestComplete;
import libomv.capabilities.CapsMessage.UploaderRequestUpload;
import libomv.inventory.InventoryItem;
import libomv.packets.AbortXferPacket;
import libomv.packets.AssetUploadCompletePacket;
import libomv.packets.AssetUploadRequestPacket;
import libomv.packets.ConfirmXferPacketPacket;
import libomv.packets.InitiateDownloadPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RequestXferPacket;
import libomv.packets.SendXferPacketPacket;
import libomv.packets.TransferAbortPacket;
import libomv.packets.TransferInfoPacket;
import libomv.packets.TransferPacketPacket;
import libomv.packets.TransferRequestPacket;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.RefObject;
import libomv.utils.TimeoutEvent;

// Summary description for AssetManager.
public class AssetManager implements PacketCallback
{
	// #region Enums
	public enum EstateAssetType
	{
		None, Covenant;

		public static EstateAssetType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(EstateAssetType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum StatusCode
	{
		OK(0),
		// Transfer completed
		Done(1), Skip(2), Abort(3),
		// Unknown error occurred
		Error(-1),
		// Equivalent to a 404 error
		UnknownSource(-2),
		// Client does not have permission for that resource
		InsufficientPermissions(-3),
		// Unknown status
		Unknown(-4);

		public static StatusCode setValue(int value)
		{
			for (StatusCode e : values())
			{
				if (e._value == value)
					return e;
			}
			return Unknown;
		}

		public static byte getValue(StatusCode value)
		{
			for (StatusCode e : values())
			{
				if (e == value)
					return e._value;
			}
			return Unknown._value;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private StatusCode(int value)
		{
			_value = (byte) value;
		}
	}

	public enum ChannelType
	{
		Unknown,
		// Unknown
		Misc,
		// Virtually all asset transfers use this channel
		Asset;

		public static ChannelType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(ChannelType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum SourceType
	{
		//
		Unknown,
		// Asset from the asset server
		Asset,
		// Inventory item
		SimInventoryItem,
		// Estate asset, such as an estate covenant
		SimEstate;

		public static SourceType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(SourceType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum TargetType
	{
		Unknown, File, VFile;

		public static TargetType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(TargetType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum ImageType
	{
		Normal, Baked;

		public static ImageType setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(ImageType value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum ImageCodec
	{
		Invalid, RGB, J2C, BMP, TGA, JPEG, DXT, PNG;

		public static ImageCodec setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(ImageCodec value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	public enum TransferError
	{
		None(0), Failed(-1), AssetNotFound(-3), AssetNotFoundInDatabase(-4), InsufficientPermissions(-5), EOF(-39), CannotOpenFile(
				-42), FileNotFound(-43), FileIsEmpty(-44), TCPTimeout(-23016), CircuitGone(-23017);

		public static TransferError setValue(int value)
		{
			for (TransferError e : values())
			{
				if (e._value == value)
					return e;
			}
			return Failed;
		}

		public static byte getValue(TransferError value)
		{
			for (TransferError e : values())
			{
				if (e == value)
					return e._value;
			}
			return Failed._value;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private TransferError(int value)
		{
			_value = (byte) value;
		}
	}

	// #endregion Enums

	// #region Transfer Classes

	public class Transfer
	{
		public UUID ID;
		public int Size;
		public byte[] AssetData;
		public int Transferred;
		public boolean Success;
		public libomv.assets.AssetItem.AssetType AssetType;

		public Transfer()
		{
			AssetData = Helpers.EmptyBytes;
		}
	}

	public class AssetDownload extends Transfer
	{
		public UUID AssetID;
		public ChannelType Channel;
		public SourceType Source;
		public TargetType Target;
		public StatusCode Status;
		public float Priority;
		public Simulator Simulator;
		public AssetReceivedCallback Callback;
		private TimeoutEvent<Boolean> HeaderReceivedEvent;

		public AssetDownload()
		{
			super();
			HeaderReceivedEvent = new TimeoutEvent<Boolean>();
		}
	}

	public class XferDownload extends Transfer
	{
		public long XferID;
		public UUID VFileID;
		public int PacketNum;
		public String Filename = Helpers.EmptyString;
		public TransferError Error = TransferError.None;

		public XferDownload()
		{
			super();
		}
	}

	public class ImageDownload extends Transfer
	{
		public short PacketCount;
		public ImageCodec Codec;
		public Simulator Simulator;
		public SortedMap<Short, Short> PacketsSeen;
		public ImageType ImageType;
		public int DiscardLevel;
		public float Priority;
		int InitialDataSize;
		// #if Debug_Timing
		long TimeSinceLastPacket;
		// #endif
		TimeoutEvent<Boolean> HeaderReceivedEvent;

		public ImageDownload()
		{
			super();
			HeaderReceivedEvent = new TimeoutEvent<Boolean>();
		}
	}

	public class AssetUpload extends Transfer
	{
		public UUID AssetID;
		public libomv.assets.AssetItem.AssetType Type;
		public long XferID;
		public int PacketNum;

		public AssetUpload()
		{
			super();
		}
	}

	public class ImageRequest
	{
		public UUID ImageID;
		public ImageType Type;
		public float Priority;
		public int DiscardLevel;

		public ImageRequest(UUID imageid, ImageType type, float priority, int discardLevel)
		{
			ImageID = imageid;
			Type = type;
			Priority = priority;
			DiscardLevel = discardLevel;
		}

	}

	// #endregion Transfer Classes

	// Number of milliseconds to wait for a transfer header packet if out of
	// order data was received
	private static final int TRANSFER_HEADER_TIMEOUT = 1000 * 15;

	// #region Callbacks

	/**
	 * Callback used for various asset download requests
	 * 
	 * @param transfer
	 *            Transfer information
	 * @param asset
	 *            Downloaded asset, null on fail
	 */
	public abstract class AssetReceivedCallback
	{
		abstract public void callback(AssetDownload transfer, AssetItem asset);
	}

	/**
	 * Callback used upon completion of baked texture upload
	 * 
	 * @param newAssetID
	 *            Asset UUID of the newly uploaded baked texture
	 */
	public abstract class BakedTextureUploadedCallback
	{
		abstract public void callback(UUID newAssetID);
	}

    public abstract class MeshDownloadCallback
    {
    	abstract public void callback(boolean success, AssetMesh assetMesh);
    }

    // #endregion Callback

	// #region Callback

	// Provides data for XferReceived event
	public class XferReceivedCallbackArgs implements CallbackArgs
	{
		private final XferDownload m_Xfer;

		// Xfer data
		public final XferDownload getXfer()
		{
			return m_Xfer;
		}

		public XferReceivedCallbackArgs(XferDownload xfer)
		{
			this.m_Xfer = xfer;
		}
	}

	public CallbackHandler<XferReceivedCallbackArgs> OnXferReceived = new CallbackHandler<XferReceivedCallbackArgs>();

	// Provides data for AssetUploaded event
	public class AssetUploadCallbackArgs implements CallbackArgs
	{
		private final AssetUpload m_Upload;

		// Upload data
		public final AssetUpload getUpload()
		{
			return m_Upload;
		}

		public AssetUploadCallbackArgs(AssetUpload upload)
		{
			this.m_Upload = upload;
		}
	}

	private CallbackHandler<AssetUploadCallbackArgs> OnAssetUploaded = new CallbackHandler<AssetUploadCallbackArgs>();
	private CallbackHandler<AssetUploadCallbackArgs> OnUploadProgress = new CallbackHandler<AssetUploadCallbackArgs>();

	// Provides data for InitiateDownloaded event
	public class InitiateDownloadCallbackArgs implements CallbackArgs
	{
		private final String m_SimFileName;
		private final String m_ViewerFileName;

		// Filename used on the simulator
		public final String getSimFileName()
		{
			return m_SimFileName;
		}

		// Filename used by the client
		public final String getViewerFileName()
		{
			return m_ViewerFileName;
		}

		public InitiateDownloadCallbackArgs(String simFilename, String viewerFilename)
		{
			this.m_SimFileName = simFilename;
			this.m_ViewerFileName = viewerFilename;
		}
	}

	private CallbackHandler<InitiateDownloadCallbackArgs> OnInitiateDownload = new CallbackHandler<InitiateDownloadCallbackArgs>();

	// Provides data for ImageReceiveProgress event
	public class ImageReceiveProgressCallbackArgs implements CallbackArgs
	{
		private final UUID m_ImageID;
		private final long m_Received;
		private final long m_Total;

		// UUID of the image that is in progress
		public final UUID getImageID()
		{
			return m_ImageID;
		}

		// Number of bytes received so far
		public final long getReceived()
		{
			return m_Received;
		}

		// Image size in bytes
		public final long getTotal()
		{
			return m_Total;
		}

		public ImageReceiveProgressCallbackArgs(UUID imageID, long received, long total)
		{
			this.m_ImageID = imageID;
			this.m_Received = received;
			this.m_Total = total;
		}
	}

	private CallbackHandler<ImageReceiveProgressCallbackArgs> OnImageReceiveProgress = new CallbackHandler<ImageReceiveProgressCallbackArgs>();

	// #endregion Events

	// Texture download cache
	private AssetCache _Cache;

	public AssetCache getCache()
	{
		return _Cache;
	}

	private TexturePipeline _Texture;

    private DownloadManager _HttpDownloads;

    private GridClient _Client;

	private HashMap<UUID, Transfer> _Transfers;

	private ExecutorService _ThreadPool;
	private Future<?> _ThreadResult = null;

	private AtomicReference<AssetUpload> PendingUpload;
	private volatile boolean WaitingForUploadConfirm = false;

	/*
	 * Default constructor
	 * 
	 * @param client A reference to the GridClient object
	 */
	public AssetManager(GridClient client)
	{
		_Client = client;
		_Cache = new AssetCache(client, this);
		_Transfers = new HashMap<UUID, Transfer>();
		_Texture = new TexturePipeline(client, _Cache);

		_ThreadPool = Executors.newSingleThreadExecutor();

		_HttpDownloads = new DownloadManager();
			
		// Transfer packets for downloading large assets
		_Client.Network.RegisterCallback(PacketType.TransferInfo, this);
		_Client.Network.RegisterCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		_Client.Network.RegisterCallback(PacketType.RequestXfer, this);
		_Client.Network.RegisterCallback(PacketType.ConfirmXferPacket, this);
		_Client.Network.RegisterCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		_Client.Network.RegisterCallback(PacketType.SendXferPacket, this);
		_Client.Network.RegisterCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		_Client.Network.RegisterCallback(PacketType.InitiateDownload, this);
	}

	@Override
	protected void finalize()
	{
		_Client = null;
		_Cache = null;
		_Texture = null;

		_Transfers = null;

		if (_ThreadResult != null)
			_ThreadResult.cancel(true);
		_ThreadPool.shutdownNow();

		// Transfer packets for downloading large assets
		_Client.Network.UnregisterCallback(PacketType.TransferInfo, this);
		_Client.Network.UnregisterCallback(PacketType.TransferPacket, this);

		// Xfer packets for uploading large assets
		_Client.Network.UnregisterCallback(PacketType.RequestXfer, this);
		_Client.Network.UnregisterCallback(PacketType.ConfirmXferPacket, this);
		_Client.Network.UnregisterCallback(PacketType.AssetUploadComplete, this);

		// Xfer packets for downloading misc assets
		_Client.Network.UnregisterCallback(PacketType.SendXferPacket, this);
		_Client.Network.UnregisterCallback(PacketType.AbortXfer, this);

		// Simulator is responding to a request to download a file
		_Client.Network.UnregisterCallback(PacketType.InitiateDownload, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case TransferInfo:
				HandleTransferInfo(packet, simulator);
				break;
			case TransferPacket:
				HandleTransferPacket(packet, simulator);
				break;
			case RequestXfer:
				HandleRequestXfer(packet, simulator);
				break;
			case ConfirmXferPacket:
				HandleConfirmXferPacket(packet, simulator);
				break;
			case AssetUploadComplete:
				HandleAssetUploadComplete(packet, simulator);
				break;
			case SendXferPacket:
				HandleSendXferPacket(packet, simulator);
				break;
			case AbortXfer:
				HandleAbortXfer(packet, simulator);
				break;
			case InitiateDownload:
				HandleInitiateDownloadPacket(packet, simulator);
				break;
		}
	}

	/**
	 * Request an asset download
	 * 
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param callback
	 *            The callback to fire when the simulator responds with the
	 *            asset data
	 * @throws Exception
	 */
	public final void RequestAsset(UUID assetID, AssetType type, boolean priority, AssetReceivedCallback callback)
			throws Exception
	{
		RequestAsset(assetID, type, priority, SourceType.Asset, new UUID(), callback);
	}

	/**
	 * Request an asset download
	 * 
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param sourceType
	 *            Source location of the requested asset
	 * @param callback
	 *            The callback to fire when the simulator responds with the
	 *            asset data
	 * @throws Exception
	 */
	public final void RequestAsset(UUID assetID, AssetType type, boolean priority, SourceType sourceType,
			AssetReceivedCallback callback) throws Exception
	{
		RequestAsset(assetID, type, priority, sourceType, new UUID(), callback);
	}

	/**
	 * Request an asset download
	 * 
	 * @param assetID
	 *            Asset UUID
	 * @param type
	 *            Asset type, must be correct for the transfer to succeed
	 * @param priority
	 *            Whether to give this transfer an elevated priority
	 * @param sourceType
	 *            Source location of the requested asset
	 * @param transactionID
	 *            UUID of the transaction
	 * @param callback
	 *            The callback to fire when the simulator responds with the
	 *            asset data
	 * @throws Exception
	 */
	public final void RequestAsset(UUID assetID, AssetType type, boolean priority, SourceType sourceType,
			UUID transactionID, AssetReceivedCallback callback) throws Exception
	{
		AssetDownload transfer = new AssetDownload();
		transfer.ID = transactionID;
		transfer.AssetID = assetID;
		// transfer.AssetType = type; // Set in TransferInfoHandler.
		transfer.Priority = 100.0f + (priority ? 1.0f : 0.0f);
		transfer.Channel = ChannelType.Asset;
		transfer.Source = sourceType;
		transfer.Simulator = _Client.Network.getCurrentSim();
		transfer.Callback = callback;

		// Check asset cache first
		if (callback != null && _Cache.containsKey(assetID))
		{
			byte[] data = _Cache.GetCachedAssetBytes(assetID);
			transfer.AssetData = data;
			transfer.Success = true;
			transfer.Status = StatusCode.OK;

			AssetItem asset = CreateAssetWrapper(type);
			asset.AssetData = data;
			asset.setAssetID(assetID);

			try
			{
				callback.callback(transfer, asset);
			}
			catch (Throwable ex)
			{
				Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
			}
			return;
		}

		// Add this transfer to the dictionary
		synchronized (_Transfers)
		{
			_Transfers.put(transfer.ID, transfer);
		}

		// Build the request packet and send it
		TransferRequestPacket request = new TransferRequestPacket();
		request.TransferInfo.ChannelType = transfer.Channel.getValue();
		request.TransferInfo.Priority = transfer.Priority;
		request.TransferInfo.SourceType = transfer.Source.getValue();
		request.TransferInfo.TransferID = transfer.ID;

		byte[] paramField = new byte[20];
		System.arraycopy(assetID.GetBytes(), 0, paramField, 0, 16);
		System.arraycopy(Helpers.Int32ToBytesL(type.getValue()), 0, paramField, 16, 4);
		request.TransferInfo.setParams(paramField);

		transfer.Simulator.SendPacket(request);
	}

	/**
	 * Request an asset download through the almost deprecated Xfer system
	 * 
	 * @param filename
	 *            Filename of the asset to request
	 * @param deleteOnCompletion
	 *            Whether or not to delete the asset off the server after it is
	 *            retrieved
	 * @param useBigPackets
	 *            Use large transfer packets or not
	 * @param vFileID
	 *            UUID of the file to request, if filename is left empty
	 * @param vFileType
	 *            Asset type of <code>vFileID</code>, or
	 *            <code>AssetType.Unknown</code> if filename is not empty
	 * @param fromCache
	 *            Sets the FilePath in the request to Cache (4) if true,
	 *            otherwise Unknown (0) is used
	 * @return
	 * @throws Exception
	 */
	public final long RequestAssetXfer(String filename, boolean deleteOnCompletion, boolean useBigPackets,
			UUID vFileID, AssetType vFileType, boolean fromCache) throws Exception
	{
		UUID uuid = new UUID();
		long id = uuid.AsLong();

		XferDownload transfer = new XferDownload();
		transfer.XferID = id;
		transfer.ID = new UUID(id); // Our dictionary tracks transfers with
									// UUIDs, so convert the long back
		transfer.Filename = filename;
		transfer.VFileID = vFileID;
		transfer.AssetType = vFileType;

		// Add this transfer to the dictionary
		synchronized (_Transfers)
		{
			_Transfers.put(transfer.ID, transfer);
		}

		RequestXferPacket request = new RequestXferPacket();
		request.XferID.ID = id;
		request.XferID.setFilename(Helpers.StringToBytes(filename));
		request.XferID.FilePath = fromCache ? (byte) 4 : (byte) 0;
		request.XferID.DeleteOnCompletion = deleteOnCompletion;
		request.XferID.UseBigPackets = useBigPackets;
		request.XferID.VFileID = vFileID;
		request.XferID.VFileType = vFileType.getValue();

		_Client.Network.SendPacket(request);

		return id;
	}

	/**
	 * 
	 * 
	 * @param assetID
	 *            Use UUID.Zero if you do not have the asset ID but have all the
	 *            necessary permissions
	 * @param itemID
	 *            The item ID of this asset in the inventory
	 * @param taskID
	 *            Use UUID.Zero if you are not requesting an asset from an
	 *            object inventory
	 * @param ownerID
	 *            The owner of this asset
	 * @param type
	 *            Asset type
	 * @param priority
	 *            Whether to prioritize this asset download or not
	 * @param callback
	 * @throws Exception
	 */
	public final void RequestInventoryAsset(UUID assetID, UUID itemID, UUID taskID, UUID ownerID, AssetType type,
			boolean priority, AssetReceivedCallback callback) throws Exception
	{
		AssetDownload transfer = new AssetDownload();
		transfer.ID = new UUID();
		transfer.AssetID = assetID;
		// transfer.AssetType = type; // Set in TransferInfoHandler.
		transfer.Priority = 100.0f + (priority ? 1.0f : 0.0f);
		transfer.Channel = ChannelType.Asset;
		transfer.Source = SourceType.SimInventoryItem;
		transfer.Simulator = _Client.Network.getCurrentSim();
		transfer.Callback = callback;

		// Check asset cache first
		if (callback != null && _Cache.containsKey(assetID))
		{
			byte[] data = _Cache.GetCachedAssetBytes(assetID);
			transfer.AssetData = data;
			transfer.Success = true;
			transfer.Status = StatusCode.OK;

			AssetItem asset = CreateAssetWrapper(type);
			asset.AssetData = data;
			asset.setAssetID(assetID);

			try
			{
				callback.callback(transfer, asset);
			}
			catch (Throwable ex)
			{
				Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
			}

			return;
		}

		// Add this transfer to the dictionary
		synchronized (_Transfers)
		{
			_Transfers.put(transfer.ID, transfer);
		}

		// Build the request packet and send it
		TransferRequestPacket request = new TransferRequestPacket();
		request.TransferInfo.ChannelType = transfer.Channel.getValue();
		request.TransferInfo.Priority = transfer.Priority;
		request.TransferInfo.SourceType = transfer.Source.getValue();
		request.TransferInfo.TransferID = transfer.ID;

		byte[] paramField = new byte[100];
		System.arraycopy(_Client.Self.getAgentID().GetBytes(), 0, paramField, 0, 16);
		System.arraycopy(_Client.Self.getSessionID().GetBytes(), 0, paramField, 16, 16);
		System.arraycopy(ownerID.GetBytes(), 0, paramField, 32, 16);
		System.arraycopy(taskID.GetBytes(), 0, paramField, 48, 16);
		System.arraycopy(itemID.GetBytes(), 0, paramField, 64, 16);
		System.arraycopy(assetID.GetBytes(), 0, paramField, 80, 16);
		System.arraycopy(Helpers.Int32ToBytesL(type.getValue()), 0, paramField, 96, 4);
		request.TransferInfo.setParams(paramField);

		transfer.Simulator.SendPacket(request);
	}

	public final void RequestInventoryAsset(InventoryItem item, boolean priority, AssetReceivedCallback callback)
			throws Exception
	{
		RequestInventoryAsset(item.AssetID, item.itemID, UUID.Zero, item.getOwnerID(), item.assetType, priority, callback);
	}

	public final void RequestEstateAsset() throws Exception
	{
		throw new Exception("This function is not implemented yet!");
	}

	/**
	 * Request an asset be uploaded to the simulator
	 * 
	 * @param asset
	 *            The {@link Asset} Object containing the asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The {@link UUID} of the transfer, can be used to correlate the
	 *         upload with events being fired
	 * @throws Exception
	 */
	public final UUID RequestUpload(AssetItem asset, boolean storeLocal) throws Exception
	{
		if (asset.AssetData == null)
		{
			throw new IllegalArgumentException("Can't upload an asset with no data (did you forget to call Encode?)");
		}

		return RequestUpload(null, asset.getAssetType(), asset.AssetData, storeLocal);
	}

	/**
	 * Request an asset be uploaded to the simulator
	 * 
	 * @param type
	 *            The {@link AssetType} of the asset being uploaded
	 * @param data
	 *            A byte array containing the encoded asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The {@link UUID} of the transfer, can be used to correlate the
	 *         upload with events being fired
	 * @throws Exception
	 */
	public final UUID RequestUpload(AssetType type, byte[] data, boolean storeLocal) throws Exception
	{
		return RequestUpload(null, type, data, storeLocal);
	}

	/**
	 * Request an asset be uploaded to the simulator
	 * 
	 * @param assetID
	 * @param type
	 *            Asset type to upload this data as
	 * @param data
	 *            A byte array containing the encoded asset data
	 * @param storeLocal
	 *            If True, the asset once uploaded will be stored on the
	 *            simulator in which the client was connected in addition to
	 *            being stored on the asset server
	 * @return The {@link UUID} of the transfer, can be used to correlate the
	 *         upload with events being fired
	 * @throws Exception
	 */
	public final UUID RequestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal)
			throws Exception
	{
		return RequestUpload(assetID, type, data, storeLocal, new UUID());
	}

	/**
	 * Initiate an asset upload
	 * 
	 * @param assetID
	 *            The ID this asset will have if the upload succeeds
	 * @param type
	 *            Asset type to upload this data as
	 * @param data
	 *            Raw asset data to upload
	 * @param storeLocal
	 *            Whether to store this asset on the local simulator or the
	 *            grid-wide asset server
	 * @param transactionID
	 *            The tranaction id for the upload <see
	 *            cref="RequestCreateItem"/>
	 * @return The transaction ID of this transfer
	 * @throws Exception
	 */
	public final UUID RequestUpload(RefObject<UUID> assetID, AssetType type, byte[] data, boolean storeLocal,
			UUID transactionID) throws Exception
	{
		AssetUpload upload = new AssetUpload();
		upload.AssetID = UUID.Combine(transactionID, _Client.Self.getSecureSessionID());
		if (assetID != null)
			assetID.argvalue = upload.AssetID;
		upload.AssetData = data;
		upload.AssetType = type;
		upload.Size = data.length;
		upload.XferID = 0;
		upload.ID = transactionID;

		// Build and send the upload packet
		AssetUploadRequestPacket request = new AssetUploadRequestPacket();
		request.AssetBlock.StoreLocal = storeLocal;
		request.AssetBlock.Tempfile = false; // This field is deprecated
		request.AssetBlock.TransactionID = transactionID;
		request.AssetBlock.Type = type.getValue();

		boolean isMultiPacketUpload;
		if (data.length + 100 < Settings.MAX_PACKET_SIZE)
		{
			isMultiPacketUpload = false;
			Logger.Log(String.format("Beginning asset upload [Single Packet], ID: %s, AssetID: %s, Size: %d",
					upload.ID, upload.AssetID, upload.Size), LogLevel.Info, _Client);

			_Transfers.put(upload.ID, upload);

			// The whole asset will fit in this packet, makes things easy
			request.AssetBlock.setAssetData(data);
			upload.Transferred = data.length;
		}
		else
		{
			isMultiPacketUpload = true;
			Logger.Log(String.format("Beginning asset upload [Multiple Packets], ID: %s, AssetID: %s, Size: %d",
					upload.ID, upload.AssetID, upload.Size), LogLevel.Info, _Client);

			// Asset is too big, send in multiple packets
			request.AssetBlock.setAssetData(Helpers.EmptyBytes);
		}

		// Wait for the previous upload to receive a RequestXferPacket
		synchronized (PendingUpload)
		{
			final int UPLOAD_CONFIRM_TIMEOUT = 20 * 1000;
			final int SLEEP_INTERVAL = 50;
			int t = 0;
			while (WaitingForUploadConfirm && t < UPLOAD_CONFIRM_TIMEOUT)
			{
				Thread.sleep(SLEEP_INTERVAL);
				t += SLEEP_INTERVAL;
			}

			if (t < UPLOAD_CONFIRM_TIMEOUT)
			{
				if (isMultiPacketUpload)
				{
					WaitingForUploadConfirm = true;
				}
				PendingUpload.set(upload);
				_Client.Network.SendPacket(request);

				return upload.ID;
			}
			throw new Exception("Timeout waiting for previous asset upload to begin");
		}
	}

	public final void RequestUploadBakedTexture(final byte[] textureData, final BakedTextureUploadedCallback callback)
			throws IOException
	{
		URI url = _Client.Network.getCapabilityURI("UploadBakedTexture");
		if (url != null)
		{
			// Fetch the uploader capability
			CapsClient request = new CapsClient();

			class RequestUploadBakedTextureComplete implements FutureCallback<OSD>
			{
				@Override
				public void completed(OSD result)
				{
					if (result instanceof OSDMap)
					{
						UploadBakedTextureMessage message = _Client.Messages.new UploadBakedTextureMessage();
						message.Deserialize((OSDMap) result);
						if (message.Request.State.equals("complete"))
						{
							callback.callback(((UploaderRequestComplete) message.Request).AssetID);
							return;
						}
						else if (message.Request.State.equals("upload"))
						{
							URI uploadUrl = ((UploaderRequestUpload) message.Request).Url;
							if (uploadUrl != null)
							{
								try
								{
									// POST the asset data
									CapsClient upload = new CapsClient();
									upload.setResultCallback(new RequestUploadBakedTextureComplete());
									upload.executeHttpPost(uploadUrl, textureData, "application/octet-stream", _Client.Settings.CAPS_TIMEOUT);
								}
								catch (IOException ex)
								{
									Logger.Log("Bake upload failed", LogLevel.Warning, _Client);
									callback.callback(UUID.Zero);
								}
							}
							return;
						}
					}
					Logger.Log("Bake upload failed", LogLevel.Warning, _Client);
					callback.callback(UUID.Zero);
				}

				@Override
				public void cancelled()
				{
					Logger.Log("Bake upload canelled", LogLevel.Warning, _Client);
					callback.callback(UUID.Zero);
				}

				@Override
				public void failed(Exception ex)
				{
					Logger.Log("Bake upload failed", LogLevel.Warning, _Client, ex);
					callback.callback(UUID.Zero);
				}
			}
			request.setResultCallback(new RequestUploadBakedTextureComplete());
			request.executeHttpPost(url, new OSDMap(), OSDFormat.Xml, _Client.Settings.CAPS_TIMEOUT);
		}
		else
		{
			Logger.Log("UploadBakedTexture not available, falling back to UDP method", LogLevel.Info, _Client);

			_ThreadResult = _ThreadPool.submit(new Runnable()
			{
				@Override
				public void run()
				{
					final UUID transactionID = new UUID();
					final TimeoutEvent<Boolean> uploadEvent = new TimeoutEvent<Boolean>();

					Callback<AssetUploadCallbackArgs> udpCallback = new Callback<AssetUploadCallbackArgs>()
					{
						@Override
						public boolean callback(AssetUploadCallbackArgs e)
						{
							if (transactionID.equals(e.getUpload().ID))
							{
								uploadEvent.set(true);
								callback.callback(e.getUpload().Success ? e.getUpload().AssetID : UUID.Zero);
							}
							return false;
						}
					};

					OnAssetUploaded.add(udpCallback);
					Boolean success;
					try
					{
						RequestUpload(null, AssetType.Texture, textureData, true, transactionID);
						success = uploadEvent.waitOne(_Client.Settings.TRANSFER_TIMEOUT);
					}
					catch (Exception t)
					{
						success = false;
					}
					OnAssetUploaded.remove(udpCallback);
					if (success == null || !success)
					{
						callback.callback(UUID.Zero);
					}
				}
			});
		}
	}

	/**
	 * Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 * 
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param priority
	 *            A float indicating the requested priority for the transfer.
	 *            Higher priority values tell the simulator to prioritize the
	 *            request before lower valued requests. An image already being
	 *            transferred using the <see cref="TexturePipeline"/> can have
	 *            its priority changed by resending the request with the new
	 *            priority value
	 * @param discardLevel
	 *            Number of quality layers to discard. This controls the end
	 *            marker of the data sent. Sending with value -1 combined with
	 *            priority of 0 cancels an in-progress transfer. A bug exists in
	 *            the Linden Simulator where a -1 will occasionally be sent with
	 *            a non-zero priority indicating an off-by-one error.
	 * @param packetStart
	 *            The packet number to begin the request at. A value of 0 begins
	 *            the request from the start of the asset texture
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 * @param progress
	 *            If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain
	 *            all previously received chunks of the texture asset starting
	 *            from the beginning of the request <example> Request an image
	 *            and fire a callback when the request is complete <code>
	 * _Client.Assets.RequestImage(UUID.Parse("c307629f-e3a1-4487-5e88-0d96ac9d4965"), ImageType.Normal, TextureDownloader_OnDownloadFinished);
	 * 
	 * private void TextureDownloader_OnDownloadFinished(TextureRequestState state, AssetTexture asset)
	 * {
	 *     if(state == TextureRequestState.Finished)
	 *     {
	 *         Console.WriteLine("Texture %s (%d bytes) has been successfully downloaded", asset.AssetID, asset.AssetData.Length);
	 *     }
	 * }
	 * </code> Request an image and use an inline anonymous method to handle the
	 *            downloaded texture data <code>
	 * _Client.Assets.RequestImage(UUID.Parse("c307629f-e3a1-4487-5e88-0d96ac9d4965"), ImageType.Normal, delegate(TextureRequestState state, AssetTexture asset)
	 *                                         {
	 *                                             if(state == TextureRequestState.Finished)
	 *                                             {
	 *                                                 Console.WriteLine("Texture %s (%d bytes) has been successfully downloaded",
	 *                                                 asset.AssetID,
	 *                                                 asset.AssetData.Length);
	 *                                             }
	 *                                         }
	 * );
	 * </code> Request a texture, decode the texture to a bitmap image and apply
	 *            it to a imagebox <code>
	 * _Client.Assets.RequestImage(UUID.Parse("c307629f-e3a1-4487-5e88-0d96ac9d4965"), ImageType.Normal, TextureDownloader_OnDownloadFinished);
	 * 
	 * private void TextureDownloader_OnDownloadFinished(TextureRequestState state, AssetTexture asset)
	 * {
	 *     if(state == TextureRequestState.Finished)
	 *     {
	 *         ManagedImage imgData;
	 *         Image bitmap;
	 * 
	 *         if (state == TextureRequestState.Finished)
	 *         {
	 *             OpenJPEG.DecodeToImage(assetTexture.AssetData, out imgData, out bitmap);
	 *             picInsignia.Image = bitmap;
	 *         }
	 *     }
	 * }
	 * </code> </example>
	 */
	public final void RequestImage(UUID textureID, ImageType imageType, float priority, int discardLevel,
			int packetStart, TextureDownloadCallback callback, boolean progress)
	{
        if (_Client.Settings.USE_HTTP_TEXTURES && _Client.Network.getCapabilityURI("GetTexture") != null)
        {
            HttpRequestTexture(textureID, imageType, priority, discardLevel, packetStart, callback, progress);
        }
        else
        {
            _Texture.RequestTexture(textureID, imageType, priority, discardLevel, packetStart, callback, progress);
        }
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 * 
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 */
	public final void RequestImage(UUID textureID, TextureDownloadCallback callback)
	{
		RequestImage(textureID, ImageType.Normal, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 * 
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 */
	public final void RequestImage(UUID textureID, ImageType imageType, TextureDownloadCallback callback)
	{
		RequestImage(textureID, imageType, 101300.0f, 0, 0, callback, false);
	}

	/**
	 * Overload: Request a texture asset from the simulator using the <see
	 * cref="TexturePipeline"/> system to manage the requests and re-assemble
	 * the image from the packets received from the simulator
	 * 
	 * @param textureID
	 *            The <see cref="UUID"/> of the texture asset to download
	 * @param imageType
	 *            The <see cref="ImageType"/> of the texture asset. Use <see
	 *            cref="ImageType.Normal"/> for most textures, or <see
	 *            cref="ImageType.Baked"/> for baked layer texture assets
	 * @param callback
	 *            The <see cref="TextureDownloadCallback"/> callback to fire
	 *            when the image is retrieved. The callback will contain the
	 *            result of the request and the texture asset data
	 * @param progress
	 *            If true, the callback will be fired for each chunk of the
	 *            downloaded image. The callback asset parameter will contain
	 *            all previously received chunks of the texture asset starting
	 *            from the beginning of the request
	 */
	public final void RequestImage(UUID textureID, ImageType imageType, TextureDownloadCallback callback,
			boolean progress)
	{
		RequestImage(textureID, imageType, 101300.0f, 0, 0, callback, progress);
	}

	/**
	 * Cancel a texture request
	 * 
	 * @param textureID
	 *            The texture assets <see cref="UUID"/>
	 * @throws Exception
	 */
	public final void RequestImageCancel(UUID textureID) throws Exception
	{
		_Texture.AbortTextureRequest(textureID);
	}

    /**
     * Requests download of a mesh asset
     * 
     * @param meshID UUID of the mesh asset
     * @param callback Callback when the request completes
     */
    public void RequestMesh(final UUID meshID, final MeshDownloadCallback callback)
    {
        if (meshID == null || meshID.equals(UUID.Zero) || callback == null)
            return;

        // Do we have this mesh asset in the cache?
        if (_Client.Assets.getCache().containsKey(meshID))
        {
            callback.callback(true, new AssetMesh(meshID, _Client.Assets.getCache().GetCachedAssetBytes(meshID)));
            return;
        }

        if (_Client.Network.getCapabilityURI("GetMesh") != null)
        {
        	try
        	{
        		URI url = new URI(String.format("{%s}/?mesh_id={%s}", _Client.Network.getCapabilityURI("GetMesh"), meshID));
        		DownloadRequest req = _HttpDownloads.new DownloadRequest(url, _Client.Settings.CAPS_TIMEOUT, null, null, new FutureCallback<byte[]>()
                {		
                    @Override
					public void completed(byte[] response)
                    {
                        if (response != null) // success
                        {
                        	callback.callback(true, new AssetMesh(meshID, response));
                            _Client.Assets.getCache().SaveAssetToCache(meshID, response);
                        }
                    }
                    
                    @Override
					public void failed(Exception ex)
                    {
                        Logger.Log(String.format("Failed to fetch mesh asset {%s}: {%s}", meshID, (ex == null) ? "" : ex.getMessage()),
                        		   LogLevel.Warning, _Client);
                        callback.callback(false, null);
                    }
                    
                    @Override
					public void cancelled()
                    {
                        callback.callback(false, null);
                    }
                });
        		_HttpDownloads.enque(req);
        	}
    		catch (URISyntaxException ex)
    		{
                Logger.Log(String.format("Failed to fetch mesh asset {%s}: {%s}", meshID, ex.getMessage()), LogLevel.Warning, _Client);
                callback.callback(false, null);
    		}
        }
        else
        {
            Logger.Log("GetMesh capability not available", LogLevel.Error, _Client);
            callback.callback(false, null);
        }
    }

    /**
	 * Lets TexturePipeline class fire the progress event
	 * 
	 * @param texureID
	 *            The texture ID currently being downloaded
	 * @param transferredBytes
	 *            the number of bytes transferred
	 * @param totalBytes
	 *            the total number of bytes expected
	 */
	public final void FireImageProgressEvent(UUID texureID, long transferredBytes, long totalBytes)
	{
		try
		{
			OnImageReceiveProgress.dispatch(new ImageReceiveProgressCallbackArgs(texureID, transferredBytes, totalBytes));
		}
		catch (Throwable ex)
		{
			Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
		}
	}
	
    /**
     * Helper method for downloading textures via GetTexture cap
     * Same signature as the UDP variant since we need all the params to pass to the UDP TexturePipeline
     * in case we need to fall back to it Linden servers currently (1.42) don't support bakes downloads via HTTP)
     * 
     * @param textureID
     * @param imageType
     * @param priority
     * @param discardLevel
     * @param packetStart
     * @param callback
     * @param progress
     */
    private void HttpRequestTexture(final UUID textureID, final ImageType imageType, final float priority,
    		final int discardLevel, final int packetStart, final TextureDownloadCallback callback, final boolean progress)
    {
        if (textureID == UUID.Zero || callback == null)
            return;

        byte[] assetData;
        // Do we have this image in the cache?
        if (_Client.Assets.getCache().containsKey(textureID)
            && (assetData = _Client.Assets.getCache().GetCachedAssetBytes(textureID)) != null)
        {
            ImageDownload image = new ImageDownload();
            image.ID = textureID;
            image.AssetData = assetData;
            image.Size = assetData.length;
            image.Transferred = assetData.length;
            image.ImageType = imageType;
            image.AssetType = AssetType.Texture;
            image.Success = true;

            callback.callback(TextureRequestState.Finished, new AssetTexture(image.ID, image.AssetData));
            FireImageProgressEvent(image.ID, image.Transferred, image.Size);
            return;
        }

        AsyncHTTPClient.ProgressCallback progressHandler = null;

        if (progress)
        {
            progressHandler = new AsyncHTTPClient.ProgressCallback()
            {
            	@Override
				public void progress(long bytesReceived, long totalBytesToReceive)
                {
                    FireImageProgressEvent(textureID, bytesReceived, totalBytesToReceive);
                }
            };
        }

		try
		{
			URI url = new URI(String.format("{%s}/?texture_id={%s}", _Client.Network.getCapabilityURI("GetTexture"), textureID));
	        DownloadRequest req = _HttpDownloads.new DownloadRequest(url, _Client.Settings.CAPS_TIMEOUT, "image/x-j2c", progressHandler, new FutureCallback<byte[]>()
	        {
            	@Override
    			public void completed(byte[] response)
            	{
                	if (response != null) // success
                	{
                		ImageDownload image = new ImageDownload();
                		image.ID = textureID;
                		image.AssetData = response;
                		image.Size = response.length;
                		image.Transferred = response.length;
                		image.ImageType = imageType;
                		image.AssetType = AssetType.Texture;
                		image.Success = true;

                		callback.callback(TextureRequestState.Finished, new AssetTexture(image.ID, image.AssetData));
                		FireImageProgressEvent(image.ID, image.Transferred, image.Size);

                		_Client.Assets.getCache().SaveAssetToCache(textureID, response);
                	}
            	}
            	
            	@Override
    			public void failed(Exception ex)
            	{
                    callback.callback(TextureRequestState.Pending, null);
                    Logger.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP: {%s}",
                            textureID, (ex == null) ? "" : ex.getMessage()), LogLevel.Warning, _Client);

                    _Texture.RequestTexture(textureID, imageType, priority, discardLevel, packetStart, callback, progress);
                }
            	
            	@Override
    			public void cancelled()
            	{
                    callback.callback(TextureRequestState.Aborted, null);
            	}
            });
	        _HttpDownloads.enque(req);
		}
		catch (URISyntaxException ex)
		{
            callback.callback(TextureRequestState.Pending, null);
            Logger.Log(String.format("Failed to fetch texture {%s} over HTTP, falling back to UDP: {%s}", textureID, ex.getMessage()), LogLevel.Warning, _Client);
            _Texture.RequestTexture(textureID, imageType, priority, discardLevel, packetStart, callback, progress);
		}
    }
	// #region Helpers

	private AssetItem CreateAssetWrapper(AssetType type)
	{
		switch (type)
		{
			case Notecard:
				return new AssetNotecard();
			case LSLText:
				return new AssetScriptText();
			case LSLBytecode:
				return new AssetScriptBinary();
			case Texture:
				return new AssetTexture();
			case Object:
				return new AssetPrim();
			case Clothing:
				return new AssetClothing();
			case Bodypart:
				return new AssetBodypart();
			case Animation:
				return new AssetAnimation();
			case Sound:
				return new AssetSound();
			case Landmark:
				return new AssetLandmark();
			case Gesture:
				return new AssetGesture();
			default:
				Logger.Log("Unimplemented asset type: " + type, LogLevel.Error, _Client);
		}
		return null;
	}

	private AssetItem WrapAsset(AssetDownload download)
	{
		AssetItem asset = CreateAssetWrapper(download.AssetType);
		if (asset != null)
		{
			asset.setAssetID(download.AssetID);
			asset.AssetData = download.AssetData;
		}
		return asset;
	}

	private void SendNextUploadPacket(AssetUpload upload) throws Exception
	{
		SendXferPacketPacket send = new SendXferPacketPacket();

		send.XferID.ID = upload.XferID;
		send.XferID.Packet = upload.PacketNum++;

		if (send.XferID.Packet == 0)
		{
			// The first packet reserves the first four bytes of the data for
			// the
			// total length of the asset and appends 1000 bytes of data after
			// that
			byte[] data = new byte[1004];
			Helpers.Int32ToBytesL(upload.Size, data, 0);
			System.arraycopy(upload.AssetData, 0, data, 4, 1000);
			send.DataPacket.setData(data);
			upload.Transferred += 1000;

			synchronized (_Transfers)
			{
				_Transfers.remove(upload.AssetID);
				_Transfers.put(upload.ID, upload);
			}
		}
		else if ((send.XferID.Packet + 1) * 1000 < upload.Size)
		{
			// This packet is somewhere in the middle of the transfer, or a
			// perfectly
			// aligned packet at the end of the transfer
			byte[] data = new byte[1000];
			System.arraycopy(upload.AssetData, upload.Transferred, data, 0, 1000);
			send.DataPacket.setData(data);
			upload.Transferred += 1000;
		}
		else
		{
			// Special handler for the last packet which will be less than 1000
			// bytes
			int lastlen = upload.Size - send.XferID.Packet * 1000;
			byte[] data = new byte[lastlen];
			System.arraycopy(upload.AssetData, send.XferID.Packet * 1000, data, 0, lastlen);
			send.XferID.Packet |= 0x80000000; // This signals the final packet
			send.DataPacket.setData(data);
			upload.Transferred += lastlen;
		}
		_Client.Network.SendPacket(send);
	}

	private void SendConfirmXferPacket(long xferID, int packetNum) throws Exception
	{
		ConfirmXferPacketPacket confirm = new ConfirmXferPacketPacket();
		confirm.XferID.ID = xferID;
		confirm.XferID.Packet = packetNum;

		_Client.Network.SendPacket(confirm);
	}

	// #endregion Helpers

	// #region Transfer Callbacks

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleTransferInfo(Packet packet, Simulator simulator)
	{
		TransferInfoPacket info = (TransferInfoPacket) packet;

		if (_Transfers.containsKey(info.TransferInfo.TransferID))
		{
			AssetDownload download = (AssetDownload) _Transfers.get(info.TransferInfo.TransferID);
			if (download == null || download.Callback == null)
			{
				return;
			}

			download.Channel = ChannelType.setValue(info.TransferInfo.ChannelType);
			download.Status = StatusCode.setValue(info.TransferInfo.Status);
			download.Target = TargetType.setValue(info.TransferInfo.TargetType);
			download.Size = info.TransferInfo.Size;

			// TODO: Once we support mid-transfer status checking and aborting
			// this will need to become smarter
			if (download.Status != StatusCode.OK)
			{
				Logger.Log("Transfer failed with status code " + download.Status, LogLevel.Warning, _Client);

				synchronized (_Transfers)
				{
					_Transfers.remove(download.ID);
				}

				// No data could have been received before the TransferInfo
				// packet
				download.AssetData = null;

				// Fire the event with our transfer that contains Success =
				// false;
				try
				{
					download.Callback.callback(download, null);
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}
			else
			{
				download.AssetData = new byte[download.Size];
				byte[] data = info.TransferInfo.getParams();

				if (download.Source == SourceType.Asset && data.length == 20)
				{
					download.AssetID = new UUID(data);
					download.AssetType = AssetType.setValue(Helpers.BytesToInt32L(data, 16));

					Logger.DebugLog(String.format("TransferInfo packet received. AssetID: %s Type: %s",
							download.AssetID, download.AssetType));
				}
				else if (download.Source == SourceType.SimInventoryItem && data.length == 100)
				{
					// TODO: Can we use these?
					UUID agentID = new UUID(data, 0);
					UUID sessionID = new UUID(data, 16);
					UUID ownerID = new UUID(data, 32);
					UUID taskID = new UUID(data, 48);
					UUID itemID = new UUID(data, 64);
					download.AssetID = new UUID(data, 80);
					download.AssetType = AssetType.setValue(Helpers.BytesToInt32L(data, 96));

					Logger.DebugLog(String
							.format("TransferInfo packet received. AgentID: %s SessionID: %s OwnerID: %s TaskID: %s ItemID: %s AssetID: %s Type: %s",
									agentID, sessionID, ownerID, taskID, itemID, download.AssetID, download.AssetType));
				}
				else
				{
					Logger.Log(String.format(
							"Received a TransferInfo packet with a SourceType of %s and a Params field length of %d",
							download.Source, data.length), LogLevel.Warning, _Client);
				}
			}
			download.HeaderReceivedEvent.set(true);
		}
		else
		{
			Logger.Log("Received a TransferInfo packet for an asset we didn't request, TransferID: "
					+ info.TransferInfo.TransferID, LogLevel.Warning, _Client);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @throws Exception
	 */
	private final void HandleTransferPacket(Packet packet, Simulator simulator) throws Exception
	{
		TransferPacketPacket asset = (TransferPacketPacket) packet;
		if (_Transfers.containsKey(asset.TransferData.TransferID))
		{
			AssetDownload download = (AssetDownload) _Transfers.get(asset.TransferData.TransferID);
			if (download.Size == 0)
			{
				Logger.DebugLog("TransferPacket received ahead of the transfer header, blocking...", _Client);

				// We haven't received the header yet, block until it's received
				// or times out
				download.HeaderReceivedEvent.waitOne(TRANSFER_HEADER_TIMEOUT);

				if (download.Size == 0)
				{
					Logger.Log(
							"Timed out while waiting for the asset header to download for " + download.ID.toString(),
							LogLevel.Warning, _Client);

					// Abort the transfer
					TransferAbortPacket abort = new TransferAbortPacket();
					abort.TransferInfo.ChannelType = download.Channel.getValue();
					abort.TransferInfo.TransferID = download.ID;
					download.Simulator.SendPacket(abort);

					download.Success = false;
					synchronized (_Transfers)
					{
						_Transfers.remove(download.ID);
					}

					// Fire the event with our transfer that contains Success =
					// false
					if (download.Callback != null)
					{
						try
						{
							download.Callback.callback(download, null);
						}
						catch (Throwable ex)
						{
							Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
						}
					}

					return;
				}
			}

			try
			{
				System.arraycopy(asset.TransferData.getData(), 0, download.AssetData, download.Transferred, asset.TransferData.getData().length);
				download.Transferred += asset.TransferData.getData().length;
			}
			catch (Exception t)
			{
				Logger.Log(
						String.format(
								"TransferPacket handling failed. TransferData.Data.Length = %d, AssetData.Length = %d, TransferData.Packet = %d",
								asset.TransferData.getData().length, download.AssetData.length,
								asset.TransferData.Packet), LogLevel.Error);
				return;
			}

			// Logger.DebugLog(String.Format("Transfer packet %d, received %d/%d/%d bytes for asset %s",
			// asset.TransferData.Packet, asset.TransferData.Data.Length,
			// transfer.Transferred, transfer.Size, transfer.AssetID));

			// Check if we downloaded the full asset
			if (download.Transferred >= download.Size)
			{
				Logger.DebugLog("Transfer for asset " + download.AssetID.toString() + " completed", _Client);

				download.Success = true;
				synchronized (_Transfers)
				{
					_Transfers.remove(download.ID);
				}

				// Cache successful asset download
				_Cache.SaveAssetToCache(download.AssetID, download.AssetData);

				if (download.Callback != null)
				{
					try
					{
						download.Callback.callback(download, WrapAsset(download));
					}
					catch (Throwable ex)
					{
						Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
					}
				}
			}
		}
	}

	// /#endregion Transfer Callbacks

	// /#region Xfer Callbacks

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleInitiateDownloadPacket(Packet packet, Simulator simulator)
	{
		InitiateDownloadPacket request = (InitiateDownloadPacket) packet;
		try
		{
			OnInitiateDownload.dispatch(new InitiateDownloadCallbackArgs(Helpers.BytesToString(request.FileData
					.getSimFilename()), Helpers.BytesToString(request.FileData.getViewerFilename())));
		}
		catch (Throwable ex)
		{
			Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @throws Exception
	 */
	private final void HandleRequestXfer(Packet packet, Simulator simulator) throws Exception
	{
		RequestXferPacket request = (RequestXferPacket) packet;
		if (PendingUpload == null)
		{
			Logger.Log("Received a RequestXferPacket for an unknown asset upload", LogLevel.Warning, _Client);
			return;
		}

		AssetUpload upload = PendingUpload.getAndSet(null);
		WaitingForUploadConfirm = false;

		upload.XferID = request.XferID.ID;
		upload.Type = AssetType.setValue(request.XferID.VFileType);

		UUID transferID = new UUID(upload.XferID);
		_Transfers.put(transferID, upload);

		// Send the first packet containing actual asset data
		SendNextUploadPacket(upload);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @throws Exception
	 */
	private final void HandleConfirmXferPacket(Packet packet, Simulator simulator) throws Exception
	{
		ConfirmXferPacketPacket confirm = (ConfirmXferPacketPacket) packet;

		// Building a new UUID every time an ACK is received for an upload is a
		// horrible thing, but this whole Xfer system is horrible
		UUID transferID = new UUID(confirm.XferID.ID);
		if (_Transfers.containsKey(transferID))
		{
			AssetUpload upload = (AssetUpload) _Transfers.get(transferID);

			Logger.DebugLog(String.format("ACK for upload %s of asset type %s (%d/%d)", upload.AssetID, upload.Type,
					upload.Transferred, upload.Size));

			try
			{
				OnUploadProgress.dispatch(new AssetUploadCallbackArgs(upload));
			}
			catch (Throwable ex)
			{
				Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
			}

			if (upload.Transferred < upload.Size)
			{
				SendNextUploadPacket(upload);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleAssetUploadComplete(Packet packet, Simulator simulator)
	{
		AssetUploadCompletePacket complete = (AssetUploadCompletePacket) packet;

		// If we uploaded an asset in a single packet, RequestXferHandler()
		// will never be called so we need to set this here as well
		WaitingForUploadConfirm = false;

		if (OnAssetUploaded.count() > 0)
		{
			Entry<UUID, Transfer> foundTransfer = null;

			// Xfer system sucks really really bad. Where is the damn XferID?
			synchronized (_Transfers)
			{
				for (Entry<UUID, Transfer> transfer : _Transfers.entrySet())
				{
					if (transfer.getValue() instanceof AssetUpload)
					{
						AssetUpload upload = (AssetUpload) transfer;

						if (upload.AssetID == complete.AssetBlock.UUID)
						{
							foundTransfer = transfer;
							upload.Success = complete.AssetBlock.Success;
							upload.Type = AssetType.setValue(complete.AssetBlock.Type);
							break;
						}
					}
				}
			}

			if (foundTransfer != null)
			{
				synchronized (_Transfers)
				{
					_Transfers.remove(foundTransfer.getKey());
				}

				try
				{
					OnAssetUploaded.dispatch(new AssetUploadCallbackArgs((AssetUpload) foundTransfer.getValue()));
				}
				catch (Throwable ex)
				{
					Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
				}
			}
			else
			{
				Logger.Log(String.format(
						"Got an AssetUploadComplete on an unrecognized asset, AssetID: %s, Type: %s, Success: %s",
						complete.AssetBlock.UUID, AssetType.setValue(complete.AssetBlock.Type),
						complete.AssetBlock.Success), LogLevel.Warning);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @throws Exception
	 */
	private final void HandleSendXferPacket(Packet packet, Simulator simulator) throws Exception
	{
		SendXferPacketPacket xfer = (SendXferPacketPacket) packet;

		// Lame ulong to UUID conversion, please go away Xfer system
		UUID transferID = new UUID(xfer.XferID.ID);
		if (_Transfers.containsKey(transferID))
		{
			XferDownload download = (XferDownload) _Transfers.get(transferID);

			// Apply a mask to get rid of the "end of transfer" bit
			int packetNum = xfer.XferID.Packet & 0x0FFFFFFF;

			// Check for out of order packets, possibly indicating a resend
			if (packetNum != download.PacketNum)
			{
				if (packetNum == download.PacketNum - 1)
				{
					Logger.DebugLog("Resending Xfer download confirmation for packet " + packetNum, _Client);
					SendConfirmXferPacket(download.XferID, packetNum);
				}
				else
				{
					Logger.Log("Out of order Xfer packet in a download, got " + packetNum + " expecting "
							+ download.PacketNum, LogLevel.Warning, _Client);
					// Re-confirm the last packet we actually received
					SendConfirmXferPacket(download.XferID, download.PacketNum - 1);
				}

				return;
			}

			byte[] bytes = xfer.DataPacket.getData();
			if (packetNum == 0)
			{
				// This is the first packet received in the download, the first
				// four bytes are a size integer
				// in little endian ordering
				download.Size = (bytes[0] + (bytes[1] << 8) + (bytes[2] << 16) + (bytes[3] << 24));
				download.AssetData = new byte[download.Size];

				Logger.DebugLog("Received first packet in an Xfer download of size " + download.Size);

				System.arraycopy(bytes, 4, download.AssetData, 0, bytes.length - 4);
				download.Transferred += bytes.length - 4;
			}
			else
			{
				System.arraycopy(bytes, 0, download.AssetData, 1000 * packetNum, bytes.length);
				download.Transferred += bytes.length;
			}

			// Increment the packet number to the packet we are expecting next
			download.PacketNum++;

			// Confirm receiving this packet
			SendConfirmXferPacket(download.XferID, packetNum);

			if ((xfer.XferID.Packet & 0x80000000) != 0)
			{
				// This is the last packet in the transfer
				if (!Helpers.isEmpty(download.Filename))
				{
					Logger.DebugLog("Xfer download for asset " + download.Filename + " completed", _Client);
				}
				else
				{
					Logger.DebugLog("Xfer download for asset " + download.VFileID.toString() + " completed", _Client);
				}

				download.Success = true;
				synchronized (_Transfers)
				{
					_Transfers.remove(download.ID);
				}

				try
				{
					OnXferReceived.dispatch(new XferReceivedCallbackArgs(download));
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
	private final void HandleAbortXfer(Packet packet, Simulator simulator)
	{
		AbortXferPacket abort = (AbortXferPacket) packet;
		XferDownload download = null;

		// Lame ulong to UUID conversion, please go away Xfer system
		UUID transferID = new UUID(abort.XferID.ID);

		synchronized (_Transfers)
		{
			if (_Transfers.containsKey(transferID))
			{
				download = (XferDownload) _Transfers.get(transferID);
				_Transfers.remove(transferID);
			}
		}

		if (download != null && OnXferReceived.count() > 0)
		{
			download.Success = false;
			download.Error = TransferError.setValue(abort.XferID.Result);

			try
			{
				OnXferReceived.dispatch(new XferReceivedCallbackArgs(download));
			}
			catch (Throwable ex)
			{
				Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
			}
		}
	}
	// #endregion Xfer Callbacks
}
