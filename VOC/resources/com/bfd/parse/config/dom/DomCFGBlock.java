package com.bfd.parse.config.dom;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

public class DomCFGBlock extends DomCFGTree implements DomCFGElement {
	private static final Log LOG = LogFactory.getLog(DomCFGBlock.class);

	private String[] path;
	private String name;

	private List<DomCFGField> fields;
	private List<DomCFGTree> children;
	private List<DomCFGBlock> blocks;

	// 是否为imglist（如taobao的imglist）
	private boolean isImglist = false;

	public DomCFGBlock(Node node) {
		String mName = node.getNodeName();
		if (!BLOCK_NAME.equalsIgnoreCase(mName)) {
			return;
		}

		fields = new ArrayList<DomCFGField>();
		children = new ArrayList<DomCFGTree>();
		blocks = new ArrayList<DomCFGBlock>();
		name = DomSearch.getNodeAttr(node, BLOCK_ATTR_NAME, "");
		if ("imglist".equalsIgnoreCase(name)) {
			isImglist = true;
		}

		String stmp = DomSearch.getNodeAttr(node, BLOCK_ATTR_PATH, "");
		if (stmp.length() > 0) {
			path = stmp.split("/");
		}

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

	public String[] getTreePath() {
		return path;
	}

	public void setTreePath(String[] treePath) {
		this.path = treePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DomCFGField> getFields() {
		return fields;
	}

	public void setFields(List<DomCFGField> field) {
		this.fields = field;
	}

	public List<DomCFGBlock> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<DomCFGBlock> blocks) {
		this.blocks = blocks;
	}

	public List<DomCFGTree> getChildren() {
		return children;
	}

	public void setChildren(List<DomCFGTree> child) {
		this.children = child;
	}

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}

	public boolean isImglist() {
		return isImglist;
	}

	public String toString() {
		StringBuilder sbBuilder = new StringBuilder();
		if (path == null) {
			sbBuilder.append("No path");
		} else {
			for (int i = 0; i < path.length; i++) {
				if (i > 0)
					sbBuilder.append("/").append(path[i]);
				else
					sbBuilder.append("path=").append(path[i]);
			}
		}
		for (int i = 0; i < fields.size(); i++) {
			sbBuilder.append(",").append("field-").append(i).append(":").append(fields.get(i).toString());
		}
		for (int i = 0; i < blocks.size(); i++) {
			// TODO
		}
		for (int i = 0; i < children.size(); i++) {
			// TODO
		}
		sbBuilder.append(",").append("name=").append(name);
		return sbBuilder.toString();
	}

}