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
package libomv.mapgenerator;

public interface FieldType
{
	public static final int U8 = 0;

	public static final int U16 = 1;

	public static final int U32 = 2;

	public static final int U64 = 3;

	public static final int S8 = 4;

	public static final int S16 = 5;

	public static final int S32 = 6;

	public static final int S64 = 7;

	public static final int F32 = 8;

	public static final int F64 = 9;

	public static final int UUID = 10;

	public static final int BOOL = 11;

	public static final int Vector3 = 12;

	public static final int Vector3d = 13;

	public static final int Vector4 = 14;

	public static final int Quaternion = 15;

	public static final int IPADDR = 16;

	public static final int IPPORT = 17;

	public static final int Variable = 18;

	public static final int Fixed = 19;

	public static final int Single = 20;

	public static final int Multiple = 21;

	public static String[] TypeNames = { "U8", "U16", "U32", "U64", "S8", "S16", "S32", "S64", "F32", "F64", "LLUUID",
			"BOOL", "LLVector3", "LLVector3d", "LLVector4", "LLQuaternion", "IPADDR", "IPPORT", "Variable", "Fixed",
			"Single", "Multiple" };
}
