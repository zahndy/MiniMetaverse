package libomv.StructuredData.LLSD;

public final class OSDJson
{
	/*
	 * public static OSD parse(InputStream json) { StreamReader streamReader =
	 * new StreamReader(json); try { JsonReader reader = new
	 * JsonReader(streamReader); return
	 * DeserializeJson(JsonMapper.ToObject(reader)); } finally {
	 * streamReader.Dispose(); } }
	 * 
	 * public static OSD parse(Reader json) {
	 * 
	 * }
	 * 
	 * public static OSD parse(String json) { return
	 * parse(JsonMapper.ToObject(json)); }
	 * 
	 * public static OSD parse(JSONObject json) { if (json == null) { return new
	 * OSD(); }
	 * 
	 * switch (json.GetJsonType()) { case JsonType.Boolean: return
	 * OSD.FromBoolean((boolean)json); case JsonType.Int: return
	 * OSD.FromInteger((int)json); case JsonType.Long: return
	 * OSD.FromLong((long)json); case JsonType.Double: return
	 * OSD.FromReal((double)json); case JsonType.String: String str =
	 * (String)json; if (String.IsNullOrEmpty(str)) { return new OSD(); } else {
	 * return OSD.FromString(str); } case JsonType.Array: OSDArray array = new
	 * OSDArray(json.getCount()); for (int i = 0; i < json.getCount(); i++) {
	 * array.add(DeserializeJson(json[i])); } return array; case
	 * JsonType.Object: OSDMap map = new OSDMap(json.getCount());
	 * IDictionaryEnumerator e = ((IOrderedDictionary)json).iterator(); while
	 * (e.MoveNext()) { map.add((String)e.getKey(),
	 * DeserializeJson((JsonData)e.getValue())); } return map; case
	 * JsonType.None: default: return new OSD(); } }
	 * 
	 * public static String serializeToString(OSD osd) { return
	 * serialize(osd).ToJson(); }
	 * 
	 * public static void SerializeJsonString(Writer writer, OSD osd) {
	 * serialize(osd).ToJson(writer.argvalue); }
	 * 
	 * public static JSONObject serialize(OSD osd) { switch (osd.getType()) {
	 * case Boolean: return new JSONObject(osd.AsBoolean()); case Integer:
	 * return new JSONObject(osd.AsInteger()); case Real: return new
	 * JSONObject(osd.AsReal()); case String: case Date: case URI: case UUID:
	 * return new JSONObject(osd.AsString()); case Binary: byte[] binary =
	 * osd.AsBinary(); JSONObject jsonbinarray = new JSONObject();
	 * jsonbinarray.SetJsonType(JsonType.Array); for (int i = 0; i <
	 * binary.length; i++) { jsonbinarray.add(new JSONObject(binary[i])); }
	 * return jsonbinarray; case Array: JSONObject jsonarray = new JSONObject();
	 * jsonarray.SetJsonType(JsonType.Array); OSDArray array = (OSDArray)osd;
	 * for (int i = 0; i < array.size(); i++) {
	 * jsonarray.add(serialize(array.get(i))); } return jsonarray; case Map:
	 * JSONObject jsonmap = new JSONObject();
	 * jsonmap.SetJsonType(JsonType.Object); OSDMap map = (OSDMap)osd; for
	 * (Entry<String, OSD> kvp : map.entrySet()) { // Default values will not be
	 * serialized to the jsonmap JSONObject data =
	 * serializeNoDefaults(kvp.getValue()); if (data != null) {
	 * jsonmap.put(kvp.getKey(), data); } } return jsonmap; case Unknown:
	 * default: return new JSONObject(); } }
	 * 
	 * private static JSONObject serializeNoDefaults(OSD osd) { switch
	 * (osd.getType()) { case Boolean: boolean b = osd.AsBoolean(); if (!b) {
	 * return null; } return new JSONObject(b); case Integer: int v =
	 * osd.AsInteger(); if (v == 0) { return null; } return new JSONObject(v);
	 * case Real: double d = osd.AsReal(); if (d == 0.0d) { return null; }
	 * return new JSONObject(d); case String: case Date: case URI: String str =
	 * osd.AsString(); if (str != null && str.isEmpty()) { return null; } return
	 * new JSONObject(str); case UUID: UUID uuid = osd.AsUUID(); if
	 * (uuid.equals(UUID.Zero)) { return null; }
	 * 
	 * return new JSONObject(uuid.toString()); case Binary: byte[] binary =
	 * osd.AsBinary(); if (binary == Helpers.EmptyBytes) { return null; }
	 * 
	 * JSONObject jsonbinarray = new JSONObject();
	 * jsonbinarray.SetJsonType(JsonType.Array); for (int i = 0; i <
	 * binary.length; i++) { jsonbinarray.add(new JSONObject(binary[i])); }
	 * return jsonbinarray; case Array: JSONObject jsonarray = new JSONObject();
	 * jsonarray.SetJsonType(JsonType.Array); OSDArray array = (OSDArray)osd;
	 * for (int i = 0; i < array.size(); i++) {
	 * jsonarray.add(serialize(array.get(i))); } return jsonarray; case Map:
	 * JSONObject jsonmap = new JSONObject();
	 * jsonmap.SetJsonType(JsonType.Object); OSDMap map = (OSDMap)osd; for
	 * (Entry<String, OSD> kvp : map.entrySet()) { JSONObject data =
	 * serializeNoDefaults(kvp.getValue()); if (data != null) {
	 * jsonmap.put(kvp.getKey(), data); } } return jsonmap; case Unknown:
	 * default: return null; } }
	 */
}
