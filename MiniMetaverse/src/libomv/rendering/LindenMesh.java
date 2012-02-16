/**
 * Copyright (c) 2008, openmetaverse.org
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
package libomv.rendering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.input.SwappedDataInputStream;

import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class LindenMesh
{
    static final String MESH_HEADER = "Linden Binary Mesh 1.0";
    static final String MORPH_FOOTER = "End Morphs";

    // #region Mesh Structs

    public class Face
    {
        public short Indices1;
        public short Indices2;
        public short Indices3;
        
        public Face(ShortBuffer indices, int idx)
        {
        	Indices1 = indices.get(idx++);
        	Indices2 = indices.get(idx++);
        	Indices3 = indices.get(idx++);
        }
    }

    public class Vertex
    {
        public Vector3 Coord;
        public Vector3 Normal;
        public Vector3 BiNormal;
        public Vector2 TexCoord;
        public Vector2 DetailTexCoord;
        public float Weight;

        @Override
		public String toString()
        {
            return String.format("Coord: %s Norm: %s BiNorm: %s TexCoord: %s DetailTexCoord: %s", Coord, Normal, BiNormal, TexCoord, DetailTexCoord, Weight);
        }
    }

    public class MorphVertex
    {
        public int VertexIndex;
        public Vector3 Coord;
        public Vector3 Normal;
        public Vector3 BiNormal;
        public Vector2 TexCoord;

        @Override
		public String toString()
        {
            return String.format("Index: %d Coord: %s Norm: %s BiNorm: %s TexCoord: %s", VertexIndex, Coord, Normal, BiNormal, TexCoord);
        }
    }

    public class Morph
    {
        public String Name;
        public int NumVertices;
        public MorphVertex[] Vertices;

        @Override
		public String toString()
        {
            return Name;
        }
    }

    public class VertexRemap
    {
        public int RemapSource;
        public int RemapDestination;

        @Override
		public String toString()
        {
            return String.format("{0} -> {1}", RemapSource, RemapDestination);
        }
    }
    // #endregion Mesh Structs

    // Level of Detail mesh
    public class LODMesh
    {
        public float MinPixelWidth;

        protected String _header;
        protected boolean _hasWeights;
        protected boolean _hasDetailTexCoords;
        protected Vector3 _position;
        protected Vector3 _rotationAngles;
        protected byte _rotationOrder;
        protected Vector3 _scale;
        protected short _numFaces;
        protected ShortBuffer Indices;

        public void LoadMesh(String filename) throws IOException
        {
        	LoadMesh(new FileInputStream(filename));
        }
        
        public void LoadMesh(File file) throws IOException
        {
        	LoadMesh(new FileInputStream(file));
        }

        public void LoadMesh(InputStream stream) throws IOException
        {
        	SwappedDataInputStream fis = new SwappedDataInputStream(stream);
        	_header = Helpers.readString(fis, 24);
            if (!_header.equals(MESH_HEADER))
                throw new IOException("Unrecognized mesh format");

            // Populate base mesh variables
            _hasWeights = fis.readByte() != 1;
            _hasDetailTexCoords = fis.readByte() != 1;
            _position = new Vector3(fis);
            _rotationAngles = new Vector3(fis);
            _rotationOrder = fis.readByte();
            _scale = new Vector3(fis);
            _numFaces = fis.readShort();

            Indices = ShortBuffer.allocate(3 * _numFaces);
            for (int i = 0; i < _numFaces; i++)
            {
                Indices.put(fis.readShort());
                Indices.put(fis.readShort());
                Indices.put(fis.readShort());
            }
        }
        
        public Face getFace(int index)
        {
        	if (index >= _numFaces)
        		return null;
    		return new Face(Indices, index * 3);
        }
    }

    public float MinPixelWidth;

    public String getName() { return _name; }
    public String getHeader() { return _header; }
    public boolean getHasWeights() { return _hasWeights; }
    public boolean getHasDetailTexCoords() { return _hasDetailTexCoords; }
    public Vector3 getPosition() { return _position; }
    public Vector3 getRotationAngles() { return _rotationAngles; }
    //public byte RotationOrder
    public Vector3 getScale() { return _scale; }
    public short getNumVertices() { return _numVertices; }

    public Vector3 getVerticeCoord(int index)
    {
    	if (index >= _numVertices)
    		return null;
    	index *= 3;
    	return new Vector3(Vertices.get(index), Vertices.get(index + 1), Vertices.get(index + 2));
    }

    public Vertex getVertex(int index)
    {
    	if (index >= _numVertices)
    		return null;

    	Vertex vertex = new Vertex();
    	int offset = index * 3;
    	vertex.Coord = new Vector3(Vertices.get(offset), Vertices.get(offset + 1), Vertices.get(offset + 2));
    	vertex.Normal = new Vector3(Normals.get(offset), Normals.get(offset + 1), Normals.get(offset + 2));
    	vertex.BiNormal = new Vector3(BiNormals.get(offset), BiNormals.get(offset + 1), BiNormals.get(offset + 2));
    	offset = index * 2;
    	vertex.TexCoord = new Vector2(TexCoords.get(offset), TexCoords.get(offset + 1));
    	vertex.DetailTexCoord = new Vector2(DetailTexCoords.get(offset), DetailTexCoords.get(offset + 1));
    	vertex.Weight = Weights.get(index);
    	return vertex;
    }
    
    public short getNumFaces() { return _numFaces; }

    public Face getFace(int index)
    {
    	if (index >= _numFaces)
    		return null;
    	return new Face(Indices, index);
    }
    
    public short getNumSkinJoints() { return _numSkinJoints; }
    public String[] getSkinJoints() { return _skinJoints; }
    public Morph[] getMorphs() { return _morphs; }
    public int getNumRemaps() { return _numRemaps; }
    public VertexRemap[] getVertexRemaps() { return _vertexRemaps; }
    public TreeMap<Integer, LODMesh> getLODMeshes() { return _lodMeshes; }

    protected String _name;
    protected String _header;
    protected boolean _hasWeights;
    protected boolean _hasDetailTexCoords;
    protected Vector3 _position;
    protected Vector3 _rotationAngles;
    protected byte _rotationOrder;
    protected Vector3 _scale;
    protected short _numVertices;
    public FloatBuffer Vertices;
    public FloatBuffer Normals;
    public FloatBuffer BiNormals;
    public FloatBuffer TexCoords;
    public FloatBuffer DetailTexCoords;
    public FloatBuffer Weights;
    public Vector3 Center;
   
    protected short _numFaces;
    public ShortBuffer Indices;
    protected short _numSkinJoints;
    protected String[] _skinJoints;
    protected Morph[] _morphs;
    protected int _numRemaps;
    protected VertexRemap[] _vertexRemaps;
    protected TreeMap<Integer, LODMesh> _lodMeshes;

    public LindenMesh(String name)
    {
        _name = name;
        _lodMeshes = new TreeMap<Integer, LODMesh>();
    }

    public void LoadMesh(String filename) throws IOException
    {
    	LoadMesh(new FileInputStream(filename));
    }
    
    public void LoadMesh(File file) throws IOException
    {
    	LoadMesh(new FileInputStream(file));
    }

    public void LoadMesh(InputStream stream) throws IOException
    {
    	SwappedDataInputStream fis = new SwappedDataInputStream(stream);
    	_header = Helpers.readString(fis, 24);
        if (!_header.equals(MESH_HEADER))
            throw new IOException("Unrecognized mesh format");

        // Populate base mesh variables
        _hasWeights = fis.readByte() != 1;
        _hasDetailTexCoords = fis.readByte() != 1;
        _position = new Vector3(fis);
        _rotationAngles = new Vector3(fis);
        _rotationOrder = fis.readByte();
        _scale = new Vector3(fis);

        _numVertices = fis.readShort();

        float temp;
        float minX, minY, minZ;
        minX = minY = minZ = Float.MAX_VALUE;
        float maxX, maxY, maxZ;
        maxX = maxY = maxZ = Float.MIN_VALUE;
        
        // Populate the vertex array
        Vertices = FloatBuffer.allocate(3 * _numVertices);
        for (int i = 0; i < _numVertices; i++)
        {
        	temp = fis.readFloat();
            if (temp < minX)
                minX = temp;
            else if (temp > maxX)
                maxX = temp;
            Vertices.put(temp);
        	temp = fis.readFloat();
            if (temp < minY)
                minY = temp;
            else if (temp > maxY)
                maxY = temp;
            Vertices.put(temp);
        	temp = fis.readFloat();
            if (temp < minZ)
                minZ = temp;
            else if (temp > maxZ)
                maxZ = temp;
            Vertices.put(temp);
        }
        // Calculate the center-point from the bounding box edges
        Center = new Vector3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        
        Normals = FloatBuffer.allocate(3 * _numVertices);
        for (int i = 0; i < _numVertices; i++)
        {
        	Normals.put(fis.readFloat());
        	Normals.put(fis.readFloat());
        	Normals.put(fis.readFloat());
        }
        
        BiNormals = FloatBuffer.allocate(3 * _numVertices);
        for (int i = 0; i < _numVertices; i++)
        {
        	BiNormals.put(fis.readFloat());
        	BiNormals.put(fis.readFloat());
        	BiNormals.put(fis.readFloat());
        }
        
        TexCoords = FloatBuffer.allocate(2 * _numVertices);
        for (int i = 0; i < _numVertices; i++)
        {
            TexCoords.put(fis.readFloat());
            TexCoords.put(fis.readFloat());
        }
        
        if (_hasDetailTexCoords)
        {
            DetailTexCoords = FloatBuffer.allocate(2 * _numVertices);
            for (int i = 0; i < _numVertices; i++)
            {
            	DetailTexCoords.put(fis.readFloat());
            	DetailTexCoords.put(fis.readFloat());
            }
        }

        if (_hasWeights)
        {
        	Weights = FloatBuffer.allocate(_numVertices);
            for (int i = 0; i < _numVertices; i++)
            {
                Weights.put(fis.readFloat());
            }
        }

        _numFaces = fis.readShort();

        Indices = ShortBuffer.allocate(3 * _numFaces);
        for (int i = 0; i < _numFaces; i++)
        {
            Indices.put(fis.readShort());
            Indices.put(fis.readShort());
            Indices.put(fis.readShort());
        }
    
        if (_hasWeights)
        {
            _numSkinJoints = fis.readShort();
            _skinJoints = new String[_numSkinJoints];

            for (int i = 0; i < _numSkinJoints; i++)
            {
                _skinJoints[i] = Helpers.readString(fis, 64);
            }
        }
        else
        {
            _numSkinJoints = 0;
            _skinJoints = new String[0];
        }

        // Grab morphs
        List<Morph> morphs = new ArrayList<Morph>();
        String morphName = Helpers.readString(fis, 64);

        while (morphName != MORPH_FOOTER)
        {
            Morph morph = new Morph();
            morph.Name = morphName;
            morph.NumVertices = fis.readInt();
            morph.Vertices = new MorphVertex[morph.NumVertices];

            for (int i = 0; i < morph.NumVertices; i++)
            {
                morph.Vertices[i].VertexIndex = fis.readInt();
                morph.Vertices[i].Coord = new Vector3(fis);
                morph.Vertices[i].Normal = new Vector3(fis);
                morph.Vertices[i].BiNormal = new Vector3(fis);
                morph.Vertices[i].TexCoord = new Vector2(fis);
            }

            morphs.add(morph);

            // Grab the next name
            morphName = Helpers.readString(fis, 64);
        }

        _morphs = morphs.toArray(_morphs);

        // Check if there are remaps or if we're at the end of the file
        try
        {
            _numRemaps = fis.readInt();
            _vertexRemaps = new VertexRemap[_numRemaps];

            for (int i = 0; i < _numRemaps; i++)
            {
                _vertexRemaps[i].RemapSource = fis.readInt();
                _vertexRemaps[i].RemapDestination = fis.readInt();
            }
        }
        catch (IOException ex)
        {
            _numRemaps = 0;
            _vertexRemaps = new VertexRemap[0];
        }
        finally
        {
        	fis.close();
        }
    }
    
    public void LoadLODMesh(int level, String filename) throws IOException
    {
        LODMesh lod = new LODMesh();
        lod.LoadMesh(filename);
        _lodMeshes.put(level, lod);
    }

    public void LoadLODMesh(int level, File file) throws IOException
    {
        LODMesh lod = new LODMesh();
        lod.LoadMesh(file);
        _lodMeshes.put(level, lod);
    }
}