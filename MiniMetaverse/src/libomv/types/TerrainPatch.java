package libomv.types;

public class TerrainPatch
{
	public enum LayerType
	{
		Land(0x4C), Water(0x57), Wind(0x37), Cloud(0x38);

		public static LayerType setValue(int value)
		{
			for (LayerType e : values())
			{
				if (e._value == value)
				{
					return e;
				}
			}
			return Land;
		}

		public byte getValue()
		{
			return _value;
		}

		private final byte _value;

		private LayerType(int value)
		{
			_value = (byte) value;
		}
	}

	public final class GroupHeader
	{
		public int Stride;
		public int PatchSize;
		public LayerType Type;
	}

	public final class Header
	{
		public float DCOffset;
		public int Range;
		public int QuantWBits;
		public int PatchIDs;
		public int WordBits;

		public int getX()
		{
			return PatchIDs >> 5;
		}

		public void setX(int value)
		{
			PatchIDs += (value << 5);
		}

		public int getY()
		{
			return PatchIDs & 0x1F;
		}

		public void setY(int value)
		{
			PatchIDs |= value & 0x1F;
		}
	}

	/* X position of this patch */
	public int X;
	/* Y position of this patch */
	public int Y;
	/* A 16x16 array of floats holding decompressed layer data */
	public float[] Data;
}
