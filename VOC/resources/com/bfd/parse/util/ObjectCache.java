package com.bfd.parse.util;

import java.util.HashMap;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectCache {

	private static final Log LOG = LogFactory.getLog(ObjectCache.class);

	private static final WeakHashMap<String, ObjectCache> CACHE = new WeakHashMap<String, ObjectCache>();

	private final HashMap<String, Object> objectMap;

	private ObjectCache() {
		objectMap = new HashMap<String, Object>();
	}

	public static ObjectCache get(String uuid) {
		ObjectCache objectCache = CACHE.get(uuid);
		if (objectCache == null) {
			LOG.debug("No object cache found for cacheId=" + uuid + ", instantiating a new object cache");
			objectCache = new ObjectCache();
			CACHE.put(uuid, objectCache);
		}
		return objectCache;
	}

	public Object getObject(String key) {
		return objectMap.get(key);
	}

	public void setObject(String key, Object value) {
		objectMap.put(key, value);
	}
}
