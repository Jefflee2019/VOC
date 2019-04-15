package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Neastmoney
 * 
 * 功能：分离评论
 * 
 * @author bfd_06
 * 
 */
public class NeastmoneyCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NeastmoneyCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			int indexA = json.indexOf("(");
			int indexB = json.lastIndexOf(")");
			if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
				json = json.substring(indexA + 1, indexB);
			}
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil
					.parseObject(json);
			List<Map<String, Object>> reL = (List<Map<String, Object>>) result.get("re");
			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> reM:reL){
				Map<String, Object> comment = new HashMap<String, Object>();
				String userName = (String) reM.get("reply_ip");
				if(userName.equals("")){
					Map<String, Object> reply_userM = (Map<String, Object>) reM.get("reply_user");
					userName = (String) reply_userM.get("user_nickname");
				}
				comment.put(Constants.USERNAME, userName);
				comment.put(Constants.COMMENT_TIME, reM.get("reply_publish_time"));
				comment.put(Constants.COMMENT_CONTENT, reM.get("reply_text"));
				comment.put(Constants.FAVOR_CNT, reM.get("reply_like_count"));
				comments.add(comment);
			}
			parsedata.put("comments", comments);
			/**
			 * 判断是否含有下一页 有则给出
			 */
			int pageNum = matchPageNum("thispage=(\\d+)", url); //当前页码
			int count = (int)result.get("count"); //评论总数
			if(pageNum*20<count){
				String nextCommentUrl = url.replace("thispage="+pageNum, "thispage="+(pageNum+1));
				Map<String, Object> task = new HashMap<String, Object>();
				task.put("link", nextCommentUrl);
				task.put("rawlink", nextCommentUrl);
				task.put("linktype", "newscomment");
				tasks.add(task);
				parsedata.put(Constants.NEXTPAGE, task);
			}

//			 System.out.println(parsedata.toString());
			
		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
	}

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 1;
	}

}
