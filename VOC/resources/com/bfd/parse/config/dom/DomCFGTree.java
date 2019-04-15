package com.bfd.parse.config.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.bfd.parse.entity.ParsetemplateEntity;

public class DomCFGTree implements Comparable<DomCFGTree>, DomCFGElement {

	private static final Log log = LogFactory.getLog(DomCFGTree.class);

	private String[] required; // 必须字段
	private Map<String, Boolean> output; // 输出字段
	private int num = 0; // 模板优先级
	private int status = 0; // 是否可用;0:可用,1:禁用
	private int id;
	private String type;

	private boolean indexMatch;
	private String[] path;

	private ArrayList<DomCFGField> fields;
	private ArrayList<DomCFGTree> children;
	private ArrayList<DomCFGBlock> blocks;

	public DomCFGTree() {
		// TODO Auto-generated constructor stub
	}

	public DomCFGTree(Node node) {
		String sName = node.getNodeName();
		if (!NODE_NAME.equalsIgnoreCase(sName) && !ROOT_NAME.equalsIgnoreCase(sName)
				&& !BLOCK_NAME.equalsIgnoreCase(sName)) {
			return;
		}
		type = DomSearch.getNodeAttr(node, NODE_ATTR_TYPE, ""); // 显示类型新添加

		String stmp = DomSearch.getNodeAttr(node, NODE_ATTR_PATH, ""); // 路径
		if (stmp.length() > 0)
			path = stmp.split("/");

		stmp = DomSearch.getNodeAttr(node, "required", "");
		if (stmp.length() > 0)
			required = stmp.split(",");

		output = new HashMap<String, Boolean>(); // 输出字段
		stmp = DomSearch.getNodeAttr(node, "output", "");
		if (stmp.length() > 0) {
			String[] ss = stmp.split(",");
			for (int i = 0; i < ss.length; i++)
				output.put(ss[i], true);
		}

		fields = new ArrayList<DomCFGField>();
		children = new ArrayList<DomCFGTree>();
		blocks = new ArrayList<DomCFGBlock>();

		stmp = DomSearch.getNodeAttr(node, "matchByIndex", "");
		indexMatch = false;
		if (stmp.equalsIgnoreCase("true"))
			indexMatch = true;

		if (sName.equalsIgnoreCase(BLOCK_NAME)) {
			blocks.add(new DomCFGBlock(node));
		} else {
			Node cld = node.getFirstChild();
			while (cld != null) {
				String childName = cld.getNodeName();
				if (childName.equalsIgnoreCase("node")) {
					children.add(new DomCFGTree(cld));// 子树
				} else if (childName.equalsIgnoreCase("fld")) { // 字段
					fields.add(new DomCFGField(cld));
				} else if (childName.equalsIgnoreCase("block")) {
					blocks.add(new DomCFGBlock(cld));
				}
				cld = cld.getNextSibling();
			}
		}
	}

	public DomCFGTree(Node node, ParsetemplateEntity template) {
		String sName = node.getNodeName();
		if (StringUtils.isEmpty(sName) //
				|| (!NODE_NAME.equalsIgnoreCase(sName) && !ROOT_NAME.equalsIgnoreCase(sName) //
				&& !BLOCK_NAME.equalsIgnoreCase(sName))) {
			return;
		}
		type = template.getPagetypeid()+"";
		num = template.getNum(); // 解析优先级
		status = template.getStatus(); // 是否可用
		id = template.getTmplid();
		String stmp = template.getRequired();
		if (stmp.length() > 0)
			required = stmp.split(",");

		stmp = DomSearch.getNodeAttr(node, NODE_ATTR_PATH, ""); // 路径
		if (stmp.length() > 0)
			path = stmp.split("/");

		output = new HashMap<String, Boolean>(); // 输出字段
		stmp = template.getOutput();
		if (stmp.length() > 0) {
			String[] ss = stmp.split(",");
			for (int i = 0; i < ss.length; i++)
				output.put(ss[i], true);
		}

		fields = new ArrayList<DomCFGField>();
		children = new ArrayList<DomCFGTree>();
		blocks = new ArrayList<DomCFGBlock>();

		stmp = DomSearch.getNodeAttr(node, "matchByIndex", "");
		indexMatch = false;
		if (stmp.equalsIgnoreCase("true"))
			indexMatch = true;

		if (sName.equalsIgnoreCase(BLOCK_NAME)) {
			blocks.add(new DomCFGBlock(node));
		} else {
			Node cld = node.getFirstChild();
			while (cld != null) {
				String childName = cld.getNodeName();
				if (NODE_NAME.equalsIgnoreCase(childName)) {
					children.add(new DomCFGTree(cld)); // 子树
				} else if (FIELD_NAME.equalsIgnoreCase(childName)) { // 字段
					fields.add(new DomCFGField(cld));
				} else if (BLOCK_NAME.equalsIgnoreCase(childName)) {
					blocks.add(new DomCFGBlock(cld)); // block
				}
				cld = cld.getNextSibling();
			}
		}
	}

	public boolean isMatchByIndex() {
		return indexMatch;
	}

	public String[] getTreePath() {
		return path;
	}

	public List<DomCFGField> getFields() {
		return fields;
	}

	public List<DomCFGTree> getChildren() {
		return children;
	}

	public List<DomCFGBlock> getBlocks() {
		return blocks;
	}

	public String getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public Integer getNum() {
		return num;
	}

	public Integer getStatus() {
		return status;
	}

	public String[] getRequiredField() {
		return required;
	}

	public Map<String, Boolean> getOutputField() {
		return output;
	}

	public String toString() {
		String res = "";
		if (path == null)
			res += "No path";
		else
			for (int i = 0; i < path.length; i++)
				if (i > 0)
					res += "/" + path[i];
				else
					res += "path=" + path[i];
		for (int i = 0; i < fields.size(); i++)
			res += ",field-" + i + ": " + fields.get(i).toString();
		for (int i = 0; i < children.size(); i++)
			res += ",child-" + i + ": " + children.get(i).toString();
		if (type == "")
			res += ",No type";
		else
			res += "," + "type=" + type;
		res += "," + "num=" + num;
		res += "," + "active=" + status;
		for (int i = 0; i < blocks.size(); i++)
			res += ",mokes-" + i + ": " + blocks.get(i).toString();
		if (required != null) {
			res += ", required:";
			for (int i = 0; i < required.length; i++)
				res += " " + required[i];
		}
		res += ", output fields:";
		Object[] objs = output.keySet().toArray();
		for (int i = 0; i < objs.length; i++)
			res += " " + (String) objs[i];
		return res;
	}

	@Override
	public int compareTo(DomCFGTree tree) {
		if (tree.getNum() > this.getNum()) {
			return 1;
		}
		if (tree.getNum() < this.getNum()) {
			return -1;
		}
		return 0;
	}

	public static void main(String[] args) {
		List<DomCFGTree> trees = new ArrayList<DomCFGTree>();
		DomCFGTree e = new DomCFGTree();
		e.num = 10;
		DomCFGTree e1 = new DomCFGTree();
		e1.num = 5;
		DomCFGTree e2 = new DomCFGTree();
		e2.num = 8;
		trees.add(e);
		trees.add(e1);
		trees.add(e2);
		Collections.sort(trees);
	}
}
