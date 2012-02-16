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
package libomv.primitives;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDMap;
import libomv.primitives.TextureEntry.TextureAnimation;
import libomv.types.Color4;
import libomv.types.NameValue;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class Primitive
{
	protected static float CUT_QUANTA = 0.00002f;
	protected static float SCALE_QUANTA = 0.01f;
	protected static float SHEAR_QUANTA = 0.01f;
	protected static float TAPER_QUANTA = 0.01f;
	protected static float REV_QUANTA = 0.015f;
	protected static float HOLLOW_QUANTA = 0.00002f;

	/** Identifier code for primitive types */
	public enum PCode
	{
		/** None */
		None(0),
		/** A Primitive */
		Prim(9),
		/** A Avatar */
		Avatar(47),
		/** Linden grass */
		Grass(95),
		/** Linden tree */
		NewTree(111),
		/** A primitive that acts as the source for a particle stream */
		ParticleSystem(143),
		/** A Linden tree */
		Tree(255);

		public static PCode setValue(int value)
		{
			for (PCode e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private PCode(int value)
		{
			_value = (byte) value;
		}
	}

	// Primary parameters for primitives such as Physics Enabled or Phantom
	// [Flags]
	public static class PrimFlags
	{
		// Deprecated
		public static final int None = 0;
		// Whether physics are enabled for this object
		public static final int Physics = 0x00000001;
		public static final int CreateSelected = 0x00000002;
		public static final int ObjectModify = 0x00000004;
		public static final int ObjectCopy = 0x00000008;
		public static final int ObjectAnyOwner = 0x00000010;
		public static final int ObjectYouOwner = 0x00000020;
		public static final int Scripted = 0x00000040;
		public static final int Touch = 0x00000080;
		public static final int ObjectMove = 0x00000100;
		public static final int Money = 0x00000200;
		public static final int Phantom = 0x00000400;
		public static final int InventoryEmpty = 0x00000800;
		public static final int JointHinge = 0x00001000;
		public static final int JointP2P = 0x00002000;
		public static final int JointLP2P = 0x00004000;
		// Deprecated */
		public static final int JointWheel = 0x00008000;
		public static final int AllowInventoryDrop = 0x00010000;
		public static final int ObjectTransfer = 0x00020000;
		public static final int ObjectGroupOwned = 0x00040000;
		// Deprecated */
		public static final int ObjectYouOfficer = 0x00080000;
		public static final int CameraDecoupled = 0x00100000;
		public static final int AnimSource = 0x00200000;
		public static final int CameraSource = 0x00400000;
		public static final int CastShadows = 0x00800000;
		// Server flag, will not be sent to clients. Specifies that the object
		// is
		// destroyed when it touches a simulator edge
		public static final int DieAtEdge = 0x01000000;
		// Server flag, will not be sent to clients. Specifies that the object
		// will
		// be returned to the owner's inventory when it touches a simulator edge
		public static final int ReturnAtEdge = 0x02000000;
		// Server flag, will not be sent to clients.
		public static final int Sandbox = 0x04000000;
		// Server flag, will not be sent to client. Specifies that the object is
		// hovering/flying
		public static final int Flying = 0x08000000;
		public static final int ObjectOwnerModify = 0x10000000;
		public static final int TemporaryOnRez = 0x20000000;
		public static final int Temporary = 0x40000000;
		public static final int ZlibCompressed = 0x80000000;

		public static int setValue(int value)
		{
			return value & _mask;
		}

		public static int getValue(int value)
		{
			return value & _mask;
		}

		private static int _mask = 0xFFFFFFFF;
	}

	// Sound flags for sounds attached to primitives
	// [Flags]
	public static class SoundFlags
	{
		public static final byte None = 0;
		public static final byte Loop = 0x01;
		public static final byte SyncMaster = 0x02;
		public static final byte SyncSlave = 0x04;
		public static final byte SyncPending = 0x08;
		public static final byte LinkedList = 0x10;
		public static final byte Stop = 0x20;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static byte _mask = (byte)0xFF;
	}

	public enum ProfileCurve
	{
		Circle, Square, IsoTriangle, EqualTriangle, RightTriangle, HalfCircle;

		public static ProfileCurve setValue(int value)
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

	public enum HoleType
	{
		Same(0x00), Circle(0x10), Square(0x20), Triangle(0x30);

		public static HoleType setValue(int value)
		{
			for (HoleType e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private HoleType(int value)
		{
			_value = (byte) value;
		}
	}

	public enum PathCurve
	{
		Line(0x10), Circle(0x20), Circle2(0x30), Test(0x40), Flexible(0x80);

		public static PathCurve setValue(int value)
		{
			for (PathCurve e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private PathCurve(int value)
		{
			_value = (byte) value;
		}
	}

	// Material type for a primitive
	public enum Material
	{
		Stone, Metal, Glass, Wood, Flesh, Plastic, Rubber, Light;

		public static Material setValue(int value)
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

	// Used in a helper function to roughly determine prim shape
	public enum PrimType
	{
		Unknown, Box, Cylinder, Prism, Sphere, Torus, Tube, Ring, Sculpt, Mesh;

		public static PrimType setValue(int value)
		{
			if (value >= 0 && value < values().length)
				return values()[value];
			return Unknown;
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// Extra parameters for primitives, these flags are for features that have
	// been
	// added after the original ObjectFlags that has all eight bits reserved
	// already
	public enum ExtraParamType
	{
		// Whether this object has flexible parameters
		Flexible(0x10),
		// Whether this object has light parameters
		Light(0x20),
		// Whether this object is a sculpted prim
		Sculpt(0x30),
        // Whether this object is a mesh</summary>
        Mesh(0x60);

		public static ExtraParamType setValue(int value)
		{
			for (ExtraParamType e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ExtraParamType(int value)
		{
			_value = (byte) value;
		}
	}

	public enum JointType
	{
		Invalid, Hinge, Point,
		// [Obsolete]
		// LPoint,
		// [Obsolete]
		// Wheel
		;

		public static JointType setValue(int value)
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

	public enum SculptType
	{
		None(0), Sphere(1), Torus(2), Plane(3), Cylinder(4), Mesh(5), Invert(64), Mirror(128);

		public static SculptType setValues(int value)
		{
			for (SculptType e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private SculptType(int value)
		{
			_value = (byte) value;
		}
	}

	public enum FaceType
	{
		PathBegin, PathEnd, InnerSide, ProfileBegin, ProfileEnd, OuterSide0, OuterSide1, OuterSide2, OuterSide3;

		public static FaceType setValue(int value)
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

	public enum ObjectCategory
	{
		Invalid(-1), None(0), Owner(1), Group(2), Other(3), Selected(4), Temporary(5);

		public static ObjectCategory setValue(int value)
		{
			for (ObjectCategory e : values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return _value;
		}

		private byte _value;

		private ObjectCategory(int value)
		{
			_value = (byte) value;
		}
	}

	/**
	 * Attachment points for objects on avatar bodies
	 * 
	 * Both InventoryObject and InventoryAttachment types can be attached
	 * 
	 */
	public enum AttachmentPoint
	{
		/** Right hand if object was not previously attached */
		Default,
		/** Chest */
		Chest,
		/** Skull */
		Skull,
		/** Left shoulder */
		LeftShoulder,
		/** Right shoulder */
		RightShoulder,
		/** Left hand */
		LeftHand,
		/** Right hand */
		RightHand,
		/** Left foot */
		LeftFoot,
		/** Right foot */
		RightFoot,
		/** Spine */
		Spine,
		/** Pelvis */
		Pelvis,
		/** Mouth */
		Mouth,
		/** Chin */
		Chin,
		/** Left ear */
		LeftEar,
		/** Right ear */
		RightEar,
		/** Left eyeball */
		LeftEyeball,
		/** Right eyeball */
		RightEyeball,
		/** Nose */
		Nose,
		/** Right upper arm */
		RightUpperArm,
		/** Right forearm */
		RightForearm,
		/** Left upper arm */
		LeftUpperArm,
		/** Left forearm */
		LeftForearm,
		/** Right hip */
		RightHip,
		/** Right upper leg */
		RightUpperLeg,
		/** Right lower leg */
		RightLowerLeg,
		/** Left hip */
		LeftHip,
		/** Left upper leg */
		LeftUpperLeg,
		/** Left lower leg */
		LeftLowerLeg,
		/** Stomach */
		Stomach,
		/** Left pectoral */
		LeftPec,
		/** Right pectoral */
		RightPec,
		/** HUD Center position 2 */
		HUDCenter2,
		/** HUD Top-right */
		HUDTopRight,
		/** HUD Top */
		HUDTop,
		/** HUD Top-left */
		HUDTopLeft,
		/** HUD Center */
		HUDCenter,
		/** HUD Bottom-left */
		HUDBottomLeft,
		/** HUD Bottom */
		HUDBottom,
		/** HUD Bottom-right */
		HUDBottomRight,
		/** Neck */
		Neck,
		/** Avatar Center */
		Root;

		private static String[] strings = { "Default", "Chest", "Head", "Left Shoulder", "Right Shoulder", "Left Hand",
				"Right Hand", "Left Foot", "Right Foot", "Back", "Pelvis", "Mouth", "Chin", "Left Ear", "Right Ear",
				"Left Eye", "Right Eye", "Nose", "Right Upper Arm", "Right Lower Arm", "Left Upper Arm",
				"Left Lower Arm", "Right Hip", "Right Upper Leg", "Right Lower Leg", "Left Hip", "Left Hip",
				"Left Lower Leg", "Belly", "Left Pec", "Right Pec", "HUD Center 2", "HUD Top Right", "HUD Top Center",
				"HUD Top Left", "HUD Center 1", "HUD Bottom Left", "HUD Bottom", "HUD Bottom Right", "Neck", "Avatar Center" };

		public static AttachmentPoint setValue(String value)
		{
			for (int i = 0; i < values().length; i++)
			{
				if (value.equals(strings[i]))
				{
					return values()[i];
				}
			}
			return Default;
		}

		public static AttachmentPoint setValue(int value)
		{
			if (value >= 0 && value < values().length)
				return values()[value];
			return Default;
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}

		@Override
		public String toString()
		{
			return toString(this);
		}

		public static String toString(AttachmentPoint point)
		{
			return strings[point.ordinal()];
		}
	}

	/** Tree foliage types */
	public enum Tree
	{
		// Pine1 tree
		Pine1,
		// Oak tree
		Oak,
		// Tropical Bush1
		TropicalBush1,
		// Palm1 tree
		Palm1,
		// Dogwood tree
		Dogwood,
		// Tropical Bush2
		TropicalBush2,
		// Palm2 tree
		Palm2,
		// Cypress1 tree
		Cypress1,
		// Cypress2 tree
		Cypress2,
		// Pine2 tree
		Pine2,
		// Plumeria
		Plumeria,
		// Winter pinetree1
		WinterPine1,
		// Winter Aspen tree
		WinterAspen,
		// Winter pinetree2
		WinterPine2,
		// Eucalyptus tree
		Eucalyptus,
		// Fern
		Fern,
		// Eelgrass
		Eelgrass,
		// Sea Sword
		SeaSword,
		// Kelp1 plant
		Kelp1,
		// Beach grass
		BeachGrass1,
		// Kelp2 plant
		Kelp2;

		public static Tree setValue(byte value)
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

	/** Grass foliage types */
	public enum Grass
	{
		Grass0, Grass1, Grass2, Grass3, Grass4, Undergrowth1;

		public static Grass setValue(int value)
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

	/** Action associated with clicking on an object */
	public enum ClickAction
	{
		/** Touch object */
		Touch,
		/** Sit on object */
		Sit,
		/** Purchase object or contents */
		Buy,
		/** Pay the object */
		Pay,
		/** Open task inventory */
		OpenTask,
		/** Play parcel media */
		PlayMedia,
		/** Open parcel media */
		OpenMedia;

		public static ClickAction setValue(int value)
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

	// #region Subclasses

	// Parameters used to construct a visual representation of a primitive
	public class ConstructionData
	{
		public ProfileCurve ProfileCurve;
		public HoleType ProfileHole;
		public PathCurve PathCurve;
		public float PathEnd;
		public float PathRadiusOffset;
		public float PathSkew;
		public float PathScaleX;
		public float PathScaleY;
		public float PathShearX;
		public float PathShearY;
		public float PathTaperX;
		public float PathTaperY;
		public float PathBegin;
		public float PathTwist;
		public float PathTwistBegin;
		public float PathRevolutions;
		public float ProfileBegin;
		public float ProfileEnd;
		public float ProfileHollow;

		public Material Material;
		public byte State;
		public PCode PCode;

		public ConstructionData()
		{

		}

		public ConstructionData(OSD osd)
		{
			fromOSD(osd);
		}

		// #region Properties

		public ConstructionData(ConstructionData primData)
		{
			ProfileCurve = primData.ProfileCurve;
			ProfileHole = primData.ProfileHole;
			PathCurve = primData.PathCurve;
			PathEnd = primData.PathEnd;
			PathRadiusOffset = primData.PathRadiusOffset;
			PathSkew = primData.PathSkew;
			PathScaleX = primData.PathScaleX;
			PathScaleY = primData.PathScaleY;
			PathShearX = primData.PathShearX;
			PathShearY = primData.PathShearY;
			PathTaperX = primData.PathTaperX;
			PathTaperY = primData.PathTaperY;
			PathBegin = primData.PathBegin;
			PathTwist = primData.PathTwist;
			PathTwistBegin = primData.PathTwistBegin;
			PathRevolutions = primData.PathRevolutions;
			ProfileBegin = primData.ProfileBegin;
			ProfileEnd = primData.ProfileEnd;
			ProfileHollow = primData.ProfileHollow;

			Material = primData.Material;
			State = primData.State;
			PCode = primData.PCode;
		}

		// Attachment point to an avatar
		public AttachmentPoint getAttachmentPoint()
		{
			return AttachmentPoint.values()[Helpers.SwapNibbles(State)];
		}

		public void setAttachmentPoint(AttachmentPoint value)
		{
			State = Helpers.SwapNibbles((byte) value.ordinal());
		}

		public byte getProfileValue()
		{
			return (byte) (ProfileCurve.getValue() | ProfileHole.getValue());
		}

		public void setProfileValue(byte value)
		{
			ProfileCurve = Primitive.ProfileCurve.setValue(value & 0xF);
			ProfileHole = HoleType.setValue(value >> 4);
		}

		public ProfileCurve getProfileCurve()
		{
			return ProfileCurve;
		}

		public void setProfileCurve(ProfileCurve value)
		{
			ProfileCurve = value;
		}

		public HoleType getProfileHole()
		{
			return ProfileHole;
		}

		public void setProfileHole(HoleType value)
		{
			ProfileHole = value;
		}

		public Vector2 getPathBeginScale()
		{
			Vector2 begin = new Vector2(1f, 1f);
			if (PathScaleX > 1f)
				begin.X = 2f - PathScaleX;
			if (PathScaleY > 1f)
				begin.Y = 2f - PathScaleY;
			return begin;
		}

		public Vector2 getPathEndScale()
		{
			Vector2 end = new Vector2(1f, 1f);
			if (PathScaleX < 1f)
				end.X = PathScaleX;
			if (PathScaleY < 1f)
				end.Y = PathScaleY;
			return end;
		}

		// #endregion Properties

		public OSD Serialize()
		{
			OSDMap path = new OSDMap(14);
			path.put("begin", OSD.FromReal(PathBegin));
			path.put("curve", OSD.FromInteger(PathCurve.getValue()));
			path.put("end", OSD.FromReal(PathEnd));
			path.put("radius_offset", OSD.FromReal(PathRadiusOffset));
			path.put("revolutions", OSD.FromReal(PathRevolutions));
			path.put("scale_x", OSD.FromReal(PathScaleX));
			path.put("scale_y", OSD.FromReal(PathScaleY));
			path.put("shear_x", OSD.FromReal(PathShearX));
			path.put("shear_y", OSD.FromReal(PathShearY));
			path.put("skew", OSD.FromReal(PathSkew));
			path.put("taper_x", OSD.FromReal(PathTaperX));
			path.put("taper_y", OSD.FromReal(PathTaperY));
			path.put("twist", OSD.FromReal(PathTwist));
			path.put("twist_begin", OSD.FromReal(PathTwistBegin));

			OSDMap profile = new OSDMap(4);
			profile.put("begin", OSD.FromReal(ProfileBegin));
			profile.put("curve", OSD.FromInteger(ProfileCurve.getValue()));
			profile.put("hole", OSD.FromInteger(ProfileHole.getValue()));
			profile.put("end", OSD.FromReal(ProfileEnd));
			profile.put("hollow", OSD.FromReal(ProfileHollow));

			OSDMap volume = new OSDMap(2);
			volume.put("path", path);
			volume.put("profile", profile);

			return volume;
		}

		public void fromOSD(OSD osd)
		{
			if (osd instanceof OSDMap)
			{
				OSDMap map = (OSDMap) osd;

				OSDMap volume = (OSDMap) map.get("volume");
				OSDMap path = (OSDMap) volume.get("path");
				OSDMap profile = (OSDMap) volume.get("profile");

				State = 0;
				Material = Primitive.Material.setValue(map.get("material").AsInteger());
				PCode = Primitive.PCode.Prim; // TODO: Put this in SD

				PathBegin = (float) path.get("begin").AsReal();
				PathCurve = Primitive.PathCurve.setValue((byte) path.get("curve").AsInteger());
				PathEnd = (float) path.get("end").AsReal();
				PathRadiusOffset = (float) path.get("radius_offset").AsReal();
				PathRevolutions = (float) path.get("revolutions").AsReal();
				PathScaleX = (float) path.get("scale_x").AsReal();
				PathScaleY = (float) path.get("scale_y").AsReal();
				PathShearX = (float) path.get("shear_x").AsReal();
				PathShearY = (float) path.get("shear_y").AsReal();
				PathSkew = (float) path.get("skew").AsReal();
				PathTaperX = (float) path.get("taper_x").AsReal();
				PathTaperY = (float) path.get("taper_y").AsReal();
				PathTwist = (float) path.get("twist").AsReal();
				PathTwistBegin = (float) path.get("twist_begin").AsReal();

				ProfileBegin = (float) profile.get("begin").AsReal();
				ProfileEnd = (float) profile.get("end").AsReal();
				ProfileHollow = (float) profile.get("hollow").AsReal();
				ProfileCurve = Primitive.ProfileCurve.setValue(profile.get("curve").AsInteger());
				ProfileHole = Primitive.HoleType.setValue(profile.get("hole").AsInteger());

			}
		}

		@Override
		public int hashCode()
		{
			return Material != null ? Material.hashCode() : 0 ^ (int) PathBegin ^ PathCurve.hashCode() ^ (int) PathEnd
					^ (int) PathRadiusOffset ^ (int) PathRevolutions ^ (int) PathScaleX ^ (int) PathScaleY
					^ (int) PathShearX ^ (int) PathShearY ^ (int) PathSkew ^ (int) PathTaperX ^ (int) PathTaperY
					^ (int) PathTwist ^ (int) PathTwistBegin ^ PCode.hashCode() ^ (int) ProfileBegin
					^ ProfileCurve.hashCode() ^ (int) ProfileEnd ^ (int) ProfileHollow ^ State;
		}

		@Override
		public boolean equals(Object o)
		{
			return o != null && o instanceof ConstructionData ? equals((ConstructionData) o) : false;
		}

		public boolean equals(ConstructionData o)
		{
			if (o == null) return false;
			return Material == null ? Material == o.Material : Material.equals(o.Material)
					&& PathBegin == o.PathBegin && PathCurve.getValue() == o.PathCurve.getValue()
					&& PathEnd == o.PathEnd && PathRadiusOffset == o.PathRadiusOffset
					&& PathRevolutions == o.PathRevolutions && PathScaleX == o.PathScaleX && PathScaleY == o.PathScaleY
					&& PathShearX == o.PathShearX && PathShearY == o.PathShearY && PathSkew == o.PathSkew
					&& PathTaperX == o.PathTaperX && PathTaperY == o.PathTaperY && PathTwist == o.PathTwist
					&& PathTwistBegin == o.PathTwistBegin && PCode.getValue() == o.PCode.getValue()
					&& ProfileCurve.getValue() == o.ProfileCurve.getValue() && ProfileBegin == o.ProfileBegin
					&& ProfileEnd == o.ProfileEnd && ProfileHollow == o.ProfileHollow && State == o.State;

		}
	}

	// Information on the flexible properties of a primitive
	public class FlexibleData
	{
		public int Softness;
		public float Gravity;
		public float Drag;
		public float Wind;
		public float Tension;
		public Vector3 Force;

		// Default constructor
		public FlexibleData()
		{
		}

		public FlexibleData(OSD osd)
		{
			fromOSD(osd);
		}

		public FlexibleData(byte[] data, int pos)
		{
			if (data.length >= pos + 16)
			{
				Softness = ((data[pos] & 0x80) >> 6) | ((data[pos + 1] & 0x80) >> 7);

				Tension = (data[pos++] & 0x7F) / 10.0f;
				Drag = (data[pos++] & 0x7F) / 10.0f;
				Gravity = (data[pos++] / 10.0f) - 10.0f;
				Wind = data[pos++] / 10.0f;
				Force = new Vector3(data, pos);
			}
			else
			{
				Softness = 0;

				Tension = 0.0f;
				Drag = 0.0f;
				Gravity = 0.0f;
				Wind = 0.0f;
				Force = Vector3.Zero;
			}
		}

		public FlexibleData(FlexibleData data)
		{
			Softness = data.Softness;

			Tension = data.Tension;
			Drag = data.Drag;
			Gravity = data.Gravity;
			Wind = data.Wind;
			Force = new Vector3(data.Force);
		}

		public byte[] GetBytes()
		{
			byte[] data = new byte[16];
			int i = 0;

			// Softness is packed in the upper bits of tension and drag
			data[i] = (byte) ((Softness & 2) << 6);
			data[i + 1] = (byte) ((Softness & 1) << 7);

			data[i++] |= (byte) ((byte) (Tension * 10.01f) & 0x7F);
			data[i++] |= (byte) ((byte) (Drag * 10.01f) & 0x7F);
			data[i++] = (byte) ((Gravity + 10.0f) * 10.01f);
			data[i++] = (byte) (Wind * 10.01f);

			Force.toBytes(data, i);

			return data;
		}

		public OSD Serialize()
		{
			OSDMap map = new OSDMap();

			map.put("simulate_lod", OSD.FromInteger(Softness));
			map.put("gravity", OSD.FromReal(Gravity));
			map.put("air_friction", OSD.FromReal(Drag));
			map.put("wind_sensitivity", OSD.FromReal(Wind));
			map.put("tension", OSD.FromReal(Tension));
			map.put("user_force", OSD.FromVector3(Force));

			return map;
		}

		public void fromOSD(OSD osd)
		{
			if (osd.getType() == OSDType.Map)
			{
				OSDMap map = (OSDMap) osd;

				Softness = map.get("simulate_lod").AsInteger();
				Gravity = (float) map.get("gravity").AsReal();
				Drag = (float) map.get("air_friction").AsReal();
				Wind = (float) map.get("wind_sensitivity").AsReal();
				Tension = (float) map.get("tension").AsReal();
				Force = map.get("user_force").AsVector3();
			}
		}

		@Override
		public int hashCode()
		{
			return Softness ^ (int) Gravity ^ (int) Drag ^ (int) Wind ^ (int) Tension ^ Force.hashCode();
		}
	}

	// Information on the light properties of a primitive
	public class LightData
	{
		public Color4 Color;
		public float Intensity;
		public float Radius;
		public float Cutoff;
		public float Falloff;

		// Default constructor
		public LightData()
		{
		}

		public LightData(OSD osd)
		{
			fromOSD(osd);
		}

		public LightData(byte[] data, int pos)
		{
			if (data.length >= 16 + pos)
			{
				Color = new Color4(data, pos, false);
				Radius = Helpers.BytesToFloatL(data, pos + 4);
				Cutoff = Helpers.BytesToFloatL(data, pos + 8);
				Falloff = Helpers.BytesToFloatL(data, pos + 12);

				// Alpha in color is actually intensity
				Intensity = Color.A;
				Color.A = 1f;
			}
			else
			{
				Color = Color4.Black;
				Radius = 0f;
				Cutoff = 0f;
				Falloff = 0f;
				Intensity = 0f;
			}
		}

		public LightData(LightData light)
		{
			Color = new Color4(light.Color);
			Radius = light.Radius;
			Cutoff = light.Cutoff;
			Falloff = light.Falloff;
			Intensity = light.Intensity;
		}

		public byte[] GetBytes()
		{
			byte[] data = new byte[16];

			// Alpha channel in color is intensity
			Color4 tmpColor = Color;
			tmpColor.A = Intensity;
			tmpColor.ToBytes(data, 0);
			Helpers.FloatToBytesL(Radius, data, 4);
			Helpers.FloatToBytesL(Cutoff, data, 8);
			Helpers.FloatToBytesL(Falloff, data, 12);

			return data;
		}

		public OSD Serialize()
		{
			OSDMap map = new OSDMap();

			map.put("color", OSD.FromColor4(Color));
			map.put("intensity", OSD.FromReal(Intensity));
			map.put("radius", OSD.FromReal(Radius));
			map.put("cutoff", OSD.FromReal(Cutoff));
			map.put("falloff", OSD.FromReal(Falloff));

			return map;
		}

		public void fromOSD(OSD osd)
		{
			if (osd.getType() == OSDType.Map)
			{
				OSDMap map = (OSDMap) osd;

				Color = map.get("color").AsColor4();
				Intensity = (float) map.get("intensity").AsReal();
				Radius = (float) map.get("radius").AsReal();
				Cutoff = (float) map.get("cutoff").AsReal();
				Falloff = (float) map.get("falloff").AsReal();
			}
		}

		@Override
		public int hashCode()
		{
			return Color.hashCode() ^ (int) Intensity ^ (int) Radius ^ (int) Cutoff ^ (int) Falloff;
		}

		@Override
		public String toString()
		{
			return String.format("Color: %s Intensity: %s Radius: %f Cutoff: %f Falloff: %f", Color, Intensity, Radius,
					Cutoff, Falloff);
		}
	}

	// Information on the sculpt properties of a sculpted primitive
	public class SculptData
	{
		public UUID SculptTexture;
		private byte type;

		public SculptType getType()
		{
			return SculptType.values()[type & 7];
		}

		public void setType(SculptType value)
		{
			type = value.getValue();
		}

		public void setType(int value)
		{
			type = (byte) (value & 0x7);
		}

		// Render inside out (inverts the normals).
		public boolean getInvert()
		{
			return (type & SculptType.Invert.getValue()) != 0;
		}

		// Render an X axis mirror of the sculpty.
		public boolean getMirror()
		{
			return (type & SculptType.Mirror.getValue()) != 0;
		}

		// Default constructor
		public SculptData()
		{
		}

		public SculptData(OSD osd)
		{
			fromOSD(osd);
		}

		public SculptData(byte[] data, int pos)
		{
			if (data.length >= 17 + pos)
			{
				SculptTexture = new UUID(data, pos);
				type = data[pos + 16];
			}
			else
			{
				SculptTexture = UUID.Zero;
				type = SculptType.None.getValue();
			}
		}

		public SculptData(SculptData value)
		{
			SculptTexture = value.SculptTexture;
			this.type = value.getType().getValue();
		}

		public byte[] GetBytes()
		{
			byte[] data = new byte[17];

			SculptTexture.ToBytes(data, 0);
			data[16] = type;

			return data;
		}

		public OSD Serialize()
		{
			OSDMap map = new OSDMap();

			map.put("texture", OSD.FromUUID(SculptTexture));
			map.put("type", OSD.FromInteger(type));

			return map;
		}

		public void fromOSD(OSD osd)
		{
			if (osd.getType() == OSDType.Map)
			{
				OSDMap map = (OSDMap) osd;

				SculptTexture = map.get("texture").AsUUID();
				type = (byte) map.get("type").AsInteger();
			}
		}

		@Override
		public int hashCode()
		{
			return SculptTexture.hashCode() ^ type;
		}
	}

	// #endregion Subclasses

	// #region Public Members

	// The Object's UUID, asset server
	public UUID ID;

	public UUID GroupID;

	// Object ID in Region (sim) it is in
	public int LocalID;

	public int ParentID;
	public long RegionHandle;
	public int Flags;
	// Location of Object (x,y,z probably)
	public Vector3 Position;
	// Rotational Position of Object
	public Quaternion Rotation = Quaternion.Identity;
	public Vector3 Scale;
	public Vector3 Velocity;
	public Vector3 AngularVelocity;
	public Vector3 Acceleration;
	public Vector4 CollisionPlane;
	public FlexibleData Flexible;
	public LightData Light;
	public SculptData Sculpt;
	public ClickAction clickAction;
	public UUID SoundID;

	// Identifies the owner if audio or a particle system is active
	public UUID OwnerID;
	// Foliage type for this primitive. Only applicable if this primitive is
	// foliage
	public Tree TreeSpecies;
	public byte[] ScratchPad;
	public byte SoundFlags;
	public float SoundGain;
	public float SoundRadius;
	public String Text;
	public Color4 TextColor;
	public String MediaURL;
	public JointType Joint;
	public Vector3 JointPivot;
	public Vector3 JointAxisOrAnchor;
	public NameValue[] NameValues;
	public ConstructionData PrimData;
	public ObjectProperties Properties;
	public PhysicsProperties PhysicsProps;

	public boolean IsAttachment;

	public TextureEntry Textures;
	public TextureAnimation TextureAnim;

	public ParticleSystem ParticleSys;

	// Current version of the media data for the prim
	public String MediaVersion = Helpers.EmptyString;

	// Array of media entries indexed by face number
	public MediaEntry[] FaceMedia;

	// #endregion Public Members

	// #region Properties

	// Uses basic heuristics to estimate the primitive shape
	public PrimType getType()
	{
		if (Sculpt != null && Sculpt.getType() != SculptType.None)
			return PrimType.Sculpt;
 
		if (Sculpt != null && Sculpt.getType() != SculptType.None && !Sculpt.SculptTexture.equals(UUID.Zero))
        {
            if (Sculpt.getType() == SculptType.Mesh)
                return PrimType.Mesh;
			return PrimType.Sculpt;
        }

		boolean linearPath = (PrimData.PathCurve == PathCurve.Line || PrimData.PathCurve == PathCurve.Flexible);
		float scaleY = PrimData.PathScaleY;

		if (linearPath)
		{
			switch (PrimData.ProfileCurve)
			{
				case Circle:
					return PrimType.Cylinder;
				case Square:
					return PrimType.Box;
				case IsoTriangle:
				case EqualTriangle:
				case RightTriangle:
					return PrimType.Prism;
				case HalfCircle:
				default:
					return PrimType.Unknown;
			}
		}

		switch (PrimData.PathCurve)
		{
			case Flexible:
				return PrimType.Unknown;
			case Circle:
				switch (PrimData.ProfileCurve)
				{
					case Circle:
						if (scaleY > 0.75f)
							return PrimType.Sphere;
						return PrimType.Torus;
					case HalfCircle:
						return PrimType.Sphere;
					case EqualTriangle:
						return PrimType.Ring;
					case Square:
						if (scaleY <= 0.75f)
							return PrimType.Tube;
					default:
						return PrimType.Unknown;
				}
			case Circle2:
				if (PrimData.ProfileCurve == ProfileCurve.Circle)
					return PrimType.Sphere;
			default:
				return PrimType.Unknown;
		}
	}

	// #endregion Properties

	// #region Constructors

	// Default constructor
	public Primitive()
	{
		// Default a few null property values to String.Empty
		Text = Helpers.EmptyString;
		MediaURL = Helpers.EmptyString;
	}

	public Primitive(OSD osd)
	{
		this();
		fromOSD(osd);
	}

	public Primitive(Primitive prim)
	{
		ID = new UUID(prim.ID);
		GroupID = new UUID(prim.GroupID);
		LocalID = prim.LocalID;
		ParentID = prim.ParentID;
		RegionHandle = prim.RegionHandle;
		Flags = prim.Flags;
		TreeSpecies = prim.TreeSpecies;
		if (prim.ScratchPad != null)
		{
			ScratchPad = new byte[prim.ScratchPad.length];
			System.arraycopy(prim.ScratchPad, 0, ScratchPad, 0, ScratchPad.length);
		}
		else
			ScratchPad = Helpers.EmptyBytes;
		Position = new Vector3(prim.Position);
		Scale = prim.Scale;
		Rotation = new Quaternion(prim.Rotation);
		Velocity = new Vector3(prim.Velocity);
		AngularVelocity = new Vector3(prim.AngularVelocity);
		Acceleration = new Vector3(prim.Acceleration);
		CollisionPlane = new Vector4(prim.CollisionPlane);
		Flexible = new FlexibleData(prim.Flexible);
		Light = new LightData(prim.Light);
		Sculpt = new SculptData(prim.Sculpt);
		clickAction = prim.clickAction;
		SoundID = new UUID(prim.SoundID);
		OwnerID = new UUID(prim.OwnerID);
		SoundFlags = prim.SoundFlags;
		SoundGain = prim.SoundGain;
		SoundRadius = prim.SoundRadius;
		Text = new String(prim.Text);
		TextColor = new Color4(prim.TextColor);
		MediaURL = prim.MediaURL;
		Joint = prim.Joint;
		JointPivot = prim.JointPivot;
		JointAxisOrAnchor = prim.JointAxisOrAnchor;
		if (prim.NameValues != null)
		{
			if (NameValues == null || NameValues.length != prim.NameValues.length)
				NameValues = new NameValue[prim.NameValues.length];
			for (int i = 0; i < prim.NameValues.length; i++)
				NameValues[i] = prim.NameValues[i];
		}
		else
			NameValues = null;
		PrimData = new ConstructionData(prim.PrimData);
		Properties = new ObjectProperties(prim.Properties);
		Textures = new TextureEntry(prim.Textures);
		TextureAnim = Textures.new TextureAnimation(prim.TextureAnim);
		ParticleSys = new ParticleSystem(prim.ParticleSys);
	}

	// #endregion Constructors

	// #region Public Methods

	public OSD Serialize()
	{

		OSDMap prim = new OSDMap(9);
		if (Properties != null)
		{
			prim.put("name", OSD.FromString(Properties.Name));
			prim.put("description", OSD.FromString(Properties.Description));
		}
		else
		{
			prim.put("name", OSD.FromString("Object"));
			prim.put("description", OSD.FromString(Helpers.EmptyString));
		}

		prim.put("phantom", OSD.FromBoolean((Flags & PrimFlags.Phantom) != 0));
		prim.put("physical", OSD.FromBoolean((Flags & PrimFlags.Physics) != 0));
		prim.put("position", OSD.FromVector3(Position));
		prim.put("rotation", OSD.FromQuaternion(Rotation));
		prim.put("scale", OSD.FromVector3(Scale));
		prim.put("material", OSD.FromInteger(PrimData.Material.getValue()));
		prim.put("shadows", OSD.FromBoolean((Flags & PrimFlags.CastShadows) != 0));
		prim.put("parentid", OSD.FromInteger(ParentID));

		prim.put("volume", PrimData.Serialize());

		if (Textures != null)
			prim.put("textures", Textures.Serialize());

		if (Light != null)
			prim.put("light", Light.Serialize());

		if (Flexible != null)
			prim.put("flex", Flexible.Serialize());

		if (Sculpt != null)
			prim.put("sculpt", Sculpt.Serialize());

		return prim;
	}

	public void fromOSD(OSD osd)
	{
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;

			if (map.get("phantom").AsBoolean())
				Flags = PrimFlags.Phantom;

			if (map.get("physical").AsBoolean())
				Flags |= PrimFlags.Physics;

			if (map.get("shadows").AsBoolean())
				Flags |= PrimFlags.CastShadows;

			ParentID = map.get("parentid").AsInteger();
			Position = map.get("position").AsVector3();
			Rotation = map.get("rotation").AsQuaternion();
			Scale = map.get("scale").AsVector3();

			PrimData = new ConstructionData(map.get("volume"));
			Flexible = new FlexibleData(map.get("flex"));
			Light = new LightData(map.get("light"));
			Sculpt = new SculptData(map.get("sculpt"));
			Textures = new TextureEntry(map.get("textures"));
			Properties = new ObjectProperties();

			String s;
			s = map.get("name").AsString();
			if (s != null && !s.isEmpty())
			{
				Properties.Name = s;
			}
			s = map.get("description").AsString();
			if (s != null && !s.isEmpty())
			{
				Properties.Description = s;
			}
		}
	}

	public int SetExtraParamsFromBytes(byte[] data, int pos)
	{
		int i = pos;
		int totalLength = 1;

		if (data.length == 0 || pos >= data.length)
			return 0;

		byte extraParamCount = data[i++];

		for (int k = 0; k < extraParamCount; k++)
		{
			ExtraParamType type = ExtraParamType.setValue(Helpers.BytesToUInt16L(data, i));
			i += 2;

			int paramLength = (int) Helpers.BytesToUInt32L(data, i);
			i += 4;

			switch (type)
			{
				case Flexible:
					Flexible = new FlexibleData(data, i);
					break;
				case Light:
					Light = new LightData(data, i);
					break;
				case Sculpt:
				case Mesh:
					Sculpt = new SculptData(data, i);
					break;
			}
			i += paramLength;
			totalLength += paramLength + 6;
		}
		return totalLength;
	}

	public byte[] GetExtraParamsBytes()
	{
		byte[] _Flexible = null;
		byte[] _Light = null;
		byte[] _Sculpt = null;
		byte[] buffer = null;
		int size = 1;
		int pos = 0;
		byte count = 0;

		if (Flexible != null)
		{
			_Flexible = Flexible.GetBytes();
			size += _Flexible.length + 6;
			++count;
		}
		if (Light != null)
		{
			_Light = Light.GetBytes();
			size += _Light.length + 6;
			++count;
		}
		if (Sculpt != null)
		{
			_Sculpt = Sculpt.GetBytes();
			size += _Sculpt.length + 6;
			++count;
		}

		buffer = new byte[size];
		buffer[0] = count;
		++pos;

		if (_Flexible != null)
		{
			System.arraycopy(Helpers.UInt16ToBytesL(ExtraParamType.Flexible.getValue()), 0, buffer, pos, 2);
			pos += 2;

			System.arraycopy(Helpers.UInt32ToBytesL(_Flexible.length), 0, buffer, pos, 4);
			pos += 4;

			System.arraycopy(_Flexible, 0, buffer, pos, _Flexible.length);
			pos += _Flexible.length;
		}
		if (_Light != null)
		{
			System.arraycopy(Helpers.UInt16ToBytesL(ExtraParamType.Light.getValue()), 0, buffer, pos, 2);
			pos += 2;

			System.arraycopy(Helpers.UInt32ToBytesL(_Light.length), 0, buffer, pos, 4);
			pos += 4;

			System.arraycopy(_Light, 0, buffer, pos, _Light.length);
			pos += _Light.length;
		}
		if (_Sculpt != null)
		{
            if (Sculpt.getType() == SculptType.Mesh)
            {
    			System.arraycopy(Helpers.UInt16ToBytesL(ExtraParamType.Mesh.getValue()), 0, buffer, pos, 2);
            }
            else
            {
    			System.arraycopy(Helpers.UInt16ToBytesL(ExtraParamType.Sculpt.getValue()), 0, buffer, pos, 2);
            }
			pos += 2;

			System.arraycopy(Helpers.UInt32ToBytesL(_Sculpt.length), 0, buffer, pos, 4);
			pos += 4;

			System.arraycopy(_Sculpt, 0, buffer, pos, _Sculpt.length);
			pos += _Sculpt.length;
		}
		return buffer;
	}

	// #endregion Public Methods

	// #region Overrides

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Primitive) ? equals(this, (Primitive) obj) : false;
	}

	public boolean equals(Primitive other)
	{
		return equals(this, other);
	}

	public static boolean equals(Primitive lhs, Primitive rhs)
	{
		if (lhs == null || rhs == null)
		{
			return rhs == lhs;
		}
		return (lhs.ID == rhs.ID);
	}

	@Override
	public String toString()
	{
		switch (PrimData.PCode)
		{
			case Prim:
				return String.format("%s (%s)", getType().toString(), ID.toString());
			default:
				return String.format("%s (%s)", PrimData.PCode.toString(), ID.toString());
		}
	}

	@Override
	public int hashCode()
	{
		return Position.hashCode() ^ Velocity.hashCode() ^ Acceleration.hashCode() ^ Rotation.hashCode()
				^ AngularVelocity.hashCode() ^ clickAction.hashCode() ^ (Flexible != null ? Flexible.hashCode() : 0)
				^ (Light != null ? Light.hashCode() : 0) ^ (Sculpt != null ? Sculpt.hashCode() : 0) ^ Flags
				^ MediaURL.hashCode() ^ (Properties != null ? Properties.OwnerID.hashCode() : 0) ^ ParentID
				^ PrimData.hashCode() ^ ParticleSys.hashCode() ^ TextColor.hashCode() ^ TextureAnim.hashCode()
				^ (Textures != null ? Textures.hashCode() : 0) ^ (int) SoundRadius ^ Scale.hashCode()
				^ SoundID.hashCode() ^ Text.hashCode() ^ TreeSpecies.hashCode();
	}

	// #endregion Overrides

	// #region Parameter Packing Methods

	public static short PackBeginCut(float beginCut)
	{
		return (short) Math.round(beginCut / CUT_QUANTA);
	}

	public static short PackEndCut(float endCut)
	{
		return (short) (50000 - (short) Math.round(endCut / CUT_QUANTA));
	}

	public static byte PackPathScale(float pathScale)
	{
		return (byte) (200 - (byte) Math.round(pathScale / SCALE_QUANTA));
	}

	public static byte PackPathShear(float pathShear)
	{
		return (byte) Math.round(pathShear / SHEAR_QUANTA);
	}

	/**
	 * Packs PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew
	 * parameters in to signed eight bit values
	 * 
	 * @param pathTwist
	 *            Floating point parameter to pack
	 * @return Signed eight bit value containing the packed parameter
	 */
	public static byte PackPathTwist(float pathTwist)
	{
		return (byte) Math.round(pathTwist / SCALE_QUANTA);
	}

	public static byte PackPathTaper(float pathTaper)
	{
		return (byte) Math.round(pathTaper / TAPER_QUANTA);
	}

	public static byte PackPathRevolutions(float pathRevolutions)
	{
		return (byte) Math.round((pathRevolutions - 1f) / REV_QUANTA);
	}

	public static short PackProfileHollow(float profileHollow)
	{
		return (short) Math.round(profileHollow / HOLLOW_QUANTA);
	}

	// #endregion Parameter Packing Methods

	// #region Parameter Unpacking Methods

	public static float UnpackBeginCut(short beginCut)
	{
		return beginCut * CUT_QUANTA;
	}

	public static float UnpackEndCut(short endCut)
	{
		return (50000 - endCut) * CUT_QUANTA;
	}

	public static float UnpackPathScale(byte pathScale)
	{
		return (200 - pathScale) * SCALE_QUANTA;
	}

	public static float UnpackPathShear(byte pathShear)
	{
		return pathShear * SHEAR_QUANTA;
	}

	/**
	 * Unpacks PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew
	 * parameters from signed eight bit integers to floating point values
	 * 
	 * @param pathTwist
	 *            Signed eight bit value to unpack
	 * @return Unpacked floating point value
	 */
	public static float UnpackPathTwist(byte pathTwist)
	{
		return pathTwist * SCALE_QUANTA;
	}

	public static float UnpackPathTaper(byte pathTaper)
	{
		return pathTaper * TAPER_QUANTA;
	}

	public static float UnpackPathRevolutions(byte pathRevolutions)
	{
		return pathRevolutions * REV_QUANTA + 1f;
	}

	public static float UnpackProfileHollow(short profileHollow)
	{
		return profileHollow * HOLLOW_QUANTA;
	}
}
