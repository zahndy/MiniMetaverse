/**
 * Copyright (c) 2006-2008, openmetaverse.org
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
package libomv.inventory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;

/**
 * Base class for {@link libomv.inventory.InventoryItem}s and {@link libomv.inventory.InventoryFolder}s
 * with tree structure support
 */
public abstract class InventoryNode implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Inventory Node Types, eg Script, Notecard, Folder, etc */
	public enum InventoryType
	{
		/** Unknown */
		Unknown(-1),
		/** Texture */
		Texture(0),
		/** Sound */
		Sound(1),
		/** Calling Card */
		CallingCard(2),
		/** Landmark */
		Landmark(3),
		// [Obsolete("See LSL")]
		Script(4),
		// [Obsolete("See Wearable")]
		Clothing(5),
		/** Object */
		Object(6),
		/** Notecard */
		Notecard(7),
		/** */
		Category(8),
		/** Folder */
		Folder(8),
		/** */
		RootCategory(9),
		/** an LSL Script */
		LSL(10),
		// [Obsolete("See LSL")] LSLBytecode = 11,
		// [Obsolete("See Texture")] TextureTGA = 12,
		// [Obsolete] Bodypart = 13,
		// [Obsolete] Trash = 14,
		/** */
		Snapshot(15),
		// [Obsolete] LostAndFound = 16,
		/** */
		Attachment(17),
		/** */
		Wearable(18),
		/** */
		Animation(19),
		/**	*/
		Gesture(20);

		private static final String[] _InventoryTypeNames = new String[] { "texture", "sound", "callcard", "landmark",
				"script", "clothing", "object", "notecard", "category", "root", "script", "", "", "", "", "snapshot",
				"", "attach", "wearable", "animation", "gesture" };

		/**
		 * Translate a string name of an AssetType into the proper Type
		 * 
		 * @param type
		 *            A string containing the AssetType name
		 * @return The AssetType which matches the string name, or
		 *         AssetType.Unknown if no match was found
		 */
		public static InventoryType setValue(String value)
		{
			for (int i = 0; i < _InventoryTypeNames.length; i++)
			{
				if (value.compareToIgnoreCase(_InventoryTypeNames[i]) == 0)
				{
					return values()[i + 1];
				}
			}
			return Unknown;
		}

		public static InventoryType setValue(int value)
		{
			for (InventoryType e : values())
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

		@Override
		public String toString()
		{
			int i = ordinal() - 1;
			if (i >= 0 && ordinal() < _InventoryTypeNames.length)
				return _InventoryTypeNames[i];
			return "unknown";
		}

		private final byte _value;

		private InventoryType(int value)
		{
			this._value = (byte) value;
		}
	}

	// {@link libomv.types.UUID} of item/folder
	public UUID itemID;
	// Name of item/folder */
	public String name;
    // Item/Folder Owners {@link libomv.types.UUID}
    protected UUID ownerID;
    // Item/Folder Parent {@link libomv.types.UUID}
    protected UUID parentID;
	// parent of item/folder in tree hierarchy
	protected InventoryFolder parent;
	
	public UUID getParentID()
	{
		return parent.itemID;
	}
	
	public abstract InventoryType getType();
	
	public UUID getOwnerID()
	{
		return ownerID;
	}

	protected InventoryNode()
	{
	}

	/**
	 * Constructor, takes an itemID as a parameter
	 * 
	 * @param itemID
	 *            The {@link OpenMetaverse.UUID} of the item
	 */
	protected InventoryNode(UUID itemID)
	{
		this.itemID = itemID;
	}
	
	public static InventoryNode create(InventoryType type, UUID id, UUID parentID, UUID ownerID)
	{
		switch (type)
		{
			case Folder:
				return new InventoryFolder(id, parentID, ownerID);
			default:
				return InventoryItem.create(type, id, parentID, ownerID);
		}
	}

	protected OSDMap toOSD()
	{
		OSDMap map = new OSDMap();
		map.put("uuid", OSD.FromUUID(itemID));
		map.put("type", OSD.FromInteger(getType().getValue()));
		map.put("name", OSD.FromString(name));
		map.put("owner", OSD.FromUUID(ownerID));
		if (parent != null)
		{
			map.put("parent", OSD.FromUUID(parent.itemID));			
		}
		return map;
	}

	public static InventoryNode fromOSD(OSD osd)
	{
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;
			UUID id = map.get("uuid").AsUUID();
			UUID parentID = null;
			if (map.containsKey("parent"))
				parentID = map.get("parent").AsUUID();
			InventoryType type = InventoryType.setValue(map.get("type").AsInteger());
			InventoryNode node = InventoryNode.create(type, id, parentID, map.get("owner").AsUUID());
			node.name = map.get("name").AsString();
			
			switch (type)
			{
				case Folder:
					return InventoryFolder.fromOSD(node, osd);
				default:
					return InventoryItem.fromOSD(node, osd);
			}
		}
		return null;
	}

	protected void readObject(ObjectInputStream info) throws IOException, ClassNotFoundException
	{
		if (serialVersionUID != info.readLong())
			throw new InvalidObjectException("InventoryNode serial version mismatch");
		itemID = (UUID) info.readObject();
		name = info.readUTF();
		ownerID = (UUID)info.readObject();
		parent = (InventoryFolder)info.readObject();
	}

	protected void writeObject(ObjectOutputStream info) throws IOException
	{
		info.writeLong(serialVersionUID);
		info.writeObject(itemID);
		info.writeUTF(name);
		info.writeObject(ownerID);
		info.writeObject(parent);
	}

	@Override
	public String toString()
	{
		return getType().toString() + ": " + name; 
	}
	
	/**
	 * Generates a number corresponding to the value of the object to support
	 * the use of a hash table, suitable for use in hashing algorithms and data
	 * structures such as a hash table
	 * 
	 * @return A Hashcode of all the combined InventoryBase fields
	 */
	@Override
	public int hashCode()
	{
		return itemID.hashCode() ^ ownerID.hashCode();
	}

	/**
	 * Determine whether the specified {@link InventoryNode}
	 * object is equal to the current object
	 * 
	 * @param o
	 *            InventoryNode object to compare against
	 * @return true if objects are the same
	 */
	@Override
	public boolean equals(Object o)
	{
		InventoryNode inv = (InventoryNode) ((o instanceof InventoryNode) ? o : null);
		return inv != null && equals(inv);
	}

	/**
	 * Determine whether the specified {@link InventoryNode}
	 * object is equal to the current object
	 * 
	 * @param o
	 *            InventoryNode object to compare against
	 * @return true if objects are the same
	 */
	public boolean equals(InventoryNode o)
	{
		return o != null && itemID.equals(o.itemID) && ownerID.equals(o.ownerID);
	}
}
