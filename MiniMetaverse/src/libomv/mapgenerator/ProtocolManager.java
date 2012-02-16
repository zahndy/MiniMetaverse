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
package libomv.mapgenerator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import libomv.types.PacketFrequency;
import libomv.utils.HashMapInt;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class ProtocolManager
{
	public Hashtable<Integer, Integer> TypeSizes;

	public HashMapInt KeywordPositions;

	public MapPacketMap LowMaps;

	public MapPacketMap MediumMaps;

	public MapPacketMap HighMaps;

	private boolean Sort;

	public ProtocolManager(String mapFile, boolean sort) throws Exception
	{
		Sort = sort;

		// Initialize the map arrays
		LowMaps = new MapPacketMap(256);
		MediumMaps = new MapPacketMap(256);
		HighMaps = new MapPacketMap(256);

		// Build the type size hash table
		TypeSizes = new Hashtable<Integer, Integer>();
		TypeSizes.put(FieldType.U8, 1);
		TypeSizes.put(FieldType.U16, 2);
		TypeSizes.put(FieldType.U32, 4);
		TypeSizes.put(FieldType.U64, 8);
		TypeSizes.put(FieldType.S8, 1);
		TypeSizes.put(FieldType.S16, 2);
		TypeSizes.put(FieldType.S32, 4);
		TypeSizes.put(FieldType.S64, 8);
		TypeSizes.put(FieldType.F32, 4);
		TypeSizes.put(FieldType.F64, 8);
		TypeSizes.put(FieldType.UUID, 16);
		TypeSizes.put(FieldType.BOOL, 1);
		TypeSizes.put(FieldType.Vector3, 12);
		TypeSizes.put(FieldType.Vector3d, 24);
		TypeSizes.put(FieldType.Vector4, 16);
		TypeSizes.put(FieldType.Quaternion, 16);
		TypeSizes.put(FieldType.IPADDR, 4);
		TypeSizes.put(FieldType.IPPORT, 2);
		TypeSizes.put(FieldType.Variable, -1);
		TypeSizes.put(FieldType.Fixed, -2);

		KeywordPositions = new HashMapInt();
		LoadMapFile(mapFile);
	}

	public MapPacket Command(String command) throws Exception
	{
		// TODO: Get a hashtable in here quick!

		MapPacket map = HighMaps.getMapPacketByName(command);
		if (map == null)
		{
			map = MediumMaps.getMapPacketByName(command);
			if (map == null)
			{
				map = LowMaps.getMapPacketByName(command);
			}
			else
			{
				throw new Exception("Cannot find map for command \"" + command + "\"");
			}
		}
		return map;
		/**
		 * TO BE PORTED // This will speed things up for now if
		 * (command.equals(LowMaps[65531].Name)) { return LowMaps[65531]; }
		 */
	}

	public MapPacket Command(byte[] data) throws Exception
	{
		int command;

		if (data.length < 7)
		{
			return null;
		}

		if (data[6] == (byte) 0xFF)
		{
			if (data[7] == (byte) 0xFF)
			{
				// Low frequency
				command = (data[8] * 256 + data[9]);
				return Command(command, PacketFrequency.Low);
			}

			// Medium frequency
			command = data[7];
			return Command(command, PacketFrequency.Medium);
		}

		// High frequency
		command = data[6];
		return Command(command, PacketFrequency.High);
	}

	public MapPacket Command(int command, int frequency) throws Exception
	{
		switch (frequency)
		{
			case PacketFrequency.High:
				return HighMaps.getMapPacketByCommand(command);
			case PacketFrequency.Medium:
				return MediumMaps.getMapPacketByCommand(command);
			case PacketFrequency.Low:
				return LowMaps.getMapPacketByCommand(command);
		}

		throw new Exception("Cannot find map for command \"" + command + "\" with frequency \"" + frequency + "\"");
	}

	public void PrintMap()
	{
		PrintOneMap(LowMaps, "Low   ");
		PrintOneMap(MediumMaps, "Medium");
		PrintOneMap(HighMaps, "High  ");
	}

	private void PrintOneMap(MapPacketMap map, String frequency)
	{
		int i;

		for (i = 0; i < map.mapPackets.size(); ++i)
		{
			MapPacket map_packet = map.mapPackets.elementAt(i);
			if (map_packet != null)
			{
				System.out.format("%s %d %d %4x - %s - %s - %s\n", frequency, i, map_packet.ID, map_packet.Frequency, map_packet.Name,
						map_packet.Trusted ? "Trusted" : "Untrusted", map_packet.Encoded ? "Unencoded" : "Zerocoded");

				for (int j = 0; j < map_packet.Blocks.size(); j++)
				{
					MapBlock block = map_packet.Blocks.get(j);
					if (block.Count == -1)
					{
						System.out.format("\t%4d %s (Variable)\n", block.KeywordPosition, block.Name);
					}
					else
					{
						System.out.format("\t%4d %s (%d)\n", block.KeywordPosition, block.Name, block.Count);
					}

					for (int k = 0; k < block.Fields.size(); k++)
					{
						MapField field = block.Fields.elementAt(k);
						System.out.format("\t\t%4d %s (%d / %d)", field.KeywordPosition, field.Name, field.Type,
								field.Count);
					}
				}
			}
		}
	}

	public static void DecodeMapFile(String mapFile, String outputFile) throws Exception
	{
		byte magicKey = 0;
		byte[] buffer = new byte[2048];
		int nread;
		InputStream map;
		OutputStream output;

		try
		{
			map = new FileInputStream(mapFile);
		}
		catch (Exception e)
		{
			throw new Exception("Map file error", e);
		}

		try
		{
			output = new FileOutputStream(outputFile);
		}
		catch (Exception e)
		{
			throw new Exception("Map file error", e);
		}

		while ((nread = map.read(buffer, 0, 2048)) != 0)
		{
			for (int i = 0; i < nread; ++i)
			{
				buffer[i] ^= magicKey;
				magicKey += 43;
			}

			output.write(buffer, 0, nread);
		}

		map.close();
		output.close();
	}

	@SuppressWarnings("null")
	private void LoadMapFile(String mapFile) throws Exception
	{
		FileReader map;
		int low = 1;
		int medium = 1;
		int high = 1;

		// Load the protocol map file
		try
		{
			map = new FileReader(mapFile);
		}
		catch (Exception e)
		{
			throw new Exception("Map file error", e);
		}

		try
		{
			BufferedReader r = new BufferedReader(map);
			String newline;
			String trimmedline;
			boolean inPacket = false;
			boolean inBlock = false;
			MapPacket currentPacket = null;
			MapBlock currentBlock = null;

			while (r.ready())
			{
				newline = r.readLine();
				trimmedline = newline.trim();

				if (!inPacket)
				{
					// Outside of all packet blocks

					if (trimmedline.equals("{"))
					{
						inPacket = true;
					}
				}
				else
				{
					// Inside of a packet block

					if (!inBlock)
					{
						// Inside a packet block, outside of the blocks

						if (trimmedline.equals("{"))
						{
							inBlock = true;
						}
						else if (trimmedline.equals("}"))
						{
							// Reached the end of the packet
							if (Sort)
								Collections.sort(currentPacket.Blocks);
							inPacket = false;
						}
						else if (trimmedline.startsWith("//"))
						{
							// ignore comment lines
						}
						else
						{
							// The packet header
							// #region ParsePacketHeader

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							if (tokens.length > 3)
							{
								if (tokens[1].equals("Fixed"))
								{
									// Remove the leading "0x"
									if (tokens[2].substring(0, 2).equals("0x"))
									{
										tokens[2] = tokens[2].substring(2, tokens[2].length());
									}

									long l_fixedID = Long.parseLong(tokens[2], 16);
									// Truncate the id to a short
									int fixedID = (int) (l_fixedID ^ 0xFFFF0000);
									currentPacket = new MapPacket();
									currentPacket.ID = fixedID;
									currentPacket.Frequency = PacketFrequency.Low;
									currentPacket.Name = tokens[0];
									currentPacket.Trusted = tokens[3].equals("Trusted");
									currentPacket.Encoded = tokens[4].equals("Zerocoded");
									currentPacket.Deprecated = tokens.length > 5 ? tokens[5].contains("Deprecated")
											: false;
									currentPacket.Blocks = new ArrayList<MapBlock>();
									LowMaps.addPacket(fixedID, currentPacket);
								}
								else if (tokens[1].equals("Low"))
								{
									currentPacket = new MapPacket();
									currentPacket.ID = low;
									currentPacket.Frequency = PacketFrequency.Low;
									currentPacket.Name = tokens[0];
									currentPacket.Trusted = tokens[2].equals("Trusted");
									currentPacket.Encoded = tokens[3].equals("Zerocoded");
									currentPacket.Deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.Blocks = new ArrayList<MapBlock>();
									LowMaps.addPacket(low, currentPacket);
									low++;
								}
								else if (tokens[1].equals("Medium"))
								{
									currentPacket = new MapPacket();
									currentPacket.ID = medium;
									currentPacket.Frequency = PacketFrequency.Medium;
									currentPacket.Name = tokens[0];
									currentPacket.Trusted = tokens[2].equals("Trusted");
									currentPacket.Encoded = tokens[3].equals("Zerocoded");
									currentPacket.Deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.Blocks = new ArrayList<MapBlock>();
									MediumMaps.addPacket(medium, currentPacket);

									medium++;
								}
								else if (tokens[1].equals("High"))
								{
									currentPacket = new MapPacket();
									currentPacket.ID = high;
									currentPacket.Frequency = PacketFrequency.High;
									currentPacket.Name = tokens[0];
									currentPacket.Trusted = tokens[2].equals("Trusted");
									currentPacket.Encoded = tokens[3].equals("Zerocoded");
									currentPacket.Deprecated = tokens.length > 4 ? tokens[4].contains("Deprecated")
											: false;
									currentPacket.Blocks = new ArrayList<MapBlock>();
									HighMaps.addPacket(high, currentPacket);

									high++;
								}
								else
								{
									Logger.Log("Unknown packet frequency : " + tokens[1], LogLevel.Error);
								}
							}
						}
					}
					else
					{
						if (trimmedline.length() > 0 && trimmedline.substring(0, 1).equals("{"))
						{
							// A field
							MapField field = new MapField();

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							field.Name = tokens[1];
							field.KeywordPosition = KeywordPosition(field.Name);
							field.Type = parseFieldType(tokens[2]);

							if (tokens[3].equals("}"))
							{
								field.Count = 1;
							}
							else
							{
								field.Count = Integer.parseInt(tokens[3]);
							}

							// Save this field to the current block
							currentBlock.Fields.addElement(field);
						}
						else if (trimmedline.equals("}"))
						{
							if (Sort)
								Collections.sort(currentBlock.Fields);
							inBlock = false;
						}
						else if (trimmedline.length() != 0 && trimmedline.substring(0, 2).equals("//") == false)
						{
							// The block header
							// #region ParseBlockHeader

							currentBlock = new MapBlock();

							// Splice the String in to tokens
							String[] tokens = trimmedline.split("\\s+");

							currentBlock.Name = tokens[0];
							currentBlock.KeywordPosition = KeywordPosition(currentBlock.Name);
							currentBlock.Fields = new Vector<MapField>();
							currentPacket.Blocks.add(currentBlock);

							if (tokens[1].equals("Single"))
							{
								currentBlock.Count = 1;
							}
							else if (tokens[1].equals("Multiple"))
							{
								currentBlock.Count = Integer.parseInt(tokens[2]);
							}
							else if (tokens[1].equals("Variable"))
							{
								currentBlock.Count = -1;
							}
							else
							{
								Logger.Log("Unknown block frequency", LogLevel.Error);
							}

							// #endregion
						}
					}
				}

				// #endregion
			}

			r.close();
			map.close();
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private int KeywordPosition(String keyword) throws Exception
	{
		if (KeywordPositions.containsKey(keyword))
		{
			return KeywordPositions.get(keyword);
		}

		int hash = 0;
		for (int i = 1; i < keyword.length(); i++)
		{
			hash = (hash + (keyword.charAt(i))) * 2;
		}
		hash *= 2;
		hash &= 0x1FFF;

		int startHash = hash;

		while (KeywordPositions.containsValue(hash))
		{
			hash++;
			hash &= 0x1FFF;
			if (hash == startHash)
			{
				// Give up looking, went through all values and they were
				// all taken.
				throw new Exception("All hash values are taken. Failed to add keyword: " + keyword);
			}
		}

		KeywordPositions.put(keyword, hash);
		return hash;
	}

	public static int parseFieldType(String token)
	{
		int value = 0;
		for (int i = 0; i < FieldType.TypeNames.length; i++)
		{
			if (FieldType.TypeNames[i].equals(token))
			{
				value = i;
				break;
			}
		}
		return value;
	}
}
