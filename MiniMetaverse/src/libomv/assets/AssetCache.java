/**
 * Copyright (c) 2008, openmetaverse.org
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import libomv.GridClient;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.Settings;
import libomv.assets.AssetItem.AssetType;
import libomv.assets.AssetManager.ImageDownload;
import libomv.types.UUID;
import libomv.utils.Callback;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// Class that handles the local asset cache
public class AssetCache
{
	// User can plug in a routine to compute the asset cache location
	public interface ComputeAssetCacheFilenameDelegate
	{
		public String callback(String cacheDir, UUID assetID);
	}

	public ComputeAssetCacheFilenameDelegate ComputeAssetCacheFilename = null;

	private GridClient Client;
	private AssetManager Manager;
	private Thread cleanerThread;
	private Timer cleanerTimer;
	private long pruneInterval = 1000 * 60 * 5;
	private boolean autoPruneEnabled = true;

	// Allows setting weather to periodically prune the cache if it grows too
	// big
	// Default is enabled, when caching is enabled
	public final void setAutoPruneEnabled(boolean value)
	{
		autoPruneEnabled = value;

		if (autoPruneEnabled)
		{
			SetupTimer();
		}
		else
		{
			DestroyTimer();
		}
	}

	public final boolean getAutoPruneEnabled()
	{
		return autoPruneEnabled;
	}

	// How long (in ms) between cache checks (default is 5 min.)
	public final void setAutoPruneInterval(long value)
	{
		pruneInterval = value;
		SetupTimer();
	}

	public final long getAutoPruneInterval()
	{
		return pruneInterval;
	}

	/**
	 * Default constructor
	 * 
	 * 
	 * @param client
	 *            A reference to the GridClient object
	 */
	public AssetCache(GridClient client, AssetManager manager)
	{
		Client = client;
		Manager = manager;
		Client.Login.OnLoginProgress.add(new Network_LoginProgress(), false);
		Client.Network.OnDisconnected.add(new Network_Disconnected(), true);
	}

	private class Network_LoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs e)
		{
			if (e.getStatus() == LoginStatus.Success)
			{
				SetupTimer();
			}
			return false;
		}
	}

	private class Network_Disconnected implements Callback<DisconnectedCallbackArgs>
	{
		@Override
		public boolean callback(DisconnectedCallbackArgs e)
		{
			DestroyTimer();
			return false;
		}
	}

	// Disposes cleanup timer
	private void DestroyTimer()
	{
		if (cleanerTimer != null)
		{
			cleanerTimer.cancel();
			cleanerTimer = null;
		}
	}

	// Only create timer when needed
	private void SetupTimer()
	{
		if (Operational() && autoPruneEnabled && Client.Network.getConnected())
		{
			cleanerTimer = new Timer();
			cleanerTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					BeginPrune();
				}
			}, pruneInterval, pruneInterval);
		}
	}

	/**
	 * Return bytes read from the local asset cache, null if it does not exist
	 * 
	 * @param assetID
	 *            UUID of the asset we want to get
	 * @return Raw bytes of the asset, or null on failure
	 */
	public final byte[] GetCachedAssetBytes(UUID assetID)
	{
		if (!Operational())
		{
			return null;
		}
		try
		{
			String fileName = FileName(assetID);
			File file = new File(fileName);
			if (file.exists())
			{
				Logger.DebugLog("Reading " + fileName + " from asset cache.");
			}
			else
			{
				fileName = StaticFileName(assetID);
				file = new File(fileName);
                Logger.DebugLog("Reading " + fileName + " from static asset cache.");
			}
			byte[] assetData = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			try
			{
				fis.read(assetData);
			}
			finally
			{
				fis.close();
			}
			return assetData;
		}
		catch (Throwable ex)
		{
			Logger.Log("Failed reading asset from cache (" + ex.getMessage() + ")", LogLevel.Warning, Client);
			return null;
		}
	}

	/**
	 * Returns ImageDownload object of the image from the local image cache,
	 * null if it does not exist
	 * 
	 * @param imageID
	 *            UUID of the image we want to get
	 * @return ImageDownload object containing the image, or null on failure
	 */
	public final ImageDownload get(UUID imageID)
	{
		if (!Operational())
		{
			return null;
		}

		byte[] imageData = GetCachedAssetBytes(imageID);
		if (imageData == null)
		{
			return null;
		}
		ImageDownload transfer = Manager.new ImageDownload();
		transfer.AssetType = AssetType.Texture;
		transfer.ID = imageID;
		transfer.AssetData = imageData;
		transfer.Size = imageData.length;
		transfer.Transferred = imageData.length;
		transfer.Success = true;
		transfer.Simulator = Client.Network.getCurrentSim();
		return transfer;
	}

	/**
	 * Constructs a file name of the cached asset
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @return String with the file name of the cached asset
	 */
	private String FileName(UUID assetID)
	{
		if (ComputeAssetCacheFilename != null)
		{
			return ComputeAssetCacheFilename.callback(Client.Settings.ASSET_CACHE_DIR, assetID);
		}
		return Client.Settings.ASSET_CACHE_DIR + File.separatorChar + assetID.toString();
	}

    /**
     * Constructs a file name of the static cached asset
     *
	 * @param assetID
	 *            UUID of the asset
	 * @return String with the file name of the static cached asset
     */
    private String StaticFileName(UUID assetID)
    {
        return Settings.RESOURCE_DIR + File.separatorChar + "static_assets" + File.separatorChar + assetID.toString();
    }

    /**
	 * Saves an asset to the local cache
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @param assetData
	 *            Raw bytes the asset consists of
	 * @return Weather the operation was successfull
	 */
	public final boolean SaveAssetToCache(UUID assetID, byte[] assetData)
	{
		if (!Operational())
		{
			return false;
		}

		try
		{
			Logger.DebugLog("Saving " + FileName(assetID) + " to asset cache.", Client);
			File di = new File(Client.Settings.ASSET_CACHE_DIR);
			if (!di.exists())
			{
				di.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(new File(FileName(assetID)));
			try
			{
				fos.write(assetData);
			}
			finally
			{
				fos.close();
			}
		}
		catch (Throwable ex)
		{
			Logger.Log("Failed saving asset to cache (" + ex.getMessage() + ")", LogLevel.Warning, Client);
			return false;
		}

		return true;
	}

	/**
	 * Get the file name of the asset stored with given UUID
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @return Null if we don't have that UUID cached on disk, file name if
	 *         found in the cache folder
	 */
	public final String AssetFileName(UUID assetID)
	{
		if (!Operational())
		{
			return null;
		}

		String fileName = FileName(assetID);
		File file = new File(fileName);
		if (file.exists())
		{
			return fileName;
		}
		return null;
	}

	/**
	 * Checks if the asset exists in the local cache
	 * Note: libOpenMetaverse: HasAsset()
	 * 
	 * @param assetID
	 *            UUID of the asset
	 * @return True is the asset is stored in the cache, otherwise false
	 */
	public final boolean containsKey(UUID assetID)
	{
		if (!Operational())
		{
			return false;
		}

		String fileName = FileName(assetID);
		File file = new File(fileName);
		if (file.exists())
		{
			return true;
		}
		file = new File(StaticFileName(assetID));
		return file.exists();
	}

	private File[] ListCacheFiles()
	{
		String cacheDir = Client.Settings.ASSET_CACHE_DIR;
		File di = new File(cacheDir);
		if (!di.exists() || !di.isDirectory())
		{
			return null;
		}

		class CacheNameFilter implements FilenameFilter
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
			}
		}

		// We save file with UUID as file name, only count those
		return di.listFiles(new CacheNameFilter());
	}

	/**
	 * Wipes out entire cache
	 * 
	 */
	public final void Clear()
	{
		File[] files = ListCacheFiles();

		int num = 0;
		for (File file : files)
		{
			file.delete();
			++num;
		}
		Logger.Log("Wiped out " + num + " files from the cache directory.", LogLevel.Debug);
	}

	/**
	 * Brings cache size to the 90% of the max size
	 * 
	 */
	public final void Prune()
	{
		File[] files = ListCacheFiles();
		long size = GetFileSize(files);

		if (size > Client.Settings.ASSET_CACHE_MAX_SIZE)
		{
			Arrays.sort(files, new SortFilesByModTimeHelper());
			long targetSize = (long) (Client.Settings.ASSET_CACHE_MAX_SIZE * 0.9);
			int num = 0;
			for (File file : files)
			{
				++num;
				size -= file.length();
				file.delete();
				if (size < targetSize)
				{
					break;
				}
			}
			Logger.Log(num + " files deleted from the cache, cache size now: " + NiceFileSize(size), LogLevel.Debug);
		}
		else
		{
			Logger.Log("Cache size is " + NiceFileSize(size) + ", file deletion not needed", LogLevel.Debug);
		}

	}

	/**
	 * Asynchronously brings cache size to the 90% of the max size
	 * 
	 */
	public final void BeginPrune()
	{
		// Check if the background cache cleaning thread is active first
		if (cleanerThread != null && cleanerThread.isAlive())
		{
			return;
		}

		synchronized (this)
		{
			cleanerThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Prune();
				}
			});
			cleanerThread.setDaemon(true);
			cleanerThread.start();
		}
	}

	/**
	 * Adds up file sizes passes in a FileInfo array
	 */
	private long GetFileSize(File[] files)
	{
		long ret = 0;
		for (File file : files)
		{
			ret += file.length();
		}
		return ret;
	}

	/**
	 * Checks whether caching is enabled
	 * 
	 */
	private boolean Operational()
	{
		return Client.Settings.USE_ASSET_CACHE;
	}

	/**
	 * Nicely formats file sizes
	 * 
	 * @param byteCount
	 *            Byte size we want to output
	 * @return String with humanly readable file size
	 */
	private String NiceFileSize(long byteCount)
	{
		String size = "0 Bytes";
		if (byteCount >= 1073741824)
		{
			size = String.format("%d", (int) (byteCount / 1073741824)) + " GB";
		}
		else if (byteCount >= 1048576)
		{
			size = String.format("%d", (int) (byteCount / 1048576)) + " MB";
		}
		else if (byteCount >= 1024)
		{
			size = String.format("%d", (int) (byteCount / 1024)) + " KB";
		}
		else if (byteCount > 0 && byteCount < 1024)
		{
			size = ((Long) byteCount).toString() + " Bytes";
		}

		return size;
	}

	/**
	 * Helper class for sorting files by their last accessed time
	 * 
	 */
	private class SortFilesByModTimeHelper implements Comparator<File>
	{
		@Override
		public int compare(File f1, File f2)
		{
			if (f1.lastModified() > f2.lastModified())
			{
				return 1;
			}
			if (f1.lastModified() < f2.lastModified())
			{
				return -1;
			}
			return 0;
		}
	}
}
