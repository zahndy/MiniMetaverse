/**
 * Copyright (c) 2006-2009, openmetaverse.org
 * Portions Copyright (c) 2006, Lateral Arts Limited
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.NetworkManager.DisconnectedCallbackArgs;
import libomv.capabilities.CapsCallback;
import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.ObjectPhysicsPropertiesMessage;
import libomv.capabilities.IMessage;
import libomv.packets.ImprovedTerseObjectUpdatePacket;
import libomv.packets.KillObjectPacket;
import libomv.packets.MultipleObjectUpdatePacket;
import libomv.packets.ObjectAddPacket;
import libomv.packets.ObjectAttachPacket;
import libomv.packets.ObjectBuyPacket;
import libomv.packets.ObjectDeGrabPacket;
import libomv.packets.ObjectDelinkPacket;
import libomv.packets.ObjectDescriptionPacket;
import libomv.packets.ObjectDeselectPacket;
import libomv.packets.ObjectDetachPacket;
import libomv.packets.ObjectDropPacket;
import libomv.packets.ObjectExtraParamsPacket;
import libomv.packets.ObjectGrabPacket;
import libomv.packets.ObjectGroupPacket;
import libomv.packets.ObjectImagePacket;
import libomv.packets.ObjectLinkPacket;
import libomv.packets.ObjectNamePacket;
import libomv.packets.ObjectOwnerPacket;
import libomv.packets.ObjectPermissionsPacket;
import libomv.packets.ObjectPropertiesFamilyPacket;
import libomv.packets.ObjectPropertiesPacket;
import libomv.packets.ObjectRotationPacket;
import libomv.packets.ObjectSaleInfoPacket;
import libomv.packets.ObjectSelectPacket;
import libomv.packets.ObjectShapePacket;
import libomv.packets.ObjectUpdateCachedPacket;
import libomv.packets.ObjectUpdateCompressedPacket;
import libomv.packets.ObjectUpdatePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PayPriceReplyPacket;
import libomv.packets.RequestMultipleObjectsPacket;
import libomv.packets.RequestObjectPropertiesFamilyPacket;
import libomv.packets.RequestPayPricePacket;
import libomv.primitives.Avatar;
import libomv.primitives.MediaEntry;
import libomv.primitives.ObjectProperties;
import libomv.primitives.ParticleSystem;
import libomv.primitives.PhysicsProperties;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.ClickAction;
import libomv.primitives.Primitive.ConstructionData;
import libomv.primitives.Primitive.ExtraParamType;
import libomv.primitives.Primitive.FlexibleData;
import libomv.primitives.Primitive.Grass;
import libomv.primitives.Primitive.JointType;
import libomv.primitives.Primitive.LightData;
import libomv.primitives.Primitive.Material;
import libomv.primitives.Primitive.ObjectCategory;
import libomv.primitives.Primitive.PathCurve;
import libomv.primitives.Primitive.PrimFlags;
import libomv.primitives.Primitive.ProfileCurve;
import libomv.primitives.Primitive.PCode;
import libomv.primitives.Primitive.SculptData;
import libomv.primitives.Primitive.SoundFlags;
import libomv.primitives.Primitive.Tree;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.Permissions;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.types.NameValue;
import libomv.types.PacketCallback;
import libomv.utils.CallbackArgs;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// Handles all network traffic related to prims and avatar positions and
// movement.
public class ObjectManager implements PacketCallback, CapsCallback
{
	/** Item Sale Status */
	public enum SaleType
	{
		/** Not for sale */
		Not,
		/** The original is for sale */
		Original,
		/** Copies are for sale */
		Copy,
		/** The contents of the object are for sale */
		Contents;

		private static final String[] _SaleTypeNames = new String[] { "not", "orig", "copy", "cntn" };

		/**
		 * Translate a string name of an SaleType into the proper Type
		 * 
		 * @param type
		 *            A string containing the SaleType name
		 * @return The SaleType which matches the string name, or
		 *         SaleType.Unknown if no match was found
		 */
		public static SaleType setValue(String value)
		{
			for (int i = 0; i < _SaleTypeNames.length; i++)
			{
				if (value.compareToIgnoreCase(_SaleTypeNames[i]) == 0)
				{
					return values()[i];
				}
			}
			return Not;
		}

		public static SaleType setValue(int value)
		{
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}

		@Override
		public String toString()
		{
			return _SaleTypeNames[ordinal()];
		}
	}

	public enum ReportType
	{
		// No report
		None,
		// Unknown report type
		Unknown,
		// Bug report
		Bug,
		// Complaint report
		Complaint,
		// Customer service report
		CustomerServiceRequest;

		public static ReportType setValue(int value)
		{
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// Bitflag field for ObjectUpdateCompressed data blocks, describing
	// which options are present for each object
	public static class CompressedFlags
	{
		public static final short None = 0x00;
		// Unknown
		public static final short ScratchPad = 0x01;
		// Whether the object has a TreeSpecies
		public static final short Tree = 0x02;
		// Whether the object has floating text ala llSetText
		public static final short HasText = 0x04;
		// Whether the object has an active particle system
		public static final short HasParticles = 0x08;
		// Whether the object has sound attached to it
		public static final short HasSound = 0x10;
		// Whether the object is attached to a root object or not
		public static final short HasParent = 0x20;
		// Whether the object has texture animation settings
		public static final short TextureAnimation = 0x40;
		// Whether the object has an angular velocity
		public static final short HasAngularVelocity = 0x80;
		// Whether the object has a name value pairs string
		public static final short HasNameValues = 0x100;
		// Whether the object has a Media URL set
		public static final short MediaURL = 0x200;

		public static short setValue(short value)
		{
			return (short) (value & _mask);
		}

		public static short getValue(int value)
		{
			return (short) (value & _mask);
		}

		private static final short _mask = 0x3FF;
	}

	// Specific Flags for MultipleObjectUpdate requests
	public static class UpdateType
	{
		// None
		public static final byte None = 0x00;
		// Change position of prims
		public static final byte Position = 0x01;
		// Change rotation of prims
		public static final byte Rotation = 0x02;
		// Change size of prims
		public static final byte Scale = 0x04;
		// Perform operation on link set
		public static final byte Linked = 0x08;
		// Scale prims uniformly, same as selecing ctrl+shift in the
		// viewer. Used in conjunction with Scale
		public static final byte Uniform = 0x10;

		public static short setValue(byte value)
		{
			return (short) (value & _mask);
		}

		public static byte getValue(int value)
		{
			return (byte) (value & _mask);
		}

		private static final short _mask = 0x1F;
	}

	// Special values in PayPriceReply. If the price is not one of these
	// literal value of the price should be use
	public enum PayPriceType
	{
		// Indicates that this pay option should be hidden
		Hide(-1),

		// Indicates that this pay option should have the default value
		Default(-2);

		public static PayPriceType setValue(int value)
		{
			if (value >= 0 && value < values().length)
				return values()[value];
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private PayPriceType(int value)
		{
			_value = (byte) value;
		}
	}

	/**
	 * Contains the variables sent in an object update packet for objects. Used
	 * to track position and movement of prims and avatars
	 */
	public final class ObjectMovementUpdate
	{
		public boolean Avatar;
		public Vector4 CollisionPlane;
		public byte State;
		public int LocalID; // uint
		public Vector3 Position;
		public Vector3 Velocity;
		public Vector3 Acceleration;
		public Quaternion Rotation;
		public Vector3 AngularVelocity;
		public TextureEntry Textures;
	}

	public static final float HAVOK_TIMESTEP = 1.0f / 45.0f;

	/**
	 * Provides data for the <see cref="ObjectManager.ObjectUpdate"/> event
	 * 
	 * <p>
	 * The <see cref="ObjectManager.ObjectUpdate"/> event occurs when the
	 * simulator sends an <see cref="ObjectUpdatePacket"/> containing a
	 * Primitive, Foliage or Attachment data
	 * </p>
	 * <p>
	 * Note 1: The <see cref="ObjectManager.ObjectUpdate"/> event will not be
	 * raised when the object is an Avatar
	 * </p>
	 * <p>
	 * Note 2: It is possible for the <see cref="ObjectManager.ObjectUpdate"/>
	 * to be raised twice for the same object if for example the primitive moved
	 * to a new simulator, then returned to the current simulator or if an
	 * Avatar crosses the border into a new simulator and returns to the current
	 * simulator
	 * </p>
	 * 
	 * <example> The following code example uses the <see
	 * cref="PrimCallbackArgs.Prim"/>, <see cref="PrimCallbackArgs.Simulator"/>,
	 * and <see cref="PrimCallbackArgs.IsAttachment"/> properties to display new
	 * Primitives and Attachments on the <see cref="Console"/> window. <code>
	 *     // Subscribe to the event that gives us prim and foliage information
	 *     Client.Objects.OnObjectUpdate.add(new Objects_ObjectUpdate());
	 * 
	 * 
	 *     private Objects_ObjectUpdate implements CallbackHandler<PrimCallbackArgs>
	 *     {
	 *         void callback(PrimCallbackArgs e)
	 *         {
	 *              Console.WriteLine("Primitive %s %s in %s is an attachment %s", e.getPrim().ID, e.getPrim().LocalID, e.getSimulator().Name, e.getIsAttachment());
	 *         }
	 *     }
	 * </code> </example> {@link ObjectManager.OnObjectUpdate}
	 * {@link ObjectManager.OnAvatarUpdate} {@link AvatarUpdateCallbackArgs}
	 */
	public class PrimCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final boolean m_IsNew;
		private final Primitive m_Prim;
		private final short m_TimeDilation;

		// Get the simulator the <see cref="Primitive"/> originated from
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the <see cref="Primitive"/> details
		public final Primitive getPrim()
		{
			return m_Prim;
		}

		// true if the <see cref="Primitive"/> did not exist in the dictionary
		// before this update (always true if object tracking has been disabled)
		public final boolean getIsNew()
		{
			return m_IsNew;
		}

		// Get the simulator Time Dilation
		public final short getTimeDilation()
		{
			return m_TimeDilation;
		}

		/**
		 * Construct a new instance of the PrimEventArgs class
		 * 
		 * @param simulator
		 *            The simulator the object originated from
		 * @param prim
		 *            The Primitive
		 * @param timeDilation
		 *            The simulator time dilation
		 * @param isNew
		 *            The prim was not in the dictionary before this update
		 */
		public PrimCallbackArgs(Simulator simulator, Primitive prim, short timeDilation, boolean isNew)
		{
			this.m_Simulator = simulator;
			this.m_IsNew = isNew;
			this.m_Prim = prim;
			this.m_TimeDilation = timeDilation;
		}
	}

	/**
	 * Raised when the simulator sends us data containing
	 * 
	 * A <see cref="Primitive"/>, Foliage or Attachment {@link RequestObject}
	 * {@link RequestObjects}
	 */
	public CallbackHandler<PrimCallbackArgs> OnObjectUpdate = new CallbackHandler<PrimCallbackArgs>();

	/**
	 * Provides additional primitive data for the <see
	 * cref="ObjectManager.ObjectProperties"/> event
	 * <p>
	 * The <see cref="ObjectManager.ObjectProperties"/> event occurs when the
	 * simulator sends an <see cref="ObjectPropertiesPacket"/> containing
	 * additional details for a Primitive, Foliage data or Attachment data
	 * </p>
	 * <p>
	 * The <see cref="ObjectManager.ObjectProperties"/> event is also raised
	 * when a <see cref="ObjectManager.SelectObject"/> request is made.
	 * </p>
	 * 
	 * <example> The following code example uses the <see
	 * cref="PrimEventArgs.Prim"/>, <see cref="PrimEventArgs.Simulator"/> and
	 * <see cref="ObjectPropertiesEventArgs.Properties"/> properties to display
	 * new attachments and send a request for additional properties containing
	 * the name of the attachment then display it on the <see cref="Console"/>
	 * window. <code>
	 *     // Subscribe to the event that provides additional primitive details
	 *     Client.Objects.ObjectProperties += Objects_ObjectProperties;
	 * 
	 *     // handle the properties data that arrives
	 *     private void Objects_ObjectProperties(object sender, ObjectPropertiesEventArgs e)
	 *     {
	 *         Console.WriteLine("Primitive Properties: %s, Name is %s", e.Properties.ObjectID, e.Properties.Name);
	 *     }
	 * </code> </example>
	 */
	public class ObjectPropertiesCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final ObjectProperties m_Properties;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the primitive properties
		public final ObjectProperties getProperties()
		{
			return m_Properties;
		}

		/**
		 * Construct a new instance of the ObjectPropertiesEventArgs class
		 * 
		 * @param simulator
		 *            The simulator the object is located
		 * @param props
		 *            The primitive Properties
		 */
		public ObjectPropertiesCallbackArgs(Simulator simulator, ObjectProperties props)
		{
			this.m_Simulator = simulator;
			this.m_Properties = props;
		}
	}

	// Raised when the simulator sends us data containing
	// additional <seea cref="Primitive"/> information
	// {@link SelectObject}
	// {@link SelectObjects}
	public CallbackHandler<ObjectPropertiesCallbackArgs> OnObjectProperties = new CallbackHandler<ObjectPropertiesCallbackArgs>();

	/**
	 * Provides additional primitive data for the <see
	 * cref="ObjectManager.ObjectPropertiesUpdated"/> event
	 * <p>
	 * The <see cref="ObjectManager.ObjectPropertiesUpdated"/> event occurs when
	 * the simulator sends an <see cref="ObjectPropertiesPacket"/> containing
	 * additional details for a Primitive or Foliage data that is currently
	 * being tracked in the <see cref="Simulator.ObjectsPrimitives"/> dictionary
	 * </p>
	 * <p>
	 * The <see cref="ObjectManager.ObjectPropertiesUpdated"/> event is also
	 * raised when a <see cref="ObjectManager.SelectObject"/> request is made
	 * and <see cref="Settings.OBJECT_TRACKING"/> is enabled
	 * </p>
	 */
	public class ObjectPropertiesUpdatedCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final Primitive m_Prim;
		private final ObjectProperties m_Properties;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the primitive details
		public final Primitive getPrim()
		{
			return m_Prim;
		}

		// Get the primitive properties
		public final ObjectProperties getProperties()
		{
			return m_Properties;
		}

		/**
		 * Construct a new instance of the ObjectPropertiesUpdatedEvenrArgs
		 * class
		 * 
		 * @param simulator
		 *            The simulator the object is located
		 * @param prim
		 *            The Primitive
		 * @param props
		 *            The primitive Properties
		 */
		public ObjectPropertiesUpdatedCallbackArgs(Simulator simulator, Primitive prim, ObjectProperties props)
		{
			this.m_Simulator = simulator;
			this.m_Prim = prim;
			this.m_Properties = props;
		}
	}

	// Raised when the simulator sends us data containing
	// Primitive.ObjectProperties for an object we are currently tracking
	public CallbackHandler<ObjectPropertiesUpdatedCallbackArgs> OnObjectPropertiesUpdated = new CallbackHandler<ObjectPropertiesUpdatedCallbackArgs>();

	/**
	 * Provides additional primitive data, permissions and sale info for the
	 * <see cref="ObjectManager.ObjectPropertiesFamily"/> event
	 * <p>
	 * The <see cref="ObjectManager.ObjectPropertiesFamily"/> event occurs when
	 * the simulator sends an <see cref="ObjectPropertiesPacket"/> containing
	 * additional details for a Primitive, Foliage data or Attachment. This
	 * includes Permissions, Sale info, and other basic details on an object
	 * </p>
	 * <p>
	 * The <see cref="ObjectManager.ObjectProperties"/> event is also raised
	 * when a <see cref="ObjectManager.RequestObjectPropertiesFamily"/> request
	 * is made, the viewer equivalent is hovering the mouse cursor over an
	 * object
	 * </p>
	 * 
	 */
	public class ObjectPropertiesFamilyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final ObjectProperties m_Properties;
		private final ReportType m_Type;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		//
		public final ObjectProperties getProperties()
		{
			return m_Properties;
		}

		//
		public final ReportType getType()
		{
			return m_Type;
		}

		public ObjectPropertiesFamilyCallbackArgs(Simulator simulator, ObjectProperties props, ReportType type)
		{
			this.m_Simulator = simulator;
			this.m_Properties = props;
			this.m_Type = type;
		}
	}

	// Raised when the simulator sends us data containing
	// additional <seea cref="Primitive"/> and <see cref="Avatar"/> details
	// {@link RequestObjectPropertiesFamily}
	public CallbackHandler<ObjectPropertiesFamilyCallbackArgs> OnObjectPropertiesFamily = new CallbackHandler<ObjectPropertiesFamilyCallbackArgs>();

	/**
	 * Provides data for the <see cref="ObjectManager.OnAvatarUpdate"/> event
	 * <p>
	 * The <see cref="ObjectManager.OnAvatarUpdate"/> event occurs when the
	 * simulator sends an <see cref="ObjectUpdatePacket"/> containing Avatar
	 * data
	 * </p>
	 * <p>
	 * Note 1: The <see cref="ObjectManager.OnAvatarUpdate"/> event will not be
	 * raised when the object is an Avatar
	 * </p>
	 * <p>
	 * Note 2: It is possible for the <see cref="ObjectManager.OnAvatarUpdate"/>
	 * to be raised twice for the same avatar if for example the avatar moved to
	 * a new simulator, then returned to the current simulator
	 * </p>
	 * 
	 * <example> The following code example uses the <see
	 * cref="AvatarUpdateCallbackArgs.Avatar"/> property to make a request for
	 * the top picks using the <see cref="AvatarManager.RequestAvatarPicks"/>
	 * method in the <see cref="AvatarManager"/> class to display the names of
	 * our own agents picks listings on the <see cref="Console"/> window. <code>
	 *     // subscribe to the OnAvatarUpdate event to get our information
	 * 
	 *     CallbackHandler<AvatarUpdateCallbackArgs> cbu = new Objects_AvatarUpdate();
	 *     CallbackHandler<AvatarPicksReplyCallbackArgs> cba = new Objects_AvatarPicksReply();
	 *     Client.Objects.OnAvatarUpdate.add(cbu, false);
	 *     Client.Avatars.OnAvatarPicksReply.add(cba, true);
	 * 
	 *     private class Objects_AvatarUpdate implements CallbackHandler<AvatarUpdateCallbackArgs>
	 *     {
	 *     	   public void callback(AvatarUpdateCallbackArgs e)
	 *         {
	 *             // we only want our own data
	 *             if (e.Avatar.LocalID == Client.Self.LocalID)
	 *             {
	 *                 // Unsubscribe from the avatar update event to prevent a loop
	 *                 // where we continually request the picks every time we get an update for ourselves
	 *                 Client.Objects.OnAvatarUpdate.remove(cbu);
	 *                 // make the top picks request through AvatarManager
	 *                 Client.Avatars.RequestAvatarPicks(e.Avatar.ID);
	 *             }
	 *         }
	 *     }
	 * 
	 *     private class Avatars_AvatarPicksReply implements CallbackHandler<AvatarPicksReplyCallbackArgs>
	 *     {
	 *         public void callback(AvatarPicksReplyCallbackArgs e)
	 *         {
	 *             // we'll unsubscribe from the AvatarPicksReply event since we now have the data
	 *             // we were looking for
	 *             Client.Avatars.AvatarPicksReply.remove(cba);
	 *             // loop through the dictionary and extract the names of the top picks from our profile
	 *             for (String pickName : e.Picks.Values)
	 *             {
	 *                 Console.WriteLine(pickName);
	 *             }
	 *         }
	 *     }
	 * </code> </example> {@link ObjectManager.OnObjectUpdate}
	 * {@link PrimEventArgs}
	 */
	public class AvatarUpdateCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final Avatar m_Avatar;
		private final short m_TimeDilation;
		private final boolean m_IsNew;

		// Get the simulator the object originated from
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the <see cref="Avatar"/> data
		public final Avatar getAvatar()
		{
			return m_Avatar;
		}

		// Get the simulator time dilation
		public final short getTimeDilation()
		{
			return m_TimeDilation;
		}

		// true if the <see cref="Avatar"/> did not exist in the dictionary
		// before this update (always true if avatar tracking has been disabled)
		public final boolean getIsNew()
		{
			return m_IsNew;
		}

		/**
		 * Construct a new instance of the AvatarUpdateEventArgs class
		 * 
		 * @param simulator
		 *            The simulator the packet originated from
		 * @param avatar
		 *            The <see cref="Avatar"/> data
		 * @param timeDilation
		 *            The simulator time dilation
		 * @param isNew
		 *            The avatar was not in the dictionary before this update
		 */
		public AvatarUpdateCallbackArgs(Simulator simulator, Avatar avatar, short timeDilation, boolean isNew)
		{
			this.m_Simulator = simulator;
			this.m_Avatar = avatar;
			this.m_TimeDilation = timeDilation;
			this.m_IsNew = isNew;
		}
	}

	// Raised when the simulator sends us data containing updated information
	// for an <see cref="Avatar"/>
	public CallbackHandler<AvatarUpdateCallbackArgs> OnAvatarUpdate = new CallbackHandler<AvatarUpdateCallbackArgs>();

	/**
	 * Provides primitive data containing updated location, velocity, rotation,
	 * textures for the <see cref="ObjectManager.TerseObjectUpdate"/> event
	 * <p>
	 * The <see cref="ObjectManager.TerseObjectUpdate"/> event occurs when the
	 * simulator sends updated location, velocity, rotation, etc
	 * </p>
	 */
	public class TerseObjectUpdateCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final Primitive m_Prim;
		private final ObjectMovementUpdate m_Update;

		private final short m_TimeDilation;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the primitive details
		public final Primitive getPrim()
		{
			return m_Prim;
		}

		public final ObjectMovementUpdate getUpdate()
		{
			return m_Update;
		}

		public final short getTimeDilation()
		{
			return m_TimeDilation;
		}

		public TerseObjectUpdateCallbackArgs(Simulator simulator, Primitive prim, ObjectMovementUpdate update,
				short timeDilation)
		{
			this.m_Simulator = simulator;
			this.m_Prim = prim;
			this.m_Update = update;
			this.m_TimeDilation = timeDilation;
		}
	}

	// Raised when the simulator sends us data containing
	// <see cref="Primitive"/> and <see cref="Avatar"/> movement changes
	public CallbackHandler<TerseObjectUpdateCallbackArgs> OnTerseObjectUpdate = new CallbackHandler<TerseObjectUpdateCallbackArgs>();

	public class ObjectDataBlockUpdateCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final Primitive m_Prim;
		private final Primitive.ConstructionData m_ConstructionData;
		private final ObjectUpdatePacket.ObjectDataBlock m_Block;
		private final ObjectMovementUpdate m_Update;
		private final NameValue[] m_NameValues;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// Get the primitive details
		public final Primitive getPrim()
		{
			return m_Prim;
		}

		//
		public final Primitive.ConstructionData getConstructionData()
		{
			return m_ConstructionData;
		}

		//
		public final ObjectUpdatePacket.ObjectDataBlock getBlock()
		{
			return m_Block;
		}

		public final ObjectMovementUpdate getUpdate()
		{
			return m_Update;
		}

		public final NameValue[] getNameValues()
		{
			return m_NameValues;
		}

		public ObjectDataBlockUpdateCallbackArgs(Simulator simulator, Primitive prim,
				Primitive.ConstructionData constructionData, ObjectUpdatePacket.ObjectDataBlock block,
				ObjectMovementUpdate objectupdate, NameValue[] nameValues)
		{
			this.m_Simulator = simulator;
			this.m_Prim = prim;
			this.m_ConstructionData = constructionData;
			this.m_Block = block;
			this.m_Update = objectupdate;
			this.m_NameValues = nameValues;
		}
	}

	// Raised when the simulator sends us data containing updates to an Objects
	// DataBlock
	public CallbackHandler<ObjectDataBlockUpdateCallbackArgs> OnObjectDataBlockUpdate = new CallbackHandler<ObjectDataBlockUpdateCallbackArgs>();

	// Provides notification when an Avatar, Object or Attachment is DeRezzed or
	// moves out of the avatars view for the
	// <see cref="ObjectManager.KillObject"/> event
	public class KillObjectCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;

		private final int m_ObjectLocalID;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		// The LocalID of the object
		public final int getObjectLocalID()
		{
			return m_ObjectLocalID;
		}

		public KillObjectCallbackArgs(Simulator simulator, int objectID)
		{
			this.m_Simulator = simulator;
			this.m_ObjectLocalID = objectID;
		}
	}

	// Raised when the simulator informs us an <see cref="Primitive"/> or <see
	// cref="Avatar"/> is no longer within view
	public CallbackHandler<KillObjectCallbackArgs> OnKillObject = new CallbackHandler<KillObjectCallbackArgs>();

	// Provides updates sit position data
	public class AvatarSitChangedCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final Avatar m_Avatar;

		private final int m_SittingOn;

		private final int m_OldSeat;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public final Avatar getAvatar()
		{
			return m_Avatar;
		}

		public final int getSittingOn()
		{
			return m_SittingOn;
		}

		public final int getOldSeat()
		{
			return m_OldSeat;
		}

		public AvatarSitChangedCallbackArgs(Simulator simulator, Avatar avatar, int sittingOn, int oldSeat)
		{
			this.m_Simulator = simulator;
			this.m_Avatar = avatar;
			this.m_SittingOn = sittingOn;
			this.m_OldSeat = oldSeat;
		}
	}

	// Raised when the simulator sends us data containing updated sit
	// information for our <see cref="Avatar"/>
	public CallbackHandler<AvatarSitChangedCallbackArgs> OnAvatarSitChanged = new CallbackHandler<AvatarSitChangedCallbackArgs>();

	public class PayPriceReplyCallbackArgs implements CallbackArgs
	{
		private final Simulator m_Simulator;
		private final UUID m_ObjectID;
		private final int m_DefaultPrice;
		private final int[] m_ButtonPrices;

		// Get the simulator the object is located
		public final Simulator getSimulator()
		{
			return m_Simulator;
		}

		public final UUID getObjectID()
		{
			return m_ObjectID;
		}

		public final int getDefaultPrice()
		{
			return m_DefaultPrice;
		}

		public final int[] getButtonPrices()
		{
			return m_ButtonPrices;
		}

		public PayPriceReplyCallbackArgs(Simulator simulator, UUID objectID, int defaultPrice, int[] buttonPrices)
		{
			this.m_Simulator = simulator;
			this.m_ObjectID = objectID;
			this.m_DefaultPrice = defaultPrice;
			this.m_ButtonPrices = buttonPrices;
		}
	}

	// Raised when the simulator sends us data containing purchase price
	// information for a <see cref="Primitive"/>
	public CallbackHandler<PayPriceReplyCallbackArgs> OnPayPriceReply = new CallbackHandler<PayPriceReplyCallbackArgs>();

	public class ObjectMediaCallbackArgs implements CallbackArgs
	{
		private boolean m_Success;
		private String m_Version;
		private MediaEntry[] m_FaceMedia;

		// Indicates if the operation was successful
		public final boolean getSuccess()
		{
			return m_Success;
		}

		public final void setSuccess(boolean value)
		{
			m_Success = value;
		}

		// Media version string
		public final String getVersion()
		{
			return m_Version;
		}

		public final void setVersion(String value)
		{
			m_Version = value;
		}

		// Array of media entries indexed by face number
		public final MediaEntry[] getFaceMedia()
		{
			return m_FaceMedia;
		}

		public final void setFaceMedia(MediaEntry[] value)
		{
			m_FaceMedia = value;
		}

		public ObjectMediaCallbackArgs(boolean success, String version, MediaEntry[] faceMedia)
		{
			this.m_Success = success;
			this.m_Version = version;
			this.m_FaceMedia = faceMedia;
		}
	}

	// Set when simulator sends us infomation on primitive's physical properties
	public CallbackHandler<PhysicsPropertiesCallbackArgs> OnPhysicsProperties = new CallbackHandler<PhysicsPropertiesCallbackArgs>();

	public class PhysicsPropertiesCallbackArgs implements CallbackArgs
	{
		// Simulator where the message originated
		public Simulator Simulator;
		// Updated physical properties
		public PhysicsProperties PhysicsProperties;

		/**
		 * Constructor
		 * 
		 * @param sim
		 *            Simulator where the message originated
		 * @param props
		 *            Updated physical properties
		 */
		public PhysicsPropertiesCallbackArgs(Simulator sim, PhysicsProperties props)
		{
			Simulator = sim;
			PhysicsProperties = props;
		}
	}

	// /#region Internal event handlers

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs>
	{
		@Override
		public boolean callback(DisconnectedCallbackArgs args)
		{
			if (InterpolationTimer != null)
			{
				InterpolationTimer.cancel();
				InterpolationTimer = null;
			}
			return false;
		}
	}

	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs>
	{
		@Override
		public boolean callback(LoginProgressCallbackArgs args)
		{
			if (Client.Settings.USE_INTERPOLATION_TIMER)
			{
				InterpolationTimer = new Timer();
				InterpolationTimer.schedule(new InterpolationTimer_Elapsed(), Settings.INTERPOLATION_INTERVAL);
			}
			return false;
		}
	}

	private class InterpolationTimer_Elapsed extends TimerTask
	{
		@Override
		public void run()
		{
			long elapsed = 0;

			if (Client.Network.getConnected())
			{
				long start = System.currentTimeMillis();

				long interval = start - lastInterpolation;
				float seconds = interval / 1000f;

				ArrayList<Simulator> simulators = Client.Network.getSimulators();
				synchronized (simulators)
				{
					// Iterate through all of the simulators
					for (Simulator sim : simulators)
					{
						float adjSeconds = seconds * sim.Statistics.Dilation;

						// Iterate through all of this sims avatars

						// #region Linear Motion
						// Only do movement interpolation (extrapolation) when
						// there is a non-zero velocity but no acceleration
						// #endregion Linear Motion
						synchronized (sim.getObjectsAvatars())
						{
							for (Avatar avatar : sim.getObjectsAvatars().values())
							{
								if (avatar.Acceleration != Vector3.Zero && avatar.Velocity == Vector3.Zero)
								{
									// avatar.Position += (avatar.Velocity +
									// avatar.Acceleration * (0.5f * (adjSeconds -
									// HAVOK_TIMESTEP))) * adjSeconds;
									// avatar.Velocity += avatar.Acceleration *
									// adjSeconds;
									avatar.Position.add(Vector3.multiply(Vector3.add(avatar.Velocity,
											Vector3.multiply(avatar.Acceleration, (0.5f * (adjSeconds - HAVOK_TIMESTEP)))),
											adjSeconds));
									avatar.Velocity.add(Vector3.multiply(avatar.Acceleration, adjSeconds));
								}
							}
						}
						
						// Iterate through all of this sims primitives
						// #region Angular Velocity

						// #endregion Angular Velocity

						// #region Linear Motion
						// Only do movement interpolation (extrapolation) when
						// there is a non-zero velocity but no acceleration

						// #endregion Linear Motion
						// FIXME: Hinge movement extrapolation
						// FIXME: Point movement extrapolation
						synchronized (sim.getObjectsPrimitives())
						{
							for (Primitive prim : sim.getObjectsPrimitives().values())
							{
								switch (prim.Joint)
								{
									case Invalid:
										Vector3 angVel = prim.AngularVelocity;
										float omega = angVel.LengthSquared();
										if (omega > 0.00001f)
										{
											omega = (float) Math.sqrt(omega);
											float angle = omega * adjSeconds;
											angVel = Vector3.multiply(angVel, 1.0f / omega);
											Quaternion dQ = Quaternion.CreateFromAxisAngle(angVel, angle);
											prim.Rotation = Quaternion.multiply(prim.Rotation, dQ);
										}
										if (prim.Acceleration != Vector3.Zero && prim.Velocity == Vector3.Zero)
										{
											// prim.Position += (prim.Velocity +
											// prim.Acceleration * (0.5f *
											// (adjSeconds - HAVOK_TIMESTEP))) *
											// adjSeconds;
											// prim.Velocity += prim.Acceleration *
											// adjSeconds;
											prim.Position
													.add(Vector3.multiply(Vector3.add(prim.Velocity, Vector3.multiply(
															prim.Acceleration, (0.5f * (adjSeconds - HAVOK_TIMESTEP)))),
															adjSeconds));
											prim.Velocity.add(Vector3.multiply(prim.Acceleration, adjSeconds));
										}
										break;
									case Hinge:
									case Point:
										break;
									default:
										Logger.Log("Unhandled joint type " + prim.Joint, LogLevel.Warning, Client);
										break;
								}
							}
						}
					}

					// Make sure the last interpolated time is always updated
					lastInterpolation = System.currentTimeMillis();

					elapsed = lastInterpolation - start;
				}
			}

			// Start the timer again. Use a minimum of a 50ms pause in between
			// calculations
			int delay = Math.max(50, (int) (Settings.INTERPOLATION_INTERVAL - elapsed));
			if (InterpolationTimer != null)
			{
				InterpolationTimer.schedule(new InterpolationTimer_Elapsed(), delay);
			}
		}
	}

	private GridClient Client;
	// Does periodic dead reckoning calculation to convert
	// velocity and acceleration to new positions for objects
	private Timer InterpolationTimer;
	private long lastInterpolation;

	public ObjectManager(GridClient client)
	{
		Client = client;

		Client.Login.OnLoginProgress.add(new Network_OnLoginProgress());
		Client.Network.OnDisconnected.add(new Network_OnDisconnected());

		Client.Network.RegisterCallback(PacketType.ObjectUpdate, this);
		Client.Network.RegisterCallback(PacketType.ImprovedTerseObjectUpdate, this);
		Client.Network.RegisterCallback(PacketType.ObjectUpdateCompressed, this);
		Client.Network.RegisterCallback(PacketType.ObjectUpdateCached, this);
		Client.Network.RegisterCallback(PacketType.KillObject, this);
		Client.Network.RegisterCallback(PacketType.ObjectPropertiesFamily, this);
		Client.Network.RegisterCallback(PacketType.ObjectProperties, this);
		Client.Network.RegisterCallback(PacketType.PayPriceReply, this);

		Client.Network.RegisterCallback(CapsEventType.ObjectPhysicsProperties, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception
	{
		switch (packet.getType())
		{
			case ObjectUpdate:
				HandleObjectUpdate(packet, simulator);
				break;
			case ImprovedTerseObjectUpdate:
				HandleTerseObjectUpdate(packet, simulator);
				break;
			case ObjectUpdateCompressed:
				HandleObjectUpdateCompressed(packet, simulator);
				break;
			case ObjectUpdateCached:
				HandleObjectUpdateCached(packet, simulator);
				break;
			case KillObject:
				HandleKillObject(packet, simulator);
				break;
			case ObjectPropertiesFamily:
				HandleObjectPropertiesFamily(packet, simulator);
				break;
			case ObjectProperties:
				HandleObjectProperties(packet, simulator);
				break;
			case PayPriceReply:
				HandlePayPriceReply(packet, simulator);
				break;
		}
	}

	@Override
	public void capsCallback(IMessage message, Simulator simulator) throws Exception
	{
		switch (message.getType())
		{
			case ObjectPhysicsProperties:
				HandleObjectPhysicsProperties(message, simulator);
				break;
		}
	}

	/**
	 * Request information for a single object from a <see cref="Simulator"/>
	 * you are currently connected to
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public void RequestObject(Simulator simulator, int localID) throws Exception
	{
		RequestMultipleObjectsPacket request = new RequestMultipleObjectsPacket();
		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.ObjectData = new RequestMultipleObjectsPacket.ObjectDataBlock[1];
		request.ObjectData[0].ID = localID;
		request.ObjectData[0].CacheMissType = 0;

		simulator.SendPacket(request);
	}

	/**
	 * Request information for multiple objects contained in the same simulator
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @throws Exception
	 */
	public final void RequestObjects(Simulator simulator, int[] localIDs) throws Exception
	{
		RequestMultipleObjectsPacket request = new RequestMultipleObjectsPacket();
		request.AgentData.AgentID = Client.Self.getAgentID();
		request.AgentData.SessionID = Client.Self.getSessionID();
		request.ObjectData = new RequestMultipleObjectsPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			request.ObjectData[i] = request.new ObjectDataBlock();
			request.ObjectData[i].ID = localIDs[i];
			request.ObjectData[i].CacheMissType = 0;
		}
		simulator.SendPacket(request);
	}

	/**
	 * Attempt to purchase an original object, a copy, or the contents of an
	 * object
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param saleType
	 *            Whether the original, a copy, or the object contents are on
	 *            sale. This is used for verification, if the this sale type is
	 *            not valid for the object the purchase will fail
	 * @param price
	 *            Price of the object. This is used for verification, if it does
	 *            not match the actual price the purchase will fail
	 * @param groupID
	 *            Group ID that will be associated with the new purchase
	 * @param categoryID
	 *            Inventory folder UUID where the object or objects purchased
	 *            should be placed <example> <code>
	 *     BuyObject(Client.Network.CurrentSim, 500, SaleType.Copy,
	 *         100, UUID.Zero, Client.Self.InventoryRootFolderUUID);
	 *  </code> </example>
	 * @throws Exception
	 */
	public final void BuyObject(Simulator simulator, int localID, SaleType saleType, int price, UUID groupID,
			UUID categoryID) throws Exception
	{
		ObjectBuyPacket buy = new ObjectBuyPacket();

		buy.AgentData.AgentID = Client.Self.getAgentID();
		buy.AgentData.SessionID = Client.Self.getSessionID();
		buy.AgentData.GroupID = groupID;
		buy.AgentData.CategoryID = categoryID;

		buy.ObjectData = new ObjectBuyPacket.ObjectDataBlock[1];
		buy.ObjectData[0].ObjectLocalID = localID;
		buy.ObjectData[0].SaleType = (byte) saleType.ordinal();
		buy.ObjectData[0].SalePrice = price;

		simulator.SendPacket(buy);
	}

	/**
	 * Request prices that should be displayed in pay dialog. This will trigger
	 * the simulator to send us back a PayPriceReply which can be handled by
	 * OnPayPriceReply event
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param objectID
	 *            The ID of the object
	 * 
	 *            The result is raised in the <see cref="OnPayPriceReply"/>
	 *            event
	 */
	public final void RequestPayPrice(Simulator simulator, UUID objectID) throws Exception
	{
		RequestPayPricePacket payPriceRequest = new RequestPayPricePacket();
		payPriceRequest.ObjectID = objectID;
		simulator.SendPacket(payPriceRequest);
	}

	/**
	 * Select a single object. This will cause the <see cref="Simulator"/> to
	 * send us an <see cref="ObjectPropertiesPacket"/> which will raise the <see
	 * cref="OnObjectProperties"/> event
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObject(Simulator simulator, int localID) throws Exception
	{
		SelectObject(simulator, localID, true);
	}

	/**
	 * Select a single object. This will cause the <see cref="Simulator"/> to
	 * send us an <see cref="ObjectPropertiesPacket"/> which will raise the <see
	 * cref="OnObjectProperties"/> event
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param automaticDeselect
	 *            if true, a call to <see cref="DeselectObject"/> is made
	 *            immediately following the request
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObject(Simulator simulator, int localID, boolean automaticDeselect) throws Exception
	{
		ObjectSelectPacket select = new ObjectSelectPacket();

		select.AgentData.AgentID = Client.Self.getAgentID();
		select.AgentData.SessionID = Client.Self.getSessionID();

		select.ObjectLocalID = new int[1];
		select.ObjectLocalID[0] = localID;

		simulator.SendPacket(select);

		if (automaticDeselect)
		{
			DeselectObject(simulator, localID);
		}
	}

	/**
	 * Select multiple objects. This will cause the <see cref="Simulator"/> to
	 * send us an <see cref="ObjectPropertiesPacket"/> which will raise the <see
	 * cref="OnObjectProperties"/> event
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObjects(Simulator simulator, int[] localIDs) throws Exception
	{
		SelectObjects(simulator, localIDs, true);
	}

	/**
	 * Select multiple objects. This will cause the <see cref="Simulator"/> to
	 * send us an <see cref="ObjectPropertiesPacket"/> which will raise the <see
	 * cref="OnObjectProperties"/> event
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @param automaticDeselect
	 *            Should objects be deselected immediately after selection
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObjects(Simulator simulator, int[] localIDs, boolean automaticDeselect) throws Exception
	{
		ObjectSelectPacket select = new ObjectSelectPacket();

		select.AgentData.AgentID = Client.Self.getAgentID();
		select.AgentData.SessionID = Client.Self.getSessionID();

		select.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			select.ObjectLocalID[i] = localIDs[i];
		}

		simulator.SendPacket(select);

		if (automaticDeselect)
		{
			DeselectObjects(simulator, localIDs);
		}
	}

	/**
	 * Sets the sale properties of a single object
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param saleType
	 *            One of the options from the <see cref="SaleType"/> enum
	 * @param price
	 *            The price of the object
	 * @throws Exception
	 */
	public final void SetSaleInfo(Simulator simulator, int localID, SaleType saleType, int price) throws Exception
	{
		ObjectSaleInfoPacket sale = new ObjectSaleInfoPacket();
		sale.AgentData.AgentID = Client.Self.getAgentID();
		sale.AgentData.SessionID = Client.Self.getSessionID();
		sale.ObjectData = new ObjectSaleInfoPacket.ObjectDataBlock[1];
		sale.ObjectData[0] = sale.new ObjectDataBlock();
		sale.ObjectData[0].LocalID = localID;
		sale.ObjectData[0].SalePrice = price;
		sale.ObjectData[0].SaleType = (byte) saleType.ordinal();

		simulator.SendPacket(sale);
	}

	/**
	 * Sets the sale properties of multiple objects
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @param saleType
	 *            One of the options from the <see cref="SaleType"/> enum
	 * @param price
	 *            The price of the object
	 * @throws Exception
	 */
	public final void SetSaleInfo(Simulator simulator, int[] localIDs, SaleType saleType, int price) throws Exception
	{
		ObjectSaleInfoPacket sale = new ObjectSaleInfoPacket();
		sale.AgentData.AgentID = Client.Self.getAgentID();
		sale.AgentData.SessionID = Client.Self.getSessionID();
		sale.ObjectData = new ObjectSaleInfoPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			sale.ObjectData[i] = sale.new ObjectDataBlock();
			sale.ObjectData[i].LocalID = localIDs[i];
			sale.ObjectData[i].SalePrice = price;
			sale.ObjectData[i].SaleType = (byte) saleType.ordinal();
		}

		simulator.SendPacket(sale);
	}

	/**
	 * Deselect a single object
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public final void DeselectObject(Simulator simulator, int localID) throws Exception
	{
		ObjectDeselectPacket deselect = new ObjectDeselectPacket();

		deselect.AgentData.AgentID = Client.Self.getAgentID();
		deselect.AgentData.SessionID = Client.Self.getSessionID();

		deselect.ObjectLocalID = new int[1];
		deselect.ObjectLocalID[0] = localID;

		simulator.SendPacket(deselect);
	}

	/**
	 * Deselect multiple objects.
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @throws Exception
	 */
	public final void DeselectObjects(Simulator simulator, int[] localIDs) throws Exception
	{
		ObjectDeselectPacket deselect = new ObjectDeselectPacket();

		deselect.AgentData.AgentID = Client.Self.getAgentID();
		deselect.AgentData.SessionID = Client.Self.getSessionID();

		deselect.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			deselect.ObjectLocalID[i] = localIDs[i];
		}

		simulator.SendPacket(deselect);
	}

	/**
	 * Perform a click action on an object
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public final void ClickObject(Simulator simulator, int localID) throws Exception
	{
		ClickObject(simulator, localID, Vector3.Zero, Vector3.Zero, 0, Vector3.Zero, Vector3.Zero, Vector3.Zero);
	}

	/**
	 * Perform a click action (Grab) on a single object
	 * 
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param uvCoord
	 *            The texture coordinates to touch
	 * @param stCoord
	 *            The surface coordinates to touch
	 * @param faceIndex
	 *            The face of the position to touch
	 * @param position
	 *            The region coordinates of the position to touch
	 * @param normal
	 *            The surface normal of the position to touch (A normal is a
	 *            vector perpindicular to the surface)
	 * @param binormal
	 *            The surface binormal of the position to touch (A binormal is a
	 *            vector tangen to the surface pointing along the U direction of
	 *            the tangent space
	 * @throws Exception
	 */
	public final void ClickObject(Simulator simulator, int localID, Vector3 uvCoord, Vector3 stCoord, int faceIndex,
			Vector3 position, Vector3 normal, Vector3 binormal) throws Exception
	{
		ObjectGrabPacket grab = new ObjectGrabPacket();
		grab.AgentData.AgentID = Client.Self.getAgentID();
		grab.AgentData.SessionID = Client.Self.getSessionID();
		grab.ObjectData.GrabOffset = Vector3.Zero;
		grab.ObjectData.LocalID = localID;
		grab.SurfaceInfo = new ObjectGrabPacket.SurfaceInfoBlock[1];
		grab.SurfaceInfo[0] = grab.new SurfaceInfoBlock();
		grab.SurfaceInfo[0].UVCoord = uvCoord;
		grab.SurfaceInfo[0].STCoord = stCoord;
		grab.SurfaceInfo[0].FaceIndex = faceIndex;
		grab.SurfaceInfo[0].Position = position;
		grab.SurfaceInfo[0].Normal = normal;
		grab.SurfaceInfo[0].Binormal = binormal;

		simulator.SendPacket(grab);

		// TODO: If these hit the server out of order the click will fail
		// and we'll be grabbing the object
		Thread.sleep(50);

		ObjectDeGrabPacket degrab = new ObjectDeGrabPacket();
		degrab.AgentData.AgentID = Client.Self.getAgentID();
		degrab.AgentData.SessionID = Client.Self.getSessionID();
		degrab.LocalID = localID;
		degrab.SurfaceInfo = new ObjectDeGrabPacket.SurfaceInfoBlock[1];
		degrab.SurfaceInfo[0] = degrab.new SurfaceInfoBlock();
		degrab.SurfaceInfo[0].UVCoord = uvCoord;
		degrab.SurfaceInfo[0].STCoord = stCoord;
		degrab.SurfaceInfo[0].FaceIndex = faceIndex;
		degrab.SurfaceInfo[0].Position = position;
		degrab.SurfaceInfo[0].Normal = normal;
		degrab.SurfaceInfo[0].Binormal = binormal;

		simulator.SendPacket(degrab);
	}

	/**
	 * Create (rez) a new prim object in a simulator
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object to
	 *            place the object in
	 * @param prim
	 *            Data describing the prim object to rez
	 * @param groupID
	 *            Group ID that this prim will be set to, or UUID.Zero if you do
	 *            not want the object to be associated with a specific group
	 * @param position
	 *            An approximation of the position at which to rez the prim
	 * @param scale
	 *            Scale vector to size this prim
	 * @param rotation
	 *            Rotation quaternion to rotate this prim Due to the way client
	 *            prim rezzing is done on the server, the requested position for
	 *            an object is only close to where the prim actually ends up. If
	 *            you desire exact placement you'll need to follow up by moving
	 *            the object after it has been created. This function will not
	 *            set textures, light and flexible data, or other extended
	 *            primitive properties
	 * @throws Exception
	 */
	public final void AddPrim(Simulator simulator, Primitive.ConstructionData prim, UUID groupID, Vector3 position,
			Vector3 scale, Quaternion rotation) throws Exception
	{
		AddPrim(simulator, prim, groupID, position, scale, rotation, PrimFlags.CreateSelected);
	}

	/**
	 * Create (rez) a new prim object in a simulator
	 * 
	 * @param simulator
	 *            A reference to the {@link Simulator} object to place the
	 *            object in
	 * @param prim
	 *            Data describing the prim object to rez
	 * @param groupID
	 *            Group ID that this prim will be set to, or UUID.Zero if you do
	 *            not want the object to be associated with a specific group
	 * @param position
	 *            An approximation of the position at which to rez the prim
	 * @param scale
	 *            Scale vector to size this prim
	 * @param rotation
	 *            Rotation quaternion to rotate this prim
	 * @param createFlags
	 *            Specify the {@link PrimFlags} Due to the way client prim
	 *            rezzing is done on the server, the requested position for an
	 *            object is only close to where the prim actually ends up. If
	 *            you desire exact placement you'll need to follow up by moving
	 *            the object after it has been created. This function will not
	 *            set textures, light and flexible data, or other extended
	 *            primitive properties
	 * @throws Exception
	 */
	public final void AddPrim(Simulator simulator, ConstructionData prim, UUID groupID, Vector3 position,
			Vector3 scale, Quaternion rotation, int createFlags) throws Exception
	{
		ObjectAddPacket packet = new ObjectAddPacket();

		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.SessionID = Client.Self.getSessionID();
		packet.AgentData.GroupID = groupID;

		packet.ObjectData.State = prim.State;
		packet.ObjectData.AddFlags = createFlags;
		packet.ObjectData.PCode = PCode.Prim.getValue();

		packet.ObjectData.Material = (byte) prim.Material.ordinal();
		packet.ObjectData.Scale = scale;
		packet.ObjectData.Rotation = rotation;

		packet.ObjectData.PathCurve = prim.PathCurve.getValue();
		packet.ObjectData.PathBegin = Primitive.PackBeginCut(prim.PathBegin);
		packet.ObjectData.PathEnd = Primitive.PackEndCut(prim.PathEnd);
		packet.ObjectData.PathRadiusOffset = Primitive.PackPathTwist(prim.PathRadiusOffset);
		packet.ObjectData.PathRevolutions = Primitive.PackPathRevolutions(prim.PathRevolutions);
		packet.ObjectData.PathScaleX = Primitive.PackPathScale(prim.PathScaleX);
		packet.ObjectData.PathScaleY = Primitive.PackPathScale(prim.PathScaleY);
		packet.ObjectData.PathShearX = Primitive.PackPathShear(prim.PathShearX);
		packet.ObjectData.PathShearY = Primitive.PackPathShear(prim.PathShearY);
		packet.ObjectData.PathSkew = Primitive.PackPathTwist(prim.PathSkew);
		packet.ObjectData.PathTaperX = Primitive.PackPathTaper(prim.PathTaperX);
		packet.ObjectData.PathTaperY = Primitive.PackPathTaper(prim.PathTaperY);
		packet.ObjectData.PathTwist = Primitive.PackPathTwist(prim.PathTwist);
		packet.ObjectData.PathTwistBegin = Primitive.PackPathTwist(prim.PathTwistBegin);

		packet.ObjectData.ProfileCurve = prim.ProfileCurve.getValue();
		packet.ObjectData.ProfileBegin = Primitive.PackBeginCut(prim.ProfileBegin);
		packet.ObjectData.ProfileEnd = Primitive.PackEndCut(prim.ProfileEnd);
		packet.ObjectData.ProfileHollow = Primitive.PackProfileHollow(prim.ProfileHollow);

		packet.ObjectData.RayStart = position;
		packet.ObjectData.RayEnd = position;
		packet.ObjectData.RayEndIsIntersection = 0;
		packet.ObjectData.RayTargetID = UUID.Zero;
		packet.ObjectData.BypassRaycast = 1;

		simulator.SendPacket(packet);
	}

	/**
	 * Rez a Linden tree
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param scale
	 *            The size of the tree
	 * @param rotation
	 *            The rotation of the tree
	 * @param position
	 *            The position of the tree
	 * @param treeType
	 *            The Type of tree
	 * @param groupOwner
	 *            The {@link UUID} of the group to set the tree to, or UUID.Zero
	 *            if no group is to be set
	 * @param newTree
	 *            true to use the "new" Linden trees, false to use the old
	 * @throws Exception
	 */
	public final void AddTree(Simulator simulator, Vector3 scale, Quaternion rotation, Vector3 position, Tree treeType,
			UUID groupOwner, boolean newTree) throws Exception
	{
		ObjectAddPacket add = new ObjectAddPacket();

		add.AgentData.AgentID = Client.Self.getAgentID();
		add.AgentData.SessionID = Client.Self.getSessionID();
		add.AgentData.GroupID = groupOwner;
		add.ObjectData.BypassRaycast = 1;
		add.ObjectData.Material = 3;
		add.ObjectData.PathCurve = 16;
		add.ObjectData.PCode = newTree ? (byte) PCode.NewTree.getValue() : (byte) PCode.Tree.getValue();
		add.ObjectData.RayEnd = position;
		add.ObjectData.RayStart = position;
		add.ObjectData.RayTargetID = UUID.Zero;
		add.ObjectData.Rotation = rotation;
		add.ObjectData.Scale = scale;
		add.ObjectData.State = (byte) treeType.ordinal();

		simulator.SendPacket(add);
	}

	/**
	 * Rez grass and ground cover
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param scale
	 *            The size of the grass
	 * @param rotation
	 *            The rotation of the grass
	 * @param position
	 *            The position of the grass
	 * @param grassType
	 *            The type of grass from the {@link Grass} enum
	 * @param groupOwner
	 *            The {@link UUID} of the group to set the tree to, or UUID.Zero
	 *            if no group is to be set
	 * @throws Exception
	 */
	public final void AddGrass(Simulator simulator, Vector3 scale, Quaternion rotation, Vector3 position,
			Grass grassType, UUID groupOwner) throws Exception
	{
		ObjectAddPacket add = new ObjectAddPacket();

		add.AgentData.AgentID = Client.Self.getAgentID();
		add.AgentData.SessionID = Client.Self.getSessionID();
		add.AgentData.GroupID = groupOwner;
		add.ObjectData.BypassRaycast = 1;
		add.ObjectData.Material = 3;
		add.ObjectData.PathCurve = 16;
		add.ObjectData.PCode = PCode.Grass.getValue();
		add.ObjectData.RayEnd = position;
		add.ObjectData.RayStart = position;
		add.ObjectData.RayTargetID = UUID.Zero;
		add.ObjectData.Rotation = rotation;
		add.ObjectData.Scale = scale;
		add.ObjectData.State = (byte) grassType.ordinal();

		simulator.SendPacket(add);
	}

	/**
	 * Set the textures to apply to the faces of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param textures
	 *            The texture data to apply
	 * @throws Exception
	 * @throws IOException
	 */
	public final void SetTextures(Simulator simulator, int localID, TextureEntry textures) throws IOException,
			Exception
	{
		SetTextures(simulator, localID, textures, Helpers.EmptyString);
	}

	/**
	 * Set the textures to apply to the faces of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param textures
	 *            The texture data to apply
	 * @param mediaUrl
	 *            A media URL (not used)
	 * @throws Exception
	 */
	public final void SetTextures(Simulator simulator, int localID, TextureEntry textures, String mediaUrl)
			throws Exception
	{
		ObjectImagePacket image = new ObjectImagePacket();

		image.AgentData.AgentID = Client.Self.getAgentID();
		image.AgentData.SessionID = Client.Self.getSessionID();
		image.ObjectData = new ObjectImagePacket.ObjectDataBlock[1];
		image.ObjectData[0] = image.new ObjectDataBlock();
		image.ObjectData[0].ObjectLocalID = localID;
		image.ObjectData[0].setTextureEntry(textures.GetBytes());
		image.ObjectData[0].setMediaURL(Helpers.StringToBytes(mediaUrl));

		simulator.SendPacket(image);
	}

	/**
	 * Set the Light data on an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param light
	 *            A {@link Primitive.LightData} object containing the data to
	 *            set
	 * @throws Exception
	 */
	public final void SetLight(Simulator simulator, int localID, LightData light) throws Exception
	{
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = Client.Self.getAgentID();
		extra.AgentData.SessionID = Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Light.getValue();
		if (light.Intensity == 0.0f)
		{
			// Disables the light if intensity is 0
			extra.ObjectData[0].ParamInUse = false;
		}
		else
		{
			extra.ObjectData[0].ParamInUse = true;
		}
		extra.ObjectData[0].setParamData(light.GetBytes());
		extra.ObjectData[0].ParamSize = light.GetBytes().length;

		simulator.SendPacket(extra);
	}

	/**
	 * Set the flexible data on an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param flexible
	 *            A {@link Primitive.FlexibleData} object containing the data to
	 *            set
	 * @throws Exception
	 */
	public final void SetFlexible(Simulator simulator, int localID, FlexibleData flexible) throws Exception
	{
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = Client.Self.getAgentID();
		extra.AgentData.SessionID = Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Flexible.getValue();
		extra.ObjectData[0].ParamInUse = true;
		extra.ObjectData[0].setParamData(flexible.GetBytes());
		extra.ObjectData[0].ParamSize = flexible.GetBytes().length;

		simulator.SendPacket(extra);
	}

	/**
	 * Set the sculptie texture and data on an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param sculpt
	 *            A {@link Primitive.SculptData} object containing the data to
	 *            set
	 * @throws Exception
	 */
	public final void SetSculpt(Simulator simulator, int localID, SculptData sculpt) throws Exception
	{
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = Client.Self.getAgentID();
		extra.AgentData.SessionID = Client.Self.getSessionID();

		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Sculpt.getValue();
		extra.ObjectData[0].ParamInUse = true;
		extra.ObjectData[0].setParamData(sculpt.GetBytes());
		extra.ObjectData[0].ParamSize = sculpt.GetBytes().length;

		simulator.SendPacket(extra);

		// Not sure why, but if you don't send this the sculpted prim disappears
		ObjectShapePacket shape = new ObjectShapePacket();

		shape.AgentData.AgentID = Client.Self.getAgentID();
		shape.AgentData.SessionID = Client.Self.getSessionID();

		shape.ObjectData = new ObjectShapePacket.ObjectDataBlock[1];
		shape.ObjectData[0] = shape.new ObjectDataBlock();
		shape.ObjectData[0].ObjectLocalID = localID;
		shape.ObjectData[0].PathScaleX = 100;
		shape.ObjectData[0].PathScaleY = (byte) 150;
		shape.ObjectData[0].PathCurve = 32;

		simulator.SendPacket(shape);
	}

	/**
	 * Unset additional primitive parameters on an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param type
	 *            The extra parameters to set
	 * @throws Exception
	 */
	public final void SetExtraParamOff(Simulator simulator, int localID, ExtraParamType type) throws Exception
	{
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = Client.Self.getAgentID();
		extra.AgentData.SessionID = Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = type.getValue();
		extra.ObjectData[0].ParamInUse = false;
		extra.ObjectData[0].setParamData(Helpers.EmptyBytes);
		extra.ObjectData[0].ParamSize = 0;

		simulator.SendPacket(extra);
	}

	/**
	 * Link multiple prims into a linkset
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to link The
	 *            last object in the array will be the root object of the
	 *            linkset TODO: Is this true?
	 * @throws Exception
	 */
	public final void LinkPrims(Simulator simulator, int[] localIDs) throws Exception
	{
		ObjectLinkPacket packet = new ObjectLinkPacket();

		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.SessionID = Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			packet.ObjectLocalID[i] = localIDs[i];
		}

		simulator.SendPacket(packet);
	}

	/**
	 * Delink/Unlink multiple prims from a linkset
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to delink
	 * @throws Exception
	 */
	public final void DelinkPrims(Simulator simulator, int[] localIDs) throws Exception
	{
		ObjectDelinkPacket packet = new ObjectDelinkPacket();

		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.SessionID = Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i)
		{
			packet.ObjectLocalID[i] = localIDs[i];
		}

		simulator.SendPacket(packet);
	}

	/**
	 * Change the rotation of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param rotation
	 *            The new rotation of the object
	 * @throws Exception
	 */
	public final void SetRotation(Simulator simulator, int localID, Quaternion rotation) throws Exception
	{
		ObjectRotationPacket objRotPacket = new ObjectRotationPacket();
		objRotPacket.AgentData.AgentID = Client.Self.getAgentID();
		objRotPacket.AgentData.SessionID = Client.Self.getSessionID();

		objRotPacket.ObjectData = new ObjectRotationPacket.ObjectDataBlock[1];

		objRotPacket.ObjectData[0] = objRotPacket.new ObjectDataBlock();
		objRotPacket.ObjectData[0].ObjectLocalID = localID;
		objRotPacket.ObjectData[0].Rotation = rotation;
		simulator.SendPacket(objRotPacket);
	}

	/**
	 * Set the name of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param name
	 *            A string containing the new name of the object
	 * @throws Exception
	 */
	public final void SetName(Simulator simulator, int localID, String name) throws Exception
	{
		SetNames(simulator, new int[] { localID }, new String[] { name });
	}

	/**
	 * Set the name of multiple objects
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to change the
	 *            name of
	 * @param names
	 *            An array which contains the new names of the objects
	 * @throws Exception
	 */
	public final void SetNames(Simulator simulator, int[] localIDs, String[] names) throws Exception
	{
		ObjectNamePacket namePacket = new ObjectNamePacket();
		namePacket.AgentData.AgentID = Client.Self.getAgentID();
		namePacket.AgentData.SessionID = Client.Self.getSessionID();

		namePacket.ObjectData = new ObjectNamePacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i)
		{
			namePacket.ObjectData[i] = namePacket.new ObjectDataBlock();
			namePacket.ObjectData[i].LocalID = localIDs[i];
			namePacket.ObjectData[i].setName(Helpers.StringToBytes(names[i]));
		}

		simulator.SendPacket(namePacket);
	}

	/**
	 * Set the description of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param description
	 *            A string containing the new description of the object
	 * @throws Exception
	 */
	public final void SetDescription(Simulator simulator, int localID, String description) throws Exception
	{
		SetDescriptions(simulator, new int[] { localID }, new String[] { description });
	}

	/**
	 * Set the descriptions of multiple objects
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to change the
	 *            description of
	 * @param descriptions
	 *            An array which contains the new descriptions of the objects
	 * @throws Exception
	 */
	public final void SetDescriptions(Simulator simulator, int[] localIDs, String[] descriptions) throws Exception
	{
		ObjectDescriptionPacket descPacket = new ObjectDescriptionPacket();
		descPacket.AgentData.AgentID = Client.Self.getAgentID();
		descPacket.AgentData.SessionID = Client.Self.getSessionID();

		descPacket.ObjectData = new ObjectDescriptionPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i)
		{
			descPacket.ObjectData[i] = descPacket.new ObjectDataBlock();
			descPacket.ObjectData[i].LocalID = localIDs[i];
			descPacket.ObjectData[i].setDescription(Helpers.StringToBytes(descriptions[i]));
		}

		simulator.SendPacket(descPacket);
	}

	/**
	 * Attach an object to this avatar
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param attachPoint
	 *            The point on the avatar the object will be attached
	 * @param rotation
	 *            The rotation of the attached object
	 * @throws Exception
	 */
	public final void AttachObject(Simulator simulator, int localID, Primitive.AttachmentPoint attachPoint,
			Quaternion rotation) throws Exception
	{
		ObjectAttachPacket attach = new ObjectAttachPacket();
		attach.AgentData.AgentID = Client.Self.getAgentID();
		attach.AgentData.SessionID = Client.Self.getSessionID();
		attach.AgentData.AttachmentPoint = (byte) attachPoint.ordinal();

		attach.ObjectData = new ObjectAttachPacket.ObjectDataBlock[1];
		attach.ObjectData[0] = attach.new ObjectDataBlock();
		attach.ObjectData[0].ObjectLocalID = localID;
		attach.ObjectData[0].Rotation = rotation;

		simulator.SendPacket(attach);
	}

	/**
	 * Drop an attached object from this avatar
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside. This will always be the simulator
	 *            the avatar is currently in
	 * 
	 * @param localID
	 *            The object's ID which is local to the simulator the object is
	 *            in
	 * @throws Exception
	 */
	public final void DropObject(Simulator simulator, int localID) throws Exception
	{
		ObjectDropPacket dropit = new ObjectDropPacket();
		dropit.AgentData.AgentID = Client.Self.getAgentID();
		dropit.AgentData.SessionID = Client.Self.getSessionID();
		dropit.ObjectLocalID = new int[1];
		dropit.ObjectLocalID[0] = localID;

		simulator.SendPacket(dropit);
	}

	/**
	 * Detach an object from yourself
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * 
	 *            This will always be the simulator the avatar is currently in
	 * 
	 * @param localIDs
	 *            An array which contains the IDs of the objects to detach
	 * @throws Exception
	 */
	public final void DetachObjects(Simulator simulator, int[] localIDs) throws Exception
	{
		ObjectDetachPacket detach = new ObjectDetachPacket();
		detach.AgentData.AgentID = Client.Self.getAgentID();
		detach.AgentData.SessionID = Client.Self.getSessionID();
		detach.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			detach.ObjectLocalID[i] = localIDs[i];
		}

		simulator.SendPacket(detach);
	}

	/**
	 * Change the position of an object, Will change position of entire linkset
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param position
	 *            The new position of the object
	 * @throws Exception
	 */
	public final void SetPosition(Simulator simulator, int localID, Vector3 position) throws Exception
	{
		byte type = UpdateType.Position | UpdateType.Linked;
		UpdateObject(simulator, localID, position, type);
	}

	/**
	 * Change the position of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param position
	 *            The new position of the object
	 * @param childOnly
	 *            if true, will change position of (this) child prim only, not
	 *            entire linkset
	 * @throws Exception
	 */
	public final void SetPosition(Simulator simulator, int localID, Vector3 position, boolean childOnly)
			throws Exception
	{
		byte type = UpdateType.Position;

		if (!childOnly)
		{
			type |= UpdateType.Linked;
		}

		UpdateObject(simulator, localID, position, type);
	}

	/**
	 * Change the Scale (size) of an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param scale
	 *            The new scale of the object
	 * @param childOnly
	 *            If true, will change scale of this prim only, not entire
	 *            linkset
	 * @param uniform
	 *            True to resize prims uniformly
	 * @throws Exception
	 */
	public final void SetScale(Simulator simulator, int localID, Vector3 scale, boolean childOnly, boolean uniform)
			throws Exception
	{
		byte type = UpdateType.Scale;

		if (!childOnly)
		{
			type |= UpdateType.Linked;
		}

		if (uniform)
		{
			type |= UpdateType.Uniform;
		}

		UpdateObject(simulator, localID, scale, type);
	}

	/**
	 * Change the Rotation of an object that is either a child or a whole
	 * linkset
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param quat
	 *            The new scale of the object
	 * @param childOnly
	 *            If true, will change rotation of this prim only, not entire
	 *            linkset
	 * @throws Exception
	 */
	public final void SetRotation(Simulator simulator, int localID, Quaternion quat, boolean childOnly)
			throws Exception
	{
		byte type = UpdateType.Rotation;

		if (!childOnly)
		{
			type |= UpdateType.Linked;
		}

		MultipleObjectUpdatePacket multiObjectUpdate = new MultipleObjectUpdatePacket();
		multiObjectUpdate.AgentData.AgentID = Client.Self.getAgentID();
		multiObjectUpdate.AgentData.SessionID = Client.Self.getSessionID();

		multiObjectUpdate.ObjectData = new MultipleObjectUpdatePacket.ObjectDataBlock[1];

		multiObjectUpdate.ObjectData[0] = multiObjectUpdate.new ObjectDataBlock();
		multiObjectUpdate.ObjectData[0].Type = type;
		multiObjectUpdate.ObjectData[0].ObjectLocalID = localID;
		multiObjectUpdate.ObjectData[0].setData(quat.GetBytes());

		simulator.SendPacket(multiObjectUpdate);
	}

	/**
	 * Send a Multiple Object Update packet to change the size, scale or
	 * rotation of a primitive
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param data
	 *            The new rotation, size, or position of the target object
	 * @param type
	 *            The flags from the {@link UpdateType} Enum
	 * @throws Exception
	 */
	public final void UpdateObject(Simulator simulator, int localID, Vector3 data, byte type) throws Exception
	{
		MultipleObjectUpdatePacket multiObjectUpdate = new MultipleObjectUpdatePacket();
		multiObjectUpdate.AgentData.AgentID = Client.Self.getAgentID();
		multiObjectUpdate.AgentData.SessionID = Client.Self.getSessionID();

		multiObjectUpdate.ObjectData = new MultipleObjectUpdatePacket.ObjectDataBlock[1];

		multiObjectUpdate.ObjectData[0] = multiObjectUpdate.new ObjectDataBlock();
		multiObjectUpdate.ObjectData[0].Type = type;
		multiObjectUpdate.ObjectData[0].ObjectLocalID = localID;
		multiObjectUpdate.ObjectData[0].setData(data.GetBytes());

		simulator.SendPacket(multiObjectUpdate);
	}

	/**
	 * Deed an object (prim) to a group, Object must be shared with group which
	 * can be accomplished with SetPermissions()
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is
	 *            in
	 * @param groupOwner
	 *            The {@link UUID} of the group to deed the object to
	 * @throws Exception
	 */
	public final void DeedObject(Simulator simulator, int localID, UUID groupOwner) throws Exception
	{
		ObjectOwnerPacket objDeedPacket = new ObjectOwnerPacket();
		objDeedPacket.AgentData.AgentID = Client.Self.getAgentID();
		objDeedPacket.AgentData.SessionID = Client.Self.getSessionID();

		// Can only be use in God mode
		objDeedPacket.HeaderData.Override = false;
		objDeedPacket.HeaderData.OwnerID = UUID.Zero;
		objDeedPacket.HeaderData.GroupID = groupOwner;

		objDeedPacket.ObjectLocalID = new int[1];

		objDeedPacket.ObjectLocalID[0] = localID;

		simulator.SendPacket(objDeedPacket);
	}

	/**
	 * Deed multiple objects (prims) to a group, Objects must be shared with
	 * group which can be accomplished with SetPermissions()
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param localIDs
	 *            An array which contains the IDs of the objects to deed
	 * @param groupOwner
	 *            The {@link UUID} of the group to deed the object to
	 * @throws Exception
	 */
	public final void DeedObjects(Simulator simulator, int[] localIDs, UUID groupOwner) throws Exception
	{
		ObjectOwnerPacket packet = new ObjectOwnerPacket();
		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.SessionID = Client.Self.getSessionID();

		// Can only be use in God mode
		packet.HeaderData.Override = false;
		packet.HeaderData.OwnerID = UUID.Zero;
		packet.HeaderData.GroupID = groupOwner;

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			packet.ObjectLocalID[i] = localIDs[i];
		}
		simulator.SendPacket(packet);
	}

	/**
	 * Set the permissions on multiple objects
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to set the
	 *            permissions on
	 * @param who
	 *            The new Who mask to set
	 * @param permissions
	 *            The new Permissions mark to set
	 * @param set
	 *            TODO: What does this do?
	 * @throws Exception
	 */
	public final void SetPermissions(Simulator simulator, int[] localIDs, byte who, int permissions, boolean set)
			throws Exception
	{
		ObjectPermissionsPacket packet = new ObjectPermissionsPacket();

		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.SessionID = Client.Self.getSessionID();

		// Override can only be used by gods
		packet.Override = false;

		packet.ObjectData = new ObjectPermissionsPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++)
		{
			packet.ObjectData[i] = packet.new ObjectDataBlock();

			packet.ObjectData[i].ObjectLocalID = localIDs[i];
			packet.ObjectData[i].Field = who;
			packet.ObjectData[i].Mask = permissions;
			packet.ObjectData[i].Set = (byte) (set ? 1 : 0);
		}

		simulator.SendPacket(packet);
	}

	/**
	 * Request additional properties for an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param objectID
	 * @throws Exception
	 */
	public final void RequestObjectPropertiesFamily(Simulator simulator, UUID objectID) throws Exception
	{
		RequestObjectPropertiesFamily(simulator, objectID, true);
	}

	/**
	 * Request additional properties for an object
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the object resides
	 * @param objectID
	 *            Absolute UUID of the object
	 * @param reliable
	 *            Whether to require server acknowledgement of this request
	 * @throws Exception
	 */
	public final void RequestObjectPropertiesFamily(Simulator simulator, UUID objectID, boolean reliable)
			throws Exception
	{
		RequestObjectPropertiesFamilyPacket properties = new RequestObjectPropertiesFamilyPacket();
		properties.AgentData.AgentID = Client.Self.getAgentID();
		properties.AgentData.SessionID = Client.Self.getSessionID();
		properties.ObjectData.ObjectID = objectID;
		// TODO: RequestFlags is typically only for bug report submissions, but
		// we might be able to
		// use it to pass an arbitrary uint back to the callback
		properties.ObjectData.RequestFlags = 0;

		properties.getHeader().setReliable(reliable);

		simulator.SendPacket(properties);
	}

	/**
	 * Set the ownership of a list of objects to the specified group
	 * 
	 * @param simulator
	 *            A reference to the {@link OpenMetaverse.Simulator} object
	 *            where the objects reside
	 * @param localIds
	 *            An array which contains the IDs of the objects to set the
	 *            group id on
	 * @param groupID
	 *            The Groups ID
	 * @throws Exception
	 */
	public final void SetObjectsGroup(Simulator simulator, int[] localIds, UUID groupID) throws Exception
	{
		ObjectGroupPacket packet = new ObjectGroupPacket();
		packet.AgentData.AgentID = Client.Self.getAgentID();
		packet.AgentData.GroupID = groupID;
		packet.AgentData.SessionID = Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIds.length];
		for (int i = 0; i < localIds.length; i++)
		{
			packet.ObjectLocalID[i] = localIds[i];
		}
		simulator.SendPacket(packet);
	}

	/**
	 * Find the object with localID in the simulator and add it with fullID if
	 * it is not there
	 * 
	 * @param simulator
	 *            The simulator in which the object is located
	 * @param localID
	 *            The simulator localID for this object
	 * @param fullID
	 *            The full object ID used to add a new object to the simulator
	 *            list, when the object could not be found.
	 * @return the object that corresponds to the localID
	 */
	protected final Primitive GetPrimitive(Simulator simulator, int localID, UUID fullID)
	{
		if (Client.Settings.OBJECT_TRACKING)
		{
			synchronized (simulator.getObjectsPrimitives())
			{
				Primitive prim = simulator.getObjectsPrimitives().get(localID);
				if (prim != null)
				{
					return prim;
				}

				prim = new Primitive();
				prim.LocalID = localID;
				prim.ID = fullID;
				prim.RegionHandle = simulator.getHandle();

				simulator.getObjectsPrimitives().put(localID, prim);

				return prim;
			}
		}
		return new Primitive();
	}

	/**
	 * Find the avatar with localID in the simulator and add it with fullID if
	 * it is not there
	 * 
	 * @param simulator
	 *            The simulator in which the avatar is located
	 * @param localID
	 *            The simulator localID for this avatar
	 * @param fullID
	 *            The full avatar ID used to add a new avatar object to the
	 *            simulator list, when the avatar could not be found.
	 * @return the avatar object that corresponds to the localID
	 */
	protected final Avatar GetAvatar(Simulator simulator, int localID, UUID fullID)
	{
		if (Client.Settings.AVATAR_TRACKING)
		{
			synchronized (simulator.getObjectsAvatars())
			{
				Avatar avatar = simulator.getObjectsAvatars().get(localID);

				if (avatar != null)
				{
					return avatar;
				}

				avatar = new Avatar();
				avatar.LocalID = localID;
				avatar.ID = fullID;
				avatar.RegionHandle = simulator.getHandle();

				simulator.getObjectsAvatars().put(localID, avatar);

				return avatar;
			}
		}
		return new Avatar();
	}

	protected final void SetAvatarSittingOn(Simulator sim, Avatar av, int localid, int oldSeatID)
	{
		if (Client.Network.getCurrentSim() == sim && av.LocalID == Client.Self.getLocalID())
		{
			Client.Self.setSittingOn(localid);
		}

		av.ParentID = localid;

		OnAvatarSitChanged.dispatch(new AvatarSitChangedCallbackArgs(sim, av, localid, oldSeatID));
	}

	protected final void UpdateDilation(Simulator s, int dilation)
	{
		s.Statistics.Dilation = dilation / 65535.0f;
	}

	private ConstructionData CreateConstructionData(Primitive enclosing, PCode pcode,
			ObjectUpdatePacket.ObjectDataBlock block)
	{
		ConstructionData data = enclosing.new ConstructionData();
		data.State = block.State;
		data.Material = Material.setValue(block.Material);
		data.PathCurve = PathCurve.setValue(block.PathCurve);
		data.setProfileCurve(ProfileCurve.setValue(block.ProfileCurve));
		data.PathBegin = Primitive.UnpackBeginCut(block.PathBegin);
		data.PathEnd = Primitive.UnpackEndCut(block.PathEnd);
		data.PathScaleX = Primitive.UnpackPathScale(block.PathScaleX);
		data.PathScaleY = Primitive.UnpackPathScale(block.PathScaleY);
		data.PathShearX = Primitive.UnpackPathShear(block.PathShearX);
		data.PathShearY = Primitive.UnpackPathShear(block.PathShearY);
		data.PathTwist = Primitive.UnpackPathTwist(block.PathTwist);
		data.PathTwistBegin = Primitive.UnpackPathTwist(block.PathTwistBegin);
		data.PathRadiusOffset = Primitive.UnpackPathTwist(block.PathRadiusOffset);
		data.PathTaperX = Primitive.UnpackPathTaper(block.PathTaperX);
		data.PathTaperY = Primitive.UnpackPathTaper(block.PathTaperY);
		data.PathRevolutions = Primitive.UnpackPathRevolutions(block.PathRevolutions);
		data.PathSkew = Primitive.UnpackPathTwist(block.PathSkew);
		data.ProfileBegin = Primitive.UnpackBeginCut(block.ProfileBegin);
		data.ProfileEnd = Primitive.UnpackEndCut(block.ProfileEnd);
		data.ProfileHollow = Primitive.UnpackProfileHollow(block.ProfileHollow);
		data.PCode = pcode;
		return data;
	}

	private void HandleObjectUpdate(Packet packet, Simulator simulator)
	{
		ObjectUpdatePacket update = (ObjectUpdatePacket) packet;

		UpdateDilation(simulator, update.RegionData.TimeDilation);

		for (ObjectUpdatePacket.ObjectDataBlock block : update.ObjectData)
		{
			// #region Relevance check
			// Check if we are interested in this object
			Primitive.PCode pcode;
			pcode = PCode.setValue(block.PCode);
			if (!Client.Settings.ALWAYS_DECODE_OBJECTS)
			{
				switch (pcode)
				{
					case Grass:
					case Tree:
					case NewTree:
					case Prim:
						if (OnObjectUpdate.count() == 0)
						{
							continue;
						}
						break;
					case Avatar:
						// Make an exception for updates about our own agent
						if (block.FullID != Client.Self.getAgentID() && OnAvatarUpdate.count() == 0)
						{
							continue;
						}
						break;
					case ParticleSystem:
						continue; // TODO: Do something with these
				}
			}
			// #endregion Relevance check

			// #region NameValue parsing
			NameValue[] nameValues;
			boolean attachment = false;
			String nameValue = Helpers.EmptyString;
			try
			{
				nameValue = Helpers.BytesToString(block.getNameValue());
			}
			catch (UnsupportedEncodingException e)
			{
			}

			if (nameValue.length() > 0)
			{
				String[] lines = nameValue.split("\n");
				nameValues = new NameValue[lines.length];

				for (int i = 0; i < lines.length; i++)
				{
					if (lines[i] != null && lines[i].equals(Helpers.EmptyString))
					{
						NameValue nv = new NameValue(lines[i]);
						if (nv.Name.equals("AttachItemID"))
						{
							attachment = true;
						}
						nameValues[i] = nv;
					}
				}
			}
			else
			{
				nameValues = new NameValue[0];
			}
			// #endregion NameValue parsing

			// /#region Decode Additional packed parameters in ObjectData
			ObjectMovementUpdate objectupdate = new ObjectMovementUpdate();
			int pos = 0;
			switch (block.getObjectData().length)
			{
				case 76:
					// Collision normal for avatar
					objectupdate.CollisionPlane = new Vector4(block.getObjectData(), pos);
					pos += 16;
					// fall through
				case 60:
					// Position
					objectupdate.Position = new Vector3(block.getObjectData(), pos);
					pos += 12;
					// Velocity
					objectupdate.Velocity = new Vector3(block.getObjectData(), pos);
					pos += 12;
					// Acceleration
					objectupdate.Acceleration = new Vector3(block.getObjectData(), pos);
					pos += 12;
					// Rotation (theta)
					objectupdate.Rotation = new Quaternion(block.getObjectData(), pos, true);
					pos += 12;
					// Angular velocity (omega)
					objectupdate.AngularVelocity = new Vector3(block.getObjectData(), pos);
					pos += 12;

					break;
				case 48:
					// Collision normal for avatar
					objectupdate.CollisionPlane = new Vector4(block.getObjectData(), pos);
					pos += 16;
					// fall through
				case 32:
					// The data is an array of unsigned shorts

					// Position
					objectupdate.Position = new Vector3(Helpers.UInt16ToFloatL(block.getObjectData(), pos,
							-0.5f * 256.0f, 1.5f * 256.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2,
							-0.5f * 256.0f, 1.5f * 256.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4,
							-256.0f, 3.0f * 256.0f));
					pos += 6;
					// Velocity
					objectupdate.Velocity = new Vector3(Helpers.UInt16ToFloatL(block.getObjectData(), pos, -256.0f,
							256.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2, -256.0f, 256.0f),
							Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4, -256.0f, 256.0f));
					pos += 6;
					// Acceleration
					objectupdate.Acceleration = new Vector3(Helpers.UInt16ToFloatL(block.getObjectData(), pos, -256.0f,
							256.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2, -256.0f, 256.0f),
							Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4, -256.0f, 256.0f));
					pos += 6;
					// Rotation (theta)
					objectupdate.Rotation = new Quaternion(Helpers.UInt16ToFloatL(block.getObjectData(), pos, -1.0f,
							1.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2, -1.0f, 1.0f),
							Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4, -1.0f, 1.0f),
							Helpers.UInt16ToFloatL(block.getObjectData(), pos + 6, -1.0f, 1.0f));
					pos += 8;
					// Angular velocity (omega)
					objectupdate.AngularVelocity = new Vector3(Helpers.UInt16ToFloatL(block.getObjectData(), pos,
							-256.0f, 256.0f), Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2, -256.0f, 256.0f),
							Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4, -256.0f, 256.0f));
					pos += 6;

					break;
				case 16:
					// The data is an array of single bytes (8-bit numbers)

					// Position
					objectupdate.Position = new Vector3(
							Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 1, -256.0f, 256.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 2, -256.0f, 256.0f));
					pos += 3;
					// Velocity
					objectupdate.Velocity = new Vector3(
							Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 1, -256.0f, 256.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 2, -256.0f, 256.0f));
					pos += 3;
					// Accleration
					objectupdate.Acceleration = new Vector3(Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f,
							256.0f), Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
							Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
					pos += 3;
					// Rotation
					objectupdate.Rotation = new Quaternion(
							Helpers.ByteToFloat(block.getObjectData(), pos, -1.0f, 1.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 1, -1.0f, 1.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 2, -1.0f, 1.0f), Helpers.ByteToFloat(
									block.getObjectData(), pos + 3, -1.0f, 1.0f));
					pos += 4;
					// Angular Velocity
					objectupdate.AngularVelocity = new Vector3(Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f,
							256.0f), Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
							Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
					pos += 3;

					break;
				default:
					Logger.Log("Got an ObjectUpdate block with ObjectUpdate field length of "
							+ block.getObjectData().length, LogLevel.Warning, Client);

					continue;
			}
			// #endregion

			// Determine the object type and create the appropriate class
			ConstructionData data;
			switch (pcode)
			{
				// #region Prim and Foliage
				case Grass:
				case Tree:
				case NewTree:
				case Prim:

					boolean isNewObject;
					synchronized (simulator.getObjectsPrimitives())
					{
						isNewObject = !simulator.getObjectsPrimitives().containsKey(block.ID);
					}

					Primitive prim = GetPrimitive(simulator, block.ID, block.FullID);
					data = CreateConstructionData(prim, pcode, block);
					// Textures
					try
					{
						objectupdate.Textures = new TextureEntry(block.getTextureEntry(), 0,
								block.getTextureEntry().length);
					}
					catch (Exception e)
					{
						Logger.Log("Failed to create Texture for object update.", LogLevel.Warning, e);
					}

					OnObjectDataBlockUpdate.dispatch(new ObjectDataBlockUpdateCallbackArgs(simulator, prim, data,
							block, objectupdate, nameValues));

					// #region Update Prim Info with decoded data
					prim.Flags = PrimFlags.setValue(block.UpdateFlags);
					if ((prim.Flags & PrimFlags.ZlibCompressed) != 0)
					{
						Logger.Log("Got a ZlibCompressed ObjectUpdate, implement me!", LogLevel.Warning, Client);
						continue;
					}

					// Automatically request ObjectProperties for prim if it was
					// rezzed selected.
					if ((prim.Flags & PrimFlags.CreateSelected) != 0)
					{
						try
						{
							SelectObject(simulator, prim.LocalID);
						}
						catch (Exception e)
						{
							Logger.Log("Requesting object properties update failed.", LogLevel.Warning, e);
						}
					}

					prim.NameValues = nameValues;
					prim.LocalID = block.ID;
					prim.ID = block.FullID;
					prim.ParentID = block.ParentID;
					prim.RegionHandle = update.RegionData.RegionHandle;
					prim.Scale = block.Scale;
					prim.clickAction = ClickAction.setValue(block.ClickAction);
					prim.OwnerID = block.OwnerID;
					try
					{
						prim.MediaURL = Helpers.BytesToString(block.getMediaURL());
						prim.Text = Helpers.BytesToString(block.getText());
					}
					catch (UnsupportedEncodingException e)
					{
						Logger.Log("Extracting MediaURL or Text for object properties update failed.",
								LogLevel.Warning, e);
					}
					prim.TextColor = new Color4(block.TextColor, 0, false, true);
					prim.IsAttachment = attachment;

					// Sound information
					prim.SoundID = block.Sound;
					prim.SoundFlags = SoundFlags.setValue(block.Flags);
					prim.SoundGain = block.Gain;
					prim.SoundRadius = block.Radius;

					// Joint information
					prim.Joint = JointType.setValue(block.JointType);
					prim.JointPivot = block.JointPivot;
					prim.JointAxisOrAnchor = block.JointAxisOrAnchor;

					// Object parameters
					prim.PrimData = data;

					// Textures, texture animations, particle system, and extra
					// params
					prim.Textures = objectupdate.Textures;

					prim.TextureAnim = prim.Textures.new TextureAnimation(block.getTextureAnim(), 0);
					prim.ParticleSys = new ParticleSystem(block.getPSBlock(), 0);
					prim.SetExtraParamsFromBytes(block.getExtraParams(), 0);

					// PCode-specific data
					switch (pcode)
					{
						case Grass:
						case Tree:
						case NewTree:
							if (block.getData().length == 1)
							{
								prim.TreeSpecies = Tree.setValue(block.getData()[0]);
							}
							else
							{
								Logger.Log("Got a foliage update with an invalid TreeSpecies field", LogLevel.Warning);
							}
							// prim.ScratchPad = Utils.EmptyBytes;
							// break;
							// default:
							// prim.ScratchPad = new byte[block.Data.Length];
							// if (block.Data.Length > 0)
							// Buffer.BlockCopy(block.Data, 0, prim.ScratchPad,
							// 0, prim.ScratchPad.Length);
							break;
					}
					prim.ScratchPad = Helpers.EmptyBytes;

					// Packed parameters
					prim.CollisionPlane = objectupdate.CollisionPlane;
					prim.Position = objectupdate.Position;
					prim.Velocity = objectupdate.Velocity;
					prim.Acceleration = objectupdate.Acceleration;
					prim.Rotation = objectupdate.Rotation;
					prim.AngularVelocity = objectupdate.AngularVelocity;
					// #endregion

					OnObjectUpdate.dispatch(new PrimCallbackArgs(simulator, prim, update.RegionData.TimeDilation,
							isNewObject));

					break;
				// #endregion Prim and Foliage

				// #region Avatar
				case Avatar:

					boolean isNewAvatar;
					synchronized (simulator.getObjectsAvatars())
					{
						isNewAvatar = !simulator.getObjectsAvatars().containsKey(block.ID);
					}

					// Update some internals if this is our avatar
					if (block.FullID.equals(Client.Self.getAgentID()) && simulator.equals(Client.Network.getCurrentSim()))
					{
						// #region Update Client.Self

						// We need the local ID to recognize terse updates for
						// our agent
						Client.Self.setLocalID(block.ID);

						// Packed parameters
						Client.Self.setCollisionPlane(objectupdate.CollisionPlane);
						Client.Self.setRelativePosition(objectupdate.Position);
						Client.Self.setVelocity(objectupdate.Velocity);
						Client.Self.setAcceleration(objectupdate.Acceleration);
						Client.Self.setRelativeRotation(objectupdate.Rotation);
						Client.Self.setAngularVelocity(objectupdate.AngularVelocity);
						// #endregion
					}

					// #region Create an Avatar from the decoded data

					Avatar avatar = GetAvatar(simulator, block.ID, block.FullID);
					data = CreateConstructionData(avatar, pcode, block);

					objectupdate.Avatar = true;
					// Textures
					try
					{
						objectupdate.Textures = new TextureEntry(block.getTextureEntry(), 0,
								block.getTextureEntry().length);
					}
					catch (Exception e)
					{
						Logger.Log("Failed to create Texture for avatar update.", LogLevel.Warning, e);
					}

					OnObjectDataBlockUpdate.dispatch(new ObjectDataBlockUpdateCallbackArgs(simulator, avatar, data,
							block, objectupdate, nameValues));

					int oldSeatID = avatar.ParentID;

					avatar.ID = block.FullID;
					avatar.LocalID = block.ID;
					avatar.Scale = block.Scale;
					avatar.CollisionPlane = objectupdate.CollisionPlane;
					avatar.Position = objectupdate.Position;
					avatar.Velocity = objectupdate.Velocity;
					avatar.Acceleration = objectupdate.Acceleration;
					avatar.Rotation = objectupdate.Rotation;
					avatar.AngularVelocity = objectupdate.AngularVelocity;
					avatar.NameValues = nameValues;
					avatar.PrimData = data;
					if (block.getData().length > 0)
					{
						Logger.Log("Unexpected Data field for an avatar update, length " + block.getData().length,
								LogLevel.Warning);
					}
					avatar.ParentID = block.ParentID;
					avatar.RegionHandle = update.RegionData.RegionHandle;

					SetAvatarSittingOn(simulator, avatar, block.ParentID, oldSeatID);

					// Textures
					avatar.Textures = objectupdate.Textures;

					// #endregion Create an Avatar from the decoded data

					OnAvatarUpdate.dispatch(new AvatarUpdateCallbackArgs(simulator, avatar,
							update.RegionData.TimeDilation, isNewAvatar));

					break;
				// #endregion Avatar
				case ParticleSystem:
					DecodeParticleUpdate(block);
					// TODO: Create a callback for particle updates
					break;
				default:
					Logger.DebugLog("Got an ObjectUpdate block with an unrecognized PCode " + pcode.toString(), Client);
					break;
			}
		}
	}

	protected final void DecodeParticleUpdate(ObjectUpdatePacket.ObjectDataBlock block)
	{
		// TODO: Handle ParticleSystem ObjectUpdate blocks

		// float bounce_b
		// Vector4 scale_range
		// Vector4 alpha_range
		// Vector3 vel_offset
		// float dist_begin_fadeout
		// float dist_end_fadeout
		// UUID image_uuid
		// long flags
		// byte createme
		// Vector3 diff_eq_alpha
		// Vector3 diff_eq_scale
		// byte max_particles
		// byte initial_particles
		// float kill_plane_z
		// Vector3 kill_plane_normal
		// float bounce_plane_z
		// Vector3 bounce_plane_normal
		// float spawn_range
		// float spawn_frequency
		// float spawn_frequency_range
		// Vector3 spawn_direction
		// float spawn_direction_range
		// float spawn_velocity
		// float spawn_velocity_range
		// float speed_limit
		// float wind_weight
		// Vector3 current_gravity
		// float gravity_weight
		// float global_lifetime
		// float individual_lifetime
		// float individual_lifetime_range
		// float alpha_decay
		// float scale_decay
		// float distance_death
		// float damp_motion_factor
		// Vector3 wind_diffusion_factor
	}

	/**
	 * A terse object update, used when a transformation matrix or
	 * velocity/acceleration for an object changes but nothing else
	 * (scale/position/rotation/acceleration/velocity)
	 */
	private final void HandleTerseObjectUpdate(Packet packet, Simulator simulator)
	{
		ImprovedTerseObjectUpdatePacket terse = (ImprovedTerseObjectUpdatePacket) packet;
		UpdateDilation(simulator, terse.RegionData.TimeDilation);

		for (int i = 0; i < terse.ObjectData.length; i++)
		{
			ImprovedTerseObjectUpdatePacket.ObjectDataBlock block = terse.ObjectData[i];

			try
			{
				int pos = 4;
				byte[] data = block.getData();
				long localid = Helpers.BytesToUInt32L(data, 0);

				// Check if we are interested in this update
				if (!Client.Settings.ALWAYS_DECODE_OBJECTS && localid != Client.Self.getLocalID()
						&& OnTerseObjectUpdate.count() > 0)
				{
					continue;
				}

				// #region Decode update data

				ObjectMovementUpdate update = new ObjectMovementUpdate();

				// LocalID
				update.LocalID = (int) localid;
				// State
				update.State = data[pos++];
				// Avatar boolean
				update.Avatar = (data[pos++] != 0);
				// Collision normal for avatar
				if (update.Avatar)
				{
					update.CollisionPlane = new Vector4(data, pos);
					pos += 16;
				}
				// Position
				update.Position = new Vector3(data, pos);
				pos += 12;
				// Velocity
				update.Velocity = new Vector3(Helpers.UInt16ToFloatL(data, pos, -128.0f, 128.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -128.0f, 128.0f), Helpers.UInt16ToFloatL(data, pos + 4,
								-128.0f, 128.0f));
				pos += 6;
				// Acceleration
				update.Acceleration = new Vector3(Helpers.UInt16ToFloatL(data, pos, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -64.0f, 64.0f), Helpers.UInt16ToFloatL(data, pos + 4,
								-64.0f, 64.0f));
				pos += 6;
				// Rotation (theta)
				update.Rotation = new Quaternion(Helpers.UInt16ToFloatL(data, pos, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -1.0f, 1.0f), Helpers.UInt16ToFloatL(data, pos + 4,
								-1.0f, 1.0f), Helpers.UInt16ToFloatL(data, pos + 6, -1.0f, 1.0f));
				pos += 8;
				// Angular velocity (omega)
				update.AngularVelocity = new Vector3(Helpers.UInt16ToFloatL(data, pos, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -64.0f, 64.0f), Helpers.UInt16ToFloatL(data, pos + 4,
								-64.0f, 64.0f));
				pos += 6;

				// Textures
				// FIXME: Why are we ignoring the first four bytes here?
				if (block.getTextureEntry().length != 0)
				{
					update.Textures = new TextureEntry(block.getTextureEntry(), 4, block.getTextureEntry().length - 4);
				}
				// #endregion Decode update data

				Primitive obj = !Client.Settings.OBJECT_TRACKING ? null : (update.Avatar) ? (Primitive) GetAvatar(
						simulator, update.LocalID, UUID.Zero) : (Primitive) GetPrimitive(simulator, update.LocalID,
						UUID.Zero);

				// Fire the pre-emptive notice (before we stomp the object)
				OnTerseObjectUpdate.dispatch(new TerseObjectUpdateCallbackArgs(simulator, obj, update,
						terse.RegionData.TimeDilation));

				// #region Update Client.Self
				if (update.LocalID == Client.Self.getLocalID())
				{
					Client.Self.setCollisionPlane(update.CollisionPlane);
					Client.Self.setRelativePosition(update.Position);
					Client.Self.setVelocity(update.Velocity);
					Client.Self.setAcceleration(update.Acceleration);
					Client.Self.setRelativeRotation(update.Rotation);
					Client.Self.setAngularVelocity(update.AngularVelocity);
				}
				// #endregion Update Client.Self
				if (Client.Settings.OBJECT_TRACKING && obj != null)
				{
					obj.Position = update.Position;
					obj.Rotation = update.Rotation;
					obj.Velocity = update.Velocity;
					obj.CollisionPlane = update.CollisionPlane;
					obj.Acceleration = update.Acceleration;
					obj.AngularVelocity = update.AngularVelocity;
					obj.PrimData = obj.new ConstructionData();
					obj.PrimData.State = update.State;
					obj.Textures = update.Textures;
				}

			}
			catch (Throwable ex)
			{
				Logger.Log(ex.getMessage(), LogLevel.Warning, Client, ex);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 * 
	 * 
	 */
	private final void HandleObjectUpdateCompressed(Packet packet, Simulator simulator)
	{
		ObjectUpdateCompressedPacket update = (ObjectUpdateCompressedPacket) packet;

		for (int b = 0; b < update.ObjectData.length; b++)
		{
			ObjectUpdateCompressedPacket.ObjectDataBlock block = update.ObjectData[b];
			int i = 0;
			byte[] data = block.getData();

			// UUID
			UUID FullID = new UUID(data, 0); i += 16;
			// Local ID
			int localid = (int) Helpers.BytesToUInt32L(data, i); i += 4;
			// PCode
			PCode pcode = PCode.setValue(data[i++]);

			// /#region Relevance check
			if (!Client.Settings.ALWAYS_DECODE_OBJECTS)
			{
				switch (pcode)
				{
					case Grass:
					case Tree:
					case NewTree:
					case Prim:
						if (OnObjectUpdate.count() == 0)
						{
							continue;
						}
						break;
				}
			}
			// /#endregion Relevance check

			boolean isNew;
			synchronized (simulator.getObjectsPrimitives())
			{
				isNew = simulator.getObjectsPrimitives().containsKey(localid);
			}

			Primitive prim = GetPrimitive(simulator, localid, FullID);

			prim.LocalID = localid;
			prim.ID = FullID;
			prim.Flags = PrimFlags.setValue(block.UpdateFlags);
			prim.PrimData = prim.new ConstructionData();
			prim.PrimData.PCode = pcode;

			// /#region Decode block and update Prim

			// State
			prim.PrimData.State = data[i++];
			// CRC
			i += 4;
			// Material
			prim.PrimData.Material = Material.setValue(data[i++]);
			// Click action
			prim.clickAction = ClickAction.setValue(data[i++]);
			// Scale
			prim.Scale = new Vector3(data, i); i += 12;
			// Position
			prim.Position = new Vector3(data, i); i += 12;
			// Rotation
			prim.Rotation = new Quaternion(data, i, true); i += 12;
			// Compressed flags
			int flags = (int) Helpers.BytesToUInt32L(data, i); i += 4;

			prim.OwnerID = new UUID(data, i); i += 16;

			// Angular velocity
			if ((flags & CompressedFlags.HasAngularVelocity) != 0)
			{
				prim.AngularVelocity = new Vector3(data, i); i += 12;
			}

			// Parent ID
			if ((flags & CompressedFlags.HasParent) != 0)
			{
				prim.ParentID = (int) Helpers.BytesToUInt32L(data, i); i += 4;
			}
			else
			{
				prim.ParentID = 0;
			}

			// Tree data
			if ((flags & CompressedFlags.Tree) != 0)
			{
				prim.TreeSpecies = Tree.setValue(data[i++]);
				prim.ScratchPad = Helpers.EmptyBytes;
			}
			// Scratch pad
			else if ((flags & CompressedFlags.ScratchPad) != 0)
			{
				prim.TreeSpecies = Tree.setValue((byte) 0);

				int size = data[i++];
				prim.ScratchPad = new byte[size];
				System.arraycopy(data, i, prim.ScratchPad, 0, size);
				i += size;
			}
			else
			{
				prim.TreeSpecies = Tree.setValue((byte) 0);
				prim.ScratchPad = Helpers.EmptyBytes;
			}
			

			// Floating text
			if ((flags & CompressedFlags.HasText) != 0)
			{
				String text = Helpers.EmptyString;
				while (data[i] != 0)
				{
					text += (char) data[i++];
				}
				i++;

				// Floating text
				prim.Text = text;

				// Text color
				prim.TextColor = new Color4(data, i, false); i += 4;
			}
			else
			{
				prim.Text = Helpers.EmptyString;
			}

			prim.IsAttachment = (((flags & CompressedFlags.HasNameValues) != 0) && prim.ParentID != 0);

			// Media URL
			if ((flags & CompressedFlags.MediaURL) != 0)
			{
				String text = Helpers.EmptyString;
				while (data[i] != 0)
				{
					text += (char) data[i++];
				}
				i++;

				prim.MediaURL = text;
			}

			// Particle system
			if ((flags & CompressedFlags.HasParticles) != 0)
			{
				prim.ParticleSys = new ParticleSystem(data, i); i += 86;
			}

			// Extra parameters
			i += prim.SetExtraParamsFromBytes(data, i);

			// Sound data
			if ((flags & CompressedFlags.HasSound) != 0)
			{
				prim.SoundID = new UUID(data, i); i += 16;

				prim.SoundGain = Helpers.BytesToFloatL(data, i); i += 4;
				prim.SoundFlags = SoundFlags.setValue(data[i++]);
				prim.SoundRadius = Helpers.BytesToFloatL(data, i); i += 4;
			}

			// Name values
			if ((flags & CompressedFlags.HasNameValues) != 0)
			{
				String text = Helpers.EmptyString;
				while (data[i] != 0)
				{
					text += (char) data[i++];
				}
				i++;

				// Parse the name values
				if (text.length() > 0)
				{
					String[] lines = text.split("\n");
					prim.NameValues = new NameValue[lines.length];

					for (int j = 0; j < lines.length; j++)
					{
						if (!Helpers.isEmpty(lines[j]))
						{
							NameValue nv = new NameValue(lines[j]);
							prim.NameValues[j] = nv;
						}
					}
				}
			}

			prim.PrimData.PathCurve = PathCurve.setValue(data[i++]);
			short pathBegin = (short) Helpers.BytesToUInt16L(data, i); i += 2;
			prim.PrimData.PathBegin = Primitive.UnpackBeginCut(pathBegin);
			short pathEnd = (short) Helpers.BytesToUInt16L(data, i); i += 2;
			prim.PrimData.PathEnd = Primitive.UnpackEndCut(pathEnd);
			prim.PrimData.PathScaleX = Primitive.UnpackPathScale(data[i++]);
			prim.PrimData.PathScaleY = Primitive.UnpackPathScale(data[i++]);
			prim.PrimData.PathShearX = Primitive.UnpackPathShear(data[i++]);
			prim.PrimData.PathShearY = Primitive.UnpackPathShear(data[i++]);
			prim.PrimData.PathTwist = Primitive.UnpackPathTwist(data[i++]);
			prim.PrimData.PathTwistBegin = Primitive.UnpackPathTwist(data[i++]);
			prim.PrimData.PathRadiusOffset = Primitive.UnpackPathTwist(data[i++]);
			prim.PrimData.PathTaperX = Primitive.UnpackPathTaper(data[i++]);
			prim.PrimData.PathTaperY = Primitive.UnpackPathTaper(data[i++]);
			prim.PrimData.PathRevolutions = Primitive.UnpackPathRevolutions(data[i++]);
			prim.PrimData.PathSkew = Primitive.UnpackPathTwist(data[i++]);

			prim.PrimData.ProfileCurve = ProfileCurve.setValue(data[i++]);
			prim.PrimData.ProfileBegin = Primitive.UnpackBeginCut((short) Helpers.BytesToUInt16L(data, i)); i += 2;
			prim.PrimData.ProfileEnd = Primitive.UnpackEndCut((short) Helpers.BytesToUInt16L(data, i)); i += 2;
			prim.PrimData.ProfileHollow = Primitive.UnpackProfileHollow((short) Helpers.BytesToUInt16L(data, i)); i += 2;

			// TextureEntry
			int textureEntryLength = (int) Helpers.BytesToUInt32L(data, i); i += 4;
			prim.Textures = new TextureEntry(data, i, textureEntryLength);
			i += textureEntryLength;

			// Texture animation
			if ((flags & CompressedFlags.TextureAnimation) != 0)
			{
				// int textureAnimLength = (int)Helpers.BytesToUInt32L(data, i);
				i += 4;
				prim.TextureAnim = prim.Textures.new TextureAnimation(data, i);
			}
			// #endregion

			OnObjectUpdate.dispatch(new PrimCallbackArgs(simulator, prim, update.RegionData.TimeDilation, isNew));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectUpdateCached(Packet packet, Simulator simulator)
	{
		if (Client.Settings.ALWAYS_REQUEST_OBJECTS)
		{
			ObjectUpdateCachedPacket update = (ObjectUpdateCachedPacket) packet;
			int[] ids = new int[update.ObjectData.length];

			// No object caching implemented yet, so request updates for all of
			// these objects
			for (int i = 0; i < update.ObjectData.length; i++)
			{
				ids[i] = update.ObjectData[i].ID;
			}
			try
			{
				RequestObjects(simulator, ids);
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleKillObject(Packet packet, Simulator simulator)
	{
		KillObjectPacket kill = (KillObjectPacket) packet;

		// Notify first, so that handler has a chance to get a
		// reference from the ObjectTracker to the object being killed
		for (int i = 0; i < kill.ID.length; i++)
		{
			OnKillObject.dispatch(new KillObjectCallbackArgs(simulator, kill.ID[i]));
		}

		ArrayList<Integer> removeAvatars = new ArrayList<Integer>();
		ArrayList<Integer> removePrims = new ArrayList<Integer>();

		synchronized (simulator.getObjectsPrimitives())
		{
			if (Client.Settings.OBJECT_TRACKING)
			{
				int localID;
				for (int i = 0; i < kill.ID.length; i++)
				{
					localID = kill.ID[i];

					if (simulator.getObjectsPrimitives().containsKey(localID))
					{
						removePrims.add(localID);
					}

					for (Entry<Integer, Primitive> e : simulator.getObjectsPrimitives().entrySet())
					{
						if (e.getValue().ParentID == localID)
						{
							OnKillObject.dispatch(new KillObjectCallbackArgs(simulator, e.getKey()));
							removePrims.add(e.getKey());
						}
					}
				}
			}

			if (Client.Settings.AVATAR_TRACKING)
			{
				int localID;

				synchronized (simulator.getObjectsAvatars())
				{
					for (int i = 0; i < kill.ID.length; i++)
					{
						localID = kill.ID[i];

						if (simulator.getObjectsAvatars().containsKey(localID))
						{
							removeAvatars.add(localID);
						}

						ArrayList<Integer> rootPrims = new ArrayList<Integer>();

						for (Entry<Integer, Primitive> e : simulator.getObjectsPrimitives().entrySet())
						{
							if (e.getValue().ParentID == localID)
							{
								OnKillObject.dispatch(new KillObjectCallbackArgs(simulator, e.getKey()));
								removePrims.add(e.getKey());
								rootPrims.add(e.getKey());
							}
						}

						for (Entry<Integer, Primitive> e : simulator.getObjectsPrimitives().entrySet())
						{
							if (rootPrims.contains(e.getValue().ParentID))
							{
								OnKillObject.dispatch(new KillObjectCallbackArgs(simulator, e.getKey()));
								removePrims.add(e.getKey());
							}
						}
					}

					// Do the actual removing outside of the loops but still
					// inside the lock.
					// This safely prevents the collection from being modified
					// during a loop.
					for (int removeID : removeAvatars)
					{
						simulator.getObjectsAvatars().remove(removeID);
					}
				}
			}

			for (int removeID : removePrims)
			{
				simulator.getObjectsPrimitives().remove(removeID);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectProperties(Packet packet, Simulator simulator)
	{
		ObjectPropertiesPacket op = (ObjectPropertiesPacket) packet;
		ObjectPropertiesPacket.ObjectDataBlock[] datablocks = op.ObjectData;

		for (int i = 0; i < datablocks.length; ++i)
		{
			ObjectPropertiesPacket.ObjectDataBlock objectData = datablocks[i];
			ObjectProperties props = new ObjectProperties();

			props.ObjectID = objectData.ObjectID;
			props.AggregatePerms = objectData.AggregatePerms;
			props.AggregatePermTextures = objectData.AggregatePermTextures;
			props.AggregatePermTexturesOwner = objectData.AggregatePermTexturesOwner;
			props.Category = ObjectCategory.setValue(objectData.Category);
			props.CreatorID = objectData.CreatorID;
			props.FolderID = objectData.FolderID;
			props.FromTaskID = objectData.FromTaskID;
			props.GroupID = objectData.GroupID;
			props.InventorySerial = objectData.InventorySerial;
			props.ItemID = objectData.ItemID;
			props.LastOwnerID = objectData.LastOwnerID;
			props.OwnerID = objectData.OwnerID;
			props.OwnershipCost = objectData.OwnershipCost;
			props.SalePrice = objectData.SalePrice;
			props.SaleType = SaleType.setValue(objectData.SaleType);
			props.Permissions = new Permissions(objectData.BaseMask, objectData.EveryoneMask, objectData.GroupMask,
					objectData.NextOwnerMask, objectData.OwnerMask);

			try
			{
				props.Name = Helpers.BytesToString(objectData.getName());
				props.Description = Helpers.BytesToString(objectData.getDescription());
				props.CreationDate = Helpers.UnixTimeToDateTime(objectData.CreationDate);
				props.SitName = Helpers.BytesToString(objectData.getSitName());
				props.TouchName = Helpers.BytesToString(objectData.getTouchName());
			}
			catch (UnsupportedEncodingException e)
			{
				Logger.Log("Encoding Exception when decoding object properties reply.", LogLevel.Warning, e);
				return;
			}

			int numTextures = objectData.getTextureID().length / 16;
			props.TextureIDs = new UUID[numTextures];
			for (int j = 0; j < numTextures; ++j)
			{
				props.TextureIDs[j] = new UUID(objectData.getTextureID(), j * 16);
			}

			if (Client.Settings.OBJECT_TRACKING)
			{
				synchronized (simulator.getObjectsPrimitives())
				{
					for (Primitive prim : simulator.getObjectsPrimitives().values())
					{
						if (prim.ID.equals(props.ObjectID))
						{
							OnObjectPropertiesUpdated.dispatch(new ObjectPropertiesUpdatedCallbackArgs(simulator, prim, props));

							if (simulator.getObjectsPrimitives().containsKey(prim.LocalID))
							{
								simulator.getObjectsPrimitives().get(prim.LocalID).Properties = props;
							}
							break;
						}
					}
				}
			}
			OnObjectProperties.dispatch(new ObjectPropertiesCallbackArgs(simulator, props));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectPropertiesFamily(Packet packet, Simulator simulator)
	{
		ObjectPropertiesFamilyPacket op = (ObjectPropertiesFamilyPacket) packet;
		ObjectProperties props = new ObjectProperties();

		ReportType requestType = ReportType.setValue(op.ObjectData.RequestFlags);

		props.ObjectID = op.ObjectData.ObjectID;
		props.Category = ObjectCategory.setValue(op.ObjectData.Category);
		props.GroupID = op.ObjectData.GroupID;
		props.LastOwnerID = op.ObjectData.LastOwnerID;
		props.OwnerID = op.ObjectData.OwnerID;
		props.OwnershipCost = op.ObjectData.OwnershipCost;
		props.SalePrice = op.ObjectData.SalePrice;
		props.SaleType = SaleType.setValue(op.ObjectData.SaleType);
		props.Permissions = new Permissions(op.ObjectData.BaseMask, op.ObjectData.EveryoneMask,
				op.ObjectData.GroupMask, op.ObjectData.NextOwnerMask, op.ObjectData.OwnerMask);
		try
		{
			props.Name = Helpers.BytesToString(op.ObjectData.getName());
			props.Description = Helpers.BytesToString(op.ObjectData.getDescription());
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Log("Encoding Exception when decoding object properties family reply.", LogLevel.Warning, e);
			return;
		}

		if (Client.Settings.OBJECT_TRACKING)
		{
			synchronized (simulator.getObjectsPrimitives())
			{
				for (Primitive prim : simulator.getObjectsPrimitives().values())
				{
					if (prim.ID.equals(op.ObjectData.ObjectID))
					{
						if (simulator.getObjectsPrimitives().containsKey(prim.LocalID))
						{
							if (simulator.getObjectsPrimitives().get(prim.LocalID).Properties == null)
							{
								simulator.getObjectsPrimitives().get(prim.LocalID).Properties = new ObjectProperties();
							}
							simulator.getObjectsPrimitives().get(prim.LocalID).Properties.SetFamilyProperties(props);
						}
						break;
					}
				}
			}
		}
		OnObjectPropertiesFamily.dispatch(new ObjectPropertiesFamilyCallbackArgs(simulator, props, requestType));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandlePayPriceReply(Packet packet, Simulator simulator)
	{
		if (OnPayPriceReply.count() > 0)
		{
			PayPriceReplyPacket p = (PayPriceReplyPacket) packet;
			UUID objectID = p.ObjectData.ObjectID;
			int defaultPrice = p.ObjectData.DefaultPayPrice;
			int[] buttonPrices = new int[p.PayButton.length];

			for (int i = 0; i < p.PayButton.length; i++)
			{
				buttonPrices[i] = p.PayButton[i];
			}

			OnPayPriceReply.dispatch(new PayPriceReplyCallbackArgs(simulator, objectID, defaultPrice, buttonPrices));
		}
	}

	private void HandleObjectPhysicsProperties(IMessage message, Simulator simulator)
	{
		ObjectPhysicsPropertiesMessage msg = (ObjectPhysicsPropertiesMessage) message;

		if (Client.Settings.OBJECT_TRACKING)
		{
			for (int i = 0; i < msg.ObjectPhysicsProperties.length; i++)
			{
				synchronized (simulator.getObjectsPrimitives())
				{
					if (simulator.getObjectsPrimitives().containsKey(msg.ObjectPhysicsProperties[i].LocalID))
					{
						simulator.getObjectsPrimitives().get(msg.ObjectPhysicsProperties[i].LocalID).PhysicsProps = msg.ObjectPhysicsProperties[i];
					}
				}
			}
		}

		if (OnPhysicsProperties.count() > 0)
		{
			for (int i = 0; i < msg.ObjectPhysicsProperties.length; i++)
			{
				OnPhysicsProperties.dispatch(new PhysicsPropertiesCallbackArgs(simulator,
						msg.ObjectPhysicsProperties[i]));
			}
		}
	}
	// #endregion Packet Handlers
}
