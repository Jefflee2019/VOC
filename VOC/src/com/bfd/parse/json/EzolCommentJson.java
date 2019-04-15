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
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class EzolCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(EzolCommentJson.class);
	private static final Pattern PNUM = Pattern.compile("\\d+");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url="
								+ taskdata.get("url") + ".jsonUrl :"
								+ jsonData.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("EzolCommentJson json error url :" + url);
		}
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.TASKS, tasks);
		if(obj instanceof Map){
			Map<String, Object> map = (Map<String, Object>) obj;
			if(map.containsKey("list")){
				String html = map.get("list").toString();
				parseReplyHtmlCleaner(html, parseData);
			}
			if(map.containsKey("filter")){
				String replyContHtml = map.get("filter").toString();
				parseRepCountHtmlCleaner(replyContHtml, parseData);
			}
			//isEnd=0表示有下一页
			if(map.containsKey("isEnd") && "0".equals(map.get("isEnd").toString())){
				getNextpageUrl(parseData, unit, tasks);
			}
			
		}
	}
	
	private void parseReplyHtmlCleaner(String html, Map<String, Object> parseData){
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(html);
		String xpath = "//div[@class=\"comments-item\"]";
		Object[] objs = null;
		List<Map<String, Object>> commList = new ArrayList<>();
		parseData.put(Constants.COMMENTS, commList);
		try {
			objs = tn.evaluateXPath(xpath);
			for(Object obj : objs){
				Map<String, Object> commMap = new HashMap<>();
				TagNode objNode = (TagNode) obj;
				TagNode userNode = (TagNode) objNode.evaluateXPath("//div[@class=\"comments-user\"]")[0];
				//据标签名字不递归寻找
				//用户名 
				TagNode userName = (TagNode) userNode.evaluateXPath("//a[@class=\"name\"]")[0];
				commMap.put(Constants.COMMENTER_NAME, userName.getText().toString());
				//评论时间
				TagNode replyTimeNode = (TagNode) objNode.evaluateXPath("//div[@class=\"comment-list-content\"]")[0];
				TagNode replyTimeNode1 = (TagNode) objNode.evaluateXPath("//div[@class=\"function-handle clearfix\"]")[0];
				TagNode replyTimeNode2 = (TagNode) objNode.evaluateXPath("//span[@class=\"date\"]")[0];
				String string = replyTimeNode2.getText().toString().replaceAll("发表于：", "").trim();
				commMap.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(string));
				//评分
				//没有评分时候将会出错
				TagNode score = (TagNode) objNode.evaluateXPath("//div[@class=\"single-score\"]")[0];
				TagNode score1 = (TagNode) score.evaluateXPath("//div[@class=\"score clearfix\"]")[0];
				commMap.put(Constants.SCORE, score1.getText().toString().trim());
				//评论内容
				TagNode title = (TagNode) replyTimeNode.evaluateXPath("//div[@class=\"title\"]")[0];
				TagNode title1 = (TagNode) replyTimeNode.evaluateXPath("//a")[0];
				StringBuilder contents = new StringBuilder();
				contents.append(title1.getText().toString().trim());
				//展开全文的结构
//				if (replyTimeNode.evaluateXPath("//div[@class=\"_j_CommentContent comment-height-limit\"]//div[@class=\"content-inner\"]").length > 0) {
//					TagNode contentObjs1 = (TagNode) replyTimeNode.evaluateXPath("//div[@class=\"_j_CommentContent comment-height-limit\"]//div[@class=\"content-inner\"]")[0];
//					contents.append(" ").append(contentObjs1.getText().toString().trim());
//				}
				//查看全文的结构
				//words优点，缺点
				if (replyTimeNode.evaluateXPath("//div[@class=\"words\"]").length > 0) {
					Object[] contentObjs1 = (Object[]) replyTimeNode.evaluateXPath("//div[@class=\"words\"]");
					for (Object tagNode : contentObjs1) {
						TagNode node = (TagNode) tagNode;
						contents.append(" ").append(node.getText().toString().trim());
					}
				}
				//总结
				if (replyTimeNode.evaluateXPath("//div[@class=\"words-article\"]").length > 0) {
					TagNode contentObjs1 = (TagNode) replyTimeNode.evaluateXPath("//div[@class=\"words-article\"]")[0];
					contents.append(" ").append(contentObjs1.getText().toString().trim());
				}
				commMap.put(Constants.COMMENT_CONTENT, contents.toString());
				//评论数
				TagNode replyCntNode = (TagNode) replyTimeNode1.evaluateXPath("//a[@class=\"_j_review_reply\"]")[0];
				parseByReg(commMap, Constants.COMMENT_REPLY_CNT, replyCntNode.getText().toString(), PNUM);
				//点赞数
				TagNode upCntNode = (TagNode) replyTimeNode1.evaluateXPath("//a[@class=\"_j_review_vote\"]")[0];
				parseByReg(commMap, Constants.UP_CNT, upCntNode.getText().toString(), PNUM); 
				commList.add(commMap);
			}
		} catch (XPatherException e) {
			LOG.error("EzolCommentJson reprocess error url");
		}
	}
	
	
	
	private void parseRepCountHtmlCleaner(String replyContHtml, Map<String, Object> parseData){
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(replyContHtml);
		String xpath = "//div[@class=\"radio-box clearfix\"]";
		Object[] objs = null;
		try {
			objs = tn.evaluateXPath(xpath);
			TagNode objNode = (TagNode) objs[0];
			Object[] labels = objNode.evaluateXPath("//label");
			if(labels.length == 7){
				String totalCnt = ((TagNode) labels[0]).getElementsByName("em", false)[0].getText().toString().trim();
				String essenceCnt = ((TagNode) labels[1]).getElementsByName("em", false)[0].getText().toString().trim();
				String withpicCnt = ((TagNode) labels[2]).getElementsByName("em", false)[0].getText().toString().trim();
				String goodCnt = ((TagNode) labels[3]).getElementsByName("em", false)[0].getText().toString().trim();
				String general_cnt = ((TagNode) labels[4]).getElementsByName("em", false)[0].getText().toString().trim();
				String poorCnt = ((TagNode) labels[5]).getElementsByName("em", false)[0].getText().toString().trim();
				String againCnt = ((TagNode) labels[6]).getElementsByName("em", false)[0].getText().toString().trim();
				parseByReg(parseData, Constants.REPLY_CNT, totalCnt, PNUM);
				parseByReg(parseData, Constants.BEST_CNT, essenceCnt, PNUM);
				parseByReg(parseData, Constants.WITHPIC_CNT, withpicCnt, PNUM);
				parseByReg(parseData, Constants.GOOD_CNT, goodCnt, PNUM);
				parseByReg(parseData, Constants.GENERAL_CNT, general_cnt, PNUM);
				parseByReg(parseData, Constants.POOR_CNT, poorCnt, PNUM);
				parseByReg(parseData, Constants.AGAIN_CNT, againCnt, PNUM);
			}
		} catch (XPatherException e) {
			LOG.error("EzolCommentJson reprocess error url");
		}
	}
	
	public void parseByReg(Map<String, Object> dataMap, String conststr, String resultStr, Pattern p){
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group();
		}
		else resultStr = "0";
		dataMap.put(conststr, Integer.valueOf(resultStr));
	}
	
	private void getNextpageUrl(Map<String, Object> parseData, ParseUnit unit, List<Map<String, Object>> taskList) {
		String url = (String) unit.getUrl();
		Pattern pat = Pattern.compile("(.*page=)(\\d+).html");
		Matcher mch = pat.matcher(url);
		
		if(mch.find()){
			int pageNo = Integer.valueOf(mch.group(2));
			Map<String,Object> nexpageTask = new HashMap<String,Object>();
			String nextpageUrl = mch.group(1) + (pageNo + 1) + ".html";
			nexpageTask.put(Constants.LINK, nextpageUrl);
			nexpageTask.put(Constants.RAWLINK, nextpageUrl);
			nexpageTask.put(Constants.LINKTYPE, "eccomment");		
			taskList.add(nexpageTask);
			parseData.put(Constants.NEXTPAGE, nexpageTask);
			parseData.put(Constants.TASKS, taskList);
		}
		
	}
}
