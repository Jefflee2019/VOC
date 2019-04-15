package com.bfd.parse.config.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.bfd.parse.entity.BaseEntity;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.vo.TwoTuple;

public class DomTemplate extends BaseEntity{
	private static final Log LOG = LogFactory.getLog(DomTemplate.class);
//	String name;
	private String siteId;
	private String pageTypeId;
	String[] required; // 必须 字段
	String[] alias; // 域名别名
	String dns; // DNS
	String urlfilter; // URL模式
	Map<String, Boolean> output; // 输出字段
	ArrayList<DomCFGTree> tmpl; // 模板数组


	public String getPageTypeId() {
		return pageTypeId;
	}

	public void setPageTypeId(String pageTypeId) {
		this.pageTypeId = pageTypeId;
	}

	public DomTemplate(ParsetemplateEntity template) {
		output = new HashMap<String, Boolean>();
		String stmp = template.getRequired();
		if (stmp.length() > 0)
			required = stmp.split(",");
		stmp = template.getOutput();
		if (stmp.length() > 0) {
			String[] ss = stmp.split(",");
			for (int i = 0; i < ss.length; i++)
				output.put(ss[i], true);
		}
		stmp = template.getDns();
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			dns = stmp;
		stmp = template.getSiteid()+"";
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			siteId = stmp;
		stmp = template.getAlias();
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			alias = stmp.split(",");
		tmpl = new ArrayList<DomCFGTree>(); // 模板数组
		DomCFGTree domConfigTree = DomConfig.readBuziConfig(template);
		tmpl.add(domConfigTree);
	}
	
	public void addTemplate(ParsetemplateEntity template){
		DomCFGTree domConfigTree = DomConfig.getInstance().readBuziConfig(template);
		tmpl.add(domConfigTree);
	}

//	public String getTemplateName() {
//		return name;
//	}

	public String toString() {
		String res = "siteId=" + siteId + ", dns=" + dns;
		if (siteId != null)
			res += ",bid=" + siteId;
		if (urlfilter != null)
			res += ",urlfilter=" + urlfilter;
		if (required != null) {
			res += ", required:";
			for (int i = 0; i < required.length; i++)
				res += " " + required[i];
		}
		if (alias != null) {
			res += ", Alias:";
			for (int i = 0; i < alias.length; i++)
				res += " " + alias[i];
		}
		res += ", output fields:";
		if (output != null && output.size() > 0) {
			Object[] objs = output.keySet().toArray();
			for (int i = 0; i < objs.length; i++)
				res += " " + (String) objs[i];
		} else {
			res += "not output";
		}
		if (urlfilter != null)
			res += ",urlfilter=" + urlfilter;
		for (int i = 0; i < tmpl.size(); i++)
			res += ", template-" + i + " is: " + tmpl;
		return res;
	}

//	public DomTemplate(Node node) {
//		name = node.getNodeName();
//		output = new HashMap<String, Boolean>(); // 输出字段
//		tmpl = new ArrayList<DomCFGTree>(); // 模板数组
//		String stmp = DomSearch.getNodeAttr(node, "required", "");
//		if (stmp.length() > 0)
//			required = stmp.split(",");
//		stmp = DomSearch.getNodeAttr(node, "output", "");
//		if (stmp.length() > 0) {
//			String[] ss = stmp.split(",");
//			for (int i = 0; i < ss.length; i++)
//				output.put(ss[i], true);
//		}
//		stmp = DomSearch.getNodeAttr(node, "dns", "");
//		if (stmp.length() > 0)
//			dns = stmp;
//		stmp = DomSearch.getNodeAttr(node, "bid", "");
//		if (stmp.length() > 0)
//			bid = stmp;
//		stmp = DomSearch.getNodeAttr(node, "urlfilter", "");
//		if (stmp.length() > 0)
//			urlfilter = stmp;
//		stmp = DomSearch.getNodeAttr(node, "alias", "");
//		if (stmp.length() > 0)
//			alias = stmp.split(",");
//		Node child = node.getFirstChild();
//		while (child != null) {
//			if ("tmpl".equalsIgnoreCase(child.getNodeName()))
//				tmpl.add(new DomCFGTree(child));
//			child = child.getNextSibling();
//		}
//		LOG.trace("load template for " + name + " is OK, dns=" + dns + ",urlfilter+" + urlfilter + ", alias=" + stmp);
//		// LOG.trace("template for " + name +" is OK, templates num: " +
//		// tmpl.size());
//		// LOG.trace("Temlate Data: " + toString());
//	}

	public DomTemplate(ParsetemplateEntity template, List<TwoTuple<Node, Object>> listChilds) {
//		name = template.getNodeName();
		tmpl = new ArrayList<DomCFGTree>(); // 模板数组
		output = new HashMap<String, Boolean>(); // 输出字段
		String stmp = template.getRequired();
		if (stmp.length() > 0)
			required = stmp.split("[;,\\s]+");
		stmp = template.getOutput();
		if (stmp.length() > 0) {
			String[] ss = stmp.split("[;,\\s]+");
			for (int i = 0; i < ss.length; i++)
				output.put(ss[i], true);
		}
		stmp = template.getDns();
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			dns = stmp;
		stmp = template.getSiteid()+"";
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			siteId = stmp;
		stmp = template.getAlias();
		if (StringUtils.isNotEmpty(stmp) && stmp.length() > 0)
			alias = stmp.split(",");
		for (TwoTuple<Node, Object> twoTuple : listChilds) {
			ParsetemplateEntity newTemplate = (ParsetemplateEntity) twoTuple.second;
			Node child = twoTuple.first;
			if ("tmpl".equalsIgnoreCase(child.getNodeName()))
				tmpl.add(new DomCFGTree(child, newTemplate));
		}
		LOG.trace("load template id for " + siteId + " is OK, dns=" + dns + ", alias=" + stmp);
	}

	public int getTemplateCount() {
		return tmpl.size();
	}

	public DomCFGTree getTemplate(int index) {
		if (index >= 0 && index < tmpl.size())
			return tmpl.get(index);
		else
			return null;
	}

	public String getURLRule() {
		return urlfilter;
	}
	
	public void setSiteId(String siteId){
		this.siteId = siteId;
	}
	public String getSiteId() {
		return siteId;
	}; // 输出字段

	public String getDNS() {
		return dns;
	}; // 输出字段

//	public String[] getRequiredField() {
//		return required;
//	}; // 必须 字段

//	public Map<String, Boolean> getOutputField() {
//		return output;
//	}; // 输出字段

	public String[] getAlias() {
		return alias;
	}

	public ArrayList<DomCFGTree> getTemplates() {
		return this.tmpl;
	}

	@Override
	public String getCacheKey() {
		
		return this.siteId+"|"+this.pageTypeId;
	}

	
}
