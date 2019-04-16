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

public class NpcpopCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NpcpopCommentJson.class);
	private static final Pattern PAGEPATTERN = Pattern.compile("&page=(\\d+)");
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
//			LOG.info("url:"+data.getUrl()+".json is "+json);
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
//				json = json.substring(0,index - 6);
//				LOG.info("url:"+data.getUrl()+".correct json is "+json);
				
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
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
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:"+taskdata.get("url"));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata,
			String json, String url, ParseUnit unit){
		try {
			Map<String,Object> map = (Map<String, Object>) JsonUtils.parseObject(json);
			Matcher match = PAGEPATTERN.matcher(url);
			List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> commentList = new ArrayList<Map<String,Object>>();
			int supportCount = 0;
			int opposeCount =0;
			String content = "";
			String commentName = "";
			if(map.containsKey("listData"))
			{
				Map<String,Object> listData = (Map<String, Object>) map.get("listData");
				if(!listData.isEmpty() && listData.containsKey("comments"))
				{
					ArrayList<Map<String,Object>> comments = (ArrayList<Map<String, Object>>) listData.get("comments");
					if(!comments.isEmpty())
					{
						for(Map<String, Object> comment:comments)
						{
							Map<String, Object> commentMap = new HashMap<String, Object>();
							supportCount = (int) comment.get("support_count");
							opposeCount = (int) comment.get("oppose_count");
							content = (String) comment.get("content");
							Long creatTime = (Long) comment.get("create_time");
							String commentTime = ConstantFunc.normalTime(Long.toString(creatTime/1000));
							Map<String,Object> passport = (java.util.Map<String, Object>) comment.get("passport");
							commentName = (String) passport.get("nickname");
							commentMap.put("comment_content", content);
							commentMap.put("comment_time", commentTime);
							commentMap.put("username", commentName);
							commentMap.put("up_cnt", supportCount);
							commentMap.put("down_cnt", opposeCount);
							commentList.add(commentMap);
						}
					}
				}
				//判断下一页
				if(listData.containsKey("total_page_no") && match.find())
				{
					int totalPage = (int) listData.get("total_page_no");
					int curPage = Integer.parseInt(match.group(1));
					if(totalPage > curPage)
					{
						int page = curPage + 1;
						String nextPage = url.replaceAll("&page=" + match.group(1), "&page=" + page);
						Map<String, Object> nextpageTask = new HashMap<String, Object>();
						nextpageTask.put("link", nextPage);
						nextpageTask.put("rawlink", nextPage);
						nextpageTask.put("linktype", "newscomment");
						taskList.add(nextpageTask);
				}
			}
		} 
			parsedata.put("comments",commentList);
			parsedata.put("tasks",taskList);
		}catch (Exception e) {
			LOG.warn("excuteParse error");
		}
	}
}
