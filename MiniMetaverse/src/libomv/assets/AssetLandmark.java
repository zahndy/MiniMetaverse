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

import java.io.UnsupportedEncodingException;

import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

// Represents a Landmark with RegionID and Position vector
public class AssetLandmark extends AssetItem
{
	@Override
	public AssetType getAssetType()
	{
		return AssetType.Landmark;
	}

	// UUID of the Landmark target region
	public UUID RegionID = UUID.Zero;
	// Local position of the target
	public Vector3 Position = Vector3.Zero;

	// Construct an Asset of type Landmark
	public AssetLandmark()
	{
	}

	/**
	 * Construct an Asset object of type Landmark
	 * 
	 * @param assetID
	 *            A unique <see cref="UUID"/> specific to this asset
	 * @param assetData
	 *            A byte array containing the raw asset data
	 */
	public AssetLandmark(UUID assetID, byte[] assetData)
	{
		super(assetID, assetData);
		Decode();
	}

	/**
	 * Constuct an asset of type Landmark
	 * 
	 * @param regionID
	 *            UUID of the target region
	 * @param pos
	 *            Local position of landmark
	 */
	public AssetLandmark(UUID regionID, Vector3 pos)
	{
		RegionID = regionID;
		Position = pos;
		Encode();
	}

	/**
	 * Encode the raw contents of a string with the specific Landmark format
	 * 
	 */
	@Override
	public void Encode()
	{
		String temp = "Landmark version 2\n";
		temp += "region_id " + RegionID + "\n";
		temp += String.format("local_pos %f %f %f\n", Position.X, Position.Y, Position.Z);
		AssetData = Helpers.StringToBytes(temp);
	}

	/**
	 * Decode the raw asset data, populating the RegionID and Position
	 * 
	 * @return true if the AssetData was successfully decoded to a UUID and
	 *         Vector
	 */
	@Override
	public boolean Decode()
	{
		try
		{
			String text = Helpers.BytesToString(AssetData);
			if (text.toLowerCase().contains("landmark version 2"))
			{
				RegionID = new UUID(text.substring(text.indexOf("region_id") + 10, 36));
				String[] vecStrings = text.substring(text.indexOf("local_pos") + 10).split(" ");
				if (vecStrings.length == 3)
				{
					Position = new Vector3(Helpers.TryParseFloat(vecStrings[0]), Helpers.TryParseFloat(vecStrings[1]),
							Helpers.TryParseFloat(vecStrings[2]));
					return true;
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
		}
		return false;
	}
}
