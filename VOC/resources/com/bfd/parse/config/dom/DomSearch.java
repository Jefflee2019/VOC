package com.bfd.parse.config.dom;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.util.TextUtil;

public class DomSearch {
	private static final Log LOG = LogFactory.getLog(DomSearch.class);
	private URLNormalizerClient normalizerClient;
	
	public URLNormalizerClient getNormalizerClient() {
		return normalizerClient;
	}

	public void setNormalizerClient(URLNormalizerClient normalizerClient) {
		this.normalizerClient = normalizerClient;
	}

	/**
	 * 0 => console, 1 => text for stdout, 2 => xml for web
	 */
	private int printMode;
	private int idxMatched;
	private boolean printTreeWhileFailed;
	private boolean matchIndex;
	private static final int iMaxChildNum = 200;
	private int lowestMatchLevel;
	private int maxPrintLevel;
	private StringBuffer treePrintBuffer;
	private Map<String, Object> resultMap;
	private List<Map<String, String>> imgtasks;
	private Map<String, Map<String, String>> contentimgs;
	private int imgIndex = 0;
	private DocumentFragment doc;
	private String cid;
	private String type;
	private boolean typeIsInfo = false;
	private boolean hasImgs = false;
	private String url;
	private String charset;
	private String workerName;

	private static final Pattern imgSuffixCompiler = Pattern.compile("\\.(BMP|JPG|JPEG|PNG|GIF)(?![a-z])",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern imgExcept = Pattern.compile("['\"\\s]");
	private static final Pattern backgroundImg = Pattern.compile("background:url\\(([^\"]+)\\)\\s+");

	private Transformer transformer;

	
	public Map<String, Map<String, String>> getContentimgs() {
		return contentimgs;
	}

	public void setContentimgs(Map<String, Map<String, String>> contentimgs) {
		this.contentimgs = contentimgs;
	}

	/**
	 * 
	 * @return
	 */
	public Transformer getTransformer() {
		if (transformer == null) {
			try {
				transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "no");
				transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "no");
				transformer.setOutputProperty(OutputKeys.METHOD, "html");
			} catch (Exception e) {
				LOG.warn(e);
			}
		}
		return transformer;
	}

	public DomSearch() {
		treePrintBuffer = new StringBuffer(32768);
		resultMap = new HashMap<String, Object>();
		resultMap.put("tasks",new ArrayList());
		imgtasks = new ArrayList<Map<String, String>>();
		contentimgs = new HashMap<String, Map<String, String>>();
		printTreeWhileFailed = true;
		lowestMatchLevel = -1;
		this.doc = null;
		maxPrintLevel = 5;
		printMode = 1;
		imgIndex = 0;
	}

	public DomSearch(String workname) {
		this();
		this.workerName = workname;
	}

	public DomSearch(DocumentFragment doc) {
		resultMap = new HashMap<String, Object>();
		treePrintBuffer = new StringBuffer(32768);
		printTreeWhileFailed = true;
		lowestMatchLevel = -1;
		maxPrintLevel = 5;
		this.doc = doc;
		printMode = 1;
		imgIndex = 0;
		workerName = Thread.currentThread().getName();
	}

	public DomSearch(String workName, URLNormalizerClient normalizerClient) {
		this(workName);
		this.normalizerClient = normalizerClient;
	}

	private void reInit() {
		resultMap.clear();
		imgtasks.clear();
		contentimgs.clear();
		charset = null;
		hasImgs = false;
		imgIndex = 0;
		treePrintBuffer.delete(0, treePrintBuffer.length());
	}

	public boolean hasImgs() {
		return this.hasImgs;
	}

	public Map<String, Object> getParseResult() {
		return resultMap;
	}

	public int getMatchTmplIDX() {
		return idxMatched;
	}

	public void setPrintMode(int mode) {
		if (mode != 2 && mode != 0)
			printMode = 1;
		else
			printMode = mode;
		printTreeWhileFailed = true;
	}

	public void setMaxPrintLevel(int level) {
		if (level > 0 && (level < 30 || level == 99))
			maxPrintLevel = level;
		printTreeWhileFailed = true;
	}

	public void setPrintFlag(boolean bPrint) {
		printTreeWhileFailed = bPrint;
	}

	public boolean urlIsOK(String url, String pattern) { // 检查URL是否匹配标准模式
		if (pattern == null || pattern.length() < 1)
			return true;
		return Pattern.matches(pattern, url);
	}

	public String getTreeString() {
		return treePrintBuffer.toString();
	}

	//通过path搜索节点，页面上单个字段的路径测试也是调用这个方法
	public Node searchNode(DocumentFragment doc, String path) {
		lowestMatchLevel = -1;
		this.doc = doc;
		Node root = searchNode((Node) doc, "html@@@0/body@@@0");
		if (root == null)
			root = searchNode((Node) doc, "body@@@0");
		if (root == null) {
			LOG.info("There is no root element, path=" + path);
			root = (Node) doc;
		}
		Node fNode = searchNode(root, path);
		if (fNode == null) {
			LOG.info("There is no parse result, path=" + path);
			return null;
		}
		return fNode;
	}

	private String getNodeData(Node node, String exRegex, String defaultVal
			, boolean isImg, boolean collectImg,String attName) {
		String txt = "";
		// 获取图片链接地址
		if (node.getNodeName().equalsIgnoreCase("img") || isImg) {
			return getImgNodeData(node, attName, false);
		}
		// do NOT get data from node of script/style/comment
		StringBuffer sb = new StringBuffer();
		recurseGetContent(node, sb, collectImg);
		txt = sb.toString();

		// mogujie显示的名称unicode编码，学要转码
		if (txt.indexOf("&amp;#") >= 0 || txt.indexOf("&#") >= 0) {
			txt = txt.replaceAll("&amp;", "&");
			Matcher matcher = Pattern.compile("&#(\\d+);").matcher(txt);
			int start = 0;
			StringBuilder chars = new StringBuilder();
			while (matcher.find()) {
				int st = matcher.start();
				String group = matcher.group(1);
				if (st > start) {
					chars.append(txt.substring(start, st));
				}
				chars.append((char) Integer.parseInt(group));
				start = st + group.length() + 3;
			}
			if (start < txt.length()) {
				chars.append(txt.substring(start));
			}
			txt = chars.toString();
		}

		// 读取节点数据，需要的话进行后处理
		if (exRegex != null && exRegex.length() > 0) {
			txt = extraHandle(txt, exRegex, defaultVal);
		} else {
			// 不需后处理，过滤空白符. JSON格式输出
			txt = new String(trimBadChar(txt.getBytes()));
		}
		txt = TextUtil.removeAllHtmlTags(txt);
		return txt;
	}

	/**
	 * 过滤特殊字符（table除外）为空格，其中空格和table连续时只留一个
	 * 
	 * @param data
	 * @return
	 */
	private byte[] trimBadChar(byte[] data) {
		int i = 0, j = 0, ff = 0, spc = 0, cspc = 0;
		for (; i < data.length; i++) {
			if (data[i] >= 0 && data[i] <= 32) { // space char
				spc++;
				if (j < 1)
					continue; // space at head
				if (ff == 1) { // space already
					if (data[i] == 9 && data[j - 1] != 9)
						data[j - 1] = 9; // new space is tab, last space is NOT
											// tab, replace it
					continue; // trim space more than one
				}
				if (data[i] == 9)
					data[j++] = 9;
				else
					data[j++] = ' '; // new space, append
				ff = 1;
				continue;
			}
			// space in Chinese
			if ((data[i] & 0xFF) == 0xC2 && i < data.length - 1) {
				if ((data[i + 1] & 0xFF) == 0xA0 || (data[i + 1] & 0xFF) == 0xA1) {
					if (ff == 0) {
						data[j++] = ' ';
						ff = 1;
					}
					i++;
					cspc++;
					continue;
				}
			}
			// space_in_Chinese_blank
			if ((data[i] & 0xFF) == 0xE3 && i < data.length - 2) {
				if ((data[i + 1] & 0xFF) == 0x80 && (data[i + 2] & 0xFF) == 0x80) {
					if (ff == 0) {
						data[j++] = ' ';
						ff = 1;
					}
					i += 2;
					cspc++;
					continue;
				}
			}

			data[j++] = data[i];
			ff = 0;
		}
		// blank or table char at the end of the string
		if (j > 0 && (data[j - 1] == ' ' || data[j - 1] == '\t')) {
			j--;
		}

		// LOG.info(this.workerName + "\told and new len: " + data.length + ", "
		// + j + ", space num: " + spc + ", " + cspc);
		return Arrays.copyOfRange(data, 0, j);
	}

	public String getNodeData(Node node, String exRegex, String defaultVal, boolean isImg, boolean collectImg) {
		return getNodeData(node, exRegex, defaultVal, isImg, collectImg, "");
	}

	public String getNodeData(Node node, String exRegex, String defaultVal, boolean isImg) {
		return getNodeData(node, exRegex, defaultVal, isImg, false);
	}

	public String getNodeData(Node node, String exRegex, String defaultVal) {
		return getNodeData(node, exRegex, defaultVal, false, false);
	}

	private String getImgNodeData(Node node) {
		return getImgNodeData(node, "", null, false);
	}

	private String getImgNodeData(Node node, String att, boolean FilteImg) {
		return getImgNodeData(node, att, null, FilteImg);
	}

	private String getImgNodeData(Node node, StringBuilder rawImg, boolean FilteImg) {
		return getImgNodeData(node, "", rawImg, FilteImg);
	}

	private String getImgNodeData(Node node, String att, StringBuilder rawImg, boolean FilteImg) {
		String srcLink = "";
		if (StringUtils.isNotEmpty(att)) {
			srcLink = filteImgUrl(getNodeAttr(node, att, ""), att);
		}
		if (StringUtils.isEmpty(srcLink)) {
			String tmpAttName = "";
			NamedNodeMap nmap = node.getAttributes();
			String otherLink = "";
			String backLink = "";
			for (int i = 0; i < nmap.getLength(); i++) { // all attributes
				Node nd = nmap.item(i);
				String attName = nd.getNodeName().trim(); // name
				String attValue = nd.getNodeValue().trim(); // value
				if (attName.equalsIgnoreCase("src")) {
					srcLink = attValue; // normal link for image
				} else if (attName.equalsIgnoreCase("style")) {
					backLink = attValue;
					tmpAttName = attName;
				} else if (!"onerror".equalsIgnoreCase(attName) && (!imgExcept.matcher(attValue.trim()).find())
						&& !"alt".equalsIgnoreCase(attName) && imgSuffixCompiler.matcher(attValue).find()) {
					otherLink = attValue;
					tmpAttName = attName;
				}
			}
			if(StringUtils.isNotEmpty(srcLink)){
				LOG.debug(new StringBuilder().append("got img via srcLink, cid=").append(this.cid).append(", type=")
						.append(this.type).append(", url=").append(this.url).append(", attName=").append(tmpAttName)
						.append(", imglink=").append(otherLink).append(",srcLink:").append(srcLink).toString());
			}else if (StringUtils.isNotEmpty(otherLink)) {
				srcLink = otherLink;
				LOG.debug(new StringBuilder().append("got img via otherLink, cid=").append(this.cid).append(", type=")
						.append(this.type).append(", url=").append(this.url).append(", attName=").append(tmpAttName)
						.append(", imglink=").append(otherLink).toString());
			} else if (StringUtils.isEmpty(srcLink)) {
				srcLink = filteImgUrl(backLink, tmpAttName);
				if (StringUtils.isNotEmpty(srcLink)) {
					LOG.debug(new StringBuilder().append("got img via style, cid=").append(this.cid).append(", type=")
							.append(this.type).append(", url=").append(this.url).append(", imglink=").append(otherLink)
							.toString());
				}
			}
		}
		if (StringUtils.isEmpty(srcLink)) {
			return "";
		} else if ((srcLink.indexOf(":")>=0 && srcLink.indexOf(":") <= 8 && !srcLink.startsWith("http")) || srcLink.length() > 300) {
			// 前8个字符含有:，但不是http（https）开头or长度>300
			return "";
		}
		if (rawImg != null) {
			rawImg.append(srcLink);
		}
		if(this.normalizerClient==null){
			this.normalizerClient = new URLNormalizerClient();
		}
		// 需要进行归一化处理
		return normalizerClient.normalize(cid, type + "_img", srcLink, this.url, true, FilteImg);
	}

	private String filteImgUrl(String link, String att) {
		if ("style".equalsIgnoreCase(att)) {
			if (StringUtils.isNotEmpty(link)) {
				Matcher matcher = backgroundImg.matcher(link);
				if (matcher.find()) {
					link = matcher.group(1);
				} else {
					return "";
				}
			}
		}
		return link;
	}

	private String getZipAnEncodedRawContent(Node node) {
		try {
			return DataUtil.zipAndEncode(getRawContent(node), "utf8");
		} catch (Exception e) {
			LOG.warn("zip and encode raw content exception,", e);
		}
		return null;
	}

	private String getRawContent(Node node) {
		try {
			StringWriter writer = new StringWriter();
			getTransformer().transform(new DOMSource(node), new StreamResult(writer));
			return (writer.getBuffer().toString());
		} catch (Exception e) {
			LOG.warn(e);
		}
		return null;
	}

	/**
	 * do NOT get data from node of script/style/comment
	 * 
	 * @param node
	 * @param sbuf
	 */
	private void recurseGetContent(Node node, StringBuffer sbuf, boolean collectImg) {
		if ("BR".equalsIgnoreCase(node.getNodeName())) {
			sbuf.append("\t");
			return;
		}
		if ("LI".equalsIgnoreCase(node.getNodeName())) {
			sbuf.append("\t");
		}
		if ("TR".equalsIgnoreCase(node.getNodeName())) {
			sbuf.append("\t");
		}
//		if ("TD".equalsIgnoreCase(node.getNodeName())) {
//			sbuf.append(": ");
//		}
//		if(node.getNodeValue()!=null&&node.getNodeValue().equals("产品名称：Samsung/三星 UA65HU9800...")){
//			formatNodeValue(node);
//		}
		
		String[] excludeNode = { "script", "style", "#comment" };
		for (int i = 0; i < excludeNode.length; i++)
			if (node.getNodeName().equalsIgnoreCase(excludeNode[i]))
				return;
		//图片现在不需要生成任务，注释掉以下代码
		if (collectImg && node.getNodeName().equalsIgnoreCase("img") 
				&& this.imgIndex < 30) {
			getContentImgs(node);
		}
		String nv = node.getNodeValue();
		
		if (nv != null){
			String formatStr = "";
			//带...的补全内容
			if(nv.endsWith("...")){
				formatStr = formatNodeValue(node);
			}
			if(formatStr.length()>0){
				sbuf.append(formatStr);
			}else{
				sbuf.append(nv);
			}
		}
			
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++)
			recurseGetContent(childs.item(i), sbuf, collectImg);
	}

	private String formatNodeValue(Node node){
		
		if(node.getParentNode()==null){
			return "";
		}
		NamedNodeMap map = node.getParentNode().getAttributes();
		if(map.getLength()==0){
			return "";
		}
		String fullStr = "";
		if(map.getNamedItem("title")!=null){
			fullStr = map.getNamedItem("title").getNodeValue();
		}
		if(map.getNamedItem("alt")!=null){
			fullStr = map.getNamedItem("alt").getNodeValue();
		}
		if(fullStr.length()==0){
			return "";
		}
		String commonStr = TextUtil.getMaxCommonStr(node.getNodeValue(), fullStr);
		String rs = node.getNodeValue().replace(commonStr+"...", fullStr);
		
		return rs;
	}
	private void getContentImgs(Node node) {
		StringBuilder rawImg = new StringBuilder();
		String img = getImgNodeData(node, rawImg, true);
		if (StringUtils.isNotEmpty(img)) {
			String imgtag = "img_" + imgIndex;
			String raw = rawImg.toString();

			Map<String, String> imgUnit = new HashMap<String, String>();
			imgUnit.put("imgtag", imgtag);
			imgUnit.put("img", img);
			imgUnit.put("rawimg", raw);

			Map<String, String> rawMap = new HashMap<String, String>();
			rawMap.put("imgtag", new String(imgtag));
			rawMap.put("img", new String(img));
			rawMap.put("rawimg", new String(raw));

			Map<String, String> tagMap = new HashMap<String, String>();
			tagMap.put("imgtag", new String(imgtag));
			tagMap.put("img", new String(img));
			tagMap.put("rawimg", new String(raw));

			imgtasks.add(imgUnit);
			contentimgs.put(raw, rawMap);// rawimg作key
			contentimgs.put(imgtag, tagMap);// imgtag作key
			imgIndex++;
		}
	}

	//模板解析入口函数,首先根据模板优先级排序，然后遍历所有模板 
	public boolean executeTemplateParse(DocumentFragment doc, DomCFGTree domTmpl, 
											String pageTypeId, String url, String charset) {
		if (doc == null || domTmpl == null || StringUtils.isEmpty(pageTypeId)) {
			LOG.info(workerName + " there is no right template, url=" + url);
			return false;
		}
		this.url = url;
		this.charset = charset;
		setType(pageTypeId);
		this.doc = doc;
		Node root = searchNode((Node) doc, "html@@@0/body@@@0");
		if (root == null) {
			root = searchNode((Node) doc, "body@@@0");
		}
		// 没有root节点
		if (root == null) {
			root = (Node) doc;
		}
		
		parseNode(root, domTmpl, domTmpl.getOutputField(), resultMap);
		return false;
	}

	private void setType(String type) {
		this.type = type;
		if (type.endsWith("info")) {
			this.typeIsInfo = true;
		}
	}

	private boolean checkResult(DomCFGTree tmpl) {
		String[] flds = tmpl.getRequiredField();
		if (flds != null) {
			for (String fld : flds) {
				Object obj = resultMap.get(fld);
				if (obj == null || (obj instanceof List && ((List) obj).size() == 0)) {
					LOG.info(workerName +" template id is "+tmpl.getId() + " Missing field " + fld);
					return false;
				}
			}
		}
		return true;
	}

	private String extraHandle(String txt, String regEx, String defaultVal) { // 根据正则表达式对结果串进行附加处理
		String res = "";
		try {
			Pattern ptrn = Pattern.compile(regEx);
			Matcher mch = ptrn.matcher(txt);
			while (mch.find()) {
				int rn = mch.groupCount();
				for (int i = 1; i <= rn; i++)
					// 合并需要的结果
					res += mch.group(i);
			}
		} catch (PatternSyntaxException x) {
			res = defaultVal; // 发生异常，返回原始数据
		} catch (IllegalStateException x) {
			res = defaultVal;
		} catch (Exception x) {
		}
		LOG.trace(workerName + " ExtraHandle: from [" + txt + "] to [" + res + "], regEx=[" + regEx + "]");
		return res;
	}

	//根据tree类型的模板解析tnode节点；outFields为模板规定要输出的字段，rMap为解析结果
	public boolean parseNode(Node tNode, DomCFGTree tmpl, Map<String, Boolean> outFields, Map<String, Object> rMap) {
		Node htmlNode = tNode;
		String[] rp = tmpl.getTreePath(); // path to interest node
		if (rp != null && rp.length > 0) {
			htmlNode = searchChildNode(tNode, rp, 0); // locate the node
			if (htmlNode == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(workerName + " curName=" + tNode.getNodeName() + ",cur_id="
							+ getNodeAttr(tNode, "id", "") + ",cur_class=" + getNodeAttr(tNode, "class", "")
							+ "Failed to locate " + rp[0] + ", H=" + rp.length + ",  node children =>");
					NodeList childNodes = tNode.getChildNodes();
					for (int i = 0; i < childNodes.getLength(); i++) {
						Node item = childNodes.item(i);
						LOG.trace(workerName + " ===> tagName=" + item.getNodeName() + ", class="
								+ getNodeAttr(item, "class", "") + ", id=" + getNodeAttr(item, "id", "") + ", idx=" + i);
					}
				}
				Node tmpNode = getLowestMismatchNode(tNode, rp, 0);
				return false;
			}
		}

		boolean childOK = true;
		//处理子节点
		List<DomCFGTree> clds = tmpl.getChildren();
		for (int i = 0; i < clds.size(); i++) { // search child node if any
			childOK &= parseNode(htmlNode, clds.get(i), outFields, rMap);
		}

		// get the field value if any
		List<DomCFGField> flds = tmpl.getFields();
		for (int i = 0; i < flds.size(); i++) {
			if (outFields.containsKey(flds.get(i).getName())) {
				childOK &= retrieveField(htmlNode, flds.get(i), rMap); // TODO
			}
		}

		List<DomCFGBlock> blocks = tmpl.getBlocks();

		for (DomCFGBlock domcfgBlock : blocks) {

			String name = domcfgBlock.getName();
			if (!outFields.containsKey(name))
				continue;

			String mPATH[] = domcfgBlock.getTreePath();
			ArrayList<Node> nodes = new ArrayList<Node>();
			if (mPATH != null && mPATH.length > 0) {
				searchChildList(htmlNode, mPATH, 0, nodes);
				if (nodes.size() < 1) {
					LOG.info(workerName + " Missing field: " + name);
					continue;
				}
			}
			// 处理imglist块
			if (domcfgBlock.isImglist()) {
				rMap.put("imgs", retrieveImageListBlock(domcfgBlock, name, nodes));
				hasImgs = true;
				continue;
			}

			List<Object> list = new ArrayList<Object>();
			List<DomCFGField> fieldList = domcfgBlock.getFields();
			for (int j = 0; j < nodes.size(); j++) {
				Map<String, Object> fields = new HashMap<String, Object>();
				FieldRs fieldRs = null;
				for (int i = 0; i < fieldList.size(); i++) { // get the field
																// value if any
					fieldRs = retrieveBlockField(nodes.get(j), fieldList.get(i),(List<Map<String, Object>>)rMap.get(Constants.tasks));
					if (fieldRs != null) {
						Object field = convert2Field(fieldRs);
						fields.put(fieldRs.getNodeName(), field);
					}
				}

				List<DomCFGTree> bChilds = domcfgBlock.getChildren(); // node
				Map<String, Object> nodesMap = new HashMap<String, Object>(); 
				for (int i = 0; i < bChilds.size(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					DomCFGTree tree = bChilds.get(i);
					childOK &= parseNode(nodes.get(j), tree, outFields, map);
					if(map.size()!=0){
						nodesMap.putAll(map);
					}
					
					
				}
				if(nodesMap.size()>0){
					list.add(nodesMap);
				}
				

				List<DomCFGBlock> tBlocks = domcfgBlock.getBlocks();
				for (int i = 0; i < tBlocks.size(); i++) {
					List<Object> blockList = new ArrayList<Object>();
					DomCFGBlock block = tBlocks.get(i);
					childOK &= parseBlock(nodes.get(j), block, outFields, blockList);
					fields.put(block.getName(), blockList);
				}

				if (fields.size() > 1) {
					list.add(fields);
				} else if (fields.size() == 1 && fieldRs != null) {
					list.add(fields.get(fieldRs.getNodeName()));
				}
			}
			rMap.put(name, list);
		}
		return true;
	}

	//block类型模板解析node，
	public boolean parseBlock(Node tNode, DomCFGBlock tmpl, Map<String, Boolean> outFields, List<Object> blockList) {

		ArrayList<Node> nodeList = new ArrayList<Node>();
		String[] rp = tmpl.getTreePath(); // path to interest node
		if (rp != null && rp.length > 0) {
			searchChildList(tNode, rp, 0, nodeList);
			if (nodeList.size() == 0) {
				if (LOG.isDebugEnabled())
					LOG.info(workerName + " Failed to locate " + rp[0] + ", H=" + rp.length);
				Node tmpNode = getLowestMismatchNode(tNode, rp, 0);
				return false;
			}
		}

		for (Node node : nodeList) {
			boolean childOK = true;

			Map<String, Object> rMap = new HashMap<String, Object>();
			List<DomCFGTree> clds = tmpl.getChildren();
			for (int i = 0; i < clds.size(); i++) { // search child node if any
				childOK &= parseNode(node, clds.get(i), outFields, rMap);
			}
			// get the field value if any
			List<DomCFGField> flds = tmpl.getFields();
			for (int i = 0; i < flds.size(); i++) {
				if (outFields.containsKey(flds.get(i).getName())) {
					childOK &= retrieveField(node, flds.get(i), rMap); // TODO
				}
			}

			List<DomCFGBlock> blocks = tmpl.getBlocks();

			for (DomCFGBlock domcfgBlock : blocks) {

				String name = domcfgBlock.getName();
				if (!outFields.containsKey(name))
					continue;

				String mPATH[] = domcfgBlock.getTreePath();
				ArrayList<Node> nodes = new ArrayList<Node>();
				if (mPATH != null && mPATH.length > 0) {
					searchChildList(node, mPATH, 0, nodes);
					if (nodes.size() < 1) {
						LOG.debug(workerName + " Missing field: " + name);
						continue;
					}
				}

				// 处理imglist块
				if ("imglist".equalsIgnoreCase(domcfgBlock.getName())) {
					rMap.put("imgs", retrieveImageListBlock(domcfgBlock, name, nodes));
					hasImgs = true;
					continue;
				}

				List<Object> list = new ArrayList<Object>();
				List<DomCFGField> fieldList = domcfgBlock.getFields();
				for (int j = 0; j < nodes.size(); j++) {
					Map<String, Object> fields = new HashMap<String, Object>();
					FieldRs template = null;
					for (int i = 0; i < fieldList.size(); i++) { // get the
																	// field
																	// value if
																	// any
						template = retrieveBlockField(nodes.get(j), fieldList.get(i),(List<Map<String, Object>>)rMap.get(Constants.tasks));
						if (template != null) {
							Object field = convert2Field(template);
							fields.put(template.getNodeName(), field);
						}
					}

					List<DomCFGTree> bChilds = domcfgBlock.getChildren(); // node
					for (int i = 0; i < bChilds.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						DomCFGTree tree = bChilds.get(i);
						childOK &= parseNode(nodes.get(j), tree, outFields, map);
						list.add(map);
					}

					List<DomCFGBlock> tBlocks = domcfgBlock.getBlocks();
					for (int i = 0; i < tBlocks.size(); i++) {
						List<Object> blockList2 = new ArrayList<Object>();
						DomCFGBlock block = tBlocks.get(i);
						childOK &= parseBlock(nodes.get(j), block, outFields, blockList2);
						fields.put(block.getName(), blockList2);
					}

					if (fields.size() > 1) {
						list.add(fields);
					} else if (fields.size() == 1 && template != null) {
						list.add(fields.get(template.getNodeName()));
					}
				}
				rMap.put(name, list);
			}

			blockList.add(rMap);
		}
		return true;
	
	}

	private List<Object> retrieveImageListBlock(DomCFGBlock domcfgBlock, String name, ArrayList<Node> nodes) {
		List<Object> valueList = new ArrayList<Object>();
		List<DomCFGField> fieldList = domcfgBlock.getFields();
		for (int j = 0; j < nodes.size(); j++) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < fieldList.size(); i++) {
				DomCFGField field = fieldList.get(i);
				Node node = nodes.get(j);
				String[] rp_ = field.getFieldPath();
				if (rp_ != null && rp_.length > 0) {
					node = searchChildNode(nodes.get(j), rp_, 0);
					if (node == null) {
						LOG.debug(workerName + " RetrieveImageListBlock Missing field: " + name);
						continue;
					}
				}
				String value = "";
				String key = "";
				if ("text".equalsIgnoreCase(field.getName())) {
					key = "imgtag";
					value = getNodeTextValue(node);
				} else {
					key = field.getName();
					value = getNodeData(node, "", "", field.isImg());
				}
				map.put(key, value);
			}
			valueList.add(map);
		}
		return valueList;
	}

	private String getNodeTextValue(Node node) {
		String value = null;
		value = getNodeAttr(node, "title", "").trim();
		if (StringUtils.isEmpty(value)) {
			value = getNodeAttr(node, "alt", "").trim();
		}
		if (StringUtils.isEmpty(value)) {
			value = getNodeData(node, "", "").trim();
		}
		return value;
	}

	private Object convert2Field(FieldRs tfld) {
		String value = tfld.getText();
		if (StringUtils.isNotEmpty(tfld.getLink())) {
			Map<String, String> ol = new HashMap<String, String>();
			ol.put("link", tfld.getLink());
			ol.put("linktype", tfld.getLinkType());
			ol.put("rawlink", tfld.getRawlink());
//			if ("nextpage".equalsIgnoreCase(tfld.getNodeName())) {
//				ol.put("text", tfld.getText());
//			}
			return ol;
		} else {
			return value;
		}
	}

	private FieldRs retrieveBlockField(Node tNode, DomCFGField fld,List<Map<String, Object>> tasks) {
//		Parsetemplate template = new Parsetemplate();
		FieldRs fieldRs = new FieldRs();
		String name = fld.getName();
		Node node = tNode;
		String[] rp = fld.getFieldPath();
		if (rp != null && rp.length > 0) {
			node = searchChildNode(tNode, rp, 0);
			if (node == null) {
				LOG.debug(workerName + " Missing field: " + name);
				return null;
			}
		}
		String val = null;
		if (fld.isHtml()) {
			try {
				val = getZipAnEncodedRawContent(node);
				if (StringUtils.isEmpty(val))
					return null;
			} catch (Exception e) {
				LOG.warn(workerName + " zip and encode rawContent error, url=" + this.url);
			}
		} else {
			val = getNodeData(node, "", "", fld.isImg(), typeIsInfo && fld.collectImg(), fld.attName());
		}

		String rawLink = null;
		String link = null;
		String linkType = null;
		// 这里是需要link的满足表现形式的
		if (fld.isLink()) {
			linkType = fld.getLinkType();
			rawLink = matchUrl(node, linkType, fld.getLinkAtt());
			if(normalizerClient==null){
				normalizerClient = new URLNormalizerClient();
			}
			link = normalizerClient.normalize(cid, linkType, rawLink, this.url, true);
			if("1".equals(fld.getCreateTask())){
				Map<String, Object> task = new HashMap<String, Object>();
				task.put("link", link);
				task.put("linktype", linkType);
				task.put("rawlink", rawLink);
				tasks.add(task);
			}
		}
		fieldRs.setNodeName(name);
		fieldRs.setText(val);
		fieldRs.setLink(link);
		fieldRs.setRawlink(rawLink);
		fieldRs.setLinkType(linkType);
		fieldRs.setCreateTask(fld.getCreateTask());
		StringBuilder sb = new StringBuilder().append(workerName).append(" Got filed: ").append(name).append("=");
		if (fld.isLink())
			sb.append(", link=").append(link).append(", rawlink=").append(rawLink);
		else
			sb.append(val);
		LOG.trace(sb.toString());
		return fieldRs;
	}

	private boolean retrieveField(Node tNode, DomCFGField fld, Map<String, Object> rMap) {
		String name = fld.getName();
		Node node = tNode;
		String[] rp = fld.getFieldPath();
		if (rp != null && rp.length > 0) {
			node = searchChildNode(tNode, rp, 0);
			if (node == null) {
				LOG.info(workerName + " Missing field: " + name+" rp:"+JsonUtils.toJSONString(rp));
				return false;
			}
		}

		String val = null;
		if (fld.isHtml()) {
			try {
				val = getZipAnEncodedRawContent(node);
				if (StringUtils.isEmpty(val))
					return false;
			} catch (Exception e) {
				LOG.warn(workerName + " zip and encode rawContent error, url=" + this.url);
			}
		} else {
//			val = getNodeData(node, "", "", fld.isImg(), typeIsInfo && fld.collectImg(), fld.attName());
			//TODO:暂时都需要抓取图片
			val = getNodeData(node, "", "", fld.isImg(), true, fld.attName());

		}
		String rawLink = null;
		String link = null;
		String linkType = null;
		String createTask = "";
		if (fld.isLink()) {
			// 这里是需要link的满足表现形式的
			linkType = fld.getLinkType();
			rawLink = matchUrl(node, linkType, fld.getLinkAtt());
			link = normalizerClient.normalize(cid, linkType, rawLink, this.url, true);
			createTask = fld.getCreateTask();
		}
		// 多个字段的（同级）
		if (fld.isMultiField()) {
			List<Object> list = new ArrayList<Object>();
			if (fld.isLink()) {
				HashMap<String, String> ov = new HashMap<String, String>();
				ov.put("text", val);
				ov.put("link", link);
				ov.put("rawlink", rawLink);
				ov.put("linktype", linkType);
				list.add(ov);
			} else
				list.add(val);
			while ((node = node.getNextSibling()) != null)
				postFun(list, node, fld);
			rMap.put(name, list);
			if("1".equals(createTask)){
				((List)rMap.get("tasks")).addAll(list);
			}
		} else {
			if (fld.isLink()) {
				HashMap<String, String> ov = new HashMap<String, String>();
				ov.put("rawlink", rawLink);
				ov.put("link", link);
				ov.put("linktype", linkType);
				rMap.put(name, ov);
				if("1".equals(createTask)){
					((List)rMap.get("tasks")).add(ov);
				}
			} else {
				if (fld.needSegm()) { // 按照分割符分割
					List<String> segs = new ArrayList<String>();
					segs.addAll(Arrays.asList(val.split(fld.getSegmflag())));
					rMap.put(name, segs);
				} else
					rMap.put(name, val);
			}
		}
//		LOG.debug(workerName + " Got field " + name + ", value=" + rMap.get(name).toString());
		return true;
	}

	
	
	public String matchUrl(Node node, String linkType, String linkatt) {
		String result = null;
		String regex = "^\\s*(?!(#|javascript))[^\\s]+\\s*/?$";
		Node urlNode = null;
		if (node.hasChildNodes() && node.getFirstChild().getNodeName().equalsIgnoreCase("a")) {
			urlNode = node.getFirstChild().getAttributes().getNamedItem(linkatt);
		} else if (node.getNodeName().equalsIgnoreCase("a")) {
			urlNode = node.getAttributes().getNamedItem(linkatt);
		}
		if (urlNode != null) {
			String url = urlNode.getNodeValue();
			Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
			Matcher ma = pa.matcher(url);
			if (ma.find()) {
				result = ma.group().trim();
				if (result.contains(" ")) {
					return result.replace(" ", "%20");
				}
				return result;
			}
		}
		return null;
	}

	private List<Object> postFun(List<Object> list, Node node, DomCFGField fld) {
		if (node == null)
			return list;
		if (node.getFirstChild() != null) {
			// 若右孩子结点，则先访问孩子结点，再访问自己，最后是下一个兄弟节点
			if (fld.getFieldPath().length > 0) {
				String[] str = fld.getFieldPath()[fld.getFieldPath().length - 1].split("@");
				if (matchName(node, str[0], null, str[2])) {
					String val = getNodeData(node, "", "");
					if (fld.isLink()) {
						String linkType = fld.getLinkType();
						String rawLink = matchUrl(node, linkType, fld.getLinkAtt());
						String link = normalizerClient.normalize(cid, linkType, rawLink, this.url, true);
						HashMap<String, String> ov = new HashMap<String, String>();
						ov.put("text", val);
						ov.put("rawlink", rawLink);
						ov.put("link", link);
						ov.put("linktype", linkType);
						list.add(ov);
					} else
						list.add(val);
				}
			}
			// 若无孩子结点，则打印当前结点，然后遍历下一个兄弟结点
		}
		return list;
	}

	public static String getNodeAttr(Node node, String attname, String strDefault) {
		if (node == null)
			return strDefault;
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
			Node att;
			att = attrs.getNamedItem(attname);
			if (att != null)
				return att.getNodeValue();
		}
		return strDefault;
	}

	private boolean matchName(Node node, String name) {
		return (name != null && name.equalsIgnoreCase(node.getNodeName().trim()));
	}

	private boolean matchName(Node node, String name, String id, String nodeClass) {
		if (!matchName(node, name))
			return false;
//		LOG.info("id:"+getNodeAttr(node, "id", "").trim()+".class:"+getNodeAttr(node, "class", "").trim());
		if (id != null && id.length() > 0) { // check id if required
			return id.trim().equalsIgnoreCase(getNodeAttr(node, "id", "").trim());
		}
		if (nodeClass != null && nodeClass.length() > 0) // check class if
															// required
			return isClassMatch(nodeClass.trim(), getNodeAttr(node, "class", "").trim());
		return true; // should not be here
	}

	private boolean isClassMatch(String tmplClass, String nodeClass) {
		int i = 0, j = 0, k = 0;
		try {
			String[] acls = tmplClass.split("\\s+");
			String[] ncls = nodeClass.split("\\s+");
			if (nodeClass.trim().length() < 1) { // node without class
				for (i = 0; i < acls.length; i++) {
					if (!acls[i].startsWith("!"))
						return false;
				}
				return true;
			}
			for (i = 0; i < acls.length; i++) {
				boolean bNeg = false;
				if (acls[i].startsWith("!")) { // Negative match
//					if (ncls.length < 1) {
//						k++;
//						continue; // node has NO class, it's OK, test next
//					}
					acls[i] = acls[i].substring(1);
					bNeg = true;
				}
				for (j = 0; j < ncls.length; j++) {
					if (acls[i].equalsIgnoreCase(ncls[j])) {
						if (bNeg) {
							if (tmplClass.startsWith("!"))
								LOG.debug("match failed");
							return false;
						}
						ncls[j] = "";
						k++;
						break;
					}
				}
			}
			if (k >= acls.length) {
				if (tmplClass.startsWith("!"))
					if (LOG.isDebugEnabled())
						LOG.debug("match OK");
				return true;
			}
		} catch (PatternSyntaxException x) {
			LOG.info(workerName, x);
		}
		if (tmplClass.startsWith("!"))
			LOG.debug(workerName + " match failed: " + i + ", " + j + "," + k);
		return false;
	}

	public Node searchNode(Node node, String path) { // 根据路径搜寻节点
		if (path == null || path.length() < 1)
			return node; // 没有路径，返回节点自身
		String[] names = path.split("/");
		LOG.trace("Search Path=" + path);
		return searchChildNode(node, names, 0);
	}

//	public Node searchNode(Node node, String[] path) { // 根据路径搜寻节点
//		if (path == null || path.length < 1)
//			return node;
//		return searchChildNode(node, path, 0); // 搜寻子节点
//	}

	//tree类型的模板，根据paths来解析node的子节点，会返回一个
	private Node searchChildNode(Node node, String[] paths, int level) { // 递归查询子节点
//		LOG.trace("execute searchChildNode params is "+node.getAttributes()+","+JsonUtils.toJSONString(paths)+","+level);
		if (level > paths.length)
			return null; // some ERROR
		String[] names = paths[level].split("@");
		if (names.length < 4) {
			LOG.info("bad path: " + paths[level] + ", len=" + names.length);
			return null;
		}
//		if (level == paths.length) { // 最后一层
//			if (matchName(node, names[0], names[1], names[2]))
//				return node; // 如果节点自身匹配，返回节点
//			return null; // 否则，返回空节点
//		}
		int nodeIDX = 0;
		
		try {
			nodeIDX = Integer.parseInt(names[3]);
		} catch (NumberFormatException x) {
		}

		int  childNum;

		Node[] childs = new Node[iMaxChildNum];
//		if (matchIndex) // matchByIndex, speed UP
		if((names[1]==null||names[1].trim().length()==0)&&(names[2]==null||names[2].trim().length()==0))
			childNum = getChildByIndex(node, names[0], childs, nodeIDX);
		else
			childNum = getStrictChild(node, names[0], names[1], names[2], childs); // match
																					// by
																					// name,id,class
																					// first
		if (childNum < 1) {
			LOG.trace(workerName + " Miss Match - 00 at level " + level + ", nodePattern:[" + paths[level] + "]");
			if (level == 0) {
				printErrorMessage(node, paths, level);
			}
			if (maxPrintLevel == 99)
				printNode(node);
			return null; // no match, failed
		}
//		if (childNum == 1) { // only one match, it's is the result
//			if (level >= paths.length - 1)
//				return childs[0];
//			Node nMatch = searchChildNode(childs[0], paths, level + 1); // OR
//																		// check
//																		// the
//																		// children's
//																		// data
//			if (nMatch == null && level == 0) {
//				Node tmpNode = getLowestMismatchNode(node, paths, level);
//				printNode(tmpNode);
//			}
//			return nMatch;
//		}
		int imatchIDX = 0;
		Node[] nodeMatch = new Node[iMaxChildNum];
		for (int i = 0; i < childNum; i++) { // multiMatch now
			if (level < paths.length - 1) // try children's match
				nodeMatch[imatchIDX] = searchChildNode(childs[i], paths, level + 1); // try
																						// each
																						// chlids
			else
				nodeMatch[imatchIDX] = childs[i];
			if (nodeMatch[imatchIDX] != null)
				imatchIDX++;
		}
		if (imatchIDX == 0) {
			printNode(node);
			LOG.debug(workerName + " Miss Match - 11 at level " + level + ", num=" + childNum + ", nodePattern:["
					+ paths[level] + "]");
			if (level == 0) {
				printErrorMessage(node, paths, level);
			}
			return null; // no match
		}
		if (imatchIDX == 1)
			return nodeMatch[0]; // only one match, it's the match result
		// 如果通过id class返回的结果有多个，就用nodeindex来得到匹配的子节点
		childNum = getChildByIndex(node, names[0], childs, nodeIDX); // multiMatch,
																		// search
																		// by
																		// index
																		// now
		if (childNum < 1)
			return null;
		if (level >= paths.length - 1)
			return childs[0]; // match the index
		childs[1] = searchChildNode(childs[0], paths, level + 1);
		if (childs[1] == null && level == 0) {
			LOG.trace("Miss Match - 22 at level " + level + ", num=" + childNum + ", nodePattern:[" + paths[level]
					+ "]");
			printErrorMessage(node, paths, level);
		}
		return childs[1];
	}

	private void searchChildList(Node node, String[] paths, int level, List<Node> rlist) {
		if (level > paths.length)
			return; // some ERROR
		String[] names = paths[level].split("@");
		if (names.length < 4) {
			LOG.info(workerName + " bad path: " + paths[level] + ", len=" + names.length);
			return;
		}
		boolean bMulti = false;
		if (paths[level].endsWith("*")) {
			bMulti = true;
			// LOG.info("level=" + level + ", path: " + paths[level] +
			// ", nodes: " + rlist.size());
		}
//		if (level == paths.length) { // 最后一层
//			// LOG.info("^^__^^: " + paths[level]);
//			while (node != null) {
//				if (matchName(node, names[0], names[1], names[2])) // 如果节点匹配，保存节点
//					rlist.add(node);
//				if (bMulti)
//					node = node.getNextSibling(); // 需要多个匹配结果？
//				else
//					node = null;
//			}
//			return; // 否则，返回空节点
//		}
		int nodeIDX = 0;
		
		if (!bMulti) {
			try {
				nodeIDX = Integer.parseInt(names[3]);
			} catch (NumberFormatException x) {
			}
		}

		int childNum;

		Node[] childs = new Node[iMaxChildNum];
//		if (matchIndex) // matchByIndex, speed UP
		if(!bMulti&&(names[1]==null||names[1].trim().length()==0)&&(names[2]==null||names[2].trim().length()==0))
			childNum = getChildByIndex(node, names[0], childs, nodeIDX);
		else
			childNum = getStrictChild(node, names[0], names[1], names[2], childs); // match
																					// by
																					// name,id,class
																					// first
		if (childNum < 1) {
			LOG.error(workerName + " Miss Match - 00 at level " + level + ", nodePattern:[" + paths[level] + "]");
			if (level == 0) {
				printErrorMessage(node, paths, level);
				// printNode(node);
			}
			if (maxPrintLevel == 99)
				printNode(node);
			return; // no match, failed
		}
//		if (childNum == 1) { // only one match, it's is the result
//			if (level >= paths.length - 1) {
//				rlist.add(childs[0]);
//				return;
//			}
//			int icnt = rlist.size();
//			searchChildList(childs[0], paths, level + 1, rlist); // OR check the
//																	// children's
//																	// data
//			if (rlist.size() <= icnt && level == 0) {
//				printErrorMessage(node, paths, level);
//			}
//			return;
//		}
		int imatchIDX = 0;
		Node[] nodeMatch = new Node[iMaxChildNum];
		for (int i = 0; i < childNum; i++) { // multiMatch now
			if (level < paths.length - 1) {
				int ix = rlist.size();
				searchChildList(childs[i], paths, level + 1, rlist); // try each
																		// chlids
				if (rlist.size() <= ix)
					nodeMatch[imatchIDX] = null;
				else
					nodeMatch[imatchIDX] = childs[i];
			} // try children's match
			else {
				nodeMatch[imatchIDX] = childs[i];
				rlist.add(childs[i]);
			}
			if (nodeMatch[imatchIDX] != null)
				imatchIDX++;
		}
		if (imatchIDX == 0) {
			printNode(node);
			LOG.trace(workerName + " Miss Match - 11 at level " + level + ", num=" + childNum + ", nodePattern:["
					+ paths[level] + "]");
			if (level == 0) {
				printErrorMessage(node, paths, level);
			}
			return; // no match
		}
		if (bMulti || imatchIDX == 1)
			return; // only one match, it's the match result
		// actually multimatch
		childNum = getChildByIndex(node, names[0], childs, nodeIDX); // multiMatch,
																		// search
																		// by
																		// index
																		// now
		if (childNum < 1)
			return;
		if (level >= paths.length - 1) {
			rlist.add(childs[0]);
			return; // match the index
		}
		searchChildList(childs[0], paths, level + 1, rlist);
		if (rlist.size() < 1 && level == 0) {
			LOG.trace(workerName + " Miss Match - 22 at level " + level + ", num=" + childNum + ", nodePattern:["
					+ paths[level] + "]");
			printErrorMessage(node, paths, level);
		}
		return;
	}

	private void printErrorMessage(Node node, String[] paths, int level){
		Node tmpNode = getLowestMismatchNode(node, paths, level);
		printNode(tmpNode);
	}
	private Node getLowestMismatchNode(Node node, String[] paths, int level) {
		if (level >= paths.length)
			return node; // some ERROR
		String[] names = paths[level].split("@");
		int nodeIDX = 0;
		if (names.length < 4) {
			LOG.info(workerName + " bad path: " + paths[level] + ", len=" + names.length);
			return node;
		}
		try {
			nodeIDX = Integer.parseInt(names[3]);
		} catch (NumberFormatException x) {
		}
		Node[] childs = new Node[200];

		int childNum = getStrictChild(node, names[0], names[1], names[2], childs); // match
																					// by
																					// name,id,class
																					// first
		if (childNum != 1)
			childNum = getChildByIndex(node, names[0], childs, nodeIDX); // but
																			// have
																			// Much
		if (childNum == 0)
			return node;
		// Match at this level
		return getLowestMismatchNode(childs[0], paths, level + 1);
	}

	// 按名称和索引取某个子节点
	private int getChildByIndex(Node parent, String name, Node[] childs, int index) {
		int ic = 0;
		NodeList clist = parent.getChildNodes();
		for (int i = 0; i < clist.getLength(); i++) {
			LOG.trace("clist.item(i):"+clist.item(i).getNodeName()+","+clist.item(i).getNodeValue());
			if (matchName(clist.item(i), name)) {
				if (ic++ >= index) {
					childs[0] = clist.item(i);
					return 1;
				}
			}
		}
		return 0;
	}

	// 按名称、id、class取所有子节点
	private int getStrictChild(Node node, String name, String id, String nodeClass, Node[] childs) {
		int cn = 0;
		NodeList clist = node.getChildNodes();
		LOG.trace("textcontent:"+node.getTextContent()+",value:"+node.getNodeValue());
		LOG.trace("node children size is "+clist.getLength());
		for (int i = 0; i < clist.getLength(); i++) {
			LOG.trace("clist.item(i):"+clist.item(i).getNodeName()+","+clist.item(i).getNodeValue());
			if (matchName(clist.item(i), name, id, nodeClass)) {
				childs[cn++] = clist.item(i);
				if (cn >= iMaxChildNum)
					return iMaxChildNum;
			}
		}
		if(childs[0]==null){
//			LOG.info("not get node :name:"+name+";id:"+id+";class:"+nodeClass);
		}
		
		return cn;
	}

	public void printNode(Node node) {
		if (treePrintBuffer == null)
			treePrintBuffer = new StringBuffer(32768);
		else
			treePrintBuffer.delete(0, treePrintBuffer.length());
		if (printMode == 1)
			print2PlainText(node, 0);
		else if (printMode == 0)
			print2Console(node, 0);
		else
			print2XmlData(node, 0);
	}

	private String getNodeInfor(Node node) {
		if (node == null)
			return "";
		StringBuffer as = new StringBuffer(512);
		String name = node.getNodeName();
		NamedNodeMap aMap = node.getAttributes();
		String id = "", aclass = "";
		if (aMap != null && aMap.getLength() > 0) {
			Node ni = aMap.getNamedItem("id");
			if (ni != null)
				id = ni.getNodeValue();
			ni = aMap.getNamedItem("class");
			if (ni != null) {
				aclass = ni.getNodeValue();
			}
		}
		int idx = 0;
		Node prev = node.getPreviousSibling();
		while (prev != null) {
			if (prev.getNodeName().equalsIgnoreCase(name))
				idx++;
			prev = prev.getPreviousSibling();
		}
		as.append("name='" + name + " ' dom='" + name + "@");
		as.append(id);
		as.append("@");
		as.append(aclass);
		as.append("@");
		as.append(idx + " '");
		String ss = getNodeData(node, "", "");
		if (ss.length() > 30)
			ss = ss.substring(0, 30);
		as.append(" value='");
		as.append(ss);
		as.append(" '");
		return as.toString();
	}

	private void print2Console(Node node, int level) {
		if (!printTreeWhileFailed || level >= maxPrintLevel)
			return;
		if (node.getNodeName().startsWith("#"))
			return;
		String as = "";
		for (int i = 0; i < level; i++)
			as += "  ";
		LOG.info(as + "l=" + level + ", " + getNodeInfor(node));
		if (level < maxPrintLevel && node.hasChildNodes()) {
			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				print2Console(childs.item(i), level + 1);
		}
		if (level == 0)
			LOG.info("tree output END ------");
	}

	private void print2PlainText(Node node, int level) {
		if (!printTreeWhileFailed || level >= maxPrintLevel)
			return;
		if (node.getNodeName().startsWith("#"))
			return;

		for (int i = 0; i < level; i++)
			treePrintBuffer.append("  ");
		treePrintBuffer.append("l=" + level + ", " + getNodeInfor(node) + "\n");
		if (level < maxPrintLevel && node.hasChildNodes()) {
			NodeList childs = node.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++)
				print2PlainText(childs.item(i), level + 1);
		}
		if (level == 0)
			LOG.trace("tree output END ------");
	}

	private void print2XmlData(Node node, int level) {
		if (!printTreeWhileFailed || level > maxPrintLevel)
			return;
		if (node.getNodeName().startsWith("#"))
			return;
		String xmlNodeName = "level" + (level - 1);
		if (level == 0) {
			treePrintBuffer.append("<?xml version='1.0'?>\n");
			xmlNodeName = "Top";
		}
		for (int i = 0; i < level; i++)
			treePrintBuffer.append(" "); // cascade
		treePrintBuffer.append("<" + xmlNodeName + " caption='"); // xml node
		treePrintBuffer.append(node.getNodeName() + " ' ");
		treePrintBuffer.append(getNodeInfor(node));
		treePrintBuffer.append(">");

		if (level < maxPrintLevel && node.hasChildNodes()) { // any child?
			NodeList childs = node.getChildNodes();
			if (childs.getLength() > 1 || !childs.item(0).getNodeName().startsWith("#")) {
				treePrintBuffer.append("\n");
				for (int i = 0; i < childs.getLength(); i++)
					print2XmlData(childs.item(i), level + 1);
				for (int i = 0; i < level; i++)
					treePrintBuffer.append(" "); // cascade
			}
		}
		treePrintBuffer.append("</" + xmlNodeName + ">\n"); // end of node
		if (level == 0)
			LOG.trace("tree output END ------");
	}

	private boolean isValidContent(String data) {
		String[] keys = { "的", "一", "是", "了", "我", "不", "人", "在", "他", " 有", "这", "个", "上", "们", "来", "到", "时" }; // 5-10%,
																													// 17-20%
		// 的 一 是 了 我-5 不 人 在 他 有 这 个 上 们 来 到 时-17 大 地 为 子 中 你 说 生 国 年 着 就 那 和 要
		// 她 出 也 得 里 后 自 以 会-42 家 可 下 而 过 天 去 能 对 小 多 然 于 心 学 么 之 都 好 看 起 发 当 没
		// 成 只 如 事 把 还 用 第 样 道 想 作 种 开-79 美 总 从 无 情 己 面 最 女 但 现 前 些 所 同 日 手 又 行
		// 意 动 方 期 它 头 经 长 儿 回 位 分 爱 老 因 很 给 名 法 间 斯 知 世 什 两 次 使 身 者 被 高 已 亲 其 进
		// 此 话 常 与 活 正 感-140 42-30%, 79-40%, 140-50%
		// 141-232 10% 见 明 问 力 理 尔 点 文 几 定 本 公 特 做 外 孩 相 西 果 走 将 月 十 实 向 声 车 全 信
		// 重 三 机 工 物 气 每 并 别 真 打 太 新 比 才 便 夫 再 书 部 水 像 眼 等 体 却 加 电 主 界 门 利 海 受 听
		// 表 德 少 克 代 员 许 稜 先 口 由 死 安 写 性 马 光 白 或 住 难 望 教 命 花 结 乐 色
		// 233-381 10% 更 拉 东 神 记 处 让 母 父 应 直 字 场 平 报 友 关 放 至 张 认 接 告 入 笑 内 英 军 候
		// 民 岁 往 何 度 山 觉 路 带 万 男 边 风 解 叫 任 金 快 原 吃 妈 变 通 师 立 象 数 四 失 满 战 远 格 士 音
		// 轻 目 条 呢 病 始 达 深 完 今 提 求 清 王 化 空 业 思 切 怎 非 找 片 罗 钱 紶 吗 语 元 喜 曾 离 飞 科 言
		// 干 流 欢 约 各 即 指 合 反 题 必 该 论 交 终 林 请 医 晚 制 球 决 窢 传 画 保 读 运 及 则 房 早 院 量 苦
		// 火 布 品 近 坐 产 答 星 精 视 五 连 司 巴
		// 382-500 5.43% 奇 管 类 未 朋 且 婚 台 夜 青 北 队 久 乎 越 观 落 尽 形 影 红 爸 百 令 周 吧 识 步
		// 希 亚 术 留 市 半 热 送 兴 造 谈 容 极 随 演 收 首 根 讲 整 式 取 照 办 强 石 古 华 諣 拿 计 您 装 似 足
		// 双 妻 尼 转 诉 米 称 丽 客 南 领 节 衣 站 黑 刻 统 断 福 城 故 历 惊 脸 选 包 紧 争 另 建 维 绝 树 系 伤
		// 示 愿 持 千 史 谁 准 联 妇 纪 基 买 志 静 阿 诗 独 复 痛 消 社 算
		// 501-631 算 义 竟 确 酒 需 单 治 卡 幸 兰 念 举 仅 钟 怕 共 毛 句 息 功 官 待 究 跟 穿 室 易 游 程 号
		// 居 考 突 皮 哪 费 倒 价 图 具 刚 脑 永 歌 响 商 礼 细 专 黄 块 脚 味 灵 改 据 般 破 引 食 仍 存 众 注 笔
		// 甚 某 沉 血 备 习 校 默 务 土 微 娘 须 试 怀 料 调 广 蜖 苏 显 赛 查 密 议 底 列 富 梦 错 座 参 八 除 跑
		// 亮 假 印 设 线 温 虽 掉 京 初 养 香 停 际 致 阳 纸 李 纳 验 助 激 够 严 证 帝 饭 忘 趣 支
		// 632-1000 春 集 丈 木 研 班 普 导 顿 睡 展 跳 获 艺 六 波 察 群 皇 段 急 庭 创 区 奥 器 谢 弟 店 否
		// 害 草 排 背 止 组 州 朝 封 睛 板 角 况 曲 馆 育 忙 质 河 续 哥 呼 若 推 境 遇 雨 标 姐 充 围 案 伦 护 冷
		// 警 贝 著 雪 索 剧 啊 船 险 烟 依 斗 值 帮 汉 慢 佛 肯 闻 唱 沙 局 伯 族 低 玩 资 屋 击 速 顾 泪 洲 团 圣
		// 旁 堂 兵 七 露 园 牛 哭 旅 街 劳 型 烈 姑 陈 莫 鱼 异 抱 宝 权 鲁 简 态 级 票 怪 寻 杀 律 胜 份 汽 右 洋
		// 范 床 舞 秘 午 登 楼 贵 吸 责 例 追 较 职 属 渐 左 录 丝 牙 党 继 托 赶 章 智 冲 叶 胡 吉 卖 坚 喝 肉 遗
		// 救 修 松 临 藏 担 戏 善 卫 药 悲 敢 靠 伊 村 戴 词 森 耳 差 短 祖 云 规 窗 散 迷 油 旧 适 乡 架 恩 投 弹
		// 铁 博 雷 府 压 超 负 勒 杂 醒 洗 采 毫 嘴 毕 九 冰 既 状 乱 景 席 珍 童 顶 派 素 脱 农 疑 练 野 按 犯 拍
		// 征 坏 骨 余 承 置 臓 彩 灯 巨 琴 免 环 姆 暗 换 技 翻 束 增 忍 餐 洛 塞 缺 忆 判 欧 层 付 阵 玛 批 岛 项
		// 狗 休 懂 武 革 良 恶 恋 委 拥 娜 妙 探 呀 营 退 摇 弄 桌 熟 诺 宣 银 势 奖 宫 忽 套 康 供 优 课 鸟 喊 降
		// 夏 困 刘 罪 亡 鞋 健 模 败 伴 守 挥 鲜 财 孤 枪 禁 恐 伙 杰 迹 妹 藸 遍 盖 副 坦 牌 江 顺 秋 萨 菜 划 授
		// 归 浪 听 凡 预 奶 雄 升 碃 编 典 袋 莱 含 盛 济 蒙 棋 端 腿 招 释 介 烧 误

		int ic = 0;
		for (int i = 0; i < keys.length; i++) {
			if (data.indexOf(keys[i]) >= 0) {
				if (++ic >= 3)
					return true;
			}
		}
		LOG.info("code check failed, data: " + data);
		return false;
	}

	public static void main(String[] args) {
		// String attValue =
		// "http://love.taobao.com/guang/this.src='http:/img02.taobaocdn.com/tps/i2/T1IJPyXmFoXXaYlBvy_450x10000.jpg';this.onerror=null;";


		String sss = "随机ss掉落s开发sss洒s落s的分ssssss块";
		sss = sss.replace("s", "\t");
	}
}