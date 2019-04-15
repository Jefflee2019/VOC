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
 * @site:新华网（Nxinhuanet）
 * @author BFD_499
 *
 */
public class NxinhuanetCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NxinhuanetCommentJson.class);
	private static final Pattern PAGE_PATTERN = Pattern.compile("newsId=(\\d+)-");
	
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
			Map<String,Object> map = (Map<String, Object>) JsonUtils.parseObject(json);
			Matcher match = PAGE_PATTERN.matcher(url);
			List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
			parsedata.put("tasks",taskList);
			List<Map<String,Object>> commentList = new ArrayList<Map<String,Object>>();
			Map<String, Object> commentMap = new HashMap<String, Object>();
			if(map.containsKey("contentAll") && map.containsKey("totalRows"))
			{
				int replyCnt = (int) map.get("totalRows");
				Object contentAllObj = map.get("contentAll");
				if(contentAllObj == null || contentAllObj.equals("")){
					return;
				}
				List<Map<String,Object>> contentAll = (List<Map<String, Object>>) contentAllObj;
				if(!contentAll.isEmpty() && replyCnt > 0)
				{
					//获取评论
					for(Map<String, Object> content:contentAll)
					{
						Long time = (Long) content.get("commentTime");
						String mytime = ConstantFunc.normalTime(Long.toString(time/1000));
						commentMap.put("comment_content", content.get("content"));
						commentMap.put("comment_time", mytime);
						commentMap.put("username", content.get("userName"));
						if(content.get("upAmount") == null)
						{
							commentMap.put("up_cnt", 0);
						}
						else {
							commentMap.put("up_cnt",content.get("upAmount"));
						}
						commentList.add(commentMap);
					}
					//获取下一页
					if(map.containsKey("totalPage") && match.find() && map.containsKey("currentPage")){
						LOG.info("now executeParse num 6");
						int totalPage = (int) map.get("totalPage");
						int curPage = Integer.parseInt(match.group(1));
						if(totalPage > curPage)
						{
							int page = curPage + 1;
							String nextPage = url.replaceAll("newsId=" + match.group(1), "newsId=" + page);
							Map<String, Object> nextPageTask = new HashMap<String, Object>();
							nextPageTask.put("link", nextPage);
							nextPageTask.put("rawlink", nextPage);
							nextPageTask.put("linktype", "newscomment");
							taskList.add(nextPageTask);
							//下一页在json中放入的是个map
							parsedata.put("nextpage", nextPageTask);
						}
					}
				}
				parsedata.put("comments",commentList);
				parsedata.put("reply_cnt", replyCnt);
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
//	public static void main(String[] args) {
////		System.out.println(ConstantFunc.normalTime("1444788053"));
//	}
}
