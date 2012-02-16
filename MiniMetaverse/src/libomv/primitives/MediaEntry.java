//
// * Copyright (c) 2007-2010, openmetaverse.org
// * All rights reserved.
// *
// * - Redistribution and use in source and binary forms, with or without
// *   modification, are permitted provided that the following conditions are met:
// *
// * - Redistributions of source code must retain the above copyright notice, this
// *   list of conditions and the following disclaimer.
// * - Neither the name of the openmetaverse.org nor the names
// *   of its contributors may be used to endorse or promote products derived from
// *   this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
//
package libomv.primitives;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;

public class MediaEntry
{
	// #region enums
	// Permissions for control of object media
	// [Flags]
	public static class MediaPermission
	{
		public static final byte None = 0;
		public static final byte Owner = 1;
		public static final byte Group = 2;
		public static final byte Anyone = 4;
		public static final byte All = Owner | Group | Anyone;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static final byte _mask = All;
	}

	// Style of cotrols that shold be displayed to the user
	public enum MediaControls
	{
		Standard, Mini;

		public byte getValue()
		{
			return (byte) ordinal();
		}

		public static MediaControls setValue(int value)
		{
			if (value > 0 && value < values().length)
				return values()[value];
			return null;
		}

	}

	// #endregion enums

	// Is display of the alternative image enabled
	public boolean EnableAlterntiveImage;

	// Should media auto loop
	public boolean AutoLoop;

	// Shoule media be auto played
	public boolean AutoPlay;

	// Auto scale media to prim face
	public boolean AutoScale;

	// Should viewer automatically zoom in on the face when clicked
	public boolean AutoZoom;

	// Should viewer interpret first click as interaction with the media
	// or when false should the first click be treated as zoom in command
	public boolean InteractOnFirstClick;

	// Style of controls viewer should display when viewer media on this face
	public MediaControls Controls;

	// Starting URL for the media
	public String HomeURL;

	// Currently navigated URL
	public String CurrentURL;

	// Media height in pixes
	public int Height;

	// Media width in pixels
	public int Width;

	// Who can controls the media, flags MediaPermission
	public byte ControlPermissions;

	// Who can interact with the media, flags MediaPermission
	public byte InteractPermissions;

	// Is URL whitelist enabled
	public boolean EnableWhiteList;

	// Array of URLs that are whitelisted
	public String[] WhiteList;

	public MediaEntry()
	{

	}

	public MediaEntry(OSD osd)
	{
		fromOSD(osd);
	}

	/**
	 * Serialize to OSD
	 * 
	 * @return OSDMap with the serialized data
	 */
	public OSDMap Serialize()
	{
		OSDMap map = new OSDMap();

		map.put("alt_image_enable", OSD.FromBoolean(EnableAlterntiveImage));
		map.put("auto_loop", OSD.FromBoolean(AutoLoop));
		map.put("auto_play", OSD.FromBoolean(AutoPlay));
		map.put("auto_scale", OSD.FromBoolean(AutoScale));
		map.put("auto_zoom", OSD.FromBoolean(AutoZoom));
		map.put("controls", OSD.FromInteger(Controls.getValue()));
		map.put("current_url", OSD.FromString(CurrentURL));
		map.put("first_click_interact", OSD.FromBoolean(InteractOnFirstClick));
		map.put("height_pixels", OSD.FromInteger(Height));
		map.put("home_url", OSD.FromString(HomeURL));
		map.put("perms_control", OSD.FromInteger(ControlPermissions));
		map.put("perms_interact", OSD.FromInteger(InteractPermissions));

		OSDArray wl = new OSDArray();
		if (WhiteList != null && WhiteList.length > 0)
		{
			for (int i = 0; i < WhiteList.length; i++)
				wl.add(OSD.FromString(WhiteList[i]));
		}

		map.put("whitelist", wl);
		map.put("whitelist_enable", OSD.FromBoolean(EnableWhiteList));
		map.put("width_pixels", OSD.FromInteger(Width));

		return map;
	}

	/**
	 * Deserialize from OSD data
	 * 
	 * @param osd
	 *            Serialized OSD data
	 * @return Deserialized object
	 */
	public void fromOSD(OSD osd)
	{
		if (osd instanceof OSDMap)
		{
			OSDMap map = (OSDMap) osd;

			EnableAlterntiveImage = map.get("alt_image_enable").AsBoolean();
			AutoLoop = map.get("auto_loop").AsBoolean();
			AutoPlay = map.get("auto_play").AsBoolean();
			AutoScale = map.get("auto_scale").AsBoolean();
			AutoZoom = map.get("auto_zoom").AsBoolean();
			Controls = MediaControls.setValue(map.get("controls").AsInteger());
			CurrentURL = map.get("current_url").AsString();
			InteractOnFirstClick = map.get("first_click_interact").AsBoolean();
			Height = map.get("height_pixels").AsInteger();
			HomeURL = map.get("home_url").AsString();
			ControlPermissions = MediaPermission.setValue(map.get("perms_control").AsInteger());
			InteractPermissions = MediaPermission.setValue(map.get("perms_interact").AsInteger());

			if (map.get("whitelist").getType() == OSDType.Array)
			{
				OSDArray wl = (OSDArray) map.get("whitelist");
				if (wl.size() > 0)
				{
					WhiteList = new String[wl.size()];
					for (int i = 0; i < wl.size(); i++)
					{
						WhiteList[i] = wl.get(i).AsString();
					}
				}
			}
			EnableWhiteList = map.get("whitelist_enable").AsBoolean();
			Width = map.get("width_pixels").AsInteger();
		}
	}
}
