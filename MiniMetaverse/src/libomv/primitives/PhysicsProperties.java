package libomv.primitives;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;

// Describes physics attributes of the prim
public class PhysicsProperties
{
	// Type of physics representation used for this prim in the simulator
	public enum PhysicsShapeType
	{
		// Use prim physics form this object
		Prim,
		// No physics, prim doesn't collide
		None,
		// Use convex hull represantion of this prim
		ConvexHull;

		public static PhysicsShapeType setValue(int value)
		{
			if (value <= ConvexHull.getValue())
				return values()[value];
			return Prim;
		}

		public static byte getValue(PhysicsShapeType value)
		{
			return (byte) value.ordinal();
		}

		public byte getValue()
		{
			return (byte) ordinal();
		}
	}

	// Primitive's local ID
	public int LocalID;
	// Density (1000 for normal density)
	public float Density;
	// Friction
	public float Friction;
	// Gravity multiplier (1 for normal gravity)
	public float GravityMultiplier;
	// Type of physics representation of this primitive in the simulator
	public PhysicsShapeType ShapeType;
	// Restitution
	public float Restitution;

	/**
	 * Creates PhysicsProperties from OSD
	 * 
	 * @param name
	 *            OSDMap with incoming data</param>
	 */
	public PhysicsProperties(OSD osd)
	{
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;
			LocalID = map.get("LocalID").AsUInteger();
			Density = (float) map.get("Density").AsReal();
			Friction = (float) map.get("Friction").AsReal();
			GravityMultiplier = (float) map.get("GravityMultiplier").AsReal();
			Restitution = (float) map.get("Restitution").AsReal();
			ShapeType = PhysicsShapeType.setValue(map.get("PhysicsShapeType").AsInteger());
		}
	}

	/**
	 * Serializes PhysicsProperties to OSD
	 * 
	 * @returns OSDMap with serialized PhysicsProperties data
	 */
	public OSDMap GetOSD()
	{
		OSDMap map = new OSDMap(6);
		map.put("LocalID", OSD.FromUInteger(LocalID));
		map.put("Density", OSD.FromReal(Density));
		map.put("Friction", OSD.FromReal(Friction));
		map.put("GravityMultiplier", OSD.FromReal(GravityMultiplier));
		map.put("Restitution", OSD.FromReal(Restitution));
		map.put("PhysicsShapeType", OSD.FromInteger(ShapeType.getValue()));
		return map;
	}
}
