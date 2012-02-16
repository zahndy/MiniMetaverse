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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.nio.concurrent.FutureCallback;

import net.xmlrpc.XMLRPCClient;

import libomv.GridClient;
import libomv.GridClient.GridInfo;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.LLSD.LLSDNotation;
import libomv.assets.AssetItem.AssetType;
import libomv.capabilities.CapsClient;
import libomv.inventory.InventoryFolder;
import libomv.packets.EconomyDataRequestPacket;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;
import libomv.utils.TimeoutEvent;
import libomv.utils.TimeoutEventQueue;

public class LoginManager
{
	// #region Enums
	public enum LoginStatus
	{
		None, Failed, ConnectingToLogin, ReadingResponse, Redirecting, ConnectingToSim, Success;
	}

	// #endregion Enums

	// #region Structs
	/** Login Request Parameters */
	public class LoginParams
	{
		/** The URL of the Login Server */
		public String URI;
		/**
		 * The number of milliseconds to wait before a login is considered
		 * failed due to timeout
		 */
		public int Timeout;
		/**
		 * The request method login_to_simulator is currently the only supported
		 * method
		 */
		public String MethodName;
		/** The Agents First name */
		public String FirstName;
		/** The Agents Last name */
		public String LastName;
		/**
		 * A md5 hashed password, plaintext password will be automatically
		 * hashed
		 */
		public String Password;
		/**
		 * The agents starting location once logged in Either "last", "home", or
		 * a string encoded URI containing the simulator name and x/y/z
		 * coordinates e.g: uri:hooper&amp;128&amp;152&amp;17
		 */
		public String Start;
		/**
		 * A string containing the client software channel information
		 * <example>Second Life Release</example>
		 */
		public String Channel;
		/**
		 * The client software version information The official viewer uses:
		 * Second Life Release n.n.n.n where n is replaced with the current
		 * version of the viewer
		 */
		public String Version;
		/** A string containing the platform information the agent is running on */
		public String Platform;
		/** A string hash of the network cards Mac Address */
		public String MAC;
		/** Unknown or deprecated */
		public String ViewerDigest;
		/**
		 * A string hash of the first disk drives ID used to identify this
		 * clients uniqueness
		 */
		public String ID0;
		/**
		 * A string containing the viewers Software, this is not directly sent
		 * to the login server but is used by the library to generate the
		 * Version information
		 */
		public String UserAgent;
		/**
		 * A string representing the software creator. This is not directly sent
		 * to the login server but is used by the library to generate the
		 * Version information
		 */
		public String Author;
		/**
		 * If true, this agent agrees to the Terms of Service of the grid its
		 * connecting to
		 */
		public boolean AgreeToTos;
		/** Unknown */
		public boolean ReadCritical;
		/**
		 * An array of string sent to the login server to enable various options
		 */
		public String[] Options;

		/**
		 * Default constructor, initializes sane default values
		 */
		public LoginParams()
		{
			this.Options = new String[] { "inventory-root", "inventory-skeleton", "inventory-lib-root",
					"inventory-lib-owner", "inventory-skel-lib", "initial-outfit", "gestures", "event_categories",
					"event_notifications", "classified_categories", "buddy-list", "ui-config", "tutorial_settings",
					"login-flags", "global-textures", "adult_compliant" };
			this.MethodName = "login_to_simulator";
			this.Start = "last";
			this.Platform = Helpers.GetPlatform();
			this.MAC = Helpers.GetMAC();
			this.ViewerDigest = "";
			this.ID0 = Helpers.GetMAC();
			this.AgreeToTos = true;
			this.ReadCritical = true;
			this.Channel = Settings.APPLICATION_NAME;
			this.Version = Settings.APPLICATION_VERSION;
		}


		public LoginParams(GridClient client)
		{
			this();
			this.Timeout = client.Settings.LOGIN_TIMEOUT;
			GridInfo gridInfo = client.getDefaultGrid();
			this.URI = gridInfo.loginuri;
			this.FirstName = gridInfo.firstname;
			this.LastName = gridInfo.lastname;
			this.Password = gridInfo.getPassword();
			if (gridInfo.startLocation != null)
				this.Start = gridInfo.startLocation;
		}
		
		/**
		 * Instantiates new LoginParams object and fills in the values
		 * 
		 * @param client
		 *            Instance of GridClient to read settings from
		 * @param firstName
		 *            Login first name
		 * @param lastName
		 *            Login last name
		 * @param password
		 *            Password
		 * @param startLocation
		 *            location to start in, if null, "last" is used
		 */
		public LoginParams(GridClient client, String firstName, String lastName, String password, String startLocation)
		{
			this();
			this.URI = client.getDefaultGrid().loginuri;
			this.Timeout = client.Settings.LOGIN_TIMEOUT;
			this.FirstName = firstName;
			this.LastName = lastName;
			this.Password = password;
			if (startLocation != null)
				this.Start = startLocation;
		}
	}

	public final class BuddyListEntry
	{
		public int buddy_rights_given;
		public String buddy_id;
		public int buddy_rights_has;
	}

	/** The decoded data returned from the login server after a successful login */
	public final class LoginResponseData
	{
		/**
		 * true, false, indeterminate [XmlRpcMember("login")]
		 */
		public String Login;
		public boolean Success;
		public String Reason;
		/** Login message of the day */
		public String Message;
		public UUID AgentID;
		public UUID SessionID;
		public UUID SecureSessionID;
		public String FirstName;
		public String LastName;
		public String StartLocation;
		/** M or PG, also agent_region_access and agent_access_max */
		public String AgentAccess;
		public Vector3 LookAt;
		public long HomeRegion;
		public Vector3 HomePosition;
		public Vector3 HomeLookAt;
		public int CircuitCode;
		public int RegionX;
		public int RegionY;
		public short SimPort;
		public InetAddress SimIP;
		public String SeedCapability;
		public BuddyListEntry[] BuddyList;
		public int SecondsSinceEpoch;
		public String UDPBlacklist;

		// #region Inventory
		public UUID InventoryRoot;
		public UUID LibraryRoot;
		public InventoryFolder[] InventorySkeleton;
		public InventoryFolder[] LibrarySkeleton;
		public UUID LibraryOwner;
		// #endregion

		// #region Redirection
		public String NextMethod;
		public String NextUrl;
		public String[] NextOptions;
		public int NextDuration;
		// #endregion

		// These aren't currently being utilized by the library
		public String AgentAccessMax;
		public String AgentRegionAccess;
		public int AOTransition;
		public String InventoryHost;
        public int MaxAgentGroups;
        public String OpenIDUrl;
        public String XMPPHost;
        
		// Unhandled:
		// reply.gestures
		// reply.event_categories
		// reply.classified_categories
		// reply.event_notifications
		// reply.ui_config
		// reply.login_flags
		// reply.global_textures
		// reply.initial_outfit

		/**
		 * Parse LLSD Login Reply Data
		 * 
		 * @param reply
		 *            An {@link OSDMap} containing the login response data.
		 *            XML-RPC logins do not require this as XML-RPC.NET
		 *            automatically populates the struct properly using
		 *            attributes
		 * @throws IOException
		 */
		private void ParseLoginReply(OSDMap reply)
		{
			AgentID = reply.get("agent_id").AsUUID();
			SessionID = reply.get("session_id").AsUUID();
			SecureSessionID = reply.get("secure_session_id").AsUUID();
			FirstName = reply.get("first_name").AsString();
			LastName = reply.get("last_name").AsString();
			StartLocation = reply.get("start_location").AsString();
			AgentAccess = reply.get("agent_access").AsString();
			LookAt = reply.get("look_at").AsVector3();
			Reason = reply.get("reason").AsString();
			Message = reply.get("message").AsString();

			Login = reply.get("login").AsString();
			Success = reply.get("login").AsBoolean();

			// Home
			OSD osdHome = null;
			try
			{
				osdHome = LLSDNotation.parse(reply.get("home").AsString());
			}
			catch (Exception ex)
			{
				Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
			}

			if (osdHome != null && osdHome.getType().equals(OSDType.Map))
			{
				OSDMap home = (OSDMap) osdHome;

				OSD homeRegion = home.get("region_handle");
				if (homeRegion != null && homeRegion.getType().equals(OSDType.Array))
				{
					OSDArray homeArray = (OSDArray) homeRegion;
					if (homeArray.size() == 2)
					{
						HomeRegion = Helpers.UIntsToLong(homeArray.get(0).AsInteger(), homeArray.get(1).AsInteger());
					}
					else
					{
						HomeRegion = 0;
					}
				}
				HomePosition = home.get("position").AsVector3();
				HomeLookAt = home.get("look_at").AsVector3();
			}
			else
			{
				HomeRegion = 0;
				HomePosition = Vector3.Zero;
				HomeLookAt = Vector3.Zero;
			}

			CircuitCode = reply.get("circuit_code").AsUInteger();
			RegionX = reply.get("region_x").AsUInteger();
			RegionY = reply.get("region_y").AsUInteger();
			SimPort = (short) reply.get("sim_port").AsUInteger();
			SimIP = reply.get("sim_ip").AsInetAddress();
			SeedCapability = reply.get("seed_capability").AsString();

			// Buddy list
			OSD buddyLLSD = reply.get("buddy-list");
			if (buddyLLSD != null && buddyLLSD.getType().equals(OSDType.Array))
			{
				OSDArray buddyArray = (OSDArray) buddyLLSD;
				BuddyList = new BuddyListEntry[buddyArray.size()];
				for (int i = 0; i < buddyArray.size(); i++)
				{
					if (buddyArray.get(i).getType().equals(OSDType.Map))
					{
						BuddyListEntry bud = new BuddyListEntry();
						OSDMap buddy = (OSDMap) buddyArray.get(i);

						bud.buddy_id = buddy.get("buddy_id").AsString();
						bud.buddy_rights_given = buddy.get("buddy_rights_given").AsUInteger();
						bud.buddy_rights_has = buddy.get("buddy_rights_has").AsUInteger();

						BuddyList[i] = bud;
					}
				}
			}

			SecondsSinceEpoch = reply.get("seconds_since_epoch").AsUInteger();

			InventoryRoot = ParseMappedUUID("inventory-root", "folder_id", reply);
			InventorySkeleton = ParseInventorySkeleton("inventory-skeleton", reply);

			LibraryOwner = ParseMappedUUID("inventory-lib-owner", "agent_id", reply);
			LibraryRoot = ParseMappedUUID("inventory-lib-root", "folder_id", reply);
			LibrarySkeleton = ParseInventorySkeleton("inventory-skel-lib", reply);

            // UDP Blacklist
            if (reply.containsKey("udp_blacklist"))
            {
                UDPBlacklist = reply.get("udp_blacklist").AsString();
            }

            if (reply.containsKey("max-agent-groups"))
            {
                MaxAgentGroups = reply.get("max-agent-groups").AsUInteger();
            }
            else
            {
                MaxAgentGroups = -1;
            }

            if (reply.containsKey("openid_url"))
            {
                OpenIDUrl = reply.get("openid_url").AsString();
            }

            if (reply.containsKey("xmpp_host"))
            {
                XMPPHost = reply.get("xmpp_host").AsString();
            }
}

		private void ParseLoginReply(Map<String, Object> reply)
		{
			try
			{
				AgentID = ParseUUID("agent_id", reply);
				SessionID = ParseUUID("session_id", reply);
				SecureSessionID = ParseUUID("secure_session_id", reply);
				FirstName = ParseString("first_name", reply);
				if (FirstName.startsWith("\""))
				{
					FirstName.substring(1);
				}
				if (FirstName.endsWith("\""))
				{
					FirstName.substring(0, FirstName.length() - 1);
				}
				LastName = ParseString("last_name", reply);
				if (LastName.startsWith("\""))
				{
					LastName.substring(1);
				}
				if (LastName.endsWith("\""))
				{
					LastName.substring(0, LastName.length() - 1);
				}
				// "first_login" for brand new accounts
				StartLocation = ParseString("start_location", reply);
				AgentAccess = ParseString("agent_access", reply);
				LookAt = ParseVector3("look_at", reply);
				Reason = ParseString("reason", reply);
				Message = ParseString("message", reply);

				if (reply.containsKey("login"))
				{
					Login = ParseString("login", reply);
					Success = Login.equals("true");

					// Parse redirect options
					if (Login.equals("indeterminate"))
					{
						NextUrl = ParseString("next_url", reply);
						NextDuration = ParseUInt("next_duration", reply);
						NextMethod = ParseString("next_method", reply);
						NextOptions = ParseArray("next_options", reply);
					}
				}
			}
			catch (Exception ex)
			{
				Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
			}

			if (!Success)
			{
				return;
			}

			// Home
			if (reply.containsKey("home"))
			{
				OSD osdHome = null;
				try
				{
					osdHome = LLSDNotation.parse(reply.get("home").toString());
				}
				catch (Exception ex)
				{
					Logger.Log("Login server returned (some) invalid data: " + ex.getMessage(), LogLevel.Warning, ex);
				}

				if (osdHome != null && osdHome.getType().equals(OSDType.Map))
				{
					OSDMap home = (OSDMap) osdHome;

					OSD homeRegion = home.get("region_handle");
					if (homeRegion != null && homeRegion.getType().equals(OSDType.Array))
					{
						OSDArray homeArray = (OSDArray) homeRegion;
						if (homeArray.size() == 2)
						{
							HomeRegion = Helpers.UIntsToLong(homeArray.get(0).AsUInteger(), homeArray.get(1)
									.AsUInteger());
						}
						else
						{
							HomeRegion = 0;
						}
					}
					HomePosition = home.get("position").AsVector3();
					HomeLookAt = home.get("look_at").AsVector3();
				}
			}
			else
			{
				HomeRegion = 0;
				HomePosition = Vector3.Zero;
				HomeLookAt = Vector3.Zero;
			}

			CircuitCode = ParseUInt("circuit_code", reply);
			RegionX = ParseUInt("region_x", reply);
			RegionY = ParseUInt("region_y", reply);
			SimPort = (short) ParseUInt("sim_port", reply);
			try
			{
				SimIP = InetAddress.getByName(ParseString("sim_ip", reply));
			}
			catch (UnknownHostException e)
			{
				SimIP = null;
			}
			SeedCapability = ParseString("seed_capability", reply);

			// Buddy list
			if (reply.containsKey("buddy-list") && reply.get("buddy-list") instanceof Object[])
			{
				Object[] buddyArray = (Object[]) reply.get("buddy-list");
				BuddyList = new BuddyListEntry[buddyArray.length];
				for (int i = 0; i < buddyArray.length; i++)
				{
					if (buddyArray[i] instanceof Map)
					{
						@SuppressWarnings("unchecked")
						Map<String, Object> buddy = (Map<String, Object>) buddyArray[i];
						BuddyList[i] = new BuddyListEntry();
						BuddyList[i].buddy_id = ParseString("buddy_id", buddy);
						BuddyList[i].buddy_rights_given = ParseUInt("buddy_rights_given", buddy);
						BuddyList[i].buddy_rights_has = ParseUInt("buddy_rights_has", buddy);
					}
				}
			}

			SecondsSinceEpoch = ParseUInt("seconds_since_epoch", reply);

			InventoryRoot = ParseMappedUUID("inventory-root", "folder_id", reply);
			InventorySkeleton = ParseInventorySkeleton("inventory-skeleton", reply);

			LibraryOwner = ParseMappedUUID("inventory-lib-owner", "agent_id", reply);
			LibraryRoot = ParseMappedUUID("inventory-lib-root", "folder_id", reply);
			LibrarySkeleton = ParseInventorySkeleton("inventory-skel-lib", reply);

			// UDP Blacklist
			if (reply.containsKey("udp_blacklist"))
			{
				UDPBlacklist = ParseString("udp_blacklist", reply);
			}

			if (reply.containsKey("max-agent-groups"))
            {
                MaxAgentGroups = ParseUInt("max-agent-groups", reply);
            }
            else
            {
                MaxAgentGroups = -1;
            }

            if (reply.containsKey("openid_url"))
            {
                OpenIDUrl = ParseString("openid_url", reply);
            }

            if (reply.containsKey("xmpp_host"))
            {
                XMPPHost = ParseString("xmpp_host", reply);
            }
		}

		private InventoryFolder[] ParseInventorySkeleton(String key, OSDMap reply)
		{
			UUID ownerID;
			if (key.equals("inventory-skel-lib"))
			{
				ownerID = LibraryOwner;
			}
			else
			{
				ownerID = AgentID;
			}

			OSD skeleton = reply.get(key);
			if (skeleton != null && skeleton.getType().equals(OSDType.Array))
			{
				OSDArray array = (OSDArray) skeleton;
				InventoryFolder[] folders = new InventoryFolder[array.size()];
				for (int i = 0; i < array.size(); i++)
				{
					if (array.get(i).getType().equals(OSDType.Map))
					{
						OSDMap map = (OSDMap) array.get(i);
						folders[i] = new InventoryFolder(map.get("folder_id").AsUUID(), map.get("parent_id").AsUUID(), ownerID);
						folders[i].name = map.get("name").AsString();
						folders[i].preferredType = AssetType.setValue(map.get("type_default").AsInteger());
						folders[i].version = map.get("version").AsInteger();
					}
				}
				return folders;
			}
			return null;
		}

		private InventoryFolder[] ParseInventorySkeleton(String key, Map<String, Object> reply)
		{
			UUID ownerID;
			if (key.equals("inventory-skel-lib"))
			{
				ownerID = LibraryOwner;
			}
			else
			{
				ownerID = AgentID;
			}

			if (reply.containsKey(key) && reply.get(key) instanceof Object[])
			{
				Object[] array = (Object[]) reply.get(key);
				InventoryFolder[] folders = new InventoryFolder[array.length];
				for (int i = 0; i < array.length; i++)
				{
					if (array[i] instanceof Map)
					{
						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) array[i];
						folders[i] = new InventoryFolder(ParseUUID("folder_id", map), ParseUUID("parent_id", map), ownerID);
						folders[i].name = ParseString("name", map);
						folders[i].preferredType = AssetType.setValue(ParseUInt("type_default", map));
						folders[i].version = ParseUInt("version", map);
					}
				}
				return folders;
			}
			return null;
		}
	}

	// #endregion Structs

	// #region Callback handlers

	// An event for being logged out either through client request, server
	// forced, or network error
	public class LoginProgressCallbackArgs implements CallbackArgs
	{
		private final LoginStatus m_Status;
		private final String m_Message;
		private final String m_Reason;
		private LoginResponseData m_Reply;

		public final LoginStatus getStatus()
		{
			return m_Status;
		}

		public final String getMessage()
		{
			return m_Message;
		}

		public final String getReason()
		{
			return m_Reason;
		}

		public LoginResponseData getReply()
		{
			return m_Reply;
		}

		public LoginProgressCallbackArgs(LoginStatus login, String message, String reason, LoginResponseData reply)
		{
			this.m_Reply = reply;
			this.m_Status = login;
			this.m_Message = message;
			this.m_Reason = reason;
		}
	}

	public CallbackHandler<LoginProgressCallbackArgs> OnLoginProgress = new CallbackHandler<LoginProgressCallbackArgs>();

	private HashMap<Callback<LoginProgressCallbackArgs>, String[]> CallbackOptions = new HashMap<Callback<LoginProgressCallbackArgs>, String[]>();

	public final void RegisterLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback, String[] options,
			boolean autoremove)
	{
		if (options != null)
			CallbackOptions.put(callback, options);
		OnLoginProgress.add(callback, autoremove);
	}

	public final void UnregisterLoginProgressCallback(Callback<LoginProgressCallbackArgs> callback)
	{
		CallbackOptions.remove(callback);
		OnLoginProgress.remove(callback);
	}

	// #endregion Callback handlers

	// #region Private Members
	private GridClient _Client;

	private TimeoutEventQueue<LoginStatus> LoginEvents = new TimeoutEventQueue<LoginStatus>();

	// #endregion

	public LoginManager(GridClient client)
	{
		this._Client = client;
	}

	// #region Public Methods

	// #region Login Routines

	/**
	 * Generate sane default values for a login request
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param startLocation
	 *            Location where to start such as "home", "last", or an explicit
	 *            start location
	 * @return A populated {@link LoginParams} struct containing sane defaults
	 */
	public final LoginParams DefaultLoginParams(String firstName, String lastName, String password, String startLocation)
	{
		return new LoginParams(_Client, firstName, lastName, password, startLocation);
	}

	/**
	 * Generate sane default values for a login request
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param userAgent
	 *            Client application name
	 * @param userVersion
	 *            Client application version
	 * @return A populated {@link LoginParams} struct containing sane defaults
	 */
	public final LoginParams DefaultLoginParams(String firstName, String lastName, String password, String userAgent,
			String userVersion)
	{
		LoginParams params = new LoginParams(_Client, firstName, lastName, password, null);
		params.Channel = userAgent;
		params.Version = userVersion;
		return params;
	}

	/**
	 * Simplified login that takes the most common and required fields to
	 * receive Logs in to the last known position the avatar was in
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information
	 *         about the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password) throws Exception
	{
		return Login(new LoginParams(_Client, firstName, lastName, password, null));
	}

	/**
	 * Simplified login that takes the most common and required fields to
	 * receive
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param startLocation
	 *            The location to login too, such as "last", "home", or an
	 *            explicit start location
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information
	 *         about the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String startLocation)
			throws Exception
	{
		return Login(new LoginParams(_Client, firstName, lastName, password, startLocation));
	}

	/**
	 * Simplified login that takes the most common and required fields To
	 * receive
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password
	 * @param userAgent
	 *            Client application name
	 * @param userVersion
	 *            Client application version
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information
	 *         about the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String userAgent, String userVersion)
			throws Exception
	{
		return Login(DefaultLoginParams(firstName, lastName, password, userAgent, userVersion));
	}

	/**
	 * Simplified login that takes the most common fields along with a starting
	 * location URI, and can accept an MD5 string instead of a plaintext
	 * password
	 * 
	 * @param firstName
	 *            Account first name
	 * @param lastName
	 *            Account last name
	 * @param password
	 *            Account password or MD5 hash of the password such as
	 *            $1$1682a1e45e9f957dcdf0bb56eb43319c
	 * @param start
	 *            Starting location URI that can be built with StartLocation()
	 * @param userAgent
	 *            Client application name
	 * @param userVersion
	 *            Client application version
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information
	 *         about the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(String firstName, String lastName, String password, String start, String userAgent,
			String userVersion) throws Exception
	{
		LoginParams loginParams = DefaultLoginParams(firstName, lastName, password, userAgent, userVersion);
		loginParams.Start = start;

		return Login(loginParams);
	}

	/**
	 * Login that takes a struct of all the values that will be passed to the
	 * login server
	 * 
	 * @param loginParams
	 *            The values that will be passed to the login server, all fields
	 *            must be set even if they are ""
	 * @return Whether the login was successful or not. Register to the
	 *         OnLoginResponse callback to receive more detailed information
	 *         about the errors that have occurred
	 * @throws Exception
	 */
	public final boolean Login(LoginParams loginParams) throws Exception
	{
		// FIXME: Now that we're using CAPS we could cancel the current login
		// and start a new one
		if (LoginEvents.size() != 0)
		{
			throw new Exception("Login already in progress");
		}

		TimeoutEvent<LoginStatus> loginEvent = LoginEvents.create();
		RequestLogin(loginParams, null);
		LoginStatus status = loginEvent.waitOne(loginParams.Timeout);
		LoginEvents.cancel(loginEvent);
		if (status == null)
		{
			UpdateLoginStatus(LoginStatus.Failed, "Logon timed out", "timeout", null);
			return false;
		}
		return (status == LoginStatus.Success);
	}

	/**
	 * Build a start location URI for passing to the Login function
	 * 
	 * @param sim
	 *            Name of the simulator to start in
	 * @param x
	 *            X coordinate to start at
	 * @param y
	 *            Y coordinate to start at
	 * @param z
	 *            Z coordinate to start at
	 * @return String with a URI that can be used to login to a specified
	 *         location
	 */
	public static String StartLocation(String sim, int x, int y, int z)
	{
		return String.format("uri:%s&%d&%d&%d", sim, x, y, z);
	}

	public void RequestLogin(final LoginParams loginParams, Callback<LoginProgressCallbackArgs> callback) throws Exception
	{
		// #region Sanity Check loginParams
		if (loginParams.Options == null)
		{
			loginParams.Options = new String[] {};
		}

		// Convert the password to MD5 if it isn't already
		if (loginParams.Password.length() != 35 && !loginParams.Password.startsWith("$1$"))
		{
			loginParams.Password = Helpers.MD5Password(loginParams.Password);
		}
		// #endregion

		if (callback != null)
			RegisterLoginProgressCallback(callback, loginParams.Options, false);

		URI loginUri;
		try
		{
			loginUri = new URI(loginParams.URI);
		}
		catch (Exception ex)
		{
			Logger.Log(String.format("Failed to parse login URI %s, %s", loginParams.URI, ex.getMessage()),
					LogLevel.Error, _Client);
			throw ex;
		}

		UpdateLoginStatus(LoginStatus.ConnectingToLogin, "Logging in as " + loginParams.FirstName + " " 
				          + loginParams.LastName + " ...", null, null);

		try
		{
			if (_Client.Settings.USE_LLSD_LOGIN)
			{
				// #region LLSD Based Login

				// Create the CAPS login structure
				OSDMap loginLLSD = new OSDMap();
				loginLLSD.put("first", OSD.FromString(loginParams.FirstName));
				loginLLSD.put("last", OSD.FromString(loginParams.LastName));
				loginLLSD.put("passwd", OSD.FromString(loginParams.Password));
				loginLLSD.put("start", OSD.FromString(loginParams.Start));
				loginLLSD.put("channel", OSD.FromString(loginParams.Channel));
				loginLLSD.put("version", OSD.FromString(loginParams.Version));
				loginLLSD.put("platform", OSD.FromString(loginParams.Platform));
				loginLLSD.put("mac", OSD.FromString(loginParams.MAC));
				loginLLSD.put("agree_to_tos", OSD.FromBoolean(loginParams.AgreeToTos));
				loginLLSD.put("read_critical", OSD.FromBoolean(loginParams.ReadCritical));
				loginLLSD.put("viewer_digest", OSD.FromString(loginParams.ViewerDigest));
				loginLLSD.put("id0", OSD.FromString(loginParams.ID0));

				OSDArray optionsOSD;
				// Create the options LLSD array
				if (loginParams.Options != null && loginParams.Options.length > 0)
				{
					optionsOSD = new OSDArray(loginParams.Options.length);
					for (int i = 0; i < loginParams.Options.length; i++)
					{
						optionsOSD.add(OSD.FromString(loginParams.Options[i]));
					}

					for (String[] callbackOpts : CallbackOptions.values())
					{
						if (callbackOpts != null)
						{
							for (int i = 0; i < callbackOpts.length; i++)
							{
								if (!optionsOSD.contains(callbackOpts[i]))
								{
									optionsOSD.add(OSD.FromString(callbackOpts[i]));
								}
							}
						}
					}
				}
				else
				{
					optionsOSD = new OSDArray();					
				}
				loginLLSD.put("options", optionsOSD);

				// Make the CAPS POST for login
				CapsClient loginRequest = new CapsClient();
				loginRequest.setResultCallback(new LoginReplyLLSDHandler(loginParams));
				loginRequest.executeHttpPost(loginUri, loginLLSD, OSDFormat.Xml, loginParams.Timeout);
				// #endregion
			}
			else
			{
				// #region XML-RPC Based Login Code

				// Create the Hashtable for XmlRpcCs
				HashMap<String, Object> loginXmlRpc = new HashMap<String, Object>();
				loginXmlRpc.put("first", loginParams.FirstName != null ? loginParams.FirstName : Helpers.EmptyString);
				loginXmlRpc.put("last", loginParams.LastName != null ? loginParams.LastName : Helpers.EmptyString);
				loginXmlRpc.put("passwd", loginParams.Password != null ? loginParams.Password : Helpers.EmptyString);
				loginXmlRpc.put("start", loginParams.Start != null ? loginParams.Start : Helpers.EmptyString);
				loginXmlRpc.put("channel", loginParams.Channel != null ? loginParams.Channel : Helpers.EmptyString);
				loginXmlRpc.put("version", loginParams.Version != null ? loginParams.Version : Helpers.EmptyString);
				loginXmlRpc.put("platform", loginParams.Platform != null ? loginParams.Platform : Helpers.EmptyString);
				loginXmlRpc.put("mac", loginParams.MAC != null ? loginParams.MAC : Helpers.EmptyString);
				if (loginParams.AgreeToTos)
				{
					loginXmlRpc.put("agree_to_tos", "true");
				}
				if (loginParams.ReadCritical)
				{
					loginXmlRpc.put("read_critical", "true");
				}
				loginXmlRpc.put("id0", loginParams.ID0 != null ? loginParams.ID0 : Helpers.EmptyString);
				loginXmlRpc.put("last_exec_event", 0);

				ArrayList<String> options = new ArrayList<String>();
				// Create the options array
				if (loginParams.Options != null && loginParams.Options.length > 0)
				{
					for (int i = 0; i < loginParams.Options.length; i++)
					{
						options.add(loginParams.Options[i]);
					}

					for (String[] callbackOpts : CallbackOptions.values())
					{
						if (callbackOpts != null)
						{
							for (int i = 0; i < callbackOpts.length; i++)
							{
								if (!options.contains(callbackOpts[i]))
								{
									options.add(callbackOpts[i]);
								}
							}
						}
					}
				}
				loginXmlRpc.put("options", options);

				final XMLRPCClient client = new XMLRPCClient(loginUri);
				final Object[] request = new Object[] { loginXmlRpc };

				if (loginUri.getScheme().equals("https"))
				{
					KeyStore ks = Helpers.GetExtendedKeyStore();
					ks.setCertificateEntry(loginUri.getHost(), Helpers.GetCertificate(loginUri.getHost()));
					client.register(new Scheme("https", 443, new SSLSocketFactory(ks)));
				}

				// Start the request
				Thread requestThread = new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							Object data = client.callEx(loginParams.MethodName, request);
							HandleLoginReplyXmlRpc(data, loginParams);
						}
						catch (Exception ex)
						{
							UpdateLoginStatus(LoginStatus.Failed, ex.toString(), ex.getClass().toString(), null);
						}
					}
				};
				requestThread.setName("XML-RPC Login");
				requestThread.start();
			}
			// #endregion
		}
		catch (Exception ex)
		{
			UpdateLoginStatus(LoginStatus.Failed, ex.toString(), ex.getClass().toString(), null);
			throw ex;
		}
	}

	// #endregion

	// #region Private Methods

	private void UpdateLoginStatus(LoginStatus status, String message, String reason, LoginResponseData reply)
	{
		// If we reached a login resolution trigger the event
		if (status == LoginStatus.Success || status == LoginStatus.Failed)
		{
			LoginEvents.set(status);
		}
		// Fire the login status callback
		OnLoginProgress.dispatch(new LoginProgressCallbackArgs(status, message, reason, reply));
	}

	/**
	 * Handles response from XML-RPC login replies
	 * 
	 * @param response
	 *            the response object
	 * @param the
	 *            login params, used to start the next round on redirects
	 * @throws Exception
	 */
	private void HandleLoginReplyXmlRpc(Object response, LoginParams loginParams) throws Exception
	{
		LoginResponseData reply = new LoginResponseData();

		// Fetch the login response
		if (response == null || !(response instanceof Map))
		{
			UpdateLoginStatus(LoginStatus.Failed, "Invalid or missing login response from the server", "bad response", null);
			return;
		}

		UpdateLoginStatus(LoginStatus.ReadingResponse, "Parsing Reply data", "parsing", null);

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) response;
		reply.ParseLoginReply(result);

		if (reply.Success)
		{
			// Remove the quotes around our first name.
			if (reply.FirstName.charAt(0) == '"')
			{
				reply.FirstName = reply.FirstName.substring(1);
			}
			if (reply.FirstName.charAt(reply.FirstName.length() - 1) == '"')
			{
				reply.FirstName = reply.FirstName.substring(0, reply.FirstName.length() - 1);
			}
		}
		HandleLoginResponse(reply, loginParams);
	}

	/**
	 * Handle response from LLSD login replies
	 * 
	 * @param client
	 * @param result
	 * @param error
	 */
	private class LoginReplyLLSDHandler implements FutureCallback<OSD>
	{
		private final LoginParams loginParams;

		public LoginReplyLLSDHandler(LoginParams loginParams)
		{
			this.loginParams = loginParams;
		}

		@Override
		public void completed(OSD result)
		{
			if (result != null && result.getType().equals(OSDType.Map))
			{
				OSDMap map = (OSDMap) result;

				UpdateLoginStatus(LoginStatus.ReadingResponse, "Parsing Reply data", "parsing", null);

				LoginResponseData reply = new LoginResponseData();
				reply.ParseLoginReply(map);

				try
				{
					HandleLoginResponse(reply, loginParams);
				}
				catch (Exception ex)
				{
					UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getClass().toString(), null);
				}
			}
			else
			{
				// No LLSD response
				UpdateLoginStatus(LoginStatus.Failed, "Empty or unparseable login response", "bad response", null);
			}
		}

		@Override
		public void failed(Exception ex)
		{
			// Connection error
			UpdateLoginStatus(LoginStatus.Failed, ex.getMessage(), ex.getClass().toString(), null);
		}

		@Override
		public void cancelled()
		{
			// Connection canceled
			UpdateLoginStatus(LoginStatus.Failed, "connection canceled", "canceled", null);
		}
	}

	private void HandleLoginResponse(LoginResponseData reply, LoginParams loginParams) throws Exception
	{
		boolean redirect = reply.Login.equals("indeterminate");
		if (redirect)
		{
			// Login redirected

			// Make the next login URL jump
			UpdateLoginStatus(LoginStatus.Redirecting, reply.Message, reply.Reason, null);
			loginParams.URI = reply.NextUrl;
			loginParams.MethodName = reply.NextMethod;
			loginParams.Options = reply.NextOptions;

			// Sleep for some amount of time while the servers work
			int seconds = reply.NextDuration;
			Logger.Log("Sleeping for " + seconds + " seconds during a login redirect", LogLevel.Info, _Client);
			try
			{
				Thread.sleep(seconds * 1000);
			}
			catch (InterruptedException ex)
			{
			}

			RequestLogin(loginParams, null);
		}
		else if (reply.Success)
		{
			// Login succeeded
			_Client.Network.setCircuitCode(reply.CircuitCode);
			_Client.Network.setUDPBlacklist(reply.UDPBlacklist);

			UpdateLoginStatus(LoginStatus.ConnectingToSim, "Connecting to simulator...", "connecting", reply);

			if (reply.SimIP != null && reply.SimPort != 0)
			{
				// Connect to the sim given in the login reply
				if (_Client.Network.Connect(reply.SimIP, reply.SimPort,
						Helpers.UIntsToLong(reply.RegionX, reply.RegionY), true, reply.SeedCapability) != null)
				{
					// Request the economy data right after login
					_Client.Network.SendPacket(new EconomyDataRequestPacket());

					// Update the login message with the MOTD returned from the server
					UpdateLoginStatus(LoginStatus.Success, reply.Message, reply.Reason, reply);
				}
				else
				{
					UpdateLoginStatus(LoginStatus.Failed, "Unable to establish a UDP connection to the simulator",
							"connection failed", null);
				}
			}
			else
			{
				UpdateLoginStatus(LoginStatus.Failed, "Login server did not return a valid simulator address", "no sim", null);
			}
		}
		else
		{
			// Login failed, make sure a usable error key is set
			if (reply.Reason == null || reply.Reason.isEmpty())
			{
				reply.Reason = "unknown";
			}
			UpdateLoginStatus(LoginStatus.Failed, reply.Message, reply.Reason, reply);
		}
	}

	// #endregion

	// #region Parsing Helpers
	private static int ParseUInt(String key, Map<String, Object> reply)
	{
		if (reply.containsKey(key))
		{
			Object value = reply.get(key);
			if (value instanceof Integer)
			{
				return (Integer) value;
			}
		}
		return 0;
	}

	private static UUID ParseUUID(String key, Map<String, Object> reply)
	{
		if (reply.containsKey(key))
		{
			return new UUID(reply.get(key).toString());
		}
		return UUID.Zero;
	}

	private static String ParseString(String key, Map<String, Object> reply)
	{
		if (reply.containsKey(key))
		{
			return reply.get(key).toString();
		}
		return Helpers.EmptyString;
	}

	private static Vector3 ParseVector3(String key, Map<String, Object> reply) throws ParseException, IOException
	{
		if (reply.containsKey(key))
		{
			Object value = reply.get(key);

			if (value instanceof Object[])
			{
				String[] list = (String[])value;
				if (list.length == 3)
				{
					float x, y, z;
					x = Helpers.TryParseFloat(list[0]);
					y = Helpers.TryParseFloat(list[1]);
					z = Helpers.TryParseFloat(list[2]);

					return new Vector3(x, y, z);
				}
			}
			else if (value instanceof String)
			{
				OSD osd = LLSDNotation.parse((String) value);
				if (osd != null && osd.getType().equals(OSDType.Array))
					return ((OSDArray) osd).AsVector3();
			}
		}
		return Vector3.Zero;
	}

	private static UUID ParseMappedUUID(String key, String key2, OSDMap reply)
	{
		OSD folderOSD = reply.get(key);
		if (folderOSD != null && folderOSD.getType().equals(OSDType.Array))
		{
			OSDArray array = (OSDArray) folderOSD;
			if (array.size() == 1 && array.get(0).getType().equals(OSDType.Map))
			{
				OSDMap map = (OSDMap) array.get(0);
				OSD folder = map.get(key2);
				if (folder != null)
				{
					return folder.AsUUID();
				}
			}
		}
		return UUID.Zero;
	}

	private static UUID ParseMappedUUID(String key, String key2, Map<String, Object> reply)
	{
		if (reply.containsKey(key) && reply.get(key) instanceof Object[])
		{
			Object[] array = (Object[])reply.get(key);
			if (array.length == 1 && array[0] instanceof Map)
			{
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) array[0];
				return ParseUUID(key2, map);
			}
		}
		return UUID.Zero;
	}

	private static String[] ParseArray(String key, Map<String, Object> reply)
	{
		Object o = reply.get(key);
		if (o instanceof Object[])
		{
			Object[] array = (Object[]) o;
			String[] strings = new String[array.length];
			for (int i = 0; i < array.length; i++)
			{
				strings[i] = array[i].toString();
			}
		}
		return null;
	}
	// #endregion Parsing Helpers

	// #region CallbackArgs

}