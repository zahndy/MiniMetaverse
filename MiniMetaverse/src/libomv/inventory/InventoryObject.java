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

import libomv.primitives.Primitive.AttachmentPoint;
import libomv.types.UUID;

/** InventoryObject Class contains details on a primitive or coalesced set of primitives */
@SuppressWarnings("serial")
public class InventoryObject extends InventoryItem
{
	/**
	 * Construct an InventoryObject object
	 * 
	 * @param itemID
	 *            A {@link OpenMetaverse.UUID} which becomes the
	 *            {@link OpenMetaverse.InventoryItem} objects AssetUUID
	 */
	public InventoryObject(UUID itemID)
	{
		super(itemID);
	}

	@Override
	public InventoryType getType()
	{
		return InventoryType.Object;
	}

	/** Gets or sets the upper byte of the Flags value */
	public final int getItemFlags()
	{
		return ItemFlags & ~0xFF;
	}

	public final void setItemFlags(int value)
	{
		ItemFlags = value | (ItemFlags & 0xFF);
	}

	/**
	 * Gets or sets the object attachment point, the lower byte of the Flags
	 * value
	 */
	public final AttachmentPoint getAttachPoint()
	{
		return AttachmentPoint.setValue(ItemFlags & 0xFF);
	}

	public final void setAttachPoint(AttachmentPoint value)
	{
		ItemFlags = value.getValue() | (ItemFlags & ~0xFF);
	}
}
