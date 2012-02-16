/**
 * Copyright (c) 2006, Second Life Reverse Engineering Team
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

import java.util.Date;

import libomv.ObjectManager.SaleType;
import libomv.primitives.Primitive.ObjectCategory;
import libomv.types.Permissions;
import libomv.types.UUID;
import libomv.utils.Helpers;

// Extended properties to describe an object
public class ObjectProperties
{
	public UUID ObjectID;
	public UUID CreatorID;
	public UUID OwnerID;
	public UUID GroupID;
	public Date CreationDate;
	public Permissions Permissions;
	public int OwnershipCost;
	public SaleType SaleType;
	public int SalePrice;
	public byte AggregatePerms;
	public byte AggregatePermTextures;
	public byte AggregatePermTexturesOwner;
	public ObjectCategory Category;
	public short InventorySerial;
	public UUID ItemID;
	public UUID FolderID;
	public UUID FromTaskID;
	public UUID LastOwnerID;
	public String Name;
	public String Description;
	public String TouchName;
	public String SitName;
	public UUID[] TextureIDs;

	// Default constructor
	public ObjectProperties()
	{
		Name = Helpers.EmptyString;
		Description = Helpers.EmptyString;
		TouchName = Helpers.EmptyString;
		SitName = Helpers.EmptyString;
	}

	public ObjectProperties(ObjectProperties p)
	{
		ObjectID = p.ObjectID;
		CreatorID = p.CreatorID;
		OwnerID = p.OwnerID;
		GroupID = p.GroupID;
		CreationDate = p.CreationDate;
		Permissions = new Permissions(p.Permissions);
		OwnershipCost = p.OwnershipCost;
		SaleType = p.SaleType;
		SalePrice = p.SalePrice;
		AggregatePerms = p.AggregatePerms;
		AggregatePermTextures = p.AggregatePermTextures;
		AggregatePermTexturesOwner = p.AggregatePermTexturesOwner;
		Category = p.Category;
		InventorySerial = p.InventorySerial;
		ItemID = p.ItemID;
		FolderID = p.FolderID;
		FromTaskID = p.FromTaskID;
		LastOwnerID = p.LastOwnerID;
		Name = p.Name;
		Description = p.Description;
		TouchName = p.TouchName;
		SitName = p.SitName;
		TextureIDs = new UUID[p.TextureIDs.length];
		for (int i = 0; i < p.TextureIDs.length; i++)
			TextureIDs[i] = p.TextureIDs[i];
	}

	/**
	 * Set the properties that are set in an ObjectPropertiesFamily packet
	 * 
	 * @param props
	 *            {@link ObjectProperties} that has been partially filled by an
	 *            ObjectPropertiesFamily packet
	 */
	public void SetFamilyProperties(ObjectProperties props)
	{
		ObjectID = props.ObjectID;
		OwnerID = props.OwnerID;
		GroupID = props.GroupID;
		Permissions = props.Permissions;
		OwnershipCost = props.OwnershipCost;
		SaleType = props.SaleType;
		SalePrice = props.SalePrice;
		Category = props.Category;
		LastOwnerID = props.LastOwnerID;
		Name = props.Name;
		Description = props.Description;
	}

	public byte[] GetTextureIDBytes()
	{
		if (TextureIDs == null || TextureIDs.length == 0)
			return Helpers.EmptyBytes;

		byte[] bytes = new byte[16 * TextureIDs.length];
		for (int i = 0; i < TextureIDs.length; i++)
			TextureIDs[i].ToBytes(bytes, 16 * i);

		return bytes;
	}
}
