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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libomv.ObjectManager.SaleType;
import libomv.inventory.InventoryItem;
import libomv.inventory.InventoryManager;
import libomv.inventory.InventoryNode.InventoryType;
import libomv.types.Permissions;
import libomv.types.Permissions.PermissionMask;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

/**
 * Represents a string of characters encoded with specific formatting properties
 */
public class AssetNotecard extends AssetItem
{
	/* Override the base classes getAssetType */
	@Override
	public AssetType getAssetType()
	{
		return AssetType.Notecard;
	}

	/** A text string containing main text of the notecard */
	public String BodyText = null;

	/**
	 * List of <see cref="OpenMetaverse.InventoryItem"/>s embedded on the
	 * notecard
	 */
	public List<InventoryItem> EmbeddedItems = null;

	/** Construct an Asset of type Notecard */
	public AssetNotecard()
	{
	}

	/**
	 * Construct an Asset object of type Notecard
	 * 
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetNotecard(UUID assetID, byte[] assetData)
	{
		super(assetID, assetData);
		Decode();
	}

	/**
	 * Construct an Asset object of type Notecard
	 * 
	 * @param text
	 *            A text string containing the main body text of the notecard
	 */
	public AssetNotecard(String text)
	{
		BodyText = text;
		Encode();
	}

	/*
	 * Encode the raw contents of a string with the specific Linden Text
	 * properties
	 */
	@Override
	public void Encode()
	{
		String body = BodyText;

		StringBuilder output = new StringBuilder();
		output.append("Linden text version 2\n");
		output.append("{\n");
		output.append("LLEmbeddedItems version 1\n");
		output.append("{\n");

		int count = 0;

		if (EmbeddedItems != null)
		{
			count = EmbeddedItems.size();
		}

		output.append("count " + count + "\n");

		if (count > 0)
		{
			output.append("{\n");

			for (int i = 0; i < EmbeddedItems.size(); i++)
			{
				InventoryItem item = EmbeddedItems.get(i);

				output.append("ext char index " + i + "\n");

				output.append("\tinv_item\t0\n");
				output.append("\t{\n");

				output.append("\t\titem_id\t" + item.itemID + "\n");
				output.append("\t\tparent_id\t" + item.getParentID() + "\n");

				output.append("\tpermissions 0\n");
				output.append("\t{\n");
				output.append("\t\tbase_mask\t" + String.format("08x", item.Permissions.BaseMask) + "\n");
				output.append("\t\towner_mask\t" + String.format("08x", item.Permissions.OwnerMask) + "\n");
				output.append("\t\tgroup_mask\t" + String.format("08x", item.Permissions.GroupMask) + "\n");
				output.append("\t\teveryone_mask\t" + String.format("08x", item.Permissions.EveryoneMask) + "\n");
				output.append("\t\tnext_owner_mask\t" + String.format("08x", item.Permissions.NextOwnerMask) + "\n");
				output.append("\t\tcreator_id\t" + item.CreatorID + "\n");
				output.append("\t\towner_id\t" + item.getOwnerID() + "\n");
				output.append("\t\tlast_owner_id\t" + item.LastOwnerID + "\n");
				output.append("\t\tgroup_id\t" + item.GroupID + "\n");
				if (item.GroupOwned)
					output.append("\t\tgroup_owned\t1\n");
				output.append("\t}\n");

				if (Permissions.hasPermissions(item.Permissions.BaseMask, PermissionMask.Modify | PermissionMask.Copy
						| PermissionMask.Transfer)
						|| item.AssetID == UUID.Zero)
				{
					output.append("\t\tasset_id\t" + item.AssetID + "\n");
				}
				else
				{
					output.append("\t\tshadow_id\t" + InventoryManager.EncryptAssetID(item.AssetID) + "\n");
				}

				output.append("\t\ttype\t" + item.assetType.toString() + "\n");
				output.append("\t\tinv_type\t" + item.getType().toString() + "\n");
				output.append("\t\tflags\t" + String.format("08x", item.ItemFlags) + "\n");

				output.append("\tsale_info\t0\n");
				output.append("\t{\n");
				output.append("\t\tsale_type\t" + item.saleType.toString() + "\n");
				output.append("\t\tsale_price\t" + item.SalePrice + "\n");
				output.append("\t}\n");

				output.append("\t\tname\t" + item.name.replace('|', '_') + "|\n");
				output.append("\t\tdesc\t" + item.Description.replace('|', '_') + "|\n");
				output.append("\t\tcreation_date\t" + Helpers.DateTimeToUnixTime(item.CreationDate) + "\n");

				output.append("\t}\n");

				if (i != EmbeddedItems.size() - 1)
				{
					output.append("}\n{\n");
				}
			}

			output.append("}\n");
		}

		output.append("}\n");
		output.append("Text length " + String.format("%d", Helpers.StringToBytes(body).length - 1) + "\n");
		output.append(body + "}\n");

		AssetData = Helpers.StringToBytes(output.toString());
	}

	/**
	 * Decode the raw asset data including the Linden Text properties
	 * 
	 * @return true if the AssetData was successfully decoded
	 */

	private Matcher match(String string, String pattern)
	{
		return Pattern.compile(pattern).matcher(string);
	}

	@Override
	public boolean Decode()
	{
		EmbeddedItems = new ArrayList<InventoryItem>();
		BodyText = Helpers.EmptyString;

		try
		{
			String data = Helpers.BytesToString(AssetData);
			String[] lines = data.split("\n");
			int i = 0;
			Matcher m;

			// Version
			if (!(m = match(lines[i++], "Linden text version\\s+(\\d+)")).matches())
				throw new Exception("could not determine version");
			int notecardVersion = Helpers.TryParseInt(m.group(1));
			if (notecardVersion < 1 || notecardVersion > 2)
				throw new Exception("unsupported version");
			if (!(m = match(lines[i++], "\\s*{$")).matches())
				throw new Exception("wrong format");

			// Embedded items header
			if (!(m = match(lines[i++], "LLEmbeddedItems version\\s+(\\d+)")).matches())
				throw new Exception("could not determine embedded items version version");
			if (m.group(1) != "1")
				throw new Exception("unsuported embedded item version");
			if (!(m = match(lines[i++], "\\s*{$")).matches())
				throw new Exception("wrong format");

			// Item count
			if (!(m = match(lines[i++], "count\\s+(\\d+)")).matches())
				throw new Exception("wrong format");
			int count = Helpers.TryParseInt(m.group(1));

			// Decode individual items
			for (int n = 0; n < count; n++)
			{
				if (!(m = match(lines[i++], "\\s*{$")).matches())
					throw new Exception("wrong format");

				// Index
				if (!(m = match(lines[i++], "ext char index\\s+(\\d+)")).matches())
					throw new Exception("missing ext char index");
				// warning CS0219: The variable `index' is assigned but its
				// value is never used
				// int index = int.Parse(m.group(1).Value);

				// Inventory item
				if (!(m = match(lines[i++], "inv_item\\s+0")).matches())
					throw new Exception("missing inv item");

				// Item itself
				UUID uuid = UUID.Zero;
				UUID creatorID = UUID.Zero;
				UUID ownerID = UUID.Zero;
				UUID lastOwnerID = UUID.Zero;
				UUID groupID = UUID.Zero;
				Permissions permissions = Permissions.NoPermissions;
				int salePrice = 0;
				SaleType saleType = SaleType.Not;
				UUID parentID = UUID.Zero;
				UUID assetID = UUID.Zero;
				AssetType assetType = AssetType.Unknown;
				InventoryType inventoryType = InventoryType.Unknown;
				int flags = 0;
				String name = Helpers.EmptyString;
				String description = Helpers.EmptyString;
				Date creationDate = Helpers.Epoch;

				while (true)
				{
					if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?(.*)?")).matches())
						throw new Exception("wrong format");
					String key = m.group(1);
					String val = m.group(3);
					if (key == "{")
						continue;
					if (key == "}")
						break;
					else if (key == "permissions")
					{
						int baseMask = 0;
						int ownerMask = 0;
						int groupMask = 0;
						int everyoneMask = 0;
						int nextOwnerMask = 0;

						while (true)
						{
							if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?([^\\s]+)?")).matches())
								throw new Exception("wrong format");
							String pkey = m.group(1);
							String pval = m.group(3);

							if (pkey == "{")
								continue;
							if (pkey == "}")
								break;
							else if (pkey == "creator_id")
							{
								creatorID = new UUID(pval);
							}
							else if (pkey == "owner_id")
							{
								ownerID = new UUID(pval);
							}
							else if (pkey == "last_owner_id")
							{
								lastOwnerID = new UUID(pval);
							}
							else if (pkey == "group_id")
							{
								groupID = new UUID(pval);
							}
							else if (pkey == "base_mask")
							{
								baseMask = (int) Helpers.TryParseHex(pval);
							}
							else if (pkey == "owner_mask")
							{
								ownerMask = (int) Helpers.TryParseHex(pval);
							}
							else if (pkey == "group_mask")
							{
								groupMask = (int) Helpers.TryParseHex(pval);
							}
							else if (pkey == "everyone_mask")
							{
								everyoneMask = (int) Helpers.TryParseHex(pval);
							}
							else if (pkey == "next_owner_mask")
							{
								nextOwnerMask = (int) Helpers.TryParseHex(pval);
							}
						}
						permissions = new Permissions(baseMask, everyoneMask, groupMask, nextOwnerMask, ownerMask);
					}
					else if (key == "sale_info")
					{
						while (true)
						{
							if (!(m = match(lines[i++], "([^\\s]+)(\\s+)?([^\\s]+)?")).matches())
								throw new Exception("wrong format");
							String pkey = m.group(1);
							String pval = m.group(3);

							if (pkey == "{")
								continue;
							if (pkey == "}")
								break;
							else if (pkey == "sale_price")
							{
								salePrice = Helpers.TryParseInt(pval);
							}
							else if (pkey == "sale_type")
							{
								saleType = SaleType.setValue(pval);
							}
						}
					}
					else if (key == "item_id")
					{
						uuid = new UUID(val);
					}
					else if (key == "parent_id")
					{
						parentID = new UUID(val);
					}
					else if (key == "asset_id")
					{
						assetID = new UUID(val);
					}
					else if (key == "type")
					{
						assetType = AssetType.setValue(val);
					}
					else if (key == "inv_type")
					{
						inventoryType = InventoryType.setValue(val);
					}
					else if (key == "flags")
					{
						flags = (int) Helpers.TryParseHex(val);
					}
					else if (key == "name")
					{
						name = val.substring(0, val.lastIndexOf("|"));
					}
					else if (key == "desc")
					{
						description = val.substring(0, val.lastIndexOf("|"));
					}
					else if (key == "creation_date")
					{
						creationDate = Helpers.UnixTimeToDateTime(Helpers.TryParseInt(val));
					}
				}
				InventoryItem finalEmbedded = InventoryItem.create(inventoryType, uuid, parentID, ownerID);

				finalEmbedded.CreatorID = creatorID;
				finalEmbedded.LastOwnerID = lastOwnerID;
				finalEmbedded.GroupID = groupID;
				finalEmbedded.Permissions = permissions;
				finalEmbedded.SalePrice = salePrice;
				finalEmbedded.saleType = saleType;
				finalEmbedded.AssetID = assetID;
				finalEmbedded.assetType = assetType;
				finalEmbedded.ItemFlags = flags;
				finalEmbedded.name = name;
				finalEmbedded.Description = description;
				finalEmbedded.CreationDate = creationDate;

				EmbeddedItems.add(finalEmbedded);

				if (!(m = match(lines[i++], "\\s*}$")).matches())
					throw new Exception("wrong format");

			}

			// Text size
			if (!(m = match(lines[i++], "\\s*}$")).matches())
				throw new Exception("wrong format");
			if (!(m = match(lines[i++], "Text length\\s+(\\d+)")).matches())
				throw new Exception("could not determine text length");

			// Read the rest of the notecard
			while (i < lines.length)
			{
				BodyText += lines[i++] + "\n";
			}
			BodyText = BodyText.substring(0, BodyText.lastIndexOf("}"));
			return true;
		}
		catch (Exception ex)
		{
			Logger.Log("Decoding notecard asset failed: " + ex.getMessage(), LogLevel.Error);
			return false;
		}
	}
}
