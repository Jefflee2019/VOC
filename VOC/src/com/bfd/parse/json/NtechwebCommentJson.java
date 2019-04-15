package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * techweb新闻评论
 * 
 * @author bfd_05
 *
 */
public class NtechwebCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NtechwebCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :"
								+ jsonData.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		parseData.put(Constants.TASKS, new ArrayList<Map<String, Object>>());
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	/**
	 * 处理新闻评论
	 * 
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	public void executeParse(Map<String, Object> parseData, String json, String url, ParseUnit unit) {
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(json);
		String xpath = "//div[@class='item']";
		Object[] objs = null;
		List<Map<String, Object>> commList = new ArrayList<>();

		try {
			objs = tn.evaluateXPath(xpath);
			for (Object obj : objs) {
				Map<String, Object> commMap = new HashMap<>();
				TagNode objNode = (TagNode) obj;
				TagNode[] children = objNode.getChildTags();
				for (TagNode child : children) {
					ContentNode cNode = getNode(child);

					if (child.getAttributes().get("class").equals("publish_time")) {
						String[] str = cNode.getContent().toString().split("&nbsp;&nbsp;");
						commMap.put(Constants.COMMENT_TIME, str[0]);
						continue;
					}
					if (child.getAttributes().get("class").equals("author")) {
						commMap.put(Constants.USERNAME, cNode.getContent().toString().replace("：", ""));
						continue;
					}
					if (child.getAttributes().get("class").equals("item_body")) {
						if (child.getChildTagList().size() > 0) {
							TagNode commBox = child.getChildTagList().get(0);
							if (commBox.getAttributes().containsValue("comment_box")) {
								// 引用回复的map
								Map<String, Object> referCommMap = new HashMap<>();
								TagNode[] referAuthor = commBox.getElementsByAttValue("class", "author", false, false);
								TagNode[] referCommCon = commBox.getElementsByAttValue("class", "item_body", false,
										false);
								if (referAuthor.length > 0 && referCommCon.length > 0) {
									referCommMap.put(Constants.REFER_COMM_USERNAME, getNode(referAuthor[0])
											.getContent());
									referCommMap.put(Constants.REFER_COMM_CONTENT, getNode(referCommCon[0])
											.getContent());
								}
								commMap.put(Constants.REFER_COMMENTS, referCommMap);
							}
						}
						commMap.put(Constants.COMMENT_CONTENT, cNode.getContent());
						continue;
					}

				}
				commList.add(commMap);
			}
		} catch (XPatherException e1) {
			// e1.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + url);
		}

		// var comment_num = 12
		Pattern p = Pattern.compile("var comment_num\\s*=\\s*(\\d+)");
		Matcher comNumMch = p.matcher(json);
		if (comNumMch.find()) {
			parseData.put(Constants.REPLY_CNT, comNumMch.group(1));
		}
		parseData.put(Constants.COMMENTS, commList);
	}

	private ContentNode getNode(TagNode node) {
		if (node.getAllChildren().size() > 0) {
			Object objNode = node.getAllChildren().get(node.getAllChildren().size() - 1);
			// Object objNode = node.getAllChildren().get(0);
			if (objNode instanceof ContentNode) {
				return (ContentNode) objNode;
			} else {
				return getNode((TagNode) objNode);
			}
		} else {
			return new ContentNode("");
		}

	}
}
