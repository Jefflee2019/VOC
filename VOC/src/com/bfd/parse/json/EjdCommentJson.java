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

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：京东 
 * 主要功能： 
 * 1.获取评论信息 
 * 2.获取客户评价标签
 * @author bfd_03
 *
 */
public class EjdCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EjdCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		String json = null;
		String url = unit.getUrl();
		
		// 遍历dataList
		for (Object obj : dataList) {
			if(null != obj) {
				JsonData data = (JsonData) obj;
				// 判断该ajax数据是否下载成功
				if (!data.downloadSuccess()) {
					continue;
				}
				// 解压缩ajax数据
				json = TextUtil.getUnzipJson(data, unit);
				
				try {
					if(url.indexOf("productpage")>0){
						// 将ajax数据转化为json数据格式
						if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
								&& (json.indexOf("[") < json.indexOf("{"))) {
							json = json.substring(json.indexOf("["),
									json.lastIndexOf("]") + 1);
						} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
							json = json.substring(json.indexOf("{"),
									json.lastIndexOf("}") + 1);
						}
						
					}
				// 执行从json数据中提取自己感兴趣的数据
					if(url.indexOf("productpage")>0){ 
						executeParse(parsedata, json, url, unit); 
					} else {
						parsecode = 0;
					}

				} catch (Exception e) {
					LOG.warn(
							"AMJsonParse exception,taskdat url="
									+ taskdata.get("url") + ".jsonUrl:"
									+ data.getUrl(), e);
				}
			}
			else{
				//当data为空，在发一次本页url的任务
				Map<String, Object> nexpageTask = new HashMap<String, Object>();
				List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
				nexpageTask.put(Constants.LINK, unit.getUrl());
				nexpageTask.put(Constants.RAWLINK, unit.getUrl());
				nexpageTask.put(Constants.LINKTYPE, "eccomment");		
				taskList.add(nexpageTask);
				parsedata.put(Constants.TASKS, taskList);
				parsedata.put(Constants.NEXTPAGE, nexpageTask);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
//			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		boolean isNextPage = true;
		//评论页：http://s.club.jd.com/productpage/p-1182292-s-0-t-3-p-0.html?callback=fetchJSON_comment
		if(url.indexOf("productpage")>0){
			Object obj = null;
			List commentDataList = new ArrayList();
			parsedata.put(Constants.COMMENTS, commentDataList);	
			try {
				if(json != null && !json.isEmpty()){
					obj = JsonUtil.parseObject(json);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				LOG.error("jsonparser reprocess error parseObject exception");
			}
			if(obj instanceof Map){
				Map map = (Map)obj;
				//总体评价信息
				if(map.containsKey("productCommentSummary")){
					Map proComSumMap = (HashMap)map.get("productCommentSummary");
					
					parsedata.put(Constants.REPLY_CNT, proComSumMap.get("commentCount"));//总评数
					parsedata.put(Constants.WITHPIC_CNT, proComSumMap.get("showCount"));//有图片的评价数量
					parsedata.put(Constants.GOOD_CNT, proComSumMap.get("goodCount"));// 好评数
					parsedata.put(Constants.GOOD_RATE, proComSumMap.get("goodRateShow"));//好评率 goodRate=0.952  goodRateShow=95 
					parsedata.put(Constants.GENERAL_CNT, proComSumMap.get("generalCount"));//中评数
					parsedata.put(Constants.GENERAL_RATE, proComSumMap.get("generalRateShow"));//中评率
					parsedata.put(Constants.POOR_CNT, proComSumMap.get("poorCount"));//差评数
					parsedata.put(Constants.POOR_RATE, proComSumMap.get("poorRateShow"));//差评率
					
				}
				//买家印象
				if(map.containsKey("hotCommentTagStatistics")){
					List<Map<String,Object>> hotCommentTag = (List<Map<String,Object>>) map.get("hotCommentTagStatistics");
					StringBuffer sb = new StringBuffer();
					for(Map<String, Object> temp:hotCommentTag){
						sb.append(temp.get("name")+":");
						sb.append(temp.get("count")+",");
					}
					if(sb.length() > 0){
						parsedata.put(Constants.BUYER_IMPRESSION, sb.substring(0, sb.length()-1));//买家印象
					}
					
				}
				//评论信息
				if(map.containsKey("comments")){
					List commentsList = (ArrayList)map.get("comments");
					for(int i=0; i<commentsList.size(); i++){
						Map commentMap = (HashMap)commentsList.get(i);
						Map temp = new HashMap();
						
						temp.put(Constants.COMMENT_CONTENT, commentMap.get("content"));
						temp.put(Constants.GUID, commentMap.get("guid"));
						temp.put(Constants.COMMENT_TIME, commentMap.get("creationTime"));
						Object tempObj = commentMap.get("images");
						
						temp.put(Constants.COMMENT_REPLY_CNT, commentMap.get("replyCount"));
						temp.put(Constants.FAVOR_CNT, commentMap.get("usefulVoteCount"));
						temp.put(Constants.SCORE, commentMap.get("score"));
						temp.put(Constants.BUY_TYPE, commentMap.get("productSize"));
						temp.put(Constants.COLOR, commentMap.get("productColor"));
						temp.put(Constants.COMMENTER_NAME, commentMap.get("nickname"));
						temp.put(Constants.COMMENTER_LEVEL, commentMap.get("userLevelName"));
						temp.put(Constants.LOCATION, commentMap.get("userProvince"));
						temp.put(Constants.BUY_TIME, commentMap.get("referenceTime"));
						
//						 //increment calculation
//						if(Integer.parseInt(commentMap.get("usefulVoteCount").toString()) > 0 ||
//								Integer.parseInt(commentMap.get("replyCount").toString()) > 0) {
//							isNextPage = true;
//						}
						//晒图列表
						List imagesList = new ArrayList();
						if (tempObj != null && tempObj instanceof List) {
							for (Object tempImgObj : (List) tempObj) {
								imagesList.add(((Map) tempImgObj).get("imgUrl"));
							}
						}
						if (imagesList != null && !imagesList.isEmpty()) {
							temp.put(Constants.COMMENT_IMG, imagesList);
						}
						
						//评价标签
						List<Map<String, Object>> commentTagsList = (List<Map<String, Object>>) commentMap.get("commentTags");
						StringBuffer sb = new StringBuffer();
						for (int j = 0; commentTagsList != null	&& j < commentTagsList.size(); j++) {
							sb.append(commentTagsList.get(j).get("name") + ",");
						}
						
						if (sb.length() > 0) {
							temp.put(Constants.COMMENT_TAG,	sb.substring(0, sb.length() - 1));
						}
						
						//评论回复
						List<Map<String, Object>> repliesList = (List<Map<String, Object>>) commentMap.get("replies");
						List<Map<String, Object>> commentReplyList = new ArrayList<Map<String, Object>>();
						for (int k = 0; repliesList != null && k < repliesList.size(); k++) {
 							Map commentReplyMap = repliesList.get(k);
							Map tempCommentReplyMap = new HashMap();
							
							tempCommentReplyMap.put(Constants.COMMENT_REPLY_NAME, commentReplyMap.get("userLevelName"));
							tempCommentReplyMap.put(Constants.COMMENT_REPLY_CONTENT, commentReplyMap.get("content"));
							tempCommentReplyMap.put(Constants.COMMENT_REPLY_TIME, commentReplyMap.get("creationTime"));
							
							commentReplyList.add(tempCommentReplyMap);
						}
						if (commentReplyList != null && !commentReplyList.isEmpty()) {
							temp.put(Constants.COMMENT_REPLY, commentReplyList);
						}
						
						commentDataList.add(temp);
					}
					parsedata.put(Constants.MSG_TYPE, "comment");
					
				}
			}
			//拼接下一页链接
			getCommentNextpageUrl(parsedata, unit,isNextPage);
			
		 }else if(url.indexOf("allconsultations")>0){//咨询页：http://club.jd.com/allconsultations/1182292-1-1.html
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(json);
						
			try {
				List<Map<String,Object>> consultList  = new ArrayList<Map<String,Object>>();
				
				TagNode referListNode = (TagNode) root.evaluateXPath("//div[@class='Refer_List']")[0];
				Object[]  referObj= referListNode.evaluateXPath("/div");
				for (int i = 0; referObj != null && i < referObj.length; i++) {
					Map<String,Object> temp = new HashMap<String,Object>();				
					TagNode referNode = (TagNode) referObj[i];
					
					Object[] objEvaluateXPath = null;
					//咨询人和咨询时间
					objEvaluateXPath = referNode.evaluateXPath("/div[@class='r_info']");
					if(objEvaluateXPath!=null){
						TagNode rInfoNode=(TagNode) objEvaluateXPath[0];
						String rInfo = rInfoNode.getText().toString().replace("网友：", "");
						if(rInfo.contains("&nbsp;")){
							temp.put(Constants.CONSULTER_NAME, rInfo.substring(0,rInfo.indexOf("&nbsp;")).trim());
							temp.put(Constants.CONSULT_TIME, rInfo.substring(rInfo.lastIndexOf(";")+1,rInfo.length()).trim());
						}
					}

					//咨询内容
					objEvaluateXPath = referNode.evaluateXPath("/dl[@class='ask']");
					if(objEvaluateXPath!=null){
						TagNode askNode=(TagNode) objEvaluateXPath[0];
						String askInfo = askNode.getText().toString().replace("咨询内容：", "");
						temp.put(Constants.CONSULT_CONTENT, askInfo.trim());
					}
					
					//咨询回复
					objEvaluateXPath = referNode.evaluateXPath("/dl[@class='answer']");
					if(objEvaluateXPath!=null){
						TagNode answerNode=(TagNode) objEvaluateXPath[0];
						String answerInfo = answerNode.getText().toString().replace("京东回复：", "");
						temp.put(Constants.CONSULT_REPLYS, answerInfo.trim());
					}
					consultList.add(temp);
				}
				
				parsedata.put(Constants.CONSULTATIONS, consultList);
				parsedata.put(Constants.MSG_TYPE, "consult");
				
			} catch (Exception e) {
				LOG.warn("htmlcleaner parse error");
			}
			//拼接下一页链接
			getConsultationsNextpageUrl(parsedata, url, json);
		}
		
		

	}

	/**
	 * 拼接咨询页下一页链接
	 * @param parsedata
	 * @param unit
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getConsultationsNextpageUrl(Map parsedata, String url, String json) {
		//class='next'		
		Pattern pattern = Pattern.compile("(http://club.jd.com/allconsultations/\\d+-)(\\d+)(-\\d+\\.html)");
		Matcher matcher = pattern.matcher(url);
		String nextpageUrl = "";
		if(matcher.find()){
			int pageNo = Integer.parseInt(matcher.group(2)) + 1; 
			nextpageUrl = matcher.group(1) + pageNo + matcher.group(3);
		}
		
		
		List taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList();
		}
		if(json.contains("class='next'")){	
			if(!nextpageUrl.equals("")){
				Map nexpageTask = new HashMap();
				nexpageTask.put(Constants.LINK, nextpageUrl);
				nexpageTask.put(Constants.RAWLINK, nextpageUrl);
				nexpageTask.put(Constants.LINKTYPE, "eccomment");		
				taskList.add(nexpageTask);
				
				parsedata.put(Constants.NEXTPAGE, nexpageTask);
			}			
		}
		parsedata.put(Constants.TASKS, taskList);
	}

	/**
	 * 拼接评论页下一页链接
	 * @param parsedata
	 * @param unit
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getCommentNextpageUrl(Map parsedata, ParseUnit unit, boolean isNextPage) {
		String url = unit.getUrl();
		int pageNo = -1;
		Pattern pattern = Pattern.compile("(http://s.club.jd.com/productpage/p-\\d+-s-0-t-3-p-)(\\d+)\\.html");
		Matcher matcher = pattern.matcher(url);
		String nextpageUrl = "";
		
		if(matcher.find()){
			int currentPageNo = Integer.parseInt(matcher.group(2));
//			if(currentPageNo > 150 && currentPageNo < 15000 && fCommentCnt/10 > 15000) {
//				pageNo =  15000; 
//			} else {
//				pageNo =  currentPageNo + 1;
//			}
			pageNo =  currentPageNo + 1;
			nextpageUrl = matcher.group(1) + pageNo + ".html?callback=fetchJSON_comment";
		}
		
		float fCommentCnt = 0l;
		Object oCommentCnt = parsedata.get(Constants.REPLY_CNT);
		
		if(oCommentCnt != null && !oCommentCnt.equals("")){
			fCommentCnt = (int) parsedata.get(Constants.REPLY_CNT);
		}
		
		List<Map<String, String>> commentList = (List<Map<String, String>>) parsedata
				.get(Constants.COMMENTS);
		if (pageNo == 1 && commentList != null && !commentList.isEmpty()) {
			Map<String, String> temp = commentList.get(0);
			commentList.set(0, commentList.get((commentList.size() - 2) < 0 ? 0
					: (commentList.size() - 2)));
			commentList
					.set((commentList.size() - 2) < 0 ? 0
							: (commentList.size() - 2), temp);
		}

		List taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList();
		}
//		if(url.contains("p-1487776-s")||url.contains("p-1333526-s")||url.contains("p-1779676-s")||url.contains("p-1487770-s")) {
//			fCommentCnt = 5;
//		}
		if (oCommentCnt==null || oCommentCnt.equals("") || (pageNo > 0 && pageNo < fCommentCnt / 10)) {
			if(isNextPage) {
				Map nexpageTask = new HashMap();
							
				nexpageTask.put(Constants.LINK, nextpageUrl);
				nexpageTask.put(Constants.RAWLINK, nextpageUrl);
				nexpageTask.put(Constants.LINKTYPE, "eccomment");		
				taskList.add(nexpageTask);
				
				parsedata.put(Constants.NEXTPAGE, nexpageTask);
			}
		}
		parsedata.put(Constants.TASKS, taskList);
	}

}
