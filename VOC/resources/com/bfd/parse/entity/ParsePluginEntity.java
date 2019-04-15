package com.bfd.parse.entity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.parseplugin.PluginClassLoader;
import com.bfd.parse.config.parseplugin.PluginRuntimeException;
import com.bfd.parse.util.JsonUtil;


public class ParsePluginEntity extends BaseEntity{
	private static final Log LOG = LogFactory.getLog(ParsePluginEntity.class);
	private static final String[] flds = { "path", "name", "pointclazz", "clazz", "exportlibs" };
	private int id;
	public static final String status = "status";
	public static final String attributesFinal = "attributes";
	public static final String exportLibsFinal = "exportlibs";
	private String path;
	private String pid;
	//website表的自增主键
	private String siteId;
	private int pageTypeId;
	private String pluginType;
	private String name;
	private String pointClazz;
	private String clazz;
	private Map<String, String> attributes;
	private List<String> exportLibs;
	private int active;

	private PluginClassLoader classLoader;

	public int getPageTypeId() {
		return pageTypeId;
	}

	public void setPageTypeId(int pageTypeId) {
		this.pageTypeId = pageTypeId;
	}

	public ParsePluginEntity(int id, String path, String pid, String cid, String type, String name, String pointClazz,
			String clazz, Map<String, String> attributes, List<String> exportLibs, int active) {
		this.id = id;
		this.path = path;
		this.pid = pid;
		this.siteId = cid;
		this.pluginType = type;
		this.name = name;
		this.pointClazz = pointClazz;
		this.clazz = clazz;
		this.attributes = attributes;
		this.exportLibs = exportLibs;
		this.active = active;
	}

	public static ParsePluginEntity fromMap(Map<String, Object> param) {
		for (String key : flds) {
			if (!param.containsKey(key)) {
				return null;
			}
		}
		Integer id = 0;
		if (param.containsKey("id")) {
			id = (Integer) param.get("id");
		}
		String path = (String) param.get("path");
		String pid = (String) param.get("pid");
		String siteId = param.get("siteid").toString();
		String pageTypeId =  param.get("pagetypeid").toString();
		if (StringUtils.isEmpty(pid)) {
			pid = siteId + "|" + pageTypeId;
		}
		String name = (String) param.get("name");
		String pointClazz = (String) param.get("pointclazz");
		String clazz = (String) param.get("clazz");
		int status = 1;
		if (param.containsKey(status)) {
			status = (Integer) param.get(status);
		}
		Map<String, String> attributes = new HashMap<String, String>();
		List<String> exportLibsMap = new ArrayList<String>();
		try {
			if (param.containsKey(attributesFinal)) {
				//新版改为String类型了
				if(param.get(attributesFinal) instanceof String){
					attributes.put("str", param.get(attributesFinal).toString() );
				}else{
					attributes = (Map<String, String>) param.get(attributesFinal);
				}
				
			}
			if(param.containsKey(exportLibsFinal)){
//				LOG.info("exportLibs  type:"+param.get("exportLibs").getClass().getName());
				if(param.get(exportLibsFinal) instanceof String){
					exportLibsMap.add(param.get(exportLibsFinal).toString());
				}else if (param.get(exportLibsFinal) instanceof List){
					exportLibsMap = (List) param.get(exportLibsFinal);
				}else{
					LOG.warn("exportLibs type error cid:"+siteId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ParsePluginEntity(id, path, pid, siteId, pageTypeId, name, pointClazz, clazz, attributes, exportLibsMap, status);
	}

	public Object newInstance() throws PluginRuntimeException {
//		LOG.info("execute newInstance ");
//		LOG.info("getpid:"+getPid());
		synchronized (getPid()) {
			try {
				PluginClassLoader loader = preClassLoader();
//				LOG.info("loader:"+JsonUtil.toJSONString(loader.getURLs()));
//				loader.
//				LOG.info("getClass:"+getClazz());
				Class extensionClazz = loader.loadClass(getClazz());
//				LOG.info("extensionClazz:"+extensionClazz.getName()+":"+extensionClazz.getClass().getName());
				return extensionClazz.newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				LOG.error("ClassNotFoundException");
				throw new PluginRuntimeException(e);
			} catch (InstantiationException e) {
				e.printStackTrace();
				LOG.error("InstantiationException");
				throw new PluginRuntimeException(e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				LOG.error("IllegalAccessException");
				throw new PluginRuntimeException(e);
			}catch (Exception e) {
				
				e.printStackTrace();
				LOG.error("Exception");
				e.printStackTrace();
				throw new PluginRuntimeException(e);
			}catch (Throwable e) {
				e.printStackTrace();
				LOG.error("execute newinstance throw throwable");
				throw new PluginRuntimeException(e);
			}
		}
	}

	private PluginClassLoader preClassLoader() {
//		LOG.info("execute preClassLoader");
//		LOG.info("classLoader == null"+(classLoader == null));
		if (classLoader != null)
			return classLoader;
		List<URL> arrayList = new ArrayList<URL>();
//		LOG.info("exportLibs:"+JsonUtil.toJSONString(exportLibs));
		try {
			for (String exportLib : exportLibs) {
				if (exportLib.startsWith(System.getProperty("file.separator"))) {
					arrayList.add(new File(exportLib).toURL());
				} else {
					StringBuilder builder = new StringBuilder();
					String dir = System.getProperty("user.dir");
					
					String sep = System.getProperty("file.separator");
//					LOG.info("dir:"+dir+".separator:"+sep+".exportLib:"+exportLib);
					builder.append(dir).append(sep).append(exportLib);
					arrayList.add(new File(builder.toString()).toURL());
				}
			}
//			LOG.info("getPath():"+getPath());
			File file = new File(getPath());
			for (File file2 : file.listFiles()) {
				if (file2.getAbsolutePath().endsWith("properties"))
					arrayList.add(file2.getParentFile().toURL());
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			LOG.warn("preClassLoader exception " + getPid(), e);
		}
		URL[] urls = arrayList.toArray(new URL[arrayList.size()]);
//		LOG.info("arrayList:"+JsonUtil.toJSONString(arrayList));
		classLoader = new PluginClassLoader(urls, ParsePluginEntity.class.getClassLoader());
		return classLoader;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getType() {
		return pluginType;
	}

	public void setType(String type) {
		this.pluginType = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPointClazz() {
		return pointClazz;
	}

	public void setPointClazz(String point) {
		this.pointClazz = point;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<String> getExportLibs() {
		return exportLibs;
	}

	public void setExportLibs(List<String> exportLibs) {
		this.exportLibs = exportLibs;
	}

	public String getAttribute(String key) {
		if (this.attributes != null)
			return attributes.get(key);
		return null;
	}
	public static void main(String[] args) {
		
	}

	@Override
	public String getCacheKey() {
		return this.siteId+"|"+this.pageTypeId+"|"+this.pluginType;
	}
}
