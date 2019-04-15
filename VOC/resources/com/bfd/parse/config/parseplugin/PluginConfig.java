package com.bfd.parse.config.parseplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.Config;
import com.bfd.parse.entity.ParsePluginEntity;
import com.bfd.parse.util.JsonUtil;
//插件加载
public class PluginConfig implements Config {

	public static final Log LOG = LogFactory.getLog(PluginConfig.class);

	private static final String CONFIG_NAME = "plugin_config";

	private static volatile PluginConfig instance;

	private ConfigClient configService = new ConfigClient();

	public Map<String, List<ParsePluginEntity>> extensionMap ;
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();

	private String uuid;

	public static PluginConfig getInstance() {
//		LOG.debug("execute PluginConfig getInstance");
		if (instance == null) {
			LOG.debug("instance == null");
			synchronized (PluginConfig.class) {
				if (instance == null) {
					LOG.debug("inside instance == null");
					instance = new PluginConfig();
				}
			}
		}
		return instance;
	}

	private PluginConfig() {
		LOG.info("PluginConfig()");
		extensionMap = new HashMap<String, List<ParsePluginEntity>>();
		requestConfig();
	}

	//TODO: 从parseplugin表获得数据
	@Override
	public boolean requestConfig() {
		LOG.info("will reload pluginconfig");
		String config = configService.getConfig("parsePlugin", REQUEST_ALL);
		LOG.debug("plugin requestConfig config:"+config);
		rwLock.writeLock().lock();
		if (config == null) {
			rwLock.writeLock().unlock();
			return false;
		}
		try {
			Map map = (Map) JsonUtil.parseObject(config);
			List<Map> data = (List<Map>) map.get("data");
			List<ParsePluginEntity> list = new ArrayList<ParsePluginEntity>();
			for (Map pluginMap : data) {
				ParsePluginEntity plugin = ParsePluginEntity.fromMap(pluginMap);
				if (plugin.getActive() == 1) {
					list.add(plugin);
				}
			}
//			LOG.info("list is "+JsonUtil.toJSONString(list));
			if (list.size() > 0) {
				extensionMap.clear();
				for (ParsePluginEntity plugin : list) {
					List<ParsePluginEntity> tmpList = null;
					if (extensionMap.containsKey(plugin.getPointClazz())) {
						tmpList = extensionMap.get(plugin.getPointClazz());
					} else {
						tmpList = new ArrayList<ParsePluginEntity>();
					}
					tmpList.add(plugin);
					extensionMap.put(plugin.getPointClazz(), tmpList);
				}
				setUuid();
				LOG.info("after requestConfig pluginMap is "+JsonUtil.toJSONString(extensionMap));
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("Exception while request plugin configs, Err=" + e.getMessage());
		} finally {
			rwLock.writeLock().unlock();
			displayStatus();
		}
		return false;
	}

	public List<ParsePluginEntity> getPlugins(String pointClazz) {
		
		List<ParsePluginEntity> rs = null;
		try {
			rwLock.readLock().lock();
			if (extensionMap != null && extensionMap.containsKey(pointClazz)) {
				rs =  extensionMap.get(pointClazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			rwLock.readLock().unlock();
		}
		
		return rs;
	}

	public synchronized void setUuid() {
		this.uuid = UUID.randomUUID().toString();
	}

	public synchronized String getUuid() {
		return uuid;
	}

	private void displayStatus() {
		int size = 0;
		if ((extensionMap == null) || (extensionMap.size() == 0)) {
			LOG.info("\tNONE");
		} else {
			for (Entry<String, List<ParsePluginEntity>> entry : extensionMap.entrySet()) {
				List<ParsePluginEntity> list = entry.getValue();
				size += list.size();
				for (ParsePluginEntity plugin : list) {
					LOG.debug("\t" + plugin.getName() + " (" + plugin.getPid() + ")");
				}
			}
		}
//		LOG.info("Registered Plugins:\t size=" + size);
//		LOG.info("Registered Extension-Points: \t size=" + extensionMap.keySet().size());
		if ((extensionMap == null) || (extensionMap.size() == 0)) {
			LOG.info("\tNONE");
		} else {
			for (Entry<String, List<ParsePluginEntity>> entry : extensionMap.entrySet()) {
				LOG.info("\t" + entry.getKey() + "\t size=" + entry.getValue().size());
			}
		}
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	@Override
	public Map getData() {
		return extensionMap;
	}

}
