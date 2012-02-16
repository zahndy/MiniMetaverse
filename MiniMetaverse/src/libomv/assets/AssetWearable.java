/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
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
package libomv.assets;

import java.util.HashMap;
import java.util.Map.Entry;

import libomv.ObjectManager.SaleType;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// Represents a Wearable Asset, Clothing, Hair, Skin, Etc
public abstract class AssetWearable extends AssetItem
{
	// Index of TextureEntry slots for avatar appearances
	public enum AvatarTextureIndex
	{
		Unknown, HeadBodypaint, UpperShirt, LowerPants, EyesIris, Hair,	UpperBodypaint, LowerBodypaint,
		LowerShoes, HeadBaked, UpperBaked, LowerBaked, EyesBaked, LowerSocks, UpperJacket, LowerJacket,
		UpperGloves, UpperUndershirt, LowerUnderpants, Skirt, SkirtBaked, HairBaked,
        LowerAlpha, UpperAlpha, HeadAlpha, EyesAlpha, HairAlpha, HeadTattoo, UpperTattoo, LowerTattoo, NumberOfEntries;

		public static AvatarTextureIndex setValue(int value)
		{
			return values()[value + 1];
		}

		public static byte getValue(AvatarTextureIndex value)
		{
			return (byte) (value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte) (ordinal() - 1);
		}
	}

	/** Types of wearable assets */
	public static enum WearableType
	{
		/** Invalid wearable asset */
		Invalid,
		/** Body shape */
		Shape,
		/** Skin textures and attributes */
		Skin,
		/** Hair */
		Hair,
		/** Eyes */
		Eyes,
		/** Shirt */
		Shirt,
		/** Pants */
		Pants,
		/** Shoes */
		Shoes,
		/** Socks */
		Socks,
		/** Jacket */
		Jacket,
		/** Gloves */
		Gloves,
		/** Undershirt */
		Undershirt,
		/** Underpants */
		Underpants,
		/** Skirt */
		Skirt,
		/** Alpha mask to hide parts of the avatar */
		Alpha,
		/** Tattoo */
		Tattoo,
		/** Physics */
		Physics;

		public static WearableType setValue(int value)
		{
			if (value >= 0 && value < values().length - 1)
			    return values()[value + 1];
			return Invalid;
		}

		public static byte getValue(WearableType value)
		{
			return (byte)(value.ordinal() - 1);
		}

		public byte getValue()
		{
			return (byte)(ordinal() - 1);
		}
	}

	// A string containing the name of the asset
	public String Name = Helpers.EmptyString;
	// A string containing a short description of the asset
	public String Description = Helpers.EmptyString;
	// The Assets WearableType
	public WearableType wearableType = WearableType.Shape;
	// The For-Sale status of the object
	public SaleType ForSale;
	// An Integer representing the purchase price of the asset
	public int SalePrice;
	// The {@link UUID} of the assets creator
	public UUID Creator;
	// The {@link UUID} of the assets current owner
	public UUID Owner;
	// The {@link UUID} of the assets prior owner
	public UUID LastOwner;
	// The {@link UUID} of the Group this asset is set to
	public UUID Group;
	// True if the asset is owned by a {@link Group}
	public boolean GroupOwned;
	// The Permissions mask of the asset
	public Permissions Permissions;
	// A Dictionary containing Key/Value pairs of the objects parameters
	public HashMap<Integer, Float> Params = new HashMap<Integer, Float>();
	// A Dictionary containing Key/Value pairs where the Key is the textures
	// Index and the Value is the Textures {@link UUID}
	public HashMap<AvatarTextureIndex, UUID> Textures = new HashMap<AvatarTextureIndex, UUID>();

	// Initializes a new instance of an AssetWearable object
	public AssetWearable()
	{
	}

	/**
	 * Initializes a new instance of an AssetWearable object with parameters
	 * 
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetWearable(UUID assetID, byte[] assetData)
	{
		super(assetID, assetData);
	}

	/**
	 * Initializes a new instance of an AssetWearable object with parameters
	 * 
	 * @param source
	 *            A string containing the asset parameters
	 */
	public AssetWearable(String source)
	{
		AssetData = Helpers.StringToBytes(source);
	}

	/**
	 * Decode an assets byte encoded data to a string
	 * 
	 * @return true if the asset data was decoded successfully
	 */
	@Override
	public boolean Decode()
	{
		int version = -1;
		Permissions = new Permissions();

		try
		{
			String data = Helpers.BytesToString(AssetData);

			data = data.replace("\r", Helpers.EmptyString);
			String[] lines = data.split("\n");
			for (int stri = 0; stri < lines.length; stri++)
			{
				if (stri == 0)
				{
					String versionstring = lines[stri];
					version = Integer.parseInt(versionstring.split(" ")[2]);
					if (version != 22 && version != 18 && version != 16 && version != 15)
						return false;
				}
				else if (stri == 1)
				{
					Name = lines[stri];
				}
				else if (stri == 2)
				{
					Description = lines[stri];
				}
				else
				{
					String line = lines[stri].trim();
					String[] fields = line.split("\t");

					if (fields.length == 1)
					{
						fields = line.split(" ");
						if (fields[0] == "parameters")
						{
							int count = Integer.parseInt(fields[1]) + stri;
							for (; stri < count;)
							{
								stri++;
								line = lines[stri].trim();
								fields = line.split(" ");

								int id = 0;

								// Special handling for -0 edge case
								if (fields[0] != "-0")
									id = Integer.parseInt(fields[0]);

								if (fields[1] == ",")
									fields[1] = "0";
								else
									fields[1] = fields[1].replace(',', '.');

								float weight = Float.parseFloat(fields[1]);

								Params.put(id, weight);
							}
						}
						else if (fields[0] == "textures")
						{
							int count = Integer.parseInt(fields[1]) + stri;
							for (; stri < count;)
							{
								stri++;
								line = lines[stri].trim();
								fields = line.split(" ");

								AvatarTextureIndex id = AvatarTextureIndex.setValue(Helpers.TryParseInt(fields[0]));
								UUID texture = new UUID(fields[1]);

								Textures.put(id, texture);
							}
						}
						else if (fields[0] == "type")
						{
							wearableType = WearableType.setValue(Helpers.TryParseInt(fields[1]));
						}

					}
					else if (fields.length == 2)
					{
						if (fields[0].equals("creator_mask"))
						{
							// Deprecated, apply this as the base mask
							Permissions.BaseMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("base_mask"))
						{
							Permissions.BaseMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("owner_mask"))
						{
							Permissions.OwnerMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("group_mask"))
						{
							Permissions.GroupMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("everyone_mask"))
						{
							Permissions.EveryoneMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("next_owner_mask"))
						{
							Permissions.NextOwnerMask = (int) Helpers.TryParseHex(fields[1]);
						}
						else if (fields[0].equals("creator_id"))
						{
							Creator = new UUID(fields[1]);
						}
						else if (fields[0].equals("owner_id"))
						{
							Owner = new UUID(fields[1]);
						}
						else if (fields[0].equals("last_owner_id"))
						{
							LastOwner = new UUID(fields[1]);
						}
						else if (fields[0].equals("group_id"))
						{
							Group = new UUID(fields[1]);
						}
						else if (fields[0].equals("group_owned"))
						{
							GroupOwned = (Integer.parseInt(fields[1]) != 0);
						}
						else if (fields[0].equals("sale_type"))
						{
							ForSale = SaleType.setValue(fields[1]);
						}
						else if (fields[0].equals("sale_price"))
						{
							SalePrice = Integer.parseInt(fields[1]);
							break;
						}
						else if (fields[0].equals("sale_info"))
						{
							// Container for sale_type and sale_price, ignore
						}
						else if (fields[0].equals("perm_mask"))
						{
							// Deprecated, apply this as the next owner mask
							Permissions.NextOwnerMask = (int) Helpers.TryParseHex(fields[1]);
							break;
						}
						else
							return false;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Logger.Log("Failed decoding wearable asset " + AssetID + ": " + ex.getMessage(), LogLevel.Warning);
			return false;
		}

		return true;
	}

	// Encode the assets string represantion into a format consumable by the
	// asset server
	@Override
	public void Encode()
	{
		final String NL = "\n";

		StringBuilder data = new StringBuilder("LLWearable version 22\n");
		data.append(Name);
		data.append(NL);
		data.append(NL);
		data.append("\tpermissions 0\n\t{\n");
		data.append("\t\tbase_mask\t");
		data.append(Helpers.UInt32ToHexString(Permissions.BaseMask));
		data.append(NL);
		data.append("\t\towner_mask\t");
		data.append(Helpers.UInt32ToHexString(Permissions.OwnerMask));
		data.append(NL);
		data.append("\t\tgroup_mask\t");
		data.append(Helpers.UInt32ToHexString(Permissions.GroupMask));
		data.append(NL);
		data.append("\t\teveryone_mask\t");
		data.append(Helpers.UInt32ToHexString(Permissions.EveryoneMask));
		data.append(NL);
		data.append("\t\tnext_owner_mask\t");
		data.append(Helpers.UInt32ToHexString(Permissions.NextOwnerMask));
		data.append(NL);
		data.append("\t\tcreator_id\t");
		data.append(Creator.toString());
		data.append(NL);
		data.append("\t\towner_id\t");
		data.append(Owner.toString());
		data.append(NL);
		data.append("\t\tlast_owner_id\t");
		data.append(LastOwner.toString());
		data.append(NL);
		data.append("\t\tgroup_id\t");
		data.append(Group.toString());
		data.append(NL);
		if (GroupOwned)
			data.append("\t\tgroup_owned\t1\n");
		data.append("\t}\n");
		data.append("\tsale_info\t0\n");
		data.append("\t{\n");
		data.append("\t\tsale_type\t");
		data.append(ForSale.toString());
		data.append(NL);
		data.append("\t\tsale_price\t");
		data.append(SalePrice);
		data.append(NL);
		data.append("\t}\n");
		data.append("type ");
		data.append(WearableType.getValue(wearableType));
		data.append(NL);

		data.append("parameters ");
		data.append(Params.size());
		data.append(NL);
		for (Entry<Integer, Float> param : Params.entrySet())
		{
			data.append(param.getKey());
			data.append(" ");
			data.append(Helpers.FloatToTerseString(param.getValue()));
			data.append(NL);
		}

		data.append("textures ");
		data.append(Textures.size());
		data.append(NL);
		for (Entry<AvatarTextureIndex, UUID> texture : Textures.entrySet())
		{
			data.append(texture.getKey().getValue());
			data.append(" ");
			data.append(texture.getValue().toString());
			data.append(NL);
		}

		AssetData = Helpers.StringToBytes(data.toString());
	}
}
