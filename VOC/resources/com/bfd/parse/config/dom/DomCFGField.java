package com.bfd.parse.config.dom;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.bfd.parse.DomParser;

public class DomCFGField implements DomCFGElement {

	private static final Log LOG = LogFactory.getLog(DomCFGField.class);

	private String name; // 字段名称
	private String[] path; // 字段路径，多级=[div@@conlist@1]
	private String extFilter; // 匹配结果的附加处理，正则表达式

	boolean isMulti; // 多匹配结果，多匹配结果时，会查询多个同级节点的值
	int multiLevel; // 多匹配结果时，多匹配结果的开始级别

	boolean isUseIndex; // 是否使用索引进行匹配，缺省是否，即在名称匹配的情况下，首先通过id，class进行匹配，失败时才通过index进行匹配

	private boolean isLink; // 链接地址
	private String linkType; // 链接类型
	private boolean needImg = false; // 是否收集图片
	private boolean isImg = false;
	private String attName;
	private boolean html = false; // 是否为获取html
	private String linkAtt;
	private boolean rmHead = false;
	private boolean rmTail = false;
	private String segflag = null;
	private boolean needSegm = false;
	private String fieldType;
	private String createTask;
	

	public String getCreateTask() {
		return createTask;
	}

	public void setCreateTask(String createTask) {
		this.createTask = createTask;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public DomCFGField(Node node) {
		init(node);
	}

	public void init(Node node) {
		String sName = node.getNodeName();
		if (!FIELD_NAME.equalsIgnoreCase(sName)) {
			return;
		}
		name = DomSearch.getNodeAttr(node, FILED_ATTR_NAME, ""); // 字段名称
		String stmp = DomSearch.getNodeAttr(node, FILED_ATTR_MULTI, ""); // 是否多匹配字段
		if (stmp.equalsIgnoreCase("true")) {
			isMulti = true;
			stmp = DomSearch.getNodeAttr(node, FILED_ATTR_MULTILEVEL, "0");
			try {
				multiLevel = Integer.parseInt(stmp); // 多匹配开始级别
			} catch (NumberFormatException x) {
				multiLevel = -1;
			}
		} else {
			isMulti = false;
			multiLevel = -1;
		}
		extFilter = DomSearch.getNodeAttr(node, FILED_ATTR_EXT, ""); // 附加处理
		stmp = DomSearch.getNodeAttr(node, FILED_ATTR_PATH, ""); // 路径
		if (stmp.length() > 0) {
			path = stmp.split("/");
		}

		// 是否收集图片
		String needImg = DomSearch.getNodeAttr(node, FILED_ATTR_NEEDIMG, "");
		if ("true".equalsIgnoreCase(needImg) || ("contents".equalsIgnoreCase(name))) {
			this.needImg = true;
		}
		// 是否是img
		String isImg = DomSearch.getNodeAttr(node, FILED_ATTR_IMG, "");
		if ("true".equalsIgnoreCase(isImg) || "small_img".equalsIgnoreCase(name) || "large_img".equalsIgnoreCase(name)
				|| "imgurl".equalsIgnoreCase(name) || "img".equalsIgnoreCase(name)) {
			this.isImg = true;
		}

		// 指定解析图片的属性键
		this.attName = DomSearch.getNodeAttr(node, "attname", "");

		// 是否是img
		String html = DomSearch.getNodeAttr(node, FILED_ATTR_HTML, "");
		if ("true".equalsIgnoreCase(html)) {
			this.html = true;
		}
		String createTaskStr = DomSearch.getNodeAttr(node, create_task, ""); // 是否生成任务
		this.createTask = createTaskStr;
		LOG.info("createTask:"+this.createTask +" name :"+this.name);
		String hrefTempName = DomSearch.getNodeAttr(node, FILED_ATTR_LINK, ""); // 链接
		if (hrefTempName.equalsIgnoreCase("true")) {
			isLink = true;
			this.linkAtt = DomSearch.getNodeAttr(node, "linkatt", "href");
			linkType = DomSearch.getNodeAttr(node, FILED_ATTR_LINKTYPE, null);// 链接的类型
		} else {
			isLink = false;
		}

		// if ("true".equalsIgnoreCase(DomSearch.getNodeAttr(node,
		// FILED_ATTR_RMHEAD, null))){
		// rmHead = true;
		// }
		// if ("true".equalsIgnoreCase(DomSearch.getNodeAttr(node,
		// FILED_ATTR_RMTAIL, null))){
		// rmTail = true;
		// }

		segflag = DomSearch.getNodeAttr(node, FILED_ATTR_SEGM, null);
		if (StringUtils.isNotEmpty(segflag)) {
			needSegm = true;
		}
	}

	public String getName() {
		return name;
	}

	public boolean isMultiField() {
		return isMulti;
	}

	public boolean collectImg() {
		return needImg;
	}

	public boolean isImg() {
		return this.isImg;
	}

	public boolean isHtml() {
		return html;
	}

	public boolean useIndex() {
		return isUseIndex;
	}

	public int getMultiLevel() {
		return multiLevel;
	}

	public String[] getFieldPath() {
		return path;
	}

	public boolean isLink() {
		return isLink;
	}

	public String getLinkType() {
		return linkType;
	}

	public String getLinkAtt() {
		return linkAtt;
	}

	public String attName() {
		return this.attName;
	}

	public String getSegmflag() {
		return segflag;
	}

	public boolean needSegm() {
		return needSegm;
	}

	public String toString() {
		String res = "name=" + name;
		if (path == null) {
			res += ", No path";
		} else {
			for (int i = 0; i < path.length; i++) {
				if (i > 0)
					res += "/" + path[i];
				else
					res += ", path=" + path[i];
			}
		}
		if (extFilter != null) {
			res += ",ext=" + extFilter;
		}
		res += ",isMulti=" + isMulti;
		res += ",isUseIndex=" + isUseIndex;
		res += ",multiLevel=" + multiLevel;
		res += ",isLink=" + isLink;
		res += ",linkTpye=" + linkType;

		return res;
	}
	
	public static void main(String[] args) {
		String str = "<fld name=\"link\" path=\"span@@@0/a@@user_name@0\" createTask=\"1\" multi=\"\" ext=\"\" link=\"true\" linkType=\"info\" />";
		DocumentFragment doc = DomParser.parse2Xml(str, "utf8");
		System.out.println(doc.getNodeName());
		System.out.println(doc.getFirstChild().getNodeName());
		System.out.println(DomSearch.getNodeAttr(doc.getFirstChild(), "linkType", ""));
		String rs = DomSearch.getNodeAttr(doc.getFirstChild(), "createtask", "");
		System.out.println(rs);
	}
}
