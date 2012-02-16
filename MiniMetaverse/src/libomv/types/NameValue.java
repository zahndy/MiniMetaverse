package libomv.types;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

public final class NameValue
{
	/* Type of the value */
	public enum ValueType
	{
		// Unknown
		Unknown(-1),
		// String value
		String(0),

		F32(1),

		S32(2),

		VEC3(3),

		U32(4),
		// Deprecated
		CAMERA(5),
		// String value, but designated as an asset
		Asset(6),

		U64(7);

		public int val;

		private ValueType(int val)
		{
			this.val = val;
		}
	}

	public enum ClassType
	{
		Unknown(-1), ReadOnly(0), ReadWrite(1), Callback(2);

		public int val;

		private ClassType(int val)
		{
			this.val = val;
		}
	}

	public enum SendtoType
	{
		Unknown(-1), Sim(0), DataSim(1), SimViewer(2), DataSimViewer(3);

		public int val;

		private SendtoType(int val)
		{
			this.val = val;
		}
	}

	public String Name;
	public ValueType Type;
	public ClassType Class;
	public SendtoType Sendto;
	public Object Value;

	private static final String[] TypeStrings = new String[] { "STRING", "F32", "S32", "VEC3", "U32", "ASSET", "U64" };
	private static final String[] ClassStrings = new String[] { "R", "RW", "CB" };
	private static final String[] SendtoStrings = new String[] { "S", "DS", "SV", "DSV" };
	private static final char[] Separators = new char[] { ' ', '\n', '\t', '\r' };

	/**
	 * Constructor that takes all the fields as parameters
	 * 
	 * @param name
	 * @param valueType
	 * @param classType
	 * @param sendtoType
	 * @param value
	 */
	public NameValue(String name, ValueType valueType, ClassType classType, SendtoType sendtoType, Object value)
	{
		Name = name;
		Type = valueType;
		Class = classType;
		Sendto = sendtoType;
		Value = value;
	}

	/**
	 * Constructor that takes a single line from a NameValue field
	 * 
	 * @param data
	 */
	public NameValue(String data)
	{
		int i;

		// Name
		i = Helpers.indexOfAny(data, Separators);
		if (i < 1)
		{
			Name = Helpers.EmptyString;
			Type = ValueType.Unknown;
			Class = ClassType.Unknown;
			Sendto = SendtoType.Unknown;
			Value = null;
			return;
		}
		Name = data.substring(0, i);
		data = data.substring(i + 1);

		// Type
		i = Helpers.indexOfAny(data, Separators);
		if (i > 0)
		{
			Type = getValueType(data.substring(0, i));
			data = data.substring(i + 1);

			// Class
			i = Helpers.indexOfAny(data, Separators);
			if (i > 0)
			{
				Class = getClassType(data.substring(0, i));
				data = data.substring(i + 1);

				// Sendto
				i = Helpers.indexOfAny(data, Separators);
				if (i > 0)
				{
					Sendto = getSendtoType(data.substring(0, 1));
					data = data.substring(i + 1);
				}
			}
		}

		// Value
		Type = ValueType.String;
		Class = ClassType.ReadOnly;
		Sendto = SendtoType.Sim;
		Value = null;
		setValue(data);
	}

	public static String NameValuesToString(NameValue[] values)
	{
		if (values == null || values.length == 0)
		{
			return "";
		}

		StringBuilder output = new StringBuilder();

		for (int i = 0; i < values.length; i++)
		{
			NameValue value = values[i];

			if (value.Value != null)
			{
				String newLine = (i < values.length - 1) ? "\n" : "";
				output.append(String.format("%s %s %s %s %s%s", value.Name, TypeStrings[value.Type.val],
						ClassStrings[value.Class.val], SendtoStrings[value.Sendto.val], value.Value, newLine));
			}
		}

		return output.toString();
	}

	private void setValue(String value)
	{
		switch (Type)
		{
			case Asset:
			case String:
				Value = value;
				break;
			case F32:
			{
				float temp = Helpers.TryParseFloat(value);
				Value = temp;
				break;
			}
			case S32:
			{
				int temp;
				temp = Helpers.TryParseInt(value);
				Value = temp;
				break;
			}
			case U32:
			{
				int temp = Helpers.TryParseInt(value);
				Value = temp;
				break;
			}
			case U64:
			{
				long temp = Helpers.TryParseLong(value);
				Value = temp;
				break;
			}
			case VEC3:
			{
				RefObject<Vector3> temp = new RefObject<Vector3>((Vector3) Value);
				Vector3.TryParse(value, temp);
				break;
			}
			default:
				Value = null;
				break;
		}
	}

	private static ValueType getValueType(String value)
	{
		ValueType type = ValueType.Unknown;
		int i = 1;
		for (String s : TypeStrings)
		{
			if (s.equals(value))
			{
				type = ValueType.values()[i];
			}
		}

		if (type == ValueType.Unknown)
		{
			type = ValueType.String;
		}

		return type;
	}

	private static ClassType getClassType(String value)
	{
		ClassType type = ClassType.Unknown;
		int i = 1;
		for (String s : ClassStrings)
		{
			if (s.equals(value))
			{
				type = ClassType.values()[i];
			}
		}

		if (type == ClassType.Unknown)
		{
			type = ClassType.ReadOnly;
		}

		return type;
	}

	private static SendtoType getSendtoType(String value)
	{
		SendtoType type = SendtoType.Unknown;
		int i = 1;
		for (String s : SendtoStrings)
		{
			if (s.equals(value))
			{
				type = SendtoType.values()[i];
			}
		}

		if (type == SendtoType.Unknown)
		{
			type = SendtoType.Sim;
		}

		return type;
	}
}
