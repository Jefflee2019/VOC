package com.bfd.parse.config.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.DomParser;
import com.bfd.parse.client.ConfigClient;
import com.bfd.parse.config.Config;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.vo.TwoTuple;
//模板加载
public class DomConfig implements Config {

	private static final Log log = LogFactory.getLog(DomConfig.class);

	private static final String CONFIG_NAME = "dom_config";

	private static volatile DomConfig instance;

	private ConfigClient configService;

	private Map<String, DomTemplate> siteTmpls = new ConcurrentHashMap<String, DomTemplate>();

	public static DomConfig getInstance() {
		if (instance == null) {
			synchronized (DomConfig.class) {
				if (instance == null) {
					instance = new DomConfig();
				}
			}
		}
		return instance;
	}

	protected DomConfig() {
		configService = new ConfigClient();
		requestConfig();
	}

	public boolean hasTemplate(String sitename) {
		return siteTmpls.containsKey(sitename);
	}
	//TODO:从parsetemplate来获取数据
	public boolean requestConfig(String siteId, String pageTypeId) {
		HashMap<String, Object> reqMap = new HashMap<String, Object>();
		reqMap.put("type", "one");
		reqMap.put("fieldName", "siteID");
		reqMap.put("fieldValue", siteId);
		reqMap.put("fieldType", "Integer");
		if (StringUtils.isNotEmpty(siteId) && !"all".equalsIgnoreCase(siteId)) {
//			reqMap.put("type", "ALL"); // list,item,info, ALL
										// ALL ---> list + item,list+info
			String templates = configService.getConfig("parsePlugin", JsonUtils.toJSONString(reqMap));
			if (templates == null) {
				return false;
			}
//			log.debug("templates:"+templates);
			List<ParsetemplateEntity> tempList = JsonUtil.parseTemplates(templates);
			DomTemplate tpl = readBuziConfig(tempList);
			addDomTemplate(tpl);
			log.info("reload tmpl config cid=" + siteId + ", size=" + tempList.size());
			return true;
		} else {
			return requestConfig();
		}
	}

	public static void main3(String[] args) {
		PropertyConfigurator.configure("/home/ian/dev/parser/log4j.properties");
		DomTemplate tpl = DomConfig.getInstance().getBySiteId("Cdida");
		log.info(tpl);
		// if (!checkDomTemplate(tpl, "item")) {
		// log.info("ss");
		// }
		log.info("ok");
	}

	@Override
	public boolean requestConfig() {
		String templates = configService.getConfig("parseTemplate", JsonUtil.REQUEST_COMMON);

		 log.info("will reload domconfig");
		 log.trace("templates:"+templates);
		if (templates == null) {
			return false;
		}
		List<ParsetemplateEntity> list = JsonUtil.parseTemplates(templates);
//		log.info("request template config success. templateList size is " + list.size()+".templates:"+templates);
		if (list.size() == 0) {
			log.error("template is error");
			return false;
		}

		siteTmpls.clear();
		Map<String, List<ParsetemplateEntity>> tempMap = new HashMap<String, List<ParsetemplateEntity>>();
		try {
			for (ParsetemplateEntity template : list) {
				List<ParsetemplateEntity> tempList = null;
				String nodeName = template.getSiteid()+"";
				//改为通过站点id和页面类型来查询模板.
//				String nodeName = template.getSiteid()+"|"+template.getPagetypeid();
//				if(nodeName.equalsIgnoreCase("Mjingdong")){
//					log.info("Mjingdong template :"+JsonUtil.toJSONString(template));
//				}
				if (tempMap.containsKey(nodeName)) {
					tempList = tempMap.get(nodeName);
				} else {
					tempList = new ArrayList<ParsetemplateEntity>();
				}
				tempList.add(template);
				tempMap.put(nodeName, tempList);
			}
			
			if (tempMap.size() > 0) {
				for (Entry<String, List<ParsetemplateEntity>> entry : tempMap.entrySet()) {
					List<ParsetemplateEntity> temList = entry.getValue();
					DomTemplate tpl = readBuziConfig(temList);
					addDomTemplate(tpl);
				}
				return true;
			}
			log.info("siteTmpls:"+JsonUtils.toJSONString(siteTmpls));
			log.warn("No template data");
		} catch (Exception e) {
			log.warn("exception", e);
		}
		return false;
	}

	private void addDomTemplate(DomTemplate tmpl) {
		if (tmpl != null) {
			String siteId = tmpl.getSiteId();

			if (siteId != null && siteId.length() > 0)
				siteTmpls.put(siteId, tmpl);
			siteId = tmpl.getDNS();
			if (siteId != null && siteId.length() > 0)
				siteTmpls.put(siteId, tmpl);
			String[] alias = tmpl.getAlias();
			if (alias != null && alias.length > 0) {
				for (int j = 0; j < alias.length; j++)
					siteTmpls.put(alias[j], tmpl);
			}
		}
	}

	public synchronized DomTemplate readBuziConfig(List<ParsetemplateEntity> list) {
		if (list != null && list.size() > 0) {
			List<TwoTuple<Node, Object>> listChilds = new ArrayList<TwoTuple<Node, Object>>();
			String XMLhead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			for (ParsetemplateEntity template : list) {
				if (StringUtils.isEmpty(template.getTmpl())) {
					log.warn("error template, tmpl is empty, tmpl id=" + template.getTmplid());
					continue;
				}
				StringBuilder sbBuilder = new StringBuilder();
				sbBuilder.append(XMLhead);
				sbBuilder.append(template.getTmpl());
				DocumentFragment doc = DomParser.parse2Xml(sbBuilder.toString(), "utf8");
				listChilds.add(new TwoTuple<Node, Object>(doc.getFirstChild(), template));
			}
			return new DomTemplate(list.get(0), listChilds);
		}
		return null;
	}

	public static DomCFGTree readBuziConfig(ParsetemplateEntity template) {
		String XMLhead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		StringBuilder sbBuilder = new StringBuilder();
		String tmpl = template.getTmpl();
		sbBuilder.append(XMLhead);
		sbBuilder.append(tmpl);
		DocumentFragment doc = DomParser.parse2Xml(sbBuilder.toString(), "utf8");
		return new DomCFGTree(doc.getFirstChild(), template);
	}

//	public synchronized DomTemplate readBuziConfig2(List<Template> list) {
//		if (list != null && list.size() > 0) {
//			List<TwoTuple<Node, Object>> listChilds = new ArrayList<TwoTuple<Node, Object>>();
//			String XMLhead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
//			for (Template template : list) {
//				StringBuilder sbBuilder = new StringBuilder();
//				String tmpl = template.getTmpl();
//				sbBuilder.append(XMLhead);
//				sbBuilder.append(tmpl);
//				DocumentFragment doc = DomParser.parse2Xml(sbBuilder.toString(), "utf8");
//				listChilds.add(new TwoTuple<Node, Object>(doc.getFirstChild(), template));
//			}
//			return new DomTemplate(list.get(0), listChilds);
//		}
//		return null;
//	}

//	public static void main(String[] args) {
//		String xml = "<tmpl path='div@@searchwrap w980@4'>"
//				+ "<fld name='category' path='div@@searchCrumb@2' multi='' ext=''/>"
//				+ "<block path='div@@itemSearchResult @*/ul@@@0/li@@producteg@*' name='items'>"
//				+ "<fld name='imgurl' path='a@@@0/img@@@0' multi='' ext=''/>"
//				+ "<fld name='itemlink' path='a@@title@1' multi='' ext='' link='true' linkType='item'/>"
//				+ "<fld name='price' path='p@@price@0/strong@@@0' multi='' ext=''/>"
//				+ "<block path='div@@test@*' name='tests'><fld name='test' path='p@@@0' multi='' ext=''></block>"
//				+ "</block>" + "</tmpl>";
//		Parsetemplate t = new Parsetemplate();
//		t.setActive(0);
//		t.setAlias("");
//		t.setCid("Cyouhao");
//		t.setDns("youhao.com");
//		t.setNodeName("Cyouhao");
//		t.setNum(3);
//		t.setOutput("category,items");
//		t.setRequired("items");
//		t.setTmpl(xml);
//		t.setType("list");
//		ArrayList<ParseTemplate> list = new ArrayList<ParseTemplate>();
//		list.add(t);
//		DomTemplate template = new DomConfig().readBuziConfig(list);
//		if (log.isDebugEnabled())
//			log.debug("=========" + template.getTemplates().get(0).getTreePath());
//	}

//	public static List<Template> parseAllTemplates2(String templates) { 
//		List<Template> list = new ArrayList<Template>();
//		ObjectMapper om = new ObjectMapper();
//		try {
//			Map readValue = om.readValue(templates, Map.class);
//			for (Object o : (List) readValue.get("all")) {
//				Map<String, String> template = (Map<String, String>) o;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return list;
//	}

//	public DomTemplate getByHostname(String hostname) {
//		String[] hostSegs = hostname.split("\\.");
//		if (log.isDebugEnabled())
//			log.debug("hostname=" + hostname + ", length=" + hostSegs.length);
//		String dns = hostSegs[hostSegs.length - 1];
//		for (int i = hostSegs.length - 2; i >= 0; i--) {
//			dns = hostSegs[i] + "." + dns;
//			DomTemplate template = get(dns);
//			if (template != null) {
//				return template;
//			}
//		}
//		return null;
//	}

	public DomTemplate getBySiteId(String siteId) {
		return get(siteId);
	}

//	public DomTemplate getWithRequest(String bid) {
//		DomTemplate template = getByBizid(bid);
//		if (template == null) {
//			String templates = configService.getConfig(bid, "{\"configName\":\"TEMPLATE\",\"type\":\"all\"}");
//			if (templates == null) {
//				//  ================ checker config service connection
//				// status
//			}
//			List<Template> list = JsonUtil.parseTemplates(templates);
//			if (list == null || list.size() == 0)
//				return null;
//			DomTemplate tpl = readBuziConfig(list);
//			addDomTemplate(tpl);
//			return tpl;
//		}
//		return template;
//	}

	public DomTemplate get(String siteId) {
		
		if (siteTmpls.containsKey(siteId)) {
			return siteTmpls.get(siteId);
		}
		return null;
	}

	public void getWithRequest(String bid, String type) {
		//  Auto-generated method stub

	}

	public List<DomCFGTree> get(String bid, String type) {
		DomTemplate domTemplate = get(bid);
		List<DomCFGTree> templates = new ArrayList<DomCFGTree>();
		if (domTemplate != null) {
			templates = domTemplate.getTemplates();
			for (DomCFGTree tree : templates) {
				if (tree.getType().equals(type)) {
					templates.add(tree);
				}
			}
		}
		return templates;
	}

	@Override
	public String name() {
		return CONFIG_NAME;
	}

	@Override
	public Map getData() {
		return this.siteTmpls;
	}
}
