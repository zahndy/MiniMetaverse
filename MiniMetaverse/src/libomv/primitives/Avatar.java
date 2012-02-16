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
package libomv.primitives;

import java.util.ArrayList;

import libomv.AgentManager;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.NameValue;
import libomv.types.UUID;
import libomv.utils.Helpers;

/* Basic class to hold other Avatar's data. */
public class Avatar extends Primitive
{
	// #region Enums

	// Avatar profile flags
	// [Flags]
	public static class ProfileFlags
	{
		public static final byte None = 0;
		public static final byte AllowPublish = 1;
		public static final byte MaturePublish = 2;
		public static final byte Identified = 4;
		public static final byte Transacted = 8;
		public static final byte Online = 16;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static byte getValue(byte value)
		{
			return (byte) (value & _mask);
		}

		private static final byte _mask = 0x1F;
	}

	// #endregion Enums

	// #region Subclasses

	// Positive and negative ratings
	public final class Statistics
	{
		// Positive ratings for Behavior
		public int BehaviorPositive;
		// Negative ratings for Behavior
		public int BehaviorNegative;
		// Positive ratings for Appearance
		public int AppearancePositive;
		// Negative ratings for Appearance
		public int AppearanceNegative;
		// Positive ratings for Building
		public int BuildingPositive;
		// Negative ratings for Building
		public int BuildingNegative;
		// Positive ratings given by this avatar
		public int GivenPositive;
		// Negative ratings given by this avatar
		public int GivenNegative;

		public Statistics(OSD osd)
		{
			OSDMap tex = (OSDMap) osd;

			BehaviorPositive = tex.get("behavior_positive").AsInteger();
			BuildingNegative = tex.get("behavior_negative").AsInteger();
			AppearancePositive = tex.get("appearance_positive").AsInteger();
			AppearanceNegative = tex.get("appearance_negative").AsInteger();
			BuildingPositive = tex.get("buildings_positive").AsInteger();
			BuildingNegative = tex.get("buildings_negative").AsInteger();
			GivenPositive = tex.get("given_positive").AsInteger();
			GivenNegative = tex.get("given_negative").AsInteger();
		}

		public OSD Serialize()
		{
			OSDMap tex = new OSDMap(8);
			tex.put("behavior_positive", OSD.FromInteger(BehaviorPositive));
			tex.put("behavior_negative", OSD.FromInteger(BehaviorNegative));
			tex.put("appearance_positive", OSD.FromInteger(AppearancePositive));
			tex.put("appearance_negative", OSD.FromInteger(AppearanceNegative));
			tex.put("buildings_positive", OSD.FromInteger(BuildingPositive));
			tex.put("buildings_negative", OSD.FromInteger(BuildingNegative));
			tex.put("given_positive", OSD.FromInteger(GivenPositive));
			tex.put("given_negative", OSD.FromInteger(GivenNegative));
			return tex;
		}

		public Statistics fromOSD(OSD osd)
		{
			return new Statistics(osd);
		}
	}

	// Avatar properties including about text, profile URL, image IDs and
	// publishing settings
	public final class AvatarProperties
	{
		// First Life about text
		public String FirstLifeText;
		// First Life image ID
		public UUID FirstLifeImage;
		//
		public UUID Partner;
		//
		public String AboutText;
		//
		public String BornOn;
		//
		public String CharterMember;
		// Profile image ID
		public UUID ProfileImage;
		// Flags of the profile
		public byte Flags;
		// Web URL for this profile
		public String ProfileURL;

		// #region Properties

		// Should this profile be published on the web
		public boolean getAllowPublish()
		{
			return ((Flags & ProfileFlags.AllowPublish) != 0);
		}

		public void setAllowPublish(boolean value)
		{
			if (value == true)
			{
				Flags |= ProfileFlags.AllowPublish;
			}
			else
			{
				Flags &= ~ProfileFlags.AllowPublish;
			}
		}

		// Avatar Online Status
		public boolean getOnline()
		{
			return ((Flags & ProfileFlags.Online) != 0);
		}

		public void setOnline(boolean value)
		{
			if (value == true)
			{
				Flags |= ProfileFlags.Online;
			}
			else
			{
				Flags &= ~ProfileFlags.Online;
			}
		}

		// Is this a mature profile
		public boolean getMaturePublish()
		{
			return ((Flags & ProfileFlags.MaturePublish) != 0);
		}

		public void setMaturePublish(boolean value)
		{
			if (value == true)
			{
				Flags |= ProfileFlags.MaturePublish;
			}
			else
			{
				Flags &= ~ProfileFlags.MaturePublish;
			}
		}

		//
		public boolean getIdentified()
		{
			return ((Flags & ProfileFlags.Identified) != 0);
		}

		public void setIdentified(boolean value)
		{
			if (value == true)
			{
				Flags |= ProfileFlags.Identified;
			}
			else
			{
				Flags &= ~ProfileFlags.Identified;
			}
		}

		//
		public boolean getTransacted()
		{
			return ((Flags & ProfileFlags.Transacted) != 0);
		}

		public void setTransacted(boolean value)
		{
			if (value == true)
			{
				Flags |= ProfileFlags.Transacted;
			}
			else
			{
				Flags &= ~ProfileFlags.Transacted;
			}
		}

		public AvatarProperties()
		{
		}

		public OSD Serialize()
		{
			OSDMap tex = new OSDMap(9);
			tex.put("first_life_text", OSD.FromString(FirstLifeText));
			tex.put("first_life_image", OSD.FromUUID(FirstLifeImage));
			tex.put("partner", OSD.FromUUID(Partner));
			tex.put("about_text", OSD.FromString(AboutText));
			tex.put("born_on", OSD.FromString(BornOn));
			tex.put("charter_member", OSD.FromString(CharterMember));
			tex.put("profile_image", OSD.FromUUID(ProfileImage));
			tex.put("flags", OSD.FromInteger(ProfileFlags.getValue(Flags)));
			tex.put("profile_url", OSD.FromString(ProfileURL));
			return tex;
		}

		public AvatarProperties(OSD osd)
		{
			OSDMap tex = (OSDMap) osd;

			FirstLifeText = tex.get("first_life_text").AsString();
			FirstLifeImage = tex.get("first_life_image").AsUUID();
			Partner = tex.get("partner").AsUUID();
			AboutText = tex.get("about_text").AsString();
			BornOn = tex.get("born_on").AsString();
			CharterMember = tex.get("chart_member").AsString();
			ProfileImage = tex.get("profile_image").AsUUID();
			Flags = ProfileFlags.setValue(tex.get("flags").AsInteger());
			ProfileURL = tex.get("profile_url").AsString();
		}
	}

	// Avatar interests including spoken languages, skills, and "want to"
	// choices
	public final class Interests
	{
		// Languages profile field
		public String LanguagesText;
		// FIXME: ORIGINAL LINE: public uint SkillsMask;
		public int SkillsMask;
		//
		public String SkillsText;
		// FIXME: ORIGINAL LINE: public uint WantToMask;
		public int WantToMask;
		//
		public String WantToText;

		public Interests()
		{
		}

		public OSD Serialize()
		{
			OSDMap InterestsOSD = new OSDMap(5);
			InterestsOSD.put("languages_text", OSD.FromString(LanguagesText));
			InterestsOSD.put("skills_mask", OSD.FromUInteger(SkillsMask));
			InterestsOSD.put("skills_text", OSD.FromString(SkillsText));
			InterestsOSD.put("want_to_mask", OSD.FromUInteger(WantToMask));
			InterestsOSD.put("want_to_text", OSD.FromString(WantToText));
			return InterestsOSD;
		}

		public Interests(OSD osd)
		{
			OSDMap tex = (OSDMap) osd;

			LanguagesText = tex.get("languages_text").AsString();
			SkillsMask = tex.get("skills_mask").AsUInteger();
			SkillsText = tex.get("skills_text").AsString();
			WantToMask = tex.get("want_to_mask").AsUInteger();
			WantToText = tex.get("want_to_text").AsString();
		}
	}

	// #endregion Subclasses

	// #region Public Members

	// Groups that this avatar is a member of
	public ArrayList<UUID> Groups = new ArrayList<UUID>();
	// Positive and negative ratings
	public Statistics ProfileStatistics;
	// Avatar properties including about text, profile URL, image IDs and
	// publishing settings
	public AvatarProperties ProfileProperties;
	// Avatar interests including spoken languages, skills, and "want to"
	// choices
	public Interests ProfileInterests;
	// Movement control flags for avatars. Typically not set or used by clients.
	// To move your avatar,
	// use Client.Self.Movement instead
	public int ControlFlags;

	// Contains the visual parameters describing the deformation of the avatar
	public byte[] VisualParameters = null;

	// #endregion Public Members

	protected String name;
	protected String displayName;
	protected String groupName;

	// /#region Properties

	// First name
	public final String getFirstName()
	{
		for (int i = 0; i < NameValues.length; i++)
		{
			if (NameValues[i].Name.equals("FirstName") && NameValues[i].Type == NameValue.ValueType.String)
			{
				return (String) NameValues[i].Value;
			}
		}
		return Helpers.EmptyString;
	}

	// Last name
	public final String getLastName()
	{
		for (int i = 0; i < NameValues.length; i++)
		{
			if (NameValues[i].Name.equals("LastName") && NameValues[i].Type == NameValue.ValueType.String)
			{
				return (String) NameValues[i].Value;
			}
		}
		return Helpers.EmptyString;
	}

	// Full name
	public final String getName()
	{
		if (!Helpers.isEmpty(name))
		{
			return name;
		}
		else if (NameValues != null && NameValues.length > 0)
		{
			synchronized (NameValues)
			{
				String firstName = Helpers.EmptyString;
				String lastName = Helpers.EmptyString;

				for (int i = 0; i < NameValues.length; i++)
				{
					if (NameValues[i].Name.equals("FirstName") && NameValues[i].Type == NameValue.ValueType.String)
					{
						firstName = (String) NameValues[i].Value;
					}
					else if (NameValues[i].Name.equals("LastName") && NameValues[i].Type == NameValue.ValueType.String)
					{
						lastName = (String) NameValues[i].Value;
					}
				}

				if (!firstName.isEmpty() && !lastName.isEmpty())
				{
					name = String.format("%s %s", firstName, lastName);
					return name;
				}
			}
		}
		return Helpers.EmptyString;
	}

	public void setNames(String firstName, String lastName)
	{
		if (!firstName.isEmpty() && !lastName.isEmpty())
		{
			name = String.format("%s %s", firstName, lastName);
		}
		else
		{
			name = Helpers.EmptyString;
		}

		if (NameValues != null && NameValues.length > 0)
		{
			synchronized (NameValues)
			{
				for (int i = 0; i < NameValues.length; i++)
				{
					if (NameValues[i].Name.equals("FirstName") && NameValues[i].Type == NameValue.ValueType.String)
					{
						NameValues[i].Value = firstName;
					}
					else if (NameValues[i].Name.equals("LastName") && NameValues[i].Type == NameValue.ValueType.String)
					{
						NameValues[i].Value = lastName;
					}
				}
			}
		}
	}

	public String getDisplayName()
	{
		return displayName;
	}
	
	public void setDisplayName(String name)
	{
		displayName = name;
	}

	// Active group
	public final String getGroupName()
	{
		if (!Helpers.isEmpty(groupName))
		{
			return groupName;
		}

		if (NameValues != null || NameValues.length > 0)
		{
			synchronized (NameValues)
			{
				for (int i = 0; i < NameValues.length; i++)
				{
					if (NameValues[i].Name.equals("Title") && NameValues[i].Type == NameValue.ValueType.String)
					{
						groupName = (String) NameValues[i].Value;
						return groupName;
					}
				}
			}
		}
		return Helpers.EmptyString;
	}

	@Override
	public OSD Serialize()
	{
		OSDMap Avi = (OSDMap) super.Serialize();
		OSDArray grp = new OSDArray();

		for (int i = 0; i < Groups.size(); i++)
		{
			grp.add(OSD.FromUUID(Groups.get(i)));
		}

		OSDArray vp = new OSDArray();

		for (int i = 0; i < VisualParameters.length; i++)
		{
			vp.add(OSD.FromInteger(VisualParameters[i]));
		}

		Avi.put("groups", grp);
		Avi.put("profile_statistics", ProfileStatistics.Serialize());
		Avi.put("profile_properties", ProfileProperties.Serialize());
		Avi.put("profile_interest", ProfileInterests.Serialize());
		Avi.put("control_flags", OSD.FromInteger(AgentManager.ControlFlags.getValue(ControlFlags)));
		Avi.put("visual_parameters", vp);
		Avi.put("first_name", OSD.FromString(getFirstName()));
		Avi.put("last_name", OSD.FromString(getLastName()));
		Avi.put("group_name", OSD.FromString(groupName));

		return Avi;

	}

	// #endregion Properties

	// #region Constructors

	// Default constructor
	public Avatar()
	{
	}

	public Avatar(UUID ID)
	{
		this.ID = ID;
	}

	public Avatar(OSD osd)
	{
		super(osd);

		OSDMap tex = (OSDMap) osd;

		Groups = new ArrayList<UUID>();

		for (OSD U : (OSDArray) tex.get("groups"))
		{
			Groups.add(U.AsUUID());
		}

		ProfileStatistics = new Statistics(tex.get("profile_statistics"));
		ProfileProperties = new AvatarProperties(tex.get("profile_properties"));
		ProfileInterests = new Interests(tex.get("profile_interest"));
		ControlFlags = AgentManager.ControlFlags.setValue(tex.get("control_flags").AsInteger());

		OSDArray vp = (OSDArray) tex.get("visual_parameters");
		VisualParameters = new byte[vp.size()];

		for (int i = 0; i < vp.size(); i++)
		{
			VisualParameters[i] = (byte) vp.get(i).AsInteger();
		}

		NameValues = new NameValue[3];

		NameValue First = new NameValue("FirstName");
		First.Type = NameValue.ValueType.String;
		First.Value = tex.get("first_name").AsString();

		NameValue Last = new NameValue("LastName");
		Last.Type = NameValue.ValueType.String;
		Last.Value = tex.get("last_name").AsString();

		NameValue Group = new NameValue("Title");
		Group.Type = NameValue.ValueType.String;
		Group.Value = tex.get("group_name").AsString();

		NameValues[0] = First;
		NameValues[1] = Last;
		NameValues[2] = Group;
	}
	// #endregion Constructors
}
