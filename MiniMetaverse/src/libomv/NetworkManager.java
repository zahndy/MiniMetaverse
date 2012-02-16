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

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import libomv.Simulator.RegionFlags;
import libomv.Simulator.SimAccess;
import libomv.Simulator.SimStatType;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.IMessage;
import libomv.packets.CompletePingCheckPacket;
import libomv.packets.EnableSimulatorPacket;
import libomv.packets.KickUserPacket;
import libomv.packets.LogoutReplyPacket;
import libomv.packets.LogoutRequestPacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.RegionHandshakePacket;
import libomv.packets.RegionHandshakeReplyPacket;
import libomv.packets.SimStatsPacket;
import libomv.packets.StartPingCheckPacket;
import libomv.types.UUID;
import libomv.types.PacketCallback;
import libomv.utils.CallbackArgs;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// NetworkManager is responsible for managing the network layer of
// libsecondlife. It tracks all the server connections, serializes
// outgoing traffic and deserializes incoming traffic, and provides
// instances of delegates for network-related events.

public class NetworkManager implements PacketCallback
{
	/** Explains why a simulator or the grid disconnected from us */
	public enum DisconnectType
	{
		/** The client requested the logout or simulator disconnect */
		ClientInitiated,
		/** The server notified us that it is disconnecting */
		ServerInitiated,
		/** Either a socket was closed or network traffic timed out */
		NetworkTimeout,
		/** The last active simulator shut down */
		SimShutdown
	}

	/**
	 * Holds a simulator reference and a decoded packet, these structs are put
	 * in the packet inbox for event handling
	 */
	public final class IncomingPacket
	{
		/** Reference to the simulator that this packet came from */
		public Simulator Simulator;
		/** Packet that needs to be processed */
		public Packet Packet;
		/** CapsMessage that needs to be processed */
		public IMessage Message;

		public IncomingPacket(Simulator simulator, Packet packet)
		{
			Simulator = simulator;
			Packet = packet;
		}

		public IncomingPacket(Simulator simulator, IMessage message)
		{
			Simulator = simulator;
			Message = message;
		}
	}

	/**
	 * Holds a simulator reference and a serialized packet, these structs are
	 * put in the packet outbox for sending
	 */
	public class OutgoingPacket
	{
		/** Reference to the simulator this packet is destined for */
		public final Simulator Simulator;
		/** Packet that needs to be sent */
		public final ByteBuffer Buffer;
		public int bytes;
		/** Sequence number of the wrapped packet */
		public int SequenceNumber;
		/** Number of times this packet has been resent */
		public int ResendCount;
		/** Environment.TickCount when this packet was last sent over the wire */
		public long TickCount;

		public OutgoingPacket(Simulator simulator, ByteBuffer buffer)
		{
			Simulator = simulator;
			Buffer = buffer;
		}
	}

	/** Callback arguments classes */
	public class SimConnectingCallbackArgs implements CallbackArgs
	{
		private InetSocketAddress endPoint;
		private boolean cancel = false;

		public InetSocketAddress getEndPoint()
		{
			return endPoint;
		}

		public void setCancel(boolean cancel)
		{
			this.cancel = cancel;
		}

		public boolean getCancel()
		{
			return cancel;
		}

		public SimConnectingCallbackArgs(InetSocketAddress endPoint)
		{
			this.endPoint = endPoint;
		}
	}

	public CallbackHandler<SimConnectingCallbackArgs> OnSimConnecting = new CallbackHandler<SimConnectingCallbackArgs>();

	public class SimConnectedCallbackArgs implements CallbackArgs
	{
		private final Simulator simulator;

		public Simulator getSimulator()
		{
			return simulator;
		}

		public SimConnectedCallbackArgs(Simulator simulator)
		{
			this.simulator = simulator;
		}
	}

	public CallbackHandler<SimConnectedCallbackArgs> OnSimConnected = new CallbackHandler<SimConnectedCallbackArgs>();

	/**
	 * Fire an event when an event queue connects for capabilities
	 * 
	 * @param simulator
	 *            Simulator the event queue is attached to
	 */
	public void RaiseSimConnectedEvent(Simulator simulator)
	{
		OnSimConnected.dispatch(new SimConnectedCallbackArgs(simulator));
	}

	public CallbackHandler<SimChangedCallbackArgs> OnSimChanged = new CallbackHandler<SimChangedCallbackArgs>();

	// An event for the connection to a simulator other than the currently
	// occupied one disconnecting
	public class SimDisconnectedCallbackArgs implements CallbackArgs
	{
		private final Simulator simulator;
		private final DisconnectType type;

		public Simulator getSimulator()
		{
			return simulator;
		}

		public DisconnectType getDisconnectType()
		{
			return type;
		}

		public SimDisconnectedCallbackArgs(Simulator simulator, DisconnectType type)
		{
			this.simulator = simulator;
			this.type = type;
		}
	}

	public CallbackHandler<SimDisconnectedCallbackArgs> OnSimDisconnected = new CallbackHandler<SimDisconnectedCallbackArgs>();

	// An event for being logged out either through client request, server
	// forced, or network error
	public class DisconnectedCallbackArgs implements CallbackArgs
	{
		private final DisconnectType type;
		private final String message;

		public DisconnectType getDisconnectType()
		{
			return type;
		}

		public String getMessage()
		{
			return message;
		}

		public DisconnectedCallbackArgs(DisconnectType type, String message)
		{
			this.type = type;
			this.message = message;
		}
	}

	public CallbackHandler<DisconnectedCallbackArgs> OnDisconnected = new CallbackHandler<DisconnectedCallbackArgs>();

	public class PacketSentCallbackArgs implements CallbackArgs
	{
		private final byte[] m_Data;
		private final int m_SentBytes;
		private final Simulator m_Simulator;

		public final byte[] getData()
		{
			return m_Data;
		}

		public final int getSentBytes()
		{
			return m_SentBytes;
		}

		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public PacketSentCallbackArgs(byte[] data, int bytesSent, Simulator simulator)
		{
			this.m_Data = data;
			this.m_SentBytes = bytesSent;
			this.m_Simulator = simulator;
		}
	}

	public CallbackHandler<PacketSentCallbackArgs> OnPacketSent = new CallbackHandler<PacketSentCallbackArgs>();

	public void RaisePacketSentCallback(byte[] data, int bytes, Simulator sim)
	{
		_Client.Network.OnPacketSent.dispatch(new NetworkManager.PacketSentCallbackArgs(data, bytes, sim));
	}

	public class EventQueueRunningCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;

		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public EventQueueRunningCallbackArgs(Simulator simulator)
		{
			this.m_Simulator = simulator;
		}
	}

	public CallbackHandler<EventQueueRunningCallbackArgs> OnEventQueueRunning = new CallbackHandler<EventQueueRunningCallbackArgs>();

	public final void RaiseConnectedEvent(Simulator simulator)
	{
		OnEventQueueRunning.dispatch(new EventQueueRunningCallbackArgs(simulator));
	}

	// An event triggered when the logout is confirmed
	public class LoggedOutCallbackArgs implements CallbackArgs
	{
		private final Vector<UUID> itemIDs;

		public Vector<UUID> getItemIDs()
		{
			return itemIDs;
		}

		public LoggedOutCallbackArgs(Vector<UUID> itemIDs)
		{
			this.itemIDs = itemIDs;
		}
	}

	public class SimChangedCallbackArgs implements CallbackArgs
	{
		private final Simulator simulator;

		public Simulator getSimulator()
		{
			return simulator;
		}

		public SimChangedCallbackArgs(Simulator simulator)
		{
			this.simulator = simulator;
		}
	}

	public CallbackHandler<LoggedOutCallbackArgs> OnLoggedOut = new CallbackHandler<LoggedOutCallbackArgs>();

	private HashMap<PacketType, ArrayList<PacketCallback>> simCallbacks;
	private HashMap<CapsEventType, ArrayList<CapsCallback>> capCallbacks;

	private GridClient _Client;

	/**
	 * The ID number associated with this particular connection to the
	 * simulator, used to emulate TCP connections. This is used internally for
	 * packets that have a CircuitCode field.
	 */
	private int _CircuitCode;

	public int getCircuitCode()
	{
		return _CircuitCode;
	}

	public void setCircuitCode(int code)
	{
		_CircuitCode = code;
	}

	/**
	 * A list of packets obtained during the login process which NetworkManager
	 * will log but not process
	 */
	private final ArrayList<PacketType> _UDPBlacklist = new ArrayList<PacketType>();

	public void setUDPBlacklist(String blacklist)
	{
		if (blacklist != null)
		{
			synchronized (_UDPBlacklist)
			{
				for (String s : blacklist.split(","))
					_UDPBlacklist.add(PacketType.valueOf(s));
				Logger.Log("UDP blacklisted packets: " + _UDPBlacklist.toString(), LogLevel.Debug, _Client);
			}
		}
	}

	private ArrayList<Simulator> _Simulators;

	/**
	 * Get the array with all currently known simulators. This list must be
	 * protected with a synchronization lock on itself if you do anything with it.
	 *
	 * @return array of simulator objects known to this client
	 */
	public ArrayList<Simulator> getSimulators()
	{
		return _Simulators;
	}

	/** Incoming packets that are awaiting handling */
	private BlockingQueue<IncomingPacket> _PacketInbox = new LinkedBlockingQueue<IncomingPacket>(
			Settings.PACKET_INBOX_SIZE);
	/** Outgoing packets that are awaiting handling */
	private BlockingQueue<OutgoingPacket> _PacketOutbox = new LinkedBlockingQueue<OutgoingPacket>(
			Settings.PACKET_INBOX_SIZE);

	/** Number of packets in the incoming queue */
	public final int getInboxCount()
	{
		return _PacketInbox.size();
	}

	/** Number of packets in the outgoing queue */
	public final int getOutboxCount()
	{
		return _PacketOutbox.size();
	}

	private IncomingPacketHandler _PacketHandlerThread;

	private class OutgoingPacketHandler implements Runnable
	{
		@Override
		public void run()
		{
			long lastTime = System.currentTimeMillis();

			while (_Connected)
			{
				try
				{
					OutgoingPacket outgoingPacket = _PacketOutbox.poll(100, TimeUnit.MILLISECONDS);
					if (outgoingPacket != null)
					{
						// Very primitive rate limiting, keeps a fixed buffer of
						// time between each packet
						long newTime = System.currentTimeMillis();
						long remains = 10 + lastTime - newTime;
						lastTime = newTime;

						if (remains > 0)
						{
							Logger.DebugLog(String.format("Rate limiting, last packet was %d ms ago", remains), _Client);
							Thread.sleep(remains);
						}
						outgoingPacket.Simulator.SendPacketFinal(outgoingPacket);
					}
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	}

	private void FirePacketCallbacks(Packet packet, Simulator simulator)
	{
		boolean specialHandler = false;

		synchronized (simCallbacks)
		{
			// Fire any default callbacks
			ArrayList<PacketCallback> callbackArray = simCallbacks.get(PacketType.Default);
			if (callbackArray != null)
			{
				for (PacketCallback callback : callbackArray)
				{
					try
					{
						callback.packetCallback(packet, simulator);
					}
					catch (Exception ex)
					{
						Logger.Log("Default packet event handler: " + packet.getType(), LogLevel.Error, _Client, ex);
					}
				}
			}
			// Fire any registered callbacks
			callbackArray = simCallbacks.get(packet.getType());
			if (callbackArray != null)
			{
				for (PacketCallback callback : callbackArray)
				{
					try
					{
						callback.packetCallback(packet, simulator);
					}
					catch (Exception ex)
					{
						Logger.Log("Packet event handler: " + packet.getType(), LogLevel.Error, _Client, ex);
					}
					specialHandler = true;
				}
			}
		}

		if (!specialHandler && packet.getType() != PacketType.Default && packet.getType() != PacketType.PacketAck)
		{
			Logger.Log("No handler registered for packet event " + packet.getType(), LogLevel.Warning, _Client);
		}
	}

	private void FireCapsCallbacks(IMessage message, Simulator simulator)
	{
		boolean specialHandler = false;

		synchronized (capCallbacks)
		{
			// Fire any default callbacks
			ArrayList<CapsCallback> callbackArray = capCallbacks.get(CapsEventType.Default);
			if (callbackArray != null)
			{
				for (CapsCallback callback : callbackArray)
				{
					try
					{
						callback.capsCallback(message, simulator);
					}
					catch (Exception ex)
					{
						Logger.Log("CAPS event handler: " + message.getType(), LogLevel.Error, _Client, ex);
					}
				}
			}
			// Fire any registered callbacks
			callbackArray = capCallbacks.get(message.getType());
			if (callbackArray != null)
			{
				for (CapsCallback callback : callbackArray)
				{
					try
					{
						callback.capsCallback(message, simulator);
					}
					catch (Exception ex)
					{
						Logger.Log("CAPS event handler: " + message.getType(), LogLevel.Error, _Client, ex);
					}
					specialHandler = true;
				}
			}
		}
		if (!specialHandler)
		{
			Logger.Log("Unhandled CAPS event " + message.getType(), LogLevel.Warning, _Client);
		}
	}

	private class PacketCallbackExecutor implements Runnable
	{
		private final IncomingPacket packet;

		public PacketCallbackExecutor(IncomingPacket packet)
		{
			this.packet = packet;
		}

		@Override
		public void run()
		{
			if (packet.Packet != null)
				FirePacketCallbacks(packet.Packet, packet.Simulator);
			else
				FireCapsCallbacks(packet.Message, packet.Simulator);
		}
	}

	private class IncomingPacketHandler implements Runnable
	{
		ExecutorService threadPool = Executors.newCachedThreadPool();

		public void shutdown()
		{
			threadPool.shutdown();
		}

		@Override
		public void run()
		{
			while (_Connected)
			{
				try
				{
					IncomingPacket incomingPacket = _PacketInbox.poll(100, TimeUnit.MILLISECONDS);
					if (incomingPacket != null)
					{
						if (incomingPacket.Packet != null)
						{
							// skip blacklisted packets
							if (_UDPBlacklist.contains(incomingPacket.Packet.getType()))
							{
								Logger.Log(
										String.format("Discarding Blacklisted packet %s from %s",
												incomingPacket.Packet.getType(),
												incomingPacket.Simulator.getIPEndPoint()), LogLevel.Warning, _Client);
							}
							else if (_Client.Settings.SYNC_PACKETCALLBACKS)
							{
								FirePacketCallbacks(incomingPacket.Packet, incomingPacket.Simulator);
							}
							else
							{
								threadPool.submit(new PacketCallbackExecutor(incomingPacket));
							}
						}
						else if (incomingPacket.Message != null)
						{
							if (_Client.Settings.SYNC_PACKETCALLBACKS)
							{
								FireCapsCallbacks(incomingPacket.Message, incomingPacket.Simulator);
							}
							else
							{
								threadPool.submit(new PacketCallbackExecutor(incomingPacket));
							}
						}
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}

	private Timer _DisconnectTimer;

	// The simulator that the logged in avatar is currently occupying
	private Simulator _CurrentSim;

	public Simulator getCurrentSim()
	{
		return _CurrentSim;
	}

	public final void setCurrentSim(Simulator value)
	{
		_CurrentSim = value;
	}

	// Shows whether the network layer is logged in to the grid or not
	private boolean _Connected;

	public boolean getConnected()
	{
		return _Connected;
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case RegionHandshake:
				HandleRegionHandshake(packet, simulator);
				break;
			case StartPingCheck:
				HandleStartPingCheck(packet, simulator);
				break;
			case CompletePingCheck:
				HandleCompletePingCheck(packet, simulator);
				break;
			case EnableSimulator:
				HandleEnableSimulator(packet, simulator);
				break;
			case DisableSimulator:
				HandleDisableSimulator(packet, simulator);
				break;
			case LogoutReply:
				HandleLogoutReply(packet, simulator);
				break;
			case SimStats:
				HandleSimStats(packet, simulator);
				break;
			case KickUser:
				HandleKickUser(packet, simulator);
				break;
		}
	}

	//
	// <param name="client"></param>
	public NetworkManager(GridClient client) throws Exception
	{
		_Client = client;
		_Simulators = new ArrayList<Simulator>();
		simCallbacks = new HashMap<PacketType, ArrayList<PacketCallback>>();
		capCallbacks = new HashMap<CapsEventType, ArrayList<CapsCallback>>();
		_CurrentSim = null;

		// Register the internal callbacks
		RegisterCallback(PacketType.RegionHandshake, this);
		RegisterCallback(PacketType.StartPingCheck, this);
		RegisterCallback(PacketType.DisableSimulator, this);
		RegisterCallback(PacketType.EnableSimulator, this);
		RegisterCallback(PacketType.KickUser, this);
		RegisterCallback(PacketType.LogoutReply, this);
		RegisterCallback(PacketType.CompletePingCheck, this);
		RegisterCallback(PacketType.SimStats, this);
	}

	public URI getCapabilityURI(String capability)
	{
		synchronized (_Simulators)
		{
			if (_CurrentSim != null)
			{
				return _CurrentSim.getCapabilityURI(capability);
			}
		}
		return null;
	}

	public URI getCapabilityURI(String capability, Simulator simulator)
	{
		if (simulator == null)
			simulator = _CurrentSim;

		if (simulator != null)
		{
			return simulator.getCapabilityURI(capability);
		}
		return null;
	}

	public boolean getIsEventQueueRunning()
	{
		synchronized (_Simulators)
		{
			return (_CurrentSim != null && _CurrentSim.getIsEventQueueRunning());
		}
	}

	public void RegisterCallback(CapsEventType capability, CapsCallback callback)
	{
		/* Don't accept null callbacks */
		if (callback == null)
			return;

		synchronized (capCallbacks)
		{
			if (!capCallbacks.containsKey(capability))
			{
				capCallbacks.put(capability, new ArrayList<CapsCallback>());
			}
			capCallbacks.get(capability).add(callback);
		}
	}

	public void UnregisterCallback(CapsEventType capability, CapsCallback callback)
	{
		synchronized (capCallbacks)
		{
			if (!capCallbacks.containsKey(capability))
			{
				Logger.Log("Trying to unregister a callback for capability " + capability
						+ " when no callbacks are setup for that capability", LogLevel.Info, _Client);
				return;
			}

			ArrayList<CapsCallback> callbackArray = capCallbacks.get(capability);

			if (callbackArray.contains(callback))
			{
				callbackArray.remove(callback);
				if (callbackArray.isEmpty())
				{
					capCallbacks.remove(capability);
				}
			}
			else
			{
				Logger.Log("Trying to unregister a non-existant callback for capability " + capability, LogLevel.Info,
						_Client);
			}
		}
	}

	public void RegisterCallback(PacketType type, PacketCallback callback)
	{
		/* Don't accept null callbacks */
		if (callback == null)
			return;

		synchronized (simCallbacks)
		{
			if (!simCallbacks.containsKey(type))
			{
				simCallbacks.put(type, new ArrayList<PacketCallback>());
			}
			simCallbacks.get(type).add(callback);
		}
	}

	public void UnregisterCallback(PacketType type, PacketCallback callback)
	{
		synchronized (simCallbacks)
		{
			if (!simCallbacks.containsKey(type))
			{
				Logger.Log("Trying to unregister a callback for packet " + type
						+ " when no callbacks are setup for that packet", LogLevel.Info, _Client);
				return;
			}

			ArrayList<PacketCallback> callbackArray = simCallbacks.get(type);
			if (callbackArray.contains(callback))
			{
				callbackArray.remove(callback);
				if (callbackArray.isEmpty())
				{
					simCallbacks.remove(type);
				}
			}
			else
			{
				Logger.Log("Trying to unregister a non-existant callback for packet " + type, LogLevel.Info, _Client);
			}
		}
	}

	/**
	 * Send an UDP packet to the current simulator
	 * 
	 * @param packet
	 *            The packet to send
	 * @throws Exception
	 */
	public void SendPacket(Packet packet) throws Exception
	{
		// try CurrentSim, however directly after login this will be null, so if it is, we'll
		// try to find the first simulator we're connected to in order to send the packet.
		Simulator simulator = _CurrentSim;
		synchronized (_Simulators)
		{
			if (simulator == null && _Simulators.size() >= 1)
			{
				Logger.DebugLog("CurrentSim object was null, using first found connected simulator", _Client);
				simulator = _Simulators.get(0);
			}
		}

		if (simulator != null && simulator.getConnected())
		{
			simulator.SendPacket(packet);
		}
		else
		{
			ConnectException ex = new ConnectException(
					"Packet received before simulator packet processing threads running, make certain you are completely logged in");
			Logger.Log(ex.getMessage(), LogLevel.Error, _Client, ex);
			throw ex;
		}
	}

	public void QueuePacket(OutgoingPacket packet) throws InterruptedException
	{
		_PacketOutbox.put(packet);
	}

	public void DistributePacket(Simulator simulator, Packet packet)
	{
		_PacketInbox.add(new IncomingPacket(simulator, packet));
	}

	public void DistributeCaps(Simulator simulator, IMessage message)
	{
		_PacketInbox.add(new IncomingPacket(simulator, message));
	}

	public Simulator Connect(InetAddress ip, short port, long handle, boolean setDefault, String seedcaps)
			throws Exception
	{
		return Connect(new InetSocketAddress(ip, port), handle, setDefault, seedcaps);
	}

	/**
	 * Connect to a simulator
	 * 
	 * @param endPoint
	 *            IP address and port to connect to
	 * @param handle
	 *            Handle for this simulator, to identify its location in the
	 *            grid
	 * @param setDefault
	 *            Whether to set CurrentSim to this new connection, use this if
	 *            the avatar is moving in to this simulator
	 * @param seedcaps
	 *            URL of the capabilities server to use for this sim connection
	 * @return A Simulator object on success, otherwise null
	 * */
	public Simulator Connect(InetSocketAddress endPoint, long handle, boolean setDefault, String seedcaps)
			throws Exception
	{
		Simulator simulator = FindSimulator(endPoint);

		if (simulator == null)
		{
			// We're not tracking this sim, create a new Simulator object
			simulator = new Simulator(_Client, endPoint, handle);

			synchronized (_Simulators)
			{
				// Immediately add this simulator to the list of current sims.
				// It will be removed if the connection fails
				_Simulators.add(simulator);
			}
		}

		if (!simulator.getConnected())
		{
			if (!_Connected)
			{
				// Mark that we are connecting/connected to the grid
				_Connected = true;

				// Start the packet decoding thread
				_PacketHandlerThread = new IncomingPacketHandler();
				Thread decodeThread = new Thread(_PacketHandlerThread);
				decodeThread.setName("Incoming UDP packet dispatcher");
				decodeThread.start();

				// Start the packet sending thread
				Thread sendThread = new Thread(new OutgoingPacketHandler());
				sendThread.setName("Outgoing UDP packet dispatcher");
				sendThread.start();
			}

			if (OnSimConnecting.count() > 0)
			{
				SimConnectingCallbackArgs args = new SimConnectingCallbackArgs(endPoint);
				OnSimConnecting.dispatch(args);
				if (args.getCancel())
				{
					synchronized (_Simulators)
					{
						// Callback is requesting that we abort this connection
						_Simulators.remove(simulator);
					}
					return null;
				}
			}

			// Attempt to establish a connection to the simulator
			if (simulator.Connect(setDefault))
			{
				if (_DisconnectTimer == null)
				{
					// Start a timer that checks if we've been disconnected
					_DisconnectTimer = new Timer();
					_DisconnectTimer.scheduleAtFixedRate(new DisconnectTimer_Elapsed(),
							_Client.Settings.SIMULATOR_TIMEOUT, _Client.Settings.SIMULATOR_TIMEOUT);
				}

				if (setDefault)
				{
					SetCurrentSim(simulator, seedcaps);
				}

				// Raise the SimConnected event
				OnSimConnected.dispatch(new SimConnectedCallbackArgs(simulator));

				// If enabled, send an AgentThrottle packet to the server to
				// increase our bandwidth
				if (_Client.Throttle != null)
				{
					_Client.Throttle.Set(simulator);
				}
			}
			else
			{
				synchronized (_Simulators)
				{
					// Connection failed, remove this simulator from our list
					// and destroy it
					_Simulators.remove(simulator);
				}
				return null;
			}
		}
		else if (setDefault)
		{
			// Move in to this simulator
			simulator.UseCircuitCode();
			_Client.Self.CompleteAgentMovement(simulator);

			// We're already connected to this server, but need to set it to the default
			SetCurrentSim(simulator, seedcaps);

			// Send an initial AgentUpdate to complete our movement in to the sim
			if (_Client.Settings.SEND_AGENT_UPDATES)
			{
				_Client.Self.SendMovementUpdate(true, simulator);
			}
		}
		else
		{
			// Already connected to this simulator and wasn't asked to set it as
			// the default, just return a reference to the existing object
		}
		return simulator;
	}

	public void Logout() throws Exception
	{
		// This will catch a Logout when the client is not logged in
		if (_CurrentSim == null || !_Connected)
		{
			return;
		}

		Logger.Log("Logging out", LogLevel.Info, _Client);

		_Connected = false;
		_DisconnectTimer.cancel();
		_PacketHandlerThread.shutdown();

		// Send a logout request to the current sim
		LogoutRequestPacket logout = new LogoutRequestPacket();
		logout.AgentData.AgentID = _Client.Self.getAgentID();
		logout.AgentData.SessionID = _Client.Self.getSessionID();

		_CurrentSim.SendPacket(logout);

		// TODO: We should probably check if the server actually received the
		// logout request

		// Shutdown the network layer
		Shutdown(DisconnectType.ClientInitiated, "User logged out");
	}

	private void SetCurrentSim(Simulator simulator, String seedcaps)
	{
		if (simulator != getCurrentSim())
		{
			Simulator oldSim = getCurrentSim();
			synchronized (_Simulators) // CurrentSim is synchronized against
										// Simulators
			{
				setCurrentSim(simulator);
			}
			simulator.SetSeedCaps(seedcaps);

			// If the current simulator changed fire the callback
			if (simulator != oldSim)
			{
				OnSimChanged.dispatch(new SimChangedCallbackArgs(oldSim));
			}
		}
	}

	public void DisconnectSim(Simulator simulator, boolean sendCloseCircuit) throws Exception
	{
		if (simulator != null)
		{
			simulator.Disconnect(sendCloseCircuit);

			// Fire the SimDisconnected event if a handler is registered
			OnSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));

			synchronized (_Simulators)
			{
				_Simulators.remove(simulator);
				if (_Simulators.isEmpty())
				{
					Shutdown(DisconnectType.SimShutdown);
				}
			}
		}
		else
		{
			Logger.Log("DisconnectSim() called with a null Simulator reference", LogLevel.Warning);
		}
	}

	/**
	 * Shutdown will disconnect all the sims except for the current sim first,
	 * and then kill the connection to CurrentSim. This should only be called if
	 * the logout process times out on <code>RequestLogout</code>
	 * 
	 * @param type
	 *            Type of shutdown
	 * @throws Exception
	 */
	public final void Shutdown(DisconnectType type) throws Exception
	{
		Shutdown(type, type.toString());
	}

	private void Shutdown(DisconnectType type, String message) throws Exception
	{
		Logger.Log("NetworkManager shutdown initiated", LogLevel.Info, _Client);

		// Send a CloseCircuit packet to simulators if we are initiating the
		// disconnect
		boolean sendCloseCircuit = (type == DisconnectType.ClientInitiated || type == DisconnectType.NetworkTimeout);

		synchronized (_Simulators)
		{
			// Disconnect all simulators except the current one
			for (int i = 0; i < _Simulators.size(); i++)
			{
				Simulator simulator = _Simulators.get(i);
				// Don't disconnect the current sim, we'll use LogoutRequest for
				// that
				if (simulator != null && simulator != _CurrentSim)
				{
					simulator.Disconnect(sendCloseCircuit);

					// Fire the SimDisconnected event if a handler is registered
					OnSimDisconnected
							.dispatch(new SimDisconnectedCallbackArgs(simulator, DisconnectType.NetworkTimeout));
				}

			}
			_Simulators.clear();

			if (_CurrentSim != null)
			{
				_CurrentSim.Disconnect(sendCloseCircuit);

				// Fire the SimDisconnected event if a handler is registered
				OnSimDisconnected.dispatch(new SimDisconnectedCallbackArgs(_CurrentSim, DisconnectType.NetworkTimeout));
			}
		}
		_Connected = false;
		if (OnDisconnected != null)
		{
			OnDisconnected.dispatch(new DisconnectedCallbackArgs(type, message));
		}
	}

	private class DisconnectTimer_Elapsed extends TimerTask
	{
		@Override
		public void run()
		{
			// If the current simulator is disconnected, shutdown + callback +
			// return
			if (!_Connected || _CurrentSim == null)
			{
				if (_DisconnectTimer != null)
				{
					_DisconnectTimer.cancel();
					_DisconnectTimer = null;
				}
				_Connected = false;
			}
			else if (_CurrentSim.getDisconnectCandidate())
			{
				// The currently occupied simulator hasn't sent us any traffic
				// in a while, shutdown
				Logger.Log("Network timeout for the current simulator (" + _CurrentSim.Name + "), logging out",
						LogLevel.Warning);

				if (_DisconnectTimer != null)
				{
					_DisconnectTimer.cancel();
					_DisconnectTimer = null;
				}
				_Connected = false;

				// Shutdown the network layer
				try
				{
					Shutdown(DisconnectType.NetworkTimeout);
				}
				catch (Exception ex)
				{
				}

				// We're completely logged out and shut down, leave this
				// function
				return;
			}

			ArrayList<Simulator> disconnectedSims = null;

			// Check all of the connected sims for disconnects
			synchronized (_Simulators)
			{
				for (Simulator simulator : _Simulators)
				{
					if (simulator.getDisconnectCandidate())
					{
						if (disconnectedSims == null)
						{
							disconnectedSims = new ArrayList<Simulator>();
						}
						disconnectedSims.add(simulator);
					}
					else
					{
						simulator.setDisconnectCandidate(true);
					}
				}
			}

			// Actually disconnect each sim we detected as disconnected
			if (disconnectedSims != null)
			{
				for (Simulator simulator : disconnectedSims)
				{
					// This sim hasn't received any network traffic since the
					// timer last elapsed, consider it disconnected
					Logger.Log("Network timeout for simulator " + simulator.Name + ", disconnecting", LogLevel.Warning);

					try
					{
						DisconnectSim(simulator, false);
					}
					catch (Exception ex)
					{
					}
				}
			}
		}
	}

	/**
	 * Searches through the list of currently connected simulators to find one
	 * attached to the given IPEndPoint
	 * 
	 * @param endPoint
	 *            InetSocketAddress of the Simulator to search for
	 * @return A Simulator reference on success, otherwise null
	 */
	private final Simulator FindSimulator(InetSocketAddress endPoint)
	{
		synchronized (_Simulators)
		{
			for (Simulator simulator : _Simulators)
			{
				if (simulator.getIPEndPoint().equals(endPoint))
				{
					return simulator;
				}
			}
		}
		return null;
	}

	private void HandleRegionHandshake(Packet packet, Simulator simulator) throws Exception
	{
		RegionHandshakePacket handshake = (RegionHandshakePacket) packet;

		simulator.ID = handshake.RegionInfo.CacheID;

		simulator.IsEstateManager = handshake.RegionInfo.IsEstateManager;
		simulator.Name = Helpers.BytesToString(handshake.RegionInfo.getSimName());
		simulator.SimOwner = handshake.RegionInfo.SimOwner;
		simulator.TerrainBase0 = handshake.RegionInfo.TerrainBase0;
		simulator.TerrainBase1 = handshake.RegionInfo.TerrainBase1;
		simulator.TerrainBase2 = handshake.RegionInfo.TerrainBase2;
		simulator.TerrainBase3 = handshake.RegionInfo.TerrainBase3;
		simulator.TerrainDetail0 = handshake.RegionInfo.TerrainDetail0;
		simulator.TerrainDetail1 = handshake.RegionInfo.TerrainDetail1;
		simulator.TerrainDetail2 = handshake.RegionInfo.TerrainDetail2;
		simulator.TerrainDetail3 = handshake.RegionInfo.TerrainDetail3;
		simulator.TerrainHeightRange00 = handshake.RegionInfo.TerrainHeightRange00;
		simulator.TerrainHeightRange01 = handshake.RegionInfo.TerrainHeightRange01;
		simulator.TerrainHeightRange10 = handshake.RegionInfo.TerrainHeightRange10;
		simulator.TerrainHeightRange11 = handshake.RegionInfo.TerrainHeightRange11;
		simulator.TerrainStartHeight00 = handshake.RegionInfo.TerrainStartHeight00;
		simulator.TerrainStartHeight01 = handshake.RegionInfo.TerrainStartHeight01;
		simulator.TerrainStartHeight10 = handshake.RegionInfo.TerrainStartHeight10;
		simulator.TerrainStartHeight11 = handshake.RegionInfo.TerrainStartHeight11;

		simulator.WaterHeight = handshake.RegionInfo.WaterHeight;
		simulator.Flags = RegionFlags.setValue(handshake.RegionInfo.RegionFlags);
		simulator.BillableFactor = handshake.RegionInfo.BillableFactor;
		simulator.Access = SimAccess.setValue(handshake.RegionInfo.SimAccess);

		simulator.RegionID = handshake.RegionID;

		simulator.ColoLocation = Helpers.BytesToString(handshake.RegionInfo3.getColoName());
		simulator.CPUClass = handshake.RegionInfo3.CPUClassID;
		simulator.CPURatio = handshake.RegionInfo3.CPURatio;
		simulator.ProductName = Helpers.BytesToString(handshake.RegionInfo3.getProductName());
		simulator.ProductSku = Helpers.BytesToString(handshake.RegionInfo3.getProductSKU());

		// Send a RegionHandshakeReply
		RegionHandshakeReplyPacket reply = new RegionHandshakeReplyPacket();
		reply.AgentData.AgentID = _Client.Self.getAgentID();
		reply.AgentData.SessionID = _Client.Self.getSessionID();
		reply.Flags = 0;
		simulator.SendPacket(reply);

		Logger.Log("Received a region handshake for " + simulator.Name, LogLevel.Info, _Client);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 * @throws Exception
	 */
	private void HandleStartPingCheck(Packet packet, Simulator simulator) throws Exception
	{
		StartPingCheckPacket incomingPing = (StartPingCheckPacket) packet;
		CompletePingCheckPacket ping = new CompletePingCheckPacket();
		ping.PingID = incomingPing.PingID.PingID;
		ping.getHeader().setReliable(false);
		// TODO: We can use OldestUnacked to correct transmission errors
		// I don't think that's right. As far as I can tell, the Viewer
		// only uses this to prune its duplicate-checking buffer. -bushing
		simulator.SendPacket(ping);
	}

	/**
	 * Process a ping answer
	 */
	private final void HandleCompletePingCheck(Packet packet, Simulator simulator)
	{
		CompletePingCheckPacket pong = (CompletePingCheckPacket) packet;
		long timeMilli = System.currentTimeMillis();

		simulator.Statistics.LastLag = timeMilli - simulator.Statistics.LastPingSent;
		simulator.Statistics.ReceivedPongs++;
		String retval = "Pong2: " + simulator.Statistics.LastLag;
		if ((pong.PingID - simulator.Statistics.LastPingID + 1) != 0)
		{
			retval += " (gap of " + (pong.PingID - simulator.Statistics.LastPingID + 1) + ")";
		}

		Logger.Log(retval, LogLevel.Info, _Client);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 */
	private final void HandleSimStats(Packet packet, Simulator simulator)
	{
		if (!_Client.Settings.ENABLE_SIMSTATS)
		{
			return;
		}
		SimStatsPacket stats = (SimStatsPacket) packet;
		for (int i = 0; i < stats.Stat.length; i++)
		{
			SimStatsPacket.StatBlock s = stats.Stat[i];

			switch (SimStatType.setValue(s.StatID))
			{
				case TimeDilation:
					simulator.Statistics.Dilation = s.StatValue;
					break;
				case SimFPS:
					simulator.Statistics.FPS = Helpers.BytesToInt32L(Helpers.FloatToBytesL(s.StatValue));
					break;
				case PhysicsFPS:
					simulator.Statistics.PhysicsFPS = s.StatValue;
					break;
				case AgentUpdates:
					simulator.Statistics.AgentUpdates = s.StatValue;
					break;
				case FrameMS:
					simulator.Statistics.FrameTime = s.StatValue;
					break;
				case NetMS:
					simulator.Statistics.NetTime = s.StatValue;
					break;
				case OtherMS:
					simulator.Statistics.OtherTime = s.StatValue;
					break;
				case PhysicsMS:
					simulator.Statistics.PhysicsTime = s.StatValue;
					break;
				case AgentMS:
					simulator.Statistics.AgentTime = s.StatValue;
					break;
				case ImageMS:
					simulator.Statistics.ImageTime = s.StatValue;
					break;
				case ScriptMS:
					simulator.Statistics.ScriptTime = s.StatValue;
					break;
				case TotalPrim:
					simulator.Statistics.Objects = (int)s.StatValue;
					break;
				case ActivePrim:
					simulator.Statistics.ScriptedObjects = (int)s.StatValue;
					break;
				case Agents:
					simulator.Statistics.Agents = (int)s.StatValue;
					break;
				case ChildAgents:
					simulator.Statistics.ChildAgents = (int)s.StatValue;
					break;
				case ActiveScripts:
					simulator.Statistics.ActiveScripts = (int)s.StatValue;
					break;
				case ScriptInstructionsPerSecond:
					simulator.Statistics.LSLIPS = (int)s.StatValue;
					break;
				case InPacketsPerSecond:
					simulator.Statistics.INPPS = (int)s.StatValue;
					break;
				case OutPacketsPerSecond:
					simulator.Statistics.OUTPPS = (int)s.StatValue;
					break;
				case PendingDownloads:
					simulator.Statistics.PendingDownloads = (int)s.StatValue;
					break;
				case PendingUploads:
					simulator.Statistics.PendingUploads = (int)s.StatValue;
					break;
				case VirtualSizeKB:
					simulator.Statistics.VirtualSize = (int)s.StatValue;
					break;
				case ResidentSizeKB:
					simulator.Statistics.ResidentSize = (int)s.StatValue;
					break;
				case PendingLocalUploads:
					simulator.Statistics.PendingLocalUploads = (int)s.StatValue;
					break;
				case UnAckedBytes:
					simulator.Statistics.UnackedBytes = (int)s.StatValue;
					break;
				case PhysicsPinnedTasks:
				case PhysicsLODTasks:
				case PhysicsStepMS:
				case PhysicsShapeMS:
				case PhysicsOtherMS:
				case PhysicsMemory:
				case ScriptEPS:
				case SimSpareTime:
				case SimSleepTime:
				case SimIOPumpTime:
					break;
				default:
					Logger.Log("Unhandled Sim Stats ID: " + s.StatID, LogLevel.Debug, _Client);
					break;
			}
		}
	}

	private void HandleEnableSimulator(Packet packet, Simulator simulator) throws Exception
	{
		if (!_Client.Settings.MULTIPLE_SIMS)
		{
			return;
		}

		EnableSimulatorPacket msg = (EnableSimulatorPacket) packet;

		InetAddress ip = InetAddress.getByAddress(Helpers.Int32ToBytesB(msg.SimulatorInfo.IP));
		InetSocketAddress endPoint = new InetSocketAddress(ip, msg.SimulatorInfo.Port);

		if (FindSimulator(endPoint) != null)
		{
			return;
		}

		if (Connect(endPoint, msg.SimulatorInfo.Handle, false, null) == null)
		{
			Logger.Log("Unabled to connect to new sim " + ip + ":" + msg.SimulatorInfo.Port, LogLevel.Error);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The sender
	 * @param packet
	 *            The packet data
	 * @throws Exception
	 */
	private final void HandleDisableSimulator(Packet packet, Simulator simulator) throws Exception
	{
		DisconnectSim(simulator, false);
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param packet
	 *            The packet data
	 * @param simulator
	 *            The sender
	 * @throws Exception
	 */
	private void HandleLogoutReply(Packet packet, Simulator simulator) throws Exception
	{
		LogoutReplyPacket logout = (LogoutReplyPacket) packet;

		if ((logout.AgentData.SessionID == _Client.Self.getSessionID())
				&& (logout.AgentData.AgentID == _Client.Self.getAgentID()))
		{
			Logger.DebugLog("Logout reply received", _Client);

			// Deal with callbacks, if any
			if (OnLoggedOut.count() > 0)
			{
				Vector<UUID> itemIDs = new Vector<UUID>();

				for (UUID inventoryID : logout.ItemID)
				{
					itemIDs.add(inventoryID);
				}
				OnLoggedOut.dispatch(new LoggedOutCallbackArgs(itemIDs));
			}

			// If we are receiving a LogoutReply packet assume this is a client
			// initiated shutdown
			Shutdown(DisconnectType.ClientInitiated);
		}
		else
		{
			Logger.Log("Invalid Session or Agent ID received in Logout Reply... ignoring", LogLevel.Warning, _Client);
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * @param simulator
	 *            The sender
	 * @param packet
	 *            The packet data
	 */
	private void HandleKickUser(Packet packet, Simulator simulator) throws Exception
	{
		String message = Helpers.BytesToString(((KickUserPacket) packet).UserInfo.getReason());

		// Shutdown the network layer
		Shutdown(DisconnectType.ServerInitiated, message);
	}
}
