package libomv.assets;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.InflaterInputStream;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// Represents Mesh asset
public class AssetMesh extends AssetItem
{
    // Decoded mesh data
    public OSDMap MeshData;

    // Override the base classes AssetType
	@Override
	public AssetItem.AssetType getAssetType()
	{
		return AssetItem.AssetType.Mesh;
	}

	// Initializes a new instance of an AssetMesh object
	public AssetMesh() { }

    /**
     * Initializes a new instance of an AssetMesh object with parameters
     *
     * @param assetID A unique <see cref="UUID"/> specific to this asset
     * @param assetData A byte array containing the raw asset data
     */
    public AssetMesh(UUID assetID, byte[] assetData)
    {
        super(assetID, assetData);
    }

    // TODO: Encodes Collada file into LLMesh format
	@Override
	public void Encode()
	{
	}

    /**
     * Decodes mesh asset. See <see cref="OpenMetaverse.Rendering.FacetedMesh.TryDecodeFromAsset"
     * to further decode it for rendering
     * 
     * @returns true
     */
	@Override
	public boolean Decode()
    {
        try
        {
            MeshData = new OSDMap();

            InputStream data = new ByteArrayInputStream(AssetData);
            OSDMap header = (OSDMap)OSD.parse(data, Helpers.UTF8_ENCODING);
            data.mark(AssetData.length);

            for (String partName : header.keySet())
            {
              	OSD value = header.get(partName);
                if (value.getType() != OSDType.Map)
                {
                    MeshData.put(partName, value);
                    continue;
                }

                OSDMap partInfo = (OSDMap)value;
                if (partInfo.get("offset").AsInteger() < 0 || partInfo.get("size").AsInteger() == 0)
                {
                    MeshData.put(partName, partInfo);
                    continue;
                }
                data.reset();
                data.skip(partInfo.get("offset").AsInteger());
                InflaterInputStream inflate = new InflaterInputStream(data);
                MeshData.put(partName, OSD.parse(inflate, Helpers.UTF8_ENCODING)); 
            }
            return true;
        }
        catch (Exception ex)
        {
            Logger.Log("Failed to decode mesh asset", LogLevel.Error, ex);
            return false;
        }
    }
}
