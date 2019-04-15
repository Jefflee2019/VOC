package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 * 站点名：安卓中文网
 * <p>
 * 主要功能：获得评论相关信息，评论人，评论内容，评论时间
 * @author bfd_01
 *
 */
public class NandroidchineseCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NandroidchineseCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		Map<String, Object> parsedata = new HashMap<String, Object>();
		parsedata.put("tasks", taskList);
		int parsecode = 0;
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
//		try {
//			obj = JsonUtil.parseObject(json);
//		} catch (Exception e) {
//			LOG.error("Nandroidchinese json error url :" + url);
//		}
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, tasks);
//		String html = obj.toString();
		parseReplyHtmlCleaner(json, parsedata);
			}
	
	private void parseReplyHtmlCleaner(String html, Map<String, Object> parseData){
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(html);
		String xpath = "//div[@class='comment-content']/div[@class='replay-box']";
		Object[] objs = null;
		List<Map<String, Object>> commList = new ArrayList<>();
		parseData.put(Constants.COMMENT_REPLY, commList);
		try {
			objs = tn.evaluateXPath(xpath);
			for(Object obj : objs){
				Map<String, Object> commMap = new HashMap<>();
				TagNode objNode = (TagNode) obj;
				String s = objNode.getText().toString();
				
				//span
				TagNode userNode = (TagNode) objNode.evaluateXPath("//span[@class='text-tit fl f12']")[0];
				String username = userNode.getText().toString();
				commMap.put(Constants.USERNAME, username);
				
				//据标签名字不递归寻找
				//用户名 
				TagNode[] children = userNode.getChildTags();
				if(children.length > 1){
					String userName = children[0].getText().toString();
					commMap.put(Constants.COMMENT_REPLY_NAME, userName);
					String regTime = children[1].getText().toString();
					commMap.put(Constants.COMMENTER_REG_TIME, regTime.trim());
				}
				//评论时间
				TagNode replyTimeNode = (TagNode) objNode.evaluateXPath("//span[@class=\"date\"]")[0];
				commMap.put(Constants.COMMENT_REPLY_TIME, replyTimeNode.getText().toString());
				//评分
				//没有评分时候将会出错
				if(objNode.evaluateXPath("//span[@class=\"score\"]").length > 0){
					TagNode scoreNode = (TagNode) objNode.evaluateXPath("//span[@class=\"score\"]")[0];
					commMap.put(Constants.SCORE, scoreNode.getText().toString());
				}
				//评论内容
				Object[] contentObjs = objNode.evaluateXPath("//div[@class=\"comments-words\"]");
				StringBuilder contents = new StringBuilder();
				//类似评论前的标题
				contents.append(objNode.getElementsByName("h3", true)[0].getText());
				for(Object contentObj : contentObjs){
					TagNode contentNode = (TagNode) contentObj;
					contents.append(contentNode.getElementsByName("strong", false)[0].getText().toString().trim());
					contents.append(contentNode.getElementsByName("p", false)[0].getText().toString().trim());
				}
				
				Object[] imgsObj = objNode.evaluateXPath("//li[@class='bigcursor']");
				List<String> contentimgs = new ArrayList<String>();
				commMap.put(Constants.CONTENTIMGS, contentimgs);
				for(Object imgObj : imgsObj){
					TagNode imgNode = (TagNode) imgObj;
					String img = ((TagNode)imgNode.evaluateXPath("//img")[0]).getAttributeByName("src");
					contentimgs.add(img);
				}
				commMap.put(Constants.COMMENT_REPLY_CONTENT, contents.toString());
				//回复数
//				TagNode replyCntNode = (TagNode) objNode.evaluateXPath("//a[@class=\"J_ShowRepy\"]")[0];
//				parseByReg(commMap, Constants.COMMENT_REPLY_CNT, replyCntNode.getText().toString(), PNUM);
//				//有帮助
//				TagNode upCntNode = (TagNode) objNode.evaluateXPath("//a[@class=\"J_ReviewHelp\"]")[0];
//				parseByReg(commMap, Constants.UP_CNT, upCntNode.getText().toString(), PNUM); 
//				//没帮助
//				TagNode downCntNode = (TagNode) objNode.evaluateXPath("//a[@class=\"J_ReviewUnhelp\"]")[0];
//				parseByReg(commMap, Constants.DOWN_CNT, downCntNode.getText().toString(), PNUM);
				commList.add(commMap);
			}
		} catch (XPatherException e) {
			LOG.error("EzolCommentJson reprocess error url");
		}
	}
	
	
//	@SuppressWarnings("unchecked")
//	public void executeParse(Map<String, Object> parsedata, String json,
//			String url, ParseUnit unit) {
//		Object obj = null;
//		try {
//			obj = JsonUtil.parseObject(json);
//			if (obj instanceof Map) {
//				Map<String, Object> data = (Map<String, Object>) obj;
//				if (data.containsKey("items")) {
//					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//					int pageNum = getPageNum(url);
//					int pageSize = getPageSize(url);
//					// 评论数
//					int replyCnt = 0;
//					if (data.containsKey("count")) {
//						replyCnt = Integer
//								.valueOf(data.get("count").toString());
//						parsedata.put(Constants.REPLY_CNT, replyCnt);
//					}
//					if (data != null && data.containsKey("items")) {
//						List<Map<String, Object>> comments = (List<Map<String, Object>>) data
//								.get("items");
//						for (int i = 0; i < comments.size(); i++) {
//							Map<String, Object> comm = new HashMap<String, Object>();
//							Map<String, Object> referComm = new HashMap<String, Object>();
//							comm.put(Constants.COMMENT_CONTENT,
//									((Map<String, Object>) comments.get(i))
//											.get("body"));
//							comm.put(Constants.COMMENTER_NAME,
//									((Map<String, Object>) comments.get(i))
//											.get("author"));
//							comm.put(Constants.UP_CNT,
//									((Map<String, Object>) comments.get(i))
//											.get("ext2"));
//							comm.put(Constants.COMMENT_TIME,
//									((Map<String, Object>) comments.get(i))
//											.get("ext10"));
//
//							if (((List<String>) (((Map<String, Object>) comments
//									.get(i)).get("ext4"))).size() > 0) {
//								List<Map<String, Object>> referList = (List<Map<String, Object>>) ((Map<String, Object>) comments
//										.get(i)).get("ext4");
//								Map<String, Object> refer = (Map<String, Object>) referList
//										.get(referList.size() - 1);
//								referComm.put(Constants.REFER_COMM_CONTENT,
//										refer.get("body"));
//								referComm.put(Constants.REFER_COMM_USERNAME,
//										refer.get("author"));
//								referComm.put(Constants.REFER_COMM_TIME,
//										refer.get("ext10"));
//							}
//							comm.put(Constants.REFER_COMMENTS, referComm);
//							list.add(comm);
//						}
//						parsedata.put(Constants.COMMENTS, list);
//					}
//					// nextpage
//					if (replyCnt - pageNum * pageSize > 0) {
//						String nextpage = getNextPage(url);
//						Map<String, Object> commentTask = new HashMap<String, Object>();
//						commentTask.put(Constants.LINK, nextpage);
//						commentTask.put(Constants.RAWLINK, nextpage);
//						commentTask.put(Constants.LINKTYPE, "newscomment");
//						List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
//						taskList.add(commentTask);
//						parsedata.put("tasks", taskList);
//						parsedata.put(Constants.NEXTPAGE, commentTask);
//					}
//				}
//			}
//		} catch (Exception e) {
//			LOG.error(e);
//		}
//	}

	private int getPageSize(String url) {
		Pattern iidPatter = Pattern.compile("&perpage=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return Integer.valueOf(match.group(1));
		}
		return 0;
	}

	private int getPageNum(String url) {
		Pattern iidPatter = Pattern.compile("&page=(\\d+)&");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return Integer.valueOf(match.group(1));
		}
		return 0;
	}

	private String getNextPage(String url) {
		return url.split("&page=")[0] + "&page=" + (getPageNum(url) + 1) + "&perpage=10";
	}
}
