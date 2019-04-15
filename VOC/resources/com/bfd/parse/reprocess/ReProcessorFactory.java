package com.bfd.parse.reprocess;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.parseplugin.PluginConfig;
import com.bfd.parse.config.parseplugin.PluginRuntimeException;
import com.bfd.parse.entity.ParsePluginEntity;
import com.bfd.parse.util.ObjectCache;

public class ReProcessorFactory {
	private static final Log LOG = LogFactory.getLog(ReProcessorFactory.class);

	public static ReProcessor getReProcessor(String siteId, String pageTypeId) throws ReProcessorNotFound {
		String processorId = getPluginId(siteId, pageTypeId);
		if (StringUtils.isEmpty(processorId)) {
			return null;
		}
		return getReProcessor(processorId);
//		return new CxcarListReprocess();
	}

	public static String getPluginId(String siteId, String pageTypeId) {
		if (StringUtils.isEmpty(siteId) || StringUtils.isEmpty(pageTypeId)) {
			LOG.warn(" invalid data, cid or type is null, siteId=" + siteId + ", pageTypeId=" + pageTypeId);
			return null;
		}
		return siteId + "|" + pageTypeId;
	}

	public static ReProcessor getReProcessor(String processorId) throws ReProcessorNotFound {
		LOG.info("execute find reprocessor for "+processorId);
		ObjectCache objectCache = ObjectCache.get(PluginConfig.getInstance().getUuid());
		try {
			String cacheId = ReProcessor.X_POINT_ID + processorId;
			if (processorId == null)
				throw new ReProcessorNotFound(processorId);

			if (objectCache.getObject(cacheId) != null) {
				LOG.debug("Found reprocess plugin, ID=" + processorId);
				return (ReProcessor) objectCache.getObject(cacheId);
			} else {
				ParsePluginEntity plugin = findPlugin(processorId);
				if (plugin == null) {
					LOG.info("findplugin is null processId is "+processorId);
					return null;
				}
				ReProcessor processor = (ReProcessor) plugin.newInstance();
				objectCache.setObject(cacheId, processor);
				LOG.debug("Found reprocess plugin, ID=" + processorId);
				return processor;
			}
		} catch (PluginRuntimeException e) {
			throw new ReProcessorNotFound(processorId, e.toString());
		}
	}

	private static ParsePluginEntity findPlugin(String pluginId) throws PluginRuntimeException {
		List<ParsePluginEntity> plugins = PluginConfig.getInstance().getPlugins(ReProcessor.X_POINT_ID);
		if (plugins != null) {
			for (ParsePluginEntity plugin : plugins) {
				if (plugin.getPid().equalsIgnoreCase(pluginId))
					return plugin;
			}
		}
		return null;
	}
}
