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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import libomv.ParcelManager.ParcelCategory;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.DirLandReplyMessage;
import libomv.capabilities.CapsMessage.PlacesReplyMessage;
import libomv.capabilities.IMessage;
import libomv.packets.DirClassifiedQueryPacket;
import libomv.packets.DirClassifiedReplyPacket;
import libomv.packets.DirEventsReplyPacket;
import libomv.packets.DirFindQueryPacket;
import libomv.packets.DirGroupsReplyPacket;
import libomv.packets.DirLandQueryPacket;
import libomv.packets.DirLandReplyPacket;
import libomv.packets.DirPeopleReplyPacket;
import libomv.packets.DirPlacesQueryPacket;
import libomv.packets.DirPlacesReplyPacket;
import libomv.packets.DirPlacesReplyPacket.QueryRepliesBlock;
import libomv.packets.EventInfoReplyPacket;
import libomv.packets.EventInfoRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PlacesQueryPacket;
import libomv.packets.PlacesReplyPacket;
import libomv.types.PacketCallback;
import libomv.types.UUID;
import libomv.types.Vector3d;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.CallbackArgs;
import libomv.utils.Helpers;

/* Access to the data server which allows searching for land, events, people, etc */
public class DirectoryManager implements PacketCallback, CapsCallback
{
	// /#region Enums
	/* Classified Ad categories */
	public enum ClassifiedCategories
	{
		// Classified is listed in the Any category
		Any,
		// Classified is shopping related
		Shopping,
		// Classified is
		LandRental,
		//
		PropertyRental,
		//
		SpecialAttraction,
		//
		NewProducts,
		//
		Employment,
		//
		Wanted,
		//
		Service,
		//
		Personal;

		public static ClassifiedCategories setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(ClassifiedCategories value)
		{
			return (byte) value.ordinal();
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// Event Categories
	public enum EventCategories
	{
		//
		All(0),
		//
		Discussion(18),
		//
		Sports(19),
		//
		LiveMusic(20),
		//
		Commercial(22),
		//
		Nightlife(23),
		//
		Games(24),
		//
		Pageants(25),
		//
		Education(26),
		//
		Arts(27),
		//
		Charity(28),
		//
		Miscellaneous(29);

		public int value;

		EventCategories(int val)
		{
			this.value = val;
		}
	}

	/*
	 * Query Flags used in many of the DirectoryManager methods to specify which
	 * query to execute and how to return the results.
	 * 
	 * Flags can be combined using the | (pipe) character, not all flags are
	 * available in all queries
	 */
	public static class DirFindFlags
	{
		// Query the People database
		public static final int People = 1 << 0;
		//
		public static final int Online = 1 << 1;
		// [Obsolete]
		// public static final int Places = 1 << 2;
		//
		public static final int Events = 1 << 3;
		// Query the Groups database
		public static final int Groups = 1 << 4;
		// Query the Events database
		public static final int DateEvents = 1 << 5;
		// Query the land holdings database for land owned by the currently
		// connected agent
		public static final int AgentOwned = 1 << 6;
		//
		public static final int ForSale = 1 << 7;
		// Query the land holdings database for land which is owned by a Group
		public static final int GroupOwned = 1 << 8;
		// [Obsolete]
		// public static final int Auction = 1 << 9;
		// Specifies the query should pre sort the results based upon traffic
		// when searching the Places database
		public static final int DwellSort = 1 << 10;
		//
		public static final int PgSimsOnly = 1 << 11;
		//
		public static final int PicturesOnly = 1 << 12;
		//
		public static final int PgEventsOnly = 1 << 13;
		//
		public static final int MatureSimsOnly = 1 << 14;
		// Specifies the query should pre sort the results in an ascending order
		// when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int SortAsc = 1 << 15;
		// Specifies the query should pre sort the results using the SalePrice
		// field when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int PricesSort = 1 << 16;
		// Specifies the query should pre sort the results by calculating the
		// average price/sq.m (SalePrice / Area) when searching the land sales
		// database.
		// This flag is only used when searching the land sales database
		public static final int PerMeterSort = 1 << 17;
		// Specifies the query should pre sort the results using the ParcelSize
		// field when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int AreaSort = 1 << 18;
		// Specifies the query should pre sort the results using the Name field
		// when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int NameSort = 1 << 19;
		// When set, only parcels less than the specified Price will be included
		// when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int LimitByPrice = 1 << 20;
		// When set, only parcels greater than the specified Size will be
		// included when searching the land sales database.
		// This flag is only used when searching the land sales database
		public static final int LimitByArea = 1 << 21;
		//
		public static final int FilterMature = 1 << 22;
		//
		public static final int PGOnly = 1 << 23;
		// Include PG land in results. This flag is used when searching both the
		// Groups, Events and Land sales databases
		public static final int IncludePG = 1 << 24;
		// Include Mature land in results. This flag is used when searching both
		// the Groups, Events and Land sales databases
		public static final int IncludeMature = 1 << 25;
		// Include Adult land in results. This flag is used when searching both
		// the Groups, Events and Land sales databases
		public static final int IncludeAdult = 1 << 26;
		//
		public static final int AdultOnly = 1 << 27;

		public static final int setValue(int value)
		{
			return (value & _mask);
		}

		public static final int getValue(int value)
		{
			return (value & _mask);
		}

		private static final int _mask = 0x0FFFFFDB;
	}

	/* Land types to search dataserver for */
	public static class SearchTypeFlags
	{
		// Search Auction, Mainland and Estate
		public static final byte Any = -1;
		// Land which is currently up for auction
		public static final byte Auction = 1 << 1;
		// Land available to new landowners (formerly the FirstLand program)
		// [Obsolete]
		// public static final byte Newbie = 1 << 2;
		// Parcels which are on the mainland (Linden owned) continents
		public static final byte Mainland = 1 << 3;
		// Parcels which are on privately owned simulators
		public static final byte Estate = 1 << 4;

		public static final byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static final int getValue(byte value)
		{
			return (value & _mask);
		}

		private static final byte _mask = 0x1B;
	}

	/* The content rating of the event */
	public enum EventFlags
	{
		// Event is PG
		PG,
		// Event is Mature
		Mature,
		// Event is Adult
		Adult;

		public static EventFlags setValue(int value)
		{
			return values()[value];
		}

		public static byte getValue(EventFlags value)
		{
			return (byte) value.ordinal();
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	/*
	 * Classified Ad Options
	 * 
	 * There appear to be two formats the flags are packed in. This set of flags
	 * is for the newer style
	 */
	public static class ClassifiedFlags
	{
		//
		public static final byte None = 1 << 0;
		//
		public static final byte Mature = 1 << 1;
		//
		public static final byte Enabled = 1 << 2;
		// Deprecated
		// public static final byte HasPrice = 1 << 3;
		//
		public static final byte UpdateTime = 1 << 4;
		//
		public static final byte AutoRenew = 1 << 5;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x37;
	}

	/* Classified ad query options */
	public static class ClassifiedQueryFlags
	{
		// Include PG ads in results
		public static final byte PG = 1 << 2;
		// Include Mature ads in results
		public static final byte Mature = 1 << 3;
		// Include Adult ads in results
		public static final byte Adult = 1 << 6;
		// Include all ads in results
		public static final byte All = PG | Mature | Adult;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x4C;
	}

	/* The For Sale flag in PlacesReplyData */
	public enum PlacesFlags
	{
		// Parcel is not listed for sale
		NotForSale(0),
		// Parcel is For Sale
		ForSale(128);

		public static PlacesFlags setValue(int value)
		{
			for (PlacesFlags e : values())
				if (e._value == value)
					return e;
			return NotForSale;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		PlacesFlags(int value)
		{
			this._value = (byte) value;
		}
	}

	// /#region Structs

	/** A classified ad on the grid */
	public final class Classified
	{
		/**
		 * UUID for this ad, useful for looking up detailed information about it
		 */
		public UUID ID;
		/** The title of this classified ad */
		public String Name;
		/** Flags that show certain options applied to the classified */
		public byte Flags;
		/** Creation date of the ad */
		public Date CreationDate;
		/** Expiration date of the ad */
		public Date ExpirationDate;
		/** Price that was paid for this ad */
		public int Price;

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/**
	 * A parcel retrieved from the dataserver such as results from the
	 * "For-Sale" listings or "Places" Search
	 */
	public final class DirectoryParcel
	{
		/**
		 * The unique dataserver parcel ID This id is used to obtain additional
		 * information from the entry by using the <see
		 * cref="ParcelManager.InfoRequest"/> method
		 */
		public UUID ID;
		/** A string containing the name of the parcel */
		public String Name;
		/**
		 * The size of the parcel This field is not returned for Places searches
		 */
		public int ActualArea;
		/**
		 * The price of the parcel This field is not returned for Places
		 * searches
		 */
		public int SalePrice;
		/** If True, this parcel is flagged to be auctioned */
		public boolean Auction;
		/** If true, this parcel is currently set for sale */
		public boolean ForSale;
		/** Parcel traffic */
		public float Dwell;

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/** An Avatar returned from the dataserver */
	public final class AgentSearchData
	{
		/**
		 * Online status of agent This field appears to be obsolete and always
		 * returns false
		 */
		public boolean Online;
		/** The agents first name */
		public String FirstName;
		/** The agents last name */
		public String LastName;
		/** The agents <see cref="UUID"/> */
		public UUID AgentID;

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/** Response to a "Groups" Search */
	public final class GroupSearchData
	{
		/** The Group ID */
		public UUID GroupID;
		/** The name of the group */
		public String GroupName;
		/** The current number of members */
		public int Members;

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/**
	 * Parcel information returned from a <see cref="StartPlacesSearch"/>
	 * request
	 * 
	 * Represents one of the following:
	 * 
	 * A parcel of land on the grid that has its Show In Search flag set A
	 * parcel of land owned by the agent making the request A parcel of land
	 * owned by a group the agent making the request is a member of
	 * 
	 * In a request for Group Land, the First record will contain an empty
	 * record
	 * 
	 * Note: This is not the same as searching the land for sale data source
	 */
	public final class PlacesSearchData
	{
		/** The ID of the Agent of Group that owns the parcel */
		public UUID OwnerID;
		/** The name */
		public String Name;
		/** The description */
		public String Desc;
		/** The Size of the parcel */
		public int ActualArea;
		/**
		 * The billable Size of the parcel, for mainland parcels this will match
		 * the ActualArea field. For Group owned land this will be 10 percent
		 * smaller than the ActualArea. For Estate land this will always be 0
		 */
		public int BillableArea;
		/** Indicates the ForSale status of the parcel */
		public PlacesFlags Flags;
		/** The Gridwide X position */
		public float GlobalX;
		/** The Gridwide Y position */
		public float GlobalY;
		/** The Z position of the parcel, or 0 if no landing point set */
		public float GlobalZ;
		/** The name of the Region the parcel is located in */
		public String SimName;
		/** The Asset ID of the parcels Snapshot texture */
		public UUID SnapshotID;
		/** The calculated visitor traffic */
		public float Dwell;
		/**
		 * The billing product SKU
		 * 
		 * Known values are: <list> <item><term>023</term><description>Mainland
		 * / Full Region</description></item>
		 * <item><term>024</term><description>Estate / Full
		 * Region</description></item> <item><term>027</term><description>Estate
		 * / Openspace</description></item>
		 * <item><term>029</term><description>Estate /
		 * Homestead</description></item>
		 * <item><term>129</term><description>Mainland / Homestead (Linden
		 * Owned)</description></item> </list>
		 */
		public String SKU;
		/** No longer used, will always be 0 */
		public int Price;

		/**
		 * Get a SL URL for the parcel
		 * 
		 * @return A string, containing a standard SLURL
		 */
		public String toSLurl()
		{
			float[] values = new float[2];
			Helpers.GlobalPosToRegionHandle(this.GlobalX, this.GlobalY, values);
			return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalZ;
		}

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/** An "Event" Listing summary */
	public final class EventsSearchData
	{
		/** The ID of the event creator */
		public UUID Owner;
		/** The name of the event */
		public String Name;
		/** The events ID */
		public int ID; /* TODO was uint */
		/** A string containing the short date/time the event will begin */
		public String Date;
		/** The event start time in Unixtime (seconds since epoch) */
		public int Time;
		/** The events maturity rating */
		public EventFlags Flags;

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	/** The details of an "Event" */
	public final class EventInfo
	{
		/** The events ID */
		public int ID; /* TODO was uint */
		/** The ID of the event creator */
		public UUID Creator;
		/** The name of the event */
		public String Name;
		/** The category */
		public EventCategories Category;
		/** The events description */
		public String Desc;
		/** The short date/time the event will begin */
		public String Date;
		/** The event start time in Unixtime (seconds since epoch) UTC adjusted */
		public int DateUTC; /* TODO was uint */
		/** The length of the event in minutes */
		public int Duration; /* TODO was uint */
		/** 0 if no cover charge applies */
		public int Cover; /* TODO was uint */
		/** The cover charge amount in L$ if applicable */
		public int Amount; /* TODO was uint */
		/** The name of the region where the event is being held */
		public String SimName;
		/** The gridwide location of the event */
		public Vector3d GlobalPos;
		/** The maturity rating */
		public EventFlags Flags;

		/**
		 * Get a SL URL for the parcel where the event is hosted
		 * 
		 * @return A string, containing a standard SLURL
		 */
		public String toSLurl()
		{
			float[] values = new float[2];
			Helpers.GlobalPosToRegionHandle((float) this.GlobalPos.X, (float) this.GlobalPos.Y, values);
			return "secondlife://" + this.SimName + "/" + values[0] + "/" + values[1] + "/" + this.GlobalPos.Z;
		}

		/**
		 * Print the struct data as a string
		 * 
		 * @return A string containing the field name, and field value
		 */
		@Override
		public String toString()
		{
			return Helpers.StructToString(this);
		}
	}

	// /#region callback handlers
	public CallbackHandler<EventInfoReplyCallbackArgs> OnEventInfo;
	public CallbackHandler<DirEventsReplyCallbackArgs> OnDirEvents;
	public CallbackHandler<PlacesReplyCallbackArgs> OnPlaces;
	public CallbackHandler<DirPlacesReplyCallbackArgs> OnDirPlaces;
	public CallbackHandler<DirClassifiedsReplyCallbackArgs> OnDirClassifieds;
	public CallbackHandler<DirGroupsReplyCallbackArgs> OnDirGroups;
	public CallbackHandler<DirPeopleReplyCallbackArgs> OnDirPeople;
	public CallbackHandler<DirLandReplyCallbackArgs> OnDirLand;

	// /#region Private Members
	private GridClient Client;

	// /#region Constructors

	/**
	 * Constructs a new instance of the DirectoryManager class
	 * 
	 * @param client
	 *            An instance of GridClient
	 */
	public DirectoryManager(GridClient client)
	{
		Client = client;

		Client.Network.RegisterCallback(PacketType.DirClassifiedReply, this);
		// Deprecated, replies come in over capabilities
		Client.Network.RegisterCallback(PacketType.DirLandReply, this);
		Client.Network.RegisterCallback(CapsEventType.DirLandReply, this);
		Client.Network.RegisterCallback(PacketType.DirPeopleReply, this);
		Client.Network.RegisterCallback(PacketType.DirGroupsReply, this);
		// Deprecated as of viewer 1.2.3
		Client.Network.RegisterCallback(PacketType.PlacesReply, this);
		Client.Network.RegisterCallback(CapsEventType.PlacesReply, this);
		Client.Network.RegisterCallback(PacketType.DirEventsReply, this);
		Client.Network.RegisterCallback(PacketType.EventInfoReply, this);
		Client.Network.RegisterCallback(PacketType.DirPlacesReply, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case DirClassifiedReply:
				DirClassifiedReplyHandler(packet, simulator);
				break;
			// Deprecated, replies come in over capabilities
			case DirLandReply:
				DirLandReplyHandler(packet, simulator);
				break;
			case DirPeopleReply:
				DirPeopleReplyHandler(packet, simulator);
				break;
			case DirGroupsReply:
				DirGroupsReplyHandler(packet, simulator);
				break;
			// Deprecated as of viewer 1.2.3
			case PlacesReply:
				PlacesReplyHandler(packet, simulator);
				break;
			case DirEventsReply:
				EventsReplyHandler(packet, simulator);
				break;
			case EventInfoReply:
				EventInfoReplyHandler(packet, simulator);
				break;
			case DirPlacesReply:
				DirPlacesReplyHandler(packet, simulator);
				break;
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator) throws Exception
	{
		switch (message.getType())
		{
			case DirLandReply:
				DirLandReplyEventHandler(message, simulator);
				break;
			case PlacesReply:
				PlacesReplyEventHandler(message, simulator);
				break;
		}
	}

	// /#region Public Methods
	/** Obsoleted due to new Adult search option */
	@Deprecated
	public final UUID StartClassifiedSearch(String searchText, ClassifiedCategories category, boolean mature)
	{
		return UUID.Zero;
	}

	/**
	 * Query the data server for a list of classified ads containing the
	 * specified string. Defaults to searching for classified placed in any
	 * category, and includes PG, Adult and Mature results.
	 * 
	 * Responses are sent 16 per response packet, there is no way to know how
	 * many results a query reply will contain however assuming the reply
	 * packets arrived ordered, a response with less than 16 entries would
	 * indicate all results have been received
	 * 
	 * The <see cref="OnClassifiedReply"/> event is raised when a response is
	 * received from the simulator
	 * 
	 * @param searchText
	 *            A string containing a list of keywords to search for
	 * @return A UUID to correlate the results when the <see
	 *         cref="OnClassifiedReply"/> event is raised
	 * @throws Exception
	 */
	public final UUID StartClassifiedSearch(String searchText) throws Exception
	{
		return StartClassifiedSearch(searchText, ClassifiedCategories.Any, ClassifiedQueryFlags.All);
	}

	/**
	 * Query the data server for a list of classified ads which contain
	 * specified keywords (Overload)
	 * 
	 * The <see cref="OnClassifiedReply"/> event is raised when a response is
	 * received from the simulator
	 * 
	 * @param searchText
	 *            A string containing a list of keywords to search for
	 * @param category
	 *            The category to search
	 * @param queryFlags
	 *            A set of flags which can be ORed to modify query options such
	 *            as classified maturity rating.
	 * @return A UUID to correlate the results when the <see
	 *         cref="OnClassifiedReply"/> event is raised
	 * @throws Exception
	 * 
	 *             <example> Search classified ads containing the key words
	 *             "foo" and "bar" in the "Any" category that are either PG or
	 *             Mature <code>
	 *  UUID searchID = StartClassifiedSearch("foo bar", ClassifiedCategories.Any, ClassifiedQueryFlags.PG | ClassifiedQueryFlags.Mature);
	 *  </code> </example>
	 * 
	 *             Responses are sent 16 at a time, there is no way to know how
	 *             many results a query reply will contain however assuming the
	 *             reply packets arrived ordered, a response with less than 16
	 *             entries would indicate all results have been received
	 */
	public final UUID StartClassifiedSearch(String searchText, ClassifiedCategories category, byte queryFlags)
			throws Exception
	{
		DirClassifiedQueryPacket query = new DirClassifiedQueryPacket();
		UUID queryID = UUID.GenerateUUID();

		query.AgentData.AgentID = Client.Self.getAgentID();
		query.AgentData.SessionID = Client.Self.getSessionID();

		query.QueryData.Category = category.getValue();
		query.QueryData.QueryFlags = queryFlags;
		query.QueryData.QueryID = queryID;
		query.QueryData.setQueryText(Helpers.StringToBytes(searchText));

		Client.Network.SendPacket(query);

		return queryID;
	}

	/**
	 * Starts search for places (Overloaded)
	 * 
	 * The <see cref="OnDirPlacesReply"/> event is raised when a response is
	 * received from the simulator
	 * 
	 * @param searchText
	 *            Search text
	 * @param queryStart
	 *            Each request is limited to 100 places being returned. To get
	 *            the first 100 result entries of a request use 0, from 100-199
	 *            use 1, 200-299 use 2, etc.
	 * @return A UUID to correlate the results when the <see
	 *         cref="OnDirPlacesReply"/> event is raised
	 * @throws Exception
	 */
	public final UUID StartDirPlacesSearch(String searchText, int queryStart) throws Exception
	{
		int flags = DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartDirPlacesSearch(searchText, flags, ParcelCategory.Any, queryStart);
	}

	/**
	 * Queries the dataserver for parcels of land which are flagged to be shown
	 * in search
	 * 
	 * The <see cref="OnDirPlacesReply"/> event is raised when a response is
	 * received from the simulator
	 * 
	 * @param searchText
	 *            A string containing a list of keywords to search for separated
	 *            by a space character
	 * @param queryFlags
	 *            A set of flags which can be ORed to modify query options such
	 *            as classified maturity rating.
	 * @param category
	 *            The category to search
	 * @param queryStart
	 *            Each request is limited to 100 places being returned. To get
	 *            the first 100 result entries of a request use 0, from 100-199
	 *            use 1, 200-299 use 2, etc.
	 * @return A UUID to correlate the results when the <see
	 *         cref="OnDirPlacesReply"/> event is raised
	 * @throws Exception
	 * 
	 *             <example> Search places containing the key words "foo" and
	 *             "bar" in the "Any" category that are either PG or Adult
	 *             <code>
	 *  UUID searchID = StartDirPlacesSearch("foo bar", DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeAdult, ParcelCategory.Any, 0);
	 *  </code> </example>
	 * 
	 *             Additional information on the results can be obtained by
	 *             using the ParcelManager.InfoRequest method
	 */
	public final UUID StartDirPlacesSearch(String searchText, int queryFlags, ParcelCategory category, int queryStart)
			throws Exception
	{
		DirPlacesQueryPacket query = new DirPlacesQueryPacket();

		query.AgentData.AgentID = Client.Self.getAgentID();
		query.AgentData.SessionID = Client.Self.getSessionID();

		query.QueryData.Category = (byte) category.ordinal();
		query.QueryData.QueryFlags = queryFlags;

		query.QueryData.QueryID = UUID.GenerateUUID();
		query.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		query.QueryData.QueryStart = queryStart;
		query.QueryData.setSimName(Helpers.StringToBytes(Helpers.EmptyString));

		Client.Network.SendPacket(query);

		return query.QueryData.QueryID;

	}

	/**
	 * Starts a search for land sales using the directory
	 * 
	 * The <see cref="OnDirLandReply"/> event is raised when a response is
	 * received from the simulator
	 * 
	 * @param typeFlags
	 *            What type of land to search for. Auction, estate, mainland,
	 *            "first land", etc
	 * @throws Exception
	 * 
	 *             The OnDirLandReply event handler must be registered before
	 *             calling this function. There is no way to determine how many
	 *             results will be returned, or how many times the callback will
	 *             be fired other than you won't get more than 100 total parcels
	 *             from each query.
	 */
	public final void StartLandSearch(byte typeFlags) throws Exception
	{
		int flags = DirFindFlags.SortAsc | DirFindFlags.PerMeterSort;
		StartLandSearch(flags, typeFlags, 0, 0, 0);
	}

	/**
	 * Starts a search for land sales using the directory
	 * 
	 * The {@link OnDirLandReply} event is raised when a response is received
	 * from the simulator
	 * 
	 * @param typeFlags
	 *            What type of land to search for. Auction, estate, mainland,
	 *            "first land", etc
	 * @param priceLimit
	 *            Maximum price to search for
	 * @param areaLimit
	 *            Maximum area to search for
	 * @param queryStart
	 *            Each request is limited to 100 parcels being returned. To get
	 *            the first 100 parcels of a request use 0, from 100-199 use 1,
	 *            200-299 use 2, etc. The OnDirLandReply event handler must be
	 *            registered before calling this function. There is no way to
	 *            determine how many results will be returned, or how many times
	 *            the callback will be fired other than you won't get more than
	 *            100 total parcels from each query.
	 * @throws Exception
	 */
	public final void StartLandSearch(byte typeFlags, int priceLimit, int areaLimit, int queryStart) throws Exception
	{
		int flags = DirFindFlags.SortAsc | DirFindFlags.PerMeterSort | DirFindFlags.LimitByPrice
				| DirFindFlags.LimitByArea;
		StartLandSearch(flags, typeFlags, priceLimit, areaLimit, queryStart);
	}

	/**
	 * Send a request to the data server for land sales listings
	 * 
	 * @param findFlags
	 *            Flags sent to specify query options
	 * 
	 *            Available flags: Specify the parcel rating with one or more of
	 *            the following: IncludePG IncludeMature IncludeAdult
	 * 
	 *            Specify the field to pre sort the results with ONLY ONE of the
	 *            following: PerMeterSort NameSort AreaSort PricesSort
	 * 
	 *            Specify the order the results are returned in, if not
	 *            specified the results are pre sorted in a Descending Order
	 *            SortAsc
	 * 
	 *            Specify additional filters to limit the results with one or
	 *            both of the following: LimitByPrice LimitByArea
	 * 
	 *            Flags can be combined by separating them with the or (|)
	 *            operator
	 * 
	 *            Additional details can be found in <see cref="DirFindFlags"/>
	 * 
	 * @param typeFlags
	 *            What type of land to search for. Auction, Estate or Mainland
	 * @param priceLimit
	 *            Maximum price to search for when the DirFindFlags.LimitByPrice
	 *            flag is specified in findFlags
	 * @param areaLimit
	 *            Maximum area to search for when the DirFindFlags.LimitByArea
	 *            flag is specified in findFlags
	 * @param queryStart
	 *            Each request is limited to 100 parcels being returned. To get
	 *            the first 100 parcels of a request use 0, from 100-199 use
	 *            100, 200-299 use 200, etc.
	 * @throws Exception
	 * 
	 *             The {@link OnDirLandReply} event will be raised with the
	 *             response from the simulator
	 * 
	 *             There is no way to determine how many results will be
	 *             returned, or how many times the callback will be fired other
	 *             than you won't get more than 100 total parcels from each
	 *             reply.
	 * 
	 *             Any land set for sale to either anybody or specific to the
	 *             connected agent will be included in the results if the land
	 *             is included in the query. <example> <code>
	 *  // request all mainland, any maturity rating that is larger than 512 sq.m
	 *  int flags = DirFindFlags.SortAsc.ordinal() | DirFindFlags.PerMeterSort.ordinal() |
	 *              DirFindFlags.LimitByArea.ordinal() | DirFindFlags.IncludePG.ordinal() |
	 *              DirFindFlags.IncludeMature.ordinal() | DirFindFlags.IncludeAdult.ordinal();
	 *  StartLandSearch(DirFindFlags.convert(flags), SearchTypeFlags.Mainland, 0, 512, 0);
	 *  </code></example>
	 */
	public final void StartLandSearch(int findFlags, byte typeFlags, int priceLimit, int areaLimit, int queryStart)
			throws Exception
	{
		DirLandQueryPacket query = new DirLandQueryPacket();
		query.AgentData.AgentID = Client.Self.getAgentID();
		query.AgentData.SessionID = Client.Self.getSessionID();
		query.QueryData.Area = areaLimit;
		query.QueryData.Price = priceLimit;
		query.QueryData.QueryStart = queryStart;
		query.QueryData.SearchType = typeFlags;
		query.QueryData.QueryFlags = findFlags;
		query.QueryData.QueryID = UUID.GenerateUUID();

		Client.Network.SendPacket(query);
	}

	/**
	 * Search for Groups
	 * 
	 * @param searchText
	 *            The name or portion of the name of the group you wish to
	 *            search for
	 * @param queryStart
	 *            Start from the match number
	 * @return
	 * @throws Exception
	 */
	public final UUID StartGroupSearch(String searchText, int queryStart) throws Exception
	{
		int flags = DirFindFlags.Groups | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartGroupSearch(searchText, queryStart, flags);
	}

	/**
	 * Search for Groups
	 * 
	 * @param searchText
	 *            The name or portion of the name of the group you wish to
	 *            search for
	 * @param queryStart
	 *            Start from the match number
	 * @param flags
	 *            Search flags
	 * @return
	 * @throws Exception
	 */
	public final UUID StartGroupSearch(String searchText, int queryStart, int flags) throws Exception
	{
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = Client.Self.getAgentID();
		find.AgentData.SessionID = Client.Self.getSessionID();
		find.QueryData.QueryFlags = flags;
		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryID = UUID.GenerateUUID();
		find.QueryData.QueryStart = queryStart;

		Client.Network.SendPacket(find);

		return find.QueryData.QueryID;
	}

	/**
	 * Search the People directory for other avatars
	 * 
	 * @param searchText
	 *            The name or portion of the name of the avatar you wish to
	 *            search for
	 * @param queryStart
	 * @return
	 * @throws Exception
	 */
	public final UUID StartPeopleSearch(String searchText, int queryStart) throws Exception
	{
		UUID uuid = UUID.GenerateUUID();
		StartPeopleSearch(searchText, queryStart, uuid);
		return uuid;
	}

	/**
	 * Search the People directory for other avatars
	 * 
	 * @param searchText
	 *            The name or portion of the name of the avatar you wish to
	 *            search for
	 * @param queryStart
	 * @return
	 * @throws Exception
	 */
	public final void StartPeopleSearch(String searchText, int queryStart, UUID uuid) throws Exception
	{
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = Client.Self.getAgentID();
		find.AgentData.SessionID = Client.Self.getSessionID();
		find.QueryData.QueryFlags = DirFindFlags.People;
		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryID = uuid;
		find.QueryData.QueryStart = queryStart;

		Client.Network.SendPacket(find);
	}

	/**
	 * Search Places for parcels of land you personally own
	 * 
	 * @return
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch() throws Exception
	{
		return StartPlacesSearch(DirFindFlags.AgentOwned, ParcelCategory.Any, Helpers.EmptyString, Helpers.EmptyString,
				UUID.Zero, UUID.GenerateUUID());
	}

	/**
	 * Searches Places for land owned by the specified group
	 * 
	 * @param groupID
	 *            ID of the group you want to recieve land list for (You must be
	 *            a member of the group)
	 * @return Transaction (Query) ID which can be associated with results from
	 *         your request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(UUID groupID) throws Exception
	{
		return StartPlacesSearch(DirFindFlags.GroupOwned, ParcelCategory.Any, Helpers.EmptyString, Helpers.EmptyString,
				groupID, UUID.GenerateUUID());
	}

	/**
	 * Search the Places directory for parcels that are listed in search and
	 * contain the specified keywords
	 * 
	 * @param searchText
	 *            A string containing the keywords to search for
	 * @return Transaction (Query) ID which can be associated with results from
	 *         your request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(String searchText) throws Exception
	{
		int flags = DirFindFlags.DwellSort | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartPlacesSearch(flags, ParcelCategory.Any, searchText, Helpers.EmptyString, UUID.Zero, new UUID());
	}

	/**
	 * Search Places - All Options
	 * 
	 * @param findFlags
	 *            One of the Values from the DirFindFlags struct, ie:
	 *            AgentOwned, GroupOwned, etc.
	 * @param searchCategory
	 *            One of the values from the SearchCategory Struct, ie: Any,
	 *            Linden, Newcomer
	 * @param searchText
	 *            A string containing a list of keywords to search for separated
	 *            by a space character
	 * @param simulatorName
	 *            String Simulator Name to search in
	 * @param groupID
	 *            LLUID of group you want to recieve results for
	 * @param transactionID
	 *            Transaction (Query) ID which can be associated with results
	 *            from your request.
	 * @return Transaction (Query) ID which can be associated with results from
	 *         your request.
	 * @throws Exception
	 */
	public final UUID StartPlacesSearch(int findFlags, ParcelCategory searchCategory, String searchText,
			String simulatorName, UUID groupID, UUID transactionID) throws Exception
	{
		PlacesQueryPacket find = new PlacesQueryPacket();
		find.AgentData.AgentID = Client.Self.getAgentID();
		find.AgentData.SessionID = Client.Self.getSessionID();
		find.AgentData.QueryID = groupID;

		find.TransactionID = transactionID;

		find.QueryData.setQueryText(Helpers.StringToBytes(searchText));
		find.QueryData.QueryFlags = findFlags;
		find.QueryData.Category = (byte) searchCategory.ordinal();
		find.QueryData.setSimName(Helpers.StringToBytes(simulatorName));

		Client.Network.SendPacket(find);
		return transactionID;
	}

	/**
	 * Search All Events with specifid searchText in all categories, includes
	 * PG, Mature and Adult
	 * 
	 * @param searchText
	 *            A string containing a list of keywords to search for separated
	 *            by a space character
	 * @param queryStart
	 *            Each request is limited to 100 entries being returned. To get
	 *            the first group of entries of a request use 0, from 100-199
	 *            use 100, 200-299 use 200, etc.
	 * @return UUID of query to correlate results in callback.
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public UUID StartEventsSearch(string searchText, uint
	// queryStart)
	public final UUID StartEventsSearch(String searchText, int queryStart) throws Exception
	{
		int flags = DirFindFlags.DateEvents | DirFindFlags.IncludePG | DirFindFlags.IncludeMature
				| DirFindFlags.IncludeAdult;
		return StartEventsSearch(searchText, flags, "u", queryStart, EventCategories.All);
	}

	/**
	 * Search Events
	 * 
	 * @param searchText
	 *            A string containing a list of keywords to search for separated
	 *            by a space character
	 * @param queryFlags
	 *            One or more of the following flags: DateEvents, IncludePG,
	 *            IncludeMature, IncludeAdult from the <see
	 *            cref="DirFindFlags"/> Enum Multiple flags can be combined by
	 *            separating the flags with the or (|) operator
	 * @param eventDay
	 *            "u" for in-progress and upcoming events, -or- number of days
	 *            since/until event is scheduled For example "0" = Today, "1" =
	 *            tomorrow, "2" = following day, "-1" = yesterday, etc.
	 * @param queryStart
	 *            Each request is limited to 100 entries being returned. To get
	 *            the first group of entries of a request use 0, from 100-199
	 *            use 100, 200-299 use 200, etc.
	 * @param category
	 *            EventCategory event is listed under.
	 * @return UUID of query to correlate results in callback.
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public UUID StartEventsSearch(string searchText,
	// DirFindFlags queryFlags, string eventDay, uint queryStart,
	// EventCategories category)
	public final UUID StartEventsSearch(String searchText, int queryFlags, String eventDay, int queryStart,
			EventCategories category) throws Exception, Exception
	{
		DirFindQueryPacket find = new DirFindQueryPacket();
		find.AgentData.AgentID = Client.Self.getAgentID();
		find.AgentData.SessionID = Client.Self.getSessionID();

		find.QueryData.QueryID = UUID.GenerateUUID();
		find.QueryData.setQueryText(Helpers.StringToBytes(eventDay + "|" + category.ordinal() + "|" + searchText));
		find.QueryData.QueryFlags = queryFlags;
		find.QueryData.QueryStart = queryStart;

		Client.Network.SendPacket(find);
		return find.QueryData.QueryID;
	}

	/**
	 * Requests Event Details
	 * 
	 * @param eventID
	 *            ID of Event returned from the <see cref="StartEventsSearch"/>
	 *            method
	 * @throws Exception
	 */
	// TODO ORIGINAL LINE: public void EventInfoRequest(uint eventID)
	public final void EventInfoRequest(int eventID) throws Exception
	{
		EventInfoRequestPacket find = new EventInfoRequestPacket();
		find.AgentData.AgentID = Client.Self.getAgentID();
		find.AgentData.SessionID = Client.Self.getSessionID();

		find.EventID = eventID;

		Client.Network.SendPacket(find);
	}

	// /#region Blocking Functions

	@Deprecated
	public final ArrayList<AgentSearchData> PeopleSearch(DirFindFlags findFlags, String searchText, int queryStart,
			int timeoutMS) throws Exception
	{
		class DirPeopleCallbackHandler implements Callback<DirPeopleReplyCallbackArgs>
		{
			private ArrayList<AgentSearchData> people = null;
			private UUID uuid;

			public ArrayList<AgentSearchData> getPeople()
			{
				return people;
			}

			@Override
			public boolean callback(DirPeopleReplyCallbackArgs e)
			{
				if (uuid == e.getQueryID())
				{
					people = e.getMatchedPeople();
				}
				return false;
			}

			public DirPeopleCallbackHandler(UUID uuid)
			{
				this.uuid = uuid;
			}
		}

		DirPeopleCallbackHandler callback = new DirPeopleCallbackHandler(UUID.GenerateUUID());

		OnDirPeople.add(callback);
		StartPeopleSearch(searchText, queryStart);
		callback.wait(timeoutMS);
		OnDirPeople.remove(callback);

		return callback.getPeople();
	}

	// /#region Packet Handlers

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The simulator from which this packet originates
	 * @throws UnsupportedEncodingException
	 */
	protected final void DirClassifiedReplyHandler(Packet packet, Simulator simulator)
			throws UnsupportedEncodingException
	{
		if (OnDirClassifieds != null)
		{
			DirClassifiedReplyPacket reply = (DirClassifiedReplyPacket) packet;
			ArrayList<Classified> classifieds = new ArrayList<Classified>();

			for (DirClassifiedReplyPacket.QueryRepliesBlock block : reply.QueryReplies)
			{
				Classified classified = new Classified();

				classified.CreationDate = Helpers.UnixTimeToDateTime(block.CreationDate);
				classified.ExpirationDate = Helpers.UnixTimeToDateTime(block.ExpirationDate);
				classified.Flags = ClassifiedFlags.setValue(block.ClassifiedFlags);
				classified.ID = block.ClassifiedID;
				classified.Name = Helpers.BytesToString(block.getName());
				classified.Price = block.PriceForListing;

				classifieds.add(classified);
			}
			OnDirClassifieds.dispatch(new DirClassifiedsReplyCallbackArgs(classifieds));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws UnsupportedEncodingException
	 */
	protected final void DirLandReplyHandler(Packet packet, Simulator simulator) throws UnsupportedEncodingException
	{
		if (OnDirLand != null)
		{
			ArrayList<DirectoryParcel> parcelsForSale = new ArrayList<DirectoryParcel>();
			DirLandReplyPacket reply = (DirLandReplyPacket) packet;

			for (DirLandReplyPacket.QueryRepliesBlock block : reply.QueryReplies)
			{
				DirectoryParcel dirParcel = new DirectoryParcel();

				dirParcel.ActualArea = block.ActualArea;
				dirParcel.ID = block.ParcelID;
				dirParcel.Name = Helpers.BytesToString(block.getName());
				dirParcel.SalePrice = block.SalePrice;
				dirParcel.Auction = block.Auction;
				dirParcel.ForSale = block.ForSale;

				parcelsForSale.add(dirParcel);
			}
			OnDirLand.dispatch(new DirLandReplyCallbackArgs(parcelsForSale));
		}
	}

	/**
	 * Process an incoming <see cref="DirLandReplyMessage"/> event message
	 * 
	 * @param message
	 *            The <see cref="DirLandReplyMessage"/> event message containing
	 *            the data
	 * @param simulator
	 *            The simulator the message originated from
	 */
	protected final void DirLandReplyEventHandler(IMessage message, Simulator simulator)
	{
		if (OnDirLand != null)
		{
			ArrayList<DirectoryParcel> parcelsForSale = new ArrayList<DirectoryParcel>();
			DirLandReplyMessage reply = (DirLandReplyMessage) message;
			for (DirLandReplyMessage.QueryReply block : reply.QueryReplies)
			{
				DirectoryParcel dirParcel = new DirectoryParcel();

				dirParcel.ActualArea = block.ActualArea;
				dirParcel.ID = block.ParcelID;
				dirParcel.Name = block.Name;
				dirParcel.SalePrice = block.SalePrice;
				dirParcel.Auction = block.Auction;
				dirParcel.ForSale = block.ForSale;

				parcelsForSale.add(dirParcel);
			}
			OnDirLand.dispatch(new DirLandReplyCallbackArgs(parcelsForSale));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	protected final void DirPeopleReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnDirPeople != null)
		{
			DirPeopleReplyPacket peopleReply = (DirPeopleReplyPacket) ((packet instanceof DirPeopleReplyPacket) ? packet
					: null);
			ArrayList<AgentSearchData> matches = new ArrayList<AgentSearchData>(peopleReply.QueryReplies.length);

			for (DirPeopleReplyPacket.QueryRepliesBlock reply : peopleReply.QueryReplies)
			{
				AgentSearchData searchData = new AgentSearchData();
				searchData.Online = reply.Online;
				searchData.FirstName = Helpers.BytesToString(reply.getFirstName());
				searchData.LastName = Helpers.BytesToString(reply.getLastName());
				searchData.AgentID = reply.AgentID;
				matches.add(searchData);
			}
			OnDirPeople.dispatch(new DirPeopleReplyCallbackArgs(peopleReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	protected final void DirGroupsReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnDirGroups != null)
		{
			DirGroupsReplyPacket groupsReply = (DirGroupsReplyPacket) packet;
			ArrayList<GroupSearchData> matches = new ArrayList<GroupSearchData>(groupsReply.QueryReplies.length);
			for (DirGroupsReplyPacket.QueryRepliesBlock reply : groupsReply.QueryReplies)
			{
				GroupSearchData groupsData = new GroupSearchData();
				groupsData.GroupID = reply.GroupID;
				groupsData.GroupName = Helpers.BytesToString(reply.getGroupName());
				groupsData.Members = reply.Members;
				matches.add(groupsData);
			}
			OnDirGroups.dispatch(new DirGroupsReplyCallbackArgs(groupsReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming <see cref="PlacesReplyMessage"/> event message
	 * 
	 * @param message
	 *            The <see cref="PlacesReplyMessage"/> event message containing
	 *            the data
	 * @param simulator
	 *            The simulator the message originated from
	 */
	protected final void PlacesReplyEventHandler(IMessage message, Simulator simulator)
	{
		if (OnPlaces != null)
		{
			ArrayList<PlacesSearchData> places = new ArrayList<PlacesSearchData>();
			PlacesReplyMessage replyMessage = (PlacesReplyMessage) message;
			for (PlacesReplyMessage.QueryData block : replyMessage.QueryDataBlocks)
			{
				PlacesSearchData place = new PlacesSearchData();
				place.ActualArea = block.ActualArea;
				place.BillableArea = block.BillableArea;
				place.Desc = block.Description;
				place.Dwell = block.Dwell;
				place.Flags = PlacesFlags.setValue(block.Flags);
				place.GlobalX = block.GlobalX;
				place.GlobalY = block.GlobalY;
				place.GlobalZ = block.GlobalZ;
				place.Name = block.Name;
				place.OwnerID = block.OwnerID;
				place.Price = block.Price;
				place.SimName = block.SimName;
				place.SnapshotID = block.SnapShotID;
				place.SKU = block.ProductSku;
				places.add(place);
			}
			OnPlaces.dispatch(new PlacesReplyCallbackArgs(replyMessage.QueryID, places));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	protected final void PlacesReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnPlaces != null)
		{
			PlacesReplyPacket placesReply = (PlacesReplyPacket) ((packet instanceof PlacesReplyPacket) ? packet : null);
			ArrayList<PlacesSearchData> places = new ArrayList<PlacesSearchData>();

			for (PlacesReplyPacket.QueryDataBlock block : placesReply.QueryData)
			{
				PlacesSearchData place = new PlacesSearchData();
				place.OwnerID = block.OwnerID;
				place.Name = Helpers.BytesToString(block.getName());
				place.Desc = Helpers.BytesToString(block.getDesc());
				place.ActualArea = block.ActualArea;
				place.BillableArea = block.BillableArea;
				place.Flags = PlacesFlags.setValue(block.Flags);
				place.GlobalX = block.GlobalX;
				place.GlobalY = block.GlobalY;
				place.GlobalZ = block.GlobalZ;
				place.SimName = Helpers.BytesToString(block.getSimName());
				place.SnapshotID = block.SnapshotID;
				place.Dwell = block.Dwell;
				place.Price = block.Price;

				places.add(place);
			}
			OnPlaces.dispatch(new PlacesReplyCallbackArgs(placesReply.TransactionID, places));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	protected final void EventsReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnDirEvents != null)
		{
			DirEventsReplyPacket eventsReply = (DirEventsReplyPacket) packet;
			ArrayList<EventsSearchData> matches = new ArrayList<EventsSearchData>(eventsReply.QueryReplies.length);

			for (DirEventsReplyPacket.QueryRepliesBlock reply : eventsReply.QueryReplies)
			{
				EventsSearchData eventsData = new EventsSearchData();
				eventsData.Owner = reply.OwnerID;
				eventsData.Name = Helpers.BytesToString(reply.getName());
				eventsData.ID = reply.EventID;
				eventsData.Date = Helpers.BytesToString(reply.getDate());
				eventsData.Time = reply.UnixTime;
				eventsData.Flags = EventFlags.setValue(reply.EventFlags);
				matches.add(eventsData);
			}
			OnDirEvents.dispatch(new DirEventsReplyCallbackArgs(eventsReply.QueryID, matches));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The data packet
	 * @param simulator
	 *            The simulator from which the packet originates
	 * @throws Exception
	 */
	protected final void EventInfoReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnEventInfo != null)
		{
			EventInfoReplyPacket eventReply = (EventInfoReplyPacket) packet;
			EventInfo evinfo = new EventInfo();
			evinfo.ID = eventReply.EventData.EventID;
			evinfo.Name = Helpers.BytesToString(eventReply.EventData.getName());
			evinfo.Desc = Helpers.BytesToString(eventReply.EventData.getDesc());
			evinfo.Amount = eventReply.EventData.Amount;
			evinfo.Category = EventCategories.valueOf(Helpers.BytesToString(eventReply.EventData.getCategory()));
			evinfo.Cover = eventReply.EventData.Cover;
			evinfo.Creator = new UUID(eventReply.EventData.getCreator());
			evinfo.Date = Helpers.BytesToString(eventReply.EventData.getDate());
			evinfo.DateUTC = eventReply.EventData.DateUTC;
			evinfo.Duration = eventReply.EventData.Duration;
			evinfo.Flags = EventFlags.setValue(eventReply.EventData.EventFlags);
			evinfo.SimName = Helpers.BytesToString(eventReply.EventData.getSimName());
			evinfo.GlobalPos = eventReply.EventData.GlobalPos;

			OnEventInfo.dispatch(new EventInfoReplyCallbackArgs(evinfo));
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
	protected final void DirPlacesReplyHandler(Packet packet, Simulator simulator) throws Exception
	{
		if (OnDirPlaces != null)
		{
			DirPlacesReplyPacket reply = (DirPlacesReplyPacket) packet;
			ArrayList<DirectoryParcel> result = new ArrayList<DirectoryParcel>();

			for (QueryRepliesBlock block : reply.QueryReplies)
			{
				DirectoryParcel p = new DirectoryParcel();

				p.ID = block.ParcelID;
				p.Name = Helpers.BytesToString(block.getName());
				p.Dwell = block.Dwell;
				p.Auction = block.Auction;
				p.ForSale = block.ForSale;

				result.add(p);
			}

			OnDirPlaces.dispatch(new DirPlacesReplyCallbackArgs(reply.QueryID[0], result));
		}
	}

	// /#region DirectoryManager EventArgs Classes

	/**
	 * Contains the Event data returned from the data server from an
	 * EventInfoRequest
	 */
	public class EventInfoReplyCallbackArgs implements CallbackArgs
	{
		private final DirectoryManager.EventInfo m_MatchedEvent;

		/** A single EventInfo object containing the details of an event */
		public final DirectoryManager.EventInfo getMatchedEvent()
		{
			return m_MatchedEvent;
		}

		/**
		 * Construct a new instance of the EventInfoReplyEventArgs class
		 * 
		 * @param matchedEvent
		 *            A single EventInfo object containing the details of an
		 *            event
		 */
		public EventInfoReplyCallbackArgs(DirectoryManager.EventInfo matchedEvent)
		{
			this.m_MatchedEvent = matchedEvent;
		}
	}

	/** Contains the "Event" detail data returned from the data server */
	public class DirEventsReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_QueryID;

		/** The ID returned by <see cref="DirectoryManager.StartEventsSearch"/> */
		public final UUID getQueryID()
		{
			return m_QueryID;
		}

		private final ArrayList<DirectoryManager.EventsSearchData> m_matchedEvents;

		/** A list of "Events" returned by the data server */
		public final ArrayList<DirectoryManager.EventsSearchData> getMatchedEvents()
		{
			return m_matchedEvents;
		}

		/**
		 * Construct a new instance of the DirEventsReplyEventArgs class
		 * 
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see
		 *            cref="StartEventsSearch"/> method
		 * @param matchedEvents
		 *            A list containing the "Events" returned by the search
		 *            query
		 */
		public DirEventsReplyCallbackArgs(UUID queryID, ArrayList<DirectoryManager.EventsSearchData> matchedEvents)
		{
			this.m_QueryID = queryID;
			this.m_matchedEvents = matchedEvents;
		}
	}

	// Contains the "Event" list data returned from the data server
	public class PlacesReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartPlacesSearch"/>
		public final UUID getQueryID()
		{
			return m_QueryID;
		}

		private final ArrayList<DirectoryManager.PlacesSearchData> m_MatchedPlaces;

		// A list of "Places" returned by the data server
		public final ArrayList<DirectoryManager.PlacesSearchData> getMatchedPlaces()
		{
			return m_MatchedPlaces;
		}

		/**
		 * Construct a new instance of PlacesReplyEventArgs class
		 * 
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see
		 *            cref="StartPlacesSearch"/> method
		 * @param matchedPlaces
		 *            A list containing the "Places" returned by the data server
		 *            query
		 */
		public PlacesReplyCallbackArgs(UUID queryID, ArrayList<DirectoryManager.PlacesSearchData> matchedPlaces)
		{
			this.m_QueryID = queryID;
			this.m_MatchedPlaces = matchedPlaces;
		}
	}

	// Contains the places data returned from the data server
	public class DirPlacesReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_QueryID;

		// The ID returned by <see
		// cref="DirectoryManager.StartDirPlacesSearch"/>
		public final UUID getQueryID()
		{
			return m_QueryID;
		}

		private final ArrayList<DirectoryManager.DirectoryParcel> m_MatchedParcels;

		// A list containing Places data returned by the data server
		public final ArrayList<DirectoryManager.DirectoryParcel> getMatchedParcels()
		{
			return m_MatchedParcels;
		}

		/**
		 * Construct a new instance of the DirPlacesReplyEventArgs class
		 * 
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see
		 *            cref="StartDirPlacesSearch"/> method
		 * @param matchedParcels
		 *            A list containing land data returned by the data server
		 */
		public DirPlacesReplyCallbackArgs(UUID queryID, ArrayList<DirectoryManager.DirectoryParcel> matchedParcels)
		{
			this.m_QueryID = queryID;
			this.m_MatchedParcels = matchedParcels;
		}
	}

	// Contains the classified data returned from the data server
	public class DirClassifiedsReplyCallbackArgs implements CallbackArgs
	{
		private final ArrayList<DirectoryManager.Classified> m_Classifieds;

		// A list containing Classified Ads returned by the data server
		public final ArrayList<DirectoryManager.Classified> getClassifieds()
		{
			return m_Classifieds;
		}

		/**
		 * Construct a new instance of the DirClassifiedsReplyEventArgs class
		 * 
		 * @param classifieds
		 *            A list of classified ad data returned from the data server
		 */
		public DirClassifiedsReplyCallbackArgs(ArrayList<DirectoryManager.Classified> classifieds)
		{
			this.m_Classifieds = classifieds;
		}
	}

	// Contains the group data returned from the data server
	public class DirGroupsReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartGroupSearch"/>
		public final UUID getQueryID()
		{
			return m_QueryID;
		}

		private final ArrayList<DirectoryManager.GroupSearchData> m_matchedGroups;

		// A list containing Groups data returned by the data server
		public final ArrayList<DirectoryManager.GroupSearchData> getMatchedGroups()
		{
			return m_matchedGroups;
		}

		/**
		 * Construct a new instance of the DirGroupsReplyEventArgs class
		 * 
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see
		 *            cref="StartGroupSearch"/> method
		 * @param matchedGroups
		 *            A list of groups data returned by the data server
		 */
		public DirGroupsReplyCallbackArgs(UUID queryID, ArrayList<DirectoryManager.GroupSearchData> matchedGroups)
		{
			this.m_QueryID = queryID;
			this.m_matchedGroups = matchedGroups;
		}
	}

	// Contains the people data returned from the data server
	public class DirPeopleReplyCallbackArgs implements CallbackArgs
	{
		private final UUID m_QueryID;

		// The ID returned by <see cref="DirectoryManager.StartPeopleSearch"/>
		public final UUID getQueryID()
		{
			return m_QueryID;
		}

		private final ArrayList<DirectoryManager.AgentSearchData> m_MatchedPeople;

		// A list containing People data returned by the data server
		public final ArrayList<DirectoryManager.AgentSearchData> getMatchedPeople()
		{
			return m_MatchedPeople;
		}

		/**
		 * Construct a new instance of the DirPeopleReplyEventArgs class
		 * 
		 * @param queryID
		 *            The ID of the query returned by the data server. This will
		 *            correlate to the ID returned by the <see
		 *            cref="StartPeopleSearch"/> method
		 * @param matchedPeople
		 *            A list of people data returned by the data server
		 */
		public DirPeopleReplyCallbackArgs(UUID queryID, ArrayList<DirectoryManager.AgentSearchData> matchedPeople)
		{
			this.m_QueryID = queryID;
			this.m_MatchedPeople = matchedPeople;
		}
	}

	// Contains the land sales data returned from the data server
	public class DirLandReplyCallbackArgs implements CallbackArgs
	{
		private final ArrayList<DirectoryManager.DirectoryParcel> m_DirParcels;

		// A list containing land forsale data returned by the data server
		public final ArrayList<DirectoryManager.DirectoryParcel> getDirParcels()
		{
			return m_DirParcels;
		}

		/**
		 * Construct a new instance of the DirLandReplyEventArgs class
		 * 
		 * @param dirParcels
		 *            A list of parcels for sale returned by the data server
		 */
		public DirLandReplyCallbackArgs(ArrayList<DirectoryManager.DirectoryParcel> dirParcels)
		{
			this.m_DirParcels = dirParcels;
		}
	}
}
