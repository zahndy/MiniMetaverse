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
package libomv.imaging;

import icc.ICCProfileException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jj2000.j2k.decoder.Decoder;
import jj2000.j2k.decoder.ImgDecoder;
import jj2000.j2k.fileformat.reader.FileFormatReader;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.Coord;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.util.ISRandomAccessIO;
import jj2000.j2k.util.ParameterList;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

public class J2KWrap
{
	public class J2KLayerInfo
	{
        public int Start;
        public int End;
	}

	private class PixelScale
	{
		int ls, mv, fb;
	}

	/**
     * Encode a <seealso cref="ManagedImage"/> object into a byte array
     * 
     * @param image The <seealso cref="ManagedImage"/> object to encode
     * @param lossless true to enable lossless conversion, only useful for small images ie: sculptmaps
     * @return
     */
    public static byte[] encode(ManagedImage image, boolean lossless)
    {
        if ((image.Channels & ManagedImage.ImageChannels.Color) == 0 ||
            ((image.Channels & ManagedImage.ImageChannels.Bump) != 0 && (image.Channels & ManagedImage.ImageChannels.Alpha) == 0))
            throw new IllegalArgumentException("JPEG2000 encoding is not supported for this channel combination");

        byte[] encoded = null;

        int components = 3;
        if ((image.Channels & ManagedImage.ImageChannels.Alpha) != 0) components++;
        if ((image.Channels & ManagedImage.ImageChannels.Bump) != 0) components++;

        
        
        

        return encoded;
    }

    /**
     * Encode a <seealso cref="ManagedImage"/> object into a byte array
     *
     * @param image The <seealso cref="ManagedImage"/> object to encode
     */
    public static byte[] encode(ManagedImage image)
    {
        return encode(image, false);
    }

    /**
     * Decode a byte array into a <seealso cref="ManagedImage"/> object
     *
     * @param data The raw byte data to decode
     */
	public static ManagedImage decode(byte[] data) throws IOException, ICCProfileException, IllegalArgumentException
	{
		BlkImgDataSrc dataSrc = decodeInternal(data);
		
		int ncomps = dataSrc.getNumComps();

		// Check component sizes and bit depths
		int imh = dataSrc.getCompImgHeight(0);
		int imw = dataSrc.getCompImgWidth(0);
		for (int i = dataSrc.getNumComps() - 1; i >= 0; i--)
		{
			if (dataSrc.getCompImgHeight(i) != imh || dataSrc.getCompImgWidth(i) != imw)
			{
				throw new IllegalArgumentException("All components must have the same dimensions and no subsampling");
			}
			if (dataSrc.getNomRangeBits(i) > 8)
			{
				throw new IllegalArgumentException("Depths greater than 8 bits per component is not supported");
			}
		}

		byte channels = ManagedImage.ImageChannels.Color;
				
		PixelScale[] scale = new PixelScale[ncomps];
		
		for (int i = 0; i < ncomps; i++)
		{
			scale[i].ls = 1 << (dataSrc.getNomRangeBits(i) - 1);
			scale[i].mv = (1 << dataSrc.getNomRangeBits(i)) - 1;
			scale[i].fb = dataSrc.getFixedPoint(i);
		}
		
		switch (ncomps)
		{
			case 1:
			case 3:
				break;
			case 5:
				channels |= ManagedImage.ImageChannels.Bump;
			case 2:
			case 4:
				channels |= ManagedImage.ImageChannels.Alpha;
				break;
			default:
                Logger.Log("Decoded image with unhandled number of components: " + ncomps, LogLevel.Error);
				return null;
		}		
		ManagedImage image = new ManagedImage(imw, imh, channels);

		int height; // tile height
		int width; // tile width
		int tOffx, tOffy; // Active tile offset
		int tIdx = 0; // index of the current tile
		int off, l, x, y;
		Coord nT = dataSrc.getNumTiles(null);
		DataBlkInt block = new DataBlkInt();
		block.ulx = 0;
		block.h = 1;
	
		// Start the data delivery to the cached consumers tile by tile
		for (y = 0; y < nT.y; y++)
		{
			// Loop on horizontal tiles
			for (x = 0; x < nT.x; x++, tIdx++)
			{
				dataSrc.setTile(x, y);

				// Initialize tile
				height = dataSrc.getTileCompHeight(tIdx, 0);
				width = dataSrc.getTileCompWidth(tIdx, 0);

				// The offset of the active tiles is the same for all components,
				// since we don't support different component dimensions.
				tOffx = dataSrc.getCompULX(0) - (int) Math.ceil(dataSrc.getImgULX() / (double) dataSrc.getCompSubsX(0));
				tOffy = dataSrc.getCompULY(0) - (int) Math.ceil(dataSrc.getImgULY() / (double) dataSrc.getCompSubsY(0));
				off = tOffy * imw + tOffx;

				// Deliver in lines to reduce memory usage
				for (l = 0; l < height; l++)
				{
					block.uly = l;
					block.w = width;

					switch (ncomps)
					{
						case 5:
							dataSrc.getInternCompData(block, 4);
							fillLine(block, scale[4], image.Bump, off);
						case 4:
							dataSrc.getInternCompData(block, 3);
							fillLine(block, scale[3], image.Alpha, off);
						case 3:
							dataSrc.getInternCompData(block, 2);
							fillLine(block, scale[2], image.Blue, off);
							dataSrc.getInternCompData(block, 1);
							fillLine(block, scale[1], image.Green, off);
							dataSrc.getInternCompData(block, 0);
							fillLine(block, scale[0], image.Red, off);
							break;
						case 2:
							dataSrc.getInternCompData(block, 1);
							fillLine(block, scale[1], image.Alpha, off);
						case 1:
							dataSrc.getInternCompData(block, 0);
							fillLine(block, scale[0], image.Red, off);
							System.arraycopy(image.Red, off, image.Green, off, width);
							System.arraycopy(image.Red, off, image.Blue, off, width);
							break;
					}
				}
			}
		}
		return image;
	}

	private static void fillLine(DataBlkInt blk, PixelScale scale, byte[] data, int off)
	{
		int k1 = blk.offset + blk.w - 1;
		for (int i = blk.w - 1; i >= 0; i--)
		{
			int temp = (blk.data[k1--] >> scale.fb) + scale.ls;
			temp = (temp < 0) ? 0 : ((temp > scale.mv) ? scale.mv : temp);
			data[off + i] = (byte)temp;
		}
	}


	public static int decodeLayerBoundaries(byte[] encoded, J2KLayerInfo[] layers)
	{
		return 0;
	}
	
	private static BlkImgDataSrc decodeInternal(byte[] data) throws IOException, ICCProfileException
	{
		ParameterList pl, defpl = new ParameterList();
    	String[][] param = Decoder.getAllParameters();

        for (int i = param.length - 1; i >= 0; i--)
        {
    	    if (param[i][3] != null)
    	    {
    	    	defpl.put(param[i][0], param[i][3]);
            }
        }
        // Create parameter list using defaults
        pl = new ParameterList(defpl);
        ImgDecoder decoder = new ImgDecoder(pl);
        
        RandomAccessIO in = new ISRandomAccessIO(new ByteArrayInputStream(data), 1 << 18, 1 << 18, data.length);

		// **** File Format ****
		// If the codestream is wrapped in the jp2 fileformat, Read the file format wrapper
		FileFormatReader ff = new FileFormatReader(in);
		ff.readFileFormat();
		if (ff.JP2FFUsed)
		{
			in.seek(ff.getFirstCodeStreamPos());
		}
		return decoder.decode(in, ff, false);
	}
}
