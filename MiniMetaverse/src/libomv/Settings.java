/**
 * Copyright (c) 2006-2008, openmetaverse.org
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

import java.net.InetSocketAddress;

import libomv.packets.EconomyDataPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.Color4;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.utils.Logger.LogLevel;

/*
 * Class for controlling various system settings.
 *
 * Some values are readonly because they affect things that
 * happen when the GridClient object is initialized, so changing them at
 * runtime won't do any good. Non-readonly values may affect things that
 * happen at login or dynamically
 */
public class Settings implements PacketCallback
{
    /* Main grid login server */
    public static String AGNI_LOGIN_SERVER = "https://login.agni.lindenlab.com/cgi-bin/login.cgi";

    /* Beta grid login server */
    public static String ADITI_LOGIN_SERVER = "https://login.aditi.lindenlab.com/cgi-bin/login.cgi";

    // #region Application Settings

	/* Application name */
	public static final String APPLICATION_NAME = "libomv 0.6.1 123 / Second Life 1.23.5.136262";

	/* Application Version */
	public static final String APPLICATION_VERSION = "0.6.1";

	/* The relative directory where external resources are kept */
	public static String RESOURCE_DIR = "character";

	/* Initialize Avatar Manager */
	public boolean ENABLE_AVATAR_MANAGER = true;

	/* Initialize Inventory Manager */
	public boolean ENABLE_INVENTORY_MANAGER = true;

	/* Initialize Sound Manager */
	public boolean ENABLE_SOUND_MANAGER = true;

	/* Initialize Asset Manager */
	public boolean ENABLE_ASSET_MANAGER = false;

	/* Initialize Directory Manager */
	public boolean ENABLE_DIRECTORY_MANAGER = false;

	/* Initialize Object Manager */
	public boolean ENABLE_OBJECT_MANAGER = false;

	/* Initialize Parcel Manager */
	public boolean ENABLE_PARCEL_MANAGER = false;

	// #region Login/Networking Settings

	/* IP Address the client will bind to */
	public static InetSocketAddress BIND_ADDR = InetSocketAddress.createUnresolved("*", 13000);

	/* Use XML-RPC Login or LLSD Login, default is XML-RPC Login */
	public boolean USE_LLSD_LOGIN = false;

	/* Use Http downloads for texture requests */
	public boolean USE_HTTP_TEXTURES = true;
	/*
	 * InventoryManager requests inventory information on login, GridClient
	 * initializes an Inventory store for main inventory.
	 */
	public static final boolean ENABLE_INVENTORY_STORE = true;

	/*
	 * InventoryManager requests library information on login, GridClient
	 * initializes an Inventory store for the library.
	 */
	public static final boolean ENABLE_LIBRARY_STORE = true;

	// #region Timeouts and Intervals

	/* Number of milliseconds before an asset transfer will time out */
	public int TRANSFER_TIMEOUT = 90 * 1000;

	/* Number of milliseconds before a teleport attempt will time out */
	public int TELEPORT_TIMEOUT = 40 * 1000;

	/* Number of milliseconds before NetworkManager.Logout() will time out */
	public int LOGOUT_TIMEOUT = 5 * 1000;

	/*
	 * Number of milliseconds before a CAPS call will time out Setting this too
	 * low will cause web requests time out and possibly retry repeatedly
	 */
	public int CAPS_TIMEOUT = 60 * 1000;

	/* Number of milliseconds for xml-rpc to timeout */
	public int LOGIN_TIMEOUT = 60 * 1000;

	/* Milliseconds before a packet is assumed lost and resent */
	public int RESEND_TIMEOUT = 4000;

	/*
	 * Milliseconds without receiving a packet before the connection to a
	 * simulator is assumed lost
	 */
	public int SIMULATOR_TIMEOUT = 30 * 1000;

	/*
	 * Milliseconds to wait for a simulator info request through the grid
	 * interface
	 */
	public int MAP_REQUEST_TIMEOUT = 5 * 1000;

	/* Number of milliseconds between sending pings to each sim */
	public final static int PING_INTERVAL = 2200;

	/* Number of milliseconds between sending camera updates */
	public static final int DEFAULT_AGENT_UPDATE_INTERVAL = 500;

	/*
	 * Number of milliseconds between updating the current positions of moving,
	 * non-accelerating and non-colliding objects
	 */
	public static final int INTERPOLATION_INTERVAL = 250;

	/*
	 * Millisecond interval between ticks, where all ACKs are sent out and the
	 * age of unACKed packets is checked
	 */
	public static final int NETWORK_TICK_INTERVAL = 500;

	// /#region Sizes

	/*
	 * The initial size of the packet inbox, where packets are stored before
	 * processing
	 */
	public static final int PACKET_INBOX_SIZE = 100;
	/* Maximum size of packet that we want to send over the wire */
	public static final int MAX_PACKET_SIZE = 1200;
	/*
	 * The maximum value of a packet sequence number before it rolls over back
	 * to one
	 */
	public static final int MAX_SEQUENCE = 0xFFFFFF;
	/*
	 * The maximum size of the sequence number archive, used to check for resent
	 * and/or duplicate packets
	 */
	public static final int PACKET_ARCHIVE_SIZE = 200;
	/*
	 * Maximum number of queued ACKs to be sent before SendAcks() is forced
	 */
	public int MAX_PENDING_ACKS = 10;
	/* Network stats queue length (seconds) */
	public int STATS_QUEUE_SIZE = 5;

	// /#region Configuration options (mostly booleans)

	/*
	 * Enable to process packets synchronously, where all of the callbacks for
	 * each packet must return before the next packet is processed. This is an
	 * experimental feature and is not completely reliable yet. Ideally it would
	 * reduce context switches and thread overhead, but several calls currently
	 * block for a long time and would need to be rewritten as asynchronous code
	 * before this is feasible
	 */
	public boolean SYNC_PACKETCALLBACKS = false;

	public boolean LOG_RAW_PACKET_BYTES = false;

	/* Enable/disable storing terrain heightmaps in the TerrainManager */
	public boolean STORE_LAND_PATCHES = false;

	/* Enable/disable sending periodic camera updates */
	public boolean SEND_AGENT_UPDATES = true;

	/*
	 * Enable/disable automatically setting agent appearance at login and after
	 * sim crossing
	 */
	public boolean SEND_AGENT_APPEARANCE = true;

	/*
	 * Enable/disable automatically setting the bandwidth throttle after
	 * connecting to each simulator The default throttle uses the equivalent of
	 * the maximum bandwidth setting in the official client. If you do not set a
	 * throttle your connection will by default be throttled well below the
	 * minimum values and you may experience connection problems
	 */
	public boolean SEND_AGENT_THROTTLE = true;

	/*
	 * Enable/disable the sending of pings to monitor lag and packet loss
	 */
	public boolean SEND_PINGS = true;

	/*
	 * Should we connect to multiple sims? This will allow viewing in to
	 * neighboring simulators and sim crossings (Experimental)
	 */
	public boolean MULTIPLE_SIMS = true;

	/*
	 * If true, all object update packets will be decoded in to native objects.
	 * If false, only updates for our own agent will be decoded. Registering an
	 * event handler will force objects for that type to always be decoded. If
	 * this is disabled the object tracking will have missing or partial prim
	 * and avatar information
	 */
	public boolean ALWAYS_DECODE_OBJECTS = true;

	/*
	 * If true, when a cached object check is received from the server the full
	 * object info will automatically be requested
	 */
	public boolean ALWAYS_REQUEST_OBJECTS = true;

	/*
	 * Whether to establish connections to HTTP capabilities servers for
	 * simulators
	 */
	public boolean ENABLE_CAPS = true;

	/* Whether to decode sim stats */
	public boolean ENABLE_SIMSTATS = true;

	/*
	 * The capabilities servers are currently designed to periodically return a
	 * 502 error which signals for the client to re-establish a connection. Set
	 * this to true to log those 502 errors
	 */
	public boolean LOG_ALL_CAPS_ERRORS = false;

	/*
	 * If true, any reference received for a folder or item the library is not
	 * aware of will automatically be fetched
	 */
	public boolean FETCH_MISSING_INVENTORY = true;

	/*
	 * If true, and <code>SEND_AGENT_UPDATES</code> is true, AgentUpdate packets
	 * will continuously be sent out to give the bot smoother movement and
	 * autopiloting
	 */
	public boolean DISABLE_AGENT_UPDATE_DUPLICATE_CHECK = true;

	/*
	 * If true, currently visible avatars will be stored in dictionaries inside
	 * <code>Simulator.ObjectAvatars</code>. If false, a new Avatar or Primitive
	 * object will be created each time an object update packet is received
	 */
	public boolean AVATAR_TRACKING = true;

	/*
	 * If true, currently visible avatars will be stored in dictionaries inside
	 * <code>Simulator.ObjectPrimitives</code>. If false, a new Avatar or
	 * Primitive object will be created each time an object update packet is
	 * received
	 */
	public boolean OBJECT_TRACKING = true;

	/*
	 * If true, position and velocity will periodically be interpolated
	 * (extrapolated, technically) for objects and avatars that are being
	 * tracked by the library. This is necessary to increase the accuracy of
	 * speed and position estimates for simulated objects
	 */
	public boolean USE_INTERPOLATION_TIMER = true;

	/*
	 * If true, utilization statistics will be tracked. There is a minor penalty
	 * in CPU time for enabling this option.
	 */
	public boolean TRACK_UTILIZATION = false;
	// #region Parcel Tracking

	/*
	 * If true, parcel details will be stored in the
	 * <code>Simulator.Parcels</code> dictionary as they are received
	 */
	public boolean PARCEL_TRACKING = true;

	/*
	 * If true, an incoming parcel properties reply will automatically send a
	 * request for the parcel access list
	 */
	public boolean ALWAYS_REQUEST_PARCEL_ACL = true;

	/*
	 * if true, an incoming parcel properties reply will automatically send a
	 * request for the traffic count.
	 */
	public boolean ALWAYS_REQUEST_PARCEL_DWELL = true;

	// #region Asset Cache

	/*
	 * If true, images, and other assets downloaded from the server will be
	 * cached in a local directory
	 */
	public boolean USE_ASSET_CACHE = true;

	/* Path to store cached texture data */
	public String ASSET_CACHE_DIR = RESOURCE_DIR + "/cache";

	/* Maximum size cached files are allowed to take on disk (bytes) */
	public long ASSET_CACHE_MAX_SIZE = 1024 * 1024 * 1024; // 1GB

	// #region Misc

	/* Default color used for viewer particle effects */
	public Color4 DEFAULT_EFFECT_COLOR = new Color4(1, 0, 0, 1);

	/*
	 * Cost of uploading an asset, Read-only since this value is dynamically
	 * fetched at login
	 */
	public final int getUPLOAD_COST()
	{
		return priceUpload;
	}

	/* Maximum number of times to resend a failed packet */
	public int MAX_RESEND_COUNT = 3;

	/* Throttle outgoing packet rate */
	public boolean THROTTLE_OUTGOING_PACKETS = true;

	/* UUID of a texture used by some viewers to identify type of client used */
	public UUID CLIENT_IDENTIFICATION_TAG = UUID.Zero;

	// #region Texture Pipeline

	/*
	 * The maximum number of concurrent texture downloads allowed Increasing
	 * this number will not necessarily increase texture retrieval times due to
	 * simulator throttles
	 */
	public int MAX_CONCURRENT_TEXTURE_DOWNLOADS = 4;

	/*
	 * The Refresh timer inteval is used to set the delay between checks for
	 * stalled texture downloads
	 * 
	 * This is a static variable which applies to all instances
	 */
	public static long PIPELINE_REFRESH_INTERVAL = 500;

	/*
	 * Textures taking longer than this value will be flagged as timed out and
	 * removed from the pipeline
	 */
	public int PIPELINE_REQUEST_TIMEOUT = 45 * 1000;
	// /#region Logging Configuration

	/*
	 * Get or set the minimum log level to output to the console by default
	 * 
	 * If the library is not compiled with DEBUG defined and this level is set
	 * to DEBUG You will get no output on the console. This behavior can be
	 * overriden by creating a logger configuration file for log4net
	 */
	public static int LOG_LEVEL = LogLevel.Debug;

	/* Attach avatar names to log messages */
	public boolean LOG_NAMES = true;

	/* Log packet retransmission info */
	public boolean LOG_RESENDS = true;

	// /#region Private Fields

	private int priceUpload = 0;

	/**
	 * Starts the settings update
	 * 
	 * @param client
	 *            Reference to a GridClient object
	 */
	public void Startup(GridClient client)
	{
		client.Network.RegisterCallback(PacketType.EconomyData, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case EconomyData:
				EconomyDataPacket econ = (EconomyDataPacket) packet;
				priceUpload = econ.Info.PriceUpload;
				break;
		}
	}
}
