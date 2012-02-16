/**
 * Copyright (c) 2007-2008, openmetaverse.org
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
package libomv.capabilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.LLSD.LLSDBinary;
import libomv.StructuredData.LLSD.LLSDXml;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class CapsToPacket
{
	// #region Serialization/Deserialization
	public static String ToXmlString(Packet packet) throws IOException, IllegalArgumentException,
			IllegalAccessException
	{
		return LLSDXml.serializeToString(GetLLSD(packet));
	}

	public static OSD GetLLSD(Packet packet) throws IllegalArgumentException, IllegalAccessException
	{
		OSDMap body = new OSDMap();
		java.lang.Class<? extends PacketType> type = packet.getType().getClass();

		for (Field field : type.getDeclaredFields())
		{
			if (Modifier.isPublic(field.getModifiers()))
			{
				Object object = field.get(packet);
				if (field.getType().isArray())
				{
					int length = Array.getLength(object);
					OSDArray blockList = new OSDArray(length);
					for (int i = 0; i < length; i++)
					{
						Object block = Array.get(object, i);
						blockList.add(BuildLLSDBlock(block));
					}
					body.put(field.getName(), blockList);
				}
				else
				{
					body.put(field.getName(), BuildLLSDBlock(object));
				}
			}
		}
		return body;
	}

	public static byte[] ToBinary(Packet packet) throws IOException, IllegalArgumentException, IllegalAccessException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		LLSDBinary.serialize(stream, GetLLSD(packet));
		return stream.toByteArray();
	}

	public static Packet FromXmlString(String xml) throws IOException, ParseException
	{
		return FromLLSD(LLSDXml.parse(xml));
	}

	public static Packet FromLLSD(OSD osd)
	{
		// FIXME: Need the inverse of the reflection magic above done here
		throw new UnsupportedOperationException();
	}

	// #endregion Serialization/Deserialization

	/**
	 * Attempts to convert an LLSD structure to a known Packet type
	 * 
	 * @param capsEventName
	 *            Event name, this must match an actual packet name for a Packet
	 *            to be successfully built
	 * @param body
	 *            LLSD to convert to a Packet
	 * @return A Packet on success, otherwise null
	 * @throws ClassNotFoundException
	 */
	public static Packet BuildPacket(String capsKey, OSDMap body)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// Check if we have a subclass of packet with the same name as this
		// event
		Class<?> type;
		try
		{
			type = classLoader.loadClass("libomv.packets." + capsKey + "Packet");
		}
		catch (ClassNotFoundException e)
		{
			return null;
		}

		Packet packet = null;

		try
		{
			// Create an instance of the object
			packet = (Packet) type.newInstance();

			// Iterate over all of the fields in the packet class, looking for
			// matches in the LLSD
			for (Field field : type.getFields())
			{
				if (body.containsKey(field.getName()))
				{
					Class<?> blockType = field.getType();

					if (blockType.isArray())
					{
						OSDArray array = (OSDArray) body.get(field.getName());
						Class<?> elementType = blockType.getComponentType();
						Object[] blockArray = (Object[]) Array.newInstance(elementType, array.size());

						for (int i = 0; i < array.size(); i++)
						{
							OSDMap map = (OSDMap) array.get(i);
							blockArray[i] = ParseLLSDBlock(map, elementType);
						}
						field.set(packet, blockArray);
					}
					else
					{
						OSDMap map = (OSDMap) body.get(field.getName());
						field.set(packet, ParseLLSDBlock(map, blockType));
					}
				}
			}
		}
		catch (Exception ex)
		{
			// FIXME
			Logger.Log(ex.getMessage(), LogLevel.Error, ex);
		}

		return packet;
	}

	private static Object ParseLLSDBlock(OSDMap blockData, Class<?> blockType) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException
	{
		Object block = blockType.newInstance();

		// Iterate over each field and set the value if a match was found in the
		// LLSD
		for (Field field : blockType.getFields())
		{
			if (blockData.containsKey(field.getName()))
			{
				Class<?> fieldType = field.getType();

				if (fieldType == long.class)
				{
					// ulongs come in as a byte array, convert it manually here
					byte[] bytes = blockData.get(field.getName()).AsBinary();
					long value = Helpers.BytesToUInt64B(bytes);
					field.set(block, value);
				}
				else if (fieldType == int.class)
				{
					// uints come in as a byte array, convert it manually here
					byte[] bytes = blockData.get(field.getName()).AsBinary();
					long value = Helpers.BytesToUInt32B(bytes);
					field.set(block, (int) value);
				}
				else if (fieldType == short.class)
				{
					field.set(block, (short) blockData.get(field.getName()).AsInteger());
				}
				else if (fieldType == byte.class)
				{
					field.set(block, (byte) blockData.get(field.getName()).AsInteger());
				}
				else if (fieldType == String.class)
				{
					field.set(block, blockData.get(field.getName()).AsString());
				}
				else if (fieldType == boolean.class)
				{
					field.set(block, blockData.get(field.getName()).AsBoolean());
				}
				else if (fieldType == float.class)
				{
					field.set(block, (float) blockData.get(field.getName()).AsReal());
				}
				else if (fieldType == double.class)
				{
					field.set(block, blockData.get(field.getName()).AsReal());
				}
				else if (fieldType == UUID.class)
				{
					field.set(block, blockData.get(field.getName()).AsUUID());
				}
				else if (fieldType == Vector3.class)
				{
					Vector3 vec = ((OSDArray) blockData.get(field.getName())).AsVector3();
					field.set(block, vec);
				}
				else if (fieldType == Vector4.class)
				{
					Vector4 vec = ((OSDArray) blockData.get(field.getName())).AsVector4();
					field.set(block, vec);
				}
				else if (fieldType == Quaternion.class)
				{
					Quaternion quat = ((OSDArray) blockData.get(field.getName())).AsQuaternion();
					field.set(block, quat);
				}
                else if (fieldType == byte[].class && blockData.get(field.getName()).getType() == OSDType.String)
                {
                    field.set(block, Helpers.StringToBytes(blockData.get(field.getName()).AsString()));
                }
			}
		}
		return block;
	}

	private static OSD BuildLLSDBlock(Object block) throws IllegalArgumentException, IllegalAccessException
	{
		OSDMap map = new OSDMap();
		Class<?> blockType = block.getClass();

		for (Field field : blockType.getFields())
		{
			if (Modifier.isPublic(field.getModifiers()))
			{
				map.put(field.getName(), OSD.FromObject(field.get(block)));
			}
		}
		return map;
	}
}
