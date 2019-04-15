
package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * 站点：华为商城 作用：处理商品评论
 * 
 * @author bfd_04
 * 
 */
public class NautohomeCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NautohomeCommentJson.class);
	private static final Pattern PAGE_PATTERN = Pattern.compile("page=(\\d+)");

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		String json = null;
		String url = unit.getUrl();
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData obj : dataList) {
			if (null != obj) {
				JsonData data = (JsonData) obj;
				if (!data.downloadSuccess()) {
					continue;
				}
				unit.setPageEncode("utf8");
				json = TextUtil.getUnzipJson(data, unit);
				// LOG.info("url:"+data.getUrl()+".json is "+json);
				try {
					// 将ajax数据转化为json数据格式
					if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
							&& (json.indexOf("[") < json.indexOf("{"))) {
						json = json.substring(json.indexOf("["),
								json.lastIndexOf("]") + 1);
					} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
						json = json.substring(json.indexOf("{"),
								json.lastIndexOf("}") + 1);
					}
					// LOG.info("url:"+data.getUrl()+".correct json is "+json);
				} catch (Exception e) {
					LOG.warn("json :" + json + ".url:" + taskdata.get("url"));
					LOG.warn(
							"AMJsonParser exception, taskdata url="
									+ taskdata.get("url") + ".jsonUrl :"
									+ data.getUrl(), e);
				}
			}
			executeParse(parsedata, json, url, unit);
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Map<String, Object> jsonMap = null;
		try {
			LOG.info("original json is: " + json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put("tasks", taskList);
			jsonMap = (Map<String, Object>) JsonUtils.parseObject(json);
		} catch (Exception e) {
			LOG.error("NautohomeCommentJson executeParse error " + url);
		}
		int commentcountall = (int) jsonMap.get("commentcountall");
		int count = 50;//单页条数
		int page = (int) jsonMap.get("page");
		//评论数不为0
		if(commentcountall != 0){
			//评论内容处理
			List commentlist = (List) jsonMap.get("commentlist");
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
			for (Object object : commentlist) {
				Map obj = (Map) object;
				Map referComments = new HashMap();//回复评论
				Map comment = new HashMap();//评论
				String comment_content = (String) obj.get("RContent");//内容
				String comment_time = (String) obj.get("replydate");//回复日期
				comment_time = ConstantFunc.convertTime(comment_time);
				int up_cnt = (int) obj.get("RUp");//顶！d=====(￣▽￣*)b
				String username = (String) obj.get("RMemberName");//作者
				int replyfloor = (int) obj.get("RFloor");//楼层
				comment.put(Constants.COMMENT_CONTENT, comment_content);
				comment.put(Constants.COMMENT_TIME, comment_time);
				comment.put(Constants.UP_CNT, up_cnt);
				comment.put(Constants.USERNAME, username);
				comment.put(Constants.REPLYFLOOR, replyfloor);
				//评论包含回复，拼接到评论后
				if (obj.containsKey("Quote")) {
					Map quote = (Map) obj.get("Quote");
					String referComment_replydate = (String) quote.get("replydate");
					referComment_replydate = ConstantFunc.convertTime(referComment_replydate);
					referComments.put(Constants.REFER_COMM_TIME, referComment_replydate);
					referComments.put(Constants.REFER_COMM_CONTENT,quote.get("RContent"));
					referComments.put(Constants.REFER_COMM_USERNAME,quote.get("RMemberName"));
					referComments.put(Constants.REFER_REPLYFLOOR,quote.get("RFloor"));
					comment.put(Constants.REFER_COMMENTS, referComments);
				}
				list.add(comment);
			}
			parsedata.put(Constants.COMMENTS, list);
			int totalPage = commentcountall / count + 1; //总页数
			//评论下一页任务
			if (page < totalPage) {
				nextTask(parsedata, url ,page);
			}
		}
		
	}
	
	/**
	 * 组装task
	 * @param parsedata
	 * @param nextPage
	 */
	@SuppressWarnings("unchecked")
	private void initTask(Map<String, Object> parsedata, String nextPage) {
		List<Map<String, Object>> taskList = (List<Map<String, Object>>) parsedata.get("tasks");
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextPage);
		nextpageTask.put("rawlink", nextPage);
		nextpageTask.put("linktype", "newscomment");
		
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextpageTask);
		parsedata.put("tasks", taskList);
	}
	
	/**
	 * 用于下一页
	 * @param parsedata
	 * @param url 本页url
	 * @param flag 判断是否需能生成下一页
	 */
	private void nextTask(Map<String, Object> parsedata, String url, int page){
		Matcher match = PAGE_PATTERN.matcher(url);
		if (match.find()) {
			page += 1;
			String nextPage = url.replaceAll(match.group(0),"page=" + page);
			initTask(parsedata, nextPage);
		}
	}
}
