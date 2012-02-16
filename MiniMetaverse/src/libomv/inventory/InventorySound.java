package libomv.inventory;

import libomv.types.UUID;

/** InventorySound Class representing a playable sound */
public class InventorySound extends InventoryItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an InventorySound object
	 * 
	 * @param itemID
	 *            A {@link OpenMetaverse.UUID} which becomes the
	 *            {@link OpenMetaverse.InventoryItem} objects AssetUUID
	 */
	public InventorySound(UUID itemID)
	{
		super(itemID);
	}

	
	@Override
	public InventoryType getType()
	{
		return InventoryType.Sound;
	}
}
