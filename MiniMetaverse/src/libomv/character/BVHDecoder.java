/**
 * Copyright (c) 2009-2011, Frederick Martian
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
package libomv.character;

import java.io.InputStream;

public class BVHDecoder
{
	public static final int EOF = 0;
	public static final int COMMENT = 5;
	public static final int HIERARCHY = 6;
	public static final int ROOT = 7;
	public static final int OFFSET = 8;
	public static final int CHANNELS = 9;
	public static final int JOINT = 10;
	public static final int END = 11;
	public static final int SITE = 12;
	public static final int MOTION = 13;
	public static final int FRAMES = 14;
	public static final int FRAME = 15;
	public static final int TIME = 16;
	public static final int XPOS = 17;
	public static final int YPOS = 18;
	public static final int ZPOS = 19;
	public static final int XROT = 20;
	public static final int YROT = 21;
	public static final int ZROT = 22;
	public static final int INTEGER = 23;
	public static final int FLOATING = 24;
	public static final int ID = 25;

	public static final int DEFAULT = 0;

	static String[] token = { "<EOF>", "\" \"", "\"\\n\"", "\"\\r\"", "\"\\t\"", "<COMMENT>", "\"HIERARCHY\"",
			"\"ROOT\"", "\"OFFSET\"", "\"CHANNELS\"", "\"JOINT\"", "\"END\"", "\"SITE\"", "\"MOTION\"", "\"FRAMES\"",
			"\"FRAME\"", "\"TIME\"", "\"Xposition\"", "\"Yposition\"", "\"Zposition\"", "\"Xrotation\"",
			"\"Yrotation\"", "\"Zrotation\"", "<INTEGER>", "<FLOATING>", "<ID>", "\"{\"", "\"}\"", "\":\"", };

	public BVHDecoder(InputStream stream)
	{
		
	}
	
	public LLAnimation parse()
	{
		return null;
	}
}
