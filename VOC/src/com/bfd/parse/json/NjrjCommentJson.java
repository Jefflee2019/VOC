package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * @site 金融界（Njrj）
 * @author BFD_499
 *
 */
public class NjrjCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NjrjCommentJson.class);
	private static final Pattern PAGE_PATTERN = Pattern.compile("page=(\\d+)");
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			LOG.info("url:"+data.getUrl()+".json is "+json);
			// json = TextUtil.removeAllHtmlTags(json);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				} 
//				if(json.contains("/*") || json.contains("*/"))
//					json = json.replaceAll("\\*\\/", "").replaceAll("\\/\\*", "");
//				int index = json.lastIndexOf("],");
//				System.out.println(">>>>>" + index);
//				json = json.substring(0,index - 6);
				LOG.info("url:"+data.getUrl()+".correct json is "+json);
				
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
				parsecode = Constants.JSONPROCESS_FAILED;
				LOG.warn(
						"AMJsonParser exception, taskdata url="
								+ taskdata.get("url")
								+ ".jsonUrl :" + data.getUrl(), e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {

			result.setData(parsedata);
			result.setParsecode(parsecode);
			LOG.info("url:"+taskdata.get("url")+"after jsonparser parsedata is "+parsedata);
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata,
		String json, String url, ParseUnit unit){
		try {
			Map<String,Object> originalMap = (Map<String, Object>) JsonUtils.parseObject(json);
			Matcher match = PAGE_PATTERN.matcher(url);
			List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
			parsedata.put("tasks",taskList);
			List<Map<String,Object>> commentList = new ArrayList<Map<String,Object>>();
			Map<String, Object> commentMap = new HashMap<String, Object>();
			int currentPageNum = 0;
			int totalPageNum = 0;
			int replyCnt = 0;
			if(originalMap != null && !originalMap.isEmpty()) {
				if(originalMap.containsKey("page")) {
					currentPageNum = Integer.parseInt(originalMap.get("page").toString());
				}
				if(originalMap.containsKey("totalPage")) {
					totalPageNum = Integer.parseInt(originalMap.get("totalPage").toString());
				}
				if(originalMap.containsKey("totalCount")) {
					replyCnt = Integer.parseInt(originalMap.get("totalCount").toString());
				}
				if(originalMap.containsKey("listData"))
				{
					List OriCommList = (List) originalMap.get("listData");
					if(OriCommList == null || OriCommList.isEmpty()){
						return;
					}
	//						//获取评论
					for(Object obj : OriCommList)
					{	
						List tempList = (List)obj;
						if(tempList !=null && tempList.size() == 1) {
							Map tempMap = (Map)tempList.get(0);
							if(tempMap != null && !tempMap.isEmpty()) {
								Long time = (Long) tempMap.get("ctime");
								String mytime = ConstantFunc.normalTime(Long.toString(time/1000));
								commentMap.put(Constants.COMMENT_CONTENT, tempMap.get("content"));
								commentMap.put(Constants.COMMENT_TIME, mytime);
								commentMap.put(Constants.USERNAME, tempMap.get("senderName"));
							}
						}
						if(tempList !=null && tempList.size() > 1) { 
							Map tempMap1 = (Map)tempList.get(tempList.size()-2);
							Map tempMap2 = (Map)tempList.get(tempList.size()-1);
							if(tempMap1 != null && !tempMap1.isEmpty() && tempMap2 != null && !tempMap2.isEmpty()) {
								Long time = (Long) tempMap1.get("ctime");
								String mytime = ConstantFunc.normalTime(Long.toString(time/1000));
								commentMap.put(Constants.COMMENT_CONTENT, tempMap1.get("content"));
								commentMap.put(Constants.COMMENT_TIME, mytime);
								commentMap.put(Constants.USERNAME, tempMap1.get("senderName"));
								
								Map<String, Object> subCommentMap = new HashMap<String, Object>();
								
								Long subTime = (Long) tempMap2.get("ctime");
								String subMytime = ConstantFunc.normalTime(Long.toString(subTime/1000));
								subCommentMap.put(Constants.COMMENT_REPLY_CONTENT, tempMap2.get("content"));
								subCommentMap.put(Constants.COMMENT_REPLY_TIME, subMytime);
								subCommentMap.put(Constants.COMMENT_REPLY_NAME, tempMap2.get("senderName"));
								
								commentMap.put(Constants.COMMENT_REPLY, subCommentMap);
							}
						}
						commentList.add(commentMap);
					}
					//获取下一页
					if(currentPageNum < totalPageNum){
						int newPage = currentPageNum + 1;
						String nextPageUrl = url.replaceAll("page=" + newPage, "page=" + newPage);
						Map<String, Object> nextPageTask = new HashMap<String, Object>();
						nextPageTask.put("link", nextPageUrl);
						nextPageTask.put("rawlink", nextPageUrl);
						nextPageTask.put("linktype", "newscomment");
						taskList.add(nextPageTask);
						//下一页在json中放入的是个map
						parsedata.put("nextpage", nextPageTask);
						}
					}
					parsedata.put("comments",commentList);
					parsedata.put("reply_cnt", replyCnt);
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
	}
}
