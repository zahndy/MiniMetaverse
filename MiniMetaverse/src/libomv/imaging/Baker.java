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
package libomv.imaging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import libomv.AppearanceManager;
import libomv.AppearanceManager.BakeType;
import libomv.Settings;
import libomv.VisualParams.VisualAlphaParam;
import libomv.assets.AssetTexture;
import libomv.assets.AssetWearable.AvatarTextureIndex;
import libomv.imaging.ManagedImage.ImageChannels;
import libomv.types.Color4;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// A set of textures that are layered on each other and "baked" in to a single texture, for avatar appearances
public class Baker
{
    // #region Properties
    // Final baked texture
    public AssetTexture getBakedTexture() { return bakedTexture; }
    // Component layers
    public List<AppearanceManager.TextureData> getTextures()  { return textures; }
    // Width of the final baked image and scratchpad
    public int getBakeWidth()  { return bakeWidth; }
    // Height of the final baked image and scratchpad
    public int getBakeHeight()  { return bakeHeight; }
    // Bake type
    public BakeType getBakeType()  { return bakeType; }
    // Is this one of the 3 skin bakes
    private boolean IsSkin() { return bakeType == BakeType.Head || bakeType == BakeType.LowerBody || bakeType == BakeType.UpperBody; }
    // #endregion

    // #region Private fields
    // Final baked texture
    private AssetTexture bakedTexture;
    // Component layers
    private List<AppearanceManager.TextureData> textures = new ArrayList<AppearanceManager.TextureData>();
    // Width of the final baked image and scratchpad
    private int bakeWidth;
    // Height of the final baked image and scratchpad
    private int bakeHeight;
    // Bake type
    private BakeType bakeType;
    // #endregion

    // #region Constructor
    /** 
     * Default constructor
     * 
     * @param bakeType Bake type
     */
    public Baker(BakeType bakeType)
    {
        this.bakeType = bakeType;

        if (bakeType == BakeType.Eyes)
        {
            bakeWidth = 128;
            bakeHeight = 128;
        }
        else
        {
            bakeWidth = 512;
            bakeHeight = 512;
        }
    }
    // #endregion

    // #region Public methods
    /** 
     * Adds layer for baking
     * 
     * @param tdata TexturaData struct that contains texture and its params
     */
    public void AddTexture(AppearanceManager.TextureData tdata)
    {
        synchronized (textures)
        {
            textures.add(tdata);
        }
    }

    public void Bake()
    {
        bakedTexture = new AssetTexture(new ManagedImage(bakeWidth, bakeHeight, (byte)(ImageChannels.Color | ImageChannels.Alpha | ImageChannels.Bump)));

        // Base color for eye bake is white, color of layer0 for others
        if (bakeType == BakeType.Eyes)
        {
            InitBakedLayerColor(Color4.White);
        }
        else if (textures.size() > 0)
        {
            InitBakedLayerColor(textures.get(0).Color);
        }

        // Do we have skin texture?
        boolean SkinTexture = textures.size() > 0 && textures.get(0).Texture != null;

        if (bakeType == BakeType.Head)
        {
            DrawLayer(LoadResourceLayer("head_color.tga"), false);
            AddAlpha(bakedTexture.Image, LoadResourceLayer("head_alpha.tga"));
            MultiplyLayerFromAlpha(bakedTexture.Image, LoadResourceLayer("head_skingrain.tga"));
        }

        if (!SkinTexture && bakeType == BakeType.UpperBody)
        {
            DrawLayer(LoadResourceLayer("upperbody_color.tga"), false);
        }

        if (!SkinTexture && bakeType == BakeType.LowerBody)
        {
            DrawLayer(LoadResourceLayer("lowerbody_color.tga"), false);
        }

        ManagedImage alphaWearableTexture = null;

        // Layer each texture on top of one other, applying alpha masks as we go
        for (int i = 0; i < textures.size(); i++)
        {
            // Skip if we have no texture on this layer
            if (textures.get(i).Texture == null) continue;

            // Is this Alpha wearable and does it have an alpha channel?
            if (textures.get(i).TextureIndex.getValue() >= AvatarTextureIndex.LowerAlpha.getValue() &&
                textures.get(i).TextureIndex.getValue() <= AvatarTextureIndex.HairAlpha.getValue())
            {
                if (textures.get(i).Texture.Image.Alpha != null)
                {
                    alphaWearableTexture = textures.get(i).Texture.Image.clone();
                }
                continue;
            }

            // Don't draw skin on head bake first
            // For head bake skin texture is drawn last, go figure
            if (bakeType == BakeType.Head && i == 0) continue;

            ManagedImage texture = textures.get(i).Texture.Image.clone();
            //File.WriteAllBytes(bakeType + "-texture-layer-" + i + ".tga", texture.ExportTGA());

            // Resize texture to the size of baked layer
            // FIXME: if texture is smaller than the layer, don't stretch it, tile it
            if (texture.Width != bakeWidth || texture.Height != bakeHeight)
            {
                try { texture.ResizeNearestNeighbor(bakeWidth, bakeHeight); }
                catch (Exception ex) { continue; }
            }

            // Special case for hair layer for the head bake
            // If we don't have skin texture, we discard hair alpha
            // and apply hair pattern over the texture
            if (!SkinTexture && bakeType == BakeType.Head && i == 1)
            {
                if (texture.Alpha != null)
                {
                    for (int j = 0; j < texture.Alpha.length; j++) texture.Alpha[j] = (byte)255;
                }
                MultiplyLayerFromAlpha(texture, LoadResourceLayer("head_hair.tga"));
            }

            // Aply tint and alpha masks except for skin that has a texture
            // on layer 0 which always overrides other skin settings
            if (!(IsSkin() && i == 0))
            {
                ApplyTint(texture, textures.get(i).Color);

                // For hair bake, we skip all alpha masks
                // and use one from the texture, for both
                // alpha and morph layers
                if (bakeType == BakeType.Hair)
                {
                    if (texture.Alpha != null)
                    {
                        bakedTexture.Image.Bump = texture.Alpha;
                    }
                    else
                    {
                        for (int j = 0; j < bakedTexture.Image.Bump.length; j++) bakedTexture.Image.Bump[j] = Byte.MAX_VALUE;
                    }
                }
                // Apply parametrized alpha masks
                else if (textures.get(i).AlphaMasks != null && textures.get(i).AlphaMasks.size() > 0)
                {
                    // Combined mask for the layer, fully transparent to begin with
                    ManagedImage combinedMask = new ManagedImage(bakeWidth, bakeHeight, ManagedImage.ImageChannels.Alpha);

                    int addedMasks = 0;

                    // First add mask in normal blend mode
                    for (Entry<VisualAlphaParam, Float> kvp : textures.get(i).AlphaMasks.entrySet())
                    {
                        if (!MaskBelongsToBake(kvp.getKey().TGAFile)) continue;

                        if (kvp.getKey().MultiplyBlend == false && (kvp.getValue() > 0f || !kvp.getKey().SkipIfZero))
                        {
                            ApplyAlpha(combinedMask, kvp.getKey(), kvp.getValue());
                            //File.WriteAllBytes(bakeType + "-layer-" + i + "-mask-" + addedMasks + ".tga", combinedMask.ExportTGA());
                            addedMasks++;
                        }
                    }

                    // If there were no mask in normal blend mode make aplha fully opaque
                    if (addedMasks == 0) for (int l = 0; l < combinedMask.Alpha.length; l++) combinedMask.Alpha[l] = (byte)255;

                    // Add masks in multiply blend mode
                    for (Entry<VisualAlphaParam, Float> kvp : textures.get(i).AlphaMasks.entrySet())
                    {
                        if (!MaskBelongsToBake(kvp.getKey().TGAFile)) continue;

                        if (kvp.getKey().MultiplyBlend == true && (kvp.getValue() > 0f || !kvp.getKey().SkipIfZero))
                        {
                            ApplyAlpha(combinedMask, kvp.getKey(), kvp.getValue());
                            //File.WriteAllBytes(bakeType + "-layer-" + i + "-mask-" + addedMasks + ".tga", combinedMask.ExportTGA());
                            addedMasks++;
                        }
                    }

                    if (addedMasks > 0)
                    {
                        // Apply combined alpha mask to the cloned texture
                        AddAlpha(texture, combinedMask);

                        // Is this layer used for morph mask? If it is, use its
                        // alpha as the morth for the whole bake
                        if (textures.get(i).TextureIndex == AppearanceManager.MorphLayerForBakeType(bakeType))
                        {
                            bakedTexture.Image.Bump = combinedMask.Alpha;
                        }
                    }
                    //File.WriteAllBytes(bakeType + "-masked-texture-" + i + ".tga", texture.ExportTGA());
                }
            }

            boolean useAlpha = i == 0 && (bakeType == BakeType.Skirt || bakeType == BakeType.Hair);
            DrawLayer(texture, useAlpha);
            //File.WriteAllBytes(bakeType + "-layer-" + i + ".tga", texture.ExportTGA());
        }

        // For head, we add skin last
        if (SkinTexture && bakeType == BakeType.Head)
        {
            ManagedImage texture = textures.get(0).Texture.Image.clone();
            if (texture.Width != bakeWidth || texture.Height != bakeHeight)
            {
                try { texture.ResizeNearestNeighbor(bakeWidth, bakeHeight); }
                catch (Exception ex) { }
            }
            DrawLayer(texture, false);
        }

        // Apply any alpha wearable textures to make parts of the avatar disappear
        if (alphaWearableTexture != null)
        {
            AddAlpha(bakedTexture.Image, alphaWearableTexture);
        }

        // We are done, encode asset for finalized bake
        bakedTexture.Encode();
        //File.WriteAllBytes(bakeType + ".tga", bakedTexture.Image.ExportTGA());
    }

    public static ManagedImage LoadResourceLayer(String fileName)
    {
        try
        {
            ManagedImage image = TGALoader.getImage(new File(Settings.RESOURCE_DIR + "/" + fileName));
            if (image == null)
            {
                Logger.Log(String.format("Failed loading resource file: %s", fileName), LogLevel.Error);
                return null;
            }
            return image;
        }
        catch (Exception ex)
        {
            Logger.Log(String.format("Failed loading resource file: %s (%s)", fileName, ex.getMessage()), LogLevel.Error, ex);
            return null;
        }
    }

    // 
    /// Converts avatar texture index (face) to Bake type
    /// 
    /// <param name="index">Face number (AvatarTextureIndex)</param>
    /// <returns>BakeType, layer to which this texture belongs to</returns>
    public static BakeType BakeTypeFor(AvatarTextureIndex index)
    {
        switch (index)
        {
            case HeadBodypaint:
                return BakeType.Head;

            case UpperBodypaint:
            case UpperGloves:
            case UpperUndershirt:
            case UpperShirt:
            case UpperJacket:
                return BakeType.UpperBody;

            case LowerBodypaint:
            case LowerUnderpants:
            case LowerSocks:
            case LowerShoes:
            case LowerPants:
            case LowerJacket:
                return BakeType.LowerBody;

            case EyesIris:
                return BakeType.Eyes;

            case Skirt:
                return BakeType.Skirt;

            case Hair:
                return BakeType.Hair;

            default:
                return BakeType.Unknown;
        }
    }
    // #endregion

    // #region Private layer compositing methods

    private boolean MaskBelongsToBake(String mask)
    {
        if ((bakeType == BakeType.LowerBody && mask.contains("upper")) ||
            (bakeType == BakeType.LowerBody && mask.contains("shirt")) ||
            (bakeType == BakeType.UpperBody && mask.contains("lower")))
        {
            return false;
        }
        return true;
    }

    private boolean DrawLayer(ManagedImage source, boolean addSourceAlpha)
    {
        if (source == null) return false;

        boolean sourceHasColor;
        boolean sourceHasAlpha;
        boolean sourceHasBump;
        int i = 0;

        sourceHasColor = ((source.Channels & ManagedImage.ImageChannels.Color) != 0 &&
                source.Red != null && source.Green != null && source.Blue != null);
        sourceHasAlpha = ((source.Channels & ManagedImage.ImageChannels.Alpha) != 0 && source.Alpha != null);
        sourceHasBump = ((source.Channels & ManagedImage.ImageChannels.Bump) != 0 && source.Bump != null);

        addSourceAlpha = (addSourceAlpha && sourceHasAlpha);

        byte alpha = Byte.MAX_VALUE;
        byte alphaInv = (byte)(Byte.MAX_VALUE - alpha);

        byte[] bakedRed = bakedTexture.Image.Red;
        byte[] bakedGreen = bakedTexture.Image.Green;
        byte[] bakedBlue = bakedTexture.Image.Blue;
        byte[] bakedAlpha = bakedTexture.Image.Alpha;
        byte[] bakedBump = bakedTexture.Image.Bump;

        byte[] sourceRed = source.Red;
        byte[] sourceGreen = source.Green;
        byte[] sourceBlue = source.Blue;
        byte[] sourceAlpha = sourceHasAlpha ? source.Alpha : null;
        byte[] sourceBump = sourceHasBump ? source.Bump : null;

        for (int y = 0; y < bakeHeight; y++)
        {
            for (int x = 0; x < bakeWidth; x++)
            {
                if (sourceHasAlpha)
                {
                    alpha = sourceAlpha[i];
                    alphaInv = (byte)(Byte.MAX_VALUE - alpha);
                }

                if (sourceHasColor)
                {
                    bakedRed[i] = (byte)((bakedRed[i] * alphaInv + sourceRed[i] * alpha) >> 8);
                    bakedGreen[i] = (byte)((bakedGreen[i] * alphaInv + sourceGreen[i] * alpha) >> 8);
                    bakedBlue[i] = (byte)((bakedBlue[i] * alphaInv + sourceBlue[i] * alpha) >> 8);
                }

                if (addSourceAlpha)
                {
                    if (sourceAlpha[i] < bakedAlpha[i])
                    {
                        bakedAlpha[i] = sourceAlpha[i];
                    }
                }

                if (sourceHasBump)
                    bakedBump[i] = sourceBump[i];

                ++i;
            }
        }

        return true;
    }

    /** 
     * Make sure images exist, resize source if needed to match the destination
     * 
     * @param dest Destination image
     * @param src Source image
     * @returns Sanitization was succefull
     */
    private boolean SanitizeLayers(ManagedImage dest, ManagedImage src)
    {
        if (dest == null || src == null) return false;

        if ((dest.Channels & ManagedImage.ImageChannels.Alpha) == 0)
        {
            dest.ConvertChannels((byte)(dest.Channels | ManagedImage.ImageChannels.Alpha));
        }

        if (dest.Width != src.Width || dest.Height != src.Height)
        {
            try { src.ResizeNearestNeighbor(dest.Width, dest.Height); }
            catch (Exception ex) { return false; }
        }

        return true;
    }


    private void ApplyAlpha(ManagedImage dest, VisualAlphaParam param, float val)
    {
        ManagedImage src = LoadResourceLayer(param.TGAFile);

        if (dest == null || src == null || src.Alpha == null) return;

        if ((dest.Channels & ManagedImage.ImageChannels.Alpha) == 0)
        {
            dest.ConvertChannels((byte)(ManagedImage.ImageChannels.Alpha | dest.Channels));
        }

        if (dest.Width != src.Width || dest.Height != src.Height)
        {
            try { src.ResizeNearestNeighbor(dest.Width, dest.Height); }
            catch (Exception ex) { return; }
        }

        for (int i = 0; i < dest.Alpha.length; i++)
        {
            byte alpha = src.Alpha[i] <= ((1 - val) * 255) ? (byte)0 : (byte)255;

            if (param.MultiplyBlend)
            {
                dest.Alpha[i] = (byte)((dest.Alpha[i] * alpha) >> 8);
            }
            else
            {
                if (alpha > dest.Alpha[i])
                {
                    dest.Alpha[i] = alpha;
                }
            }
        }
    }

    private void AddAlpha(ManagedImage dest, ManagedImage src)
    {
        if (!SanitizeLayers(dest, src)) return;

        for (int i = 0; i < dest.Alpha.length; i++)
        {
            if (src.Alpha[i] < dest.Alpha[i])
            {
                dest.Alpha[i] = src.Alpha[i];
            }
        }
    }

    private void MultiplyLayerFromAlpha(ManagedImage dest, ManagedImage src)
    {
        if (!SanitizeLayers(dest, src)) return;

        for (int i = 0; i < dest.Red.length; i++)
        {
            dest.Red[i] = (byte)((dest.Red[i] * src.Alpha[i]) >> 8);
            dest.Green[i] = (byte)((dest.Green[i] * src.Alpha[i]) >> 8);
            dest.Blue[i] = (byte)((dest.Blue[i] * src.Alpha[i]) >> 8);
        }
    }

    private void ApplyTint(ManagedImage dest, Color4 src)
    {
        if (dest == null) return;

        for (int i = 0; i < dest.Red.length; i++)
        {
            dest.Red[i] = (byte)((dest.Red[i] * Helpers.FloatToByte(src.R, 0f, 1f)) >> 8);
            dest.Green[i] = (byte)((dest.Green[i] * Helpers.FloatToByte(src.G, 0f, 1f)) >> 8);
            dest.Blue[i] = (byte)((dest.Blue[i] * Helpers.FloatToByte(src.B, 0f, 1f)) >> 8);
        }
    }

    // 
    /// Fills a baked layer as a solid *appearing* color. The colors are 
    /// subtly dithered on a 16x16 grid to prevent the JPEG2000 stage from 
    /// compressing it too far since it seems to cause upload failures if 
    /// the image is a pure solid color
    /// 
    /// <param name="color">Color of the base of this layer</param>
    private void InitBakedLayerColor(Color4 color)
    {
        InitBakedLayerColor(color.R, color.G, color.B);
    }

    // 
    /// Fills a baked layer as a solid *appearing* color. The colors are 
    /// subtly dithered on a 16x16 grid to prevent the JPEG2000 stage from 
    /// compressing it too far since it seems to cause upload failures if 
    /// the image is a pure solid color
    /// 
    /// <param name="r">Red value</param>
    /// <param name="g">Green value</param>
    /// <param name="b">Blue value</param>
    private void InitBakedLayerColor(float r, float g, float b)
    {
        byte rByte = Helpers.FloatToByte(r, 0f, 1f);
        byte gByte = Helpers.FloatToByte(g, 0f, 1f);
        byte bByte = Helpers.FloatToByte(b, 0f, 1f);

        byte rAlt, gAlt, bAlt;

        rAlt = rByte;
        gAlt = gByte;
        bAlt = bByte;

        if (rByte < Byte.MAX_VALUE)
            rAlt++;
        else rAlt--;

        if (gByte < Byte.MAX_VALUE)
            gAlt++;
        else gAlt--;

        if (bByte < Byte.MAX_VALUE)
            bAlt++;
        else bAlt--;

        int i = 0;

        byte[] red = bakedTexture.Image.Red;
        byte[] green = bakedTexture.Image.Green;
        byte[] blue = bakedTexture.Image.Blue;
        byte[] alpha = bakedTexture.Image.Alpha;
        byte[] bump = bakedTexture.Image.Bump;

        for (int y = 0; y < bakeHeight; y++)
        {
            for (int x = 0; x < bakeWidth; x++)
            {
                if (((x ^ y) & 0x10) == 0)
                {
                    red[i] = rAlt;
                    green[i] = gByte;
                    blue[i] = bByte;
                    alpha[i] = Byte.MAX_VALUE;
                    bump[i] = 0;
                }
                else
                {
                    red[i] = rByte;
                    green[i] = gAlt;
                    blue[i] = bAlt;
                    alpha[i] = Byte.MAX_VALUE;
                    bump[i] = 0;
                }

                ++i;
            }
        }

    }
    // #endregion
}
