package com.bfd.parse.json;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.parseplugin.PluginConfig;
import com.bfd.parse.config.parseplugin.PluginRuntimeException;
import com.bfd.parse.entity.ParsePluginEntity;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ObjectCache;

public class JsonParserFactory {

	private static final Log LOG = LogFactory.getLog(JsonParserFactory.class);

	public static JsonParser getJsonParser(ParseUnit unit, boolean inData, String workName) throws JsonParserNotFound {
		String pluginId = getJsonParserPluginId(unit, inData);
		LOG.info(workName + "got jsonParserPluginId=" + pluginId);
		return getJsonParser(pluginId);
		//TODO:test
//		return new MSuNingJsonParser();
	}

	/**
	 * 拼接ajaxData解析插件的id
	 * 
	 * @param unit
	 * @param inData
	 * @return
	 */
	public static String getJsonParserPluginId(ParseUnit unit, boolean inData) {
		//TODO改为pid改为siteId|pageTypeId
		StringBuilder pluginId = new StringBuilder().append(unit.getSiteId()).append("|").append(unit.getPageTypeId());
//		if (inData) {
//			pluginId.append("|").append(unit.getDatatype()).append("|").append(inData);
//		} else if (!"json".equals(unit.getAjaxdatatype())) {
//			pluginId.append("|").append(unit.getAjaxdatatype());
//		}
		return pluginId.toString();
	}

	public static JsonParser getJsonParser(String parserId) throws JsonParserNotFound {

		ObjectCache objectCache = ObjectCache.get(PluginConfig.getInstance().getUuid());
		try {
			String cacheId = JsonParser.X_POINT_ID + parserId;
			if (parserId == null)
				throw new JsonParserNotFound(parserId);

			if (objectCache.getObject(cacheId) != null) {
				LOG.debug("Found json parser not null, pluginId=" + parserId);
				return (JsonParser) objectCache.getObject(cacheId);
			} else {
				ParsePluginEntity plugin = findPlugin(parserId);
				if (plugin == null) {
					throw new JsonParserNotFound(parserId);
				}
//				LOG.info("plugin is "+JsonUtil.toJSONString(plugin));
				JsonParser parser = (JsonParser) plugin.newInstance();
				objectCache.setObject(cacheId, parser);
//				LOG.info("parser is "+parser.getClass().getName()+":");
//				LOG.info("Found json parser, pluginId=" + parserId);
				return parser;
			}
		} catch (PluginRuntimeException e) {
			throw new JsonParserNotFound(parserId, e.toString());
		}
	}
	
	private static ParsePluginEntity findPlugin(String pluginId) throws PluginRuntimeException {
//		LOG.info("execute findPlugin!");
		List<ParsePluginEntity> plugins = PluginConfig.getInstance().getPlugins(JsonParser.X_POINT_ID);
//		LOG.info("plugins:"+JsonUtil.toJSONString(plugins));
		if (plugins != null) {
			for (ParsePluginEntity plugin : plugins) {
//				LOG.info("plugin.gegPid:"+plugin.getPid());
				if (plugin.getPid().equalsIgnoreCase(pluginId))
					return plugin;
			}
		}
		return null;
	}
}
