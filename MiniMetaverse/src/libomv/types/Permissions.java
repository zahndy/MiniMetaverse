/**
 * Copyright (c) 2007-2008, openmetaverse.org
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
package libomv.types;

import java.io.Serializable;

import libomv.StructuredData.*;

public class Permissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	// [Flags]
	public static class PermissionMask
	{
		public static final int None = 0;
		public static final int Transfer = 1 << 13;
		public static final int Modify = 1 << 14;
		public static final int Copy = 1 << 15;
		// [Obsolete]
		// public static final int EnterParcel = 1 << 16,
		// [Obsolete]
		// public static final int Terraform = 1 << 17,
		// [Obsolete]
		// public static final int OwnerDebit = 1 << 18,
		public static final int Move = 1 << 19;
		public static final int Damage = 1 << 20;
		public static final int All = 0x7FFFFFFF;

		public static int setValue(int value)
		{
			return value & _mask;
		}

		public static int getValue(int value)
		{
			return value;
		}

		private static final int _mask = All;
	}

	// [Flags]
	public static class PermissionWho
	{
		public static final byte Base = 0x01;
		public static final byte Owner = 0x02;
		public static final byte Group = 0x04;
		public static final byte Everyone = 0x08;
		public static final byte NextOwner = 0x10;
		public static final byte All = 0x1F;

		public static byte setValue(int value)
		{
			return (byte) (value & _mask);
		}

		public static int getValue(int value)
		{
			return value;
		}

		private static final byte _mask = All;
	}

	public int BaseMask;
	public int EveryoneMask;
	public int GroupMask;
	public int NextOwnerMask;
	public int OwnerMask;

	public Permissions()
	{
		BaseMask = 0;
		EveryoneMask = 0;
		GroupMask = 0;
		NextOwnerMask = 0;
		OwnerMask = 0;
	}

	public Permissions(OSD osd)
	{
		fromOSD(osd);
	}

	public Permissions(int baseMask, int everyoneMask, int groupMask, int nextOwnerMask, int ownerMask)
	{
		BaseMask = baseMask;
		EveryoneMask = everyoneMask;
		GroupMask = groupMask;
		NextOwnerMask = nextOwnerMask;
		OwnerMask = ownerMask;
	}

	public Permissions(Permissions perm)
	{
		BaseMask = perm.BaseMask;
		EveryoneMask = perm.EveryoneMask;
		GroupMask = perm.GroupMask;
		NextOwnerMask = perm.NextOwnerMask;
		OwnerMask = perm.OwnerMask;
	}

	public Permissions GetNextPermissions()
	{
		int nextMask = NextOwnerMask;

		return new Permissions(BaseMask & nextMask, EveryoneMask & nextMask, GroupMask & nextMask, NextOwnerMask,
				OwnerMask & nextMask);
	}

	public OSD Serialize()
	{
		OSDMap permissions = new OSDMap(5);
		permissions.put("base_mask", OSD.FromInteger(BaseMask));
		permissions.put("everyone_mask", OSD.FromInteger(EveryoneMask));
		permissions.put("group_mask", OSD.FromInteger(GroupMask));
		permissions.put("next_owner_mask", OSD.FromInteger(NextOwnerMask));
		permissions.put("owner_mask", OSD.FromInteger(OwnerMask));
		return permissions;
	}

	public static Permissions fromOSD(OSD llsd)
	{
		Permissions permissions = new Permissions();
		OSDMap map = (OSDMap) ((llsd instanceof OSDMap) ? llsd : null);

		if (map != null)
		{
			permissions.BaseMask = map.get("base_mask").AsUInteger();
			permissions.EveryoneMask = map.get("everyone_mask").AsUInteger();
			permissions.GroupMask = map.get("group_mask").AsUInteger();
			permissions.NextOwnerMask = map.get("next_owner_mask").AsUInteger();
			permissions.OwnerMask = map.get("owner_mask").AsUInteger();
		}

		return permissions;
	}

	@Override
	public String toString()
	{
		return String.format("Base: %s, Everyone: %s, Group: %s, NextOwner: %s, Owner: %s", BaseMask, EveryoneMask,
				GroupMask, NextOwnerMask, OwnerMask);
	}

	@Override
	public int hashCode()
	{
		return BaseMask ^ EveryoneMask ^ GroupMask ^ NextOwnerMask ^ OwnerMask;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Permissions) ? equals(this, (Permissions) obj) : false;
	}

	public boolean equals(Permissions other)
	{
		return equals(this, other);
	}

	public static boolean equals(Permissions lhs, Permissions rhs)
	{
		return (lhs.BaseMask == rhs.BaseMask) && (lhs.EveryoneMask == rhs.EveryoneMask)
				&& (lhs.GroupMask == rhs.GroupMask) && (lhs.NextOwnerMask == rhs.NextOwnerMask)
				&& (lhs.OwnerMask == rhs.OwnerMask);
	}

	public static boolean hasPermissions(int perms, int checkPerms)
	{
		return (perms & checkPerms) == checkPerms;
	}

	public static final Permissions NoPermissions = new Permissions();
	public static final Permissions FullPermissions = new Permissions(PermissionMask.All, PermissionMask.All,
			PermissionMask.All, PermissionMask.All, PermissionMask.All);
}
